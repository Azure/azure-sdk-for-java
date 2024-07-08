// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import io.clientcore.core.http.models.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ReflectionUtils}.
 */
public class ReflectionUtilsTests {
    @EnabledForJreRange(max = JRE.JAVA_8)
    @Test
    public void java8UsesClassicReflection() {
        assertFalse(ReflectionUtils.isModuleBased());
    }

    @EnabledForJreRange(min = JRE.JAVA_9)
    @Test
    public void java9PlusUsesModuleBasedPrivateLookupIn() {
        assertTrue(ReflectionUtils.isModuleBased());
    }

    @EnabledIf("invokePackageUnavailable")
    @Test
    public void classicReflectionUsedWhenInvokePackageUnavailable() {
        assertFalse(ReflectionUtils.isModuleBased());
    }

    private static boolean invokePackageUnavailable() {
        try {
            Class.forName("java.lang.invoke.MethodHandles");
            return false;
        } catch (ClassNotFoundException ex) {
            return true;
        }
    }

    @ParameterizedTest
    @MethodSource("validateNullPointerExceptionThrownSupplier")
    public void validateNullPointerExceptionThrown(Executable executable) {
        assertThrows(NullPointerException.class, executable);
    }

    @SuppressWarnings("DataFlowIssue")
    private static Stream<Executable> validateNullPointerExceptionThrownSupplier() {
        return Stream.of(() -> ReflectionUtils.getConstructorInvoker(null, null),
            () -> ReflectionUtils.getConstructorInvoker(null, null, false),
            () -> ReflectionUtils.getMethodInvoker(null, null),
            () -> ReflectionUtils.getMethodInvoker(null, null, false));
    }

    @ParameterizedTest
    @MethodSource("nullTargetClassUsesAnImplicitClassSupplier")
    public void nullTargetClassUsesAnImplicitClass(Executable executable) {
        assertDoesNotThrow(executable);
    }

    private static Stream<Executable> nullTargetClassUsesAnImplicitClassSupplier() {
        try {
            Constructor<?> httpHeadersConstructor = HttpHeaders.class.getDeclaredConstructor();
            Method httpHeadersSet = HttpHeaders.class.getDeclaredMethod("set", String.class, String.class);
            return Stream.of(() -> ReflectionUtils.getConstructorInvoker(null, httpHeadersConstructor),
                () -> ReflectionUtils.getConstructorInvoker(null, httpHeadersConstructor, true),
                () -> ReflectionUtils.getMethodInvoker(null, httpHeadersSet),
                () -> ReflectionUtils.getMethodInvoker(null, httpHeadersSet, true));
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
