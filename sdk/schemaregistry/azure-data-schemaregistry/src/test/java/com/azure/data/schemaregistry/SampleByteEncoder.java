// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SampleByteEncoder implements ByteEncoder {

    public SampleByteEncoder() { }

    @Override
    public String getSchemaName(Object object) throws SerializationException {
        return null;
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
    public String schemaType() {
        return "test";
    }

    @Override
    public String parseSchemaString(String s) {
        return s;
    }
}
