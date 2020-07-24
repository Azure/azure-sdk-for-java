// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.experimental.serializer.PropertyNameSerializer;
import com.azure.core.experimental.serializer.PropertyNameSerializerProvider;

/**
 * Implementation of {@link PropertyNameSerializerProvider}.
 */
public class JacksonPropertyNameSerializerProvider implements PropertyNameSerializerProvider {
    @Override
    public PropertyNameSerializer createInstance() {
        return new JacksonPropertyNameSerializer();
    }
}
