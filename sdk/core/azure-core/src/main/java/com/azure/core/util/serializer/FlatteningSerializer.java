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
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Custom serializer for serializing types with wrapped properties. For example, a property with annotation
 *
 * {@code @JsonProperty(value = "properties.name")} will be mapped from a top level "name" property in the POJO model to
 * {@code {'properties' : { 'name' : 'my_name' }}} in the serialized payload.
 */
class FlatteningSerializer extends StdSerializer<Object> implements ResolvableSerializer {
    private static final long serialVersionUID = -6130180289951110573L;

    private static final Pattern CHECK_IF_FLATTEN_PROPERTY_PATTERN = Pattern.compile(".+[^\\\\]\\..+");
    private static final Pattern SPLIT_FLATTEN_PROPERTY_PATTERN = Pattern.compile("((?<!\\\\))\\.");

    private static final Pattern CREATE_ESCAPED_MAP_PATTERN = Pattern.compile("((?<!\\\\))\\.");
    private static final Pattern CHECK_IF_ESCAPED_MAP_PATTERN = Pattern.compile(".*[^\\\\]\\\\..+");
    private static final Pattern REPLACE_ESCAPED_MAP_PATTERN = Pattern.compile("\\\\.");

    private final ClientLogger logger = new ClientLogger(FlatteningSerializer.class);

    /*
     * The default mapperAdapter for the current type.
     */
    private final JsonSerializer<?> defaultSerializer;

    /*
     * The object mapper for default serializations.
     */
    private final ObjectMapper mapper;

    /*
     * Flag indicating if the class using the serializer is annotated with @JsonFlatten.
     */
    private final boolean classHasJsonFlatten;

    /*
     * Set containing which JSON properties are annotated with @JsonFlatten.
     *
     * If classHasJsonFlatten is true this value is ignored as inspecting properties isn't required.
     */
    private final Set<String> jsonPropertiesWithJsonFlatten;

    /**
     * Creates an instance of FlatteningSerializer.
     *
     * @param beanDesc The {@link BeanDescription} of the class being serialized.
     * @param defaultSerializer The default JSON serializer.
     * @param mapper The {@link ObjectMapper} for default serialization.
     */
    FlatteningSerializer(BeanDescription beanDesc, JsonSerializer<?> defaultSerializer, ObjectMapper mapper) {
        super(beanDesc.getBeanClass(), false);
        this.defaultSerializer = defaultSerializer;
        this.mapper = mapper;
        this.classHasJsonFlatten = beanDesc.getClassAnnotations().has(JsonFlatten.class);

        if (classHasJsonFlatten) {
            // If the class is annotated with @JsonFlatten creating a JSON property -> flattened map isn't required.
            this.jsonPropertiesWithJsonFlatten = Collections.emptySet();
        } else {
            // Otherwise each property in the serialized class will be inspected for being annotated with @JsonFlatten
            // to determine which JSON properties need to be flattened.
            this.jsonPropertiesWithJsonFlatten = beanDesc.findProperties().stream()
                .filter(BeanPropertyDefinition::hasField)
                .filter(property -> property.getField().hasAnnotation(JsonFlatten.class))
                .map(BeanPropertyDefinition::getName)
                .collect(Collectors.toSet());
        }
    }

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson ObjectMapper.
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
                // If the class is annotated with @JsonFlatten add the serializer.
                // Else if any property is annotated with @JsonFlatten add the serializer.
                // Otherwise do not add the serializer.
                boolean hasJsonFlattenOnClass = beanDesc.getClassAnnotations().has(JsonFlatten.class);
                boolean hasJsonFlattenOnProperty = beanDesc.findProperties().stream()
                    .filter(BeanPropertyDefinition::hasField)
                    .map(BeanPropertyDefinition::getField)
                    .anyMatch(field -> field.hasAnnotation(JsonFlatten.class));

                if (hasJsonFlattenOnClass || hasJsonFlattenOnProperty) {
                    return new FlatteningSerializer(beanDesc, serializer, mapper);
                }

                return serializer;
            }
        });
        return module;
    }

    private List<Field> getAllDeclaredFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
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

        if (value instanceof Map<?, ?>) {
            for (String key : new HashSet<>(((Map<String, Object>) value).keySet())) {
                if (key.contains(".")) {
                    String newKey = CREATE_ESCAPED_MAP_PATTERN.matcher(key).replaceAll("\\\\.");
                    Object val = ((Map<String, Object>) value).remove(key);
                    ((Map<String, Object>) value).put(newKey, val);
                }
            }

            for (Object val : ((Map<?, ?>) value).values()) {
                escapeMapKeys(val);
            }

            return;
        }

        if (value instanceof List<?>) {
            for (Object val : ((List<?>) value)) {
                escapeMapKeys(val);
            }
            return;
        }

        int mod = value.getClass().getModifiers();
        if (Modifier.isFinal(mod) || Modifier.isStatic(mod)) {
            return;
        }

        for (Field f : getAllDeclaredFields(value.getClass())) {
            // Why is this setting accessible to true?
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
        Queue<ObjectNode> source = new LinkedBlockingQueue<>();
        Queue<ObjectNode> target = new LinkedBlockingQueue<>();
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

                // If the class isn't annotated with @JsonFlatten and the JSON property isn't annotated with
                // @JsonFlatten don't attempt flattening.
                if (!classHasJsonFlatten && !jsonPropertiesWithJsonFlatten.contains(key)) {
                    continue;
                }

                if (CHECK_IF_FLATTEN_PROPERTY_PATTERN.matcher(key).matches()) {
                    // Handle flattening properties
                    //
                    String[] values = SPLIT_FLATTEN_PROPERTY_PATTERN.split(key);
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
                            node.set(val, child);
                            node = child;
                        }
                    }
                    node.set(values[values.length - 1], resCurrent.get(key));
                    resCurrent.remove(key);
                    outNode = node.get(values[values.length - 1]);
                } else if (CHECK_IF_ESCAPED_MAP_PATTERN.matcher(key).matches()) {
                    // Handle escaped map key
                    //
                    String originalKey = REPLACE_ESCAPED_MAP_PATTERN.matcher(key).replaceAll(".");
                    resCurrent.remove(key);
                    resCurrent.set(originalKey, outNode);
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
        if (this.defaultSerializer instanceof ResolvableSerializer) {
            ((ResolvableSerializer) this.defaultSerializer).resolve(provider);
        }
    }

    @Override
    public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider,
        TypeSerializer typeSerializer) throws IOException {
        serialize(value, gen, provider);
    }
}
