// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json;

import java.io.IOException;

/**
 * A callback used when writing a JSON value, such as {@link JsonWriter#writeArray(Object[], WriteValueCallback)}.
 *
 * @param <T> First type of the callback.
 * @param <U> Second type of the callback.
 *
 * @see JsonWriter
 */
@FunctionalInterface
public interface WriteValueCallback<T, U> {
    /**
     * Applies the write callback to {@code input1} and {@code input2}.
     *
     * @param input1 First type of the callback.
     * @param input2 Second type of the callback.
     * @throws IOException If an I/O error occurs during the callback.
     */
    void write(T input1, U input2) throws IOException;
}
