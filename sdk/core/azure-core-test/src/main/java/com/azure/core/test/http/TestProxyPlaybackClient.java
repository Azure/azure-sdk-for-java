// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.utils.HttpURLConnectionHttpClient;
import com.azure.core.test.utils.TestProxyUtils;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static com.azure.core.test.implementation.TestingHelpers.X_RECORDING_ID;
import static com.azure.core.test.utils.TestProxyUtils.checkForTestProxyErrors;
import static com.azure.core.test.utils.TestProxyUtils.getMatcherRequests;
import static com.azure.core.test.utils.TestProxyUtils.getSanitizerRequests;
import static com.azure.core.test.utils.TestProxyUtils.loadSanitizers;

/**
 * A {@link HttpClient} that plays back test recordings from the external test proxy.
 */
public class TestProxyPlaybackClient implements HttpClient {

    private final HttpClient client;
    private final URL proxyUrl;
    private String xRecordingId;
    private static final SerializerAdapter SERIALIZER = new JacksonAdapter();

    private static final List<TestProxySanitizer> DEFAULT_SANITIZERS = loadSanitizers();
    private final List<TestProxySanitizer> sanitizers = new ArrayList<>();

    private final List<TestProxyRequestMatcher> matchers = new ArrayList<>();

    /**
     * Create an instance of {@link TestProxyPlaybackClient} with a list of custom sanitizers.
     *
     * @param httpClient The {@link HttpClient} to use. If none is passed {@link HttpURLConnectionHttpClient} is the default.
     * @param proxyUrl The {@link URL} for the test proxy instance.
     */
    public TestProxyPlaybackClient(HttpClient httpClient, URL proxyUrl) {
        this.client = (httpClient == null ? new HttpURLConnectionHttpClient() : httpClient);
        this.proxyUrl = proxyUrl;
        this.sanitizers.addAll(DEFAULT_SANITIZERS);
    }

    /**
     * Starts playback of a test recording.
     * @param recordFile The name of the file to read.
     * @return A {@link Queue} representing the variables in the recording.
     * @throws UncheckedIOException if an {@link IOException} is thrown.
     */
    public Queue<String> startPlayback(String recordFile) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/playback/start", proxyUrl))
            .setBody(String.format("{\"x-recording-file\": \"%s\"}", recordFile));
        try (HttpResponse response = client.sendSync(request, Context.NONE)) {
            checkForTestProxyErrors(response);
            xRecordingId = response.getHeaderValue(X_RECORDING_ID);
            addProxySanitization(this.sanitizers);
            addMatcherRequests(this.matchers);
            String body = response.getBodyAsString().block();
            // The test proxy stores variables in a map with no guaranteed order.
            // The Java implementation of recording did not use a map, but relied on the order
            // of the variables as they were stored. Our scheme instead sets an increasing integer
            // the key. See TestProxyRecordPolicy.serializeVariables.
            // This deserializes the map returned from the test proxy and creates an ordered list
            // based on the key.
            List<Map.Entry<String, String>> toSort;
            toSort = new ArrayList<>(SERIALIZER.<Map<String, String>>deserialize(body, Map.class, SerializerEncoding.JSON).entrySet());
            toSort.sort(Comparator.comparingInt(e -> Integer.parseInt(e.getKey())));
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
     * Redirects the request to the test-proxy to retrieve the playback response.
     * @param request The HTTP request to send.
     * @return The HTTP response.
     */
    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        if (xRecordingId == null) {
            throw new RuntimeException("Playback was not started before a request was sent.");
        }
        TestProxyUtils.changeHeaders(request, proxyUrl, xRecordingId, "playback");
        return client.send(request).map(response -> {
            TestProxyUtils.checkForTestProxyErrors(response);
            return TestProxyUtils.revertUrl(response);
        });
    }

    /**
     * Redirects the request to the test-proxy to retrieve the playback response synchronously.
     * @param request The HTTP request to send.
     * @return The HTTP response.
     */
    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        if (xRecordingId == null) {
            throw new RuntimeException("Playback was not started before a request was sent.");
        }
        TestProxyUtils.changeHeaders(request, proxyUrl, xRecordingId, "playback");
        HttpResponse response = client.sendSync(request, context);
        TestProxyUtils.checkForTestProxyErrors(response);
        return TestProxyUtils.revertUrl(response);
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
            getMatcherRequests(matchers, proxyUrl)
                .forEach(request -> {
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
}
