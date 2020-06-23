// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.util.serializer.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link JacksonJsonObject}.
 */
public class JacksonJsonObjectTests {
    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance;
    private static final JacksonJsonPrimitive NAME;
    private static final JacksonJsonPrimitive ADDRESS;
    private static final JacksonJsonPrimitive CITY;
    private static final JacksonJsonPrimitive ZIP;
    private static final Supplier<JacksonJsonObject> LOCATION_SUPPLIER;

    static {
        NAME = new JacksonJsonPrimitive("John Doe");
        ADDRESS = new JacksonJsonPrimitive("street address");
        CITY = new JacksonJsonPrimitive("city");
        ZIP = new JacksonJsonPrimitive(99999);
        LOCATION_SUPPLIER = () -> (JacksonJsonObject) new JacksonJsonObject()
            .put("address", ADDRESS)
            .put("city", CITY)
            .put("zip", ZIP);
    }

    @Test
    public void emptyConstructor() {
        JacksonJsonObject gsonJsonObject = new JacksonJsonObject();

        assertEquals(NODE_FACTORY.objectNode(), gsonJsonObject.getObjectNode());
    }

    @Test
    public void jsonObjectConstructor() {
        ObjectNode jsonObject = NODE_FACTORY.objectNode();
        jsonObject.put("test", true);

        JacksonJsonObject gsonJsonObject = new JacksonJsonObject(jsonObject);

        assertEquals(jsonObject, gsonJsonObject.getObjectNode());
    }

