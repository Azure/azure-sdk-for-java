// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Contains metadata about a request call.
 */
public final class CallMetadata {
    private volatile int firstCall = 1;
    private static final AtomicIntegerFieldUpdater<CallMetadata> FIRST_CALL_UPDATER
        = AtomicIntegerFieldUpdater.newUpdater(CallMetadata.class, "firstCall");

    private volatile int firstCallWithProxy = 0;
    private static final AtomicIntegerFieldUpdater<CallMetadata> FIRST_CALL_WITH_PROXY_UPDATER
        = AtomicIntegerFieldUpdater.newUpdater(CallMetadata.class, "firstCallWithProxy");

    /**
     * Compares and sets the first call property atomically.
     *
     * @param expected Expected value.
     * @param updated Updated value.
     * @return Whether the value was updated.
     */
    public boolean compareAndSetFirstCall(boolean expected, boolean updated) {
        return FIRST_CALL_UPDATER.compareAndSet(this, expected ? 1 : 0, updated ? 1 : 0);
    }

    /**
     * Compares and sets the first call with proxy property atomically.
     *
     * @param expected Expected value.
     * @param updated Updated value.
     * @return Whether the value was updated.
     */
    public boolean compareAndSetFirstCallWithProxy(boolean expected, boolean updated) {
        return FIRST_CALL_WITH_PROXY_UPDATER.compareAndSet(this, expected ? 1 : 0, updated ? 1 : 0);
    }

    /**
     * Sets the first call with proxy value.
     *
     * @param value The value.
     */
    public void setFirstCallWithProxy(boolean value) {
        FIRST_CALL_WITH_PROXY_UPDATER.set(this, value ? 1 : 0);
    }

    /**
     * Gets the first call with proxy value.
     *
     * @return First call with proxy value.
     */
    public boolean getFirstCallWithProxy() {
        return FIRST_CALL_WITH_PROXY_UPDATER.get(this) == 1;
    }
}
