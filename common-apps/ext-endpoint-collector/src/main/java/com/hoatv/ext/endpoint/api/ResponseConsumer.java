package com.hoatv.ext.endpoint.api;

import java.util.function.BiConsumer;

public interface ResponseConsumer extends BiConsumer<String, String> {

    @Override
    default void accept(String randomValue, String responseString) {
        onSuccessResponse(randomValue, responseString);
    }

    default ResponseConsumerType getResponseConsumerType() {
        return ResponseConsumerType.CONSOLE;
    }

    void onSuccessResponse(String randomValue, String responseString);
}
