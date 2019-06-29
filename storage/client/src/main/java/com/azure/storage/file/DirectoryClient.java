// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.file.models.DirectoryInfo;
import com.azure.storage.file.models.DirectoryProperties;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.FileRef;
import com.azure.storage.file.models.HandleItem;
import java.util.Map;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DirectoryClient {

    private final DirectoryAsyncClient client;

    DirectoryClient() {
        throw new UnsupportedOperationException();
    }

    public static DirectoryClientBuilder syncBuilder() {
        throw new UnsupportedOperationException();
    }

    public FileClient getFileClient(String name) {
        throw new UnsupportedOperationException();
    }

    public DirectoryClient getDirectoryClient(String directoryName) {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<DirectoryInfo>> create(Map<String, String> metadata) {
        throw new UnsupportedOperationException();
    }

    public Mono<VoidResponse> delete() {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<DirectoryProperties>> getProperties(String shareSnapshot) {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<DirectoryInfo>> setMetadata(Map<String, String> metadata) {
        throw new UnsupportedOperationException();
    }

    public Flux<FileRef> listFilesAndDirectories(String prefix, int maxResults, String shareSnapshot) {
        throw new UnsupportedOperationException();
    }

    public Flux<HandleItem> getHandles(int maxResult, boolean recursive) {
        throw new UnsupportedOperationException();
    }

    public Flux<Integer> forceCloseHandles(String handleId, boolean recursive) {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<DirectoryClient>> createSubDirectory(String directoryName, Map<String, String> metadata) {
        throw new UnsupportedOperationException();
    }

    public Mono<VoidResponse> deleteSubDirectory(String directoryName) {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<FileClient>> createFile(String fileName, long maxSize, FileHTTPHeaders httpHeaders, Map<String, String> meatadata) {
        throw new UnsupportedOperationException();
    }

    public Mono<VoidResponse> deleteFile(String fileName) {
        throw new UnsupportedOperationException();
    }
}
