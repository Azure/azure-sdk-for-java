// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.implementation.AzureFileStorageImpl;
import com.azure.storage.file.share.implementation.models.SharePermission;
import com.azure.storage.file.share.implementation.models.SharesCreateSnapshotHeaders;
import com.azure.storage.file.share.implementation.models.SharesCreateSnapshotResponse;
import com.azure.storage.file.share.implementation.models.SharesGetPropertiesHeaders;
import com.azure.storage.file.share.implementation.models.SharesGetPropertiesResponse;
import com.azure.storage.file.share.implementation.models.SharesGetStatisticsResponse;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.implementation.util.ShareSasImplUtil;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareInfo;
import com.azure.storage.file.share.models.ShareProperties;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareSignedIdentifier;
import com.azure.storage.file.share.models.ShareSnapshotInfo;
import com.azure.storage.file.share.models.ShareStatistics;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.options.ShareCreateOptions;
import com.azure.storage.file.share.options.ShareDeleteOptions;
import com.azure.storage.file.share.options.ShareGetAccessPolicyOptions;
import com.azure.storage.file.share.options.ShareGetPropertiesOptions;
import com.azure.storage.file.share.options.ShareGetStatisticsOptions;
import com.azure.storage.file.share.options.ShareSetAccessPolicyOptions;
import com.azure.storage.file.share.options.ShareSetMetadataOptions;
import com.azure.storage.file.share.options.ShareSetPropertiesOptions;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;

