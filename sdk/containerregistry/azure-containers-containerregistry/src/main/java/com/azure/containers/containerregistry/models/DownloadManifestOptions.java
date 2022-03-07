// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

/**
 * Options for configuring the download manifest operation.
 */
public final class DownloadManifestOptions {
    private String tag;
    private String digest;

    private DownloadManifestOptions() { }

    /**
     * Instantiate the options class with tag.
     * @param tag The tag associated with the manifest.
     * @return The DownloadManifestOptions object.
     */
    public static DownloadManifestOptions fromTag(String tag) {
        DownloadManifestOptions options = new DownloadManifestOptions();
        options.tag = tag;
        return options;
    }

    /**
     * Instantiate the options class with tag.
     * @param digest The digest associated with the manifest.
     * @return The DownloadManifestOptions object.
     */
    public static DownloadManifestOptions fromDigest(String digest) {
        DownloadManifestOptions options = new DownloadManifestOptions();
        options.digest = digest;
        return options;
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
