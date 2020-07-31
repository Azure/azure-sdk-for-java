// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.data.schemaregistry.CachedSchemaRegistryAsyncClient;

import java.util.Collections;

public class TestDummySerializer extends AbstractSchemaRegistrySerializer {
    TestDummySerializer(
        CachedSchemaRegistryAsyncClient mockClient,
        boolean autoRegisterSchemas) {
        super(mockClient, new SampleCodec(), Collections.singletonList(new SampleCodec()),
            "sgroup", autoRegisterSchemas);
    }
}
