package com.hoatv.ext.endpoint.services;

import com.hoatv.ext.endpoint.api.ResponseConsumer;
import com.hoatv.ext.endpoint.api.ResponseConsumerType;
import com.hoatv.ext.endpoint.repositories.ExtEndpointResponseRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class ResponseConsumerFactory {

    private final Map<ResponseConsumerType, ResponseConsumer> consumerRegistry = new HashMap<>();
    private final ExtEndpointResponseRepository endpointResponseRepository;

    public ResponseConsumerFactory(ExtEndpointResponseRepository endpointResponseRepository) {
        this.endpointResponseRepository = endpointResponseRepository;
    }

    public void registerResponseConsumer(ResponseConsumer responseConsumer) {
        consumerRegistry.put(responseConsumer.getResponseConsumerType(), responseConsumer);
    }

    public ResponseConsumer getResponseConsumer(ResponseConsumerType responseConsumerType) {
        return consumerRegistry.get(responseConsumerType);
    }

    @PostConstruct
    public void init() {
        registerResponseConsumer(new ConsoleResponseConsumer());
        registerResponseConsumer(DBResponseConsumer.builder()
                .endpointResponseRepository(endpointResponseRepository).build());
    }
}
