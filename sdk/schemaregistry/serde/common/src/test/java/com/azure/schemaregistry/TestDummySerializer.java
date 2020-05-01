/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry;

import com.azure.schemaregistry.client.SchemaRegistryClient;

public class TestDummySerializer extends AbstractDataSerializer {
    private TestDummySerializer(Builder builder) {
        super(builder.schemaRegistryClient);

        // allows simulating improperly written serializer constructor that does not initialize byte encoder
        if (!builder.constructWithoutByteEncoder) {
            setByteEncoder(builder.byteEncoder);
        }

        this.autoRegisterSchemas = builder.autoRegisterSchemas;
    }

    public static class Builder extends AbstractDataSerializer.AbstractBuilder<Builder> {
        private boolean constructWithoutByteEncoder = false;

        public Builder(SchemaRegistryClient schemaRegistryClient) {
            super(schemaRegistryClient);
        }

        @Override
        public Builder getActualBuilder() {
            return this;
        }

        public TestDummySerializer build() {
            return new TestDummySerializer(this);
        }

        public Builder constructWithoutByteEncoder() {
            this.constructWithoutByteEncoder = true;
            return this;
        }
    }
}
