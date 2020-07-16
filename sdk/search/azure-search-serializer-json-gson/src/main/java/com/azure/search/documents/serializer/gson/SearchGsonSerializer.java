// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer.gson;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.serializer.SearchSerializer;
import com.azure.search.documents.serializer.SearchType;
import com.azure.search.documents.serializer.SerializationInclusion;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Reader;
import java.util.Map;

public class SearchGsonSerializer implements SearchSerializer {
    private final ClientLogger logger = new ClientLogger(SearchJacksonSerializer.class);
    private final Map<SerializationInclusion, Gson> objectMapperMap;

    SearchSerializer() {

    }

    @Override
    public <T> T convertValue(Object fromValue, SearchType<T> clazz) {
        return null;
    }

    @Override
    public <T> T convertValue(Object fromValue, SearchType<T> type, SerializationInclusion inclusion) {
        return null;
    }

    @Override
    public <T> T convertValue(Object fromValue, Class<T> clazz) {
        return null;
    }

    @Override
    public <T> T convertValue(Object fromValue, Class<T> clazz, SerializationInclusion inclusion) {
        return null;
    }

    @Override
    public <T> T readValue(Reader fromValue, SearchType<T> type) {
        return null;
    }

    @Override
    public <T> T readValue(String fromValue, SearchType<T> type) {
        return null;
    }

    private SearchGsonSerializer() {
    }
}
