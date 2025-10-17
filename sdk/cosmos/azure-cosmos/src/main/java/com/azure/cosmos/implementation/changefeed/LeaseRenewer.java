// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed;

import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Interface for the lease renewer.
 */
public interface LeaseRenewer {
    /**
     * Starts the lease renewer.
     *
     * @param cancellationToken the token used for canceling the workload.
     * @return a deferred operation of this call.
     */
    Mono<Void> run(CancellationToken cancellationToken);

    /**
     * @return the inner exception if any, otherwise null.
     */
    RuntimeException getResultException();

    /**
     * @return the interval at which the lease will be renewed.
     */
    Duration getLeaseRenewInterval();
}
