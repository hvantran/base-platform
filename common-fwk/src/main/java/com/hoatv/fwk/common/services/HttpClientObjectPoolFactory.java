package com.hoatv.fwk.common.services;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpClientObjectPoolFactory extends BasePooledObjectFactory<HttpClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientObjectPoolFactory.class);

    @Override
    public HttpClient create() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .executor(executor)
                .build();
    }

    @Override
    public void destroyObject(PooledObject<HttpClient> p) {
        HttpClient httpClient = p.getObject();
        Optional<Executor> executorOptional = httpClient.executor();
        if (executorOptional.isPresent()) {
            LOGGER.info("Destroy HTTP client {}", httpClient);
            ExecutorService executor = (ExecutorService) executorOptional.get();
            executor.shutdownNow();
        }
    }

    @Override
    public PooledObject<HttpClient> wrap(HttpClient httpClient) {
        return new DefaultPooledObject<>(httpClient);
    }

}
