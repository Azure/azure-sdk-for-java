// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.implementation.models.FilterBlobsIncludeItem;

import java.util.ArrayList;

/**
 * This type allows users to specify additional information the service should return with each blob when listing blobs
 * in a container (via a {@link BlobContainerClient} object). This type is immutable to ensure thread-safety of
 * requests, so changing the details for a different listing operation requires construction of a new object. Null may
 * be passed if none of the options are desirable.
 */
@Fluent
public final class FindBlobsByTagsDetails {
    private boolean retrieveVersions;

    /**
     * Constructs an unpopulated {@link BlobListDetails}.
     */
    public FindBlobsByTagsDetails() {
    }

    /**
     * Whether versions should be returned. Versions are listed from oldest to newest.
     *
     * @return a flag indicating if versions will be returned in the listing
     */
    public boolean getRetrieveVersions() {
        return retrieveVersions;
    }

    /**
     * Whether versions should be returned. Versions are listed from oldest to newest.
     *
     * @param retrieveVersions Flag indicating whether versions should be returned
     * @return the updated BlobListDetails object
     */
    public FindBlobsByTagsDetails setRetrieveVersions(boolean retrieveVersions) {
        this.retrieveVersions = retrieveVersions;
        return this;
    }

    /**
     * @return a list of the flag set to true
     */
    public ArrayList<FilterBlobsIncludeItem> toList() {
        ArrayList<FilterBlobsIncludeItem> details = new ArrayList<>();
        if (this.retrieveVersions) {
            details.add(FilterBlobsIncludeItem.VERSIONS);
        }
        return details;
    }
}
