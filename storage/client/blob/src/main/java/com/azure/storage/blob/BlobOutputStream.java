// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob;

import com.azure.storage.blob.models.AppendBlobAccessConditions;
import com.azure.storage.blob.models.AppendPositionAccessConditions;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.PageBlobAccessConditions;
import com.azure.storage.blob.models.PageRange;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;


public final class BlobOutputStream extends OutputStream {
    /**
     * Holds the {@link BlobAccessConditions} object that represents the access conditions for the blob.
     */
    private BlobAccessConditions accessCondition;

    private AppendPositionAccessConditions appendPositionAccessConditions;

    /**
     * Used for block blobs, holds the block id prefix.
     */
    private String blockIdPrefix;

    /**
     * Used for block blobs, holds the block list.
     */
    private TreeMap<Long, String> blockList;

    /**
     * Holds the write threshold of number of bytes to buffer prior to dispatching a write. For block blob this is the
     * block size, for page blob this is the Page commit size.
     */
    private int internalWriteThreshold = -1;

    /**
     * Holds the last exception this stream encountered.
     */
    private volatile IOException lastError = null;


    private long initialBlobOffset;

    /**
     * Holds the reference to the blob this stream is associated with.
     */
    private final BlobAsyncClient blobClient;

    /**
     * Determines if this stream is used against a page blob or block blob.
     */
    private BlobType streamType = BlobType.BLOCK_BLOB;

    /**
     * Initializes a new instance of the BlobOutputStream class.
     *
     * @param parentBlob
     *            A {@link BlobAsyncClient} object which represents the blob that this stream is associated with.
     *
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    private BlobOutputStream(final BlobAsyncClient parentBlob) throws StorageException {
        this.blobClient = parentBlob;
//        completion = Flux.defer(() -> {
//            if (this.streamType == BlobType.APPEND_BLOB) {
//                return writeProcessor.concatMap(b -> {
//                    long offset = currentOffset.getAndAdd(b.length);
//                    return dispatchWrite(b, offset);
//                });
//            } else {
//                return writeProcessor.map(b -> Tuples.of(b, currentOffset.getAndAdd(b.length)))
//                .flatMap(chunk -> dispatchWrite(chunk.getT1(), chunk.getT2()));
//            }
//        })
//        .doOnError(t -> {
//            if (t instanceof IOException) {
//                lastError = (IOException) t;
//            } else {
//                lastError = new IOException(t);
//            }
//            completionSink.error(t);
//        })
//        .doOnNext(length -> completionSink.next(length))
//        .doOnComplete(() -> completionSink.complete());
    }

    /**
     * Initializes a new instance of the BlobOutputStream class for a CloudBlockBlob
     *
     * @param parentBlob
     *            A {@link BlobAsyncClient} object which represents the blob that this stream is associated with.
     * @param accessCondition
     *            An {@link BlobAccessConditions} object which represents the access conditions for the blob.
     *
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    BlobOutputStream(final BlockBlobAsyncClient parentBlob, final BlobAccessConditions accessCondition) throws StorageException {
        this((BlobAsyncClient) parentBlob);

        this.accessCondition = accessCondition;
        this.blockList = new TreeMap<>();
        this.blockIdPrefix = UUID.randomUUID().toString() + "-";

        this.streamType = BlobType.BLOCK_BLOB;
        this.internalWriteThreshold = (int) BlockBlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;
    }

    /**
     * Initializes a new instance of the BlobOutputStream class for a CloudPageBlob
     *
     * @param parentBlob
     *            A {@link PageBlobClient} object which represents the blob that this stream is associated with.
     * @param length
     *            A <code>long</code> which represents the length of the page blob in bytes, which must be a multiple of
     *            512.
     * @param accessCondition
     *            An {@link BlobAccessConditions} object which represents the access conditions for the blob.
     *
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    BlobOutputStream(final PageBlobAsyncClient parentBlob, final long length, final BlobAccessConditions accessCondition)
        throws StorageException {
        this((BlobAsyncClient) parentBlob);
        this.streamType = BlobType.PAGE_BLOB;
        this.accessCondition = accessCondition;
        this.internalWriteThreshold = (int) Math.min(BlockBlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE, length);
    }

    /**
     * Initializes a new instance of the BlobOutputStream class for a CloudAppendBlob
     *
     * @param parentBlob
     *            A {@link AppendBlobAsyncClient} object which represents the blob that this stream is associated with.
     * @param accessCondition
     *            An {@link BlobAccessConditions} object which represents the access conditions for the blob.
     *
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    BlobOutputStream(final AppendBlobAsyncClient parentBlob, final AppendBlobAccessConditions accessCondition)
        throws StorageException {
        this((BlobAsyncClient) parentBlob);
        this.streamType = BlobType.APPEND_BLOB;

        this.accessCondition = new BlobAccessConditions();
        if (accessCondition != null) {
            this.appendPositionAccessConditions = accessCondition.appendPositionAccessConditions();
            this.accessCondition = new BlobAccessConditions().modifiedAccessConditions(accessCondition.modifiedAccessConditions()).leaseAccessConditions(accessCondition.leaseAccessConditions());
            if (accessCondition.appendPositionAccessConditions().appendPosition() != null) {
                this.initialBlobOffset = accessCondition.appendPositionAccessConditions().appendPosition();
            } else {
                this.initialBlobOffset = parentBlob.getProperties().block().value().blobSize();
            }
        }

        this.internalWriteThreshold = (int) BlockBlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;
    }

    /**
     * Helper function to check if the stream is faulted, if it is it surfaces the exception.
     *
     * @throws IOException
     *             If an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     */
    private void checkStreamState() throws IOException {
        if (this.lastError != null) {
            throw this.lastError;
        }
    }

