// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.Context;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.ContainerItem;
import com.azure.storage.blob.models.ServicesListContainersSegmentResponse;
import com.azure.storage.blob.models.StorageServiceProperties;
import com.azure.storage.blob.models.StorageServiceStats;
import com.azure.storage.blob.models.UserDelegationKey;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Client to a storage account. It may only be instantiated through a {@link StorageClientBuilder}.
 * This class does not hold any state about a particular storage account but is
 * instead a convenient way of sending off appropriate requests to the resource on the service.
 * It may also be used to construct URLs to blobs and containers.
 *
 * <p>
 * This client contains operations on a blob. Operations on a container are available on {@link ContainerAsyncClient}
 * through {@link #getContainerAsyncClient(String)}, and operations on a blob are available on {@link BlobAsyncClient}.
 *
 * <p>
 * Please see <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction>here</a> for more
 * information on containers.
 *
 * <p>
 * Note this client is an async client that returns reactive responses from Spring Reactor Core
 * project (https://projectreactor.io/). Calling the methods in this client will <strong>NOT</strong>
 * start the actual network operation, until {@code .subscribe()} is called on the reactive response.
 * You can simply convert one of these responses to a {@link java.util.concurrent.CompletableFuture}
 * object through {@link Mono#toFuture()}.
 */
public final class StorageAsyncClient {

    StorageAsyncRawClient storageAsyncRawClient;
    private StorageClientBuilder builder;

    /**
     * Package-private constructor for use by {@link StorageClientBuilder}.
     * @param azureBlobStorage the API client for blob storage API
     */
    StorageAsyncClient(AzureBlobStorageImpl azureBlobStorage) {
        this.storageAsyncRawClient = new StorageAsyncRawClient(azureBlobStorage);
    }

    /**
     * Static method for getting a new builder for this class.
     *
     * @return
     *      A new {@link StorageClientBuilder} instance.
     */
    public static StorageClientBuilder storageClientBuilder() {
        return new StorageClientBuilder();
    }

    /**
     * Package-private constructor for use by {@link StorageClientBuilder}.
     * @param builder the storage account client builder
     */
    StorageAsyncClient(StorageClientBuilder builder) {
        this.builder = builder;
        this.storageAsyncRawClient = new StorageAsyncRawClient(builder.buildImpl());
    }

    /**
     * Initializes a {@link ContainerAsyncClient} object pointing to the specified container. This method does not create a
     * container. It simply constructs the URL to the container and offers access to methods relevant to containers.
     *
     * @param containerName
     *     The name of the container to point to.
     * @return
     *     A {@link ContainerAsyncClient} object pointing to the specified container
     */
    public ContainerAsyncClient getContainerAsyncClient(String containerName) {
        try {
            return new ContainerAsyncClient(this.builder.copyAsContainerBuilder().endpoint(Utility.appendToURLPath(new URL(builder.endpoint()), containerName).toString()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a reactive Publisher emitting all the containers in this account lazily as needed. For more information, see
     * the <a href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * @return
     *      A reactive response emitting the list of containers.
     */
    public Flux<ContainerItem> listContainers() {
        return this.listContainers(new ListContainersOptions(), null);
    }

    /**
     * Returns a reactive Publisher emitting all the containers in this account lazily as needed. For more information, see
     * the <a href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * @param options
     *         A {@link ListContainersOptions} which specifies what data should be returned by the service.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      A reactive response emitting the list of containers.
     */
    public Flux<ContainerItem> listContainers(ListContainersOptions options, Context context) {
        return storageAsyncRawClient
            .listContainersSegment(null, options, context)
            .flatMapMany(response -> listContainersHelper(response.value().marker(), options, context, response));
    }

    private Flux<ContainerItem> listContainersHelper(String marker, ListContainersOptions options, Context context,
            ServicesListContainersSegmentResponse response){
        Flux<ContainerItem> result = Flux.fromIterable(response.value().containerItems());
        if (response.value().nextMarker() != null) {
            // Recursively add the continuation items to the observable.
            result = result.concatWith(storageAsyncRawClient.listContainersSegment(marker, options,
                context)
                .flatMapMany((r) ->
                    listContainersHelper(response.value().nextMarker(), options, context, r)));
        }

        return result;
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * @return
     *      A reactive response containing the storage account properties.
     */
    public Mono<StorageServiceProperties> getProperties() {
        return this.getProperties(null);
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      A reactive response containing the storage account properties.
     */
    public Mono<StorageServiceProperties> getProperties(Context context) {
        return storageAsyncRawClient
            .getProperties(context)
            .map(ResponseBase::value);
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
     *      A reactive response containing the storage account properties.
     */
    public Mono<Void> setProperties(StorageServiceProperties properties) {
        return this.setProperties(properties, null);
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     *
     * @param properties
     *         Configures the service.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      A reactive response containing the storage account properties.
     */
    public Mono<Void> setProperties(StorageServiceProperties properties, Context context) {
        return storageAsyncRawClient
            .setProperties(properties, context)
            .then();
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
     *      A reactive response containing the user delegation key.
     */
    public Mono<UserDelegationKey> getUserDelegationKey(OffsetDateTime start, OffsetDateTime expiry) {
        return this.getUserDelegationKey(start, expiry, null);
    }

    /**
     * Gets a user delegation key for use with this account's blob storage.
     * Note: This method call is only valid when using {@link TokenCredentials} in this object's {@link HttpPipeline}.
     *
     * @param start
     *         Start time for the key's validity. Null indicates immediate start.
     * @param expiry
     *         Expiration of the key's validity.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      A reactive response containing the user delegation key.
     */
    public Mono<UserDelegationKey> getUserDelegationKey(OffsetDateTime start, OffsetDateTime expiry,
            Context context) {
        return storageAsyncRawClient
            .getUserDelegationKey(start, expiry, context)
            .map(ResponseBase::value);
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary
     * location endpoint when read-access geo-redundant replication is enabled for the storage account. For more
     * information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * @return
     *      A reactive response containing the storage account statistics.
     */
    public Mono<StorageServiceStats> getStatistics() {
        return this.getStatistics(null);
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary
     * location endpoint when read-access geo-redundant replication is enabled for the storage account. For more
     * information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      A reactive response containing the storage account statistics.
     */
    public Mono<StorageServiceStats> getStatistics(Context context) {
        return storageAsyncRawClient
            .getStatistics(context)
            .map(ResponseBase::value);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @return
     *      A reactive response containing the storage account info.
     */
    public Mono<StorageAccountInfo> getAccountInfo() {
        return this.getAccountInfo(null);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      A reactive response containing the storage account info.
     */
    public Mono<StorageAccountInfo> getAccountInfo(Context context) {
        return storageAsyncRawClient
            .getAccountInfo(context)
            .map(ResponseBase::deserializedHeaders)
            .map(StorageAccountInfo::new);
    }
}
