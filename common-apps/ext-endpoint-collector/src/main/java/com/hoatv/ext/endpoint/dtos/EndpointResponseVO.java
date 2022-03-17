package com.hoatv.ext.endpoint.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EndpointResponseVO {

    private final Long id;
    private final String column1;
    private final String column2;
    private final String column3;
    private final String column4;
    private final String column5;
    private final String column6;
    private final String column7;
    private final String column8;
    private final String column9;
    private final String column10;
}
