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
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Custom serializer for deserializing complex types with wrapped properties. For example, a property with annotation
 *
 * {@code @JsonProperty(value = "properties.name")} will be mapped to a top level "name" property in the POJO model.
 */
final class FlatteningDeserializer extends StdDeserializer<Object> implements ResolvableDeserializer {
    private static final long serialVersionUID = -2133095337545715498L;

    /**
     * The default mapperAdapter for the current type.
     */
    private final JsonDeserializer<?> defaultDeserializer;

    /**
     * The object mapper for default deserializations.
     */
    private final ObjectMapper mapper;

    /**
     * Creates an instance of FlatteningDeserializer.
     *
     * @param vc handled type
     * @param defaultDeserializer the default JSON mapperAdapter
     * @param mapper the object mapper for default deserializations
     */
    protected FlatteningDeserializer(Class<?> vc, JsonDeserializer<?> defaultDeserializer, ObjectMapper mapper) {
        super(vc);
        this.defaultDeserializer = defaultDeserializer;
        this.mapper = mapper;
    }

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson ObjectMapper.
     *
     * @param mapper the object mapper for default deserializations
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule(final ObjectMapper mapper) {
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                BeanDescription beanDesc,
                JsonDeserializer<?> deserializer) {
                if (beanDesc.getBeanClass().getAnnotation(JsonFlatten.class) != null) {
                    // Register 'FlatteningDeserializer' for complex type so that 'deserializeWithType'
                    // will get called for complex types and it can analyze typeId discriminator.
                    return new FlatteningDeserializer(beanDesc.getBeanClass(), deserializer, mapper);
                } else {
                    return deserializer;
                }
            }
        });
        return module;
    }

    @Override
    public Object deserializeWithType(JsonParser jp,
        DeserializationContext cxt,
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
        JsonNode currentJsonNode = mapper.readTree(jp);
        if (currentJsonNode.isNull()) {
            currentJsonNode = mapper.getNodeFactory().objectNode();
        }
        final Class<?> tClass = this.defaultDeserializer.handledType();
        for (Class<?> c : TypeUtil.getAllClasses(tClass)) {
            if (c.isAssignableFrom(Object.class)) {
                continue;
            } else {
                for (Field classField : c.getDeclaredFields()) {
                    handleFlatteningForField(classField, currentJsonNode);
                }
            }
        }
        return this.defaultDeserializer.deserialize(newJsonParserForNode(currentJsonNode), cxt);
    }

    @Override
    public void resolve(DeserializationContext cxt) throws JsonMappingException {
        ((ResolvableDeserializer) this.defaultDeserializer).resolve(cxt);
    }

    /**
     * Given a field of a POJO class and JsonNode corresponds to the same POJO class, check field's {@link JsonProperty}
     * has flattening dots in it if so flatten the nested child JsonNode corresponds to the field in the given
     * JsonNode.
     *
     * @param classField the field in a POJO class
     * @param jsonNode the json node corresponds to POJO class that field belongs to
     */
    private static void handleFlatteningForField(Field classField, JsonNode jsonNode) {
        final JsonProperty jsonProperty = classField.getAnnotation(JsonProperty.class);
        if (jsonProperty != null) {
            final String jsonPropValue = jsonProperty.value();
            if (jsonNode.has(jsonPropValue)) {
                // There is an additional property with it's key conflicting with the
                // JsonProperty value, escape this additional property's key.
                final String escapedJsonPropValue = jsonPropValue.replace(".", "\\.");
                ((ObjectNode) jsonNode).set(escapedJsonPropValue, jsonNode.get(jsonPropValue));
            }
            if (containsFlatteningDots(jsonPropValue)) {
                // The jsonProperty value contains flattening dots, uplift the nested
                // json node that this value resolving to the current level.
                JsonNode childJsonNode = findNestedNode(jsonNode, jsonPropValue);
                ((ObjectNode) jsonNode).set(jsonPropValue, childJsonNode);
            }
        }
    }

    /**
     * Checks whether the given key has flattening dots in it. Flattening dots are dot '.' characters those are not
     * preceded by slash '\'
     *
     * @param key the key
     * @return true if the key has flattening dots, false otherwise.
     */
    private static boolean containsFlatteningDots(String key) {
        return key.matches(".+[^\\\\]\\..+");
    }

    /**
     * Given a json node, find a nested node in it identified by the given composed key.
     *
     * @param jsonNode the parent json node
     * @param composedKey a key combines multiple keys using flattening dots. Flattening dots are dot character '.'
     * those are not preceded by slash '\' Each flattening dot represents a level with following key as field key in
     * that level
     * @return nested json node located using given composed key
     */
    private static JsonNode findNestedNode(JsonNode jsonNode, String composedKey) {
        String[] jsonNodeKeys = splitKeyByFlatteningDots(composedKey);
        for (String jsonNodeKey : jsonNodeKeys) {
            jsonNode = jsonNode.get(unescapeEscapedDots(jsonNodeKey));
            if (jsonNode == null) {
                return null;
            }
        }
        return jsonNode;
    }

    /**
     * Split the key by flattening dots. Flattening dots are dot character '.' those are not preceded by slash '\'
     *
     * @param key the key to split
     * @return the array of sub keys
     */
    private static String[] splitKeyByFlatteningDots(String key) {
        return key.split("((?<!\\\\))\\.");
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
