// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

class TestSerializer extends SchemaRegistrySerializer {
    TestSerializer(
        SchemaRegistryAsyncClient mockClient,
        SchemaRegistrySerializationUtils utils,
        boolean autoRegisterSchemas,
        String schemaGroup) {
        super(mockClient, utils, autoRegisterSchemas, schemaGroup);
    }
}
