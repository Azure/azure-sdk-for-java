// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.data.tables.implementation.models.TransactionalBatchChangeSet;
import com.azure.data.tables.implementation.models.TransactionalBatchSubRequest;
import com.azure.data.tables.implementation.models.MultipartPart;
import com.azure.data.tables.implementation.models.TableServiceError;
import com.azure.data.tables.models.TableTransactionActionResponse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializer for Multipart requests and responses.
 */
public class TablesMultipartSerializer extends TablesJacksonSerializer {
    private static final String BOUNDARY_DELIMETER = "--";
    private final ClientLogger logger = new ClientLogger(TablesMultipartSerializer.class);

    private static class TableTransactionActionResponseBuilder {
        private int statusCode;
        private Object value;
        private final HttpHeaders headers = new HttpHeaders();

        TableTransactionActionResponse build() {
            TableTransactionActionResponse response =
                ModelHelper.createTableTransactionActionResponse(statusCode, value);

            headers.forEach(h -> response.getHeaders().set(h.getName(), h.getValue()));

            return response;
        }

        void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        void setValue(Object value) {
            this.value = value;
        }

        void putHeader(String name, String value) {
            this.headers.set(name, value);
        }
    }

    @Override
    public void serialize(Object object, SerializerEncoding encoding, OutputStream outputStream) throws IOException {
        if (object instanceof MultipartPart<?>) {
            writeMultipartPart(object, encoding, outputStream);
        } else if (object instanceof TransactionalBatchSubRequest) {
            writeRequest(object, outputStream);
        } else {
            super.serialize(object, encoding, outputStream);
        }
    }

    @Override
    public String serialize(Object object, SerializerEncoding encoding) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        serialize(object, encoding, stream);

        return stream.toString(StandardCharsets.UTF_8.name());
    }

    @Override
    public byte[] serializeToBytes(Object object, SerializerEncoding encoding) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        serialize(object, encoding, stream);

        return stream.toByteArray();
    }

    private void writeMultipartPart(Object object, SerializerEncoding encoding, OutputStream os) throws IOException {
        MultipartPart<?> part = (MultipartPart<?>) object;

        if (part instanceof TransactionalBatchChangeSet) {
            write("Content-Type: " + part.getContentType() + "\r\n\r\n", os);
        }

        for (Object content : part.getContents()) {
            write(BOUNDARY_DELIMETER + part.getBoundary() + "\r\n", os);
            serialize(content, encoding, os);
            write("\r\n", os);
        }

        write(BOUNDARY_DELIMETER + part.getBoundary() + BOUNDARY_DELIMETER + "\r\n", os);
    }

    private void writeRequest(Object object, OutputStream os) throws IOException {
        HttpRequest request = ((TransactionalBatchSubRequest) object).getHttpRequest();
        String method = request.getHttpMethod() == HttpMethod.PATCH ? "MERGE" : request.getHttpMethod().toString();

        write("Content-Type: application/http\r\n", os);
        write("Content-Transfer-Encoding: binary\r\n\r\n", os);
        write(method + " " + request.getUrl().toString() + " HTTP/1.1\r\n", os);

        for (HttpHeader header : request.getHeaders()) {
            if (!"x-ms-version".equalsIgnoreCase(header.getName())) {
                write(header.getName() + ": " + header.getValue() + "\r\n", os);
            }
        }

        write("\r\n", os);

        if (request.getBody() != null) {
            byte[] bodyBytes = FluxUtil.collectBytesInByteBufferStream(request.getBody()).block();
            if (bodyBytes != null) {
                os.write(bodyBytes);
            }
        }
    }

    private void write(String s, OutputStream os) throws IOException {
        os.write(s.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public <U> U deserialize(String value, Type type, SerializerEncoding serializerEncoding) throws IOException {
        return deserialize(value.getBytes(StandardCharsets.UTF_8), type, serializerEncoding);
    }

    @Override
    public <U> U deserialize(byte[] bytes, Type type, SerializerEncoding encoding) throws IOException {
        if (type == TableTransactionActionResponse.class) {
            return deserialize(new ByteArrayInputStream(bytes), type, encoding);
        } else {
            return super.deserialize(bytes, type, encoding);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> U deserialize(InputStream inputStream, Type type, SerializerEncoding serializerEncoding) throws IOException {
        if (type == TableTransactionActionResponse[].class) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line = reader.readLine();

            if (line == null || !line.startsWith(BOUNDARY_DELIMETER + "batchresponse_")) {
                throw logger.logExceptionAsError(new IllegalStateException("Invalid multipart response."));
            }

            TableTransactionActionResponseBuilder responseParams = null;
            boolean foundBody = false;
            String body = null;
            List<TableTransactionActionResponse> responses = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(BOUNDARY_DELIMETER + "changesetresponse_")) {
                    if (responseParams != null) {
                        if (body != null && !body.isEmpty()) {
                            try {
                                responseParams.setValue(deserialize(body, TableServiceError.class, serializerEncoding));
                            } catch (IOException e) {
                                logger.logThrowableAsWarning(
                                    new IOException("Unable to deserialize sub-response body.", e));
                                responseParams.setValue(body);
                            }
                        }
                        responses.add(responseParams.build());
                    }
                    responseParams = null;
                    foundBody = false;
                    body = null;
                } else if (responseParams == null && line.startsWith("HTTP/1.1")) {
                    responseParams = new TableTransactionActionResponseBuilder();
                    // Characters 9-12 in the first line of the HTTP response are the status code.
                    responseParams.setStatusCode(Integer.parseInt(line.substring(9, 12)));
                } else if (responseParams != null && !foundBody) {
                    if (line.isEmpty()) {
                        // An empty line after the headers delimits the body.
                        foundBody = true;
                    } else {
                        // A header line
                        String[] header = line.split(":", 2);
                        responseParams.putHeader(header[0].trim(), header[1].trim());
                    }
                } else if (responseParams != null && !line.isEmpty()) {
                    // The rest of the lines constitute the body until we get to the delimiter again.
                    body = (body == null ? line : body + line) + "\r\n";
                }
            }

            return (U) responses.toArray(new TableTransactionActionResponse[0]);
        } else {
            return super.deserialize(inputStream, type, serializerEncoding);
        }
    }
}
