// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.http.ServiceVersion;

/**
 * The versions of Azure App Configuration supported by this client library.
 */
public enum QueueServiceVersion implements ServiceVersion {
    V2018_03_28("2018-03-28");

    private final String version;

    QueueServiceVersion(String version) {
        this.version = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersionString() {
        return this.version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link QueueServiceVersion}
     */
    public static QueueServiceVersion getLatest() {
        return V2018_03_28;
    }
}
