// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.implementation.AzureFileStorageImpl;
import com.azure.storage.file.share.implementation.models.FilePermissionFormat;
import com.azure.storage.file.share.implementation.models.SharePermission;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.implementation.util.ShareSasImplUtil;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareFilePermission;
import com.azure.storage.file.share.models.ShareInfo;
import com.azure.storage.file.share.models.ShareProperties;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareSignedIdentifier;
import com.azure.storage.file.share.models.ShareSnapshotInfo;
import com.azure.storage.file.share.models.ShareStatistics;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.options.ShareCreateOptions;
import com.azure.storage.file.share.options.ShareDeleteOptions;
import com.azure.storage.file.share.options.ShareDirectoryCreateOptions;
import com.azure.storage.file.share.options.ShareGetAccessPolicyOptions;
import com.azure.storage.file.share.options.ShareGetPropertiesOptions;
import com.azure.storage.file.share.options.ShareGetStatisticsOptions;
import com.azure.storage.file.share.options.ShareSetAccessPolicyOptions;
import com.azure.storage.file.share.options.ShareSetMetadataOptions;
import com.azure.storage.file.share.options.ShareSetPropertiesOptions;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a azureFileStorageClient that contains all the operations for interacting with a share in Azure
 * Storage Share. Operations allowed by the azureFileStorageClient are creating and deleting the share, creating
 * snapshots for the share, creating and deleting directories in the share and retrieving and updating properties
 * metadata and access policies of the share.
 *
 * <p><strong>Instantiating an Asynchronous Share Client</strong></p>
 *
 * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.instantiation -->
 * <pre>
 * ShareAsyncClient client = new ShareClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;$&#123;connectionString&#125;&quot;&#41;
 *     .endpoint&#40;&quot;$&#123;endpoint&#125;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.share.ShareAsyncClient.instantiation -->
 *
 * <p>View {@link ShareClientBuilder this} for additional ways to construct the azureFileStorageClient.</p>
 *
 * @see ShareClientBuilder
 * @see ShareClient
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = ShareClientBuilder.class, isAsync = true)
public class ShareAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(ShareAsyncClient.class);

    private final AzureFileStorageImpl azureFileStorageClient;
    private final String shareName;
    private final String snapshot;
    private final String accountName;
    private final ShareServiceVersion serviceVersion;
    private final AzureSasCredential sasToken;

    /**
     * Creates a ShareAsyncClient that sends requests to the storage share at {@link AzureFileStorageImpl#getUrl()
     * endpoint}. Each service call goes through the {@link HttpPipeline pipeline} in the
     * {@code azureFileStorageClient}.
     *
     * @param client Client that interacts with the service interfaces
     * @param shareName Name of the share
     * @param sasToken The SAS token to use for authenticating requests.
     */
    ShareAsyncClient(AzureFileStorageImpl client, String shareName, String snapshot, String accountName,
        ShareServiceVersion serviceVersion, AzureSasCredential sasToken) {
        Objects.requireNonNull(shareName, "'shareName' cannot be null.");
        this.shareName = shareName;
        this.snapshot = snapshot;
        this.accountName = accountName;
        this.azureFileStorageClient = client;
        this.serviceVersion = serviceVersion;
        this.sasToken = sasToken;
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

    AzureSasCredential getSasToken() {
        return sasToken;
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
            serviceVersion, sasToken);
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
            serviceVersion, sasToken);
    }

    /**
     * Creates a new {@link ShareAsyncClient} linked to the {@code snapshot} of this share resource.
     *
     * @param snapshot the identifier for a specific snapshot of this share
     * @return a {@link ShareAsyncClient} used to interact with the specific snapshot.
     */
    public ShareAsyncClient getSnapshotClient(String snapshot) {
        return new ShareAsyncClient(azureFileStorageClient, getShareName(), snapshot, getAccountName(),
            getServiceVersion(), getSasToken());
    }

    /**
     * Determines if the share this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.exists -->
     * <pre>
     * client.exists&#40;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Exists? %b%n&quot;, response&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.exists -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.existsWithResponse -->
     * <pre>
     * client.existsWithResponse&#40;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Exists? %b%n&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.existsWithResponse -->
     *
     * @return Flag indicating existence of the share.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> existsWithResponse() {
        try {
            return withContext(this::existsWithResponse);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.create -->
     * <pre>
     * shareAsyncClient.create&#40;&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *     &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete creating the share!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.create -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @return The information about the {@link ShareInfo share}
     * @throws ShareStorageException If the share already exists with different metadata
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareInfo> create() {
        return createWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates the share in the storage account with the specified metadata and quota.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share with metadata "share:metadata"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createWithResponse#map-integer.metadata -->
     * <pre>
     * shareAsyncClient.createWithResponse&#40;Collections.singletonMap&#40;&quot;share&quot;, &quot;metadata&quot;&#41;, null&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Creating the share completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete creating the share!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createWithResponse#map-integer.metadata -->
     *
     * <p>Create the share with a quota of 10 GB</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createWithResponse#map-integer.quota -->
     * <pre>
     * shareAsyncClient.createWithResponse&#40;null, 10&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Creating the share completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete creating the share!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createWithResponse#map-integer.quota -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * <p>For more information on updated max file share size values, see the
     * <a href="https://learn.microsoft.com/azure/storage/files/storage-files-scale-targets#azure-file-share-scale-targets">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to associate with the share
     * @param quotaInGB Optional maximum size the share is allowed to grow to in GB.  The default value is 5120.
     * Refer to the Azure Docs for updated values.
     * @return A response containing information about the {@link ShareInfo share} and the status its creation.
     * @throws ShareStorageException If the share already exists with different metadata or {@code quotaInGB} is outside
     * the allowed range.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareInfo>> createWithResponse(Map<String, String> metadata, Integer quotaInGB) {
        return createWithResponse(new ShareCreateOptions().setMetadata(metadata).setQuotaInGb(quotaInGB));
    }

    /**
     * Creates the share in the storage account with the specified options.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share with optional parameters</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createWithResponse#ShareCreateOptions -->
     * <pre>
     * shareAsyncClient.createWithResponse&#40;new ShareCreateOptions&#40;&#41;
     *     .setMetadata&#40;Collections.singletonMap&#40;&quot;share&quot;, &quot;metadata&quot;&#41;&#41;.setQuotaInGb&#40;1&#41;
     *     .setAccessTier&#40;ShareAccessTier.HOT&#41;&#41;.subscribe&#40;
     *         response -&gt; System.out.printf&#40;&quot;Creating the share completed with status code %d&quot;,
     *             response.getStatusCode&#40;&#41;&#41;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete creating the share!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createWithResponse#ShareCreateOptions -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareInfo>> createWithResponse(ShareCreateOptions options, Context context) {
        context = context == null ? Context.NONE : context;
        options = options == null ? new ShareCreateOptions() : options;
        String enabledProtocol = options.getProtocols() == null ? null : options.getProtocols().toString();
        enabledProtocol = "".equals(enabledProtocol) ? null : enabledProtocol;
        return azureFileStorageClient.getShares()
            .createNoCustomHeadersWithResponseAsync(shareName, null, options.getMetadata(), options.getQuotaInGb(),
                options.getAccessTier(), enabledProtocol, options.getRootSquash(),
                options.isSnapshotVirtualDirectoryAccessEnabled(), null, null,
                null, context)
            .map(ModelHelper::mapToShareInfoResponse);
    }

    /**
     * Creates the share in the storage account if it does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createIfNotExists -->
     * <pre>
     * shareAsyncClient.createIfNotExists&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Created at %s%n&quot;, response.getLastModified&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createIfNotExists -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @return A reactive response {@link Mono} signaling completion. {@link ShareInfo} contains information about the
     * created share.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareInfo> createIfNotExists() {
        return createIfNotExistsWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates the share in the storage account with the specified options if it does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share with optional parameters</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createIfNotExistsWithResponse#ShareCreateOptions -->
     * <pre>
     * shareAsyncClient.createIfNotExistsWithResponse&#40;new ShareCreateOptions&#40;&#41;
     *     .setMetadata&#40;Collections.singletonMap&#40;&quot;share&quot;, &quot;metadata&quot;&#41;&#41;.setQuotaInGb&#40;1&#41;
     *     .setAccessTier&#40;ShareAccessTier.HOT&#41;&#41;.subscribe&#40;response -&gt; &#123;
     *         if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *             System.out.println&#40;&quot;Already exists.&quot;&#41;;
     *         &#125; else &#123;
     *             System.out.println&#40;&quot;successfully created.&quot;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createIfNotExistsWithResponse#ShareCreateOptions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param options {@link ShareCreateOptions}
     * @return A {@link Mono} containing {@link Response} signaling completion, whose {@link Response#getValue() value}
     * contains a {@link ShareInfo} containing information about the share. If {@link Response}'s status code is
     * 201, a new share was successfully created. If status code is 409, a share already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareInfo>> createIfNotExistsWithResponse(ShareCreateOptions options) {
        try {
            return createIfNotExistsWithResponse(options, null);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareInfo>> createIfNotExistsWithResponse(ShareCreateOptions options, Context context) {
        try {
            options = options == null ? new ShareCreateOptions() : options;
            return createWithResponse(options, context).onErrorResume(t -> t instanceof ShareStorageException
                && ((ShareStorageException) t).getStatusCode() == 409, t -> {
                    HttpResponse response = ((ShareStorageException) t).getResponse();
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), null));
                });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a snapshot of the share with the same metadata associated to the share at the time of creation.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a snapshot</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createSnapshot -->
     * <pre>
     * shareAsyncClient.createSnapshot&#40;&#41;.subscribe&#40;
     *     response -&gt; System.out.println&#40;&quot;Successfully creating the share snapshot with snapshot id: &quot;
     *         + response.getSnapshot&#40;&#41;&#41;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete creating the share snapshot.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createSnapshot -->
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
        return createSnapshotWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a snapshot of the share with the metadata that was passed associated to the snapshot.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a snapshot with metadata "snapshot:metadata"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createSnapshotWithResponse#map -->
     * <pre>
     * shareAsyncClient.createSnapshotWithResponse&#40;Collections.singletonMap&#40;&quot;snapshot&quot;, &quot;metadata&quot;&#41;&#41;.subscribe&#40;
     *     response -&gt; System.out.println&#40;&quot;Successfully creating the share snapshot with snapshot id: &quot;
     *         + response.getValue&#40;&#41;.getSnapshot&#40;&#41;&#41;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete creating the share snapshot.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createSnapshotWithResponse#map -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareSnapshotInfo>> createSnapshotWithResponse(Map<String, String> metadata, Context context) {
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getShares().createSnapshotWithResponseAsync(shareName, null, metadata, context)
            .map(ModelHelper::mapCreateSnapshotResponse);
    }

    /**
     * Deletes the share in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.delete -->
     * <pre>
     * shareAsyncClient.delete&#40;&#41;.subscribe&#40;
     *     response -&gt; System.out.println&#40;&quot;Deleting the shareAsyncClient completed.&quot;&#41;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the share.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.delete -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @return An empty response
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> delete() {
        return deleteWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the share in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.deleteWithResponse -->
     * <pre>
     * shareAsyncClient.deleteWithResponse&#40;&#41;.subscribe&#40;
     *     response -&gt; System.out.println&#40;&quot;Deleting the shareAsyncClient completed with status code: &quot;
     *         + response.getStatusCode&#40;&#41;&#41;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the share.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.deleteWithResponse -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse() {
        return deleteWithResponse(new ShareDeleteOptions());
    }

    /**
     * Deletes the share in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.deleteWithResponse#ShareDeleteOptions -->
     * <pre>
     * shareAsyncClient.deleteWithResponse&#40;new ShareDeleteOptions&#40;&#41;
     *     .setRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;&#41;.subscribe&#40;
     *         response -&gt; System.out.println&#40;&quot;Deleting the shareAsyncClient completed with status code: &quot;
     *             + response.getStatusCode&#40;&#41;&#41;, error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the share.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.deleteWithResponse#ShareDeleteOptions -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteWithResponse(ShareDeleteOptions options, Context context) {
        options = options == null ? new ShareDeleteOptions() : options;
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getShares().deleteNoCustomHeadersWithResponseAsync(shareName, snapshot, null,
            ModelHelper.toDeleteSnapshotsOptionType(options.getDeleteSnapshotsOptions()),
            requestConditions.getLeaseId(), context);
    }

    /**
     * Deletes the share in the storage account if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.deleteIfExists -->
     * <pre>
     * shareAsyncClient.deleteIfExists&#40;&#41;.subscribe&#40;deleted -&gt; &#123;
     *     if &#40;deleted&#41; &#123;
     *         System.out.println&#40;&quot;Successfully deleted.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.deleteIfExists -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @return a reactive response signaling completion. {@code true} indicates that the share was successfully
     * deleted, {@code false} indicates that the share did not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteIfExists() {
        return deleteIfExistsWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the share in the storage account if it exists
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.deleteIfExistsWithResponse#ShareDeleteOptions -->
     * <pre>
     * shareAsyncClient.deleteIfExistsWithResponse&#40;new ShareDeleteOptions&#40;&#41;
     *     .setRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;&#41;.subscribe&#40;response -&gt; &#123;
     *         if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *             System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *         &#125; else &#123;
     *             System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.deleteIfExistsWithResponse#ShareDeleteOptions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param options {@link ShareDeleteOptions}
     * @return A reactive response signaling completion. If {@link Response}'s status code is 202, the share was
     * successfully deleted. If status code is 404, the share does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteIfExistsWithResponse(ShareDeleteOptions options) {
        try {
            return withContext(context -> deleteIfExistsWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Boolean>> deleteIfExistsWithResponse(ShareDeleteOptions options, Context context) {
        return deleteWithResponse(options, context)
            .map(response -> (Response<Boolean>) new SimpleResponse<>(response, true))
            .onErrorResume(t -> t instanceof ShareStorageException && ((ShareStorageException) t).getStatusCode() == 404,
                t -> {
                    HttpResponse response = ((ShareStorageException) t).getResponse();
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), false));
                });
    }

    /**
     * Retrieves the properties of the share, these include the metadata associated to it and the quota that the share
     * is restricted to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the share properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.getProperties -->
     * <pre>
     * shareAsyncClient.getProperties&#40;&#41;
     *     .subscribe&#40;properties -&gt; &#123;
     *         System.out.printf&#40;&quot;Share quota: %d, Metadata: %s&quot;, properties.getQuota&#40;&#41;, properties.getMetadata&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.getProperties -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @return The {@link ShareProperties properties of the share}
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareProperties> getProperties() {
        return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves the properties of the share, these include the metadata associated with it and the quota that the share
     * is restricted to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the share properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.getPropertiesWithResponse -->
     * <pre>
     * shareAsyncClient.getPropertiesWithResponse&#40;&#41;
     *     .subscribe&#40;properties -&gt; &#123;
     *         System.out.printf&#40;&quot;Share quota: %d, Metadata: %s&quot;, properties.getValue&#40;&#41;.getQuota&#40;&#41;,
     *             properties.getValue&#40;&#41;.getMetadata&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.getPropertiesWithResponse -->
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
        return getPropertiesWithResponse(new ShareGetPropertiesOptions());
    }

    /**
     * Retrieves the properties of the share, these include the metadata associated with it and the quota that the share
     * is restricted to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the share properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.getPropertiesWithResponse#ShareGetPropertiesOptions -->
     * <pre>
     * shareAsyncClient.getPropertiesWithResponse&#40;new ShareGetPropertiesOptions&#40;&#41;
     *     .setRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;&#41;
     *     .subscribe&#40;properties -&gt; &#123;
     *         System.out.printf&#40;&quot;Share quota: %d, Metadata: %s&quot;, properties.getValue&#40;&#41;.getQuota&#40;&#41;,
     *             properties.getValue&#40;&#41;.getMetadata&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.getPropertiesWithResponse#ShareGetPropertiesOptions -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareProperties>> getPropertiesWithResponse(ShareGetPropertiesOptions options, Context context) {
        options = options == null ? new ShareGetPropertiesOptions() : options;
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getShares()
            .getPropertiesWithResponseAsync(shareName, snapshot, null, requestConditions.getLeaseId(), context)
            .map(ModelHelper::mapGetPropertiesResponse);
    }

    /**
     * Sets the maximum size in GB that the share is allowed to grow.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the quota to 1024 GB</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.setQuota#int -->
     * <pre>
     * shareAsyncClient.setQuota&#40;1024&#41;.doOnSuccess&#40;response -&gt;
     *     System.out.println&#40;&quot;Setting the share quota completed.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.setQuota#int -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * <p>For more information on updated max file share size values, see the
     * <a href="https://learn.microsoft.com/azure/storage/files/storage-files-scale-targets#azure-file-share-scale-targets">Azure Docs</a>.</p>
     *
     * @param quotaInGB Size in GB to limit the share's growth. The quota in GB must be between 1 and 5120.
     * Refer to the Azure Docs for updated values.
     * @return The {@link ShareInfo information about the share}
     * @throws ShareStorageException If the share doesn't exist or {@code quotaInGB} is outside the allowed bounds
     * @deprecated Use {@link ShareAsyncClient#setProperties(ShareSetPropertiesOptions)}
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareInfo> setQuota(int quotaInGB) {
        return setQuotaWithResponse(quotaInGB).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets the maximum size in GB that the share is allowed to grow.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the quota to 1024 GB</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.setQuotaWithResponse#int -->
     * <pre>
     * shareAsyncClient.setQuotaWithResponse&#40;1024&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Setting the share quota completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.setQuotaWithResponse#int -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * <p>For more information on updated max file share size values, see the
     * <a href="https://learn.microsoft.com/azure/storage/files/storage-files-scale-targets#azure-file-share-scale-targets">Azure Docs</a>.</p>
     *
     * @param quotaInGB Size in GB to limit the share's growth. The default value is 5120. Refer to the Azure Docs for updated values.
     * @return A response containing the {@link ShareInfo information about the share} with headers and response status
     * code
     * @throws ShareStorageException If the share doesn't exist or {@code quotaInGB} is outside the allowed bounds
     * @deprecated Use {@link ShareAsyncClient#setPropertiesWithResponse(ShareSetPropertiesOptions)}
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareInfo>> setQuotaWithResponse(int quotaInGB) {
        return setPropertiesWithResponse(new ShareSetPropertiesOptions().setQuotaInGb(quotaInGB));
    }

    /**
     * Sets the share's properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.setProperties#ShareSetPropertiesOptions -->
     * <pre>
     * shareAsyncClient.setProperties&#40;new ShareSetPropertiesOptions&#40;&#41;.setAccessTier&#40;ShareAccessTier.HOT&#41;
     *     .setQuotaInGb&#40;2014&#41;&#41;
     *     .doOnSuccess&#40;response -&gt; System.out.println&#40;&quot;Setting the share access tier completed.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.setProperties#ShareSetPropertiesOptions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-share-properties">Azure Docs</a>.</p>
     *
     * @param options {@link ShareSetPropertiesOptions}
     * @return The {@link ShareInfo information about the share}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareInfo> setProperties(ShareSetPropertiesOptions options) {
        return setPropertiesWithResponse(options).map(Response::getValue);
    }

    /**
     * Sets the share's properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.setPropertiesWithResponse#ShareSetPropertiesOptions -->
     * <pre>
     * shareAsyncClient.setPropertiesWithResponse&#40;new ShareSetPropertiesOptions&#40;&#41;.setAccessTier&#40;ShareAccessTier.HOT&#41;
     *     .setQuotaInGb&#40;1024&#41;.setRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Setting the share quota completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.setPropertiesWithResponse#ShareSetPropertiesOptions -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareInfo>> setPropertiesWithResponse(ShareSetPropertiesOptions options, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getShares().setPropertiesNoCustomHeadersWithResponseAsync(shareName, null,
            options.getQuotaInGb(), options.getAccessTier(), requestConditions.getLeaseId(), options.getRootSquash(),
            options.isSnapshotVirtualDirectoryAccessEnabled(), null, null,
            null, context)
            .map(ModelHelper::mapToShareInfoResponse);
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
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.setMetadata#map -->
     * <pre>
     * shareAsyncClient.setMetadata&#40;Collections.singletonMap&#40;&quot;share&quot;, &quot;updatedMetadata&quot;&#41;&#41;.doOnSuccess&#40;response -&gt;
     *     System.out.println&#40;&quot;Setting the share metadata completed.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.setMetadata#map -->
     *
     * <p>Clear the metadata of the share</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.clearMetadata#map -->
     * <pre>
     * shareAsyncClient.setMetadata&#40;null&#41;.doOnSuccess&#40;response -&gt;
     *     System.out.println&#40;&quot;Setting the share metadata completed.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.clearMetadata#map -->
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
        return setMetadataWithResponse(metadata).flatMap(FluxUtil::toMono);
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
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.setMetadata#map -->
     * <pre>
     * shareAsyncClient.setMetadata&#40;Collections.singletonMap&#40;&quot;share&quot;, &quot;updatedMetadata&quot;&#41;&#41;.doOnSuccess&#40;response -&gt;
     *     System.out.println&#40;&quot;Setting the share metadata completed.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.setMetadata#map -->
     *
     * <p>Clear the metadata of the share</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.clearMetadata#map -->
     * <pre>
     * shareAsyncClient.setMetadata&#40;null&#41;.doOnSuccess&#40;response -&gt;
     *     System.out.println&#40;&quot;Setting the share metadata completed.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.clearMetadata#map -->
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
        return setMetadataWithResponse(new ShareSetMetadataOptions().setMetadata(metadata));
    }

    /**
     * Sets the user-defined metadata to associate to the share.
     *
     * <p>If {@code null} is passed for the metadata it will clear the metadata associated to the share.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.setMetadataWithResponse#ShareSetMetadataOptions -->
     * <pre>
     * shareAsyncClient.setMetadataWithResponse&#40;new ShareSetMetadataOptions&#40;&#41;
     *     .setMetadata&#40;Collections.singletonMap&#40;&quot;share&quot;, &quot;updatedMetadata&quot;&#41;&#41;
     *     .setRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Setting the share metadata completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.setMetadataWithResponse#ShareSetMetadataOptions -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareInfo>> setMetadataWithResponse(ShareSetMetadataOptions options, Context context) {
        options = options == null ? new ShareSetMetadataOptions() : options;
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getShares().setMetadataNoCustomHeadersWithResponseAsync(shareName, null,
            options.getMetadata(), requestConditions.getLeaseId(), context)
            .map(ModelHelper::mapToShareInfoResponse);
    }

    /**
     * Retrieves stored access policies specified for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List the stored access policies</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.getAccessPolicy -->
     * <pre>
     * shareAsyncClient.getAccessPolicy&#40;&#41;
     *     .subscribe&#40;result -&gt; System.out.printf&#40;&quot;Access policy %s allows these permissions: %s&quot;, result.getId&#40;&#41;,
     *         result.getAccessPolicy&#40;&#41;.getPermissions&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.getAccessPolicy -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-acl">Azure Docs</a>.</p>
     *
     * @return The stored access policies specified on the queue.
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ShareSignedIdentifier> getAccessPolicy() {
        return getAccessPolicy(new ShareGetAccessPolicyOptions());
    }

    /**
     * Retrieves stored access policies specified for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List the stored access policies</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.getAccessPolicy#ShareGetAccessPolicyOptions -->
     * <pre>
     * shareAsyncClient.getAccessPolicy&#40;new ShareGetAccessPolicyOptions&#40;&#41;
     *     .setRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;&#41;
     *     .subscribe&#40;result -&gt; System.out.printf&#40;&quot;Access policy %s allows these permissions: %s&quot;, result.getId&#40;&#41;,
     *         result.getAccessPolicy&#40;&#41;.getPermissions&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.getAccessPolicy#ShareGetAccessPolicyOptions -->
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
                        response.getValue().items(),
                        null,
                        response.getDeserializedHeaders()));

            return new PagedFlux<>(() -> retriever.apply(null), retriever);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Sets stored access policies for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.setAccessPolicy#List -->
     * <pre>
     * ShareAccessPolicy accessPolicy = new ShareAccessPolicy&#40;&#41;.setPermissions&#40;&quot;r&quot;&#41;
     *     .setStartsOn&#40;OffsetDateTime.now&#40;ZoneOffset.UTC&#41;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;ZoneOffset.UTC&#41;.plusDays&#40;10&#41;&#41;;
     *
     * ShareSignedIdentifier permission = new ShareSignedIdentifier&#40;&#41;.setId&#40;&quot;mypolicy&quot;&#41;.setAccessPolicy&#40;accessPolicy&#41;;
     * shareAsyncClient.setAccessPolicy&#40;Collections.singletonList&#40;permission&#41;&#41;.doOnSuccess&#40;
     *     response -&gt; System.out.println&#40;&quot;Setting access policies completed.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.setAccessPolicy#List -->
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
        return setAccessPolicyWithResponse(permissions).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets stored access policies for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.setAccessPolicyWithResponse#List -->
     * <pre>
     * ShareAccessPolicy accessPolicy = new ShareAccessPolicy&#40;&#41;.setPermissions&#40;&quot;r&quot;&#41;
     *     .setStartsOn&#40;OffsetDateTime.now&#40;ZoneOffset.UTC&#41;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;ZoneOffset.UTC&#41;.plusDays&#40;10&#41;&#41;;
     *
     * ShareSignedIdentifier permission = new ShareSignedIdentifier&#40;&#41;.setId&#40;&quot;mypolicy&quot;&#41;.setAccessPolicy&#40;accessPolicy&#41;;
     * shareAsyncClient.setAccessPolicyWithResponse&#40;Collections.singletonList&#40;permission&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Setting access policies completed completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.setAccessPolicyWithResponse#List -->
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
        return setAccessPolicyWithResponse(new ShareSetAccessPolicyOptions().setPermissions(permissions));
    }

    /**
     * Sets stored access policies for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.setAccessPolicyWithResponse#ShareSetAccessPolicyOptions -->
     * <pre>
     * ShareAccessPolicy accessPolicy = new ShareAccessPolicy&#40;&#41;.setPermissions&#40;&quot;r&quot;&#41;
     *     .setStartsOn&#40;OffsetDateTime.now&#40;ZoneOffset.UTC&#41;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;ZoneOffset.UTC&#41;.plusDays&#40;10&#41;&#41;;
     *
     * ShareSignedIdentifier permission = new ShareSignedIdentifier&#40;&#41;.setId&#40;&quot;mypolicy&quot;&#41;.setAccessPolicy&#40;accessPolicy&#41;;
     * shareAsyncClient.setAccessPolicyWithResponse&#40;new ShareSetAccessPolicyOptions&#40;&#41;
     *     .setPermissions&#40;Collections.singletonList&#40;permission&#41;&#41;
     *     .setRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Setting access policies completed completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.setAccessPolicyWithResponse#ShareSetAccessPolicyOptions -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareInfo>> setAccessPolicyWithResponse(ShareSetAccessPolicyOptions options, Context context) {
        options = options == null ? new ShareSetAccessPolicyOptions() : options;
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        List<ShareSignedIdentifier> permissions =
            ModelHelper.truncateAccessPolicyPermissionsToSeconds(options.getPermissions());

        context = context == null ? Context.NONE : context;

        return azureFileStorageClient.getShares().setAccessPolicyNoCustomHeadersWithResponseAsync(shareName, null,
                requestConditions.getLeaseId(), permissions, context)
            .map(ModelHelper::mapToShareInfoResponse);
    }

    /**
     * Retrieves storage statistics about the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the storage statistics</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.getStatistics -->
     * <pre>
     * shareAsyncClient.getStatistics&#40;&#41;.doOnSuccess&#40;response -&gt; System.out.printf&#40;&quot;The share is using %d GB&quot;,
     *     response.getShareUsageInGB&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.getStatistics -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-stats">Azure Docs</a>.</p>
     *
     * @return The storage {@link ShareStatistics statistics of the share}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareStatistics> getStatistics() {
        return getStatisticsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves storage statistics about the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the storage statistics</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.getStatisticsWithResponse -->
     * <pre>
     * shareAsyncClient.getStatisticsWithResponse&#40;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;The share is using %d GB&quot;,
     *     response.getValue&#40;&#41;.getShareUsageInGB&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.getStatisticsWithResponse -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-stats">Azure Docs</a>.</p>
     *
     * @return A response containing the storage {@link ShareStatistics statistics of the share} with headers and
     * response status code
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareStatistics>> getStatisticsWithResponse() {
        return getStatisticsWithResponse(new ShareGetStatisticsOptions());
    }

    /**
     * Retrieves storage statistics about the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the storage statistics</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.getStatisticsWithResponse#ShareGetStatisticsOptions -->
     * <pre>
     * shareAsyncClient.getStatisticsWithResponse&#40;new ShareGetStatisticsOptions&#40;&#41;
     *     .setRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;The share is using %d GB&quot;,
     *     response.getValue&#40;&#41;.getShareUsageInGB&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.getStatisticsWithResponse#ShareGetStatisticsOptions -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareStatistics>> getStatisticsWithResponse(ShareGetStatisticsOptions options, Context context) {
        options = options == null ? new ShareGetStatisticsOptions() : options;
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getShares().getStatisticsNoCustomHeadersWithResponseAsync(shareName, null,
            requestConditions.getLeaseId(), context)
            .map(ModelHelper::mapGetStatisticsResponse);
    }

    /**
     * Creates the directory in the share with the given name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory "mydirectory"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createDirectory#string -->
     * <pre>
     * shareAsyncClient.createDirectory&#40;&quot;mydirectory&quot;&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *     &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete creating the directory!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createDirectory#string -->
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
        return createDirectoryWithResponse(directoryName, null, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates the directory in the share with the given name and associates the passed metadata to it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory "documents" with metadata "directory:metadata"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createDirectoryWithResponse#String-FileSmbProperties-String-Map -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * shareAsyncClient.createDirectoryWithResponse&#40;&quot;documents&quot;, smbProperties, filePermission,
     *     Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Creating the directory completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createDirectoryWithResponse#String-FileSmbProperties-String-Map -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareDirectoryAsyncClient>> createDirectoryWithResponse(String directoryName,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata, Context context) {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = getDirectoryClient(directoryName);
        return shareDirectoryAsyncClient.createWithResponse(smbProperties, filePermission, metadata)
            .map(response -> new SimpleResponse<>(response, shareDirectoryAsyncClient));
    }

    /**
     * Creates the directory in the share with the given name if it does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory "mydirectory"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createDirectoryIfNotExists#string -->
     * <pre>
     * shareAsyncClient.createDirectoryIfNotExists&#40;&quot;mydirectory&quot;&#41;
     *     .switchIfEmpty&#40;Mono.&lt;ShareDirectoryAsyncClient&gt;empty&#40;&#41;
     *         .doOnSuccess&#40;x -&gt; System.out.println&#40;&quot;Already exists.&quot;&#41;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Create completed.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createDirectoryIfNotExists#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @return A {@link Mono} containing a {@link ShareDirectoryAsyncClient} used to interact with the directory
     * created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareDirectoryAsyncClient> createDirectoryIfNotExists(String directoryName) {
        return createDirectoryIfNotExistsWithResponse(directoryName, new ShareDirectoryCreateOptions())
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates the directory in the share with the given name and associates the passed metadata to it if it
     * does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory "documents" with metadata "directory:metadata"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createDirectoryIfNotExistsWithResponse#String-ShareDirectoryCreateOptions -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;;
     * ShareDirectoryCreateOptions options = new ShareDirectoryCreateOptions&#40;&#41;.setSmbProperties&#40;smbProperties&#41;
     *     .setFilePermission&#40;filePermission&#41;.setMetadata&#40;metadata&#41;;
     *
     * shareAsyncClient.createDirectoryIfNotExistsWithResponse&#40;&quot;documents&quot;, options&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *         System.out.println&#40;&quot;Already exists.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully created.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createDirectoryIfNotExistsWithResponse#String-ShareDirectoryCreateOptions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @param options {@link ShareDirectoryCreateOptions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a
     * {@link ShareDirectoryAsyncClient} used to interact with the directory created. If {@link Response}'s status
     * code is 201, a new directory was successfully created. If status code is 409, a directory with the same name
     * already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareDirectoryAsyncClient>> createDirectoryIfNotExistsWithResponse(String directoryName,
        ShareDirectoryCreateOptions options) {
        try {
            return withContext(context ->
                createDirectoryIfNotExistsWithResponse(directoryName, options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareDirectoryAsyncClient>> createDirectoryIfNotExistsWithResponse(String directoryName,
        ShareDirectoryCreateOptions options, Context context) {
        try {
            options = options == null ? new ShareDirectoryCreateOptions() : options;
            return createDirectoryWithResponse(directoryName, options.getSmbProperties(), options.getFilePermission(),
                options.getMetadata(), context).onErrorResume(t -> t instanceof ShareStorageException
                && ((ShareStorageException) t).getStatusCode() == 409, t -> {
                    HttpResponse response = ((ShareStorageException) t).getResponse();
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), getDirectoryClient(directoryName)));
                });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates the file in the share with the given name and file max size.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file "myfile" with size of 1024 bytes.</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createFile#string-long -->
     * <pre>
     * shareAsyncClient.createFile&#40;&quot;myfile&quot;, 1024&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *     &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete creating the directory!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createFile#string-long -->
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
        return createFileWithResponse(fileName, maxSize, null, null, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates the file in the share with the given name, file max size and associates the passed properties to it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file "myfile" with length of 1024 bytes, some headers, file smb properties and metadata</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map -->
     * <pre>
     * ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders&#40;&#41;
     *     .setContentType&#40;&quot;text&#47;html&quot;&#41;
     *     .setContentEncoding&#40;&quot;gzip&quot;&#41;
     *     .setContentLanguage&#40;&quot;en&quot;&#41;
     *     .setCacheControl&#40;&quot;no-transform&quot;&#41;
     *     .setContentDisposition&#40;&quot;attachment&quot;&#41;;
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;
     *     .setNtfsFileAttributes&#40;EnumSet.of&#40;NtfsFileAttributes.READ_ONLY&#41;&#41;
     *     .setFileCreationTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFileLastWriteTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFilePermissionKey&#40;&quot;filePermissionKey&quot;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * &#47;&#47; NOTE: filePermission and filePermissionKey should never be both set
     * shareAsyncClient.createFileWithResponse&#40;&quot;myfile&quot;, 1024, httpHeaders, smbProperties,
     *     filePermission, Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Creating the file completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions -->
     * <pre>
     * ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders&#40;&#41;
     *     .setContentType&#40;&quot;text&#47;html&quot;&#41;
     *     .setContentEncoding&#40;&quot;gzip&quot;&#41;
     *     .setContentLanguage&#40;&quot;en&quot;&#41;
     *     .setCacheControl&#40;&quot;no-transform&quot;&#41;
     *     .setContentDisposition&#40;&quot;attachment&quot;&#41;;
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;
     *     .setNtfsFileAttributes&#40;EnumSet.of&#40;NtfsFileAttributes.READ_ONLY&#41;&#41;
     *     .setFileCreationTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFileLastWriteTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFilePermissionKey&#40;&quot;filePermissionKey&quot;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * &#47;&#47; NOTE: filePermission and filePermissionKey should never be both set
     *
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * shareAsyncClient.createFileWithResponse&#40;&quot;myfile&quot;, 1024, httpHeaders, smbProperties,
     *     filePermission, Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;, requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Creating the file completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareFileAsyncClient>> createFileWithResponse(String fileName, long maxSize,
        ShareFileHttpHeaders httpHeaders, FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata, ShareRequestConditions requestConditions, Context context) {
        ShareFileAsyncClient shareFileAsyncClient = getFileClient(fileName);
        return shareFileAsyncClient
            .createWithResponse(maxSize, httpHeaders, smbProperties, filePermission, null, metadata, requestConditions,
                context).map(response -> new SimpleResponse<>(response, shareFileAsyncClient));
    }

    /**
     * Deletes the specified directory in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory "mydirectory"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.deleteDirectory#string -->
     * <pre>
     * shareAsyncClient.deleteDirectory&#40;&quot;mydirectory&quot;&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *     &#125;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the directory.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.deleteDirectory#string -->
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
        return deleteDirectoryWithResponse(directoryName).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified directory in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory "mydirectory"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.deleteDirectory#string -->
     * <pre>
     * shareAsyncClient.deleteDirectory&#40;&quot;mydirectory&quot;&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *     &#125;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the directory.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.deleteDirectory#string -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteDirectoryWithResponse(String directoryName, Context context) {
        return getDirectoryClient(directoryName).deleteWithResponse(context);
    }

    /**
     * Deletes the specified directory in the share if exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory "mydirectory"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.deleteDirectoryIfExists#string -->
     * <pre>
     * shareAsyncClient.deleteDirectoryIfExists&#40;&quot;mydirectory&quot;&#41;.subscribe&#40;deleted -&gt; &#123;
     *     if &#40;deleted&#41; &#123;
     *         System.out.println&#40;&quot;Successfully deleted.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.deleteDirectoryIfExists#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @return a reactive response signaling completion. {@code true} indicates that the directory was successfully
     * deleted, {@code false} indicates that the directory did not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteDirectoryIfExists(String directoryName) {
        return deleteDirectoryIfExistsWithResponse(directoryName).map(response -> response.getStatusCode() != 404);
    }

    /**
     * Deletes the specified directory in the share if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory "mydirectory"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.deleteDirectoryIfExistsWithResponse#string -->
     * <pre>
     * shareAsyncClient.deleteDirectoryIfExistsWithResponse&#40;&quot;mydirectory&quot;&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.deleteDirectoryIfExistsWithResponse#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @return A reactive response signaling completion. If {@link Response}'s status code is 202, the directory was
     * successfully deleted. If status code is 404, the directory does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteDirectoryIfExistsWithResponse(String directoryName) {
        try {
            return withContext(context -> deleteDirectoryIfExistsWithResponse(directoryName, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Boolean>> deleteDirectoryIfExistsWithResponse(String directoryName, Context context) {
        try {
            return deleteDirectoryWithResponse(directoryName, context)
                .map(response -> (Response<Boolean>) new SimpleResponse<>(response, true))
                .onErrorResume(t -> t
                instanceof ShareStorageException && ((ShareStorageException) t).getStatusCode() == 404,
                    t -> {
                        HttpResponse response = ((ShareStorageException) t).getResponse();
                        return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), false));
                    });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes the specified file in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "myfile"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.deleteFile#string -->
     * <pre>
     * shareAsyncClient.deleteFile&#40;&quot;myfile&quot;&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *     &#125;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the file.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.deleteFile#string -->
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
        return deleteFileWithResponse(fileName).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified file in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "myfile"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.deleteFile#string -->
     * <pre>
     * shareAsyncClient.deleteFile&#40;&quot;myfile&quot;&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *     &#125;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the file.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.deleteFile#string -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.deleteFile#string-ShareRequestConditions -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * shareAsyncClient.deleteFileWithResponse&#40;&quot;myfile&quot;, requestConditions&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *     &#125;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the file.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.deleteFile#string-ShareRequestConditions -->
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
            return withContext(context -> deleteFileWithResponse(fileName, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteFileWithResponse(String fileName, ShareRequestConditions requestConditions,
        Context context) {
        return getFileClient(fileName).deleteWithResponse(requestConditions, context);
    }

    /**
     * Deletes the specified file in the share if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "myfile"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.deleteFileIfExists#string -->
     * <pre>
     * shareAsyncClient.deleteFileIfExists&#40;&quot;myfile&quot;&#41;.subscribe&#40;deleted -&gt; &#123;
     *     if &#40;deleted&#41; &#123;
     *         System.out.println&#40;&quot;Successfully deleted.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.deleteFileIfExists#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file.
     * @return a reactive response signaling completion. {@code true} indicates that the file was successfully
     * deleted, {@code false} indicates that the file did not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteFileIfExists(String fileName) {
        return deleteFileIfExistsWithResponse(fileName, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified file in the share if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "myfile"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.deleteFileIfExistsWithResponse#string-ShareRequestConditions -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * shareAsyncClient.deleteFileIfExistsWithResponse&#40;&quot;myfile&quot;, requestConditions&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.deleteFileIfExistsWithResponse#string-ShareRequestConditions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file.
     * @param requestConditions {@link ShareRequestConditions}
     * @return A reactive response signaling completion. If {@link Response}'s status code is 202, the file was
     * successfully deleted. If status code is 404, the file does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteFileIfExistsWithResponse(String fileName, ShareRequestConditions requestConditions) {
        try {
            return withContext(context -> deleteFileIfExistsWithResponse(fileName, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Boolean>> deleteFileIfExistsWithResponse(String fileName, ShareRequestConditions requestConditions, Context context) {
        try {
            requestConditions = requestConditions == null ? new ShareRequestConditions() : requestConditions;
            return deleteFileWithResponse(fileName, requestConditions, context)
                .map(response -> (Response<Boolean>) new SimpleResponse<>(response, true))
                .onErrorResume(t -> t instanceof ShareStorageException && ((ShareStorageException) t)
                    .getStatusCode() == 404,
                    t -> {
                        HttpResponse response = ((ShareStorageException) t).getResponse();
                        return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), false));
                    });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a permission at the share level. If a permission already exists, it returns the key of it, else creates a
     * new permission and returns the key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createPermission#string -->
     * <pre>
     * shareAsyncClient.createPermission&#40;&quot;filePermission&quot;&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;The file permission key is %s&quot;, response&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createPermission#string -->
     *
     * @param filePermission The file permission to get/create.
     * @return The file permission key associated with the file permission.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> createPermission(String filePermission) {
        return createPermissionWithResponse(filePermission).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a permission at the share level. If a permission already exists, it returns the key of it, else creates a
     * new permission and returns the key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createPermission#ShareFilePermission -->
     * <pre>
     * ShareFilePermission permission = new ShareFilePermission&#40;&#41;.setPermission&#40;&quot;filePermission&quot;&#41;
     *     .setPermissionFormat&#40;FilePermissionFormat.BINARY&#41;;
     * shareAsyncClient.createPermission&#40;permission&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;The file permission key is %s&quot;, response&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createPermission#ShareFilePermission -->
     *
     * @param filePermission The file permission to get/create.
     * @return The file permission key associated with the file permission.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> createPermission(ShareFilePermission filePermission) {
        return createPermissionWithResponse(filePermission).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a permission at the share level. If a permission already exists, it returns the key of it, else creates a
     * new permission and returns the key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createPermissionWithResponse#string -->
     * <pre>
     * shareAsyncClient.createPermissionWithResponse&#40;&quot;filePermission&quot;&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;The file permission key is %s&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createPermissionWithResponse#string -->
     *
     * @param filePermission The file permission to get/create.
     * @return A response that contains the file permission key associated with the file permission.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> createPermissionWithResponse(String filePermission) {
        try {
            return withContext(context -> createPermissionWithResponse(filePermission, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
     * Creates a permission at the share level. If a permission already exists, it returns the key of it, else creates a
     * new permission and returns the key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.createPermissionWithResponse#ShareFilePermission -->
     * <pre>
     * ShareFilePermission permission = new ShareFilePermission&#40;&#41;.setPermission&#40;&quot;filePermission&quot;&#41;
     *     .setPermissionFormat&#40;FilePermissionFormat.BINARY&#41;;
     * shareAsyncClient.createPermissionWithResponse&#40;permission&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;The file permission key is %s&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.createPermissionWithResponse#ShareFilePermission -->
     *
     * @param filePermission The file permission to get/create.
     * @return A response that contains the file permission key associated with the file permission.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> createPermissionWithResponse(ShareFilePermission filePermission) {
        try {
            return withContext(context -> createPermissionWithResponse(filePermission, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<String>> createPermissionWithResponse(ShareFilePermission filePermission, Context context) {
        // NOTE: Should we check for null or empty?
        SharePermission sharePermission = new SharePermission().setPermission(filePermission.getPermission())
            .setFormat(filePermission.getPermissionFormat());
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
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.getPermission#string -->
     * <pre>
     * shareAsyncClient.getPermission&#40;&quot;filePermissionKey&quot;&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;The file permission is %s&quot;, response&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.getPermission#string -->
     *
     * @param filePermissionKey The file permission key.
     * @return The file permission associated with the file permission key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> getPermission(String filePermissionKey) {
        return getPermissionWithResponse(filePermissionKey).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a permission for a given key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.getPermission#string-FilePermissionFormat -->
     * <pre>
     * FilePermissionFormat filePermissionFormat = FilePermissionFormat.BINARY;
     * shareAsyncClient.getPermission&#40;&quot;filePermissionKey&quot;, filePermissionFormat&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;The file permission is %s&quot;, response&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.getPermission#string-FilePermissionFormat -->
     *
     * @param filePermissionKey The file permission key.
     * @param filePermissionFormat Optional. Available for version 2024-11-04 and later. Specifies the format in which
     * the permission is returned. If filePermissionFormat is unspecified or explicitly set to SDDL, the permission will
     * be returned in SSDL format. If filePermissionFormat is explicity set to binary, the permission is returned as a
     * base64 string representing the binary encoding of the permission in self-relative format.
     * @return The file permission associated with the file permission key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> getPermission(String filePermissionKey, FilePermissionFormat filePermissionFormat) {
        return getPermissionWithResponse(filePermissionKey, filePermissionFormat).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a permission for a given key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.getPermissionWithResponse#string -->
     * <pre>
     * shareAsyncClient.getPermissionWithResponse&#40;&quot;filePermissionKey&quot;&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;The file permission is %s&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.getPermissionWithResponse#string -->
     *
     * @param filePermissionKey The file permission key.
     * @return A response that contains th file permission associated with the file permission key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> getPermissionWithResponse(String filePermissionKey) {
        try {
            return withContext(context -> getPermissionWithResponse(filePermissionKey, null, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Gets a permission for a given key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.getPermissionWithResponse#string-FilePermissionFormat -->
     * <pre>
     * FilePermissionFormat filePermissionFormat = FilePermissionFormat.BINARY;
     * shareAsyncClient.getPermissionWithResponse&#40;&quot;filePermissionKey&quot;, filePermissionFormat&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;The file permission is %s&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.getPermissionWithResponse#string-FilePermissionFormat -->
     *
     * @param filePermissionKey The file permission key.
     * @param filePermissionFormat Optional. Available for version 2024-11-04 and later. Specifies the format in which
     * the permission is returned. If filePermissionFormat is unspecified or explicitly set to SDDL, the permission will
     * be returned in SSDL format. If filePermissionFormat is explicity set to binary, the permission is returned as a
     * base64 string representing the binary encoding of the permission in self-relative format.
     * @return A response that contains th file permission associated with the file permission key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> getPermissionWithResponse(String filePermissionKey,
                                                            FilePermissionFormat filePermissionFormat) {
        try {
            return withContext(context -> getPermissionWithResponse(filePermissionKey, filePermissionFormat, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<String>> getPermissionWithResponse(String filePermissionKey, FilePermissionFormat filePermissionFormat,
        Context context) {
        return azureFileStorageClient.getShares()
            .getPermissionWithResponseAsync(shareName, filePermissionKey, filePermissionFormat, null, context)
            .map(response -> new SimpleResponse<>(response, response.getValue().getPermission()));
    }

    /**
     * Get snapshot id which attached to {@link ShareAsyncClient}. Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.getSnapshotId -->
     * <pre>
     * OffsetDateTime currentTime = OffsetDateTime.of&#40;LocalDateTime.now&#40;&#41;, ZoneOffset.UTC&#41;;
     * ShareAsyncClient shareAsyncClient = new ShareClientBuilder&#40;&#41;
     *     .endpoint&#40;&quot;https:&#47;&#47;$&#123;accountName&#125;.file.core.windows.net&quot;&#41;
     *     .sasToken&#40;&quot;$&#123;SASToken&#125;&quot;&#41;
     *     .shareName&#40;&quot;myshare&quot;&#41;
     *     .snapshot&#40;currentTime.toString&#40;&#41;&#41;
     *     .buildAsyncClient&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Snapshot ID: %s%n&quot;, shareAsyncClient.getSnapshotId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.getSnapshotId -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.getShareName -->
     * <pre>
     * String shareName = shareAsyncClient.getShareName&#40;&#41;;
     * System.out.println&#40;&quot;The name of the share is &quot; + shareName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.getShareName -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.generateSas#ShareServiceSasSignatureValues -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * ShareSasPermission permission = new ShareSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * ShareServiceSasSignatureValues values = new ShareServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * shareAsyncClient.generateSas&#40;values&#41;; &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.generateSas#ShareServiceSasSignatureValues -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareAsyncClient.generateSas#ShareServiceSasSignatureValues-Context -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * ShareSasPermission permission = new ShareSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * ShareServiceSasSignatureValues values = new ShareServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * shareAsyncClient.generateSas&#40;values, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareAsyncClient.generateSas#ShareServiceSasSignatureValues-Context -->
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
}
