package com.hoatv.ext.endpoint.services;

import com.hoatv.ext.endpoint.api.ResponseConsumer;
import com.hoatv.ext.endpoint.api.ResponseConsumerType;

import java.util.HashMap;
import java.util.Map;

public class ResponseConsumerFactory {

    private final Map<ResponseConsumerType, ResponseConsumer> consumerRegistry = new HashMap<>();

    public void registerResponseConsumer(ResponseConsumer responseConsumer) {
        consumerRegistry.put(responseConsumer.getResponseConsumerType(), responseConsumer);
    }

    public ResponseConsumer getResponseConsumer(ResponseConsumerType responseConsumerType) {
        return consumerRegistry.get(responseConsumerType);
    }
}
