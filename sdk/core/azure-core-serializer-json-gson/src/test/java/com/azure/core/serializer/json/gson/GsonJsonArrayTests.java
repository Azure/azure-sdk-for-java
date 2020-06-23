// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.util.serializer.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link GsonJsonArray}.
 */
public class GsonJsonArrayTests {

    @Test
    public void emptyConstructor() {
        GsonJsonArray gsonJsonArray = new GsonJsonArray();

        assertEquals(new JsonArray(), gsonJsonArray.getJsonArray());
    }

    @Test
    public void jsonArrayConstructor() {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(false);

        GsonJsonArray gsonJsonArray = new GsonJsonArray(jsonArray);

        assertEquals(jsonArray, gsonJsonArray.getJsonArray());
    }

    @Test
    public void nullJsonArrayConstructorThrows() {
        assertThrows(NullPointerException.class, () -> new GsonJsonArray(null));
    }

    @ParameterizedTest
    @MethodSource("addSupplier")
    public void add(GsonJsonArray gsonJsonArray, JsonNode jsonNode, JsonArray expected) {
        assertEquals(expected, ((GsonJsonArray) gsonJsonArray.add(jsonNode)).getJsonArray());
    }

    private static Stream<Arguments> addSupplier() {
        JsonArray simpleExpected = new JsonArray();
        simpleExpected.add(false);

        JsonArray complexExpected = new JsonArray();
        complexExpected.add(false);
        complexExpected.add(new JsonObject());

        GsonJsonArray gsonJsonArray = new GsonJsonArray();
        gsonJsonArray.add(new GsonJsonPrimitive(false));

        return Stream.of(
            Arguments.of(new GsonJsonArray(), new GsonJsonPrimitive(false), simpleExpected),
            Arguments.of(gsonJsonArray, new GsonJsonObject(), complexExpected)
        );
    }

    @Test
    public void clear() {
        GsonJsonArray gsonJsonArray = (GsonJsonArray) new GsonJsonArray()
            .add(GsonJsonNull.INSTANCE)
            .add(GsonJsonNull.INSTANCE)
            .add(GsonJsonNull.INSTANCE);

        gsonJsonArray.clear();
        assertEquals(0, gsonJsonArray.size());
    }

    @ParameterizedTest
    @MethodSource("elementsSupplier")
    public void elements(GsonJsonArray gsonJsonArray, Set<JsonNode> expected) {
        assertEquals(expected, gsonJsonArray.elements().collect(Collectors.toSet()));
    }

    private static Stream<Arguments> elementsSupplier() {
        GsonJsonArray simpleArray = (GsonJsonArray) new GsonJsonArray()
            .add(GsonJsonNull.INSTANCE);

        GsonJsonArray complexArray = (GsonJsonArray) new GsonJsonArray()
            .add(GsonJsonNull.INSTANCE)
            .add(new GsonJsonPrimitive("value"));

        return Stream.of(
            Arguments.of(new GsonJsonArray(), new HashSet<>()),
            Arguments.of(simpleArray, Collections.singleton(GsonJsonNull.INSTANCE)),
            Arguments.of(complexArray, new HashSet<>(Arrays.asList(GsonJsonNull.INSTANCE,
                new GsonJsonPrimitive("value"))))
        );
    }

    @ParameterizedTest
    @MethodSource("getSupplier")
    public void get(GsonJsonArray gsonJsonArray, int index, JsonNode expected) {
        assertEquals(expected, gsonJsonArray.get(index));
    }

    private static Stream<Arguments> getSupplier() {
        GsonJsonArray gsonJsonArray = (GsonJsonArray) new GsonJsonArray()
            .add(GsonJsonNull.INSTANCE)
            .add(new GsonJsonPrimitive("value"));

        return Stream.of(
            Arguments.of(new GsonJsonArray().add(GsonJsonNull.INSTANCE), 0, GsonJsonNull.INSTANCE),
            Arguments.of(gsonJsonArray, 0, GsonJsonNull.INSTANCE),
            Arguments.of(gsonJsonArray, 1, new GsonJsonPrimitive("value"))
        );
    }

    @ParameterizedTest
    @MethodSource("hasSupplier")
    public void has(GsonJsonArray gsonJsonArray, int index, boolean expected) {
        assertEquals(expected, gsonJsonArray.has(index));
    }

    private static Stream<Arguments> hasSupplier() {
        GsonJsonArray gsonJsonArray = (GsonJsonArray) new GsonJsonArray()
            .add(GsonJsonNull.INSTANCE)
            .add(new GsonJsonPrimitive("value"));

        return Stream.of(
            Arguments.of(new GsonJsonArray().add(GsonJsonNull.INSTANCE), 0, true),
            Arguments.of(gsonJsonArray, 0, true),
            Arguments.of(gsonJsonArray, 1, true),
            Arguments.of(gsonJsonArray, 2, false)
        );
    }

    @ParameterizedTest
    @MethodSource("removeSupplier")
    public void remove(GsonJsonArray gsonJsonArray, int index, JsonNode expectedValue) {
        assertEquals(expectedValue, gsonJsonArray.remove(index));
    }

