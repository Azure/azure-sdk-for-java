// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import static com.azure.core.implementation.util.FluxUtil.withContext;
import static com.azure.storage.file.PostProcessor.postProcessResponse;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.DateTimeRfc1123;
import com.azure.core.implementation.http.PagedResponseBase;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.implementation.AzureFileStorageImpl;
import com.azure.storage.file.implementation.models.ShareCreateSnapshotHeaders;
import com.azure.storage.file.implementation.models.ShareGetPropertiesHeaders;
import com.azure.storage.file.implementation.models.SharePermission;
import com.azure.storage.file.implementation.models.SharesCreateSnapshotResponse;
import com.azure.storage.file.implementation.models.SharesGetPropertiesResponse;
import com.azure.storage.file.implementation.models.SharesGetStatisticsResponse;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.ShareInfo;
import com.azure.storage.file.models.ShareProperties;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.ShareStatistics;
import com.azure.storage.file.models.SignedIdentifier;
import com.azure.storage.file.models.StorageException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import reactor.core.publisher.Mono;

/**
 * This class provides a azureFileStorageClient that contains all the operations for interacting with a share in Azure
 * Storage Share. Operations allowed by the azureFileStorageClient are creating and deleting the share, creating
 * snapshots for the share, creating and deleting directories in the share and retrieving and updating properties
 * metadata and access policies of the share.
 *
 * <p><strong>Instantiating an Asynchronous Share Client</strong></p>
 *
 * {@codesnippet com.azure.storage.file.shareAsyncClient.instantiation}
 *
 * <p>View {@link ShareClientBuilder this} for additional ways to construct the azureFileStorageClient.</p>
 *
 * @see ShareClientBuilder
 * @see ShareClient
 * @see SharedKeyCredential
 */
@ServiceClient(builder = ShareClientBuilder.class, isAsync = true)
public class ShareAsyncClient {
    private final ClientLogger logger = new ClientLogger(ShareAsyncClient.class);

    private final AzureFileStorageImpl azureFileStorageClient;
    private final String shareName;
    private final String snapshot;
    private final String accountName;

    /**
     * Creates a ShareAsyncClient that sends requests to the storage share at {@link AzureFileStorageImpl#getUrl()
     * endpoint}. Each service call goes through the {@link HttpPipeline pipeline} in the
     * {@code azureFileStorageClient}.
     *
     * @param client Client that interacts with the service interfaces
     * @param shareName Name of the share
     */
    ShareAsyncClient(AzureFileStorageImpl client, String shareName, String snapshot, String accountName) {
        Objects.requireNonNull(shareName, "'shareName' cannot be null.");
        this.shareName = shareName;
        this.snapshot = snapshot;
        this.accountName = accountName;
        this.azureFileStorageClient = client;
    }

    /**
     * Get the url of the storage share client.
     *
     * @return the url of the Storage Share.
     */
    public String getShareUrl() {
        StringBuilder shareUrlString = new StringBuilder(azureFileStorageClient.getUrl()).append("/").append(shareName);
        if (snapshot != null) {
            shareUrlString.append("?snapshot=").append(snapshot);
        }
        return shareUrlString.toString();
    }


    /**
     * Constructs a {@link DirectoryAsyncClient} that interacts with the root directory in the share.
     *
     * <p>If the directory doesn't exist in the share {@link DirectoryAsyncClient#create()} in the
     * azureFileStorageClient will need to be called before interaction with the directory can happen.</p>
     *
     * @return a {@link DirectoryAsyncClient} that interacts with the root directory in the share
     */
    public DirectoryAsyncClient getRootDirectoryClient() {
        return getDirectoryClient("");
    }

    /**
     * Constructs a {@link DirectoryAsyncClient} that interacts with the specified directory.
     *
     * <p>If the directory doesn't exist in the share {@link DirectoryAsyncClient#create() create} in the
     * azureFileStorageClient will need to be called before interaction with the directory can happen.</p>
     *
     * @param directoryName Name of the directory
     * @return a {@link DirectoryAsyncClient} that interacts with the directory in the share
     */
    public DirectoryAsyncClient getDirectoryClient(String directoryName) {
        return new DirectoryAsyncClient(azureFileStorageClient, shareName, directoryName, snapshot, accountName);
    }

