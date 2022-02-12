package com.hoatv.ext.endpoint.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoatv.ext.endpoint.dtos.EndpointResponseVO;
import com.hoatv.ext.endpoint.dtos.EndpointSettingVO;
import com.hoatv.ext.endpoint.dtos.MetadataVO;
import com.hoatv.ext.endpoint.dtos.MetadataVO.ColumnMetadataVO;
import com.hoatv.ext.endpoint.models.EndpointExecutionResult;
import com.hoatv.ext.endpoint.models.EndpointResponse;
import com.hoatv.ext.endpoint.models.EndpointSetting;
import com.hoatv.ext.endpoint.repositories.ExtEndpointResponseRepository;
import com.hoatv.ext.endpoint.repositories.ExtEndpointSettingRepository;
import com.hoatv.ext.endpoint.repositories.ExtExecutionResultRepository;
import com.hoatv.ext.endpoint.utils.DecryptUtils;
import com.hoatv.ext.endpoint.utils.SaltGeneratorUtils;
import com.hoatv.fwk.common.services.*;
import com.hoatv.fwk.common.services.GenericHttpClientPool.ExecutionTemplate;
import com.hoatv.fwk.common.services.HttpClientService.HttpMethod;
import com.hoatv.fwk.common.services.HttpClientService.RequestParams.RequestParamsBuilder;
import com.hoatv.fwk.common.ultilities.ObjectUtils;
import com.hoatv.task.mgmt.entities.TaskEntry;
import com.hoatv.task.mgmt.services.TaskMgmtService;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Service
public class ExtRestDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtRestDataService.class);
    public static final HttpClientService HTTP_CLIENT_SERVICE = HttpClientService.INSTANCE;

    private final ExtEndpointSettingRepository extEndpointSettingRepository;
    private final ExtEndpointResponseRepository endpointResponseRepository;
    private final ExtExecutionResultRepository extExecutionResultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExtRestDataService(ExtEndpointSettingRepository extEndpointSettingRepository,
                              ExtEndpointResponseRepository endpointResponseRepository,
                              ExtExecutionResultRepository extExecutionResultRepository) {
        this.extEndpointSettingRepository = extEndpointSettingRepository;
        this.endpointResponseRepository = endpointResponseRepository;
        this.extExecutionResultRepository = extExecutionResultRepository;
    }

    public List<EndpointSettingVO> getAllExtEndpoints(String application) {
        List<EndpointSetting> endpointSettings = new ArrayList<>();
        if (StringUtils.isEmpty(application)) {
            List<EndpointSetting> extEndpointSettingRepositoryAll = extEndpointSettingRepository.findAll();
            endpointSettings.addAll(extEndpointSettingRepositoryAll);
        } else {
            List<EndpointSetting> endpointConfigsByApplication = extEndpointSettingRepository.findEndpointConfigsByApplication(
                    application);
            endpointSettings.addAll(endpointConfigsByApplication);
        }
        return endpointSettings.stream().map(EndpointSetting::toEndpointConfigVO).collect(Collectors.toList());
    }

    public void addExtEndpoint(EndpointSettingVO endpointSettingVO) {
        EndpointSetting endpointSetting = EndpointSetting.fromEndpointConfigVO(endpointSettingVO);
        HttpMethod extSupportedMethod = HttpMethod.fromString(endpointSetting.getMethod());
        ObjectUtils.checkThenThrow(Objects::isNull, extSupportedMethod, HttpMethod.INVALID_SUPPORTED_METHOD);

        extEndpointSettingRepository.save(endpointSetting);
        TaskMgmtService<Object> taskMgmtExecutorV1 = new TaskMgmtService<>(1, 5000);
        TaskEntry mainTaskEntry = new TaskEntry();
        Callable<Object> callable = getEndpointResponseTasks(endpointSetting, endpointSettingVO);
        mainTaskEntry.setTaskHandler(callable);
        mainTaskEntry.setApplicationName("Main");
        mainTaskEntry.setName("Execute get endpoint response");
        taskMgmtExecutorV1.execute(mainTaskEntry);

        LOGGER.info("Endpoint {} is added successfully", endpointSetting.getExtEndpoint());
    }

    private Callable<Object> getEndpointResponseTasks(EndpointSetting endpointSetting, EndpointSettingVO endpointSettingVO) {
        // Job configuration
        String application = endpointSetting.getApplication();
        String taskName = endpointSetting.getTaskName();
        Integer noAttemptTimes = endpointSettingVO.getNoAttemptTimes();
        int noParallelThread = endpointSetting.getNoParallelThread();

        // Ext endpoint configuration
        String extEndpoint = endpointSetting.getExtEndpoint();
        String endpointMethod = endpointSetting.getMethod();
        HttpMethod extSupportedMethod = HttpMethod.valueOf(endpointMethod);
        String data = endpointSetting.getData();

        // Generator salt
        String generatorMethodName = endpointSetting.getGeneratorMethodName();
        Integer generatorSaltLength = endpointSetting.getGeneratorSaltLength();
        String generatorSaltStartWith = Optional.ofNullable(endpointSetting.getGeneratorSaltStartWith()).orElse("");

        // Metadata
        String columnMetadata = endpointSetting.getColumnMetadata();
        CheckedSupplier<MetadataVO> columnMetadataVOSup = () -> objectMapper.readValue(columnMetadata,
                MetadataVO.class);
        MetadataVO metadataVO = columnMetadataVOSup.get();

        // Success criteria
        String successCriteria = endpointSetting.getSuccessCriteria();

        CheckedFunction<String, Method> generatorMethodFunc = getGeneratorMethodFunc(generatorSaltStartWith);

        EndpointExecutionResult executionResult = new EndpointExecutionResult();
        executionResult.setEndpointSetting(endpointSetting);
        executionResult.setNumberOfTasks(noAttemptTimes);
        extExecutionResultRepository.save(executionResult);

        return () -> {
            try (GenericHttpClientPool httpClientPool = new GenericHttpClientPool(noParallelThread, 2000);
                 TaskMgmtService<Object> taskMgmtExecutorV2 = new TaskMgmtService<>(noParallelThread, 5000,
                         application)) {

                for (int index = 1; index <= noAttemptTimes; index++) {
                    TaskEntry taskEntry = getTaskEntry(endpointSetting, extEndpoint, extSupportedMethod, data,
                            generatorMethodName, generatorSaltLength, generatorSaltStartWith, metadataVO,
                            successCriteria, generatorMethodFunc, httpClientPool, index);

                    taskEntry.setApplicationName(application);
                    taskEntry.setName(taskName + " " + index);
                    taskMgmtExecutorV2.execute(taskEntry);

                    int percentComplete = executionResult.getPercentComplete();
                    int nextPercentComplete = index * 100 / noAttemptTimes;

                    if (percentComplete != nextPercentComplete) {
                        executionResult.setNumberOfCompletedTasks(index);
                        executionResult.setPercentComplete(nextPercentComplete);
                        extExecutionResultRepository.save(executionResult);
                    }
                }
            }
            executionResult.setEndedAt(LocalDateTime.now());
            extExecutionResultRepository.save(executionResult);
            LOGGER.info("{} is completed successfully.", taskName);
            return null;
        };
    }

    private TaskEntry getTaskEntry(EndpointSetting endpointSetting, String extEndpoint,
                                   HttpMethod extSupportedMethod, String data, String generatorMethodName,
                                   Integer generatorSaltLength, String generatorSaltStartWith, MetadataVO metadataVO,
                                   String successCriteria, CheckedFunction<String, Method> generatorMethodFunc,
                                   GenericHttpClientPool httpClientPool, int index) {

        TaskEntry taskEntry = new TaskEntry();
        taskEntry.setTaskHandler(() -> {
            String random = generateRandomValue(generatorMethodName, generatorSaltLength, generatorSaltStartWith,
                    generatorMethodFunc);
            if (random == null) return null;

            ExecutionTemplate<String> executionTemplate = getExecutionTemplate(extEndpoint, extSupportedMethod, data,
                    random);
            String responseString = httpClientPool.executeWithTemplate(executionTemplate);
            if (StringUtils.isNotEmpty(responseString) && responseString.contains(successCriteria)) {
                onSuccessResponse(endpointSetting, metadataVO, random, responseString);
                LOGGER.warn("index: {}, value: {}", index, random);
            }
            return responseString;
        });
        return taskEntry;
    }

    private String generateRandomValue(String generatorMethodName, Integer generatorSaltLength, String generatorSaltStartWith, CheckedFunction<String, Method> generatorMethodFunc) throws IllegalAccessException, InvocationTargetException, InvocationTargetException {
        String random = "";
        if (StringUtils.isNotEmpty(generatorMethodName)) {
            Method generatorMethod = generatorMethodFunc.apply(generatorMethodName);
            random = (String) generatorMethod.invoke(SaltGeneratorUtils.class, generatorSaltLength, generatorSaltStartWith);
            while (endpointResponseRepository.existsEndpointResponseByColumn1(random)) {
                random = (String) generatorMethod.invoke(SaltGeneratorUtils.class, generatorSaltLength,
                        generatorSaltStartWith);
            }
            return random;
        }
        return random;
    }

    private void onSuccessResponse(EndpointSetting endpointSetting, MetadataVO metadataVO, String random, String responseString) {
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(responseString);

        List<ColumnMetadataVO> columnMetadataVOs = metadataVO.getColumnMetadata();
        EndpointResponse endpointResponse = new EndpointResponse();
        endpointResponse.setEndpointSetting(endpointSetting);
        DocumentContext documentContext = JsonPath.parse(document);
        CheckedConsumer<ColumnMetadataVO> columnVOConsumer = column -> {
            String fieldJsonPath = column.getFieldPath();
            String columnName = StringUtils.capitalize(column.getMappingColumnName());
            String decryptFunctionName = column.getDecryptFunctionName();
            String getMethodName = "set".concat(columnName);
            String value = random;

            if (!fieldJsonPath.equals("random")) {
                value = documentContext.read(fieldJsonPath, String.class);
            }

            if (StringUtils.isNotEmpty(decryptFunctionName)) {
                Method decryptMethod = DecryptUtils.class.getMethod(decryptFunctionName, String.class);
                value = (String) decryptMethod.invoke(DecryptUtils.class, value);
            }

            Method setMethod = EndpointResponse.class.getMethod(getMethodName, String.class);
            setMethod.invoke(endpointResponse, value);
        };
        columnMetadataVOs.forEach(columnVOConsumer);
        endpointResponseRepository.save(endpointResponse);
    }

    private ExecutionTemplate<String> getExecutionTemplate(String extEndpoint, HttpMethod endpointMethod, String data,
                                                           String random) {
        return httpClient -> {
            RequestParamsBuilder requestParamsBuilder = HttpClientService.RequestParams.builder()
                    .method(endpointMethod)
                    .url(endpointMethod == HttpMethod.GET ? String.format(extEndpoint, random) : extEndpoint)
                    .data(endpointMethod == HttpMethod.POST ? String.format(data, random) : null)
                    .httpClient(httpClient);

            return HTTP_CLIENT_SERVICE.sendHTTPRequest()
                    .andThen(HttpClientService::asString)
                    .apply(requestParamsBuilder.build());
        };
    }

    private CheckedFunction<String, Method> getGeneratorMethodFunc(String generatorSaltStartWith) {
        return methodName -> {
            if (StringUtils.isNotEmpty(generatorSaltStartWith)) {
                return SaltGeneratorUtils.class.getMethod(methodName, Integer.class, String.class);
            }
            return SaltGeneratorUtils.class.getMethod(methodName, Integer.class);
        };
    }

    public List<EndpointResponseVO> getEndpointResponses(String application) {
        List<EndpointSetting> endpointSettings = extEndpointSettingRepository.findEndpointConfigsByApplication(
                application);
        if (endpointSettings.isEmpty()) {
            return Collections.emptyList();
        }
        List<EndpointResponse> responses = endpointResponseRepository.findEndpointResponsesByEndpointSettingIn(
                endpointSettings);
        return responses.stream().map(EndpointResponse::toEndpointResponseVO).collect(Collectors.toList());
    }
}
