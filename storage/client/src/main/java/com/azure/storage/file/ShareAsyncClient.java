// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.file.implementation.AzureFileStorageImpl;
import com.azure.storage.file.models.ShareCreateHeaders;
import com.azure.storage.file.models.ShareCreateSnapshotHeaders;
import com.azure.storage.file.models.ShareGetPropertiesHeaders;
import com.azure.storage.file.models.ShareGetStatisticsHeaders;
import com.azure.storage.file.models.ShareInfo;
import com.azure.storage.file.models.ShareProperties;
import com.azure.storage.file.models.ShareSetAccessPolicyHeaders;
import com.azure.storage.file.models.ShareSetMetadataHeaders;
import com.azure.storage.file.models.ShareSetQuotaHeaders;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.ShareStatistics;
import com.azure.storage.file.models.SharesCreateResponse;
import com.azure.storage.file.models.SharesCreateSnapshotResponse;
import com.azure.storage.file.models.SharesGetPropertiesResponse;
import com.azure.storage.file.models.SharesGetStatisticsResponse;
import com.azure.storage.file.models.SharesSetAccessPolicyResponse;
import com.azure.storage.file.models.SharesSetMetadataResponse;
import com.azure.storage.file.models.SharesSetQuotaResponse;
import com.azure.storage.file.models.SignedIdentifier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Async share client
 */
public class ShareAsyncClient {
    private final AzureFileStorageImpl client;
    private final String shareName;
    private final String shareSnapshot;

    ShareAsyncClient(AzureFileStorageImpl client, String shareName) {
        this.shareName = shareName;
        this.shareSnapshot = null;
        this.client = new AzureFileStorageImpl(client.httpPipeline())
            .withUrl(client.url())
            .withVersion(client.version());
    }

    ShareAsyncClient(URL endpoint, HttpPipeline pipeline, String shareName, String shareSnapshot) {
        this.shareName = shareName;
        this.shareSnapshot = shareSnapshot;
        this.client = new AzureFileStorageImpl(pipeline)
            .withUrl(endpoint.toString());
    }

    /**
     * @return a new builder instance
     */
    public static ShareClientBuilder builder() {
        return new ShareClientBuilder();
    }

    /**
     * @return the URL of the share in the Storage account
     */
    public String getUrl() {
        return client.url();
    }

    // TODO (alzimmer): When Sima's and my development are merged into the feature branch implement this.
    /**
     * @return a DirectoryAsyncClient that interacts with the root directory in the share
     */
    public DirectoryAsyncClient getRootDirectoryClient() {
        throw new UnsupportedOperationException();
    }

    // TODO (alzimmer): When Sima's and my development are merged into the feature branch implement this.
    /**
     * @param directoryName Name of the directory
     * @return a DirectoryAsyncClient that interacts with the specified directory
     */
    public DirectoryAsyncClient getDirectoryClient(String directoryName) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates the share that is linked to this client
     *
     * This share will not have metadata and will have a maximum size of 5TB
     *
     * @return information about the share
     * @throws com.azure.storage.file.models.StorageErrorException If the share already exists
     */
    public Mono<Response<ShareInfo>> create() {
        return create(null, null);
    }

    /**
     * Creates the share that is linked to this client with the specified metadata and quota.
     *
     * @param metadata Optional. Metadata about the share
     * @param quotaInGB Maximum size in GB that the share is allowed to grow
     * @return information about the share
     */
    public Mono<Response<ShareInfo>> create(Map<String, String> metadata, Integer quotaInGB) {
        return client.shares().createWithRestResponseAsync(shareName, null, metadata, quotaInGB, Context.NONE)
            .map(this::mapCreateResponse);
    }

    /**
     * Creates a snapshot of the share.
     *
     * @return information about the snapshot
     */
    public Mono<Response<ShareSnapshotInfo>> createSnapshot() {
        return createSnapshot(null);
    }

    /**
     * Creates a snapshot with specific metadata.
     *
     * @param metadata Metadata to give to the snapshot
     * @return information about the snapshot
     */
    public Mono<Response<ShareSnapshotInfo>> createSnapshot(Map<String, String> metadata) {
        return client.shares().createSnapshotWithRestResponseAsync(shareName, null, metadata, Context.NONE)
            .map(this::mapCreateSnapshotResponse);
    }

    /**
     * Deletes the share and all snapshots of the share that the client is interacting with
     * @return an empty response
     */
    public Mono<VoidResponse> delete() {
        return delete(null);
    }

    /**
     * Deletes a specific snapshot of the share
     * @param shareSnapshot Identifier of the snapshot
     * @return an empty response
     */
    public Mono<VoidResponse> delete(String shareSnapshot) {
        return client.shares().deleteWithRestResponseAsync(shareName, shareSnapshot, null, null,  Context.NONE)
            .map(VoidResponse::new);
    }

    /**
     * Gets properties of the share
     *
     * @return properties of the share
     */
    public Mono<Response<ShareProperties>> getProperties() {
        return getProperties(null);
    }

    /**
     * Gets properties of a specific snapshot of the share
     * @param shareSnapshot Identifier of the snapshot
     * @return properties of the specific snapshot
     */
    public Mono<Response<ShareProperties>> getProperties(String shareSnapshot) {
        return client.shares().getPropertiesWithRestResponseAsync(shareName, shareSnapshot, null, Context.NONE)
            .map(this::mapGetPropertiesResponse);
    }

