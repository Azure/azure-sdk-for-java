// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.core.experimental.serializer.JsonSerializer;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.search.documents.implementation.serializer.SerializationUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class Utility {
    /**
     * Helper class to initialize the SerializerAdapter.
     * @return The SerializeAdapter instance.
     */
    public static SerializerAdapter initializeSerializerAdapter() {
        JacksonAdapter adapter = (JacksonAdapter) JacksonAdapter.createDefaultSerializerAdapter();

        ObjectMapper mapper = adapter.serializer();
        SerializationUtil.configureMapper(mapper);

        return adapter;
    }

    public static JsonSerializer creatDefaultJsonSerializerInstance() {
        JacksonAdapter adapter = (JacksonAdapter) JacksonAdapter.createDefaultSerializerAdapter();

        ObjectMapper mapper = adapter.serializer();
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        SerializationUtil.configureMapper(mapper);

        return new JacksonJsonSerializerBuilder().serializer(mapper).build();
    }

    private Utility() {
    }
}
