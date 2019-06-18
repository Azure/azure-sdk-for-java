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
import com.azure.storage.file.models.FileRangeInfo;
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
     * @param offset
     * @param length
     * @param rangeGetContentMD5
     * @return
     */
    public Response<FileDownloadInfo> downloadWithProperties(long offset, long length, boolean rangeGetContentMD5) {
        return client.downloadWithProperties(offset, length, rangeGetContentMD5).block();
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
     * @param type
     * @param offset
     * @param length
     * @param data
     * @return
     */
    public Response<FileUploadInfo> upload(FileRangeWriteType type, long offset, long length, Flux<ByteBuf> data) {
        return client.upload(type, offset, length, data).block();
    }

    /**
     * List ranges of a file.
     * @param offset
     * @param length
     * @return
     */
    public Iterable<FileRangeInfo> listRanges(long offset, long length) {
        return client.listRanges(offset, length).toIterable();
    }

    /**
     * List handles of a file.
     * @param maxResults
     * @return
     */
    public Iterable<HandleItem> listHandles(int maxResults) {
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

