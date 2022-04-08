package com.hoatv.ext.endpoint.dtos;

import com.hoatv.ext.endpoint.api.ResponseConsumerType;
import com.hoatv.ext.endpoint.utils.SaltGeneratorUtils;
import com.hoatv.fwk.common.services.HttpClientService;
import com.hoatv.springboot.common.validation.ValueOfEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointSettingVO {

    @Valid
    private Input input;

    @Valid
    private Filter filter;

    @Valid
    private Output output;


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Input {
        @NotEmpty(message = "Application cannot be NULL/empty")
        private String application;
        @NotEmpty(message = "Task name cannot be NULL/empty")
        private String taskName;

        @Min(value = 1, message = "Number of running times cannot be 0")
        private int noAttemptTimes;

        @Min(value = 1, message = "Number of threads cannot be 0")
        private int noParallelThread;

        private String columnMetadata;

        @Valid
        private RequestInfoVO requestInfo;

        @Valid
        private DataGeneratorInfoVO dataGeneratorInfo;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filter {
        @NotEmpty(message = "Application cannot be NULL/empty")
        private String successCriteria;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Output {

        @ValueOfEnum(ResponseConsumerType.class)
        private String responseConsumerType = ResponseConsumerType.CONSOLE.name();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestInfoVO {

        @NotEmpty(message = "Endpoint cannot be NULL/empty")
        private String extEndpoint;

        @NotNull(message = "Method cannot be NULL")
        @ValueOfEnum(HttpClientService.HttpMethod.class)
        private String method;
        private String data;

        private Map<String, String> headers;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataGeneratorInfoVO {

        private int generatorSaltLength;
        private String generatorMethodName;
        private String generatorSaltStartWith;

        @ValueOfEnum(SaltGeneratorUtils.GeneratorType.class)
        private String generatorStrategy = SaltGeneratorUtils.GeneratorType.NONE.name();
    }
}

