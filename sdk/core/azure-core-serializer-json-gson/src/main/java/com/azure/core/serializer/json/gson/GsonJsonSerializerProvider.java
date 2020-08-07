// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.util.serializer.JsonSerializerProvider;
import com.azure.core.util.serializer.MemberNameConverterProvider;

/**
 * Implementation of {@link JsonSerializerProvider}.
 */
public class GsonJsonSerializerProvider implements MemberNameConverterProvider, JsonSerializerProvider {
    @Override
    public GsonJsonSerializer createInstance() {
        return new GsonJsonSerializerBuilder().build();
    }
}
