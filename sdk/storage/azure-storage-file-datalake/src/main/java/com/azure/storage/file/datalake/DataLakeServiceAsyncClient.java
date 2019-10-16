// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.common.Utility;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientBuilder;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientImpl;
import com.azure.storage.file.datalake.models.ListFileSystemsOptions;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import reactor.core.publisher.Mono;
import com.azure.core.credentials.TokenCredential;

import java.time.OffsetDateTime;
import java.util.Map;


/**
 * Client to a storage account. It may only be instantiated through a {@link DataLakeServiceClientBuilder}. This class
 * does not hold any state about a particular storage account but is instead a convenient way of sending off appropriate
 * requests to the resource on the service. It may also be used to construct URLs to file systems, files and
 * directories.
 *
 * <p>
 * This client contains operations on the main data lake service account. Operations on a file system are available on
 * {@link FileSystemAsyncClient} through {@link #getFileSystemAsyncClient(String)}, and operations on a file or
 * directory are available on {@link FileAsyncClient} or {@link DirectoryAsyncClient}.
 *
 * <p>
 * Note this client is an async client that returns reactive responses from Spring Reactor Core project
 * (https://projectreactor.io/). Calling the methods in this client will <strong>NOT</strong> start the actual network
 * operation, until {@code .subscribe()} is called on the reactive response. You can simply convert one of these
 * responses to a {@link java.util.concurrent.CompletableFuture} object through {@link Mono#toFuture()}.
 */
@ServiceClient(builder = DataLakeServiceClientBuilder.class, isAsync = true)
public class DataLakeServiceAsyncClient {

    private DataLakeStorageClientImpl dataLakeImpl;
    private final String accountName;
    BlobServiceAsyncClient blobServiceAsyncClient;

    /**
     * Package-private constructor for use by {@link DataLakeServiceClientBuilder}.
     */
    DataLakeServiceAsyncClient(BlobServiceAsyncClient blobServiceAsyncClient, DataLakeStorageClientImpl dataLakeImpl,
        String accountName) {
        this.blobServiceAsyncClient = blobServiceAsyncClient;
        this.dataLakeImpl = dataLakeImpl;
        this.accountName = accountName;
    }

    /**
     * Initializes a {@link FileSystemAsyncClient} object pointing to the specified file system. This method does not
     * create a file system. It simply constructs the URL to the file system and offers access to methods relevant to
     * file systems.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceAsyncClient.getFileSystemAsyncClient#String}
     *
     * @param fileSystemName The name of the file system to point to. A value of null or empty string will be
     *                       interpreted as pointing to the root file system and will be replaced by "$root".
     * @return A {@link FileSystemAsyncClient} object pointing to the specified file system
     */
    public FileSystemAsyncClient getFileSystemAsyncClient(String fileSystemName) {
        if (ImplUtils.isNullOrEmpty(fileSystemName)) {
            fileSystemName = FileSystemAsyncClient.ROOT_FILESYSTEM_NAME;
        }

        return new FileSystemAsyncClient(blobServiceAsyncClient.getBlobContainerAsyncClient(fileSystemName),
            new DataLakeStorageClientBuilder()
                .url(Utility.appendToURLPath(getAccountUrl(), fileSystemName).toString())
                .pipeline(dataLakeImpl.getHttpPipeline())
                .build(), accountName, fileSystemName);
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return dataLakeImpl.getHttpPipeline();
    }

    /**
     * Creates a new file system within a storage account. If a file system with the same name already exists, the
     * operation fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceAsyncClient.createFileSystem#String}
     *
     * @param fileSystemName Name of the file system to create
     * @return A {@link Mono} containing a {@link FileSystemAsyncClient} used to interact with the file system created.
     */
    public Mono<FileSystemAsyncClient> createFileSystem(String fileSystemName) {
        return createFileSystemWithResponse(fileSystemName, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new file system within a storage account. If a file system with the same name already exists, the
     * operation fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceAsyncClient.createFileSystemWithResponse#String-Map-PublicAccessType}
     *
     * @param fileSystemName Name of the file system to create
     * @param metadata Metadata to associate with the file system
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * FileSystemAsyncClient} used to interact with the file system created.
     */
    public Mono<Response<FileSystemAsyncClient>> createFileSystemWithResponse(String fileSystemName,
        Map<String, String> metadata, PublicAccessType accessType) {
        FileSystemAsyncClient fileSystemAsyncClient = getFileSystemAsyncClient(fileSystemName);
        return fileSystemAsyncClient.createWithResponse(metadata, accessType).
            map(response -> new SimpleResponse<>(response, fileSystemAsyncClient));
    }

    /**
     * Deletes the specified file system in the storage account. If the file system doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceAsyncClient.deleteFileSystem#String}
     *
     * @param fileSystemName Name of the file system to delete
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    public Mono<Void> deleteFileSystem(String fileSystemName) {
        return deleteFileSystemWithResponse(fileSystemName).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified file system in the storage account. If the file system doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceAsyncClient.deleteFileSystemWithResponse#String-Context}
     *
     * @param fileSystemName Name of the file system to delete
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    public Mono<Response<Void>> deleteFileSystemWithResponse(String fileSystemName) {
        return getFileSystemAsyncClient(fileSystemName).deleteWithResponse(null);
    }

    // TODO (gapra) : Make this method return FileSystemItem
    /**
     * Returns a reactive Publisher emitting all the file systems in this account lazily as needed. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceAsyncClient.listFileSystems}
     *
     * @return A reactive response emitting the list of file systems.
     */
    public PagedFlux<BlobContainerItem> listFileSystems() {
        return this.listFileSystems(new ListFileSystemsOptions());
    }

    /**
     * Returns a reactive Publisher emitting all the file systems in this account lazily as needed. For more information,
     * see the <a href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.listBlobContainers#ListBlobContainersOptions}
     *
     * @param options A {@link ListFileSystemsOptions} which specifies what data should be returned by the service.
     * @return A reactive response emitting the list of containers.
     */
    public PagedFlux<BlobContainerItem> listFileSystems(ListFileSystemsOptions options) {
        return blobServiceAsyncClient.listBlobContainers(Transforms.toListBlobContainersOptions(options));
    }

    /**
     * Gets a user delegation key for use with this account's data lake storage. Note: This method call is only valid
     * when using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceAsyncClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime}
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @return A {@link Mono} containing the user delegation key.
     * @throws IllegalArgumentException If {@code start} isn't null and is after {@code expiry}.
     * @throws NullPointerException If {@code expiry} is null.
     */
    public Mono<UserDelegationKey> getUserDelegationKey(OffsetDateTime start, OffsetDateTime expiry) {
        return blobServiceAsyncClient.getUserDelegationKey(start, expiry).map(Transforms::toDataLakeUserDelegationKey);
    }

    /**
     * Gets a user delegation key for use with this account's data lake storage. Note: This method call is only valid
     * when using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceAsyncClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime}
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} containing the user
     * delegation key.
     * @throws IllegalArgumentException If {@code start} isn't null and is after {@code expiry}.
     * @throws NullPointerException If {@code expiry} is null.
     */
    public Mono<Response<UserDelegationKey>> getUserDelegationKeyWithResponse(OffsetDateTime start,
        OffsetDateTime expiry) {
        return blobServiceAsyncClient.getUserDelegationKeyWithResponse(start, expiry).map(response ->
            new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                Transforms.toDataLakeUserDelegationKey(response.getValue())));
    }

    /**
     * Gets the URL of the storage account represented by this client.
     *
     * @return the URL.
     */
    public String getAccountUrl() {
        return dataLakeImpl.getUrl();
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.accountName;
    }

}
