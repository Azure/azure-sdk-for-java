// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link HttpClient} using the JDK {@link HttpURLConnectionHttpClient} for communicating with
 * the test proxy.
 */
public class HttpURLConnectionHttpClient implements HttpClient {

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        HttpURLConnection connection = null;

        try {

            connection = (HttpURLConnection) request.getUrl().openConnection();
            connection.setRequestMethod(request.getHttpMethod().toString());

            HttpHeaders headers = request.getHeaders();
            if (headers != null) {
                for (HttpHeader header : headers) {
                    String name = header.getName();

                    connection.setRequestProperty(name, headers.getValue(name));
                }
            }
            BinaryData body = request.getBodyAsBinaryData();
            if (body != null) {
                connection.setDoOutput(true);
                try (BufferedOutputStream stream = new BufferedOutputStream(connection.getOutputStream())) {
                    stream.write(body.toBytes());
                    stream.flush();
                }
            }
            connection.connect();
            return Mono.just(createHttpResponse(connection));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpResponse createHttpResponse(HttpURLConnection connection) {

        if (connection == null) {
            return null;
        }


        return new HttpURLResponse(connection);
    }

    private static class HttpURLResponse extends HttpResponse {
        private final HttpURLConnection connection;
        private final ByteBuffer body;

        /**
         * Creates an instance of {@link HttpResponse}.
         *
         * @param request The {@link HttpRequest} that resulted in this {@link HttpResponse}.
         */
        protected HttpURLResponse(HttpRequest request) {
            super(request);
            this.connection = null;
            this.body = null;
        }

        /**
         * Constructor for HttpURLResponse
         * @param connection The {@link HttpURLConnection} to create a {@link HttpResponse} from.
         * @throws RuntimeException if a failure occurs reading the body of the response.
         */
        HttpURLResponse(HttpURLConnection connection) {
            super(null);
            this.connection = connection;
            try {
                byte[] bytes = BinaryData.fromStream(connection.getInputStream()).toBytes();
                this.body = ByteBuffer.wrap(bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

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
            for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
                for (String value : entry.getValue()) {
                    ret.add(entry.getKey(), value);
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