    /**
     * Constructs a {@link FileAsyncClient} that interacts with the specified file.
     *
     * <p>If the file doesn't exist in the share {@link FileAsyncClient#create(long)} ) create} in the client will
     * need to be called before interaction with the file can happen.</p>
     *
     * @param filePath Name of the file
     * @return a {@link FileAsyncClient} that interacts with the file in the share
     */
    public FileAsyncClient getFileClient(String filePath) {
        return new FileAsyncClient(azureFileStorageClient, shareName, filePath, snapshot, accountName);
    }

    /**
     * Creates the share in the storage account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.create}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @return The information about the {@link ShareInfo share}
     * @throws StorageException If the share already exists with different metadata
     */
    public Mono<ShareInfo> create() {
        return createWithResponse(null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates the share in the storage account with the specified metadata and quota.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share with metadata "share:metadata"</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.createWithResponse#map-integer.metadata}
     *
     * <p>Create the share with a quota of 10 GB</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.createWithResponse#map-integer.quota}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to associate with the share
     * @param quotaInGB Optional maximum size the share is allowed to grow to in GB. This must be greater than 0 and
     * less than or equal to 5120. The default value is 5120.
     * @return A response containing information about the {@link ShareInfo share} and the status its creation.
     * @throws StorageException If the share already exists with different metadata or {@code quotaInGB} is outside the
     * allowed range.
     */
    public Mono<Response<ShareInfo>> createWithResponse(Map<String, String> metadata, Integer quotaInGB) {
        return withContext(context -> createWithResponse(metadata, quotaInGB, context));
    }

    Mono<Response<ShareInfo>> createWithResponse(Map<String, String> metadata, Integer quotaInGB, Context context) {
        return postProcessResponse(azureFileStorageClient.shares()
            .createWithRestResponseAsync(shareName, null, metadata, quotaInGB, context))
            .map(this::mapToShareInfoResponse);
    }

    /**
     * Creates a snapshot of the share with the same metadata associated to the share at the time of creation.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a snapshot</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.createSnapshot}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/snapshot-share">Azure Docs</a>.</p>
     *
     * @return The information about the {@link ShareSnapshotInfo snapshot of share}.
     * @throws StorageException If the share doesn't exist, there are 200 snapshots of the share, or a snapshot is in
     * progress for the share
     */
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
     * {@codesnippet com.azure.storage.file.shareAsyncClient.createSnapshotWithResponse#map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/snapshot-share">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to associate with the snapshot. If {@code null} the metadata of the share will
     * be copied to the snapshot.
     * @return A response containing information about the {@link ShareSnapshotInfo snapshot of share}.
     * @throws StorageException If the share doesn't exist, there are 200 snapshots of the share, or a snapshot is in
     * progress for the share
     */
    public Mono<Response<ShareSnapshotInfo>> createSnapshotWithResponse(Map<String, String> metadata) {
        return withContext(context -> createSnapshotWithResponse(metadata, context));
    }

    Mono<Response<ShareSnapshotInfo>> createSnapshotWithResponse(Map<String, String> metadata, Context context) {
        return postProcessResponse(azureFileStorageClient.shares()
            .createSnapshotWithRestResponseAsync(shareName, null, metadata, context))
            .map(this::mapCreateSnapshotResponse);
    }

    /**
     * Deletes the share in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.delete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @return An empty response
     * @throws StorageException If the share doesn't exist
     */
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
     * {@codesnippet com.azure.storage.file.shareAsyncClient.deleteWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     * @throws StorageException If the share doesn't exist
     */
    public Mono<Response<Void>> deleteWithResponse() {
        return withContext(this::deleteWithResponse);
    }

    Mono<Response<Void>> deleteWithResponse(Context context) {
        return postProcessResponse(azureFileStorageClient.shares()
            .deleteWithRestResponseAsync(shareName, snapshot, null, null, context))
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
     * {@codesnippet com.azure.storage.file.shareAsyncClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @return The {@link ShareProperties properties of the share}
     * @throws StorageException If the share doesn't exist
     */
    public Mono<ShareProperties> getProperties() {
        return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves the properties of the share, these include the metadata associated to it and the quota that the share
     * is restricted to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the share properties</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.getPropertiesWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @return A response containing the {@link ShareProperties properties of the share} with headers and response
     * status code
     * @throws StorageException If the share doesn't exist
     */
    public Mono<Response<ShareProperties>> getPropertiesWithResponse() {
        return withContext(this::getPropertiesWithResponse);
    }

    Mono<Response<ShareProperties>> getPropertiesWithResponse(Context context) {
        return postProcessResponse(azureFileStorageClient.shares()
            .getPropertiesWithRestResponseAsync(shareName, snapshot, null, context))
            .map(this::mapGetPropertiesResponse);
    }

    /**
     * Sets the maximum size in GB that the share is allowed to grow.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the quota to 1024 GB</p>
     *
     * {@codesnippet com.azure.storage.file.ShareAsyncClient.setQuota#int}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @param quotaInGB Size in GB to limit the share's growth. The quota in GB must be between 1 and 5120.
     * @return The {@link ShareInfo information about the share}
     * @throws StorageException If the share doesn't exist or {@code quotaInGB} is outside the allowed bounds
     */
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
     * {@codesnippet com.azure.storage.file.ShareAsyncClient.setQuotaWithResponse#int}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @param quotaInGB Size in GB to limit the share's growth. The quota in GB must be between 1 and 5120.
     * @return A response containing the {@link ShareInfo information about the share} with headers and response status
     * code
     * @throws StorageException If the share doesn't exist or {@code quotaInGB} is outside the allowed bounds
     */
    public Mono<Response<ShareInfo>> setQuotaWithResponse(int quotaInGB) {
        return withContext(context -> setQuotaWithResponse(quotaInGB, context));
    }

    Mono<Response<ShareInfo>> setQuotaWithResponse(int quotaInGB, Context context) {
        return postProcessResponse(azureFileStorageClient.shares()
            .setQuotaWithRestResponseAsync(shareName, null, quotaInGB, context))
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
     * {@codesnippet com.azure.storage.file.shareAsyncClient.setMetadata#map}
     *
     * <p>Clear the metadata of the share</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.clearMetadata#map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-share-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to set on the share, if null is passed the metadata for the share is cleared
     * @return The {@link ShareInfo information about the share}
     * @throws StorageException If the share doesn't exist or the metadata contains invalid keys
     */
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
     * {@codesnippet com.azure.storage.file.shareAsyncClient.setMetadata#map}
     *
     * <p>Clear the metadata of the share</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.clearMetadata#map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-share-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to set on the share, if null is passed the metadata for the share is cleared
     * @return A response containing the {@link ShareInfo information about the share} with headers and response status
     * code
     * @throws StorageException If the share doesn't exist or the metadata contains invalid keys
     */
    public Mono<Response<ShareInfo>> setMetadataWithResponse(Map<String, String> metadata) {
        return withContext(context -> setMetadataWithResponse(metadata, context));
    }

    Mono<Response<ShareInfo>> setMetadataWithResponse(Map<String, String> metadata, Context context) {
        return postProcessResponse(azureFileStorageClient.shares()
            .setMetadataWithRestResponseAsync(shareName, null, metadata, context))
            .map(this::mapToShareInfoResponse);
    }

    /**
     * Retrieves stored access policies specified for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List the stored access policies</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.getAccessPolicy}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-share-acl">Azure Docs</a>.</p>
     *
     * @return The stored access policies specified on the queue.
     * @throws StorageException If the share doesn't exist
     */
    public PagedFlux<SignedIdentifier> getAccessPolicy() {
        Function<String, Mono<PagedResponse<SignedIdentifier>>> retriever =
            marker -> postProcessResponse(this.azureFileStorageClient.shares()
                .getAccessPolicyWithRestResponseAsync(shareName, Context.NONE))
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue(),
                    null,
                    response.getDeserializedHeaders()));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    /**
     * Sets stored access policies for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * {@codesnippet com.azure.storage.file.ShareAsyncClient.setAccessPolicy#List}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-share-acl">Azure Docs</a>.</p>
     *
     * @param permissions Access policies to set on the queue
     * @return The {@link ShareInfo information about the share}
     * @throws StorageException If the share doesn't exist, a stored access policy doesn't have all fields filled out,
     * or the share will have more than five policies.
     */

    public Mono<ShareInfo> setAccessPolicy(List<SignedIdentifier> permissions) {
        return setAccessPolicyWithResponse(permissions).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets stored access policies for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * {@codesnippet com.azure.storage.file.ShareAsyncClient.setAccessPolicyWithResponse#List}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-share-acl">Azure Docs</a>.</p>
     *
     * @param permissions Access policies to set on the queue
     * @return A response containing the {@link ShareInfo information about the share} with headers and response status
     * code
     * @throws StorageException If the share doesn't exist, a stored access policy doesn't have all fields filled out,
     * or the share will have more than five policies.
     */
    public Mono<Response<ShareInfo>> setAccessPolicyWithResponse(List<SignedIdentifier> permissions) {
        return withContext(context -> setAccessPolicyWithResponse(permissions, context));
    }

    Mono<Response<ShareInfo>> setAccessPolicyWithResponse(List<SignedIdentifier> permissions, Context context) {
        /*
        We truncate to seconds because the service only supports nanoseconds or seconds, but doing an
        OffsetDateTime.now will only give back milliseconds (more precise fields are zeroed and not serialized). This
        allows for proper serialization with no real detriment to users as sub-second precision on active time for
        signed identifiers is not really necessary.
         */
        if (permissions != null) {
            for (SignedIdentifier permission : permissions) {
                if (permission.getAccessPolicy() != null && permission.getAccessPolicy().getStart() != null) {
                    permission.getAccessPolicy().setStart(
                        permission.getAccessPolicy().getStart().truncatedTo(ChronoUnit.SECONDS));
                }
                if (permission.getAccessPolicy() != null && permission.getAccessPolicy().getExpiry() != null) {
                    permission.getAccessPolicy().setExpiry(
                        permission.getAccessPolicy().getExpiry().truncatedTo(ChronoUnit.SECONDS));
                }
            }
        }

        return postProcessResponse(azureFileStorageClient.shares()
            .setAccessPolicyWithRestResponseAsync(shareName, permissions, null, context))
            .map(this::mapToShareInfoResponse);
    }

    /**
     * Retrieves storage statistics about the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the storage statistics</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.getStatistics}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-share-stats">Azure Docs</a>.</p>
     *
     * @return The storage {@link ShareStatistics statistics of the share}
     */
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
     * {@codesnippet com.azure.storage.file.shareAsyncClient.getStatisticsWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-share-stats">Azure Docs</a>.</p>
     *
     * @return A response containing the storage {@link ShareStatistics statistics of the share} with headers and
     * response status code
     */
    public Mono<Response<ShareStatistics>> getStatisticsWithResponse() {
        return withContext(this::getStatisticsWithResponse);
    }

    Mono<Response<ShareStatistics>> getStatisticsWithResponse(Context context) {
        return postProcessResponse(azureFileStorageClient.shares()
            .getStatisticsWithRestResponseAsync(shareName, context))
            .map(this::mapGetStatisticsResponse);
    }

    /**
     * Creates the directory in the share with the given name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory "mydirectory"</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.createDirectory#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @return The {@link DirectoryAsyncClient} to interact with the created directory.
     * @throws StorageException If the share doesn't exist, the directory already exists or is in the process of being
     * deleted, or the parent directory for the new directory doesn't exist
     */
    public Mono<DirectoryAsyncClient> createDirectory(String directoryName) {
        return createDirectoryWithResponse(directoryName, null, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates the directory in the share with the given name and associates the passed metadata to it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory "documents" with metadata "directory:metadata"</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.createDirectoryWithResponse#string-filesmbproperties-string-map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param metadata Optional metadata to associate with the directory
     * @return A response containing a {@link DirectoryAsyncClient} to interact with the created directory and the
     * status of its creation.
     * @throws StorageException If the share doesn't exist, the directory already exists or is in the process of being
     * deleted, the parent directory for the new directory doesn't exist, or the metadata is using an illegal key name
     */
    public Mono<Response<DirectoryAsyncClient>> createDirectoryWithResponse(String directoryName,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata) {
        return withContext(context ->
            createDirectoryWithResponse(directoryName, smbProperties, filePermission, metadata, context));
    }

    Mono<Response<DirectoryAsyncClient>> createDirectoryWithResponse(String directoryName,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata, Context context) {
        DirectoryAsyncClient directoryAsyncClient = getDirectoryClient(directoryName);
        return postProcessResponse(directoryAsyncClient.createWithResponse(smbProperties, filePermission, metadata))
            .map(response -> new SimpleResponse<>(response, directoryAsyncClient));
    }

    /**
     * Creates the file in the share with the given name and file max size.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file "myfile" with size of 1024 bytes.</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.createFile#string-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file.
     * @param maxSize The maximum size in bytes for the file, up to 1 TiB.
     * @return The {@link FileAsyncClient} to interact with the created file.
     * @throws StorageException If one of the following cases happen:
     * <ul>
     * <li>
     * If the share or parent directory does not exist.
     * </li>
     * <li>
     * An attempt to create file on a share snapshot will fail with 400 (InvalidQueryParameterValue).
     * </li>
     * </ul>
     */
    public Mono<FileAsyncClient> createFile(String fileName, long maxSize) {
        return createFileWithResponse(fileName, maxSize, null, null, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates the file in the share with the given name, file max size and associates the passed properties to it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file "myfile" with length of 1024 bytes, some headers, file smb properties and metadata</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.createFileWithResponse#string-long-filehttpheaders-filesmbproperties-string-map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file.
     * @param maxSize The maximum size in bytes for the file, up to 1 TiB.
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file.
     * @param metadata Optional name-value pairs associated with the file as metadata.
     * @return A response containing a {@link FileAsyncClient} to interact with the created file and the status of its
     * creation.
     * @throws StorageException If one of the following cases happen:
     * <ul>
     * <li>
     * If the share or parent directory does not exist.
     * </li>
     * <li>
     * An attempt to create file on a share snapshot will fail with 400 (InvalidQueryParameterValue).
     * </li>
     * </ul>
     */
    public Mono<Response<FileAsyncClient>> createFileWithResponse(String fileName, long maxSize,
        FileHTTPHeaders httpHeaders, FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata) {
        return withContext(context ->
            createFileWithResponse(fileName, maxSize, httpHeaders, smbProperties, filePermission, metadata, context));
    }

    Mono<Response<FileAsyncClient>> createFileWithResponse(String fileName, long maxSize, FileHTTPHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata, Context context) {
        FileAsyncClient fileAsyncClient = getFileClient(fileName);
        return postProcessResponse(fileAsyncClient
            .createWithResponse(maxSize, httpHeaders, smbProperties, filePermission, metadata, context))
            .map(response -> new SimpleResponse<>(response, fileAsyncClient));
    }

    /**
     * Deletes the specified directory in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory "mydirectory"</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.deleteDirectory#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @return An empty response
     * @throws StorageException If the share doesn't exist or the directory isn't empty
     */
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
     * {@codesnippet com.azure.storage.file.shareAsyncClient.deleteDirectory#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @return A response that only contains headers and response status code
     * @throws StorageException If the share doesn't exist or the directory isn't empty
     */
    public Mono<Response<Void>> deleteDirectoryWithResponse(String directoryName) {
        return withContext(context -> deleteDirectoryWithResponse(directoryName, context));
    }

    Mono<Response<Void>> deleteDirectoryWithResponse(String directoryName, Context context) {
        return postProcessResponse(getDirectoryClient(directoryName).deleteWithResponse(context));
    }

    /**
     * Deletes the specified file in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "myfile"</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.deleteFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file.
     * @return A empty response
     * @throws StorageException If the share or the file doesn't exist.
     */
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
     * {@codesnippet com.azure.storage.file.shareAsyncClient.deleteFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file.
     * @return A response that only contains headers and response status code
     * @throws StorageException If the share or the file doesn't exist.
     */
    public Mono<Response<Void>> deleteFileWithResponse(String fileName) {
        return withContext(context -> deleteFileWithResponse(fileName, context));
    }

    Mono<Response<Void>> deleteFileWithResponse(String fileName, Context context) {
        return postProcessResponse(getFileClient(fileName).deleteWithResponse(context));
    }

    /**
     * Creates a permission at the share level. If a permission already exists, it returns the key of it, else creates a
     * new permission and returns the key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.createPermission#string}
     *
     * @param filePermission The file permission to get/create.
     * @return The file permission key associated with the file permission.
     */
    public Mono<String> createPermission(String filePermission) {
        return createPermissionWithResponse(filePermission).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a permission at the share level. If a permission already exists, it returns the key of it, else creates a
     * new permission and returns the key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.createPermissionWithResponse#string}
     *
     * @param filePermission The file permission to get/create.
     * @return A response that contains the file permission key associated with the file permission.
     */
    public Mono<Response<String>> createPermissionWithResponse(String filePermission) {
        return withContext(context -> createPermissionWithResponse(filePermission, context));
    }

    Mono<Response<String>> createPermissionWithResponse(String filePermission, Context context) {
        // NOTE: Should we check for null or empty?
        SharePermission sharePermission = new SharePermission().setPermission(filePermission);
        return postProcessResponse(azureFileStorageClient.shares()
            .createPermissionWithRestResponseAsync(shareName, sharePermission, null, context))
            .map(response -> new SimpleResponse<>(response, response.getDeserializedHeaders().getFilePermissionKey()));
    }

    /**
     * Gets a permission for a given key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.getPermission#string}
     *
     * @param filePermissionKey The file permission key.
     * @return The file permission associated with the file permission key.
     */
    public Mono<String> getPermission(String filePermissionKey) {
        return getPermissionWithResponse(filePermissionKey).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a permission for a given key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.getPermissionWithResponse#string}
     *
     * @param filePermissionKey The file permission key.
     * @return A response that contains th file permission associated with the file permission key.
     */
    public Mono<Response<String>> getPermissionWithResponse(String filePermissionKey) {
        return withContext(context -> getPermissionWithResponse(filePermissionKey, context));
    }

    Mono<Response<String>> getPermissionWithResponse(String filePermissionKey, Context context) {
        return postProcessResponse(azureFileStorageClient.shares()
            .getPermissionWithRestResponseAsync(shareName, filePermissionKey, null, context))
            .map(response -> new SimpleResponse<>(response, response.getValue().getPermission()));
    }

    /**
     * Get snapshot id which attached to {@link ShareAsyncClient}. Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.getShareSnapshotId}
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
     * {@codesnippet com.azure.storage.file.shareAsyncClient.getShareName}
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

    private Response<ShareInfo> mapToShareInfoResponse(Response<?> response) {
        String eTag = response.getHeaders().getValue("ETag");
        OffsetDateTime lastModified =
            new DateTimeRfc1123(response.getHeaders().getValue("Last-Modified")).getDateTime();

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            new ShareInfo(eTag, lastModified));
    }

    private Response<ShareSnapshotInfo> mapCreateSnapshotResponse(SharesCreateSnapshotResponse response) {
        ShareCreateSnapshotHeaders headers = response.getDeserializedHeaders();
        ShareSnapshotInfo snapshotInfo =
            new ShareSnapshotInfo(headers.getSnapshot(), headers.getETag(), headers.getLastModified());

        return new SimpleResponse<>(response, snapshotInfo);
    }

    private Response<ShareProperties> mapGetPropertiesResponse(SharesGetPropertiesResponse response) {
        ShareGetPropertiesHeaders headers = response.getDeserializedHeaders();
        ShareProperties shareProperties = new ShareProperties().setQuota(headers.getQuota())
            .setEtag(headers.getETag())
            .setLastModified(headers.getLastModified())
            .setMetadata(headers.getMetadata());

        return new SimpleResponse<>(response, shareProperties);
    }

    private Response<ShareStatistics> mapGetStatisticsResponse(SharesGetStatisticsResponse response) {
        ShareStatistics shareStatistics = new ShareStatistics((int) (response.getValue().getShareUsageBytes() / 1024));

        return new SimpleResponse<>(response, shareStatistics);
    }
}
