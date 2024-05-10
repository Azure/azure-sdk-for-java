// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation;

import io.clientcore.core.util.ClientLogger;

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
            LOGGER.atVerbose().log("Attempting to use java.lang.invoke package to handle reflection.");
            instance = new ReflectionUtilsMethodHandle();
            LOGGER.atVerbose().log("Successfully used java.lang.invoke package to handle reflection.");
        } catch (LinkageError ignored) {
            LOGGER.atVerbose().log("Failed to use java.lang.invoke package to handle reflection. Falling back to "
                + "java.lang.reflect package to handle reflection.");
            instance = new ReflectionUtilsClassic();
            LOGGER.atVerbose().log("Successfully used java.lang.reflect package to handle reflection.");
        }

        INSTANCE = instance;
    }


    /**
     * Creates an {@link ReflectiveInvoker} instance that will invoke a {@link Method}.
     * <p>
     * Calls {@link #getMethodInvoker(Class, Method, boolean)} with {@code scopeToGenericCore} set to true.
     *
     * @param targetClass The class that contains the method.
     * @param method The method to invoke.
     * @return An {@link ReflectiveInvoker} instance that will invoke the method.
     * @throws NullPointerException If {@code method} is null.
     * @throws Exception If the {@link ReflectiveInvoker} cannot be created.
     */
    public static ReflectiveInvoker getMethodInvoker(Class<?> targetClass, Method method) throws Exception {
        return getMethodInvoker(targetClass, method, true);
    }

    /**
     * Creates an {@link ReflectiveInvoker} instance that will invoke a {@link Method}.
     * <p>
     * {@code targetClass} may be null but when using an environment that supports MethodHandles for handling reflection
     * this may result in exceptions being thrown due to the inability to scope the MethodHandle to a module. To attempt
     * to alleviate this issue, if {@code targetClass} is null {@link Method#getDeclaringClass()} will be used to infer
     * the class.
     * <p>
     * {@code scopeToAzure} is only when used when MethodHandles are being used and Java 9+ modules are being used. This
     * will determine whether to use a MethodHandles.Lookup scoped to {@code core} or to use a public
     * MethodHandles.Lookup. Scoping a MethodHandles.Lookup to {@code core} requires to module containing the
     * class to open or export to {@code core} which generally only holds true for other Azure SDKs, for example
     * there are cases where a reflective invocation is needed to Jackson which won't open or export to
     * {@code core} and the only APIs invoked reflectively are public APIs so the public MethodHandles.Lookup will
     * be used.
     *
     * @param targetClass The class that contains the method.
     * @param method The method to invoke.
     * @param scopeToGenericCore If Java 9+ modules is being used this will scope MethodHandle-based reflection to using
     * {@code core} as the scoped module, otherwise this is ignored.
     * @return An {@link ReflectiveInvoker} instance that will invoke the method.
     * @throws NullPointerException If {@code method} is null.
     * @throws Exception If the {@link ReflectiveInvoker} cannot be created.
     */
    public static ReflectiveInvoker getMethodInvoker(Class<?> targetClass, Method method, boolean scopeToGenericCore)
        throws Exception {
        if (method == null) {
            throw LOGGER.logThrowableAsError(new NullPointerException("'method' cannot be null."));
        }

        targetClass = (targetClass == null) ? method.getDeclaringClass() : targetClass;
        return INSTANCE.getMethodInvoker(targetClass, method, scopeToGenericCore);
    }

    /**
     * Creates an {@link ReflectiveInvoker} instance that will invoke a {@link Constructor}.
     * <p>
     * Calls {@link #getConstructorInvoker(Class, Constructor, boolean)} with {@code scopeToAzureCore} set to true.
     *
     * @param targetClass The class that contains the constructor.
     * @param constructor The constructor to invoke.
     * @return An {@link ReflectiveInvoker} instance that will invoke the constructor.
     * @throws NullPointerException If {@code constructor} is null.
     * @throws Exception If the {@link ReflectiveInvoker} cannot be created.
     */
    public static ReflectiveInvoker getConstructorInvoker(Class<?> targetClass, Constructor<?> constructor)
        throws Exception {
        return getConstructorInvoker(targetClass, constructor, true);
    }

    /**
     * Creates an {@link ReflectiveInvoker} instance that will invoke a {@link Constructor}.
     * <p>
     * {@code targetClass} may be null but when using an environment that supports MethodHandles for handling reflection
     * this may result in exceptions being thrown due to the inability to scope the MethodHandle to a module. To attempt
     * to alleviate this issue, if {@code targetClass} is null {@link Constructor#getDeclaringClass()} will be used to
     * infer the class.
     * <p>
     * {@code scopeToAzure} is only when used when MethodHandles are being used and Java 9+ modules are being used. This
     * will determine whether to use a MethodHandles.Lookup scoped to {@code core} or to use a public
     * MethodHandles.Lookup. Scoping a MethodHandles.Lookup to {@code core} requires to module containing the
     * class to open or export to {@code core} which generally only holds true for other Azure SDKs, for example
     * there are cases where a reflective invocation is needed to Jackson which won't open or export to
     * {@code core} and the only APIs invoked reflectively are public APIs so the public MethodHandles.Lookup will
     * be used.
     *
     * @param targetClass The class that contains the constructor.
     * @param constructor The constructor to invoke.
     * @param scopeToAzureCore If Java 9+ modules is being used this will scope MethodHandle-based reflection to using
     * {@code core} as the scoped module, otherwise this is ignored.
     * @return An {@link ReflectiveInvoker} instance that will invoke the constructor.
     * @throws NullPointerException If {@code constructor} is null.
     * @throws Exception If the {@link ReflectiveInvoker} cannot be created.
     */
    public static ReflectiveInvoker getConstructorInvoker(Class<?> targetClass, Constructor<?> constructor,
                                                          boolean scopeToAzureCore) throws Exception {
        if (constructor == null) {
            throw LOGGER.logThrowableAsError(new NullPointerException("'constructor' cannot be null."));
        }

        targetClass = (targetClass == null) ? constructor.getDeclaringClass() : targetClass;
        return INSTANCE.getConstructorInvoker(targetClass, constructor, scopeToAzureCore);
    }

    /**
     * Determines whether a Java 9+ module-based implementation of {@link ReflectionUtilsApi} is being used.
     *
     * @return Whether a Java 9+ module-based implementation of {@link ReflectionUtilsApi} is being used.
     */
    public static boolean isModuleBased() {
        return INSTANCE.isModuleBased();
    }

    /**
     * Creates a dummy {@link ReflectiveInvoker} that will always return null. Used for scenarios where an {@link ReflectiveInvoker} is
     * needed as an identifier but will never be used.
     *
     * @return A dummy {@link ReflectiveInvoker} that will always return null.
     */
    public static ReflectiveInvoker createNoOpInvoker() {
        return new NoOpReflectiveInvoker();
    }

    private static final class NoOpReflectiveInvoker implements ReflectiveInvoker {
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
