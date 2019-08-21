// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.ContainerItem;
import com.azure.storage.blob.models.KeyInfo;
import com.azure.storage.blob.models.ListContainersOptions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.ServicesListContainersSegmentResponse;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.StorageServiceProperties;
import com.azure.storage.blob.models.StorageServiceStats;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.common.AccountSASPermission;
import com.azure.storage.common.AccountSASQueryParameters;
import com.azure.storage.common.AccountSASResourceType;
import com.azure.storage.common.AccountSASService;
import com.azure.storage.common.AccountSASSignatureValues;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SharedKeyCredential;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;

import static com.azure.storage.blob.PostProcessor.postProcessResponse;
import static com.azure.core.implementation.util.FluxUtil.withContext;

/**
 * Client to a storage account. It may only be instantiated through a {@link BlobServiceClientBuilder}. This class does
 * not hold any state about a particular storage account but is instead a convenient way of sending off appropriate
 * requests to the resource on the service. It may also be used to construct URLs to blobs and containers.
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
 * Note this client is an async client that returns reactive responses from Spring Reactor Core project
 * (https://projectreactor.io/). Calling the methods in this client will <strong>NOT</strong> start the actual network
 * operation, until {@code .subscribe()} is called on the reactive response. You can simply convert one of these
 * responses to a {@link java.util.concurrent.CompletableFuture} object through {@link Mono#toFuture()}.
 */
public final class BlobServiceAsyncClient {
    private final ClientLogger logger = new ClientLogger(BlobServiceAsyncClient.class);

    private final AzureBlobStorageImpl azureBlobStorage;

    /**
     * Package-private constructor for use by {@link BlobServiceClientBuilder}.
     *
     * @param azureBlobStorage the API client for blob storage
     */
    BlobServiceAsyncClient(AzureBlobStorageImpl azureBlobStorage) {
        this.azureBlobStorage = azureBlobStorage;
    }

