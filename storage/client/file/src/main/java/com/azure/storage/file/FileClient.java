// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.file.models.FileCopyInfo;
import com.azure.storage.file.models.FileDownloadInfo;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.FileInfo;
import com.azure.storage.file.models.FileProperties;
import com.azure.storage.file.models.FileRangeInfo;
import com.azure.storage.file.models.FileRangeWriteType;
import com.azure.storage.file.models.FileUploadInfo;
import com.azure.storage.file.models.HandleItem;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class FileClient {
    private final FileAsyncClient client;

    FileClient() {
        throw new UnsupportedOperationException();
    }

    public static FileClientBuilder syncBuilder() {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<FileInfo>> create(long maxSize, FileHTTPHeaders httpHeaders, Map<String, String> metadata) {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<FileCopyInfo>> startCopy(String sourceUrl, Map<String, String> metadata) {
        throw new UnsupportedOperationException();
    }

    public Mono<VoidResponse> abortCopy(String copyId) {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<FileDownloadInfo>> downloadWithProperties(long offset, long length, boolean rangeGetContentMD5) {
        throw new UnsupportedOperationException();
    }

    public Mono<VoidResponse> delete() {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<FileProperties>> getProperties(String shareSnapshot) {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<FileInfo>> setHttpHeaders(long newFileSize, FileHTTPHeaders httpHeaders) {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<FileInfo>> setMeatadata(Map<String, String> meatadata) {
        throw new UnsupportedOperationException();
    }

    public Mono<Response<FileUploadInfo>> upload(FileRangeWriteType type, long offset, long length, Flux<ByteBuf> data) {
        throw new UnsupportedOperationException();
    }

    public Flux<FileRangeInfo> listRanges(long offset, long length, String shareSnapshot) {
        throw new UnsupportedOperationException();
    }

    public Flux<HandleItem> listHandles(int maxResults) {
        throw new UnsupportedOperationException();
    }

    public Flux<Integer> forceCloseHandles(String handleId) {
        throw new UnsupportedOperationException();
    }
}

