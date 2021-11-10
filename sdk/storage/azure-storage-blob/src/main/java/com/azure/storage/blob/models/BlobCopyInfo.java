// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Immutable;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * The status of a long running Azure Blob copy operation.
 *
 * @see BlobAsyncClient#beginCopy(String, Duration)
 * @see BlobClient#beginCopy(String, Duration)
 */
@Immutable
public class BlobCopyInfo {
    private final String copyId;
    private final String copySource;
    private final CopyStatusType copyStatus;
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final String error;
    private final String versionId;

    /**
     * Creates an instance of {@link BlobCopyInfo}.
     *
     * @param copyId The identifier of the copy operation.
     * @param copySource The url of the source blob. The contents are being copied from this blob.
     * @param copyStatus The status of the copy operation.
     * @param error An error message for the copy operation. {@code null} if there are no errors.
     * @param eTag If the copy is completed, contains the ETag of the destination blob. If the copy is not complete,
     *     contains the ETag of the empty blob created.
     * @param lastModified The date/time that the copy operation to the destination blob completed.
     *
     * @throws NullPointerException If {@code copyId}, {@code copySource}, {@code eTag}, or {@code copyStatus} is null.
     */
    public BlobCopyInfo(String copySource, String copyId, CopyStatusType copyStatus, String eTag,
                        OffsetDateTime lastModified, String error) {
        this(copySource, copyId, copyStatus, eTag, lastModified, error, null);
    }

    /**
     * Creates an instance of {@link BlobCopyInfo}.
     *
     * @param copyId The identifier of the copy operation.
     * @param copySource The url of the source blob. The contents are being copied from this blob.
     * @param copyStatus The status of the copy operation.
     * @param error An error message for the copy operation. {@code null} if there are no errors.
     * @param eTag If the copy is completed, contains the ETag of the destination blob. If the copy is not complete,
     *     contains the ETag of the empty blob created.
     * @param lastModified The date/time that the copy operation to the destination blob completed.
     * @param versionId The version identifier of the destination blob.
     *
     * @throws NullPointerException If {@code copyId}, {@code copySource}, {@code eTag}, or {@code copyStatus} is null.
     */
    public BlobCopyInfo(String copySource, String copyId, CopyStatusType copyStatus, String eTag,
                        OffsetDateTime lastModified, String error, String versionId) {
        this.copyId = Objects.requireNonNull(copyId, "'copyId' cannot be null.");
        this.copySource = Objects.requireNonNull(copySource, "'copySource' cannot be null.");
        this.copyStatus = Objects.requireNonNull(copyStatus, "'copyStatus' cannot be null.");
        this.eTag = Objects.requireNonNull(eTag, "'eTag' cannot be null.");
        this.lastModified = lastModified;
        this.error = error;
        this.versionId = versionId;
    }

    /**
     * Gets the identifier for the copy operation.
     *
     * @return The identifier for the copy operation.
     */
    public String getCopyId() {
        return copyId;
    }

    /**
     * Gets the url of the source blob.
     *
     * @return The url of the source blob.
     */
    public String getCopySourceUrl() {
        return copySource;
    }

    /**
     * Gets the status of the copy operation.
     *
     * @return The status of the copy operation.
     */
    public CopyStatusType getCopyStatus() {
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

    /**
     * Gets the date/time that the copy operation to the destination blob completed.
     *
     * @return The date/time that the copy operation to the destination blob completed.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * If the copy is complete, contains the ETag of the destination blob. If the copy isn't complete, contains the
     * ETag of the empty blob created at the start of the copy.
     *
     * The ETag value will be in quotes.
     *
     * @return The ETag for the copy.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Gets the version identifier of the destination blob completed.
     *
     * @return The version identifier of the destination blob completed.
     */
    public String getVersionId() {
        return versionId;
    }
}
