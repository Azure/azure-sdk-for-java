// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import reactor.core.publisher.Mono;

/**
 * Interface for check-pointing the lease.
 */
public interface LeaseCheckpointer {
    /**
     * Check-points the lease.
     * <p>
     * Throws LeaseLostException if other host acquired the lease or lease was deleted.
     *
     * @param lease the lease to renew.
     * @param continuationToken the continuation token.
     * @return the updated renewed lease.
     */
    Mono<Lease> checkpoint(Lease lease, String continuationToken);
}
