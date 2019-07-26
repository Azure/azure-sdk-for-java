// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import reactor.core.publisher.Flux;

/**
 * Represents operations to get leases from lease store..
 */
public interface LeaseContainer {
    /**
     * @return all leases.
     */
    Flux<Lease> getAllLeases();

    /**
     * @return all leases owned by the current host.
     */
    Flux<Lease> getOwnedLeases();
}
