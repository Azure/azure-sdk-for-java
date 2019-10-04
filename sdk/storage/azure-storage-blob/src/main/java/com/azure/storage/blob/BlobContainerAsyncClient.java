// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
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
import com.azure.storage.blob.implementation.models.ContainersListBlobFlatSegmentResponse;
import com.azure.storage.blob.implementation.models.ContainersListBlobHierarchySegmentResponse;
import com.azure.storage.blob.models.BlobContainerAccessPolicies;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobContainerAccessConditions;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.SignedIdentifier;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.StorageException;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.specialized.BlobServiceSasQueryParameters;
import com.azure.storage.blob.specialized.BlobServiceSasSignatureValues;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IpRange;
import com.azure.storage.common.SASProtocol;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SharedKeyCredential;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.core.implementation.util.FluxUtil.withContext;
import static com.azure.storage.blob.implementation.PostProcessor.postProcessResponse;

/**
 * Client to a container. It may only be instantiated through a {@link BlobContainerClientBuilder} or via the method
 * {@link BlobServiceAsyncClient#getBlobContainerAsyncClient(String)}. This class does not hold any state about a
 * particular blob but is instead a convenient way of sending off appropriate requests to the resource on the service.
 * It may also be used to construct URLs to blobs.
 *
 * <p>
 * This client contains operations on a container. Operations on a blob are available on {@link BlobAsyncClient} through
 * {@link #getBlobAsyncClient(String)}, and operations on the service are available on {@link BlobServiceAsyncClient}.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction>Azure
 * Docs</a> for more information on containers.
 *
 * <p>
 * Note this client is an async client that returns reactive responses from Spring Reactor Core project
 * (https://projectreactor.io/). Calling the methods in this client will <strong>NOT</strong> start the actual network
 * operation, until {@code .subscribe()} is called on the reactive response. You can simply convert one of these
 * responses to a {@link java.util.concurrent.CompletableFuture} object through {@link Mono#toFuture()}.
 */
@ServiceClient(builder = BlobContainerClientBuilder.class, isAsync = true)
public final class BlobContainerAsyncClient {
    public static final String ROOT_CONTAINER_NAME = "$root";

    public static final String STATIC_WEBSITE_CONTAINER_NAME = "$web";

    public static final String LOG_CONTAINER_NAME = "$logs";

    private final ClientLogger logger = new ClientLogger(BlobContainerAsyncClient.class);
    private final AzureBlobStorageImpl azureBlobStorage;
    private final CpkInfo customerProvidedKey; // only used to pass down to blob clients
    private final String accountName;

    /**
     * Package-private constructor for use by {@link BlobContainerClientBuilder}.
     *
     * @param azureBlobStorage the API client for blob storage
     */
    BlobContainerAsyncClient(AzureBlobStorageImpl azureBlobStorage, CpkInfo cpk, String accountName) {
        this.azureBlobStorage = azureBlobStorage;
        this.customerProvidedKey = cpk;
        this.accountName = accountName;
    }

    /**
     * Creates a new BlobAsyncClient object by concatenating blobName to the end of ContainerAsyncClient's URL. The new
     * BlobAsyncClient uses the same request policy pipeline as the ContainerAsyncClient. To change the pipeline, create
     * the BlobAsyncClient and then call its WithPipeline method passing in the desired pipeline object. Or, call this
     * package's getBlobAsyncClient instead of calling this object's getBlobAsyncClient method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.getBlobAsyncClient#String}
     *
     * @param blobName A {@code String} representing the name of the blob.
     * @return A new {@link BlobAsyncClient} object which references the blob with the specified name in this container.
     */
    public BlobAsyncClient getBlobAsyncClient(String blobName) {
        return getBlobAsyncClient(blobName, null);
    }

