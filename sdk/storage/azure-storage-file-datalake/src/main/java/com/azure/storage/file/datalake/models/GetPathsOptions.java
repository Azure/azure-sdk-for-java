package com.azure.storage.file.datalake.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.file.datalake.FileSystemAsyncClient;
import com.azure.storage.file.datalake.FileSystemClient;

/**
 * Defines options available to configure the behavior of a call to listContainersSegment on a {@link FileSystemClient}
 * or a {@link FileSystemAsyncClient} object. See the constructor for details on each of the options. Null may be
 * passed in place of an object of this type if no options are desirable.
 */
public class GetPathsOptions {
    private final ClientLogger logger = new ClientLogger(ListBlobContainersOptions.class);

    private String path;
    private boolean recursive;
    private boolean returnUpn;
    private Integer maxResults;


    public GetPathsOptions() {
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
     * @return the returnUpn value.
     */
    public boolean isReturnUpn() {
        return returnUpn;
    }

    /**
     * Specifies the path to filter the results to.
     * An error occurs if the path does not exist.
     *
     * @param path The path value
     * @return the updated GetPathsOptions object
     */
    public GetPathsOptions setPath(String path) {
        this.path = path;
        return this;
    }

    public GetPathsOptions setRecursive(boolean recursive) {
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
     * @param returnUpn The returnUpn value
     * @return the updated GetPathsOptions object
     */
    public GetPathsOptions setReturnUpn(boolean returnUpn) {
        this.returnUpn = returnUpn;
        return this;
    }

    /**
     * Specifies the maximum number of blobs to return, including all BlobPrefix elements. If the request does not
     * specify maxResults or specifies a value greater than 5,000, the server will return up to 5,000 items.
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
    public GetPathsOptions setMaxResults(Integer maxResults) {
        if (maxResults != null && maxResults <= 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("MaxResults must be greater than 0."));
        }
        this.maxResults = maxResults;
        return this;
    }
}
