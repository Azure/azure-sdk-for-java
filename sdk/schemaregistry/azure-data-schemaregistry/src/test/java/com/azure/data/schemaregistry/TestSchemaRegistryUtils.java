// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.data.schemaregistry.models.SerializationType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TestSchemaRegistryUtils implements SchemaRegistrySerializationUtils {

    public TestSchemaRegistryUtils() { }

    @Override
    public String getSchemaName(Object object) {
        return "schema name";
    }

    @Override
    public String getSchemaString(Object object) {
        return "string representation of schema";
    }

    @Override
    public byte[] encode(Object object) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write("sample payload".getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("this should never happen", e);
        }
        return outputStream.toByteArray();
    }

    @Override
    public SerializationType getSerializationType() {
        return SerializationType.fromString("test");
    }

    @Override
    public String parseSchemaString(String s) {
        return s;
    }

    public static final String CONSTANT_PAYLOAD = "sample payload!";

    @Override
    public Object decode(byte[] bytes, Object o) {
        return CONSTANT_PAYLOAD;
    }
}
