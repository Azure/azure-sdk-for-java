// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.FileSystemItem;
import com.azure.storage.file.datalake.models.ListFileSystemsOptions;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.models.UserDelegationKey;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Client to a storage account. It may only be instantiated through a {@link DataLakeServiceClientBuilder}. This class
 * does not hold any state about a particular storage account but is instead a convenient way of sending off appropriate
 * requests to the resource on the service. It may also be used to construct URLs to file systems, files and
 * directories.
 *
 * <p>
 * This client contains operations on a data lake service account. Operations on a file system are available on
 * {@link DataLakeFileSystemClient} through {@link #getFileSystemClient(String)}, and operations on a file or directory
 * are available on {@link DataLakeFileClient} and {@link DataLakeDirectoryClient} respectively.
 */
@ServiceClient(builder = DataLakeServiceClientBuilder.class)
public class DataLakeServiceClient {

    private final DataLakeServiceAsyncClient dataLakeServiceAsyncClient;
    private final BlobServiceClient blobServiceClient;

    /**
     * Package-private constructor for use by {@link DataLakeServiceClientBuilder}.
     *
     * @param dataLakeServiceAsyncClient the async data lake service client.
     * @param blobServiceClient the sync blob service client.
     */
    DataLakeServiceClient(DataLakeServiceAsyncClient dataLakeServiceAsyncClient, BlobServiceClient blobServiceClient) {
        this.dataLakeServiceAsyncClient = dataLakeServiceAsyncClient;
        this.blobServiceClient = blobServiceClient;
    }

    /**
     * Initializes a {@link DataLakeFileSystemClient} object pointing to the specified file system. This method does
     * not create a file system. It simply constructs the URL to the file system and offers access to methods relevant
     * to file systems.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceClient.getFileSystemClient#String}
     *
     * @param fileSystemName The name of the file system to point to.
     * @return A {@link DataLakeFileSystemClient} object pointing to the specified file system
     */
    public DataLakeFileSystemClient getFileSystemClient(String fileSystemName) {
        return new DataLakeFileSystemClient(dataLakeServiceAsyncClient.getFileSystemAsyncClient(fileSystemName),
            blobServiceClient.getBlobContainerClient(fileSystemName));
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return dataLakeServiceAsyncClient.getHttpPipeline();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public DataLakeServiceVersion getServiceVersion() {
        return this.dataLakeServiceAsyncClient.getServiceVersion();
    }

    /**
     * Creates a new file system within a storage account. If a file system with the same name already exists, the
     * operation fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceClient.createFileSystem#String}
     *
     * @param fileSystemName Name of the file system to create
     * @return The {@link DataLakeFileSystemClient} used to interact with the file system created.
     */
    public DataLakeFileSystemClient createFileSystem(String fileSystemName) {
        return createFileSystemWithResponse(fileSystemName, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a new file system within a storage account. If a file system with the same name already exists, the
     * operation fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceClient.createFileSystemWithResponse#String-Map-PublicAccessType-Context}
     *
     * @param fileSystemName Name of the file system to create
     * @param metadata Metadata to associate with the file system.
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DataLakeFileSystemClient}
     * used to interact with the file system created.
     */
    public Response<DataLakeFileSystemClient> createFileSystemWithResponse(String fileSystemName,
        Map<String, String> metadata, PublicAccessType accessType, Context context) {
        DataLakeFileSystemClient client = getFileSystemClient(fileSystemName);

        return new SimpleResponse<>(client.createWithResponse(metadata, accessType, null, context), client);
    }

    /**
     * Deletes the specified file system in the storage account. If the file system doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceClient.deleteFileSystem#String}
     *
     * @param fileSystemName Name of the file system to delete
     */
    public void deleteFileSystem(String fileSystemName) {
        deleteFileSystemWithResponse(fileSystemName, null, Context.NONE).getValue();
    }

    /**
     * Deletes the specified file system in the storage account. If the file system doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceClient.deleteFileSystemWithResponse#String-DataLakeRequestConditions-Context}
     *
     * @param fileSystemName Name of the file system to delete
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> deleteFileSystemWithResponse(String fileSystemName,
        DataLakeRequestConditions requestConditions, Context context) {
        return getFileSystemClient(fileSystemName).deleteWithResponse(requestConditions, null, context);
    }

    /**
     * Gets the URL of the storage account represented by this client.
     *
     * @return the URL.
     */
    public String getAccountUrl() {
        return dataLakeServiceAsyncClient.getAccountUrl();
    }

    // TODO (gapra) : Change return type
    /**
     * Returns a lazy loaded list of file systems in this account. The returned {@link PagedIterable} can be consumed
     * while new items are automatically retrieved as needed. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceClient.listFileSystems}
     *
     * @return The list of file systems.
     */
    public PagedIterable<FileSystemItem> listFileSystems() {
        return this.listFileSystems(new ListFileSystemsOptions(), null);
    }

    /**
     * Returns a lazy loaded list of file systems in this account. The returned {@link PagedIterable} can be consumed
     * while new items are automatically retrieved as needed. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceClient.listFileSystems#ListFileSystemsOptions-Duration}
     *
     * @param options A {@link ListFileSystemsOptions} which specifies what data should be returned by the service.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The list of file systems.
     */
    public PagedIterable<FileSystemItem> listFileSystems(ListFileSystemsOptions options, Duration timeout) {
        return blobServiceClient.listBlobContainers(Transforms.toListBlobContainersOptions(options), timeout)
            .mapPage(Transforms::toFileSystemItem);
    }

    /**
     * Gets a user delegation key for use with this account's data lake storage. Note: This method call is only valid
     * when using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime}
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @return The user delegation key.
     */
    public UserDelegationKey getUserDelegationKey(OffsetDateTime start, OffsetDateTime expiry) {
        return getUserDelegationKeyWithResponse(start, expiry, null, Context.NONE).getValue();
    }

    /**
     * Gets a user delegation key for use with this account's data lake storage. Note: This method call is only valid
     * when using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime-Duration-Context}
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the user delegation key.
     */
    public Response<UserDelegationKey> getUserDelegationKeyWithResponse(OffsetDateTime start, OffsetDateTime expiry,
        Duration timeout, Context context) {
        Response<com.azure.storage.blob.models.UserDelegationKey> response = blobServiceClient
            .getUserDelegationKeyWithResponse(start, expiry, timeout, context);
        return new SimpleResponse<>(response, Transforms.toDataLakeUserDelegationKey(response.getValue()));
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.dataLakeServiceAsyncClient.getAccountName();
    }

    /**
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * <p>The snippet below generates a SAS that lasts for two days and gives the user read and list access to file
     * systems and file shares.</p>
     * {@codesnippet com.azure.storage.file.datalake.DataLakeServiceClient.generateAccountSas#AccountSasSignatureValues}
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     *
     * @return A {@code String} representing all SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues) {
        return dataLakeServiceAsyncClient.generateAccountSas(accountSasSignatureValues);
    }
}
