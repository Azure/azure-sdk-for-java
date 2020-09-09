// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.data.schemaregistry.client.SchemaRegistryClient;

public class TestDummyDeserializer extends AbstractDataDeserializer {
    TestDummyDeserializer(SchemaRegistryClient mockClient) {
        super(mockClient);
        ByteDecoder sampleDecoder = new SampleByteDecoder();
        this.loadByteDecoder(sampleDecoder);
    }
}
