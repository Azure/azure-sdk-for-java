// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.TypeReference;
import com.azure.search.documents.implementation.serializer.SerializationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public final class Utility {
    // Type reference that used across many places. Have one copy here to minimize the memory.
    public static final TypeReference<Map<String, Object>> TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {
    };

    /**
     * Helper class to initialize the SerializerAdapter.
     * @return The SerializeAdapter instance.
     */
    public static SerializerAdapter initializeSerializerAdapter() {
        JacksonAdapter adapter = new JacksonAdapter();

        ObjectMapper mapper = adapter.serializer();
        SerializationUtil.configureMapper(mapper);

        return adapter;
    }

//    public static JsonSerializer creatDefaultJsonSerializerInstance() {
//        JacksonAdapter adapter = (JacksonAdapter) initializeSerializerAdapter();
//        return new JacksonJsonSerializerBuilder().serializer(adapter.serializer()).build();
//    }

    private Utility() {
    }
}
