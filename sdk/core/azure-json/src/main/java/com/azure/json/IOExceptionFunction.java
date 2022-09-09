// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.IOException;
import java.util.function.Function;

/**
 * Equivalent in functionality to {@link Function} except that {@link #apply(Object)} is checked with
 * {@link IOException}.
 *
 * @param <T> Input type of the function.
 * @param <R> Output type of the function.
 */
@FunctionalInterface
public interface IOExceptionFunction<T, R> {
    /**
     * Applies the function to the {@code input}.
     *
     * @param input Input to the function.
     * @return The output of the function.
     * @throws IOException If an I/O error occurs during application of the function.
     */
    R apply(T input) throws IOException;
}