    /**
     * Closes this output stream and releases any system resources associated with this stream. If any data remains in
     * the buffer it is committed to the service.
     *
     * @throws IOException
     *             If an I/O error occurs.
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

    /**
     * Commits the blob, for block blob this uploads the block list.
     *
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    private synchronized void commit() throws StorageException {
        if (this.streamType == BlobType.BLOCK_BLOB) {
            // wait for all blocks to finish
            assert this.blobClient instanceof BlockBlobAsyncClient;
            final BlockBlobAsyncClient blobRef = (BlockBlobAsyncClient) this.blobClient;
            blobRef.commitBlockList(new ArrayList<>(this.blockList.values()), null, null, this.accessCondition).block();
        }
    }

    /**
     * Dispatches a write operation for a given length.
     *
     * @throws IOException
     *             If an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     */
    private Mono<Integer> dispatchWrite(Flux<ByteBuf> bufferRef, int writeLength, long offset) {
        if (writeLength == 0) {
            return Mono.empty();
        }

        if (this.streamType == BlobType.PAGE_BLOB && (writeLength % Constants.PAGE_SIZE != 0)) {
            return Mono.error(new IOException(String.format(SR.INVALID_NUMBER_OF_BYTES_IN_THE_BUFFER, writeLength)));
        }

        if (this.streamType == BlobType.BLOCK_BLOB) {
            final String blockID = this.getCurrentBlockId();
            this.blockList.put(offset, blockID);
            return BlobOutputStream.this.writeBlock(bufferRef, blockID, writeLength).then(Mono.just(writeLength));
        } else if (this.streamType == BlobType.PAGE_BLOB) {
            return BlobOutputStream.this.writePages(bufferRef, offset, writeLength).then(Mono.just(writeLength));
        } else if (this.streamType == BlobType.APPEND_BLOB) {
            // We cannot differentiate between max size condition failing only in the retry versus failing in the
            // first attempt and retry even for a single writer scenario. So we will eliminate the latter and handle
            // the former in the append block method.
            if (this.appendPositionAccessConditions != null && this.appendPositionAccessConditions.maxSize() != null
                && this.initialBlobOffset > this.appendPositionAccessConditions.maxSize()) {
                this.lastError = new IOException(SR.INVALID_BLOCK_SIZE);
                return Mono.error(this.lastError);
            }

            return BlobOutputStream.this.appendBlock(bufferRef, offset, writeLength).then(Mono.justOrEmpty(writeLength));
        } else {
            return Mono.error(new RuntimeException("Unknown blob type " + this.streamType));
        }
    }

    private Mono<Void> writeBlock(Flux<ByteBuf> blockData, String blockId, long writeLength) {
        assert this.blobClient instanceof BlockBlobAsyncClient;
        final BlockBlobAsyncClient blobRef = (BlockBlobAsyncClient) this.blobClient;

        LeaseAccessConditions leaseAccessConditions = accessCondition == null ? null : accessCondition.leaseAccessConditions();

        return blobRef.stageBlock(blockId, blockData, writeLength, leaseAccessConditions)
            .then()
            .onErrorResume(t -> t instanceof StorageException, e -> {
                this.lastError = new IOException(e);
                return null;
            });
    }

