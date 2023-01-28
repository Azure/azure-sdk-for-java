// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import static com.azure.core.test.utils.TestProxyUtils.getSanitizerRequests;
import static com.azure.core.test.utils.TestProxyUtils.loadSanitizers;

/**
 * A {@link HttpClient} that plays back test recordings from the external test proxy.
 */
public class TestProxyPlaybackClient implements HttpClient {

    private final HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();
    private String xRecordingId;
    private static final SerializerAdapter SERIALIZER = new JacksonAdapter();

    private static final List<TestProxySanitizer> DEFAULT_SANITIZERS = loadSanitizers();
    private final List<TestProxySanitizer> sanitizers = new ArrayList<>();

    public TestProxyPlaybackClient(List<TestProxySanitizer> customSanitizers) {
        this.sanitizers.addAll(DEFAULT_SANITIZERS);
        this.sanitizers.addAll(customSanitizers == null ? Collections.emptyList() : customSanitizers);
    }

    /**
     * Starts playback of a test recording.
     * @param recordFile The name of the file to read.
     * @return A {@link Queue} representing the variables in the recording.
     * @throws RuntimeException if an {@link IOException} is thrown.
     */
    public Queue<String> startPlayback(String recordFile) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/playback/start", TestProxyUtils.getProxyUrl()))
            .setBody(String.format("{\"x-recording-file\": \"%s\"}", recordFile));
        try (HttpResponse response = client.sendSync(request, Context.NONE)) {
            xRecordingId = response.getHeaderValue("x-recording-id");
            addProxySanitization();
            String body = response.getBodyAsString().block();
            // The test proxy stores variables in a map with no guaranteed order.
            // The Java implementation of recording did not use a map, but relied on the order
            // of the variables as they were stored. Our scheme instead sets an increasing integer
            // the key. See TestProxyRecordPolicy.serializeVariables.
            // This deserializes the map returned from the test proxy and creates an ordered list
            // based on the key.
            return SERIALIZER.<Map<String, String>>deserialize(body, Map.class, SerializerEncoding.JSON).entrySet().stream().sorted(Comparator.comparingInt(e -> Integer.parseInt(e.getKey()))).map(Map.Entry::getValue).collect(Collectors.toCollection(LinkedList::new));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stops playback of a test recording.
     */
    public void stopPlayback() {
        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/playback/stop", TestProxyUtils.getProxyUrl()))
            .setHeader("x-recording-id", xRecordingId);
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
        TestProxyUtils.changeHeaders(request, xRecordingId, "playback");
        return client.send(request);
    }

    private void addProxySanitization() {
        getSanitizerRequests(this.sanitizers)
            .forEach(request -> {
                request.setHeader("x-recording-id", xRecordingId);
                client.sendSync(request, Context.NONE);
            });
    }
}