/**
 * This class provides a azureFileStorageClient that contains all the operations for interacting with a share in Azure
 * Storage Share. Operations allowed by the azureFileStorageClient are creating and deleting the share, creating
 * snapshots for the share, creating and deleting directories in the share and retrieving and updating properties
 * metadata and access policies of the share.
 *
 * <p><strong>Instantiating an Asynchronous Share Client</strong></p>
 *
 * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.instantiation}
 *
 * <p>View {@link ShareClientBuilder this} for additional ways to construct the azureFileStorageClient.</p>
 *
 * @see ShareClientBuilder
 * @see ShareClient
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = ShareClientBuilder.class, isAsync = true)
public class ShareAsyncClient {
    private final ClientLogger logger = new ClientLogger(ShareAsyncClient.class);

    private final AzureFileStorageImpl azureFileStorageClient;
    private final String shareName;
    private final String snapshot;
    private final String accountName;
    private final ShareServiceVersion serviceVersion;

    /**
     * Creates a ShareAsyncClient that sends requests to the storage share at {@link AzureFileStorageImpl#getUrl()
     * endpoint}. Each service call goes through the {@link HttpPipeline pipeline} in the
     * {@code azureFileStorageClient}.
     *
     * @param client Client that interacts with the service interfaces
     * @param shareName Name of the share
     */
    ShareAsyncClient(AzureFileStorageImpl client, String shareName, String snapshot, String accountName,
        ShareServiceVersion serviceVersion) {
        Objects.requireNonNull(shareName, "'shareName' cannot be null.");
        this.shareName = shareName;
        this.snapshot = snapshot;
        this.accountName = accountName;
        this.azureFileStorageClient = client;
        this.serviceVersion = serviceVersion;
    }

    /**
     * Get the url of the storage account.
     *
     * @return the URL of the storage account
     */
    public String getAccountUrl() {
        return azureFileStorageClient.getUrl();
    }

    /**
     * Get the url of the storage share client.
     *
     * @return the url of the Storage Share.
     */
    public String getShareUrl() {
        StringBuilder shareUrlString = new StringBuilder(azureFileStorageClient.getUrl()).append("/").append(shareName);
        if (snapshot != null) {
            shareUrlString.append("?sharesnapshot=").append(snapshot);
        }
        return shareUrlString.toString();
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
     * Constructs a {@link ShareDirectoryAsyncClient} that interacts with the root directory in the share.
     *
     * <p>If the directory doesn't exist in the share {@link ShareDirectoryAsyncClient#create()} in the
     * azureFileStorageClient will need to be called before interaction with the directory can happen.</p>
     *
     * @return a {@link ShareDirectoryAsyncClient} that interacts with the root directory in the share
     */
    public ShareDirectoryAsyncClient getRootDirectoryClient() {
        return getDirectoryClient("");
    }

    /**
     * Constructs a {@link ShareDirectoryAsyncClient} that interacts with the specified directory.
     *
     * <p>If the directory doesn't exist in the share {@link ShareDirectoryAsyncClient#create() create} in the
     * azureFileStorageClient will need to be called before interaction with the directory can happen.</p>
     *
     * @param directoryName Name of the directory
     * @return a {@link ShareDirectoryAsyncClient} that interacts with the directory in the share
     */
    public ShareDirectoryAsyncClient getDirectoryClient(String directoryName) {
        directoryName = "/".equals(directoryName)
            ? ""
            : directoryName;
        return new ShareDirectoryAsyncClient(azureFileStorageClient, shareName, directoryName, snapshot, accountName,
            serviceVersion);
    }

    /**
     * Constructs a {@link ShareFileAsyncClient} that interacts with the specified file.
     *
     * <p>If the file doesn't exist in the share {@link ShareFileAsyncClient#create(long)} ) create} in the client will
     * need to be called before interaction with the file can happen.</p>
     *
     * @param filePath Name of the file
     * @return a {@link ShareFileAsyncClient} that interacts with the file in the share
     */
    public ShareFileAsyncClient getFileClient(String filePath) {
        return new ShareFileAsyncClient(azureFileStorageClient, shareName, filePath, snapshot, accountName,
            serviceVersion);
    }

    /**
     * Creates a new {@link ShareAsyncClient} linked to the {@code snapshot} of this share resource.
     *
     * @param snapshot the identifier for a specific snapshot of this share
     * @return a {@link ShareAsyncClient} used to interact with the specific snapshot.
     */
    public ShareAsyncClient getSnapshotClient(String snapshot) {
        return new ShareAsyncClient(azureFileStorageClient, getShareName(), snapshot, getAccountName(),
            getServiceVersion());
    }

    /**
     * Determines if the share this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.exists}
     *
     * @return Flag indicating existence of the share.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> exists() {
        return existsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Determines if the share this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.existsWithResponse}
     *
     * @return Flag indicating existence of the share.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> existsWithResponse() {
        try {
            return withContext(context -> existsWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Boolean>> existsWithResponse(Context context) {
        return this.getPropertiesWithResponse(new ShareGetPropertiesOptions(), context)
            .map(cp -> (Response<Boolean>) new SimpleResponse<>(cp, true))
            .onErrorResume(t ->
                    t instanceof ShareStorageException && ((ShareStorageException) t).getStatusCode() == 404
                && ((ShareStorageException) t).getErrorCode() == ShareErrorCode.SHARE_NOT_FOUND,
                t -> {
                    HttpResponse response = ((ShareStorageException) t).getResponse();
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), false));
                });
    }

    /**
     * Creates the share in the storage account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.create}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @return The information about the {@link ShareInfo share}
     * @throws ShareStorageException If the share already exists with different metadata
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareInfo> create() {
        try {
            return createWithResponse(null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates the share in the storage account with the specified metadata and quota.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share with metadata "share:metadata"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.createWithResponse#map-integer.metadata}
     *
     * <p>Create the share with a quota of 10 GB</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.createWithResponse#map-integer.quota}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to associate with the share
     * @param quotaInGB Optional maximum size the share is allowed to grow to in GB. This must be greater than 0 and
     * less than or equal to 5120. The default value is 5120.
     * @return A response containing information about the {@link ShareInfo share} and the status its creation.
     * @throws ShareStorageException If the share already exists with different metadata or {@code quotaInGB} is outside
     * the allowed range.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareInfo>> createWithResponse(Map<String, String> metadata, Integer quotaInGB) {
        try {
            return withContext(context -> createWithResponse(new ShareCreateOptions().setMetadata(metadata)
                .setQuotaInGb(quotaInGB), context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates the share in the storage account with the specified options.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share with optional parameters</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.createWithResponse#ShareCreateOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param options {@link ShareCreateOptions}
     * @return A response containing information about the {@link ShareInfo share} and the status its creation.
     * @throws ShareStorageException If the share already exists with different metadata or {@code quotaInGB} is outside
     * the allowed range.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareInfo>> createWithResponse(ShareCreateOptions options) {
        try {
            return withContext(context -> createWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareInfo>> createWithResponse(ShareCreateOptions options, Context context) {
        context = context == null ? Context.NONE : context;
        options = options == null ? new ShareCreateOptions() : options;
        String enabledProtocol = options.getProtocols() == null ? null : options.getProtocols().toString();
        enabledProtocol = "".equals(enabledProtocol) ? null : enabledProtocol;
        return azureFileStorageClient.getShares()
            .createWithResponseAsync(shareName, null, options.getMetadata(), options.getQuotaInGb(),
                options.getAccessTier(), enabledProtocol, options.getRootSquash(),
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(this::mapToShareInfoResponse);
    }

    /**
     * Creates a snapshot of the share with the same metadata associated to the share at the time of creation.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a snapshot</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.createSnapshot}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/snapshot-share">Azure Docs</a>.</p>
     *
     * @return The information about the {@link ShareSnapshotInfo snapshot of share}.
     * @throws ShareStorageException If the share doesn't exist, there are 200 snapshots of the share, or a snapshot is
     * in progress for the share
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareSnapshotInfo> createSnapshot() {
        try {
            return createSnapshotWithResponse(null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a snapshot of the share with the metadata that was passed associated to the snapshot.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a snapshot with metadata "snapshot:metadata"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.createSnapshotWithResponse#map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/snapshot-share">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to associate with the snapshot. If {@code null} the metadata of the share will
     * be copied to the snapshot.
     * @return A response containing information about the {@link ShareSnapshotInfo snapshot of share}.
     * @throws ShareStorageException If the share doesn't exist, there are 200 snapshots of the share, or a snapshot is
     * in progress for the share
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareSnapshotInfo>> createSnapshotWithResponse(Map<String, String> metadata) {
        try {
            return withContext(context -> createSnapshotWithResponse(metadata, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareSnapshotInfo>> createSnapshotWithResponse(Map<String, String> metadata, Context context) {
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getShares().createSnapshotWithResponseAsync(shareName, null, metadata,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(this::mapCreateSnapshotResponse);
    }

    /**
     * Deletes the share in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.delete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @return An empty response
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> delete() {
        try {
            return deleteWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the share in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.deleteWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse() {
        try {
            return deleteWithResponse(new ShareDeleteOptions());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the share in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.deleteWithResponse#ShareDeleteOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param options {@link ShareDeleteOptions}
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse(ShareDeleteOptions options) {
        try {
            return withContext(context -> deleteWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteWithResponse(ShareDeleteOptions options, Context context) {
        options = options == null ? new ShareDeleteOptions() : options;
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getShares()
            .deleteWithResponseAsync(shareName, snapshot, null,
                ModelHelper.toDeleteSnapshotsOptionType(options.getDeleteSnapshotsOptions()),
                requestConditions.getLeaseId(),
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Retrieves the properties of the share, these include the metadata associated to it and the quota that the share
     * is restricted to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the share properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @return The {@link ShareProperties properties of the share}
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareProperties> getProperties() {
        try {
            return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves the properties of the share, these include the metadata associated with it and the quota that the share
     * is restricted to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the share properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.getPropertiesWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @return A response containing the {@link ShareProperties properties of the share} with headers and response
     * status code
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareProperties>> getPropertiesWithResponse() {
        try {
            return getPropertiesWithResponse(new ShareGetPropertiesOptions());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves the properties of the share, these include the metadata associated with it and the quota that the share
     * is restricted to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the share properties</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.getPropertiesWithResponse#ShareGetPropertiesOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @param options {@link ShareGetPropertiesOptions}
     * @return A response containing the {@link ShareProperties properties of the share} with headers and response
     * status code
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareProperties>> getPropertiesWithResponse(ShareGetPropertiesOptions options) {
        try {
            return withContext(context -> getPropertiesWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareProperties>> getPropertiesWithResponse(ShareGetPropertiesOptions options, Context context) {
        options = options == null ? new ShareGetPropertiesOptions() : options;
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getShares()
            .getPropertiesWithResponseAsync(shareName, snapshot, null, requestConditions.getLeaseId(),
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(this::mapGetPropertiesResponse);
    }

    /**
     * Sets the maximum size in GB that the share is allowed to grow.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the quota to 1024 GB</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.setQuota#int}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @param quotaInGB Size in GB to limit the share's growth. The quota in GB must be between 1 and 5120.
     * @return The {@link ShareInfo information about the share}
     * @throws ShareStorageException If the share doesn't exist or {@code quotaInGB} is outside the allowed bounds
     * @deprecated Use {@link ShareAsyncClient#setProperties(ShareSetPropertiesOptions)}
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareInfo> setQuota(int quotaInGB) {
        try {
            return setQuotaWithResponse(quotaInGB).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets the maximum size in GB that the share is allowed to grow.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the quota to 1024 GB</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.setQuotaWithResponse#int}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @param quotaInGB Size in GB to limit the share's growth. The quota in GB must be between 1 and 5120.
     * @return A response containing the {@link ShareInfo information about the share} with headers and response status
     * code
     * @throws ShareStorageException If the share doesn't exist or {@code quotaInGB} is outside the allowed bounds
     * @deprecated Use {@link ShareAsyncClient#setPropertiesWithResponse(ShareSetPropertiesOptions)}
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareInfo>> setQuotaWithResponse(int quotaInGB) {
        try {
            return setPropertiesWithResponse(new ShareSetPropertiesOptions().setQuotaInGb(quotaInGB));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets the share's properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.setProperties#ShareSetPropertiesOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-share-properties">Azure Docs</a>.</p>
     *
     * @param options {@link ShareSetPropertiesOptions}
     * @return The {@link ShareInfo information about the share}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareInfo> setProperties(ShareSetPropertiesOptions options) {
        try {
            return setPropertiesWithResponse(options).map(Response::getValue);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets the share's properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.setPropertiesWithResponse#ShareSetPropertiesOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-share-properties">Azure Docs</a>.</p>
     *
     * @param options {@link ShareSetPropertiesOptions}
     * @return A response containing the {@link ShareInfo information about the share} with headers and response status
     * code
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareInfo>> setPropertiesWithResponse(ShareSetPropertiesOptions options) {
        try {
            return withContext(context -> setPropertiesWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareInfo>> setPropertiesWithResponse(ShareSetPropertiesOptions options, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getShares().setPropertiesWithResponseAsync(shareName, null,
            options.getQuotaInGb(), options.getAccessTier(), requestConditions.getLeaseId(), options.getRootSquash(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(this::mapToShareInfoResponse);
    }

    /**
     * Sets the user-defined metadata to associate to the share.
     *
     * <p>If {@code null} is passed for the metadata it will clear the metadata associated to the share.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the metadata to "share:updatedMetadata"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.setMetadata#map}
     *
     * <p>Clear the metadata of the share</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.clearMetadata#map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-share-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to set on the share, if null is passed the metadata for the share is cleared
     * @return The {@link ShareInfo information about the share}
     * @throws ShareStorageException If the share doesn't exist or the metadata contains invalid keys
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareInfo> setMetadata(Map<String, String> metadata) {
        try {
            return setMetadataWithResponse(metadata).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets the user-defined metadata to associate to the share.
     *
     * <p>If {@code null} is passed for the metadata it will clear the metadata associated to the share.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the metadata to "share:updatedMetadata"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.setMetadata#map}
     *
     * <p>Clear the metadata of the share</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.clearMetadata#map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-share-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to set on the share, if null is passed the metadata for the share is cleared
     * @return A response containing the {@link ShareInfo information about the share} with headers and response status
     * code
     * @throws ShareStorageException If the share doesn't exist or the metadata contains invalid keys
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareInfo>> setMetadataWithResponse(Map<String, String> metadata) {
        try {
            return setMetadataWithResponse(new ShareSetMetadataOptions().setMetadata(metadata));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets the user-defined metadata to associate to the share.
     *
     * <p>If {@code null} is passed for the metadata it will clear the metadata associated to the share.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.setMetadataWithResponse#ShareSetMetadataOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-share-metadata">Azure Docs</a>.</p>
     *
     * @param options {@link ShareSetMetadataOptions}
     * @return A response containing the {@link ShareInfo information about the share} with headers and response status
     * code
     * @throws ShareStorageException If the share doesn't exist or the metadata contains invalid keys
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareInfo>> setMetadataWithResponse(ShareSetMetadataOptions options) {
        try {
            return withContext(context -> setMetadataWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareInfo>> setMetadataWithResponse(ShareSetMetadataOptions options, Context context) {
        options = options == null ? new ShareSetMetadataOptions() : options;
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getShares().setMetadataWithResponseAsync(shareName, null,
            options.getMetadata(), requestConditions.getLeaseId(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(this::mapToShareInfoResponse);
    }

    /**
     * Retrieves stored access policies specified for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List the stored access policies</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.getAccessPolicy}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-acl">Azure Docs</a>.</p>
     *
     * @return The stored access policies specified on the queue.
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ShareSignedIdentifier> getAccessPolicy() {
        try {
            return getAccessPolicy(new ShareGetAccessPolicyOptions());
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Retrieves stored access policies specified for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List the stored access policies</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.getAccessPolicy#ShareGetAccessPolicyOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-acl">Azure Docs</a>.</p>
     *
     * @param options {@link ShareGetAccessPolicyOptions}
     * @return The stored access policies specified on the queue.
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ShareSignedIdentifier> getAccessPolicy(ShareGetAccessPolicyOptions options) {
        ShareGetAccessPolicyOptions finalOptions = options == null ? new ShareGetAccessPolicyOptions() : options;
        ShareRequestConditions requestConditions = finalOptions.getRequestConditions() == null
            ? new ShareRequestConditions() : finalOptions.getRequestConditions();
        try {
            Function<String, Mono<PagedResponse<ShareSignedIdentifier>>> retriever =
                marker -> this.azureFileStorageClient.getShares()
                    .getAccessPolicyWithResponseAsync(shareName, null, requestConditions.getLeaseId(),
                        Context.NONE)
                    .map(response -> new PagedResponseBase<>(response.getRequest(),
                        response.getStatusCode(),
                        response.getHeaders(),
                        response.getValue(),
                        null,
                        response.getDeserializedHeaders()));

            return new PagedFlux<>(() -> retriever.apply(null), retriever);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Sets stored access policies for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.setAccessPolicy#List}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-share-acl">Azure Docs</a>.</p>
     *
     * @param permissions Access policies to set on the queue
     * @return The {@link ShareInfo information about the share}
     * @throws ShareStorageException If the share doesn't exist, a stored access policy doesn't have all fields filled
     * out, or the share will have more than five policies.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareInfo> setAccessPolicy(List<ShareSignedIdentifier> permissions) {
        try {
            return setAccessPolicyWithResponse(permissions).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets stored access policies for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.setAccessPolicyWithResponse#List}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-share-acl">Azure Docs</a>.</p>
     *
     * @param permissions Access policies to set on the queue
     * @return A response containing the {@link ShareInfo information about the share} with headers and response status
     * code
     * @throws ShareStorageException If the share doesn't exist, a stored access policy doesn't have all fields filled
     * out, or the share will have more than five policies.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareInfo>> setAccessPolicyWithResponse(List<ShareSignedIdentifier> permissions) {
        try {
            return withContext(context -> setAccessPolicyWithResponse(new ShareSetAccessPolicyOptions()
                .setPermissions(permissions), context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets stored access policies for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.setAccessPolicyWithResponse#ShareSetAccessPolicyOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-share-acl">Azure Docs</a>.</p>
     *
     * @param options {@link ShareSetAccessPolicyOptions}
     * @return A response containing the {@link ShareInfo information about the share} with headers and response status
     * code
     * @throws ShareStorageException If the share doesn't exist, a stored access policy doesn't have all fields filled
     * out, or the share will have more than five policies.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareInfo>> setAccessPolicyWithResponse(ShareSetAccessPolicyOptions options) {
        try {
            return withContext(context -> setAccessPolicyWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareInfo>> setAccessPolicyWithResponse(ShareSetAccessPolicyOptions options, Context context) {
        options = options == null ? new ShareSetAccessPolicyOptions() : options;
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        List<ShareSignedIdentifier> permissions = options.getPermissions();
        /*
        We truncate to seconds because the service only supports nanoseconds or seconds, but doing an
        OffsetDateTime.now will only give back milliseconds (more precise fields are zeroed and not serialized). This
        allows for proper serialization with no real detriment to users as sub-second precision on active time for
        signed identifiers is not really necessary.
         */
        if (permissions != null) {
            for (ShareSignedIdentifier permission : permissions) {
                if (permission.getAccessPolicy() != null && permission.getAccessPolicy().getStartsOn() != null) {
                    permission.getAccessPolicy().setStartsOn(
                        permission.getAccessPolicy().getStartsOn().truncatedTo(ChronoUnit.SECONDS));
                }
                if (permission.getAccessPolicy() != null && permission.getAccessPolicy().getExpiresOn() != null) {
                    permission.getAccessPolicy().setExpiresOn(
                        permission.getAccessPolicy().getExpiresOn().truncatedTo(ChronoUnit.SECONDS));
                }
            }
        }
        context = context == null ? Context.NONE : context;

        return azureFileStorageClient.getShares()
            .setAccessPolicyWithResponseAsync(shareName, null, requestConditions.getLeaseId(), permissions,
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(this::mapToShareInfoResponse);
    }

    /**
     * Retrieves storage statistics about the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the storage statistics</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.getStatistics}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-stats">Azure Docs</a>.</p>
     *
     * @return The storage {@link ShareStatistics statistics of the share}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareStatistics> getStatistics() {
        try {
            return getStatisticsWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves storage statistics about the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the storage statistics</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.getStatisticsWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-stats">Azure Docs</a>.</p>
     *
     * @return A response containing the storage {@link ShareStatistics statistics of the share} with headers and
     * response status code
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareStatistics>> getStatisticsWithResponse() {
        try {
            return getStatisticsWithResponse(new ShareGetStatisticsOptions());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves storage statistics about the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the storage statistics</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.getStatisticsWithResponse#ShareGetStatisticsOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-stats">Azure Docs</a>.</p>
     *
     * @param options {@link ShareGetStatisticsOptions}
     * @return A response containing the storage {@link ShareStatistics statistics of the share} with headers and
     * response status code
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareStatistics>> getStatisticsWithResponse(ShareGetStatisticsOptions options) {
        try {
            return withContext(context -> getStatisticsWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareStatistics>> getStatisticsWithResponse(ShareGetStatisticsOptions options, Context context) {
        options = options == null ? new ShareGetStatisticsOptions() : options;
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getShares().getStatisticsWithResponseAsync(shareName, null,
            requestConditions.getLeaseId(), context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(this::mapGetStatisticsResponse);
    }

    /**
     * Creates the directory in the share with the given name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory "mydirectory"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.createDirectory#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @return The {@link ShareDirectoryAsyncClient} to interact with the created directory.
     * @throws ShareStorageException If the share doesn't exist, the directory already exists or is in the process of
     * being deleted, or the parent directory for the new directory doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareDirectoryAsyncClient> createDirectory(String directoryName) {
        try {
            return createDirectoryWithResponse(directoryName, null, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates the directory in the share with the given name and associates the passed metadata to it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory "documents" with metadata "directory:metadata"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.createDirectoryWithResponse#String-FileSmbProperties-String-Map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param metadata Optional metadata to associate with the directory
     * @return A response containing a {@link ShareDirectoryAsyncClient} to interact with the created directory and the
     * status of its creation.
     * @throws ShareStorageException If the share doesn't exist, the directory already exists or is in the process of
     * being deleted, the parent directory for the new directory doesn't exist, or the metadata is using an illegal key
     * name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareDirectoryAsyncClient>> createDirectoryWithResponse(String directoryName,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata) {
        try {
            return withContext(context ->
                createDirectoryWithResponse(directoryName, smbProperties, filePermission, metadata, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareDirectoryAsyncClient>> createDirectoryWithResponse(String directoryName,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata, Context context) {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = getDirectoryClient(directoryName);
        return shareDirectoryAsyncClient.createWithResponse(smbProperties, filePermission, metadata)
            .map(response -> new SimpleResponse<>(response, shareDirectoryAsyncClient));
    }

    /**
     * Creates the file in the share with the given name and file max size.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file "myfile" with size of 1024 bytes.</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.createFile#string-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file.
     * @param maxSize The maximum size in bytes for the file.
     * @return The {@link ShareFileAsyncClient} to interact with the created file.
     * @throws ShareStorageException If one of the following cases happen:
     * <ul>
     * <li>
     * If the share or parent directory does not exist.
     * </li>
     * <li>
     * An attempt to create file on a share snapshot will fail with 400 (InvalidQueryParameterValue).
     * </li>
     * </ul>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareFileAsyncClient> createFile(String fileName, long maxSize) {
        try {
            return createFileWithResponse(fileName, maxSize, null, null, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates the file in the share with the given name, file max size and associates the passed properties to it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file "myfile" with length of 1024 bytes, some headers, file smb properties and metadata</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file.
     * @param maxSize The maximum size in bytes for the file.
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file.
     * @param metadata Optional name-value pairs associated with the file as metadata.
     * @return A response containing a {@link ShareFileAsyncClient} to interact with the created file and the status of
     * its creation.
     * @throws ShareStorageException If one of the following cases happen:
     * <ul>
     * <li>
     * If the share or parent directory does not exist.
     * </li>
     * <li>
     * An attempt to create file on a share snapshot will fail with 400 (InvalidQueryParameterValue).
     * </li>
     * </ul>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileAsyncClient>> createFileWithResponse(String fileName, long maxSize,
        ShareFileHttpHeaders httpHeaders, FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata) {
        return this.createFileWithResponse(fileName, maxSize, httpHeaders, smbProperties, filePermission, metadata,
            null);
    }

    /**
     * Creates the file in the share with the given name, file max size and associates the passed properties to it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file "myfile" with length of 1024 bytes, some headers, file smb properties and metadata</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file.
     * @param maxSize The maximum size in bytes for the file.
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file.
     * @param metadata Optional name-value pairs associated with the file as metadata.
     * @param requestConditions {@link ShareRequestConditions}
     * @return A response containing a {@link ShareFileAsyncClient} to interact with the created file and the status of
     * its creation.
     * @throws ShareStorageException If one of the following cases happen:
     * <ul>
     * <li>
     * If the share or parent directory does not exist.
     * </li>
     * <li>
     * An attempt to create file on a share snapshot will fail with 400 (InvalidQueryParameterValue).
     * </li>
     * </ul>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileAsyncClient>> createFileWithResponse(String fileName, long maxSize,
        ShareFileHttpHeaders httpHeaders, FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata, ShareRequestConditions requestConditions) {
        try {
            return withContext(context ->
                createFileWithResponse(fileName, maxSize, httpHeaders, smbProperties, filePermission, metadata,
                    requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ShareFileAsyncClient>> createFileWithResponse(String fileName, long maxSize,
        ShareFileHttpHeaders httpHeaders, FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata, ShareRequestConditions requestConditions, Context context) {
        ShareFileAsyncClient shareFileAsyncClient = getFileClient(fileName);
        return shareFileAsyncClient
            .createWithResponse(maxSize, httpHeaders, smbProperties, filePermission, metadata, requestConditions,
                context).map(response -> new SimpleResponse<>(response, shareFileAsyncClient));
    }

    /**
     * Deletes the specified directory in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory "mydirectory"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.deleteDirectory#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @return An empty response
     * @throws ShareStorageException If the share doesn't exist or the directory isn't empty
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteDirectory(String directoryName) {
        try {
            return deleteDirectoryWithResponse(directoryName).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the specified directory in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory "mydirectory"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.deleteDirectory#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share doesn't exist or the directory isn't empty
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDirectoryWithResponse(String directoryName) {
        try {
            return withContext(context -> deleteDirectoryWithResponse(directoryName, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteDirectoryWithResponse(String directoryName, Context context) {
        return getDirectoryClient(directoryName).deleteWithResponse(context);
    }

    /**
     * Deletes the specified file in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "myfile"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.deleteFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file.
     * @return A empty response
     * @throws ShareStorageException If the share or the file doesn't exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteFile(String fileName) {
        try {
            return deleteFileWithResponse(fileName).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the specified file in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "myfile"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.deleteFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file.
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share or the file doesn't exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteFileWithResponse(String fileName) {
        return this.deleteFileWithResponse(fileName, null);
    }

    /**
     * Deletes the specified file in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "myfile"</p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.deleteFile#string-ShareRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file.
     * @param requestConditions {@link ShareRequestConditions}
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share or the file doesn't exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteFileWithResponse(String fileName, ShareRequestConditions requestConditions) {
        try {
            return withContext(context -> deleteFileWithResponse(fileName, requestConditions,
                context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteFileWithResponse(String fileName, ShareRequestConditions requestConditions,
        Context context) {
        return getFileClient(fileName).deleteWithResponse(requestConditions, context);
    }

    /**
     * Creates a permission at the share level. If a permission already exists, it returns the key of it, else creates a
     * new permission and returns the key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.createPermission#string}
     *
     * @param filePermission The file permission to get/create.
     * @return The file permission key associated with the file permission.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> createPermission(String filePermission) {
        try {
            return createPermissionWithResponse(filePermission).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a permission at the share level. If a permission already exists, it returns the key of it, else creates a
     * new permission and returns the key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.createPermissionWithResponse#string}
     *
     * @param filePermission The file permission to get/create.
     * @return A response that contains the file permission key associated with the file permission.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> createPermissionWithResponse(String filePermission) {
        try {
            return withContext(context -> createPermissionWithResponse(filePermission, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<String>> createPermissionWithResponse(String filePermission, Context context) {
        // NOTE: Should we check for null or empty?
        SharePermission sharePermission = new SharePermission().setPermission(filePermission);
        return azureFileStorageClient.getShares()
            .createPermissionWithResponseAsync(shareName, sharePermission, null, context)
            .map(response -> new SimpleResponse<>(response,
                response.getDeserializedHeaders().getXMsFilePermissionKey()));
    }

    /**
     * Gets a permission for a given key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.getPermission#string}
     *
     * @param filePermissionKey The file permission key.
     * @return The file permission associated with the file permission key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> getPermission(String filePermissionKey) {
        try {
            return getPermissionWithResponse(filePermissionKey).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a permission for a given key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.getPermissionWithResponse#string}
     *
     * @param filePermissionKey The file permission key.
     * @return A response that contains th file permission associated with the file permission key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> getPermissionWithResponse(String filePermissionKey) {
        try {
            return withContext(context -> getPermissionWithResponse(filePermissionKey, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<String>> getPermissionWithResponse(String filePermissionKey, Context context) {
        return azureFileStorageClient.getShares()
            .getPermissionWithResponseAsync(shareName, filePermissionKey, null, context)
            .map(response -> new SimpleResponse<>(response, response.getValue().getPermission()));
    }

    /**
     * Get snapshot id which attached to {@link ShareAsyncClient}. Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.getSnapshotId}
     *
     * @return The snapshot id which is a unique {@code DateTime} value that identifies the share snapshot to its base
     * share.
     */
    public String getSnapshotId() {
        return this.snapshot;
    }

    /**
     * Get share name from share client.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.getShareName}
     *
     * @return The name of the share.
     */
    public String getShareName() {
        return shareName;
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
        return azureFileStorageClient.getHttpPipeline();
    }

    /**
     * Generates a service sas for the queue using the specified {@link ShareServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link ShareServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.generateSas#ShareServiceSasSignatureValues}
     *
     * @param shareServiceSasSignatureValues {@link ShareServiceSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(ShareServiceSasSignatureValues shareServiceSasSignatureValues) {
        return generateSas(shareServiceSasSignatureValues, Context.NONE);
    }

    /**
     * Generates a service sas for the queue using the specified {@link ShareServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link ShareServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.ShareAsyncClient.generateSas#ShareServiceSasSignatureValues-Context}
     *
     * @param shareServiceSasSignatureValues {@link ShareServiceSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(ShareServiceSasSignatureValues shareServiceSasSignatureValues, Context context) {
        return new ShareSasImplUtil(shareServiceSasSignatureValues, getShareName())
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), context);
    }

    private Response<ShareInfo> mapToShareInfoResponse(Response<?> response) {
        String eTag = response.getHeaders().getValue("ETag");
        OffsetDateTime lastModified =
            new DateTimeRfc1123(response.getHeaders().getValue("Last-Modified")).getDateTime();

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            new ShareInfo(eTag, lastModified));
    }

    private Response<ShareSnapshotInfo> mapCreateSnapshotResponse(SharesCreateSnapshotResponse response) {
        SharesCreateSnapshotHeaders headers = response.getDeserializedHeaders();
        ShareSnapshotInfo snapshotInfo =
            new ShareSnapshotInfo(headers.getXMsSnapshot(), headers.getETag(), headers.getLastModified());

        return new SimpleResponse<>(response, snapshotInfo);
    }

    private Response<ShareProperties> mapGetPropertiesResponse(SharesGetPropertiesResponse response) {
        SharesGetPropertiesHeaders headers = response.getDeserializedHeaders();
        ShareProperties shareProperties = new ShareProperties()
            .setETag(headers.getETag())
            .setLastModified(headers.getLastModified())
            .setMetadata(headers.getXMsMeta())
            .setQuota(headers.getXMsShareQuota())
            .setNextAllowedQuotaDowngradeTime(headers.getXMsShareNextAllowedQuotaDowngradeTime())
            .setProvisionedEgressMBps(headers.getXMsShareProvisionedEgressMbps())
            .setProvisionedIngressMBps(headers.getXMsShareProvisionedIngressMbps())
            .setProvisionedIops(headers.getXMsShareProvisionedIops())
            .setLeaseDuration(headers.getXMsLeaseDuration())
            .setLeaseState(headers.getXMsLeaseState())
            .setLeaseStatus(headers.getXMsLeaseStatus())
            .setAccessTier(headers.getXMsAccessTier())
            .setAccessTierChangeTime(headers.getXMsAccessTierChangeTime())
            .setAccessTierTransitionState(headers.getXMsAccessTierTransitionState())
            .setProtocols(ModelHelper.parseShareProtocols(headers.getXMsEnabledProtocols()))
            .setRootSquash(headers.getXMsRootSquash());

        return new SimpleResponse<>(response, shareProperties);
    }

    private Response<ShareStatistics> mapGetStatisticsResponse(SharesGetStatisticsResponse response) {
        ShareStatistics shareStatistics =
            new ShareStatistics(response.getValue().getShareUsageBytes());

        return new SimpleResponse<>(response, shareStatistics);
    }
}