    /**
     * Creates a new BlobAsyncClient object by concatenating blobName to the end of ContainerAsyncClient's URL. The new
     * BlobAsyncClient uses the same request policy pipeline as the ContainerAsyncClient. To change the pipeline, create
     * the BlobAsyncClient and then call its WithPipeline method passing in the desired pipeline object. Or, call this
     * package's getBlobAsyncClient instead of calling this object's getBlobAsyncClient method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.getBlobAsyncClient#String-String}
     *
     * @param blobName A {@code String} representing the name of the blob.
     * @param snapshot the snapshot identifier for the blob.
     * @return A new {@link BlobAsyncClient} object which references the blob with the specified name in this container.
     */
    public BlobAsyncClient getBlobAsyncClient(String blobName, String snapshot) {
        return new BlobAsyncClient(new AzureBlobStorageBuilder()
            .url(Utility.appendToURLPath(getBlobContainerUrl(), blobName).toString())
            .pipeline(azureBlobStorage.getHttpPipeline())
            .build(), snapshot, customerProvidedKey, accountName);
    }

    /**
     * Gets the URL of the container represented by this client.
     *
     * @return the URL.
     */
    public String getBlobContainerUrl() {
        return azureBlobStorage.getUrl();
    }

    /**
     * Get the container name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.getBlobContainerName}
     *
     * @return The name of container.
     */
    public String getBlobContainerName() {
        return BlobURLParts.parse(this.azureBlobStorage.getUrl(), logger).getBlobContainerName();
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.accountName;
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
     * Gets the {@link CpkInfo} associated with this client that will be passed to
     * {@link BlobAsyncClient BlobAsyncClients} when {@link #getBlobAsyncClient(String) getBlobAsyncClient} is called.
     *
     * @return the customer provided key used for encryption.
     */
    public CpkInfo getCustomerProvidedKey() {
        return customerProvidedKey;
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.exists}
     *
     * @return true if the container exists, false if it doesn't
     */
    public Mono<Boolean> exists() {
        return existsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.existsWithResponse}
     *
     * @return true if the container exists, false if it doesn't
     */
    public Mono<Response<Boolean>> existsWithResponse() {
        return withContext(this::existsWithResponse);
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     *
     * @return true if the container exists, false if it doesn't
     */
    Mono<Response<Boolean>> existsWithResponse(Context context) {
        return this.getPropertiesWithResponse(null, context)
            .map(cp -> (Response<Boolean>) new SimpleResponse<>(cp, true))
            .onErrorResume(t -> t instanceof StorageException && ((StorageException) t).getStatusCode() == 404, t -> {
                HttpResponse response = ((StorageException) t).getResponse();
                return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), false));
            });
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.create}
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Void> create() {
        return createWithResponse(null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.createWithResponse#Map-PublicAccessType}
     *
     * @param metadata Metadata to associate with the container.
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> createWithResponse(Map<String, String> metadata, PublicAccessType accessType) {
        return withContext(context -> createWithResponse(metadata, accessType, context));
    }

    Mono<Response<Void>> createWithResponse(Map<String, String> metadata, PublicAccessType accessType,
        Context context) {
        return postProcessResponse(this.azureBlobStorage.containers().createWithRestResponseAsync(
            null, null, metadata, accessType, null, context))
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later deleted
     * during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.delete}
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Void> delete() {
        return deleteWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later deleted
     * during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.deleteWithResponse#BlobContainerAccessConditions}
     *
     * @param accessConditions {@link BlobContainerAccessConditions}
     * @return A reactive response signalling completion.
     * @throws UnsupportedOperationException If {@link BlobContainerAccessConditions#getModifiedAccessConditions()} has
     * either {@link ModifiedAccessConditions#getIfMatch()} or {@link ModifiedAccessConditions#getIfNoneMatch()} set.
     */
    public Mono<Response<Void>> deleteWithResponse(BlobContainerAccessConditions accessConditions) {
        return withContext(context -> deleteWithResponse(accessConditions, context));
    }

    Mono<Response<Void>> deleteWithResponse(BlobContainerAccessConditions accessConditions, Context context) {
        accessConditions = accessConditions == null ? new BlobContainerAccessConditions() : accessConditions;

        if (!validateNoEtag(accessConditions.getModifiedAccessConditions())) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(
                new UnsupportedOperationException("ETag access conditions are not supported for this API."));
        }

        return postProcessResponse(this.azureBlobStorage.containers().deleteWithRestResponseAsync(null, null, null,
            accessConditions.getLeaseAccessConditions(), accessConditions.getModifiedAccessConditions(), context))
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.getProperties}
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} containing the
     * container properties.
     */
    public Mono<BlobContainerProperties> getProperties() {
        return getPropertiesWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.getPropertiesWithResponse#LeaseAccessConditions}
     *
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the blob.
     * @return A reactive response containing the container properties.
     */
    public Mono<Response<BlobContainerProperties>> getPropertiesWithResponse(
        LeaseAccessConditions leaseAccessConditions) {
        return withContext(context -> getPropertiesWithResponse(leaseAccessConditions, context));
    }

    Mono<Response<BlobContainerProperties>> getPropertiesWithResponse(LeaseAccessConditions leaseAccessConditions,
        Context context) {
        return postProcessResponse(this.azureBlobStorage.containers()
            .getPropertiesWithRestResponseAsync(null, null, null, leaseAccessConditions, context))
            .map(rb -> new SimpleResponse<>(rb, new BlobContainerProperties(rb.getDeserializedHeaders())));
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.setMetadata#Map}
     *
     * @param metadata Metadata to associate with the container.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains signalling
     * completion.
     */
    public Mono<Void> setMetadata(Map<String, String> metadata) {
        return setMetadataWithResponse(metadata, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.setMetadataWithResponse#Map-BlobContainerAccessConditions}
     *
     * @param metadata Metadata to associate with the container.
     * @param accessConditions {@link BlobContainerAccessConditions}
     * @return A reactive response signalling completion.
     * @throws UnsupportedOperationException If {@link BlobContainerAccessConditions#getModifiedAccessConditions()} has
     * anything set other than {@link ModifiedAccessConditions#getIfModifiedSince()}.
     */
    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        BlobContainerAccessConditions accessConditions) {
        return withContext(context -> setMetadataWithResponse(metadata, accessConditions, context));
    }

    Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        BlobContainerAccessConditions accessConditions, Context context) {
        accessConditions = accessConditions == null ? new BlobContainerAccessConditions() : accessConditions;
        if (!validateNoEtag(accessConditions.getModifiedAccessConditions())
            || accessConditions.getModifiedAccessConditions().getIfUnmodifiedSince() != null) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(new UnsupportedOperationException(
                "If-Modified-Since is the only HTTP access condition supported for this API"));
        }

        return postProcessResponse(this.azureBlobStorage.containers().setMetadataWithRestResponseAsync(null, null,
            metadata, null, accessConditions.getLeaseAccessConditions(), accessConditions.getModifiedAccessConditions(),
            context)).map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.getAccessPolicy}
     *
     * @return A reactive response containing the container access policy.
     */
    public Mono<BlobContainerAccessPolicies> getAccessPolicy() {
        return getAccessPolicyWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.getAccessPolicyWithResponse#LeaseAccessConditions}
     *
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the blob.
     * @return A reactive response containing the container access policy.
     */
    public Mono<Response<BlobContainerAccessPolicies>> getAccessPolicyWithResponse(
        LeaseAccessConditions leaseAccessConditions) {
        return withContext(context -> getAccessPolicyWithResponse(leaseAccessConditions, context));
    }

    Mono<Response<BlobContainerAccessPolicies>> getAccessPolicyWithResponse(LeaseAccessConditions leaseAccessConditions,
        Context context) {
        return postProcessResponse(this.azureBlobStorage.containers().getAccessPolicyWithRestResponseAsync(null, null,
            null, leaseAccessConditions, context).map(response -> new SimpleResponse<>(response,
            new BlobContainerAccessPolicies(response.getDeserializedHeaders().getBlobPublicAccess(),
                response.getValue()))));
    }

    /**
     * Sets the container's permissions. The permissions indicate whether blobs in a container may be accessed publicly.
     * Note that, for each signed identifier, we will truncate the start and expiry times to the nearest second to
     * ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.setAccessPolicy#PublicAccessType-List}
     *
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link SignedIdentifier} objects that specify the permissions for the container.
     * Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     * @return A reactive response signalling completion.
     */
    public Mono<Void> setAccessPolicy(PublicAccessType accessType,
        List<SignedIdentifier> identifiers) {
        return setAccessPolicyWithResponse(accessType, identifiers, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets the container's permissions. The permissions indicate whether blobs in a container may be accessed publicly.
     * Note that, for each signed identifier, we will truncate the start and expiry times to the nearest second to
     * ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.setAccessPolicyWithResponse#PublicAccessType-List-BlobContainerAccessConditions}
     *
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link SignedIdentifier} objects that specify the permissions for the container.
     * Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     * @param accessConditions {@link BlobContainerAccessConditions}
     * @return A reactive response signalling completion.
     * @throws UnsupportedOperationException If {@link BlobContainerAccessConditions#getModifiedAccessConditions()} has
     * either {@link ModifiedAccessConditions#getIfMatch()} or {@link ModifiedAccessConditions#getIfNoneMatch()} set.
     */
    public Mono<Response<Void>> setAccessPolicyWithResponse(PublicAccessType accessType,
        List<SignedIdentifier> identifiers, BlobContainerAccessConditions accessConditions) {
        return withContext(context -> setAccessPolicyWithResponse(accessType, identifiers, accessConditions, context));
    }

    Mono<Response<Void>> setAccessPolicyWithResponse(PublicAccessType accessType, List<SignedIdentifier> identifiers,
        BlobContainerAccessConditions accessConditions, Context context) {
        accessConditions = accessConditions == null ? new BlobContainerAccessConditions() : accessConditions;

        if (!validateNoEtag(accessConditions.getModifiedAccessConditions())) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(
                new UnsupportedOperationException("ETag access conditions are not supported for this API."));
        }

        /*
        We truncate to seconds because the service only supports nanoseconds or seconds, but doing an
        OffsetDateTime.now will only give back milliseconds (more precise fields are zeroed and not serialized). This
        allows for proper serialization with no real detriment to users as sub-second precision on active time for
        signed identifiers is not really necessary.
         */
        if (identifiers != null) {
            for (SignedIdentifier identifier : identifiers) {
                if (identifier.getAccessPolicy() != null && identifier.getAccessPolicy().getStart() != null) {
                    identifier.getAccessPolicy().setStart(
                        identifier.getAccessPolicy().getStart().truncatedTo(ChronoUnit.SECONDS));
                }
                if (identifier.getAccessPolicy() != null && identifier.getAccessPolicy().getExpiry() != null) {
                    identifier.getAccessPolicy().setExpiry(
                        identifier.getAccessPolicy().getExpiry().truncatedTo(ChronoUnit.SECONDS));
                }
            }
        }

        return postProcessResponse(this.azureBlobStorage.containers().setAccessPolicyWithRestResponseAsync(null,
            identifiers, null, accessType, null, accessConditions.getLeaseAccessConditions(),
            accessConditions.getModifiedAccessConditions(), context))
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Returns a reactive Publisher emitting all the blobs in this container lazily as needed. The directories are
     * flattened and only actual blobs and no directories are returned.
     *
     * <p>
     * Blob names are returned in lexicographic order. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p>
     * E.g. listing a container containing a 'foo' folder, which contains blobs 'foo1' and 'foo2', and a blob on the
     * root level 'bar', will return
     *
     * <ul>
     * <li>foo/foo1
     * <li>foo/foo2
     * <li>bar
     * </ul>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.listBlobsFlat}
     *
     * @return A reactive response emitting the flattened blobs.
     */
    public PagedFlux<BlobItem> listBlobsFlat() {
        return this.listBlobsFlat(new ListBlobsOptions());
    }

    /**
     * Returns a reactive Publisher emitting all the blobs in this container lazily as needed. The directories are
     * flattened and only actual blobs and no directories are returned.
     *
     * <p>
     * Blob names are returned in lexicographic order. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p>
     * E.g. listing a container containing a 'foo' folder, which contains blobs 'foo1' and 'foo2', and a blob on the
     * root level 'bar', will return
     *
     * <ul>
     * <li>foo/foo1
     * <li>foo/foo2
     * <li>bar
     * </ul>
     *
     *
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.listBlobsFlat#ListBlobsOptions}
     *
     * @param options {@link ListBlobsOptions}
     * @return A reactive response emitting the listed blobs, flattened.
     */
    public PagedFlux<BlobItem> listBlobsFlat(ListBlobsOptions options) {
        return listBlobsFlatWithOptionalTimeout(options, null);
    }

    /*
     * Implementation for this paged listing operation, supporting an optional timeout provided by the synchronous
     * ContainerClient. Applies the given timeout to each Mono<ContainersListBlobFlatSegmentResponse> backing the
     * PagedFlux.
     *
     * @param options {@link ListBlobsOptions}.
     * @param timeout An optional timeout to be applied to the network asynchronous operations.
     * @return A reactive response emitting the listed blobs, flattened.
     */
    PagedFlux<BlobItem> listBlobsFlatWithOptionalTimeout(ListBlobsOptions options, Duration timeout) {
        Function<String, Mono<PagedResponse<BlobItem>>> func =
            marker -> listBlobsFlatSegment(marker, options, timeout)
                .map(response -> {
                    List<BlobItem> value = response.getValue().getSegment() == null
                        ? new ArrayList<>(0)
                        : response.getValue().getSegment().getBlobItems();

                    return new PagedResponseBase<>(
                        response.getRequest(),
                        response.getStatusCode(),
                        response.getHeaders(),
                        value,
                        response.getValue().getNextMarker(),
                        response.getDeserializedHeaders());
                });

        return new PagedFlux<>(() -> func.apply(null), func);
    }

    /*
     * Returns a single segment of blobs starting from the specified Marker. Use an empty
     * marker to start enumeration from the beginning. Blob names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListBlobs again (passing the the previously-returned
     * Marker) to get the next segment. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * @param marker
     *         Identifies the portion of the list to be returned with the next list operation.
     *         This value is returned in the response of a previous list operation as the
     *         ListBlobsFlatSegmentResponse.body().getNextMarker(). Set to null to list the first segment.
     * @param options
     *         {@link ListBlobsOptions}
     *
     * @return Emits the successful response.
     */
    private Mono<ContainersListBlobFlatSegmentResponse> listBlobsFlatSegment(String marker, ListBlobsOptions options,
        Duration timeout) {
        options = options == null ? new ListBlobsOptions() : options;

        return postProcessResponse(Utility.applyOptionalTimeout(
            this.azureBlobStorage.containers().listBlobFlatSegmentWithRestResponseAsync(null, options.getPrefix(),
                marker, options.getMaxResults(), options.getDetails().toList(), null, null, Context.NONE), timeout));
    }

    /**
     * Returns a reactive Publisher emitting all the blobs and directories (prefixes) under the given directory
     * (prefix). Directories will have {@link BlobItem#isPrefix()} set to true.
     *
     * <p>
     * Blob names are returned in lexicographic order. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p>
     * E.g. listing a container containing a 'foo' folder, which contains blobs 'foo1' and 'foo2', and a blob on the
     * root level 'bar', will return the following results when prefix=null:
     *
     * <ul>
     * <li>foo/ (isPrefix = true)
     * <li>bar (isPrefix = false)
     * </ul>
     * <p>
     * will return the following results when prefix="foo/":
     *
     * <ul>
     * <li>foo/foo1 (isPrefix = false)
     * <li>foo/foo2 (isPrefix = false)
     * </ul>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.listBlobsHierarchy#String}
     *
     * @param directory The directory to list blobs underneath
     * @return A reactive response emitting the prefixes and blobs.
     */
    public PagedFlux<BlobItem> listBlobsHierarchy(String directory) {
        return this.listBlobsHierarchy("/", new ListBlobsOptions().setPrefix(directory));
    }

    /**
     * Returns a reactive Publisher emitting all the blobs and prefixes (directories) under the given prefix
     * (directory). Directories will have {@link BlobItem#isPrefix()} set to true.
     *
     * <p>
     * Blob names are returned in lexicographic order. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p>
     * E.g. listing a container containing a 'foo' folder, which contains blobs 'foo1' and 'foo2', and a blob on the
     * root level 'bar', will return the following results when prefix=null:
     *
     * <ul>
     * <li>foo/ (isPrefix = true)
     * <li>bar (isPrefix = false)
     * </ul>
     * <p>
     * will return the following results when prefix="foo/":
     *
     * <ul>
     * <li>foo/foo1 (isPrefix = false)
     * <li>foo/foo2 (isPrefix = false)
     * </ul>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.listBlobsHierarchy#String-ListBlobsOptions}
     *
     * @param delimiter The delimiter for blob hierarchy, "/" for hierarchy based on directories
     * @param options {@link ListBlobsOptions}
     * @return A reactive response emitting the prefixes and blobs.
     */
    public PagedFlux<BlobItem> listBlobsHierarchy(String delimiter, ListBlobsOptions options) {
        return listBlobsHierarchyWithOptionalTimeout(delimiter, options, null);
    }

    /*
     * Implementation for this paged listing operation, supporting an optional timeout provided by the synchronous
     * ContainerClient. Applies the given timeout to each Mono<ContainersListBlobHierarchySegmentResponse> backing the
     * PagedFlux.
     *
     * @param delimiter The delimiter for blob hierarchy, "/" for hierarchy based on directories
     * @param options {@link ListBlobsOptions}
     * @param timeout An optional timeout to be applied to the network asynchronous operations.
     * @return A reactive response emitting the listed blobs, flattened.
     */
    PagedFlux<BlobItem> listBlobsHierarchyWithOptionalTimeout(String delimiter, ListBlobsOptions options,
        Duration timeout) {
        Function<String, Mono<PagedResponse<BlobItem>>> func =
            marker -> listBlobsHierarchySegment(marker, delimiter, options, timeout)
                .map(response -> {
                    List<BlobItem> value = response.getValue().getSegment() == null
                        ? new ArrayList<>(0)
                        : Stream.concat(
                        response.getValue().getSegment().getBlobItems().stream(),
                        response.getValue().getSegment().getBlobPrefixes().stream()
                            .map(blobPrefix -> new BlobItem().setName(blobPrefix.getName()).setIsPrefix(true))
                    ).collect(Collectors.toList());

                    return new PagedResponseBase<>(
                        response.getRequest(),
                        response.getStatusCode(),
                        response.getHeaders(),
                        value,
                        response.getValue().getNextMarker(),
                        response.getDeserializedHeaders());
                });

        return new PagedFlux<>(() -> func.apply(null), func);
    }

    private Mono<ContainersListBlobHierarchySegmentResponse> listBlobsHierarchySegment(String marker, String delimiter,
        ListBlobsOptions options, Duration timeout) {
        options = options == null ? new ListBlobsOptions() : options;
        if (options.getDetails().getRetrieveSnapshots()) {
            throw logger.logExceptionAsError(
                new UnsupportedOperationException("Including snapshots in a hierarchical listing is not supported."));
        }

        return postProcessResponse(Utility.applyOptionalTimeout(
            this.azureBlobStorage.containers().listBlobHierarchySegmentWithRestResponseAsync(null, delimiter,
                options.getPrefix(), marker, options.getMaxResults(), options.getDetails().toList(), null, null,
                Context.NONE),
            timeout));
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.getAccountInfo}
     *
     * @return A reactive response containing the account info.
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
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.getAccountInfoWithResponse}
     *
     * @return A reactive response containing the account info.
     */
    public Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse() {
        return withContext(this::getAccountInfoWithResponse);
    }

    Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse(Context context) {
        return postProcessResponse(
            this.azureBlobStorage.containers().getAccountInfoWithRestResponseAsync(null, context))
            .map(rb -> new SimpleResponse<>(rb, new StorageAccountInfo(rb.getDeserializedHeaders())));
    }


    private boolean validateNoEtag(ModifiedAccessConditions modifiedAccessConditions) {
        if (modifiedAccessConditions == null) {
            return true;
        }
        return modifiedAccessConditions.getIfMatch() == null && modifiedAccessConditions.getIfNoneMatch() == null;
    }

    /**
     * Generates a user delegation SAS with the specified parameters
     *
     * @param userDelegationKey The {@code UserDelegationKey} user delegation key for the SAS
     * @param accountName The {@code String} account name for the SAS
     * @param permissions The {@code BlobContainerSasPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @return A string that represents the SAS token
     */
    public String generateUserDelegationSAS(UserDelegationKey userDelegationKey, String accountName,
        BlobContainerSasPermission permissions, OffsetDateTime expiryTime) {
        return this.generateUserDelegationSAS(userDelegationKey, accountName, permissions, expiryTime, null, null,
            null, null, null, null, null, null, null);
    }

    /**
     * Generates a user delegation SAS token with the specified parameters
     *
     * @param userDelegationKey The {@code UserDelegationKey} user delegation key for the SAS
     * @param accountName The {@code String} account name for the SAS
     * @param permissions The {@code BlobContainerSasPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @param startTime An optional {@code OffsetDateTime} start time for the SAS
     * @param version An optional {@code String} version for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @param ipRange An optional {@code IpRange} ip address range for the SAS
     * @return A string that represents the SAS token
     */
    public String generateUserDelegationSAS(UserDelegationKey userDelegationKey, String accountName,
        BlobContainerSasPermission permissions, OffsetDateTime expiryTime, OffsetDateTime startTime, String version,
        SASProtocol sasProtocol, IpRange ipRange) {
        return this.generateUserDelegationSAS(userDelegationKey, accountName, permissions, expiryTime, startTime,
            version, sasProtocol, ipRange, null /* cacheControl */, null /* contentDisposition */, null /*
            contentEncoding */, null /* contentLanguage */, null /* contentType */);
    }

    /**
     * Generates a user delegation SAS token with the specified parameters
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.generateUserDelegationSAS#UserDelegationKey-String-BlobContainerSasPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IpRange-String-String-String-String-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-user-delegation-sas">Azure
     * Docs</a></p>
     *
     * @param userDelegationKey The {@code UserDelegationKey} user delegation key for the SAS
     * @param accountName The {@code String} account name for the SAS
     * @param permissions The {@code BlobContainerSasPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @param startTime An optional {@code OffsetDateTime} start time for the SAS
     * @param version An optional {@code String} version for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @param ipRange An optional {@code IpRange} ip address range for the SAS
     * @param cacheControl An optional {@code String} cache-control header for the SAS.
     * @param contentDisposition An optional {@code String} content-disposition header for the SAS.
     * @param contentEncoding An optional {@code String} content-encoding header for the SAS.
     * @param contentLanguage An optional {@code String} content-language header for the SAS.
     * @param contentType An optional {@code String} content-type header for the SAS.
     * @return A string that represents the SAS token
     */
    public String generateUserDelegationSAS(UserDelegationKey userDelegationKey, String accountName,
            BlobContainerSasPermission permissions, OffsetDateTime expiryTime, OffsetDateTime startTime, String version,
            SASProtocol sasProtocol, IpRange ipRange, String cacheControl, String contentDisposition,
            String contentEncoding, String contentLanguage, String contentType) {
        BlobServiceSasSignatureValues blobServiceSASSignatureValues = new BlobServiceSasSignatureValues(version,
            sasProtocol, startTime, expiryTime, permissions == null ? null : permissions.toString(), ipRange,
            null /* identifier*/, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType);

        BlobServiceSasSignatureValues values =
            configureServiceSASSignatureValues(blobServiceSASSignatureValues, accountName);

        BlobServiceSasQueryParameters blobServiceSasQueryParameters =
            values.generateSASQueryParameters(userDelegationKey);

        return blobServiceSasQueryParameters.encode();
    }

    /**
     * Generates a SAS token with the specified parameters
     *
     * @param permissions The {@code BlobContainerSasPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @return A string that represents the SAS token
     */
    public String generateSAS(BlobContainerSasPermission permissions, OffsetDateTime expiryTime) {
        return this.generateSAS(null, permissions,  /* identifier */  expiryTime, null /* startTime */, null /* version
             */, null /* sasProtocol */, null /* ipRange */, null /* cacheControl */, null /* contentDisposition */,
            null /* contentEncoding */, null /* contentLanguage */, null /*contentType*/);
    }

    /**
     * Generates a SAS token with the specified parameters
     *
     * @param identifier The {@code String} name of the access policy on the container this SAS references if any
     * @return A string that represents the SAS token
     */
    public String generateSAS(String identifier) {
        return this.generateSAS(identifier, null /* permissions*/, null /* expiryTime */, null /* startTime */, null
            /* version */, null /* sasProtocol */, null /* ipRange */, null /* cacheControl */, null /*
            contentDisposition */, null /* contentEncoding */, null /* contentLanguage */, null /*contentType*/);
    }

    /**
     * Generates a SAS token with the specified parameters
     *
     * @param identifier The {@code String} name of the access policy on the container this SAS references if any
     * @param permissions The {@code BlobContainerSasPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @param startTime An optional {@code OffsetDateTime} start time for the SAS
     * @param version An optional {@code String} version for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @param ipRange An optional {@code IpRange} ip address range for the SAS
     * @return A string that represents the SAS token
     */
    public String generateSAS(String identifier, BlobContainerSasPermission permissions, OffsetDateTime expiryTime,
        OffsetDateTime startTime, String version, SASProtocol sasProtocol, IpRange ipRange) {
        return this.generateSAS(identifier, permissions, expiryTime, startTime, version, sasProtocol, ipRange, null
            /* cacheControl */, null /* contentDisposition */, null /* contentEncoding */, null /* contentLanguage */,
            null /*contentType*/);
    }

    /**
     * Generates a SAS token with the specified parameters
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerAsyncClient.generateSAS#String-BlobContainerSasPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IpRange-String-String-String-String-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-service-sas">Azure Docs</a></p>
     *
     * @param identifier The {@code String} name of the access policy on the container this SAS references if any
     * @param permissions The {@code BlobContainerSasPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @param startTime An optional {@code OffsetDateTime} start time for the SAS
     * @param version An optional {@code String} version for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @param ipRange An optional {@code IpRange} ip address range for the SAS
     * @param cacheControl An optional {@code String} cache-control header for the SAS.
     * @param contentDisposition An optional {@code String} content-disposition header for the SAS.
     * @param contentEncoding An optional {@code String} content-encoding header for the SAS.
     * @param contentLanguage An optional {@code String} content-language header for the SAS.
     * @param contentType An optional {@code String} content-type header for the SAS.
     * @return A string that represents the SAS token
     */
    public String generateSAS(String identifier, BlobContainerSasPermission permissions, OffsetDateTime expiryTime,
            OffsetDateTime startTime, String version, SASProtocol sasProtocol, IpRange ipRange, String cacheControl,
            String contentDisposition, String contentEncoding, String contentLanguage, String contentType) {
        BlobServiceSasSignatureValues blobServiceSASSignatureValues = new BlobServiceSasSignatureValues(version,
            sasProtocol, startTime, expiryTime, permissions == null ? null : permissions.toString(), ipRange,
            identifier, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType);

        SharedKeyCredential sharedKeyCredential =
            Utility.getSharedKeyCredential(this.azureBlobStorage.getHttpPipeline());

        Utility.assertNotNull("sharedKeyCredential", sharedKeyCredential);

        BlobServiceSasSignatureValues values = configureServiceSASSignatureValues(blobServiceSASSignatureValues,
            sharedKeyCredential.getAccountName());

        BlobServiceSasQueryParameters blobServiceSasQueryParameters =
            values.generateSASQueryParameters(sharedKeyCredential);

        return blobServiceSasQueryParameters.encode();
    }

    /**
     * Sets blobServiceSASSignatureValues parameters dependent on the current blob type
     */
    private BlobServiceSasSignatureValues configureServiceSASSignatureValues(
        BlobServiceSasSignatureValues blobServiceSASSignatureValues, String accountName) {
        // Set canonical name
        blobServiceSASSignatureValues.setCanonicalName(this.azureBlobStorage.getUrl(), accountName);

        // Set snapshotId to null
        blobServiceSASSignatureValues.setSnapshotId(null);

        // Set resource
        blobServiceSASSignatureValues.setResource(Constants.UrlConstants.SAS_CONTAINER_CONSTANT);
        return blobServiceSASSignatureValues;
    }
}
