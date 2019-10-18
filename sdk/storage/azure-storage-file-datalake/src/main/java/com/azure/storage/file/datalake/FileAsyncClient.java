// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientImpl;
import com.azure.storage.file.datalake.implementation.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.implementation.models.PathHTTPHeaders;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.PathAccessConditions;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.ReliableDownloadOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Map;

import static com.azure.core.implementation.util.FluxUtil.withContext;

public class FileAsyncClient extends PathAsyncClient {
    /**
     * Package-private constructor for use by {@link PathClientBuilder}.
     *
     * @param dataLakeStorage The API client for data lake storage.
     * @param accountName The account name for storage account.
     * @param blockBlobAsyncClient
     */
    protected FileAsyncClient(DataLakeStorageClientImpl dataLakeStorage, String accountName, String fileSystemName,
        String pathName, BlockBlobAsyncClient blockBlobAsyncClient) {
        super(dataLakeStorage, accountName, fileSystemName, pathName, blockBlobAsyncClient);
    }

    /**
     * Creates the resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathAsyncClient.create}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @return A reactive response containing the information of the created resource.
     */
    public Mono<PathItem> create() {
        return createWithResponse(null, null, null, null, null).flatMap(FluxUtil::toMono);
    }

    public Mono<Response<PathItem>> createWithResponse(PathHTTPHeaders httpHeaders, Map<String, String> metadata,
        String permissions, String umask, PathAccessConditions accessConditions) {
        return withContext(context -> createWithResponse(PathResourceType.FILE, httpHeaders, metadata,
            permissions, umask, accessConditions, context));
    }

