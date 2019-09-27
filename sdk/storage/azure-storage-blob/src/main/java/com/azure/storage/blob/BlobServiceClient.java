// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.ContainerItem;
import com.azure.storage.blob.models.ListContainersOptions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.StorageServiceProperties;
import com.azure.storage.blob.models.StorageServiceStats;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.common.AccountSASPermission;
import com.azure.storage.common.AccountSASResourceType;
import com.azure.storage.common.AccountSASService;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;
import com.azure.storage.common.Utility;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Client to a storage account. It may only be instantiated through a {@link BlobServiceClientBuilder}. This class does
 * not hold any state about a particular storage account but is instead a convenient way of sending off appropriate
 * requests to the resource on the service. It may also be used to construct URLs to blobs and containers.
 *
 * <p>
 * This client contains operations on a blob. Operations on a container are available on {@link ContainerClient} through
 * {@link #getContainerClient(String)}, and operations on a blob are available on {@link BlobClient}.
 *
 * <p>
 * Please see <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction>here</a> for more
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
     * Initializes a {@link ContainerClient} object pointing to the specified container. This method does not create a
     * container. It simply constructs the URL to the container and offers access to methods relevant to containers.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.getContainerClient#String}
     *
     * @param containerName The name of the container to point to.
     * @return A {@link ContainerClient} object pointing to the specified container
     */
    public ContainerClient getContainerClient(String containerName) {
        return new ContainerClient(blobServiceAsyncClient.getContainerAsyncClient(containerName));
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
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.createContainer#String}
     *
     * @param containerName Name of the container to create
     * @return The {@link ContainerClient} used to interact with the container created.
     */
    public ContainerClient createContainer(String containerName) {
        return createContainerWithResponse(containerName, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.createContainerWithResponse#String-Metadata-PublicAccessType-Context}
     *
     * @param containerName Name of the container to create
     * @param metadata {@link Metadata}
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link ContainerClient} used to
     * interact with the container created.
     */
    public Response<ContainerClient> createContainerWithResponse(String containerName, Metadata metadata,
        PublicAccessType accessType, Context context) {
        ContainerClient client = getContainerClient(containerName);

        return new SimpleResponse<>(client.createWithResponse(metadata, accessType, null, context), client);
    }

    /**
     * Deletes the specified container in the storage account. If the container doesn't exist the operation fails. For
     * more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.deleteContainer#String}
     *
     * @param containerName Name of the container to delete
     */
    public void deleteContainer(String containerName) {
        deleteContainerWithResponse(containerName, Context.NONE);
    }

    /**
     * Deletes the specified container in the storage account. If the container doesn't exist the operation fails. For
     * more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * @param containerName Name of the container to delete
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> deleteContainerWithResponse(String containerName, Context context) {
        return blobServiceAsyncClient.deleteContainerWithResponse(containerName).block();
    }

    /**
     * Gets the URL of the storage account represented by this client.
     *
     * @return the URL.
     */
    public URL getAccountUrl() {
        return blobServiceAsyncClient.getAccountUrl();
    }

    /**
     * Returns a lazy loaded list of containers in this account. The returned {@link PagedIterable} can be consumed
     * while new items are automatically retrieved as needed. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.listContainers}
     *
     * @return The list of containers.
     */
    public PagedIterable<ContainerItem> listContainers() {
        return this.listContainers(new ListContainersOptions(), null);
    }

    /**
     * Returns a lazy loaded list of containers in this account. The returned {@link PagedIterable} can be consumed
     * while new items are automatically retrieved as needed. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.listContainers#ListContainersOptions-Duration}
     *
     * @param options A {@link ListContainersOptions} which specifies what data should be returned by the service.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The list of containers.
     */
    public PagedIterable<ContainerItem> listContainers(ListContainersOptions options, Duration timeout) {
        return new PagedIterable<>(blobServiceAsyncClient.listContainersWithOptionalTimeout(options, timeout));
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.getProperties}
     *
     * @return The storage account properties.
     */
    public StorageServiceProperties getProperties() {
        return getPropertiesWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.getPropertiesWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the storage account properties.
     */
    public Response<StorageServiceProperties> getPropertiesWithResponse(Duration timeout, Context context) {

        Mono<Response<StorageServiceProperties>> response = blobServiceAsyncClient.getPropertiesWithResponse(context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.setProperties#StorageServiceProperties}
     *
     * @param properties Configures the service.
     */
    public void setProperties(StorageServiceProperties properties) {
        setPropertiesWithResponse(properties, null, Context.NONE);
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.setPropertiesWithResponse#StorageServiceProperties-Duration-Context}
     *
     * @param properties Configures the service.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The storage account properties.
     */
    public Response<Void> setPropertiesWithResponse(StorageServiceProperties properties, Duration timeout,
        Context context) {
        Mono<Response<Void>> response = blobServiceAsyncClient.setPropertiesWithResponse(properties, context);

        return Utility.blockWithOptionalTimeout(response, timeout);
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
    public Response<UserDelegationKey> getUserDelegationKeyWithResponse(OffsetDateTime start, OffsetDateTime expiry,
        Duration timeout, Context context) {
        Mono<Response<UserDelegationKey>> response = blobServiceAsyncClient.getUserDelegationKeyWithResponse(start,
            expiry, context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the storage account. For more information, see
     * the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.getStatistics}
     *
     * @return The storage account statistics.
     */
    public StorageServiceStats getStatistics() {
        return getStatisticsWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the storage account. For more information, see
     * the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.getStatisticsWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} the storage account statistics.
     */
    public Response<StorageServiceStats> getStatisticsWithResponse(Duration timeout, Context context) {
        Mono<Response<StorageServiceStats>> response = blobServiceAsyncClient.getStatisticsWithResponse(context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceClient.getAccountInfo}
     *
     * @return The storage account info.
     */
    public StorageAccountInfo getAccountInfo() {
        return getAccountInfoWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the storage account info.
     */
    public Response<StorageAccountInfo> getAccountInfoWithResponse(Duration timeout, Context context) {
        Mono<Response<StorageAccountInfo>> response = blobServiceAsyncClient.getAccountInfoWithResponse(context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Generates an account SAS token with the specified parameters
     *
     * @param accountSASService The {@code AccountSASService} services for the account SAS
     * @param accountSASResourceType An optional {@code AccountSASResourceType} resources for the account SAS
     * @param accountSASPermission The {@code AccountSASPermission} permission for the account SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the account SAS
     * @return A string that represents the SAS token
     */
    public String generateAccountSAS(AccountSASService accountSASService, AccountSASResourceType accountSASResourceType,
        AccountSASPermission accountSASPermission, OffsetDateTime expiryTime) {
        return this.blobServiceAsyncClient.generateAccountSAS(accountSASService, accountSASResourceType,
            accountSASPermission, expiryTime);
    }

    /**
     * Generates an account SAS token with the specified parameters
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.blobServiceClient.generateAccountSAS#AccountSASService-AccountSASResourceType-AccountSASPermission-OffsetDateTime-OffsetDateTime-String-IPRange-SASProtocol}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-account-sas">Azure Docs</a></p>
     *
     * @param accountSASService The {@code AccountSASService} services for the account SAS
     * @param accountSASResourceType An optional {@code AccountSASResourceType} resources for the account SAS
     * @param accountSASPermission The {@code AccountSASPermission} permission for the account SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the account SAS
     * @param startTime The {@code OffsetDateTime} start time for the account SAS
     * @param version The {@code String} version for the account SAS
     * @param ipRange An optional {@code IPRange} ip address range for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @return A string that represents the SAS token
     */
    public String generateAccountSAS(AccountSASService accountSASService, AccountSASResourceType accountSASResourceType,
        AccountSASPermission accountSASPermission, OffsetDateTime expiryTime, OffsetDateTime startTime, String version,
        IPRange ipRange, SASProtocol sasProtocol) {
        return this.blobServiceAsyncClient.generateAccountSAS(accountSASService, accountSASResourceType,
            accountSASPermission, expiryTime, startTime, version, ipRange, sasProtocol);
    }
}
