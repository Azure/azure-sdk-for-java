// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.models.BlobFlatListSegment;
import com.azure.storage.blob.models.BlobHierarchyListSegment;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobPrefix;
import com.azure.storage.blob.models.ContainerGetAccessPolicyHeaders;
import com.azure.storage.blob.models.ContainersListBlobFlatSegmentResponse;
import com.azure.storage.blob.models.ContainersListBlobHierarchySegmentResponse;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.ListBlobsFlatSegmentResponse;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.SignedIdentifier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Client to a container. It may only be instantiated through a {@link ContainerClientBuilder} or via the method
 * {@link StorageAsyncClient#getContainerAsyncClient(String)}. This class does not hold any
 * state about a particular blob but is instead a convenient way of sending off appropriate requests to
 * the resource on the service. It may also be used to construct URLs to blobs.
 *
 * <p>
 * This client contains operations on a container. Operations on a blob are available on {@link BlobAsyncClient} through
 * {@link #getBlobAsyncClient(String)}, and operations on the service are available on {@link StorageAsyncClient}.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction>Azure Docs</a>
 * for more information on containers.
 *
 * <p>
 * Note this client is an async client that returns reactive responses from Spring Reactor Core
 * project (https://projectreactor.io/). Calling the methods in this client will <strong>NOT</strong>
 * start the actual network operation, until {@code .subscribe()} is called on the reactive response.
 * You can simply convert one of these responses to a {@link java.util.concurrent.CompletableFuture}
 * object through {@link Mono#toFuture()}.
 */
public final class ContainerAsyncClient {

    ContainerAsyncRawClient containerAsyncRawClient;
    private AzureBlobStorageBuilder azureBlobStorageBuilder;

    public static final String ROOT_CONTAINER_NAME = "$root";

    public static final String STATIC_WEBSITE_CONTAINER_NAME = "$web";

    public static final String LOG_CONTAINER_NAME = "$logs";

    /**
     * Package-private constructor for use by {@link ContainerClientBuilder}.
     * @param azureBlobStorageBuilder the API client builder for blob storage API
     */
    ContainerAsyncClient(AzureBlobStorageBuilder azureBlobStorageBuilder) {
        this.azureBlobStorageBuilder = azureBlobStorageBuilder;
        this.containerAsyncRawClient = new ContainerAsyncRawClient(azureBlobStorageBuilder.build());
    }

    /**
     * @return a new client {@link ContainerClientBuilder} instance.
     */
    public static ContainerClientBuilder containerClientBuilder() {
        return new ContainerClientBuilder();
    }

    /**
     * Creates a new {@link BlockBlobAsyncClient} object by concatenating the blobName to the end of
     * ContainerAsyncClient's URL. The new BlockBlobAsyncClient uses the same request policy pipeline as the ContainerAsyncClient.
     * To change the pipeline, create the BlockBlobAsyncClient and then call its WithPipeline method passing in the
     * desired pipeline object. Or, call this package's NewBlockBlobAsyncClient instead of calling this object's
     * NewBlockBlobAsyncClient method.
     *
     * @param blobName
     *         A {@code String} representing the name of the blob.
     *
     * @return A new {@link BlockBlobAsyncClient} object which references the blob with the specified name in this container.
     */
    public BlockBlobAsyncClient getBlockBlobAsyncClient(String blobName) {
        return new BlockBlobAsyncClient(new AzureBlobStorageBuilder()
            .url(Utility.appendToURLPath(getUrl(), blobName).toString())
            .pipeline(containerAsyncRawClient.azureBlobStorage.httpPipeline()));
    }

    /**
     * Creates creates a new PageBlobAsyncClient object by concatenating blobName to the end of
     * ContainerAsyncClient's URL. The new PageBlobAsyncClient uses the same request policy pipeline as the ContainerAsyncClient.
     * To change the pipeline, create the PageBlobAsyncClient and then call its WithPipeline method passing in the
     * desired pipeline object. Or, call this package's NewPageBlobAsyncClient instead of calling this object's
     * NewPageBlobAsyncClient method.
     *
     * @param blobName
     *         A {@code String} representing the name of the blob.
     *
     * @return A new {@link PageBlobAsyncClient} object which references the blob with the specified name in this container.
     */
    public PageBlobAsyncClient getPageBlobAsyncClient(String blobName) {
        return new PageBlobAsyncClient(new AzureBlobStorageBuilder()
            .url(Utility.appendToURLPath(getUrl(), blobName).toString())
            .pipeline(containerAsyncRawClient.azureBlobStorage.httpPipeline()));
    }

    /**
     * Creates creates a new AppendBlobAsyncClient object by concatenating blobName to the end of
     * ContainerAsyncClient's URL. The new AppendBlobAsyncClient uses the same request policy pipeline as the ContainerAsyncClient.
     * To change the pipeline, create the AppendBlobAsyncClient and then call its WithPipeline method passing in the
     * desired pipeline object. Or, call this package's NewAppendBlobAsyncClient instead of calling this object's
     * NewAppendBlobAsyncClient method.
     *
     * @param blobName
     *         A {@code String} representing the name of the blob.
     *
     * @return A new {@link AppendBlobAsyncClient} object which references the blob with the specified name in this container.
     */
    public AppendBlobAsyncClient getAppendBlobAsyncClient(String blobName) {
        return new AppendBlobAsyncClient(new AzureBlobStorageBuilder()
            .url(Utility.appendToURLPath(getUrl(), blobName).toString())
            .pipeline(containerAsyncRawClient.azureBlobStorage.httpPipeline()));
    }

    /**
     * Creates a new BlobAsyncClient object by concatenating blobName to the end of
     * ContainerAsyncClient's URL. The new BlobAsyncClient uses the same request policy pipeline as the ContainerAsyncClient.
     * To change the pipeline, create the BlobAsyncClient and then call its WithPipeline method passing in the
     * desired pipeline object. Or, call this package's getBlobAsyncClient instead of calling this object's
     * getBlobAsyncClient method.
     *
     * @param blobName
     *         A {@code String} representing the name of the blob.
     *
     * @return A new {@link BlobAsyncClient} object which references the blob with the specified name in this container.
     */
    public BlobAsyncClient getBlobAsyncClient(String blobName) {
        return new BlobAsyncClient(new AzureBlobStorageBuilder()
            .url(Utility.appendToURLPath(getUrl(), blobName).toString())
            .pipeline(containerAsyncRawClient.azureBlobStorage.httpPipeline()));
    }

    /**
     * Initializes a {@link StorageAsyncClient} object pointing to the storage account this container is in.
     *
     * @return
     *     A {@link StorageAsyncClient} object pointing to the specified storage account
     */
    public StorageAsyncClient getStorageAsyncClient() {
        return new StorageAsyncClient(new AzureBlobStorageBuilder()
            .url(Utility.stripLastPathSegment(getUrl()).toString())
            .pipeline(containerAsyncRawClient.azureBlobStorage.httpPipeline()));
    }

    /**
     * Gets the URL of the container represented by this client.
     * @return the URL.
     */
    public URL getUrl() {
        try {
            return new URL(containerAsyncRawClient.azureBlobStorage.url());
        } catch (MalformedURLException e) {
            throw new RuntimeException(String.format("Invalid URL on %s: %s" + getClass().getSimpleName(), containerAsyncRawClient.azureBlobStorage.url()), e);
        }
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     *
     * @return
     *         true if the container exists, false if it doesn't
     */
    public Mono<Response<Boolean>> exists() {
        return this.exists(null);
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     *
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     * @return
     *         true if the container exists, false if it doesn't
     */
    public Mono<Response<Boolean>> exists(Context context) {
        return this.getProperties(null, context)
            .map(cp -> (Response<Boolean>) new SimpleResponse<>(cp, true))
            .onErrorResume(t -> t instanceof StorageException && ((StorageException) t).statusCode() == 404, t -> {
                HttpResponse response = ((StorageException) t).response();
                return Mono.just(new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), false));
            });
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> create() {
        return this.create(null, null, null);
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * @param metadata
     *         {@link Metadata}
     * @param accessType
     *         Specifies how the data in this container is available to the public. See the x-ms-blob-public-access header
     *         in the Azure Docs for more information. Pass null for no public access.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> create(Metadata metadata, PublicAccessType accessType, Context context) {
        return containerAsyncRawClient
            .create(metadata, accessType, context)
            .map(VoidResponse::new);
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later
     * deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> delete() {
        return this.delete(null, null);
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later
     * deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * @param accessConditions
     *         {@link ContainerAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> delete(ContainerAccessConditions accessConditions, Context context) {
        return containerAsyncRawClient
            .delete(accessConditions, context)
            .map(VoidResponse::new);
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * @return
     *      A reactive response containing the container properties.
     */
    public Mono<Response<ContainerProperties>> getProperties() {
        return this.getProperties(null, null);
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * @param leaseAccessConditions
     *         By setting lease access conditions, requests will fail if the provided lease does not match the active
     *         lease on the blob.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return
     *      A reactive response containing the container properties.
     */
    public Mono<Response<ContainerProperties>> getProperties(LeaseAccessConditions leaseAccessConditions,
            Context context) {
        return containerAsyncRawClient
            .getProperties(leaseAccessConditions, context)
            .map(rb -> new SimpleResponse<>(rb, new ContainerProperties(rb.deserializedHeaders())));
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * @param metadata
     *         {@link Metadata}
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> setMetadata(Metadata metadata) {
        return this.setMetadata(metadata, null, null);
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link ContainerAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> setMetadata(Metadata metadata,
            ContainerAccessConditions accessConditions, Context context) {
        return containerAsyncRawClient
            .setMetadata(metadata, accessConditions, context)
            .map(VoidResponse::new);
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * @return
     *      A reactive response containing the container access policy.
     */
    public Mono<Response<PublicAccessType>> getAccessPolicy() {
        return this.getAccessPolicy(null, null);
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * @param leaseAccessConditions
     *         By setting lease access conditions, requests will fail if the provided lease does not match the active
     *         lease on the blob.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return
     *      A reactive response containing the container access policy.
     */
    public Mono<Response<PublicAccessType>> getAccessPolicy(LeaseAccessConditions leaseAccessConditions,
            Context context) {
        return containerAsyncRawClient
            .getAccessPolicy(leaseAccessConditions, context)
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().blobPublicAccess()));
    }

    /**
     * Sets the container's permissions. The permissions indicate whether blobs in a container may be accessed publicly.
     * Note that, for each signed identifier, we will truncate the start and expiry times to the nearest second to
     * ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * @param accessType
     *         Specifies how the data in this container is available to the public. See the x-ms-blob-public-access header
     *         in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers
     *         A list of {@link SignedIdentifier} objects that specify the permissions for the container. Please see
     *         <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     *         for more information. Passing null will clear all access policies.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> setAccessPolicy(PublicAccessType accessType,
            List<SignedIdentifier> identifiers) {
        return this.setAccessPolicy(accessType, identifiers, null, null);
    }

    /**
     * Sets the container's permissions. The permissions indicate whether blobs in a container may be accessed publicly.
     * Note that, for each signed identifier, we will truncate the start and expiry times to the nearest second to
     * ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * @param accessType
     *         Specifies how the data in this container is available to the public. See the x-ms-blob-public-access header
     *         in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers
     *         A list of {@link SignedIdentifier} objects that specify the permissions for the container. Please see
     *         <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     *         for more information. Passing null will clear all access policies.
     * @param accessConditions
     *         {@link ContainerAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> setAccessPolicy(PublicAccessType accessType,
                                      List<SignedIdentifier> identifiers, ContainerAccessConditions accessConditions, Context context) {
        return containerAsyncRawClient
            .setAccessPolicy(accessType, identifiers, accessConditions, context)
            .map(VoidResponse::new);
    }

    // TODO: figure out if this is meant to stay private or change to public
    private boolean validateNoEtag(ModifiedAccessConditions modifiedAccessConditions) {
        if (modifiedAccessConditions == null) {
            return true;
        }
        return modifiedAccessConditions.ifMatch() == null && modifiedAccessConditions.ifNoneMatch() == null;
    }


    /**
     * Returns a reactive Publisher emitting all the blobs in this container lazily as needed.
     * The directories are flattened and only actual blobs and no directories are returned.
     *
     * <p>
     * Blob names are returned in lexicographic order. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p>
     * E.g. listing a container containing a 'foo' folder, which contains blobs 'foo1' and 'foo2', and a blob
     * on the root level 'bar', will return
     * <p><ul>
     *     <li>foo/foo1
     *     <li>foo/foo2
     *     <li>bar
     * </ul>
     *
     * @return
     *      A reactive response emitting the flattened blobs.
     */
    public Flux<BlobItem> listBlobsFlat() {
        return this.listBlobsFlat(new ListBlobsOptions(), null);
    }

    /**
     * Returns a reactive Publisher emitting all the blobs in this container lazily as needed.
     * The directories are flattened and only actual blobs and no directories are returned.
     *
     * <p>
     * Blob names are returned in lexicographic order. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p>
     * E.g. listing a container containing a 'foo' folder, which contains blobs 'foo1' and 'foo2', and a blob
     * on the root level 'bar', will return
     * <p><ul>
     *     <li>foo/foo1
     *     <li>foo/foo2
     *     <li>bar
     * </ul>
     *
     * @param options
     *         {@link ListBlobsOptions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return
     *      A reactive response emitting the listed blobs, flattened.
     */
    public Flux<BlobItem> listBlobsFlat(ListBlobsOptions options, Context context) {
        return containerAsyncRawClient
            .listBlobsFlatSegment(null, options, context)
            .flatMapMany(response -> listBlobsFlatHelper(options, context, response));
    }

    private Flux<BlobItem> listBlobsFlatHelper(ListBlobsOptions options,
                                               Context context, ContainersListBlobFlatSegmentResponse response){
        Flux<BlobItem> result;
        BlobFlatListSegment segment = response.value().segment();
        if (segment != null && segment.blobItems() != null) {
            result = Flux.fromIterable(segment.blobItems());
        } else {
            result = Flux.empty();
        }

        if (response.value().nextMarker() != null) {
            // Recursively add the continuation items to the observable.
            result = result.concatWith(containerAsyncRawClient.listBlobsFlatSegment(response.value().nextMarker(), options, context)
                .flatMapMany(r -> listBlobsFlatHelper(options, context, r)));
        }

        return result;
    }

    /**
     * Returns a reactive Publisher emitting all the blobs and prefixes (directories) under
     * the given prefix (directory). Directories will have {@link BlobItem#isPrefix()} set to
     * true.
     *
     * <p>
     * Blob names are returned in lexicographic order. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p>
     * E.g. listing a container containing a 'foo' folder, which contains blobs 'foo1' and 'foo2', and a blob
     * on the root level 'bar', will return the following results when prefix=null:
     * <p><ul>
     *     <li>foo/ (isPrefix = true)
     *     <li>bar (isPrefix = false)
     * </ul>
     * <p>
     * will return the following results when prefix="foo/":
     * <p><ul>
     *     <li>foo/foo1 (isPrefix = false)
     *     <li>foo/foo2 (isPrefix = false)
     * </ul>
     *
     * @return
     *      A reactive response emitting the prefixes and blobs.
     */
    public Flux<BlobItem> listBlobsHierarchy(String prefix) {
        return this.listBlobsHierarchy("/", new ListBlobsOptions().withPrefix(prefix), null);
    }

    /**
     * Returns a reactive Publisher emitting all the blobs and prefixes (directories) under
     * the given prefix (directory). Directories will have {@link BlobItem#isPrefix()} set to
     * true.
     *
     * <p>
     * Blob names are returned in lexicographic order. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p>
     * E.g. listing a container containing a 'foo' folder, which contains blobs 'foo1' and 'foo2', and a blob
     * on the root level 'bar', will return the following results when prefix=null:
     * <p><ul>
     *     <li>foo/ (isPrefix = true)
     *     <li>bar (isPrefix = false)
     * </ul>
     * <p>
     * will return the following results when prefix="foo/":
     * <p><ul>
     *     <li>foo/foo1 (isPrefix = false)
     *     <li>foo/foo2 (isPrefix = false)
     * </ul>
     *
     * @param options
     *         {@link ListBlobsOptions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return
     *      A reactive response emitting the prefixes and blobs.
     */
    public Flux<BlobItem> listBlobsHierarchy(String delimiter, ListBlobsOptions options, Context context) {
        return containerAsyncRawClient.listBlobsHierarchySegment(null, delimiter, options, context)
            .flatMapMany(response -> listBlobsHierarchyHelper(delimiter, options, context, response));
    }

    private Flux<BlobItem> listBlobsHierarchyHelper(String delimiter, ListBlobsOptions options,
                                               Context context, ContainersListBlobHierarchySegmentResponse response){
        Flux<BlobItem> blobs;
        Flux<BlobPrefix> prefixes;
        BlobHierarchyListSegment segment = response.value().segment();
        if (segment != null && segment.blobItems() != null) {
            blobs = Flux.fromIterable(segment.blobItems());
        } else {
            blobs = Flux.empty();
        }
        if (segment != null && segment.blobItems() != null) {
            prefixes = Flux.fromIterable(segment.blobPrefixes());
        } else {
            prefixes = Flux.empty();
        }
        Flux<BlobItem> result = blobs.concatWith(prefixes.map(prefix -> new BlobItem().name(prefix.name()).isPrefix(true)));

        if (response.value().nextMarker() != null) {
            // Recursively add the continuation items to the observable.
            result = result.concatWith(containerAsyncRawClient.listBlobsHierarchySegment(response.value().nextMarker(), delimiter, options, context)
                .flatMapMany(r -> listBlobsHierarchyHelper(delimiter, options, context, r)));
        }

        return result;
    }

    /**
     * Returns a single segment of blobs and blob prefixes starting from the specified Marker. Use an empty
     * marker to start enumeration from the beginning. Blob names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListBlobs again (passing the the previously-returned
     * Marker) to get the next segment. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * @param marker
     *         Identifies the portion of the list to be returned with the next list operation.
     *         This value is returned in the response of a previous list operation as the
     *         ListBlobsHierarchySegmentResponse.body().nextMarker(). Set to null to list the first segment.
     * @param delimiter
     *         The operation returns a BlobPrefix element in the response body that acts as a placeholder for all blobs
     *         whose names begin with the same substring up to the appearance of the delimiter character. The delimiter may
     *         be a single character or a string.
     * @param options
     *         {@link ListBlobsOptions}
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_hierarchy "Sample code for ContainerAsyncClient.listBlobsHierarchySegment")] \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_hierarchy_helper "helper code for ContainerAsyncClient.listBlobsHierarchySegment")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
//    public Flux<BlobHierarchyListSegment> listBlobsHierarchySegment(String marker, String delimiter,
//            ListBlobsOptions options) {
//        return this.listBlobsHierarchySegment(marker, delimiter, options, null);
//    }

    /**
     * Returns a single segment of blobs and blob prefixes starting from the specified Marker. Use an empty
     * marker to start enumeration from the beginning. Blob names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListBlobs again (passing the the previously-returned
     * Marker) to get the next segment. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * @param marker
     *         Identifies the portion of the list to be returned with the next list operation.
     *         This value is returned in the response of a previous list operation as the
     *         ListBlobsHierarchySegmentResponse.body().nextMarker(). Set to null to list the first segment.
     * @param delimiter
     *         The operation returns a BlobPrefix element in the response body that acts as a placeholder for all blobs
     *         whose names begin with the same substring up to the appearance of the delimiter character. The delimiter may
     *         be a single character or a string.
     * @param options
     *         {@link ListBlobsOptions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_hierarchy "Sample code for ContainerAsyncClient.listBlobsHierarchySegment")] \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_hierarchy_helper "helper code for ContainerAsyncClient.listBlobsHierarchySegment")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
//    public Flux<BlobHierarchyListSegment> listBlobsHierarchySegment(String marker, String delimiter,
//            ListBlobsOptions options, Context context) {
//        return containerAsyncRawClient
//            .listBlobsHierarchySegment(null, delimiter, options, context)
//            .flatMapMany();
//    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1).
     *
     * @param proposedId
     *      A {@code String} in any valid GUID format. May be null.
     * @param duration
     *         The  duration of the lease, in seconds, or negative one (-1) for a lease that
     *         never expires. A non-infinite lease can be between 15 and 60 seconds.
     *
     * @return
     *      A reactive response containing the lease ID.
     */
    public Mono<Response<String>> acquireLease(String proposedId, int duration) {
        return this.acquireLease(proposedId, duration, null, null);
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1).
     *
     * @param proposedID
     *         A {@code String} in any valid GUID format. May be null.
     * @param duration
     *         The  duration of the lease, in seconds, or negative one (-1) for a lease that
     *         never expires. A non-infinite lease can be between 15 and 60 seconds.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      A reactive response containing the lease ID.
     */
    public Mono<Response<String>> acquireLease(String proposedID, int duration, ModifiedAccessConditions modifiedAccessConditions,
        Context context) {
        return containerAsyncRawClient
            .acquireLease(proposedID, duration, modifiedAccessConditions, context)
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().leaseId()));
    }

    /**
     * Renews the blob's previously-acquired lease.
     *
     * @param leaseID
     *         The leaseId of the active lease on the blob.
     *
     * @return
     *      A reactive response containing the renewed lease ID.
     */
    public Mono<Response<String>> renewLease(String leaseID) {
        return this.renewLease(leaseID, null, null);
    }

    /**
     * Renews the blob's previously-acquired lease.
     *
     * @param leaseID
     *         The leaseId of the active lease on the blob.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      A reactive response containing the renewed lease ID.
     */
    public Mono<Response<String>> renewLease(String leaseID, ModifiedAccessConditions modifiedAccessConditions, Context context) {
        return containerAsyncRawClient
            .renewLease(leaseID, modifiedAccessConditions, context)
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().leaseId()));
    }

    /**
     * Releases the blob's previously-acquired lease.
     *
     * @param leaseID
     *         The leaseId of the active lease on the blob.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> releaseLease(String leaseID) {
        return this.releaseLease(leaseID, null, null);
    }

    /**
     * Releases the blob's previously-acquired lease.
     *
     * @param leaseID
     *         The leaseId of the active lease on the blob.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      A reactive response signalling completion.
     */
    public Mono<VoidResponse> releaseLease(String leaseID, ModifiedAccessConditions modifiedAccessConditions, Context context) {
        return containerAsyncRawClient
            .releaseLease(leaseID, modifiedAccessConditions, context)
            .map(VoidResponse::new);
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     *
     * @return
     *      A reactive response containing the remaining time in the broken lease in seconds.
     */
    public Mono<Response<Integer>> breakLease() {
        return this.breakLease(null, null, null);
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     *
     * @param breakPeriodInSeconds
     *         An optional {@code Integer} representing the proposed duration of seconds that the lease should continue
     *         before it is broken, between 0 and 60 seconds. This break period is only used if it is shorter than the
     *         time remaining on the lease. If longer, the time remaining on the lease is used. A new lease will not be
     *         available before the break period has expired, but the lease may be held for longer than the break
     *         period.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return
     *      A reactive response containing the remaining time in the broken lease in seconds.
     */
    public Mono<Response<Integer>> breakLease(Integer breakPeriodInSeconds, ModifiedAccessConditions modifiedAccessConditions,
        Context context) {
        return containerAsyncRawClient
            .breakLease(breakPeriodInSeconds, modifiedAccessConditions, context)
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().leaseTime()));
    }

    /**
     * ChangeLease changes the blob's lease ID.
     *
     * @param leaseId
     *         The leaseId of the active lease on the blob.
     * @param proposedID
     *         A {@code String} in any valid GUID format.
     *
     * @return
     *      A reactive response containing the new lease ID.
     */
    public Mono<Response<String>> changeLease(String leaseId, String proposedID) {
        return this.changeLease(leaseId, proposedID, null, null);
    }

    /**
     * ChangeLease changes the blob's lease ID. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param leaseId
     *         The leaseId of the active lease on the blob.
     * @param proposedID
     *         A {@code String} in any valid GUID format.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to
     *         its parent, forming a linked list.
     *
     * @return A reactive response containing the new lease ID.
     */
    public Mono<Response<String>> changeLease(String leaseId, String proposedID, ModifiedAccessConditions modifiedAccessConditions,
        Context context) {
        return containerAsyncRawClient
            .changeLease(leaseId, proposedID, modifiedAccessConditions, context)
            .map(rb -> new SimpleResponse<>(rb, rb.deserializedHeaders().leaseId()));
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @return
     *      A reactive response containing the account info.
     */
    public Mono<Response<StorageAccountInfo>> getAccountInfo() {
        return this.getAccountInfo(null);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return
     *      A reactive response containing the account info.
     */
    public Mono<Response<StorageAccountInfo>> getAccountInfo(Context context) {
        return containerAsyncRawClient
            .getAccountInfo(context)
            .map(rb -> new SimpleResponse<>(rb, new StorageAccountInfo(rb.deserializedHeaders())));
    }
}
