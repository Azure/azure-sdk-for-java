// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.DateTimeRfc1123;
import com.azure.core.util.Context;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.implementation.AzureFileStorageBuilder;
import com.azure.storage.file.implementation.AzureFileStorageImpl;
import com.azure.storage.file.models.ShareCreateSnapshotHeaders;
import com.azure.storage.file.models.ShareGetPropertiesHeaders;
import com.azure.storage.file.models.ShareInfo;
import com.azure.storage.file.models.ShareProperties;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.ShareStatistics;
import com.azure.storage.file.models.SharesCreateSnapshotResponse;
import com.azure.storage.file.models.SharesGetPropertiesResponse;
import com.azure.storage.file.models.SharesGetStatisticsResponse;
import com.azure.storage.file.models.SignedIdentifier;
import com.azure.storage.file.models.StorageErrorException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This class provides a azureFileStorageClient that contains all the operations for interacting with a share in Azure Storage Share.
 * Operations allowed by the azureFileStorageClient are creating and deleting the share, creating snapshots for the share, creating and
 * deleting directories in the share and retrieving and updating properties metadata and access policies of the share.
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
 * @see SASTokenCredential
 */
public class ShareAsyncClient {
    private final AzureFileStorageImpl azureFileStorageClient;
    private final String shareName;
    private final String snapshot;

    /**
     * Creates a ShareAsyncClient that sends requests to the storage share at {@link AzureFileStorageImpl#url() endpoint}.
     * Each service call goes through the {@link HttpPipeline pipeline} in the {@code azureFileStorageClient}.
     *
     * @param client Client that interacts with the service interfaces
     * @param shareName Name of the share
     */
    ShareAsyncClient(AzureFileStorageImpl client, String shareName) {
        this.shareName = shareName;
        this.snapshot = null;

        this.azureFileStorageClient = client;
    }

    /**
     * Creates a ShareAsyncClient that sends requests to the storage share at {@code endpoint}.
     * Each service call goes through the {@code httpPipeline}.
     *
     * @param endpoint URL for the Storage File service
     * @param httpPipeline HttpPipeline that the HTTP requests and response flow through
     * @param shareName Name of the share
     * @param snapshot Optional specific snapshot of the share
     */
    ShareAsyncClient(URL endpoint, HttpPipeline httpPipeline, String shareName, String snapshot) {
        this.shareName = shareName;
        this.snapshot = snapshot;

        this.azureFileStorageClient = new AzureFileStorageBuilder().pipeline(httpPipeline)
            .url(endpoint.toString())
            .build();
    }

    /**
     * Get the url of the storage share client.
     * @return the url of the Storage Share.
     * @throws RuntimeException If the share is using a malformed URL.
     */
    public URL getShareUrl() {
        try {
            return new URL(azureFileStorageClient.url());
        } catch (MalformedURLException e) {
            throw new RuntimeException(String.format("Invalid URL on %s: %s" + getClass().getSimpleName(),
                azureFileStorageClient.url()), e);
        }
    }


    /**
     * Constructs a {@link DirectoryAsyncClient} that interacts with the root directory in the share.
     *
     * <p>If the directory doesn't exist in the share {@link DirectoryAsyncClient#create(Map) create} in the azureFileStorageClient will
     * need to be called before interaction with the directory can happen.</p>
     *
     * @return a {@link DirectoryAsyncClient} that interacts with the root directory in the share
     */
    public DirectoryAsyncClient getRootDirectoryClient() {
        return getDirectoryClient("");
    }

