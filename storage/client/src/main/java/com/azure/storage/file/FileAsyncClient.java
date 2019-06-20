// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.file.implementation.AzureFileStorageBuilder;
import com.azure.storage.file.implementation.AzureFileStorageImpl;
import com.azure.storage.file.models.CopyStatusType;
import com.azure.storage.file.models.FileCopyInfo;
import com.azure.storage.file.models.FileDownloadInfo;
import com.azure.storage.file.models.FileGetPropertiesHeaders;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.FileInfo;
import com.azure.storage.file.models.FileMetadataInfo;
import com.azure.storage.file.models.FileProperties;
import com.azure.storage.file.models.FileRange;
import com.azure.storage.file.models.FileRangeWriteType;
import com.azure.storage.file.models.FileUploadInfo;
import com.azure.storage.file.models.FileUploadRangeHeaders;
import com.azure.storage.file.models.FilesCreateResponse;
import com.azure.storage.file.models.FilesDownloadResponse;
import com.azure.storage.file.models.FilesForceCloseHandlesResponse;
import com.azure.storage.file.models.FilesGetPropertiesResponse;
import com.azure.storage.file.models.FilesGetRangeListResponse;
import com.azure.storage.file.models.FilesListHandlesResponse;
import com.azure.storage.file.models.FilesSetHTTPHeadersResponse;
import com.azure.storage.file.models.FilesSetMetadataResponse;
import com.azure.storage.file.models.FilesStartCopyResponse;
import com.azure.storage.file.models.FilesUploadRangeResponse;
import com.azure.storage.file.models.HandleItem;
import io.netty.buffer.ByteBuf;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Map;

public class FileAsyncClient {
    private final AzureFileStorageImpl azureFileStorageClient;
    private final String shareName;
    private final String filePath;
    private final String shareSnapshot;

    /**
     * Constructor of FileAsyncClient
     * @param azureFileStorageClient
     * @param shareName
     * @param filePath
     */
    FileAsyncClient(AzureFileStorageImpl azureFileStorageClient, String shareName, String filePath, String shareSnapshot) {
        this.shareName = shareName;
        this.filePath = filePath;
        this.shareSnapshot = shareSnapshot;
        this.azureFileStorageClient = new AzureFileStorageBuilder().pipeline(azureFileStorageClient.httpPipeline())
                            .url(azureFileStorageClient.url())
                            .version(azureFileStorageClient.version())
                            .build();
    }

    /**
     * Constructor of FileAsyncClient
     * @param endpoint
     * @param httpPipeline
     * @param shareName
     * @param filePath
     */
    FileAsyncClient(URL endpoint, HttpPipeline httpPipeline, String shareName, String filePath, String shareSnapshot) {
        this.shareName = shareName;
        this.filePath = filePath;
        this.shareSnapshot = shareSnapshot;
        this.azureFileStorageClient = new AzureFileStorageBuilder().pipeline(httpPipeline)
                          .url(endpoint.toString())
                          .build();
    }

    /**
     * Get URL from azureFileStorageClient.
     * @return
     */
    public URL url() throws Exception {
        return new URL(azureFileStorageClient.url());
    }

    /**
     * Builder of FileAsyncClient and FileClient.
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
    public Mono<Response<FileInfo>> create(long maxSize) {
        return create(maxSize, null, null);
    }

    /**
     * Create a new file in storage.
     * @param maxSize
     * @param httpHeaders
     * @param metadata
     * @return
     */
    public Mono<Response<FileInfo>> create(long maxSize, FileHTTPHeaders httpHeaders, Map<String, String> metadata) {
        return azureFileStorageClient.files().createWithRestResponseAsync(shareName, filePath, maxSize, null, metadata, httpHeaders, Context.NONE)
            .map(this::createResponse);
    }

    /**
     * Copy from another source.
     * @param sourceUrl
     * @param metadata
     * @return
     */
    public Mono<Response<FileCopyInfo>> startCopy(String sourceUrl, Map<String, String> metadata) {
        return azureFileStorageClient.files().startCopyWithRestResponseAsync(shareName, filePath, sourceUrl, null, metadata, Context.NONE)
                    .map(this::startCopyResponse);
    }

