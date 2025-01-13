// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.union;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test class for {@link Union}.
 */
public class UnionTests {
    private static final String STRING_VALUE = "Hello, world!";
    private static final int INT_VALUE = 42;
    private static final long LONG_VALUE = 42L;
    private static final double DOUBLE_VALUE = Math.PI;
    private static final float FLOAT_VALUE = 3.14f;

    private static final String[] STRING_ARRAY_VALUE = { "Hello", "world", "!" };
    private static final int[] INT_ARRAY_VALUE = { 1, 2, 3 };
    private static final long[] LONG_ARRAY_VALUE = { 1L, 2L, 3L };
    private static final float[] FLOAT_ARRAY_VALUE = { 1.1f, 2.2f, 3.3f };
    private static final double[] DOUBLE_ARRAY_VALUE = { 1.1d, 2.2d, 3.3d };

    private static final ParameterizedTypeImpl LIST_OF_STRING_TYPE
        = new ParameterizedTypeImpl(List.class, String.class);
    private static final ParameterizedTypeImpl LIST_OF_INTEGER_TYPE
        = new ParameterizedTypeImpl(List.class, Integer.class);
    private static final ParameterizedTypeImpl LIST_OF_LONG_TYPE = new ParameterizedTypeImpl(List.class, Long.class);
    private static final ParameterizedTypeImpl LIST_OF_FLOAT_TYPE = new ParameterizedTypeImpl(List.class, Float.class);
    private static final ParameterizedTypeImpl LIST_OF_DOUBLE_TYPE
        = new ParameterizedTypeImpl(List.class, Double.class);
    private static final ParameterizedTypeImpl SET_OF_STRING_TYPE = new ParameterizedTypeImpl(Set.class, String.class);

    private static final List<String> LIST_OF_STRING_VALUE = List.of("Hello", "world", "!");
    private static final List<Integer> LIST_OF_INTEGER_VALUE = List.of(1, 2, 3);
    private static final List<Long> LIST_OF_LONG_VALUE = List.of(1L, 2L, 3L);
    private static final List<Float> LIST_OF_FLOAT_VALUE = List.of(1.1f, 2.2f, 3.3f);
    private static final List<Double> LIST_OF_DOUBLE_VALUE = List.of(1.1d, 2.2d, 3.3d);

    private static final Set<String> SET_OF_STRING_VALUE = Set.of("Hello", "world", "!");

    @Test
    void createUnionWithMultipleTypes() {
        Union union = Union.ofTypes(String.class, Integer.class, Double.class);
        assertNotNull(union);
        assertEquals(3, union.getTypes().size());
    }

    @Test
    void setAndGetValue() {
        Union union = Union.ofTypes(String.class, Integer.class, Double.class);

        union.setValue(STRING_VALUE);
        assertEquals(String.class, union.getType());
        assertEquals(STRING_VALUE, union.getValue());

        union.setValue(INT_VALUE);
        assertEquals(Integer.class, union.getType());
        assertEquals(INT_VALUE, union.getValue(Integer.class));

        union.setValue(DOUBLE_VALUE);
        assertEquals(Double.class, union.getType());
        assertEquals(DOUBLE_VALUE, union.getValue(Double.class));
    }

    @Test
    void setValueWithInvalidType() {
        Union union = Union.ofTypes(String.class, Integer.class, Double.class);
        assertThrows(IllegalArgumentException.class, () -> union.setValue(LONG_VALUE));
        assertThrows(IllegalArgumentException.class, () -> union.setValue(FLOAT_VALUE));
    }

    @Test
    void tryConsumeWithValidType() {
        Union union = Union.ofTypes(String.class, Integer.class, Double.class);
        union.setValue(STRING_VALUE);
        assertTrue(union.tryConsume(value -> assertEquals(STRING_VALUE, value), String.class));
        union.setValue(INT_VALUE);
        assertTrue(union.tryConsume(value -> assertEquals(INT_VALUE, value), Integer.class));
        union.setValue(DOUBLE_VALUE);
        assertTrue(union.tryConsume(value -> assertEquals(DOUBLE_VALUE, value), Double.class));
    }