    /**
     * Sets the maximum size in GB that the share is allowed to grow
     * @param quotaInGB Size in GB to limit the share's growth
     * @return information about the share
     */
    public Mono<Response<ShareInfo>> setQuota(int quotaInGB) {
        return client.shares().setQuotaWithRestResponseAsync(shareName, null, quotaInGB, Context.NONE)
            .map(this::mapSetQuotaResponse);
    }

    /**
     * Sets metadata about the share
     * @param metadata Metadata to set on the share, if null is passed the metadata for the share is cleared
     * @return information about the share
     */
    public Mono<Response<ShareInfo>> setMetadata(Map<String, String> metadata) {
        return client.shares().setMetadataWithRestResponseAsync(shareName, null, metadata, Context.NONE)
            .map(this::mapSetMetadataResponse);
    }

    /**
     * Gets the access policies for the share
     * @return the access policies of the share
     */
    public Flux<SignedIdentifier> getAccessPolicy() {
        return client.shares().getAccessPolicyWithRestResponseAsync(shareName, Context.NONE)
            .flatMapMany(response -> Flux.fromIterable(response.value()));
    }

    /**
     * Sets the access policies for the share
     * @param permissions Permissions to set on the share
     * @return information about the share
     */
    public Mono<Response<ShareInfo>> setAccessPolicy(List<SignedIdentifier> permissions) {
        return client.shares().setAccessPolicyWithRestResponseAsync(shareName, permissions, null, Context.NONE)
            .map(this::mapSetAccessPolicyResponse);
    }

    /**
     * Gets the storage statistics for the share
     * @return storage statistics of the share
     */
    public Mono<Response<ShareStatistics>> getStatistics() {
        return client.shares().getStatisticsWithRestResponseAsync(shareName, Context.NONE)
            .map(this::mapGetStatisticsResponse);
    }

    // TODO (alzimmer): When Sima's and my development are merged into the feature branch implement this.

    /**
     * Creates a directory in the share
     * @param directoryName Name of the directory
     * @param metadata Metadata to set on the directory
     * @return a DirectoryAsyncClient to interact with the created directory
     */
    public Mono<Response<DirectoryAsyncClient>> createDirectory(String directoryName, Map<String, String> metadata) {
        throw new UnsupportedOperationException();
    }

    // TODO (alzimmer): When Sima's and my development are merged into the feature branch implement this.

    /**
     * Deletes a directory in the share
     * @param directoryName Name of the directory
     * @return an empty response
     */
    public Mono<VoidResponse> deleteDirectory(String directoryName) {
        throw new UnsupportedOperationException();
    }

    private Response<ShareInfo> mapCreateResponse(SharesCreateResponse response) {
        ShareCreateHeaders headers = response.deserializedHeaders();
        ShareInfo shareInfo = new ShareInfo().eTag(headers.eTag())
            .lastModified(headers.lastModified());

        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), shareInfo);
    }

    private Response<ShareSnapshotInfo> mapCreateSnapshotResponse(SharesCreateSnapshotResponse response) {
        ShareCreateSnapshotHeaders headers = response.deserializedHeaders();
        ShareSnapshotInfo snapshotInfo = new ShareSnapshotInfo().eTag(headers.eTag())
            .lastModified(headers.lastModified())
            .snapshot(headers.snapshot());

        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), snapshotInfo);
    }

    private Response<ShareInfo> mapSetQuotaResponse(SharesSetQuotaResponse response) {
        ShareSetQuotaHeaders headers = response.deserializedHeaders();
        ShareInfo shareInfo = new ShareInfo().eTag(headers.eTag())
            .lastModified(headers.lastModified());

        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), shareInfo);
    }

    private Response<ShareProperties> mapGetPropertiesResponse(SharesGetPropertiesResponse response) {
        ShareGetPropertiesHeaders headers = response.deserializedHeaders();
        ShareProperties shareProperties = new ShareProperties().quota(headers.quota())
            .etag(headers.eTag())
            .lastModified(headers.lastModified())
            .metadata(headers.metadata());

        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), shareProperties);
    }

    private Response<ShareInfo> mapSetMetadataResponse(SharesSetMetadataResponse response) {
        ShareSetMetadataHeaders headers = response.deserializedHeaders();
        ShareInfo shareInfo = new ShareInfo().eTag(headers.eTag())
            .lastModified(headers.lastModified());

        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), shareInfo);
    }

    private Response<ShareInfo> mapSetAccessPolicyResponse(SharesSetAccessPolicyResponse response) {
        ShareSetAccessPolicyHeaders headers = response.deserializedHeaders();
        ShareInfo shareInfo = new ShareInfo().eTag(headers.eTag())
            .lastModified(headers.lastModified());

        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), shareInfo);
    }

    private Response<ShareStatistics> mapGetStatisticsResponse(SharesGetStatisticsResponse response) {
        ShareGetStatisticsHeaders headers = response.deserializedHeaders();
        ShareStatistics shareStatistics = new ShareStatistics(response.value().shareUsageBytes() / 1024);

        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), shareStatistics);
    }
}
