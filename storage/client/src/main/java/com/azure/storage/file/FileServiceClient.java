// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.file.models.FileServiceProperties;
import com.azure.storage.file.models.ListSharesOptions;
import com.azure.storage.file.models.ShareItem;

import java.util.Map;

public final class FileServiceClient {
    private final FileServiceAsyncClient client;

    FileServiceClient(FileServiceAsyncClient client) {
        this.client = client;
    }

    public static FileServiceClientBuilder builder() {
        return new FileServiceClientBuilder();
    }

    public String url() {
        return client.url();
    }

    public ShareClient getShareClient(String shareName) {
        return new ShareClient(client.getShareAsyncClient(shareName));
    }

    public Iterable<ShareItem> listShares() {
        return listShares(null);
    }

    public Iterable<ShareItem> listShares(ListSharesOptions options) {
        return client.listShares(options).toIterable();
    }

    public Response<FileServiceProperties> getProperties() {
        return client.getProperties().block();
    }

    public VoidResponse setProperties(FileServiceProperties properties) {
        return client.setProperties(properties).block();
    }

    public Response<ShareClient> createShare(String shareName) {
        return createShare(shareName, null, null);
    }

    public Response<ShareClient> createShare(String shareName, Map<String, String> metadata, Integer quotaInGB) {
        Response<ShareAsyncClient> response = client.createShare(shareName, metadata, quotaInGB).block();
        return FileServiceAsyncClient.mapToResponse(response, new ShareClient(response.value()));
    }

    public VoidResponse deleteShare(String shareName) {
        return deleteShare(shareName, null);
    }

    public VoidResponse deleteShare(String shareName, String shareSnapshot) {
        return client.deleteShare(shareName, shareSnapshot).block();
    }
}
