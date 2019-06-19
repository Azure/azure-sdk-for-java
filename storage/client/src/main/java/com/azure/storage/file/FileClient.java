// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.storage.file.models.FileCopyInfo;
import com.azure.storage.file.models.FileDownloadInfo;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.FileInfo;
import com.azure.storage.file.models.FileMetadataInfo;
import com.azure.storage.file.models.FileProperties;
import com.azure.storage.file.models.FileRange;
import com.azure.storage.file.models.FileRangeWriteType;
import com.azure.storage.file.models.FileUploadInfo;
import com.azure.storage.file.models.HandleItem;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import java.util.Map;

public class FileClient {
    private final FileAsyncClient client;

    /**
     * Constructor for file async client
     * @param client
     */
    FileClient(FileAsyncClient client) {
        this.client = client;
    }

    /**
     * Builder of File client.
     * @return
     */
    public static FileClientBuilder builder() {
        return new FileClientBuilder();
    }

    /**
     * Create a new file in storage.
     * @param maxSize
     * @return
     */
    public Response<FileInfo> create(long maxSize) {
        return client.create(maxSize).block();
    }

    /**
     * Create a new file in storage.
     * @param maxSize
     * @param httpHeaders
     * @param metadata
     * @return
     */
    public Response<FileInfo> create(long maxSize, FileHTTPHeaders httpHeaders, Map<String, String> metadata) {
        return client.create(maxSize, httpHeaders, metadata).block();
    }

    /**
     * Copy from another source.
     * @param sourceUrl
     * @param metadata
     * @return
     */
    public Response<FileCopyInfo> startCopy(String sourceUrl, Map<String, String> metadata) {
        return client.startCopy(sourceUrl, metadata).block();
    }

    /**
     * Abort the copy.
     * @param copyId
     * @return
     */
    public void abortCopy(String copyId) {
        client.abortCopy(copyId).block();
    }

    /**
     * Download with properties
     * @return
     */
    public Response<FileDownloadInfo> downloadWithProperties() {
        return client.downloadWithProperties(null, null).block();
    }

    /**
     * Download with properties
     * @param range
     * @param rangeGetContentMD5
     * @return
     */
    public Response<FileDownloadInfo> downloadWithProperties(FileRange range, Boolean rangeGetContentMD5) {
        return client.downloadWithProperties(range, rangeGetContentMD5).block();
    }

    /**
     * Delete files from storage.
     * @return
     */
    public void delete() {
        client.delete().block();
    }

    /**
     * Get properties from the storage.
     * @return
     */
    public Response<FileProperties> getProperties() {
        return client.getProperties().block();
    }

    /**
     * Set http headers for files.
     * @param newFileSize
     * @param httpHeaders
     * @return
     */
    public Response<FileInfo> setHttpHeaders(long newFileSize, FileHTTPHeaders httpHeaders) {
        return client.setHttpHeaders(newFileSize, httpHeaders).block();
    }

    /**
     * Set metadata file. (Change the response type in according with the metadata response.)
     * @param meatadata
     * @return
     */
    public Response<FileMetadataInfo> setMeatadata(Map<String, String> meatadata) {
        return client.setMeatadata(meatadata).block();
    }

    /**
     * Upload file to storage.
     * @param data
     * @param length
     * @return
     */
    public Response<FileUploadInfo> upload(Flux<ByteBuf> data, long length) {
        return client.upload(data, length).block();
    }

    public Response<FileUploadInfo> upload(ByteBuf data, long length, FileRange range, FileRangeWriteType type) {
        return client.upload(Flux.just(data), length, range, type).block();
    }

    /**
     * List ranges of a file.
     * @return
     */
    public Iterable<FileRange> listRanges() {
        return client.listRanges(null).toIterable();
    }

    /**
     * List ranges of a file.
     * @return
     */
    public Iterable<FileRange> listRanges(FileRange range) {
        return client.listRanges(range).toIterable();
    }

    /**
     * List handles of a file.
     * @return
     */
    public Iterable<HandleItem> listHandles() {
        return listHandles(null);
    }

    /**
     * List handles of a file.
     * @param maxResults
     * @return
     */
    public Iterable<HandleItem> listHandles(Integer maxResults) {
        return client.listHandles(maxResults).toIterable();
    }

    /**
     * Force close handles for a file.
     * @param handleId
     * @return
     */
    public Iterable<Integer> forceCloseHandles(String handleId) {
        return client.forceCloseHandles(handleId).toIterable();
    }
}

