// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import java.io.IOException;

/**
 * A function that is checked with {@link IOException}.
 *
 * @param <T> Input type of the function.
 * @param <R> Output type of the function.
 */
@FunctionalInterface
public interface IOExceptionCheckedFunction<T, R> {
    /**
     * Applies the function to the {@code input}.
     *
     * @param input Input to the function.
     * @return The output of the function.
     * @throws IOException If an I/O error occurs during the function call.
     */
    R apply(T input) throws IOException;
}
