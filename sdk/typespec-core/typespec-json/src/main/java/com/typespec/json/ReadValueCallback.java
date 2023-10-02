// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json;

import java.io.IOException;

/**
 * A callback used when reading a JSON value, such as {@link JsonReader#readArray(ReadValueCallback)}.
 *
 * @param <T> Input type of the callback.
 * @param <R> Output type of the callback.
 *
 * @see JsonReader
 */
@FunctionalInterface
public interface ReadValueCallback<T, R> {
    /**
     * Applies the read callback to the {@code input}.
     *
     * @param input Input to the callback.
     * @return The output of the callback.
     * @throws IOException If an I/O error occurs during the callback.
     */
    R read(T input) throws IOException;
}
