/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry;

import com.azure.schemaregistry.client.SchemaRegistryClient;

public class TestDummyDeserializer extends AbstractDataDeserializer {
    private TestDummyDeserializer(Builder builder) {
        super(builder.schemaRegistryClient);
        for (ByteDecoder decoder: builder.byteDecoders)
            this.byteDecoderMap.put(decoder.serializationFormat(), decoder);
    }

    public static class Builder extends AbstractDataDeserializer.AbstractBuilder<Builder> {
        public Builder(SchemaRegistryClient schemaRegistryClient) {
            super(schemaRegistryClient);
        }

        @Override
        public Builder getActualBuilder() {
            return this;
        }

        public TestDummyDeserializer build() {
            return new TestDummyDeserializer(this);
        }
    }
}