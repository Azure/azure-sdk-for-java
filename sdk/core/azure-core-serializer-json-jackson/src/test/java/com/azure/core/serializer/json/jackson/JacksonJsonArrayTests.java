// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.experimental.serializer.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
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
 * Tests {@link JacksonJsonArray}.
 */
public class JacksonJsonArrayTests {
    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance;

    @Test
    public void emptyConstructor() {
        JacksonJsonArray gsonJsonArray = new JacksonJsonArray();

        assertEquals(NODE_FACTORY.arrayNode(), gsonJsonArray.getArrayNode());
    }

    @Test
    public void jsonArrayConstructor() {
        ArrayNode jsonArray = NODE_FACTORY.arrayNode();
        jsonArray.add(false);

        JacksonJsonArray gsonJsonArray = new JacksonJsonArray(jsonArray);

        assertEquals(jsonArray, gsonJsonArray.getArrayNode());
    }

    @Test
    public void nullJsonArrayConstructorThrows() {
        assertThrows(NullPointerException.class, () -> new JacksonJsonArray(null));
    }

    @ParameterizedTest
    @MethodSource("addSupplier")
    public void add(JacksonJsonArray jacksonJsonArray, JsonNode jsonNode, ArrayNode expected) {
        assertEquals(expected, ((JacksonJsonArray) jacksonJsonArray.add(jsonNode)).getArrayNode());
    }

    private static Stream<Arguments> addSupplier() {
        ArrayNode simpleExpected = NODE_FACTORY.arrayNode();
        simpleExpected.add(false);

        ArrayNode complexExpected = NODE_FACTORY.arrayNode();
        complexExpected.add(false);
        complexExpected.add(NODE_FACTORY.objectNode());

        JacksonJsonArray gsonJsonArray = new JacksonJsonArray();
        gsonJsonArray.add(new JacksonJsonPrimitive(false));

        return Stream.of(
            Arguments.of(new JacksonJsonArray(), new JacksonJsonPrimitive(false), simpleExpected),
            Arguments.of(gsonJsonArray, new JacksonJsonObject(), complexExpected)
        );
    }

    @Test
    public void clear() {
        JacksonJsonArray gsonJsonArray = (JacksonJsonArray) new JacksonJsonArray()
            .add(JacksonJsonNull.INSTANCE)
            .add(JacksonJsonNull.INSTANCE)
            .add(JacksonJsonNull.INSTANCE);

        gsonJsonArray.clear();
        assertEquals(0, gsonJsonArray.size());
    }

    @ParameterizedTest
    @MethodSource("elementsSupplier")
    public void elements(JacksonJsonArray jacksonJsonArray, Set<JsonNode> expected) {
        assertEquals(expected, jacksonJsonArray.elements().collect(Collectors.toSet()));
    }

    private static Stream<Arguments> elementsSupplier() {
        JacksonJsonArray simpleArray = (JacksonJsonArray) new JacksonJsonArray()
            .add(JacksonJsonNull.INSTANCE);

        JacksonJsonArray complexArray = (JacksonJsonArray) new JacksonJsonArray()
            .add(JacksonJsonNull.INSTANCE)
            .add(new JacksonJsonPrimitive("value"));

        return Stream.of(
            Arguments.of(new JacksonJsonArray(), new HashSet<>()),
            Arguments.of(simpleArray, Collections.singleton(JacksonJsonNull.INSTANCE)),
            Arguments.of(complexArray, new HashSet<>(Arrays.asList(JacksonJsonNull.INSTANCE,
                new JacksonJsonPrimitive("value"))))
        );
    }

    @ParameterizedTest
    @MethodSource("getSupplier")
    public void get(JacksonJsonArray jacksonJsonArray, int index, JsonNode expected) {
        assertEquals(expected, jacksonJsonArray.get(index));
    }

    private static Stream<Arguments> getSupplier() {
        JacksonJsonArray jsonArray = (JacksonJsonArray) new JacksonJsonArray()
            .add(JacksonJsonNull.INSTANCE)
            .add(new JacksonJsonPrimitive("value"));

        return Stream.of(
            Arguments.of(new JacksonJsonArray().add(JacksonJsonNull.INSTANCE), 0, JacksonJsonNull.INSTANCE),
            Arguments.of(jsonArray, 0, JacksonJsonNull.INSTANCE),
            Arguments.of(jsonArray, 1, new JacksonJsonPrimitive("value"))
        );
    }

    @ParameterizedTest
    @MethodSource("hasSupplier")
    public void has(JacksonJsonArray jacksonJsonArray, int index, boolean expected) {
        assertEquals(expected, jacksonJsonArray.has(index));
    }

    private static Stream<Arguments> hasSupplier() {
        JacksonJsonArray jsonArray = (JacksonJsonArray) new JacksonJsonArray()
            .add(JacksonJsonNull.INSTANCE)
            .add(new JacksonJsonPrimitive("value"));

        return Stream.of(
            Arguments.of(new JacksonJsonArray().add(JacksonJsonNull.INSTANCE), 0, true),
            Arguments.of(jsonArray, 0, true),
            Arguments.of(jsonArray, 1, true),
            Arguments.of(jsonArray, 2, false)
        );
    }

