// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer.jackson;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.serializer.SearchSerializer;
import com.azure.search.documents.serializer.SearchType;
import com.azure.search.documents.serializer.SerializationInclusion;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SearchJacksonSerializer implements SearchSerializer {
    private final ClientLogger logger = new ClientLogger(SearchJacksonSerializer.class);
    private final Map<SerializationInclusion, ObjectMapper> objectMapperMap;

    SearchJacksonSerializer(Supplier<ObjectMapper> mapperSupplier) {
        this.objectMapperMap = new HashMap<>();
        this.objectMapperMap.put(SerializationInclusion.DEFAULT,
            mapperSupplier.get());
        this.objectMapperMap.put(SerializationInclusion.ALWAYS,
            mapperSupplier.get().setSerializationInclusion(JsonInclude.Include.ALWAYS));
    }

    @Override
    public <T> T convertValue(Object o, SearchType<T> searchType) {
        ObjectMapper objectMapper = objectMapperMap.get(SerializationInclusion.DEFAULT);
        return objectMapper.convertValue(o, objectMapper.getTypeFactory()
            .constructType(searchType.getType()));
    }

    @Override
    public <T> T convertValue(Object o, SearchType<T> type, SerializationInclusion inclusion) {
        ObjectMapper objectMapper = objectMapperMap.get(inclusion);
        return objectMapper.convertValue(o, objectMapper.getTypeFactory()
            .constructType(type.getType()));
    }

    @Override
    public <T> T convertValue(Object o, Class<T> clazz) {
        return objectMapperMap.get(SerializationInclusion.DEFAULT).convertValue(o, clazz);
    }

    @Override
    public <T> T convertValue(Object o, Class<T> clazz, SerializationInclusion inclusion) {
        return objectMapperMap.get(inclusion).convertValue(o, clazz);
    }

    @Override
    public <T> T readValue(Reader reader, SearchType<T> type) {
        try {
            ObjectMapper objectMapper = objectMapperMap.get(SerializationInclusion.DEFAULT);
            return objectMapper.readValue(reader, objectMapper.getTypeFactory()
                .constructType(type.getType()));
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new RuntimeException(ex));
        }
    }

    @Override
    public <T> T readValue(String jsonString, SearchType<T> type) {
        try {
            ObjectMapper objectMapper = objectMapperMap.get(SerializationInclusion.DEFAULT);
            return objectMapper.readValue(jsonString, objectMapper.getTypeFactory()
                .constructType(type.getType()));
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new RuntimeException(ex));
        }
    }
}
