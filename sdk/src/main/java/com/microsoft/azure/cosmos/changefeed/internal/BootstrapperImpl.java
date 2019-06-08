/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmos.changefeed.internal;

import com.microsoft.azure.cosmos.changefeed.Bootstrapper;
import com.microsoft.azure.cosmos.changefeed.LeaseStore;
import com.microsoft.azure.cosmos.changefeed.PartitionSynchronizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implementation for the bootstrapping interface.
 */
public class BootstrapperImpl implements Bootstrapper {
    private final Logger logger = LoggerFactory.getLogger(BootstrapperImpl.class);
    private final PartitionSynchronizer synchronizer;
    private final LeaseStore leaseStore;
    private final Duration lockTime;
    private final Duration sleepTime;

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
    }

    @Override
    public Mono<Void> initialize() {
        BootstrapperImpl self = this;

        return Mono.fromRunnable( () -> {
            while (true) {
                boolean initialized = self.leaseStore.isInitialized().block();

                if (initialized) break;

                boolean isLockAcquired = self.leaseStore.acquireInitializationLock(self.lockTime).block();

                try {
                    if (!isLockAcquired) {
                        logger.info("Another instance is initializing the store");
                        try {
                            Thread.sleep(self.sleepTime.toMillis());
                        } catch (InterruptedException ex) {
                            logger.warn("Unexpected exception caught", ex);
                        }
                        continue;
                    }

                    logger.info("Initializing the store");
                    self.synchronizer.createMissingLeases().block();
                    self.leaseStore.markInitialized().block();

                } catch (RuntimeException ex) {
                    break;
                } finally {
                    if (isLockAcquired) {
                        self.leaseStore.releaseInitializationLock().block();
                    }
                }
            }

            logger.info("The store is initialized");
        });
    }
}
