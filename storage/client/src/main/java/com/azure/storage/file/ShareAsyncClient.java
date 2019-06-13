// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.file.implementation.AzureFileStorageImpl;
import com.azure.storage.file.models.ShareInfo;
import com.azure.storage.file.models.ShareProperties;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.ShareStatistics;
import com.azure.storage.file.models.SignedIdentifier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class ShareAsyncClient {
    private final AzureFileStorageImpl client;
    private final String shareName;

    ShareAsyncClient(AzureFileStorageImpl client, String shareName) {
        this.shareName = shareName;
        this.client = new AzureFileStorageImpl(client.httpPipeline())
            .withUrl(client.url())
            .withVersion(client.version());
    }

    ShareAsyncClient(URL endpoint, HttpPipeline pipeline, String shareName) {
        this.shareName = shareName;
        this.client = new AzureFileStorageImpl(pipeline)
            .withUrl(endpoint.toString());
    }

    public static ShareClientBuilder asyncBuilder() {
        throw new UnsupportedOperationException();
    }

    public String url() {
        throw new UnsupportedOperationException();
    }

    public DirectoryAsyncClient getRootDirectoryClient() {
        throw new UnsupportedOperationException();
    }

    public DirectoryAsyncClient getDirectoryClient(String directoryName) {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<ShareInfo>> create() {
        return create(null, null);
    }

    public Mono<Response<ShareInfo>> create(Map<String, String> metadata, Integer quotaInGB) {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<ShareSnapshotInfo>> createSnapshot() {
        return createSnapshot(null);
    }

    public Mono<Response<ShareSnapshotInfo>> createSnapshot(Map<String, String> metadata) {
        throw new UnsupportedOperationException();
    }

    public Mono<VoidResponse> delete(String shareSnapshot) {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<ShareProperties>> getProperties(String shareSnapshot) {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<ShareInfo>> setQuota(int quotaInGB) {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<ShareInfo>> setMetadata(Map<String, String> metadata) {
        throw new UnsupportedOperationException();
    }

    public Flux<SignedIdentifier> listAccessPolicy() {
        throw new UnsupportedOperationException();
    }
    public Mono<Response<ShareInfo>> setAccessPolicy(List<SignedIdentifier> permissions) {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<ShareStatistics>> getStatistics() {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<DirectoryAsyncClient>> createDirectory(String directoryName, Map<String, String> metadata) {
        throw new UnsupportedOperationException();
    }

    public Mono<VoidResponse> deleteDirectory(String directoryName) {
        throw new UnsupportedOperationException();
    }
}
