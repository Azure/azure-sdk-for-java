// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

public class WriteRetryPolicy {
    public static final WriteRetryPolicy DISABLED = new WriteRetryPolicy(false, false);
    private final boolean enabled;
    private final boolean useTrackingIds;

    public WriteRetryPolicy(boolean enabled, boolean useTrackingIds) {
        this.enabled = enabled;
        this.useTrackingIds = useTrackingIds;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean useTrackingIdProperty() {
        return this.useTrackingIds;
    }

    @Override
    public String toString() {
        if (!this.enabled) {
            return "NoRetries";
        }

        if (!this.useTrackingIds) {
            return "No TrackingIds";
        }

        return "WithTrackingIds";
    }
}
