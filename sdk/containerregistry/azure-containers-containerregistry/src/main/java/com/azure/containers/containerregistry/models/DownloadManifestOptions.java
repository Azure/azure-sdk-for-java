// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

import java.util.Objects;

/**
 * Options for configuring the download manifest operation.
 */
public final class DownloadManifestOptions {
    private final String tag;
    private final String digest;

    private DownloadManifestOptions(String tag, String digest) {
        this.tag = tag;
        this.digest = digest;
    }

    /**
     * Instantiate the options class with tag.
     * @param tag The tag associated with the manifest.
     * @return The DownloadManifestOptions object.
     */
    public static DownloadManifestOptions fromTag(String tag) {
        Objects.requireNonNull(tag, "tag can't be null");
        return new DownloadManifestOptions(tag, null);
    }

    /**
     * Instantiate the options class with tag.
     * @param digest The digest associated with the manifest.
     * @return The DownloadManifestOptions object.
     */
    public static DownloadManifestOptions fromDigest(String digest) {
        Objects.requireNonNull(digest, "digest can't be null");
        return new DownloadManifestOptions(null, digest);
    }

    /**
     * Digest identifier of the manifest.
     * @return The associated digest.
     */
    public String getDigest() {
        return this.digest;
    }

    /**
     * Tag identifier of the manifest.
     * @return The associated tag.
     */
    public String getTag() {
        return this.tag;
    }
}
