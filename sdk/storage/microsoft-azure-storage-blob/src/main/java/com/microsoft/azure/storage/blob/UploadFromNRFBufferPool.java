// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import io.reactivex.Flowable;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This type is to support the implementation of uploadFromNonReplaybleFlowable only. It is mandatory that the caller
 * has broken the source into ByteBuffers that are no greater than the size of a chunk and therefore a buffer in the
 * pool. This is necessary because it upper bounds the number of buffers we need for a given call to write() to 2. If
 * the size of ByteBuffer passed into write() were unbounded, the pool could stall as it would run out of buffers before
 * it is able to return a result, and if it is unable to return, no data can be uploaded and therefore no pools
 * returned.
 *
 * It is incumbent upon the caller to return the buffers after an upload is completed. It is also the caller's
 * responsibility to signal to the pool when the stream is empty and call flush to return any data still sitting in
 * the pool.
 *
 * Broadly, the workflow of this operation is to chunk the source into reasonable sized pieces. On each piece, one
 * thread will call write on the pool. The pool will grab a buffer from the queue to write to, possibly waiting for one
 * to be available, and either store the incomplete buffer to be filled on the next write or return the buffer to be
 * sent. Filled buffers can be uploaded in parallel and should return buffers to the pool after the upload completes.
 * Once the source terminates, it should call flush.
 */
final class UploadFromNRFBufferPool {

    private final BlockingQueue<ByteBuffer> buffers;

    private final int maxBuffs;

    private int numBuffs = 0;

    private final int buffSize;

    private ByteBuffer currentBuf;

    UploadFromNRFBufferPool(final int numBuffs, final int buffSize) {
        /*
        We require at least two buffers because it is possible that a given write will spill over into a second buffer.
        We only need one overflow buffer because the max size of a ByteBuffer is assumed to be the size as a buffer in
        the pool.
         */
        Utility.assertInBounds("numBuffs", numBuffs, 2, Integer.MAX_VALUE);
        this.maxBuffs = numBuffs;
        buffers = new LinkedBlockingQueue<>(numBuffs);


        //These buffers will be used in calls to stageBlock, so they must be no greater than block size.
        Utility.assertInBounds("buffSize", buffSize, 1, BlockBlobURL.MAX_STAGE_BLOCK_BYTES);
        this.buffSize = buffSize;

        //We prep the queue with two buffers in case there is overflow.
        buffers.add(ByteBuffer.allocate(this.buffSize));
        buffers.add(ByteBuffer.allocate(this.buffSize));
        this.numBuffs = 2;
    }

    public Flowable<ByteBuffer> write(ByteBuffer buf) {
        // Check if there's a buffer holding any data from a previous call to write. If not, get a new one.
        if (this.currentBuf == null) {
            this.currentBuf = this.getBuffer();
        }

        Flowable<ByteBuffer> result;
        // We can fit this whole write in the buffer we currently have.
        if (this.currentBuf.remaining() >= buf.remaining()) {
            this.currentBuf.put(buf);
            if (this.currentBuf.remaining() == 0) {
                // Reset the position so that we can read the whole thing then return this buffer.
                this.currentBuf.position(0);
                result = Flowable.just(this.currentBuf);
                // This will force us to get a new buffer next time we try to write.
                this.currentBuf = null;
            } else {
                /*
                We are still filling the current buffer, so we have no data to return. We will return the buffer once it
                is filled
                 */
                result = Flowable.empty();
            }
        } else {
            // We will overflow the current buffer and require another one.
            // Adjust the window of buf so that we fill up currentBuf without going out of bounds.
            int oldLimit = buf.limit();
            buf.limit(buf.position() + this.currentBuf.remaining());
            this.currentBuf.put(buf);
            // Set the old limit so we can read to the end in the next buffer.
            buf.limit(oldLimit);

            // Reset the position so we can read the buffer.
            this.currentBuf.position(0);
            result =  Flowable.just(this.currentBuf);

            /*
            Get a new buffer and fill it with whatever is left from buf. Note that this relies on the assumption that
            the source Flowable has been split up into buffers that are no bigger than chunk size. This assumption
            means we'll only have to over flow once, and the buffer we overflow into will not be filled. This is the
            buffer we will write to on the next call to write().
             */
            this.currentBuf = this.getBuffer();
            this.currentBuf.put(buf);
        }
        return result;
    }

    private ByteBuffer getBuffer() {
        ByteBuffer result;
        // There are no buffers in the queue and we have space to allocate one.
        if (this.buffers.isEmpty() && this.numBuffs < this.maxBuffs) {
            result = ByteBuffer.allocate(this.buffSize);
            this.numBuffs++;
        } else {
            try {
                // If empty, this will wait for an upload to finish and return a buffer.
                result = this.buffers.take();

            } catch (InterruptedException e) {
                throw new IllegalStateException("UploadFromStream thread interrupted." + " Thread:"
                        + Thread.currentThread().getId());
            }
        }
        return result;
    }

    Flowable<ByteBuffer> flush() {
        /*
        Prep and return any data left in the pool. It is important to set the limit so that we don't read beyond the
        actual data as this buffer may have been used before and therefore may have some garbage at the end.
         */
        if (this.currentBuf != null) {
            this.currentBuf.flip();
            ByteBuffer last = this.currentBuf;
            // If there is an accidental duplicate call to flush, this prevents sending the last buffer twice
            this.currentBuf = null;
            return Flowable.just(last);
        }
        return Flowable.empty();
    }

    void returnBuffer(ByteBuffer b) {
        // Reset the buffer.
        b.position(0);
        b.limit(b.capacity());

        try {
            this.buffers.put(b);
        } catch (InterruptedException e) {
            throw new IllegalStateException("UploadFromStream thread interrupted.");
        }
    }
}
