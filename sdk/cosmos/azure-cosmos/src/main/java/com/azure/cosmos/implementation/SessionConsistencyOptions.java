// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

public class SessionConsistencyOptions {

    private final boolean partitionKeyScopedSessionCapturingEnabled;
    private final boolean sessionConsistencyDisabledForWrites;
    static final SessionConsistencyOptions DEFAULT_OPTIONS = new SessionConsistencyOptions(false, false);

    public SessionConsistencyOptions(boolean partitionKeyScopedSessionCapturingEnabled,
                                     boolean sessionConsistencyDisabledForWrites) {
        this.partitionKeyScopedSessionCapturingEnabled = partitionKeyScopedSessionCapturingEnabled;
        this.sessionConsistencyDisabledForWrites = sessionConsistencyDisabledForWrites;
    }

    public boolean isPartitionKeyScopedSessionCapturingEnabled() {
        return partitionKeyScopedSessionCapturingEnabled;
    }

    public boolean isSessionConsistencyDisabledForWrites() {
        return sessionConsistencyDisabledForWrites;
    }
}
