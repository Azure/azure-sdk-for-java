// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Invoker that has an {@link IOException} on {@link #invoke()}.
 */
@FunctionalInterface
interface IoExceptionInvoker {
    /**
     * Invokes a method.
     *
     * @throws IOException If an error occurs while invoking the method.
     */
    void invoke() throws IOException;

    /**
     * Invokes a method and handles wrapping the {@link IOException} thrown by {@link #invoke()} with an
     * {@link UncheckedIOException}.
     *
     * @param logger The {@link ClientLogger} used for logging.
     * @throws UncheckedIOException If an error occurs while invoking the method.
     */
    default void invokeWithUncheckedIoException(ClientLogger logger) {
        try {
            invoke();
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }
}