    /**
     * Abort the copy.
     * @param copyId
     * @return
     */
    public Mono<VoidResponse> abortCopy(String copyId) {
        return azureFileStorageClient.files().abortCopyWithRestResponseAsync(shareName, filePath, copyId, Context.NONE)
                    .map(VoidResponse::new);
    }
    /**
     * Download with properties
     * @return
     */
    public Mono<Response<FileDownloadInfo>> downloadWithProperties() {
        return downloadWithProperties(null, null);
    }

    /**
     * Download with properties
     * @param rangeGetContentMD5
     * @return
     */
    public Mono<Response<FileDownloadInfo>> downloadWithProperties(FileRange range, Boolean rangeGetContentMD5) {
        String rangeString = range == null ? null : range.toString();
        return azureFileStorageClient.files().downloadWithRestResponseAsync(shareName, filePath, null, rangeString, rangeGetContentMD5, Context.NONE)
                    .map(this::downloadWithPropertiesResponse);
    }

    /**
     * Delete files from storage.
     * @return
     */
    public Mono<VoidResponse> delete() {
        return azureFileStorageClient.files().deleteWithRestResponseAsync(shareName, filePath, Context.NONE)
                    .map(VoidResponse::new);
    }

    /**
     * Get properties from the storage.
     * @return
     */
    public Mono<Response<FileProperties>> getProperties() {
        return azureFileStorageClient.files().getPropertiesWithRestResponseAsync(shareName, filePath, shareSnapshot, null, Context.NONE)
                    .map(this::getPropertiesResponse);
    }

    /**
     * Set http headers for files.
     * @param newFileSize
     * @param httpHeaders
     * @return
     */
    public Mono<Response<FileInfo>> setHttpHeaders(long newFileSize, FileHTTPHeaders httpHeaders) {
        return azureFileStorageClient.files().setHTTPHeadersWithRestResponseAsync(shareName, filePath, null, newFileSize, httpHeaders, Context.NONE)
                        .map(this::setHttpHeadersResponse);
    }

    /**
     * Set metadata file. (Change the response type in according with the metadata response.)
     * @param meatadata
     * @return
     */
    public Mono<Response<FileMetadataInfo>> setMeatadata(Map<String, String> meatadata) {
        return azureFileStorageClient.files().setMetadataWithRestResponseAsync(shareName,  filePath, null, meatadata, Context.NONE)
                    .map(this::setMeatadataResponse);
    }

    /**
     * Upload file to storage.
     * @param data
     * @param length
     * @return
     */
    public Mono<Response<FileUploadInfo>> upload(Flux<ByteBuf> data, long length) {
        FileRange range = new FileRange(0, length - 1);
        return azureFileStorageClient.files().uploadRangeWithRestResponseAsync(shareName, filePath, range.toString(), FileRangeWriteType.UPDATE, length, data, null, null, Context.NONE)
            .map(this::uploadResponse);
    }

    /**
     * Upload file to storage.
     * @param data
     * @param length
     * @return
     */
    public Mono<Response<FileUploadInfo>> upload(Flux<ByteBuf> data, long length, int offset, FileRangeWriteType type) {
        FileRange range = new FileRange(offset, offset + length - 1);
        return azureFileStorageClient.files().uploadRangeWithRestResponseAsync(shareName, filePath, range.toString(), type, length, data, null, null, Context.NONE)
                   .map(this::uploadResponse);
    }

    /**
     * List ranges of a file.
     * @return
     */
    public Flux<FileRange> listRanges() {
        return azureFileStorageClient.files().getRangeListWithRestResponseAsync(shareName, filePath, shareSnapshot, null, null, Context.NONE)
                   .flatMapMany(this::convertListRangesResponseToFileRangeInfo);
    }

