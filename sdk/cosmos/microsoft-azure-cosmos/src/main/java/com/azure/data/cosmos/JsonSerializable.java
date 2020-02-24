// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Strings;
import com.azure.data.cosmos.internal.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents a base resource that can be serialized to JSON in the Azure Cosmos DB database service.
 */
public class JsonSerializable {
    private final static Logger logger = LoggerFactory.getLogger(JsonSerializable.class);
    private final static ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();
    private ObjectMapper om;
    transient ObjectNode propertyBag = null;

    protected JsonSerializable() {
        this.propertyBag = OBJECT_MAPPER.createObjectNode();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the JsonSerializable.
     * @param objectMapper the custom object mapper
     */
    JsonSerializable(String jsonString, ObjectMapper objectMapper) {
        // TODO: Made package private due to #153. #171 adding custom serialization options back.
        this.propertyBag = fromJson(jsonString);
        this.om = objectMapper;
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the JsonSerializable.
     */
    protected JsonSerializable(String jsonString) {
        this.propertyBag = fromJson(jsonString);
    }

    /**
     * Constructor.
     *
     * @param objectNode the {@link ObjectNode} that represent the {@link JsonSerializable}
     */
    JsonSerializable(ObjectNode objectNode) {
        this.propertyBag = objectNode;
    }

    private ObjectMapper getMapper() {
        // TODO: Made package private due to #153. #171 adding custom serialization options back.
        if (this.om != null) { return this.om; }
        return OBJECT_MAPPER;
    }

    void setMapper(ObjectMapper om) {
        this.om = om;
    }

    private static void checkForValidPOJO(Class<?> c) {
        if (c.isAnonymousClass() || c.isLocalClass()) {
            throw new IllegalArgumentException(
                    String.format("%s can't be an anonymous or local class.", c.getName()));
        }
        if (c.isMemberClass() && !Modifier.isStatic(c.getModifiers())) {
            throw new IllegalArgumentException(
                    String.format("%s must be static if it's a member class.", c.getName()));
        }
    }

    @JsonIgnore
    public Logger getLogger() {
        return logger;
    }

    void populatePropertyBag() {
    }

    /**
     * Returns the propertybag(JSONObject) in a hashMap
     *
     * @return the HashMap.
     */
    public Map<String, Object> getMap() {
        return getMapper().convertValue(this.propertyBag, HashMap.class);
    }

    /**
     * Checks whether a property exists.
     *
     * @param propertyName the property to look up.
     * @return true if the property exists.
     */
    public boolean has(String propertyName) {
        return this.propertyBag.has(propertyName);
    }

    /**
     * Removes a value by propertyName.
     *
     * @param propertyName the property to remove.
     */
    void remove(String propertyName) {
        this.propertyBag.remove(propertyName);
    }

    /**
     * Sets the value of a property.
     *
     * @param <T>          the type of the object.
     * @param propertyName the property to set.
     * @param value        the value of the property.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    <T> void set(String propertyName, T value) {
        if (value == null) {
            // Sets null.
            this.propertyBag.putNull(propertyName);
        } else if (value instanceof Collection) {
            // Collection.
            ArrayNode jsonArray = propertyBag.arrayNode();
            this.internalSetCollection(propertyName, (Collection) value, jsonArray);
            this.propertyBag.set(propertyName, jsonArray);
        } else if (value instanceof JsonNode) {
            this.propertyBag.set(propertyName, (JsonNode) value);
        }  else if (value instanceof JsonSerializable) {
            // JsonSerializable
            JsonSerializable castedValue = (JsonSerializable) value;
            if (castedValue != null) {
                castedValue.populatePropertyBag();
            }
            this.propertyBag.set(propertyName, castedValue != null ? castedValue.propertyBag : null);
        } else {
            // POJO, ObjectNode, number (includes int, float, double etc), boolean,
            // and string.
            this.propertyBag.set(propertyName, getMapper().valueToTree(value));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> void internalSetCollection(String propertyName, Collection<T> collection, ArrayNode targetArray) {
        for (T childValue : collection) {
            if (childValue == null) {
                // Sets null.
                targetArray.addNull();
            } else if (childValue instanceof Collection) {
                // When T is also a Collection, use recursion.
                ArrayNode childArray = targetArray.addArray();
                this.internalSetCollection(propertyName, (Collection) childValue, childArray);
            } else if (childValue instanceof JsonNode) {
                targetArray.add((JsonNode) childValue);
            } else if (childValue instanceof JsonSerializable) {
                // JsonSerializable
                JsonSerializable castedValue = (JsonSerializable) childValue;
                castedValue.populatePropertyBag();
                targetArray.add(castedValue.propertyBag != null ? castedValue.propertyBag : this.getMapper().createObjectNode());
            } else {
                // POJO, JSONObject, NUMBER (includes Int, Float, Double etc),
                // Boolean, and STRING.
                targetArray.add(this.getMapper().valueToTree(childValue));
            }
        }
    }

    /**
     * Gets a property value as Object.
     *
     * @param propertyName the property to get.
     * @return the value of the property.
     */
    public Object get(String propertyName) {
        if (this.has(propertyName) && this.propertyBag.hasNonNull(propertyName)) {
            return getValue(this.propertyBag.get(propertyName));
        } else {
            return null;
        }
    }

    /**
     * Gets a string value.
     *
     * @param propertyName the property to get.
     * @return the string value.
     */
    public String getString(String propertyName) {
        if (this.has(propertyName) && this.propertyBag.hasNonNull(propertyName)) {
            return this.propertyBag.get(propertyName).asText();
        } else {
            return null;
        }
    }

    /**
     * Gets a boolean value.
     *
     * @param propertyName the property to get.
     * @return the boolean value.
     */
    public Boolean getBoolean(String propertyName) {
        if (this.has(propertyName) && this.propertyBag.hasNonNull(propertyName)) {
            return this.propertyBag.get(propertyName).asBoolean();
        } else {
            return null;
        }
    }

    /**
     * Gets an integer value.
     *
     * @param propertyName the property to get.
     * @return the boolean value
     */
    public Integer getInt(String propertyName) {
        if (this.has(propertyName) && this.propertyBag.hasNonNull(propertyName)) {
            return Integer.valueOf(this.propertyBag.get(propertyName).asInt());
        } else {
            return null;
        }
    }

    /**
     * Gets a long value.
     *
     * @param propertyName the property to get.
     * @return the long value
     */
    public Long getLong(String propertyName) {
        if (this.has(propertyName) && this.propertyBag.hasNonNull(propertyName)) {
            return Long.valueOf(this.propertyBag.get(propertyName).asLong());
        } else {
            return null;
        }
    }

    /**
     * Gets a double value.
     *
     * @param propertyName the property to get.
     * @return the double value.
     */
    public Double getDouble(String propertyName) {
        if (this.has(propertyName) && this.propertyBag.hasNonNull(propertyName)) {
            return new Double(this.propertyBag.get(propertyName).asDouble());
        } else {
            return null;
        }
    }

    /**
     * Gets an object value.
     *
     * @param <T>          the type of the object.
     * @param propertyName the property to get.
     * @param c            the class of the object. If c is a POJO class, it must be a member (and not an anonymous or local)
     *                     and a static one.
     * @param convertFromCamelCase  boolean indicating if String should be converted from camel case to upper case separated by underscore,
     *                              before converting to required class.
     * @return the object value.
     */
    public <T> T getObject(String propertyName, Class<T> c, boolean ... convertFromCamelCase) {
        if (this.propertyBag.has(propertyName) && this.propertyBag.hasNonNull(propertyName)) {
            JsonNode jsonObj = propertyBag.get(propertyName);
            if (Number.class.isAssignableFrom(c) || String.class.isAssignableFrom(c)
                    || Boolean.class.isAssignableFrom(c) || Object.class == c) {
                // NUMBER, STRING, Boolean
                return c.cast(getValue(jsonObj));
            } else if (Enum.class.isAssignableFrom(c)) {
                try {
                    String value = String.class.cast(getValue(jsonObj));
                    value = convertFromCamelCase.length > 0 && convertFromCamelCase[0] ? Strings.fromCamelCaseToUpperCase(value) : value;
                    return c.cast(c.getMethod("valueOf", String.class).invoke(null, value));
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException e) {
                    throw new IllegalStateException("Failed to create enum.", e);
                }
            } else if (JsonSerializable.class.isAssignableFrom(c)) {
                try {
                    Constructor<T> constructor = c.getDeclaredConstructor(String.class);
                    if(Modifier.isPrivate(constructor.getModifiers())) {
                        constructor.setAccessible(true);
                    }
                    return constructor.newInstance(toJson(jsonObj));
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    throw new IllegalStateException("Failed to instantiate class object.", e);
                }
            } else {
                // POJO
                JsonSerializable.checkForValidPOJO(c);
                try {
                    return this.getMapper().treeToValue(jsonObj, c);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to get POJO.", e);
                }
            }
        }

        return null;
    }

    /**
     * Gets an object List.
     *
     * @param <T>          the type of the objects in the List.
     * @param propertyName the property to get
     * @param c            the class of the object. If c is a POJO class, it must be a member (and not an anonymous or local)
     *                     and a static one.
     * @param convertFromCamelCase  boolean indicating if String should be converted from camel case to upper case separated by underscore,
     *                              before converting to required class.
     * @return the object collection.
     */
    public <T> List<T> getList(String propertyName, Class<T> c, boolean ... convertFromCamelCase) {
        if (this.propertyBag.has(propertyName) && this.propertyBag.hasNonNull(propertyName)) {
            ArrayNode jsonArray = (ArrayNode) this.propertyBag.get(propertyName);
            ArrayList<T> result = new ArrayList<T>();

            boolean isBaseClass = false;
            boolean isEnumClass = false;
            boolean isJsonSerializable = false;

            // Check once.
            if (Number.class.isAssignableFrom(c) || String.class.isAssignableFrom(c)
                    || Boolean.class.isAssignableFrom(c) || Object.class == c) {
                isBaseClass = true;
            } else if (Enum.class.isAssignableFrom(c)) {
                isEnumClass = true;
            } else if (JsonSerializable.class.isAssignableFrom(c)) {
                isJsonSerializable = true;
            } else {
                JsonSerializable.checkForValidPOJO(c);
            }

            for (JsonNode n : jsonArray) {
                if (isBaseClass) {
                    // NUMBER, STRING, Boolean
                    result.add(c.cast(getValue(n)));
                } else if (isEnumClass) {
                    try {
                        String value = String.class.cast(getValue(n));
                        value = convertFromCamelCase.length > 0 && convertFromCamelCase[0] ? Strings.fromCamelCaseToUpperCase(value) : value;
                        result.add(c.cast(c.getMethod("valueOf", String.class).invoke(null, value)));
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                            | NoSuchMethodException | SecurityException e) {
                        throw new IllegalStateException("Failed to create enum.", e);
                    }
                } else if (isJsonSerializable) {
                    // JsonSerializable
                    try {
                        Constructor<T> constructor = c.getDeclaredConstructor(String.class);
                        if(Modifier.isPrivate(constructor.getModifiers())) {
                            constructor.setAccessible(true);
                        }
                        result.add(constructor.newInstance(toJson(n)));
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                        throw new IllegalStateException("Failed to instantiate class object.", e);
                    }
                } else {
                    // POJO
                    try {
                        result.add(this.getMapper().treeToValue(n, c));
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to get POJO.", e);
                    }
                }
            }
            return result;
        }
        return null;
    }

    /**
     * Gets an object collection.
     *
     * @param <T>          the type of the objects in the collection.
     * @param propertyName the property to get
     * @param c            the class of the object. If c is a POJO class, it must be a member (and not an anonymous or local)
     *                     and a static one.
     * @param convertFromCamelCase  boolean indicating if String should be converted from camel case to upper case separated by underscore,
     *                              before converting to required class.
     * @return the object collection.
     */
    public <T> Collection<T> getCollection(String propertyName, Class<T> c, boolean ... convertFromCamelCase) {
        return getList(propertyName, c, convertFromCamelCase);
    }

    /**
     * Gets a JSONObject.
     *
     * @param propertyName the property to get.
     * @return the JSONObject.
     */
    ObjectNode getObject(String propertyName) {
        if (this.propertyBag.has(propertyName) && this.propertyBag.hasNonNull(propertyName)) {
            ObjectNode jsonObj = (ObjectNode) this.propertyBag.get(propertyName);
            return jsonObj;
        }
        return null;
    }

    /**
     * Gets a JSONObject collection.
     *
     * @param propertyName the property to get.
     * @return the JSONObject collection.
     */
    Collection<ObjectNode> getCollection(String propertyName) {
        Collection<ObjectNode> result = null;
        if (this.propertyBag.has(propertyName) && this.propertyBag.hasNonNull(propertyName)) {
            result = new ArrayList<ObjectNode>();

            for (JsonNode n : this.propertyBag.findValues(propertyName)) {
                result.add((ObjectNode) n);
            }
        }

        return result;
    }

    /**
     * Gets the value of a property identified by an array of property names that forms the path.
     *
     * @param propertyNames that form the path to the property to get.
     * @return the value of the property.
     */
    public Object getObjectByPath(List<String> propertyNames) {
        ObjectNode propBag = this.propertyBag;
        JsonNode value = null;
        String propertyName = null;
        Integer matchedProperties = 0;
        Iterator<String> iterator = propertyNames.iterator();
        if (iterator.hasNext()) {
            do {
                propertyName = iterator.next();
                if (propBag.has(propertyName)) {
                    matchedProperties++;
                    value = propBag.get(propertyName);
                    if (!value.isObject()) {
                        break;
                    }
                    propBag = (ObjectNode) value;
                } else {
                    break;
                }
            } while (iterator.hasNext());

            if (value != null && matchedProperties == propertyNames.size()) {
                return getValue(value);
            }
        }

        return null;
    }

    static Object getValue(JsonNode value) {
        if (value.isValueNode()) {
            switch (value.getNodeType()) {
                case BOOLEAN:
                    return value.asBoolean();
                case NUMBER:
                    if (value.isInt()) {
                        return value.asInt();
                    } else if (value.isLong()) {
                        return value.asLong();
                    } else if (value.isDouble()) {
                        return value.asDouble();
                    }
                case STRING :
                    return value.asText();
            }
        }
        return value;
    }

    private ObjectNode fromJson(String json){
        try {
            return (ObjectNode) getMapper().readTree(json);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Unable to parse JSON %s", json), e);
        }
    }

    private String toJson(Object object){
        try {
            return getMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to convert JSON to STRING", e);
        }
    }

    private String toPrettyJson(Object object){
        try {
            return getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to convert JSON to STRING", e);
        }
    }

    /**
     * Converts to an Object (only POJOs and JSONObject are supported).
     *
     * @param <T> the type of the object.
     * @param c   the class of the object, either a POJO class or JSONObject. If c is a POJO class, it must be a member
     *            (and not an anonymous or local) and a static one.
     * @return the POJO.
     */
    public <T> T toObject(Class<T> c) {
        if (JsonSerializable.class.isAssignableFrom(c) || String.class.isAssignableFrom(c)
                || Number.class.isAssignableFrom(c) || Boolean.class.isAssignableFrom(c)) {
            throw new IllegalArgumentException("c can only be a POJO class or JSONObject");
        }
        if (ObjectNode.class.isAssignableFrom(c)) {
            // JSONObject
            if (ObjectNode.class != c) {
                throw new IllegalArgumentException("We support JSONObject but not its sub-classes.");
            }
            return c.cast(this.propertyBag);
        } else {
            // POJO
            JsonSerializable.checkForValidPOJO(c);
            try {
                return this.getMapper().readValue(this.toJson(), c);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to get POJO.", e);
            }
        }
    }

    /**
     * Converts to a JSON string.
     *
     * @return the JSON string.
     */
    public String toJson() {
        return this.toJson(SerializationFormattingPolicy.NONE);
    }

    /**
     * Converts to a JSON string.
     *
     * @param formattingPolicy the formatting policy to be used.
     * @return the JSON string.
     */
    public String toJson(SerializationFormattingPolicy formattingPolicy) {
        this.populatePropertyBag();
        if (SerializationFormattingPolicy.INDENTED.equals(formattingPolicy) ) {
            return toPrettyJson(propertyBag);
        } else {
            return toJson(propertyBag);
        }
    }

    /**
     * Gets Simple STRING representation of property bag.
     *
     * For proper conversion to json and inclusion of the default values
     * use {@link #toJson()}.
     *
     * @return string representation of property bag.
     */
    public String toString() {
        return toJson(propertyBag);
    }
}
