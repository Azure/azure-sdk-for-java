// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.changefeed.Bootstrapper;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseStore;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.LeaseVersion;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Implementation for the bootstrapping interface.
 */
class BootstrapperImpl implements Bootstrapper {
    private final Logger logger = LoggerFactory.getLogger(BootstrapperImpl.class);
    private final PartitionSynchronizer synchronizer;
    private final LeaseStore leaseStore;
    private final LeaseStoreManager epkRangeVersionLeaseStoreManager;
    private final LeaseStoreManager pkRangeVersionLeaseStoreManager;
    private final ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private final ChangeFeedMode changeFeedModeToStart;
    private final Duration lockTime;
    private final Duration sleepTime;

    private volatile boolean isInitialized;
    private volatile boolean isLockAcquired;

    public BootstrapperImpl(
        PartitionSynchronizer synchronizer,
        LeaseStore leaseStore,
        Duration lockTime,
        Duration sleepTime,
        LeaseStoreManager epkRangeVersionLeaseStoreManager,
        LeaseStoreManager pkRangeVersionLeaseStoreManager,
        ChangeFeedProcessorOptions changeFeedProcessorOptions,
        ChangeFeedMode changeFeedModeToStart) {

        checkNotNull(synchronizer, "Argument 'synchronizer' can not be null");
        checkNotNull(leaseStore, "Argument 'leaseStore' can not be null");
        checkNotNull(epkRangeVersionLeaseStoreManager, "Argument 'epkRangeVersionLeaseStoreManager' can not be null");
        checkNotNull(pkRangeVersionLeaseStoreManager, "Argument 'pkRangeVersionLeaseStoreManager' can not be null");
        checkNotNull(changeFeedProcessorOptions, "Argument 'changeFeedProcessorOptions' can not be null");
        checkNotNull(changeFeedModeToStart, "Argument 'changeFeedModeToStart' can not be null");

        checkArgument(lockTime != null && this.isPositive(lockTime), "lockTime should be non-null and positive");
        checkArgument(sleepTime != null && this.isPositive(sleepTime), "sleepTime should be non-null and positive");

        this.synchronizer = synchronizer;
        this.leaseStore = leaseStore;
        this.epkRangeVersionLeaseStoreManager = epkRangeVersionLeaseStoreManager;
        this.pkRangeVersionLeaseStoreManager = pkRangeVersionLeaseStoreManager;
        this.changeFeedProcessorOptions = changeFeedProcessorOptions;
        this.changeFeedModeToStart = changeFeedModeToStart;
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
                Mono<Void> previousOperation = Mono.empty();
                if (initialized) {
                    if (!this.changeFeedProcessorOptions.isLeaseVerificationEnabledOnRestart()) {
                        return this.validateLeaseCFModeInteroperabilityForEpkRangeBasedLease();
                    } else {
                        previousOperation = this.validateLeaseCFModeInteroperabilityForEpkRangeBasedLease();
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
                    })
                );
            })
            .repeat(() -> !this.isInitialized)
            .then();
    }

    private Mono<Void> validateLeaseCFModeInteroperabilityForEpkRangeBasedLease() {

        // fetch pk-range based lease first (all versions and deletes is bootstrapping)
        return this.pkRangeVersionLeaseStoreManager.getTopLeases(1).next()
            .flatMap(lease -> {

                    if (lease.getVersion() == LeaseVersion.PARTITION_KEY_BASED_LEASE) {

                        String errorMessage = String.format("ChangeFeedProcessor#handleAllVersionsAndDeletes cannot be invoked when" +
                            "ChangeFeedProcessor#handleChanges was also started for" +
                            "lease prefix : %s", this.changeFeedProcessorOptions.getLeasePrefix());

                        return Mono.error(new IllegalStateException(errorMessage));
                    }

                    return Mono.empty();
                }
            )
            // if no pk-range based lease exists, try fetching epk-range based lease
            .switchIfEmpty(this.epkRangeVersionLeaseStoreManager.getTopLeases(1).next())
            // type is known from upstream so doing an explicit cast
            .flatMap(epkRangeVersionLease -> Mono.just((Lease) epkRangeVersionLease))
            .flatMap(lease -> {

                if (lease.getVersion() == LeaseVersion.EPK_RANGE_BASED_LEASE) {
                    if (!Strings.isNullOrEmpty(lease.getId())) {

                        if (!Strings.isNullOrEmpty(lease.getContinuationToken())) {
                            ChangeFeedState changeFeedState = ChangeFeedState.fromString(lease.getContinuationToken());

                            if (changeFeedState.getMode() != this.changeFeedModeToStart) {

                                String errorMessage = String.format("ChangeFeedProcessor#handleAllVersionsAndDeletes cannot be invoked when " +
                                    "ChangeFeedProcessor#handleLatestVersionChanges were also started for " +
                                    "lease prefix : %s", this.changeFeedProcessorOptions.getLeasePrefix());

                                return Mono.error(new IllegalStateException(errorMessage));
                            }
                        }
                    }
                }

                return Mono.empty();
            });
    }
}