    /**
     * Constructs a {@link DirectoryAsyncClient} that interacts with the specified directory.
     *
     * <p>If the directory doesn't exist in the share {@link DirectoryAsyncClient#create(Map) create} in the azureFileStorageClient will
     * need to be called before interaction with the directory can happen.</p>
     *
     * @param directoryName Name of the directory
     * @return a {@link DirectoryAsyncClient} that interacts with the directory in the share
     */
    public DirectoryAsyncClient getDirectoryClient(String directoryName) {
        return new DirectoryAsyncClient(azureFileStorageClient, shareName, directoryName, snapshot);
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
     * @return A response containing information about the share and the status its creation.
     * @throws StorageErrorException If the share already exists with different metadata
     */
    public Mono<Response<ShareInfo>> create() {
        return create(null, null);
    }

    /**
     * Creates the share in the storage account with the specified metadata and quota.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share with metadata "share:metadata"</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.create#map-integer.metadata}
     *
     * <p>Create the share with a quota of 10 GB</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.create#map-integer.quota}
     *
     * @param metadata Optional metadata to associate with the share
     * @param quotaInGB Optional maximum size the share is allowed to grow to in GB. This must be greater than 0 and
     * less than or equal to 5120. The default value is 5120.
     * @return A response containing information about the share and the status its creation.
     * @throws StorageErrorException If the share already exists with different metadata or {@code quotaInGB} is outside the
     * allowed range.
     */
    public Mono<Response<ShareInfo>> create(Map<String, String> metadata, Integer quotaInGB) {
        return azureFileStorageClient.shares().createWithRestResponseAsync(shareName, null, metadata, quotaInGB, Context.NONE)
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
     * @return A response containing information about the snapshot of share.
     * @throws StorageErrorException If the share doesn't exist, there are 200 snapshots of the share, or a snapshot is
     * in progress for the share
     */
    public Mono<Response<ShareSnapshotInfo>> createSnapshot() {
        return createSnapshot(null);
    }

    /**
     * Creates a snapshot of the share with the metadata that was passed associated to the snapshot.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a snapshot with metadata "snapshot:metadata"</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.createSnapshot#map}
     *
     * @param metadata Optional metadata to associate with the snapshot. If {@code null} the metadata of the share
     * will be copied to the snapshot.
     * @return A response containing information about the snapshot of share.
     * @throws StorageErrorException If the share doesn't exist, there are 200 snapshots of the share, or a snapshot is
     * in progress for the share
     */
    public Mono<Response<ShareSnapshotInfo>> createSnapshot(Map<String, String> metadata) {
        return azureFileStorageClient.shares().createSnapshotWithRestResponseAsync(shareName, null, metadata, Context.NONE)
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
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist
     */
    public Mono<VoidResponse> delete() {
        return azureFileStorageClient.shares().deleteWithRestResponseAsync(shareName, snapshot, null, null,  Context.NONE)
            .map(VoidResponse::new);
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
     * @return the properties of the share
     * @throws StorageErrorException If the share doesn't exist
     */
    public Mono<Response<ShareProperties>> getProperties() {
        return azureFileStorageClient.shares().getPropertiesWithRestResponseAsync(shareName, snapshot, null, Context.NONE)
            .map(this::mapGetPropertiesResponse);
    }

    /**
     * Sets the maximum size in GB that the share is allowed to grow.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the quota to 1024 GB</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.setQuota}
     *
     * @param quotaInGB Size in GB to limit the share's growth. The quota in GB must be between 1 and 5120.
     * @return information about the share
     * @throws StorageErrorException If the share doesn't exist or {@code quotaInGB} is outside the allowed bounds
     */
    public Mono<Response<ShareInfo>> setQuota(int quotaInGB) {
        return azureFileStorageClient.shares().setQuotaWithRestResponseAsync(shareName, null, quotaInGB, Context.NONE)
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
     * @param metadata Metadata to set on the share, if null is passed the metadata for the share is cleared
     * @return information about the share
     * @throws StorageErrorException If the share doesn't exist or the metadata contains invalid keys
     */
    public Mono<Response<ShareInfo>> setMetadata(Map<String, String> metadata) {
        return azureFileStorageClient.shares().setMetadataWithRestResponseAsync(shareName, null, metadata, Context.NONE)
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
     * @return The stored access policies specified on the queue.
     * @throws StorageErrorException If the share doesn't exist
     */
    public Flux<SignedIdentifier> getAccessPolicy() {
        return azureFileStorageClient.shares().getAccessPolicyWithRestResponseAsync(shareName, Context.NONE)
            .flatMapMany(response -> Flux.fromIterable(response.value()));
    }

    /**
     * Sets stored access policies for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.setAccessPolicy}
     *
     * @param permissions Access policies to set on the queue
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist, a stored access policy doesn't have all fields filled out,
     * or the share will have more than five policies.
     */
    public Mono<Response<ShareInfo>> setAccessPolicy(List<SignedIdentifier> permissions) {
        return azureFileStorageClient.shares().setAccessPolicyWithRestResponseAsync(shareName, permissions, null, Context.NONE)
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
     * @return the storage statistics of the share
     */
    public Mono<Response<ShareStatistics>> getStatistics() {
        return azureFileStorageClient.shares().getStatisticsWithRestResponseAsync(shareName, Context.NONE)
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
     *      *
     * @param directoryName Name of the directory
     * @return A response containing a {@link DirectoryAsyncClient} to interact with the created directory and the
     * status of its creation.
     * @throws StorageErrorException If the share doesn't exist, the directory already exists or is in the process of
     * being deleted, or the parent directory for the new directory doesn't exist
     */
    public Mono<Response<DirectoryAsyncClient>> createDirectory(String directoryName) {
        return createDirectory(directoryName, null);
    }

    /**
     * Creates the directory in the share with the given name and associates the passed metadata to it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory "documents" with metadata "directory:metadata"</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.createDirectory#string-map}
     *
     * @param directoryName Name of the directory
     * @param metadata Optional metadata to associate with the directory
     * @return A response containing a {@link DirectoryAsyncClient} to interact with the created directory and the
     * status of its creation.
     * @throws StorageErrorException If the share doesn't exist, the directory already exists or is in the process of
     * being deleted, the parent directory for the new directory doesn't exist, or the metadata is using an illegal
     * key name
     */
    public Mono<Response<DirectoryAsyncClient>> createDirectory(String directoryName, Map<String, String> metadata) {
        DirectoryAsyncClient directoryAsyncClient = getDirectoryClient(directoryName);
        return directoryAsyncClient.create(metadata).map(response -> new SimpleResponse<>(response, directoryAsyncClient));
    }

    /**
     * Deletes the specified directory in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory "empty"</p>
     *
     * {@codesnippet com.azure.storage.file.shareAsyncClient.deleteDirectory#string}
     *
     * @param directoryName Name of the directory
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist or the directory isn't empty
     */
    public Mono<VoidResponse> deleteDirectory(String directoryName) {
        return getDirectoryClient(directoryName).delete().map(VoidResponse::new);
    }

    /**
     * Get snapshot id which attached to {@link ShareAsyncClient}.
     * Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * {@codesnippet com.azure.storage.file.fileAsyncClient.getShareSnapshotId}
     *
     * @return The snapshot id which is a unique {@code DateTime} value that identifies the share snapshot to its base share.
     */
    public String getSnapshotId() {
        return this.snapshot;
    }

    private Response<ShareInfo> mapToShareInfoResponse(Response<?> response) {
        String eTag = response.headers().value("ETag");
        OffsetDateTime lastModified = new DateTimeRfc1123(response.headers().value("Last-Modified")).dateTime();

        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), new ShareInfo(eTag, lastModified));
    }

    private Response<ShareSnapshotInfo> mapCreateSnapshotResponse(SharesCreateSnapshotResponse response) {
        ShareCreateSnapshotHeaders headers = response.deserializedHeaders();
        ShareSnapshotInfo snapshotInfo = new ShareSnapshotInfo(headers.snapshot(), headers.eTag(), headers.lastModified());

        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), snapshotInfo);
    }

    private Response<ShareProperties> mapGetPropertiesResponse(SharesGetPropertiesResponse response) {
        ShareGetPropertiesHeaders headers = response.deserializedHeaders();
        ShareProperties shareProperties = new ShareProperties().quota(headers.quota())
            .etag(headers.eTag())
            .lastModified(headers.lastModified())
            .metadata(headers.metadata());

        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), shareProperties);
    }

    private Response<ShareStatistics> mapGetStatisticsResponse(SharesGetStatisticsResponse response) {
        ShareStatistics shareStatistics = new ShareStatistics(response.value().shareUsageBytes() / 1024);

        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), shareStatistics);
    }
}
