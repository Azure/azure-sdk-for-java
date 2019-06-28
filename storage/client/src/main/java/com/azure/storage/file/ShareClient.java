// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.ShareInfo;
import com.azure.storage.file.models.ShareProperties;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.ShareStatistics;
import com.azure.storage.file.models.SignedIdentifier;
import com.azure.storage.file.models.StorageErrorException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class provides a client that contains all the operations for interacting with a share in Azure Storage Share.
 * Operations allowed by the client are creating and deleting the share, creating snapshots for the share, creating and
 * deleting directories in the share and retrieving and updating properties metadata and access policies of the share.
 *
 * <p><strong>Instantiating a Synchronous Share Client</strong></p>
 *
 * <pre>
 * ShareClient client = ShareClient.builder()
 *     .connectionString(connectionString)
 *     .endpoint(endpoint)
 *     .build();
 * </pre>
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
     * Creates a builder that can configure options for the ShareClient before creating an instance of it.
     *
     * @return A new {@link ShareClientBuilder} used create ShareClient instances.
     */
    public static ShareClientBuilder builder() {
        return new ShareClientBuilder();
    }

    /**
     * @return the getShareUrl of the storage file service
     */
    public String getShareUrl() {
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
     * @codesnippet com.azure.storage.file.shareClient.createShare
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
     * <pre>
     * Response&lt;ShareInfo&gt; response = client.createShare(Collections.singletonMap("share", "metadata"), null);
     * System.out.printf("Creating the share completed with status code %d", response.statusCode());
     * </pre>
     *
     * <p>Create the share with a quota of 10 GB</p>
     *
     * <pre>
     * Response&lt;ShareInfo&gt; response = client.createShare(null, 10);
     * System.out.printf("Creating the share completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param metadata Optional. Metadata to associate with the share
     * @param quotaInGB Optional. Maximum size the share is allowed to grow to in GB. This must be greater than 0 and
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
     * @codesnippt com.azure.storage.file.shareClient.createSnapshot
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
     * <pre>
     * Response&lt;ShareSnapshotInfo&gt; response = client.createShare(Collections.singletonMap("snapshot", "metadata"));
     * System.out.printf("Snapshot %s was created", response.value().snapshot());
     * </pre>
     *
     * @param metadata Optional. Metadata to associate with the snapshot. If {@code null} the metadata of the share
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
     * @codesnippet com.azure.storage.file.shareClient.delete
     *
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist
     */
    public VoidResponse delete() {
        return delete(null);
    }

    /**
     * Deletes the specific snapshot of the share in the storage account. Snapshot are identified by the time they
     * were created.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the snapshot of share that was created at midnight</p>
     *
     * <pre>
     * OffsetDateTime midnight = OffsetDateTime.of(LocalTime.MIDNIGHT, ZoneOffset.UTC));
     * VoidResponse response = client.deleteShare(midnight.toString());
     * System.out.printf("Deleting the snapshot completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param shareSnapshot Identifier of the snapshot
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist or the snapshot doesn't exist
     */
    public VoidResponse delete(String shareSnapshot) {
        return client.delete(shareSnapshot).block();
    }

    /**
     * Retrieves the properties of the share, these include the metadata associated to it and the quota that the share
     * is restricted to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the share properties</p>
     *
     * <pre>
     * ShareProperties properties = client.getProperties().value();
     * System.out.printf("Share quota: %d, Metadata: %s", properties.quota(), properties.metadata());
     * </pre>
     *
     * @return the properties of the share
     * @throws StorageErrorException If the share doesn't exist
     */
    public Response<ShareProperties> getProperties() {
        return getProperties(null);
    }

    /**
     * Retrieves the properties of a specific snapshot of the share, these include the metadata associated to it and
     * the quota that the share is restricted to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the properties from the snapshot at midnight</p>
     *
     * <pre>
     * OffsetDateTime midnight = OffsetDateTime.of(LocalTime.MIDNIGHT, ZoneOffset.UTC));
     * ShareProperties properties = client.getProperties(midnight.toString()).value();
     * System.out.printf("Share quota: %d, Metadata: %s", properties.quota(), properties.metadata());
     * </pre>
     *
     * @param shareSnapshot Identifier of the snapshot
     * @return the properties of the share snapshot
     * @throws StorageErrorException If the share or snapshot doesn't exist
     */
    public Response<ShareProperties> getProperties(String shareSnapshot) {
        return client.getProperties(shareSnapshot).block();
    }

    /**
     * Sets the maximum size in GB that the share is allowed to grow.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the quota to 1024 GB</p>
     *
     * <pre>
     * Response&lt;ShareInfo&gt; response = client.setQuota(1024);
     * System.out.printf("Setting the share quota completed with status code %d", response.statusCode());
     * </pre>
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
     * <pre>
     * Response&lt;ShareInfo&gt; response = client.setMetadata(Collections.singletonMap("share", "updatedMetadata"));
     * System.out.printf("Setting the share metadata completed with status code %d", response.statusCode());
     * </pre>
     *
     * <p>Clear the metadata of the share</p>
     *
     * <pre>
     * Response&lt;ShareInfo&gt; response = client.setMetadata(null);
     * System.out.printf("Clearing the share metadata completed with status code %d", response.statusCode());
     * </pre>
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
     * <pre>
     * for (SignedIdentifier result : client.getAccessPolicy()) {
     *     System.out.printf("Access policy %s allows these permissions: %s", result.id(), result.accessPolicy().permission());
     * }
     * </pre>
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
     * <pre>
     * AccessPolicy policy = new AccessPolicy().permission("r")
     *     .start(OffsetDateTime.now(ZoneOffset.UTC))
     *     .expiry(OffsetDateTime.now(ZoneOffset.UTC).addDays(10));
     *
     * SignedIdentifier permission = new SignedIdentifier().id("mypolicy").accessPolicy(accessPolicy);
     *
     * Response&lt;ShareInfo&gt; response = client.setAccessPolicy(Collections.singletonList(permission));
     * System.out.printf("Setting access policies completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param permissions Access policies to set on the queue
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist, a stored access policy doesn't have all fields filled out,
     * or the share will have more than five policies.
     */
    public Response<ShareInfo> setAccessPolicy(List<SignedIdentifier> permissions) {
        List<SignedIdentifier> p = Arrays.asList(new SignedIdentifier());
        return client.setAccessPolicy(permissions).block();
    }

    /**
     * Retrieves storage statistics about the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the storage statistics</p>
     *
     * <pre>
     * Response&lt;ShareStatistics&gt; response = client.getStatistics();
     * System.out.printf("The share is using %d GB", response.value().getShareUsageInGB());
     * </pre>
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
     * @codesnippet com.azure.storage.file.shareClient.createDirectory
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
     * <pre>
     * Response&lt;DirectoryClient&gt; response = client.createDirectory("documents", Collections.singletonMap("directory", "metadata"));
     * System.out.printf("Creating the directory completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param directoryName Name of the directory
     * @param metadata Optional. Metadata to associate with the directory
     * @return A response containing a {@link DirectoryAsyncClient} to interact with the created directory and the
     * status of its creation.
     * @throws StorageErrorException If the share doesn't exist, the directory already exists or is in the process of
     * being deleted, the parent directory for the new directory doesn't exist, or the metadata is using an illegal
     * key name
     */
    public Response<DirectoryClient> createDirectory(String directoryName, Map<String, String> metadata) {
        return client.createDirectory(directoryName, metadata).map(response -> DirectoryAsyncClient.mapResponse(response, new DirectoryClient(response.value()))).block();
    }

    /**
     * Deletes the specified directory in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory "empty"</p>
     *
     * @codesnippet com.azure.storage.file.shareClient.deleteDirectory#string
     *
     * @param directoryName Name of the directory
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist or the directory isn't empty
     */
    public VoidResponse deleteDirectory(String directoryName) {
        return client.deleteDirectory(directoryName).block();
    }

}
