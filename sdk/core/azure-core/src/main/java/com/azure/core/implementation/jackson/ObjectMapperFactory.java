// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Constructs and configures {@link ObjectMapper} instances.
 */
final class ObjectMapperFactory {
    // ObjectMapperFactory is a commonly used factory, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(ObjectMapperFactory.class);

    public  static final ObjectMapperFactory INSTANCE = new ObjectMapperFactory();

    public ObjectMapper createJsonMapper(ObjectMapper innerMapper) {
        ObjectMapper flatteningMapper = initializeMapperBuilder(JsonMapper.builder())
            .addModule(FlatteningSerializer.getModule(innerMapper))
            .addModule(FlatteningDeserializer.getModule(innerMapper))
            .build();

        return initializeMapperBuilder(JsonMapper.builder())
            // Order matters: must register in reverse order of hierarchy
            .addModule(AdditionalPropertiesSerializer.getModule(flatteningMapper))
            .addModule(AdditionalPropertiesDeserializer.getModule(flatteningMapper))
            .addModule(FlatteningSerializer.getModule(innerMapper))
            .addModule(FlatteningDeserializer.getModule(innerMapper))
            .build();
    }

    public ObjectMapper createXmlMapper() {
        return XmlMapperFactory.INSTANCE.createXmlMapper();
    }

    public ObjectMapper createSimpleMapper() {
        return initializeMapperBuilder(JsonMapper.builder()).build();
    }

    public ObjectMapper createDefaultMapper() {
        return new ObjectMapper();
    }

    public ObjectMapper createPrettyPrintMapper() {
        return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public ObjectMapper createHeaderMapper() {
        return initializeMapperBuilder(JsonMapper.builder())
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
            .build();
    }

    @SuppressWarnings("deprecation")
    static <S extends MapperBuilder<?, ?>> S initializeMapperBuilder(S mapper) {
        mapper.enable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .addModule(new JavaTimeModule())
            .addModule(BinaryDataSerializer.getModule())
            .addModule(BinaryDataDeserializer.getModule())
            .addModule(ByteArraySerializer.getModule())
            .addModule(Base64UrlSerializer.getModule())
            .addModule(DateTimeSerializer.getModule())
            .addModule(DateTimeDeserializer.getModule())
            .addModule(DateTimeRfc1123Serializer.getModule())
            .addModule(DurationSerializer.getModule())
            .addModule(HttpHeadersSerializer.getModule())
            .addModule(GeoJsonSerializer.getModule())
            .addModule(GeoJsonDeserializer.getModule())
            .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .visibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)
            .visibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
            .visibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);

        return mapper;
    }
}
