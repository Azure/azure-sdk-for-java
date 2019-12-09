// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.AccountSasImplUtil;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.file.share.implementation.AzureFileStorageImpl;
import com.azure.storage.file.share.implementation.models.DeleteSnapshotsOptionType;
import com.azure.storage.file.share.implementation.models.ListSharesIncludeType;
import com.azure.storage.file.share.models.ShareCorsRule;
import com.azure.storage.file.share.models.ShareServiceProperties;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.models.ListSharesOptions;
import com.azure.storage.file.share.models.ShareItem;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a azureFileStorageClient that contains all the operations for interacting with a file account in
 * Azure Storage. Operations allowed by the azureFileStorageClient are creating, listing, and deleting shares and
 * retrieving and updating properties of the account.
 *
 * <p><strong>Instantiating an Asynchronous File Service Client</strong></p>
 *
 * {@codesnippet com.azure.storage.file.share.ShareServiceAsyncClient.instantiation}
 *
 * <p>View {@link ShareServiceClientBuilder this} for additional ways to construct the azureFileStorageClient.</p>
 *
 * @see ShareServiceClientBuilder
 * @see ShareServiceClient
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = ShareServiceClientBuilder.class, isAsync = true)
public final class ShareServiceAsyncClient {
    private final ClientLogger logger = new ClientLogger(ShareServiceAsyncClient.class);
    private final AzureFileStorageImpl azureFileStorageClient;
    private final String accountName;
    private final ShareServiceVersion serviceVersion;

    /**
     * Creates a ShareServiceClient from the passed {@link AzureFileStorageImpl implementation client}.
     *
     * @param azureFileStorage Client that interacts with the service interfaces.
     */
    ShareServiceAsyncClient(AzureFileStorageImpl azureFileStorage, String accountName,
                            ShareServiceVersion serviceVersion) {
        this.azureFileStorageClient = azureFileStorage;
        this.accountName = accountName;
        this.serviceVersion = serviceVersion;
    }

