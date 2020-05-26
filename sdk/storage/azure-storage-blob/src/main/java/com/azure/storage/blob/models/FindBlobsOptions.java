// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;

/**
 * Defines options available to configure the behavior of a call to
 * {@link com.azure.storage.blob.BlobServiceClient#findBlobsByTags(String, FindBlobsOptions, Duration)} or
 * {@link com.azure.storage.blob.BlobServiceAsyncClient#findBlobsByTags(String, FindBlobsOptions)}. See the constructor
 * for details on each of the options.
 */
public class FindBlobsOptions {
    private final ClientLogger logger = new ClientLogger(FindBlobsOptions.class);

    private Integer maxResultsPerPage;

    /**
     * Constructs an unpopulated {@link FindBlobsOptions}.
     */
    public FindBlobsOptions() {

    }

    /**
     * Specifies the maximum number of blobs to return. If the request does not specify maxResultsPerPage or specifies a
     * value greater than 5,000, the server will return up to 5,000 items.
     *
     * @return the number of blobs that will be returned in a single response
     */
    public Integer getMaxResultsPerPage() {
        return maxResultsPerPage;
    }

    /**
     * Specifies the maximum number of blobs to return. If the request does not specify maxResultsPerPage or specifies a
     * value greater than 5,000, the server will return up to 5,000 items.
     *
     * @param maxResultsPerPage The number of blobs to returned in a single response
     * @return the updated FindBlobsOptions object
     * @throws IllegalArgumentException If {@code maxResultsPerPage} is less than or equal to {@code 0}.
     */
    public FindBlobsOptions setMaxResultsPerPage(Integer maxResultsPerPage) {
        if (maxResultsPerPage != null && maxResultsPerPage <= 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("MaxResultsPerPage must be greater than 0."));
        }
        this.maxResultsPerPage = maxResultsPerPage;
        return this;
    }
}
