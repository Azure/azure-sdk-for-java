// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.IOException;
import java.util.function.BiConsumer;

/**
 * Equivalent in functionality to {@link BiConsumer} except that {@link #accept(Object, Object)} is checked with
 * {@link IOException}.
 *
 * @param <T> Type of the first input.
 * @param <U> Type of the second input.
 */
@FunctionalInterface
public interface IOExceptionBiConsumer<T, U> {
    /**
     * Passes the inputs to the consumer.
     *
     * @param input1 The first input.
     * @param input2 The second input.
     * @throws IOException If an I/O error occurs during the consumption of the inputs.
     */
    void accept(T input1, U input2) throws IOException;
}
