// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.file.datalake.implementation.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.implementation.models.ModifiedAccessConditions;
import com.azure.storage.file.datalake.implementation.models.PathHTTPHeaders;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DownloadRetryOptions;
import com.azure.storage.file.datalake.models.FileDownloadAsyncResponse;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Map;

import static com.azure.core.implementation.util.FluxUtil.*;

/**
 * This class provides a client that contains file operations for Azure Storage Data Lake. Operations provided by
 * this client include creating a file, deleting a file, renaming a file, setting metadata and
 * http headers, setting and retrieving access control, getting properties, reading a file, and appending and flushing
 * data to write to a file.
 *
 * <p>
 * This client is instantiated through {@link PathClientBuilder} or retrieved via
 * {@link FileSystemAsyncClient#getFileAsyncClient(String)}.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/data-lake-storage-introduction?toc=%2fazure%2fstorage%2fblobs%2ftoc.json>Azure
 * Docs</a> for more information.
 */
public class FileAsyncClient extends PathAsyncClient {

    private final ClientLogger logger = new ClientLogger(FileAsyncClient.class);

    /**
     * Package-private constructor for use by {@link PathClientBuilder}.
     *
     * @param pipeline The pipeline used to send and receive service requests.
     * @param url The endpoint where to send service requests.
     * @param serviceVersion The version of the service to receive requests.
     * @param accountName The storage account name.
     * @param fileSystemName The file system name.
     * @param fileName The file name.
     * @param blockBlobAsyncClient The underlying {@link BlobContainerAsyncClient}
     */
    FileAsyncClient(HttpPipeline pipeline, String url, DataLakeServiceVersion serviceVersion, String accountName,
        String fileSystemName, String fileName, BlockBlobAsyncClient blockBlobAsyncClient) {
        super(pipeline, url, serviceVersion, accountName, fileSystemName, fileName, blockBlobAsyncClient);
    }

