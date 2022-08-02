// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Contains metadata about a request call.
 */
public final class CallMetadata {
    private volatile Boolean firstCall = true;
    private static final AtomicReferenceFieldUpdater<CallMetadata, Boolean> FIRST_CALL_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(CallMetadata.class, Boolean.class, "firstCall");

    private volatile Boolean firstCallWithProxy = false;
    private static final AtomicReferenceFieldUpdater<CallMetadata, Boolean> FIRST_CALL_WITH_PROXY_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(CallMetadata.class, Boolean.class, "firstCallWithProxy");

    /**
     * Compares and sets the first call property atomically.
     *
     * @param expected Expected value.
     * @param updated Updated value.
     * @return Whether the value was updated.
     */
    public boolean compareAndSetFirstCall(boolean expected, boolean updated) {
        return FIRST_CALL_UPDATER.compareAndSet(this, expected, updated);
    }

    /**
     * Compares and sets the first call with proxy property atomically.
     *
     * @param expected Expected value.
     * @param updated Updated value.
     * @return Whether the value was updated.
     */
    public boolean compareAndSetFirstCallWithProxy(boolean expected, boolean updated) {
        return FIRST_CALL_WITH_PROXY_UPDATER.compareAndSet(this, expected, updated);
    }

    /**
     * Sets the first call with proxy value.
     *
     * @param value The value.
     */
    public void setFirstCallWithProxy(boolean value) {
        FIRST_CALL_WITH_PROXY_UPDATER.set(this, value);
    }

    /**
     * Gets the first call with proxy value.
     *
     * @return First call with proxy value.
     */
    public boolean getFirstCallWithProxy() {
        return FIRST_CALL_WITH_PROXY_UPDATER.get(this);
    }
}
