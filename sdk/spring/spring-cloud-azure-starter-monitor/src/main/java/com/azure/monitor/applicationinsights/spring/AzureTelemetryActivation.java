// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

/**
 * Azure telemetry activation
 */
public final class AzureTelemetryActivation {

    /** a flag to indicate if AzureTelemetry is activated or not. */
    private final boolean activated;

    /**
     * Creates an instance of {@link AzureTelemetryActivation}.
     */
    public AzureTelemetryActivation() {
        this.activated = true; // We leave the AzureTelemetryActivation class because it could be used to provide the ability
        // to disable the starter features
    }

    /**
     * @return true if it's activated.
     */
    public boolean isTrue() {
        return activated;
    }

}
