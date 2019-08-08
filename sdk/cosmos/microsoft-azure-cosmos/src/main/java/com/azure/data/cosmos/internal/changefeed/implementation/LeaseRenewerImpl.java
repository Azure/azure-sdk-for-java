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

/**
 * Implementation for the {@link LeaseRenewer}.
 */
class LeaseRenewerImpl implements LeaseRenewer {
    private static final Logger logger = LoggerFactory.getLogger(LeaseRenewerImpl.class);
    private final LeaseManager leaseManager;
    private final Duration leaseRenewInterval;
    private Lease lease;
    private RuntimeException resultException;

    public LeaseRenewerImpl(Lease lease, LeaseManager leaseManager, Duration leaseRenewInterval)
    {
        this.lease = lease;
        this.leaseManager = leaseManager;
        this.leaseRenewInterval = leaseRenewInterval;
    }

    @Override
    public Mono<Void> run(CancellationToken cancellationToken) {
        LeaseRenewerImpl self = this;

        logger.info("Partition {}: renewer task started.", self.lease.getLeaseToken());
        long remainingWork = this.leaseRenewInterval.toMillis();

        try {
            while (!cancellationToken.isCancellationRequested() && remainingWork > 0) {
                Thread.sleep(100);
                remainingWork -= 100;
            }
        } catch (InterruptedException ex) {
            // exception caught
            logger.info("Partition {}: renewer task stopped.", self.lease.getLeaseToken());
            return Mono.empty();
        }

        return Mono.just(self)
            .flatMap(value -> self.renew(cancellationToken))
            .repeat(() -> {
                if (cancellationToken.isCancellationRequested()) return false;

                long remainingWorkInLoop = this.leaseRenewInterval.toMillis();

                try {
                    while (!cancellationToken.isCancellationRequested() && remainingWorkInLoop > 0) {
                        Thread.sleep(100);
                        remainingWorkInLoop -= 100;
                    }
                } catch (InterruptedException ex) {
                    // exception caught
                    logger.info("Partition {}: renewer task stopped.", self.lease.getLeaseToken());
                    return false;
                }

                return !cancellationToken.isCancellationRequested();
            })
            .then()
            .onErrorResume(throwable -> {
                logger.error("Partition {}: renew lease loop failed.", self.lease.getLeaseToken(), throwable);
                return Mono.error(throwable);
            });
    }

    @Override
    public RuntimeException getResultException() {
        return this.resultException;
    }

    private Mono<Lease> renew(CancellationToken cancellationToken) {
        LeaseRenewerImpl self = this;

        if (cancellationToken.isCancellationRequested()) return Mono.empty();

        return self.leaseManager.renew(self.lease)
            .map(renewedLease -> {
                if (renewedLease != null) {
                    self.lease = renewedLease;
                }
                logger.info("Partition {}: renewed lease with result {}", self.lease.getLeaseToken(), renewedLease != null);
                return renewedLease;
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof LeaseLostException) {
                    LeaseLostException lle = (LeaseLostException) throwable;
                    self.resultException = lle;
                    logger.error("Partition {}: lost lease on renew.", self.lease.getLeaseToken(), lle);
                    return Mono.error(lle);
                }

                logger.error("Partition {}: failed to renew lease.", self.lease.getLeaseToken(), throwable);
                return Mono.empty();
            });
    }

}
