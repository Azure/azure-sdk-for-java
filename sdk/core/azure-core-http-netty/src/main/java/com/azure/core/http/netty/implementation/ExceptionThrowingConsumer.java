// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import java.util.function.Consumer;

/**
 * {@link Consumer}-like interface that has a checked {@link Exception} on the consume method to allow consumers to
 * throw checked exceptions.
 *
 * @param <T> Type of object that is consumed.
 */
interface ExceptionThrowingConsumer<T> {
    /**
     * Consumes the object.
     *
     * @param t The object.
     * @throws Exception Any exception thrown during consumption.
     */
    void consume(T t) throws Exception;
}
