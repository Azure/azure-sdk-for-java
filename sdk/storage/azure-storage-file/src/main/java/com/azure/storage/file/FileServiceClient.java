// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.CorsRule;
import com.azure.storage.file.models.FileServiceProperties;
import com.azure.storage.file.models.ListSharesOptions;
import com.azure.storage.file.models.ShareItem;
import com.azure.storage.file.models.StorageException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * This class provides a fileServiceAsyncClient that contains all the operations for interacting with a file account in
 * Azure Storage. Operations allowed by the fileServiceAsyncClient are creating, listing, and deleting shares and
 * retrieving and updating properties of the account.
 *
 * <p><strong>Instantiating a Synchronous File Service Client</strong></p>
 *
 * {@codesnippet com.azure.storage.file.fileServiceClient.instantiation}
 *
 * <p>View {@link FileServiceClientBuilder this} for additional ways to construct the fileServiceAsyncClient.</p>
 *
 * @see FileServiceClientBuilder
 * @see FileServiceAsyncClient
 * @see SharedKeyCredential
 */
@ServiceClient(builder = FileServiceClientBuilder.class)
public final class FileServiceClient {
    private final FileServiceAsyncClient fileServiceAsyncClient;

    /**
     * Creates a FileServiceClient that wraps a FileServiceAsyncClient and blocks requests.
     *
     * @param client FileServiceAsyncClient that is used to send requests
     */
    FileServiceClient(FileServiceAsyncClient client) {
        this.fileServiceAsyncClient = client;
    }

    /**
     * Get the url of the storage file service client.
     *
     * @return the url of the Storage File service.
     */
    public String getFileServiceUrl() {
        return fileServiceAsyncClient.getFileServiceUrl();
    }

    /**
     * Constructs a ShareClient that interacts with the specified share.
     *
     * <p>If the share doesn't exist in the storage account {@link ShareClient#create() create} in the
     * fileServiceAsyncClient will
     * need to be called before interaction with the share can happen.</p>
     *
     * @param shareName Name of the share
     * @return a ShareClient that interacts with the specified share
     */
    public ShareClient getShareClient(String shareName) {
        return new ShareClient(fileServiceAsyncClient.getShareAsyncClient(shareName));
    }

    /**
     * Lists all shares in the storage account without their metadata or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all shares in the account</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceClient.listShares}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-shares">Azure Docs</a>.</p>
     *
     * @return {@link ShareItem Shares} in the storage account without their metadata or snapshots
     */
    public PagedIterable<ShareItem> listShares() {
        return listShares(null, null, null);
    }

