// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.containers.containerregistry.models;

import com.azure.containers.containerregistry.implementation.ConstructorAccessors;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;

/**
 * The result from downloading an OCI manifest from the registry.
 */
public final class DownloadManifestResult {
    private static final ClientLogger LOGGER = new ClientLogger(DownloadManifestResult.class);
    static {
        ConstructorAccessors.setDownloadManifestResultAccessor(DownloadManifestResult::new);
    }

    private final String digest;
    private OciImageManifest ociManifest;
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
     * @return The {@link OciImageManifest} instance.
     * @throws IllegalStateException thrown when attempting to get {@link OciImageManifest} from incompatible media type.
     */
    public OciImageManifest asOciManifest() {
        if (ociManifest != null) {
            return ociManifest;
        }

        if (!ManifestMediaType.DOCKER_MANIFEST.equals(mediaType)
            && !ManifestMediaType.OCI_MANIFEST.equals(mediaType)) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                String.format("Cannot convert manifest with %s media type to OciImageManifest", mediaType)));
        }

        ociManifest = rawData.toObject(OciImageManifest.class);

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
    public ManifestMediaType getManifestMediaType() {
        return mediaType;
    }
}
