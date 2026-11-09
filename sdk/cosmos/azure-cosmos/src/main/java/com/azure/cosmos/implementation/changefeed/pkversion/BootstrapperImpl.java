// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.changefeed.Bootstrapper;
import com.azure.cosmos.implementation.changefeed.LeaseStore;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import com.azure.cosmos.implementation.changefeed.common.LeaseVersion;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
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
    private final LeaseStoreManager epkRangeVersionLeaseStoreManager;
    private final ChangeFeedProcessorOptions changeFeedProcessorOptions;

    private volatile boolean isInitialized;
    private volatile boolean isLockAcquired;

    public BootstrapperImpl(
        PartitionSynchronizer synchronizer,
        LeaseStore leaseStore,
        LeaseStoreManager epkRangeVersionLeaseStoreManager,
        ChangeFeedProcessorOptions changeFeedProcessorOptions,
        Duration lockTime,
        Duration sleepTime) {

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

        if (epkRangeVersionLeaseStoreManager == null) {
            throw new IllegalArgumentException("epkRangeBasedLeaseStoreManager cannot be null!");
        }

        if (changeFeedProcessorOptions == null) {
            throw new IllegalArgumentException("changeFeedProcessorOptions cannot be null!");
        }

        this.synchronizer = synchronizer;
        this.leaseStore = leaseStore;
        this.lockTime = lockTime;
        this.sleepTime = sleepTime;
        this.epkRangeVersionLeaseStoreManager = epkRangeVersionLeaseStoreManager;
        this.changeFeedProcessorOptions = changeFeedProcessorOptions;

        this.isInitialized = false;
    }

    @Override
    public Mono<Void> initialize() {
        this.isInitialized = false;

        return Mono.just(this)
            .flatMap(value -> this.leaseStore.isInitialized())
            .flatMap(initialized -> {
                this.isInitialized = initialized;
                Mono<Void> previousOperation = Mono.empty();
                if (initialized) {
                    Mono<Void> validateExistingLeasesMono = this.epkRangeVersionLeaseStoreManager
                        .getTopLeases(1)
                        .next()
                        .flatMap(lease -> {

                            if (lease.getVersion() == LeaseVersion.EPK_RANGE_BASED_LEASE) {

                                String errorMessage = String.format("ChangeFeedProcessor#handleChanges cannot be invoked when one of " +
                                    "ChangeFeedProcessor#handleLatestVersionChanges or " +
                                    "ChangeFeedProcessor#handleAllVersionsAndDeletes were also started for " +
                                    "lease prefix : %s", this.changeFeedProcessorOptions.getLeasePrefix());

                                return Mono.error(new IllegalStateException(errorMessage));
                            }

                            return Mono.empty();
                        });

                    if (!this.changeFeedProcessorOptions.isLeaseVerificationEnabledOnRestart()) {
                        return validateExistingLeasesMono;
                    } else {
                        previousOperation = validateExistingLeasesMono;
                    }
                }

                logger.info("Acquire initialization lock");
                return previousOperation.then(
                    this.leaseStore.acquireInitializationLock(this.lockTime)
                    .flatMap(lockAcquired -> {
                        this.isLockAcquired = lockAcquired;

                        if (!this.isLockAcquired) {
                            logger.info("Another instance is initializing the store");
                            return Mono.just(isLockAcquired).delayElement(this.sleepTime, CosmosSchedulers.COSMOS_PARALLEL);
                        } else {
                            return this.synchronizer.createMissingLeases()
                                .then(!isInitialized
                                    ? this.leaseStore.markInitialized().flatMap((initSucceeded) -> Mono.just(lockAcquired))
                                    : Mono.just(lockAcquired));
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
                    }));
            })
            .repeat( () -> !this.isInitialized)
            .then();
    }
}
