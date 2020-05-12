/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry;

import org.apache.avro.Schema;

public class SampleByteDecoder implements ByteDecoder {
    public SampleByteDecoder() {}

    @Override
    public String serializationFormat() {
        return "sample";
    }

    public static final String samplePayload = "sample payload!";

    @Override
    public Object decodeBytes(byte[] bytes, Object o) throws SerializationException {
        return samplePayload;
    }

    @Override
    public Schema parseSchemaString(String s) {
        return new Schema.Parser().parse(s);
    }
}
