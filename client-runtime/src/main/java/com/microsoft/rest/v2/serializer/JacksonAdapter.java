/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.serializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.microsoft.rest.v2.CollectionFormat;
import com.microsoft.rest.v2.protocol.SerializerAdapter;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A serialization helper class wrapped around {@link ObjectMapper}.
 */
public class JacksonAdapter implements SerializerAdapter<ObjectMapper> {
    /**
     * An instance of {@link ObjectMapper} to serialize/deserialize objects.
     */
    private final ObjectMapper mapper;

    /**
     * An instance of {@link ObjectMapper} that does not do flattening.
     */
    private final ObjectMapper simpleMapper;

    private final XmlMapper xmlMapper;

    /**
     * Creates a new JacksonAdapter instance with default mapper settings.
     */
    public JacksonAdapter() {
        simpleMapper = initializeObjectMapper(new ObjectMapper());
        xmlMapper = initializeObjectMapper(new XmlMapper());
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        xmlMapper.setDefaultUseWrapper(false);
        mapper = initializeObjectMapper(new ObjectMapper())
                .registerModule(FlatteningSerializer.getModule(simpleMapper()))
                .registerModule(FlatteningDeserializer.getModule(simpleMapper()));
    }

    /**
     * Gets a static instance of {@link ObjectMapper} that doesn't handle flattening.
     *
     * @return an instance of {@link ObjectMapper}.
     */
    protected ObjectMapper simpleMapper() {
        return simpleMapper;
    }

    @Override
    public ObjectMapper serializer() {
        return mapper;
    }

    @Override
    public String serialize(Object object, Encoding encoding) throws IOException {
        if (object == null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        if (encoding == Encoding.XML) {
            xmlMapper.writeValue(writer, object);
        } else {
            serializer().writeValue(writer, object);
        }

        return writer.toString();
    }

    @Override
    public String serialize(Object object) throws IOException {
        return serialize(object, Encoding.JSON);
    }

    @Override
    public String serializeRaw(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return CharMatcher.is('"').trimFrom(serialize(object));
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public String serializeList(List<?> list, CollectionFormat format) {
        if (list == null) {
            return null;
        }
        List<String> serialized = new ArrayList<>();
        for (Object element : list) {
            String raw = serializeRaw(element);
            serialized.add(raw != null ? raw : "");
        }
        return Joiner.on(format.getDelimiter()).join(serialized);
    }

    @Override
    public JacksonTypeFactory getTypeFactory() {
        return new JacksonTypeFactory(mapper.getTypeFactory());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(String value, final Type type, Encoding encoding) throws IOException {
        if (value == null || value.isEmpty()) {
            return null;
        }

        final JacksonTypeFactory typeFactory = getTypeFactory();
        final JavaType javaType = typeFactory.create(type);
        if (encoding == Encoding.XML) {
            return (T) xmlMapper.readValue(value, javaType);
        } else {
            return (T) serializer().readValue(value, javaType);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(String value, final Type type) throws IOException {
        return deserialize(value, type, Encoding.JSON);
    }

    /**
     * Initializes an instance of JacksonMapperAdapter with default configurations
     * applied to the object mapper.
     *
     * @param mapper the object mapper to use.
     */
    private static <T extends ObjectMapper> T initializeObjectMapper(T mapper) {
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModule(new JodaModule())
                .registerModule(ByteArraySerializer.getModule())
                .registerModule(Base64UrlSerializer.getModule())
                .registerModule(DateTimeSerializer.getModule())
                .registerModule(DateTimeRfc1123Serializer.getModule());
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
        return mapper;
    }
}
