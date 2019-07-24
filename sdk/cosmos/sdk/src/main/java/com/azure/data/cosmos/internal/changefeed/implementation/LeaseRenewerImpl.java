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
    private final Logger logger = LoggerFactory.getLogger(LeaseRenewerImpl.class);
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

        return Mono.fromRunnable( () -> {
            try {
                logger.info(String.format("Partition %s: renewer task started.", self.lease.getLeaseToken()));
                long remainingWork = this.leaseRenewInterval.toMillis() / 2;

                try {
                    while (!cancellationToken.isCancellationRequested() && remainingWork > 0) {
                        Thread.sleep(100);
                        remainingWork -= 100;
                    }
                } catch (InterruptedException ex) {
                    // exception caught
                    logger.info(String.format("Partition %s: renewer task stopped.", self.lease.getLeaseToken()));
                }

                while (!cancellationToken.isCancellationRequested()) {
                    self.renew().block();

                    remainingWork = this.leaseRenewInterval.toMillis();

                    try {
                        while (!cancellationToken.isCancellationRequested() && remainingWork > 0) {
                            Thread.sleep(100);
                            remainingWork -= 100;
                        }
                    } catch (InterruptedException ex) {
                        // exception caught
                        logger.info(String.format("Partition %s: renewer task stopped.", self.lease.getLeaseToken()));
                        break;
                    }
                }
            } catch (RuntimeException ex) {
                logger.error(String.format("Partition %s: renew lease loop failed.", self.lease.getLeaseToken()), ex);
                self.resultException = ex;
            }
        });
    }

    @Override
    public RuntimeException getResultException() {
        return this.resultException;
    }

    private Mono<Void> renew() {
        LeaseRenewerImpl self = this;

        return Mono.fromRunnable( () -> {
            try {
                Lease renewedLease = self.leaseManager.renew(this.lease).block();
                if (renewedLease != null) this.lease = renewedLease;

                logger.info(String.format("Partition %s: renewed lease with result %s", self.lease.getLeaseToken(), renewedLease != null));
            } catch (LeaseLostException leaseLostException) {
                logger.error(String.format("Partition %s: lost lease on renew.", self.lease.getLeaseToken()), leaseLostException);
                self.resultException = leaseLostException;
                throw leaseLostException;
            } catch (Exception ex) {
                logger.error(String.format("Partition %s: failed to renew lease.", self.lease.getLeaseToken()), ex);
            }
        });
    }

}
