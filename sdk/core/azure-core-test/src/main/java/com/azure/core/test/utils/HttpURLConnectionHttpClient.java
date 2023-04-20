// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A {@link HttpClient} that uses the JDK {@link HttpURLConnection}.
 */
public class HttpURLConnectionHttpClient implements HttpClient {

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) request.getUrl().openConnection();
            connection.setRequestMethod(request.getHttpMethod().name());

            setHeadersOnRequest(request, connection);
            setBodyOnRequest(request, connection);
            connection.connect();

            return createHttpResponse(connection, request);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) request.getUrl().openConnection();
            connection.setRequestMethod(request.getHttpMethod().toString());

            setHeadersOnRequest(request, connection);
            setBodyOnRequest(request, connection);
            connection.connect();
            return Mono.just(createHttpResponse(connection, request));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpResponse createHttpResponse(HttpURLConnection connection, HttpRequest request) {

        if (connection == null) {
            return null;
        }

        return new HttpURLResponse(connection, request);
    }

    private static void setBodyOnRequest(HttpRequest request, HttpURLConnection connection) {
        try {
            BinaryData body = request.getBodyAsBinaryData();
            if (body != null) {
                connection.setDoOutput(true);
                try (BufferedOutputStream stream = new BufferedOutputStream(connection.getOutputStream())) {
                    stream.write(body.toBytes());
                    stream.flush();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void setHeadersOnRequest(HttpRequest request, HttpURLConnection connection) {
        HttpHeaders headers = request.getHeaders();
        if (headers != null) {
            for (HttpHeader header : headers) {
                String name = header.getName();

                connection.setRequestProperty(name, header.getValue());
            }
        }
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
         *
         * @param connection The {@link HttpURLConnection} to create a {@link HttpResponse} from.
         * @param request The {@link HttpRequest} that resulted in this {@link HttpResponse}.
         * @throws UncheckedIOException if a failure occurs.
         */
        HttpURLResponse(HttpURLConnection connection, HttpRequest request) {
            super(request);
            this.connection = connection;
            try {

                byte[] bytes = null;
                if (connection.getResponseCode() >= 100 && connection.getResponseCode() < 400) {
                    bytes = BinaryData.fromStream(connection.getInputStream()).toBytes();
                } else {
                    InputStream inputStream = connection.getErrorStream();
                    if (inputStream != null) {
                        bytes = BinaryData.fromStream(inputStream).toBytes();
                    }
                }
                if (bytes != null) {
                    this.body = ByteBuffer.wrap(bytes);
                } else {
                    this.body = null;
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
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
        @Deprecated
        public String getHeaderValue(String name) {
            return connection.getHeaderField(name);
        }

        @Override
        public String getHeaderValue(HttpHeaderName headerName) {
            return connection.getHeaderField(headerName.getCaseInsensitiveName());
        }

        @Override
        public HttpHeaders getHeaders() {
            return new HttpHeaders().setAll(connection.getHeaderFields());
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
    }
}
