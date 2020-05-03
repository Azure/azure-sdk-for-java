// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.signalr;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure SignalR Service supported by this client library.
 */
public enum SignalRServiceVersion implements ServiceVersion {
    V1_0("1.0");

    private final String version;

    SignalRServiceVersion(final String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@code SignalRServiceVersion}
     */
    public static SignalRServiceVersion getLatest() {
        return V1_0;
    }
}
