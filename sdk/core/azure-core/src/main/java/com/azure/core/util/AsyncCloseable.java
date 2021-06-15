// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import reactor.core.publisher.Mono;

/**
 * Interface for close operations that are asynchronous.
 *
 * <p><strong>Asynchronously closing a class</strong></p>
 * <p>In the snippet below, we have a long-lived {@code NetworkResource} class. There are some operations such
 * as closing {@literal I/O}. Instead of returning a sync {@code close()}, we use {@code closeAsync()} so users'
 * programs don't block waiting for this operation to complete.</p>
 *
 * {@codesnippet com.azure.core.util.AsyncCloseable.closeAsync}
 */
public interface AsyncCloseable {
    /**
     * Begins the close operation. If one is in progress, will return that existing close operation. If the close
     * operation is unsuccessful, the Mono completes with an error.
     *
     * @return A Mono representing the close operation. If the close operation is unsuccessful, the Mono completes with
     *     an error.
     */
    Mono<Void> closeAsync();
}
