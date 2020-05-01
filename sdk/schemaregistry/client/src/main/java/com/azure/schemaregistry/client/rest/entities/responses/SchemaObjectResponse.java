/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.client.rest.entities.responses;

import com.azure.schemaregistry.client.rest.exceptions.RestClientException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class SchemaObjectResponse {
    public final byte[] schemaByteArray;
    public final String serializationType;
    public final String schemaGuid;

    public SchemaObjectResponse(HttpURLConnection connection) throws RestClientException, IOException {
        this.serializationType = getSerializationType(connection.getHeaderField("content-type"));
        this.schemaGuid = connection.getHeaderField("Schema-Id");

        InputStream is = connection.getInputStream();
        ByteArrayOutputStream streamContent = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            streamContent.write(buffer, 0, length);
        }
        schemaByteArray = streamContent.toByteArray();
        is.close();
    }

    // for testing
    public SchemaObjectResponse(byte[] schemaByteArray, String serializationType, String schemaGuid) {
        this.schemaByteArray = schemaByteArray;
        this.serializationType = serializationType;
        this.schemaGuid = schemaGuid;
    }

    static String getSerializationType(String contentTypeHeader) throws RestClientException {
        String[] contentTypeTokens = contentTypeHeader.split(";");
        for (String contentTypeToken : contentTypeTokens) {
            if (contentTypeToken.contains("serialization")) {
                return contentTypeToken.split("=")[1];
            }
        }
        throw new RestClientException(String.format("No serialization type defined in header: %s", contentTypeHeader), 0 , 0);
    }
}