// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Service Bus supported by this client library.
 */
public enum ServiceBusServiceVersion implements ServiceVersion {
    V2017_04("2017-04");

    private final String version;

    /**
     * Creates a new instance with the specified version.
     * @param version The version of the service.
     */
    ServiceBusServiceVersion(String version) {
        this.version = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link ServiceBusServiceVersion}.
     */
    public static ServiceBusServiceVersion getLatest() {
        return V2017_04;
    }
}
