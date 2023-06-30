// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.specialized;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.PageBlobRequestConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlockBlobCommitBlockListOptions;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.options.BlockBlobStageBlockOptions;
import com.azure.storage.common.StorageOutputStream;
import com.azure.storage.common.implementation.Constants;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * BlobOutputStream allows for the uploading of data to a blob using a stream-like approach.
 */
public abstract class BlobOutputStream extends StorageOutputStream {

    private volatile boolean isClosed;

    /**
     * @param writeThreshold How many bytes the output will retain before it initiates a write to the Storage service.
     */
    BlobOutputStream(final int writeThreshold) {
        super(writeThreshold);
    }

    static BlobOutputStream appendBlobOutputStream(final AppendBlobAsyncClient client,
        final AppendBlobRequestConditions appendBlobRequestConditions) {
        return new AppendBlobOutputStream(client, appendBlobRequestConditions);
    }

    /**
     * Creates a block blob output stream from a BlobAsyncClient
     *
     * @param client {@link BlobAsyncClient} The blob client.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any metadata
     * key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the destination blob.
     * @param requestConditions {@link BlobRequestConditions}
     * @return {@link BlobOutputStream} associated with the blob.
     */
    public static BlobOutputStream blockBlobOutputStream(final BlobAsyncClient client,
        final ParallelTransferOptions parallelTransferOptions, final BlobHttpHeaders headers,
        final Map<String, String> metadata, final AccessTier tier, final BlobRequestConditions requestConditions) {
        return blockBlobOutputStream(client, parallelTransferOptions, headers, metadata, tier, requestConditions,
            Context.NONE);
    }

