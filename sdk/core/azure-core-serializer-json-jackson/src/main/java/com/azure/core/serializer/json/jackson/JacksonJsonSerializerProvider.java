// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.experimental.serializer.JsonSerializer;
import com.azure.core.experimental.serializer.JsonSerializerProvider;

/**
 * Implementation of {@link JsonSerializerProvider}.
 */
public class JacksonJsonSerializerProvider implements JsonSerializerProvider {
    @Override
    public JsonSerializer createInstance() {
        return new JacksonJsonSerializerBuilder().build();
    }
}
