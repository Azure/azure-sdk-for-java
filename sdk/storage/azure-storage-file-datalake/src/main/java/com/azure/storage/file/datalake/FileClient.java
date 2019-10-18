package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.Utility;
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
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class FileClient extends PathClient {

    private FileAsyncClient fileAsyncClient;
    private BlockBlobClient blockBlobClient;
    private ClientLogger logger = new ClientLogger(FileClient.class);

    protected FileClient(FileAsyncClient pathAsyncClient, BlockBlobClient blockBlobClient) {
        super(pathAsyncClient, blockBlobClient);
        this.fileAsyncClient = pathAsyncClient;
        this.blockBlobClient = blockBlobClient;
    }

    /**
     * Creates the resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathClient.create}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @return Information about the created resource.
     */
    public PathItem create() {
        return createWithResponse(null, null, null, null, null, null, Context.NONE).getValue();
    }

    public Response<PathItem> createWithResponse(PathHTTPHeaders httpHeaders, Map<String, String> metadata,
        String permissions, String umask, PathAccessConditions accessConditions, Duration timeout, Context context) {
        Mono<Response<PathItem>> response = client.createWithResponse(PathResourceType.FILE, httpHeaders, metadata,
            permissions, umask, accessConditions, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    public void delete() {
        deleteWithResponse(null, null, null).getValue();
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
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A reactive response signalling completion.
     */
    public Response<Void> deleteWithResponse(PathAccessConditions accessConditions, Duration timeout, Context context) {
        // TODO (rickle-msft): Update for continuation token if we support HNS off
        Mono<Response<Void>> response = client.deleteWithResponse(null, accessConditions, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

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
     * @param length The exact length of the data.
     *
     * @return A response signalling completion.
     */
    public void appendData(InputStream data, long offset, long length) {
        appendDataWithResponse(data, offset, length, null, null, null, Context.NONE);
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
     * @param length The exact length of the data.
     * @param contentMd5 An MD5 hash of the content of the data. If specified, the service will calculate the MD5 of the
     * received data and fail the request if it does not match the provided MD5.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response signalling completion.
     */
    public Response<Void> appendDataWithResponse(InputStream data, long offset, long length,
        byte[] contentMd5, LeaseAccessConditions leaseAccessConditions, Duration timeout, Context context) {

        Objects.requireNonNull(data);
        Flux<ByteBuffer> fbb = Utility.convertStreamToByteBuffer(data, length,
            BlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE);
        Mono<Response<Void>> response = fileAsyncClient.appendDataWithResponse(fbb.subscribeOn(Schedulers.elastic()),
            offset, length, contentMd5, leaseAccessConditions, context);

        try {
            return Utility.blockWithOptionalTimeout(response, timeout);
        } catch (UncheckedIOException e) {
            throw logger.logExceptionAsError(e);
        }
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
     * @return A response containing the information of the created resource.
     */
    public PathInfo flushData(long position) {
        return flushDataWithResponse(position, null, null, null, null, null, null).getValue();
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
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing the information of the created resource.
     */
    public Response<PathInfo> flushDataWithResponse(long position, Boolean retainUncommittedData, Boolean close,
        PathHTTPHeaders httpHeaders, PathAccessConditions accessConditions, Duration timeout, Context context) {
        Mono<Response<PathInfo>> response =  fileAsyncClient.flushDataWithResponse(position, retainUncommittedData,
            close, httpHeaders, accessConditions, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Downloads the entire file into an output stream.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileClient.read#OutputStream}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param stream A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null
     */
    public void read(OutputStream stream) {
        readWithResponse(stream, null, null, null, false, null, Context.NONE);
    }

    /**
     * Downloads a range of bytes from a file into an output stream.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.FileClient.readWithResponse#OutputStream-BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param stream A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @param range {@link FileRange}
     * @param options {@link ReliableDownloadOptions}
     * @param accessConditions {@link PathAccessConditions}
     * @param rangeGetContentMD5 Whether the contentMD5 for the specified file range should be returned.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null
     */
    public Response<Void> readWithResponse(OutputStream stream, FileRange range, ReliableDownloadOptions options,
        PathAccessConditions accessConditions, boolean rangeGetContentMD5, Duration timeout, Context context) {
        return blockBlobClient.downloadWithResponse(stream, Transforms.toBlobRange(range),
            Transforms.toBlobReliableDownloadOptions(options), Transforms.toBlobAccessConditions(accessConditions),
            rangeGetContentMD5, timeout, context);
    }
}
