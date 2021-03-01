// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.deviceprovisioning;

import com.azure.core.util.ServiceVersion;

/**
 * The service API versions of Device Provisioning Service that are supported by this client.
 */
public enum DeviceProvisioningServiceVersion implements ServiceVersion {
    V2021_02_01("2021-02-01-preview");

    private final String version;

    DeviceProvisioningServiceVersion(String version) {
        this.version = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the latest service API version of Device Provisioning Service that is supported by this client.
     * @return The latest service API version of Device Provisioning Service that is supported by this client.
     */
    public static DeviceProvisioningServiceVersion getLatest() {
        return V2021_02_01;
    }
}
