// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

/**
 * The result from uploading a manifest.
 */
public final class UploadManifestResult {
    private final String digest;

    /**
     * Initializes upload manifest result object.
     * @param digest The digest associated with the manifest.
     */
    public UploadManifestResult(String digest) {
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
