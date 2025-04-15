// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test.policy;

import com.azure.v2.core.test.models.RecordFilePayload;
import com.azure.v2.core.test.models.TestProxyRecordingOptions;
import com.azure.v2.core.test.models.TestProxySanitizer;
import com.azure.v2.core.test.utils.HttpUrlConnectionHttpClient;
import com.azure.v2.core.test.utils.TestProxyUtils;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePosition;
import io.clientcore.core.serialization.json.JsonWriter;
import io.clientcore.core.models.binarydata.BinaryData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static com.azure.v2.core.test.utils.TestProxyUtils.checkForTestProxyErrors;
import static com.azure.v2.core.test.utils.TestProxyUtils.createAddSanitizersRequest;
import static com.azure.v2.core.test.utils.TestProxyUtils.getAssetJsonFile;
import static com.azure.v2.core.test.utils.TestProxyUtils.getRemoveSanitizerRequest;
import static com.azure.v2.core.test.utils.TestProxyUtils.loadSanitizers;

/**
 * A {@link HttpPipelinePolicy} for redirecting traffic through the test proxy for recording.
 */
public class TestProxyRecordPolicy implements HttpPipelinePolicy {
    private static final HttpHeaderName X_RECORDING_ID = HttpHeaderName.fromString("x-recording-id");
    private final HttpClient client;
    private final URI proxyUri;
    private final boolean skipRecordingRequestBody;
    private String xRecordingId;
    private final List<TestProxySanitizer> sanitizers = new ArrayList<>();
    private static final List<TestProxySanitizer> DEFAULT_SANITIZERS = loadSanitizers();

    /**
     * The mode to use when recording.
     */
    public static final String RECORD_MODE = "record";

    /**
     * Create an instance of {@link TestProxyRecordPolicy} with a list of custom sanitizers.
     *
     * @param httpClient The {@link HttpClient} to use. If none is passed {@link HttpUrlConnectionHttpClient} is the default.
     * @param skipRecordingRequestBody Flag indicating to skip recording request bodies when tests run in Record mode.
     */
    public TestProxyRecordPolicy(HttpClient httpClient, boolean skipRecordingRequestBody) {
        this.client = (httpClient == null ? new HttpUrlConnectionHttpClient() : httpClient);
        this.skipRecordingRequestBody = skipRecordingRequestBody;
        this.proxyUri = TestProxyUtils.getProxyUri();
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
            HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST)
                .setUri(proxyUri + "/record/start")
                .setBody(BinaryData.fromObject(new RecordFilePayload(recordFile.toString(), assetJsonPath)));
            request.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");

            try (Response<BinaryData> response = client.send(request)) {
                checkForTestProxyErrors(response);
                if (response.getStatusCode() != 200) {
                    throw new RuntimeException(response.getValue().toString());
                }

                this.xRecordingId = response.getHeaders().getValue(X_RECORDING_ID);
            }

            addProxySanitization(this.sanitizers);
            setDefaultRecordingOptions();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setDefaultRecordingOptions() throws IOException {
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST)
            .setUri(proxyUri + "/Admin/SetRecordingOptions")
            .setBody(BinaryData.fromString("{\"HandleRedirects\": false}"));
        request.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        client.send(request).close();
    }

    /**
     * Stops recording of test traffic.
     * @param variables A list of random variables generated during the test which is saved in the recording.
     * @throws RuntimeException If the test proxy returns an error while stopping recording.
     * @throws IOException If an error occurs while sending the request.
     */
    public void stopRecording(Queue<String> variables) throws IOException {
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST)
            .setUri(proxyUri + "/record/stop")
            .setBody(serializeVariables(variables));
        request.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json").set(X_RECORDING_ID, xRecordingId);

        try (Response<BinaryData> response = client.send(request)) {
            checkForTestProxyErrors(response);
            if (response.getStatusCode() != 200) {
                throw new RuntimeException(response.getValue().toString());
            }
        }
    }

    /**
     * Transform the {@link Queue} containing variables into a JSON map for sending to the test proxy.
     * @param variables The variables to send.
     * @return A BinaryData containing the JSON map (or an empty map.)
     */
    private BinaryData serializeVariables(Queue<String> variables) {
        if (variables.isEmpty()) {
            return BinaryData.fromString("{}");
        }

        StringBuilder builder = new StringBuilder().append('{');

        int count = 0;
        for (String variable : variables) {
            if (count > 0) {
                builder.append(',');
            }

            builder.append('"').append(count).append("\":\"");
            count++;

            if (variable == null) {
                builder.append("null");
            } else {
                builder.append(variable).append('"');
            }
        }

        return BinaryData.fromString(builder.append('}').toString());
    }

    /**
     * Method is invoked before the request is sent.
     *
     * @param request The request.
     */
    private void beforeSendingRequest(HttpRequest request) {
        TestProxyUtils.changeHeaders(request, proxyUri, xRecordingId, RECORD_MODE, skipRecordingRequestBody);
    }

    /**
     * Method is invoked after the response is received.
     *
     * @param response The response received.
     * @return The transformed response.
     */
    private Response<BinaryData> afterReceivedResponse(Response<BinaryData> response) {
        TestProxyUtils.checkForTestProxyErrors(response);
        return TestProxyUtils.resetTestProxyData(response);
    }

    @Override
    public Response<BinaryData> process(HttpRequest request, HttpPipelineNextPolicy next) {
        beforeSendingRequest(request);
        return afterReceivedResponse(next.process());
    }

    /**
     * Add a list of {@link TestProxySanitizer} to the current recording session.
     * @param sanitizers The sanitizers to add.
     * @throws IOException If an error occurs while sending the request.
     */
    public void addProxySanitization(List<TestProxySanitizer> sanitizers) throws IOException {
        if (isRecording()) {
            HttpRequest request = createAddSanitizersRequest(sanitizers, proxyUri);
            request.getHeaders().set(X_RECORDING_ID, xRecordingId);

            client.send(request).close();
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
     * @throws IOException If an error occurs while sending the request.
     */
    public void removeProxySanitization(List<String> sanitizers) throws IOException {
        if (isRecording()) {
            Map<String, List<String>> data = new HashMap<>();
            data.put("Sanitizers", sanitizers);

            HttpRequest request;
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JsonWriter jsonWriter = JsonWriter.toStream(outputStream)) {
                jsonWriter.writeMap(data, (writer, value) -> writer.writeArray(value, JsonWriter::writeString)).flush();
                request = getRemoveSanitizerRequest().setBody(BinaryData.fromBytes(outputStream.toByteArray()));
                request.getHeaders().set(X_RECORDING_ID, xRecordingId);
            }

            client.send(request).close();
        }
    }

    /**
     * Set transport layer test proxy recording options
     * @param testProxyRecordingOptions the transport layer test proxy recording options to set
     * @throws IllegalArgumentException if testProxyRecordingOptions cannot be serialized
     */
    public void setRecordingOptions(TestProxyRecordingOptions testProxyRecordingOptions) {
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST)
            .setUri(proxyUri + "/admin/setrecordingoptions")
            .setBody(BinaryData.fromObject(testProxyRecordingOptions));
        request.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        client.send(request).close();
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.AFTER_INSTRUMENTATION;
    }
}