    /**
     * List ranges of a file.
     * @param range
     * @return
     */
    public Flux<FileRange> listRanges(FileRange range) {
        return azureFileStorageClient.files().getRangeListWithRestResponseAsync(shareName, filePath, shareSnapshot, null, range.toString(), Context.NONE)
                    .flatMapMany(this::convertListRangesResponseToFileRangeInfo);
    }

    /**
     * List handles of a file.
     * @return
     */
    public Flux<HandleItem> listHandles() {
        return listHandles(null);
    }

    /**
     * List handles of a file.
     * @param maxResults
     * @return
     */
    public Flux<HandleItem> listHandles(Integer maxResults) {
        return azureFileStorageClient.files().listHandlesWithRestResponseAsync(shareName, filePath, null, maxResults, null, shareSnapshot, Context.NONE)
                   .flatMapMany(response -> nextPageForHandles(response, maxResults));
    }

    /**
     * Force close handles for a file.
     * @param handleId
     * @return
     */
    public Flux<Integer> forceCloseHandles(String handleId) {
        return azureFileStorageClient.files().forceCloseHandlesWithRestResponseAsync(shareName, filePath, handleId, null, null, shareSnapshot, Context.NONE)
                   .flatMapMany(response -> nextPageForForceCloseHandles(response, handleId));
    }

    private Flux<Integer> nextPageForForceCloseHandles(final FilesForceCloseHandlesResponse response, final String handleId) {
        List<Integer> handleCount = Arrays.asList(response.deserializedHeaders().numberOfHandlesClosed());

        if (response.deserializedHeaders().marker() == null) {
            return Flux.fromIterable(handleCount);
        }
        Mono<FilesForceCloseHandlesResponse> listResponse = azureFileStorageClient.files().forceCloseHandlesWithRestResponseAsync(shareName, filePath, handleId, null, response.deserializedHeaders().marker(), shareSnapshot, Context.NONE);
        Flux<Integer> fileRefPublisher = listResponse.flatMapMany(newResponse -> nextPageForForceCloseHandles(newResponse, handleId));
        return Flux.fromIterable(handleCount).concatWith(fileRefPublisher);
    }

    private Publisher<? extends HandleItem> nextPageForHandles(final FilesListHandlesResponse response, final Integer maxResults) {
        List<HandleItem> handleItems = response.value().handleList();

        if (response.value().nextMarker() == null) {
            return Flux.fromIterable(handleItems);
        }
        final Integer results = maxResults - handleItems.size();
        Mono<FilesListHandlesResponse> listResponse = azureFileStorageClient.files().listHandlesWithRestResponseAsync(shareName, filePath, response.value().nextMarker(), results, null, shareSnapshot,  Context.NONE);
        Flux<HandleItem> fileRefPublisher = listResponse.flatMapMany(newResponse -> nextPageForHandles(newResponse, results));
        return Flux.fromIterable(handleItems).concatWith(fileRefPublisher);
    }

    private Response<FileInfo> createResponse(final FilesCreateResponse response) {
        String eTag = response.deserializedHeaders().eTag();
        OffsetDateTime lastModified = response.deserializedHeaders().lastModified();
        boolean isServerEncrypted = response.deserializedHeaders().isServerEncrypted();
        FileInfo fileInfo = new FileInfo(eTag, lastModified, isServerEncrypted);
        return mapResponse(response, fileInfo);
    }

    private Response<FileCopyInfo> startCopyResponse(final FilesStartCopyResponse response) {
        String eTag = response.deserializedHeaders().eTag();
        OffsetDateTime lastModified = response.deserializedHeaders().lastModified();
        String copyId = response.deserializedHeaders().copyId();
        CopyStatusType copyStatus = response.deserializedHeaders().copyStatus();
        FileCopyInfo fileCopyInfo = new FileCopyInfo(eTag, lastModified, copyId, copyStatus);
        return mapResponse(response, fileCopyInfo);
    }

