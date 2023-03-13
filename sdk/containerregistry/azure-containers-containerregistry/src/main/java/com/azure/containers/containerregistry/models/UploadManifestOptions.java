// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

import com.azure.core.util.BinaryData;

import java.util.Objects;

/**
 * Options for configuring the upload manifest operation.
 */
public final class UploadManifestOptions {
    private final ManifestMediaType mediaType;
    private final BinaryData manifest;
    private String tag;

    /**
     * Instantiate an instance of upload manifest options with the ocimanifest information.
     * @param ociManifest The Oci manifest.
     */
    public UploadManifestOptions(OciImageManifest ociManifest) {
        Objects.requireNonNull(ociManifest, "'ociManifest' can't be null.");
        this.manifest = BinaryData.fromObject(ociManifest);
        this.mediaType = ManifestMediaType.OCI_MANIFEST;
    }

    /**
     * Instantiate an instance of upload manifest options with the manifest information.
     * @param manifest The manifest that needs to be uploaded.
     * @param manifestMediaType The media type of supplied manifest.
     */
    public UploadManifestOptions(BinaryData manifest, ManifestMediaType manifestMediaType) {
        Objects.requireNonNull(manifest, "'manifest' can't be null.");
        Objects.requireNonNull(manifestMediaType, "'manifestMediaType' can't be null.");
        this.manifest = manifest;
        this.mediaType = manifestMediaType;
    }

    /**
     * A tag to assign to the artifact represented by this manifest.
     * @param tag The tag of the manifest.
     * @return The UploadManifestOptions object.
     */
    public UploadManifestOptions setTag(String tag) {
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
     * The manifest to be uploaded.
     * @return The BinaryData representing the manifest.
     */
    public BinaryData getManifest() {
        return this.manifest;
    }

    /**
     * Media type of the corresponding manifest.
     * @return instance of {@link ManifestMediaType}.
     */
    public ManifestMediaType getMediaType() {
        return mediaType;
    }
}
