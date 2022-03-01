// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

import com.azure.core.annotation.Fluent;
import reactor.core.publisher.Flux;
import java.nio.ByteBuffer;

/**
 * The object returned by the downloadBlob operation
 * containing the blob contents and its digest.
 */
@Fluent
public final class DownloadBlobResult {
    String digest;
    Flux<ByteBuffer> content;

    /**
     * Initialize an instance of DownloadBlobResult.
     */
    public DownloadBlobResult() { }

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
    public Flux<ByteBuffer> getContent() {
        return this.content;
    }

    /**
     * Set the digest for the blob.
     * @param digest The digest.
     * @return The object instance.
     */
    public DownloadBlobResult setDigest(String digest) {
        this.digest = digest;
        return this;
    }

    /**
     * Set the blob contents.
     * @param content The byte buffer with the blob content.
     * @return The object instance.
     */
    public DownloadBlobResult setContent(Flux<ByteBuffer> content) {
        this.content = content;
        return this;
    }
}
