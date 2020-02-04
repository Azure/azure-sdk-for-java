// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;


import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.ProgressReporter;
import com.azure.storage.common.implementation.UploadBufferPool;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.UploadUtils;
import com.azure.storage.file.datalake.implementation.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.implementation.models.ModifiedAccessConditions;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.implementation.util.ModelHelper;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DownloadRetryOptions;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.FileReadAsyncResponse;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;

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
 * Please refer to the
 *
 * <a href="https://docs.microsoft.com/en-us/azure/storage/blobs/data-lake-storage-introduction?toc=%2fazure%2fstorage%2fblobs%2ftoc.json">Azure
 * Docs</a> for more information.
 */
public class DataLakeFileAsyncClient extends DataLakePathAsyncClient {

    /**
     * Indicates the maximum number of bytes that can be sent in a call to upload.
     */
    public static final int MAX_APPEND_FILE_BYTES = 100 * Constants.MB;

    /**
     * The block size to use if none is specified in parallel operations.
     */
    public static final int FILE_DEFAULT_UPLOAD_BLOCK_SIZE = 4 * Constants.MB;

    /**
     * The number of buffers to use if none is specified on the buffered upload method.
     */
    public static final int FILE_DEFAULT_NUMBER_OF_BUFFERS = 8;

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
     * Creates a new file. By default this method will not overwrite an existing file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.upload#Flux-ParallelTransferOptions}
     *
     * @param data The data to write to the file. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @return A reactive response containing the information of the uploaded file.
     */
    public Mono<PathInfo> upload(Flux<ByteBuffer> data, long fileOffset, long length, boolean retainUncommittedData,
        boolean close, ParallelTransferOptions parallelTransferOptions) {
        return upload(data, fileOffset, length, retainUncommittedData, close, parallelTransferOptions, false);
    }

    /**
     * Creates a new file, or updates the content of an existing file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.upload#Flux-ParallelTransferOptions-boolean}
     *
     * @param data The data to write to the file. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param overwrite Whether or not to overwrite, should the file already exist.
     * @return A reactive response containing the information of the uploaded file.
     */
    public Mono<PathInfo> upload(Flux<ByteBuffer> data, long fileOffset, long length, boolean retainUncommittedData,
        boolean close, ParallelTransferOptions parallelTransferOptions,
        boolean overwrite) {

        Mono<Void> overwriteCheck;
        DataLakeRequestConditions requestConditions;

        if (overwrite) {
            overwriteCheck = Mono.empty();
            requestConditions = null;
        } else {
            overwriteCheck = exists().flatMap(exists -> exists
                ? monoError(logger, new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS))
                : Mono.empty());
            requestConditions = new DataLakeRequestConditions()
                .setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }

