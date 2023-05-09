// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.changefeed.Bootstrapper;
import com.azure.cosmos.implementation.changefeed.LeaseStore;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/***
 * In order to support merge (which requires epk version leases) and align with long term designs,
 * we have introduced a new API {@code ChangeFeedProcessorBuilder.handleLatestVersionChanges}, which internally use epk version leases.
 * Semantically it is the same for {@code ChangeFeedProcessorBuilder.handleChanges} which uses pkRangeId version leases.
 *
 * When customer onboard to the new API {@code ChangeFeedProcessorBuilder.handleLatestVersionChanges}, for a better experience,
 * we are going to bootstrap from the pkRangeId version lease store if it has been initialized
 * or initialize the pkRangeId version lease store to prevent it being initialized again through the old API {@code ChangeFeedProcessorBuilder.handleChanges}.
 */
public class PkRangeIdVersionLeaseStoreBootstrapperImpl implements Bootstrapper {
    private final Logger logger = LoggerFactory.getLogger(PkRangeIdVersionLeaseStoreBootstrapperImpl.class);
    private final PartitionSynchronizer synchronizer;
    private final LeaseStore leaseStore;
    private final LeaseStoreManager pkRangeIdVersionLeaseStoreManager;
    private final Duration lockTime;
    private final Duration sleepTime;

    private volatile boolean isInitialized;
    private volatile boolean isLockAcquired;
    private volatile boolean isPkRangeIdVersionLeaseStoreLockAcquired;

    public PkRangeIdVersionLeaseStoreBootstrapperImpl(
        PartitionSynchronizer synchronizer,
        LeaseStore leaseStore,
        Duration lockTime,
        Duration sleepTime,
        LeaseStoreManager pkRangeIdVersionLeaseStoreManager) {
        checkNotNull(synchronizer, "Argument 'synchronizer' can not be null");
        checkNotNull(leaseStore, "Argument 'leaseStore' can not be null");
        checkArgument(lockTime != null && this.isPositive(lockTime), "lockTime should be non-null and positive");
        checkArgument(sleepTime != null && this.isPositive(sleepTime), "sleepTime should be non-null and positive");
        checkArgument(
            pkRangeIdVersionLeaseStoreManager != null,
            "Argument 'pkRangeIdVersionLeaseStoreManager' should not be null");

        this.synchronizer = synchronizer;
        this.leaseStore = leaseStore;
        this.pkRangeIdVersionLeaseStoreManager = pkRangeIdVersionLeaseStoreManager;
        this.lockTime = lockTime;
        this.sleepTime = sleepTime;

        this.isInitialized = false;
    }

    private boolean isPositive(Duration duration) {
        return !duration.isNegative() && !duration.isZero();
    }

    @Override
    public Mono<Void> initialize() {
        this.isInitialized = false;

        return Mono.just(this)
            .flatMap(value -> this.leaseStore.isInitialized())
            .flatMap(initialized -> {
                this.isInitialized = initialized;

                if (initialized) {
                    return Mono.empty();
                } else {
                    logger.info("Acquire initialization lock");
                    return this.acquireInitializationLock()
                        .flatMap(lockAcquired -> {

                            if (!lockAcquired) {
                                logger.info("Another instance is initializing the store");
                                return Mono.just(isLockAcquired).delayElement(this.sleepTime, CosmosSchedulers.COSMOS_PARALLEL);
                            } else {
                                return this.pkRangeIdVersionLeaseStoreManager.isInitialized()
                                    .flatMap(pkRangeIdVersionLeaseStoreInitialized -> {
                                        if (pkRangeIdVersionLeaseStoreInitialized) {
                                           return this.bootstrapFromPkRangeVersionLeases();
                                        } else {
                                           return this.bootstrapFromScratch();
                                        }
                                    });
                            }
                        })
                        .onErrorResume(throwable -> {
                            logger.warn("Unexpected exception caught while initializing the lock", throwable);
                            return Mono.just(this.isLockAcquired);
                        })
                        .flatMap(lockAcquired -> {
                            if (this.isLockAcquired) {
                                return this.leaseStore.releaseInitializationLock()
                                    .then(this.releasePkRangeIdVersionLeaseStoreLock());
                            }

                            return Mono.just(lockAcquired);
                        });
                }
            })
            .repeat(() -> !this.isInitialized)
            .then();
    }

    private Mono<Boolean> releasePkRangeIdVersionLeaseStoreLock() {
        if (this.isPkRangeIdVersionLeaseStoreLockAcquired) {
            return this.pkRangeIdVersionLeaseStoreManager.releaseInitializationLock();
        }

        return Mono.just(Boolean.FALSE);
    }

    private Mono<Boolean> bootstrapFromPkRangeVersionLeases() {
        return this.pkRangeIdVersionLeaseStoreManager.getAllLeases()
            .collectList()
            .flatMap(pkRangeIdVersionLeases -> {
                return this.synchronizer.createMissingLeases(pkRangeIdVersionLeases)
                    .thenReturn(pkRangeIdVersionLeases);
            })
            .flatMap(pkRangeIdVersionLeases -> {
                return this.leaseStore.markInitialized()
                    .flatMap(isInitialized -> {
                        if (isInitialized) {
                            return this.pkRangeIdVersionLeaseStoreManager.deleteAll(pkRangeIdVersionLeases);
                        }
                        return Mono.empty();
                    })
                    .thenReturn(this.isLockAcquired);

            });
    }

    private Mono<Boolean> bootstrapFromScratch() {
        return this.synchronizer.createMissingLeases()
            .thenReturn(this)
            .flatMap(bootstrapper -> this.leaseStore.markInitialized())
            .flatMap(leaseStoreInitialized -> {
                if (leaseStoreInitialized) {
                    return this.pkRangeIdVersionLeaseStoreManager.markInitialized();
                }

                return Mono.empty();
            })
            .thenReturn(this.isLockAcquired);
    }

    private Mono<Boolean> acquireInitializationLock() {
        return this.leaseStore.acquireInitializationLock(this.lockTime)
            .flatMap(isLockAcquired -> {
                this.isLockAcquired = isLockAcquired;

                if (!isLockAcquired) {
                    return Mono.just(Boolean.FALSE);
                } else {
                    return this.pkRangeIdVersionLeaseStoreManager.isInitialized()
                        .flatMap(pkRangeIdVersionLeaseStoreInitialized -> {
                            if (pkRangeIdVersionLeaseStoreInitialized) {
                                return Mono.just(Boolean.TRUE);
                            } else {
                                return this.pkRangeIdVersionLeaseStoreManager.acquireInitializationLock(this.lockTime)
                                    .flatMap(pkRangeIdVersionLeaseStoreLockAcquired -> {
                                        this.isPkRangeIdVersionLeaseStoreLockAcquired = pkRangeIdVersionLeaseStoreLockAcquired;

                                        return Mono.just(this.isPkRangeIdVersionLeaseStoreLockAcquired);
                                    });
                            }
                        });
                }
            });
    }
}
