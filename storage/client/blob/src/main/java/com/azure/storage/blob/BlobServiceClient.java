// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.blob.models.ContainerItem;
import com.azure.storage.blob.models.ListContainersOptions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.StorageServiceProperties;
import com.azure.storage.blob.models.StorageServiceStats;
import com.azure.storage.blob.models.UserDelegationKey;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Client to a storage account. It may only be instantiated through a {@link BlobServiceClientBuilder}. This class does not
 * hold any state about a particular storage account but is instead a convenient way of sending off appropriate requests
 * to the resource on the service. It may also be used to construct URLs to blobs and containers.
 *
 * <p>
 * This client contains operations on a blob. Operations on a container are available on {@link ContainerClient} through
 * {@link #getContainerClient(String)}, and operations on a blob are available on {@link BlobClient}.
 *
 * <p>
 * Please see <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction>here</a> for more
 * information on containers.
 */
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
     * @param containerName The name of the container to point to.
     * @return A {@link ContainerClient} object pointing to the specified container
     */
    public ContainerClient getContainerClient(String containerName) {
        return new ContainerClient(blobServiceAsyncClient.getContainerAsyncClient(containerName));
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * @param containerName Name of the container to create
     * @return A response containing a {@link ContainerClient} used to interact with the container created.
     */
    public Response<ContainerClient> createContainer(String containerName) {
        return createContainer(containerName, null, null);
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * @param containerName Name of the container to create
     * @param metadata {@link Metadata}
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @return A response containing a {@link ContainerClient} used to interact with the container created.
     */
    public Response<ContainerClient> createContainer(String containerName, Metadata metadata, PublicAccessType accessType) {
        ContainerClient client = getContainerClient(containerName);

        return new SimpleResponse<>(client.create(metadata, accessType, null), client);
    }

    /**
     * Deletes the specified container in the storage account. If the container doesn't exist the operation fails. For
     * more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * @param containerName Name of the container to delete
     * @return A response containing status code and HTTP headers
     */
    public VoidResponse deleteContainer(String containerName) {
        return blobServiceAsyncClient.deleteContainer(containerName).block();
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
     * Returns a lazy loaded list of containers in this account. The returned {@link Iterable} can be iterated through
     * while new items are automatically retrieved as needed. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * @return The list of containers.
     */
    public Iterable<ContainerItem> listContainers() {
        return this.listContainers(new ListContainersOptions(), null);
    }

    /**
     * Returns a lazy loaded list of containers in this account. The returned {@link Iterable} can be iterated through
     * while new items are automatically retrieved as needed. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * @param options A {@link ListContainersOptions} which specifies what data should be returned by the service.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The list of containers.
     */
    public Iterable<ContainerItem> listContainers(ListContainersOptions options, Duration timeout) {
        Flux<ContainerItem> response = blobServiceAsyncClient.listContainers(options);

        return timeout == null ? response.toIterable() : response.timeout(timeout).toIterable();
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * @return The storage account properties.
     */
    public Response<StorageServiceProperties> getProperties() {
        return this.getProperties(null);
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The storage account properties.
     */
    public Response<StorageServiceProperties> getProperties(Duration timeout) {

        Mono<Response<StorageServiceProperties>> response = blobServiceAsyncClient.getProperties();

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     *
     * @param properties Configures the service.
     * @return The storage account properties.
     */
    public VoidResponse setProperties(StorageServiceProperties properties) {
        return this.setProperties(properties, null);
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     *
     * @param properties Configures the service.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The storage account properties.
     */
    public VoidResponse setProperties(StorageServiceProperties properties, Duration timeout) {
        Mono<VoidResponse> response = blobServiceAsyncClient.setProperties(properties);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Gets a user delegation key for use with this account's blob storage. Note: This method call is only valid when
     * using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @return The user delegation key.
     */
    public Response<UserDelegationKey> getUserDelegationKey(OffsetDateTime start, OffsetDateTime expiry) {
        return this.getUserDelegationKey(start, expiry, null);
    }

    /**
     * Gets a user delegation key for use with this account's blob storage. Note: This method call is only valid when
     * using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The user delegation key.
     */
    public Response<UserDelegationKey> getUserDelegationKey(OffsetDateTime start, OffsetDateTime expiry,
                                                            Duration timeout) {
        Mono<Response<UserDelegationKey>> response = blobServiceAsyncClient.getUserDelegationKey(start, expiry);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the storage account. For more information, see
     * the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * @return The storage account statistics.
     */
    public Response<StorageServiceStats> getStatistics() {
        return this.getStatistics(null);
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the storage account. For more information, see
     * the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The storage account statistics.
     */
    public Response<StorageServiceStats> getStatistics(Duration timeout) {
        Mono<Response<StorageServiceStats>> response = blobServiceAsyncClient.getStatistics();

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @return The storage account info.
     */
    public Response<StorageAccountInfo> getAccountInfo() {
        return this.getAccountInfo(null);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The storage account info.
     */
    public Response<StorageAccountInfo> getAccountInfo(Duration timeout) {
        Mono<Response<StorageAccountInfo>> response = blobServiceAsyncClient.getAccountInfo();

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
        return this.blobServiceAsyncClient.generateAccountSAS(accountSASService, accountSASResourceType, accountSASPermission, expiryTime);
    }

    /**
     * Generates an account SAS token with the specified parameters
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
                                     AccountSASPermission accountSASPermission, OffsetDateTime expiryTime, OffsetDateTime startTime, String version, IPRange ipRange,
                                     SASProtocol sasProtocol) {
        return this.blobServiceAsyncClient.generateAccountSAS(accountSASService, accountSASResourceType, accountSASPermission, expiryTime, startTime, version, ipRange, sasProtocol);
    }
}
