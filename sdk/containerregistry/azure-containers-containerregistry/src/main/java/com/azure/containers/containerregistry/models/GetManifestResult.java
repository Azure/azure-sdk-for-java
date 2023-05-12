// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.containers.containerregistry.models;

import com.azure.containers.containerregistry.implementation.ConstructorAccessors;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

/**
 * The result of getting a manifest from the registry.
 */
@Immutable
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
     * The manifest content retrieved from the service.
     * @return The associated manifest content.
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
