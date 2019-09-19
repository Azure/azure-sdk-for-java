// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.ContainerClient;

/**
 * Defines options available to configure the behavior of a call to listBlobsFlatSegment on a {@link ContainerClient}
 * object. See the constructor for details on each of the options.
 */
public final class ListBlobsOptions {
    private final ClientLogger logger = new ClientLogger(ListBlobsOptions.class);

    private BlobListDetails details;

    private String prefix;

    private Integer maxResults;

    public ListBlobsOptions() {
        this.details = new BlobListDetails();
    }

    /**
     * @return the details for listing specific blobs
     */
    public BlobListDetails getDetails() {
        return details;
    }

    /**
     * @param details The details for listing specific blobs
     * @return the updated ListBlobsOptions object
     */
    public ListBlobsOptions setDetails(BlobListDetails details) {
        this.details = details;
        return this;
    }

    /**
     * Filters the results to return only blobs whose names begin with the specified prefix. May be null to return all
     * blobs.
     *
     * @return the prefix that a blob must match to be returned in the listing
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Filters the results to return only blobs whose names begin with the specified prefix. May be null to return all
     * blobs.
     *
     * @param prefix A prefix that a blob must match to be returned
     * @return the updated ListBlobsOptions object
     */
    public ListBlobsOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Specifies the maximum number of blobs to return, including all BlobPrefix elements. If the request does not
     * specify maxResults or specifies a value greater than 5,000, the server will return up to 5,000 items.
     *
     * @return the number of blobs that will be returned in a single response
     */
    public Integer getMaxResults() {
        return maxResults;
    }

    /**
     * Specifies the maximum number of blobs to return, including all BlobPrefix elements. If the request does not
     * specify maxResults or specifies a value greater than 5,000, the server will return up to 5,000 items.
     *
     * @param maxResults The number of blobs to returned in a single response
     * @return the updated ListBlobsOptions object
     * @throws IllegalArgumentException If {@code maxResults} is less than or equal to {@code 0}.
     */
    public ListBlobsOptions setMaxResults(Integer maxResults) {
        if (maxResults != null && maxResults <= 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("MaxResults must be greater than 0."));
        }
        this.maxResults = maxResults;
        return this;
    }


}
