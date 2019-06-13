package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.file.models.ShareInfo;
import com.azure.storage.file.models.ShareProperties;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.ShareStatistics;
import com.azure.storage.file.models.SignedIdentifier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class ShareClient {
    private final ShareAsyncClient client;

    ShareClient(ShareAsyncClient client) {
        this.client = client;
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

    public Response<ShareInfo> create() {
        return create(null, null);
    }

    public Response<ShareInfo> create(Map<String, String> metadata, Integer quotaInGB) {
        throw new UnsupportedOperationException();
    }

    public Response<ShareSnapshotInfo> createSnapshot() {
        return createSnapshot(null);
    }

    public Response<ShareSnapshotInfo> createSnapshot(Map<String, String> metadata) {
        throw new UnsupportedOperationException();
    }

    public VoidResponse delete(String shareSnapshot) {
        throw new UnsupportedOperationException();
    }

    public Response<ShareProperties> getProperties(String shareSnapshot) {
        throw new UnsupportedOperationException();
    }

    public Response<ShareInfo> setQuota(int quotaInGB) {
        throw new UnsupportedOperationException();
    }

    public Response<ShareInfo> setMetadata(Map<String, String> metadata) {
        throw new UnsupportedOperationException();
    }

    public Iterable<SignedIdentifier> listAccessPolicy() {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<ShareInfo>> setAccessPolicy(List<SignedIdentifier> permissions) {
        throw new UnsupportedOperationException();
    }

    public Response<ShareStatistics> getStatistics() {
        throw new UnsupportedOperationException();
    }

    public Response<DirectoryClient> createDirectory(String directoryName, Map<String, String> metadata) {
        throw new UnsupportedOperationException();
    }

    public VoidResponse deleteDirectory(String directoryName) {
        throw new UnsupportedOperationException();
    }
}
