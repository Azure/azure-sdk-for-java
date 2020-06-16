// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

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

    private final String query;
    private Integer maxResultsPerPage;
    private Duration timeout;

    /**
     * @param query Filters the results to return only blobs whose tags match the specified expression.
     */
    public FindBlobsOptions(String query) {
        this.query = query;
    }

    /**
     * @return Filters the results to return only blobs whose tags match the specified expression.
     */
    public String getQuery() {
        return this.query;
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

    /**
     * Gets the timeout.
     *
     * @return An optional timeout value beyond which a {@link RuntimeException} will be raised.
     */
    public Duration getTimeout() {
        return this.timeout;
    }

    /**
     * Sets the timeout.
     * <p>
     * This value will be ignored on async operations and must be set on the returned async object itself.
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The updated options.
     */
    public FindBlobsOptions setTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }
}
