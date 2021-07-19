// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.ProgressReporter;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.BufferAggregator;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.implementation.BufferStagingArea;
import com.azure.storage.common.implementation.UploadUtils;
import com.azure.storage.file.datalake.implementation.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.implementation.models.ModifiedAccessConditions;
import com.azure.storage.file.datalake.implementation.models.PathExpiryOptions;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.implementation.util.ModelHelper;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DownloadRetryOptions;
import com.azure.storage.file.datalake.models.FileExpirationOffset;
import com.azure.storage.file.datalake.models.FileQueryAsyncResponse;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.FileReadAsyncResponse;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.options.FileParallelUploadOptions;
import com.azure.storage.file.datalake.options.FileQueryOptions;
import com.azure.storage.file.datalake.options.FileScheduleDeletionOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;


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
 * <a href="https://docs.microsoft.com/azure/storage/blobs/data-lake-storage-introduction">Azure
 * Docs</a> for more information.
 */
@ServiceClient(builder = DataLakePathClientBuilder.class, isAsync = true)
public class DataLakeFileAsyncClient extends DataLakePathAsyncClient {

    /**
     * Indicates the maximum number of bytes that can be sent in a call to upload.
     */
    static final long MAX_APPEND_FILE_BYTES = 4000L * Constants.MB;

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
        super(pathAsyncClient.getHttpPipeline(), pathAsyncClient.getAccountUrl(), pathAsyncClient.getServiceVersion(),
            pathAsyncClient.getAccountName(), pathAsyncClient.getFileSystemName(),
            Utility.urlEncode(pathAsyncClient.pathName), PathResourceType.FILE, pathAsyncClient.getBlockBlobAsyncClient());
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param requestConditions {@link DataLakeRequestConditions}
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse(DataLakeRequestConditions requestConditions) {
        // TODO (rickle-msft): Update for continuation token if we support HNS off
        try {
            return withContext(context -> deleteWithResponse(null /* recursive */, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }

    }

    /**
     * Creates a new file and uploads content.
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathInfo> upload(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions) {
        return upload(data, parallelTransferOptions, false);
    }

    /**
     * Creates a new file and uploads content.
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathInfo> upload(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions,
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
            .then(uploadWithResponse(data, parallelTransferOptions, null, null, requestConditions))
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new file.
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
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A reactive response containing the information of the uploaded file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathInfo>> uploadWithResponse(Flux<ByteBuffer> data,
        ParallelTransferOptions parallelTransferOptions, PathHttpHeaders headers, Map<String, String> metadata,
        DataLakeRequestConditions requestConditions) {
        try {
            return uploadWithResponse(new FileParallelUploadOptions(data)
                .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers).setMetadata(metadata)
                .setRequestConditions(requestConditions));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new file.
     * <p>
     * To avoid overwriting, pass "*" to {@link DataLakeRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#FileParallelUploadOptions}
     *
     * <p><strong>Using Progress Reporting</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#FileParallelUploadOptions.ProgressReporter}
     *
     * @param options {@link FileParallelUploadOptions}
     * @return A reactive response containing the information of the uploaded file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathInfo>> uploadWithResponse(FileParallelUploadOptions options) {
        try {
            StorageImplUtils.assertNotNull("options", options);
            DataLakeRequestConditions validatedRequestConditions = options.getRequestConditions() == null
                ? new DataLakeRequestConditions() : options.getRequestConditions();
            /* Since we are creating a file with the request conditions, everything but lease id becomes invalid
             after creation, so remove them for the append/flush calls. */
            DataLakeRequestConditions validatedUploadRequestConditions = new DataLakeRequestConditions()
                .setLeaseId(validatedRequestConditions.getLeaseId());
            final ParallelTransferOptions validatedParallelTransferOptions =
                ModelHelper.populateAndApplyDefaults(options.getParallelTransferOptions());
            long fileOffset = 0;

            Function<Flux<ByteBuffer>, Mono<Response<PathInfo>>> uploadInChunksFunction = (stream) ->
                uploadInChunks(stream, fileOffset, validatedParallelTransferOptions, options.getHeaders(),
                    validatedUploadRequestConditions);

            BiFunction<Flux<ByteBuffer>, Long, Mono<Response<PathInfo>>> uploadFullMethod =
                (stream, length) -> uploadWithResponse(ProgressReporter
                        .addProgressReporting(stream, validatedParallelTransferOptions.getProgressReceiver()),
                    fileOffset, length, options.getHeaders(), validatedUploadRequestConditions);

            Flux<ByteBuffer> data = options.getDataFlux();
            // no specified length: use azure.core's converter
            if (data == null && options.getOptionalLength() == null) {
                // We can only buffer up to max int due to restrictions in ByteBuffer.
                int chunkSize = (int) Math.min(Integer.MAX_VALUE, validatedParallelTransferOptions.getBlockSizeLong());
                data = FluxUtil.toFluxByteBuffer(options.getDataStream(), chunkSize);
            // specified length (legacy requirement): use custom converter. no marking because we buffer anyway.
            } else if (data == null) {
                // We can only buffer up to max int due to restrictions in ByteBuffer.
                int chunkSize = (int) Math.min(Integer.MAX_VALUE, validatedParallelTransferOptions.getBlockSizeLong());
                data = Utility.convertStreamToByteBuffer(
                    options.getDataStream(), options.getOptionalLength(), chunkSize, false);
            }

            return createWithResponse(options.getPermissions(), options.getUmask(), options.getHeaders(),
                options.getMetadata(), validatedRequestConditions)
                .then(UploadUtils.uploadFullOrChunked(data, validatedParallelTransferOptions,
                    uploadInChunksFunction, uploadFullMethod));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private Mono<Response<PathInfo>> uploadInChunks(Flux<ByteBuffer> data, long fileOffset,
        ParallelTransferOptions parallelTransferOptions, PathHttpHeaders httpHeaders,
        DataLakeRequestConditions requestConditions) {
        // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
        AtomicLong totalProgress = new AtomicLong();
        Lock progressLock = new ReentrantLock();

        // Validation done in the constructor.
        BufferStagingArea stagingArea = new BufferStagingArea(parallelTransferOptions.getBlockSizeLong(), MAX_APPEND_FILE_BYTES);

        Flux<ByteBuffer> chunkedSource = UploadUtils.chunkSource(data, parallelTransferOptions);

        /*
         Write to the stagingArea and upload the output.
         maxConcurrency = 1 when writing means only 1 BufferAggregator will be accumulating at a time.
         parallelTransferOptions.getMaxConcurrency() appends will be happening at once, so we guarantee buffering of
         only concurrency + 1 chunks at a time.
         */
        return chunkedSource.flatMapSequential(stagingArea::write, 1, 1)
            .concatWith(Flux.defer(stagingArea::flush))
            /* Map the data to a tuple 3, of buffer, buffer length, buffer offset */
            .map(bufferAggregator -> Tuples.of(bufferAggregator, bufferAggregator.length(), 0L))
            /* Scan reduces a flux with an accumulator while emitting the intermediate results. */
            /* As an example, data consists of ByteBuffers of length 10-10-5.
               In the map above we transform the initial ByteBuffer to a tuple3 of buff, 10, 0.
               Scan will emit that as is, then accumulate the tuple for the next emission.
               On the second iteration, the middle ByteBuffer gets transformed to buff, 10, 10+0
               (from previous emission). Scan emits that, and on the last iteration, the last ByteBuffer gets
               transformed to buff, 5, 10+10 (from previous emission). */
            .scan((result, source) -> {
                BufferAggregator bufferAggregator = source.getT1();
                long currentBufferLength = bufferAggregator.length();
                long lastBytesWritten = result.getT2();
                long lastOffset = result.getT3();

                return Tuples.of(bufferAggregator, currentBufferLength, lastBytesWritten + lastOffset);
            })
            .flatMapSequential(tuple3 -> {
                BufferAggregator bufferAggregator = tuple3.getT1();
                long currentBufferLength = bufferAggregator.length();
                long currentOffset = tuple3.getT3() + fileOffset;
                // Report progress as necessary.
                Flux<ByteBuffer> progressData = ProgressReporter.addParallelProgressReporting(
                    bufferAggregator.asFlux(), parallelTransferOptions.getProgressReceiver(),
                    progressLock, totalProgress);
                final long offset = currentBufferLength + currentOffset;
                return appendWithResponse(progressData, currentOffset, currentBufferLength, null,
                    requestConditions.getLeaseId())
                    .map(resp -> offset) /* End of file after append to pass to flush. */
                    .flux();
            }, parallelTransferOptions.getMaxConcurrency(), 1)
            .last()
            .flatMap(length -> flushWithResponse(length, false, false, httpHeaders, requestConditions));
    }

    private Mono<Response<PathInfo>> uploadWithResponse(Flux<ByteBuffer> data, long fileOffset, long length,
        PathHttpHeaders httpHeaders, DataLakeRequestConditions requestConditions) {
        return appendWithResponse(data, fileOffset, length, null, requestConditions.getLeaseId())
            .flatMap(resp -> flushWithResponse(fileOffset + length, false, false, httpHeaders,
                requestConditions));
    }

    /**
     * Creates a new file, with the content of the specified file. By default this method will not overwrite an
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> uploadFromFile(String filePath) {
        try {
            return uploadFromFile(filePath, false);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new file, with the content of the specified file.
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> uploadFromFile(String filePath, boolean overwrite) {
        try {
            Mono<Void> overwriteCheck = Mono.empty();
            DataLakeRequestConditions requestConditions = null;

            // Note that if the file will be uploaded using a putBlob, we also can skip the exists check.
            if (!overwrite) {
                if (UploadUtils.shouldUploadInChunks(filePath,
                    DataLakeFileAsyncClient.MAX_APPEND_FILE_BYTES, logger)) {
                    overwriteCheck = exists().flatMap(exists -> exists
                        ? monoError(logger, new IllegalArgumentException(Constants.FILE_ALREADY_EXISTS))
                        : Mono.empty());
                }

                requestConditions = new DataLakeRequestConditions()
                    .setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
            }

            return overwriteCheck.then(uploadFromFile(filePath, null, null, null, requestConditions));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new file, with the content of the specified file.
     * <p>
     * To avoid overwriting, pass "*" to {@link DataLakeRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFile#String-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions}
     *
     * @param filePath Path to the upload file
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to upload from file. Number of parallel
     * transfers parameter is ignored.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return An empty response
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions,
        PathHttpHeaders headers, Map<String, String> metadata, DataLakeRequestConditions requestConditions) {
        Long originalBlockSize = (parallelTransferOptions == null)
            ? null
            : parallelTransferOptions.getBlockSizeLong();

        DataLakeRequestConditions validatedRequestConditions = requestConditions == null
            ? new DataLakeRequestConditions() : requestConditions;
        /* Since we are creating a file with the request conditions, everything but lease id becomes invalid
           after creation, so e remove them for the append/flush calls. */
        DataLakeRequestConditions validatedUploadRequestConditions = new DataLakeRequestConditions()
            .setLeaseId(validatedRequestConditions.getLeaseId());

        final ParallelTransferOptions finalParallelTransferOptions =
            ModelHelper.populateAndApplyDefaults(parallelTransferOptions);
        long fileOffset = 0;

        try {
            return Mono.using(() ->
                    UploadUtils.uploadFileResourceSupplier(filePath, logger),
                channel -> {
                    try {
                        long fileSize = channel.size();

                        if (fileSize == 0) {
                            throw logger.logExceptionAsError(new IllegalArgumentException("Size of the file must be "
                                + "greater than 0."));
                        }
                        if (UploadUtils.shouldUploadInChunks(filePath,
                            finalParallelTransferOptions.getMaxSingleUploadSizeLong(), logger)) {
                            return createWithResponse(null, null, headers, metadata, validatedRequestConditions)
                                .then(uploadFileChunks(fileOffset, fileSize, finalParallelTransferOptions,
                                    originalBlockSize, headers, validatedUploadRequestConditions, channel));
                        } else {
                            // Otherwise we know it can be sent in a single request reducing network overhead.
                            return createWithResponse(null, null, headers, metadata, validatedRequestConditions)
                                .then(uploadWithResponse(FluxUtil.readFile(channel), fileOffset, fileSize, headers,
                                    validatedUploadRequestConditions))
                                .then();
                        }
                    } catch (IOException ex) {
                        return Mono.error(ex);
                    }
                },
                channel ->
                    UploadUtils.uploadFileCleanup(channel, logger));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private Mono<Void> uploadFileChunks(long fileOffset, long fileSize, ParallelTransferOptions parallelTransferOptions,
        Long originalBlockSize, PathHttpHeaders headers, DataLakeRequestConditions requestConditions,
        AsynchronousFileChannel channel) {
        // parallelTransferOptions are finalized in the calling method.

        // See ProgressReporter for an explanation on why this lock is necessary and why we use AtomicLong.
        AtomicLong totalProgress = new AtomicLong();
        Lock progressLock = new ReentrantLock();

        return Flux.fromIterable(sliceFile(fileSize, originalBlockSize, parallelTransferOptions.getBlockSizeLong()))
            .flatMap(chunk -> {
                Flux<ByteBuffer> progressData = ProgressReporter.addParallelProgressReporting(
                    FluxUtil.readFile(channel, chunk.getOffset(), chunk.getCount()),
                    parallelTransferOptions.getProgressReceiver(), progressLock, totalProgress);

                return appendWithResponse(progressData, fileOffset + chunk.getOffset(), chunk.getCount(), null,
                    requestConditions.getLeaseId());
            }, parallelTransferOptions.getMaxConcurrency())
            .then(Mono.defer(() ->
                flushWithResponse(fileSize, false, false, headers, requestConditions)))
            .then();
    }

    private List<FileRange> sliceFile(long fileSize, Long originalBlockSize, long blockSize) {
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
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.append#Flux-long-long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param data The data to write to the file.
     * @param fileOffset The position where the data is to be appended.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data emitted by the {@code Flux}.
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.appendWithResponse#Flux-long-long-byte-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
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
    @ServiceMethod(returns = ReturnType.SINGLE)
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

        return this.dataLakeStorage.getPaths().appendDataWithResponseAsync(data, fileOffset, null, length, null, null,
            headers, leaseAccessConditions, context).map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Flushes (writes) data previously appended to the file through a call to append.
     * The previously uploaded data must be contiguous.
     * <p>By default this method will not overwrite existing data.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.flush#long}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param position The length of the file after all data has been written.
     *
     * @return A reactive response containing the information of the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.flush#long-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure
     * Docs</a></p>
     *
     * @param position The length of the file after all data has been written.
     * @param overwrite Whether or not to overwrite, should data exist on the file.
     *
     * @return A reactive response containing the information of the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.flushWithResponse#long-boolean-boolean-PathHttpHeaders-DataLakeRequestConditions}
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
     *
     * @return A reactive response containing the information of the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathInfo>> flushWithResponse(long position, boolean retainUncommittedData, boolean close,
        PathHttpHeaders httpHeaders, DataLakeRequestConditions requestConditions) {
        try {
            return withContext(context -> flushWithResponse(position, retainUncommittedData,
                close, httpHeaders, requestConditions, context));
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
        context = context == null ? Context.NONE : context;

        return this.dataLakeStorage.getPaths().flushDataWithResponseAsync(null, position, retainUncommittedData, close,
            (long) 0, null, httpHeaders, lac, mac,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
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
     * Reads the entire file into a file specified by the path.
     *
     * <p>The file will be created and must not exist, if the file already exists a {@link FileAlreadyExistsException}
     * will be thrown.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.readToFile#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @return A reactive response containing the file properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathProperties> readToFile(String filePath) {
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.readToFile#String-boolean}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @param overwrite Whether or not to overwrite the file, should the file exist.
     * @return A reactive response containing the file properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathProperties> readToFile(String filePath, boolean overwrite) {
        Set<OpenOption> openOptions = null;
        if (overwrite) {
            openOptions = new HashSet<>();
            openOptions.add(StandardOpenOption.CREATE);
            openOptions.add(StandardOpenOption.TRUNCATE_EXISTING); // If the file already exists and it is opened
            // for WRITE access, then its length is truncated to 0.
            openOptions.add(StandardOpenOption.READ);
            openOptions.add(StandardOpenOption.WRITE);
        }
        return readToFileWithResponse(filePath, null, null, null, null, false, openOptions)
            .flatMap(FluxUtil::toMono);
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.readToFileWithResponse#String-FileRange-ParallelTransferOptions-DownloadRetryOptions-DataLakeRequestConditions-boolean-Set}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param filePath A {@link String} representing the filePath where the downloaded data will be written.
     * @param range {@link FileRange}
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to download to file. Number of parallel
     * transfers parameter is ignored.
     * @param options {@link DownloadRetryOptions}
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param rangeGetContentMd5 Whether the contentMD5 for the specified file range should be returned.
     * @param openOptions {@link OpenOption OpenOptions} to use to configure how to open or create the file.
     * @return A reactive response containing the file properties and metadata.
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 100MB.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathProperties>> readToFileWithResponse(String filePath, FileRange range,
        ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions options,
        DataLakeRequestConditions requestConditions, boolean rangeGetContentMd5, Set<OpenOption> openOptions) {
        return blockBlobAsyncClient.downloadToFileWithResponse(new BlobDownloadToFileOptions(filePath)
        .setRange(Transforms.toBlobRange(range)).setParallelTransferOptions(parallelTransferOptions)
        .setDownloadRetryOptions(Transforms.toBlobDownloadRetryOptions(options))
        .setRequestConditions(Transforms.toBlobRequestConditions(requestConditions))
        .setRetrieveContentRangeMd5(rangeGetContentMd5).setOpenOptions(openOptions))
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException)
            .map(response -> new SimpleResponse<>(response, Transforms.toPathProperties(response.getValue())));
    }

    /**
     * Moves the file to another location within the file system.
     * For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeFileAsyncClient> rename(String destinationFileSystem, String destinationPath) {
        try {
            return renameWithResponse(destinationFileSystem, destinationPath, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Moves the file to another location within the file system. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
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
    @ServiceMethod(returns = ReturnType.SINGLE)
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

    /**
     * Queries the entire file.
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/query-blob-contents">Azure Docs</a></p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.query#String}
     *
     * @param expression The query expression.
     * @return A reactive response containing the queried data.
     */
    public Flux<ByteBuffer> query(String expression) {
        return queryWithResponse(new FileQueryOptions(expression))
            .flatMapMany(FileQueryAsyncResponse::getValue);
    }

    /**
     * Queries the entire file.
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/query-blob-contents">Azure Docs</a></p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.queryWithResponse#FileQueryOptions}
     *
     * @param queryOptions {@link FileQueryOptions The query options}
     * @return A reactive response containing the queried data.
     */
    public Mono<FileQueryAsyncResponse> queryWithResponse(FileQueryOptions queryOptions) {
        return blockBlobAsyncClient.queryWithResponse(Transforms.toBlobQueryOptions(queryOptions))
            .map(Transforms::toFileQueryAsyncResponse)
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
    }

    // TODO (kasobol-msft) add REST DOCS
    /**
     * Schedules the file for deletion.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.scheduleDeletion#FileScheduleDeletionOptions}
     *
     * @param options Schedule deletion parameters.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> scheduleDeletion(FileScheduleDeletionOptions options) {
        return scheduleDeletionWithResponse(options).flatMap(FluxUtil::toMono);
    }

    // TODO (kasobol-msft) add REST DOCS
    /**
     * Schedules the file for deletion.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileAsyncClient.scheduleDeletionWithResponse#FileScheduleDeletionOptions}
     *
     * @param options Schedule deletion parameters.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> scheduleDeletionWithResponse(FileScheduleDeletionOptions options) {
        try {
            return withContext(context -> scheduleDeletionWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> scheduleDeletionWithResponse(FileScheduleDeletionOptions options, Context context) {
        PathExpiryOptions pathExpiryOptions;
        context = context == null ? Context.NONE : context;
        String expiresOn = null;
        if (options != null && options.getExpiresOn() != null) {
            pathExpiryOptions = PathExpiryOptions.ABSOLUTE;
            expiresOn = new DateTimeRfc1123(options.getExpiresOn()).toString();
        } else if (options != null && options.getTimeToExpire() != null) {
            if (options.getExpiryRelativeTo() == FileExpirationOffset.CREATION_TIME) {
                pathExpiryOptions = PathExpiryOptions.RELATIVE_TO_CREATION;
            } else {
                pathExpiryOptions = PathExpiryOptions.RELATIVE_TO_NOW;
            }
            expiresOn = Long.toString(options.getTimeToExpire().toMillis());
        } else {
            pathExpiryOptions = PathExpiryOptions.NEVER_EXPIRE;
        }
        return this.blobDataLakeStorage.getPaths().setExpiryWithResponseAsync(
            pathExpiryOptions, null,
            null, expiresOn,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> new SimpleResponse<>(rb, null));
    }

}
