// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import javax.crypto.Mac;
import java.util.concurrent.ConcurrentLinkedQueue;

class MacPool {
    final Mac macInstance;
    final ConcurrentLinkedQueue<Mac> pool;

    public MacPool(Mac rootMac) {
        if (rootMac == null) {
            throw new IllegalArgumentException("rootMac");
        }

        this.macInstance = rootMac;
        this.pool = new ConcurrentLinkedQueue<>();
    }

    public ReUsableMac take() {
        try {
            Mac cachedInstance = pool.poll();
            if (cachedInstance == null) {
                cachedInstance = (Mac) this.macInstance.clone();
            }

            return new ReUsableMac(cachedInstance, this);
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
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
