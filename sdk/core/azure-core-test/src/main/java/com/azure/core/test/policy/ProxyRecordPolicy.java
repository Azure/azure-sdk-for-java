package com.azure.core.test.policy;

import com.azure.core.http.*;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.utils.HttpURLConnectionHttpClient;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ProxyRecordPolicy implements HttpPipelinePolicy {
    private final String proxyUrlScheme = "http";
    private final String proxyUrlHost = "localhost";
    private final int proxyUrlPort = 5000;
    private final String proxyUrl = String.format("%s://%s:%d", proxyUrlScheme, proxyUrlHost, proxyUrlPort);

    private final HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();
    private String xRecordingId;
    private String recordFile;
    public ProxyRecordPolicy(List<Function<String, String>> redactors) {
        // TODO: redactors
    }

    public void startRecording(String recordFile) {
        sendStart(recordFile, "record");
    }

    public void stopRecording(Map<String, String> variables) {
        sendStop("record");
    }

    public void startPlayback(String recordFile) {
        sendStart(recordFile, "playback");
    }

    public void stopPlayback() {
        sendStop("playback");
    }

    private void sendStart(String recordFile, String endpoint) {

        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/%s/start", proxyUrl, endpoint))
            .setBody(String.format("{\"x-recording-file\": \"%s\"}", recordFile));
        HttpResponse response = client.sendSync(request, Context.NONE);

        xRecordingId = response.getHeaderValue("x-recording-id");
    }

    private void sendStop(String endpoint) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/%s/stop", proxyUrl, endpoint))
            .setHeader("x-recording-id", xRecordingId);
        client.sendSync(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpRequest request = context.getHttpRequest();
        URL originalUrl = request.getUrl();
        UrlBuilder builder = UrlBuilder.parse(request.getUrl());
        builder.setScheme(proxyUrlScheme);
        builder.setHost(proxyUrlHost);
        builder.setPort(proxyUrlPort);

        String url = builder.toString();

        HttpHeaders headers = request.getHeaders();
        headers.add("x-recording-upstream-base-uri", originalUrl.toString());
        headers.add("x-recording-mode", "record");
        headers.add("x-recording-id", xRecordingId);
        try {
            request.setUrl(builder.toUrl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return next.process();
    }
}

