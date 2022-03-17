package com.hoatv.ext.endpoint.services;

import com.hoatv.ext.endpoint.api.ResponseConsumer;
import com.hoatv.ext.endpoint.api.ResponseConsumerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleResponseConsumer implements ResponseConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleResponseConsumer.class);

    @Override
    public ResponseConsumerType getResponseConsumerType() {
        return ResponseConsumerType.CONSOLE;
    }

    @Override
    public void onSuccessResponse(String randomValue, String responseString) {
        LOGGER.info("{} - {}", randomValue, responseString);
    }
}
