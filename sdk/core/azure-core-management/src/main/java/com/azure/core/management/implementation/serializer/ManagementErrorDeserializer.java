// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.serializer;

import com.azure.core.management.exception.ManagementError;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

/**
 * Custom serializer for serializing {@link ManagementError} objects.
 */
public final class ManagementErrorDeserializer extends StdDeserializer<Object> {

    private static final long serialVersionUID = 1L;

    private final ObjectMapper mapper;

    /**
     * Creates an instance of {@link ManagementErrorDeserializer}.
     *
     * @param vc type of values this deserializer handles.
     * @param mapper the object mapper for default deserializations.
     */
    private ManagementErrorDeserializer(Class<?> vc, ObjectMapper mapper) {
        super(vc);
        this.mapper = mapper;
    }

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson
     * ObjectMapper.
     *
     * @param mapper the object mapper for default deserializations.
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule(ObjectMapper mapper) {
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                                                          BeanDescription beanDesc,
                                                          JsonDeserializer<?> deserializer) {
                if (ManagementError.class.isAssignableFrom(beanDesc.getBeanClass())) {
                    // Register 'ManagementErrorDeserializer' for class and subclass of 'ManagementError'
                    return new ManagementErrorDeserializer(beanDesc.getBeanClass(), mapper);
                } else {
                    return deserializer;
                }
            }
        });
        return module;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        p.setCodec(mapper);
        JsonNode errorNode = p.readValueAsTree();
        if (errorNode == null) {
            return null;
        }
        if (errorNode.get("error") != null) {
            errorNode = errorNode.get("error");
        }
        String nodeContent = errorNode.toString();
        nodeContent = nodeContent.replaceFirst("(?i)\"code\"", "\"code\"")
                .replaceFirst("(?i)\"message\"", "\"message\"")
                .replaceFirst("(?i)\"target\"", "\"target\"")
                .replaceFirst("(?i)\"details\"", "\"details\"");
        JsonParser parser = new JsonFactory().createParser(nodeContent);
        parser.setCodec(mapper);
        return parser.readValueAs(this.handledType());
    }
}
