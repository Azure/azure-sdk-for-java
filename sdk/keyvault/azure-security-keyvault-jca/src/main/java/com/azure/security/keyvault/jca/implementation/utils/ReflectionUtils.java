// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

/**
 * Utility methods that aid in performing reflective operations.
 */
public abstract class ReflectionUtils {
    private static final Logger LOGGER = Logger.getLogger(ReflectionUtils.class.getName());
    private static final ReflectionUtilsApi INSTANCE;

    static {
        ReflectionUtilsApi instance;

        try {
            LOGGER.log(FINE, "Attempting to use java.lang.invoke package to handle reflection.");

            instance = new ReflectionUtilsMethodHandle();

            LOGGER.log(FINE, "Successfully used java.lang.invoke package to handle reflection.");
        } catch (LinkageError ignored) {
            LOGGER.log(FINE, "Failed to use java.lang.invoke package to handle reflection. Falling back to "
                + "java.lang.reflect package to handle reflection.");

            instance = new ReflectionUtilsClassic();

            LOGGER.log(FINE, "Successfully used java.lang.reflect package to handle reflection.");
        }

        INSTANCE = instance;
    }

    /**
     * Creates an {@link ReflectiveInvoker} instance that will invoke a {@link Method}.
     *
     * <p>Calls {@link #getMethodInvoker(Class, Method, boolean)} with {@code scopeToAzureCore} set to true.</p>
     *
     * @param targetClass The class that contains the method.
     * @param method The method to invoke.
     *
     * @return An {@link ReflectiveInvoker} instance that will invoke the method.
     *
     * @throws NullPointerException If {@code method} is null.
     * @throws Exception If the {@link ReflectiveInvoker} cannot be created.
     */
    public static ReflectiveInvoker getMethodInvoker(Class<?> targetClass, Method method) throws Exception {
        return getMethodInvoker(targetClass, method, true);
    }

    /**
     * Creates an {@link ReflectiveInvoker} instance that will invoke a {@link Method}.
     *
     * <p>{@code targetClass} may be null but when using an environment that supports MethodHandles for handling reflection
     * this may result in exceptions being thrown due to the inability to scope the MethodHandle to a module. To attempt
     * to alleviate this issue, if {@code targetClass} is null {@link Method#getDeclaringClass()} will be used to infer
     * the class.</p>
     *
     * <p>{@code scopeToAzure} is only when used when MethodHandles are being used and Java 9+ modules are being used. This
     * will determine whether to use a MethodHandles.Lookup scoped to {@code azure-core} or to use a public
     * MethodHandles.Lookup. Scoping a MethodHandles.Lookup to {@code azure-core} requires to module containing the
     * class to open or export to {@code azure-core} which generally only holds true for other Azure SDKs, for example
     * there are cases where a reflective invocation is needed to Jackson which won't open or export to
     * {@code azure-core} and the only APIs invoked reflectively are public APIs so the public MethodHandles.Lookup will
     * be used.</p>
     *
     * @param targetClass The class that contains the method.
     * @param method The method to invoke.
     * @param scopeToAzureCore If Java 9+ modules is being used this will scope MethodHandle-based reflection to using
     * {@code azure-core} as the scoped module, otherwise this is ignored.
     *
     *  @return An {@link ReflectiveInvoker} instance that will invoke the method.
     *
     * @throws NullPointerException If {@code method} is null.
     * @throws Exception If the {@link ReflectiveInvoker} cannot be created.
     */
    public static ReflectiveInvoker getMethodInvoker(Class<?> targetClass, Method method, boolean scopeToAzureCore)
        throws Exception {

        if (method == null) {
            LOGGER.log(SEVERE, "'method' cannot be null.");

            throw new NullPointerException("'method' cannot be null.");
        }

        targetClass = (targetClass == null) ? method.getDeclaringClass() : targetClass;

        return INSTANCE.getMethodInvoker(targetClass, method, scopeToAzureCore);
    }

    ReflectionUtils() {
    }
}
