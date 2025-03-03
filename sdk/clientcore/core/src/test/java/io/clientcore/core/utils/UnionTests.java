// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import io.clientcore.core.implementation.GenericParameterizedType;
import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Test
    void createUnionWithNullTypes() {
        assertThrows(IllegalArgumentException.class, () -> Union.ofTypes((Type) null));
        assertThrows(IllegalArgumentException.class, () -> Union.ofTypes(String.class, null, int.class));
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


    @MethodSource("getDeserializationTestData")
    @ParameterizedTest
    void primitiveDeserializationTest(String inputJson) throws Exception {
        JsonReader jsonReader = JsonReader.fromString(inputJson);
        Union union = Union.fromJson(jsonReader, int.class, boolean.class, String.class, double.class, float.class, long.class,
            TypeUtil.createParameterizedType(List.class, String.class), TypeUtil.createParameterizedType(List.class, Boolean.class),
            TypeUtil.createParameterizedType(List.class, Integer.class), TypeUtil.createParameterizedType(List.class, Long.class),
            TypeUtil.createParameterizedType(List.class, Float.class), TypeUtil.createParameterizedType(List.class, Double.class),
            TypeUtil.createParameterizedType(List.class, TypeUtil.createParameterizedType(List.class, Integer.class)));
        System.out.println("Input: " + inputJson + "; output: " + (union.getValue() == null ? "null" : union.getValue().toString()));
    }


    @MethodSource("getModelDeserializationTestData")
    @ParameterizedTest
    void modelDeserializationTest(String inputJson) throws Exception {
        JsonReader jsonReader = JsonReader.fromString(inputJson);
        FooModel fooModel = FooModel.fromJson(jsonReader);
        System.out.println("Input: " + inputJson + "; output: " + fooModel.getName() + " " + fooModel.getBarOrBaz().getValue());
    }


    @MethodSource("getModelSerializationTestData")
    @ParameterizedTest
    void modelSerializationTest(FooModel model) throws Exception {
        ByteArrayOutputStream byteArraOS = new ByteArrayOutputStream();
        JsonWriter jsonWriter = JsonWriter.toStream(byteArraOS);
        JsonWriter updatedJsonWriter = model.toJson(jsonWriter);
        updatedJsonWriter.close();
        String json = byteArraOS.toString();
        System.out.println("Json output: " + json);
    }

    public static Stream<Arguments> getModelSerializationTestData() {
        Union bar = Union.ofTypes(BarModel.class, BazModel.class).setValue(new BarModel().setBarId("barId").setBarName("barName"));
        Union baz = Union.ofTypes(BarModel.class, BazModel.class).setValue(new BazModel().setBazId("bazId").setBazName("bazName"));
//        Union fizz = Union.ofTypes(BarModel.class, BazModel.class).setValue(new FizzModel().setFizzId("fizzId").setFizzName("fizzName"));

        Union str = Union.ofTypes(String.class, Integer.class)
            .setValue("hello world");
        Union intValue = Union.ofTypes(String.class, Integer.class)
            .setValue(42);

        Union intList = Union.ofTypes(TypeUtil.createParameterizedType(List.class, String.class),
            TypeUtil.createParameterizedType(List.class, Integer.class))
            .setValue(Arrays.asList(1, 2, 3));

        Union strList = Union.ofTypes(TypeUtil.createParameterizedType(List.class, String.class),
            TypeUtil.createParameterizedType(List.class, Integer.class))
            .setValue(Arrays.asList("hello", "world"));

        Union bytes = Union.ofTypes(TypeUtil.createParameterizedType(List.class, String.class, byte[].class),
            TypeUtil.createParameterizedType(List.class, Integer.class), byte[].class)
            .setValue("hello".getBytes(StandardCharsets.UTF_8));

        FooModel foo1 = new FooModel()
            .setName("foo1")
            .setBarOrBaz(bar)
            .setStringOrInt(str)
            .setCollectionTypes(intList);

        FooModel foo2 = new FooModel()
            .setName("foo2")
            .setBarOrBaz(baz)
            .setStringOrInt(intValue)
            .setCollectionTypes(strList);

        FooModel foo3 = new FooModel()
            .setName("foo3")
            .setBarOrBaz(baz)
            .setStringOrInt(intValue)
            .setCollectionTypes(bytes);


        return Stream.of(
            Arguments.of(foo1),
            Arguments.of(foo2),
            Arguments.of(foo3)
//            Arguments.of(new FooModel().setName("fooFizz").setBarOrBaz(fizz))
        );
    }

    public static Stream<Arguments> getDeserializationTestData() {
        return Stream.of(
            Arguments.of("5"),          // Integer
            Arguments.of("2.0"),       // Double
            Arguments.of("true"),       // Boolean true
            Arguments.of("false"),      // Boolean false
            Arguments.of("\"string\""), // String
            Arguments.of("null"),       // Null
            Arguments.of("[]"),         // Empty array
            Arguments.of("[1, 2, 3]"),  // Array of integers
            Arguments.of("[1.0, 2.0, 3.0]"), // Array of doubles
            Arguments.of("[true, false]"), // Array of booleans
            Arguments.of("[\"string1\", \"string2\"]"), // Array of strings
            Arguments.of("[[1, 2], [3, 4]]"), // Array of arrays
            Arguments.of("[[1, 2], [3.0, 4.0]]"), // Array of mixed arrays
            Arguments.of("[[1, 2], [true, false]]"), // Array of mixed arrays
            Arguments.of("[[1, 2], [\"string1\", \"string2\"]]"), // Array of mixed arrays
            Arguments.of("[[1, 2], [null, null]]") // Array of mixed arrays
        );
    }

    public static Stream<Arguments> getModelDeserializationTestData() {
        return Stream.of(
            Arguments.of("{\"name\": \"fooId1\", \"barOrBaz\": {\"barId\": \"barId\", \"barName\": \"barName\"}}"),
            Arguments.of("{\"name\": \"fooId2\", \"barOrBaz\": {\"bazId\": \"bazId\", \"bazName\": \"bazName\"}}")
        );
    }
}
