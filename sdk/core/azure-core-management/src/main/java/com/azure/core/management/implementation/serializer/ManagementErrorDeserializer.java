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
import java.util.regex.Pattern;

/**
 * Custom serializer for serializing {@link ManagementError} objects.
 */
public final class ManagementErrorDeserializer extends StdDeserializer<Object> {
    private static final long serialVersionUID = 1L;

    // Use compiled Patterns instead of String.replaceFirst as that compiles the target Pattern per invocation.
    private static final Pattern CODE_PATTERN = Pattern.compile("\"code\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("\"message\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern TARGET_PATTERN = Pattern.compile("\"target\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern DETAILS_PATTERN = Pattern.compile("\"details\"", Pattern.CASE_INSENSITIVE);

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

        /*
         * This uses static compiled Patterns instead of String.replaceFirst as replaceFirst is implemented using a
         * compiled Pattern. Using String.replaceFirst will cause the Pattern to be compiled every time the code path
         * is reach, using the static Patterns only requires that to be done once.
         */
        nodeContent = CODE_PATTERN.matcher(nodeContent).replaceFirst("\"code\"");
        nodeContent = MESSAGE_PATTERN.matcher(nodeContent).replaceFirst("\"message\"");
        nodeContent = TARGET_PATTERN.matcher(nodeContent).replaceFirst("\"target\"");
        nodeContent = DETAILS_PATTERN.matcher(nodeContent).replaceFirst("\"details\"");

        JsonParser parser = new JsonFactory().createParser(nodeContent);
        parser.setCodec(mapper);
        return parser.readValueAs(this.handledType());
    }
}
