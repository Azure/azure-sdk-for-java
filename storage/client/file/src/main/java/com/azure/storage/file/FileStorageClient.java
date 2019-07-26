// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.CorsRule;
import com.azure.storage.file.models.FileServiceProperties;
import com.azure.storage.file.models.ListSharesOptions;
import com.azure.storage.file.models.ShareItem;
import com.azure.storage.file.models.StorageErrorException;
import java.net.URL;
import java.util.Map;

/**
 * This class provides operations for interacting with a file account in Azure Storage.
 * Operations allowed by the FileStorageClient are creating, listing, and deleting shares and retrieving and updating properties
 * of the account.
 *
 * <p><strong>Instantiating a Synchronous File Service Client</strong></p>
 *
 * {@codesnippet com.azure.storage.file.fileStorageClient.instantiation}
 *
 * <p>View {@link FileStorageClientBuilder this} for additional ways to construct the FileStorageClient.</p>
 *
 * @see FileStorageClientBuilder
 * @see FileStorageAsyncClient
 * @see SharedKeyCredential
 * @see SASTokenCredential
 */
public final class FileStorageClient {
    private final FileStorageAsyncClient fileStorageAsyncClient;

    /**
     * Creates a FileStorageClient that wraps a FileStorageAsyncClient and blocks requests.
     *
     * @param client FileStorageAsyncClient that is used to send requests
     */
    FileStorageClient(FileStorageAsyncClient client) {
        this.fileStorageAsyncClient = client;
    }

    /**
     * Get the url of the storage file service client.
     * @return the url of the Storage File service.
     * @throws RuntimeException If the directory is using a malformed URL.
     */
    public URL getFileStorageUrl() {
        return fileStorageAsyncClient.getFileStorageUrl();
    }

    /**
     * Constructs a ShareClient that interacts with the specified share.
     *
     * <p>If the share doesn't exist in the storage account {@link ShareClient#create() create} in the ShareClient will
     * need to be called before interaction with the share can happen.</p>
     *
     * @param shareName Name of the share
     * @return a ShareClient that interacts with the specified share
     */
    public ShareClient getShareClient(String shareName) {
        return new ShareClient(fileStorageAsyncClient.getShareAsyncClient(shareName));
    }

    /**
     * Lists all shares in the storage account without their metadata or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all shares in the account</p>
     *
     * {@codesnippet com.azure.storage.file.fileStorageClient.listShares}
     *
     * @return {@link ShareItem Shares} in the storage account without their metadata or snapshots
     */
    public Iterable<ShareItem> listShares() {
        return listShares(null);
    }

    /**
     * Lists the shares in the Storage account that pass the options filter.
     *
     * <p>Set starts with name filter using {@link ListSharesOptions#prefix(String) prefix} to filter shares that are
     * listed.</p>
     *
     * <p>Pass true to {@link ListSharesOptions#includeMetadata(boolean) includeMetadata} to have metadata returned for
     * the shares.</p>
     *
     * <p>Pass true to {@link ListSharesOptions#includeSnapshots(boolean) includeSnapshots} to have snapshots of the
     * shares listed.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all shares that begin with "azure"</p>
     *
     * {@codesnippet com.azure.storage.file.fileStorageClient.listShares#ListSharesOptions.prefix}
     *
     * <p>List all shares including their snapshots and metadata</p>
     *
     * {@codesnippet com.azure.storage.file.fileStorageClient.listShares#ListSharesOptions.metadata.snapshot}
     *
     * @param options Options for listing shares
     * @return {@link ShareItem Shares} in the storage account that satisfy the filter requirements
     */
    public Iterable<ShareItem> listShares(ListSharesOptions options) {
        return fileStorageAsyncClient.listShares(options).toIterable();
    }

    /**
     * Retrieves the properties of the storage account's File service. The properties range from storage analytics and
     * metrics to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve File service properties</p>
     *
     * {@codesnippet com.azure.storage.file.fileStorageClient.getProperties}
     *
     * @return Storage account File service properties
     */
    public Response<FileServiceProperties> getProperties() {
        return fileStorageAsyncClient.getProperties().block();
    }

