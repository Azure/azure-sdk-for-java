package com.azure.core.test.policy;

import com.azure.core.http.*;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.api.DisplayNameGenerator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ProxyRecordPolicy implements HttpPipelinePolicy {
    private final String proxyUrlBase = "http://localhost:5000";
    private String xRecordingId;
    private String recordFile;
    public ProxyRecordPolicy(List<Function<String, String>> redactors) {

    }

    public void startRecording(String recordFile) {
        HttpResponse response = post(String.format("%s/record/start", proxyUrlBase), String.format("{\"x-recording-file\": \"%s\"}", recordFile));
        xRecordingId = response.getHeaderValue("x-recording-id");
    }

    public void stopRecording() {
        post(String.format("%s/record/start", proxyUrlBase), null, getRecordingIdHeader());
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpRequest request = context.getHttpRequest().copy();
        HttpHeader upstreamUri = new HttpHeader("x-recording-upstream-base-uri", request.getUrl().toString());
        HttpHeader recordingHeader = new HttpHeader("x-recording-mode", "record");
        request.setUrl(proxyUrlBase);
        HttpHeaders headers = new HttpHeaders();
        headers.
    }

    private HttpHeader getRecordingIdHeader() {
        return new HttpHeader("x-recording-id", xRecordingId);
    }
    private HttpResponse post(String url, String body) {
        return post(url, body, (HttpHeaders)null);
    }
    private HttpResponse post(String url, String body, HttpHeader header) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(header.getName(), header.getValue());
        return post(url, body, headers);
    }
    private HttpResponse post(String url, String body, HttpHeaders headers) {
        HttpURLConnection connection = null;

        try {
            URL target = new URL(url);
            connection = (HttpURLConnection) target.openConnection();
            connection.setRequestMethod("POST");
            if(body != null) {
                connection.setDoOutput(true);
                BufferedOutputStream stream = new BufferedOutputStream(connection.getOutputStream());
                stream.write(body.getBytes(StandardCharsets.UTF_8));
                stream.flush();
            }
            if (headers != null) {
                for (HttpHeader header : headers) {
                    connection.setRequestProperty(header.getName(), header.getValue());
                }
            }
            connection.connect();
            return createHttpResponse(connection);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
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
