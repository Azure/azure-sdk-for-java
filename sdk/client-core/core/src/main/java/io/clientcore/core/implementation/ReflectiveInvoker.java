// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation;

/**
 * Interface that defines an implementation-agnostic way to invoke APIs reflectively.
 */
public interface ReflectiveInvoker {
    /**
     * Invokes an API that doesn't have a target.
     * <p>
     * APIs without a target are constructors and static methods.
     *
     * @return The result of invoking the API.
     * @param args The arguments to pass to the API.
     * @throws Exception If the API invocation fails.
     */
    Object invokeStatic(Object... args) throws Exception;

    /**
     * Invokes the API on the target object with the provided arguments.
     *
     * @param target The target object to invoke the API on.
     * @param args The arguments to pass to the API.
     * @return The result of invoking the API.
     * @throws Exception If the API invocation fails.
     */
    Object invokeWithArguments(Object target, Object... args) throws Exception;

    /**
     * Gets the number of parameters the API takes.
     *
     * @return The number of parameters the API takes.
     */
    int getParameterCount();
}
