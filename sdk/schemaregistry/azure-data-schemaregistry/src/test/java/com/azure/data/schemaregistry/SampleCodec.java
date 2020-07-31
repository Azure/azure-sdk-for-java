// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.data.schemaregistry.models.SerializationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SampleCodec implements Codec {

    public SampleCodec() { }

    @Override
    public String getSchemaName(Object object) throws SerializationException {
        return "schema name";
    }

    @Override
    public String getSchemaGroup() {
        return "schema group";
    }

    @Override
    public String getSchemaString(Object object) {
        return "string representation of schema";
    }

    @Override
    public byte[] encode(Object object) throws SerializationException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write("sample payload".getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new SerializationException("this should never happen", e);
        }
        return outputStream.toByteArray();
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
