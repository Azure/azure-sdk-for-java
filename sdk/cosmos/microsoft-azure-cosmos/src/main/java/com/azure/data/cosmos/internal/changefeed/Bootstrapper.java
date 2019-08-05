// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import reactor.core.publisher.Mono;

/**
 * Bootstrapping interface.
 */
public interface Bootstrapper {
    /**
     * It initializes the bootstrapping.
     *
     * @return a deferred computation of this call.
     */
    Mono<Void> initialize();
}
