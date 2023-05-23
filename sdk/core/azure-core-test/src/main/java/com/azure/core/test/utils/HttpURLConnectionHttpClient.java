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
import com.azure.core.util.Contexts;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressReporter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

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
            setBodyOnRequest(request, connection, Contexts.with(context).getHttpRequestProgressReporter());
            connection.setInstanceFollowRedirects(false);
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
        return send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) request.getUrl().openConnection();
            connection.setRequestMethod(request.getHttpMethod().toString());

            setHeadersOnRequest(request, connection);
            setBodyOnRequest(request, connection, Contexts.with(context).getHttpRequestProgressReporter());
            connection.setInstanceFollowRedirects(false);
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

    private static void setBodyOnRequest(HttpRequest request, HttpURLConnection connection,
        ProgressReporter progressReporter) {
        try {
            BinaryData body = request.getBodyAsBinaryData();
            if (body != null) {
                connection.setDoOutput(true);
                try (BufferedOutputStream stream = new BufferedOutputStream(connection.getOutputStream())) {
                    byte[] bodyBytes = body.toBytes();
                    if (progressReporter != null) {
                        progressReporter.reportProgress(bodyBytes.length);
                    }
                    stream.write(bodyBytes);
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
            return Flux.just(body.duplicate());
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return body == null ? Mono.empty() : Mono.just(body.array());
        }

        @Override
        public Mono<String> getBodyAsString() {
            return body == null ? Mono.empty() : Mono.just(CoreUtils.bomAwareToString(body.array(),
                getHeaderValue(HttpHeaderName.CONTENT_TYPE)));
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return body == null ? Mono.empty() : Mono.just(new String(body.array(), charset));
        }

        @Override
        public BinaryData getBodyAsBinaryData() {
            return body == null ? null : BinaryData.fromBytes(body.array());
        }

        @Override
        public Mono<InputStream> getBodyAsInputStream() {
            return body == null ? Mono.empty() : Mono.fromSupplier(() -> new ByteArrayInputStream(body.array()));
        }

        @Override
        public Mono<Void> writeBodyToAsync(AsynchronousByteChannel channel) {
            return FluxUtil.writeToAsynchronousByteChannel(getBody(), channel);
        }

        @Override
        public void writeBodyTo(WritableByteChannel channel) throws IOException {
            channel.write(body.duplicate());
        }

        @Override
        public HttpResponse buffer() {
            return this;
        }

        @Override
        public void close() {
            connection.disconnect();
        }
    }
}
