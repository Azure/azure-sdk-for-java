// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.http.PagedResponseBase;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.AccountSASPermission;
import com.azure.storage.common.AccountSASResourceType;
import com.azure.storage.common.AccountSASService;
import com.azure.storage.common.AccountSASSignatureValues;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.implementation.AzureFileStorageImpl;
import com.azure.storage.file.models.CorsRule;
import com.azure.storage.file.models.DeleteSnapshotsOptionType;
import com.azure.storage.file.models.FileServiceProperties;
import com.azure.storage.file.models.ListSharesIncludeType;
import com.azure.storage.file.models.ListSharesOptions;
import com.azure.storage.file.models.ShareItem;
import com.azure.storage.file.models.StorageException;
import java.time.Duration;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.azure.core.implementation.util.FluxUtil.withContext;
import static com.azure.storage.file.PostProcessor.postProcessResponse;

/**
 * This class provides a azureFileStorageClient that contains all the operations for interacting with a file account in Azure Storage.
 * Operations allowed by the azureFileStorageClient are creating, listing, and deleting shares and retrieving and updating properties
 * of the account.
 *
 * <p><strong>Instantiating an Asynchronous File Service Client</strong></p>
 *
 * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.instantiation}
 *
 * <p>View {@link FileServiceClientBuilder this} for additional ways to construct the azureFileStorageClient.</p>
 *
 * @see FileServiceClientBuilder
 * @see FileServiceClient
 * @see SharedKeyCredential
 * @see SASTokenCredential
 */
public final class FileServiceAsyncClient {
    private final ClientLogger logger = new ClientLogger(FileServiceAsyncClient.class);
    private final AzureFileStorageImpl azureFileStorageClient;

    /**
     * Creates a FileServiceClient from the passed {@link AzureFileStorageImpl implementation client}.
     *
     * @param azureFileStorage Client that interacts with the service interfaces.
     */
    FileServiceAsyncClient(AzureFileStorageImpl azureFileStorage) {
        this.azureFileStorageClient = azureFileStorage;
    }

