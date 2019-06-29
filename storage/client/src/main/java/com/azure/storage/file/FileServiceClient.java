// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.file.models.FileServiceProperties;
import com.azure.storage.file.models.ListSharesOptions;
import com.azure.storage.file.models.ShareItem;
import java.util.Map;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FileServiceClient {

    private final FileServiceAsyncClient client;

    FileServiceClient() {
        throw new UnsupportedOperationException();
    }

    public static FileServiceClientBuilder syncBuilder() {
        throw new UnsupportedOperationException();
    }

    public String url() {
        throw new UnsupportedOperationException();
    }

    public ShareClient getShareClient(String shareName) {
        throw new UnsupportedOperationException();
    }

    public Flux<ShareItem> listShares(ListSharesOptions options) {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<FileServiceProperties>> getProperties() {
        throw new UnsupportedOperationException();
    }

    public Mono<VoidResponse> setProperties() {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<ShareClient>> createShare(String shareName, Map<String, String> metadata, int quotaInGB) {
        throw new UnsupportedOperationException();
    }

    public Mono<VoidResponse> deleteShare(String shareName, String shareSnapshot) {
        throw new UnsupportedOperationException();
    }
}
