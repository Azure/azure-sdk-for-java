// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.util.ServiceVersion;

/**
 *
 */
public enum AzureMonitorServiceVersion implements ServiceVersion {
    V_1("v1");

    String version;

    /**
     * @param version
     */
    AzureMonitorServiceVersion(String version) {
        this.version = version;
    }

    /**
     * @return
     */
    public static AzureMonitorServiceVersion getLatest() {
        return V_1;
    }

    @Override
    public String getVersion() {
        return version;
    }
}