    /**
     * Lists the shares in the Storage account that pass the options filter.
     *
     * <p>Set starts with name filter using {@link ListSharesOptions#setPrefix(String) prefix} to filter shares that
     * are
     * listed.</p>
     *
     * <p>Pass true to {@link ListSharesOptions#setIncludeMetadata(boolean) includeMetadata} to have metadata returned
     * for
     * the shares.</p>
     *
     * <p>Pass true to {@link ListSharesOptions#setIncludeSnapshots(boolean) includeSnapshots} to have snapshots of the
     * shares listed.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all shares that begin with "azure"</p>
     *
     * {@codesnippet com.azure.storage.file.FileServiceClient.listShares#ListSharesOptions-Duration-Context1}
     *
     * <p>List all shares including their snapshots and metadata</p>
     *
     * {@codesnippet com.azure.storage.file.FileServiceClient.listShares#ListSharesOptions-Duration-Context2}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-shares">Azure Docs</a>.</p>
     *
     * @param options Options for listing shares
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link ShareItem Shares} in the storage account that satisfy the filter requirements
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public PagedIterable<ShareItem> listShares(ListSharesOptions options, Duration timeout, Context context) {
        return new PagedIterable<>(fileServiceAsyncClient
            .listSharesWithOptionalTimeout(null, options, timeout, context));
    }

    /**
     * Retrieves the properties of the storage account's File service. The properties range from storage analytics and
     * metrics to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve File service properties</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file-service-properties">Azure
     * Docs</a>.</p>
     *
     * @return Storage account {@link FileServiceProperties File service properties}
     */
    public FileServiceProperties getProperties() {
        return getPropertiesWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Retrieves the properties of the storage account's File service. The properties range from storage analytics and
     * metrics to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve File service properties</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceClient.getPropertiesWithResponse#duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file-service-properties">Azure
     * Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the Storage account {@link FileServiceProperties File service properties} with
     * headers and response status code
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<FileServiceProperties> getPropertiesWithResponse(Duration timeout, Context context) {
        Mono<Response<FileServiceProperties>> response = fileServiceAsyncClient.getPropertiesWithResponse(context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Sets the properties for the storage account's File service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * To maintain the CORS in the Queue service pass a {@code null} value for {@link FileServiceProperties#getCors()
     * CORS}. To disable all CORS in the Queue service pass an empty list for {@link FileServiceProperties#getCors()
     * CORS}.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p>Clear CORS in the File service</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceClient.setPropertiesWithResponse#fileServiceProperties-Context.clearCORS}
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceClient.setProperties#fileServiceProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-service-properties">Azure
     * Docs</a>.</p>
     *
     * @param properties Storage account File service properties
     * @throws StorageException When one of the following is true
     * <ul>
     * <li>A CORS rule is missing one of its fields</li>
     * <li>More than five CORS rules will exist for the Queue service</li>
     * <li>Size of all CORS rules exceeds 2KB</li>
     * <li>
     * Length of {@link CorsRule#getAllowedHeaders() allowed headers}, {@link CorsRule#getExposedHeaders() exposed
     * headers}, or {@link CorsRule#getAllowedOrigins() allowed origins} exceeds 256 characters.
     * </li>
     * <li>{@link CorsRule#getAllowedMethods() Allowed methods} isn't DELETE, GET, HEAD, MERGE, POST, OPTIONS, or
     * PUT</li>
     * </ul>
     */
    public void setProperties(FileServiceProperties properties) {
        setPropertiesWithResponse(properties, null, Context.NONE);
    }

    /**
     * Sets the properties for the storage account's File service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * To maintain the CORS in the Queue service pass a {@code null} value for {@link FileServiceProperties#getCors()
     * CORS}. To disable all CORS in the Queue service pass an empty list for {@link FileServiceProperties#getCors()
     * CORS}.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p>Clear CORS in the File service</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceClient.setPropertiesWithResponse#fileServiceProperties-Context.clearCORS}
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceClient.setPropertiesWithResponse#fileServiceProperties-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-service-properties">Azure
     * Docs</a>.</p>
     *
     * @param properties Storage account File service properties
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws StorageException When one of the following is true
     * <ul>
     * <li>A CORS rule is missing one of its fields</li>
     * <li>More than five CORS rules will exist for the Queue service</li>
     * <li>Size of all CORS rules exceeds 2KB</li>
     * <li>
     * Length of {@link CorsRule#getAllowedHeaders() allowed headers}, {@link CorsRule#getExposedHeaders() exposed
     * headers}, or {@link CorsRule#getAllowedOrigins() allowed origins} exceeds 256 characters.
     * </li>
     * <li>{@link CorsRule#getAllowedMethods() Allowed methods} isn't DELETE, GET, HEAD, MERGE, POST, OPTIONS, or
     * PUT</li>
     * </ul>
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<Void> setPropertiesWithResponse(FileServiceProperties properties, Duration timeout,
                                                    Context context) {
        Mono<Response<Void>> response = fileServiceAsyncClient.setPropertiesWithResponse(properties, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Creates a share in the storage account with the specified name and returns a ShareClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share with share name of "myshare"</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceClient.createShare#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @return The {@link ShareClient ShareClient}
     * @throws StorageException If a share with the same name already exists
     */
    public ShareClient createShare(String shareName) {
        return createShareWithResponse(shareName, null, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a share in the storage account with the specified name and metadata and returns a ShareClient to interact
     * with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share "test" with a quota of 10 GB</p>
     *
     * {@codesnippet com.azure.storage.file.FileServiceClient.createShareWithResponse#string-map-integer-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @param metadata Optional metadata to associate with the share
     * @param quotaInGB Optional maximum size the share is allowed to grow to in GB. This must be greater than 0 and
     * less than or equal to 5120. The default value is 5120.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareClient ShareClient} and the status of creating the share.
     * @throws StorageException If a share with the same name already exists or {@code quotaInGB} is outside the allowed
     * range.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<ShareClient> createShareWithResponse(String shareName, Map<String, String> metadata,
        Integer quotaInGB, Duration timeout, Context context) {
        ShareClient shareClient = getShareClient(shareName);
        return new SimpleResponse<>(shareClient.createWithResponse(metadata, quotaInGB, null, context), shareClient);
    }

    /**
     * Deletes the share in the storage account with the given name
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share "test"</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceClient.deleteShare#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @throws StorageException If the share doesn't exist
     */
    public void deleteShare(String shareName) {
        deleteShareWithResponse(shareName, null, null, Context.NONE);
    }

    /**
     * Deletes the specific snapshot of the share in the storage account with the given name. Snapshot are identified by
     * the time they were created.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the snapshot of share "test" that was created at current time. </p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceClient.deleteShareWithResponse#string-string-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @param snapshot Identifier of the snapshot
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws StorageException If the share doesn't exist or the snapshot doesn't exist
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<Void> deleteShareWithResponse(String shareName, String snapshot, Duration timeout,
                                                  Context context) {
        Mono<Response<Void>> response = fileServiceAsyncClient.deleteShareWithResponse(shareName, snapshot, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }
}
