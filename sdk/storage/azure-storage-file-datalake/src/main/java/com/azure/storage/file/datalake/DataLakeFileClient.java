// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobQueryResponse;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.FluxInputStream;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.implementation.UploadUtils;
import com.azure.storage.file.datalake.implementation.models.InternalDataLakeFileOpenInputStreamResult;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.DownloadRetryOptions;
import com.azure.storage.file.datalake.models.DataLakeFileOpenInputStreamResult;
import com.azure.storage.file.datalake.models.FileQueryAsyncResponse;
import com.azure.storage.file.datalake.options.DataLakeFileInputStreamOptions;
import com.azure.storage.file.datalake.options.FileParallelUploadOptions;
import com.azure.storage.file.datalake.options.FileQueryOptions;
import com.azure.storage.file.datalake.models.FileQueryResponse;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.FileReadResponse;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.options.FileScheduleDeletionOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This class provides a client that contains file operations for Azure Storage Data Lake. Operations provided by
 * this client include creating a file, deleting a file, renaming a file, setting metadata and
 * http headers, setting and retrieving access control, getting properties, reading a file, and appending and flushing
 * data to write to a file.
 *
 * <p>
 * This client is instantiated through {@link DataLakePathClientBuilder} or retrieved via
 * {@link DataLakeFileSystemClient#getFileClient(String) getFileClient}.
 *
 * <p>
 * Please refer to the
 *
 * <a href="https://docs.microsoft.com/azure/storage/blobs/data-lake-storage-introduction">Azure
 * Docs</a> for more information.
 */
@ServiceClient(builder = DataLakePathClientBuilder.class)
public class DataLakeFileClient extends DataLakePathClient {

    /**
     * Indicates the maximum number of bytes that can be sent in a call to upload.
     */
    private static final long MAX_APPEND_FILE_BYTES = DataLakeFileAsyncClient.MAX_APPEND_FILE_BYTES;

    private final ClientLogger logger = new ClientLogger(DataLakeFileClient.class);

    private final DataLakeFileAsyncClient dataLakeFileAsyncClient;

    DataLakeFileClient(DataLakeFileAsyncClient pathAsyncClient, BlockBlobClient blockBlobClient) {
        super(pathAsyncClient, blockBlobClient);
        this.dataLakeFileAsyncClient = pathAsyncClient;
    }

    private DataLakeFileClient(DataLakePathClient dataLakePathClient) {
        super(dataLakePathClient.dataLakePathAsyncClient, dataLakePathClient.blockBlobClient);
        this.dataLakeFileAsyncClient = new DataLakeFileAsyncClient(dataLakePathClient.dataLakePathAsyncClient);
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.delete}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete() {
        deleteWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Deletes a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.deleteWithResponse#DataLakeRequestConditions-Duration-Context}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(DataLakeRequestConditions requestConditions, Duration timeout,
        Context context) {
        // TODO (rickle-msft): Update for continuation token if we support HNS off
        Mono<Response<Void>> response = dataLakePathAsyncClient.deleteWithResponse(null, requestConditions, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Creates a new file. By default this method will not overwrite an existing file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.upload#InputStream-long}
     *
     * @param data The data to write to the blob. The data must be markable. This is in order to support retries. If
     * the data is not markable, consider wrapping your data source in a {@link java.io.BufferedInputStream} to add mark
     * support.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     * @return Information about the uploaded path.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PathInfo upload(InputStream data, long length) {
        return upload(data, length, false);
    }

    /**
     * Creates a new file, or updates the content of an existing file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.upload#InputStream-long-boolean}
     *
     * @param data The data to write to the blob. The data must be markable. This is in order to support retries. If
     * the data is not markable, consider wrapping your data source in a {@link java.io.BufferedInputStream} to add mark
     * support.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     * @param overwrite Whether or not to overwrite, should data exist on the bfilelob.
     * @return Information about the uploaded path.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PathInfo upload(InputStream data, long length, boolean overwrite) {
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions();
        if (!overwrite) {
            requestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        return uploadWithResponse(new FileParallelUploadOptions(data, length).setRequestConditions(requestConditions),
            null, Context.NONE).getValue();
    }

    /**
     * Creates a new file.
     * To avoid overwriting, pass "*" to {@link DataLakeRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.uploadWithResponse#FileParallelUploadOptions-Duration-Context}
     *
     * @param options {@link FileParallelUploadOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return Information about the uploaded path.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PathInfo> uploadWithResponse(FileParallelUploadOptions options, Duration timeout,
        Context context) {
        Objects.requireNonNull(options);
        Mono<Response<PathInfo>> upload = this.dataLakeFileAsyncClient.uploadWithResponse(options)
            .subscriberContext(FluxUtil.toReactorContext(context));

        try {
            return StorageImplUtils.blockWithOptionalTimeout(upload, timeout);
        } catch (UncheckedIOException e) {
            throw logger.logExceptionAsError(e);
        }
    }

    /**
     * Creates a file, with the content of the specified file. By default this method will not overwrite an
     * existing file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.uploadFromFile#String}
     *
     * @param filePath Path of the file to upload
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void uploadFromFile(String filePath) {
        uploadFromFile(filePath, false);
    }

    /**
     * Creates a file, with the content of the specified file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.uploadFromFile#String-boolean}
     *
     * @param filePath Path of the file to upload
     * @param overwrite Whether or not to overwrite, should the file already exist
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void uploadFromFile(String filePath, boolean overwrite) {
        DataLakeRequestConditions requestConditions = null;

        if (!overwrite) {
            // Note we only want to make the exists call if we will be uploading in stages. Otherwise it is superfluous.
            if (UploadUtils.shouldUploadInChunks(filePath, DataLakeFileClient.MAX_APPEND_FILE_BYTES, logger)
                && exists()) {
                throw logger.logExceptionAsError(new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS));
            }
            requestConditions = new DataLakeRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        uploadFromFile(filePath, null, null, null, requestConditions, null);
    }

    /**
     * Creates a file, with the content of the specified file.
     * <p>
     * To avoid overwriting, pass "*" to {@link DataLakeRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.uploadFromFile#String-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions-Duration}
     *
     * @param filePath Path of the file to upload
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions,
        PathHttpHeaders headers, Map<String, String> metadata, DataLakeRequestConditions requestConditions,
        Duration timeout) {
        Mono<Void> upload = this.dataLakeFileAsyncClient.uploadFromFile(
            filePath, parallelTransferOptions, headers, metadata, requestConditions);

        try {
            StorageImplUtils.blockWithOptionalTimeout(upload, timeout);
        } catch (UncheckedIOException e) {
            throw logger.logExceptionAsError(e);
        }
    }

    /**
     * Appends data to the specified resource to later be flushed (written) by a call to flush
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.append#InputStream-long-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param data The data to write to the file.
     * @param fileOffset The position where the data is to be appended.
     * @param length The exact length of the data.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void append(InputStream data, long fileOffset, long length) {
        appendWithResponse(data, fileOffset, length, null, null, null, Context.NONE);
    }

    /**
     * Appends data to the specified resource to later be flushed (written) by a call to flush
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.appendWithResponse#InputStream-long-long-byte-String-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param data The data to write to the file.
     * @param fileOffset The position where the data is to be appended.
     * @param length The exact length of the data.
     * @param contentMd5 An MD5 hash of the content of the data. If specified, the service will calculate the MD5 of the
     * received data and fail the request if it does not match the provided MD5.
     * @param leaseId By setting lease id, requests will fail if the provided lease does not match the active lease on
     * the file.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> appendWithResponse(InputStream data, long fileOffset, long length,
        byte[] contentMd5, String leaseId, Duration timeout, Context context) {

        Objects.requireNonNull(data);
        Flux<ByteBuffer> fbb = Utility.convertStreamToByteBuffer(data, length,
            BlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE, true);
        Mono<Response<Void>> response = dataLakeFileAsyncClient.appendWithResponse(
            fbb.subscribeOn(Schedulers.elastic()), fileOffset, length, contentMd5, leaseId, context);

        try {
            return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
        } catch (UncheckedIOException e) {
            throw logger.logExceptionAsError(e);
        }
    }

    /**
     * Flushes (writes) data previously appended to the file through a call to append.
     * The previously uploaded data must be contiguous.
     * <p>By default this method will not overwrite existing data.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.flush#long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param position The length of the file after all data has been written.
     *
     * @return Information about the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PathInfo flush(long position) {
        return flush(position, false);
    }

    /**
     * Flushes (writes) data previously appended to the file through a call to append.
     * The previously uploaded data must be contiguous.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.flush#long-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param position The length of the file after all data has been written.
     * @param overwrite Whether or not to overwrite, should data exist on the file.
     *
     * @return Information about the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PathInfo flush(long position, boolean overwrite) {
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions();
        if (overwrite) {
            requestConditions = new DataLakeRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        return flushWithResponse(position, false, false, null, requestConditions, null, Context.NONE).getValue();
    }

    /**
     * Flushes (writes) data previously appended to the file through a call to append.
     * The previously uploaded data must be contiguous.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.flushWithResponse#long-boolean-boolean-PathHttpHeaders-DataLakeRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param position The length of the file after all data has been written.
     * @param retainUncommittedData Whether or not uncommitted data is to be retained after the operation.
     * @param close Whether or not a file changed event raised indicates completion (true) or modification (false).
     * @param httpHeaders {@link PathHttpHeaders httpHeaders}
     * @param requestConditions {@link DataLakeRequestConditions requestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing the information of the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PathInfo> flushWithResponse(long position, boolean retainUncommittedData, boolean close,
        PathHttpHeaders httpHeaders, DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        Mono<Response<PathInfo>> response =  dataLakeFileAsyncClient.flushWithResponse(position, retainUncommittedData,
            close, httpHeaders, requestConditions, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Reads the entire file into an output stream.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.read#OutputStream}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param stream A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null
     */
    public void read(OutputStream stream) {
        readWithResponse(stream, null, null, null, false, null, Context.NONE);
    }

    /**
     * Reads a range of bytes from a file into an output stream.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.readWithResponse#OutputStream-FileRange-DownloadRetryOptions-DataLakeRequestConditions-boolean-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param stream A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @param range {@link FileRange}
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param getRangeContentMd5 Whether the contentMD5 for the specified file range should be returned.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing status code and HTTP headers.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null
     */
    public FileReadResponse readWithResponse(OutputStream stream, FileRange range, DownloadRetryOptions options,
        DataLakeRequestConditions requestConditions, boolean getRangeContentMd5, Duration timeout, Context context) {
        return DataLakeImplUtils.returnOrConvertException(() -> {
            BlobDownloadResponse response = blockBlobClient.downloadWithResponse(stream, Transforms.toBlobRange(range),
                Transforms.toBlobDownloadRetryOptions(options), Transforms.toBlobRequestConditions(requestConditions),
                getRangeContentMd5, timeout, context);
            return Transforms.toFileReadResponse(response);
        }, logger);
    }

    /**
     * Opens a file input stream to download the file. Locks on ETags.
     *
     * @return An {@link InputStream} object that represents the stream to use for reading from the file.
     * @throws DataLakeStorageException If a storage service error occurred.
     */
    public DataLakeFileOpenInputStreamResult openInputStream() {
        return openInputStream(null);
    }

    /**
     * Opens a file input stream to download the specified range of the file. Defaults to ETag locking if the option
     * is not specified.
     *
     * @param options {@link DataLakeFileInputStreamOptions}
     * @return A {@link DataLakeFileOpenInputStreamResult} object that contains the stream to use for reading from the file.
     * @throws DataLakeStorageException If a storage service error occurred.
     */
    public DataLakeFileOpenInputStreamResult openInputStream(DataLakeFileInputStreamOptions options) {
        BlobInputStreamOptions convertedOptions = Transforms.toBlobInputStreamOptions(options);
        BlobInputStream inputStream = blockBlobClient.openInputStream(convertedOptions);
        return new InternalDataLakeFileOpenInputStreamResult(inputStream,
            Transforms.toPathProperties(inputStream.getProperties()));
    }

    /**
     * Reads the entire file into a file specified by the path.
     *
     * <p>The file will be created and must not exist, if the file already exists a {@link FileAlreadyExistsException}
     * will be thrown.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.readToFile#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @return The file properties and metadata.
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PathProperties readToFile(String filePath) {
        return readToFile(filePath, false);
    }

    /**
     * Reads the entire file into a file specified by the path.
     *
     * <p>If overwrite is set to false, the file will be created and must not exist, if the file already exists a
     * {@link FileAlreadyExistsException} will be thrown.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.readToFile#String-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @param overwrite Whether or not to overwrite the file, should the file exist.
     * @return The file properties and metadata.
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PathProperties readToFile(String filePath, boolean overwrite) {
        Set<OpenOption> openOptions = null;
        if (overwrite) {
            openOptions = new HashSet<>();
            openOptions.add(StandardOpenOption.CREATE);
            openOptions.add(StandardOpenOption.TRUNCATE_EXISTING); // If the file already exists and it is opened
            // for WRITE access, then its length is truncated to 0.
            openOptions.add(StandardOpenOption.READ);
            openOptions.add(StandardOpenOption.WRITE);
        }
        return readToFileWithResponse(filePath, null, null, null, null, false, openOptions, null, Context.NONE)
            .getValue();
    }

    /**
     * Reads the entire file into a file specified by the path.
     *
     * <p>By default the file will be created and must not exist, if the file already exists a
     * {@link FileAlreadyExistsException} will be thrown. To override this behavior, provide appropriate
     * {@link OpenOption OpenOptions} </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.readToFileWithResponse#String-FileRange-ParallelTransferOptions-DownloadRetryOptions-DataLakeRequestConditions-boolean-Set-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @param range {@link FileRange}
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to download to file. Number of parallel
     * transfers parameter is ignored.
     * @param downloadRetryOptions {@link DownloadRetryOptions}
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param rangeGetContentMd5 Whether the contentMD5 for the specified file range should be returned.
     * @param openOptions {@link OpenOption OpenOptions} to use to configure how to open or create the file.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the file properties and metadata.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PathProperties> readToFileWithResponse(String filePath, FileRange range,
        ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions downloadRetryOptions,
        DataLakeRequestConditions requestConditions, boolean rangeGetContentMd5, Set<OpenOption> openOptions,
        Duration timeout, Context context) {
        return DataLakeImplUtils.returnOrConvertException(() -> {
            Response<BlobProperties> response = blockBlobClient.downloadToFileWithResponse(
                new BlobDownloadToFileOptions(filePath)
                    .setRange(Transforms.toBlobRange(range)).setParallelTransferOptions(parallelTransferOptions)
                    .setDownloadRetryOptions(Transforms.toBlobDownloadRetryOptions(downloadRetryOptions))
                    .setRequestConditions(Transforms.toBlobRequestConditions(requestConditions))
                    .setRetrieveContentRangeMd5(rangeGetContentMd5).setOpenOptions(openOptions), timeout,
                context);
            return new SimpleResponse<>(response, Transforms.toPathProperties(response.getValue()));
        }, logger);
    }

    /**
     * Moves the file to another location within the file system.
     * For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.rename#String-String}
     *
     * @param destinationFileSystem The file system of the destination within the account.
     * {@code null} for the current file system.
     * @param destinationPath Relative path from the file system to rename the file to, excludes the file system name.
     * For example if you want to move a file with fileSystem = "myfilesystem", path = "mydir/hello.txt" to another path
     * in myfilesystem (ex: newdir/hi.txt) then set the destinationPath = "newdir/hi.txt"
     * @return A {@link DataLakeFileClient} used to interact with the new file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakeFileClient rename(String destinationFileSystem, String destinationPath) {
        return renameWithResponse(destinationFileSystem, destinationPath, null, null, null, null).getValue();
    }

    /**
     * Moves the file to another location within the file system.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.renameWithResponse#String-String-DataLakeRequestConditions-DataLakeRequestConditions-Duration-Context}
     *
     * @param destinationFileSystem The file system of the destination within the account.
     * {@code null} for the current file system.
     * @param destinationPath Relative path from the file system to rename the file to, excludes the file system name.
     * For example if you want to move a file with fileSystem = "myfilesystem", path = "mydir/hello.txt" to another path
     * in myfilesystem (ex: newdir/hi.txt) then set the destinationPath = "newdir/hi.txt"
     * @param sourceRequestConditions {@link DataLakeRequestConditions} against the source.
     * @param destinationRequestConditions {@link DataLakeRequestConditions} against the destination.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} that contains a {@link DataLakeFileClient}
     * used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakeFileClient> renameWithResponse(String destinationFileSystem, String destinationPath,
        DataLakeRequestConditions sourceRequestConditions, DataLakeRequestConditions destinationRequestConditions,
        Duration timeout, Context context) {

        Mono<Response<DataLakePathClient>> response = renameWithResponse(destinationFileSystem, destinationPath,
            sourceRequestConditions, destinationRequestConditions, context);

        Response<DataLakePathClient> resp = StorageImplUtils.blockWithOptionalTimeout(response, timeout);
        return new SimpleResponse<>(resp, new DataLakeFileClient(resp.getValue()));
    }

    /**
     * Opens an input stream to query the file.
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/query-blob-contents">Azure Docs</a></p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.openQueryInputStream#String}
     *
     * @param expression The query expression.
     * @return An <code>InputStream</code> object that represents the stream to use for reading the query response.
     */
    public InputStream openQueryInputStream(String expression) {
        return openQueryInputStreamWithResponse(new FileQueryOptions(expression)).getValue();
    }

    /**
     * Opens an input stream to query the file.
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/query-blob-contents">Azure Docs</a></p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.openQueryInputStream#FileQueryOptions}
     *
     * @param queryOptions {@link FileQueryOptions The query options}.
     * @return A response containing status code and HTTP headers including an <code>InputStream</code> object
     * that represents the stream to use for reading the query response.
     */
    public Response<InputStream> openQueryInputStreamWithResponse(FileQueryOptions queryOptions) {

        // Data to subscribe to and read from.
        FileQueryAsyncResponse response = dataLakeFileAsyncClient.queryWithResponse(queryOptions)
            .block();

        // Create input stream from the data.
        if (response == null) {
            throw logger.logExceptionAsError(new IllegalStateException("Query response cannot be null"));
        }
        return new ResponseBase<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            new FluxInputStream(response.getValue()), response.getDeserializedHeaders());
    }

    /**
     * Queries an entire file into an output stream.
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/query-blob-contents">Azure Docs</a></p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.query#OutputStream-String}
     *
     * @param stream A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @param expression The query expression.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null.
     */
    public void query(OutputStream stream, String expression) {
        queryWithResponse(new FileQueryOptions(expression, stream), null, Context.NONE);
    }

    /**
     * Queries an entire file into an output stream.
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/query-blob-contents">Azure Docs</a></p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.queryWithResponse#FileQueryOptions-Duration-Context}
     *
     * @param queryOptions {@link FileQueryOptions The query options}.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null.
     */
    public FileQueryResponse queryWithResponse(FileQueryOptions queryOptions, Duration timeout, Context context) {
        return DataLakeImplUtils.returnOrConvertException(() -> {
            BlobQueryResponse response = blockBlobClient.queryWithResponse(
                Transforms.toBlobQueryOptions(queryOptions), timeout, context);
            return Transforms.toFileQueryResponse(response);
        }, logger);
    }

    // TODO (kasobol-msft) add REST DOCS
    /**
     * Schedules the file for deletion.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.scheduleDeletion#FileScheduleDeletionOptions}
     *
     * @param options Schedule deletion parameters.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void scheduleDeletion(FileScheduleDeletionOptions options) {
        this.scheduleDeletionWithResponse(options, null, Context.NONE);
    }

    // TODO (kasobol-msft) add REST DOCS
    /**
     * Schedules the file for deletion.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileClient.scheduleDeletionWithResponse#FileScheduleDeletionOptions-Duration-Context}
     *
     * @param options Schedule deletion parameters.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> scheduleDeletionWithResponse(FileScheduleDeletionOptions options,
        Duration timeout, Context context) {
        Mono<Response<Void>> response = this.dataLakeFileAsyncClient.scheduleDeletionWithResponse(options, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }
}
