// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;
import com.azure.storage.file.share.ShareFileAsyncClient;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Contains copy information about a File in the storage File service.
 *
 * @see ShareFileAsyncClient#beginCopy(String, Map, Duration)
 */
@Immutable
public final class ShareFileCopyInfo {
    private final String copySource;
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final String copyId;
    private final CopyStatusType copyStatus;
    private final String error;

    /**
     * Creates an instance of copy information about a specific File.
     *
     * @param eTag If the copy is completed, contains the ETag of the destination file. If the copy is not complete,
     *     contains the ETag of the empty file created at the start of the copy.
     * @param lastModified The date/time that the copy operation to the destination file completed.
     * @param copyId String identifier for this copy operation.
     * @param copyStatus State of the copy operation with these values:
     *                       <ul>
     *                           <li>success: the copy completed successfully.</li>
     *                           <li>pending: the copy is still in progress.</li>
     *                       </ul>
     * @param copySource The url of the source file.
     * @param error An error message for the copy operation. {@code null} if there are no errors.
     */
    public ShareFileCopyInfo(String copySource, String copyId, CopyStatusType copyStatus, String eTag,
                             OffsetDateTime lastModified, String error) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.copyId = copyId;
        this.copyStatus = copyStatus;
        this.copySource = copySource;
        this.error = error;
    }

    /**
     * If the copy is completed, contains the ETag of the destination file. If the copy is not complete, contains the
     * ETag of the empty file created at the start of the copy.
     *
     * @return The ETag for the copy.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Gets the date/time that the copy operation to the destination file completed.
     *
     * @return Gets the date/time that the copy operation to the destination file completed.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Gets the string identifier for this copy operation.
     *
     * @return String identifier for this copy operation.
     */
    public String getCopyId() {
        return copyId;
    }

    /**
     * Gets the status of the copy operation. The status could be:
     * <ol>
     *     <li>{@link CopyStatusType#SUCCESS success}: The copy completed successfully.</li>
     *     <li>{@link CopyStatusType#PENDING pending}: The copy is still in progress.</li>
     * </ol>
     *
     * @return Status of the copy operation.
     */
    public CopyStatusType getCopyStatus() {
        return copyStatus;
    }

    /**
     * Gets the the source file used in the last attempted copy file operation.
     *
     * @return The url of the source file.
     */
    public String getCopySourceUrl() {
        return copySource;
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