    @Test
    void tryConsumeWithInvalidType() {
        Union union = Union.ofTypes(String.class, Integer.class, Double.class);

        union.setValue(INT_VALUE);
        assertTrue(union.tryConsume(value -> assertEquals(INT_VALUE, value), Integer.class));
        assertFalse(union.tryConsume(value -> fail("Should not consume String"), String.class));
        assertFalse(union.tryConsume(value -> fail("Should not consume Double"), Double.class));
        assertFalse(union.tryConsume(value -> fail("Should not consume Long"), Long.class));
        assertFalse(union.tryConsume(value -> fail("Should not consume Float"), Float.class));
    }

    // Autoboxing tests

    @Test
    void createUnionWithMultipleTypesAutoboxing() {
        Union union = Union.ofTypes(String.class, int.class, double.class);
        union.setValue(STRING_VALUE);
        assertEquals(String.class, union.getType());
        assertEquals(STRING_VALUE, union.getValue());

        union.setValue(42);
        assertEquals(int.class, union.getType());
        assertEquals(42, union.getValue(int.class));

        union.setValue(DOUBLE_VALUE);
        assertEquals(double.class, union.getType());
        assertEquals(DOUBLE_VALUE, union.getValue(double.class));
    }

    @Test
    void setAndGetValueAutoboxing() {
        Union union = Union.ofTypes(String.class, double.class, int.class);
        union.setValue(STRING_VALUE);
        assertEquals(String.class, union.getType());
        assertEquals(STRING_VALUE, union.getValue(String.class));

        union.setValue(INT_VALUE);
        assertEquals(int.class, union.getType());
        assertEquals(INT_VALUE, union.getValue(int.class));

        union.setValue(DOUBLE_VALUE);
        assertEquals(double.class, union.getType());
        assertEquals(DOUBLE_VALUE, union.getValue(double.class));
    }

    @Test
    void setValueWithInvalidTypeAutoboxing() {
        Union union = Union.ofTypes(String.class, int.class, double.class);
        assertThrows(IllegalArgumentException.class, () -> union.setValue(FLOAT_VALUE));
    }

    @Test
    void tryConsumeWithValidTypeAutoboxing() {
        Union union = Union.ofTypes(String.class, int.class, double.class);

        union.setValue(STRING_VALUE);
        assertEquals(String.class, union.getType());
        assertTrue(union.tryConsume(value -> assertEquals(STRING_VALUE, value), String.class));

        union.setValue(INT_VALUE);
        assertEquals(int.class, union.getType());
        assertTrue(union.tryConsume(value -> assertEquals(INT_VALUE, value), int.class));

        union.setValue(DOUBLE_VALUE);
        assertEquals(double.class, union.getType());
        assertTrue(union.tryConsume(value -> assertEquals(DOUBLE_VALUE, value), double.class));
    }

    @Test
    void tryConsumeWithInvalidTypeAutoboxing() {
        Union union = Union.ofTypes(String.class, int.class, double.class);
        union.setValue(INT_VALUE);
        assertTrue(union.tryConsume(value -> assertEquals(INT_VALUE, value), int.class));
        assertFalse(union.tryConsume(value -> fail("Should not consume String"), String.class));
        assertFalse(union.tryConsume(value -> fail("Should not consume Double"), Double.class));
        assertFalse(union.tryConsume(value -> fail("Should not consume Long"), Long.class));
        assertFalse(union.tryConsume(value -> fail("Should not consume Float"), Float.class));
    }

    // Array types tests

    @Test
    void createUnionWithArrayTypes() {
        Union union = Union.ofTypes(String[].class, int[].class, float[].class);
        assertNotNull(union);
        assertEquals(3, union.getTypes().size());
    }

    @Test
    void setAndGetValueWithArrayTypes() {
        Union union = Union.ofTypes(String[].class, int[].class, float[].class);

        union.setValue(STRING_ARRAY_VALUE);
        assertEquals(String[].class, union.getType());
        assertArrayEquals(STRING_ARRAY_VALUE, union.getValue(String[].class));

        union.setValue(INT_ARRAY_VALUE);
        assertEquals(int[].class, union.getType());
        assertArrayEquals(INT_ARRAY_VALUE, union.getValue(int[].class));

        union.setValue(FLOAT_ARRAY_VALUE);
        assertEquals(float[].class, union.getType());
        assertArrayEquals(FLOAT_ARRAY_VALUE, union.getValue(float[].class));
    }

    @Test
    void setValueWithInvalidArrayType() {
        Union union = Union.ofTypes(String[].class, int[].class, float[].class);
        assertThrows(IllegalArgumentException.class, () -> union.setValue(LONG_ARRAY_VALUE));
        assertThrows(IllegalArgumentException.class, () -> union.setValue(DOUBLE_ARRAY_VALUE));
    }

