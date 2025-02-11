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
     * <p>
     * This method provides convenience for invoking static APIs with a variable number of arguments.
     * Don't use this method in performance-sensitive scenarios. Use {@code
     * invoke()} and other invoke overloads on the hot path.
     * <p>
     *
     * @return The result of invoking the API.
     * @param args The arguments to pass to the API.
     * @throws Exception If the API invocation fails.
     */
    Object invokeStatic(Object... args) throws Exception;

    /**
     * Invokes the API on the target object with the provided arguments.
     * <p>
     * This method provides convenience for invoking APIs with a variable number of arguments.
     * Don't use this method in performance-sensitive scenarios. Use {@code
     * invoke(Object argOrTarget)} and other invoke overloads on the hot path.
     * <p>
     *
     * @param target The target object to invoke the API on.
     * @param args The arguments to pass to the API.
     * @return The result of invoking the API.
     * @throws Exception If the API invocation fails.
     */
    Object invokeWithArguments(Object target, Object... args) throws Exception;

    /**
     * Invokes the static API with no arguments.
     *
     * @return The result of invoking the API.
     * @throws Exception If the API invocation fails.
     */
    Object invoke() throws Exception;

    /**
     * Invokes the API with the provided argument or on the provided target.
     *
     * @param argOrTarget The argument to pass to the API or the target object to invoke the API on.
     * @return The result of invoking the API.
     * @throws Exception If the API invocation fails.
     */
    Object invoke(Object argOrTarget) throws Exception;

    /**
     * Invokes the API with the provided arguments or on the provided target.
     *
     * @param argOrTarget The argument to pass to the API or the target object to invoke the API on.
     * @param arg1 The second argument to pass to the API.
     * @return The result of invoking the API.
     * @throws Exception If the API invocation fails.
     */
    Object invoke(Object argOrTarget, Object arg1) throws Exception;

    /**
     * Invokes the API with the provided arguments or on the provided target.
     *
     * @param argOrTarget The argument to pass to the API or the target object to invoke the API on.
     * @param arg1 The second argument to pass to the API.
     * @param arg2 The third argument to pass to the API.
     * @return The result of invoking the API.
     * @throws Exception If the API invocation fails.
     */
    Object invoke(Object argOrTarget, Object arg1, Object arg2) throws Exception;

    /**
     * Invokes the API with the provided arguments or on the provided target.
     *
     * @param argOrTarget The argument to pass to the API or the target object to invoke the API on.
     * @param arg1 The second argument to pass to the API.
     * @param arg2 The third argument to pass to the API.
     * @param arg3 The fourth argument to pass to the API.
     * @return The result of invoking the API.
     * @throws Exception If the API invocation fails.
     */
    Object invoke(Object argOrTarget, Object arg1, Object arg2, Object arg3) throws Exception;

    /**
     * Gets the number of parameters the API takes.
     *
     * @return The number of parameters the API takes.
     */
    int getParameterCount();
}
