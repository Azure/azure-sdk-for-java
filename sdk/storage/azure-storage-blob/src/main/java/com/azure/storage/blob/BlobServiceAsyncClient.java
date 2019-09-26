// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.BatchResult;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.http.PagedResponseBase;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.ContainerItem;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.KeyInfo;
import com.azure.storage.blob.models.ListContainersOptions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.ServicesListContainersSegmentResponse;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.StorageServiceProperties;
import com.azure.storage.blob.models.StorageServiceStats;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.specialized.BlobBatch;
import com.azure.storage.blob.specialized.BlobBatchResult;
import com.azure.storage.common.AccountSASPermission;
import com.azure.storage.common.AccountSASResourceType;
import com.azure.storage.common.AccountSASService;
import com.azure.storage.common.AccountSASSignatureValues;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SharedKeyCredential;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.function.Function;

import static com.azure.core.implementation.util.FluxUtil.withContext;
import static com.azure.storage.blob.implementation.PostProcessor.postProcessResponse;

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
@ServiceClient(builder = BlobServiceClientBuilder.class, isAsync = true)
public final class BlobServiceAsyncClient {
    private final ClientLogger logger = new ClientLogger(BlobServiceAsyncClient.class);

    private final AzureBlobStorageImpl azureBlobStorage;
    private final CpkInfo customerProvidedKey;

    /**
     * Package-private constructor for use by {@link BlobServiceClientBuilder}.
     *
     * @param azureBlobStorage the API client for blob storage
     */
    BlobServiceAsyncClient(AzureBlobStorageImpl azureBlobStorage, CpkInfo customerProvidedKey) {
        this.azureBlobStorage = azureBlobStorage;
        this.customerProvidedKey = customerProvidedKey;
    }

    /**
     * Initializes a {@link ContainerAsyncClient} object pointing to the specified container. This method does not
     * create a container. It simply constructs the URL to the container and offers access to methods relevant to
     * containers.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getContainerAsyncClient#String}
     *
     * @param containerName The name of the container to point to.
     * @return A {@link ContainerAsyncClient} object pointing to the specified container
     */
    public ContainerAsyncClient getContainerAsyncClient(String containerName) {
        return new ContainerAsyncClient(new AzureBlobStorageBuilder()
            .url(Utility.appendToURLPath(getAccountUrl(), containerName).toString())
            .pipeline(azureBlobStorage.getHttpPipeline())
            .build(), customerProvidedKey);
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return azureBlobStorage.getHttpPipeline();
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.createContainer#String}
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
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.createContainerWithResponse#String-Metadata-PublicAccessType}
     *
     * @param containerName Name of the container to create
     * @param metadata {@link Metadata}
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * ContainerAsyncClient} used to interact with the container created.
     */
    public Mono<Response<ContainerAsyncClient>> createContainerWithResponse(String containerName, Metadata metadata,
        PublicAccessType accessType) {
        return withContext(context -> createContainerWithResponse(containerName, metadata, accessType, context));
    }

    Mono<Response<ContainerAsyncClient>> createContainerWithResponse(String containerName, Metadata metadata,
        PublicAccessType accessType, Context context) {
        ContainerAsyncClient containerAsyncClient = getContainerAsyncClient(containerName);

        return containerAsyncClient.createWithResponse(metadata, accessType, context)
            .map(response -> new SimpleResponse<>(response, containerAsyncClient));
    }

    /**
     * Deletes the specified container in the storage account. If the container doesn't exist the operation fails. For
     * more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.deleteContainer#String}
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
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.deleteContainerWithResponse#String-Context}
     *
     * @param containerName Name of the container to delete
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    public Mono<Response<Void>> deleteContainerWithResponse(String containerName) {
        return withContext(context -> deleteContainerWithResponse(containerName, context));
    }

    Mono<Response<Void>> deleteContainerWithResponse(String containerName, Context context) {
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
            throw logger.logExceptionAsError(new RuntimeException(
                String.format("Invalid URL on %s: %s" + getClass().getSimpleName(), azureBlobStorage.getUrl()), e));
        }
    }

    /**
     * Returns a reactive Publisher emitting all the containers in this account lazily as needed. For more information,
     * see the <a href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.listContainers}
     *
     * @return A reactive response emitting the list of containers.
     */
    public PagedFlux<ContainerItem> listContainers() {
        return this.listContainers(new ListContainersOptions());
    }

    /**
     * Returns a reactive Publisher emitting all the containers in this account lazily as needed. For more information,
     * see the <a href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.listContainers#ListContainersOptions}
     *
     * @param options A {@link ListContainersOptions} which specifies what data should be returned by the service.
     * @return A reactive response emitting the list of containers.
     */
    public PagedFlux<ContainerItem> listContainers(ListContainersOptions options) {
        return listContainersWithOptionalTimeout(options, null);
    }

    PagedFlux<ContainerItem> listContainersWithOptionalTimeout(ListContainersOptions options, Duration timeout) {
        Function<String, Mono<PagedResponse<ContainerItem>>> func =
            marker -> listContainersSegment(marker, options, timeout)
                .map(response -> new PagedResponseBase<>(
                    response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue().getContainerItems(),
                    response.getValue().getNextMarker(),
                    response.getDeserializedHeaders()));

        return new PagedFlux<>(() -> func.apply(null), func);
    }

