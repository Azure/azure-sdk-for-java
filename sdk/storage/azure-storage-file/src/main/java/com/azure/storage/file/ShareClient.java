// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.ShareInfo;
import com.azure.storage.file.models.ShareProperties;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.ShareStatistics;
import com.azure.storage.file.models.SignedIdentifier;
import com.azure.storage.file.models.StorageErrorException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * This class provides a client that contains all the operations for interacting with a share in Azure Storage Share.
 * Operations allowed by the client are creating and deleting the share, creating snapshots for the share, creating and
 * deleting directories in the share and retrieving and updating properties metadata and access policies of the share.
 *
 * <p><strong>Instantiating a Synchronous Share Client</strong></p>
 *
 * {@codesnippet com.azure.storage.file.shareClient.instantiation}
 *
 * <p>View {@link ShareClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ShareClientBuilder
 * @see ShareAsyncClient
 * @see SharedKeyCredential
 * @see SASTokenCredential
 */
public class ShareClient {
    private final ShareAsyncClient client;

    ShareClient(ShareAsyncClient client) {
        this.client = client;
    }

    /**
     * Get the url of the storage share client.
     * @return the url of the Storage Share.
     * @throws RuntimeException If the share is using a malformed URL.
     */
    public URL getShareUrl() {
        return client.getShareUrl();
    }

    /**
     * Constructs a {@link DirectoryClient} that interacts with the root directory in the share.
     *
     * <p>If the directory doesn't exist in the share {@link DirectoryClient#create(Map) create} in the client will
     * need to be called before interaction with the directory can happen.</p>
     *
     * @return a {@link DirectoryClient} that interacts with the root directory in the share
     */
    public DirectoryClient getRootDirectoryClient() {
        return getDirectoryClient("");
    }

    /**
     * Constructs a {@link DirectoryClient} that interacts with the specified directory.
     *
     * <p>If the directory doesn't exist in the share {@link DirectoryClient#create(Map) create} in the client will
     * need to be called before interaction with the directory can happen.</p>
     *
     * @param directoryName Name of the directory
     * @return a {@link DirectoryClient} that interacts with the directory in the share
     */
    public DirectoryClient getDirectoryClient(String directoryName) {
        return new DirectoryClient(client.getDirectoryClient(directoryName));
    }

    /**
     * Creates the share in the storage account.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.create}
     *
     * @return A response containing information about the share and the status its creation.
     * @throws StorageErrorException If the share already exists with different metadata
     */
    public Response<ShareInfo> create() {
        return create(null, null);
    }

    /**
     * Creates the share in the storage account with the specified metadata and quota.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share with metadata "share:metadata"</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.create#map-integer.metadata}
     *
     * <p>Create the share with a quota of 10 GB</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.create#map-integer.quota}
     *
     * @param metadata Optional metadata to associate with the share
     * @param quotaInGB Optional maximum size the share is allowed to grow to in GB. This must be greater than 0 and
     * less than or equal to 5120. The default value is 5120.
     * @return A response containing information about the share and the status its creation.
     * @throws StorageErrorException If the share already exists with different metadata or {@code quotaInGB} is outside the
     * allowed range.
     */
    public Response<ShareInfo> create(Map<String, String> metadata, Integer quotaInGB) {
        return client.create(metadata, quotaInGB).block();
    }

    /**
     * Creates a snapshot of the share with the same metadata associated to the share at the time of creation.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a snapshot</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.createSnapshot}
     *
     * @return A response containing information about the snapshot of share.
     * @throws StorageErrorException If the share doesn't exist, there are 200 snapshots of the share, or a snapshot is
     * in progress for the share
     */
    public Response<ShareSnapshotInfo> createSnapshot() {
        return createSnapshot(null);
    }

    /**
     * Creates a snapshot of the share with the metadata that was passed associated to the snapshot.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a snapshot with metadata "snapshot:metadata"</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.createSnapshot#map}
     *
     * @param metadata Optional metadata to associate with the snapshot. If {@code null} the metadata of the share
     * will be copied to the snapshot.
     * @return A response containing information about the snapshot of share.
     * @throws StorageErrorException If the share doesn't exist, there are 200 snapshots of the share, or a snapshot is
     * in progress for the share
     */
    public Response<ShareSnapshotInfo> createSnapshot(Map<String, String> metadata) {
        return client.createSnapshot(metadata).block();
    }

    /**
     * Deletes the share in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.delete}
     *
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist
     */
    public VoidResponse delete() {
        return client.delete().block();
    }

    /**
     * Retrieves the properties of the share, these include the metadata associated to it and the quota that the share
     * is restricted to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the share properties</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.getProperties}
     *
     * @return the properties of the share
     * @throws StorageErrorException If the share doesn't exist
     */
    public Response<ShareProperties> getProperties() {
        return client.getProperties().block();
    }

    /**
     * Sets the maximum size in GB that the share is allowed to grow.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the quota to 1024 GB</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.setQuota}
     *
     * @param quotaInGB Size in GB to limit the share's growth. The quota in GB must be between 1 and 5120.
     * @return information about the share
     * @throws StorageErrorException If the share doesn't exist or {@code quotaInGB} is outside the allowed bounds
     */
    public Response<ShareInfo> setQuota(int quotaInGB) {
        return client.setQuota(quotaInGB).block();
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
     * {@codesnippet com.azure.storage.file.shareClient.setMetadata#map}
     *
     * <p>Clear the metadata of the share</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.clearMetadata#map}
     *
     * @param metadata Metadata to set on the share, if null is passed the metadata for the share is cleared
     * @return information about the share
     * @throws StorageErrorException If the share doesn't exist or the metadata contains invalid keys
     */
    public Response<ShareInfo> setMetadata(Map<String, String> metadata) {
        return client.setMetadata(metadata).block();
    }

    /**
     * Retrieves stored access policies specified for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List the stored access policies</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.getAccessPolicy}
     *
     * @return The stored access policies specified on the queue.
     * @throws StorageErrorException If the share doesn't exist
     */
    public Iterable<SignedIdentifier> getAccessPolicy() {
        return client.getAccessPolicy().toIterable();
    }

    /**
     * Sets stored access policies for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.setAccessPolicy}
     *
     * @param permissions Access policies to set on the queue
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist, a stored access policy doesn't have all fields filled out,
     * or the share will have more than five policies.
     */
    public Response<ShareInfo> setAccessPolicy(List<SignedIdentifier> permissions) {
        return client.setAccessPolicy(permissions).block();
    }

    /**
     * Retrieves storage statistics about the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the storage statistics</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.getStatistics}
     *
     * @return the storage statistics of the share
     */
    public Response<ShareStatistics> getStatistics() {
        return client.getStatistics().block();
    }

    /**
     * Creates the directory in the share with the given name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory "documents"</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.createDirectory#string}
     *
     * @param directoryName Name of the directory
     * @return A response containing a {@link DirectoryClient} to interact with the created directory and the
     * status of its creation.
     * @throws StorageErrorException If the share doesn't exist, the directory already exists or is in the process of
     * being deleted, or the parent directory for the new directory doesn't exist
     */
    public Response<DirectoryClient> createDirectory(String directoryName) {
        return createDirectory(directoryName, null);
    }

    /**
     * Creates the directory in the share with the given name and associates the passed metadata to it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory "documents" with metadata "directory:metadata"</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.createDirectory#string-map}
     *
     * @param directoryName Name of the directory
     * @param metadata Optional metadata to associate with the directory
     * @return A response containing a {@link DirectoryAsyncClient} to interact with the created directory and the
     * status of its creation.
     * @throws StorageErrorException If the share doesn't exist, the directory already exists or is in the process of
     * being deleted, the parent directory for the new directory doesn't exist, or the metadata is using an illegal
     * key name
     */
    public Response<DirectoryClient> createDirectory(String directoryName, Map<String, String> metadata) {
        DirectoryClient directoryClient = getDirectoryClient(directoryName);
        return new SimpleResponse<>(directoryClient.create(metadata), directoryClient);
    }

    /**
     * Deletes the specified directory in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory "empty"</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.deleteDirectory#string}
     *
     * @param directoryName Name of the directory
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist or the directory isn't empty
     */
    public VoidResponse deleteDirectory(String directoryName) {
        return client.deleteDirectory(directoryName).block();
    }

    /**
     * Get snapshot id which attached to {@link ShareClient}.
     * Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.getSnapshotId}
     *
     * @return The snapshot id which is a unique {@code DateTime} value that identifies the share snapshot to its base share.
     */
    public String getSnapshotId() {
        return client.getSnapshotId();
    }
}
