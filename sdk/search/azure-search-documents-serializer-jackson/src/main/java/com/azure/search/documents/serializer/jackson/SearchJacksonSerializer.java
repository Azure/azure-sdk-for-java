// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer.jackson;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.serializer.SearchSerializer;
import com.azure.search.documents.serializer.SearchType;
import com.azure.search.documents.serializer.SerializationInclusion;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.io.Reader;

public class SearchJacksonSerializer implements SearchSerializer {
    private final ClientLogger logger = new ClientLogger(SearchJacksonSerializer.class);
    private final ObjectMapper mapper;
    private final TypeFactory typeFactory;

    SearchJacksonSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
        this.typeFactory = mapper.getTypeFactory();
    }

    @Override
    public <T> T convertValue(Object o, SearchType<T> type) {
        return mapper.convertValue(o, typeFactory.constructType(type.getType()));
    }

    @Override
    public <T> T convertValue(Object o, SearchType<T> type, SerializationInclusion inclusion) {
        if (inclusion == SerializationInclusion.ALWAYS) {
            mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        }
        return mapper.convertValue(o, typeFactory.constructType(type.getType()));
    }

    @Override
    public <T> T convertValue(Object o, Class<T> clazz) {
        return mapper.convertValue(o, clazz);
    }

    @Override
    public <T> T convertValue(Object o, Class<T> clazz, SerializationInclusion inclusion) {
        if (inclusion == SerializationInclusion.ALWAYS) {
            mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        }
        return mapper.convertValue(o, clazz);
    }

    @Override
    public <T> T readValue(Reader reader, SearchType<T> type) {
        try {
            return mapper.readValue(reader, typeFactory.constructType(type.getType()));
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new RuntimeException(ex));
        }
    }

    @Override
    public <T> T readValue(String jsonString, SearchType<T> type) {
        try {
            return mapper.readValue(jsonString, typeFactory.constructType(type.getType()));
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new RuntimeException(ex));
        }
    }
}
