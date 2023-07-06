// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

public final class AzureTelemetryActivation {

    private final boolean activated;

    public AzureTelemetryActivation() {
        this.activated = true; // We leave the AzureTelemetryActivation class because it could be used to provide the ability
        // to disable the starter features
    }

    public boolean isTrue() {
        return activated;
    }

}
