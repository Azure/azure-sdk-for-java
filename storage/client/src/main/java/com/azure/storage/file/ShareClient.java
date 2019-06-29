package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.file.models.ShareInfo;
import com.azure.storage.file.models.ShareProperties;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.ShareStatistics;
import com.azure.storage.file.models.SignedIdentifier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class ShareClient {

    private final ShareAsyncClient client;

    ShareClient() {
        throw new UnsupportedOperationException();
    }

    public static ShareClientBuilder syncBuilder() {
        throw new UnsupportedOperationException();
    }

    public String url() {
        throw new UnsupportedOperationException();
    }

    public DirectoryClient getRootDirectoryClient() {
        throw new UnsupportedOperationException();
    }

    public DirectoryClient getDirectoryClient(String directoryName) {
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

    public Mono<Response<DirectoryClient>> createDirectory(String directoryName, Map<String, String> metadata) {
        throw new UnsupportedOperationException();
    }

    public Mono<VoidResponse> deleteDirectory(String directoryName) {
        throw new UnsupportedOperationException();
    }
}
