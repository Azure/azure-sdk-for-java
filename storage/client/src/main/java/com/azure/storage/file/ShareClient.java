// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.file.models.ShareInfo;
import com.azure.storage.file.models.ShareProperties;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.ShareStatistics;
import com.azure.storage.file.models.SignedIdentifier;

import java.util.List;
import java.util.Map;

/**
 * Share client
 */
public class ShareClient {
    private final ShareAsyncClient client;

    ShareClient(ShareAsyncClient client) {
        this.client = client;
    }

    /**
     * @return a new builder instace
     */
    public static ShareClientBuilder builder() {
        return new ShareClientBuilder();
    }

    /**
     * @return the URL of the share in the Storage account
     */
    public String getUrl() {
        return client.getUrl();
    }

    /**
     * @return a DirectoryClient that interacts with the root directory in the share
     */
    public DirectoryClient getRootDirectoryClient() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param directoryName Name of the directory
     * @return a DirectoryClient that interacts with the specified directory
     */
    public DirectoryClient getDirectoryClient(String directoryName) {
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
    public Response<ShareInfo> create() {
        return create(null, null);
    }

    /**
     * Creates the share that is linked to this client with the specified metadata and quota.
     *
     * @param metadata Optional. Metadata about the share
     * @param quotaInGB Maximum size in GB that the share is allowed to grow
     * @return information about the share
     */
    public Response<ShareInfo> create(Map<String, String> metadata, Integer quotaInGB) {
        return client.create(metadata, quotaInGB).block();
    }

    /**
     * Creates a snapshot of the share.
     *
     * @return information about the snapshot
     */
    public Response<ShareSnapshotInfo> createSnapshot() {
        return createSnapshot(null);
    }

    /**
     * Creates a snapshot with specific metadata.
     *
     * @param metadata Metadata to give to the snapshot
     * @return information about the snapshot
     */
    public Response<ShareSnapshotInfo> createSnapshot(Map<String, String> metadata) {
        return client.createSnapshot(metadata).block();
    }

    /**
     * Deletes the share and all snapshots of the share that the client is interacting with
     * @return an empty response
     */
    public VoidResponse delete() {
        return delete(null);
    }

    /**
     * Deletes a specific snapshot of the share
     * @param shareSnapshot Identifier of the snapshot
     * @return an empty response
     */
    public VoidResponse delete(String shareSnapshot) {
        return client.delete(shareSnapshot).block();
    }

    /**
     * Gets properties of the share
     *
     * @return properties of the share
     */
    public Response<ShareProperties> getProperties() {
        return getProperties(null);
    }

    /**
     * Gets properties of a specific snapshot of the share
     * @param shareSnapshot Identifier of the snapshot
     * @return properties of the specific snapshot
     */
    public Response<ShareProperties> getProperties(String shareSnapshot) {
        return client.getProperties(shareSnapshot).block();
    }

    /**
     * Sets the maximum size in GB that the share is allowed to grow
     * @param quotaInGB Size in GB to limit the share's growth
     * @return information about the share
     */
    public Response<ShareInfo> setQuota(int quotaInGB) {
        return client.setQuota(quotaInGB).block();
    }

    /**
     * Sets metadata about the share
     * @param metadata Metadata to set on the share, if null is passed the metadata for the share is cleared
     * @return information about the share
     */
    public Response<ShareInfo> setMetadata(Map<String, String> metadata) {
        return client.setMetadata(metadata).block();
    }

    /**
     * Gets the access policies for the share
     * @return the access policies of the share
     */
    public Iterable<SignedIdentifier> getAccessPolicy() {
        return client.getAccessPolicy().toIterable();
    }

    /**
     * Sets the access policies for the share
     * @param permissions Permissions to set on the share
     * @return information about the share
     */
    public Response<ShareInfo> setAccessPolicy(List<SignedIdentifier> permissions) {
        return client.setAccessPolicy(permissions).block();
    }

    /**
     * Gets the storage statistics for the share
     * @return storage statistics of the share
     */
    public Response<ShareStatistics> getStatistics() {
        return client.getStatistics().block();
    }

    /**
     * Creates a directory in the share
     * @param directoryName Name of the directory
     * @param metadata Metadata to set on the directory
     * @return a DirectoryClient to interact with the created directory
     */
    public Response<DirectoryClient> createDirectory(String directoryName, Map<String, String> metadata) {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes a directory in the share
     * @param directoryName Name of the directory
     * @return an empty response
     */
    public VoidResponse deleteDirectory(String directoryName) {
        throw new UnsupportedOperationException();
    }
}
