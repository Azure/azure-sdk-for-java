// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.internal.changefeed.Bootstrapper;
import com.azure.data.cosmos.internal.changefeed.LeaseStore;
import com.azure.data.cosmos.internal.changefeed.PartitionSynchronizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implementation for the bootstrapping interface.
 */
class BootstrapperImpl implements Bootstrapper {
    private final Logger logger = LoggerFactory.getLogger(BootstrapperImpl.class);
    private final PartitionSynchronizer synchronizer;
    private final LeaseStore leaseStore;
    private final Duration lockTime;
    private final Duration sleepTime;

    private boolean isInitialized;
    private boolean isLockAcquired;

    public BootstrapperImpl(PartitionSynchronizer synchronizer, LeaseStore leaseStore, Duration lockTime, Duration sleepTime)
    {
        if (synchronizer == null) throw new IllegalArgumentException("synchronizer");
        if (leaseStore == null) throw new IllegalArgumentException("leaseStore");
        if (lockTime == null || lockTime.isNegative() || lockTime.isZero()) throw new IllegalArgumentException("lockTime should be non-null and positive");
        if (sleepTime == null || sleepTime.isNegative() || sleepTime.isZero()) throw new IllegalArgumentException("sleepTime should be non-null and positive");

        this.synchronizer = synchronizer;
        this.leaseStore = leaseStore;
        this.lockTime = lockTime;
        this.sleepTime = sleepTime;

        this.isInitialized = false;
    }

    @Override
    public Mono<Void> initialize() {
        BootstrapperImpl self = this;
        self.isInitialized = false;

        return Mono.just(self)
            .flatMap( value -> self.leaseStore.isInitialized())
            .flatMap(initialized -> {
                self.isInitialized = initialized;

                if (initialized) {
                    return Mono.empty();
                } else {
                    return self.leaseStore.acquireInitializationLock(self.lockTime)
                        .flatMap(lockAcquired -> {
                            self.isLockAcquired = lockAcquired;

                            if (!self.isLockAcquired) {
                                logger.info("Another instance is initializing the store");
                                try {
                                    Thread.sleep(self.sleepTime.toMillis());
                                } catch (InterruptedException ex) {
                                    logger.warn("Unexpected exception caught", ex);
                                }
                                return Mono.just(isLockAcquired);
                            } else {
                                return self.synchronizer.createMissingLeases()
                                    .then(self.leaseStore.markInitialized());
                            }
                        })
                        .onErrorResume(throwable -> {
                            logger.warn("Unexpected exception caught", throwable);
                            return Mono.just(self.isLockAcquired);
                        })
                        .flatMap(lockAcquired -> {
                            if (self.isLockAcquired) {
                                return self.leaseStore.releaseInitializationLock();
                            }
                            return Mono.just(lockAcquired);
                        });
                }
            })
            .repeat( () -> !self.isInitialized)
            .then();
    }
}
