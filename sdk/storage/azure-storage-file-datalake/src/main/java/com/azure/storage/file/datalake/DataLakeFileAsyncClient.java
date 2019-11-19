// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;


import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.file.datalake.implementation.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.implementation.models.ModifiedAccessConditions;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DownloadRetryOptions;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.FileReadAsyncResponse;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a client that contains file operations for Azure Storage Data Lake. Operations provided by
 * this client include creating a file, deleting a file, renaming a file, setting metadata and
 * http headers, setting and retrieving access control, getting properties, reading a file, and appending and flushing
 * data to write to a file.
 *
 * <p>
 * This client is instantiated through {@link DataLakePathClientBuilder} or retrieved via
 * {@link DataLakeFileSystemAsyncClient#getFileAsyncClient(String)}.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/data-lake-storage-introduction?toc=%2fazure%2fstorage%2fblobs%2ftoc.json>Azure
 * Docs</a> for more information.
 */
public class DataLakeFileAsyncClient extends DataLakePathAsyncClient {

    private final ClientLogger logger = new ClientLogger(DataLakeFileAsyncClient.class);

    /**
     * Package-private constructor for use by {@link DataLakePathClientBuilder}.
     *
     * @param pipeline The pipeline used to send and receive service requests.
     * @param url The endpoint where to send service requests.
     * @param serviceVersion The version of the service to receive requests.
     * @param accountName The storage account name.
     * @param fileSystemName The file system name.
     * @param fileName The file name.
     * @param blockBlobAsyncClient The underlying {@link BlobContainerAsyncClient}
     */
    DataLakeFileAsyncClient(HttpPipeline pipeline, String url, DataLakeServiceVersion serviceVersion,
        String accountName, String fileSystemName, String fileName, BlockBlobAsyncClient blockBlobAsyncClient) {
        super(pipeline, url, serviceVersion, accountName, fileSystemName, fileName, PathResourceType.FILE,
            blockBlobAsyncClient);
    }

    DataLakeFileAsyncClient(DataLakePathAsyncClient pathAsyncClient) {
        super(pathAsyncClient.getHttpPipeline(), pathAsyncClient.getPathUrl(), pathAsyncClient.getServiceVersion(),
            pathAsyncClient.getAccountName(), pathAsyncClient.getFileSystemName(), pathAsyncClient.getObjectPath(),
            PathResourceType.FILE, pathAsyncClient.getBlockBlobAsyncClient());
    }

    /**
     * Gets the URL of the file represented by this client on the Data Lake service.
     *
     * @return the URL.
     */
    public String getFileUrl() {
        return getPathUrl();
    }

    /**
     * Gets the path of this file, not including the name of the resource itself.
     *
     * @return The path of the file.
     */
    public String getFilePath() {
        return getObjectPath();
    }

    /**
     * Gets the name of this file, not including its full path.
     *
     * @return The name of the file.
     */
    public String getFileName() {
        return getObjectName();
    }

    /**
     * Deletes a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.delete}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Void> delete() {
        try {
            return deleteWithResponse(null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.deleteWithResponse#DataLakeRequestConditions}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param requestConditions {@link DataLakeRequestConditions}
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> deleteWithResponse(DataLakeRequestConditions requestConditions) {
        // TODO (rickle-msft): Update for continuation token if we support HNS off
        try {
            return withContext(context -> deleteWithResponse(null /* recursive */, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }

    }

    /**
     * Appends data to the specified resource to later be flushed (written) by a call to flush
     *
     * <p><strong>Code Samples>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.append#Flux-long-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param data The data to write to the file.
     * @param fileOffset The position where the data is to be appended.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Void> append(Flux<ByteBuffer> data, long fileOffset, long length) {
        try {
            return appendWithResponse(data, fileOffset, length, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Appends data to the specified resource to later be flushed (written) by a call to flush
     *
     * <p><strong>Code Samples>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.appendWithResponse#Flux-long-long-byte-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param data The data to write to the file.
     * @param fileOffset The position where the data is to be appended.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     * @param contentMd5 An MD5 hash of the content of the data. If specified, the service will calculate the MD5 of the
     * received data and fail the request if it does not match the provided MD5.
     * @param leaseId By setting lease id, requests will fail if the provided lease does not match the active lease on
     * the file.
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> appendWithResponse(Flux<ByteBuffer> data, long fileOffset, long length,
        byte[] contentMd5, String leaseId) {
        try {
            return withContext(context -> appendWithResponse(data, fileOffset, length, contentMd5,
                leaseId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> appendWithResponse(Flux<ByteBuffer> data, long fileOffset, long length,
        byte[] contentMd5, String leaseId, Context context) {

        LeaseAccessConditions leaseAccessConditions = new LeaseAccessConditions().setLeaseId(leaseId);

        PathHttpHeaders headers = new PathHttpHeaders().setTransactionalContentHash(contentMd5);

        return this.dataLakeStorage.paths().appendDataWithRestResponseAsync(data, fileOffset, null, length, null,
            headers, leaseAccessConditions, context).map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Flushes (writes) data previously appended to the file through a call to append.
     * The previously uploaded data must be contiguous.
     *
     * <p><strong>Code Samples>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.flush#long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param position The length of the file after all data has been written.
     *
     * @return A reactive response containing the information of the created resource.
     */
    public Mono<PathInfo> flush(long position) {
        try {
            return flushWithResponse(position, false, false, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Flushes (writes) data previously appended to the file through a call to append.
     * The previously uploaded data must be contiguous.
     *
     * <p><strong>Code Samples>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.flushWithResponse#long-boolean-boolean-PathHttpHeaders-DataLakeRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param position The length of the file after all data has been written.
     * @param retainUncommittedData Whether or not uncommitted data is to be retained after the operation.
     * @param close Whether or not a file changed event raised indicates completion (true) or modification (false).
     * @param httpHeaders {@link PathHttpHeaders httpHeaders}
     * @param requestConditions {@link DataLakeRequestConditions requestConditions}
     *
     * @return A reactive response containing the information of the created resource.
     */
    public Mono<Response<PathInfo>> flushWithResponse(long position, boolean retainUncommittedData, boolean close,
        PathHttpHeaders httpHeaders, DataLakeRequestConditions requestConditions) {
        try {
            return withContext(context -> flushWithResponse(position, retainUncommittedData, close, httpHeaders,
                requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PathInfo>> flushWithResponse(long position, boolean retainUncommittedData, boolean close,
        PathHttpHeaders httpHeaders, DataLakeRequestConditions requestConditions, Context context) {

        httpHeaders = httpHeaders == null ? new PathHttpHeaders() : httpHeaders;
        requestConditions = requestConditions == null ? new DataLakeRequestConditions() : requestConditions;

        LeaseAccessConditions lac = new LeaseAccessConditions().setLeaseId(requestConditions.getLeaseId());
        ModifiedAccessConditions mac = new ModifiedAccessConditions()
            .setIfMatch(requestConditions.getIfMatch())
            .setIfNoneMatch(requestConditions.getIfNoneMatch())
            .setIfModifiedSince(requestConditions.getIfModifiedSince())
            .setIfUnmodifiedSince(requestConditions.getIfUnmodifiedSince());

        return this.dataLakeStorage.paths().flushDataWithRestResponseAsync(null, position, retainUncommittedData, close,
            (long) 0, null, httpHeaders, lac, mac, context)
            .map(response -> new SimpleResponse<>(response, new PathInfo(response.getDeserializedHeaders().getETag(),
                response.getDeserializedHeaders().getLastModified())));
    }

    /**
     * Reads the entire file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.read}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @return A reactive response containing the file data.
     */
    public Flux<ByteBuffer> read() {
        try {
            return readWithResponse(null, null, null, false)
                .flatMapMany(FileReadAsyncResponse::getValue);
        } catch (RuntimeException ex) {
            return fluxError(logger, ex);
        }
    }

    /**
     * Reads a range of bytes from a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.readWithResponse#FileRange-DownloadRetryOptions-DataLakeRequestConditions-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param range {@link FileRange}
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param getRangeContentMd5 Whether the contentMD5 for the specified file range should be returned.
     * @return A reactive response containing the file data.
     */
    public Mono<FileReadAsyncResponse> readWithResponse(FileRange range, DownloadRetryOptions options,
        DataLakeRequestConditions requestConditions, boolean getRangeContentMd5) {
        try {
            return blockBlobAsyncClient.downloadWithResponse(Transforms.toBlobRange(range),
                Transforms.toBlobDownloadRetryOptions(options), Transforms.toBlobRequestConditions(requestConditions),
                getRangeContentMd5).map(Transforms::toFileReadAsyncResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Moves the file to another location within the file system.
     * For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.rename#String}
     *
     * @param destinationPath Relative path from the file system to rename the file to, excludes the file system name.
     * For example if you want to move a file with fileSystem = "myfilesystem", path = "mydir/hello.txt" to another path
     * in myfilesystem (ex: newdir/hi.txt) then set the destinationPath = "newdir/hi.txt"
     * @return A {@link Mono} containing a {@link DataLakeFileAsyncClient} used to interact with the new file created.
     */
    public Mono<DataLakeFileAsyncClient> rename(String destinationPath) {
        try {
            return renameWithResponse(destinationPath, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Moves the file to another location within the file system. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.renameWithResponse#String-DataLakeRequestConditions-DataLakeRequestConditions}
     *
     * @param destinationPath Relative path from the file system to rename the file to, excludes the file system name.
     * For example if you want to move a file with fileSystem = "myfilesystem", path = "mydir/hello.txt" to another path
     * in myfilesystem (ex: newdir/hi.txt) then set the destinationPath = "newdir/hi.txt"
     * @param sourceRequestConditions {@link DataLakeRequestConditions} against the source.
     * @param destinationRequestConditions {@link DataLakeRequestConditions} against the destination.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeFileAsyncClient} used to interact with the file created.
     */
    public Mono<Response<DataLakeFileAsyncClient>> renameWithResponse(String destinationPath,
        DataLakeRequestConditions sourceRequestConditions, DataLakeRequestConditions destinationRequestConditions) {
        try {
            return withContext(context -> renameWithResponse(destinationPath, sourceRequestConditions,
                destinationRequestConditions, context)).map(response -> new SimpleResponse<>(response,
                    new DataLakeFileAsyncClient(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }


}
