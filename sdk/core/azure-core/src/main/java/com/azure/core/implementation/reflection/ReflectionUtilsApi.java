// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.reflection;

import com.azure.core.implementation.Invoker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Interface that defines implementation-agnostic methods for creating {@link Invoker Invokers} that will invoke
 * {@link Method Methods}, {@link Constructor Constructors}, and {@link Field Fields}.
 */
public interface ReflectionUtilsApi {
    /**
     * Creates an {@link Invoker} instance that will invoke a {@link Method}.
     *
     * @param targetClass The class that contains the method.
     * @param method The method to invoke.
     * @param scopeToAzureCore If Java 9+ modules is being used this will scope MethodHandle-based reflection to using
     * {@code azure-core} as the scoped module, otherwise this is ignored.
     * @return An {@link Invoker} instance that will invoke the method.
     * @throws Exception If the {@link Invoker} cannot be created.
     */
    Invoker getMethodInvoker(Class<?> targetClass, Method method, boolean scopeToAzureCore) throws Exception;

    /**
     * Creates an {@link Invoker} instance that will invoke a {@link Constructor}.
     *
     * @param targetClass The class that contains the constructor.
     * @param constructor The constructor to invoke.
     * @param scopeToAzureCore If Java 9+ modules is being used this will scope MethodHandle-based reflection to using
     * {@code azure-core} as the scoped module, otherwise this is ignored.
     * @return An {@link Invoker} instance that will invoke the constructor.
     * @throws Exception If the {@link Invoker} cannot be created.
     */
    Invoker getConstructorInvoker(Class<?> targetClass, Constructor<?> constructor, boolean scopeToAzureCore)
        throws Exception;

    /**
     * Creates an {@link Invoker} instance that will invoke a {@link Field} setter.
     *
     * @param targetClass The class that contains the setter.
     * @param setter The setter to invoke.
     * @param scopeToAzureCore If Java 9+ modules is being used this will scope MethodHandle-based reflection to using
     * {@code azure-core} as the scoped module, otherwise this is ignored.
     * @return An {@link Invoker} instance that will invoke the setter.
     * @throws Exception If the {@link Invoker} cannot be created.
     */
    Invoker getSetterFieldInvoker(Class<?> targetClass, Field setter, boolean scopeToAzureCore) throws Exception;

    /**
     * Indicates whether the {@link ReflectionUtilsApi} instance uses Java 9+ modules.
     *
     * @return Whether the {@link ReflectionUtilsApi} instance uses Java 9+ modules.
     */
    boolean isModuleBased();
}
