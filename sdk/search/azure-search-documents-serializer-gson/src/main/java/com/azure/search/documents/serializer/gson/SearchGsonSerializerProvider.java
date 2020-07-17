// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer.gson;

import com.azure.core.experimental.serializer.JsonOptions;
import com.azure.core.experimental.serializer.JsonSerializer;
import com.azure.core.experimental.serializer.JsonSerializerProvider;
import com.azure.search.documents.serializer.SearchSerializer;

public class SearchGsonSerializerProvider implements JsonSerializerProvider {
    @Override
    public JsonSerializer createInstance() {
        return new JacksonJsonSerializerBuilder().build();
    }

    @Override
    public JsonSerializer createInstance(JsonOptions jsonOptions) {
        return new SearchGsonSerializerBuilder().options(jsonOptions).build();
    }
}