    private Mono<ServicesListContainersSegmentResponse> listContainersSegment(String marker,
        ListContainersOptions options, Duration timeout) {
        options = options == null ? new ListContainersOptions() : options;

        return postProcessResponse(Utility.applyOptionalTimeout(
            this.azureBlobStorage.services().listContainersSegmentWithRestResponseAsync(
                options.getPrefix(), marker, options.getMaxResults(), options.getDetails().toIncludeType(), null,
                null, Context.NONE), timeout));
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getProperties}
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
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getPropertiesWithResponse}
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the storage
     * account properties.
     */
    public Mono<Response<StorageServiceProperties>> getPropertiesWithResponse() {
        return withContext(this::getPropertiesWithResponse);
    }

    Mono<Response<StorageServiceProperties>> getPropertiesWithResponse(Context context) {
        return postProcessResponse(
            this.azureBlobStorage.services().getPropertiesWithRestResponseAsync(null, null, context))
            .map(rb -> new SimpleResponse<>(rb, rb.getValue()));
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.setProperties#StorageServiceProperties}
     *
     * @param properties Configures the service.
     * @return A {@link Mono} containing the storage account properties.
     */
    public Mono<Void> setProperties(StorageServiceProperties properties) {
        return setPropertiesWithResponse(properties).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.setPropertiesWithResponse#StorageServiceProperties}
     *
     * @param properties Configures the service.
     * @return A {@link Mono} containing the storage account properties.
     */
    public Mono<Response<Void>> setPropertiesWithResponse(StorageServiceProperties properties) {
        return withContext(context -> setPropertiesWithResponse(properties, context));
    }

    Mono<Response<Void>> setPropertiesWithResponse(StorageServiceProperties properties, Context context) {
        return postProcessResponse(
            this.azureBlobStorage.services().setPropertiesWithRestResponseAsync(properties, null, null, context))
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Gets a user delegation key for use with this account's blob storage. Note: This method call is only valid when
     * using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime}
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @return A {@link Mono} containing the user delegation key.
     * @throws IllegalArgumentException If {@code start} isn't null and is after {@code expiry}.
     */
    public Mono<UserDelegationKey> getUserDelegationKey(OffsetDateTime start, OffsetDateTime expiry) {
        return withContext(context -> getUserDelegationKeyWithResponse(start, expiry, context))
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a user delegation key for use with this account's blob storage. Note: This method call is only valid when
     * using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime}
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} containing the user
     * delegation key.
     * @throws IllegalArgumentException If {@code start} isn't null and is after {@code expiry}.
     */
    public Mono<Response<UserDelegationKey>> getUserDelegationKeyWithResponse(OffsetDateTime start,
        OffsetDateTime expiry) {
        return withContext(context -> getUserDelegationKeyWithResponse(start, expiry, context));
    }

    Mono<Response<UserDelegationKey>> getUserDelegationKeyWithResponse(OffsetDateTime start, OffsetDateTime expiry,
        Context context) {
        Utility.assertNotNull("expiry", expiry);
        if (start != null && !start.isBefore(expiry)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("`start` must be null or a datetime before `expiry`."));
        }

        return postProcessResponse(
            this.azureBlobStorage.services().getUserDelegationKeyWithRestResponseAsync(
                new KeyInfo()
                    .setStart(start == null ? "" : Utility.ISO_8601_UTC_DATE_FORMATTER.format(start))
                    .setExpiry(Utility.ISO_8601_UTC_DATE_FORMATTER.format(expiry)),
                null, null, context)
        ).map(rb -> new SimpleResponse<>(rb, rb.getValue()));
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the storage account. For more information, see
     * the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getStatistics}
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
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getStatisticsWithResponse}
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} containing the
     * storage account statistics.
     */
    public Mono<Response<StorageServiceStats>> getStatisticsWithResponse() {
        return withContext(this::getStatisticsWithResponse);
    }

    Mono<Response<StorageServiceStats>> getStatisticsWithResponse(Context context) {
        return postProcessResponse(
            this.azureBlobStorage.services().getStatisticsWithRestResponseAsync(null, null, context))
            .map(rb -> new SimpleResponse<>(rb, rb.getValue()));
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getAccountInfo}
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
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getAccountInfoWithResponse}
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} the storage account
     * info.
     */
    public Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse() {
        return withContext(this::getAccountInfoWithResponse);
    }

    Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse(Context context) {
        return postProcessResponse(this.azureBlobStorage.services().getAccountInfoWithRestResponseAsync(context))
            .map(rb -> new SimpleResponse<>(rb, new StorageAccountInfo(rb.getDeserializedHeaders())));
    }

    /**
     * Submits a batch operation.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * @param batch Batch to submit.
     * @return Results of the batch.
     */
    public Mono<BatchResult> submitBatch(BlobBatch batch) {
        return withContext(context -> submitBatch(batch, context));
    }

    Mono<BatchResult> submitBatch(BlobBatch batch, Context context) {
        return postProcessResponse(this.azureBlobStorage.services()
            .submitBatchWithRestResponseAsync(batch.getBody(), batch.getContentLength(), batch.getContentType(), context)
            .map(BlobBatchResult::new));
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
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.blobServiceAsyncClient.generateAccountSAS#AccountSASService-AccountSASResourceType-AccountSASPermission-OffsetDateTime-OffsetDateTime-String-IPRange-SASProtocol}
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

        SharedKeyCredential sharedKeyCredential =
            Utility.getSharedKeyCredential(this.azureBlobStorage.getHttpPipeline());
        Utility.assertNotNull("sharedKeyCredential", sharedKeyCredential);

        return AccountSASSignatureValues.generateAccountSAS(sharedKeyCredential, accountSASService,
            accountSASResourceType, accountSASPermission, expiryTime, startTime, version, ipRange, sasProtocol);
    }
}