    @Test
    void tryConsumeWithValidArrayType() {
        Union union = Union.ofTypes(String[].class, int[].class, float[].class);

        union.setValue(STRING_ARRAY_VALUE);
        assertTrue(union.tryConsume(value -> assertArrayEquals(STRING_ARRAY_VALUE, value), String[].class));

        union.setValue(INT_ARRAY_VALUE);
        assertTrue(union.tryConsume(value -> assertArrayEquals(INT_ARRAY_VALUE, value), int[].class));

        union.setValue(FLOAT_ARRAY_VALUE);
        assertTrue(union.tryConsume(value -> assertArrayEquals(FLOAT_ARRAY_VALUE, value), float[].class));
    }

    @Test
    void tryConsumeWithInvalidArrayType() {
        Union union = Union.ofTypes(String[].class, int[].class, float[].class);
        union.setValue(INT_ARRAY_VALUE);

        assertTrue(union.tryConsume(value -> assertArrayEquals(INT_ARRAY_VALUE, value), int[].class));
        assertFalse(union.tryConsume(value -> fail("Should not consume Double"), double[].class));
        assertFalse(union.tryConsume(value -> fail("Should not consume Long"), long[].class));
    }

    // Parameterized types tests

    @Test
    void createUnionWithParameterizedTypes() {
        Union union = Union.ofTypes(LIST_OF_STRING_TYPE, LIST_OF_INTEGER_TYPE, LIST_OF_DOUBLE_TYPE);
        assertNotNull(union);
        assertEquals(3, union.getTypes().size());
    }

    @Test
    void setAndGetValueWithParameterizedTypes() {
        Union union = Union.ofTypes(LIST_OF_STRING_TYPE, LIST_OF_INTEGER_TYPE, LIST_OF_DOUBLE_TYPE);

        union.setValue(LIST_OF_STRING_VALUE);
        assertEquals(LIST_OF_STRING_TYPE, union.getType());
        assertEquals(LIST_OF_STRING_VALUE, union.getValue(LIST_OF_STRING_TYPE));

        union.setValue(LIST_OF_INTEGER_VALUE);
        assertEquals(LIST_OF_INTEGER_TYPE, union.getType());
        assertEquals(LIST_OF_INTEGER_VALUE, union.getValue(LIST_OF_INTEGER_TYPE));

        union.setValue(LIST_OF_DOUBLE_VALUE);
        assertEquals(LIST_OF_DOUBLE_TYPE, union.getType());
        assertEquals(LIST_OF_DOUBLE_VALUE, union.getValue(LIST_OF_DOUBLE_TYPE));
    }

    @Test
    void setValueWithInvalidParameterizedType() {
        Union union = Union.ofTypes(LIST_OF_STRING_TYPE, LIST_OF_INTEGER_TYPE, LIST_OF_DOUBLE_TYPE);

        assertThrows(IllegalArgumentException.class, () -> union.setValue(LIST_OF_LONG_VALUE));
        assertThrows(IllegalArgumentException.class, () -> union.setValue(LIST_OF_FLOAT_VALUE));
        assertThrows(IllegalArgumentException.class, () -> union.setValue(SET_OF_STRING_VALUE));
    }

    @Test
    void tryConsumeWithValidParameterizedType() {
        Union union = Union.ofTypes(LIST_OF_STRING_TYPE, LIST_OF_INTEGER_TYPE, LIST_OF_DOUBLE_TYPE);

        union.setValue(LIST_OF_STRING_VALUE);
        assertTrue(union.tryConsume(value -> assertEquals(LIST_OF_STRING_VALUE, value), LIST_OF_STRING_TYPE));

        union.setValue(LIST_OF_INTEGER_VALUE);
        assertTrue(union.tryConsume(value -> assertEquals(LIST_OF_INTEGER_VALUE, value), LIST_OF_INTEGER_TYPE));

        union.setValue(LIST_OF_DOUBLE_VALUE);
        assertTrue(union.tryConsume(value -> assertEquals(LIST_OF_DOUBLE_VALUE, value), LIST_OF_DOUBLE_TYPE));
    }

