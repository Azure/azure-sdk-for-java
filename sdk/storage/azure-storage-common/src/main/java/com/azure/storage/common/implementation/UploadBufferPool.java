// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This type is to support the implementation of buffered upload only. It is mandatory that the caller has broken the
 * source into ByteBuffers that are no greater than the size of a chunk and therefore a buffer in the pool. This is
 * necessary because it upper bounds the number of buffers we need for a given call to write() to 2. If the size of
 * ByteBuffer passed into write() were unbounded, the pool could stall as it would run out of buffers before it is able
 * to return a result, and if it is unable to return, no data can be uploaded and therefore no pools returned.
 *
 * It is incumbent upon the caller to return the buffers after an upload is completed. It is also the caller's
 * responsibility to signal to the pool when the stream is empty and call flush to return any data still sitting in the
 * pool.
 *
 * Broadly, the workflow of this operation is to chunk the source into reasonable sized pieces. On each piece, one
 * thread will call write on the pool. The pool will grab a buffer from the queue to write to, possibly waiting for one
 * to be available, and either store the incomplete buffer to be filled on the next write or return the filled buffer to
 * be sent. Filled buffers can be uploaded in parallel and should return buffers to the pool after the upload completes.
 * Once the source terminates, it should call flush.
 *
 * {@link BufferAggregator} is used to store {@link ByteBuffer}s in order to handle situations when chunk size
 * exceeds single {@link ByteBuffer} capacity.
 *
 * RESERVED FOR INTERNAL USE ONLY
 */
public final class UploadBufferPool {
    private final ClientLogger logger = new ClientLogger(UploadBufferPool.class);

    /*
    Note that a blocking on a synchronized object is not the same as blocking on a reactive operation; blocking on this
    queue will not compromise the async nature of this workflow. Fluxes themselves are internally synchronized to ensure
    only one call to onNext happens at a time.
     */
    private final BlockingQueue<BufferAggregator> buffers;

    private final int maxBuffs;

    // The number of buffs we have allocated. We can query the queue for how many are available.
    private int numBuffs;

    private final long buffSize;

    private BufferAggregator currentBuf;

    /**
     * Creates a new instance of UploadBufferPool
     * @param numBuffs The number of buffers in the buffer pool.
     * @param buffSize The size of the buffers
     */
    public UploadBufferPool(final int numBuffs, final long buffSize, long maxBuffSize) {
        /*
        We require at least two buffers because it is possible that a given write will spill over into a second buffer.
        We only need one overflow buffer because the max size of a ByteBuffer is assumed to be the size as a buffer in
        the pool.
         */
        StorageImplUtils.assertInBounds("numBuffs", numBuffs, 2, Integer.MAX_VALUE);
        this.maxBuffs = numBuffs;
        buffers = new LinkedBlockingQueue<>(numBuffs);


        // These buffers will be used in calls to stageBlock, so they must be no greater than block size.
        StorageImplUtils.assertInBounds("buffSize", buffSize, 1, maxBuffSize);
        this.buffSize = buffSize;

        // We prep the queue with two buffers in case there is overflow.
        buffers.add(new BufferAggregator(this.buffSize));
        buffers.add(new BufferAggregator(this.buffSize));
        this.numBuffs = 2;
    }

    /*
    Note that the upload method will be calling write sequentially as there is only one worker reading from the source
    and calling write. This means operations like currentBuf.remaining() will not result in race conditions.
     */

    /**
     * Writes ByteBuffers to a {@code Flux<BufferAggregator>}
     * @param buf The buffer to write
     * @return The {@code Flux<BufferAggregator>}
     */
    public Flux<BufferAggregator> write(ByteBuffer buf) {
        // Check if there's a buffer holding any data from a previous call to write. If not, get a new one.
        if (this.currentBuf == null) {
            this.currentBuf = this.getBuffer();
        }

        Flux<BufferAggregator> result;
        // We can fit this whole write in the buffer we currently have.
        if (this.currentBuf.remainingCapacity() >= buf.remaining()) {
            this.currentBuf.append(buf);
            if (this.currentBuf.remainingCapacity() == 0) {
                result = Flux.just(this.currentBuf);
                // This will force us to get a new buffer next time we try to write.
                this.currentBuf = null;
            } else {
                /*
                We are still filling the current buffer, so we have no data to return. We will return the buffer once it
                is filled
                 */
                result = Flux.empty();
            }
        } else {
            // We will overflow the current buffer and require another one.
            // Duplicate and adjust the window of buf so that we fill up currentBuf without going out of bounds.
            ByteBuffer duplicate = buf.duplicate();
            duplicate.limit(buf.position() + (int) this.currentBuf.remainingCapacity());
            this.currentBuf.append(duplicate);
            // Adjust the window of original buffer to represent remaining part.
            buf.position(buf.position() + (int) this.currentBuf.remainingCapacity());

            result = Flux.just(this.currentBuf);

            /*
            Get a new buffer and fill it with whatever is left from buf. Note that this relies on the assumption that
            the source Flux has been split up into buffers that are no bigger than chunk size. This assumption
            means we'll only have to over flow once, and the buffer we overflow into will not be filled. This is the
            buffer we will write to on the next call to write().
             */
            this.currentBuf = this.getBuffer();
            this.currentBuf.append(buf);
        }
        return result;
    }

    /*
    Note that the upload method will be calling write sequentially as there is only one worker reading from the source
    and calling write. Hence there is only one worker calling getBuffer at any time.
     */
    private BufferAggregator getBuffer() {
        BufferAggregator result;
        /*
         There are no buffers in the queue and we have space to allocate one. We do not add the new buffer to the queue
         because we want to make immediate use of it. This is effectively equivalent to a buffers.add(newBuffer) and
         then result = buffers.pop()--because we only get here when the queue is empty, the buffer returned is the one
         we just created. The new buffer will be added to buffers when it is returned to the pool.
         */
        if (this.buffers.isEmpty() && this.numBuffs < this.maxBuffs) {
            result = new BufferAggregator(this.buffSize);
            this.numBuffs++;
        } else {
            try {
                // If empty, this will wait for an upload to finish and return a buffer.
                result = this.buffers.take();

            } catch (InterruptedException e) {
                throw logger.logExceptionAsError(new IllegalStateException("BufferedUpload thread interrupted. Thread:"
                    + Thread.currentThread().getId()));
            }
        }
        return result;
    }

    /**
     * Flushes the current buffer
     * @return the flushed buffer
     */
    public Flux<BufferAggregator> flush() {
        /*
        Prep and return any data left in the pool. It is important to set the limit so that we don't read beyond the
        actual data as this buffer may have been used before and therefore may have some garbage at the end.
         */
        if (this.currentBuf != null) {
            BufferAggregator last = this.currentBuf;
            // If there is an accidental duplicate call to flush, this prevents sending the last buffer twice
            this.currentBuf = null;
            return Flux.just(last);
        }
        return Flux.empty();
    }

    /**
     * Returns the ByteBuffer
     * @param b The ByteBuffer to reset and return
     */
    public void returnBuffer(BufferAggregator b) {
        // Reset the buffer aggregator.
        b.reset();

        try {
            this.buffers.put(new BufferAggregator(this.buffSize));
        } catch (InterruptedException e) {
            throw logger.logExceptionAsError(new IllegalStateException("UploadFromStream thread interrupted."));
        }
    }
}
