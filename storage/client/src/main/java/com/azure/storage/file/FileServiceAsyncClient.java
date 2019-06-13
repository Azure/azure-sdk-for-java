package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.file.models.ShareItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class FileServiceAsyncClient {

    FileServiceAsyncClient() {
        throw new UnsupportedOperationException();
    }

    public static FileServiceClientBuilder asyncBuilder() {
        throw new UnsupportedOperationException();
    }

    public String url() {
        throw new UnsupportedOperationException();
    }

    public ShareAsyncClient getShareClient(String shareName) {
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

    public Mono<Response<ShareAsyncClient>> createShare(String shareName, Map<String, String> metadata, int quotaInGB) {
        throw new UnsupportedOperationException();
    }

    public Mono<VoidResponse> deleteShare(String shareName, String shareSnapshot) {
        throw new UnsupportedOperationException();
    }
}
