// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

/**
 * Azure Azure Spring Monitor activation
 */
public final class AzureSpringMonitorActivation {

    /** a flag to indicate if Azure Spring Monitor is activated or not. */
    private final boolean activated;

    /**
     * Creates an instance of {@link AzureSpringMonitorActivation}.
     */
    public AzureSpringMonitorActivation() {
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
