package com.azure.core.test.policy;

import com.azure.core.http.*;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.UrlBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ProxyRecordPolicy implements HttpPipelinePolicy {
    private final String proxyUrlScheme = "http";
    private final String proxyUrlHost = "localhost";
    private final int proxyUrlPort = 5000;
    private final String proxyUrl = String.format("%s://%s:%d", proxyUrlScheme, proxyUrlHost, proxyUrlPort);
    private String xRecordingId;
    private String recordFile;
    public ProxyRecordPolicy(List<Function<String, String>> redactors) {
        // TODO: redactors
    }

    public void startRecording(String recordFile) {
        HttpResponse response = send(String.format("%s/record/start", proxyUrl),
            String.format("{\"x-recording-file\": \"%s\"}", recordFile).getBytes(StandardCharsets.UTF_8),
            null,
            HttpMethod.POST);
        xRecordingId = response.getHeaderValue("x-recording-id");
    }

    public void stopRecording(Map<String, String> variables) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-recording-id", xRecordingId);
        send(String.format("%s/record/start", proxyUrl), null, headers, HttpMethod.POST);
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpRequest request = context.getHttpRequest().copy();
        UrlBuilder builder = UrlBuilder.parse(request.getUrl());
        builder.setScheme(proxyUrlScheme);
        builder.setHost(proxyUrlHost);
        builder.setPort(proxyUrlPort);
        String url = builder.toString();

        HttpHeaders headers = request.getHeaders();
        headers.add("x-recording-upstream-base-uri", request.getUrl().toString());
        headers.add("x-recording-mode", "record");
        headers.add("x-recording-id", xRecordingId);
        HttpResponse response = send(url,
            request.getBodyAsBinaryData() != null ? request.getBodyAsBinaryData().toBytes() : null,
            headers,
            request.getHttpMethod());
        return Mono.just(response);
    }

    private HttpResponse send(String url, byte[] body, HttpHeaders headers, HttpMethod method) {
        HttpURLConnection connection = null;

        try {
            URL target = new URL(url);
            connection = (HttpURLConnection) target.openConnection();
            connection.setRequestMethod(method.toString());
            if(body != null) {
                connection.setDoOutput(true);
                BufferedOutputStream stream = new BufferedOutputStream(connection.getOutputStream());
                stream.write(body);
                stream.flush();
            }
            if (headers != null) {
                for (HttpHeader header : headers) {
                    String name = header.getName();
                    connection.setRequestProperty(name, headers.getValue(name));
                }
            }
            connection.connect();
            return createHttpResponse(connection);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null ) { connection.disconnect(); }
        }
    }

    private HttpResponse createHttpResponse(HttpURLConnection connection) {

        if (connection == null) {
            return null;
        }

        ByteBuffer body;
        try {
            body = ByteBuffer.wrap(connection.getInputStream().readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new HttpResponse(null) {
            @Override
            public int getStatusCode() {
                try {
                    return connection.getResponseCode();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String getHeaderValue(String name) {
                return connection.getHeaderField(name);
            }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders ret = new HttpHeaders();
                Map<String, List<String>> headers = connection.getHeaderFields();
                for (String key : headers.keySet()) {
                    for (String value : headers.get(key)) {
                        ret.add(key, value);
                    }
                }
                return ret;
            }

            @Override
            public Flux<ByteBuffer> getBody() {
                return Flux.just(body);
            }

            @Override
            public Mono<byte[]> getBodyAsByteArray() {
                return Mono.just(body.array());
            }

            @Override
            public Mono<String> getBodyAsString() {
                return Mono.just(new String(body.array(), StandardCharsets.UTF_8));
            }

            @Override
            public Mono<String> getBodyAsString(Charset charset) {
                return Mono.just(new String(body.array(), charset));
            }
        };
    }
}