    @ParameterizedTest
    @MethodSource("removeSupplier")
    public void remove(JacksonJsonArray jacksonJsonArray, int index, JsonNode expectedValue) {
        assertEquals(expectedValue, jacksonJsonArray.remove(index));
    }

    private static Stream<Arguments> removeSupplier() {
        JacksonJsonArray jsonArray = (JacksonJsonArray) new JacksonJsonArray()
            .add(JacksonJsonNull.INSTANCE)
            .add(new JacksonJsonPrimitive("value"));

        return Stream.of(
            Arguments.of(new JacksonJsonArray().add(JacksonJsonNull.INSTANCE), 0, JacksonJsonNull.INSTANCE),
            Arguments.of(new JacksonJsonArray(jsonArray.getArrayNode().deepCopy()), 0, JacksonJsonNull.INSTANCE),
            Arguments.of(new JacksonJsonArray(jsonArray.getArrayNode().deepCopy()), 1,
                new JacksonJsonPrimitive("value"))
        );
    }

    @ParameterizedTest
    @MethodSource("setSupplier")
    public void set(JacksonJsonArray jacksonJsonArray, int index, JsonNode jsonNode, JsonNode expectedOldValue,
        ArrayNode expectedArray) {
        JsonNode actualOldValue = jacksonJsonArray.set(index, jsonNode);

        assertEquals(expectedOldValue, actualOldValue);
        assertEquals(expectedArray, jacksonJsonArray.getArrayNode());
    }

    private static Stream<Arguments> setSupplier() {
        JacksonJsonArray jsonArray = (JacksonJsonArray) new JacksonJsonArray().add(JacksonJsonNull.INSTANCE)
            .add(new JacksonJsonPrimitive("value"));

        JacksonJsonPrimitive jsonPrimitive = new JacksonJsonPrimitive(false);
        ArrayNode replacedNull = NODE_FACTORY.arrayNode();
        replacedNull.add(false);

        ArrayNode replaceFirstValue = NODE_FACTORY.arrayNode();
        replaceFirstValue.add(false);
        replaceFirstValue.add("value");

        ArrayNode replaceSecondValue = NODE_FACTORY.arrayNode();
        replaceSecondValue.add(NullNode.getInstance());
        replaceSecondValue.add(NullNode.getInstance());

        return Stream.of(
            Arguments.of(new JacksonJsonArray().add(JacksonJsonNull.INSTANCE), 0, jsonPrimitive,
                JacksonJsonNull.INSTANCE, replacedNull),
            Arguments.of(new JacksonJsonArray(jsonArray.getArrayNode().deepCopy()), 0, jsonPrimitive,
                JacksonJsonNull.INSTANCE, replaceFirstValue),
            Arguments.of(new JacksonJsonArray(jsonArray.getArrayNode().deepCopy()), 1, JacksonJsonNull.INSTANCE,
                new JacksonJsonPrimitive("value"), replaceSecondValue)
        );
    }

    @ParameterizedTest
    @MethodSource("outOfBoundsSupplier")
    public void outOfBounds(Consumer<Integer> executable, int index) {
        assertThrows(IndexOutOfBoundsException.class, () -> executable.accept(index));
    }

    private static Stream<Arguments> outOfBoundsSupplier() {
        JacksonJsonArray gsonJsonArray = new JacksonJsonArray();
        Consumer<Integer> add = gsonJsonArray::get;
        Consumer<Integer> remove = gsonJsonArray::remove;
        Consumer<Integer> set = (index) -> gsonJsonArray.set(index, JacksonJsonNull.INSTANCE);

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
    public void size(JacksonJsonArray jacksonJsonArray, int expected) {
        assertEquals(expected, jacksonJsonArray.size());
    }

    private static Stream<Arguments> sizeSupplier() {
        JacksonJsonArray jsonArray = new JacksonJsonArray();
        jsonArray.add(JacksonJsonNull.INSTANCE).add(JacksonJsonNull.INSTANCE);

        return Stream.of(
            Arguments.of(new JacksonJsonArray(), 0),
            Arguments.of(jsonArray, 2)
        );
    }

    @ParameterizedTest
    @MethodSource("equalsSupplier")
    public void equals(JacksonJsonArray jacksonJsonArray, Object obj, boolean expected) {
        assertEquals(expected, jacksonJsonArray.equals(obj));
    }

    private static Stream<Arguments> equalsSupplier() {
        JacksonJsonArray jsonArray = new JacksonJsonArray();

        return Stream.of(
            // Same object.
            Arguments.of(jsonArray, jsonArray, true),

            // Null and different object types.
            Arguments.of(jsonArray, null, false),
            Arguments.of(jsonArray, 0, false),

            // Same object type, same values.
            Arguments.of(jsonArray, new JacksonJsonArray(), true),

            // Same object type, different values.
            Arguments.of(jsonArray, new JacksonJsonArray().add(JacksonJsonNull.INSTANCE), false)
        );
    }

    @Test
    public void hashSetTest() {
        ArrayNode jsonArray = NODE_FACTORY.arrayNode();
        jsonArray.add(false);
        JacksonJsonArray gsonJsonArray = new JacksonJsonArray(jsonArray);

        assertEquals(jsonArray.hashCode(), gsonJsonArray.hashCode());
    }
}
