// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test.utils;

import com.azure.v2.core.util.ProgressReporter;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.utils.binarydata.BinaryData;
import io.clientcore.core.utils.binarydata.ByteArrayBinaryData;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;

/**
 * A {@link HttpClient} that uses the JDK {@link HttpURLConnection}.
 */
public class HttpUrlConnectionHttpClient implements HttpClient {
    /**
     * Creates an instance of {@link HttpUrlConnectionHttpClient}.
     */
    public HttpUrlConnectionHttpClient() {
    }

    @Override
    public Response<?> send(HttpRequest request) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) request.getUri().toURL().openConnection();
            createConnection(connection, request);

            return createHttpResponse(connection, request);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void createConnection(HttpURLConnection connection, HttpRequest request) throws IOException {
        connection.setRequestMethod(request.getHttpMethod().name());
        setHeadersOnRequest(request, connection);
        setBodyOnRequest(request, connection, null);
        connection.setInstanceFollowRedirects(false);
        connection.connect();
    }

    private Response<?> createHttpResponse(HttpURLConnection connection, HttpRequest request) {

        if (connection == null) {
            return null;
        }

        return new HttpURLResponse(connection, request);
    }

    private static void setBodyOnRequest(HttpRequest request, HttpURLConnection connection,
        ProgressReporter progressReporter) {
        try {
            BinaryData body = request.getBody();
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
                String name = header.getName().getCaseSensitiveName();

                header.getValues().forEach(value -> connection.addRequestProperty(name, value));
            }
        }
    }

    private static class HttpURLResponse implements Response<BinaryData> {
        private final HttpRequest request;
        private final HttpURLConnection connection;
        private final BinaryData body;

        /**
         * Creates an instance of {@link Response}.
         *
         * @param request The {@link HttpRequest} that resulted in this {@link Response}.
         */
        protected HttpURLResponse(HttpRequest request) {
            this.request = request;
            this.connection = null;
            this.body = null;
        }

        /**
         * Constructor for HttpURLResponse
         *
         * @param connection The {@link HttpURLConnection} to create a {@link Response} from.
         * @param request The {@link HttpRequest} that resulted in this {@link Response}.
         * @throws UncheckedIOException if a failure occurs.
         */
        HttpURLResponse(HttpURLConnection connection, HttpRequest request) {
            this.request = request;
            this.connection = connection;
            try {
                if (connection.getResponseCode() >= 100 && connection.getResponseCode() < 400) {
                    InputStream inputStream = connection.getInputStream();
                    body = new ByteArrayBinaryData(readResponseBytes(inputStream));
                } else {
                    InputStream inputStream = connection.getErrorStream();
                    body = new ByteArrayBinaryData(readResponseBytes(inputStream));
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

                throw new UncheckedIOException(
                    String.format("Connection failed: %s, %s", responseCode, responseMessage), e);
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
        public HttpHeaders getHeaders() {
            HttpHeaders headers = new HttpHeaders();
            connection.getHeaderFields().forEach((key, value) -> headers.add(HttpHeaderName.fromString(key), value));
            return headers;
        }

        @Override
        public HttpRequest getRequest() {
            return request;
        }

        @Override
        public BinaryData getValue() {
            return body;
        }

        @Override
        public BinaryData getBody() {
            return body;
        }

        @Override
        public void close() {
            connection.disconnect();
        }
    }
}
