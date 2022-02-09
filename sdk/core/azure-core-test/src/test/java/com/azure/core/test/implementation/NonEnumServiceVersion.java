// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation;

import com.azure.core.util.ServiceVersion;

/**
 * This class represents an invalid implementation of {@link ServiceVersion} as the implementation isn't an {@link
 * Enum}.
 */
public final class NonEnumServiceVersion implements ServiceVersion {
    @Override
    public String getVersion() {
        return null;
    }

    /**
     * Gets the latest service version.
     *
     * @return The latest service version.
     */
    public static NonEnumServiceVersion getLatest() {
        return null;
    }
}
