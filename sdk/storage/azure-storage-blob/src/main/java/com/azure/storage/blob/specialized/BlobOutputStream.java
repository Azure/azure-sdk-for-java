// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.specialized;

import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.implementation.util.StorageBlockingSink;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.PageBlobRequestConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.common.StorageOutputStream;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * BlobOutputStream allows for the uploading of data to a blob using a stream-like approach.
 */
public abstract class BlobOutputStream extends StorageOutputStream {

    BlobOutputStream(final int writeThreshold) {
        super(writeThreshold);
    }

    static BlobOutputStream appendBlobOutputStream(final AppendBlobAsyncClient client,
        final AppendBlobRequestConditions appendBlobRequestConditions) {
        return new AppendBlobOutputStream(client, appendBlobRequestConditions);
    }

    /**
     * Creates a block blob output stream from a BlobAsyncClient
     * @param client {@link BlobAsyncClient} The blob client.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
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
     * @param client {@link BlobAsyncClient} The blob client.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
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
            // if the user has already closed the stream, this will throw a STREAM_CLOSED exception
            // if an exception was thrown by any thread in the threadExecutor, realize it now
            this.checkStreamState();

            // flush any remaining data
            this.flush();

            // try to commit the blob
            try {
                this.commit();
            } catch (final BlobStorageException e) {
                throw new IOException(e);
            }
            /* Need this check because for block blob the buffered upload error only manifests itself after commit is
               called */
            if (this.lastError != null) {
                throw lastError;
            }
        } finally {
            // if close() is called again, an exception will be thrown
            this.lastError = new IOException(Constants.STREAM_CLOSED);
        }
    }

    private static final class AppendBlobOutputStream extends BlobOutputStream {
        private static final String INVALID_BLOCK_SIZE =
            "Block data should not exceed BlockBlobURL.MAX_STAGE_BLOCK_BYTES";

        private final AppendBlobRequestConditions appendBlobRequestConditions;
        private final AppendBlobAsyncClient client;

        private AppendBlobOutputStream(final AppendBlobAsyncClient client,
            final AppendBlobRequestConditions appendBlobRequestConditions) {
            super(AppendBlobClient.MAX_APPEND_BLOCK_BYTES);
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

            Flux<ByteBuffer> fbb = Flux.range(0, 1).concatMap(pos -> Mono.fromCallable(() ->
                ByteBuffer.wrap(data, (int) offset, writeLength)));

            return this.appendBlock(fbb.subscribeOn(Schedulers.elastic()), writeLength);
        }

        @Override
        void commit() {
            // AppendBlob doesn't need to commit anything.
        }
    }

    private static final class BlockBlobOutputStream extends BlobOutputStream {

        private final Lock lock;
        private final Condition transferComplete;
        private final StorageBlockingSink sink;

        boolean complete;

        private BlockBlobOutputStream(final BlobAsyncClient client,
            final ParallelTransferOptions parallelTransferOptions, final BlobHttpHeaders headers,
            final Map<String, String> metadata, Map<String, String> tags, final AccessTier tier,
            final BlobRequestConditions requestConditions, Context context) {
            super(Integer.MAX_VALUE); // writeThreshold is effectively not used by BlockBlobOutputStream.
            // There is a bug in reactor core that does not handle converting Context.NONE to a reactor context.
            context = context == null || context.equals(Context.NONE) ? null : context;

            this.lock = new ReentrantLock();
            this.transferComplete = lock.newCondition();
            this.sink = new StorageBlockingSink();

            Flux<ByteBuffer> body = this.sink.asFlux();

            client.uploadWithResponse(new BlobParallelUploadOptions(body)
                .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers).setMetadata(metadata)
                .setTags(tags).setTier(tier).setRequestConditions(requestConditions))
                // This allows the operation to continue while maintaining the error that occurred.
                .onErrorResume(e -> {
                    if (e instanceof IOException) {
                        this.lastError = (IOException) e;
                    } else {
                        this.lastError = new IOException(e);
                    }
                    return Mono.empty();
                })
                // Use doFinally to cover all termination scenarios of the Flux.
                .doFinally(signalType -> {
                    lock.lock();
                    try {
                        complete = true;
                        transferComplete.signal();
                    } finally {
                        lock.unlock();
                    }
                })
                .contextWrite(FluxUtil.toReactorContext(context))
                .subscribe();
        }

        @Override
        void commit() {

            // Need to wait until the uploadTask completes
            lock.lock();
            try {
                sink.emitCompleteOrThrow(); /* Allow upload task to try to complete. */

                while (!complete) {
                    transferComplete.await();
                }
            } catch (InterruptedException e) {
                this.lastError = new IOException(e.getMessage()); // Should we just throw and not populate this since its recoverable?
            } catch (Exception e) { // Catch any exceptions by the sink.
                this.lastError = new IOException(e);
            } finally {
                lock.unlock();
            }

        }

        @Override
        protected void writeInternal(final byte[] data, int offset, int length) {
            this.checkStreamState();
            /*
            We need to do a deep copy here because the writing is async in this case. It is a common pattern for
            customers writing to an output stream to perform the writes in a tight loop with a reused buffer. This
            coupled with async network behavior can result in the data being overwritten as the buffer is reused.
             */
            byte[] buffer = new byte[length];
            System.arraycopy(data, offset, buffer, 0, length);

            try {
                this.sink.emitNext(ByteBuffer.wrap(buffer));
            } catch (Exception e) {
                this.lastError = new IOException(e);
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

        private final ClientLogger logger = new ClientLogger(PageBlobOutputStream.class);
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

            Flux<ByteBuffer> fbb = Flux.range(0, 1)
                .concatMap(pos -> Mono.fromCallable(() -> ByteBuffer.wrap(data, (int) offset, writeLength)));

            long pageOffset = pageRange.getStart();
            if (pageOffset + writeLength - 1 > pageRange.getEnd()) {
                throw logger.logExceptionAsError(
                    new RuntimeException("The input data length is larger than the page range."));
            }
            pageRange.setStart(pageRange.getStart() + writeLength);
            return this.writePages(fbb.subscribeOn(Schedulers.elastic()), writeLength, pageOffset);
        }

        @Override
        void commit() {
            // PageBlob doesn't need to commit anything.
        }
    }
}
