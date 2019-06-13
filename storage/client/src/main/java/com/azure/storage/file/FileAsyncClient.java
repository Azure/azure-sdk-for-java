package com.azure.storage.file;

import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.FileRangeWriteType;
import com.azure.storage.file.models.HandleItem;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class FileAsyncClient {
    FileAsyncClient() {
        throw new UnsupportedOperationException();
    }

    public static FileClientBuilder asyncBuilder() {
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
