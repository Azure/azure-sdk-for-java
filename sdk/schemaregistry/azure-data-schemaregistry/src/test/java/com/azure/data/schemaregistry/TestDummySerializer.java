// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.data.schemaregistry.models.SerializationType;

public class TestDummySerializer extends SchemaRegistrySerializer {
    TestDummySerializer(
        SchemaRegistryAsyncClient mockClient,
        boolean autoRegisterSchemas) {
        super(mockClient, );

        // allows simulating improperly written serializer constructor that does not initialize byte encoder
        this.autoRegisterSchemas = autoRegisterSchemas;
    }

    @Override
    protected SerializationType getSerializationType() {
        return null;
    }

    @Override
    protected String getSchemaName(Object object) {
        return null;
    }

    @Override
    protected String getSchemaString(Object object) {
        return null;
    }

    @Override
    protected byte[] encode(Object object) {
        return new byte[0];
    }

    @Override
    protected Object decode(byte[] encodedBytes, Object schemaObject) {
        return null;
    }
}
