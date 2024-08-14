// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Interface that defines implementation-agnostic methods for creating {@link ReflectiveInvoker Invokers} that will
 * invoke {@link Method Methods}, {@link Constructor Constructors}, and {@link Field Fields}.
 */
interface ReflectionUtilsApi {
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
     * @return An {@link ReflectiveInvoker} instance that will invoke the method.
     *
     * @throws NullPointerException If {@code method} is null.
     * @throws Exception If the {@link ReflectiveInvoker} cannot be created.
     */
    ReflectiveInvoker getMethodInvoker(Class<?> targetClass, Method method, boolean scopeToAzureCore) throws Exception;
}
