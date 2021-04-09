// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import reactor.core.publisher.Mono;

/**
 * Asynchronous class to close resources.
 */
public interface AsyncAutoCloseable extends AutoCloseable {

    /**
     * Begins the close operation. If one is in progress, will return that existing close operation.
     *
     * @return A mono representing the close operation.
     */
    Mono<Void> closeAsync();

    /**
     * Synchronous close operation. Blocks until the entire operation is complete.
     */
    @Override
    void close();
}
