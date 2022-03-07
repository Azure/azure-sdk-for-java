// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

/**
 * Options for configuring the upload manifest operation.
 */
public final class UploadManifestOptions {
    private String tag;

    /**
     * Instantiate an instance of upload manifest options with the tag information.
     */
    public UploadManifestOptions() { }

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
}
