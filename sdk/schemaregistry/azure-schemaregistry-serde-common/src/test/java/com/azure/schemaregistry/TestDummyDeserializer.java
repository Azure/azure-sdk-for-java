/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry;

import com.azure.schemaregistry.client.SchemaRegistryClient;

public class TestDummyDeserializer extends AbstractDataDeserializer {
    TestDummyDeserializer(SchemaRegistryClient mockClient) {
        super(mockClient);
        ByteDecoder sampleDecoder = new SampleByteDecoder();
        this.byteDecoderMap.put(sampleDecoder.serializationFormat(), sampleDecoder);
    }
}
