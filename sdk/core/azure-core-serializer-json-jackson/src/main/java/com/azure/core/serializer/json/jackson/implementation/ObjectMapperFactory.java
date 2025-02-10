// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson.implementation;

import com.azure.core.serializer.json.jackson.JacksonJsonProvider;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.cfg.PackageVersion;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Constructs and configures {@link ObjectMapper} instances.
 */
final class ObjectMapperFactory {
    private static final ClientLogger LOGGER = new ClientLogger(ObjectMapperFactory.class);
    final boolean useJackson215;
    private boolean jackson215IsSafe = true;

    ObjectMapperFactory() {
        this.useJackson215 = PackageVersion.VERSION.getMinorVersion() >= 15
            && com.fasterxml.jackson.core.json.PackageVersion.VERSION.getMinorVersion() >= 15;
    }

    public static final ObjectMapperFactory INSTANCE = new ObjectMapperFactory();

    public ObjectMapper createXmlMapper() {
        return attemptJackson215Mutation(XmlMapperFactory.INSTANCE.createXmlMapper());
    }

    public ObjectMapper createJsonMapper(ObjectMapper innerMapper) {
        ObjectMapper flatteningMapper = attemptJackson215Mutation(
            initializeMapperBuilder(JsonMapper.builder()).addModule(FlatteningSerializer.getModule(innerMapper))
                .addModule(FlatteningDeserializer.getModule(innerMapper))
                .build());

        return attemptJackson215Mutation(initializeMapperBuilder(JsonMapper.builder())
            // Order matters: must register in reverse order of hierarchy
            .addModule(AdditionalPropertiesSerializer.getModule(flatteningMapper))
            .addModule(AdditionalPropertiesDeserializer.getModule(flatteningMapper))
            .addModule(FlatteningSerializer.getModule(innerMapper))
            .addModule(FlatteningDeserializer.getModule(innerMapper))
            .addModule(JacksonJsonProvider.getJsonSerializableDatabindModule())
            .build());
    }

    public ObjectMapper createSimpleMapper() {
        return attemptJackson215Mutation(initializeMapperBuilder(JsonMapper.builder()).build());
    }

    public ObjectMapper createHeaderMapper() {
        return attemptJackson215Mutation(
            initializeMapperBuilder(JsonMapper.builder()).enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                .addModule(JacksonJsonProvider.getJsonSerializableDatabindModule())
                .build());
    }

    private ObjectMapper attemptJackson215Mutation(ObjectMapper objectMapper) {
        if (useJackson215 && jackson215IsSafe) {
            try {
                return JacksonDatabind215.mutateStreamReadConstraints(objectMapper);
            } catch (Throwable ex) {
                if (ex instanceof LinkageError) {
                    jackson215IsSafe = false;
                    LOGGER.log(LogLevel.VERBOSE, JacksonVersion::getHelpInfo, ex);
                }

                throw ex;
            }
        }

        return objectMapper;
    }

    @SuppressWarnings("deprecation")
    static MapperBuilder<?, ?> initializeMapperBuilder(MapperBuilder<?, ?> mapper) {
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
