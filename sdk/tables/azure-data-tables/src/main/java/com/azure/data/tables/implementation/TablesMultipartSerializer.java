// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.data.tables.implementation.models.BatchChangeSet;
import com.azure.data.tables.implementation.models.MultipartPart;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Serializer for Multipart requests and responses.
 */
public class TablesMultipartSerializer extends TablesJacksonSerializer {
    private static final String BOUNDARY_DELIMETER = "--";

    @Override
    public void serialize(Object object, SerializerEncoding encoding, OutputStream outputStream) throws IOException {
        if (object instanceof MultipartPart<?>) {
            writeMultipartPart(object, encoding, outputStream);
        } else if (object instanceof HttpRequest) {
            writeRequest(object, outputStream);
        } else {
            super.serialize(object, encoding, outputStream);
        }
    }

    private void writeMultipartPart(Object object, SerializerEncoding encoding, OutputStream os) throws IOException {
        MultipartPart<?> part = (MultipartPart<?>)object;

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
        HttpRequest request = (HttpRequest)object;
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

//    @Override
//    public <T> T deserialize(String s, Type type, SerializerEncoding serializerEncoding) {
//        // TODO
//        return null;
//    }
//
//    @Override
//    public <T> T deserialize(HttpHeaders httpHeaders, Type type) {
//        // TODO
//        return null;
//    }
}