    /**
     * Get the url of the storage file service client.
     *
     * @return the url of the Storage File service.
     */
    public String getFileServiceUrl() {
        return azureFileStorageClient.getUrl();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public ShareServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Constructs a ShareAsyncClient that interacts with the specified share.
     *
     * <p>If the share doesn't exist in the storage account {@link ShareAsyncClient#create() create} in the
     * azureFileStorageClient will
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
     * <p>If the share doesn't exist in the storage account {@link ShareAsyncClient#create() create} in the
     * azureFileStorageClient will
     * need to be called before interaction with the share can happen.</p>
     *
     * @param shareName Name of the share
     * @param snapshot Snapshot ID of the share
     * @return a ShareAsyncClient that interacts with the specified share
     */
    public ShareAsyncClient getShareAsyncClient(String shareName, String snapshot) {
        return new ShareAsyncClient(azureFileStorageClient, shareName, snapshot, accountName, serviceVersion);
    }

    /**
     * Lists all shares in the storage account without their metadata or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all shares in the account</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareServiceAsyncClient.listShares}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-shares">Azure Docs</a>.</p>
     *
     * @return {@link ShareItem Shares} in the storage account without their metadata or snapshots
     */
    public PagedFlux<ShareItem> listShares() {
        try {
            return listShares(null);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
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
     * {@codesnippet com.azure.storage.file.share.ShareServiceAsyncClient.listShares#ListSharesOptions.prefix}
     *
     * <p>List all shares including their snapshots and metadata</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareServiceAsyncClient.listShares#ListSharesOptions.metadata.snapshot}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-shares">Azure Docs</a>.</p>
     *
     * @param options Options for listing shares
     * @return {@link ShareItem Shares} in the storage account that satisfy the filter requirements
     */
    public PagedFlux<ShareItem> listShares(ListSharesOptions options) {
        try {
            return listSharesWithOptionalTimeout(null, options, null, Context.NONE);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Lists the shares in the storage account that pass the filter starting at the specified marker.
     *
     * @param marker Starting point to list the shares
     * @param options Options for listing shares
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @return {@link ShareItem Shares} in the storage account that satisfy the filter requirements
     */
    PagedFlux<ShareItem> listSharesWithOptionalTimeout(String marker, ListSharesOptions options, Duration timeout,
        Context context) {
        final String prefix = (options != null) ? options.getPrefix() : null;
        final Integer maxResultsPerPage = (options != null) ? options.getMaxResultsPerPage() : null;
        List<ListSharesIncludeType> include = new ArrayList<>();

        if (options != null) {
            if (options.isIncludeMetadata()) {
                include.add(ListSharesIncludeType.METADATA);
            }

            if (options.isIncludeSnapshots()) {
                include.add(ListSharesIncludeType.SNAPSHOTS);
            }
        }

        Function<String, Mono<PagedResponse<ShareItem>>> retriever =
            nextMarker -> StorageImplUtils.applyOptionalTimeout(this.azureFileStorageClient.services()
                    .listSharesSegmentWithRestResponseAsync(
                        prefix, nextMarker, maxResultsPerPage, include, null, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue().getShareItems(),
                    response.getValue().getNextMarker(),
                    response.getDeserializedHeaders()));
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
     * {@codesnippet com.azure.storage.file.share.ShareServiceAsyncClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file-service-properties">Azure
     * Docs</a>.</p>
     *
     * @return Storage account {@link ShareServiceProperties File service properties}
     */
    public Mono<ShareServiceProperties> getProperties() {
        try {
            return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves the properties of the storage account's File service. The properties range from storage analytics and
     * metrics to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve File service properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareServiceAsyncClient.getPropertiesWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file-service-properties">Azure
     * Docs</a>.</p>
     *
     * @return A response containing the Storage account {@link ShareServiceProperties File service properties}
     */
    public Mono<Response<ShareServiceProperties>> getPropertiesWithResponse() {
        try {
            return withContext(this::getPropertiesWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareServiceProperties>> getPropertiesWithResponse(Context context) {
        return azureFileStorageClient.services().getPropertiesWithRestResponseAsync(context)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Sets the properties for the storage account's File service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * To maintain the CORS in the Queue service pass a {@code null} value for {@link ShareServiceProperties#getCors()
     * CORS}. To disable all CORS in the Queue service pass an empty list for {@link ShareServiceProperties#getCors()
     * CORS}.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareServiceAsyncClient.setProperties#fileServiceProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-service-properties">Azure
     * Docs</a>.</p>
     *
     * @param properties Storage account File service properties
     * @return An empty response
     * @throws ShareStorageException When one of the following is true
     * <ul>
     * <li>A CORS rule is missing one of its fields</li>
     * <li>More than five CORS rules will exist for the Queue service</li>
     * <li>Size of all CORS rules exceeds 2KB</li>
     * <li>
     * Length of {@link ShareCorsRule#getAllowedHeaders() allowed headers}, {@link ShareCorsRule#getExposedHeaders()
     * exposed headers}, or {@link ShareCorsRule#getAllowedOrigins() allowed origins} exceeds 256 characters.
     * </li>
     * <li>{@link ShareCorsRule#getAllowedMethods() Allowed methods} isn't DELETE, GET, HEAD, MERGE, POST, OPTIONS, or
     * PUT</li>
     * </ul>
     */
    public Mono<Void> setProperties(ShareServiceProperties properties) {
        try {
            return setPropertiesWithResponse(properties).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets the properties for the storage account's File service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * To maintain the CORS in the Queue service pass a {@code null} value for {@link ShareServiceProperties#getCors()
     * CORS}. To disable all CORS in the Queue service pass an empty list for {@link ShareServiceProperties#getCors()
     * CORS}.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p>Clear CORS in the File service</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareServiceAsyncClient.setPropertiesWithResponse#fileServiceProperties.clearCORS}
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareServiceAsyncClient.setPropertiesWithResponseAsync#fileServiceProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-service-properties">Azure
     * Docs</a>.</p>
     *
     * @param properties Storage account File service properties
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException When one of the following is true
     * <ul>
     * <li>A CORS rule is missing one of its fields</li>
     * <li>More than five CORS rules will exist for the Queue service</li>
     * <li>Size of all CORS rules exceeds 2KB</li>
     * <li>
     * Length of {@link ShareCorsRule#getAllowedHeaders() allowed headers}, {@link ShareCorsRule#getExposedHeaders()
     * exposed headers}, or {@link ShareCorsRule#getAllowedOrigins() allowed origins} exceeds 256 characters.
     * </li>
     * <li>{@link ShareCorsRule#getAllowedMethods() Allowed methods} isn't DELETE, GET, HEAD, MERGE, POST, OPTIONS, or
     * PUT</li>
     * </ul>
     */
    public Mono<Response<Void>> setPropertiesWithResponse(ShareServiceProperties properties) {
        try {
            return withContext(context -> setPropertiesWithResponse(properties, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> setPropertiesWithResponse(ShareServiceProperties properties, Context context) {
        return azureFileStorageClient.services().setPropertiesWithRestResponseAsync(properties, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Creates a share in the storage account with the specified name and returns a ShareAsyncClient to interact with
     * it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share "test"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareServiceAsyncClient.createShare#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @return The {@link ShareAsyncClient ShareAsyncClient}
     * @throws ShareStorageException If a share with the same name already exists
     */
    public Mono<ShareAsyncClient> createShare(String shareName) {
        try {
            return createShareWithResponse(shareName, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a share in the storage account with the specified name, metadata, and quota and returns a
     * ShareAsyncClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share "test" with metadata "share:metadata"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareServiceAsyncClient.createShareWithResponse#string-map-integer.metadata}
     *
     * <p>Create the share "test" with a quota of 10 GB</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareServiceAsyncClient.createShareWithResponse#string-map-integer.quota}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @param metadata Optional metadata to associate with the share
     * @param quotaInGB Optional maximum size the share is allowed to grow to in GB. This must be greater than 0 and
     * less than or equal to 5120. The default value is 5120.
     * @return A response containing the {@link ShareAsyncClient ShareAsyncClient} and the status of creating the share.
     * @throws ShareStorageException If a share with the same name already exists or {@code quotaInGB} is outside the
     * allowed range.
     */
    public Mono<Response<ShareAsyncClient>> createShareWithResponse(String shareName, Map<String, String> metadata,
        Integer quotaInGB) {
        try {
            return withContext(context -> createShareWithResponse(shareName, metadata, quotaInGB, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareAsyncClient>> createShareWithResponse(String shareName, Map<String, String> metadata,
        Integer quotaInGB, Context context) {
        ShareAsyncClient shareAsyncClient = new ShareAsyncClient(azureFileStorageClient, shareName, null,
            accountName, serviceVersion);

        return shareAsyncClient.createWithResponse(metadata, quotaInGB, context).map(response ->
            new SimpleResponse<>(response, shareAsyncClient));
    }

    /**
     * Deletes the share in the storage account with the given name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share "test"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareServiceAsyncClient.deleteShare#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @return An empty response
     * @throws ShareStorageException If the share doesn't exist
     */
    public Mono<Void> deleteShare(String shareName) {
        try {
            return deleteShareWithResponse(shareName, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the specific snapshot of the share in the storage account with the given name. Snapshot are identified by
     * the time they were created.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the snapshot of share "test" that was created at current time.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareServiceAsyncClient.deleteShareWithResponse#string-string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @param snapshot Identifier of the snapshot
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share doesn't exist or the snapshot doesn't exist
     */
    public Mono<Response<Void>> deleteShareWithResponse(String shareName, String snapshot) {
        try {
            return withContext(context -> deleteShareWithResponse(shareName, snapshot, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteShareWithResponse(String shareName, String snapshot, Context context) {
        DeleteSnapshotsOptionType deleteSnapshots = null;
        if (CoreUtils.isNullOrEmpty(snapshot)) {
            deleteSnapshots = DeleteSnapshotsOptionType.INCLUDE;
        }
        return azureFileStorageClient.shares()
            .deleteWithRestResponseAsync(shareName, snapshot, null, deleteSnapshots, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return this.azureFileStorageClient.getHttpPipeline();
    }

    /**
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * <p>The snippet below generates a SAS that lasts for two days and gives the user read and list access to blob
     * containers and file shares.</p>
     * {@codesnippet com.azure.storage.file.share.ShareServiceAsyncClient.generateAccountSas#AccountSasSignatureValues}
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     *
     * @return A {@code String} representing all SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues) {
        return new AccountSasImplUtil(accountSasSignatureValues)
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()));
    }
}
