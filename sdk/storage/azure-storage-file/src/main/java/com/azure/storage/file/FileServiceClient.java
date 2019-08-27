// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.AccountSASPermission;
import com.azure.storage.common.AccountSASResourceType;
import com.azure.storage.common.AccountSASService;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;
import com.azure.core.util.Context;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.CorsRule;
import com.azure.storage.file.models.FileServiceProperties;
import com.azure.storage.file.models.ListSharesOptions;
import com.azure.storage.file.models.ShareItem;
import com.azure.storage.file.models.StorageErrorException;

import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * This class provides a fileServiceAsyncClient that contains all the operations for interacting with a file account in Azure Storage.
 * Operations allowed by the fileServiceAsyncClient are creating, listing, and deleting shares and retrieving and updating properties
 * of the account.
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
 * @see SASTokenCredential
 */
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
     * @return the url of the Storage File service.
     * @throws RuntimeException If the directory is using a malformed URL.
     */
    public URL getFileServiceUrl() {
        return fileServiceAsyncClient.getFileServiceUrl();
    }

    /**
     * Constructs a ShareClient that interacts with the specified share.
     *
     * <p>If the share doesn't exist in the storage account {@link ShareClient#create() create} in the fileServiceAsyncClient will
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
     * {@codesnippet com.azure.storage.file.fileServiceClient.listShares#ListSharesOptions.prefix}
     *
     * <p>List all shares including their snapshots and metadata</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceClient.listShares#ListSharesOptions.metadata.snapshot}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-shares">Azure Docs</a>.</p>
     *
     * @param options Options for listing shares
     * @return {@link ShareItem Shares} in the storage account that satisfy the filter requirements
     */
    public Iterable<ShareItem> listShares(ListSharesOptions options) {
        return fileServiceAsyncClient.listShares(options).toIterable();
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
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file-service-properties">Azure Docs</a>.</p>
     *
     * @return Storage account {@link FileServiceProperties File service properties}
     */
    public FileServiceProperties getProperties() {
        return getPropertiesWithResponse(Context.NONE).value();
    }

    /**
     * Retrieves the properties of the storage account's File service. The properties range from storage analytics and
     * metrics to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve File service properties</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceClient.getPropertiesWithResponse#Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file-service-properties">Azure Docs</a>.</p>
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the Storage account {@link FileServiceProperties File service properties} with headers and response status code
     */
    public Response<FileServiceProperties> getPropertiesWithResponse(Context context) {
        return fileServiceAsyncClient.getPropertiesWithResponse(context).block();
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
     * {@codesnippet com.azure.storage.file.fileServiceClient.setPropertiesWithResponse#fileServiceProperties-Context.clearCORS}
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceClient.setProperties#fileServiceProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-service-properties">Azure Docs</a>.</p>
     *
     * @param properties Storage account File service properties
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
    public void setProperties(FileServiceProperties properties) {
        setPropertiesWithResponse(properties, Context.NONE);
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
     * {@codesnippet com.azure.storage.file.fileServiceClient.setPropertiesWithResponse#fileServiceProperties-Context.clearCORS}
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceClient.setPropertiesWithResponse#fileServiceProperties-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-service-properties">Azure Docs</a>.</p>
     *
     * @param properties Storage account File service properties
     * @param context Additional context that is passed through the Http pipeline during the service call.
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
    public VoidResponse setPropertiesWithResponse(FileServiceProperties properties, Context context) {
        return fileServiceAsyncClient.setPropertiesWithResponse(properties, context).block();
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
     * @throws StorageErrorException If a share with the same name already exists
     */
    public ShareClient createShare(String shareName) {
        return createShareWithResponse(shareName, null, null, Context.NONE).value();
    }

    /**
     * Creates a share in the storage account with the specified name and metadata and returns a ShareClient to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share "test" with a quota of 10 GB</p>
     *
     * {@codesnippet com.azure.storage.file.FileServiceClient.createShareWithResponse#String-Map-Integer-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @param metadata Optional metadata to associate with the share
     * @param quotaInGB Optional maximum size the share is allowed to grow to in GB. This must be greater than 0 and
     * less than or equal to 5120. The default value is 5120.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareClient ShareClient} and the status of creating the share.
     * @throws StorageErrorException If a share with the same name already exists or {@code quotaInGB} is outside the
     * allowed range.
     */
    public Response<ShareClient> createShareWithResponse(String shareName, Map<String, String> metadata, Integer quotaInGB, Context context) {
        ShareClient shareClient = getShareClient(shareName);
        return new SimpleResponse<>(shareClient.createWithResponse(metadata, quotaInGB, context), shareClient);
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
     * @throws StorageErrorException If the share doesn't exist
     */
    public void deleteShare(String shareName) {
        deleteShareWithResponse(shareName, null, Context.NONE);
    }

    /**
     * Deletes the specific snapshot of the share in the storage account with the given name. Snapshot are identified
     * by the time they were created.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the snapshot of share "test" that was created at current time. </p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceClient.deleteShareWithResponse#string-string-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @param snapshot Identifier of the snapshot
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist or the snapshot doesn't exist
     */
    public VoidResponse deleteShareWithResponse(String shareName, String snapshot, Context context) {
        return fileServiceAsyncClient.deleteShareWithResponse(shareName, snapshot, context).block();
    }

    /**
     * Generates an account SAS token with the specified parameters
     *
     * @param accountSASService The {@code AccountSASService} services for the account SAS
     * @param accountSASResourceType An optional {@code AccountSASResourceType} resources for the account SAS
     * @param accountSASPermission The {@code AccountSASPermission} permission for the account SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the account SAS
     * @return A string that represents the SAS token
     */
    public String generateAccountSAS(AccountSASService accountSASService, AccountSASResourceType accountSASResourceType,
        AccountSASPermission accountSASPermission, OffsetDateTime expiryTime) {
        return this.fileServiceAsyncClient.generateAccountSAS(accountSASService, accountSASResourceType, accountSASPermission, expiryTime);
    }

    /**
     * Generates an account SAS token with the specified parameters
     *
     * @param accountSASService The {@code AccountSASService} services for the account SAS
     * @param accountSASResourceType An optional {@code AccountSASResourceType} resources for the account SAS
     * @param accountSASPermission The {@code AccountSASPermission} permission for the account SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the account SAS
     * @param startTime The {@code OffsetDateTime} start time for the account SAS
     * @param version The {@code String} version for the account SAS
     * @param ipRange An optional {@code IPRange} ip address range for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @return A string that represents the SAS token
     */
    public String generateAccountSAS(AccountSASService accountSASService, AccountSASResourceType accountSASResourceType,
        AccountSASPermission accountSASPermission, OffsetDateTime expiryTime, OffsetDateTime startTime, String version,
        IPRange ipRange, SASProtocol sasProtocol) {
        return this.fileServiceAsyncClient.generateAccountSAS(accountSASService, accountSASResourceType, accountSASPermission, expiryTime, startTime, version, ipRange, sasProtocol);
    }
}
