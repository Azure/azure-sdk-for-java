// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.datalake.DataLakeServiceAsyncClient;

/**
 * Defines options available to configure the behavior of a call to listFileSystemsSegment on a
 * {@link DataLakeServiceAsyncClient} object. See the constructor for details on each of the options. Null may be
 * passed in place of an object of this type if no options are desirable.
 */
public final class ListFileSystemsOptions {
    private final ClientLogger logger = new ClientLogger(ListFileSystemsOptions.class);

    private FileSystemListDetails details;

    private String prefix;

    private Integer maxResultsPerPage;

    /**
     * Constructs an unpopulated {@link ListFileSystemsOptions}.
     */
    public ListFileSystemsOptions() {
        this.details = new FileSystemListDetails();
    }

    /**
     * @return the details for listing specific file systems
     */
    public FileSystemListDetails getDetails() {
        return details;
    }

    /**
     * @param details The details for listing specific file systems
     * @return the updated FileSystemListDetails object
     */
    public ListFileSystemsOptions setDetails(FileSystemListDetails details) {
        this.details = details;
        return this;
    }

    /**
     * Filters the results to return only paths whose names begin with the specified prefix.
     *
     * @return the prefix a file system must start with to be returned
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Filters the results to return only paths whose names begin with the specified prefix.
     *
     * @param prefix The prefix that a file system must match to be returned
     * @return the updated ListFileSystemsOptions object
     */
    public ListFileSystemsOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Specifies the maximum number of paths to return, including all PathPrefix elements. If the request does not
     * specify maxResultsPerPage or specifies a value greater than 5,000, the server will return up to 5,000 items.
     *
     * @return the number of file systems to be returned in a single response
     */
    public Integer getMaxResultsPerPage() {
        return maxResultsPerPage;
    }

    /**
     * Specifies the maximum number of paths to return, including all PathPrefix elements. If the request does not
     * specify maxResultsPerPage or specifies a value greater than 5,000, the server will return up to 5,000 items.
     *
     * @param maxResultsPerPage The number of file systems to return in a single response
     * @return the updated ListFileSystemsOptions object
     * @throws IllegalArgumentException If {@code maxResultsPerPage} is less than or equal to {@code 0}.
     */
    public ListFileSystemsOptions setMaxResultsPerPage(Integer maxResultsPerPage) {
        if (maxResultsPerPage != null && maxResultsPerPage <= 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("MaxResults must be greater than 0."));
        }
        this.maxResultsPerPage = maxResultsPerPage;
        return this;
    }
}
