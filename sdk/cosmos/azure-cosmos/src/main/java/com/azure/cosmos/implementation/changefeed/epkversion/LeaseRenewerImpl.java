// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.LeaseRenewer;
import com.azure.cosmos.implementation.changefeed.exceptions.LeaseLostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation for the {@link LeaseRenewer}.
 */
class LeaseRenewerImpl implements LeaseRenewer {
    private static final Logger logger = LoggerFactory.getLogger(LeaseRenewerImpl.class);
    private final LeaseManager leaseManager;
    private final Duration leaseRenewInterval;
    private Lease lease;
    private RuntimeException resultException;
    private Instant lastVerification;
    private final AtomicBoolean processedBatches;
    private static final int VERIFICATION_FACTOR = 25;

    public LeaseRenewerImpl(Lease lease, LeaseManager leaseManager, Duration leaseRenewInterval, AtomicBoolean processedBatches) {
        this.lease = lease;
        this.leaseManager = leaseManager;
        this.leaseRenewInterval = leaseRenewInterval;
        this.lastVerification = Instant.now();
        this.processedBatches = processedBatches;
    }

    @Override
    public Mono<Void> run(CancellationToken cancellationToken) {
        logger.info("Lease with token {}: renewer task started.", this.lease.getLeaseToken());

        return Mono.just(this)
            .flatMap(value -> {
                if (cancellationToken.isCancellationRequested()) {
                    return Mono.empty();
                }

                Instant stopTimer = Instant.now().plus(this.leaseRenewInterval);
                return Mono.just(value)
                    .delayElement(Duration.ofMillis(100), CosmosSchedulers.COSMOS_PARALLEL)
                    .repeat( () -> {
                        Instant currentTime = Instant.now();
                        return !cancellationToken.isCancellationRequested() && currentTime.isBefore(stopTimer);
                    }).last();
            })
            .flatMap(value -> {
                if (cancellationToken.isCancellationRequested()) {
                    return Mono.empty();
                }
                Duration timeSinceLastVerification = Duration.between(this.lastVerification, Instant.now());
                if (timeSinceLastVerification.getSeconds() > this.leaseRenewInterval.getSeconds() * VERIFICATION_FACTOR) {
                    this.lastVerification = Instant.now();
                    // if cfp has seen successes processing, we do a renew,
                    // otherwise we do not to allow lease stealing
                    if (processedBatches.get()) {
                        logger.info("Lease with token {}: renewing lease as batches have been processed.", this.lease.getLeaseToken());
                        processedBatches.set(false);
                        return this.renew(cancellationToken);
                    } else {
                        logger.info("Lease with token {}: skipping renew as no batches processed.", this.lease.getLeaseToken());
                        return Mono.empty();
                    }
                }
                return this.renew(cancellationToken);
            })
            .repeat(() -> {
                if (cancellationToken.isCancellationRequested()) {
                    logger.info("Lease with token {}: renewer task stopped.", this.lease.getLeaseToken());
                }

                return !cancellationToken.isCancellationRequested();
            })
            .then()
            .doOnError(throwable -> {
                if (throwable instanceof LeaseLostException) {
                    logger.info("Lease with token {}: renew lease loop failed.", this.lease.getLeaseToken(), throwable);
                } else {
                    logger.error("Lease with token {}: renew lease loop failed.", this.lease.getLeaseToken(), throwable);
                }
            });
    }

    @Override
    public RuntimeException getResultException() {
        return this.resultException;
    }

    private Mono<Lease> renew(CancellationToken cancellationToken) {
        if (cancellationToken.isCancellationRequested()) {
            return Mono.empty();
        }

        return this.leaseManager.renew(this.lease)
            .map(renewedLease -> {
                if (renewedLease != null) {
                    this.lease = renewedLease;
                }
                logger.info("Lease with token {}: renewed lease with result {}", this.lease.getLeaseToken(), renewedLease != null);
                return renewedLease;
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof LeaseLostException) {
                    LeaseLostException lle = (LeaseLostException) throwable;
                    this.resultException = lle;
                    logger.error("Lease with token {} with lease token{}: lost lease on renew.", this.lease.getLeaseToken(), lle);
                    return Mono.error(lle);
                }

                logger.error("Lease with token {}: failed to renew lease.", this.lease.getLeaseToken(), throwable);
                return Mono.empty();
            });
    }

}