        return overwriteCheck
            .then(uploadWithResponse(data, fileOffset, length, retainUncommittedData, close,
                parallelTransferOptions, null, requestConditions))
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new file, or updates the content of an existing file.
     * <p>
     * To avoid overwriting, pass "*" to {@link DataLakeRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions}
     *
     * <p><strong>Using Progress Reporting</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions.ProgressReporter}
     *
     * @param data The data to write to the file. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link PathHttpHeaders}
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A reactive response containing the information of the uploaded file.
     */
    public Mono<Response<PathInfo>> uploadWithResponse(Flux<ByteBuffer> data,
        long fileOffset, long length, boolean retainUncommittedData, boolean close,
        ParallelTransferOptions parallelTransferOptions, PathHttpHeaders headers,
        DataLakeRequestConditions requestConditions) {
        try {
            Objects.requireNonNull(data, "'data' must not be null");
            DataLakeRequestConditions validatedRequestConditions = requestConditions == null
                ? new DataLakeRequestConditions() : requestConditions;
            final ParallelTransferOptions validatedParallelTransferOptions =
                ModelHelper.populateAndApplyDefaults(parallelTransferOptions);

            Function<Flux<ByteBuffer>, Mono<Response<PathInfo>>> uploadInChunksFunction = (stream) ->
                uploadInChunks(stream, fileOffset, length, retainUncommittedData, close,
                    validatedParallelTransferOptions, headers, validatedRequestConditions);

            BiFunction<Flux<ByteBuffer>, Long, Mono<Response<PathInfo>>> uploadFullMethod =
                (stream, lengthUploaded) -> uploadWithResponse(ProgressReporter
                        .addProgressReporting(stream, validatedParallelTransferOptions.getProgressReceiver()),
                    fileOffset, length, retainUncommittedData, close, headers, validatedRequestConditions);

            return UploadUtils.determineUploadFullOrChunked(data, validatedParallelTransferOptions,
                uploadInChunksFunction, uploadFullMethod);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private Mono<Response<PathInfo>> uploadInChunks(Flux<ByteBuffer> data, long fileOffset, long length,
        boolean retainUncommittedData, boolean close, ParallelTransferOptions parallelTransferOptions,
        PathHttpHeaders httpHeaders, DataLakeRequestConditions requestConditions) {
        // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
        AtomicLong totalProgress = new AtomicLong();
        Lock progressLock = new ReentrantLock();

        // Validation done in the constructor.
        UploadBufferPool pool = new UploadBufferPool(parallelTransferOptions.getNumBuffers(),
            parallelTransferOptions.getBlockSize(), MAX_APPEND_FILE_BYTES);

        Flux<ByteBuffer> chunkedSource = UploadUtils.chunkSource(data, parallelTransferOptions);

        /*
         Write to the pool and upload the output.
         */
        return chunkedSource.concatMap(pool::write)
            .concatWith(Flux.defer(pool::flush))
            .map(buffer -> Tuples.of(buffer, (long) buffer.remaining()))
            /* The tuple keeps track of the next buffer to write and the fileOffset for the next buffer */
            .scan((result, source) -> {
                ByteBuffer buffer = source.getT1();
                long currentBufferLength = buffer.remaining();
                long lastBytesWritten = result.getT2();

                return Tuples.of(buffer, currentBufferLength + lastBytesWritten);
            })
            .flatMapSequential(tuple2 -> {
                ByteBuffer buffer = tuple2.getT1();
                long currentBufferLength = buffer.remaining();
                long currentOffset = tuple2.getT2() - currentBufferLength + fileOffset;
                // Report progress as necessary.
                Flux<ByteBuffer> progressData = ProgressReporter.addParallelProgressReporting(
                    Flux.just(buffer), parallelTransferOptions.getProgressReceiver(), progressLock, totalProgress);
                return appendWithResponse(progressData, currentOffset, currentBufferLength, null,
                    requestConditions.getLeaseId())
                    .doFinally(x -> pool.returnBuffer(buffer))
                    .flux();
            })
            .last()
            .flatMap(resp -> flushWithResponse(length, retainUncommittedData, close, httpHeaders, requestConditions));
    }

    private Mono<Response<PathInfo>> uploadWithResponse(Flux<ByteBuffer> data, long fileOffset, long length,
        boolean retainUncommittedData, boolean close, PathHttpHeaders httpHeaders,
        DataLakeRequestConditions requestConditions) {
        return appendWithResponse(data, fileOffset, length, null, requestConditions.getLeaseId())
            .flatMap(resp -> flushWithResponse(fileOffset + length, retainUncommittedData, close, httpHeaders,
                requestConditions));
    }

    /**
     * Creates a new file with the content of the specified file. By default this method will not overwrite an
     * existing file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFile#String}
     *
     * @param filePath Path to the upload file
     * @return An empty response
     * @throws UncheckedIOException If an I/O error occurs
     */
    public Mono<Void> uploadFromFile(String filePath, long destOffset) {
        try {
            return uploadFromFile(filePath, destOffset, false);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new file, or updates the content of an existing file, with the content of the specified
     * file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFile#String-boolean}
     *
     * @param filePath Path to the upload file
     * @param overwrite Whether or not to overwrite, should the file already exist.
     * @return An empty response
     * @throws UncheckedIOException If an I/O error occurs
     */
    public Mono<Void> uploadFromFile(String filePath, long destOffset, boolean overwrite) {
        try {
            Mono<Void> overwriteCheck = Mono.empty();
            DataLakeRequestConditions requestConditions = null;

            // Note that if the file will be uploaded using a putBlob, we also can skip the exists check.
            if (!overwrite) {
                if (uploadInBlocks(filePath, DataLakeFileAsyncClient.MAX_APPEND_FILE_BYTES)) {
                    overwriteCheck = exists().flatMap(exists -> exists
                        ? monoError(logger, new IllegalArgumentException(Constants.FILE_ALREADY_EXISTS))
                        : Mono.empty());
                }

                requestConditions = new DataLakeRequestConditions()
                    .setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
            }

            return overwriteCheck.then(uploadFromFile(filePath, destOffset, false, false, null, null, requestConditions));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new file, or updates the content of an existing file, with the content of the specified
     * file.
     * <p>
     * To avoid overwriting, pass "*" to {@link DataLakeRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFile#String-ParallelTransferOptions-PathHttpHeaders-Map-AccessTier-DataLakeRequestConditions}
     *
     * @param filePath Path to the upload file
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to upload from file. Number of parallel
     * transfers parameter is ignored.
     * @param headers {@link PathHttpHeaders}
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return An empty response
     * @throws UncheckedIOException If an I/O error occurs
     */
    public Mono<Void> uploadFromFile(String filePath, long fileOffset, boolean retainUncommittedData,
        boolean close, ParallelTransferOptions parallelTransferOptions, PathHttpHeaders headers,
        DataLakeRequestConditions requestConditions) {
        Integer originalBlockSize = (parallelTransferOptions == null)
            ? null
            : parallelTransferOptions.getBlockSize();
        final ParallelTransferOptions finalParallelTransferOptions =
            ModelHelper.populateAndApplyDefaults(parallelTransferOptions);
        try {
            return Mono.using(() -> uploadFileResourceSupplier(filePath),
                channel -> {
                    try {
                        long fileSize = channel.size();

                        if (fileSize == 0) {
                            throw logger.logExceptionAsError(new IllegalArgumentException("Size of the file must be "
                                + "greater than 0."));
                        }

                        // If the file is larger than 256MB chunk it and stage it as blocks.
                        if (uploadInBlocks(filePath, finalParallelTransferOptions.getMaxSingleUploadSize())) {
                            return uploadBlocks(fileOffset, fileSize, retainUncommittedData, close,
                                finalParallelTransferOptions, originalBlockSize, headers, requestConditions, channel);
                        } else {
                            // Otherwise we know it can be sent in a single request reducing network overhead.
                            return uploadWithResponse(FluxUtil.readFile(channel), fileOffset, fileSize, retainUncommittedData,
                                close, parallelTransferOptions, headers, requestConditions)
                                .then();
                        }
                    } catch (IOException ex) {
                        return Mono.error(ex);
                    }
                }, this::uploadFileCleanup);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    boolean uploadInBlocks(String filePath, Integer maxSingleUploadSize) {
        AsynchronousFileChannel channel = uploadFileResourceSupplier(filePath);
        boolean retVal;
        try {
            retVal = channel.size() > maxSingleUploadSize;
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        } finally {
            uploadFileCleanup(channel);
        }

        return retVal;
    }

    private Mono<Void> uploadBlocks(long fileOffset, long fileSize, boolean retainUncommittedData,
        boolean close, ParallelTransferOptions parallelTransferOptions, Integer originalBlockSize,
        PathHttpHeaders headers, DataLakeRequestConditions requestConditions, AsynchronousFileChannel channel) {
        final DataLakeRequestConditions finalRequestConditions = (requestConditions == null)
            ? new DataLakeRequestConditions() : requestConditions;
        // parallelTransferOptions are finalized in the calling method.

        // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
        AtomicLong totalProgress = new AtomicLong();
        Lock progressLock = new ReentrantLock();

        return Flux.fromIterable(sliceFile(fileSize, originalBlockSize, parallelTransferOptions.getBlockSize()))
            .flatMap(chunk -> {
                Flux<ByteBuffer> progressData = ProgressReporter.addParallelProgressReporting(
                    FluxUtil.readFile(channel, chunk.getOffset(), chunk.getCount()),
                    parallelTransferOptions.getProgressReceiver(), progressLock, totalProgress);

                return appendWithResponse(progressData, fileOffset + chunk.getOffset(), chunk.getCount(), null,
                    finalRequestConditions.getLeaseId());
            })
            .then(Mono.defer(() ->
                flushWithResponse(fileSize, retainUncommittedData, close, headers, requestConditions)))
            .then();
    }

    /**
     * RESERVED FOR INTERNAL USE.
     *
     * Resource Supplier for UploadFile.
     *
     * @param filePath The path for the file
     * @return {@code AsynchronousFileChannel}
     * @throws UncheckedIOException an input output exception.
     */
    protected AsynchronousFileChannel uploadFileResourceSupplier(String filePath) {
        try {
            return AsynchronousFileChannel.open(Paths.get(filePath), StandardOpenOption.READ);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private void uploadFileCleanup(AsynchronousFileChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private List<FileRange> sliceFile(long fileSize, Integer originalBlockSize, int blockSize) {
        List<FileRange> ranges = new ArrayList<>();
        if (fileSize > 100 * Constants.MB && originalBlockSize == null) {
            blockSize = BlobAsyncClient.BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE;
        }
        for (long pos = 0; pos < fileSize; pos += blockSize) {
            long count = blockSize;
            if (pos + count > fileSize) {
                count = fileSize - pos;
            }
            ranges.add(new FileRange(pos, count));
        }
        return ranges;
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
     * <p>By default this method will not overwrite existing data.</p>
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
            return flush(position, false);
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.flush#long-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param position The length of the file after all data has been written.
     * @param overwrite Whether or not to overwrite, should data exist on the file.
     *
     * @return A reactive response containing the information of the created resource.
     */
    public Mono<PathInfo> flush(long position, boolean overwrite) {
        try {
            DataLakeRequestConditions requestConditions = null;
            if (!overwrite) {
                requestConditions = new DataLakeRequestConditions()
                    .setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
            }
            return flushWithResponse(position, false, false, null, requestConditions).flatMap(FluxUtil::toMono);
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
                getRangeContentMd5).map(Transforms::toFileReadAsyncResponse)
                .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.rename#String-String}
     *
     * @param destinationFileSystem The file system of the destination within the account.
     * {@code null} for the current file system.
     * @param destinationPath Relative path from the file system to rename the file to, excludes the file system name.
     * For example if you want to move a file with fileSystem = "myfilesystem", path = "mydir/hello.txt" to another path
     * in myfilesystem (ex: newdir/hi.txt) then set the destinationPath = "newdir/hi.txt"
     * @return A {@link Mono} containing a {@link DataLakeFileAsyncClient} used to interact with the new file created.
     */
    public Mono<DataLakeFileAsyncClient> rename(String destinationFileSystem, String destinationPath) {
        try {
            return renameWithResponse(destinationFileSystem, destinationPath, null, null).flatMap(FluxUtil::toMono);
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.renameWithResponse#String-String-DataLakeRequestConditions-DataLakeRequestConditions}
     *
     * @param destinationFileSystem The file system of the destination within the account.
     * {@code null} for the current file system.
     * @param destinationPath Relative path from the file system to rename the file to, excludes the file system name.
     * For example if you want to move a file with fileSystem = "myfilesystem", path = "mydir/hello.txt" to another path
     * in myfilesystem (ex: newdir/hi.txt) then set the destinationPath = "newdir/hi.txt"
     * @param sourceRequestConditions {@link DataLakeRequestConditions} against the source.
     * @param destinationRequestConditions {@link DataLakeRequestConditions} against the destination.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeFileAsyncClient} used to interact with the file created.
     */
    public Mono<Response<DataLakeFileAsyncClient>> renameWithResponse(String destinationFileSystem,
        String destinationPath, DataLakeRequestConditions sourceRequestConditions,
        DataLakeRequestConditions destinationRequestConditions) {
        try {
            return withContext(context -> renameWithResponse(destinationFileSystem, destinationPath,
                sourceRequestConditions, destinationRequestConditions, context))
                .map(response -> new SimpleResponse<>(response,
                    new DataLakeFileAsyncClient(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }


}
