// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of the Azure Mixed Reality STS supported by this client library.
 */
public enum MixedRealityStsServiceVersion implements ServiceVersion {
    V2019_02_28_PREVIEW("2019-02-28-preview");

    private final String version;

    MixedRealityStsServiceVersion(String version) {
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
     * Gets the latest service version supported by this client library.
     *
     * @return the latest {@link MixedRealityStsServiceVersion}
     */
    public static MixedRealityStsServiceVersion getLatest() {
        return V2019_02_28_PREVIEW;
    }
}
