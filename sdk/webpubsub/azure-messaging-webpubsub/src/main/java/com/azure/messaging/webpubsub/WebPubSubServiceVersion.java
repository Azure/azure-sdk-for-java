// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Web Pub Sub Service supported by this client library.
 */
public enum WebPubSubServiceVersion implements ServiceVersion {
    V2021_05_01_PREVIEW("2021-05-01-preview"),
    V2021_08_01_PREVIEW("2021-08-01-preview");

    private final String version;

    WebPubSubServiceVersion(final String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@code WebPubSubServiceVersion}
     */
    public static WebPubSubServiceVersion getLatest() {
        return V2021_08_01_PREVIEW;
    }
}
