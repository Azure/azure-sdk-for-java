// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.implementation;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Supplier that has an {@link IOException} on {@link #get()}.
 *
 * @param <T> Type returned by the supplier.
 */
@FunctionalInterface
public interface IoExceptionSupplier<T> {
    /**
     * Supplies a value.
     *
     * @return The value.
     * @throws IOException If an error occurs supplying the value.
     */
    T get() throws IOException;

    /**
     * Supplies a value and handles wrapping the {@link IOException} thrown by {@link #get()} with an {@link
     * UncheckedIOException}.
     *
     * @return The value.
     * @throws UncheckedIOException If an error occurs supplying the value.
     */
    default T getWithUncheckedIoException() {
        try {
            return get();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