    private Mono<Void> writePages(Flux<ByteBuf> pageData, long offset, long writeLength) {
        assert this.blobClient instanceof PageBlobAsyncClient;
        final PageBlobAsyncClient blobRef = (PageBlobAsyncClient) this.blobClient;

        PageBlobAccessConditions pageBlobAccessConditions = accessCondition == null ? null : new PageBlobAccessConditions().leaseAccessConditions(accessCondition.leaseAccessConditions()).modifiedAccessConditions(accessCondition.modifiedAccessConditions());

        return blobRef.uploadPages(new PageRange().start(offset).end(offset + writeLength - 1), pageData, pageBlobAccessConditions)
            .then()
            .onErrorResume(t -> t instanceof StorageException, e -> {
                this.lastError = new IOException(e);
                return null;
            });
    }

    private Mono<Void> appendBlock(Flux<ByteBuf> blockData, long offset, long writeLength) {
        assert this.blobClient instanceof AppendBlobAsyncClient;
        final AppendBlobAsyncClient blobRef = (AppendBlobAsyncClient) this.blobClient;
        if (this.appendPositionAccessConditions == null) {
            appendPositionAccessConditions = new AppendPositionAccessConditions();
        }
        this.appendPositionAccessConditions.appendPosition(offset);

        AppendBlobAccessConditions appendBlobAccessConditions = accessCondition == null ? null : new AppendBlobAccessConditions().leaseAccessConditions(accessCondition.leaseAccessConditions()).modifiedAccessConditions(accessCondition.modifiedAccessConditions());
        return blobRef.appendBlock(blockData, writeLength, appendBlobAccessConditions)
            .then()
            .onErrorResume(t -> t instanceof IOException || t instanceof StorageException, e -> {
                this.lastError = new IOException(e);
                return null;
            });
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out. If any data remains in the
     * buffer it is committed to the service.
     *
     * @throws IOException
     *             If an I/O error occurs.
     */
    @Override
    public void flush() throws IOException {
        this.checkStreamState();
    }

    /**
     * Generates a new block ID to be used for PutBlock.
     *
     * @return Base64 encoded block ID
     */
    private String getCurrentBlockId() {
        String blockIdSuffix = String.format("%06d", this.blockList.size());

        byte[] blockIdInBytes;
        blockIdInBytes = (this.blockIdPrefix + blockIdSuffix).getBytes(StandardCharsets.UTF_8);

        return Base64.getEncoder().encodeToString(blockIdInBytes);
    }

    /**
     * Writes <code>b.length</code> bytes from the specified byte array to this output stream.
     * <p>
     *
     * @param data
     *            A <code>byte</code> array which represents the data to write.
     *
     * @throws IOException
     *             If an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     */
    @Override
    public void write(final byte[] data) throws IOException {
        this.write(data, 0, data.length);
    }

    /**
     * Writes length bytes from the specified byte array starting at offset to this output stream.
     * <p>
     *
     * @param data
     *            A <code>byte</code> array which represents the data to write.
     * @param offset
     *            An <code>int</code> which represents the start offset in the data.
     * @param length
     *            An <code>int</code> which represents the number of bytes to write.
     *
     * @throws IOException
     *             If an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
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
     * @param byteVal
     *            An <code>int</code> which represents the bye value to write.
     *
     * @throws IOException
     *             If an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     */
    @Override
    public void write(final int byteVal) throws IOException {
        this.write(new byte[] { (byte) (byteVal & 0xFF) });
    }

    /**
     * Writes the data to the buffer and triggers writes to the service as needed.
     *
     * @param data
     *            A <code>byte</code> array which represents the data to write.
     * @param offset
     *            An <code>int</code> which represents the start offset in the data.
     * @param length
     *            An <code>int</code> which represents the number of bytes to write.
     *
     * @throws IOException
     *             If an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     */
    private void writeInternal(final byte[] data, int offset, int length) {
        int chunks = (int) (Math.ceil((double) length / (double) this.internalWriteThreshold));
        Flux<Integer> chunkPositions = Flux.range(0, chunks).map(c -> offset + c * this.internalWriteThreshold);
        if (this.streamType == BlobType.APPEND_BLOB) {
            chunkPositions.concatMap(pos -> processChunk(data, pos, offset, length)).then().block();
        } else {
            chunkPositions.concatMap(pos -> processChunk(data, pos, offset, length)).then().block();
        }

//        synchronized (outBufferLock) {
//            while (length > 0) {
//                this.checkStreamState();
//
//                final int availableBufferBytes = this.internalWriteThreshold - this.outBuffer.size();
//                final int nextWrite = Math.min(availableBufferBytes, length);
//
//                this.outBuffer.write(data, offset, nextWrite);
//                offset += nextWrite;
//                length -= nextWrite;
//
//                if (this.outBuffer.size() == this.internalWriteThreshold) {
//                    this.writeSink.next(outBuffer.toByteArray());
//                    outBuffer.reset();
//                    numInFlight.incrementAndGet();
//                }
//            }
//        }
    }

    private Mono<Integer> processChunk(byte[] data, int position, int offset, int length) {
        int chunkLength = this.internalWriteThreshold;
        if (position + chunkLength > offset + length) {
            chunkLength = offset + length - position;
        }
        Flux<ByteBuf> chunkData = new ByteBufStreamFromByteArray(data, 64 * 1024, position, chunkLength);
        return dispatchWrite(chunkData, chunkLength, position - offset)
            .doOnError(t -> {
                if (t instanceof IOException) {
                    lastError = (IOException) t;
                } else {
                    lastError = new IOException(t);
                }
            });
    }

    private static final class ByteBufStreamFromByteArray extends Flux<ByteBuf> {
        private final ByteBufAllocator alloc;
        private final byte[] bigByteArray;
        private final int chunkSize;
        private final int offset;
        private final int length;

        ByteBufStreamFromByteArray(byte[] bigByteArray, int chunkSize, int offset, int length) {
            this.alloc = ByteBufAllocator.DEFAULT;
            this.bigByteArray = bigByteArray;
            this.chunkSize = chunkSize;
            this.offset = offset;
            this.length = length;
        }

        @Override
        public void subscribe(CoreSubscriber<? super ByteBuf> actual) {
            ByteBufStreamFromByteArray.FileReadSubscription subscription = new ByteBufStreamFromByteArray.FileReadSubscription(actual, bigByteArray, alloc, chunkSize, offset, length);
            actual.onSubscribe(subscription);
        }

        static final class FileReadSubscription implements Subscription, CompletionHandler<Integer, ByteBuf> {
            private static final int NOT_SET = -1;
            private static final long serialVersionUID = -6831808726875304256L;
            //
            private final Subscriber<? super ByteBuf> subscriber;
            private volatile int position;
            //
            private final byte[] bigByteArray;
            private final ByteBufAllocator alloc;
            private final int chunkSize;
            private final int offset;
            private final int length;
            //
            private volatile boolean done;
            private Throwable error;
            private volatile ByteBuf next;
            private volatile boolean cancelled;
            //
            volatile int wip;
            @SuppressWarnings("rawtypes")
            static final AtomicIntegerFieldUpdater<ByteBufStreamFromByteArray.FileReadSubscription> WIP = AtomicIntegerFieldUpdater.newUpdater(ByteBufStreamFromByteArray.FileReadSubscription.class, "wip");
            volatile long requested;
            @SuppressWarnings("rawtypes")
            static final AtomicLongFieldUpdater<ByteBufStreamFromByteArray.FileReadSubscription> REQUESTED = AtomicLongFieldUpdater.newUpdater(ByteBufStreamFromByteArray.FileReadSubscription.class, "requested");
            //

            FileReadSubscription(Subscriber<? super ByteBuf> subscriber, byte[] bigByteArray, ByteBufAllocator alloc, int chunkSize, int offset, int length) {
                this.subscriber = subscriber;
                //
                this.bigByteArray = bigByteArray;
                this.alloc = alloc;
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
            public void completed(Integer bytesRead, ByteBuf buffer) {
                if (!cancelled) {
                    if (bytesRead == -1) {
                        done = true;
                    } else {
                        // use local variable to perform fewer volatile reads
                        int pos = position;
                        //
                        int bytesWanted = (int) Math.min(bytesRead, maxRequired(pos));
                        buffer.writerIndex(bytesWanted);
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
            public void failed(Throwable exc, ByteBuf attachment) {
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
                for (;;) {
                    if (cancelled) {
                        return;
                    }
                    if (REQUESTED.get(this) > 0) {
                        boolean emitted = false;
                        // read d before next to avoid race
                        boolean d = done;
                        ByteBuf bb = next;
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
                ByteBuf innerBuf = alloc.buffer(readSize, readSize);
                try {
                    innerBuf.writeBytes(bigByteArray, pos, readSize);
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
