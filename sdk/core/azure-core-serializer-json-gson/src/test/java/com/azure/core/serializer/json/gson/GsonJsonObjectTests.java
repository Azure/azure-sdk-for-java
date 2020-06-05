// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.util.serializer.JsonNode;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link GsonJsonObject}.
 */
public class GsonJsonObjectTests {
    private static final GsonJsonPrimitive NAME;
    private static final GsonJsonPrimitive ADDRESS;
    private static final GsonJsonPrimitive CITY;
    private static final GsonJsonPrimitive ZIP;
    private static final Supplier<GsonJsonObject> LOCATION_SUPPLIER;

    static {
        NAME = new GsonJsonPrimitive("John Doe");
        ADDRESS = new GsonJsonPrimitive("street address");
        CITY = new GsonJsonPrimitive("city");
        ZIP = new GsonJsonPrimitive(99999);
        LOCATION_SUPPLIER = () -> (GsonJsonObject) new GsonJsonObject()
            .put("address", ADDRESS)
            .put("city", CITY)
            .put("zip", ZIP);
    }

    @Test
    public void emptyConstructor() {
        GsonJsonObject gsonJsonObject = new GsonJsonObject();

        assertEquals(new JsonObject(), gsonJsonObject.getJsonObject());
    }

    @Test
    public void jsonObjectConstructor() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("test", true);

        GsonJsonObject gsonJsonObject = new GsonJsonObject(jsonObject);