    @ParameterizedTest
    @MethodSource("fieldsSupplier")
    public void fields(JacksonJsonObject jacksonJsonObject, Set<JsonNode> expected) {
        Set<JsonNode> actual = jacksonJsonObject.fields()
            .map(Map.Entry::getValue)
            .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    private static Stream<Arguments> fieldsSupplier() {
        JacksonJsonObject simpleObject = new JacksonJsonObject();
        simpleObject.put("name", NAME);

        JacksonJsonObject complexObject = new JacksonJsonObject();
        complexObject.put("name", NAME);
        complexObject.put("location", LOCATION_SUPPLIER.get());

        Set<JsonNode> complexObjectFields = new HashSet<>();
        complexObjectFields.add(NAME);
        complexObjectFields.add(LOCATION_SUPPLIER.get());

        return Stream.of(
            Arguments.of(new JacksonJsonObject(), new HashSet<>()),
            Arguments.of(simpleObject, Collections.singleton(NAME)),
            Arguments.of(complexObject, complexObjectFields)
        );
    }

    @ParameterizedTest
    @MethodSource("fieldNamesSupplier")
    public void fieldNames(JacksonJsonObject jacksonJsonObject, Set<String> expected) {
        Set<String> actual = jacksonJsonObject.fieldNames().collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    private static Stream<Arguments> fieldNamesSupplier() {
        JacksonJsonObject simpleObject = new JacksonJsonObject();
        simpleObject.put("name", NAME);

        JacksonJsonObject complexObject = new JacksonJsonObject();
        complexObject.put("name", NAME);
        complexObject.put("location", LOCATION_SUPPLIER.get());

        Set<String> complexObjectFieldNames = new HashSet<>();
        complexObjectFieldNames.add("name");
        complexObjectFieldNames.add("location");

        return Stream.of(
            Arguments.of(new JacksonJsonObject(), new HashSet<>()),
            Arguments.of(simpleObject, Collections.singleton("name")),
            Arguments.of(complexObject, complexObjectFieldNames)
        );
    }

    @ParameterizedTest
    @MethodSource("getSupplier")
    public void get(JacksonJsonObject jacksonJsonObject, String name, JsonNode expected) {
        assertEquals(expected, jacksonJsonObject.get(name));
    }

    private static Stream<Arguments> getSupplier() {
        return Stream.of(
            Arguments.of(new JacksonJsonObject(), "missing", null),
            Arguments.of(LOCATION_SUPPLIER.get(), "missing", null),
            Arguments.of(LOCATION_SUPPLIER.get(), "city", CITY)
        );
    }

    @ParameterizedTest
    @MethodSource("hasSupplier")
    public void has(JacksonJsonObject jacksonJsonObject, String name, boolean expected) {
        assertEquals(expected, jacksonJsonObject.has(name));
    }

    private static Stream<Arguments> hasSupplier() {
        return Stream.of(
            Arguments.of(new JacksonJsonObject(), "missing", false),
            Arguments.of(LOCATION_SUPPLIER.get(), "missing", false),
            Arguments.of(LOCATION_SUPPLIER.get(), "city", true)
        );
    }

    @ParameterizedTest
    @MethodSource("putSupplier")
    public void put(JacksonJsonObject jacksonJsonObject, String name, JsonNode jsonNode, ObjectNode expected) {
        assertEquals(expected, ((JacksonJsonObject) jacksonJsonObject.put(name, jsonNode)).getObjectNode());
    }

    private static Stream<Arguments> putSupplier() {
        ObjectNode nullMissing = NODE_FACTORY.objectNode();
        nullMissing.set("missing", NullNode.getInstance());

        JacksonJsonObject location = LOCATION_SUPPLIER.get();
        ObjectNode locationNullMissing = location.getObjectNode().deepCopy();
        locationNullMissing.set("missing", NullNode.getInstance());

        JacksonJsonObject location2 = LOCATION_SUPPLIER.get();
        ObjectNode locationNewCity = location2.getObjectNode().deepCopy();
        locationNewCity.put("city", "new_city");

        return Stream.of(
            Arguments.of(new JacksonJsonObject(), "missing", JacksonJsonNull.INSTANCE, nullMissing),
            Arguments.of(location, "missing", JacksonJsonNull.INSTANCE, locationNullMissing),
            Arguments.of(location2, "city", new JacksonJsonPrimitive("new_city"), locationNewCity)
        );
    }

    @ParameterizedTest
    @MethodSource("removeSupplier")
    public void remove(JacksonJsonObject jacksonJsonObject, String name, JsonNode expected) {
        assertEquals(expected, jacksonJsonObject.remove(name));
    }

    private static Stream<Arguments> removeSupplier() {
        return Stream.of(
            Arguments.of(new JacksonJsonObject(), "missing", null),
            Arguments.of(LOCATION_SUPPLIER.get(), "missing", null),
            Arguments.of(LOCATION_SUPPLIER.get(), "city", CITY)
        );
    }

    @ParameterizedTest
    @MethodSource("setSupplier")
    public void set(JacksonJsonObject jacksonJsonObject, String name, JsonNode jsonNode, JsonNode expectedOldValue,
        ObjectNode expectedUpdatedObject) {
        JsonNode oldValue = jacksonJsonObject.set(name, jsonNode);

        assertEquals(expectedOldValue, oldValue);
        assertEquals(expectedUpdatedObject, jacksonJsonObject.getObjectNode());
    }

    private static Stream<Arguments> setSupplier() {
        ObjectNode nullMissing = NODE_FACTORY.objectNode();
        nullMissing.set("missing", NullNode.getInstance());

        JacksonJsonObject location = LOCATION_SUPPLIER.get();
        ObjectNode locationNullMissing = location.getObjectNode().deepCopy();
        locationNullMissing.set("missing", NullNode.getInstance());

        ObjectNode locationNewCity = location.getObjectNode().deepCopy();
        locationNewCity.put("city", "new_city");

        return Stream.of(
            Arguments.of(new JacksonJsonObject(), "missing", JacksonJsonNull.INSTANCE, null, nullMissing),
            Arguments.of(LOCATION_SUPPLIER.get(), "missing", JacksonJsonNull.INSTANCE, null, locationNullMissing),
            Arguments.of(LOCATION_SUPPLIER.get(), "city", new JacksonJsonPrimitive("new_city"), CITY, locationNewCity)
        );
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void equals(JacksonJsonObject jacksonJsonObject, Object obj, boolean expected) {
        assertEquals(expected, jacksonJsonObject.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        JacksonJsonObject gsonJsonObject = new JacksonJsonObject();

        return Stream.of(
            // Same object.
            Arguments.of(gsonJsonObject, gsonJsonObject, true),

            // Null and different object types.
            Arguments.of(gsonJsonObject, null, false),
            Arguments.of(gsonJsonObject, 0, false),

            // Same object type, same values.
            Arguments.of(gsonJsonObject, new JacksonJsonObject(), true),

            // Same object type, different values.
            Arguments.of(gsonJsonObject, new JacksonJsonObject().put("property", JacksonJsonNull.INSTANCE), false)
        );
    }

    @Test
    public void hashCodeTest() {
        ObjectNode jsonObject = NODE_FACTORY.objectNode();
        jsonObject.put("test", true);

        JacksonJsonObject gsonJsonObject = new JacksonJsonObject(jsonObject);

        assertEquals(jsonObject.hashCode(), gsonJsonObject.hashCode());
    }
}
