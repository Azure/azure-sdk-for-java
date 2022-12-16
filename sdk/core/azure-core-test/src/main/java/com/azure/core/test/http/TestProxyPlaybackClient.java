package com.azure.core.test.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.utils.HttpURLConnectionHttpClient;
import com.azure.core.test.utils.TestProxyUtils;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TestProxyPlaybackClient implements HttpClient {

    private final String proxyUrlScheme = "http";
    private final String proxyUrlHost = "localhost";
    private final int proxyUrlPort = 5000;
    private final String proxyUrl = String.format("%s://%s:%d", proxyUrlScheme, proxyUrlHost, proxyUrlPort);

    private final HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();
    private String xRecordingId;
    private static final SerializerAdapter SERIALIZER = new JacksonAdapter();

    public Queue<String> startPlayback(String recordFile, Map<String, String> textReplacementRules) {
        // TODO: replacement rules
        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/playback/start", proxyUrl))
            .setBody(String.format("{\"x-recording-file\": \"%s\"}", recordFile));
        try (HttpResponse response = client.sendSync(request, Context.NONE)) {
            xRecordingId = response.getHeaderValue("x-recording-id");
            String body = response.getBodyAsString().block();
            return SERIALIZER.<Map<String, String>>deserialize(body, Map.class, SerializerEncoding.JSON).entrySet().stream().sorted(Comparator.comparingInt(e -> Integer.parseInt(e.getKey()))).map(Map.Entry::getValue).collect(Collectors.toCollection(LinkedList::new));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public void stopPlayback() {
        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/playback/stop", proxyUrl))
            .setHeader("x-recording-id", xRecordingId);
        client.sendSync(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        if (xRecordingId == null) {
            throw new RuntimeException("Playback was not started before a request was sent.");
        }
        TestProxyUtils.changeHeaders(request, xRecordingId, "playback");
        return client.send(request);
    }
}
