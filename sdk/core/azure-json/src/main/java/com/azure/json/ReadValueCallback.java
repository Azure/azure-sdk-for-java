// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.IOException;
import java.util.function.Function;

/**
 * A callback used when reading a JSON value, such as {@link JsonReader#readArray(ReadValueCallback)}.
 * <p>
 * This interface has a similar API as {@link Function}, except that {@link #apply(Object)} is checked with
 * {@link IOException}, to provide a familiar feel.
 *
 * @param <T> Input type of the callback.
 * @param <R> Output type of the callback.
 */
@FunctionalInterface
public interface ReadValueCallback<T, R> {
    /**
     * Applies the callback to the {@code input}.
     *
     * @param input Input to the callback.
     * @return The output of the callback.
     * @throws IOException If an I/O error occurs during the callback.
     */
    R apply(T input) throws IOException;
}
