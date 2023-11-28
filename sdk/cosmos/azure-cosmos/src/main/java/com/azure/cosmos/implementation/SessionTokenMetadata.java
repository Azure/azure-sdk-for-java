// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

public class SessionTokenMetadata {

    private final ISessionToken sessionToken;

    private AtomicReference<Instant> lastAccessedTimestamp;

    private final String pkRangeId;

    public SessionTokenMetadata(ISessionToken sessionToken, String pkRangeId) {
        this.sessionToken = sessionToken;
        this.lastAccessedTimestamp = new AtomicReference<>(Instant.now());
        this.pkRangeId = pkRangeId;
    }

    public ISessionToken getSessionToken() {
        return sessionToken;
    }

    public Instant getLastAccessedTimestamp() {
        return lastAccessedTimestamp.get();
    }

    public void setLastAccessedTimestamp(Instant instant) {
        this.lastAccessedTimestamp.set(instant);
    }

    public String getPkRangeId() {
        return pkRangeId;
    }
}
