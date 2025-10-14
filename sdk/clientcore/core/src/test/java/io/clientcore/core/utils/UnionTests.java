// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import io.clientcore.core.implementation.GenericParameterizedType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
    private static final double DOUBLE_VALUE = 3.11d;
    private static final float FLOAT_VALUE = 3.11f;

    private static final String[] STRING_ARRAY_VALUE = { "Hello", "world", "!" };
    private static final int[] INT_ARRAY_VALUE = { 1, 2, 3 };
    private static final long[] LONG_ARRAY_VALUE = { 1L, 2L, 3L };
    private static final float[] FLOAT_ARRAY_VALUE = { 1.1f, 2.2f, 3.3f };
    private static final double[] DOUBLE_ARRAY_VALUE = { 1.1d, 2.2d, 3.3d };

    private static final GenericParameterizedType LIST_OF_STRING_TYPE
        = new GenericParameterizedType(List.class, String.class);
    private static final GenericParameterizedType LIST_OF_INTEGER_TYPE
        = new GenericParameterizedType(List.class, Integer.class);
    private static final GenericParameterizedType LIST_OF_LONG_TYPE
        = new GenericParameterizedType(List.class, Long.class);
    private static final GenericParameterizedType LIST_OF_FLOAT_TYPE
        = new GenericParameterizedType(List.class, Float.class);
    private static final GenericParameterizedType LIST_OF_DOUBLE_TYPE
        = new GenericParameterizedType(List.class, Double.class);
    private static final GenericParameterizedType SET_OF_STRING_TYPE
        = new GenericParameterizedType(Set.class, String.class);

    private static final List<String> LIST_OF_STRING_VALUE = Arrays.asList("Hello", "world", "!");
    private static final List<Integer> LIST_OF_INTEGER_VALUE = Arrays.asList(1, 2, 3);
    private static final List<Long> LIST_OF_LONG_VALUE = Arrays.asList(1L, 2L, 3L);
    private static final List<Float> LIST_OF_FLOAT_VALUE = Arrays.asList(1.1f, 2.2f, 3.3f);
    private static final List<Double> LIST_OF_DOUBLE_VALUE = Arrays.asList(1.1d, 2.2d, 3.3d);

    private static final Set<String> SET_OF_STRING_VALUE
        = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("Hello", "world", "!")));

    @Test
    void createUnionWithMultipleTypes() {
        Union union = Union.ofTypes(String.class, Integer.class, Double.class);
        assertNotNull(union);
        assertEquals(3, union.getSupportedTypes().size());
    }

    @Test
    void setAndGetValue() {
        Union union = Union.ofTypes(String.class, Integer.class, Double.class);

        union.setValue(STRING_VALUE);
        assertEquals(String.class, union.getCurrentType());
        assertEquals(STRING_VALUE, union.getValue());

        union.setValue(INT_VALUE);
        assertEquals(Integer.class, union.getCurrentType());
        assertEquals(INT_VALUE, union.getValue(Integer.class));

        union.setValue(DOUBLE_VALUE);
        assertEquals(Double.class, union.getCurrentType());
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
        assertEquals(String.class, union.getCurrentType());
        assertEquals(STRING_VALUE, union.getValue());

        union.setValue(42);
        assertEquals(int.class, union.getCurrentType());
        assertEquals(42, union.getValue(int.class));

        union.setValue(DOUBLE_VALUE);
        assertEquals(double.class, union.getCurrentType());
        assertEquals(DOUBLE_VALUE, union.getValue(double.class));
    }

    @Test
    void setAndGetValueAutoboxing() {
        Union union = Union.ofTypes(String.class, double.class, int.class);
        union.setValue(STRING_VALUE);
        assertEquals(String.class, union.getCurrentType());
        assertEquals(STRING_VALUE, union.getValue(String.class));

        union.setValue(INT_VALUE);
        assertEquals(int.class, union.getCurrentType());
        assertEquals(INT_VALUE, union.getValue(int.class));

        union.setValue(DOUBLE_VALUE);
        assertEquals(double.class, union.getCurrentType());
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
        assertEquals(String.class, union.getCurrentType());
        assertTrue(union.tryConsume(value -> assertEquals(STRING_VALUE, value), String.class));

        union.setValue(INT_VALUE);
        assertEquals(int.class, union.getCurrentType());
        assertTrue(union.tryConsume(value -> assertEquals(INT_VALUE, value), int.class));

        union.setValue(DOUBLE_VALUE);
        assertEquals(double.class, union.getCurrentType());
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
        assertEquals(3, union.getSupportedTypes().size());
    }

    @Test
    void setAndGetValueWithArrayTypes() {
        Union union = Union.ofTypes(String[].class, int[].class, float[].class);

        union.setValue(STRING_ARRAY_VALUE);
        assertEquals(String[].class, union.getCurrentType());
        assertArrayEquals(STRING_ARRAY_VALUE, union.getValue(String[].class));

        union.setValue(INT_ARRAY_VALUE);
        assertEquals(int[].class, union.getCurrentType());
        assertArrayEquals(INT_ARRAY_VALUE, union.getValue(int[].class));

        union.setValue(FLOAT_ARRAY_VALUE);
        assertEquals(float[].class, union.getCurrentType());
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
        assertEquals(3, union.getSupportedTypes().size());
    }

    @Test
    void setAndGetValueWithParameterizedTypes() {
        Union union = Union.ofTypes(LIST_OF_STRING_TYPE, LIST_OF_INTEGER_TYPE, LIST_OF_DOUBLE_TYPE);

        union.setValue(LIST_OF_STRING_VALUE);
        assertEquals(LIST_OF_STRING_TYPE, union.getCurrentType());
        assertEquals(LIST_OF_STRING_VALUE, union.getValue(LIST_OF_STRING_TYPE));

        union.setValue(LIST_OF_INTEGER_VALUE);
        assertEquals(LIST_OF_INTEGER_TYPE, union.getCurrentType());
        assertEquals(LIST_OF_INTEGER_VALUE, union.getValue(LIST_OF_INTEGER_TYPE));

        union.setValue(LIST_OF_DOUBLE_VALUE);
        assertEquals(LIST_OF_DOUBLE_TYPE, union.getCurrentType());
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
    @ParameterizedTest
    @MethodSource("invalidUnionOfTypesSupplier")
    public void invalidUnionOfTypes(Supplier<Union> unionSupplier) {
        assertThrows(IllegalArgumentException.class, unionSupplier::get);
    }

    private static Stream<Supplier<Union>> invalidUnionOfTypesSupplier() {
        return Stream.of(
            // null array
            () -> Union.ofTypes((Type[]) null),

            // empty array
            () -> Union.ofTypes(),

            // null type in array
            () -> Union.ofTypes(String.class, null, int.class),

            // unknown type
            () -> Union.ofTypes(new Type() {
            }));
    }

    @Test
    void setAndGetValueWithNull() {
        Union union = Union.ofTypes(String.class, Integer.class, Double.class);
        union.setValue(null);
        assertNull(union.getValue());
    }

    @Test
    void setAndGetValueWithEmptyCollection() {
        Union union = Union.ofTypes(LIST_OF_STRING_TYPE);
        List<String> emptyList = Arrays.asList();
        union.setValue(emptyList);
        assertEquals(LIST_OF_STRING_TYPE, union.getCurrentType());
        assertEquals(emptyList, union.getValue(LIST_OF_STRING_TYPE));
    }

    @Test
    void setValueWithMixedTypeCollection() {
        Union union = Union.ofTypes(LIST_OF_STRING_TYPE);

        List<Object> mixedList = Arrays.asList("Hello", 1);
        assertThrows(IllegalArgumentException.class, () -> union.setValue(mixedList));
    }

    @Test
    void setAndGetValueWithNestedParameterizedType() {
        GenericParameterizedType listOfListOfString
            = new GenericParameterizedType(List.class, new GenericParameterizedType(List.class, String.class));
        Union union = Union.ofTypes(listOfListOfString);

        List<List<String>> nestedList = Arrays.asList(LIST_OF_STRING_VALUE);
        union.setValue(nestedList);
        assertEquals(listOfListOfString, union.getCurrentType());
        assertEquals(nestedList, union.getValue(listOfListOfString));
    }

    @Test
    void setAndGetValueWithPrimitiveArray() {
        Union union = Union.ofTypes(int[].class, double[].class);

        union.setValue(INT_ARRAY_VALUE);
        assertEquals(int[].class, union.getCurrentType());
        assertArrayEquals(INT_ARRAY_VALUE, union.getValue(int[].class));

        union.setValue(DOUBLE_ARRAY_VALUE);
        assertEquals(double[].class, union.getCurrentType());
        assertArrayEquals(DOUBLE_ARRAY_VALUE, union.getValue(double[].class));
    }

    @Test
    void setAndGetValueWithDeeplyNestedParameterizedType() {
        GenericParameterizedType listOfListOfListOfString
            = new GenericParameterizedType(List.class, new GenericParameterizedType(List.class, LIST_OF_STRING_TYPE));
        Union union = Union.ofTypes(listOfListOfListOfString);

        List<List<List<String>>> deeplyNestedList = Arrays.asList(Arrays.asList(LIST_OF_STRING_VALUE));
        union.setValue(deeplyNestedList);
        assertEquals(listOfListOfListOfString, union.getCurrentType());
        assertEquals(deeplyNestedList, union.getValue(listOfListOfListOfString));
    }

    @Test
    void setAndGetValueWithMixedParameterizedTypesAndClass() {
        Union union = Union.ofTypes(LIST_OF_STRING_TYPE, SET_OF_STRING_TYPE, String.class);

        union.setValue(LIST_OF_STRING_VALUE);
        assertEquals(LIST_OF_STRING_TYPE, union.getCurrentType());
        assertEquals(LIST_OF_STRING_VALUE, union.getValue(LIST_OF_STRING_TYPE));

        union.setValue(SET_OF_STRING_VALUE);
        assertEquals(SET_OF_STRING_TYPE, union.getCurrentType());
        assertEquals(SET_OF_STRING_VALUE, union.getValue(SET_OF_STRING_TYPE));

        union.setValue(STRING_VALUE);
        assertEquals(String.class, union.getCurrentType());
        assertEquals(STRING_VALUE, union.getValue(String.class));
    }

    @Test
    void unionWithMap() {
        Union union = Union.ofTypes(String.class, Integer.class, Map.class);
        String key = "key";
        String value = "value";
        Map<String, String> map = Collections.singletonMap(key, value);
        union.setValue(map);

        assertEquals(Map.class, union.getCurrentType());
        assertEquals(map, union.getValue(Map.class));

        union.tryConsume(map1 -> assertEquals(value, map1.get(key)), Map.class);

        assertFalse(union.tryConsume(ignore -> fail("Should not consume List<Float>"), LIST_OF_FLOAT_TYPE));
    }

    @Test
    void unionWithBooleanShortBytes() {
        Union union = Union.ofTypes(Boolean.class, Short.class, Byte.class);
        union.setValue(true);
        assertEquals(Boolean.class, union.getCurrentType());
        assertEquals(true, union.getValue(boolean.class));

        union.setValue((short) 1);
        assertEquals(Short.class, union.getCurrentType());
        assertEquals((short) 1, union.getValue(Short.class));
        union.setValue((byte) 1);
        assertEquals(Byte.class, union.getCurrentType());
        assertEquals((byte) 1, union.getValue(Byte.class));

        assertFalse(union.tryConsume(ignore -> fail("Should not consume List<Float>"), LIST_OF_FLOAT_TYPE));
    }

    @Test
    void unionWithNestedUnion() {
        Union union = Union.ofTypes(String.class, Union.class);
        Union nestedUnion = Union.ofTypes(String.class, Integer.class, Double.class);
        nestedUnion = nestedUnion.setValue(STRING_VALUE);
        union.setValue(nestedUnion);

        assertEquals(Union.class, union.getCurrentType());
        Union unionValue = union.getValue(Union.class);
        assertNotNull(unionValue);
        assertEquals(nestedUnion, unionValue);
        assertEquals(STRING_VALUE, unionValue.getValue());
    }

    @Test
    public void getValueWithSuperType() {
        Union union = Union.ofTypes(Long.class, Integer.class);
        union.setValue(1L);

        Number number = union.getValue(Number.class);
        assertEquals(1L, number.longValue());
    }

    @Test
    public void getValueWithSubType() {
        Union union = Union.ofTypes(Number.class);
        union.setValue(1L);

        Long number = union.getValue(Long.class);
        assertEquals(1L, number.longValue());
    }
}
