// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.common.Utility;
import com.azure.storage.file.datalake.implementation.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.implementation.models.Path;
import com.azure.storage.file.datalake.models.FileSystemAccessConditions;
import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.GetPathsOptions;
import com.azure.storage.file.datalake.models.PublicAccessType;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Client to a file system. It may only be instantiated through a {@link FileSystemClientBuilder} or via the method
 * {@link DataLakeServiceClient#getFileSystemClient(String)}. This class does not hold any state about a particular
 * file system but is instead a convenient way of sending off appropriate requests to the resource on the service. It
 * may also be used to construct URLs to files/directories.
 *
 * <p>
 * This client contains operations on a file system. Operations on a path are available on {@link FileClient} and
 * {@link DirectoryClient} through {@link #getFileClient(String)} and {@link #getDirectoryClient(String)} respectively,
 * and operations on the service are available on {@link DataLakeServiceClient}.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/data-lake-storage-introduction?toc=%2fazure%2fstorage%2fblobs%2ftoc.json>
 *     Azure Docs</a> for more information on file systems.
 */
@ServiceClient(builder = FileSystemClientBuilder.class)
public class FileSystemClient {
    private final FileSystemAsyncClient fileSystemAsyncClient;

    /**
     * Package-private constructor for use by {@link FileSystemClientBuilder}.
     */
    FileSystemClient(FileSystemAsyncClient fileSystemAsyncClient) {
        this.fileSystemAsyncClient = fileSystemAsyncClient;
    }

    /**
     * Initializes a new FileClient object by concatenating fileName to the end of FileSystemClient's URL. The new
     * FileClient uses the same request policy pipeline as the FileSystemClient.
     *
     * @param fileName A {@code String} representing the name of the file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemClient.getFileClient#String}
     *
     * @return A new {@link FileClient} object which references the file with the specified name in this file system.
     */
    public FileClient getFileClient(String fileName) {
        return null;
//        return new FileClient(fileSystemAsyncClient.getFileAsyncClient(fileName));
    }

    /**
     * Initializes a new DirectoryClient object by concatenating directoryName to the end of FileSystemClient's URL.
     * The new DirectoryClient uses the same request policy pipeline as the FileSystemClient.
     *
     * @param directoryName A {@code String} representing the name of the directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemClient.getDirectoryClient#String}
     *
     * @return A new {@link DirectoryClient} object which references the directory with the specified name in this
     * file system.
     */
    public DirectoryClient getDirectoryClient(String directoryName) {
        return null;
//        return new DirectoryClient(fileSystemAsyncClient.getDirectoryAsyncClient(directoryName));
    }

    /**
     * Get the file system name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemClient.getFileSystemName}
     *
     * @return The name of file system.
     */
    public String getFileSystemName() {
        return fileSystemAsyncClient.getFileSystemName();
    }

    /**
     * Gets the URL of the file system represented by this client.
     *
     * @return the URL.
     */
    public String getFileSystemUrl() {
        return fileSystemAsyncClient.getFileSystemUrl();
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return fileSystemAsyncClient.getAccountName();
    }


    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return fileSystemAsyncClient.getHttpPipeline();
    }


    /**
     * Creates a new file system within a storage account. If a file system with the same name already exists, the
     * operation fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemClient.create}
     */
    public void create() {
        createWithResponse(null, null, null, Context.NONE);
    }

    /**
     * Creates a new file system within a storage account. If a file system with the same name already exists, the
     * operation fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemClient.createWithResponse#Map-PublicAccessType-Duration-Context}
     *
     * @param metadata Metadata to associate with the file system.
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> createWithResponse(Map<String, String> metadata, PublicAccessType accessType,
        Duration timeout, Context context) {
        Mono<Response<Void>> response = null;
            //fileSystemAsyncClient.createWithResponse(metadata, accessType, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Marks the specified file system for deletion. The file system and any files/directories contained within it are
     * later deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemClient.delete}
     */
    public void delete() {
        deleteWithResponse(null, null, Context.NONE);
    }

    /**
     * Marks the specified file system for deletion. The file system and any files/directories contained within it are
     * later deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemClient.deleteWithResponse#FileSystemAccessConditions-Duration-Context}
     *
     * @param accessConditions {@link FileSystemAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> deleteWithResponse(FileSystemAccessConditions accessConditions, Duration timeout,
        Context context) {
        Mono<Response<Void>> response = null;
//            fileSystemAsyncClient.deleteWithResponse(accessConditions, context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the file system's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemClient.getProperties}
     *
     * @return The file system properties.
     */
    public FileSystemProperties getProperties() {
        return getPropertiesWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Returns the file system's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemClient.getPropertiesWithResponse#LeaseAccessConditions-Duration-Context}
     *
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the file/directory.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The file system properties.
     */
    public Response<FileSystemProperties> getPropertiesWithResponse(LeaseAccessConditions leaseAccessConditions,
        Duration timeout, Context context) {
        Mono<Response<FileSystemProperties>> response = null;
//            fileSystemAsyncClient
//            .getPropertiesWithResponse(leaseAccessConditions, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Sets the file system's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemClient.setMetadata#Map}
     *
     * @param metadata Metadata to associate with the file system.
     */
    public void setMetadata(Map<String, String> metadata) {
        setMetadataWithResponse(metadata, null, null, Context.NONE);
    }

    /**
     * Sets the file system's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemClient.setMetadataWithResponse#Map-FileSystemAccessConditions-Duration-Context}
     * @param metadata Metadata to associate with the file system.
     * @param accessConditions {@link FileSystemAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> setMetadataWithResponse(Map<String, String> metadata,
        FileSystemAccessConditions accessConditions, Duration timeout, Context context) {
        Mono<Response<Void>> response = null;
//            fileSystemAsyncClient.setMetadataWithResponse(metadata, accessConditions,
//            context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns a lazy loaded list of files/directories in this account. The returned {@link PagedIterable} can be
     * consumed while new items are automatically retrieved as needed. For more information, see the <a
     * href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/filesystem/list#filesystem">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemClient.getPaths}
     *
     * @return The list of files/directories.
     */
    public PagedIterable<Path> getPaths() {
        return this.getPaths(new GetPathsOptions(), null);
    }

    /**
     * Returns a lazy loaded list of files/directories in this account. The returned {@link PagedIterable} can be
     * consumed while new items are automatically retrieved as needed. For more information, see the <a
     * href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/filesystem/list#filesystem">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileSystemClient.getPaths#ListBlobContainersOptions-Duration}
     *
     * @param options A {@link GetPathsOptions} which specifies what data should be returned by the service.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The list of files/directories.
     */
    public PagedIterable<Path> getPaths(GetPathsOptions options, Duration timeout) {
        return new PagedIterable<>(fileSystemAsyncClient.getPathsWithOptionalTimeout(options, timeout));
    }

}
