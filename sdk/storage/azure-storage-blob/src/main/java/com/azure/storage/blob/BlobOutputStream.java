// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob;

import com.azure.storage.blob.models.AppendBlobAccessConditions;
import com.azure.storage.blob.models.AppendPositionAccessConditions;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.PageBlobAccessConditions;
import com.azure.storage.blob.models.PageRange;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

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

    public static BlobOutputStream appendBlobOutputStream(final AppendBlobAsyncClient client, final AppendBlobAccessConditions appendBlobAccessConditions) {
        return new AppendBlobOutputStream(client, appendBlobAccessConditions);
    }

    public static BlobOutputStream blockBlobOutputStream(final BlockBlobAsyncClient client, final BlobAccessConditions accessConditions) {
        return new BlockBlobOutputStream(client, accessConditions);
    }

    public static BlobOutputStream pageBlobOutputStream(final PageBlobAsyncClient client, final long length, final BlobAccessConditions accessConditions) {
        return new PageBlobOutputStream(client, length, accessConditions);
    }

    abstract Mono<Integer> dispatchWrite(Flux<ByteBuffer> bufferRef, int writeLength, long offset);

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

    private Mono<Integer> processChunk(byte[] data, int position, int offset, int length) {
        int chunkLength = this.writeThreshold;
        if (position + chunkLength > offset + length) {
            chunkLength = offset + length - position;
        }
        Flux<ByteBuffer> chunkData = new ByteBufStreamFromByteArray(data, 64 * 1024, position, chunkLength);
        return dispatchWrite(chunkData, chunkLength, position - offset)
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
     * @throws IOException If an I/O error occurs. In particular, an IOException may be thrown if the output stream has
     * been closed.
     */
    @Override
    public void write(final byte[] data) throws IOException {
        this.write(data, 0, data.length);
    }

    /**
     * Writes length bytes from the specified byte array starting at offset to this output stream.
     * <p>
     *
     * @param data A <code>byte</code> array which represents the data to write.
     * @param offset An <code>int</code> which represents the start offset in the data.
     * @param length An <code>int</code> which represents the number of bytes to write.
     * @throws IOException If an I/O error occurs. In particular, an IOException may be thrown if the output stream has
     * been closed.
     */
    @Override
    public void write(final byte[] data, final int offset, final int length) throws IOException {
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
     * @throws IOException If an I/O error occurs. In particular, an IOException may be thrown if the output stream has
     * been closed.
     */
    @Override
    public void write(final int byteVal) throws IOException {
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


    private static class AppendBlobOutputStream extends BlobOutputStream {
        private final AppendBlobAccessConditions appendBlobAccessConditions;
        private final AppendPositionAccessConditions appendPositionAccessConditions;
        private final long initialBlobOffset;
        private final AppendBlobAsyncClient client;

        private AppendBlobOutputStream(final AppendBlobAsyncClient client, final AppendBlobAccessConditions appendBlobAccessConditions) {
            this.client = client;
            this.writeThreshold = BlockBlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;
            this.appendBlobAccessConditions = appendBlobAccessConditions;

            if (appendBlobAccessConditions != null) {
                this.appendPositionAccessConditions = appendBlobAccessConditions.appendPositionAccessConditions();

                if (appendBlobAccessConditions.appendPositionAccessConditions().appendPosition() != null) {
                    this.initialBlobOffset = appendBlobAccessConditions.appendPositionAccessConditions().appendPosition();
                } else {
                    this.initialBlobOffset = client.getProperties().block().value().blobSize();
                }
            } else {
                this.initialBlobOffset = client.getProperties().block().value().blobSize();
                this.appendPositionAccessConditions = new AppendPositionAccessConditions();
            }
        }

        private Mono<Void> appendBlock(Flux<ByteBuffer> blockData, long offset, long writeLength) {
            this.appendPositionAccessConditions.appendPosition(offset);

            return client.appendBlock(blockData, writeLength, appendBlobAccessConditions)
                .then()
                .onErrorResume(t -> t instanceof IOException || t instanceof StorageException, e -> {
                    this.lastError = new IOException(e);
                    return null;
                });
        }

        @Override
        Mono<Integer> dispatchWrite(Flux<ByteBuffer> bufferRef, int writeLength, long offset) {
            if (writeLength == 0) {
                return Mono.empty();
            }

            // We cannot differentiate between max size condition failing only in the retry versus failing in the
            // first attempt and retry even for a single writer scenario. So we will eliminate the latter and handle
            // the former in the append block method.
            if (this.appendPositionAccessConditions != null &&
                this.appendPositionAccessConditions.maxSize() != null &&
                this.initialBlobOffset > this.appendPositionAccessConditions.maxSize()) {
                this.lastError = new IOException(SR.INVALID_BLOCK_SIZE);
                return Mono.error(this.lastError);
            }

            return this.appendBlock(bufferRef, offset, writeLength).then(Mono.justOrEmpty(writeLength));
        }

        @Override
        void commit() {
            // AppendBlob doesn't need to commit anything.
        }
    }

    private static class BlockBlobOutputStream extends BlobOutputStream {
        private final BlobAccessConditions accessConditions;
        private final String blockIdPrefix;
        private final TreeMap<Long, String> blockList;
        private final BlockBlobAsyncClient client;

        private BlockBlobOutputStream(final BlockBlobAsyncClient client, final BlobAccessConditions accessConditions) {
            this.client = client;
            this.accessConditions = accessConditions;
            this.blockIdPrefix = UUID.randomUUID().toString() + '-';
            this.blockList = new TreeMap<>();
            this.writeThreshold = BlockBlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;
        }

        /**
         * Generates a new block ID to be used for PutBlock.
         *
         * @return Base64 encoded block ID
         */
        private String getCurrentBlockId() {
            String blockIdSuffix = String.format("%06d", this.blockList.size());
            return Base64.getEncoder().encodeToString((this.blockIdPrefix + blockIdSuffix).getBytes(StandardCharsets.UTF_8));
        }

        private Mono<Void> writeBlock(Flux<ByteBuffer> blockData, String blockId, long writeLength) {
            LeaseAccessConditions leaseAccessConditions = (accessConditions == null) ? null : accessConditions.leaseAccessConditions();

            return client.stageBlock(blockId, blockData, writeLength, leaseAccessConditions)
                .then()
                .onErrorResume(t -> t instanceof StorageException, e -> {
                    this.lastError = new IOException(e);
                    return null;
                });
        }

        @Override
        Mono<Integer> dispatchWrite(Flux<ByteBuffer> bufferRef, int writeLength, long offset) {
            if (writeLength == 0) {
                return Mono.empty();
            }

            final String blockID = this.getCurrentBlockId();
            this.blockList.put(offset, blockID);
            return this.writeBlock(bufferRef, blockID, writeLength).then(Mono.just(writeLength));
        }

        /**
         * Commits the blob, for block blob this uploads the block list.
         */
        @Override
        synchronized void commit() {
            client.commitBlockList(new ArrayList<>(this.blockList.values()), null, null, this.accessConditions).block();
        }
    }

    private static class PageBlobOutputStream extends BlobOutputStream {
        private final PageBlobAsyncClient client;
        private final PageBlobAccessConditions pageBlobAccessConditions;

        private PageBlobOutputStream(final PageBlobAsyncClient client, final long length, final BlobAccessConditions blobAccessConditions) {
            this.client = client;
            this.writeThreshold = (int) Math.min(BlockBlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE, length);

            if (blobAccessConditions != null) {
                this.pageBlobAccessConditions = new PageBlobAccessConditions()
                    .modifiedAccessConditions(blobAccessConditions.modifiedAccessConditions())
                    .leaseAccessConditions(blobAccessConditions.leaseAccessConditions());
            } else {
                this.pageBlobAccessConditions = null;
            }
        }

        private Mono<Void> writePages(Flux<ByteBuffer> pageData, long offset, long writeLength) {
            return client.uploadPages(new PageRange().start(offset).end(offset + writeLength - 1), pageData, pageBlobAccessConditions)
                .then()
                .onErrorResume(t -> t instanceof StorageException, e -> {
                    this.lastError = new IOException(e);
                    return null;
                });
        }

        @Override
        Mono<Integer> dispatchWrite(Flux<ByteBuffer> bufferRef, int writeLength, long offset) {
            if (writeLength == 0) {
                return Mono.empty();
            }

            if (writeLength % PageBlobAsyncClient.PAGE_BYTES != 0) {
                return Mono.error(new IOException(String.format(SR.INVALID_NUMBER_OF_BYTES_IN_THE_BUFFER, writeLength)));
            }

            return this.writePages(bufferRef, offset, writeLength).then(Mono.just(writeLength));
        }

        @Override
        void commit() {
            // PageBlob doesn't need to commit anything.
        }
    }

    private static final class ByteBufStreamFromByteArray extends Flux<ByteBuffer> {
        private final byte[] bigByteArray;
        private final int chunkSize;
        private final int offset;
        private final int length;

        ByteBufStreamFromByteArray(byte[] bigByteArray, int chunkSize, int offset, int length) {
            this.bigByteArray = bigByteArray;
            this.chunkSize = chunkSize;
            this.offset = offset;
            this.length = length;
        }

        @Override
        public void subscribe(CoreSubscriber<? super ByteBuffer> actual) {
            ByteBufStreamFromByteArray.FileReadSubscription subscription = new ByteBufStreamFromByteArray.FileReadSubscription(actual, bigByteArray, chunkSize, offset, length);
            actual.onSubscribe(subscription);
        }

        static final class FileReadSubscription implements Subscription, CompletionHandler<Integer, ByteBuffer> {
            private static final int NOT_SET = -1;
            private static final long serialVersionUID = -6831808726875304256L;
            //
            private final Subscriber<? super ByteBuffer> subscriber;
            private volatile int position;
            //
            private final byte[] bigByteArray;
            private final int chunkSize;
            private final int offset;
            private final int length;
            //
            private volatile boolean done;
            private Throwable error;
            private volatile ByteBuffer next;
            private volatile boolean cancelled;
            //
            volatile int wip;
            @SuppressWarnings("rawtypes")
            static final AtomicIntegerFieldUpdater<ByteBufStreamFromByteArray.FileReadSubscription> WIP = AtomicIntegerFieldUpdater.newUpdater(ByteBufStreamFromByteArray.FileReadSubscription.class, "wip");
            volatile long requested;
            @SuppressWarnings("rawtypes")
            static final AtomicLongFieldUpdater<ByteBufStreamFromByteArray.FileReadSubscription> REQUESTED = AtomicLongFieldUpdater.newUpdater(ByteBufStreamFromByteArray.FileReadSubscription.class, "requested");
            //

            FileReadSubscription(Subscriber<? super ByteBuffer> subscriber, byte[] bigByteArray, int chunkSize, int offset, int length) {
                this.subscriber = subscriber;
                //
                this.bigByteArray = bigByteArray;
                this.chunkSize = chunkSize;
                this.offset = offset;
                this.length = length;
                //
                this.position = NOT_SET;
            }

            //region Subscription implementation

            @Override
            public void request(long n) {
                if (Operators.validate(n)) {
                    Operators.addCap(REQUESTED, this, n);
                    drain();
                }
            }

            @Override
            public void cancel() {
                this.cancelled = true;
            }

            //endregion

            //region CompletionHandler implementation

            @Override
            public void completed(Integer bytesRead, ByteBuffer buffer) {
                if (!cancelled) {
                    if (bytesRead == -1) {
                        done = true;
                    } else {
                        // use local variable to perform fewer volatile reads
                        int pos = position;
                        //
                        int bytesWanted = Math.min(bytesRead, maxRequired(pos));
                        buffer.position(bytesWanted);
                        int position2 = pos + bytesWanted;
                        //noinspection NonAtomicOperationOnVolatileField
                        position = position2;
                        next = buffer;
                        if (position2 >= offset + length) {
                            done = true;
                        }
                    }
                    drain();
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                if (!cancelled) {
                    // must set error before setting done to true
                    // so that is visible in drain loop
                    error = exc;
                    done = true;
                    drain();
                }
            }

            //endregion

            private void drain() {
                if (WIP.getAndIncrement(this) != 0) {
                    return;
                }
                // on first drain (first request) we initiate the first read
                if (position == NOT_SET) {
                    position = offset;
                    doRead();
                }
                int missed = 1;
                for (; ; ) {
                    if (cancelled) {
                        return;
                    }
                    if (REQUESTED.get(this) > 0) {
                        boolean emitted;
                        // read d before next to avoid race
                        boolean d = done;
                        ByteBuffer bb = next;
                        if (bb != null) {
                            next = null;
                            //
                            // try {
                            subscriber.onNext(bb);
                            // } finally {
                            // Note: Don't release here, we follow netty disposal pattern
                            // it's consumers responsiblity to release chunks after consumption.
                            //
                            // ReferenceCountUtil.release(bb);
                            // }
                            //
                            emitted = true;
                        } else {
                            emitted = false;
                        }
                        if (d) {
                            if (error != null) {
                                subscriber.onError(error);
                                // exit without reducing wip so that further drains will be NOOP
                                return;
                            } else {
                                subscriber.onComplete();
                                // exit without reducing wip so that further drains will be NOOP
                                return;
                            }
                        }
                        if (emitted) {
                            // do this after checking d to avoid calling read
                            // when done
                            Operators.produced(REQUESTED, this, 1);
                            //
                            doRead();
                        }
                    }
                    missed = WIP.addAndGet(this, -missed);
                    if (missed == 0) {
                        return;
                    }
                }
            }

            private void doRead() {
                // use local variable to limit volatile reads
                int pos = position;
                int readSize = Math.min(chunkSize, maxRequired(pos));
                ByteBuffer innerBuf = ByteBuffer.allocate(readSize);
                try {
                    innerBuf.put(bigByteArray, pos, readSize);
                    completed(readSize, innerBuf);
                } catch (Exception e) {
                    failed(e, innerBuf);
                }
            }

            private int maxRequired(long pos) {
                long maxRequired = offset + length - pos;
                if (maxRequired <= 0) {
                    return 0;
                } else {
                    int m = (int) (maxRequired);
                    // support really large files by checking for overflow
                    if (m < 0) {
                        return Integer.MAX_VALUE;
                    } else {
                        return m;
                    }
                }
            }
        }
    }
}
