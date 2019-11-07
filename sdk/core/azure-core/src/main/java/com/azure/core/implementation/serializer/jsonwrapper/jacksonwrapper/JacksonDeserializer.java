// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.serializer.jsonwrapper.jacksonwrapper;

import com.azure.core.implementation.serializer.jsonwrapper.api.Config;
import com.azure.core.implementation.serializer.jsonwrapper.api.Deserializer;
import com.azure.core.implementation.serializer.jsonwrapper.api.JsonApi;
import com.azure.core.implementation.serializer.jsonwrapper.api.Type;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


public class JacksonDeserializer implements JsonApi {

    private static final Map<Config, DeserializationFeature> CONFIG_MAP;
    private final ClientLogger logger = new ClientLogger(JacksonDeserializer.class);


    static {
        CONFIG_MAP = new HashMap<>();

        CONFIG_MAP.put(Config.FAIL_ON_NULL_FOR_PRIMITIVES, DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        CONFIG_MAP.put(Config.FAIL_ON_NUMBERS_FOR_ENUM, DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS);
        CONFIG_MAP.put(Config.FAIL_ON_UNKNOWN_PROPERTIES, DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private ObjectMapper objectMapper = new ObjectMapper();
    private final TypeFactory typeFactory = objectMapper.getTypeFactory();

    @Override
    public void configure(Config key, boolean value) {
        DeserializationFeature feature = CONFIG_MAP.get(key);
        if (feature == null) {
            logger.logExceptionAsError(
                new IllegalArgumentException("Internal error: configuration key " + key.name() + " was not set"));
        }
        this.objectMapper = objectMapper.configure(feature, value);
    }

    @Override
    public void configureTimezone() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getDefault());
        objectMapper.setDateFormat(df);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> void registerCustomDeserializer(final Deserializer<T> deserializer) {
        SimpleModule module = new SimpleModule("deserializer", new Version(1, 0, 0, null, null, null));
        module.addDeserializer(deserializer.getRawType(), new com.fasterxml.jackson.databind.JsonDeserializer() {

            @Override
            public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException, JsonProcessingException {
                ObjectCodec codec = jsonParser.getCodec();
                JsonNode node = codec.readTree(jsonParser);
                return deserializer.deserialize(new JacksonNode(node));
            }
        });

        objectMapper.registerModule(module);

        // registry JavaTimeModule
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
    }

    /**
     * read json string convert to class type
     *
     * @param json input string
     * @param cls class type
     * @param <T> type
     * @return object of type T
     */
    public <T> T readString(final String json, final Class<? extends T> cls) {
        try {
            return objectMapper.readValue(json, cls);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public <T> T readString(String json, Type<T> type) {
        assert type.isParameterizedType();

        try {
            return objectMapper.readValue(json, typeFactory.constructType(type.getJavaType()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public <T> List<T> readStringToList(String json, Type<List<T>> type) {
        assert type.isParameterizedType();

        try {
            return objectMapper.readValue(json, listTypeReference(type));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private <T> CollectionType listTypeReference(Type<T> type) {
        return typeFactory.constructCollectionType(List.class, typeFactory.constructType(type.getListType()));
    }

    @Override
    public <T> T convertObjectToType(Object source, Class<T> cls) {
        return objectMapper.convertValue(source, cls);
    }
}
