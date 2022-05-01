package com.hoatv.ext.endpoint.services;

import com.hoatv.ext.endpoint.dtos.DataGeneratorVO;
import com.hoatv.ext.endpoint.dtos.EndpointSettingVO;
import com.hoatv.ext.endpoint.utils.SaltGeneratorUtils;
import com.hoatv.fwk.common.services.CheckedSupplier;
import com.hoatv.fwk.common.services.GenericHttpClientPool;
import com.hoatv.fwk.common.services.GenericHttpClientPool.ExecutionTemplate;
import com.hoatv.fwk.common.services.HttpClientService;
import com.hoatv.fwk.common.services.HttpClientService.HttpMethod;
import com.hoatv.fwk.common.services.HttpClientService.RequestParams;
import com.hoatv.fwk.common.services.HttpClientService.RequestParams.RequestParamsBuilder;
import com.hoatv.system.health.metrics.MethodStatisticCollector;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

@Builder
public class ExtTaskEntry implements Callable<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtTaskEntry.class);
    private static final HttpClientService HTTP_CLIENT_SERVICE = HttpClientService.INSTANCE;

    private final int index;
    private final EndpointSettingVO.Input input;
    private final EndpointSettingVO.Filter filter;
    private final GenericHttpClientPool httpClientPool;
    private final DataGeneratorVO dataGeneratorVO;
    private final BiConsumer<String, String> onSuccessResponse;
    private final BiConsumer<String, String> onErrorResponse;
    private final MethodStatisticCollector methodStatisticCollector;


    public static ExecutionTemplate<String> getExecutionTemplate(String extEndpoint, HttpMethod endpointMethod,
                                                           String data, String random, Map<String, String> headers) {
        String url = endpointMethod == HttpMethod.GET ? String.format(extEndpoint, random) : extEndpoint;
        return httpClient -> {
            RequestParamsBuilder requestParamsBuilder = RequestParams.builder(url, httpClient)
                .method(endpointMethod)
                .headers(headers)
                .data(endpointMethod == HttpMethod.POST ? String.format(data, random) : null);
            requestParamsBuilder.httpClient(httpClient);
            return HTTP_CLIENT_SERVICE.sendHTTPRequest()
                    .andThen(HttpClientService::asString)
                    .apply(requestParamsBuilder.build());
        };
    }

    public Void call() {
        long startTime = System.currentTimeMillis();
        CheckedSupplier<String> supplier = () -> SaltGeneratorUtils.generateSaltValue(dataGeneratorVO, index);
        String random = supplier.get();
        String extEndpoint = input.getRequestInfo().getExtEndpoint();
        String endpointMethod = input.getRequestInfo().getMethod();
        String data = input.getRequestInfo().getData();
        Map<String, String> headers = input.getRequestInfo().getHeaders();
        HttpMethod extSupportedMethod = HttpMethod.valueOf(endpointMethod);

        ExecutionTemplate<String> executionTemplate = getExecutionTemplate(extEndpoint, extSupportedMethod, data, random, headers);
        String responseString = httpClientPool.executeWithTemplate(executionTemplate);
        if (StringUtils.isNotEmpty(responseString) && responseString.contains(filter.getSuccessCriteria())) {
            onSuccessResponse.accept(random, responseString);
        } else {
            onErrorResponse.accept(random, responseString);
        }
        long endTime = System.currentTimeMillis();
        methodStatisticCollector.addMethodStatistics("endpoint-processing-data-task", "ms", endTime - startTime);
        return null;
    }
}
