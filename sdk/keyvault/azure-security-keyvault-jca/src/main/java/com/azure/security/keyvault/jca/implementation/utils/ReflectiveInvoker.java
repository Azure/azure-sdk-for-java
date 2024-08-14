// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

/**
 * Interface that defines an implementation-agnostic way to invoke APIs reflectively.
 */
public interface ReflectiveInvoker {
    /**
     * Invokes an API that doesn't have a target.
     *
     * <p>APIs without a target are constructors and static methods.</p>
     *
     * @return The result of invoking the API.
     *
     * @param args The arguments to pass to the API.
     *
     * @throws Exception If the API invocation fails.
     */
    Object invokeStatic(Object... args) throws Exception;
}
