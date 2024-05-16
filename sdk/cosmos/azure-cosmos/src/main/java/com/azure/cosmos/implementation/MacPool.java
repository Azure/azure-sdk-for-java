// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

class MacPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(MacPool.class);
    final Mac macInstance;
    final ConcurrentLinkedQueue<Mac> pool;
    final AtomicBoolean isMacInstanceCloneable = new AtomicBoolean(true);
    final Supplier<Mac> macProvider;

    public MacPool(Supplier<Mac> macProvider) {
        if (macProvider == null) {
            throw new IllegalArgumentException("macProvider");
        }

        this.macProvider = macProvider;
        this.macInstance = macProvider.get();
        this.pool = new ConcurrentLinkedQueue<>();
    }

    public ReUsableMac take() {
        Mac cachedInstance = pool.poll();
        if (cachedInstance == null) {
            cachedInstance = this.createNewMac();
        }

        return new ReUsableMac(cachedInstance, this);
    }

    private Mac createNewMac() {
        if (!this.isMacInstanceCloneable.get()) {
            return this.macProvider.get();
        }

        // Cloning teh rootMac is the fastest option
        // But while Mac implements Cloneable interface not every MacSpi is required to support cloning
        // If it doesn't  - bad for performance - but functionally correct and necessary
        // - see https://docs.oracle.com/javase/7/docs/technotes/guides/security/crypto/HowToImplAProvider.html#Step11
        // - see https://docs.oracle.com/en/java/javase/18/docs/api/java.base/javax/crypto/Mac.html#clone()
        try {
            return (Mac) this.macInstance.clone();
        } catch (CloneNotSupportedException e) {

            LOGGER.warn(
                "Cloning for the {} algorithm with provider {} ({}) not possible - this will " +
                    "result in less than ideal performance.",
                this.macInstance.getAlgorithm(),
                this.macInstance.getProvider().toString(),
                this.macInstance.getProvider().getInfo());

            this.isMacInstanceCloneable.set(false);

            return this.macProvider.get();
        }
    }

    public void give(ReUsableMac closableMac) {
        this.pool.add(closableMac.macInstance);
    }

    /*
     * Closable contract forces to add the Throws Exception contract unnecessarily
     */
    static class ReUsableMac {
        final Mac macInstance;
        final MacPool pool;

        public ReUsableMac(Mac macInstance, MacPool pool) {
            this.macInstance = macInstance;
            this.pool = pool;
        }

        public Mac get() {
            return this.macInstance;
        }

        public void close() {
            pool.give(this);
        }
    }
}