    private static Stream<Arguments> removeSupplier() {
        GsonJsonArray gsonJsonArray = (GsonJsonArray) new GsonJsonArray()
            .add(GsonJsonNull.INSTANCE)
            .add(new GsonJsonPrimitive("value"));

        return Stream.of(
            Arguments.of(new GsonJsonArray().add(GsonJsonNull.INSTANCE), 0, GsonJsonNull.INSTANCE),
            Arguments.of(new GsonJsonArray(gsonJsonArray.getJsonArray().deepCopy()), 0, GsonJsonNull.INSTANCE),
            Arguments.of(new GsonJsonArray(gsonJsonArray.getJsonArray().deepCopy()), 1, new GsonJsonPrimitive("value"))
        );
    }

    @ParameterizedTest
    @MethodSource("setSupplier")
    public void set(GsonJsonArray gsonJsonArray, int index, JsonNode jsonNode, JsonNode expectedOldValue,
        JsonArray expectedArray) {
        JsonNode actualOldValue = gsonJsonArray.set(index, jsonNode);

        assertEquals(expectedOldValue, actualOldValue);
        assertEquals(expectedArray, gsonJsonArray.getJsonArray());
    }

    private static Stream<Arguments> setSupplier() {
        GsonJsonArray gsonJsonArray = (GsonJsonArray) new GsonJsonArray().add(GsonJsonNull.INSTANCE)
            .add(new GsonJsonPrimitive("value"));

        GsonJsonPrimitive gsonJsonPrimitive = new GsonJsonPrimitive(false);
        JsonArray replacedNull = new JsonArray();
        replacedNull.add(false);

        JsonArray replaceFirstValue = new JsonArray();
        replaceFirstValue.add(false);
        replaceFirstValue.add("value");

        JsonArray replaceSecondValue = new JsonArray();
        replaceSecondValue.add(JsonNull.INSTANCE);
        replaceSecondValue.add(JsonNull.INSTANCE);

        return Stream.of(
            Arguments.of(new GsonJsonArray().add(GsonJsonNull.INSTANCE), 0, gsonJsonPrimitive, GsonJsonNull.INSTANCE,
                replacedNull),
            Arguments.of(new GsonJsonArray(gsonJsonArray.getJsonArray().deepCopy()), 0, gsonJsonPrimitive,
                GsonJsonNull.INSTANCE, replaceFirstValue),
            Arguments.of(new GsonJsonArray(gsonJsonArray.getJsonArray().deepCopy()), 1, GsonJsonNull.INSTANCE,
                new GsonJsonPrimitive("value"), replaceSecondValue)
        );
    }

    @ParameterizedTest
    @MethodSource("outOfBoundsSupplier")
    public void outOfBounds(Consumer<Integer> executable, int index) {
        assertThrows(IndexOutOfBoundsException.class, () -> executable.accept(index));
    }

    private static Stream<Arguments> outOfBoundsSupplier() {
        GsonJsonArray gsonJsonArray = new GsonJsonArray();
        Consumer<Integer> add = gsonJsonArray::get;
        Consumer<Integer> remove = gsonJsonArray::remove;
        Consumer<Integer> set = (index) -> gsonJsonArray.set(index, GsonJsonNull.INSTANCE);

        return Stream.of(
            Arguments.of(add, -1),
            Arguments.of(add, 999999),
            Arguments.of(remove, -1),
            Arguments.of(remove, 999999),
            Arguments.of(set, -1),
            Arguments.of(set, 999999)
        );
    }

    @ParameterizedTest
    @MethodSource("sizeSupplier")
    public void size(GsonJsonArray gsonJsonArray, int expected) {
        assertEquals(expected, gsonJsonArray.size());
    }

    private static Stream<Arguments> sizeSupplier() {
        GsonJsonArray gsonJsonArray = new GsonJsonArray();
        gsonJsonArray.add(GsonJsonNull.INSTANCE).add(GsonJsonNull.INSTANCE);

        return Stream.of(
            Arguments.of(new GsonJsonArray(), 0),
            Arguments.of(gsonJsonArray, 2)
        );
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void equals(GsonJsonArray gsonJsonArray, Object obj, boolean expected) {
        assertEquals(expected, gsonJsonArray.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        GsonJsonArray gsonJsonArray = new GsonJsonArray();

        return Stream.of(
            // Same object.
            Arguments.of(gsonJsonArray, gsonJsonArray, true),

            // Null and different object types.
            Arguments.of(gsonJsonArray, null, false),
            Arguments.of(gsonJsonArray, 0, false),

            // Same object type, same values.
            Arguments.of(gsonJsonArray, new GsonJsonArray(), true),

            // Same object type, different values.
            Arguments.of(gsonJsonArray, new GsonJsonArray().add(GsonJsonNull.INSTANCE), false)
        );
    }

    @Test
    public void hashSetTest() {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(false);
        GsonJsonArray gsonJsonArray = new GsonJsonArray(jsonArray);

        assertEquals(jsonArray.hashCode(), gsonJsonArray.hashCode());
    }
}
