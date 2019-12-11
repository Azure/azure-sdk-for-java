// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.internal.changefeed.CancellationToken;
import com.azure.data.cosmos.internal.changefeed.Lease;
import com.azure.data.cosmos.internal.changefeed.LeaseManager;
import com.azure.data.cosmos.internal.changefeed.LeaseRenewer;
import com.azure.data.cosmos.internal.changefeed.exceptions.LeaseLostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Implementation for the {@link LeaseRenewer}.
 */
class LeaseRenewerImpl implements LeaseRenewer {
    private static final Logger logger = LoggerFactory.getLogger(LeaseRenewerImpl.class);
    private final LeaseManager leaseManager;
    private final Duration leaseRenewInterval;
    private Lease lease;
    private RuntimeException resultException;

    public LeaseRenewerImpl(Lease lease, LeaseManager leaseManager, Duration leaseRenewInterval) {
        this.lease = lease;
        this.leaseManager = leaseManager;
        this.leaseRenewInterval = leaseRenewInterval;
    }

    @Override
    public Mono<Void> run(CancellationToken cancellationToken) {
        logger.info("Partition {}: renewer task started.", this.lease.getLeaseToken());

        return Mono.just(this)
            .flatMap(value -> {
                if (cancellationToken.isCancellationRequested()) {
                    return Mono.empty();
                }

                ZonedDateTime stopTimer = ZonedDateTime.now().plus(this.leaseRenewInterval);
                return Mono.just(value)
                    .delayElement(Duration.ofMillis(100))
                    .repeat( () -> {
                        ZonedDateTime currentTime = ZonedDateTime.now();
                        return !cancellationToken.isCancellationRequested() && currentTime.isBefore(stopTimer);
                    }).last();
            })
            .flatMap(value -> {
                if (cancellationToken.isCancellationRequested()) {
                    return Mono.empty();
                }
                return this.renew(cancellationToken);
            })
            .repeat(() -> {
                if (cancellationToken.isCancellationRequested()) {
                    logger.info("Partition {}: renewer task stopped.", this.lease.getLeaseToken());
                }

                return !cancellationToken.isCancellationRequested();
            })
            .then()
            .doOnError(throwable -> {
                if (throwable instanceof LeaseLostException) {
                    logger.info("Partition {}: renew lease loop failed.", this.lease.getLeaseToken(), throwable);
                    this.resultException = (LeaseLostException) throwable;
                } else {
                    logger.error("Partition {}: renew lease loop failed.", this.lease.getLeaseToken(), throwable);
                    this.resultException = new RuntimeException(throwable);
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
                logger.info("Partition {}: renewed lease with result {}", this.lease.getLeaseToken(), renewedLease != null);
                return renewedLease;
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof LeaseLostException) {
                    LeaseLostException lle = (LeaseLostException) throwable;
                    this.resultException = lle;
                    logger.error("Partition {}: lost lease on renew.", this.lease.getLeaseToken(), lle);
                    return Mono.error(lle);
                }

                logger.error("Partition {}: failed to renew lease.", this.lease.getLeaseToken(), throwable);
                return Mono.empty();
            });
    }

}
