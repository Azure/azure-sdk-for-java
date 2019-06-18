// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import com.azure.storage.file.implementation.AzureFileStorageImpl;
import com.azure.storage.file.models.DeleteSnapshotsOptionType;
import com.azure.storage.file.models.FileServiceProperties;
import com.azure.storage.file.models.ListSharesIncludeType;
import com.azure.storage.file.models.ListSharesOptions;
import com.azure.storage.file.models.ShareItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * File service async client
 */
public final class FileServiceAsyncClient {
    private final AzureFileStorageImpl client;

    FileServiceAsyncClient(URL endpoint, HttpPipeline httpPipeline) {
        this.client = new AzureFileStorageImpl(httpPipeline)
            .withUrl(endpoint.toString());
    }

    /**
     * @return a new client builder instance
     */
    public static FileServiceClientBuilder builder() {
        return new FileServiceClientBuilder();
    }

    /**
     * @return the URL of the file service
     */
    public String url() {
        return client.url();
    }

    /**
     * Gets a ShareAsyncClient that is able to interact with the specified share.
     * @param shareName Name of the share
     * @return a ShareAsyncClient that interacts with the specified share
     */
    public ShareAsyncClient getShareAsyncClient(String shareName) {
        return new ShareAsyncClient(client, shareName);
    }

    /**
     * Lists the shares in the Storage account
     *
     * @return the shares in the Storage account
     */
    public Flux<ShareItem> listShares() {
        return listShares(null);
    }

    /**
     * Lists the shares in the Storage account that pass the options filter
     *
     * @param options Options used to filter which shares are listed
     * @return the shares in the Storage account that passed the filter
     */
    public Flux<ShareItem> listShares(ListSharesOptions options) {
        String prefix = null;
        String marker = null;
        Integer maxResults = null;
        List<ListSharesIncludeType> include = new ArrayList<>();

        if (options != null) {
            prefix = options.prefix();
            marker = options.marker();
            maxResults = options.maxResults();

            if (options.includeMetadata()) {
                include.add(ListSharesIncludeType.fromString(ListSharesIncludeType.METADATA.toString()));
            }

            if (options.includeSnapshots()) {
                include.add(ListSharesIncludeType.fromString(ListSharesIncludeType.SNAPSHOTS.toString()));
            }
        }

        return client.services().listSharesSegmentWithRestResponseAsync(prefix, marker, maxResults, include, null, Context.NONE)
            .flatMapMany(response -> Flux.fromIterable(response.value().shareItems()));
    }

    /**
     * @return the global file properties in the storage account
     */
    public Mono<Response<FileServiceProperties>> getProperties() {
        return client.services().getPropertiesWithRestResponseAsync(Context.NONE)
            .map(response -> mapToResponse(response, response.value()));
    }

    /**
     * Sets the global file properties for the storage account
     * @param properties File properties
     * @return an empty response
     */
    public Mono<VoidResponse> setProperties(FileServiceProperties properties) {
        return client.services().setPropertiesWithRestResponseAsync(properties, Context.NONE)
            .map(VoidResponse::new);
    }

    /**
     * Creates a new share and returns a client to interact with it
     * @param shareName Name of the share
     * @return a ShareAsyncClient that interacts with the share that was created
     */
    public Mono<Response<ShareAsyncClient>> createShare(String shareName) {
        return createShare(shareName, null, null);
    }

    /**
     * Creates a new share and returns a client to interact with it
     * @param shareName Name of the share
     * @param metadata Metadata for the new share
     * @param quotaInGB Maximum size the share is allowed to grow to in GB
     * @return a ShareAsyncClient that interacts with the share that was created
     */
    public Mono<Response<ShareAsyncClient>> createShare(String shareName, Map<String, String> metadata, Integer quotaInGB) {
        ShareAsyncClient shareAsyncClient = new ShareAsyncClient(client, shareName);

        return shareAsyncClient.create(metadata, quotaInGB)
            .map(response -> mapToResponse(response, shareAsyncClient));
    }

    /**
     * Deletes a share and all of its snapshots
     * @param shareName Name of the share
     * @return an empty response
     */
    public Mono<VoidResponse> deleteShare(String shareName) {
        return deleteShare(shareName, null);
    }

    /**
     * Deletes a specific snapshot of a share
     * @param shareName Name of the share
     * @param shareSnapshot Identifier of the snapshot
     * @return an empty response
     */
    public Mono<VoidResponse> deleteShare(String shareName, String shareSnapshot) {
        DeleteSnapshotsOptionType deleteSnapshots = null;
        if (ImplUtils.isNullOrEmpty(shareSnapshot)) {
            deleteSnapshots = DeleteSnapshotsOptionType.fromString(DeleteSnapshotsOptionType.INCLUDE.toString());
        }
        return client.shares().deleteWithRestResponseAsync(shareName, shareSnapshot, null, deleteSnapshots, Context.NONE)
            .map(VoidResponse::new);
    }

    static <T> Response<T> mapToResponse(Response<?> response, T value) {
        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), value);
    }
}
