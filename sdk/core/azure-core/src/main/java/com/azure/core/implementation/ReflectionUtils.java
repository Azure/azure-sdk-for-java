// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.logging.ClientLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Utility methods that aid in performing reflective operations.
 */
public abstract class ReflectionUtils {
    private static final ClientLogger LOGGER = new ClientLogger(ReflectionUtils.class);
    private static final ReflectionUtilsApi INSTANCE;

    static {
        ReflectionUtilsApi instance;
        try {
            LOGGER.verbose("Attempting to use java.lang.invoke package to handle reflection.");
            instance = new ReflectionUtilsMethodHandle();
            LOGGER.verbose("Successfully used java.lang.invoke package to handle reflection.");
        } catch (LinkageError ignored) {
            LOGGER.verbose("Failed to use java.lang.invoke package to handle reflection. Falling back to "
                           + "java.lang.reflect package to handle reflection.");
            instance = new ReflectionUtilsClassic();
            LOGGER.verbose("Successfully used java.lang.reflect package to handle reflection.");
        }

        INSTANCE = instance;
    }

    public static Invoker getMethodInvoker(Class<?> targetClass, Method method) throws Exception {
        return getMethodInvoker(targetClass, method, true);
    }

    public static Invoker getMethodInvoker(Class<?> targetClass, Method method, boolean scopeToAzureCore)
        throws Exception {
        return INSTANCE.getMethodInvoker(targetClass, method, scopeToAzureCore);
    }

    public static Invoker getConstructorInvoker(Class<?> targetClass, Constructor<?> constructor)
        throws Exception {
        return getConstructorInvoker(targetClass, constructor, true);
    }

    public static Invoker getConstructorInvoker(Class<?> targetClass, Constructor<?> constructor,
        boolean scopeToAzureCore) throws Exception {
        return INSTANCE.getConstructorInvoker(targetClass, constructor, scopeToAzureCore);
    }

    public static boolean isModuleBased() {
        return INSTANCE.isModuleBased();
    }

    public static Invoker createNoOpInvoker() {
        return new NoOpInvoker();
    }

    private static final class NoOpInvoker implements Invoker {
        @Override
        public Object invokeStatic(Object... args) {
            return null;
        }

        @Override
        public Object invokeWithArguments(Object target, Object... args) {
            return null;
        }

        @Override
        public int getParameterCount() {
            return 0;
        }
    }

    ReflectionUtils() {
    }
}
