// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test.utils;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.models.binarydata.ByteArrayBinaryData;
import io.clientcore.core.utils.ProgressReporter;

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
    public Response<BinaryData> send(HttpRequest request) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) request.getUri().toURL().openConnection();
            createConnection(connection, request);

            return createHttpResponse(connection, request);
        } catch (IOException e) {
            throw CoreException.from(e);
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

    private Response<BinaryData> createHttpResponse(HttpURLConnection connection, HttpRequest request) {

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
            headers.stream().forEach(header -> {
                String name = header.getName().getCaseSensitiveName();

                header.getValues().forEach(value -> connection.addRequestProperty(name, value));
            });
        }
    }

    private static class HttpURLResponse extends Response<BinaryData> {
        private final HttpURLConnection connection;

        /**
         * Constructor for HttpURLResponse
         *
         * @param connection The {@link HttpURLConnection} to create a {@link Response} from.
         * @param request The {@link HttpRequest} that resulted in this {@link Response}.
         */
        HttpURLResponse(HttpURLConnection connection, HttpRequest request) {
            super(request, getResponseCode(connection), createHttpHeaders(connection), getResponseBody(connection));
            this.connection = connection;
        }

        private static int getResponseCode(HttpURLConnection connection) {
            try {
                return connection.getResponseCode();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private static HttpHeaders createHttpHeaders(HttpURLConnection connection) {
            HttpHeaders headers = new HttpHeaders();
            connection.getHeaderFields().forEach((key, value) -> {
                if (key == null) {
                    return;
                }
                headers.add(new HttpHeader(HttpHeaderName.fromString(key), value));
            });
            return headers;
        }

        private static BinaryData getResponseBody(HttpURLConnection connection) {
            try {
                if (connection.getResponseCode() >= 100 && connection.getResponseCode() < 400) {
                    InputStream inputStream = connection.getInputStream();
                    return new ByteArrayBinaryData(readResponseBytes(inputStream));
                } else {
                    InputStream inputStream = connection.getErrorStream();
                    return new ByteArrayBinaryData(readResponseBytes(inputStream));
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
        public void close() {
            connection.disconnect();
        }
    }
}
