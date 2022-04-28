package com.hoatv.ext.endpoint.services;

import com.hoatv.ext.endpoint.api.ResponseConsumer;
import com.hoatv.ext.endpoint.api.ResponseConsumerType;
import com.hoatv.ext.endpoint.dtos.MetadataVO;
import com.hoatv.ext.endpoint.models.EndpointSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

public class ConsoleResponseConsumer implements ResponseConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleResponseConsumer.class);

    @Override
    public ResponseConsumerType getResponseConsumerType() {
        return ResponseConsumerType.CONSOLE;
    }

    @Override
    public BiConsumer<String, String> onSuccessResponse(MetadataVO metadataVO, EndpointSetting endpointSetting) {
        return (randomValue, responseString) -> LOGGER.info("{} - {}", randomValue, responseString);
    }
}
