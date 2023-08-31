// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation;

/**
 * Interface that defines an implementation-agnostic way to invoke APIs reflectively.
 */
public interface Invoker {
    /**
     * Invokes the API on the target object with the provided arguments.
     * <p>
     * If the implementation is MethodHandle-based {@code target} is ignored as the MethodHandle already knows the
     * target object.
     * <p>
     * For static APIs {@code target} must be null.
     * <p>
     * For MethodHandle-based implementations this is equivalent to MethodHandle.invoke, for reflection-based
     * implementations all APIs are equivalent.
     *
     * @param target The target object to invoke the API on.
     * @param args The arguments to pass to the API.
     * @return The result of invoking the API.
     * @throws Throwable If the API invocation fails.
     */
    Object invoke(Object target, Object... args) throws Throwable;

    /**
     * Invokes the API on the target object with the provided arguments.
     * <p>
     * If the implementation is MethodHandle-based {@code target} is ignored as the MethodHandle already knows the
     * target object.
     * <p>
     * For static APIs {@code target} must be null.
     * <p>
     * For MethodHandle-based implementations this is equivalent to MethodHandle.invoke, for reflection-based
     * implementations all APIs are equivalent.
     *
     * @param target The target object to invoke the API on.
     * @param args The arguments to pass to the API.
     * @return The result of invoking the API.
     * @throws Throwable If the API invocation fails.
     */
    Object invokeWithArguments(Object target, Object... args) throws Throwable;

    /**
     * Invokes the API on the target object with the provided arguments.
     * <p>
     * If the implementation is MethodHandle-based {@code target} is ignored as the MethodHandle already knows the
     * target object.
     * <p>
     * For static APIs {@code target} must be null.
     * <p>
     * For MethodHandle-based implementations this is equivalent to MethodHandle.invoke, for reflection-based
     * implementations all APIs are equivalent.
     *
     * @param target The target object to invoke the API on.
     * @param args The arguments to pass to the API.
     * @return The result of invoking the API.
     * @throws Throwable If the API invocation fails.
     */
    Object invokeExact(Object target, Object... args) throws Throwable;

    /**
     * Gets the number of parameters the API takes.
     *
     * @return The number of parameters the API takes.
     */
    int getParameterCount();
}