    @Test
    void tryConsumeWithInvalidParameterizedType() {
        Union union = Union.ofTypes(LIST_OF_STRING_TYPE, LIST_OF_INTEGER_TYPE, LIST_OF_DOUBLE_TYPE, LIST_OF_FLOAT_TYPE);

        union.setValue(LIST_OF_INTEGER_VALUE);
        assertTrue(union.tryConsume(value -> assertEquals(LIST_OF_INTEGER_VALUE, value), LIST_OF_INTEGER_TYPE));
        assertFalse(union.tryConsume(value -> fail("Should not consume List<String>"), LIST_OF_STRING_TYPE));
        assertFalse(union.tryConsume(value -> fail("Should not consume List<Long>"), LIST_OF_LONG_TYPE));
        assertFalse(union.tryConsume(value -> fail("Should not consume List<Float>"), LIST_OF_FLOAT_TYPE));
        assertFalse(union.tryConsume(value -> fail("Should not consume List<Double>"), LIST_OF_DOUBLE_TYPE));
        assertFalse(union.tryConsume(value -> fail("Should not consume Set<String>"), SET_OF_STRING_TYPE));
    }

    // Additional tests
    @Test
    void setAndGetValueWithNull() {
        Union union = Union.ofTypes(String.class, Integer.class, Double.class);

        assertThrows(NullPointerException.class, () -> union.setValue(null));
        assertNull(union.getValue());
    }

    @Test
    void setAndGetValueWithEmptyCollection() {
        Union union = Union.ofTypes(LIST_OF_STRING_TYPE);
        List<String> emptyList = List.of();
        union.setValue(emptyList);
        assertEquals(LIST_OF_STRING_TYPE, union.getType());
        assertEquals(emptyList, union.getValue(LIST_OF_STRING_TYPE));
    }

    @Test
    void setValueWithMixedTypeCollection() {
        Union union = Union.ofTypes(LIST_OF_STRING_TYPE);

        List<Object> mixedList = List.of("Hello", 1);
        assertThrows(IllegalArgumentException.class, () -> union.setValue(mixedList));
    }

    @Test
    void setAndGetValueWithNestedParameterizedType() {
        ParameterizedTypeImpl listOfListOfString
            = new ParameterizedTypeImpl(List.class, new ParameterizedTypeImpl(List.class, String.class));
        Union union = Union.ofTypes(listOfListOfString);

        List<List<String>> nestedList = List.of(LIST_OF_STRING_VALUE);
        union.setValue(nestedList);
        assertEquals(listOfListOfString, union.getType());
        assertEquals(nestedList, union.getValue(listOfListOfString));
    }

    @Test
    void setAndGetValueWithPrimitiveArray() {
        Union union = Union.ofTypes(int[].class, double[].class);

        union.setValue(INT_ARRAY_VALUE);
        assertEquals(int[].class, union.getType());
        assertArrayEquals(INT_ARRAY_VALUE, union.getValue(int[].class));

        union.setValue(DOUBLE_ARRAY_VALUE);
        assertEquals(double[].class, union.getType());
        assertArrayEquals(DOUBLE_ARRAY_VALUE, union.getValue(double[].class));
    }

    @Test
    void setAndGetValueWithDeeplyNestedParameterizedType() {
        ParameterizedTypeImpl listOfListOfListOfString
            = new ParameterizedTypeImpl(List.class, new ParameterizedTypeImpl(List.class, LIST_OF_STRING_TYPE));
        Union union = Union.ofTypes(listOfListOfListOfString);

        List<List<List<String>>> deeplyNestedList = List.of(List.of(LIST_OF_STRING_VALUE));
        union.setValue(deeplyNestedList);
        assertEquals(listOfListOfListOfString, union.getType());
        assertEquals(deeplyNestedList, union.getValue(listOfListOfListOfString));
    }

    @Test
    void setAndGetValueWithMixedParameterizedTypes() {
        Union union = Union.ofTypes(LIST_OF_STRING_TYPE, SET_OF_STRING_TYPE);

        union.setValue(LIST_OF_STRING_VALUE);
        assertEquals(LIST_OF_STRING_TYPE, union.getType());
        assertEquals(LIST_OF_STRING_VALUE, union.getValue(LIST_OF_STRING_TYPE));

        union.setValue(SET_OF_STRING_VALUE);
        assertEquals(SET_OF_STRING_TYPE, union.getType());
        assertEquals(SET_OF_STRING_VALUE, union.getValue(SET_OF_STRING_TYPE));
    }
}
