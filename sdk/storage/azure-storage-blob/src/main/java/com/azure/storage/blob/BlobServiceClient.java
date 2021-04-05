// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobServiceProperties;
import com.azure.storage.blob.models.BlobServiceStatistics;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.TaggedBlobItem;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.options.FindBlobsOptions;
import com.azure.storage.blob.options.UndeleteBlobContainerOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Client to a storage account. It may only be instantiated through a {@link BlobServiceClientBuilder}. This class does
 * not hold any state about a particular storage account but is instead a convenient way of sending off appropriate
 * requests to the resource on the service. It may also be used to construct URLs to blobs and containers.
 *
 * <p>
 * This client contains operations on a blob. Operations on a container are available on {@link BlobContainerClient}
 * through {@link #getBlobContainerClient(String)}, and operations on a blob are available on {@link BlobClient}.
 *
 * <p>
 * Please see <a href=https://docs.microsoft.com/azure/storage/blobs/storage-blobs-introduction>here</a> for more
 * information on containers.
 */
@ServiceClient(builder = BlobServiceClientBuilder.class)
public final class BlobServiceClient {
    private final BlobServiceAsyncClient blobServiceAsyncClient;

    /**
     * Package-private constructor for use by {@link BlobServiceClientBuilder}.
     *
     * @param blobServiceAsyncClient the async storage account client
     */
    BlobServiceClient(BlobServiceAsyncClient blobServiceAsyncClient) {
        this.blobServiceAsyncClient = blobServiceAsyncClient;
    }

    /**
     * Initializes a {@link BlobContainerClient} object pointing to the specified container. This method does not create
     * a container. It simply constructs the URL to the container and offers access to methods relevant to containers.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.getBlobContainerClient#String}
     *
     * @param containerName The name of the container to point to.
     * @return A {@link BlobContainerClient} object pointing to the specified container
     */
    public BlobContainerClient getBlobContainerClient(String containerName) {
        return new BlobContainerClient(blobServiceAsyncClient.getBlobContainerAsyncClient(containerName));
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return blobServiceAsyncClient.getHttpPipeline();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public BlobServiceVersion getServiceVersion() {
        return this.blobServiceAsyncClient.getServiceVersion();
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.createBlobContainer#String}
     *
     * @param containerName Name of the container to create
     * @return The {@link BlobContainerClient} used to interact with the container created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobContainerClient createBlobContainer(String containerName) {
        return createBlobContainerWithResponse(containerName, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.createBlobContainerWithResponse#String-Map-PublicAccessType-Context}
     *
     * @param containerName Name of the container to create
     * @param metadata Metadata to associate with the container. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link BlobContainerClient} used
     * to interact with the container created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobContainerClient> createBlobContainerWithResponse(String containerName,
        Map<String, String> metadata, PublicAccessType accessType, Context context) {
        BlobContainerClient client = getBlobContainerClient(containerName);
        return new SimpleResponse<>(client.createWithResponse(metadata, accessType, null, context), client);
    }

    /**
     * Deletes the specified container in the storage account. If the container doesn't exist the operation fails. For
     * more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.deleteBlobContainer#String}
     *
     * @param containerName Name of the container to delete
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteBlobContainer(String containerName) {
        deleteBlobContainerWithResponse(containerName, Context.NONE);
    }

    /**
     * Deletes the specified container in the storage account. If the container doesn't exist the operation fails. For
     * more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * @param containerName Name of the container to delete
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteBlobContainerWithResponse(String containerName, Context context) {
        return blobServiceAsyncClient.deleteBlobContainerWithResponse(containerName, context).block();
    }

    /**
     * Gets the URL of the storage account represented by this client.
     *
     * @return the URL.
     */
    public String getAccountUrl() {
        return blobServiceAsyncClient.getAccountUrl();
    }

    /**
     * Returns a lazy loaded list of containers in this account. The returned {@link PagedIterable} can be consumed
     * while new items are automatically retrieved as needed. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.listBlobContainers}
     *
     * @return The list of containers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BlobContainerItem> listBlobContainers() {
        return this.listBlobContainers(new ListBlobContainersOptions(), null);
    }

    /**
     * Returns a lazy loaded list of containers in this account. The returned {@link PagedIterable} can be consumed
     * while new items are automatically retrieved as needed. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.listBlobContainers#ListBlobContainersOptions-Duration}
     *
     * @param options A {@link ListBlobContainersOptions} which specifies what data should be returned by the service.
     * If iterating by page, the page size passed to byPage methods such as
     * {@link PagedIterable#iterableByPage(int)} will be preferred over the value set on these options.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The list of containers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BlobContainerItem> listBlobContainers(ListBlobContainersOptions options, Duration timeout) {
        return new PagedIterable<>(blobServiceAsyncClient.listBlobContainersWithOptionalTimeout(options, timeout));
    }

    /**
     * Returns a lazy loaded list of blobs in this account whose tags match the query expression. The returned
     * {@link PagedIterable} can be consumed while new items are automatically retrieved as needed. For more
     * information, including information on the query syntax, see the <a href="https://docs.microsoft.com/rest/api/storageservices/find-blobs-by-tags">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.findBlobsByTag#String}
     *
     * @param query Filters the results to return only blobs whose tags match the specified expression.
     * @return The list of blobs.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TaggedBlobItem> findBlobsByTags(String query) {
        return this.findBlobsByTags(new FindBlobsOptions(query), null, Context.NONE);
    }

    /**
     * Returns a lazy loaded list of blobs in this account whose tags match the query expression. The returned
     * {@link PagedIterable} can be consumed while new items are automatically retrieved as needed. For more
     * information, including information on the query syntax, see the <a href="https://docs.microsoft.com/rest/api/storageservices/find-blobs-by-tags">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.findBlobsByTag#FindBlobsOptions-Duration}
     *
     * @param options {@link FindBlobsOptions}. If iterating by page, the page size passed to byPage methods such as
     * {@link PagedIterable#iterableByPage(int)} will be preferred over the value set on these options.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The list of blobs.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TaggedBlobItem> findBlobsByTags(FindBlobsOptions options, Duration timeout, Context context) {
        return new PagedIterable<>(blobServiceAsyncClient.findBlobsByTags(options, timeout, context));
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.getProperties}
     *
     * @return The storage account properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobServiceProperties getProperties() {
        return getPropertiesWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.getPropertiesWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the storage account properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobServiceProperties> getPropertiesWithResponse(Duration timeout, Context context) {

        Mono<Response<BlobServiceProperties>> response = blobServiceAsyncClient.getPropertiesWithResponse(context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     * <p>This method checks to ensure the properties being sent follow the specifications indicated in the Azure Docs.
     * If CORS policies are set, CORS parameters that are not set default to the empty string.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.setProperties#BlobServiceProperties}
     *
     * @param properties Configures the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setProperties(BlobServiceProperties properties) {
        setPropertiesWithResponse(properties, null, Context.NONE);
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     * <p>This method checks to ensure the properties being sent follow the specifications indicated in the Azure Docs.
     * If CORS policies are set, CORS parameters that are not set default to the empty string.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.setPropertiesWithResponse#BlobServiceProperties-Duration-Context}
     *
     * @param properties Configures the service.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The storage account properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setPropertiesWithResponse(BlobServiceProperties properties, Duration timeout,
        Context context) {
        Mono<Response<Void>> response = blobServiceAsyncClient.setPropertiesWithResponse(properties, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Gets a user delegation key for use with this account's blob storage. Note: This method call is only valid when
     * using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime}
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @return The user delegation key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UserDelegationKey getUserDelegationKey(OffsetDateTime start, OffsetDateTime expiry) {
        return getUserDelegationKeyWithResponse(start, expiry, null, Context.NONE).getValue();
    }

    /**
     * Gets a user delegation key for use with this account's blob storage. Note: This method call is only valid when
     * using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime-Duration-Context}
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the user delegation key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<UserDelegationKey> getUserDelegationKeyWithResponse(OffsetDateTime start, OffsetDateTime expiry,
        Duration timeout, Context context) {
        Mono<Response<UserDelegationKey>> response = blobServiceAsyncClient.getUserDelegationKeyWithResponse(start,
            expiry, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the storage account. For more information, see
     * the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.getStatistics}
     *
     * @return The storage account statistics.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobServiceStatistics getStatistics() {
        return getStatisticsWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the storage account. For more information, see
     * the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.getStatisticsWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} the storage account statistics.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobServiceStatistics> getStatisticsWithResponse(Duration timeout, Context context) {
        Mono<Response<BlobServiceStatistics>> response = blobServiceAsyncClient.getStatisticsWithResponse(context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.getAccountInfo}
     *
     * @return The storage account info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public StorageAccountInfo getAccountInfo() {
        return getAccountInfoWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the storage account info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<StorageAccountInfo> getAccountInfoWithResponse(Duration timeout, Context context) {
        Mono<Response<StorageAccountInfo>> response = blobServiceAsyncClient.getAccountInfoWithResponse(context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.blobServiceAsyncClient.getAccountName();
    }

    /**
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * <p><strong>Generating an account SAS</strong></p>
     * <p>The snippet below generates an AccountSasSignatureValues object that lasts for two days and gives the user
     * read and list access to blob  and file shares.</p>
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.generateAccountSas#AccountSasSignatureValues}
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues) {
        return this.blobServiceAsyncClient.generateAccountSas(accountSasSignatureValues);
    }

    /* TODO(gapra): REST Docs*/
    /**
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * <p><strong>Generating an account SAS</strong></p>
     * <p>The snippet below generates an AccountSasSignatureValues object that lasts for two days and gives the user
     * read and list access to blob  and file shares.</p>
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.generateAccountSas#AccountSasSignatureValues-Context}
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues, Context context) {
        return this.blobServiceAsyncClient.generateAccountSas(accountSasSignatureValues, context);
    }

    /**
     * Restores a previously deleted container.
     * If the container associated with provided <code>deletedContainerName</code>
     * already exists, this call will result in a 409 (conflict).
     * This API is only functional if Container Soft Delete is enabled
     * for the storage account associated with the container.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.undeleteBlobContainer#String-String}
     *
     * @param deletedContainerName The name of the previously deleted container.
     * @param deletedContainerVersion The version of the previously deleted container.
     * @return The {@link BlobContainerClient} used to interact with the restored container.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobContainerClient undeleteBlobContainer(String deletedContainerName, String deletedContainerVersion) {
        return this.undeleteBlobContainerWithResponse(
            new UndeleteBlobContainerOptions(deletedContainerName, deletedContainerVersion), null,
            Context.NONE).getValue();
    }

    /**
     * Restores a previously deleted container. The restored container
     * will be renamed to the <code>destinationContainerName</code> if provided in <code>options</code>.
     * Otherwise <code>deletedContainerName</code> is used as destination container name.
     * If the container associated with provided <code>destinationContainerName</code>
     * already exists, this call will result in a 409 (conflict).
     * This API is only functional if Container Soft Delete is enabled
     * for the storage account associated with the container.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.undeleteBlobContainerWithResponse#UndeleteBlobContainerOptions-Duration-Context}
     *
     * @param options {@link UndeleteBlobContainerOptions}.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link BlobContainerClient} used
     * to interact with the restored container.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobContainerClient> undeleteBlobContainerWithResponse(
        UndeleteBlobContainerOptions options, Duration timeout, Context context) {
        Mono<Response<BlobContainerClient>> response =
            this.blobServiceAsyncClient.undeleteBlobContainerWithResponse(options, context)
            .map(r -> new SimpleResponse<>(r, getBlobContainerClient(r.getValue().getBlobContainerName())));

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

//    /**
//     * Renames an existing blob container.
//     *
//     * <p><strong>Code Samples</strong></p>
//     *
//     * {@codesnippet com.azure.storage.blob.BlobServiceClient.renameBlobContainer#String-String}
//     *
//     * @param sourceContainerName The current name of the container.
//     * @param destinationContainerName The new name of the container.
//     * @return A {@link BlobContainerClient} used to interact with the renamed container.
//     */
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    BlobContainerClient renameBlobContainer(String sourceContainerName, String destinationContainerName) {
//        return renameBlobContainerWithResponse(sourceContainerName, new BlobContainerRenameOptions(destinationContainerName
//        ), null, Context.NONE).getValue();
//    }
//
//    /**
//     * Renames an existing blob container.
//     *
//     * <p><strong>Code Samples</strong></p>
//     *
//     * {@codesnippet com.azure.storage.blob.BlobServiceClient.renameBlobContainerWithResponse#String-BlobContainerRenameOptions-Duration-Context}
//     *
//     * @param options {@link BlobContainerRenameOptions}
//     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
//     * @param context Additional context that is passed through the Http pipeline during the service call.
//     * @return A {@link Response} whose {@link Response#getValue() value} contains a
//     * {@link BlobContainerClient} used to interact with the renamed container.
//     */
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    Response<BlobContainerClient> renameBlobContainerWithResponse(String sourceContainerName,
//        BlobContainerRenameOptions options, Duration timeout, Context context) {
//        Mono<Response<BlobContainerClient>> response =
//            this.blobServiceAsyncClient.renameBlobContainerWithResponse(sourceContainerName, options, context)
//                .map(r -> new SimpleResponse<>(r, getBlobContainerClient(r.getValue().getBlobContainerName())));
//
//        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
//    }
}
