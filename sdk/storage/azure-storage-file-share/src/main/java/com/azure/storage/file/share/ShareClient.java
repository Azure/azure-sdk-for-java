// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.implementation.AzureFileStorageImpl;
import com.azure.storage.file.share.implementation.models.SharePermission;
import com.azure.storage.file.share.implementation.models.ShareSignedIdentifierWrapper;
import com.azure.storage.file.share.implementation.models.ShareStats;
import com.azure.storage.file.share.implementation.models.SharesCreatePermissionHeaders;
import com.azure.storage.file.share.implementation.models.SharesCreateSnapshotHeaders;
import com.azure.storage.file.share.implementation.models.SharesGetAccessPolicyHeaders;
import com.azure.storage.file.share.implementation.models.SharesGetPermissionHeaders;
import com.azure.storage.file.share.implementation.models.SharesGetPropertiesHeaders;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.implementation.util.ShareSasImplUtil;
import com.azure.storage.file.share.models.ShareDirectoryInfo;
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
import com.azure.storage.file.share.options.ShareDirectoryCreateOptions;
import com.azure.storage.file.share.options.ShareGetAccessPolicyOptions;
import com.azure.storage.file.share.options.ShareGetPropertiesOptions;
import com.azure.storage.file.share.options.ShareGetStatisticsOptions;
import com.azure.storage.file.share.options.ShareSetAccessPolicyOptions;
import com.azure.storage.file.share.options.ShareSetMetadataOptions;
import com.azure.storage.file.share.options.ShareSetPropertiesOptions;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static com.azure.storage.common.implementation.StorageImplUtils.sendRequest;

