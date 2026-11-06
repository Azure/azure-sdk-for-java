// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.changefeed.Bootstrapper;
import com.azure.cosmos.implementation.changefeed.LeaseStore;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import com.azure.cosmos.implementation.changefeed.common.LeaseVersion;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedQueryFilter;
import com.azure.cosmos.implementation.changefeed.common.LeaseStoreInitializationLocks;
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
    private final Duration sleepTime;
    private final LeaseStoreManager epkRangeVersionLeaseStoreManager;
    private final ChangeFeedProcessorOptions changeFeedProcessorOptions;

    private volatile boolean isInitialized;
    private volatile boolean isLockAcquired;
    private final LeaseStoreInitializationLocks initializationLocks;

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
        this.sleepTime = sleepTime;
        this.initializationLocks = new LeaseStoreInitializationLocks(epkRangeVersionLeaseStoreManager, leaseStore, lockTime);
        this.epkRangeVersionLeaseStoreManager = epkRangeVersionLeaseStoreManager;
        this.changeFeedProcessorOptions = changeFeedProcessorOptions;

        this.isInitialized = false;
    }

    @Override
    public Mono<Void> initialize() {
        this.isInitialized = false;

        return Mono.just(this)
            .flatMap(value -> this.leaseStore.isInitialized())
            .flatMap(initialized -> this.epkRangeVersionLeaseStoreManager.isInitialized().thenReturn(initialized))
            .flatMap(initialized -> this.epkRangeVersionLeaseStoreManager.getAllLeases().then().thenReturn(initialized))
            .flatMap(initialized -> {
                this.isInitialized = initialized;
                Mono<Void> previousOperation = Mono.empty();
                if (initialized) {
                    Mono<Void> validateExistingLeasesMono = this.validateAuxiliaryLeaseMode();

                    if (!this.changeFeedProcessorOptions.isLeaseVerificationEnabledOnRestart()) {
                        return validateExistingLeasesMono;
                    } else {
                        previousOperation = validateExistingLeasesMono;
                    }
                }

                logger.info("Acquire initialization lock");
                return previousOperation.then(
                    this.initializationLocks.acquire()
                    .flatMap(lockAcquired -> {
                        this.isLockAcquired = lockAcquired;

                        if (!this.isLockAcquired) {
                            logger.info("Another instance is initializing the store");
                            return Mono.just(isLockAcquired).delayElement(this.sleepTime, CosmosSchedulers.COSMOS_PARALLEL);
                        } else {
                            return this.leaseStore.isInitialized()
                                .flatMap(currentlyInitialized -> {
                                    this.isInitialized = currentlyInitialized;
                                    return this.epkRangeVersionLeaseStoreManager.isInitialized();
                                })
                                .then(Mono.defer(() -> this.epkRangeVersionLeaseStoreManager.getAllLeases().then()))
                                .then(Mono.defer(() -> this.isInitialized ? this.validateAuxiliaryLeaseMode() : Mono.empty()))
                                .then(Mono.defer(() -> this.synchronizer.createMissingLeases()))
                                .then(Mono.defer(() -> !this.isInitialized
                                    ? this.leaseStore.markInitialized().thenReturn(lockAcquired)
                                    : Mono.just(lockAcquired)));
                        }
                    })
                    .onErrorResume(throwable -> {
                        if (throwable instanceof ChangeFeedQueryFilter.QueryFilterMismatchException
                            || throwable instanceof LeaseStoreInitializationLocks.ConfigurationMismatchException) {
                            return this.initializationLocks.release()
                                .then(Mono.error(throwable));
                        }
                        logger.warn("Unexpected exception caught while initializing the lock", throwable);
                        return Mono.just(this.isLockAcquired);
                    })
                    .flatMap(lockAcquired -> {
                        if (this.isLockAcquired) {
                            return this.initializationLocks.release();
                        }
                        return Mono.just(lockAcquired);
                    }));
            })
            .repeat( () -> !this.isInitialized)
            .then();
    }

    private Mono<Void> validateAuxiliaryLeaseMode() {
        return this.epkRangeVersionLeaseStoreManager.getTopLeases(1).next()
            .flatMap(lease -> {
                if (lease.getVersion() == LeaseVersion.EPK_RANGE_BASED_LEASE) {
                    return Mono.error(new LeaseStoreInitializationLocks.ConfigurationMismatchException(String.format(
                        "ChangeFeedProcessor#handleChanges cannot be invoked when one of "
                            + "ChangeFeedProcessor#handleLatestVersionChanges or "
                            + "ChangeFeedProcessor#handleAllVersionsAndDeletes were also started for "
                            + "lease prefix : %s", this.changeFeedProcessorOptions.getLeasePrefix())));
                }
                return Mono.empty();
            });
    }
}
