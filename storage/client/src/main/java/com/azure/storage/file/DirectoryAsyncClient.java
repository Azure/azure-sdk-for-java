package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.HandleItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class DirectoryAsyncClient {

    DirectoryAsyncClient() {
        throw new UnsupportedOperationException();
    }

    public static DirectoryClientBuilder asyncBuilder() {
        throw new UnsupportedOperationException();
    }

    public FileAsyncClient getFileClient(String name) {
        throw new UnsupportedOperationException();
    }

    public DirectoryAsyncClient getDirectoryClient(String directoryName) {
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

    public Mono<Response<DirectoryAsyncClient>> createSubDirectory(String directoryName, Map<String, String> metadata) {
        throw new UnsupportedOperationException();
    }

    public Mono<VoidResponse> deleteSubDirectory(String directoryName) {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<FileAsyncClient>> createFile(String fileName, long maxSize, FileHTTPHeaders httpHeaders, Map<String, String> meatadata) {
        throw new UnsupportedOperationException();
    }

    public Mono<VoidResponse> deleteFile(String fileName) {
        throw new UnsupportedOperationException();
    }
}
