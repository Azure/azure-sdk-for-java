// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.implementation;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Invoker that has an {@link IOException} on {@link #invoke()}.
 */
@FunctionalInterface
public interface IoExceptionInvoker {
    /**
     * Invokes a method.
     *
     * @throws IOException If an error occurs while invoking the method.
     */
    void invoke() throws IOException;

    /**
     * Invokes a method and handles wrapping the {@link IOException} thrown by {@link #invoke()} with an {@link
     * UncheckedIOException}.
     *
     * @throws UncheckedIOException If an error occurs while invoking the method.
     */
    default void invokeWithUncheckedIoException() {
        try {
            invoke();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
