package com.hoatv.ext.endpoint.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoatv.ext.endpoint.api.ResponseConsumer;
import com.hoatv.ext.endpoint.api.ResponseConsumerType;
import com.hoatv.ext.endpoint.dtos.DataGeneratorVO;
import com.hoatv.ext.endpoint.dtos.EndpointResponseVO;
import com.hoatv.ext.endpoint.dtos.EndpointSettingVO;
import com.hoatv.ext.endpoint.dtos.EndpointSettingVO.Input;
import com.hoatv.ext.endpoint.dtos.MetadataVO;
import com.hoatv.ext.endpoint.models.EndpointExecutionResult;
import com.hoatv.ext.endpoint.models.EndpointResponse;
import com.hoatv.ext.endpoint.models.EndpointSetting;
import com.hoatv.ext.endpoint.repositories.ExtEndpointResponseRepository;
import com.hoatv.ext.endpoint.repositories.ExtEndpointSettingRepository;
import com.hoatv.ext.endpoint.repositories.ExtExecutionResultRepository;
import com.hoatv.ext.endpoint.utils.SaltGeneratorUtils;
import com.hoatv.fwk.common.services.CheckedFunction;
import com.hoatv.fwk.common.services.CheckedSupplier;
import com.hoatv.fwk.common.services.GenericHttpClientPool;
import com.hoatv.fwk.common.services.HttpClientFactory;
import com.hoatv.fwk.common.services.HttpClientService.HttpMethod;
import com.hoatv.fwk.common.ultilities.ObjectUtils;
import com.hoatv.task.mgmt.entities.TaskEntry;
import com.hoatv.task.mgmt.services.TaskFactory;
import com.hoatv.task.mgmt.services.TaskMgmtService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.hoatv.ext.endpoint.utils.SaltGeneratorUtils.GeneratorType;

