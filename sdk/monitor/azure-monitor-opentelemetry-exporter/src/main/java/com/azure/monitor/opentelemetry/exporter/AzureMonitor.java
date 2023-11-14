// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

/**
 * A convenient class to instantiate an instance of {@link AzureMonitorInstaller}
 */
public final class AzureMonitor {

    /**
     * Construct an intance of {@link AzureMonitorInstaller}
     * @return a new {@link AzureMonitorInstaller} object
     */
    public static AzureMonitorInstaller installer() {
        return new AzureMonitorInstaller();
    }

    private AzureMonitor() {}
}
