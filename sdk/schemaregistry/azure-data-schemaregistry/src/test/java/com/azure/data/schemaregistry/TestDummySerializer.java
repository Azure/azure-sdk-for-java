// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.data.schemaregistry.client.CachedSchemaRegistryAsyncClient;

import java.util.Collections;

public class TestDummySerializer extends SchemaRegistrySerializer {
    TestDummySerializer(
        CachedSchemaRegistryAsyncClient mockClient,
        boolean autoRegisterSchemas) {
        super(mockClient, new SampleCodec(), Collections.singletonList(new SampleCodec()));

        // allows simulating improperly written serializer constructor that does not initialize byte encoder
        this.autoRegisterSchemas = autoRegisterSchemas;
    }
}
