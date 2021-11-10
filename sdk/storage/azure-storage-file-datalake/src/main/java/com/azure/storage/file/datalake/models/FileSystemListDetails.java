// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.storage.file.datalake.DataLakeServiceAsyncClient;

/**
 * This type allows users to specify additional information the service should return with each file system when listing
 * file systems in an account (via a {@link DataLakeServiceAsyncClient} object). This type is immutable to ensure
 * thread-safety of requests, so changing the details for a different listing operation requires construction of a
 * new object. Null may be passed if none of the options are desirable.
 */
public final class FileSystemListDetails {

    private boolean retrieveMetadata;
    private boolean retrieveDeleted;

    /**
     * Creates a new instance of {@link FileSystemListDetails}.
     */
    public FileSystemListDetails() {

    }

    /**
     * Whether metadata should be returned.
     *
     * @return a flag indicating whether metadata should be returned in the listing
     */
    public boolean getRetrieveMetadata() {
        return this.retrieveMetadata;
    }

    /**
     * Whether metadata should be returned.
     *
     * @param retrieveMetadata Flag indicating whether metadata should be returned
     * @return the updated FileSystemListDetails object
     */
    public FileSystemListDetails setRetrieveMetadata(boolean retrieveMetadata) {
        this.retrieveMetadata = retrieveMetadata;
        return this;
    }

    /**
     * Whether deleted file systems should be returned.
     *
     * @return a flag indicating whether deleted file systems should be returned
     */
    public boolean getRetrieveDeleted() {
        return this.retrieveDeleted;
    }

    /**
     * Whether deleted file systems should be returned.
     *
     * @param retrieveDeleted Flag indicating whether deleted file systems should be returned.
     * @return the updated FileSystemListDetails object
     */
    public FileSystemListDetails setRetrieveDeleted(boolean retrieveDeleted) {
        this.retrieveDeleted = retrieveDeleted;
        return this;
    }

    /**
     * @return the listing flags
     */
    public ListFileSystemsIncludeType toIncludeType() {
        if (this.retrieveMetadata) {
            return ListFileSystemsIncludeType.METADATA;
        }
        return null;
    }
}
