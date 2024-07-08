// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.v2.implementation.EnumWithPackagePrivateCtor;
import io.clientcore.core.implementation.ReflectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link ExpandableStringEnum}
 */
public class ExpandableStringEnumTests {
    /**
     * Tests that using {@code null} to get an {@link ExpandableStringEnum} will return {@code null}.
     */
    @Test
    public void nullNameReturnsNull() {
        assertNull(TestStringEnum.fromString(null));
    }

    /**
     * Tests that a private {@link ExpandableStringEnum} class will return {@code null} on Java 9+
     */
    @Test
    public void privateStringEnumDifferentPackageInCoreAlwaysReturnsNull() {
        if (ReflectionUtils.isModuleBased()) {
            // on Java 9+, when enum impl is in the core module, we use MethodHandles.lookup
            // which does not provide access com.azure.core.util.ExpandableStringEnumTests.PrivateStringEnum
            // from com.azure.core.implementation.ExpandableStringEnum
            //
            // but if it was in a different module, which is open to core, we would be able to access it
            assertNull(PrivateStringEnum.fromString("test"));
            assertNull(PrivateStringEnum.fromString("anotherTest"));
            assertNull(PrivateStringEnum.fromString("finalTest"));
        } else {
            // Java 8 does not work with modules, we always use private lookups there
            // and therefore can access com.azure.core.util.ExpandableStringEnumTests.PrivateStringEnum
            // from com.azure.core.implementation.ExpandableStringEnum
            assertEquals("java8", PrivateStringEnum.fromString("java8").toString());
        }
    }

    /**
     * Tests that a same package, package-private {@link ExpandableStringEnum} class will always return non-null value
     */
    @Test
    public void privateStringEnumSamePackageInCoreReturnsValue() {
        // EnumWithPackagePrivateCtor is in the same package as ExpandableStringEnum
        assertEquals("test", EnumWithPackagePrivateCtor.fromString("test").toString());
        assertEquals("anotherTest", EnumWithPackagePrivateCtor.fromString("anotherTest").toString());
        assertEquals("finalTest", EnumWithPackagePrivateCtor.fromString("finalTest").toString());
    }

    /**
     * Tests that adding to an {@link ExpandableStringEnum} will modify the values for the enum.
     */
    @Test
    public void stringEnumValues() {
        Set<ValuesTestStringEnum> values = new HashSet<>(ExpandableStringEnum.values(ValuesTestStringEnum.class));
        assertEquals(0, values.size());

        ValuesTestStringEnum value1 = ValuesTestStringEnum.fromString("value1");
        ValuesTestStringEnum value2 = ValuesTestStringEnum.fromString("value2");

        values = new HashSet<>(ExpandableStringEnum.values(ValuesTestStringEnum.class));
        assertEquals(2, values.size());
        assertTrue(values.contains(value1));
        assertTrue(values.contains(value2));
    }

    /**
     * Tests that the {@link ExpandableStringEnum} will generate the expected hashcode.
     */
    @Test
    public void validateHashCode() {
        assertEquals(Objects.hash(TestStringEnum.class, "test"), TestStringEnum.fromString("test").hashCode());
    }

    /**
     * Tests permutation of the {@code equals} and verifies they return the expected output.
     */
    @ParameterizedTest
    @MethodSource("validateEqualsSupplier")
    public void validateEquals(ExpandableStringEnum<?> lhs, ExpandableStringEnum<?> rhs, boolean expected) {
        assertEquals(expected, lhs.equals(rhs));
    }

    private static Stream<Arguments> validateEqualsSupplier() {
        TestStringEnum testStringEnum = TestStringEnum.fromString("test");
        TestStringEnum2 testStringEnum2 = TestStringEnum2.fromString("test");

        return Stream.of(Arguments.of(testStringEnum, null, false),
            Arguments.of(testStringEnum, testStringEnum2, false), Arguments.of(testStringEnum, testStringEnum, true),
            Arguments.of(testStringEnum2, testStringEnum2, true),
            Arguments.of(testStringEnum, TestStringEnum.fromString("test"), true),
            Arguments.of(testStringEnum, TestStringEnum.fromString("test2"), false));
    }

    public static final class TestStringEnum extends ExpandableStringEnum<TestStringEnum> {
        @Deprecated
        public TestStringEnum() {
        }

        static TestStringEnum fromString(String name) {
            return fromString(name, TestStringEnum.class);
        }
    }

    public static final class TestStringEnum2 extends ExpandableStringEnum<TestStringEnum2> {
        @Deprecated
        public TestStringEnum2() {
        }

        static TestStringEnum2 fromString(String name) {
            return fromString(name, TestStringEnum2.class);
        }
    }

    public static final class PrivateStringEnum extends ExpandableStringEnum<PrivateStringEnum> {
        @Deprecated
        PrivateStringEnum() {
        }

        static PrivateStringEnum fromString(String name) {
            return fromString(name, PrivateStringEnum.class);
        }
    }

    public static final class ValuesTestStringEnum extends ExpandableStringEnum<ValuesTestStringEnum> {
        @Deprecated
        public ValuesTestStringEnum() {
        }

        static ValuesTestStringEnum fromString(String name) {
            return fromString(name, ValuesTestStringEnum.class);
        }
    }
}
