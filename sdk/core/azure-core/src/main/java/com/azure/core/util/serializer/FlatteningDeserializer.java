// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.annotation.JsonFlatten;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Custom serializer for deserializing complex types with wrapped properties. For example, a property with annotation
 *
 * {@code @JsonProperty(value = "properties.name")} will be mapped to a top level "name" property in the POJO model.
 */
final class FlatteningDeserializer extends StdDeserializer<Object> implements ResolvableDeserializer {
    private static final long serialVersionUID = -2133095337545715498L;

    private static final Pattern IS_FLATTENED_PATTERN = Pattern.compile(".+[^\\\\]\\..+");
    private static final Pattern SPLIT_KEY_PATTERN = Pattern.compile("((?<!\\\\))\\.");

    private final BeanDescription beanDescription;

    /**
     * The default mapperAdapter for the current type.
     */
    private final JsonDeserializer<?> defaultDeserializer;

    /**
     * The object mapper for default deserialization.
     */
    private final ObjectMapper mapper;

    private final boolean classHasJsonFlatten;

    /**
     * Creates an instance of FlatteningDeserializer.
     *
     * @param beanDesc The {@link BeanDescription} of the class being deserialized.
     * @param defaultDeserializer the default JSON mapperAdapter
     * @param mapper the object mapper for default deserialization
     */
    protected FlatteningDeserializer(BeanDescription beanDesc, JsonDeserializer<?> defaultDeserializer,
        ObjectMapper mapper) {
        super(beanDesc.getBeanClass());
        this.beanDescription = beanDesc;
        this.defaultDeserializer = defaultDeserializer;
        this.mapper = mapper;
        this.classHasJsonFlatten = beanDesc.getClassAnnotations().has(JsonFlatten.class);
    }

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson ObjectMapper.
     *
     * @param mapper the object mapper for default deserialization
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule(final ObjectMapper mapper) {
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
                JsonDeserializer<?> deserializer) {
                // If the class is annotated with @JsonFlatten add the deserializer.
                // Else if any property is annotated with @JsonFlatten add the deserializer.
                // Otherwise do not add the deserializer.
                boolean hasJsonFlattenOnClass = beanDesc.getClassAnnotations().has(JsonFlatten.class);
                boolean hasJsonFlattenOnProperty = beanDesc.findProperties().stream()
                    .filter(BeanPropertyDefinition::hasField)
                    .map(BeanPropertyDefinition::getField)
                    .anyMatch(field -> field.hasAnnotation(JsonFlatten.class));

                if (hasJsonFlattenOnClass || hasJsonFlattenOnProperty) {
                    return new FlatteningDeserializer(beanDesc, deserializer, mapper);
                }

                return deserializer;
            }
        });
        return module;
    }

    @Override
    public Object deserializeWithType(JsonParser jp, DeserializationContext cxt,
        TypeDeserializer tDeserializer) throws IOException {
        // This method will be called from Jackson for each "Json object with TypeId" as it
        // process the input data. This enable us to pre-process then give it to the next
        // deserializer in the Jackson pipeline.
        //
        // The parameter 'jp' is the reader to read "Json object with TypeId"
        //
        JsonNode currentJsonNode = mapper.readTree(jp);
        final Class<?> tClass = this.defaultDeserializer.handledType();

        for (Class<?> c : TypeUtil.getAllClasses(tClass)) {
            final JsonTypeInfo typeInfo = c.getAnnotation(com.fasterxml.jackson.annotation.JsonTypeInfo.class);
            if (typeInfo != null) {
                String typeId = typeInfo.property();
                if (containsDot(typeId)) {
                    final String typeIdOnWire = unescapeEscapedDots(typeId);
                    JsonNode typeIdValue = ((ObjectNode) currentJsonNode).remove(typeIdOnWire);
                    if (typeIdValue != null) {
                        ((ObjectNode) currentJsonNode).set(typeId, typeIdValue);
                    }
                }
            }
        }
        return tDeserializer.deserializeTypedFromAny(newJsonParserForNode(currentJsonNode), cxt);
    }

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext cxt) throws IOException {
        // This method will be called by Jackson for each "Json object" in the input wire stream
        // it is trying to deserialize.
        //
        // The parameter 'jp' is the reader to read "Json object with TypeId"
        //
        JsonNode currentJsonNode = cxt.readTree(jp);
        if (currentJsonNode.isNull()) {
            currentJsonNode = mapper.getNodeFactory().objectNode();
        }

        for (BeanPropertyDefinition beanPropertyDefinition : beanDescription.findProperties()) {
            if (!beanPropertyDefinition.hasField()) {
                continue;
            }

            handleFlatteningForField(beanPropertyDefinition.getField(), currentJsonNode);
        }

        return this.defaultDeserializer.deserialize(newJsonParserForNode(currentJsonNode), cxt);
    }

    @Override
    public void resolve(DeserializationContext cxt) throws JsonMappingException {
        if (this.defaultDeserializer instanceof ResolvableDeserializer) {
            ((ResolvableDeserializer) this.defaultDeserializer).resolve(cxt);
        }
    }

    /**
     * Given a field of a POJO class and JsonNode corresponds to the same POJO class, check field's {@link JsonProperty}
     * has flattening dots in it if so flatten the nested child JsonNode corresponds to the field in the given
     * JsonNode.
     *
     * @param annotatedField the field in a POJO class
     * @param jsonNode the json node corresponds to POJO class that field belongs to
     */
    private void handleFlatteningForField(AnnotatedField annotatedField, JsonNode jsonNode) {
        final JsonProperty jsonProperty = annotatedField.getAnnotation(JsonProperty.class);
        if (jsonProperty != null) {
            final String jsonPropValue = jsonProperty.value();
            if (jsonNode.has(jsonPropValue)) {
                // There is an additional property with it's key conflicting with the
                // JsonProperty value, escape this additional property's key.
                final String escapedJsonPropValue = jsonPropValue.replace(".", "\\.");
                ((ObjectNode) jsonNode).set(escapedJsonPropValue, jsonNode.get(jsonPropValue));
            }

            if ((classHasJsonFlatten || annotatedField.hasAnnotation(JsonFlatten.class))
                && IS_FLATTENED_PATTERN.matcher(jsonPropValue).matches()) {
                // The jsonProperty value contains flattening dots, uplift the nested
                // json node that this value resolving to the current level.
                String[] jsonNodeKeys = Arrays.stream(SPLIT_KEY_PATTERN.split(jsonPropValue))
                    .map(FlatteningDeserializer::unescapeEscapedDots)
                    .toArray(String[]::new);

                // Keep track of the JsonNodes which lead to the flattened property being deserialized.
                //
                // This will be used later to clean up the parent JsonNode so that there aren't additional dangling
                // JSON once the flattened value is uplifted. If this isn't done it can result in models with
                // @JsonAnySetter containing errant values.
                List<JsonNode> nodePath = new ArrayList<>();
                nodePath.add(jsonNode);
                JsonNode nodeToAdd = jsonNode;
                for (String jsonNodeKey : jsonNodeKeys) {
                    nodeToAdd = nodeToAdd.get(jsonNodeKey);
                    if (nodeToAdd == null) {
                        break;
                    }
                    nodePath.add(nodeToAdd);
                }

                // No sub-properties leading to the flattened property exists. Set the un-flattened property to null and
                // return early.
                if (nodePath.size() == 1) {
                    ((ObjectNode) jsonNode).set(jsonPropValue, null);
                    return;
                }

                if (!nodePath.get(nodePath.size() - 2).has(jsonNodeKeys[jsonNodeKeys.length - 1])) {
                    // If some properties leading to the flattened property exists, but not all of them, set the
                    // un-flattened property to null.
                    ((ObjectNode) jsonNode).set(jsonPropValue, null);
                } else {
                    // If all properties leading to the flattened property exists, set the un-flattened property to the
                    // value contained at the flattened location.
                    ((ObjectNode) jsonNode).set(jsonPropValue, nodePath.get(nodePath.size() - 1));
                }

                // After uplifting the flattened property attempt to clean up the flattened values.
                for (int i = nodePath.size() - 2; i >= 0; i--) {
                    // On the first child node removal and if the full flattened path didn't exist, only remove the node
                    // if it doesn't have any other children nodes.
                    if (i == nodePath.size() - 2
                        && nodePath.size() - 1 != jsonNodeKeys.length
                        && nodePath.get(i).get(jsonNodeKeys[i]).size() != 0) {
                        break;
                    }

                    ((ObjectNode) nodePath.get(i)).remove(jsonNodeKeys[i]);

                    // Only continue the removal cycle while the nodes in the flattening path have no additional
                    // children nodes.
                    if (nodePath.get(i).size() > 0) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Unescape the escaped dots in the key. Escaped dots are non-flattening dots those are preceded by slash '\'
     *
     * @param key the key unescape
     * @return unescaped key
     */
    private static String unescapeEscapedDots(String key) {
        // Replace '\.' with '.'
        return key.replace("\\.", ".");
    }

    /**
     * Checks the given string contains 0 or more dots.
     *
     * @param str the string to check
     * @return true if at least one dot found
     */
    private static boolean containsDot(String str) {
        return !CoreUtils.isNullOrEmpty(str) && str.contains(".");
    }

    /**
     * Create a JsonParser for a given json node.
     *
     * @param jsonNode the json node
     * @return the json parser
     * @throws IOException if underlying reader fails to read the json string
     */
    private static JsonParser newJsonParserForNode(JsonNode jsonNode) throws IOException {
        JsonParser parser = new JsonFactory().createParser(jsonNode.toString());
        parser.nextToken();
        return parser;
    }
}
