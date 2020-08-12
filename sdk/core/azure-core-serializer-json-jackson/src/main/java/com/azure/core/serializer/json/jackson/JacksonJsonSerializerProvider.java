// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.util.serializer.JsonSerializerProvider;
import com.azure.core.util.serializer.MemberNameConverterProvider;

/**
 * Implementation of {@link JsonSerializerProvider} and {@link MemberNameConverterProvider}.
 */
public class JacksonJsonSerializerProvider implements JsonSerializerProvider, MemberNameConverterProvider {
    @Override
    public JacksonJsonSerializer createInstance() {
        return new JacksonJsonSerializerBuilder().build();
    }
}
