// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.specialized;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.AppendBlobAccessConditions;
import com.azure.storage.blob.models.AppendPositionAccessConditions;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.PageBlobAccessConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.StorageException;
import com.azure.storage.common.SR;
import com.azure.storage.common.StorageOutputStream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public abstract class BlobOutputStream extends StorageOutputStream {
    /*
     * Holds the last exception this stream encountered.
     */
    volatile IOException lastError;

    BlobOutputStream(final int writeThreshold) {
        super(writeThreshold);
    }

    static BlobOutputStream appendBlobOutputStream(final AppendBlobAsyncClient client,
                                                   final AppendBlobAccessConditions appendBlobAccessConditions) {
        return new AppendBlobOutputStream(client, appendBlobAccessConditions);
    }

    static BlobOutputStream blockBlobOutputStream(final BlockBlobAsyncClient client,
                                                  final BlobAccessConditions accessConditions) {
        return new BlockBlobOutputStream(client, accessConditions);
    }

    static BlobOutputStream pageBlobOutputStream(final PageBlobAsyncClient client, final PageRange pageRange,
                                                 final BlobAccessConditions accessConditions) {
        return new PageBlobOutputStream(client, pageRange, accessConditions);
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
            } catch (final StorageException e) {
                throw new IOException(e);
            }
        } finally {
            // if close() is called again, an exception will be thrown
            this.lastError = new IOException(SR.STREAM_CLOSED);
        }
    }

    private static final class AppendBlobOutputStream extends BlobOutputStream {
        private final AppendBlobAccessConditions appendBlobAccessConditions;
        private final AppendPositionAccessConditions appendPositionAccessConditions;
        private final long initialBlobOffset;
        private final AppendBlobAsyncClient client;

        private AppendBlobOutputStream(final AppendBlobAsyncClient client,
                                       final AppendBlobAccessConditions appendBlobAccessConditions) {
            super(BlockBlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE);
            this.client = client;
            this.appendBlobAccessConditions = appendBlobAccessConditions;

            if (appendBlobAccessConditions != null) {
                this.appendPositionAccessConditions = appendBlobAccessConditions.getAppendPositionAccessConditions();

                if (appendBlobAccessConditions.getAppendPositionAccessConditions().getAppendPosition() != null) {
                    this.initialBlobOffset = appendBlobAccessConditions
                        .getAppendPositionAccessConditions()
                        .getAppendPosition();
                } else {
                    this.initialBlobOffset = client.getProperties().block().getBlobSize();
                }
            } else {
                this.initialBlobOffset = client.getProperties().block().getBlobSize();
                this.appendPositionAccessConditions = new AppendPositionAccessConditions();
            }
        }

        private Mono<Void> appendBlock(Flux<ByteBuffer> blockData, long offset, long writeLength) {
            this.appendPositionAccessConditions.setAppendPosition(offset);

            return client.appendBlockWithResponse(blockData, writeLength, appendBlobAccessConditions)
                .then()
                .onErrorResume(t -> t instanceof IOException || t instanceof StorageException, e -> {
                    this.lastError = new IOException(e);
                    return null;
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
            if (this.appendPositionAccessConditions != null
                && this.appendPositionAccessConditions.getMaxSize() != null
                && this.initialBlobOffset > this.appendPositionAccessConditions.getMaxSize()) {
                this.lastError = new IOException(SR.INVALID_BLOCK_SIZE);
                return Mono.error(this.lastError);
            }

            Flux<ByteBuffer> fbb = Flux.range(0, 1)
                .concatMap(pos -> Mono.fromCallable(() -> ByteBuffer.wrap(data, (int) offset, writeLength)));

            return this.appendBlock(fbb.subscribeOn(Schedulers.elastic()), this.initialBlobOffset, writeLength);
        }

        @Override
        void commit() {
            // AppendBlob doesn't need to commit anything.
        }
    }

    private static final class BlockBlobOutputStream extends BlobOutputStream {
        private final BlobAccessConditions accessConditions;
        private final String blockIdPrefix;
        private final List<String> blockList;
        private final BlockBlobAsyncClient client;

        private BlockBlobOutputStream(final BlockBlobAsyncClient client, final BlobAccessConditions accessConditions) {
            super(BlockBlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE);
            this.client = client;
            this.accessConditions = accessConditions;
            this.blockIdPrefix = UUID.randomUUID().toString() + '-';
            this.blockList = new ArrayList<>();
        }

        /**
         * Generates a new block ID to be used for PutBlock.
         *
         * @return Base64 encoded block ID
         */
        private String getCurrentBlockId() {
            String blockIdSuffix = String.format("%06d", this.blockList.size());
            return Base64.getEncoder().encodeToString((this.blockIdPrefix + blockIdSuffix)
                .getBytes(StandardCharsets.UTF_8));
        }

        private Mono<Void> writeBlock(Flux<ByteBuffer> blockData, String blockId, long writeLength) {
            LeaseAccessConditions leaseAccessConditions = (accessConditions == null)
                ? null : accessConditions.getLeaseAccessConditions();

            return client.stageBlockWithResponse(blockId, blockData, writeLength, leaseAccessConditions)
                .then()
                .onErrorResume(t -> t instanceof StorageException, e -> {
                    this.lastError = new IOException(e);
                    return null;
                });
        }

        @Override
        protected Mono<Void> dispatchWrite(byte[] data, int writeLength, long offset) {
            if (writeLength == 0) {
                return Mono.empty();
            }

            final String blockID = this.getCurrentBlockId();
            this.blockList.add(blockID);

            Flux<ByteBuffer> fbb = Flux.range(0, 1)
                .concatMap(pos -> Mono.fromCallable(() -> ByteBuffer.wrap(data, (int) offset, writeLength)));

            return this.writeBlock(fbb.subscribeOn(Schedulers.elastic()), blockID, writeLength);
        }

        /**
         * Commits the blob, for block blob this uploads the block list.
         */
        @Override
        synchronized void commit() {
            client.commitBlockListWithResponse(this.blockList, null, null, null, this.accessConditions).block();
        }
    }

    private static final class PageBlobOutputStream extends BlobOutputStream {
        private final ClientLogger logger = new ClientLogger(BlobOutputStream.class);
        private final PageBlobAsyncClient client;
        private final PageBlobAccessConditions pageBlobAccessConditions;
        private final PageRange pageRange;

        private PageBlobOutputStream(final PageBlobAsyncClient client, final PageRange pageRange,
                                     final BlobAccessConditions blobAccessConditions) {
            super(BlockBlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE);
            this.client = client;
            this.pageRange = pageRange;
            if (blobAccessConditions != null) {
                this.pageBlobAccessConditions = new PageBlobAccessConditions()
                    .setModifiedAccessConditions(blobAccessConditions.getModifiedAccessConditions())
                    .setLeaseAccessConditions(blobAccessConditions.getLeaseAccessConditions());
            } else {
                this.pageBlobAccessConditions = null;
            }
        }

        private Mono<Void> writePages(Flux<ByteBuffer> pageData, int length, long offset) {
            return client.uploadPagesWithResponse(new PageRange().setStart(offset).setEnd(offset + length - 1),
                pageData, pageBlobAccessConditions)
                .then()
                .onErrorResume(t -> t instanceof StorageException, e -> {
                    this.lastError = new IOException(e);
                    return null;
                });
        }

        @Override
        protected Mono<Void> dispatchWrite(byte[] data, int writeLength, long offset) {
            if (writeLength == 0) {
                return Mono.empty();
            }

            if (writeLength % PageBlobAsyncClient.PAGE_BYTES != 0) {
                return Mono.error(new IOException(String.format(SR.INVALID_NUMBER_OF_BYTES_IN_THE_BUFFER,
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
