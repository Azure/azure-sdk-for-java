// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;

/**
 * Defines options available to configure the behavior of a call to listDeletedPaths on a
 * {@link DataLakeFileSystemClient} or a {@link DataLakeFileSystemAsyncClient} object. See the constructor for details
 * on each of the options. Null may be passed in place of an object of this type if no options are desirable.
 */
public class ListDeletedPathsOptions {
    private final ClientLogger logger = new ClientLogger(ListDeletedPathsOptions.class);

    private String path;
    private Integer maxResults;

    /**
     * Constructs an unpopulated {@link ListPathsOptions}.
     */
    public ListDeletedPathsOptions() {
    }

    /**
     * Specifies the path to filter the results to.
     * An error occurs if the path does not exist.
     *
     * @return the path value.
     */
    public String getPath() {
        return path;
    }

    /**
     * Specifies the path to filter the results to.
     * An error occurs if the path does not exist.
     *
     * @param path The path value
     * @return the updated ListPathsOptions object
     */
    public ListDeletedPathsOptions setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Specifies the maximum number of paths to return, including all prefx elements. If the request does not
     * specify maxResults or specifies a value greater than 5,000, the server will return up to 5,000 items.
     *
     * @return the number of paths to be returned in a single response
     */
    public Integer getMaxResults() {
        return maxResults;
    }

    /**
     * Specifies the maximum number of paths to return, including all prefix elements. If the request does not
     * specify maxResults or specifies a value greater than 5,000, the server will return up to 5,000 items.
     *
     * @param maxResults The number of paths to return in a single response
     * @return the updated ListDeletedPathsOptions object
     * @throws IllegalArgumentException If {@code maxResults} is less than or equal to {@code 0}.
     */
    public ListDeletedPathsOptions setMaxResults(Integer maxResults) {
        if (maxResults != null && maxResults <= 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("MaxResults must be greater than 0."));
        }
        this.maxResults = maxResults;
        return this;
    }
}
