// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.containers.containerregistry.models;

import com.azure.containers.containerregistry.implementation.ConstructorAccessors;
import com.azure.core.util.BinaryData;

/**
 * The result from downloading an OCI manifest from the registry.
 */
public final class GetManifestResult {
    static {
        ConstructorAccessors.setGetManifestResultAccessor(GetManifestResult::new);
    }

    private final String digest;
    private final ManifestMediaType mediaType;
    private final BinaryData rawData;

    GetManifestResult(String digest, ManifestMediaType mediaType, BinaryData rawData) {
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
     * The manifest stream that was downloaded.
     * @return The associated manifest stream.
     */
    public BinaryData getManifest() {
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
