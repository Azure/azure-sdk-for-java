// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import java.io.IOException;

/**
 * A bi-consumer checked with {@link IOException}.
 *
 * @param <T> First type of the bi-consumer.
 * @param <U> Second type of the bi-consumer.
 */
@FunctionalInterface
public interface IOExceptionCheckedBiConsumer<T, U> {
    /**
     * Applies the inputs to the bi-consumer.
     *
     * @param input1 First type of the bi-consumer.
     * @param input2 Second type of the bi-consumer.
     * @throws IOException If an I/O error occurs during consumption.
     */
    void accept(T input1, U input2) throws IOException;
}
