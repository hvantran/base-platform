package com.hoatv.fwk.common.services;

import com.hoatv.fwk.common.annotations.HttpConnectionPoolSettings;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.util.Optional;

public class GenericHttpClientPool extends GenericObjectPool<HttpClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericHttpClientPool.class);

    public GenericHttpClientPool(HttpConnectionPoolSettings httpConnectionPoolSettings) {
        this(httpConnectionPoolSettings.httpClientCorePoolSize(), httpConnectionPoolSettings.maxWaitMillis());
    }

    public GenericHttpClientPool(int maxTotal, int maxWaitMillis) {
        super(new HttpClientObjectPoolFactory());
        this.setMaxTotal(maxTotal);
        this.setMaxWaitMillis(maxWaitMillis);
    }

    @FunctionalInterface
    public interface ExecutionTemplate<T> {

        T execute(HttpClient httpClient);
    }

    public <T> T executeWithTemplate(ExecutionTemplate<T> executionTemplate) {
        Optional<HttpClient> httpClientOp = Optional.empty();

        try {
            httpClientOp = Optional.of(this.borrowObject());
            HttpClient httpClient = httpClientOp.orElseThrow();
            T returnValue = executionTemplate.execute(httpClient);

            returnObject(httpClientOp.get());
            return returnValue;
        } catch (Exception exception) {
            LOGGER.error("An exception occurred while executing task", exception);
            if (httpClientOp.isPresent()) {
                LOGGER.info("Invalid HTTP client from pool");
                CheckedConsumer<HttpClient> checkedConsumer = this::invalidateObject;
                checkedConsumer.accept(httpClientOp.get());
            }
            return null;
        }
    }
}
