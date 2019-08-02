// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.storage.blob.BlobServiceClient;

/**
 * Defines options available to configure the behavior of a call to listContainersSegment on a {@link BlobServiceClient}
 * object. See the constructor for details on each of the options. Null may be passed in place of an object of this
 * type if no options are desirable.
 */
public final class ListContainersOptions {

    private ContainerListDetails details;

    private String prefix;

    private Integer maxResults;

    public ListContainersOptions() {
        this.details = new ContainerListDetails();
    }

    /**
     * @return the details for listing specific containers
     */
    public ContainerListDetails details() {
        return details;
    }

    /**
     * @param details The details for listing specific containers
     * @return the updated ListContainersOptions object
     */
    public ListContainersOptions details(ContainerListDetails details) {
        this.details = details;
        return this;
    }

    /**
     * Filters the results to return only blobs whose names begin with the specified prefix.
     *
     * @return the prefix a container must start with to be returned
     */
    public String prefix() {
        return prefix;
    }

    /**
     * Filters the results to return only blobs whose names begin with the specified prefix.
     *
     * @param prefix The prefix that a container must match to be returned
     * @return the updated ListContainersOptions object
     */
    public ListContainersOptions prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Specifies the maximum number of blobs to return, including all BlobPrefix elements. If the request does not
     * specify maxResults or specifies a value greater than 5,000, the server will return up to 5,000 items.
     *
     * @return the number of containers to be returned in a single response
     */
    public Integer maxResults() {
        return maxResults;
    }

    /**
     * Specifies the maximum number of blobs to return, including all BlobPrefix elements. If the request does not
     * specify maxResults or specifies a value greater than 5,000, the server will return up to 5,000 items.
     *
     * @param maxResults The number of containers to return in a single response
     * @return the updated ListContainersOptions object
     */
    public ListContainersOptions maxResults(Integer maxResults) {
        if (maxResults != null && maxResults <= 0) {
            throw new IllegalArgumentException("MaxResults must be greater than 0.");
        }
        this.maxResults = maxResults;
        return this;
    }
}