    /**
     * Get the url of the storage file service client.
     * @return the url of the Storage File service.
     * @throws RuntimeException If the file service is using a malformed URL.
     */
    public URL getFileServiceUrl() {
        try {
            return new URL(azureFileStorageClient.getUrl());
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new RuntimeException(String.format("Invalid URL on %s: %s" + getClass().getSimpleName(),
                azureFileStorageClient.getUrl()), e));
        }
    }

    /**
     * Constructs a ShareAsyncClient that interacts with the specified share.
     *
     * <p>If the share doesn't exist in the storage account {@link ShareAsyncClient#create() create} in the azureFileStorageClient will
     * need to be called before interaction with the share can happen.</p>
     *
     * @param shareName Name of the share
     * @return a ShareAsyncClient that interacts with the specified share
     */
    public ShareAsyncClient getShareAsyncClient(String shareName) {
        return this.getShareAsyncClient(shareName, null);
    }

    /**
     * Constructs a ShareAsyncClient that interacts with the specified share.
     *
     * <p>If the share doesn't exist in the storage account {@link ShareAsyncClient#create() create} in the azureFileStorageClient will
     * need to be called before interaction with the share can happen.</p>
     *
     * @param shareName Name of the share
     * @param snapshot Snapshot ID of the share
     * @return a ShareAsyncClient that interacts with the specified share
     */
    public ShareAsyncClient getShareAsyncClient(String shareName, String snapshot) {
        return new ShareAsyncClient(azureFileStorageClient, shareName, snapshot);
    }

    /**
     * Lists all shares in the storage account without their metadata or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all shares in the account</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.listShares}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-shares">Azure Docs</a>.</p>
     *
     * @return {@link ShareItem Shares} in the storage account without their metadata or snapshots
     */
    public PagedFlux<ShareItem> listShares() {
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
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.listShares#ListSharesOptions.prefix}
     *
     * <p>List all shares including their snapshots and metadata</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.listShares#ListSharesOptions.metadata.snapshot}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-shares">Azure Docs</a>.</p>
     *
     * @param options Options for listing shares
     * @return {@link ShareItem Shares} in the storage account that satisfy the filter requirements
     */
    public PagedFlux<ShareItem> listShares(ListSharesOptions options) {
        return listSharesWithOptionalTimeout(null, options, null, Context.NONE);
    }

    /**
     * Lists the shares in the storage account that pass the filter starting at the specified marker.
     *
     * @param marker Starting point to list the shares
     * @param options Options for listing shares
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout concludes a {@link RuntimeException} will be thrown.
     * @return {@link ShareItem Shares} in the storage account that satisfy the filter requirements
     */
     PagedFlux<ShareItem> listSharesWithOptionalTimeout(String marker, ListSharesOptions options, Duration timeout, Context context) {
        final String prefix = (options != null) ? options.prefix() : null;
        final Integer maxResults = (options != null) ? options.maxResults() : null;
        List<ListSharesIncludeType> include = new ArrayList<>();

        if (options != null) {
            if (options.includeMetadata()) {
                include.add(ListSharesIncludeType.fromString(ListSharesIncludeType.METADATA.toString()));
            }

            if (options.includeSnapshots()) {
                include.add(ListSharesIncludeType.fromString(ListSharesIncludeType.SNAPSHOTS.toString()));
            }
        }

        Function<String, Mono<PagedResponse<ShareItem>>> retriever =
            nextMarker -> postProcessResponse(Utility.applyOptionalTimeout(this.azureFileStorageClient.services()
                .listSharesSegmentWithRestResponseAsync(prefix, nextMarker, maxResults, include, null, context), timeout)
            .map(response -> new PagedResponseBase<>(response.request(),
                response.statusCode(),
                response.headers(),
                response.value().shareItems(),
                response.value().nextMarker(),
                response.deserializedHeaders())));

        return new PagedFlux<>(() -> retriever.apply(marker), retriever);
    }

    /**
     * Retrieves the properties of the storage account's File service. The properties range from storage analytics and
     * metrics to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve File service properties</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file-service-properties">Azure Docs</a>.</p>
     *
     * @return Storage account {@link FileServiceProperties File service properties}
     */
    public Mono<FileServiceProperties> getProperties() {
        return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves the properties of the storage account's File service. The properties range from storage analytics and
     * metrics to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve File service properties</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.getPropertiesWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file-service-properties">Azure Docs</a>.</p>
     *
     * @return A response containing the Storage account {@link FileServiceProperties File service properties}
     */
    public Mono<Response<FileServiceProperties>> getPropertiesWithResponse() {
        return withContext(this::getPropertiesWithResponse);
    }

    Mono<Response<FileServiceProperties>> getPropertiesWithResponse(Context context) {
        return postProcessResponse(azureFileStorageClient.services().getPropertiesWithRestResponseAsync(context))
            .map(response -> new SimpleResponse<>(response, response.value()));
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
     * <p>Enable Minute and Hour Metrics</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.setProperties#fileServiceProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-service-properties">Azure Docs</a>.</p>
     *
     * @param properties Storage account File service properties
     * @return An empty response
     * @throws StorageException When one of the following is true
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
    public Mono<Void> setProperties(FileServiceProperties properties) {
        return setPropertiesWithResponse(properties).flatMap(FluxUtil::toMono);
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
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.setPropertiesWithResponse#fileServiceProperties.clearCORS}
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.setPropertiesWithResponseAsync#fileServiceProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-service-properties">Azure Docs</a>.</p>
     *
     * @param properties Storage account File service properties
     * @return A response that only contains headers and response status code
     * @throws StorageException When one of the following is true
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
    public Mono<VoidResponse> setPropertiesWithResponse(FileServiceProperties properties) {
        return withContext(context -> setPropertiesWithResponse(properties, context));
    }

    Mono<VoidResponse> setPropertiesWithResponse(FileServiceProperties properties, Context context) {
        return postProcessResponse(azureFileStorageClient.services()
            .setPropertiesWithRestResponseAsync(properties, context))
            .map(VoidResponse::new);
    }

    /**
     * Creates a share in the storage account with the specified name and returns a ShareAsyncClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share "test"</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.createShare#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @return The {@link ShareAsyncClient ShareAsyncClient}
     * @throws StorageException If a share with the same name already exists
     */
    public Mono<ShareAsyncClient> createShare(String shareName) {
        return createShareWithResponse(shareName, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a share in the storage account with the specified name, metadata, and quota and returns a ShareAsyncClient to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share "test" with metadata "share:metadata"</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.createShareWithResponse#string-map-integer.metadata}
     *
     * <p>Create the share "test" with a quota of 10 GB</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.createShareWithResponse#string-map-integer.quota}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @param metadata Optional metadata to associate with the share
     * @param quotaInGB Optional maximum size the share is allowed to grow to in GB. This must be greater than 0 and
     * less than or equal to 5120. The default value is 5120.
     * @return A response containing the {@link ShareAsyncClient ShareAsyncClient} and the status of creating the share.
     * @throws StorageException If a share with the same name already exists or {@code quotaInGB} is outside the
     * allowed range.
     */
    public Mono<Response<ShareAsyncClient>> createShareWithResponse(String shareName, Map<String, String> metadata, Integer quotaInGB) {
        return withContext(context -> createShareWithResponse(shareName, metadata, quotaInGB, context));
    }

    Mono<Response<ShareAsyncClient>> createShareWithResponse(String shareName, Map<String, String> metadata, Integer quotaInGB, Context context) {
        ShareAsyncClient shareAsyncClient = new ShareAsyncClient(azureFileStorageClient, shareName, null);

        return postProcessResponse(shareAsyncClient.createWithResponse(metadata, quotaInGB, context))
            .map(response -> new SimpleResponse<>(response, shareAsyncClient));
    }

    /**
     * Deletes the share in the storage account with the given name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share "test"</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.deleteShare#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @return An empty response
     * @throws StorageException If the share doesn't exist
     */
    public Mono<Void> deleteShare(String shareName) {
        return deleteShareWithResponse(shareName, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specific snapshot of the share in the storage account with the given name. Snapshot are identified
     * by the time they were created.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the snapshot of share "test" that was created at current time.</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.deleteShareWithResponse#string-string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @param snapshot Identifier of the snapshot
     * @return A response that only contains headers and response status code
     * @throws StorageException If the share doesn't exist or the snapshot doesn't exist
     */
    public Mono<VoidResponse> deleteShareWithResponse(String shareName, String snapshot) {
        return withContext(context -> deleteShareWithResponse(shareName, snapshot, context));
    }

    Mono<VoidResponse> deleteShareWithResponse(String shareName, String snapshot, Context context) {
        DeleteSnapshotsOptionType deleteSnapshots = null;
        if (ImplUtils.isNullOrEmpty(snapshot)) {
            deleteSnapshots = DeleteSnapshotsOptionType.fromString(DeleteSnapshotsOptionType.INCLUDE.toString());
        }
        return postProcessResponse(azureFileStorageClient.shares()
            .deleteWithRestResponseAsync(shareName, snapshot, null, deleteSnapshots, context))
            .map(VoidResponse::new);
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
        return this.generateAccountSAS(accountSASService, accountSASResourceType, accountSASPermission, expiryTime,
            null /* startTime */, null /* version */, null /* ipRange */, null /* sasProtocol */);
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

        SharedKeyCredential sharedKeyCredential = Utility.getSharedKeyCredential(this.azureFileStorageClient.getHttpPipeline());
        Utility.assertNotNull("sharedKeyCredential", sharedKeyCredential);

        return AccountSASSignatureValues.generateAccountSAS(sharedKeyCredential, accountSASService, accountSASResourceType, accountSASPermission, expiryTime, startTime, version, ipRange, sasProtocol);

    }
}
