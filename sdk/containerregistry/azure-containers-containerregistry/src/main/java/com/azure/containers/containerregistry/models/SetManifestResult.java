// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

import com.azure.containers.containerregistry.implementation.ConstructorAccessors;

/**
 * The result from uploading a manifest.
 */
public final class SetManifestResult {
    static {
        ConstructorAccessors.setSetManifestResultAccessor(SetManifestResult::new);
    }
    private final String digest;

    /**
     * Initializes upload manifest result object.
     * @param digest The digest associated with the manifest.
     */
    private SetManifestResult(String digest) {
        this.digest = digest;
    }

    /**
     * Get the digest associated with the upload manifest operation.
     * @return The digest.
     */
    public String getDigest() {
        return this.digest;
    }
}
