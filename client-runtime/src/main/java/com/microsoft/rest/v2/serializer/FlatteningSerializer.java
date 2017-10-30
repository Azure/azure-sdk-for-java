/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.serializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.microsoft.rest.v2.DateTimeRfc1123;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom serializer for serializing types with wrapped properties.
 * For example, a property with annotation @JsonProperty(value = "properties.name")
 * will be mapped from a top level "name" property in the POJO model to
 * {'properties' : { 'name' : 'my_name' }} in the serialized payload.
 */
public class FlatteningSerializer extends StdSerializer<Object> implements ResolvableSerializer {
    /**
     * The default mapperAdapter for the current type.
     */
    private final JsonSerializer<?> defaultSerializer;

    /**
     * The object mapper for default serializations.
     */
    private final ObjectMapper mapper;

    /**
     * Creates an instance of FlatteningSerializer.
     * @param vc handled type
     * @param defaultSerializer the default JSON serializer
     * @param mapper the object mapper for default serializations
     */
    protected FlatteningSerializer(Class<?> vc, JsonSerializer<?> defaultSerializer, ObjectMapper mapper) {
        super(vc, false);
        this.defaultSerializer = defaultSerializer;
        this.mapper = mapper;
    }

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson
     * ObjectMapper.
     *
     * @param mapper the object mapper for default serializations
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule(final ObjectMapper mapper) {
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                if (beanDesc.getBeanClass().getAnnotation(JsonFlatten.class) != null) {
                    return new FlatteningSerializer(beanDesc.getBeanClass(), serializer, mapper);
                }
                return serializer;
            }
        });
        return module;
    }

    private List<Field> getAllDeclaredFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<Field>();
        while (clazz != null && !clazz.equals(Object.class)) {
            for (Field f : clazz.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (!Modifier.isFinal(mod) && !Modifier.isStatic(mod)) {
                    fields.add(f);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private JsonNode serializePartial(Object value) {
        if (value.getClass().isPrimitive()
                || value.getClass().isEnum()
                || value instanceof LocalDate
                || value instanceof DateTime
                || value instanceof String
                || value instanceof DateTimeRfc1123
                || value instanceof Period) {
            return mapper.valueToTree(value);
        }

        int mod = value.getClass().getModifiers();
        if (Modifier.isFinal(mod) || Modifier.isStatic(mod)) {
            return mapper.valueToTree(value);
        }

        if (value instanceof List<?>) {
            ArrayNode node = new ArrayNode(mapper.getNodeFactory());
            for (Object val : ((List<?>) value)) {
                node.add(serializePartial(val));
            }
            return node;
        }

        if (value instanceof Map<?, ?>) {
            ObjectNode node = new ObjectNode(mapper.getNodeFactory());
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                node.set((String) entry.getKey(), serializePartial(entry.getValue()));
            }
            return node;
        }

        ObjectNode res = new ObjectNode(mapper.getNodeFactory());
        for (Field f : getAllDeclaredFields(value.getClass())) {
            f.setAccessible(true);
            String wireName = f.getName();
            ObjectNode pointer = res;
            JsonProperty property = f.getAnnotation(JsonProperty.class);
            if (property != null && !property.value().isEmpty()) {
                wireName = f.getAnnotation(JsonProperty.class).value();
            }
            try {
                Object propValue = f.get(value);
                if (propValue != null) {
                    if (value.getClass().isAnnotationPresent(JsonFlatten.class) && wireName.matches(".+[^\\\\]\\..+")) {
                        String[] values = wireName.split("((?<!\\\\))\\.");
                        for (int i = 0; i < values.length; ++i) {
                            values[i] = values[i].replace("\\.", ".");
                            if (i == values.length - 1) {
                                break;
                            }
                            String val = values[i];
                            if (!pointer.has(val)) {
                                ObjectNode child = new ObjectNode(mapper.getNodeFactory());
                                pointer.set(val, child);
                                pointer = child;
                            } else {
                                pointer = (ObjectNode) pointer.get(val);
                            }
                        }
                        wireName = values[values.length - 1];
                    }
                    pointer.set(wireName, serializePartial(propValue));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    @Override
    public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (value == null) {
            jgen.writeNull();
            return;
        }

        jgen.writeTree(serializePartial(value));
    }

    @Override
    public void resolve(SerializerProvider provider) throws JsonMappingException {
        ((ResolvableSerializer) defaultSerializer).resolve(provider);
    }
}
