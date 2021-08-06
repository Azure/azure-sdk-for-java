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
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Constructs and configures {@link ObjectMapper} instances.
 */
final class ObjectMapperFactory {
    private final ClientLogger logger = new ClientLogger(ObjectMapperFactory.class);

    private static final String MUTABLE_COERCION_CONFIG = "com.fasterxml.jackson.databind.cfg.MutableCoercionConfig";
    private static final String COERCION_INPUT_SHAPE = "com.fasterxml.jackson.databind.cfg.CoercionInputShape";
    private static final String COERCION_ACTION = "com.fasterxml.jackson.databind.cfg.CoercionAction";

    private MethodHandle coersionConfigDefaults;
    private MethodHandle setCoercion;
    private Object coercionInputShapeEmptyString;
    private Object coercionActionAsNull;
    private boolean useReflectionToSetCoercion;

    private static ObjectMapperFactory instance;

    public static synchronized ObjectMapperFactory getInstance() {
        if (instance == null) {
            instance = new ObjectMapperFactory();
        }

        return instance;
    }

    private ObjectMapperFactory() {
        MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();

        try {
            Class<?> mutableCoercionConfig = Class.forName(MUTABLE_COERCION_CONFIG);
            Class<?> coercionInputShapeClass = Class.forName(COERCION_INPUT_SHAPE);
            Class<?> coercionActionClass = Class.forName(COERCION_ACTION);

            coersionConfigDefaults = publicLookup.findVirtual(ObjectMapper.class, "coercionConfigDefaults",
                MethodType.methodType(mutableCoercionConfig));
            setCoercion = publicLookup.findVirtual(mutableCoercionConfig, "setCoercion",
                MethodType.methodType(mutableCoercionConfig, coercionInputShapeClass, coercionActionClass));
            coercionInputShapeEmptyString = publicLookup.findStaticGetter(coercionInputShapeClass, "EmptyString",
                coercionInputShapeClass).invoke();
            coercionActionAsNull = publicLookup.findStaticGetter(coercionActionClass, "AsNull", coercionActionClass)
                .invoke();
            useReflectionToSetCoercion = true;
        } catch (Throwable ex) {
            logger.verbose("Failed to retrieve MethodHandles used to set coercion configurations. "
                + "Setting coercion configurations will be skipped.", ex);
        }
    }

    public ObjectMapper createJsonMapper(ObjectMapperShim innerMapperShim) {
        ObjectMapper innerMapper = innerMapperShim.getMapper();
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
        ObjectMapper xmlMapper = initializeMapperBuilder(XmlMapper.builder())
            .defaultUseWrapper(false)
            .enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
            /*
             * In Jackson 2.12 the default value of this feature changed from true to false.
             * https://github.com/FasterXML/jackson/wiki/Jackson-Release-2.12#xml-module
             */
            .enable(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL)
            .build();


        if (useReflectionToSetCoercion) {
            try {
                Object object = coersionConfigDefaults.invoke(xmlMapper);
                setCoercion.invoke(object, coercionInputShapeEmptyString, coercionActionAsNull);
            } catch (Throwable e) {
                logger.verbose("Failed to set coercion actions.", e);
            }
        } else {
            logger.verbose("Didn't set coercion defaults as it wasn't found on the classpath.");
        }

        return xmlMapper;
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
    private <S extends MapperBuilder<?, ?>> S initializeMapperBuilder(S mapper) {
        mapper.enable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .addModule(new JavaTimeModule())
            .addModule(ByteArraySerializer.getModule())
            .addModule(Base64UrlSerializer.getModule())
            .addModule(DateTimeSerializer.getModule())
            .addModule(DateTimeDeserializer.getModule())
            .addModule(DateTimeRfc1123Serializer.getModule())
            .addModule(DurationSerializer.getModule())
            .addModule(HttpHeadersSerializer.getModule())
            .addModule(UnixTimeSerializer.getModule())
            .addModule(UnixTimeDeserializer.getModule())
            .addModule(GeoJsonSerializer.getModule())
            .addModule(GeoJsonDeserializer.getModule())
            .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .visibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)
            .visibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
            .visibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);

        return mapper;
    }
}
