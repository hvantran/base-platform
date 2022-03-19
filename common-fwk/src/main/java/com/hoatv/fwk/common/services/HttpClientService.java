package com.hoatv.fwk.common.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoatv.fwk.common.exceptions.AppException;
import lombok.Builder;
import org.apache.commons.collections4.MapUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.net.http.HttpRequest.BodyPublishers.ofString;

public enum HttpClientService {

    INSTANCE;

    public static final String ACCEPT = "Accept";
    private static final String APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE = "Content-Type";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE;

        public static final String INVALID_SUPPORTED_METHOD = "Invalid method name. Only support POST/GET";

        public static HttpMethod fromString(String methodName) {
            return Arrays.stream(HttpMethod.values()).filter(p-> p.name().equals(methodName)).findFirst().orElse(null);
        }
    }

    @Builder
    public static class RequestParams {
        private String url;
        private String data;
        private HttpClient httpClient;
        private HttpRequest.BodyPublisher bodyPublishers;

        @Builder.Default
        private int retryTimes = 0;
        @Builder.Default
        private int requestTimeoutInMs = 5000;
        @Builder.Default
        private HttpMethod method = HttpMethod.GET;
        @Builder.Default
        private Map<String, String> headers = new HashMap<>();
        @Builder.Default
        private Predicate<HttpResponse<String>> successRequestPredicate = response -> true;
    }

    private void appendRequestHeaders(RequestParams requestParams, HttpRequest.Builder httpRequestBuilder) {
        if (MapUtils.isEmpty(requestParams.headers)) {
            requestParams.headers.put(CONTENT_TYPE, APPLICATION_JSON);
        }
        requestParams.headers.forEach(httpRequestBuilder::header);
    }

    private void appendHttpRequestMethod(RequestParams requestParams, HttpRequest.Builder httpRequestBuilder) {
        switch (requestParams.method) {
            case GET:
                httpRequestBuilder.GET();
                return;
            case POST:
                HttpRequest.BodyPublisher bodyPublisher = Objects.isNull(requestParams.bodyPublishers) ? ofString(
                        requestParams.data) : requestParams.bodyPublishers;
                httpRequestBuilder.POST(bodyPublisher);
                return;
            case DELETE:
                httpRequestBuilder.DELETE();
                return;
            case PUT:
                bodyPublisher = Objects.isNull(requestParams.bodyPublishers) ? ofString(
                        requestParams.data) : requestParams.bodyPublishers;
                httpRequestBuilder.PUT(bodyPublisher);
                return;
            default:
                throw new AppException("Unsupported " + requestParams.method + " method");
        }
    }

    public Function<RequestParams, HttpResponse<String>> sendHTTPRequest() {
        return requestParams -> {
            Objects.requireNonNull(requestParams.httpClient);
            Objects.requireNonNull(requestParams.method);
            Objects.requireNonNull(requestParams.url);

            HttpRequest.Builder httpRequestBuilder = HttpRequest
                    .newBuilder()
                    .timeout(Duration.ofMillis(requestParams.requestTimeoutInMs))
                    .uri(URI.create(requestParams.url));

            appendRequestHeaders(requestParams, httpRequestBuilder);
            appendHttpRequestMethod(requestParams, httpRequestBuilder);
            HttpRequest httpRequest = httpRequestBuilder.build();

            CheckedSupplier<HttpResponse<String>> supplier = () -> requestParams.httpClient
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString());

            HttpResponse<String> response = supplier.get();
            for (int index = 0; index < requestParams.retryTimes; index++) {
                if (requestParams.successRequestPredicate.test(response)) {
                    return response;
                }
                response = supplier.get();
            }
            return response;
        };
    }

    public static <T> T asObject(HttpResponse<String> response, Class<T> tClass) {
        String responseString = response.body();
        CheckedSupplier<T> supplier = () -> objectMapper.readValue(responseString, tClass);
        return supplier.get();
    }

    public static String asString(HttpResponse<String> response) {
        return response.body();
    }
}
