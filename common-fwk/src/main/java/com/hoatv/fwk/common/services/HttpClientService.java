package com.hoatv.fwk.common.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoatv.fwk.common.exceptions.AppException;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public enum HttpClientService {

    INSTANCE;

    private static final String APPLICATION_JSON = "application/json";
    public static final String ACCEPT = "Accept";
    public static final String CONTENT_TYPE = "Content-Type";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public HttpResponse<String> sendPOSTRequest(HttpClient httpClient, String postData, String url)  {
        HttpRequest.Builder httpRequestBuilder = HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(postData));
        return sendHTTPRequest(httpClient, httpRequestBuilder);
    }

    public <T> T sendPOSTRequest(HttpClient httpClient, String postData, String url, Class<T> tClass)  {
        return sendPOSTRequest(httpClient, postData, url, tClass, 0);
    }

    public <T> T sendPOSTRequest(HttpClient httpClient, String postData, String url, Class<T> tClass, int retryTimes)  {
        HttpRequest.Builder httpRequestBuilder = HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(postData));
        HttpResponse<String> response = sendRequestWithRetries(httpClient, httpRequestBuilder, retryTimes);
        String responseString = response.body();
        if (tClass.equals(String.class)) {
            return (T) responseString;
        }
        CheckedSupplier<T> supplier = () -> objectMapper.readValue(responseString, tClass);
        return supplier.get();
    }

    public HttpResponse<String> sendGETRequest(HttpClient httpClient, String url) {
        HttpRequest.Builder httpRequestBuilder = HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .header(ACCEPT, APPLICATION_JSON)
                .GET();
        return sendHTTPRequest(httpClient, httpRequestBuilder);
    }

    public HttpResponse<String> sendGETRequest(HttpClient httpClient, String url, String authorization) {
        HttpRequest.Builder httpRequestBuilder = HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .header(ACCEPT, APPLICATION_JSON)
                .headers("Authorization", authorization)
                .GET();
        return sendHTTPRequest(httpClient, httpRequestBuilder);
    }

    public <T> T sendGETRequest(HttpClient httpClient, String url, Class<T> tClass) {
        return sendGETRequest(httpClient, url, tClass, 0);
    }

    public <T> T sendGETRequest(HttpClient httpClient, String url, Class<T> tClass, int retryTimes) {
        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(ACCEPT, APPLICATION_JSON).GET();
        HttpResponse<String> response = sendRequestWithRetries(httpClient, httpRequestBuilder, retryTimes);
        CheckedSupplier<T> supplier = () -> objectMapper.readValue(response.body(), tClass);
        return supplier.get();
    }

    public HttpResponse<String> sendRequestWithRetries(HttpClient httpClient, HttpRequest.Builder httpRequestBuilder, int retryTimes) {
        AppException appException;
        int currentRetryTimes = 0;
        do {
            try {
                Thread.sleep(100);
                HttpResponse<String> response = sendHTTPRequest(httpClient, httpRequestBuilder);
                if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                    appException = new AppException(response.body());
                    currentRetryTimes++;
                    continue;
                }
                return response;
            } catch (AppException exception) {
                appException = exception;
                currentRetryTimes++;
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
