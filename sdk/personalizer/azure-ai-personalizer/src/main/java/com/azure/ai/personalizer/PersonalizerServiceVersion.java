// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Personalizer supported by this client library.
 */
public enum PersonalizerServiceVersion implements ServiceVersion {
    /**
     * Service version {@code v1.1-preview.3}.
     */
    V1_1_PREVIEW_3("v1.1-preview.3");

    private final String version;

    PersonalizerServiceVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link PersonalizerServiceVersion}
     */
    public static PersonalizerServiceVersion getLatest() {
        return V1_1_PREVIEW_3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return this.version;
    }
}
