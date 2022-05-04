package com.hoatv.webflux;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointSettingVO {

    private Input input;

    private Filter filter;

    private Output output;


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Input {
        private String application;
        private String taskName;

        private int noAttemptTimes;

        private int noParallelThread;

        private String columnMetadata;

        private RequestInfoVO requestInfo;

        private DataGeneratorInfoVO dataGeneratorInfo;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filter {
        private String successCriteria;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Output {
        private String responseConsumerType = "CONSOLE";
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestInfoVO {

        private String extEndpoint;
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

        private String generatorStrategy = "NONE";
    }
}

