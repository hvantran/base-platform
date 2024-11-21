package com.hoatv.fwk.common.services;

import com.hoatv.fwk.common.annotations.HttpConnectionPoolSettings;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public enum HttpClientFactory {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientFactory.class);

    private final Map<String, GenericHttpClientPool> serviceRegistry = new ConcurrentHashMap<>();


    /**
     * Create a GenericHttpClientPool.
     * If the GenericHttpClientPool is not created or already closed, a new GenericHttpClientPool will be created
     *
     * @param httpConnectionPoolSettings
     * @return
     */
    public GenericHttpClientPool getGenericHttpClientPool(HttpConnectionPoolSettings httpConnectionPoolSettings) {
        synchronized (serviceRegistry) {
            GenericHttpClientPool genericHttpClientPool = serviceRegistry.get(httpConnectionPoolSettings.name());
            if (genericHttpClientPool == null || genericHttpClientPool.isClosed()) {
                genericHttpClientPool = new GenericHttpClientPool(httpConnectionPoolSettings.httpClientCorePoolSize(),
                        httpConnectionPoolSettings.maxWaitMillis());
                serviceRegistry.put(httpConnectionPoolSettings.name(), genericHttpClientPool);
            }
            return genericHttpClientPool;
        }
    }

    /**
     * Create a GenericHttpClientPool.
     * If the GenericHttpClientPool is not created or already closed, a new GenericHttpClientPool will be created
     *
     * @param categoryName
     * @param maxTotal
     * @param maxWaitMillis
     * @param pooledObjectFactory
     * @return
     */
    public GenericHttpClientPool getGenericHttpClientPool(
            String categoryName,
            int maxTotal,
            int maxWaitMillis,
            PooledObjectFactory<HttpClient> pooledObjectFactory) {
        synchronized (serviceRegistry) {
            GenericHttpClientPool genericHttpClientPool = serviceRegistry.get(categoryName);
            if (genericHttpClientPool == null || genericHttpClientPool.isClosed()) {
                if (pooledObjectFactory == null) {
                    genericHttpClientPool = new GenericHttpClientPool(maxTotal, maxWaitMillis);
                } else {
                    genericHttpClientPool = new GenericHttpClientPool(maxTotal, maxWaitMillis, pooledObjectFactory);
                }
                serviceRegistry.put(categoryName, genericHttpClientPool);
                LOGGER.info("Creating new HttpClient Pool for category: {} - max clients: {} - max waiting time: {}",
                        categoryName, maxTotal, maxWaitMillis);
                return genericHttpClientPool;
            }

            LOGGER.info("Using existing HttpClient Pool: {} for category: {}", genericHttpClientPool, categoryName);
            return genericHttpClientPool;
        }
    }


    /**
     * Create a GenericHttpClientPool.
     * If the GenericHttpClientPool is not created or already closed, a new GenericHttpClientPool will be created
     *
     * @param categoryName
     * @param maxTotal
     * @param maxWaitMillis
     * @return
     */
    public GenericHttpClientPool getGenericHttpClientPool(String categoryName, int maxTotal, int maxWaitMillis) {
        return getGenericHttpClientPool(categoryName, maxTotal, maxWaitMillis, null);
    }

    public void destroy(String categoryName) {
        LOGGER.info("Close all registered http client pool under category: {}", categoryName);
        GenericHttpClientPool genericHttpClientPool = serviceRegistry.get(categoryName);
        if (Objects.nonNull(genericHttpClientPool)) {
            genericHttpClientPool.close();
            return;
        }
        LOGGER.warn("Category {} is not found", categoryName);
    }

    public void destroy() {
        LOGGER.info("Close all registered http client pool");
        serviceRegistry.values().stream()
                .filter(genericHttpClientPool -> !genericHttpClientPool.isClosed())
                .forEach(GenericObjectPool::close);
    }
}
