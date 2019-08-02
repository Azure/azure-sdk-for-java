// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

/**
 * Defines options available to configure the behavior of a call to listContainersSegment on a {@link ServiceURL}
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
     * {@link ContainerListDetails}
     */
    public ContainerListDetails details() {
        return details;
    }

    /**
     * {@link ContainerListDetails}
     */
    public ListContainersOptions withDetails(ContainerListDetails details) {
        this.details = details;
        return this;
    }

    /**
     * Filters the results to return only blobs whose names begin with the specified prefix.     *
     */
    public String prefix() {
        return prefix;
    }

    /**
     * Filters the results to return only blobs whose names begin with the specified prefix.     *
     */
    public ListContainersOptions withPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Specifies the maximum number of blobs to return, including all BlobPrefix elements. If the request does not
     * specify maxResults or specifies a value greater than 5,000, the server will return up to 5,000 items.
     */
    public Integer maxResults() {
        return maxResults;
    }

    /**
     * Specifies the maximum number of blobs to return, including all BlobPrefix elements. If the request does not
     * specify maxResults or specifies a value greater than 5,000, the server will return up to 5,000 items.
     */
    public ListContainersOptions withMaxResults(Integer maxResults) {
        if (maxResults != null && maxResults <= 0) {
            throw new IllegalArgumentException("MaxResults must be greater than 0.");
        }
        this.maxResults = maxResults;
        return this;
    }
}
