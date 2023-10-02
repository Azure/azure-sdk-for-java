// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.http;

import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.test.models.RecordFilePayload;
import com.typespec.core.test.models.TestProxyRequestMatcher;
import com.typespec.core.test.models.TestProxySanitizer;
import com.typespec.core.test.utils.HttpURLConnectionHttpClient;
import com.typespec.core.test.utils.TestProxyUtils;
import com.typespec.core.util.Context;
import com.typespec.core.util.serializer.JacksonAdapter;
import com.typespec.core.util.serializer.SerializerAdapter;
import com.typespec.core.util.serializer.SerializerEncoding;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static com.typespec.core.test.implementation.TestingHelpers.X_RECORDING_FILE_LOCATION;
import static com.typespec.core.test.implementation.TestingHelpers.X_RECORDING_ID;
import static com.typespec.core.test.utils.TestProxyUtils.checkForTestProxyErrors;
import static com.typespec.core.test.utils.TestProxyUtils.getAssetJsonFile;
import static com.typespec.core.test.utils.TestProxyUtils.getMatcherRequests;
import static com.typespec.core.test.utils.TestProxyUtils.getSanitizerRequests;
import static com.typespec.core.test.utils.TestProxyUtils.loadSanitizers;

/**
 * A {@link HttpClient} that plays back test recordings from the external test proxy.
 */
public class TestProxyPlaybackClient implements HttpClient {

    private final HttpClient client;
    private final URL proxyUrl;
    private String xRecordingId;
    private String xRecordingFileLocation;
    private static final SerializerAdapter SERIALIZER = new JacksonAdapter();

    private static final List<TestProxySanitizer> DEFAULT_SANITIZERS = loadSanitizers();
    private final List<TestProxySanitizer> sanitizers = new ArrayList<>();

    private final List<TestProxyRequestMatcher> matchers = new ArrayList<>();
    private final boolean skipRecordingRequestBody;

    /**
     * Create an instance of {@link TestProxyPlaybackClient} with a list of custom sanitizers.
     *
     * @param httpClient The {@link HttpClient} to use. If none is passed {@link HttpURLConnectionHttpClient} is the default.
     * @param skipRecordingRequestBody Flag indicating to skip recording request bodies, so to set a custom matcher to skip comparing bodies when run in playback.
     */
    public TestProxyPlaybackClient(HttpClient httpClient, boolean skipRecordingRequestBody) {
        this.client = (httpClient == null ? new HttpURLConnectionHttpClient() : httpClient);
        this.proxyUrl = TestProxyUtils.getProxyUrl();
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
        HttpRequest request = null;
        String assetJsonPath = getAssetJsonFile(recordFile, testClassPath);
        try {
            request = new HttpRequest(HttpMethod.POST, String.format("%s/playback/start", proxyUrl))
                .setBody(SERIALIZER.serialize(new RecordFilePayload(recordFile.toString(), assetJsonPath),
                    SerializerEncoding.JSON))
                .setHeader(HttpHeaderName.ACCEPT, "application/json")
                .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (HttpResponse response = client.sendSync(request, Context.NONE)) {
            checkForTestProxyErrors(response);
            xRecordingId = response.getHeaderValue(X_RECORDING_ID);
            xRecordingFileLocation
                = new String(Base64.getUrlDecoder().decode(
                    response.getHeaders().get(X_RECORDING_FILE_LOCATION).getValue()), StandardCharsets.UTF_8);
            addProxySanitization(this.sanitizers);
            addMatcherRequests(this.matchers);
            String body = response.getBodyAsString().block();
            // The test proxy stores variables in a map with no guaranteed order.
            // The Java implementation of recording did not use a map, but relied on the order
            // of the variables as they were stored. Our scheme instead sets an increasing integer
            // the key. See TestProxyRecordPolicy.serializeVariables.
            // This deserializes the map returned from the test proxy and creates an ordered list
            // based on the key.
            Map<String, String> variables = SERIALIZER.deserialize(body, Map.class, SerializerEncoding.JSON);
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
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Stops playback of a test recording.
     */
    public void stopPlayback() {
        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/playback/stop", proxyUrl.toString()))
            .setHeader(X_RECORDING_ID, xRecordingId);
        client.sendSync(request, Context.NONE);
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
        TestProxyUtils.changeHeaders(request, proxyUrl, xRecordingId, "playback", false);
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

    /**
     * Redirects the request to the test-proxy to retrieve the playback response.
     * @param request The HTTP request to send.
     * @return The HTTP response.
     */
    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        beforeSendingRequest(request);
        return client.send(request).map(this::afterReceivedResponse);
    }

    /**
     * Redirects the request to the test-proxy to retrieve the playback response synchronously.
     * @param request The HTTP request to send.
     * @return The HTTP response.
     */
    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        beforeSendingRequest(request);
        HttpResponse response = client.sendSync(request, context);
        return afterReceivedResponse(response);
    }

    /**
     * Add a list of {@link TestProxySanitizer} to the current playback session.
     * @param sanitizers The sanitizers to add.
     */
    public void addProxySanitization(List<TestProxySanitizer> sanitizers) {
        if (isPlayingBack()) {
            getSanitizerRequests(sanitizers, proxyUrl)
                .forEach(request -> {
                    request.setHeader(X_RECORDING_ID, xRecordingId);
                    client.sendSync(request, Context.NONE);
                });
        } else {
            this.sanitizers.addAll(sanitizers);
        }
    }

    /**
     * Add a list of {@link TestProxyRequestMatcher} to the current playback session.
     * @param matchers The matchers to add.
     */
    public void addMatcherRequests(List<TestProxyRequestMatcher> matchers) {
        if (isPlayingBack()) {
            List<HttpRequest> matcherRequests = getMatcherRequests(matchers, proxyUrl);
            if (skipRecordingRequestBody) {
                matcherRequests.add(TestProxyUtils.setCompareBodiesMatcher());
            }
            matcherRequests.forEach(request -> {
                request.setHeader(X_RECORDING_ID, xRecordingId);
                client.sendSync(request, Context.NONE);
            });
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
