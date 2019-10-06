// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.ShareInfo;
import com.azure.storage.file.models.ShareProperties;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.ShareStatistics;
import com.azure.storage.file.models.SignedIdentifier;
import com.azure.storage.file.models.StorageException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

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
 */
@ServiceClient(builder = ShareClientBuilder.class)
public class ShareClient {
    private final ShareAsyncClient client;

    ShareClient(ShareAsyncClient client) {
        this.client = client;
    }

    /**
     * Get the url of the storage share client.
     *
     * @return the url of the Storage Share.
     */
    public String getShareUrl() {
        return client.getShareUrl();
    }

    /**
     * Constructs a {@link DirectoryClient} that interacts with the root directory in the share.
     *
     * <p>If the directory doesn't exist in the share {@link DirectoryClient#create() create} in the client will
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
     * <p>If the directory doesn't exist in the share {@link DirectoryClient#create() create} in the client will
     * need to be called before interaction with the directory can happen.</p>
     *
     * @param directoryName Name of the directory
     * @return a {@link DirectoryClient} that interacts with the directory in the share
     */
    public DirectoryClient getDirectoryClient(String directoryName) {
        return new DirectoryClient(client.getDirectoryClient(directoryName));
    }

    /**
     * Constructs a {@link FileClient} that interacts with the specified file.
     *
     * <p>If the file doesn't exist in the share {@link FileClient#create(long)} ) create} in the client will
     * need to be called before interaction with the file can happen.</p>
     *
     * @param filePath Name of the file
     * @return a {@link FileClient} that interacts with the file in the share
     */
    public FileClient getFileClient(String filePath) {
        return new FileClient(client.getFileClient(filePath));
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
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @return The {@link ShareInfo information about the share}.
     * @throws StorageException If the share already exists with different metadata
     */
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
     * {@codesnippet com.azure.storage.file.ShareClient.createWithResponse#map-integer-duration-context.metadata}
     *
     * <p>Create the share with a quota of 10 GB</p>
     *
     * {@codesnippet com.azure.storage.file.ShareClient.createWithResponse#map-integer-duration-context.quota}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to associate with the share
     * @param quotaInGB Optional maximum size the share is allowed to grow to in GB. This must be greater than 0 and
     * less than or equal to 5120. The default value is 5120.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareInfo information about the share} and the status its creation.
     * @throws StorageException If the share already exists with different metadata or {@code quotaInGB} is outside the
     * allowed range.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<ShareInfo> createWithResponse(Map<String, String> metadata, Integer quotaInGB, Duration timeout,
        Context context) {
        Mono<Response<ShareInfo>> response = client.createWithResponse(metadata, quotaInGB, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
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
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/snapshot-share">Azure Docs</a>.</p>
     *
     * @return The {@link ShareSnapshotInfo information about snapshot of share}
     * @throws StorageException If the share doesn't exist, there are 200 snapshots of the share, or a snapshot is in
     * progress for the share
     */
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
     * {@codesnippet com.azure.storage.file.shareClient.createSnapshotWithResponse#map-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/snapshot-share">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to associate with the snapshot. If {@code null} the metadata of the share will
     * be copied to the snapshot.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareSnapshotInfo information about snapshot of the share} and status of
     * creation.
     * @throws StorageException If the share doesn't exist, there are 200 snapshots of the share, or a snapshot is in
     * progress for the share
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<ShareSnapshotInfo> createSnapshotWithResponse(Map<String, String> metadata, Duration timeout,
        Context context) {
        Mono<Response<ShareSnapshotInfo>> response = client.createSnapshotWithResponse(metadata, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
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
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @throws StorageException If the share doesn't exist
     */
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
     * {@codesnippet com.azure.storage.file.shareClient.deleteWithResponse#duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws StorageException If the share doesn't exist
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<Void> deleteWithResponse(Duration timeout, Context context) {
        Mono<Response<Void>> response = client.deleteWithResponse(context);
        return Utility.blockWithOptionalTimeout(response, timeout);
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
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @return The {@link ShareProperties properties of the share}
     * @throws StorageException If the share doesn't exist
     */
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
     * {@codesnippet com.azure.storage.file.shareClient.getPropertiesWithResponse#duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing {@link ShareProperties properties of the share} with response status code
     * @throws StorageException If the share doesn't exist
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<ShareProperties> getPropertiesWithResponse(Duration timeout, Context context) {
        Mono<Response<ShareProperties>> response = client.getPropertiesWithResponse(context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Sets the maximum size in GB that the share is allowed to grow.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the quota to 1024 GB</p>
     *
     * {@codesnippet com.azure.storage.file.ShareClient.setQuota#int}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @param quotaInGB Size in GB to limit the share's growth. The quota in GB must be between 1 and 5120.
     * @return The {@link ShareProperties properties of the share}
     * @throws StorageException If the share doesn't exist or {@code quotaInGB} is outside the allowed bounds
     */
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
     * {@codesnippet com.azure.storage.file.shareClient.setQuotaWithResponse#int-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-share-properties">Azure Docs</a>.</p>
     *
     * @param quotaInGB Size in GB to limit the share's growth. The quota in GB must be between 1 and 5120.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing {@link ShareProperties properties of the share} with response status code
     * @throws StorageException If the share doesn't exist or {@code quotaInGB} is outside the allowed bounds
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<ShareInfo> setQuotaWithResponse(int quotaInGB, Duration timeout, Context context) {
        Mono<Response<ShareInfo>> response = client.setQuotaWithResponse(quotaInGB, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
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
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-share-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to set on the share, if null is passed the metadata for the share is cleared
     * @return The {@link ShareProperties properties of the share}
     * @throws StorageException If the share doesn't exist or the metadata contains invalid keys
     */
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
     * {@codesnippet com.azure.storage.file.shareClient.setMetadataWithResponse#map-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-share-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to set on the share, if null is passed the metadata for the share is cleared
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing {@link ShareProperties properties of the share} with response status code
     * @throws StorageException If the share doesn't exist or the metadata contains invalid keys
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<ShareInfo> setMetadataWithResponse(Map<String, String> metadata, Duration timeout,
        Context context) {
        Mono<Response<ShareInfo>> response = client.setMetadataWithResponse(metadata, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
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
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-share-acl">Azure Docs</a>.</p>
     *
     * @return The stored access policies specified on the queue.
     * @throws StorageException If the share doesn't exist
     */
    public PagedIterable<SignedIdentifier> getAccessPolicy() {
        return new PagedIterable<>(client.getAccessPolicy());
    }

    /**
     * Sets stored access policies for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * {@codesnippet com.azure.storage.file.ShareClient.setAccessPolicy#List}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-share-acl">Azure Docs</a>.</p>
     *
     * @param permissions Access policies to set on the queue
     * @return The {@link ShareInfo information of the share}
     * @throws StorageException If the share doesn't exist, a stored access policy doesn't have all fields filled out,
     * or the share will have more than five policies.
     */
    public ShareInfo setAccessPolicy(List<SignedIdentifier> permissions) {
        return setAccessPolicyWithResponse(permissions, null, Context.NONE).getValue();
    }

    /**
     * Sets stored access policies for the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.setAccessPolicyWithResponse#list-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-share-acl">Azure Docs</a>.</p>
     *
     * @param permissions Access policies to set on the queue
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareInfo information of the share} with headers and response status
     * code
     * @throws StorageException If the share doesn't exist, a stored access policy doesn't have all fields filled out,
     * or the share will have more than five policies.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<ShareInfo> setAccessPolicyWithResponse(List<SignedIdentifier> permissions, Duration timeout,
        Context context) {
        Mono<Response<ShareInfo>> response = client.setAccessPolicyWithResponse(permissions, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
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
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-share-stats">Azure Docs</a>.</p>
     *
     * @return The storage {@link ShareStatistics statistics of the share}
     */
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
     * {@codesnippet com.azure.storage.file.shareClient.getStatisticsWithResponse#duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-share-stats">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareStatistics statistics of the share}
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<ShareStatistics> getStatisticsWithResponse(Duration timeout, Context context) {
        Mono<Response<ShareStatistics>> response = client.getStatisticsWithResponse(context);
        return Utility.blockWithOptionalTimeout(response, timeout);
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
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @return A response containing a {@link DirectoryClient} to interact with the created directory.
     * @throws StorageException If the share doesn't exist, the directory already exists or is in the process of being
     * deleted, or the parent directory for the new directory doesn't exist
     */
    public DirectoryClient createDirectory(String directoryName) {
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
     * {@codesnippet com.azure.storage.file.shareClient.createDirectoryWithResponse#string-filesmbproperties-string-map-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param metadata Optional metadata to associate with the directory
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @return A response containing a {@link DirectoryAsyncClient} to interact with the created directory and the
     * status of its creation.
     * @throws StorageException If the share doesn't exist, the directory already exists or is in the process of being
     * deleted, the parent directory for the new directory doesn't exist, or the metadata is using an illegal key name
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<DirectoryClient> createDirectoryWithResponse(String directoryName, FileSmbProperties smbProperties,
        String filePermission, Map<String, String> metadata, Duration timeout, Context context) {
        DirectoryClient directoryClient = getDirectoryClient(directoryName);
        return new SimpleResponse<>(directoryClient.createWithResponse(smbProperties, filePermission, metadata, timeout,
            context), directoryClient);
    }

    /**
     * Creates the file in the share with the given name and file max size.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file "myfile" with size of 1024 bytes.</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.createFile#string-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file.
     * @param maxSize The maximum size in bytes for the file, up to 1 TiB.
     * @return A response containing a {@link FileClient} to interact with the created file.
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
    public FileClient createFile(String fileName, long maxSize) {
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
     * {@codesnippet com.azure.storage.file.shareClient.createFileWithResponse#string-long-filehttpheaders-filesmbproperties-string-map-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file.
     * @param maxSize The maximum size in bytes for the file, up to 1 TiB.
     * @param httpHeaders Additional parameters for the operation.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file
     * @param metadata Optional name-value pairs associated with the file as metadata.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing a {@link FileClient} to interact with the created file and the status of its
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
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<FileClient> createFileWithResponse(String fileName, long maxSize, FileHTTPHeaders httpHeaders,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata, Duration timeout,
        Context context) {
        FileClient fileClient = getFileClient(fileName);
        return new SimpleResponse<>(fileClient.createWithResponse(maxSize, httpHeaders, smbProperties,
            filePermission, metadata, timeout, context), fileClient);
    }

    /**
     * Deletes the specified directory in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory "mydirectory"</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.deleteDirectory#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @throws StorageException If the share doesn't exist or the directory isn't empty
     */
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
     * {@codesnippet com.azure.storage.file.shareClient.deleteDirectoryWithResponse#string-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param directoryName Name of the directory
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws StorageException If the share doesn't exist or the directory isn't empty
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<Void> deleteDirectoryWithResponse(String directoryName, Duration timeout, Context context) {
        Mono<Response<Void>> response = client.deleteDirectoryWithResponse(directoryName, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Deletes the specified file in the share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "myfile"</p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.deleteFile#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @throws StorageException If the share or the file doesn't exist.
     */
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
     * {@codesnippet com.azure.storage.file.shareClient.deleteFileWithResponse#string-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws StorageException If the share or the file doesn't exist.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<Void> deleteFileWithResponse(String fileName, Duration timeout, Context context) {
        Mono<Response<Void>> response = client.deleteFileWithResponse(fileName, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Creates a permission at the share level. If a permission already exists, it returns the key of it, else creates a
     * new permission and returns the key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.createPermission#string}
     *
     * @param filePermission The file permission to get/create.
     * @return The file permission key associated with the file permission.
     */
    public String createPermission(String filePermission) {
        return createPermissionWithResponse(filePermission, Context.NONE).getValue();
    }

    /**
     * Creates a permission t the share level. If a permission already exists, it returns the key of it, else creates a
     * new permission and returns the key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.createPermissionWithResponse#string-context}
     *
     * @param filePermission The file permission to get/create.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that contains the file permission key associated with the file permission.
     */
    public Response<String> createPermissionWithResponse(String filePermission, Context context) {
        return client.createPermissionWithResponse(filePermission, context).block();
    }

    /**
     * Gets a permission for a given key
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.getPermission#string}
     *
     * @param filePermissionKey The file permission key.
     * @return The file permission associated with the file permission key.
     */
    public String getPermission(String filePermissionKey) {
        return getPermissionWithResponse(filePermissionKey, Context.NONE).getValue();
    }

    /**
     * Gets a permission for a given key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.getPermissionWithResponse#string-context}
     *
     * @param filePermissionKey The file permission key.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that contains th file permission associated with the file permission key.
     */
    public Response<String> getPermissionWithResponse(String filePermissionKey, Context context) {
        return client.getPermissionWithResponse(filePermissionKey, context).block();
    }

    /**
     * Get snapshot id which attached to {@link ShareClient}. Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.getSnapshotId}
     *
     * @return The snapshot id which is a unique {@code DateTime} value that identifies the share snapshot to its base
     * share.
     */
    public String getSnapshotId() {
        return client.getSnapshotId();
    }

    /**
     * Get share name from share client.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.shareClient.getShareName}
     *
     * @return The name of the share.
     */
    public String getShareName() {
        return this.client.getShareName();
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.client.getAccountName();
    }
}
