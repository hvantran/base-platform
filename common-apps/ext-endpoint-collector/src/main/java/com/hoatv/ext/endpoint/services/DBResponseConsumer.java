package com.hoatv.ext.endpoint.services;

import com.hoatv.ext.endpoint.api.ResponseConsumer;
import com.hoatv.ext.endpoint.api.ResponseConsumerType;
import com.hoatv.ext.endpoint.dtos.MetadataVO;
import com.hoatv.ext.endpoint.dtos.MetadataVO.ColumnMetadataVO;
import com.hoatv.ext.endpoint.models.EndpointResponse;
import com.hoatv.ext.endpoint.models.EndpointSetting;
import com.hoatv.ext.endpoint.repositories.ExtEndpointResponseRepository;
import com.hoatv.ext.endpoint.utils.DecryptUtils;
import com.hoatv.fwk.common.services.CheckedConsumer;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

@Builder
public class DBResponseConsumer implements ResponseConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBResponseConsumer.class);

    private final MetadataVO metadataVO;
    private final EndpointSetting endpointSetting;
    private final ExtEndpointResponseRepository endpointResponseRepository;

    @Override
    public ResponseConsumerType getResponseConsumerType() {
        return ResponseConsumerType.DATABASE;
    }

    @Override
    public void onSuccessResponse(String random, String responseString) {

        Object document = Configuration.defaultConfiguration().jsonProvider().parse(responseString);

        List<ColumnMetadataVO> columnMetadataVOs = metadataVO.getColumnMetadata();
        EndpointResponse endpointResponse = new EndpointResponse();
        endpointResponse.setEndpointSetting(endpointSetting);
        DocumentContext documentContext = JsonPath.parse(document);
        CheckedConsumer<ColumnMetadataVO> columnVOConsumer = column -> {
            String fieldJsonPath = column.getFieldPath();
            String columnName = StringUtils.capitalize(column.getMappingColumnName());
            String decryptFunctionName = column.getDecryptFunctionName();
            String getMethodName = "set".concat(columnName);
            String value = random;

            if (!fieldJsonPath.equals("random")) {
                value = documentContext.read(fieldJsonPath, String.class);
            }

            if (StringUtils.isNotEmpty(decryptFunctionName)) {
                Method decryptMethod = DecryptUtils.class.getMethod(decryptFunctionName, String.class);
                value = (String) decryptMethod.invoke(DecryptUtils.class, value);
            }

            Method setMethod = EndpointResponse.class.getMethod(getMethodName, String.class);
            setMethod.invoke(endpointResponse, value);
        };
        columnMetadataVOs.forEach(columnVOConsumer);
        endpointResponseRepository.save(endpointResponse);
        LOGGER.info("{} - {}", random, responseString);
    }
}
