// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.utils;

import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpHeader;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.util.BinaryData;
import com.typespec.core.util.Context;
import com.typespec.core.util.Contexts;
import com.typespec.core.util.CoreUtils;
import com.typespec.core.util.FluxUtil;
import com.typespec.core.util.ProgressReporter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
            createConnection(connection, request, context);

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
            createConnection(connection, request, context);
            return Mono.just(createHttpResponse(connection, request));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    private void createConnection(HttpURLConnection connection, HttpRequest request, Context context) throws IOException {
        connection.setRequestMethod(request.getHttpMethod().name());
        setHeadersOnRequest(request, connection);
        setBodyOnRequest(request, connection, Contexts.with(context).getHttpRequestProgressReporter());
        connection.setInstanceFollowRedirects(false);
        connection.connect();
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

                header.getValuesList().forEach(value -> connection.addRequestProperty(name, value));
            }
        }
    }

    private static class HttpURLResponse extends HttpResponse {
        private final HttpURLConnection connection;
        private final byte[] body;

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
                if (connection.getResponseCode() >= 100 && connection.getResponseCode() < 400) {
                    InputStream inputStream = connection.getInputStream();
                    body = readResponseBytes(inputStream);
                } else {
                    InputStream inputStream = connection.getErrorStream();
                    body = readResponseBytes(inputStream);
                }
            } catch (IOException e) {
                // Handle connection exception and retrieve error information
                int responseCode = -1;
                String responseMessage = "Unknown error";
                try {
                    responseCode = connection.getResponseCode();
                    responseMessage = connection.getResponseMessage();
                } catch (IOException ignored) {

                }

                throw new UncheckedIOException(String.format("Connection failed: %s, %s", responseCode,
                    responseMessage), e);
            }
        }

        private static byte[] readResponseBytes(InputStream inputStream) throws IOException {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            bufferedInputStream.close();
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
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
            return Mono.fromSupplier(() -> ByteBuffer.wrap(body)).flux();
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return Mono.just(body);
        }

        @Override
        public Mono<String> getBodyAsString() {
            return Mono.just(CoreUtils.bomAwareToString(body, getHeaderValue(HttpHeaderName.CONTENT_TYPE)));
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return Mono.just(new String(body, charset));
        }

        @Override
        public BinaryData getBodyAsBinaryData() {
            return BinaryData.fromBytes(body);
        }

        @Override
        public Mono<InputStream> getBodyAsInputStream() {
            return Mono.fromSupplier(() -> new ByteArrayInputStream(body));
        }

        @Override
        public Mono<Void> writeBodyToAsync(AsynchronousByteChannel channel) {
            return FluxUtil.writeToAsynchronousByteChannel(getBody(), channel);
        }

        @Override
        public void writeBodyTo(WritableByteChannel channel) throws IOException {
            channel.write(ByteBuffer.wrap(body));
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
