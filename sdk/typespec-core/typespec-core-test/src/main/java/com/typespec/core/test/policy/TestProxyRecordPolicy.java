// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.policy;

import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpPipelineCallContext;
import com.typespec.core.http.HttpPipelineNextPolicy;
import com.typespec.core.http.HttpPipelineNextSyncPolicy;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.http.policy.HttpPipelinePolicy;
import com.typespec.core.test.models.TestProxyRecordingOptions;
import com.typespec.core.test.models.RecordFilePayload;
import com.typespec.core.test.models.TestProxySanitizer;
import com.typespec.core.test.utils.HttpURLConnectionHttpClient;
import com.typespec.core.test.utils.TestProxyUtils;
import com.typespec.core.util.Context;
import com.typespec.core.util.serializer.JacksonAdapter;
import com.typespec.core.util.serializer.SerializerAdapter;
import com.typespec.core.util.serializer.SerializerEncoding;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static com.typespec.core.test.utils.TestProxyUtils.getAssetJsonFile;
import static com.typespec.core.test.utils.TestProxyUtils.getSanitizerRequests;
import static com.typespec.core.test.utils.TestProxyUtils.loadSanitizers;


/**
 * A {@link HttpPipelinePolicy} for redirecting traffic through the test proxy for recording.
 */
public class TestProxyRecordPolicy implements HttpPipelinePolicy {
    private static final SerializerAdapter SERIALIZER = new JacksonAdapter();
    private static final HttpHeaderName X_RECORDING_ID = HttpHeaderName.fromString("x-recording-id");
    private final HttpClient client;
    private final URL proxyUrl;
    private final boolean skipRecordingRequestBody;
    private String xRecordingId;
    private final List<TestProxySanitizer> sanitizers = new ArrayList<>();
    private static final List<TestProxySanitizer> DEFAULT_SANITIZERS = loadSanitizers();
    public static final String RECORD_MODE = "record";

    /**
     * Create an instance of {@link TestProxyRecordPolicy} with a list of custom sanitizers.
     *
     * @param httpClient The {@link HttpClient} to use. If none is passed {@link HttpURLConnectionHttpClient} is the default.
     * @param skipRecordingRequestBody Flag indicating to skip recording request bodies when tests run in Record mode.
     */
    public TestProxyRecordPolicy(HttpClient httpClient, boolean skipRecordingRequestBody) {
        this.client = (httpClient == null ? new HttpURLConnectionHttpClient() : httpClient);
        this.skipRecordingRequestBody = skipRecordingRequestBody;
        this.proxyUrl = TestProxyUtils.getProxyUrl();
        this.sanitizers.addAll(DEFAULT_SANITIZERS);
    }

    /**
     * Starts a recording of test traffic.
     *
     * @param recordFile The name of the file to save the recording to.
     * @param testClassPath the test class path
     * @throws RuntimeException Failed to serialize body payload.
     */
    public void startRecording(File recordFile, Path testClassPath) {
        String assetJsonPath = getAssetJsonFile(recordFile, testClassPath);
        HttpRequest request = null;
        try {
            request = new HttpRequest(HttpMethod.POST, String.format("%s/record/start", proxyUrl.toString()))
                .setBody(SERIALIZER.serialize(new RecordFilePayload(recordFile.toString(), assetJsonPath),
                    SerializerEncoding.JSON))
                .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HttpResponse response = client.sendSync(request, Context.NONE);

        this.xRecordingId = response.getHeaderValue(X_RECORDING_ID);

        addProxySanitization(this.sanitizers);
        setDefaultRecordingOptions();
    }

    private void setDefaultRecordingOptions() {
        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/Admin/SetRecordingOptions", proxyUrl.toString()));
        request.setBody("{\"HandleRedirects\": false}");
        request.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        client.sendSync(request, Context.NONE);
    }

    /**
     * Stops recording of test traffic.
     * @param variables A list of random variables generated during the test which is saved in the recording.
     */
    public void stopRecording(Queue<String> variables) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/record/stop", proxyUrl.toString()))
            .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json")
            .setHeader(X_RECORDING_ID, xRecordingId)
            .setBody(serializeVariables(variables));
        client.sendSync(request, Context.NONE);
    }

    /**
     * Transform the {@link Queue} containing variables into a JSON map for sending to the test proxy.
     * @param variables The variables to send.
     * @return A string containing the JSON map (or an empty map.)
     */
    private String serializeVariables(Queue<String> variables) {
        if (variables.isEmpty()) {
            return "{}";
        }

        int count = 0;
        Map<String, String> map = new LinkedHashMap<>();
        for (String variable : variables) {
            map.put(String.valueOf(count++), variable);
        }
        try {
            return SERIALIZER.serialize(map, SerializerEncoding.JSON);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method is invoked before the request is sent.
     *
     * @param context The request context.
     */
    private void beforeSendingRequest(HttpPipelineCallContext context) {
        TestProxyUtils.changeHeaders(context.getHttpRequest(), proxyUrl, xRecordingId, RECORD_MODE, skipRecordingRequestBody);
    }

    /**
     * Method is invoked after the response is received.
     *
     * @param response The response received.
     * @return The transformed response.
     */
    private HttpResponse afterReceivedResponse(HttpResponse response) {
        TestProxyUtils.checkForTestProxyErrors(response);
        return TestProxyUtils.resetTestProxyData(response);
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        beforeSendingRequest(context);
        HttpResponse response = next.processSync();
        return afterReceivedResponse(response);
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return Mono.fromCallable(
                () -> {
                    beforeSendingRequest(context);
                    return next;
                })
            .flatMap(ignored -> next.process())
            .map(this::afterReceivedResponse);
    }

    /**
     * Add a list of {@link TestProxySanitizer} to the current recording session.
     * @param sanitizers The sanitizers to add.
     */
    public void addProxySanitization(List<TestProxySanitizer> sanitizers) {
        if (isRecording()) {
            getSanitizerRequests(sanitizers, proxyUrl)
                .forEach(request -> {
                    request.setHeader(X_RECORDING_ID, xRecordingId);
                    client.sendSync(request, Context.NONE);
                });
        } else {
            this.sanitizers.addAll(sanitizers);
        }
    }

    private boolean isRecording() {
        return xRecordingId != null;
    }

    /**
     * Set transport layer test proxy recording options
     * @param testProxyRecordingOptions the transport layer test proxy recording options to set
     * @throws IllegalArgumentException if testProxyRecordingOptions cannot be serialized
     */
    public void setRecordingOptions(TestProxyRecordingOptions testProxyRecordingOptions) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/admin/setrecordingoptions", proxyUrl.toString()));
        String body;
        try {
            ObjectMapper mapper = new ObjectMapper();
            body = mapper.writeValueAsString(testProxyRecordingOptions);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to process JSON input", ex);
        }
        request.setBody(body);
        request.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        client.sendSync(request, Context.NONE);
    }
}