    public Mono<Void> delete() {
        return this.deleteWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified resource.
     *
     * <p><strong>Code Samples>Code Samples</strong></p>
     *
     * TODO (rickle-msft): code snippet
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param accessConditions {@link PathAccessConditions}
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> deleteWithResponse(PathAccessConditions accessConditions) {
        // TODO (rickle-msft): Update for continuation token if we support HNS off
        return withContext(context -> deleteWithResponse(null /* recursive */, accessConditions, context));
    }

    // Append Data

    /**
     * Appends data to the specified resource to later be flushed (written) by a call to flushData
     *
     * <p><strong>Code Samples>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileAsyncClient.appendData#Flux-Long-Long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param data The data to write to the file.
     * @param offset The position where the data is to be appended.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Void> appendData(Flux<ByteBuffer> data, long offset, long length) {
        return appendDataWithResponse(data, offset, length, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Appends data to the specified resource to later be flushed (written) by a call to flushData
     *
     * <p><strong>Code Samples>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileAsyncClient.appendDataWithResponse#Flux-Long-Long-byte-LeaseAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param data The data to write to the file.
     * @param offset The position where the data is to be appended.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @param contentMd5 An MD5 hash of the content of the data. If specified, the service will calculate the MD5 of the
     * received data and fail the request if it does not match the provided MD5.
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> appendDataWithResponse(Flux<ByteBuffer> data, long offset, long length,
        byte[] contentMd5, LeaseAccessConditions leaseAccessConditions) {
        return withContext(context -> appendDataWithResponse(data, offset, length, contentMd5,
            leaseAccessConditions, context));
    }

    Mono<Response<Void>> appendDataWithResponse(Flux<ByteBuffer> data, long offset, long length,
        byte[] contentMd5, LeaseAccessConditions leaseAccessConditions, Context context) {

        leaseAccessConditions = leaseAccessConditions == null ? new LeaseAccessConditions() : leaseAccessConditions;

        PathHTTPHeaders httpHeaders = new PathHTTPHeaders().setTransactionalContentMD5(contentMd5);

        return this.dataLakeStorage.paths().appendDataWithRestResponseAsync(data, offset, null, length, null,
            httpHeaders, leaseAccessConditions, context).map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Flushes (writes) data previously appended to the file through a call to appendData.
     * The previously uploaded data must be contiguous.
     *
     * <p><strong>Code Samples>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileAsyncClient.flushData#Long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param position The length of the file after all data has been written.
     *
     * @return A reactive response containing the information of the created resource.
     */
    public Mono<PathInfo> flushData(long position) {
        return flushDataWithResponse(position, null, null, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Flushes (writes) data previously appended to the file through a call to appendData.
     * The previously uploaded data must be contiguous.
     *
     * <p><strong>Code Samples>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileAsyncClient.flushDataWithResponse#Long-Boolean-Boolean-PathHTTPHeaders-PathAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param position The length of the file after all data has been written.
     * @param retainUncommittedData If "true", uncommitted data is retained after the flush operation completes;
     * otherwise, the uncommitted data is deleted after the flush operation. The default is false. Data at offsets
     * less than the specified position are written to the file when flush succeeds, but this optional parameter
     * allows data after the flush position to be retained for a future flush operation.
     * @param close Azure Storage Events allow applications to receive notifications when files change. When Azure
     * Storage Events are enabled, a file changed event is raised. This event has a property indicating whether this is
     * the final change to distinguish the difference between an intermediate flush to a file stream and the final close
     * of a file stream. The close query parameter is valid only when the action is "flush" and change notifications
     * are enabled. If the value of close is "true" and the flush operation completes successfully, the service raises
     * a file change notification with a property indicating that this is the final update (the file stream has been
     * closed). If "false" a change notification is raised indicating the file has changed. The default is false.
     * This query parameter is set to true by the Hadoop ABFS driver to indicate that the file stream has been closed.
     * @param httpHeaders {@link PathHTTPHeaders httpHeaders}
     * @param accessConditions {@link PathAccessConditions accessConditions}
     *
     * @return A reactive response containing the information of the created resource.
     */
    public Mono<Response<PathInfo>> flushDataWithResponse(long position, Boolean retainUncommittedData, Boolean close,
        PathHTTPHeaders httpHeaders, PathAccessConditions accessConditions) {
        return withContext(context -> flushDataWithResponse(position, retainUncommittedData, close, httpHeaders,
            accessConditions, context));
    }

    Mono<Response<PathInfo>> flushDataWithResponse(long position, Boolean retainUncommittedData, Boolean close,
        PathHTTPHeaders httpHeaders, PathAccessConditions accessConditions, Context context) {

        httpHeaders = httpHeaders == null ? new PathHTTPHeaders() : httpHeaders;
        accessConditions = accessConditions == null ? new PathAccessConditions() : accessConditions;

        return this.dataLakeStorage.paths().flushDataWithRestResponseAsync(null, position, retainUncommittedData, close,
            (long) 0, null, httpHeaders, accessConditions.getLeaseAccessConditions(),
            accessConditions.getModifiedAccessConditions(), context)
            .map(response -> new SimpleResponse<>(response, new PathInfo(response.getDeserializedHeaders())));
    }

//    public Mono<FileAsyncClient> move(String destinationPath) {
//        return moveWithResponse(destinationPath, null, null, null, null, null, null).flatMap(FluxUtil::toMono);
//    }
//
//    public Mono<Response<FileAsyncClient>> moveWithResponse(String destinationPath,
//        PathHTTPHeaders httpHeaders, Map<String, String> metadata, String permissions, String umask,
//        PathAccessConditions sourceAccessConditions, PathAccessConditions destAccessConditions) {
//        return withContext(context -> moveWithResponse(PathResourceType.FILE, destinationPath, httpHeaders,
//            metadata, permissions, umask, sourceAccessConditions, destAccessConditions, context))
//            .map(response -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
//                response.getHeaders(), new FileAsyncClient(response.getValue().dataLakeStorage,
//                response.getValue().accountName, response.getValue().fileSystemName, response.getValue().pathName,
//                response.getValue().blockBlobAsyncClient)));
//    }

    // Read

    /**
     * Reads the entire file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileAsyncClient.read}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @return A reactive response containing the file data.
     */
    public Flux<ByteBuffer> read() {
        return readWithResponse(null, null, null, false).flatMapMany(Response::getValue);
    }

    /**
     * Reads a range of bytes from a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileAsyncClient.readWithResponse#FileRange-ReliableDownloadOptions-PathAccessConditions-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param range {@link FileRange}
     * @param options {@link ReliableDownloadOptions}
     * @param accessConditions {@link PathAccessConditions}
     * @param rangeGetContentMD5 Whether the contentMD5 for the specified file range should be returned.
     * @return A reactive response containing the file data.
     */
    public Mono<Response<Flux<ByteBuffer>>> readWithResponse(FileRange range, ReliableDownloadOptions options,
        PathAccessConditions accessConditions, boolean rangeGetContentMD5) {
        return blockBlobAsyncClient.downloadWithResponse(Transforms.toBlobRange(range),
            Transforms.toBlobReliableDownloadOptions(options), Transforms.toBlobAccessConditions(accessConditions),
            rangeGetContentMD5);
    }

}
