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
import com.azure.core.test.models.RecordFilePayload;
import com.azure.core.test.models.TestProxyRecordingOptions;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.utils.HttpURLConnectionHttpClient;
import com.azure.core.test.utils.TestProxyUtils;
import com.azure.core.util.Context;
import com.azure.json.JsonWriter;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static com.azure.core.test.implementation.TestingHelpers.X_RECORDING_ID;
import static com.azure.core.test.implementation.TestingHelpers.drainHttpResponse;
import static com.azure.core.test.implementation.TestingHelpers.jsonWriteHelper;
import static com.azure.core.test.utils.TestProxyUtils.checkForTestProxyErrors;
import static com.azure.core.test.utils.TestProxyUtils.createAddSanitizersRequest;
import static com.azure.core.test.utils.TestProxyUtils.createProxyRequestUrl;
import static com.azure.core.test.utils.TestProxyUtils.getAssetJsonFile;
import static com.azure.core.test.utils.TestProxyUtils.getRemoveSanitizerRequest;
import static com.azure.core.test.utils.TestProxyUtils.loadSanitizers;

/**
 * A {@link HttpPipelinePolicy} for redirecting traffic through the test proxy for recording.
 */
public class TestProxyRecordPolicy implements HttpPipelinePolicy {
    private final HttpClient client;
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
        try {
            String assetJsonPath = getAssetJsonFile(recordFile, testClassPath);
            HttpRequest request = new HttpRequest(HttpMethod.POST, createProxyRequestUrl("/record/start"))
                .setBody(new RecordFilePayload(recordFile.toString(), assetJsonPath).toJsonString())
                .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json");

            HttpResponse response = null;
            try {
                response = client.sendSync(request, Context.NONE);
                checkForTestProxyErrors(response);
                if (response.getStatusCode() != 200) {
                    throw new RuntimeException(response.getBodyAsBinaryData().toString());
                }

                this.xRecordingId = response.getHeaderValue(X_RECORDING_ID);
            } finally {
                if (response != null) {
                    drainHttpResponse(response);
                }
            }

            addProxySanitization(this.sanitizers);
            setDefaultRecordingOptions();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setDefaultRecordingOptions() {
        HttpRequest request = new HttpRequest(HttpMethod.POST, createProxyRequestUrl("/Admin/SetRecordingOptions"))
            .setBody("{\"HandleRedirects\": false}")
            .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json");
        drainHttpResponse(client.sendSync(request, Context.NONE));
    }

    /**
     * Stops recording of test traffic.
     * @param variables A list of random variables generated during the test which is saved in the recording.
     * @throws RuntimeException If the test proxy returns an error while stopping recording.
     */
    public void stopRecording(Queue<String> variables) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, createProxyRequestUrl("/record/stop"))
            .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json")
            .setHeader(X_RECORDING_ID, xRecordingId)
            .setBody(serializeVariables(variables));

        HttpResponse response = null;
        try {
            response = client.sendSync(request, Context.NONE);
            checkForTestProxyErrors(response);
            if (response.getStatusCode() != 200) {
                throw new RuntimeException(response.getBodyAsBinaryData().toString());
            }
        } finally {
            if (response != null) {
                drainHttpResponse(response);
            }
        }
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

        return jsonWriteHelper(jsonWriter -> {
            jsonWriter.writeStartObject();

            int count = 0;
            for (String variable : variables) {
                jsonWriter.writeNullableField(String.valueOf(count), variable, JsonWriter::writeString);
            }

            return jsonWriter.writeEndObject();
        });
    }

    /**
     * Method is invoked before the request is sent.
     *
     * @param context The request context.
     */
    private void beforeSendingRequest(HttpPipelineCallContext context) {
        TestProxyUtils.changeHeaders(context.getHttpRequest(), xRecordingId, RECORD_MODE, skipRecordingRequestBody);
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
        beforeSendingRequest(context);
        return next.process().map(this::afterReceivedResponse);
    }

    /**
     * Add a list of {@link TestProxySanitizer} to the current recording session.
     * @param sanitizers The sanitizers to add.
     */
    public void addProxySanitization(List<TestProxySanitizer> sanitizers) {
        if (isRecording()) {
            HttpRequest request = createAddSanitizersRequest(sanitizers).setHeader(X_RECORDING_ID, xRecordingId);

            drainHttpResponse(client.sendSync(request, Context.NONE));
        } else {
            this.sanitizers.addAll(sanitizers);
        }
    }

    private boolean isRecording() {
        return xRecordingId != null;
    }

    /**
     * Removes the list of sanitizers from the current playback session.
     * @param sanitizers The sanitizers to remove.
     * @throws RuntimeException if an {@link IOException} is thrown.
     */
    public void removeProxySanitization(List<String> sanitizers) {
        if (isRecording()) {
            HttpRequest request = getRemoveSanitizerRequest().setHeader(X_RECORDING_ID, xRecordingId)
                .setBody(jsonWriteHelper(jsonWriter -> jsonWriter.writeStartObject()
                    .writeArrayField("Sanitizers", sanitizers, JsonWriter::writeString)
                    .writeEndObject()));

            drainHttpResponse(client.sendSync(request, Context.NONE));
        }
    }

    /**
     * Set transport layer test proxy recording options
     * @param testProxyRecordingOptions the transport layer test proxy recording options to set
     * @throws IllegalArgumentException if testProxyRecordingOptions cannot be serialized
     */
    public void setRecordingOptions(TestProxyRecordingOptions testProxyRecordingOptions) {
        try {
            HttpRequest request = new HttpRequest(HttpMethod.POST, createProxyRequestUrl("/admin/setrecordingoptions"))
                .setBody(testProxyRecordingOptions.toJsonString())
                .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json");
            drainHttpResponse(client.sendSync(request, Context.NONE));
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to process JSON input", ex);
        }
    }
}
