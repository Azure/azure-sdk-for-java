// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test.http;

import com.azure.v2.core.test.models.RecordFilePayload;
import com.azure.v2.core.test.models.TestProxyRequestMatcher;
import com.azure.v2.core.test.models.TestProxySanitizer;
import com.azure.v2.core.test.utils.HttpUrlConnectionHttpClient;
import com.azure.v2.core.test.utils.TestProxyUtils;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonWriter;
import io.clientcore.core.models.binarydata.BinaryData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static com.azure.v2.core.test.implementation.TestingHelpers.X_RECORDING_FILE_LOCATION;
import static com.azure.v2.core.test.implementation.TestingHelpers.X_RECORDING_ID;
import static com.azure.v2.core.test.utils.TestProxyUtils.checkForTestProxyErrors;
import static com.azure.v2.core.test.utils.TestProxyUtils.createAddSanitizersRequest;
import static com.azure.v2.core.test.utils.TestProxyUtils.getAssetJsonFile;
import static com.azure.v2.core.test.utils.TestProxyUtils.getMatcherRequests;
import static com.azure.v2.core.test.utils.TestProxyUtils.getRemoveSanitizerRequest;
import static com.azure.v2.core.test.utils.TestProxyUtils.loadSanitizers;

/**
 * A {@link HttpClient} that plays back test recordings from the external test proxy.
 */
public class TestProxyPlaybackClient implements HttpClient {

    private static final ClientLogger LOGGER = new ClientLogger(TestProxyPlaybackClient.class);
    private final HttpClient client;
    private final URI proxyUri;
    private String xRecordingId;
    private String xRecordingFileLocation;

    private static final List<TestProxySanitizer> DEFAULT_SANITIZERS = loadSanitizers();
    private final List<TestProxySanitizer> sanitizers = new ArrayList<>();

    private final List<TestProxyRequestMatcher> matchers = new ArrayList<>();
    private final boolean skipRecordingRequestBody;

    /**
     * Create an instance of {@link TestProxyPlaybackClient} with a list of custom sanitizers.
     *
     * @param httpClient The {@link HttpClient} to use. If none is passed {@link HttpUrlConnectionHttpClient} is the
     * default.
     * @param skipRecordingRequestBody Flag indicating to skip recording request bodies, so to set a custom matcher to
     * skip comparing bodies when run in playback.
     */
    public TestProxyPlaybackClient(HttpClient httpClient, boolean skipRecordingRequestBody) {
        this.client = (httpClient == null ? new HttpUrlConnectionHttpClient() : httpClient);
        this.proxyUri = TestProxyUtils.getProxyUri();
        this.sanitizers.addAll(DEFAULT_SANITIZERS);
        this.skipRecordingRequestBody = skipRecordingRequestBody;
    }

