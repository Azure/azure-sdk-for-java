// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.apache;

import com.azure.core.util.serializer.AvroSerializerProvider;
import com.azure.core.util.serializer.ObjectSerializer;

/**
 * Implementation of {@link AvroSerializerProvider}.
 */
public class ApacheAvroSerializerProvider implements AvroSerializerProvider {
    @Override
    public ObjectSerializer createInstance(String schema) {
        return new ApacheAvroSerializerBuilder()
            .schema(schema)
            .build();
    }
}
