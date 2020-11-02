// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.data.tables.implementation.models.BatchChangeSet;
import com.azure.data.tables.implementation.models.BatchOperationResponse;
import com.azure.data.tables.implementation.models.BatchSubRequest;
import com.azure.data.tables.implementation.models.MultipartPart;
import com.azure.data.tables.implementation.models.TableServiceError;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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


    @Override
    public void serialize(Object object, SerializerEncoding encoding, OutputStream outputStream) throws IOException {
        if (object instanceof MultipartPart<?>) {
            writeMultipartPart(object, encoding, outputStream);
        } else if (object instanceof BatchSubRequest) {
            writeRequest(object, outputStream);
        } else {
            super.serialize(object, encoding, outputStream);
        }
    }

    private void writeMultipartPart(Object object, SerializerEncoding encoding, OutputStream os) throws IOException {
        MultipartPart<?> part = (MultipartPart<?>) object;

        if (part instanceof BatchChangeSet) {
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
        HttpRequest request = ((BatchSubRequest) object).getHttpRequest();
        String method = request.getHttpMethod().toString();
        String urlPath = request.getUrl().getPath();
        String urlQuery = request.getUrl().getQuery();
        if (!CoreUtils.isNullOrEmpty(urlQuery)) {
            urlPath = urlPath + "?" + urlQuery;
        }

        write("Content-Type: application/http\r\n", os);
        write("Content-Transfer-Encoding: binary\r\n\r\n", os);
        write(method + " " + urlPath + " HTTP/1.1\r\n", os);

        for (HttpHeader header : request.getHeaders()) {
            if (!"x-ms-version".equalsIgnoreCase(header.getName())) {
                write(header.getName() + ": " + header.getValue() + "\r\n", os);
            }
        }

        write("\r\n", os);

        byte[] bodyBytes = FluxUtil.collectBytesInByteBufferStream(request.getBody()).block();
        if (bodyBytes != null) {
            os.write(bodyBytes);
        }
    }

    private void write(String s, OutputStream os) throws IOException {
        os.write(s.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public <U> U deserialize(String value, Type type, SerializerEncoding serializerEncoding) throws IOException {
        if (type == BatchOperationResponse.class) {
            return deserialize(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)), type,
                serializerEncoding);
        } else {
            return super.deserialize(value, type, serializerEncoding);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> U deserialize(InputStream inputStream, Type type, SerializerEncoding serializerEncoding)
        throws IOException {
        if (type == BatchOperationResponse[].class) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            if (!line.startsWith(BOUNDARY_DELIMETER + "batchresponse_")) {
                throw logger.logThrowableAsError(new IOException("Invalid multipart response"));
            }

            BatchOperationResponse response = null;
            boolean foundBody = false;
            String body = null;
            List<BatchOperationResponse> responses = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(BOUNDARY_DELIMETER + "changesetresponse_")) {
                    if (response != null) {
                        if (body != null && !body.isEmpty()) {
                            try {
                                TableServiceError errorBody = deserialize(body, TableServiceError.class,
                                    serializerEncoding);
                                response.setValue(errorBody);
                            } catch (IOException e) {
                                logger.logThrowableAsWarning(
                                    new IOException("Unable to deserialize sub-response body", e));
                                response.setValue(body);
                            }
                        }
                        responses.add(response);
                    }
                    response = null;
                    foundBody = false;
                    body = null;
                } else if (response == null && line.startsWith("HTTP/1.1")) {
                    response = new BatchOperationResponse();
                    // Characters 9-12 in the first line of the HTTP response are the status code
                    response.setStatusCode(Integer.parseInt(line.substring(9, 12)));
                } else if (response != null && !foundBody) {
                    if (line.isEmpty()) {
                        // An empty line after the headers delimits the body
                        foundBody = true;
                    } else {
                        // A header line
                        String[] header = line.split(":", 2);
                        response.putHeader(header[0].trim(), header[1].trim());
                    }
                } else if (response != null && !line.isEmpty()) {
                    // The rest of the lines constitute the body until we get to the delimiter again
                    body = (body == null ? line : body + line) + "\r\n";
                }
            }

            return (U) responses.toArray(new BatchOperationResponse[0]);
        } else {
            return super.deserialize(inputStream, type, serializerEncoding);
        }
    }
}