    /**
     * Initializes a {@link ContainerAsyncClient} object pointing to the specified container. This method does not
     * create a container. It simply constructs the URL to the container and offers access to methods relevant to
     * containers.
     *
     * @param containerName The name of the container to point to.
     * @return A {@link ContainerAsyncClient} object pointing to the specified container
     */
    public ContainerAsyncClient getContainerAsyncClient(String containerName) {
        return new ContainerAsyncClient(new AzureBlobStorageBuilder()
            .url(Utility.appendToURLPath(getAccountUrl(), containerName).toString())
            .pipeline(azureBlobStorage.getHttpPipeline())
            .build());
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * @param containerName Name of the container to create
     * @return A {@link Mono} containing a {@link ContainerAsyncClient} used to interact with the container created.
     */
    public Mono<ContainerAsyncClient> createContainer(String containerName) {
        return createContainerWithResponse(containerName, null, null).flatMap(FluxUtil::toMono);
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
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains a {@link
     * ContainerAsyncClient} used to interact with the container created.
     */
    public Mono<Response<ContainerAsyncClient>> createContainerWithResponse(String containerName, Metadata metadata, PublicAccessType accessType) {
        return withContext(context -> createContainerWithResponse(containerName, metadata, accessType, context));
    }

    Mono<Response<ContainerAsyncClient>> createContainerWithResponse(String containerName, Metadata metadata, PublicAccessType accessType, Context context) {
        ContainerAsyncClient containerAsyncClient = getContainerAsyncClient(containerName);

        return containerAsyncClient.createWithResponse(metadata, accessType, context)
            .map(response -> new SimpleResponse<>(response, containerAsyncClient));
    }

    /**
     * Deletes the specified container in the storage account. If the container doesn't exist the operation fails. For
     * more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * @param containerName Name of the container to delete
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    public Mono<Void> deleteContainer(String containerName) {
        return deleteContainerWithResponse(containerName).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified container in the storage account. If the container doesn't exist the operation fails. For
     * more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * @param containerName Name of the container to delete
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    public Mono<VoidResponse> deleteContainerWithResponse(String containerName) {
        return withContext(context -> deleteContainerWithResponse(containerName, context));
    }

    Mono<VoidResponse> deleteContainerWithResponse(String containerName, Context context) {
        return getContainerAsyncClient(containerName).deleteWithResponse(null, context);
    }

    /**
     * Gets the URL of the storage account represented by this client.
     *
     * @return the URL.
     * @throws RuntimeException If the account URL is malformed.
     */
    public URL getAccountUrl() {
        try {
            return new URL(azureBlobStorage.getUrl());
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new RuntimeException(String.format("Invalid URL on %s: %s" + getClass().getSimpleName(), azureBlobStorage.getUrl()), e));
        }
    }

    /**
     * Returns a reactive Publisher emitting all the containers in this account lazily as needed. For more information,
     * see the <a href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * @return A reactive response emitting the list of containers.
     */
    public Flux<ContainerItem> listContainers() {
        return this.listContainers(new ListContainersOptions());
    }

    /**
     * Returns a reactive Publisher emitting all the containers in this account lazily as needed. For more information,
     * see the <a href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * @param options A {@link ListContainersOptions} which specifies what data should be returned by the service.
     * @return A reactive response emitting the list of containers.
     */
    public Flux<ContainerItem> listContainers(ListContainersOptions options) {
        return listContainersSegment(null, options)
            .flatMapMany(response -> listContainersHelper(response.value().marker(), options, response));
    }

    /*
     * Returns a Mono segment of containers starting from the specified Marker.
     * Use an empty marker to start enumeration from the beginning. Container names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListContainers again (passing the the previously-returned
     * Marker) to get the next segment. For more information, see
     * the <a href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * @param marker
     *         Identifies the portion of the list to be returned with the next list operation.
     *         This value is returned in the response of a previous list operation as the
     *         ListContainersSegmentResponse.body().nextMarker(). Set to null to list the first segment.
     * @param options
     *         A {@link ListContainersOptions} which specifies what data should be returned by the service.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=service_list "Sample code for ServiceURL.listContainersSegment")] \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=service_list_helper "Helper code for ServiceURL.listContainersSegment")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    private Mono<ServicesListContainersSegmentResponse> listContainersSegment(String marker, ListContainersOptions options) {
        options = options == null ? new ListContainersOptions() : options;

        return postProcessResponse(
            this.azureBlobStorage.services().listContainersSegmentWithRestResponseAsync(
                options.prefix(), marker, options.maxResults(), options.details().toIncludeType(), null,
                null, Context.NONE));
    }

    private Flux<ContainerItem> listContainersHelper(String marker, ListContainersOptions options,
                                                     ServicesListContainersSegmentResponse response) {
        Flux<ContainerItem> result = Flux.fromIterable(response.value().containerItems());
        if (response.value().nextMarker() != null) {
            // Recursively add the continuation items to the observable.
            result = result.concatWith(listContainersSegment(marker, options)
                .flatMapMany((r) ->
                    listContainersHelper(response.value().nextMarker(), options, r)));
        }

        return result;
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * @return A reactive response containing the storage account properties.
     */
    public Mono<StorageServiceProperties> getProperties() {
        return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the storage
     * account properties.
     */
    public Mono<Response<StorageServiceProperties>> getPropertiesWithResponse() {
        return withContext(context -> getPropertiesWithResponse(context));
    }

    Mono<Response<StorageServiceProperties>> getPropertiesWithResponse(Context context) {
        return postProcessResponse(
            this.azureBlobStorage.services().getPropertiesWithRestResponseAsync(null, null, context))
            .map(rb -> new SimpleResponse<>(rb, rb.value()));
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     *
     * @param properties Configures the service.
     * @return A {@link Mono} containing the storage account properties.
     */
    public Mono<Void> setProperties(StorageServiceProperties properties) {
        return setPropertiesWithReponse(properties).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     *
     * @param properties Configures the service.
     * @return A {@link Mono} containing the storage account properties.
     */
    public Mono<VoidResponse> setPropertiesWithReponse(StorageServiceProperties properties) {
        return withContext(context -> setPropertiesWithReponse(properties, context));
    }

    Mono<VoidResponse> setPropertiesWithReponse(StorageServiceProperties properties, Context context) {
        return postProcessResponse(
            this.azureBlobStorage.services().setPropertiesWithRestResponseAsync(properties, null, null, context))
            .map(VoidResponse::new);
    }

    /**
     * Gets a user delegation key for use with this account's blob storage. Note: This method call is only valid when
     * using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @return A {@link Mono} containing the user delegation key.
     * @throws IllegalArgumentException If {@code start} isn't null and is after {@code expiry}.
     */
    public Mono<UserDelegationKey> getUserDelegationKey(OffsetDateTime start, OffsetDateTime expiry) {
        return withContext(context -> getUserDelegationKeyWithResponse(start, expiry, context)).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a user delegation key for use with this account's blob storage. Note: This method call is only valid when
     * using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} containing the user
     * delegation key.
     * @throws IllegalArgumentException If {@code start} isn't null and is after {@code expiry}.
     */
    public Mono<Response<UserDelegationKey>> getUserDelegationKeyWithResponse(OffsetDateTime start, OffsetDateTime expiry) {
        return withContext(context -> getUserDelegationKeyWithResponse(start, expiry, context));
    }

    Mono<Response<UserDelegationKey>> getUserDelegationKeyWithResponse(OffsetDateTime start, OffsetDateTime expiry, Context context) {
        Utility.assertNotNull("expiry", expiry);
        if (start != null && !start.isBefore(expiry)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("`start` must be null or a datetime before `expiry`."));
        }

        return postProcessResponse(
            this.azureBlobStorage.services().getUserDelegationKeyWithRestResponseAsync(
                new KeyInfo()
                    .start(start == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(start))
                    .expiry(Utility.ISO_8601_UTC_DATE_FORMATTER.format(expiry)),
                null, null, context)
        ).map(rb -> new SimpleResponse<>(rb, rb.value()));
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the storage account. For more information, see
     * the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * @return A {@link Mono} containing the storage account statistics.
     */
    public Mono<StorageServiceStats> getStatistics() {
        return getStatisticsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the storage account. For more information, see
     * the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} containing the storage
     * account statistics.
     */
    public Mono<Response<StorageServiceStats>> getStatisticsWithResponse() {
        return withContext(context -> getStatisticsWithResponse(context));
    }

    Mono<Response<StorageServiceStats>> getStatisticsWithResponse(Context context) {
        return postProcessResponse(
            this.azureBlobStorage.services().getStatisticsWithRestResponseAsync(null, null, context))
            .map(rb -> new SimpleResponse<>(rb, rb.value()));
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @return A {@link Mono} containing containing the storage account info.
     */
    public Mono<StorageAccountInfo> getAccountInfo() {
        return getAccountInfoWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} the storage account
     * info.
     */
    public Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse() {
        return withContext(context -> getAccountInfoWithResponse(context));
    }

    Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse(Context context) {
        return postProcessResponse(this.azureBlobStorage.services().getAccountInfoWithRestResponseAsync(context))
            .map(rb -> new SimpleResponse<>(rb, new StorageAccountInfo(rb.deserializedHeaders())));
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
        return this.generateAccountSAS(accountSASService, accountSASResourceType, accountSASPermission, expiryTime,
            null /* startTime */, null /* version */, null /* ipRange */, null /* sasProtocol */);
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
        AccountSASPermission accountSASPermission, OffsetDateTime expiryTime, OffsetDateTime startTime, String version,
        IPRange ipRange, SASProtocol sasProtocol) {

        AccountSASSignatureValues accountSASSignatureValues = new AccountSASSignatureValues();
        accountSASSignatureValues.services(accountSASService == null ? null : accountSASService.toString());
        accountSASSignatureValues.resourceTypes(accountSASResourceType == null ? null : accountSASResourceType.toString());
        accountSASSignatureValues.permissions(accountSASPermission == null ? null : accountSASPermission.toString());
        accountSASSignatureValues.expiryTime(expiryTime);
        accountSASSignatureValues.startTime(startTime);

        if (version != null) {
            accountSASSignatureValues.version(version);
        }

        accountSASSignatureValues.ipRange(ipRange);
        accountSASSignatureValues.protocol(sasProtocol);

        SharedKeyCredential sharedKeyCredential = Utility.getSharedKeyCredential(this.azureBlobStorage.getHttpPipeline());

        AccountSASQueryParameters sasQueryParameters = accountSASSignatureValues.generateSASQueryParameters(sharedKeyCredential);

        return sasQueryParameters.encode();
    }
}
