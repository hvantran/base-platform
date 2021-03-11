package com.hoatv.fwk.common.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoatv.fwk.common.exceptions.AppException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public enum HttpClientService {

    INSTANCE;

    private static final String APPLICATION_JSON = "application/json";

    private ObjectMapper objectMapper = new ObjectMapper();

    public HttpResponse<String> sendPOSTRequest(HttpClient httpClient, String postData, String url)  {
        HttpRequest.Builder httpRequestBuilder = HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(postData));
        return sendHTTPRequest(httpClient, httpRequestBuilder);
    }

    public HttpResponse<String> sendGETRequest(HttpClient httpClient, String url) {
        HttpRequest.Builder httpRequestBuilder = HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .header("Accept", APPLICATION_JSON)
                .GET();
        return sendHTTPRequest(httpClient, httpRequestBuilder);
    }

    public HttpResponse<String> sendGETRequest(HttpClient httpClient, String url, String authorization) {
        HttpRequest.Builder httpRequestBuilder = HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .header("Accept", APPLICATION_JSON)
                .headers("Authorization", authorization)
                .GET();
        return sendHTTPRequest(httpClient, httpRequestBuilder);
    }

    public <T> T sendGETRequest(HttpClient httpClient, String url, Class<T> tClass) {
        return sendGETRequest(httpClient, url, tClass, 0);
    }

    public <T> T sendGETRequest(HttpClient httpClient, String url, Class<T> tClass, int retryTimes) {
        AppException appException;
        int currentRetryTimes = 0;
        do {
            try {
                Thread.sleep(100);
                HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Accept", APPLICATION_JSON).GET();
                HttpResponse<String> response = sendHTTPRequest(httpClient, httpRequestBuilder);
                CheckedSupplier<T> supplier = () -> objectMapper.readValue(response.body(), tClass);
                return supplier.get();
            } catch (AppException exception) {
                appException = exception;
                currentRetryTimes++;
                continue;
            } catch (InterruptedException interruptedException) {
                appException = new AppException(interruptedException);
                currentRetryTimes++;
            }
        }
        while (currentRetryTimes <= retryTimes);

        throw appException;
    }

    private HttpResponse<String> sendHTTPRequest(HttpClient httpClient, HttpRequest.Builder httpRequestBuilder) {
        HttpRequest httpRequest = httpRequestBuilder.build();
        CheckedSupplier<HttpResponse<String>> supplier = () -> httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return supplier.get();
    }
}