    private Response<FileInfo> setHttpHeadersResponse(final FilesSetHTTPHeadersResponse response) {
        String eTag = response.deserializedHeaders().eTag();
        OffsetDateTime lastModified = response.deserializedHeaders().lastModified();
        boolean isServerEncrypted = response.deserializedHeaders().isServerEncrypted();
        FileInfo fileInfo = new FileInfo(eTag, lastModified, isServerEncrypted);
        return mapResponse(response, fileInfo);
    }
    private Response<FileDownloadInfo> downloadWithPropertiesResponse(final FilesDownloadResponse response) {
        String eTag = response.deserializedHeaders().eTag();
        OffsetDateTime lastModified = response.deserializedHeaders().lastModified();
        Map<String, String> metadata = response.deserializedHeaders().metadata();
        Long contentLength = response.deserializedHeaders().contentLength();
        String contentType = response.deserializedHeaders().contentType();
        String contentRange = response.deserializedHeaders().contentRange();
        FileDownloadInfo fileDownloadInfo = new FileDownloadInfo(eTag, lastModified, metadata, contentLength, contentType, contentRange);
        return mapResponse(response, fileDownloadInfo);
    }

    private Response<FileProperties> getPropertiesResponse(final FilesGetPropertiesResponse response) {
        FileGetPropertiesHeaders headers = response.deserializedHeaders();
        String eTag = headers.eTag();
        OffsetDateTime lastModified = headers.lastModified();
        Map<String, String> metadata = headers.metadata();
        String fileType = headers.fileType();
        Long contentLength = headers.contentLength();
        String contentType = headers.contentType();
        byte[] contentMD5;
        try {
            contentMD5 = headers.contentMD5();
        } catch (NullPointerException e) {
            contentMD5 = null;
        }
        String contentEncoding = headers.contentEncoding();
        String cacheControl = headers.cacheControl();
        String contentDisposition = headers.contentDisposition();
        OffsetDateTime copyCompletionTime = headers.copyCompletionTime();
        String copyStatusDescription = headers.copyStatusDescription();
        String copyId = headers.copyId();
        String copyProgress = headers.copyProgress();
        String copySource = headers.copySource();
        CopyStatusType copyStatus = headers.copyStatus();
        Boolean isServerEncrpted = headers.isServerEncrypted();
        FileProperties fileProperties = new FileProperties(eTag, lastModified, metadata, fileType, contentLength, contentType, contentMD5,
            contentEncoding, cacheControl, contentDisposition, copyCompletionTime, copyStatusDescription, copyId, copyProgress,
            copySource, copyStatus, isServerEncrpted);
        return mapResponse(response, fileProperties);
    }

    private Response<FileUploadInfo> uploadResponse(final FilesUploadRangeResponse response) {
        FileUploadRangeHeaders headers = response.deserializedHeaders();
        String eTag = headers.eTag();
        OffsetDateTime lastModified = headers.lastModified();
        byte[] contentMD5;
        try {
            contentMD5 = headers.contentMD5();
        } catch (NullPointerException e) {
            contentMD5 = null;
        }
        Boolean isServerEncrypted = headers.isServerEncrypted();
        FileUploadInfo fileUploadInfo = new FileUploadInfo(eTag, lastModified, contentMD5, isServerEncrypted);
        return mapResponse(response, fileUploadInfo);
    }

    private Response<FileMetadataInfo> setMeatadataResponse(final FilesSetMetadataResponse response) {
        String eTag = response.deserializedHeaders().eTag();
        boolean isServerEncrypted = response.deserializedHeaders().isServerEncrypted();
        FileMetadataInfo fileMetadataInfo = new FileMetadataInfo(eTag, isServerEncrypted);
        return mapResponse(response, fileMetadataInfo);
    }

    private Flux<FileRange> convertListRangesResponseToFileRangeInfo(FilesGetRangeListResponse response) {
        List<FileRange> fileRanges = new ArrayList<>();
        response.value().forEach(range -> {
            long start = range.start();
            long end = range.end();
            fileRanges.add(new FileRange(start, end));
        });
        return Flux.fromIterable(fileRanges);
    }

    private <T> SimpleResponse<T> mapResponse(Response response, T value) {
        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), value);
    }
}