/**
 * This class provides a client that contains all the operations for interacting with a share in Azure Storage Share.
 * Operations allowed by the client are creating and deleting the share, creating snapshots for the share, creating and
 * deleting directories in the share and retrieving and updating properties metadata and access policies of the share.
 *
 * <p><strong>Instantiating a Synchronous Share Client</strong></p>
 *
 * <!-- src_embed com.azure.storage.file.share.ShareClient.instantiation -->
 * <pre>
 * ShareClient client = new ShareClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;$&#123;connectionString&#125;&quot;&#41;
 *     .endpoint&#40;&quot;$&#123;endpoint&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.share.ShareClient.instantiation -->
 *
 * <p>View {@link ShareClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ShareClientBuilder
 * @see ShareAsyncClient
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = ShareClientBuilder.class)
public class ShareClient {
    private static final ClientLogger LOGGER = new ClientLogger(ShareClient.class);

    private final AzureFileStorageImpl azureFileStorageClient;
    private final String shareName;
    private final String snapshot;
    private final String accountName;
    private final ShareServiceVersion serviceVersion;
    private final AzureSasCredential sasToken;
    private final String shareUrlString;

    /**
     * Creates a ShareClient.
     * @param azureFileStorageClient Client that interacts with the service interfaces
     * @param shareName Name of the share
     * @param snapshot The snapshot of the share
     * @param accountName Name of the account
     * @param serviceVersion The version of the service to be used when making requests.
     * @param sasToken The SAS token used to authenticate the request
     */
    ShareClient(AzureFileStorageImpl azureFileStorageClient, String shareName, String snapshot, String accountName,
        ShareServiceVersion serviceVersion, AzureSasCredential sasToken) {
        Objects.requireNonNull(shareName, "'shareName' cannot be null.");
        this.shareName = shareName;
        this.snapshot = snapshot;
        this.accountName = accountName;
        this.azureFileStorageClient = azureFileStorageClient;
        this.serviceVersion = serviceVersion;
        this.sasToken = sasToken;

        StringBuilder shareUrlString = new StringBuilder(azureFileStorageClient.getUrl()).append("/").append(shareName);
        if (snapshot != null) {
            shareUrlString.append("?sharesnapshot=").append(snapshot);
        }
        this.shareUrlString = shareUrlString.toString();
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
        return this.shareUrlString;
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public ShareServiceVersion getServiceVersion() {
        return this.serviceVersion;
    }

    /**
     * Constructs a {@link ShareDirectoryClient} that interacts with the root directory in the share.
     *
     * <p>If the directory doesn't exist in the share {@link ShareDirectoryClient#create() create} in the client will
     * need to be called before interaction with the directory can happen.</p>
     *
     * @return a {@link ShareDirectoryClient} that interacts with the root directory in the share
     */
    public ShareDirectoryClient getRootDirectoryClient() {
        return getDirectoryClient("");
    }

    /**
     * Constructs a {@link ShareDirectoryClient} that interacts with the specified directory.
     *
     * <p>If the directory doesn't exist in the share {@link ShareDirectoryClient#create() create} in the client will
     * need to be called before interaction with the directory can happen.</p>
     *
     * @param directoryName Name of the directory
     * @return a {@link ShareDirectoryClient} that interacts with the directory in the share
     */
    public ShareDirectoryClient getDirectoryClient(String directoryName) {
        directoryName = "/".equals(directoryName) ? "" : directoryName;
        return new ShareDirectoryClient(azureFileStorageClient, shareName, directoryName, snapshot, accountName,
            serviceVersion, sasToken);
    }

    /**
     * Constructs a {@link ShareFileClient} that interacts with the specified file.
     *
     * <p>If the file doesn't exist in the share {@link ShareFileClient#create(long)} ) create} in the client will
     * need to be called before interaction with the file can happen.</p>
     *
     * @param filePath Name of the file
     * @return a {@link ShareFileClient} that interacts with the file in the share
     */
    public ShareFileClient getFileClient(String filePath) {
        return new ShareFileClient(new ShareFileAsyncClient(azureFileStorageClient, shareName, filePath, snapshot,
            accountName, serviceVersion, sasToken), azureFileStorageClient, shareName, filePath, snapshot,
            accountName, serviceVersion, sasToken);
    }


    /**
     * Creates a new {@link ShareAsyncClient} linked to the {@code snapshot} of this share resource.
     *
     * @param snapshot the identifier for a specific snapshot of this share
     * @return a {@link ShareClient} used to interact with the specific snapshot.
     */
    public ShareClient getSnapshotClient(String snapshot) {
        return new ShareClient(azureFileStorageClient, shareName, snapshot, accountName, serviceVersion, sasToken);
    }

    /**
     * Determines if the share this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.exists -->
     * <pre>
     * System.out.printf&#40;&quot;Exists? %b%n&quot;, client.exists&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.exists -->
     *
     * @return Flag indicating existence of the share.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Boolean exists() {
        return existsWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Determines if the share this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.existsWithResponse#Duration-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     * System.out.printf&#40;&quot;Exists? %b%n&quot;, client.existsWithResponse&#40;timeout, context&#41;.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.existsWithResponse#Duration-Context -->
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return Flag indicating existence of the share.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> existsWithResponse(Duration timeout, Context context) {
        try {
            Response<ShareProperties> response = getPropertiesWithResponse(timeout, context);
            return new SimpleResponse<>(response, true);
        } catch (RuntimeException e) {
            if (ModelHelper.checkDoesNotExistStatusCode(e) && e instanceof HttpResponseException) {
                HttpResponse response = ((HttpResponseException) e).getResponse();
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), false);
            } else {
                throw LOGGER.logExceptionAsError(e);
            }
        }
    }

    /**
     * Creates the share in the storage account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.create -->
     * <pre>
     * ShareInfo response = shareClient.create&#40;&#41;;
     * System.out.println&#40;&quot;Complete creating the shares with status code: &quot; + response&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.create -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @return The {@link ShareInfo information about the share}.
     * @throws ShareStorageException If the share already exists with different metadata
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareInfo create() {
        return createWithResponse(null, null, null, Context.NONE).getValue();
    }

    /**
     * Creates the share in the storage account with the specified metadata and quota.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share with metadata "share:metadata"</p>
     *
     * <!-- src_embed ShareClient.createWithResponse#map-integer-duration-context.metadata -->
     * <pre>
     * Response&lt;ShareInfo&gt; response = shareClient.createWithResponse&#40;Collections.singletonMap&#40;&quot;share&quot;, &quot;metadata&quot;&#41;,
     *     null, Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Complete creating the shares with status code: &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end ShareClient.createWithResponse#map-integer-duration-context.metadata -->
     *
     * <p>Create the share with a quota of 10 GB</p>
     *
     * <!-- src_embed ShareClient.createWithResponse#map-integer-duration-context.quota -->
     * <pre>
     * Response&lt;ShareInfo&gt; response = shareClient.createWithResponse&#40;null, 10,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Complete creating the shares with status code: &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end ShareClient.createWithResponse#map-integer-duration-context.quota -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to associate with the share
     * @param quotaInGB Optional maximum size the share is allowed to grow to in GB. This must be greater than 0 and
     * less than or equal to 5120. The default value is 5120.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareInfo information about the share} and the status its creation.
     * @throws ShareStorageException If the share already exists with different metadata or {@code quotaInGB} is outside
     * the allowed range.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareInfo> createWithResponse(Map<String, String> metadata, Integer quotaInGB, Duration timeout,
        Context context) {
        return createWithResponse(new ShareCreateOptions().setQuotaInGb(quotaInGB).setMetadata(metadata), timeout,
            context);
    }

    /**
     * Creates the share in the storage account with the specified options.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed ShareClient.createWithResponse#ShareCreateOptions-Duration-Context -->
     * <pre>
     * Response&lt;ShareInfo&gt; response = shareClient.createWithResponse&#40;new ShareCreateOptions&#40;&#41;
     *         .setMetadata&#40;Collections.singletonMap&#40;&quot;share&quot;, &quot;metadata&quot;&#41;&#41;.setQuotaInGb&#40;1&#41;
     *         .setAccessTier&#40;ShareAccessTier.HOT&#41;, Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Complete creating the shares with status code: &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end ShareClient.createWithResponse#ShareCreateOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param options {@link ShareCreateOptions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareInfo information about the share} and the status its creation.
     * @throws ShareStorageException If the share already exists with different metadata or {@code quotaInGB} is outside
     * the allowed range.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareInfo> createWithResponse(ShareCreateOptions options, Duration timeout,
        Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        ShareCreateOptions finalOptions = options == null ? new ShareCreateOptions() : options;
        String enabledProtocol = finalOptions.getProtocols() == null ? null : finalOptions.getProtocols().toString();
        String finalEnabledProtocol = "".equals(enabledProtocol) ? null : enabledProtocol;

        Callable<Response<Void>> operation = () -> azureFileStorageClient.getShares()
            .createNoCustomHeadersWithResponse(shareName, null, finalOptions.getMetadata(), finalOptions.getQuotaInGb(),
                finalOptions.getAccessTier(), finalEnabledProtocol, finalOptions.getRootSquash(),
                finalOptions.isSnapshotVirtualDirectoryAccessEnabled(), finalOptions.isPaidBurstingEnabled(),
                finalOptions.getPaidBurstingMaxBandwidthMibps(), finalOptions.getPaidBurstingMaxIops(), finalContext);

        return ModelHelper.mapToShareInfoResponse(sendRequest(operation, timeout, ShareStorageException.class));
    }

    /**
     * Creates the share in the storage account if it does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.createIfNotExists -->
     * <pre>
     * shareClient.createIfNotExists&#40;&#41;;
     * System.out.println&#40;&quot;Completed creating the share.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.createIfNotExists -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @return {@link ShareInfo} that contains information about the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareInfo createIfNotExists() {
        return createIfNotExistsWithResponse(null, null, null).getValue();
    }

    /**
     * Creates the share in the storage account with the specified options if it does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed ShareClient.createIfNotExistsWithResponse#ShareCreateOptions-Duration-Context -->
     * <pre>
     * Response&lt;ShareInfo&gt; response = shareClient.createIfNotExistsWithResponse&#40;new ShareCreateOptions&#40;&#41;
     *     .setMetadata&#40;Collections.singletonMap&#40;&quot;share&quot;, &quot;metadata&quot;&#41;&#41;.setQuotaInGb&#40;1&#41;
     *     .setAccessTier&#40;ShareAccessTier.HOT&#41;, Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     *
     * if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *     System.out.println&#40;&quot;Already existed.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Create completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end ShareClient.createIfNotExistsWithResponse#ShareCreateOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param options {@link ShareCreateOptions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A reactive {@link Response} signaling completion, whose {@link Response#getValue() value} contains a
     * {@link ShareInfo} containing information about the share. If {@link Response}'s status code is 201, a new
     * share was successfully created. If status code is 409, a share already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareInfo> createIfNotExistsWithResponse(ShareCreateOptions options, Duration timeout, Context context) {
        try {
            return createWithResponse(options, timeout, context);
        } catch (ShareStorageException e) {
            if (e.getStatusCode() == 409 && e.getErrorCode().equals(ShareErrorCode.SHARE_ALREADY_EXISTS)) {
                HttpResponse res = e.getResponse();
                return new SimpleResponse<>(res.getRequest(), res.getStatusCode(), res.getHeaders(), null);
            } else {
                throw LOGGER.logExceptionAsError(e);
            }
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Creates a snapshot of the share with the same metadata associated to the share at the time of creation.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a snapshot</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.createSnapshot -->
     * <pre>
     * ShareSnapshotInfo response = shareClient.createSnapshot&#40;&#41;;
     * System.out.println&#40;&quot;Complete creating the share snpashot with snapshot id: &quot; + response.getSnapshot&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.createSnapshot -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/snapshot-share">Azure Docs</a>.</p>
     *
     * @return The {@link ShareSnapshotInfo information about snapshot of share}
     * @throws ShareStorageException If the share doesn't exist, there are 200 snapshots of the share, or a snapshot is
     * in progress for the share
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareSnapshotInfo createSnapshot() {
        return createSnapshotWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Creates a snapshot of the share with the metadata that was passed associated to the snapshot.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a snapshot with metadata "snapshot:metadata"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.createSnapshotWithResponse#map-duration-context -->
     * <pre>
     * Response&lt;ShareSnapshotInfo&gt; response =
     *     shareClient.createSnapshotWithResponse&#40;Collections.singletonMap&#40;&quot;snpashot&quot;, &quot;metadata&quot;&#41;,
     *         Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Complete creating the share snpashot with snapshot id: &quot; + response.getValue&#40;&#41;.getSnapshot&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.createSnapshotWithResponse#map-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/snapshot-share">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to associate with the snapshot. If {@code null} the metadata of the share will
     * be copied to the snapshot.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareSnapshotInfo information about snapshot of the share} and status of
     * creation.
     * @throws ShareStorageException If the share doesn't exist, there are 200 snapshots of the share, or a snapshot is
     * in progress for the share
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareSnapshotInfo> createSnapshotWithResponse(Map<String, String> metadata, Duration timeout,
        Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        Callable<ResponseBase<SharesCreateSnapshotHeaders, Void>> operation = () ->
            this.azureFileStorageClient.getShares().createSnapshotWithResponse(shareName, null, metadata, finalContext);

        return ModelHelper.mapCreateSnapshotResponse(sendRequest(operation, timeout, ShareStorageException.class));
    }

    /**
     * Deletes the share in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.delete -->
     * <pre>
     * shareClient.delete&#40;&#41;;
     * System.out.println&#40;&quot;Completed deleting the share.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.delete -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete() {
        deleteWithResponse(null, Context.NONE);
    }

    /**
     * Deletes the share in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.deleteWithResponse#duration-context -->
     * <pre>
     * Response&lt;Void&gt; response = shareClient.deleteWithResponse&#40;Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Complete deleting the share with status code: &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.deleteWithResponse#duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share doesn't exist
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(Duration timeout, Context context) {
        return deleteWithResponse(new ShareDeleteOptions(), timeout, context);
    }

    /**
     * Deletes the share in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.deleteWithResponse#ShareDeleteOptions-Duration-Context -->
     * <pre>
     * Response&lt;Void&gt; response = shareClient.deleteWithResponse&#40;new ShareDeleteOptions&#40;&#41;
     *         .setRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Complete deleting the share with status code: &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.deleteWithResponse#ShareDeleteOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param options {@link ShareDeleteOptions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share doesn't exist
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(ShareDeleteOptions options, Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        ShareDeleteOptions finalOptions = options == null ? new ShareDeleteOptions() : options;
        ShareRequestConditions requestConditions = finalOptions.getRequestConditions() == null
            ? new ShareRequestConditions() : finalOptions.getRequestConditions();

        Callable<Response<Void>> operation = () -> this.azureFileStorageClient.getShares()
            .deleteNoCustomHeadersWithResponse(shareName, snapshot, null, ModelHelper.toDeleteSnapshotsOptionType(
                finalOptions.getDeleteSnapshotsOptions()), requestConditions.getLeaseId(), finalContext);

        return sendRequest(operation, timeout, ShareStorageException.class);
    }

    /**
     * Deletes the share in the storage account if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.deleteIfExists -->
     * <pre>
     * boolean result = shareClient.deleteIfExists&#40;&#41;;
     * System.out.println&#40;&quot;Share deleted: &quot; + result&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.deleteIfExists -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     * @return {@code true} if the share is successfully deleted, {@code false} if the share does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean deleteIfExists() {
        return deleteIfExistsWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Deletes the share in the storage account if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.deleteIfExistsWithResponse#ShareDeleteOptions-Duration-Context -->
     * <pre>
     * Response&lt;Boolean&gt; response = shareClient.deleteIfExistsWithResponse&#40;new ShareDeleteOptions&#40;&#41;
     *         .setRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *     System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.deleteIfExistsWithResponse#ShareDeleteOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param options {@link ShareDeleteOptions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 202, the share
     * was successfully deleted. If status code is 404, the share does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> deleteIfExistsWithResponse(ShareDeleteOptions options, Duration timeout, Context context) {
        try {
            Response<Void> response = this.deleteWithResponse(options, timeout, context);
            return new SimpleResponse<>(response, true);
        } catch (ShareStorageException e) {
            if (e.getStatusCode() == 404 && e.getErrorCode().equals(ShareErrorCode.SHARE_NOT_FOUND)) {
                HttpResponse res = e.getResponse();
                return new SimpleResponse<>(res.getRequest(), res.getStatusCode(), res.getHeaders(), false);
            } else {
                throw LOGGER.logExceptionAsError(e);
            }
        }
    }

    /**
     * Retrieves the properties of the share, these include the metadata associated to it and the quota that the share
     * is restricted to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the share properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.getProperties -->
     * <pre>
     * ShareProperties properties = shareClient.getProperties&#40;&#41;;
     * System.out.printf&#40;&quot;Share quota: %d, Metadata: %s&quot;, properties.getQuota&#40;&#41;, properties.getMetadata&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.getProperties -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @return The {@link ShareProperties properties of the share}
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareProperties getProperties() {
        return getPropertiesWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Retrieves the properties of the share, these include the metadata associated to it and the quota that the share
     * is restricted to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the share properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.getPropertiesWithResponse#duration-context -->
     * <pre>
     * ShareProperties properties = shareClient.getPropertiesWithResponse&#40;
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Share quota: %d, Metadata: %s&quot;, properties.getQuota&#40;&#41;, properties.getMetadata&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.getPropertiesWithResponse#duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing {@link ShareProperties properties of the share} with response status code
     * @throws ShareStorageException If the share doesn't exist
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareProperties> getPropertiesWithResponse(Duration timeout, Context context) {
        return getPropertiesWithResponse(new ShareGetPropertiesOptions(), timeout, context);
    }

    /**
     * Retrieves the properties of the share, these include the metadata associated to it and the quota that the share
     * is restricted to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the share properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.getPropertiesWithResponse#ShareGetPropertiesOptions-Duration-Context -->
     * <pre>
     * ShareProperties properties = shareClient.getPropertiesWithResponse&#40;new ShareGetPropertiesOptions&#40;&#41;
     *     .setRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Share quota: %d, Metadata: %s&quot;, properties.getQuota&#40;&#41;, properties.getMetadata&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.getPropertiesWithResponse#ShareGetPropertiesOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @param options {@link ShareGetPropertiesOptions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing {@link ShareProperties properties of the share} with response status code
     * @throws ShareStorageException If the share doesn't exist
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareProperties> getPropertiesWithResponse(ShareGetPropertiesOptions options, Duration timeout,
        Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        Callable<ResponseBase<SharesGetPropertiesHeaders, Void>> operation = () ->
            this.azureFileStorageClient.getShares()
                .getPropertiesWithResponse(shareName, snapshot, null, requestConditions.getLeaseId(), finalContext);

        return ModelHelper.mapGetPropertiesResponse(sendRequest(operation, timeout, ShareStorageException.class));
    }

    /**
     * Sets the maximum size in GB that the share is allowed to grow.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the quota to 1024 GB</p>
     *
     * <!-- src_embed ShareClient.setQuota#int -->
     * <pre>
     * System.out.println&#40;&quot;Setting the share quota completed.&quot; + shareClient.setQuota&#40;1024&#41;&#41;;
     * </pre>
     * <!-- end ShareClient.setQuota#int -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @param quotaInGB Size in GB to limit the share's growth. The quota in GB must be between 1 and 5120.
     * @return The {@link ShareInfo information about the share}
     * @throws ShareStorageException If the share doesn't exist or {@code quotaInGB} is outside the allowed bounds
     * @deprecated Use {@link ShareClient#setProperties(ShareSetPropertiesOptions)}
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareInfo setQuota(int quotaInGB) {
        return setQuotaWithResponse(quotaInGB, null, Context.NONE).getValue();
    }

    /**
     * Sets the maximum size in GB that the share is allowed to grow.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the quota to 1024 GB</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.setQuotaWithResponse#int-duration-context -->
     * <pre>
     * Response&lt;ShareInfo&gt; response = shareClient.setQuotaWithResponse&#40;1024,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Setting the share quota completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.setQuotaWithResponse#int-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @param quotaInGB Size in GB to limit the share's growth. The quota in GB must be between 1 and 5120.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing {@link ShareInfo information about the share} with response status code
     * @throws ShareStorageException If the share doesn't exist or {@code quotaInGB} is outside the allowed bounds
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     * @deprecated Use {@link ShareClient#setPropertiesWithResponse(ShareSetPropertiesOptions, Duration, Context)}
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareInfo> setQuotaWithResponse(int quotaInGB, Duration timeout, Context context) {
        return setPropertiesWithResponse(new ShareSetPropertiesOptions().setQuotaInGb(quotaInGB), timeout, context);
    }

    /**
     * Sets the share's properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed ShareClient.setProperties#ShareSetPropertiesOptions -->
     * <pre>
     * System.out.println&#40;&quot;Setting the share access tier completed.&quot; + shareClient.setProperties&#40;
     *     new ShareSetPropertiesOptions&#40;&#41;.setAccessTier&#40;ShareAccessTier.HOT&#41;.setQuotaInGb&#40;1024&#41;&#41;&#41;;
     * </pre>
     * <!-- end ShareClient.setProperties#ShareSetPropertiesOptions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @param options {@link ShareSetPropertiesOptions}
     * @return The {@link ShareInfo information about the share}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareInfo setProperties(ShareSetPropertiesOptions options) {
        return setPropertiesWithResponse(options, null, Context.NONE).getValue();
    }

    /**
     * Sets the share's properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.setPropertiesWithResponse#ShareSetPropertiesOptions-Duration-Context -->
     * <pre>
     * Response&lt;ShareInfo&gt; response = shareClient.setPropertiesWithResponse&#40;
     *     new ShareSetPropertiesOptions&#40;&#41;.setAccessTier&#40;ShareAccessTier.HOT&#41;.setQuotaInGb&#40;1024&#41;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Setting the share access tier completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.setPropertiesWithResponse#ShareSetPropertiesOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @param options {@link ShareSetPropertiesOptions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing {@link ShareInfo information about the share} with response status code
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareInfo> setPropertiesWithResponse(ShareSetPropertiesOptions options, Duration timeout,
        Context context) {
        StorageImplUtils.assertNotNull("options", options);
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        Context finalContext = context == null ? Context.NONE : context;

        Callable<Response<Void>> operation = () -> this.azureFileStorageClient.getShares()
            .setPropertiesNoCustomHeadersWithResponse(shareName, null, options.getQuotaInGb(), options.getAccessTier(),
                requestConditions.getLeaseId(), options.getRootSquash(),
                options.isSnapshotVirtualDirectoryAccessEnabled(), options.isPaidBurstingEnabled(),
                options.getPaidBurstingMaxBandwidthMibps(), options.getPaidBurstingMaxIops(), finalContext);

        return ModelHelper.mapToShareInfoResponse(sendRequest(operation, timeout, ShareStorageException.class));
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
     * <!-- src_embed com.azure.storage.file.share.ShareClient.setMetadata#map -->
     * <pre>
     * shareClient.setMetadata&#40;Collections.singletonMap&#40;&quot;share&quot;, &quot;updatedMetadata&quot;&#41;&#41;;
     * System.out.println&#40;&quot;Setting the share metadata.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.setMetadata#map -->
     *
     * <p>Clear the metadata of the share</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.clearMetadata#map -->
     * <pre>
     * shareClient.setMetadata&#40;null&#41;;
     * System.out.println&#40;&quot;Clear metadata completed.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.clearMetadata#map -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-share-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to set on the share, if null is passed the metadata for the share is cleared
     * @return The {@link ShareProperties properties of the share}
     * @throws ShareStorageException If the share doesn't exist or the metadata contains invalid keys
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareInfo setMetadata(Map<String, String> metadata) {
        return setMetadataWithResponse(metadata, null, Context.NONE).getValue();
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
     * <!-- src_embed com.azure.storage.file.share.ShareClient.setMetadataWithResponse#map-duration-context -->
     * <pre>
     * Response&lt;ShareInfo&gt; response = shareClient.setMetadataWithResponse&#40;
     *     Collections.singletonMap&#40;&quot;share&quot;, &quot;updatedMetadata&quot;&#41;, Duration.ofSeconds&#40;1&#41;,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Setting the share metadata completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.setMetadataWithResponse#map-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-share-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to set on the share, if null is passed the metadata for the share is cleared
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing {@link ShareProperties properties of the share} with response status code
     * @throws ShareStorageException If the share doesn't exist or the metadata contains invalid keys
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareInfo> setMetadataWithResponse(Map<String, String> metadata, Duration timeout,
        Context context) {
        return setMetadataWithResponse(new ShareSetMetadataOptions().setMetadata(metadata), timeout, context);
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
     * <!-- src_embed com.azure.storage.file.share.ShareClient.setMetadataWithResponse#ShareSetMetadataOptions-Duration-Context -->
     * <pre>
     * Response&lt;ShareInfo&gt; response = shareClient.setMetadataWithResponse&#40;new ShareSetMetadataOptions&#40;&#41;
     *         .setMetadata&#40;Collections.singletonMap&#40;&quot;share&quot;, &quot;updatedMetadata&quot;&#41;&#41;
     *         .setRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;,
     *     Duration.ofSeconds&#40;1&#41;,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Setting the share metadata completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.setMetadataWithResponse#ShareSetMetadataOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-share-metadata">Azure Docs</a>.</p>
     *
     * @param options {@link ShareSetMetadataOptions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing {@link ShareProperties properties of the share} with response status code
     * @throws ShareStorageException If the share doesn't exist or the metadata contains invalid keys
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareInfo> setMetadataWithResponse(ShareSetMetadataOptions options, Duration timeout,
        Context context) {
        StorageImplUtils.assertNotNull("options", options);
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        Context finalContext = context == null ? Context.NONE : context;

        Callable<Response<Void>> operation = () -> this.azureFileStorageClient.getShares()
            .setMetadataNoCustomHeadersWithResponse(shareName, null, options.getMetadata(),
                requestConditions.getLeaseId(), finalContext);

        return ModelHelper.mapToShareInfoResponse(sendRequest(operation, timeout, ShareStorageException.class));
    }

    /**
     * Retrieves stored access policies specified for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List the stored access policies</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.getAccessPolicy -->
     * <pre>
     * for &#40;ShareSignedIdentifier result : shareClient.getAccessPolicy&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Access policy %s allows these permissions: %s&quot;,
     *         result.getId&#40;&#41;, result.getAccessPolicy&#40;&#41;.getPermissions&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.getAccessPolicy -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-acl">Azure Docs</a>.</p>
     *
     * @return The stored access policies specified on the queue.
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ShareSignedIdentifier> getAccessPolicy() {
        return getAccessPolicy(new ShareGetAccessPolicyOptions());
    }

    /**
     * Retrieves stored access policies specified for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List the stored access policies</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.getAccessPolicy#ShareGetAccessPolicyOptions -->
     * <pre>
     * for &#40;ShareSignedIdentifier result : shareClient
     *     .getAccessPolicy&#40;new ShareGetAccessPolicyOptions&#40;&#41;
     *         .setRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Access policy %s allows these permissions: %s&quot;,
     *         result.getId&#40;&#41;, result.getAccessPolicy&#40;&#41;.getPermissions&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.getAccessPolicy#ShareGetAccessPolicyOptions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-acl">Azure Docs</a>.</p>
     *
     * @param options {@link ShareGetAccessPolicyOptions}
     * @return The stored access policies specified on the queue.
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ShareSignedIdentifier> getAccessPolicy(ShareGetAccessPolicyOptions options) {
        ShareGetAccessPolicyOptions finalOptions = options == null ? new ShareGetAccessPolicyOptions() : options;
        ShareRequestConditions requestConditions = finalOptions.getRequestConditions() == null
            ? new ShareRequestConditions() : finalOptions.getRequestConditions();

        ResponseBase<SharesGetAccessPolicyHeaders, ShareSignedIdentifierWrapper> responseBase =
            this.azureFileStorageClient.getShares()
                .getAccessPolicyWithResponse(shareName, null, requestConditions.getLeaseId(), Context.NONE);

        Supplier<PagedResponse<ShareSignedIdentifier>> response = () -> new PagedResponseBase<>(
            responseBase.getRequest(),
            responseBase.getStatusCode(),
            responseBase.getHeaders(),
            responseBase.getValue().items(),
            null,
            responseBase.getDeserializedHeaders());

        return new PagedIterable<>(response);
    }

    /**
     * Sets stored access policies for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * <!-- src_embed ShareClient.setAccessPolicy#List -->
     * <pre>
     * ShareAccessPolicy accessPolicy = new ShareAccessPolicy&#40;&#41;.setPermissions&#40;&quot;r&quot;&#41;
     *     .setStartsOn&#40;OffsetDateTime.now&#40;ZoneOffset.UTC&#41;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;ZoneOffset.UTC&#41;.plusDays&#40;10&#41;&#41;;
     *
     * ShareSignedIdentifier permission = new ShareSignedIdentifier&#40;&#41;.setId&#40;&quot;mypolicy&quot;&#41;.setAccessPolicy&#40;accessPolicy&#41;;
     *
     * shareClient.setAccessPolicy&#40;Collections.singletonList&#40;permission&#41;&#41;;
     * System.out.println&#40;&quot;Setting access policies completed.&quot;&#41;;
     * </pre>
     * <!-- end ShareClient.setAccessPolicy#List -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-share-acl">Azure Docs</a>.</p>
     *
     * @param permissions Access policies to set on the queue
     * @return The {@link ShareInfo information of the share}
     * @throws ShareStorageException If the share doesn't exist, a stored access policy doesn't have all fields filled
     * out, or the share will have more than five policies.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareInfo setAccessPolicy(List<ShareSignedIdentifier> permissions) {
        return setAccessPolicyWithResponse(permissions, null, Context.NONE).getValue();
    }

    /**
     * Sets stored access policies for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.setAccessPolicyWithResponse#list-duration-context -->
     * <pre>
     * ShareAccessPolicy accessPolicy = new ShareAccessPolicy&#40;&#41;.setPermissions&#40;&quot;r&quot;&#41;
     *     .setStartsOn&#40;OffsetDateTime.now&#40;ZoneOffset.UTC&#41;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;ZoneOffset.UTC&#41;.plusDays&#40;10&#41;&#41;;
     *
     * ShareSignedIdentifier permission = new ShareSignedIdentifier&#40;&#41;.setId&#40;&quot;mypolicy&quot;&#41;.setAccessPolicy&#40;accessPolicy&#41;;
     *
     * Response&lt;ShareInfo&gt; response = shareClient.setAccessPolicyWithResponse&#40;Collections.singletonList&#40;permission&#41;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Setting access policies completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.setAccessPolicyWithResponse#list-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-share-acl">Azure Docs</a>.</p>
     *
     * @param permissions Access policies to set on the queue
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareInfo information of the share} with headers and response status
     * code
     * @throws ShareStorageException If the share doesn't exist, a stored access policy doesn't have all fields filled
     * out, or the share will have more than five policies.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareInfo> setAccessPolicyWithResponse(List<ShareSignedIdentifier> permissions, Duration timeout,
                                                           Context context) {
        return setAccessPolicyWithResponse(new ShareSetAccessPolicyOptions().setPermissions(permissions), timeout,
            context);
    }

    /**
     * Sets stored access policies for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.setAccessPolicyWithResponse#ShareSetAccessPolicyOptions-Duration-Context -->
     * <pre>
     * ShareAccessPolicy accessPolicy = new ShareAccessPolicy&#40;&#41;.setPermissions&#40;&quot;r&quot;&#41;
     *     .setStartsOn&#40;OffsetDateTime.now&#40;ZoneOffset.UTC&#41;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;ZoneOffset.UTC&#41;.plusDays&#40;10&#41;&#41;;
     *
     * ShareSignedIdentifier permission = new ShareSignedIdentifier&#40;&#41;.setId&#40;&quot;mypolicy&quot;&#41;.setAccessPolicy&#40;accessPolicy&#41;;
     *
     * Response&lt;ShareInfo&gt; response = shareClient.setAccessPolicyWithResponse&#40;
     *     new ShareSetAccessPolicyOptions&#40;&#41;.setPermissions&#40;Collections.singletonList&#40;permission&#41;&#41;
     *         .setRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Setting access policies completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.setAccessPolicyWithResponse#ShareSetAccessPolicyOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-share-acl">Azure Docs</a>.</p>
     *
     * @param options {@link ShareSetAccessPolicyOptions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareInfo information of the share} with headers and response status
     * code
     * @throws ShareStorageException If the share doesn't exist, a stored access policy doesn't have all fields filled
     * out, or the share will have more than five policies.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareInfo> setAccessPolicyWithResponse(ShareSetAccessPolicyOptions options, Duration timeout,
        Context context) {
        options = options == null ? new ShareSetAccessPolicyOptions() : options;
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        List<ShareSignedIdentifier> permissions =
            ModelHelper.truncateAccessPolicyPermissionsToSeconds(options.getPermissions());
        Context finalContext = context == null ? Context.NONE : context;

        Callable<Response<Void>> operation = () -> this.azureFileStorageClient.getShares()
            .setAccessPolicyNoCustomHeadersWithResponse(shareName, null, requestConditions.getLeaseId(), permissions,
                finalContext);

        return ModelHelper.mapToShareInfoResponse(sendRequest(operation, timeout, ShareStorageException.class));
    }

    /**
     * Retrieves storage statistics about the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the storage statistics</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.getStatistics -->
     * <pre>
     * ShareStatistics response = shareClient.getStatistics&#40;&#41;;
     * System.out.printf&#40;&quot;The share is using %d GB&quot;, response.getShareUsageInGB&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.getStatistics -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-stats">Azure Docs</a>.</p>
     *
     * @return The storage {@link ShareStatistics statistics of the share}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareStatistics getStatistics() {
        return getStatisticsWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Retrieves storage statistics about the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the storage statistics</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.getStatisticsWithResponse#duration-context -->
     * <pre>
     * Response&lt;ShareStatistics&gt; response = shareClient.getStatisticsWithResponse&#40;
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;The share is using %d GB&quot;, response.getValue&#40;&#41;.getShareUsageInGB&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.getStatisticsWithResponse#duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-stats">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareStatistics statistics of the share}
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareStatistics> getStatisticsWithResponse(Duration timeout, Context context) {
        return getStatisticsWithResponse(new ShareGetStatisticsOptions(), timeout, context);
    }

    /**
     * Retrieves storage statistics about the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the storage statistics</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.getStatisticsWithResponse#ShareGetStatisticsOptions-Duration-Context -->
     * <pre>
     * Response&lt;ShareStatistics&gt; response = shareClient.getStatisticsWithResponse&#40;
     *     new ShareGetStatisticsOptions&#40;&#41;.setRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;The share is using %d GB&quot;, response.getValue&#40;&#41;.getShareUsageInGB&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.getStatisticsWithResponse#ShareGetStatisticsOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-share-stats">Azure Docs</a>.</p>
     *
     * @param options {@link ShareGetStatisticsOptions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareStatistics statistics of the share}
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareStatistics> getStatisticsWithResponse(ShareGetStatisticsOptions options, Duration timeout,
        Context context) {
        options = options == null ? new ShareGetStatisticsOptions() : options;
        ShareRequestConditions requestConditions = options.getRequestConditions() == null
            ? new ShareRequestConditions() : options.getRequestConditions();
        Context finalContext = context == null ? Context.NONE : context;

        Callable<Response<ShareStats>> operation = () -> this.azureFileStorageClient.getShares()
            .getStatisticsNoCustomHeadersWithResponse(shareName, null, requestConditions.getLeaseId(), finalContext);

        return ModelHelper.mapGetStatisticsResponse(sendRequest(operation, timeout, ShareStorageException.class));
    }

    /**
     * Creates the directory in the share with the given name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory "documents"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.createDirectory#string -->
     * <pre>
     * ShareDirectoryClient response = shareClient.createDirectory&#40;&quot;mydirectory&quot;&#41;;
     * System.out.println&#40;&quot;Complete creating the directory.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.createDirectory#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @return A response containing a {@link ShareDirectoryClient} to interact with the created directory.
     * @throws ShareStorageException If the share doesn't exist, the directory already exists or is in the process of
     * being deleted, or the parent directory for the new directory doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareDirectoryClient createDirectory(String directoryName) {
        return createDirectoryWithResponse(directoryName, null, null, null,
            null, Context.NONE).getValue();
    }

    /**
     * Creates the directory in the share with the given name and associates the passed metadata to it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory "documents" with metadata "directory:metadata"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.createDirectoryWithResponse#String-FileSmbProperties-String-Map-Duration-Context -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * Response&lt;ShareDirectoryClient&gt; response = shareClient.createDirectoryWithResponse&#40;&quot;documents&quot;,
     *     smbProperties, filePermission, Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Creating the directory completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.createDirectoryWithResponse#String-FileSmbProperties-String-Map-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param metadata Optional metadata to associate with the directory
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @return A response containing a {@link ShareDirectoryAsyncClient} to interact with the created directory and the
     * status of its creation.
     * @throws ShareStorageException If the share doesn't exist, the directory already exists or is in the process of
     * being deleted, the parent directory for the new directory doesn't exist, or the metadata is using an illegal key
     * name
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareDirectoryClient> createDirectoryWithResponse(String directoryName,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata, Duration timeout,
        Context context) {
        ShareDirectoryClient shareDirectoryClient = getDirectoryClient(directoryName);
        return new SimpleResponse<>(shareDirectoryClient.createWithResponse(smbProperties, filePermission, metadata,
            timeout, context), shareDirectoryClient);
    }

    /**
     * Creates the directory in the share with the given name if it does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory "documents"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.createDirectoryIfNotExists#string -->
     * <pre>
     * ShareDirectoryClient directoryClient = shareClient.createDirectoryIfNotExists&#40;&quot;mydirectory&quot;&#41;;
     * System.out.println&#40;&quot;Complete creating the directory.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.createDirectoryIfNotExists#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @return The {@link ShareDirectoryClient} used to interact with the directory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareDirectoryClient createDirectoryIfNotExists(String directoryName) {
        return createDirectoryIfNotExistsWithResponse(directoryName, new ShareDirectoryCreateOptions(), null,
            Context.NONE).getValue();
    }

    /**
     * Creates the directory if it does not exist in the share with the given name and associates the
     * passed metadata to it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory "documents" with metadata "directory:metadata"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.createDirectoryIfNotExistsWithResponse#String-ShareDirectoryCreateOptions-Duration-Context -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;;
     * ShareDirectoryCreateOptions options = new ShareDirectoryCreateOptions&#40;&#41;.setSmbProperties&#40;smbProperties&#41;.
     *     setFilePermission&#40;filePermission&#41;.setMetadata&#40;metadata&#41;;
     * Response&lt;ShareDirectoryClient&gt; response = shareClient.createDirectoryIfNotExistsWithResponse&#40;&quot;documents&quot;,
     *     options, Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     *
     * if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *     System.out.println&#40;&quot;Already existed.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Create completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.createDirectoryIfNotExistsWithResponse#String-ShareDirectoryCreateOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @param options {@link ShareDirectoryCreateOptions}
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link ShareDirectoryClient}
     * used to interact with the directory created. If {@link Response}'s status code is 201, a new directory was
     * successfully created. If status code is 409, a directory with the same name already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareDirectoryClient> createDirectoryIfNotExistsWithResponse(String directoryName,
        ShareDirectoryCreateOptions options, Duration timeout, Context context) {
        ShareDirectoryClient shareDirectoryClient = getDirectoryClient(directoryName);
        Response<ShareDirectoryInfo> response = shareDirectoryClient.createIfNotExistsWithResponse(options, timeout, context);
        return new SimpleResponse<>(response, shareDirectoryClient);
    }

    /**
     * Creates the file in the share with the given name and file max size.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file "myfile" with size of 1024 bytes.</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.createFile#string-long -->
     * <pre>
     * ShareFileClient response = shareClient.createFile&#40;&quot;myfile&quot;, 1024&#41;;
     * System.out.println&#40;&quot;Complete creating the file with snapshot Id:&quot; + response.getShareSnapshotId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.createFile#string-long -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file.
     * @param maxSize The maximum size in bytes for the file.
     * @return A response containing a {@link ShareFileClient} to interact with the created file.
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
    public ShareFileClient createFile(String fileName, long maxSize) {
        return createFileWithResponse(fileName, maxSize, null, null, null,
            null, null, Context.NONE).getValue();
    }

    /**
     * Creates the file in the share with the given name, file max size and associates the passed properties to it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file "myfile" with length of 1024 bytes, some headers, file smb properties and metadata</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-Duration-Context -->
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
     * Response&lt;ShareFileClient&gt; response = shareClient.createFileWithResponse&#40;&quot;myfile&quot;, 1024,
     *     httpHeaders, smbProperties, filePermission, Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Creating the file completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file.
     * @param maxSize The maximum size in bytes for the file.
     * @param httpHeaders Additional parameters for the operation.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file
     * @param metadata Optional name-value pairs associated with the file as metadata.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing a {@link ShareFileClient} to interact with the created file and the status of its
     * creation.
     * @throws ShareStorageException If one of the following cases happen:
     * <ul>
     * <li>
     * If the share or parent directory does not exist.
     * </li>
     * <li>
     * An attempt to create file on a share snapshot will fail with 400 (InvalidQueryParameterValue).
     * </li>
     * </ul>
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileClient> createFileWithResponse(String fileName, long maxSize,
        ShareFileHttpHeaders httpHeaders, FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata, Duration timeout, Context context) {
        return this.createFileWithResponse(fileName, maxSize, httpHeaders, smbProperties, filePermission, metadata,
            null, timeout, context);
    }

    /**
     * Creates the file in the share with the given name, file max size and associates the passed properties to it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file "myfile" with length of 1024 bytes, some headers, file smb properties and metadata</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions-Duration-Context -->
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
     * Response&lt;ShareFileClient&gt; response = shareClient.createFileWithResponse&#40;&quot;myfile&quot;, 1024,
     *     httpHeaders, smbProperties, filePermission, Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;,
     *     requestConditions, Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Creating the file completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file.
     * @param maxSize The maximum size in bytes for the file.
     * @param httpHeaders Additional parameters for the operation.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file
     * @param metadata Optional name-value pairs associated with the file as metadata.
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing a {@link ShareFileClient} to interact with the created file and the status of its
     * creation.
     * @throws ShareStorageException If one of the following cases happen:
     * <ul>
     * <li>
     * If the share or parent directory does not exist.
     * </li>
     * <li>
     * An attempt to create file on a share snapshot will fail with 400 (InvalidQueryParameterValue).
     * </li>
     * </ul>
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileClient> createFileWithResponse(String fileName, long maxSize,
        ShareFileHttpHeaders httpHeaders, FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata, ShareRequestConditions requestConditions, Duration timeout, Context context) {
        ShareFileClient shareFileClient = getFileClient(fileName);
        return new SimpleResponse<>(shareFileClient.createWithResponse(maxSize, httpHeaders, smbProperties,
            filePermission, metadata, requestConditions, timeout, context), shareFileClient);
    }

    /**
     * Deletes the specified directory in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory "mydirectory"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.deleteDirectory#string -->
     * <pre>
     * shareClient.deleteDirectory&#40;&quot;mydirectory&quot;&#41;;
     * System.out.println&#40;&quot;Completed deleting the directory.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.deleteDirectory#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @throws ShareStorageException If the share doesn't exist or the directory isn't empty
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteDirectory(String directoryName) {
        deleteDirectoryWithResponse(directoryName, null, Context.NONE);
    }


    /**
     * Deletes the specified directory in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory "mydirectory"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.deleteDirectoryWithResponse#string-duration-context -->
     * <pre>
     * Response&lt;Void&gt; response = shareClient.deleteDirectoryWithResponse&#40;&quot;mydirectory&quot;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Complete deleting the directory with status code: &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.deleteDirectoryWithResponse#string-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share doesn't exist or the directory isn't empty
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDirectoryWithResponse(String directoryName, Duration timeout, Context context) {
        return getDirectoryClient(directoryName).deleteWithResponse(timeout, context);
    }

    /**
     * Deletes the specified directory in the share if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory "mydirectory"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.deleteDirectoryIfExists#string -->
     * <pre>
     * boolean result = shareClient.deleteDirectoryIfExists&#40;&quot;mydirectory&quot;&#41;;
     * System.out.println&#40;&quot;Directory deleted: &quot; + result&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.deleteDirectoryIfExists#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @throws ShareStorageException If the directory isn't empty
     * @return {@code true} if the directory is successfully deleted, {@code false} if the directory does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean deleteDirectoryIfExists(String directoryName) {
        return deleteDirectoryIfExistsWithResponse(directoryName, null, Context.NONE).getValue();
    }

    /**
     * Deletes the specified directory in the share if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory "mydirectory"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.deleteDirectoryIfExistsWithResponse#string-duration-context -->
     * <pre>
     * Response&lt;Boolean&gt; response = shareClient.deleteDirectoryIfExistsWithResponse&#40;&quot;mydirectory&quot;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *     System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.deleteDirectoryIfExistsWithResponse#string-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 202, the directory
     * was successfully deleted. If status code is 404, the directory does not exist.
     * @throws ShareStorageException If the directory isn't empty
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> deleteDirectoryIfExistsWithResponse(String directoryName, Duration timeout, Context context) {
        return getDirectoryClient(directoryName).deleteIfExistsWithResponse(timeout, context);
    }

    /**
     * Deletes the specified file in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "myfile"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.deleteFile#string -->
     * <pre>
     * shareClient.deleteFile&#40;&quot;myfile&quot;&#41;;
     * System.out.println&#40;&quot;Complete deleting the file.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.deleteFile#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @throws ShareStorageException If the share or the file doesn't exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteFile(String fileName) {
        deleteFileWithResponse(fileName, null, Context.NONE);
    }

    /**
     * Deletes the specified file in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "myfile"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.deleteFileWithResponse#string-duration-context -->
     * <pre>
     * Response&lt;Void&gt; response = shareClient.deleteFileWithResponse&#40;&quot;myfile&quot;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Complete deleting the file with status code: &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.deleteFileWithResponse#string-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share or the file doesn't exist.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteFileWithResponse(String fileName, Duration timeout, Context context) {
        return this.deleteFileWithResponse(fileName, null, timeout, context);
    }

    /**
     * Deletes the specified file in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "myfile"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.deleteFileWithResponse#string-ShareRequestConditions-duration-context -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * Response&lt;Void&gt; response = shareClient.deleteFileWithResponse&#40;&quot;myfile&quot;, requestConditions,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Complete deleting the file with status code: &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.deleteFileWithResponse#string-ShareRequestConditions-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share or the file doesn't exist.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteFileWithResponse(String fileName, ShareRequestConditions requestConditions,
        Duration timeout, Context context) {
        return getFileClient(fileName).deleteWithResponse(requestConditions, timeout, context);
    }

    /**
     * Deletes the specified file in the share if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "myfile"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.deleteFileIfExists#string -->
     * <pre>
     * boolean result = shareClient.deleteFileIfExists&#40;&quot;myfile&quot;&#41;;
     * System.out.println&#40;&quot;File deleted: &quot; + result&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.deleteFileIfExists#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @return {@code true} if the file is successfully deleted, {@code false} if the file does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean deleteFileIfExists(String fileName) {
        return deleteFileIfExistsWithResponse(fileName, null, null, Context.NONE).getValue();
    }

    /**
     * Deletes the specified file in the share if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "myfile"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.deleteFileIfExistsWithResponse#string-ShareRequestConditions-duration-context -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * Response&lt;Boolean&gt; response = shareClient.deleteFileIfExistsWithResponse&#40;&quot;myfile&quot;, requestConditions,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *     System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.deleteFileIfExistsWithResponse#string-ShareRequestConditions-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 202, the file
     * was successfully deleted. If status code is 404, the file does not exist.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> deleteFileIfExistsWithResponse(String fileName, ShareRequestConditions requestConditions,
        Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        ShareRequestConditions finalRequestConditions = requestConditions == null ? new ShareRequestConditions() : requestConditions;

        try {
            Response<Void> response = this.deleteFileWithResponse(fileName, finalRequestConditions, timeout, finalContext);
            return new SimpleResponse<>(response, true);

        } catch (ShareStorageException e) {
            if (e.getStatusCode() == 404 && e.getErrorCode().equals(ShareErrorCode.RESOURCE_NOT_FOUND)) {
                HttpResponse res = e.getResponse();
                return new SimpleResponse<>(res.getRequest(), res.getStatusCode(), res.getHeaders(), false);
            } else {
                throw LOGGER.logExceptionAsError(e);
            }
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Creates a permission at the share level. If a permission already exists, it returns the key of it, else creates a
     * new permission and returns the key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.createPermission#string -->
     * <pre>
     * String response = shareClient.createPermission&#40;&quot;filePermission&quot;&#41;;
     * System.out.printf&#40;&quot;The file permission key is %s&quot;, response&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.createPermission#string -->
     *
     * @param filePermission The file permission to get/create.
     * @return The file permission key associated with the file permission.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String createPermission(String filePermission) {
        return createPermissionWithResponse(filePermission, Context.NONE).getValue();
    }

    /**
     * Creates a permission t the share level. If a permission already exists, it returns the key of it, else creates a
     * new permission and returns the key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.createPermissionWithResponse#string-context -->
     * <pre>
     * Response&lt;String&gt; response = shareClient.createPermissionWithResponse&#40;&quot;filePermission&quot;, Context.NONE&#41;;
     * System.out.printf&#40;&quot;The file permission key is %s&quot;, response.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.createPermissionWithResponse#string-context -->
     *
     * @param filePermission The file permission to get/create.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that contains the file permission key associated with the file permission.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> createPermissionWithResponse(String filePermission, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        SharePermission sharePermission = new SharePermission().setPermission(filePermission);
        ResponseBase<SharesCreatePermissionHeaders, Void> response = this.azureFileStorageClient.getShares()
            .createPermissionWithResponse(shareName, sharePermission, null, finalContext);

        return new SimpleResponse<>(response,
            response.getDeserializedHeaders().getXMsFilePermissionKey());
    }

    /**
     * Gets a permission for a given key
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.getPermission#string -->
     * <pre>
     * String response = shareClient.getPermission&#40;&quot;filePermissionKey&quot;&#41;;
     * System.out.printf&#40;&quot;The file permission is %s&quot;, response&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.getPermission#string -->
     *
     * @param filePermissionKey The file permission key.
     * @return The file permission associated with the file permission key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String getPermission(String filePermissionKey) {
        return getPermissionWithResponse(filePermissionKey, Context.NONE).getValue();
    }

    /**
     * Gets a permission for a given key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.getPermissionWithResponse#string-context -->
     * <pre>
     * Response&lt;String&gt; response = shareClient.getPermissionWithResponse&#40;&quot;filePermissionKey&quot;, Context.NONE&#41;;
     * System.out.printf&#40;&quot;The file permission is %s&quot;, response.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.getPermissionWithResponse#string-context -->
     *
     * @param filePermissionKey The file permission key.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that contains th file permission associated with the file permission key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> getPermissionWithResponse(String filePermissionKey, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        ResponseBase<SharesGetPermissionHeaders, SharePermission> response = this.azureFileStorageClient.getShares()
            .getPermissionWithResponse(shareName, filePermissionKey, null, finalContext);

        return new SimpleResponse<>(response, response.getValue().getPermission());
    }

    /**
     * Get snapshot id which attached to {@link ShareClient}. Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareClient.getSnapshotId -->
     * <pre>
     * OffsetDateTime currentTime = OffsetDateTime.of&#40;LocalDateTime.now&#40;&#41;, ZoneOffset.UTC&#41;;
     * ShareClient shareClient = new ShareClientBuilder&#40;&#41;.endpoint&#40;&quot;https:&#47;&#47;$&#123;accountName&#125;.file.core.windows.net&quot;&#41;
     *     .sasToken&#40;&quot;$&#123;SASToken&#125;&quot;&#41;
     *     .shareName&#40;&quot;myshare&quot;&#41;
     *     .snapshot&#40;currentTime.toString&#40;&#41;&#41;
     *     .buildClient&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Snapshot ID: %s%n&quot;, shareClient.getSnapshotId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.getSnapshotId -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareClient.getShareName -->
     * <pre>
     * String shareName = shareClient.getShareName&#40;&#41;;
     * System.out.println&#40;&quot;The name of the share is &quot; + shareName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareClient.getShareName -->
     *
     * @return The name of the share.
     */
    public String getShareName() {
        return this.shareName;
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
        return this.generateSas(shareServiceSasSignatureValues, Context.NONE);
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
