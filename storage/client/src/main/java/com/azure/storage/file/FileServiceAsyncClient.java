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

public final class FileServiceAsyncClient {
    private final AzureFileStorageImpl client;

    FileServiceAsyncClient(URL endpoint, HttpPipeline httpPipeline) {
        this.client = new AzureFileStorageImpl(httpPipeline)
            .withUrl(endpoint.toString());
    }

    public static FileServiceClientBuilder builder() {
        return new FileServiceClientBuilder();
    }

    public String url() {
        return client.url();
    }

    public ShareAsyncClient getShareAsyncClient(String shareName) {
        return new ShareAsyncClient(client, shareName);
    }

    public Flux<ShareItem> listShares() {
        return listShares(null);
    }

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

    public Mono<Response<FileServiceProperties>> getProperties() {
        return client.services().getPropertiesWithRestResponseAsync(Context.NONE)
            .map(response -> mapToResponse(response, response.value()));
    }

    public Mono<VoidResponse> setProperties(FileServiceProperties properties) {
        return client.services().setPropertiesWithRestResponseAsync(properties, Context.NONE)
            .map(VoidResponse::new);
    }

    public Mono<Response<ShareAsyncClient>> createShare(String shareName) {
        return createShare(shareName, null, null);
    }

    public Mono<Response<ShareAsyncClient>> createShare(String shareName, Map<String, String> metadata, Integer quotaInGB) {
        ShareAsyncClient shareAsyncClient = new ShareAsyncClient(client, shareName);

        return shareAsyncClient.create(metadata, quotaInGB)
            .map(response -> mapToResponse(response, shareAsyncClient));
    }

    public Mono<VoidResponse> deleteShare(String shareName) {
        return deleteShare(shareName, null);
    }

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