@Service
public class ExtRestDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtRestDataService.class);

    private final ExtEndpointSettingRepository extEndpointSettingRepository;
    private final ExtEndpointResponseRepository endpointResponseRepository;
    private final ExtExecutionResultRepository extExecutionResultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConsoleResponseConsumer consoleResponseConsumer = new ConsoleResponseConsumer();

    public ExtRestDataService(ExtEndpointSettingRepository extEndpointSettingRepository,
                              ExtEndpointResponseRepository endpointResponseRepository,
                              ExtExecutionResultRepository extExecutionResultRepository) {
        this.extEndpointSettingRepository = extEndpointSettingRepository;
        this.endpointResponseRepository = endpointResponseRepository;
        this.extExecutionResultRepository = extExecutionResultRepository;
    }

    public void addExtEndpoint(EndpointSettingVO endpointSettingVO) {
        EndpointSetting endpointSetting = EndpointSetting.fromEndpointConfigVO(endpointSettingVO);
        HttpMethod extSupportedMethod = HttpMethod.fromString(endpointSetting.getMethod());
        ObjectUtils.checkThenThrow(Objects::isNull, extSupportedMethod, HttpMethod.INVALID_SUPPORTED_METHOD);

        extEndpointSettingRepository.save(endpointSetting);
        TaskMgmtService taskMgmtExecutorV1 = TaskFactory.INSTANCE.getTaskMgmtService(1, 5000);
        TaskEntry mainTaskEntry = new TaskEntry();
        Callable<Object> callable = getEndpointResponseTasks(endpointSetting, endpointSettingVO);
        mainTaskEntry.setTaskHandler(callable);
        mainTaskEntry.setApplicationName("Main");
        mainTaskEntry.setName("Execute get endpoint response");
        taskMgmtExecutorV1.execute(mainTaskEntry);

        LOGGER.info("Endpoint {} is added successfully", endpointSetting.getExtEndpoint());
    }

    private Callable<Object> getEndpointResponseTasks(EndpointSetting endpointSetting, EndpointSettingVO endpointSettingVO) {
        // Metadata
        String columnMetadata = endpointSetting.getColumnMetadata();
        CheckedSupplier<MetadataVO> columnMetadataVOSup = () -> objectMapper.readValue(columnMetadata, MetadataVO.class);
        MetadataVO metadataVO = columnMetadataVOSup.get();

        // Register response consumers
        ResponseConsumerFactory factory = new ResponseConsumerFactory();
        factory.registerResponseConsumer(consoleResponseConsumer);
        factory.registerResponseConsumer(DBResponseConsumer.builder()
                .endpointResponseRepository(endpointResponseRepository)
                .endpointSetting(endpointSetting)
                .metadataVO(metadataVO)
                .build());

        // Job configuration
        String application = endpointSetting.getApplication();
        String taskName = endpointSetting.getTaskName();
        Input input = endpointSettingVO.getInput();
        int noAttemptTimes = input.getNoAttemptTimes();
        int noParallelThread = endpointSetting.getNoParallelThread();

        // Generator data for executing http methods
        String generatorMethodName = endpointSetting.getGeneratorMethodName();
        Integer generatorSaltLength = endpointSetting.getGeneratorSaltLength();
        String generatorSaltStartWith = Optional.ofNullable(endpointSetting.getGeneratorSaltStartWith()).orElse("");
        EndpointSettingVO.DataGeneratorInfoVO dataGeneratorInfo = input.getDataGeneratorInfo();
        GeneratorType generatorType = GeneratorType.valueOf(dataGeneratorInfo.getGeneratorStrategy());

        CheckedFunction<String, Method> generatorMethodFunc = getGeneratorMethodFunc(generatorSaltStartWith);
        Predicate<String> existingDataChecker = endpointResponseRepository::existsEndpointResponseByColumn1;
        DataGeneratorVO dataGeneratorVO = DataGeneratorVO.builder()
                .generatorMethodFunc(generatorMethodFunc)
                .generatorMethodName(generatorMethodName)
                .generatorSaltLength(generatorSaltLength)
                .generatorSaltStartWith(generatorSaltStartWith)
                .generatorType(generatorType)
                .checkExistingFunc(existingDataChecker)
                .build();

        EndpointExecutionResult executionResult = new EndpointExecutionResult();
        executionResult.setEndpointSetting(endpointSetting);
        executionResult.setNumberOfTasks(noAttemptTimes);
        extExecutionResultRepository.save(executionResult);

        EndpointSettingVO.Output output = endpointSettingVO.getOutput();
        String responseConsumerTypeName = output.getResponseConsumerType().toUpperCase();
        ResponseConsumerType responseConsumerType = ResponseConsumerType.valueOf(responseConsumerTypeName);
        ResponseConsumer responseConsumer = factory.getResponseConsumer(responseConsumerType);

        return getExecutionTasks(endpointSettingVO, metadataVO, application, taskName, input, noAttemptTimes,
            noParallelThread, dataGeneratorVO, executionResult, responseConsumer);
    }

    private Callable<Object> getExecutionTasks(EndpointSettingVO endpointSettingVO, MetadataVO metadataVO,
                                              String application, String taskName, Input input,
                                              int noAttemptTimes, int noParallelThread, DataGeneratorVO dataGeneratorVO,
                                              EndpointExecutionResult executionResult, ResponseConsumer responseConsumer) {
        return () -> {
            TaskMgmtService taskMgmtExecutorV2 = TaskFactory.INSTANCE.getTaskMgmtService(noParallelThread, 5000, application);
            GenericHttpClientPool httpClientPool = HttpClientFactory.INSTANCE.getGenericHttpClientPool(input.getTaskName(), noParallelThread, 2000);
            for (int index = 1; index <= noAttemptTimes; index++) {
                String executionTaskName = taskName.concat(String.valueOf(index));
                ExtTaskEntry extTaskEntry = ExtTaskEntry.builder()
                    .input(input)
                    .index(index)
                    .metadataVO(metadataVO)
                    .httpClientPool(httpClientPool)
                    .dataGeneratorVO(dataGeneratorVO)
                    .filter(endpointSettingVO.getFilter())
                    .onSuccessResponse(responseConsumer)
                    .build();

                CheckedFunction<Object, TaskEntry> taskEntryFunc = TaskEntry.fromObject(executionTaskName, application);
                TaskEntry taskEntry = taskEntryFunc.apply(extTaskEntry);
                taskMgmtExecutorV2.execute(taskEntry);

                int percentComplete = executionResult.getPercentComplete();
                int nextPercentComplete = index * 100 / noAttemptTimes;

                if (percentComplete != nextPercentComplete) {
                    executionResult.setNumberOfCompletedTasks(index);
                    executionResult.setPercentComplete(nextPercentComplete);
                    extExecutionResultRepository.save(executionResult);
                }
            }
            executionResult.setEndedAt(LocalDateTime.now());
            extExecutionResultRepository.save(executionResult);
            LOGGER.info("{} is completed successfully.", taskName);
            return null;
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

    public List<EndpointSettingVO> getAllExtEndpoints(String application) {
        List<EndpointSetting> endpointSettings = new ArrayList<>();
        if (StringUtils.isEmpty(application)) {
            List<EndpointSetting> extEndpointSettingRepositoryAll = extEndpointSettingRepository.findAll();
            endpointSettings.addAll(extEndpointSettingRepositoryAll);
        } else {
            List<EndpointSetting> endpointConfigsByApplication = extEndpointSettingRepository.findEndpointConfigsByApplication(application);
            endpointSettings.addAll(endpointConfigsByApplication);
        }
        return endpointSettings.stream().map(EndpointSetting::toEndpointConfigVO).collect(Collectors.toList());
    }
}
