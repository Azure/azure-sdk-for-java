// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

public class SessionConsistencyOptionsBuilder {

    private boolean partitionKeyScopedSessionCapturingEnabled;
    private boolean sessionConsistencyDisabledForWrites;

    public SessionConsistencyOptionsBuilder partitionKeyScopedSessionCapturingEnabled(boolean partitionKeyScopedSessionCapturingEnabled) {
        this.partitionKeyScopedSessionCapturingEnabled = partitionKeyScopedSessionCapturingEnabled;
        return this;
    }

    public SessionConsistencyOptionsBuilder sessionConsistencyDisabledForWrites(boolean sessionConsistencyDisabledForWrites) {
        this.sessionConsistencyDisabledForWrites = sessionConsistencyDisabledForWrites;
        return this;
    }

    public SessionConsistencyOptions build() {
        return new SessionConsistencyOptions(this.partitionKeyScopedSessionCapturingEnabled, this.sessionConsistencyDisabledForWrites);
    }
}
