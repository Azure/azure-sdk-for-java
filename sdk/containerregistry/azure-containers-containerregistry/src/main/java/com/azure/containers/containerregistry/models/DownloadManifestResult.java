// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.containers.containerregistry.models;

import com.azure.containers.containerregistry.implementation.ConstructorAccessors;
import com.azure.core.util.BinaryData;

/**
 * The result from downloading an OCI manifest from the registry.
 */
public class DownloadManifestResult {
    static {
        ConstructorAccessors.setDownloadManifestResultAccessor(DownloadManifestResult::new);
    }

    private final String digest;
    private OciManifest ociManifest;
    private final ManifestMediaType mediaType;
    private final BinaryData rawData;

    DownloadManifestResult(String digest, ManifestMediaType mediaType, BinaryData rawData) {
        this.digest = digest;
        this.mediaType = mediaType;
        this.rawData = rawData;
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
    public OciManifest asOciManifest() {
        if (ociManifest != null) {
            return ociManifest;
        }

        ociManifest = rawData.toObject(OciManifest.class);

        return ociManifest;
    }

    /**
     * The manifest stream that was downloaded.
     * @return The associated manifest stream.
     */
    public BinaryData getContent() {
        return this.rawData;
    }

    /**
     * Get manifest media type.
     * @return Instance of {@link ManifestMediaType}
     */
    public ManifestMediaType getMediaType() {
        return mediaType;
    }
}
