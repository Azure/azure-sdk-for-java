// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.azure.core.experimental.serializer.AvroSerializerProvider;
import com.azure.core.experimental.serializer.ObjectSerializer;

/**
 * Implementation of {@link AvroSerializerProvider}.
 */
public class JacksonAvroSerializerProvider implements AvroSerializerProvider {
    @Override
    public ObjectSerializer createInstance(String schema) {
        return new JacksonAvroSerializerBuilder()
            .schema(schema)
            .build();
    }
}
