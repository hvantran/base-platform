package com.hoatv.ext.endpoint.api;

import com.hoatv.ext.endpoint.dtos.MetadataVO;
import com.hoatv.ext.endpoint.models.EndpointSetting;

import java.util.function.BiConsumer;

public interface ResponseConsumer {

    default ResponseConsumerType getResponseConsumerType() {
        return ResponseConsumerType.CONSOLE;
    }

    BiConsumer<String, String> onSuccessResponse(MetadataVO metadataVO, EndpointSetting endpointSetting);

    BiConsumer<String, String> onErrorResponse();
}
