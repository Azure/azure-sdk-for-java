// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.utils;

import java.io.IOException;

/**
 * A consumer checked with {@link IOException}.
 *
 * @param <T> The type of the consumer.
 */
@FunctionalInterface
public interface IOExceptionCheckedConsumer<T> {
    /**
     * Applies the inputs to the bi-consumer.
     *
     * @param input The input of the consumer.
     * @throws IOException If an I/O error occurs during consumption.
     */
    void accept(T input) throws IOException;
}
