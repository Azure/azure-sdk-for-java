// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.IOException;
import java.util.function.BiConsumer;

/**
 * A callback used when writing a JSON value, such as {@link JsonWriter#writeArray(Object[], WriteValueCallback)}.
 * <p>
 * This interface has a similar API as {@link BiConsumer}, except that {@link #accept(Object, Object)} is checked with
 * {@link IOException}, to provide a familiar feel.
 *
 * @param <T> First type of the callback.
 * @param <U> Second type of the callback.
 */
@FunctionalInterface
public interface WriteValueCallback<T, U> {
    /**
     * Applies the callback to {@code input1} and {@code input2}.
     *
     * @param input1 First type of the callback.
     * @param input2 Second type of the callback.
     * @throws IOException If an I/O error occurs during the callback.
     */
    void accept(T input1, U input2) throws IOException;
}
