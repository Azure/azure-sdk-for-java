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
    private final LeaseStoreManager pkRangeBasedLeaseStoreManager;
    private final LeaseStoreManager epkRangeVersionLeaseStoreManager;

    private volatile boolean isInitialized;
    private volatile boolean isLockAcquired;

    public BootstrapperImpl(PartitionSynchronizer synchronizer, LeaseStore leaseStore, LeaseStoreManager pkRangeBasedLeaseStoreManager, LeaseStoreManager epkRangeVersionLeaseStoreManager, Duration lockTime, Duration sleepTime) {
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

        if (epkRangeVersionLeaseStoreManager == null) {
            throw new IllegalArgumentException("epkRangeBasedLeaseStoreManager cannot be null!");
        }

        this.synchronizer = synchronizer;
        this.leaseStore = leaseStore;
        this.lockTime = lockTime;
        this.sleepTime = sleepTime;
        this.pkRangeBasedLeaseStoreManager = pkRangeBasedLeaseStoreManager;
        this.epkRangeVersionLeaseStoreManager = epkRangeVersionLeaseStoreManager;

        this.isInitialized = false;
    }

    @Override
    public Mono<Void> initialize() {
        this.isInitialized = false;

        return Mono.just(this)
            .flatMap( value -> this.leaseStore.isInitialized())
            .flatMap(initialized -> {
                this.isInitialized = initialized;

                if (initialized) {

                    return this.epkRangeVersionLeaseStoreManager
                        .getTopLeases(1)
                        // pick one lease corresponding to a lease prefix (lease prefix denotes a unique feed)
                        .next()
                        .flatMap(lease -> {

                            if (lease.getVersion() == LeaseVersion.EPK_RANGE_BASED_LEASE) {
                                // todo: modify error message - lease interoperability works but only for LatestVersion but from Pk-Range to Epk-Range and not vice-versa
                                // error out if Pk-Range based handleChanges is started with same lease prefix as pre-existing Epk-Range based lease (no lease reuse even from LatestVersion)
                                return Mono.error(new IllegalStateException("Use handleLatestVersion or handleAllVersionsAndDeletes instead."));
                            }

                            return Mono.empty();
                        });
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
}
