// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.file.models.ShareInfo;
import com.azure.storage.file.models.ShareProperties;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.ShareStatistics;
import com.azure.storage.file.models.SignedIdentifier;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ShareAsyncClient {

    ShareAsyncClient() {
        throw new UnsupportedOperationException();
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

    public Mono<Response<ShareInfo>> create(Map<String, String> metadata, int quotaInGB) {
        throw new UnsupportedOperationException();
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
