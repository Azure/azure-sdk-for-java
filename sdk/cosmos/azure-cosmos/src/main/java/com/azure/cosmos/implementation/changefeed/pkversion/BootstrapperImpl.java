// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.changefeed.Bootstrapper;
import com.azure.cosmos.implementation.changefeed.LeaseStore;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.LeaseVersion;
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

    private volatile boolean isInitialized;
    private volatile boolean isLockAcquired;

    private final LeaseStoreManager pkRangeBasedLeaseStoreManager;
    private final ChangeFeedMode changeFeedModeToStart;

    public BootstrapperImpl(PartitionSynchronizer synchronizer, LeaseStore leaseStore, Duration lockTime, LeaseStoreManager pkRangeBasedLeaseStoreManager, Duration sleepTime, ChangeFeedMode changeFeedModeToStart) {
        if (synchronizer == null) {
            throw new IllegalArgumentException("synchronizer cannot be null!");
        }

        if (leaseStore == null) {
            throw new IllegalArgumentException("leaseStore cannot be null!");
        }

        if (lockTime == null || lockTime.isNegative() || lockTime.isZero()) {
            throw new IllegalArgumentException("lockTime should be non-null and positive");
        }

        if (sleepTime == null || sleepTime.isNegative() || sleepTime.isZero()) {
            throw new IllegalArgumentException("sleepTime should be non-null and positive");
        }

        if (pkRangeBasedLeaseStoreManager == null) {
            throw new IllegalArgumentException("pkRangeBasedLeaseStoreManager cannot be null!");
        }

        if (changeFeedModeToStart == null) {
            throw new IllegalArgumentException("changeFeedModeToStart cannot be null!");
        }

        this.synchronizer = synchronizer;
        this.leaseStore = leaseStore;
        this.lockTime = lockTime;
        this.sleepTime = sleepTime;

        this.isInitialized = false;
        this.pkRangeBasedLeaseStoreManager = pkRangeBasedLeaseStoreManager;
        this.changeFeedModeToStart = changeFeedModeToStart;
    }

    @Override
    public Mono<Void> initialize() {
        this.isInitialized = false;

        return Mono.just(this)
            .flatMap( value -> this.leaseStore.isInitialized())
            .flatMap(initialized -> {
                this.isInitialized = initialized;

                if (initialized) {
                    return this.validateLeaseCFModeInteroperabilityForPkRangeBasedLease();
                } else {
                    logger.info("Acquire initialization lock");
                    return this.leaseStore.acquireInitializationLock(this.lockTime)
                        .flatMap(lockAcquired -> {
                            this.isLockAcquired = lockAcquired;

                            if (!this.isLockAcquired) {
                                logger.info("Another instance is initializing the store");
                                return Mono.just(isLockAcquired).delayElement(this.sleepTime, CosmosSchedulers.COSMOS_PARALLEL);
                            } else {
                                return this.synchronizer.createMissingLeases()
                                    .then(this.leaseStore.markInitialized());
                            }
                        })
                        .onErrorResume(throwable -> {
                            logger.warn("Unexpected exception caught while initializing the lock", throwable);
                            return Mono.just(this.isLockAcquired);
                        })
                        .flatMap(lockAcquired -> {
                            if (this.isLockAcquired) {
                                return this.leaseStore.releaseInitializationLock();
                            }
                            return Mono.just(lockAcquired);
                        });
                }
            })
            .repeat( () -> !this.isInitialized)
            .then();
    }

    private Mono<Void> validateLeaseCFModeInteroperabilityForPkRangeBasedLease() {

        // fetches only 1 epk-based leases for a given lease prefix
        return this.pkRangeBasedLeaseStoreManager
            .getTopLeases(1)
            // pick one lease corresponding to a lease prefix (lease prefix denotes a unique feed)
            .next()
            .flatMap(lease -> {

                if (lease.getVersion() == LeaseVersion.PARTITION_KEY_BASED_LEASE) {
                    if (!Strings.isNullOrEmpty(lease.getId())) {

                        if (!Strings.isNullOrEmpty(lease.getContinuationToken())) {
                            ChangeFeedState changeFeedState = ChangeFeedState.fromString(lease.getContinuationToken());

                            if (changeFeedState.getMode() != this.changeFeedModeToStart) {
                                return Mono.error(new IllegalStateException("Change feed mode in the pre-existing lease is : " + changeFeedState.getMode() + " while the expected change feed mode is : " + this.changeFeedModeToStart));
                            }
                        }
                    }
                }

                return Mono.empty();
            });
    }
}
