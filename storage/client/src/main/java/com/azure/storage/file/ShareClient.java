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

public class ShareClient {
    private final ShareAsyncClient client;

    ShareClient(ShareAsyncClient client) {
        this.client = client;
    }

    public static ShareClientBuilder builder() {
        return new ShareClientBuilder();
    }

    public String getUrl() {
        return client.getUrl();
    }

    public DirectoryAsyncClient getRootDirectoryClient() {
        throw new UnsupportedOperationException();
    }

    public DirectoryAsyncClient getDirectoryClient(String directoryName) {
        throw new UnsupportedOperationException();
    }

    public Response<ShareInfo> create() {
        return create(null, null);
    }

    public Response<ShareInfo> create(Map<String, String> metadata, Integer quotaInGB) {
        return client.create(metadata, quotaInGB).block();
    }

    public Response<ShareSnapshotInfo> createSnapshot() {
        return createSnapshot(null);
    }

    public Response<ShareSnapshotInfo> createSnapshot(Map<String, String> metadata) {
        return client.createSnapshot(metadata).block();
    }

    public VoidResponse delete() {
        return delete(null);
    }

    public VoidResponse delete(String shareSnapshot) {
        return client.delete(shareSnapshot).block();
    }

    public Response<ShareProperties> getProperties() {
        return getProperties(null);
    }

    public Response<ShareProperties> getProperties(String shareSnapshot) {
        return client.getProperties(shareSnapshot).block();
    }

    public Response<ShareInfo> setQuota(int quotaInGB) {
        return client.setQuota(quotaInGB).block();
    }

    public Response<ShareInfo> setMetadata(Map<String, String> metadata) {
        return client.setMetadata(metadata).block();
    }

    public Iterable<SignedIdentifier> listAccessPolicy() {
        return client.listAccessPolicy().toIterable();
    }

    public Response<ShareInfo> setAccessPolicy(List<SignedIdentifier> permissions) {
        return client.setAccessPolicy(permissions).block();
    }

    public Response<ShareStatistics> getStatistics() {
        return client.getStatistics().block();
    }

    public Response<DirectoryAsyncClient> createDirectory(String directoryName, Map<String, String> metadata) {
        throw new UnsupportedOperationException();
    }

    public VoidResponse deleteDirectory(String directoryName) {
        throw new UnsupportedOperationException();
    }
}
