// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.specialized;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.models.AppendBlobAccessConditions;
import com.azure.storage.blob.models.AppendPositionAccessConditions;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.PageBlobAccessConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.StorageException;
import com.azure.storage.common.SR;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public abstract class BlobOutputStream extends OutputStream {
    /*
     * Holds the write threshold of number of bytes to buffer prior to dispatching a write. For block blob this is the
     * block size, for page blob this is the Page commit size.
     */
    int writeThreshold;

    /*
     * Holds the last exception this stream encountered.
     */
    volatile IOException lastError;

    static BlobOutputStream appendBlobOutputStream(final AppendBlobAsyncClient client,
        final AppendBlobAccessConditions appendBlobAccessConditions) {
        return new AppendBlobOutputStream(client, appendBlobAccessConditions);
    }

    static BlobOutputStream blockBlobOutputStream(final BlockBlobAsyncClient client,
        final BlobAccessConditions accessConditions) {
        return new BlockBlobOutputStream(client, accessConditions);
    }

    static BlobOutputStream pageBlobOutputStream(final PageBlobAsyncClient client, final long length,
        final BlobAccessConditions accessConditions) {
        return new PageBlobOutputStream(client, length, accessConditions);
    }

    abstract Mono<Void> dispatchWrite(byte[] data, int writeLength, long offset);

    abstract void commit();

    /**
     * Writes the data to the buffer and triggers writes to the service as needed.
     *
     * @param data A <code>byte</code> array which represents the data to write.
     * @param offset An <code>int</code> which represents the start offset in the data.
     * @param length An <code>int</code> which represents the number of bytes to write.
     * @throws IOException If an I/O error occurs. In particular, an IOException may be thrown if the output stream has
     * been closed.
     */
    private void writeInternal(final byte[] data, int offset, int length) {
        int chunks = (int) (Math.ceil((double) length / (double) this.writeThreshold));
        Flux.range(0, chunks).map(c -> offset + c * this.writeThreshold)
            .concatMap(pos -> processChunk(data, pos, offset, length))
            .then()
            .block();
    }

    private Mono<Void> processChunk(byte[] data, int position, int offset, int length) {
        int chunkLength = this.writeThreshold;

        if (position + chunkLength > offset + length) {
            chunkLength = offset + length - position;
        }

        // Flux<ByteBuffer> chunkData = new ByteBufferStreamFromByteArray(data, writeThreshold, position, chunkLength);
        return dispatchWrite(data, chunkLength, position - offset)
            .doOnError(t -> {
                if (t instanceof IOException) {
                    lastError = (IOException) t;
                } else {
                    lastError = new IOException(t);
                }
            });
    }

    /**
     * Helper function to check if the stream is faulted, if it is it surfaces the exception.
     *
     * @throws IOException If an I/O error occurs. In particular, an IOException may be thrown if the output stream has
     * been closed.
     */
    private void checkStreamState() throws IOException {
        if (this.lastError != null) {
            throw this.lastError;
        }
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out. If any data remains in the
     * buffer it is committed to the service.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void flush() throws IOException {
        this.checkStreamState();
    }

    /**
     * Writes <code>b.length</code> bytes from the specified byte array to this output stream.
     * <p>
     *
     * @param data A <code>byte</code> array which represents the data to write.
     */
    @Override
    public void write(@NonNull final byte[] data) {
        this.write(data, 0, data.length);
    }

    /**
     * Writes length bytes from the specified byte array starting at offset to this output stream.
     * <p>
     *
     * @param data A <code>byte</code> array which represents the data to write.
     * @param offset An <code>int</code> which represents the start offset in the data.
     * @param length An <code>int</code> which represents the number of bytes to write.
     * @throws IndexOutOfBoundsException If {@code offset} or {@code length} are less than {@code 0} or {@code offset}
     * plus {@code length} is greater than the {@code data} length.
     */
    @Override
    public void write(@NonNull final byte[] data, final int offset, final int length) {
        if (offset < 0 || length < 0 || length > data.length - offset) {
            throw new IndexOutOfBoundsException();
        }

        this.writeInternal(data, offset, length);
    }

    /**
     * Writes the specified byte to this output stream. The general contract for write is that one byte is written to
     * the output stream. The byte to be written is the eight low-order bits of the argument b. The 24 high-order bits
     * of b are ignored.
     * <p>
     * <code>true</code> is acceptable for you.
     *
     * @param byteVal An <code>int</code> which represents the bye value to write.
     */
    @Override
    public void write(final int byteVal) {
        this.write(new byte[]{(byte) (byteVal & 0xFF)});
    }

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
            this.client = client;
            this.writeThreshold = BlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;
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
        Mono<Void> dispatchWrite(byte[] data, int writeLength, long offset) {
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

            return this.appendBlock(fbb.subscribeOn(Schedulers.elastic()), offset, writeLength);
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
            this.client = client;
            this.accessConditions = accessConditions;
            this.blockIdPrefix = UUID.randomUUID().toString() + '-';
            this.blockList = new ArrayList<>();
            this.writeThreshold = BlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;
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
        Mono<Void> dispatchWrite(byte[] data, int writeLength, long offset) {
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
        private final PageBlobAsyncClient client;
        private final PageBlobAccessConditions pageBlobAccessConditions;

        private PageBlobOutputStream(final PageBlobAsyncClient client, final long length,
            final BlobAccessConditions blobAccessConditions) {
            this.client = client;
            this.writeThreshold = (int) Math.min(BlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE, length);

            if (blobAccessConditions != null) {
                this.pageBlobAccessConditions = new PageBlobAccessConditions()
                    .setModifiedAccessConditions(blobAccessConditions.getModifiedAccessConditions())
                    .setLeaseAccessConditions(blobAccessConditions.getLeaseAccessConditions());
            } else {
                this.pageBlobAccessConditions = null;
            }
        }

        private Mono<Void> writePages(Flux<ByteBuffer> pageData, long offset, long writeLength) {
            return client.uploadPagesWithResponse(new PageRange().setStart(offset).setEnd(offset + writeLength - 1),
                pageData, pageBlobAccessConditions)
                .then()
                .onErrorResume(t -> t instanceof StorageException, e -> {
                    this.lastError = new IOException(e);
                    return null;
                });
        }

        @Override
        Mono<Void> dispatchWrite(byte[] data, int writeLength, long offset) {
            if (writeLength == 0) {
                return Mono.empty();
            }

            if (writeLength % PageBlobAsyncClient.PAGE_BYTES != 0) {
                return Mono.error(new IOException(String.format(SR.INVALID_NUMBER_OF_BYTES_IN_THE_BUFFER,
                    writeLength)));
            }

            Flux<ByteBuffer> fbb = Flux.range(0, 1)
                .concatMap(pos -> Mono.fromCallable(() -> ByteBuffer.wrap(data, (int) offset, writeLength)));

            return this.writePages(fbb.subscribeOn(Schedulers.elastic()), offset, writeLength);
        }

        @Override
        void commit() {
            // PageBlob doesn't need to commit anything.
        }
    }
}