    FileAsyncClient(PathAsyncClient pathAsyncClient) {
        super(pathAsyncClient.getHttpPipeline(), pathAsyncClient.getPathUrl(), pathAsyncClient.getServiceVersion(),
            pathAsyncClient.getAccountName(), pathAsyncClient.getFileSystemName(), pathAsyncClient.getObjectPath(),
            pathAsyncClient.getBlockBlobAsyncClient());
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
     * Creates a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileAsyncClient.create}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @return A reactive response containing information about the created file.
     */
    public Mono<PathInfo> create() {
        try {
            return createWithResponse(null, null, null, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileAsyncClient.createWithResponse#PathHTTPHeaders-Map-DataLakeRequestConditions-String-String}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @param headers {@link PathHTTPHeaders}
     * @param metadata Metadata to associate with the resource.
     * @param accessConditions {@link DataLakeRequestConditions}
     * @param permissions POSIX access permissions for the file owner, the file owning group, and others.
     * @param umask Restricts permissions of the file to be created.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * PathItem}.
     */
    public Mono<Response<PathInfo>> createWithResponse(PathHTTPHeaders headers, Map<String, String> metadata,
        DataLakeRequestConditions accessConditions, String permissions, String umask) {
        try {
            return withContext(context -> createWithResponse(PathResourceType.FILE, headers, metadata,
                accessConditions, permissions, umask, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileAsyncClient.delete}
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
     * {@codesnippet com.azure.storage.file.datalake.FileAsyncClient.deleteWithResponse#DataLakeRequestConditions}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param accessConditions {@link DataLakeRequestConditions}
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> deleteWithResponse(DataLakeRequestConditions accessConditions) {
        // TODO (rickle-msft): Update for continuation token if we support HNS off
        try {
            return withContext(context -> deleteWithResponse(null /* recursive */, accessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }

    }

    /**
     * Appends data to the specified resource to later be flushed (written) by a call to flush
     *
     * <p><strong>Code Samples>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileAsyncClient.append#Flux-long-long}
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
        try {
            return appendDataWithResponse(data, offset, length, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Appends data to the specified resource to later be flushed (written) by a call to flush
     *
     * <p><strong>Code Samples>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileAsyncClient.appendWithResponse#Flux-long-long-byte-LeaseAccessConditions}
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
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the file.
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> appendDataWithResponse(Flux<ByteBuffer> data, long offset, long length,
        byte[] contentMd5, LeaseAccessConditions leaseAccessConditions) {
        try {
            return withContext(context -> appendDataWithResponse(data, offset, length, contentMd5,
                leaseAccessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> appendDataWithResponse(Flux<ByteBuffer> data, long offset, long length,
        byte[] contentMd5, LeaseAccessConditions leaseAccessConditions, Context context) {

        leaseAccessConditions = leaseAccessConditions == null ? new LeaseAccessConditions() : leaseAccessConditions;

        PathHTTPHeaders headers = new PathHTTPHeaders().setContentMD5(contentMd5);

        return this.dataLakeStorage.paths().appendDataWithRestResponseAsync(data, offset, null, length, null,
            headers, leaseAccessConditions, context).map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Flushes (writes) data previously appended to the file through a call to append.
     * The previously uploaded data must be contiguous.
     *
     * <p><strong>Code Samples>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileAsyncClient.flush#Long}
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
        try {
            return flushDataWithResponse(position, false, false, null, null).flatMap(FluxUtil::toMono);
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
     * {@codesnippet com.azure.storage.file.datalake.FileAsyncClient.flushWithResponse#Long-boolean-boolean-PathHTTPHeaders-DataLakeRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param position The length of the file after all data has been written.
     * @param retainUncommittedData Whether or not uncommitted data is to be retained after the operation.
     * @param close Whether or not a file changed event raised indicates completion (true) or modification (false).
     * @param httpHeaders {@link PathHTTPHeaders httpHeaders}
     * @param accessConditions {@link DataLakeRequestConditions accessConditions}
     *
     * @return A reactive response containing the information of the created resource.
     */
    public Mono<Response<PathInfo>> flushDataWithResponse(long position, boolean retainUncommittedData, boolean close,
        PathHTTPHeaders httpHeaders, DataLakeRequestConditions accessConditions) {
        try {
            return withContext(context -> flushDataWithResponse(position, retainUncommittedData, close, httpHeaders,
                accessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PathInfo>> flushDataWithResponse(long position, boolean retainUncommittedData, boolean close,
        PathHTTPHeaders httpHeaders, DataLakeRequestConditions accessConditions, Context context) {

        httpHeaders = httpHeaders == null ? new PathHTTPHeaders() : httpHeaders;
        accessConditions = accessConditions == null ? new DataLakeRequestConditions() : accessConditions;

        LeaseAccessConditions lac = new LeaseAccessConditions().setLeaseId(accessConditions.getLeaseId());
        ModifiedAccessConditions mac = new ModifiedAccessConditions()
            .setIfMatch(accessConditions.getIfMatch())
            .setIfNoneMatch(accessConditions.getIfNoneMatch())
            .setIfModifiedSince(accessConditions.getIfModifiedSince())
            .setIfUnmodifiedSince(accessConditions.getIfUnmodifiedSince());

        return this.dataLakeStorage.paths().flushDataWithRestResponseAsync(null, position, retainUncommittedData, close,
            (long) 0, null, httpHeaders, lac, mac, context)
            .map(response -> new SimpleResponse<>(response, new PathInfo(response.getDeserializedHeaders())));
    }

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
        try {
            return readWithResponse(null, null, null, false)
                .flatMapMany(FileDownloadAsyncResponse::getValue);
        } catch (RuntimeException ex) {
            return fluxError(logger, ex);
        }
    }

    /**
     * Reads a range of bytes from a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileAsyncClient.readWithResponse#FileRange-DownloadRetryOptions-DataLakeRequestConditions-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param range {@link FileRange}
     * @param options {@link DownloadRetryOptions}
     * @param accessConditions {@link DataLakeRequestConditions}
     * @param rangeGetContentMD5 Whether the contentMD5 for the specified file range should be returned.
     * @return A reactive response containing the file data.
     */
    public Mono<FileDownloadAsyncResponse> readWithResponse(FileRange range, DownloadRetryOptions options,
        DataLakeRequestConditions accessConditions, boolean rangeGetContentMD5) {
        try {
            return blockBlobAsyncClient.downloadWithResponse(Transforms.toBlobRange(range),
                Transforms.toBlobDownloadRetryOptions(options), Transforms.toBlobRequestConditions(accessConditions),
                rangeGetContentMD5).map(response -> new FileDownloadAsyncResponse(response.getRequest(),
                response.getStatusCode(), response.getHeaders(), response.getValue(),
                response.getDeserializedHeaders()));
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
     * {@codesnippet com.azure.storage.file.datalake.FileAsyncClient.rename#String}
     *
     * @param destinationPath Relative path from the file system to rename the file to.
     * @return A {@link Mono} containing a {@link FileAsyncClient} used to interact with the new file created.
     */
    public Mono<FileAsyncClient> rename(String destinationPath) {
        try {
            return renameWithResponse(destinationPath, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new sub-directory within a directory. If a sub-directory with the same name already exists, the
     * sub-directory will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileAsyncClient.renameWithResponse#String-PathHTTPHeaders-Map-String-String-DataLakeRequestConditions-DataLakeRequestConditions}
     *
     * @param destinationPath Relative path from the file system to rename the file to.
     * @param sourceAccessConditions {@link DataLakeRequestConditions} against the source.
     * @param destAccessConditions {@link DataLakeRequestConditions} against the destination.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * FileAsyncClient} used to interact with the file created.
     */
    public Mono<Response<FileAsyncClient>> renameWithResponse(String destinationPath,
        DataLakeRequestConditions sourceAccessConditions, DataLakeRequestConditions destAccessConditions) {
        try {
            return withContext(context -> renameWithResponse(destinationPath, sourceAccessConditions,
                destAccessConditions, context)).map(response -> new SimpleResponse<>(response,
                    new FileAsyncClient(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }


}
