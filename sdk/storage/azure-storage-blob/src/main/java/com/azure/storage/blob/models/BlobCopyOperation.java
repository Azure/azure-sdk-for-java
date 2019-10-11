// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Immutable;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;

import java.net.URL;
import java.util.Objects;

/**
 * The status of a long running Azure Blob copy operation.
 *
 * @see BlobAsyncClient#beginCopyFromUrl(URL)
 * @see BlobClient#beginCopyFromUrl(URL)
 */
@Immutable
public class BlobCopyOperation {
    private final String id;
    private final String targetUrl;
    private final String sourceUrl;
    private final CopyStatusType copyStatus;
    private final String error;

    /**
     * Creates an instance of {@link BlobCopyOperation}.
     *
     * @param id The identifier of the copy operation.
     * @param targetUrl The url of the destination blob. The contents are being copied to this blob.
     * @param sourceUrl The url of the source blob. The contents are being copied from this blob.
     * @param copyStatus The status of the copy operation.
     * @param error An error message for the copy operation. {@code null} if there are no errors.
     *
     * @throws NullPointerException If {@code id}, {@code targetUrl}, {@code sourceUrl}, or {@code copyStatus} is
     *     null.
     */
    public BlobCopyOperation(String id, String targetUrl, String sourceUrl, CopyStatusType copyStatus, String error) {
        this.id = Objects.requireNonNull(id, "'id' cannot be null.");
        this.targetUrl = Objects.requireNonNull(targetUrl, "'targetUrl' cannot be null.");
        this.sourceUrl = Objects.requireNonNull(sourceUrl, "'sourceUrl' cannot be null.");
        this.copyStatus = Objects.requireNonNull(copyStatus, "'copyStatus' cannot be null.");
        this.error = error;
    }

    // Identifier for the copy operation

    /**
     * Gets the identifier for the copy operation.
     *
     * @return The identifier for the copy operation.
     */
    public String getId() {
        return id;
    }

    // Gets the url of the destination blob.

    /**
     * Gets the url of the destination blob. Contents are being copied to this blob.
     *
     * @return The url of the destination blob.
     */
    public String getTargetUrl() {
        return targetUrl;
    }

    /**
     * Gets the url of the source blob. Contents are being copied from this blob to {@link #getTargetUrl()}.
     *
     * @return The url of the source blob.
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Gets the status of the copy operation.
     *
     * @return The status of the copy operation.
     */
    public CopyStatusType getStatus() {
        return copyStatus;
    }

    /**
     * Gets an error description associated with the copy operation.
     *
     * @return An error description associated with the copy, or {@code null} if there is no error associated with this
     *     copy operation.
     */
    public String getError() {
        return error;
    }
}