    /**
     * Sets the properties for the storage account's File service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * To maintain the CORS in the Queue service pass a {@code null} value for {@link FileServiceProperties#cors() CORS}.
     * To disable all CORS in the Queue service pass an empty list for {@link FileServiceProperties#cors() CORS}.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p>Clear CORS in the File service</p>
     *
     * {@codesnippet com.azure.storage.file.fileStorageClient.setProperties#fileServiceProperties.clearCORS}
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * {@codesnippet com.azure.storage.file.fileStorageClient.setProperties#fileServiceProperties}
     *
     * @param properties Storage account File service properties
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException When one of the following is true
     * <ul>
     *     <li>A CORS rule is missing one of its fields</li>
     *     <li>More than five CORS rules will exist for the Queue service</li>
     *     <li>Size of all CORS rules exceeds 2KB</li>
     *     <li>
     *         Length of {@link CorsRule#allowedHeaders() allowed headers}, {@link CorsRule#exposedHeaders() exposed headers},
     *         or {@link CorsRule#allowedOrigins() allowed origins} exceeds 256 characters.
     *     </li>
     *     <li>{@link CorsRule#allowedMethods() Allowed methods} isn't DELETE, GET, HEAD, MERGE, POST, OPTIONS, or PUT</li>
     * </ul>
     */
    public VoidResponse setProperties(FileServiceProperties properties) {
        return fileStorageAsyncClient.setProperties(properties).block();
    }

    /**
     * Creates a share in the storage account with the specified name and returns a ShareClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share with share name of "myshare"</p>
     * {@codesnippet com.azure.storage.file.fileStorageClient.createShare#string}
     *
     * @param shareName Name of the share
     * @return A response containing the ShareClient and the status of creating the share.
     * @throws StorageErrorException If a share with the same name already exists
     */
    public Response<ShareClient> createShare(String shareName) {
        return createShare(shareName, null, null);
    }

    /**
     * Creates a share in the storage account with the specified name and metadata and returns a ShareClient to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share "test" with metadata "share:metadata"</p>
     *
     * {@codesnippet com.azure.storage.file.fileStorageClient.createShare#string-map-integer.metadata}
     *
     * <p>Create the share "test" with a quota of 10 GB</p>
     *
     * {@codesnippet com.azure.storage.file.fileStorageClient.createShare#string-map-integer.quota}
     *
     * @param shareName Name of the share
     * @param metadata Optional metadata to associate with the share
     * @param quotaInGB Optional maximum size the share is allowed to grow to in GB. This must be greater than 0 and
     * less than or equal to 5120. The default value is 5120.
     * @return A response containing the ShareClient and the status of creating the share.
     * @throws StorageErrorException If a share with the same name already exists or {@code quotaInGB} is outside the
     * allowed range.
     */
    public Response<ShareClient> createShare(String shareName, Map<String, String> metadata, Integer quotaInGB) {
        ShareClient shareClient = getShareClient(shareName);
        return new SimpleResponse<>(shareClient.create(metadata, quotaInGB), shareClient);
    }

    /**
     * Deletes the share in the storage account with the given name
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share "test"</p>
     *
     * {@codesnippet com.azure.storage.file.fileStorageClient.deleteShare#string}
     *
     * @param shareName Name of the share
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist
     */
    public VoidResponse deleteShare(String shareName) {
        return deleteShare(shareName, null);
    }

    /**
     * Deletes the specific snapshot of the share in the storage account with the given name. Snapshot are identified
     * by the time they were created.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the snapshot of share "test" that was created at current time. </p>
     *
     * {@codesnippet com.azure.storage.file.fileStorageClient.deleteShare#string-string}
     *
     * @param shareName Name of the share
     * @param snapshot Identifier of the snapshot
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist or the snapshot doesn't exist
     */
    public VoidResponse deleteShare(String shareName, String snapshot) {
        return fileStorageAsyncClient.deleteShare(shareName, snapshot).block();
    }
}
