// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.policy;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.azure.core.test.utils.TestProxyUtils.getSanitizerRequests;
import static com.azure.core.test.utils.TestProxyUtils.loadSanitizers;


/**
 * A {@link HttpPipelinePolicy} for redirecting traffic through the test proxy for recording.
 */
public class TestProxyRecordPolicy implements HttpPipelinePolicy {
    private static final SerializerAdapter SERIALIZER = new JacksonAdapter();
    private final HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();
    private String xRecordingId;
    private final List<TestProxySanitizer> sanitizers = new ArrayList<>();
    private static final List<TestProxySanitizer> DEFAULT_SANITIZERS = loadSanitizers();

    /**
     * Create an instance of {@link TestProxyRecordPolicy} with a list of custom sanitizers.
     *
     * @param customSanitizers the list of custom sanitizers to be added to {@link TestProxyRecordPolicy}
     */
    public TestProxyRecordPolicy(List<TestProxySanitizer> customSanitizers) {
        this.sanitizers.addAll(DEFAULT_SANITIZERS);
        this.sanitizers.addAll(customSanitizers == null ? Collections.emptyList() : customSanitizers);
    }

    /**
     * Starts a recording of test traffic.
     *
     * @param recordFile The name of the file to save the recording to.
     */
    public void startRecording(String recordFile) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/record/start", TestProxyUtils.getProxyUrl()))
            .setBody(String.format("{\"x-recording-file\": \"%s\"}", recordFile));

        HttpResponse response = client.sendSync(request, Context.NONE);

        this.xRecordingId = response.getHeaderValue("x-recording-id");

        addProxySanitization();
    }

    /**
     * Stops recording of test traffic.
     * @param variables A list of random variables generated during the test which is saved in the recording.
     */
    public void stopRecording(Queue<String> variables) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/record/stop", TestProxyUtils.getProxyUrl()))
            .setHeader("content-type", "application/json")
            .setHeader("x-recording-id", xRecordingId)
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
        AtomicInteger count = new AtomicInteger(0);
        Map<String, String> map = variables.stream().collect(Collectors.toMap(k -> String.format("%d", count.getAndIncrement()), k -> k));
        try {
            return SERIALIZER.serialize(map, SerializerEncoding.JSON);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        TestProxyUtils.changeHeaders(context.getHttpRequest(), xRecordingId, "record");
        return next.processSync();
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpRequest request = context.getHttpRequest();
        TestProxyUtils.changeHeaders(request, xRecordingId, "record");
        return next.process();
    }

    private void addProxySanitization() {
        getSanitizerRequests(this.sanitizers)
            .forEach(request -> {
                request.setHeader("x-recording-id", xRecordingId);
                client.sendSync(request, Context.NONE);
            });
    }
}

