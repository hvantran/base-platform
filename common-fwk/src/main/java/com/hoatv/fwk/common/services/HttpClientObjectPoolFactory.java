package com.hoatv.fwk.common.services;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.net.http.HttpClient;

public class HttpClientObjectPoolFactory extends BasePooledObjectFactory<HttpClient> {

    @Override
    public HttpClient create() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    @Override
    public PooledObject<HttpClient> wrap(HttpClient httpClient) {
        return new DefaultPooledObject<>(httpClient);
    }

}
