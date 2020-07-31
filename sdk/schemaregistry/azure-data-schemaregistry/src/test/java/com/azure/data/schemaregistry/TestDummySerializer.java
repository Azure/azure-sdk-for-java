// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.data.schemaregistry.client.CachedSchemaRegistryAsyncClient;
import java.util.HashMap;
import java.util.Map;

public class TestDummySerializer extends AbstractSchemaRegistrySerializer {
    TestDummySerializer(
        CachedSchemaRegistryAsyncClient mockClient,
        boolean byteEncoder,
        boolean autoRegisterSchemas) {

        super(mockClient, new SampleCodec(), getDeserializerCodec());

        // allows simulating improperly written serializer constructor that does not initialize byte encoder
        this.autoRegisterSchemas = autoRegisterSchemas;
    }

    private static Map<String, Codec> getDeserializerCodec() {
        HashMap<String, Codec> codecMap = new HashMap<>();
        SampleCodec codec = new SampleCodec();
        codecMap.put(codec.getSchemaType(), codec);
        return codecMap;
    }
}
