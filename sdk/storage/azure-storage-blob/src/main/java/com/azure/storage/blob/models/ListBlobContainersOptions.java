// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobServiceClient;

/**
 * Defines options available to configure the behavior of a call to listContainersSegment on a {@link BlobServiceClient}
 * object. See the constructor for details on each of the options. Null may be passed in place of an object of this type
 * if no options are desirable.
 */
@Fluent
public final class ListBlobContainersOptions {
    private final ClientLogger logger = new ClientLogger(ListBlobContainersOptions.class);

    private BlobContainerListDetails details;

    private String prefix;

    private Integer maxResultsPerPage;

    /**
     * Constructs an unpopulated {@link ListBlobContainersOptions}.
     */
    public ListBlobContainersOptions() {
        this.details = new BlobContainerListDetails();
    }

    /**
     * @return the details for listing specific containers
     */
    public BlobContainerListDetails getDetails() {
        return details;
    }

    /**
     * @param details The details for listing specific containers
     * @return the updated ListBlobContainersOptions object
     */
    public ListBlobContainersOptions setDetails(BlobContainerListDetails details) {
        this.details = details;
        return this;
    }

    /**
     * Filters the results to return only blobs whose names begin with the specified prefix.
     *
     * @return the prefix a container must start with to be returned
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Filters the results to return only blobs whose names begin with the specified prefix.
     *
     * @param prefix The prefix that a container must match to be returned
     * @return the updated ListBlobContainersOptions object
     */
    public ListBlobContainersOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Specifies the maximum number of blobs to return, including all BlobPrefix elements. If the request does not
     * specify maxResultsPerPage or specifies a value greater than 5,000, the server will return up to 5,000 items.
     *
     * @return the number of containers to be returned in a single response
     */
    public Integer getMaxResultsPerPage() {
        return maxResultsPerPage;
    }

    /**
     * Specifies the maximum number of blobs to return, including all BlobPrefix elements. If the request does not
     * specify maxResultsPerPage or specifies a value greater than 5,000, the server will return up to 5,000 items.
     *
     * @param maxResultsPerPage The number of containers to return in a single response
     * @return the updated ListBlobContainersOptions object
     * @throws IllegalArgumentException If {@code maxResultsPerPage} is less than or equal to {@code 0}.
     */
    public ListBlobContainersOptions setMaxResultsPerPage(Integer maxResultsPerPage) {
        if (maxResultsPerPage != null && maxResultsPerPage <= 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("MaxResultsPerPage must be greater than 0."));
        }
        this.maxResultsPerPage = maxResultsPerPage;
        return this;
    }
}
