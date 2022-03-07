// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.containers.containerregistry.models;

import com.azure.core.util.BinaryData;

/**
 * The result from downloading an OCI manifest from the registry.
 */
public class DownloadManifestResult {
    private final String digest;
    private final OciManifest manifest;
    private final BinaryData manifestStream;

    /**
     * Instantiate an instance of the DownloadManifestResult object.
     * @param digest The digest of the manifest.
     * @param manifest The OCIManifest object.
     * @param manifestStream The manifest stream.
     */
    public DownloadManifestResult(String digest, OciManifest manifest, BinaryData manifestStream) {
        this.digest = digest;
        this.manifest = manifest;
        this.manifestStream = manifestStream;
    }

    /**
     * The manifest's digest, calculated by the registry.
     * @return The digest.
     */
    public String getDigest() {
        return this.digest;
    }

    /**
     * The OCI manifest that was downloaded.
     * @return The OCIManifest object.
     */
    public OciManifest getManifest() {
        return this.manifest;
    }

    /**
     * The manifest stream that was downloaded.
     * @return The associated manifest stream.
     */
    public BinaryData getManifestStream() {
        return this.manifestStream;
    }
}
