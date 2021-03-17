// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer;

import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProvider;

/**
 * Implementation of {@link JsonSerializerProvider} to create an instance of {@link JacksonSerializer}.
 */
public class JacksonSerializerProvider implements JsonSerializerProvider {

    @Override
    public JsonSerializer createInstance() {
        return new JacksonSerializer();
    }
}