    /**
     * Creates a block blob output stream from a BlobAsyncClient
     *
     * @param client {@link BlobAsyncClient} The blob client.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any metadata
     * key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the destination blob.
     * @param requestConditions {@link BlobRequestConditions}
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link BlobOutputStream} associated with the blob.
     */
    public static BlobOutputStream blockBlobOutputStream(final BlobAsyncClient client,
        final ParallelTransferOptions parallelTransferOptions, final BlobHttpHeaders headers,
        final Map<String, String> metadata, final AccessTier tier,
        final BlobRequestConditions requestConditions, Context context) {
        return blockBlobOutputStream(client, new BlockBlobOutputStreamOptions()
                .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers).setMetadata(metadata)
                .setTier(tier).setRequestConditions(requestConditions),
            context);
    }

    /**
     * Creates a block blob output stream from a BlobAsyncClient
     *
     * @param client {@link BlobAsyncClient} The blob client.
     * @param options {@link BlockBlobOutputStreamOptions}
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link BlobOutputStream} associated with the blob.
     */
    public static BlobOutputStream blockBlobOutputStream(final BlobAsyncClient client,
        BlockBlobOutputStreamOptions options, Context context) {
        options = options == null ? new BlockBlobOutputStreamOptions() : options;
        return new BlockBlobOutputStream(client, options.getParallelTransferOptions(), options.getHeaders(),
            options.getMetadata(), options.getTags(), options.getTier(), options.getRequestConditions(), context);
    }

    static BlobOutputStream pageBlobOutputStream(final PageBlobAsyncClient client, final PageRange pageRange,
        final BlobRequestConditions requestConditions) {
        return new PageBlobOutputStream(client, pageRange, requestConditions);
    }

    abstract void commit();

    /**
     * Closes this output stream and releases any system resources associated with this stream. If any data remains in
     * the buffer it is committed to the service.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public synchronized void close() throws IOException {
        try {
            // if the stream is already closed, we can stop executing any further steps to avoid throwing
            // STREAM_CLOSED exception
            if (isClosed) {
                return;
            }
            // if an exception was thrown by any thread in the threadExecutor, realize it now
            if (this.lastError != null) {
                throw lastError;
            }

            // flush any remaining data
            this.flush();

            // try to commit the blob
            try {
                this.commit();
            } catch (final BlobStorageException e) {
                throw new IOException("The blob has not been committed. Data has not been persisted.", e);
            }
            /* Need this check because for block blob the buffered upload error only manifests itself after commit is
               called */
            if (this.lastError != null) {
                throw lastError;
            }
        } finally {
            this.lastError = new IOException(Constants.STREAM_CLOSED);
            isClosed = true;
        }
    }

    private static final class AppendBlobOutputStream extends BlobOutputStream {
        private static final String INVALID_BLOCK_SIZE =
            "Block data should not exceed BlockBlobURL.MAX_STAGE_BLOCK_BYTES";

        private final AppendBlobRequestConditions appendBlobRequestConditions;
        private final AppendBlobAsyncClient client;

        private AppendBlobOutputStream(final AppendBlobAsyncClient client,
            final AppendBlobRequestConditions appendBlobRequestConditions) {
            // service versions 2022-11-02 and above support uploading block bytes up to 100MB, all older service
            // versions support up to 4MB
            super(client.getServiceVersion().ordinal() < BlobServiceVersion.V2022_11_02.ordinal()
                ? AppendBlobClient.MAX_APPEND_BLOCK_BYTES_VERSIONS_2021_12_02_AND_BELOW
                : AppendBlobClient.MAX_APPEND_BLOCK_BYTES_VERSIONS_2022_11_02_AND_ABOVE);

            this.client = client;
            this.appendBlobRequestConditions = (appendBlobRequestConditions == null)
                ? new AppendBlobRequestConditions() : appendBlobRequestConditions;

            if (this.appendBlobRequestConditions.getAppendPosition() == null) {
                this.appendBlobRequestConditions.setAppendPosition(client.getProperties().block().getBlobSize());
            }
        }

        private Mono<Void> appendBlock(Flux<ByteBuffer> blockData, long writeLength) {
            long newAppendOffset = appendBlobRequestConditions.getAppendPosition() + writeLength;
            return client.appendBlockWithResponse(blockData, writeLength, null, appendBlobRequestConditions)
                .doOnNext(ignored -> appendBlobRequestConditions.setAppendPosition(newAppendOffset))
                .then()
                .onErrorResume(t -> t instanceof IOException || t instanceof BlobStorageException, e -> {
                    this.lastError = new IOException(e);
                    return Mono.empty();
                });
        }

        @Override
        protected Mono<Void> dispatchWrite(byte[] data, int writeLength, long offset) {
            if (writeLength == 0) {
                return Mono.empty();
            }

            // We cannot differentiate between max size condition failing only in the retry versus failing in the
            // first attempt and retry even for a single writer scenario. So we will eliminate the latter and handle
            // the former in the append block method.
            if (appendBlobRequestConditions.getMaxSize() != null
                && appendBlobRequestConditions.getAppendPosition() > appendBlobRequestConditions.getMaxSize()) {
                this.lastError = new IOException(INVALID_BLOCK_SIZE);
                return Mono.error(this.lastError);
            }

            return this.appendBlock(Mono.fromCallable(() -> ByteBuffer.wrap(data, (int) offset, writeLength)).flux(),
                writeLength);
        }

        @Override
        void commit() {
            // AppendBlob doesn't need to commit anything.
        }
    }

    private static final class BlockBlobOutputStream extends BlobOutputStream {
        private final List<String> blockIds = new LinkedList<>();
        private List<ByteBuffer> buffers = new LinkedList<>();
        private long chunkSize;

        private final long blockSize;

        private final BlockBlobAsyncClient client;
        private final BlobHttpHeaders headers;
        private final Map<String, String> metadata;
        private final Map<String, String> tags;
        private final AccessTier tier;
        private final BlobRequestConditions requestConditions;
        private final Context context;

        private BlockBlobOutputStream(final BlobAsyncClient client,
            final ParallelTransferOptions parallelTransferOptions, final BlobHttpHeaders headers,
            final Map<String, String> metadata, Map<String, String> tags, final AccessTier tier,
            final BlobRequestConditions requestConditions, Context context) {
            super(Integer.MAX_VALUE); // writeThreshold is effectively not used by BlockBlobOutputStream.
            // There is a bug in reactor core that does not handle converting Context.NONE to a reactor context.
            context = context == null || context.equals(Context.NONE) ? null : context;
            this.client = client.getBlockBlobAsyncClient();
            this.blockSize = ModelHelper.populateAndApplyDefaults(parallelTransferOptions).getBlockSizeLong();
            this.headers = headers;
            this.metadata = metadata;
            this.tags = tags;
            this.tier = tier;
            this.requestConditions = requestConditions;
            this.context = context;
        }

        @Override
        void commit() {
            try {
                if (!buffers.isEmpty()) {
                    writeBlock(buffers);
                }

                client.commitBlockListWithResponse(new BlockBlobCommitBlockListOptions(blockIds)
                        .setHeaders(headers).setMetadata(metadata).setTags(tags).setTier(tier)
                        .setRequestConditions(requestConditions), context)
                    .block();
            } catch (Exception e) {
                handleException(e);
            }
        }

        @Override
        protected void writeInternal(final byte[] data, int offset, int length) {
            this.checkStreamState();
            /*
             * We need to do a deep copy here because the writing is async in this case. It is a common pattern for
             * customers writing to an output stream to perform the writes in a tight loop with a reused buffer. This
             * coupled with async network behavior can result in the data being overwritten as the buffer is reused.
             */

            // There are three potential scenarios here:
            // 1. The data is smaller than the remaining chunk size, if so just buffer it and add it to the chunk.
            // 2. The data completes the chunk, fill the remaining chunk and write the chunk. Then retain the remaining
            //    data for a future chunk.
            // 3. The data is larger than a chunk, use it to fill any partial chunk and write the chunk. Then consume
            //    the remaining data writing any chunks that the data fills and retaining the remaining data for a
            //    future chunk.
            if (chunkSize + length < blockSize) {
                // Data doesn't complete the chunk, buffer it and increase the current chunk size.
                chunkSize += length;
                byte[] buffer = new byte[length];
                System.arraycopy(data, offset, buffer, 0, length);
                buffers.add(ByteBuffer.wrap(buffer));
            } else {
                try {
                    int remainingBytes = completeAndWriteChunk(data, offset, length);
                    while (remainingBytes >= blockSize) {
                        // While there are enough bytes remaining to complete a chunk write the chunk without buffering.
                        // This is safe, even though at the beginning of this function it calls out needing to do a deep
                        // copy, as this write will block until it's complete, leaving no potential race condition.
                        String blockId = generateBlockId();
                        blockIds.add(blockId);
                        writeBlock(Collections.singletonList(
                            ByteBuffer.wrap(data, offset + length - remainingBytes, (int) blockSize)));

                        remainingBytes -= blockSize;
                    }

                    buffers = new LinkedList<>();
                    if (remainingBytes > 0) {
                        byte[] initialBuffer = new byte[remainingBytes];
                        System.arraycopy(data, offset + length - remainingBytes, initialBuffer, 0, remainingBytes);
                        buffers.add(ByteBuffer.wrap(initialBuffer));
                    }
                    chunkSize = remainingBytes;
                } catch (Exception e) {
                    handleException(e);
                }
            }
        }

        private int completeAndWriteChunk(byte[] data, int offset, int length) throws Exception {
            // Block write scenario, we've reached the block size and should write the data we've received.
            int remainingBytes = (int) (blockSize - chunkSize);
            byte[] finalBuffer = new byte[remainingBytes];
            System.arraycopy(data, offset, finalBuffer, 0, remainingBytes);

            buffers.add(ByteBuffer.wrap(finalBuffer));
            writeBlock(buffers);

            return length - remainingBytes;
        }

        private void writeBlock(List<ByteBuffer> buffers) throws Exception {
            String blockId = generateBlockId();
            blockIds.add(blockId);
            client.stageBlockWithResponse(new BlockBlobStageBlockOptions(blockId,
                BinaryData.fromListByteBuffer(buffers))).block();
        }

        private static String generateBlockId() {
            return Base64.getEncoder().encodeToString(
                CoreUtils.randomUuid().toString().getBytes(StandardCharsets.UTF_8));
        }

        private void handleException(Exception e) {
            Throwable unwrapped = Exceptions.unwrap(e);
            if (unwrapped instanceof IOException) {
                this.lastError = (IOException) unwrapped;
            } else {
                this.lastError = new IOException(unwrapped);
            }
        }

        // Never called
        @Override
        protected Mono<Void> dispatchWrite(byte[] data, int writeLength, long offset) {
            return Mono.empty();
        }
    }

    private static final class PageBlobOutputStream extends BlobOutputStream {
        private static final String INVALID_NUMBER_OF_BYTES_IN_THE_BUFFER =
            "Page data must be a multiple of 512 bytes. Buffer currently contains %d bytes.";

        private static final ClientLogger LOGGER = new ClientLogger(PageBlobOutputStream.class);
        private final PageBlobAsyncClient client;
        private final PageBlobRequestConditions pageBlobRequestConditions;
        private final PageRange pageRange;

        private PageBlobOutputStream(final PageBlobAsyncClient client, final PageRange pageRange,
            final BlobRequestConditions blobRequestConditions) {
            super(PageBlobClient.MAX_PUT_PAGES_BYTES);
            this.client = client;
            this.pageRange = pageRange;

            if (blobRequestConditions != null) {
                this.pageBlobRequestConditions = new PageBlobRequestConditions()
                    .setLeaseId(blobRequestConditions.getLeaseId())
                    .setIfMatch(blobRequestConditions.getIfMatch())
                    .setIfNoneMatch(blobRequestConditions.getIfNoneMatch())
                    .setIfModifiedSince(blobRequestConditions.getIfModifiedSince())
                    .setIfUnmodifiedSince(blobRequestConditions.getIfUnmodifiedSince());
            } else {
                this.pageBlobRequestConditions = null;
            }
        }

        private Mono<Void> writePages(Flux<ByteBuffer> pageData, int length, long offset) {
            return client.uploadPagesWithResponse(new PageRange().setStart(offset).setEnd(offset + length - 1),
                    pageData, null, pageBlobRequestConditions)
                .then()
                .onErrorResume(BlobStorageException.class, e -> {
                    this.lastError = new IOException(e);
                    return Mono.empty();
                });
        }

        @Override
        protected Mono<Void> dispatchWrite(byte[] data, int writeLength, long offset) {
            if (writeLength == 0) {
                return Mono.empty();
            }

            if (writeLength % PageBlobAsyncClient.PAGE_BYTES != 0) {
                return Mono.error(new IOException(String.format(INVALID_NUMBER_OF_BYTES_IN_THE_BUFFER,
                    writeLength)));
            }

            long pageOffset = pageRange.getStart();
            if (pageOffset + writeLength - 1 > pageRange.getEnd()) {
                throw LOGGER.logExceptionAsError(
                    new RuntimeException("The input data length is larger than the page range."));
            }
            pageRange.setStart(pageRange.getStart() + writeLength);

            return this.writePages(Mono.fromCallable(() -> ByteBuffer.wrap(data, (int) offset, writeLength)).flux(),
                writeLength, pageOffset);
        }

        @Override
        void commit() {
            // PageBlob doesn't need to commit anything.
        }
    }
}
