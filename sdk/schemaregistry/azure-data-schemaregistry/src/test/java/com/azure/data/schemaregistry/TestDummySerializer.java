// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.data.schemaregistry.client.CachedSchemaRegistryAsyncClient;

public class TestDummySerializer extends AbstractSchemaRegistrySerializer {
    TestDummySerializer(
        CachedSchemaRegistryAsyncClient mockClient,
        boolean byteEncoder,
        boolean autoRegisterSchemas) {
        super(mockClient);

        // allows simulating improperly written serializer constructor that does not initialize byte encoder
        if (byteEncoder) {
            setSerializerCodec(new SampleCodec());
        }

        this.addDeserializerCodec(new SampleCodec());

        this.autoRegisterSchemas = autoRegisterSchemas;
    }
}
