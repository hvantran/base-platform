package com.hoatv.ext.endpoint.services;

import com.hoatv.ext.endpoint.dtos.DataGeneratorVO;
import com.hoatv.ext.endpoint.dtos.EndpointSettingVO;
import com.hoatv.ext.endpoint.dtos.MetadataVO;
import com.hoatv.ext.endpoint.utils.SaltGeneratorUtils;
import com.hoatv.fwk.common.services.CheckedSupplier;
import com.hoatv.fwk.common.services.GenericHttpClientPool;
import com.hoatv.fwk.common.services.GenericHttpClientPool.ExecutionTemplate;
import com.hoatv.fwk.common.services.HttpClientService;
import com.hoatv.fwk.common.services.HttpClientService.HttpMethod;
import com.hoatv.fwk.common.services.HttpClientService.RequestParams;
import com.hoatv.fwk.common.services.HttpClientService.RequestParams.RequestParamsBuilder;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

@Builder
public class ExtTaskEntry implements Callable<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtTaskEntry.class);
    private static final HttpClientService HTTP_CLIENT_SERVICE = HttpClientService.INSTANCE;

    private final int index;
    private final MetadataVO metadataVO;
    private final EndpointSettingVO endpointSettingVO;
    private final GenericHttpClientPool httpClientPool;
    private final DataGeneratorVO dataGeneratorVO;
    private final BiConsumer<String, String> onSuccessResponse;


    private ExecutionTemplate<String> getExecutionTemplate(String extEndpoint, HttpMethod endpointMethod, String data, String random) {
        return httpClient -> {
            RequestParamsBuilder requestParamsBuilder = RequestParams.builder()
                    .method(endpointMethod)
                    .url(endpointMethod == HttpMethod.GET ? String.format(extEndpoint, random) : extEndpoint)
                    .data(endpointMethod == HttpMethod.POST ? String.format(data, random) : null)
                    .httpClient(httpClient);

            return HTTP_CLIENT_SERVICE.sendHTTPRequest()
                    .andThen(HttpClientService::asString)
                    .apply(requestParamsBuilder.build());
        };
    }

    public Void call() {

        CheckedSupplier<String> supplier = () -> SaltGeneratorUtils.generateSaltValue(dataGeneratorVO, index);
        String random = supplier.get();
        String extEndpoint = endpointSettingVO.getExtEndpoint();
        String endpointMethod = endpointSettingVO.getMethod();
        String data = endpointSettingVO.getData();
        HttpMethod extSupportedMethod = HttpMethod.valueOf(endpointMethod);

        ExecutionTemplate<String> executionTemplate = getExecutionTemplate(extEndpoint, extSupportedMethod, data, random);
        String responseString = httpClientPool.executeWithTemplate(executionTemplate);
        if (StringUtils.isNotEmpty(responseString) && responseString.contains(endpointSettingVO.getSuccessCriteria())) {
            onSuccessResponse.accept(random, responseString);
        }
        return null;
    }
}
