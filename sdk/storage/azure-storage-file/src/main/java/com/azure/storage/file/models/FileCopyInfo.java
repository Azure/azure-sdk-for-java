// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import java.time.OffsetDateTime;

/**
 * Contains copy information about a File in the storage File service.
 */
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
     * @return Entity tag that corresponds to the directory.
     */
    public String eTag() {
        return eTag;
    }

    /**
     * @return Last time the directory was modified.
     */
    public OffsetDateTime lastModified() {
        return lastModified;
    }

    /**
     * @return String identifier for this copy operation.
     */
    public String copyId() {
        return copyId;
    }

    /**
     * @return State of the copy operation with these values:
     *                       - success: the copy completed successfully.
     *                       - pending: the copy is still in progress.
     */
    public CopyStatusType copyStatus() {
        return copyStatus;
    }
}
