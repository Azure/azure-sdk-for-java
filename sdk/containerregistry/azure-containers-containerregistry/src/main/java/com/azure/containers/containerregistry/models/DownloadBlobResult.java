// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.BinaryData;

/**
 * The object returned by the downloadBlob operation
 * containing the blob contents and its digest.
 */
@Fluent
public final class DownloadBlobResult {
    final String digest;
    final BinaryData content;

    /**
     * Initialize an instance of DownloadBlobResult.
     * @param digest The SHA for the the returned blob.
     * @param content The content of the blob.
     */
    public DownloadBlobResult(String digest, BinaryData content) {
        this.digest = digest;
        this.content = content;
    }

    /**
     * Get the digest associated with the blob.
     * @return The digest.
     */
    public String getDigest() {
        return this.digest;
    }

    /**
     * Get the blob contents.
     * @return The contents of the blob.
     */
    public BinaryData getContent() {
        return this.content;
    }
}
