// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.Context;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.ContainerItem;
import com.azure.storage.blob.models.ServiceGetAccountInfoHeaders;
import com.azure.storage.blob.models.ServiceGetPropertiesHeaders;
import com.azure.storage.blob.models.ServiceGetStatisticsHeaders;
import com.azure.storage.blob.models.ServiceGetUserDelegationKeyHeaders;
import com.azure.storage.blob.models.ServiceSetPropertiesHeaders;
import com.azure.storage.blob.models.StorageServiceProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Client to a blob service. It may be obtained through a {@link BlobServiceClientBuilder}.
 * This class does not hold any state about a particular storage account but is
 * instead a convenient way of sending off appropriate requests to the resource on the service.
 * It may also be used to construct URLs to blobs and containers.
 * Please see <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction>here</a> for more
 * information on containers.
 */
public final class BlobServiceClient {

    private BlobServiceAsyncClient blobServiceAsyncClient;
    private BlobServiceClientBuilder builder;

    /**
     * Package-private constructor for use by {@link BlobServiceClientBuilder}.
     * @param azureBlobStorage the API client for blob storage API
     */
    BlobServiceClient(AzureBlobStorageImpl azureBlobStorage) {
        this.blobServiceAsyncClient = new BlobServiceAsyncClient(azureBlobStorage);
    }

    /**
     * Static method for getting a new builder for this class.
     *
     * @return
     *      A new {@link BlobServiceClientBuilder} instance.
     */
    public static BlobServiceClientBuilder builder() {
        return new BlobServiceClientBuilder();
    }

    /**
     * Package-private constructor for use by {@link BlobServiceClientBuilder}.
     * @param builder the blob service client builder
     */
    BlobServiceClient(BlobServiceClientBuilder builder) {
        this.builder = builder;
        this.blobServiceAsyncClient = new BlobServiceAsyncClient(builder);
    }

    /**
     * Creates a {@link ContainerClient} object pointing to the specified container. This method does not create a
     * container. It simply constructs the URL to the container and offers access to methods relevant to containers.
     *
     * @param containerName
     *     The name of the container to point to.
     * @return
     *     A {@link ContainerClient} object pointing to the specified container
     */
    public ContainerClient createContainerClient(String containerName) {
        try {
            return new ContainerClient(this.builder.copyAsContainerBuilder().endpoint(Utility.appendToURLPath(new URL(builder.endpoint()), containerName).toString()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a Mono segment of containers starting from the specified Marker.
     * Use an empty marker to start enumeration from the beginning. Container names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListContainers again (passing the the previously-returned
     * Marker) to get the next segment. For more information, see
     * the <a href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * @param options
     *         A {@link ListContainersOptions} which specifies what data should be returned by the service.
     *
     * @return
     *      The list of containers.
     */
    public Iterable<ContainerItem> listContainers(ListContainersOptions options) {
        return this.listContainers(options, null);
    }

    /**
     * Returns a Mono segment of containers starting from the specified Marker.
     * Use an empty marker to start enumeration from the beginning. Container names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListContainers again (passing the the previously-returned
     * Marker) to get the next segment. For more information, see
     * the <a href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * @param options
     *         A {@link ListContainersOptions} which specifies what data should be returned by the service.
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return
     *      The list of containers.
     */
    public Iterable<ContainerItem> listContainers(ListContainersOptions options, Duration timeout) {
        Flux<ContainerItem> response = blobServiceAsyncClient.listContainers(options, null);

        return timeout == null ?
            response.toIterable():
            response.timeout(timeout).toIterable();
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * @return
     *      The blob service properties.
     */
    public ServiceGetPropertiesHeaders getProperties() {
        return this.getProperties(null, null);
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      The blob service properties.
     */
    public ServiceGetPropertiesHeaders getProperties(Duration timeout, Context context) {

        Mono<ServiceGetPropertiesHeaders> response = blobServiceAsyncClient.getProperties(context);

        return timeout == null ?
            response.block():
            response.block(timeout);
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     *
     * @param properties
     *         Configures the service.
     *
     * @return
     *      The blob service properties.
     */
    public ServiceSetPropertiesHeaders setProperties(StorageServiceProperties properties) {
        return this.setProperties(properties, null, null);
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     *
     * @param properties
     *         Configures the service.
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      The blob service properties.
     */
    public ServiceSetPropertiesHeaders setProperties(StorageServiceProperties properties, Duration timeout, Context context) {
        Mono<ServiceSetPropertiesHeaders> response = blobServiceAsyncClient.setProperties(properties, context);

        return timeout == null ?
            response.block():
            response.block(timeout);
    }

    /**
     * Gets a user delegation key for use with this account's blob storage.
     * Note: This method call is only valid when using {@link TokenCredentials} in this object's {@link HttpPipeline}.
     *
     * @param start
     *         Start time for the key's validity. Null indicates immediate start.
     * @param expiry
     *         Expiration of the key's validity.
     *
     * @return
     *      The user delegation key.
     */
    public ServiceGetUserDelegationKeyHeaders getUserDelegationKey(OffsetDateTime start, OffsetDateTime expiry) {
        return this.getUserDelegationKey(start, expiry, null, null);
    }

    /**
     * Gets a user delegation key for use with this account's blob storage.
     * Note: This method call is only valid when using {@link TokenCredentials} in this object's {@link HttpPipeline}.
     *
     * @param start
     *         Start time for the key's validity. Null indicates immediate start.
     * @param expiry
     *         Expiration of the key's validity.
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      The user delegation key.
     */
    public ServiceGetUserDelegationKeyHeaders getUserDelegationKey(OffsetDateTime start, OffsetDateTime expiry,
            Duration timeout, Context context) {
        Mono<ServiceGetUserDelegationKeyHeaders> response = blobServiceAsyncClient.getUserDelegationKey(start, expiry, context);

        return timeout == null ?
            response.block():
            response.block(timeout);
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary
     * location endpoint when read-access geo-redundant replication is enabled for the storage account. For more
     * information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * @return
     *      The blob service statistics.
     */
    public ServiceGetStatisticsHeaders getStatistics() {
        return this.getStatistics(null, null);
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary
     * location endpoint when read-access geo-redundant replication is enabled for the storage account. For more
     * information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      The blob service statistics.
     */
    public ServiceGetStatisticsHeaders getStatistics(Duration timeout, Context context) {
        Mono<ServiceGetStatisticsHeaders> response = blobServiceAsyncClient.getStatistics(context);

        return timeout == null ?
            response.block():
            response.block(timeout);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @return
     *      The blob service account info.
     */
    public ServiceGetAccountInfoHeaders getAccountInfo() {
        return this.getAccountInfo(null, null);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      The blob service account info.
     */
    public ServiceGetAccountInfoHeaders getAccountInfo(Duration timeout, Context context) {
        Mono<ServiceGetAccountInfoHeaders> response = blobServiceAsyncClient.getAccountInfo(context);

        return timeout == null ?
            response.block():
            response.block(timeout);
    }
}
