// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.BinaryData;

import java.util.Objects;

/**
 * Set manifest options.
 */
@Fluent
public final class SetManifestOptions {
    private final ManifestMediaType mediaType;
    private final BinaryData manifest;
    private String tag;

    /**
     * Creates new instance of {@link SetManifestOptions}
     * @param ociImageManifest Instance of {@link OciImageManifest} to be set on the service.
     */
    public SetManifestOptions(OciImageManifest ociImageManifest) {
        Objects.requireNonNull(ociImageManifest, "'ociManifest' can't be null.");
        this.manifest = BinaryData.fromObject(ociImageManifest);
        this.mediaType = ManifestMediaType.OCI_MANIFEST;
    }

    /**
     * Creates new instance of {@link SetManifestOptions}
     * @param manifest The manifest to set.
     * @param manifestMediaType The media type of supplied manifest.
     */
    public SetManifestOptions(BinaryData manifest, ManifestMediaType manifestMediaType) {
        Objects.requireNonNull(manifest, "'manifest' can't be null.");
        Objects.requireNonNull(manifestMediaType, "'manifestMediaType' can't be null.");
        this.manifest = manifest;
        this.mediaType = manifestMediaType;
    }

    /**
     * A tag to assign to the artifact represented by this manifest.
     * @param tag Tag to be set on the manifest when sending it.
     * @return The {@link SetManifestOptions} object for chaining.
     */
    public SetManifestOptions setTag(String tag) {
        this.tag = tag;
        return this;
    }

    /**
     * The tag assigned to the artifact represented by this manifest.
     * @return The tag of the manifest.
     */
    public String getTag() {
        return this.tag;
    }

    /**
     * The manifest to be sent to the service.
     * @return The {@link BinaryData} representing the manifest.
     */
    public BinaryData getManifest() {
        return this.manifest;
    }

    /**
     * Media type of the corresponding manifest.
     * @return instance of {@link ManifestMediaType}.
     */
    public ManifestMediaType getManifestMediaType() {
        return mediaType;
    }
}
