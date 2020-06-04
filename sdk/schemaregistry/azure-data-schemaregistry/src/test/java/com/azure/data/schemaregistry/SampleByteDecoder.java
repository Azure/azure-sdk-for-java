// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import org.apache.avro.Schema;

public class SampleByteDecoder implements ByteDecoder {
    public SampleByteDecoder() { }

    @Override
    public String schemaType() {
        return "sample";
    }

    public static final String CONSTANT_PAYLOAD = "sample payload!";

    @Override
    public Object decodeBytes(byte[] bytes, Object o) throws SerializationException {
        return CONSTANT_PAYLOAD;
    }

    @Override
    public Schema parseSchemaString(String s) {
        return new Schema.Parser().parse(s);
    }
}
