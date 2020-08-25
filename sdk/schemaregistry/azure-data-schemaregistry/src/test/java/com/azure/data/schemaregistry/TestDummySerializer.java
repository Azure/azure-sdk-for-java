// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.data.schemaregistry.models.SerializationType;

public class TestDummySerializer extends SchemaRegistrySerializer {
    TestDummySerializer(
        SchemaRegistryAsyncClient mockClient,
        SchemaRegistrySerializationUtils utils,
        boolean autoRegisterSchemas,
        String schemaGroup) {
        super(mockClient, utils, autoRegisterSchemas, schemaGroup);
    }
}
