package com.hoatv.ext.endpoint.api;

import com.hoatv.ext.endpoint.dtos.MetadataVO;
import com.hoatv.ext.endpoint.models.EndpointSetting;

import java.util.function.BiConsumer;

public interface ResponseConsumer {

    default ResponseConsumerType getResponseConsumerType() {
        return ResponseConsumerType.CONSOLE;
    }

    BiConsumer<MetadataVO, EndpointSetting> onSuccessResponse(String randomValue, String responseString);
}