        assertEquals(jsonObject, gsonJsonObject.getJsonObject());
    }

    @Test
    public void nullJsonObjectConstructorThrows() {
        assertThrows(NullPointerException.class, () -> new GsonJsonObject(null));
    }

    @ParameterizedTest
    @MethodSource("fieldsSupplier")
    public void fields(GsonJsonObject gsonJsonObject, Set<JsonNode> expected) {
        Set<JsonNode> actual = gsonJsonObject.fields()
            .map(Map.Entry::getValue)
            .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    private static Stream<Arguments> fieldsSupplier() {
        GsonJsonObject simpleObject = new GsonJsonObject();
        simpleObject.put("name", NAME);

        GsonJsonObject complexObject = new GsonJsonObject();
        complexObject.put("name", NAME);
        complexObject.put("location", LOCATION_SUPPLIER.get());

        Set<JsonNode> complexObjectFields = new HashSet<>();
        complexObjectFields.add(NAME);
        complexObjectFields.add(LOCATION_SUPPLIER.get());

        return Stream.of(
            Arguments.of(new GsonJsonObject(), new HashSet<>()),
            Arguments.of(simpleObject, Collections.singleton(NAME)),
            Arguments.of(complexObject, complexObjectFields)
        );
    }

    @ParameterizedTest
    @MethodSource("fieldNamesSupplier")
    public void fieldNames(GsonJsonObject gsonJsonObject, Set<String> expected) {
        Set<String> actual = gsonJsonObject.fieldNames().collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    private static Stream<Arguments> fieldNamesSupplier() {
        GsonJsonObject simpleObject = new GsonJsonObject();
        simpleObject.put("name", NAME);

        GsonJsonObject complexObject = new GsonJsonObject();
        complexObject.put("name", NAME);
        complexObject.put("location", LOCATION_SUPPLIER.get());

        Set<String> complexObjectFieldNames = new HashSet<>();
        complexObjectFieldNames.add("name");
        complexObjectFieldNames.add("location");

        return Stream.of(
            Arguments.of(new GsonJsonObject(), new HashSet<>()),
            Arguments.of(simpleObject, Collections.singleton("name")),
            Arguments.of(complexObject, complexObjectFieldNames)
        );
    }

    @ParameterizedTest
    @MethodSource("getSupplier")
    public void get(GsonJsonObject gsonJsonObject, String name, JsonNode expected) {
        assertEquals(expected, gsonJsonObject.get(name));
    }

    private static Stream<Arguments> getSupplier() {
        return Stream.of(
            Arguments.of(new GsonJsonObject(), "missing", null),
            Arguments.of(LOCATION_SUPPLIER.get(), "missing", null),
            Arguments.of(LOCATION_SUPPLIER.get(), "city", CITY)
        );
    }

    @ParameterizedTest
    @MethodSource("hasSupplier")
    public void has(GsonJsonObject gsonJsonObject, String name, boolean expected) {
        assertEquals(expected, gsonJsonObject.has(name));
    }

    private static Stream<Arguments> hasSupplier() {
        return Stream.of(
            Arguments.of(new GsonJsonObject(), "missing", false),
            Arguments.of(LOCATION_SUPPLIER.get(), "missing", false),
            Arguments.of(LOCATION_SUPPLIER.get(), "city", true)
        );
    }

    @ParameterizedTest
    @MethodSource("putSupplier")
    public void put(GsonJsonObject gsonJsonObject, String name, JsonNode jsonNode, JsonObject expected) {
        assertEquals(expected, ((GsonJsonObject) gsonJsonObject.put(name, jsonNode)).getJsonObject());
    }

    private static Stream<Arguments> putSupplier() {
        JsonObject nullMissing = new JsonObject();
        nullMissing.add("missing", JsonNull.INSTANCE);

        GsonJsonObject location = LOCATION_SUPPLIER.get();
        JsonObject locationNullMissing = location.getJsonObject().deepCopy();
        locationNullMissing.add("missing", JsonNull.INSTANCE);

        GsonJsonObject location2 = LOCATION_SUPPLIER.get();
        JsonObject locationNewCity = location2.getJsonObject().deepCopy();
        locationNewCity.addProperty("city", "new_city");

        return Stream.of(
            Arguments.of(new GsonJsonObject(), "missing", GsonJsonNull.INSTANCE, nullMissing),
            Arguments.of(location, "missing", GsonJsonNull.INSTANCE, locationNullMissing),
            Arguments.of(location2, "city", new GsonJsonPrimitive("new_city"), locationNewCity)
        );
    }

    @ParameterizedTest
    @MethodSource("removeSupplier")
    public void remove(GsonJsonObject gsonJsonObject, String name, JsonNode expected) {
        assertEquals(expected, gsonJsonObject.remove(name));
    }

    private static Stream<Arguments> removeSupplier() {
        return Stream.of(
            Arguments.of(new GsonJsonObject(), "missing", null),
            Arguments.of(LOCATION_SUPPLIER.get(), "missing", null),
            Arguments.of(LOCATION_SUPPLIER.get(), "city", CITY)
        );
    }

    @ParameterizedTest
    @MethodSource("setSupplier")
    public void set(GsonJsonObject gsonJsonObject, String name, JsonNode jsonNode, JsonNode expectedOldValue,
        JsonObject expectedUpdatedObject) {
        JsonNode oldValue = gsonJsonObject.set(name, jsonNode);

        assertEquals(expectedOldValue, oldValue);
        assertEquals(expectedUpdatedObject, gsonJsonObject.getJsonObject());
    }

    private static Stream<Arguments> setSupplier() {
        JsonObject nullMissing = new JsonObject();
        nullMissing.add("missing", JsonNull.INSTANCE);

        GsonJsonObject location = LOCATION_SUPPLIER.get();
        JsonObject locationNullMissing = location.getJsonObject().deepCopy();
        locationNullMissing.add("missing", JsonNull.INSTANCE);

        JsonObject locationNewCity = location.getJsonObject().deepCopy();
        locationNewCity.addProperty("city", "new_city");

        return Stream.of(
            Arguments.of(new GsonJsonObject(), "missing", GsonJsonNull.INSTANCE, null, nullMissing),
            Arguments.of(LOCATION_SUPPLIER.get(), "missing", GsonJsonNull.INSTANCE, null, locationNullMissing),
            Arguments.of(LOCATION_SUPPLIER.get(), "city", new GsonJsonPrimitive("new_city"), CITY, locationNewCity)
        );
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void equals(GsonJsonObject gsonJsonObject, Object obj, boolean expected) {
        assertEquals(expected, gsonJsonObject.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        GsonJsonObject gsonJsonObject = new GsonJsonObject();

        return Stream.of(
            // Same object.
            Arguments.of(gsonJsonObject, gsonJsonObject, true),

            // Null and different object types.
            Arguments.of(gsonJsonObject, null, false),
            Arguments.of(gsonJsonObject, 0, false),

            // Same object type, same values.
            Arguments.of(gsonJsonObject, new GsonJsonObject(), true),

            // Same object type, different values.
            Arguments.of(gsonJsonObject, new GsonJsonObject().put("property", GsonJsonNull.INSTANCE), false)
        );
    }

    @Test
    public void hashCodeTest() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("test", true);

        GsonJsonObject gsonJsonObject = new GsonJsonObject(jsonObject);

        assertEquals(jsonObject.hashCode(), gsonJsonObject.hashCode());
    }
}
