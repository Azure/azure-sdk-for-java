// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;

/**
 * Defines options available to configure the behavior of a call to listContainersSegment on a {@link DataLakeFileSystemClient}
 * or a {@link DataLakeFileSystemAsyncClient} object. See the constructor for details on each of the options. Null may be
 * passed in place of an object of this type if no options are desirable.
 */
public class ListPathsOptions {
    private final ClientLogger logger = new ClientLogger(ListPathsOptions.class);

    private String path;
    private boolean recursive;
    private boolean userPrincipalNameReturned;
    private Integer maxResults;

    /**
     * Constructs an unpopulated {@link ListPathsOptions}.
     */
    public ListPathsOptions() {
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
     * Specifies if the call to listContainersSegment should recursively include all paths.
     *
     * @return {@code true} if the call to listContainerSegment recursively includes all paths.
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Valid only when Hierarchical Namespace is enabled for the account.
     * If "true", the user identity values returned in the x-ms-owner, x-ms-group, and x-ms-acl response headers will
     * be transformed from Azure Active Directory Object IDs to User Principal Names.
     * If "false", the values will be returned as Azure Active Directory Object IDs.
     * The default value is false. Note that group and application Object IDs are not translated because they do not
     * have unique friendly names.
     *
     * @return the userPrincipalNameReturned value.
     */
    public boolean isUserPrincipalNameReturned() {
        return userPrincipalNameReturned;
    }

    /**
     * Specifies the path to filter the results to.
     * An error occurs if the path does not exist.
     *
     * @param path The path value
     * @return the updated ListPathsOptions object
     */
    public ListPathsOptions setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Specifies if the call to listContainersSegment should recursively include all paths.
     *
     * @param recursive {@code true} if the call to listContainerSegment recursively includes all paths.
     * @return the updated ListPathsOptions object.
     */
    public ListPathsOptions setRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }
    /**
     * Valid only when Hierarchical Namespace is enabled for the account.
     * If "true", the user identity values returned in the x-ms-owner, x-ms-group, and x-ms-acl response headers will
     * be transformed from Azure Active Directory Object IDs to User Principal Names.
     * If "false", the values will be returned as Azure Active Directory Object IDs.
     * The default value is false. Note that group and application Object IDs are not translated because they do not
     * have unique friendly names.
     *
     * @param isUserPrincipalNameReturned The userPrincipalNameReturned value
     * @return the updated ListPathsOptions object
     */
    public ListPathsOptions setUserPrincipalNameReturned(boolean isUserPrincipalNameReturned) {
        this.userPrincipalNameReturned = isUserPrincipalNameReturned;
        return this;
    }

    /**
     * Specifies the maximum number of blobs to return per page, including all BlobPrefix elements. If the request does
     * not specify maxResults or specifies a value greater than 5,000, the server will return up to 5,000 items per
     * page.
     *
     * @return the number of containers to be returned in a single response
     */
    public Integer getMaxResults() {
        return maxResults;
    }

    /**
     * Specifies the maximum number of blobs to return, including all BlobPrefix elements. If the request does not
     * specify maxResults or specifies a value greater than 5,000, the server will return up to 5,000 items.
     *
     * @param maxResults The number of containers to return in a single response
     * @return the updated ListBlobContainersOptions object
     * @throws IllegalArgumentException If {@code maxResults} is less than or equal to {@code 0}.
     */
    public ListPathsOptions setMaxResults(Integer maxResults) {
        if (maxResults != null && maxResults <= 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("MaxResults must be greater than 0."));
        }
        this.maxResults = maxResults;
        return this;
    }
}
