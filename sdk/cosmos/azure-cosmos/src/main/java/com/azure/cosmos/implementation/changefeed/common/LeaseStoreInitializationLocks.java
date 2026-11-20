// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.implementation.changefeed.LeaseStore;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Acquires namespace initialization locks in EPK then PK order without waiting while holding a lock.
 */
public final class LeaseStoreInitializationLocks {
    private final LeaseStore epkStore;
    private final LeaseStore pkStore;
    private final Duration lockTime;
    private boolean epkAcquired;
    private boolean pkAcquired;

    public LeaseStoreInitializationLocks(LeaseStore epkStore, LeaseStore pkStore, Duration lockTime) {
        this.epkStore = epkStore;
        this.pkStore = pkStore;
        this.lockTime = lockTime;
    }

    public Mono<Boolean> acquire() {
        return Mono.defer(() -> this.epkStore.acquireInitializationLock(this.lockTime))
            .flatMap(acquired -> {
                this.epkAcquired = acquired;
                if (!acquired) {
                    return Mono.just(false);
                }
                return this.pkStore.acquireInitializationLock(this.lockTime)
                    .flatMap(pkAcquired -> {
                        this.pkAcquired = pkAcquired;
                        return pkAcquired ? Mono.just(true) : this.release().thenReturn(false);
                    });
            })
            .onErrorResume(error -> this.release().then(Mono.error(error)));
    }

    public Mono<Boolean> release() {
        return Mono.defer(() -> {
            Mono<Boolean> releasePk = this.pkAcquired
                ? Mono.defer(this.pkStore::releaseInitializationLock) : Mono.just(false);
            Mono<Boolean> releaseEpk = this.epkAcquired
                ? Mono.defer(this.epkStore::releaseInitializationLock) : Mono.just(false);
            this.pkAcquired = false;
            this.epkAcquired = false;
            return releasePk.onErrorResume(error -> releaseEpk.then(Mono.error(error))).then(releaseEpk);
        });
    }

    public static final class ConfigurationMismatchException extends IllegalStateException {
        private static final long serialVersionUID = 1L;

        public ConfigurationMismatchException(String message) {
            super(message);
        }
    }
}
