// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
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
import java.net.MalformedURLException;
import java.net.URL;
import reactor.core.publisher.Flux;
import java.util.Map;

/**
 * This class provides a client that contains all the operations for interacting files under Azure Storage File Service.
 * Operations allowed by the client are creating, uploading, copying, listing, downloading, and deleting files.
 *
 * <p><strong>Instantiating a synchronous File Client</strong></p>
 *
 * <pre>
 * FileClient client = FileClient.builder()
 *        .connectionString(connectionString)
 *        .endpoint(endpoint)
 *        .buildSync();
 * </pre>
 *
 * <p>View {@link FileClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see FileClientBuilder
 * @see FileAsyncClient
 * @see SharedKeyCredential
 * @see SASTokenCredential
 */
public class FileClient {
    private final FileAsyncClient fileAsyncClient;

    /**
     * Creates a FileClient that wraps a FileAsyncClient and blocks requests to the file service at {@code serviceEndpoint}.
     * Each service call goes through the {@code pipeline}.
     *
     * @param fileAsyncClient The {@link FileAsyncClient} that the client routes its request through.
     */
    FileClient(FileAsyncClient fileAsyncClient) {
        this.fileAsyncClient = fileAsyncClient;
    }

    /**
     * Get URL from FileAsyncClient.
     *
     * @return URL The URL contains {@code serviceEndpoint}
     * @throws MalformedURLException if no protocol is specified, or an
     *         unknown protocol is found, or {@code spec} is {@code null}.
     */
    public URL url() throws Exception {
        return fileAsyncClient.url();
    }

    /**
     * Creates a builder that can configure options for the FileClient before creating an instance of it.
     *
     * @return A new {@link FileClientBuilder} to create a ConfigurationClient.
     */
    public static FileClientBuilder builder() {
        return new FileClientBuilder();
    }

    /**
     * Adds a configuration value in the service if that key does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection" and value "db_connection".</p>
     *
     * {@codesnippet com.azure.applicationconfig.configurationclient.addSetting#string-string}
     *

     */
    public Response<FileInfo> create(long maxSize) {
        return fileAsyncClient.create(maxSize).block();
    }

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

    public void downloadToFile(String downloadFilePath) {
        downloadToFile(downloadFilePath, null);
    }

    public void downloadToFile(String downloadFilePath, FileRange fileRange) {
        fileAsyncClient.downloadToFile(downloadFilePath, fileRange).block();
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

    public void uploadFromFile(String uploadFilePath) {
        uploadFromFile(uploadFilePath, FileRangeWriteType.UPDATE);
    }

    public void uploadFromFile(String uploadFilePath, FileRangeWriteType type) {
        fileAsyncClient.uploadFromFile(uploadFilePath, type).block();
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

