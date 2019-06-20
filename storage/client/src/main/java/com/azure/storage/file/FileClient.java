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
import java.net.URL;
import reactor.core.publisher.Flux;
import java.util.Map;

public class FileClient {
    private final FileAsyncClient fileAsyncClient;

    /**
     * Constructor for file async fileAsyncClient
     * @param fileAsyncClient
     */
    FileClient(FileAsyncClient fileAsyncClient) {
        this.fileAsyncClient = fileAsyncClient;
    }

    /**
     * Get URL from fileAsyncClient.
     * @return
     */
    public URL url() throws Exception {
        return fileAsyncClient.url();
    }
    /**
     * Builder of File fileAsyncClient.
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
        return fileAsyncClient.create(maxSize).block();
    }

    /**
     * Create a new file in storage.
     * @param maxSize
     * @param httpHeaders
     * @param metadata
     * @return
     */
    public Response<FileInfo> create(long maxSize, FileHTTPHeaders httpHeaders, Map<String, String> metadata) {
        return fileAsyncClient.create(maxSize, httpHeaders, metadata).block();
    }

    /**
     * Copy from another source.
     * @param sourceUrl
     * @param metadata
     * @return
     */
    public Response<FileCopyInfo> startCopy(String sourceUrl, Map<String, String> metadata) {
        return fileAsyncClient.startCopy(sourceUrl, metadata).block();
    }

    /**
     * Abort the copy.
     * @param copyId
     * @return
     */
    public void abortCopy(String copyId) {
        fileAsyncClient.abortCopy(copyId).block();
    }

    /**
     * Download with properties
     * @return
     */
    public Response<FileDownloadInfo> downloadWithProperties() {
        return fileAsyncClient.downloadWithProperties(null, null).block();
    }

    /**
     * Download with properties
     * @param range
     * @param rangeGetContentMD5
     * @return
     */
    public Response<FileDownloadInfo> downloadWithProperties(FileRange range, Boolean rangeGetContentMD5) {
        return fileAsyncClient.downloadWithProperties(range, rangeGetContentMD5).block();
    }

    /**
     * Delete files from storage.
     * @return
     */
    public void delete() {
        fileAsyncClient.delete().block();
    }

    /**
     * Get properties from the storage.
     * @return
     */
    public Response<FileProperties> getProperties() {
        return fileAsyncClient.getProperties().block();
    }

    /**
     * Set http headers for files.
     * @param newFileSize
     * @param httpHeaders
     * @return
     */
    public Response<FileInfo> setHttpHeaders(long newFileSize, FileHTTPHeaders httpHeaders) {
        return fileAsyncClient.setHttpHeaders(newFileSize, httpHeaders).block();
    }

    /**
     * Set metadata file. (Change the response type in according with the metadata response.)
     * @param meatadata
     * @return
     */
    public Response<FileMetadataInfo> setMeatadata(Map<String, String> meatadata) {
        return fileAsyncClient.setMeatadata(meatadata).block();
    }

    /**
     * Upload file to storage.
     * @param data
     * @param length
     * @return
     */
    public Response<FileUploadInfo> upload(ByteBuf data, long length) {
        return fileAsyncClient.upload(Flux.just(data), length).block();
    }

    public Response<FileUploadInfo> upload(ByteBuf data, long length, int offset, FileRangeWriteType type) {
        return fileAsyncClient.upload(Flux.just(data), length, offset, type).block();
    }

    /**
     * List ranges of a file.
     * @return
     */
    public Iterable<FileRange> listRanges() {
        return fileAsyncClient.listRanges(null).toIterable();
    }

    /**
     * List ranges of a file.
     * @return
     */
    public Iterable<FileRange> listRanges(FileRange range) {
        return fileAsyncClient.listRanges(range).toIterable();
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
        return fileAsyncClient.listHandles(maxResults).toIterable();
    }

    /**
     * Force close handles for a file.
     * @param handleId
     * @return
     */
    public Iterable<Integer> forceCloseHandles(String handleId) {
        return fileAsyncClient.forceCloseHandles(handleId).toIterable();
    }
}

