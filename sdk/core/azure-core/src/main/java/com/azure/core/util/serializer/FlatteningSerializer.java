// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.annotation.JsonFlatten;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Custom serializer for serializing types with wrapped properties.
 * For example, a property with annotation @JsonProperty(value = "properties.name")
 * will be mapped from a top level "name" property in the POJO model to
 * {'properties' : { 'name' : 'my_name' }} in the serialized payload.
 */
class FlatteningSerializer extends StdSerializer<Object> implements ResolvableSerializer {
    private static final long serialVersionUID = -6130180289951110573L;
    private final ClientLogger logger = new ClientLogger(FlatteningSerializer.class);

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
            public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc,
                                                      JsonSerializer<?> serializer) {
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

    @SuppressWarnings("unchecked")
    private void escapeMapKeys(Object value) {
        if (value == null) {
            return;
        }

        if (value.getClass().isPrimitive()
            || value.getClass().isEnum()
            || value instanceof OffsetDateTime
            || value instanceof Duration
            || value instanceof String) {
            return;
        }

        int mod = value.getClass().getModifiers();
        if (Modifier.isFinal(mod) || Modifier.isStatic(mod)) {
            return;
        }

        if (value instanceof List<?>) {
            for (Object val : ((List<?>) value)) {
                escapeMapKeys(val);
            }
            return;
        }

        if (value instanceof Map<?, ?>) {
            for (String key : new HashSet<>(((Map<String, Object>) value).keySet())) {
                if (key.contains(".")) {
                    String newKey = key.replaceAll("((?<!\\\\))\\.", "\\\\.");
                    Object val = ((Map<String, Object>) value).remove(key);
                    ((Map<String, Object>) value).put(newKey, val);
                }
            }
            for (Object val : ((Map<?, ?>) value).values()) {
                escapeMapKeys(val);
            }
            return;
        }

        for (Field f : getAllDeclaredFields(value.getClass())) {
            f.setAccessible(true);
            try {
                escapeMapKeys(f.get(value));
            } catch (IllegalAccessException e) {
                throw logger.logExceptionAsError(new RuntimeException(e));
            }
        }
    }

    @Override
    public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (value == null) {
            jgen.writeNull();
            return;
        }
        escapeMapKeys(value);
        // BFS for all collapsed properties
        ObjectNode root = mapper.valueToTree(value);
        ObjectNode res = root.deepCopy();
        Queue<ObjectNode> source = new LinkedBlockingQueue<ObjectNode>();
        Queue<ObjectNode> target = new LinkedBlockingQueue<ObjectNode>();
        source.add(root);
        target.add(res);
        while (!source.isEmpty()) {
            ObjectNode current = source.poll();
            ObjectNode resCurrent = target.poll();
            Iterator<Map.Entry<String, JsonNode>> fields = current.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                ObjectNode node = resCurrent;
                String key = field.getKey();
                JsonNode outNode = resCurrent.get(key);
                if (key.matches(".+[^\\\\]\\..+")) {
                    // Handle flattening properties
                    //
                    String[] values = key.split("((?<!\\\\))\\.");
                    for (int i = 0; i < values.length; ++i) {
                        values[i] = values[i].replace("\\.", ".");
                        if (i == values.length - 1) {
                            break;
                        }
                        String val = values[i];
                        if (node.has(val)) {
                            node = (ObjectNode) node.get(val);
                        } else {
                            ObjectNode child = new ObjectNode(JsonNodeFactory.instance);
                            node.put(val, child);
                            node = child;
                        }
                    }
                    node.set(values[values.length - 1], resCurrent.get(key));
                    resCurrent.remove(key);
                    outNode = node.get(values[values.length - 1]);
                } else if (key.matches(".*[^\\\\]\\\\..+")) {
                    // Handle escaped map key
                    //
                    String originalKey = key.replaceAll("\\\\.", ".");
                    resCurrent.remove(key);
                    resCurrent.put(originalKey, outNode);
                }
                if (field.getValue() instanceof ObjectNode) {
                    source.add((ObjectNode) field.getValue());
                    target.add((ObjectNode) outNode);
                } else if (field.getValue() instanceof ArrayNode
                    && (field.getValue()).size() > 0
                    && (field.getValue()).get(0) instanceof ObjectNode) {
                    Iterator<JsonNode> sourceIt = field.getValue().elements();
                    Iterator<JsonNode> targetIt = outNode.elements();
                    while (sourceIt.hasNext()) {
                        source.add((ObjectNode) sourceIt.next());
                        target.add((ObjectNode) targetIt.next());
                    }
                }
            }
        }
        jgen.writeTree(res);
    }

    @Override
    public void resolve(SerializerProvider provider) throws JsonMappingException {
        ((ResolvableSerializer) defaultSerializer).resolve(provider);
    }

    @Override
    public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider,
                                  TypeSerializer typeSerializer) throws IOException {
        serialize(value, gen, provider);
    }
}
