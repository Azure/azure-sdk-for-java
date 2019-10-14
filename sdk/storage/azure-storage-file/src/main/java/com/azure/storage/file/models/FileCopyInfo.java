// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;

/**
 * Contains copy information about a File in the storage File service.
 */
@Immutable
public final class FileCopyInfo {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final String copyId;
    private final CopyStatusType copyStatus;

    /**
     * Creates an instance of copy information about a specific File.
     *
     * @param eTag Entity tag that corresponds to the directory.
     * @param lastModified Last time the directory was modified.
     * @param copyId String identifier for this copy operation.
     * @param copyStatus State of the copy operation with these values:
     *                       <ul>
     *                           <li>success: the copy completed successfully.</li>
     *                           <li>pending: the copy is still in progress.</li>
     *                       </ul>
     */
    public FileCopyInfo(final String eTag, final OffsetDateTime lastModified, final String copyId, final CopyStatusType copyStatus) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.copyId = copyId;
        this.copyStatus = copyStatus;
    }

    /**
     * Gets the entity tag that corresponds to the directory.
     *
     * @return Entity tag that corresponds to the directory.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Gets the last time the directory was modified.
     *
     * @return Last time the directory was modified.
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
}
