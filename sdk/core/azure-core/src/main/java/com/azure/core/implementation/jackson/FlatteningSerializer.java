// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.annotation.JsonFlatten;
import com.azure.core.util.ExpandableStringEnum;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.AnyGetterWriter;
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
    private static final Pattern UNESCAPED_PERIOD_PATTERN = Pattern.compile("((?<!\\\\))\\.");

    private static final Pattern CHECK_IF_ESCAPED_MAP_PATTERN = Pattern.compile(".*[^\\\\]\\\\..+");
    private static final Pattern REPLACE_ESCAPED_MAP_PATTERN = Pattern.compile("\\\\.");

    private final ClientLogger logger = new ClientLogger(FlatteningSerializer.class);

    private final BeanDescription beanDescription;

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
        this.beanDescription = beanDesc;
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

    private static List<Field> getAllDeclaredFields(Class<?> clazz) {
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
    private static void escapeMapKeys(Object value, ClientLogger logger) {
        if (value == null) {
            return;
        }

        if (value.getClass().isPrimitive()
            || value.getClass().isEnum()
            || value instanceof OffsetDateTime
            || value instanceof Duration
            || value instanceof String
            || value instanceof ExpandableStringEnum) {
            return;
        }

        if (value instanceof Map<?, ?>) {
            for (String key : new HashSet<>(((Map<String, Object>) value).keySet())) {
                if (key.contains(".")) {
                    String newKey = UNESCAPED_PERIOD_PATTERN.matcher(key).replaceAll("\\\\.");
                    Object val = ((Map<String, Object>) value).remove(key);
                    ((Map<String, Object>) value).put(newKey, val);
                }
            }

            for (Object val : ((Map<?, ?>) value).values()) {
                escapeMapKeys(val, logger);
            }

            return;
        }

        if (value instanceof List<?>) {
            for (Object val : ((List<?>) value)) {
                escapeMapKeys(val, logger);
            }
            return;
        }

        for (Field f : getAllDeclaredFields(value.getClass())) {
            // Why is this setting accessible to true?
            f.setAccessible(true);
            try {
                escapeMapKeys(f.get(value), logger);
            } catch (IllegalAccessException e) {
                throw logger.logExceptionAsError(new RuntimeException(e));
            }
        }
    }

    @Override
    public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider,
        TypeSerializer typeSer) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        if (classHasJsonFlatten) {
            classLevelFlattenSerialize(value, gen);
        } else {
            ObjectNode node = mapper.createObjectNode();

            if (typeSer != null) {
                // Need to write JsonType information before serialization.
                node.put(typeSer.getPropertyName(), typeSer.getTypeIdResolver().idFromValue(value));
            }

            propertyOnlyFlattenSerialize(value, gen, provider, node);
        }
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        serializeWithType(value, gen, provider, null);
    }

    @Override
    public void resolve(SerializerProvider provider) throws JsonMappingException {
        if (this.defaultSerializer instanceof ResolvableSerializer) {
            ((ResolvableSerializer) this.defaultSerializer).resolve(provider);
        }
    }

    private void propertyOnlyFlattenSerialize(Object value, JsonGenerator gen, SerializerProvider provider,
        ObjectNode node) throws IOException {
        for (BeanPropertyDefinition beanProp : beanDescription.findProperties()) {
            ObjectNode nodeToUse = node;
            String propertyName = beanProp.getName();
            if (jsonPropertiesWithJsonFlatten.contains(beanProp.getName())) {
                String[] splitNames = UNESCAPED_PERIOD_PATTERN.split(beanProp.getName());
                propertyName = splitNames[splitNames.length - 1];

                // Find or create the ObjectNodes to use.
                // This is done so that multiple flattened properties using the same path doesn't result in objects
                // for each child property.
                //
                // For example a class with two JSON flattened properties with names "flattened.string" and
                // "flattened.number" should serialize into the following:
                //
                // {
                //   "flattened": {
                //     "string": "string",
                //     "number": 0
                //   }
                // }
                //
                // If this isn't done it could result in the following:
                //
                // {
                //   "flattened": {
                //     { "string": "string" },
                //     { "number": 0 }
                //   }
                // }
                for (int i = 0; i < splitNames.length - 1; i++) {
                    nodeToUse = (nodeToUse.has(splitNames[i]))
                        ? (ObjectNode) nodeToUse.get(splitNames[i])
                        : nodeToUse.putObject(splitNames[i]);
                }
            }

            nodeToUse.putPOJO(propertyName, beanProp.getField().getValue(value));
        }

        gen.writeStartObject();

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            gen.writeFieldName(field.getKey());
            gen.writeTree(field.getValue());
        }

        // Attempt to find if the class has a method with @JsonAnyGetter.
        //
        // If @JsonAnyGetter is found serialize the properties into the generator before finalizing the object.
        // Values from the any getter are serialized as key:fields and not as a sub-object.
        AnnotatedMember anyGetter = beanDescription.findAnyGetter();
        if (anyGetter != null && anyGetter.getAnnotation(JsonAnyGetter.class).enabled()) {
            BeanProperty.Std anyProperty = new BeanProperty.Std(PropertyName.construct(anyGetter.getName()),
                anyGetter.getType(), null, anyGetter, PropertyMetadata.STD_OPTIONAL);
            JsonSerializer<Object> anySerializer = provider.findTypedValueSerializer(anyGetter.getType(), true,
                anyProperty);
            AnyGetterWriter anyGetterWriter = new AnyGetterWriter(anyProperty, anyGetter, anySerializer);

            try {
                anyGetterWriter.getAndSerialize(value, gen, provider);
            } catch (Exception exception) {
                throw logger.logThrowableAsError(new IOException(exception));
            }
        }

        gen.writeEndObject();
    }

    private void classLevelFlattenSerialize(Object value, JsonGenerator gen) throws IOException {
        escapeMapKeys(value, logger);

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

                if (CHECK_IF_FLATTEN_PROPERTY_PATTERN.matcher(key).matches()) {
                    // Handle flattening properties
                    String[] values = UNESCAPED_PERIOD_PATTERN.split(key);
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
        gen.writeTree(res);
    }
}
