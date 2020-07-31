// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SampleCodec implements Codec {

    public SampleCodec() { }

    @Override
    public String getSchemaName(Object object) throws SerializationException {
        return "schema name";
    }

    @Override
    public String getSchemaString(Object object) {
        return "string representation of schema";
    }

    @Override
    public ByteArrayOutputStream encode(Object object) throws SerializationException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write("sample payload".getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new SerializationException("this should never happen", e);
        }
        return outputStream;
    }

    @Override
    public String getSchemaType() {
        return "test";
    }

    @Override
    public String parseSchemaString(String s) {
        return s;
    }

    public static final String CONSTANT_PAYLOAD = "sample payload!";

    @Override
    public Object decode(byte[] bytes, Object o) throws SerializationException {
        return CONSTANT_PAYLOAD;
    }
}
