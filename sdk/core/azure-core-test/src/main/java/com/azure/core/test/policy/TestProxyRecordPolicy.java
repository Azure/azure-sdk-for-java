// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.policy;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.utils.HttpURLConnectionHttpClient;
import com.azure.core.test.utils.TestProxyUtils;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static com.azure.core.test.implementation.TestingHelpers.X_RECORDING_ID;
import static com.azure.core.test.utils.TestProxyUtils.getSanitizerRequests;
import static com.azure.core.test.utils.TestProxyUtils.loadSanitizers;


/**
 * A {@link HttpPipelinePolicy} for redirecting traffic through the test proxy for recording.
 */
public class TestProxyRecordPolicy implements HttpPipelinePolicy {
    private static final SerializerAdapter SERIALIZER = new JacksonAdapter();
    private static final HttpHeaderName X_RECORDING_ID = HttpHeaderName.fromString("x-recording-id");
    private final HttpClient client;
    private final URL proxyUrl;
    private String xRecordingId;
    private final List<TestProxySanitizer> sanitizers = new ArrayList<>();
    private static final List<TestProxySanitizer> DEFAULT_SANITIZERS = loadSanitizers();

    /**
     * Create an instance of {@link TestProxyRecordPolicy} with a list of custom sanitizers.
     *
     * @param httpClient The {@link HttpClient} to use. If none is passed {@link HttpURLConnectionHttpClient} is the default.
     * @param proxyUrl The {@link URL} for the test proxy instance.
     */
    public TestProxyRecordPolicy(HttpClient httpClient, URL proxyUrl) {
        this.client = (httpClient == null ? new HttpURLConnectionHttpClient() : httpClient);
        this.proxyUrl = proxyUrl;
        this.sanitizers.addAll(DEFAULT_SANITIZERS);
    }

    /**
     * Starts a recording of test traffic.
     *
     * @param recordFile The name of the file to save the recording to.
     */
    public void startRecording(String recordFile) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/record/start", proxyUrl.toString()))
            .setBody(String.format("{\"x-recording-file\": \"%s\"}", recordFile));

        HttpResponse response = client.sendSync(request, Context.NONE);

        this.xRecordingId = response.getHeaderValue(X_RECORDING_ID);

        addProxySanitization(this.sanitizers);
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

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        TestProxyUtils.changeHeaders(context.getHttpRequest(), proxyUrl, xRecordingId, "record");
        HttpResponse response = next.processSync();
        TestProxyUtils.checkForTestProxyErrors(response);
        return TestProxyUtils.revertUrl(response);
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpRequest request = context.getHttpRequest();
        TestProxyUtils.changeHeaders(request, proxyUrl, xRecordingId, "record");
        return next.process().map(response -> {
            TestProxyUtils.checkForTestProxyErrors(response);
            return TestProxyUtils.revertUrl(response);
        });
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
}

