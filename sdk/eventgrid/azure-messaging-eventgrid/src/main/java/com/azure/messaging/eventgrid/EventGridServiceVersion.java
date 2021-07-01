// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.util.ServiceVersion;

/**
 * An enum defining the available service versions for the Event Grid service. Note currently only
 * one service version, {@code 2018_01_01} is supported.
 */
public enum EventGridServiceVersion implements ServiceVersion {

    V2018_01_01("2018-01-01");

    private String version;

    EventGridServiceVersion(String version) {
        this.version = version;
    }

    /**
     * Get the version string for this particular service version instance.
     * @return the version string corresponding to this service version.
     */
    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Gets the latest supported service version.
     * @return the latest supported service version.
     */
    public static EventGridServiceVersion getLatest() {
        return V2018_01_01;
    }
}