    /**
     * Starts playback of a test recording.
     *
     * @param recordFile The name of the file to read.
     * @return A {@link Queue} representing the variables in the recording.
     * @param testClassPath the test class path
     * @throws UncheckedIOException if an {@link IOException} is thrown.
     * @throws RuntimeException Failed to serialize body payload.
     */
    public Queue<String> startPlayback(File recordFile, Path testClassPath) {
        HttpRequest request;
        String assetJsonPath = getAssetJsonFile(recordFile, testClassPath);
        try {
            request = new HttpRequest().setMethod(HttpMethod.POST)
                .setUri(proxyUri + "/playback/start")
                .setBody(BinaryData.fromObject(new RecordFilePayload(recordFile.toString(), assetJsonPath)));
            request.getHeaders()
                .set(HttpHeaderName.ACCEPT, "application/json")
                .set(HttpHeaderName.CONTENT_TYPE, "application/json");
            Response<BinaryData> response = sendRequestWithRetries(request);
            checkForTestProxyErrors(response);
            xRecordingId = response.getHeaders().getValue(X_RECORDING_ID);
            xRecordingFileLocation
                = new String(Base64.getUrlDecoder().decode(response.getHeaders().getValue(X_RECORDING_FILE_LOCATION)),
                    StandardCharsets.UTF_8);
            addProxySanitization(this.sanitizers);
            addMatcherRequests(this.matchers);
            // The test proxy stores variables in a map with no guaranteed order.
            // The Java implementation of recording did not use a map, but relied on the order
            // of the variables as they were stored. Our scheme instead sets an increasing integer
            // the key. See TestProxyRecordPolicy.serializeVariables.
            // This deserializes the map returned from the test proxy and creates an ordered list
            // based on the key.
            try (JsonReader jsonReader = JsonReader.fromBytes(response.getValue().toBytes())) {
                Map<String, String> variables = jsonReader.readMap(JsonReader::getString);
                List<Map.Entry<String, String>> toSort;
                if (variables == null) {
                    toSort = new ArrayList<>();
                } else {
                    toSort = new ArrayList<>(variables.entrySet());
                    toSort.sort(Comparator.comparingInt(e -> Integer.parseInt(e.getKey())));
                }

                LinkedList<String> strings = new LinkedList<>();
                for (Map.Entry<String, String> stringStringEntry : toSort) {
                    String value = stringStringEntry.getValue();
                    strings.add(value);
                }
                return strings;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Response<BinaryData> sendRequestWithRetries(HttpRequest request) throws IOException {
        int retries = 0;
        while (true) {
            try {
                Response<BinaryData> response = client.send(request);
                if (response.getStatusCode() / 100 != 2) {
                    throw new RuntimeException("Test proxy returned a non-successful status code. "
                        + response.getStatusCode() + "; response: " + response.getValue());
                }
                return response;
            } catch (Exception e) {
                retries++;
                if (retries >= 3) {
                    throw e;
                }
                sleep(1);
                LOGGER.atWarning().addKeyValue("retryAttempt", retries).log("Retrying request to test proxy.");
            }
        }
    }

    private void sleep(int durationInSeconds) {
        try {
            TimeUnit.SECONDS.sleep(durationInSeconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stops playback of a test recording.
     *
     * @throws IOException If an error occurs while sending the request.
     */
    public void stopPlayback() throws IOException {
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST).setUri(proxyUri + "/playback/stop");
        request.getHeaders().set(X_RECORDING_ID, xRecordingId);
        sendRequestWithRetries(request).close();
    }

    /**
     * Method is invoked before the request is sent.
     *
     * @param request The request context.
     * @throws RuntimeException if playback was started before request is sent.
     */
    private void beforeSendingRequest(HttpRequest request) {
        if (xRecordingId == null) {
            throw new RuntimeException("Playback was not started before a request was sent.");
        }
        TestProxyUtils.changeHeaders(request, proxyUri, xRecordingId, "playback", false);
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

    /**
     * Redirects the request to the test-proxy to retrieve the playback response synchronously.
     * @param request The HTTP request to send.
     * @return The HTTP response.
     * @throws CoreException If an error occurs while sending the request.
     */
    @Override
    public Response<BinaryData> send(HttpRequest request) {
        beforeSendingRequest(request);
        Response<BinaryData> response = client.send(request);
        return afterReceivedResponse(response);
    }

    /**
     * Add a list of {@link TestProxySanitizer} to the current playback session.
     * @param sanitizers The sanitizers to add.
     * @throws IOException If an error occurs while sending the request.
     */
    public void addProxySanitization(List<TestProxySanitizer> sanitizers) throws IOException {
        if (isPlayingBack()) {
            HttpRequest request = createAddSanitizersRequest(sanitizers, proxyUri);
            request.getHeaders().set(X_RECORDING_ID, xRecordingId);

            sendRequestWithRetries(request).close();
        } else {
            this.sanitizers.addAll(sanitizers);
        }
    }

    /**
     * Removes the list of sanitizers from the current playback session.
     * @param sanitizers The sanitizers to remove.
     * @throws IOException If an error occurs while sending the request.
     */
    public void removeProxySanitization(List<String> sanitizers) throws IOException {
        if (isPlayingBack()) {
            Map<String, List<String>> data = new HashMap<>();
            data.put("Sanitizers", sanitizers);

            HttpRequest request;
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JsonWriter jsonWriter = JsonWriter.toStream(outputStream)) {
                jsonWriter.writeMap(data, (writer, value) -> writer.writeArray(value, JsonWriter::writeString)).flush();
                request = getRemoveSanitizerRequest().setBody(BinaryData.fromBytes(outputStream.toByteArray()));
                request.getHeaders().set(X_RECORDING_ID, xRecordingId);
            }

            sendRequestWithRetries(request).close();
        }
    }

    /**
     * Add a list of {@link TestProxyRequestMatcher} to the current playback session.
     * @param matchers The matchers to add.
     * @throws IOException If an error occurs while sending the request.
     */
    public void addMatcherRequests(List<TestProxyRequestMatcher> matchers) throws IOException {
        if (isPlayingBack()) {
            List<HttpRequest> matcherRequests = getMatcherRequests(matchers, proxyUri);
            if (skipRecordingRequestBody) {
                matcherRequests.add(TestProxyUtils.setCompareBodiesMatcher());
            }

            for (HttpRequest request : matcherRequests) {
                request.getHeaders().set(X_RECORDING_ID, xRecordingId);
                sendRequestWithRetries(request).close();
            }
        } else {
            this.matchers.addAll(matchers);
        }
    }

    private boolean isPlayingBack() {
        return xRecordingId != null;
    }

    /**
     * Get the recording file location in assets repo.
     * @return the assets repo location of the recording file.
     */
    public String getRecordingFileLocation() {
        return xRecordingFileLocation;
    }
}
