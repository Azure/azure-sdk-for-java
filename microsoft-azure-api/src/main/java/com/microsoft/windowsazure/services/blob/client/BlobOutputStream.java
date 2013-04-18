/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.blob.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.microsoft.windowsazure.services.core.storage.AccessCondition;
import com.microsoft.windowsazure.services.core.storage.DoesServiceRequest;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.Base64;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * The class is an append-only stream for writing into storage.
 */
public final class BlobOutputStream extends OutputStream {
    /**
     * Holds the random number generator used to create starting blockIDs.
     */
    private static Random blockSequenceGenerator = new Random();

    /**
     * Holds the reference to the blob this stream is associated with.
     */
    private final CloudBlob parentBlobRef;

    /**
     * Determines if this stream is used against a page blob or block blob.
     */
    private BlobType streamType = BlobType.UNSPECIFIED;

    /**
     * A flag to determine if the stream is faulted, if so the lasterror will be thrown on next operation.
     */
    volatile boolean streamFaulted;

    /**
     * Holds the lock for synchronized access to the last error.
     */
    Object lastErrorLock = new Object();

    /**
     * Holds the last exception this stream encountered.
     */
    IOException lastError;

    /**
     * Holds the OperationContext for the current stream.
     */
    OperationContext opContext;

    /**
     * Holds the options for the current stream.
     */
    BlobRequestOptions options;

    /**
     * Holds the reference to the MD5 digest for the blob.
     */
    private MessageDigest md5Digest;

    /**
     * Used for block blobs, holds the current BlockID Sequence number.
     */
    private long blockIdSequenceNumber = -1;

    /**
     * Used for block blobs, holds the block list.
     */
    private ArrayList<BlockEntry> blockList;

    /**
     * Used for page blobs, holds the currentOffset the stream is writing to.
     */
    private long currentPageOffset;

    /**
     * Used for page blobs, when uploading UseSparsePageBlob is true holds the buffer index of the first non zero byte.
     */
    private long firstNonZeroBufferedByte = -1;

    /**
     * Used for page blobs, when uploading UseSparsePageBlob is true holds the buffer index of the last non zero byte.
     */
    private long lastNonZeroBufferedByte = -1;

    /**
     * A private buffer to store data prior to committing to the cloud.
     */
    private ByteArrayOutputStream outBuffer;

    /**
     * Holds the number of currently buffered bytes.
     */
    private int currentBufferedBytes;

    /**
     * Holds the write threshold of number of bytes to buffer prior to dispatching a write. For block blob this is the
     * block size, for page blob this is the Page commit size.
     */
    private int internalWriteThreshold = -1;

    /**
     * Holds the number of current outstanding requests.
     */
    private volatile int outstandingRequests;

    /**
     * The ExecutorService used to schedule tasks for this stream.
     */
    private final ExecutorService threadExecutor;

    /**
     * The CompletionService used to await task completion for this stream.
     */
    private final CompletionService<Void> completionService;

    /**
     * Holds the {@link AccessCondition} object that represents the access conditions for the blob.
     */
    AccessCondition accessCondition = null;

    /**
     * Initializes a new instance of the BlobWriteStream class.
     * 
     * @param parentBlob
     *            the blob that this stream is associated with.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            An object that specifies any additional options for the request
     * @param opContext
     *            an object used to track the execution of the operation
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     */
    protected BlobOutputStream(final CloudBlob parentBlob, final AccessCondition accessCondition,
            final BlobRequestOptions options, final OperationContext opContext) throws StorageException {
        this.accessCondition = accessCondition;
        this.parentBlobRef = parentBlob;
        this.parentBlobRef.assertCorrectBlobType();
        this.options = new BlobRequestOptions(options);
        this.outBuffer = new ByteArrayOutputStream();
        this.opContext = opContext;
        this.streamFaulted = false;

        if (this.options.getConcurrentRequestCount() < 1) {
            throw new IllegalArgumentException("ConcurrentRequestCount");
        }

        if (this.options.getStoreBlobContentMD5()) {
            try {
                this.md5Digest = MessageDigest.getInstance("MD5");
            }
            catch (final NoSuchAlgorithmException e) {
                // This wont happen, throw fatal.
                throw Utility.generateNewUnexpectedStorageException(e);
            }
        }

        // V2 cachedThreadPool for perf.
        this.threadExecutor = Executors.newFixedThreadPool(this.options.getConcurrentRequestCount());
        this.completionService = new ExecutorCompletionService<Void>(this.threadExecutor);
    }

    /**
     * Initializes a new instance of the BlobWriteStream class for a CloudBlockBlob
     * 
     * @param parentBlob
     *            the blob that this stream is associated with.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            An object that specifies any additional options for the request
     * @param opContext
     *            an object used to track the execution of the operation
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     */
    protected BlobOutputStream(final CloudBlockBlob parentBlob, final AccessCondition accessCondition,
            final BlobRequestOptions options, final OperationContext opContext) throws StorageException {
        this((CloudBlob) parentBlob, accessCondition, options, opContext);
        this.blockIdSequenceNumber = (long) (blockSequenceGenerator.nextInt(Integer.MAX_VALUE))
                + blockSequenceGenerator.nextInt(Integer.MAX_VALUE - 100000);
        this.blockList = new ArrayList<BlockEntry>();

        this.streamType = BlobType.BLOCK_BLOB;
        this.internalWriteThreshold = this.parentBlobRef.blobServiceClient.getWriteBlockSizeInBytes();
    }

    /**
     * Initializes a new instance of the BlobWriteStream class for a CloudPageBlob
     * 
     * @param parentBlob
     *            the blob that this stream is associated with.
     * @param length
     *            the length of the page blob in bytes, must be a multiple of 512.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            An object that specifies any additional options for the request
     * @param opContext
     *            an object used to track the execution of the operation
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    protected BlobOutputStream(final CloudPageBlob parentBlob, final long length,
            final AccessCondition accessCondition, final BlobRequestOptions options, final OperationContext opContext)
            throws StorageException {
        this(parentBlob, accessCondition, options, opContext);
        this.streamType = BlobType.PAGE_BLOB;
        this.internalWriteThreshold = (int) Math.min(
                this.parentBlobRef.blobServiceClient.getPageBlobStreamWriteSizeInBytes(), length);

        if (length % BlobConstants.PAGE_SIZE != 0) {
            throw new IllegalArgumentException("Page blob length must be multiple of 512.");
        }

        if (this.options.getStoreBlobContentMD5()) {
            throw new IllegalArgumentException("Blob Level MD5 is not supported for PageBlob");
        }

        parentBlob.create(length, accessCondition, options, opContext);
    }

    /**
     * Helper function to check if the stream is faulted, if it is it surfaces the exception.
     * 
     * @throws IOException
     *             if an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     */
    private void checkStreamState() throws IOException {
        synchronized (this.lastErrorLock) {
            if (this.streamFaulted) {
                throw this.lastError;
            }
        }
    }

    /**
     * Closes this output stream and releases any system resources associated with this stream. If any data remains in
     * the buffer it is committed to the service.
     */
    @Override
    @DoesServiceRequest
    public void close() throws IOException {
        this.flush();
        this.checkStreamState();
        Exception tempException = null;

        synchronized (this.lastErrorLock) {
            this.streamFaulted = true;
            this.lastError = new IOException("Stream is already closed.");
            tempException = this.lastError;
        }

        while (this.outstandingRequests > 0) {
            this.waitForTaskToComplete();
        }

        this.threadExecutor.shutdown();
        synchronized (this.lastErrorLock) {
            // if one of the workers threw an exception, realize it now.
            if (tempException != this.lastError) {
                throw this.lastError;
            }
        }

        try {
            this.commit();
        }
        catch (final StorageException e) {
            throw Utility.initIOException(e);
        }
    }

    /**
     * Commits the blob, for block blob this uploads the block list.
     * 
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IOException
     *             if an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     */
    @DoesServiceRequest
    private void commit() throws StorageException, IOException {
        if (this.options.getStoreBlobContentMD5()) {
            this.parentBlobRef.getProperties().setContentMD5(Base64.encode(this.md5Digest.digest()));
        }

        if (this.streamType == BlobType.BLOCK_BLOB) {
            // wait for all blocks to finish
            final CloudBlockBlob blobRef = (CloudBlockBlob) this.parentBlobRef;

            blobRef.commitBlockList(this.blockList, this.accessCondition, this.options, this.opContext);
        }
        else if (this.streamType == BlobType.PAGE_BLOB) {
            this.parentBlobRef.uploadProperties(this.accessCondition, this.options, this.opContext);
        }
    }

    /**
     * Dispatches a write operation for a given length.
     * 
     * @param writeLength
     *            the length of the data to write, this is the write threshold that triggered the write.
     * @throws IOException
     *             if an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     */
    @DoesServiceRequest
    private synchronized void dispatchWrite(final int writeLength) throws IOException {
        if (writeLength == 0) {
            return;
        }

        Callable<Void> worker = null;

        if (this.outstandingRequests > this.options.getConcurrentRequestCount() * 2) {
            this.waitForTaskToComplete();
        }

        final ByteArrayInputStream bufferRef = new ByteArrayInputStream(this.outBuffer.toByteArray());

        if (this.streamType == BlobType.BLOCK_BLOB) {
            final CloudBlockBlob blobRef = (CloudBlockBlob) this.parentBlobRef;
            final String blockID = Base64.encode(Utility.getBytesFromLong(this.blockIdSequenceNumber++));
            this.blockList.add(new BlockEntry(blockID, BlockSearchMode.LATEST));

            worker = new Callable<Void>() {
                @Override
                public Void call() {
                    try {
                        blobRef.uploadBlock(blockID, bufferRef, writeLength, BlobOutputStream.this.accessCondition,
                                BlobOutputStream.this.options, BlobOutputStream.this.opContext);
                    }
                    catch (final IOException e) {
                        synchronized (BlobOutputStream.this.lastErrorLock) {
                            BlobOutputStream.this.streamFaulted = true;
                            BlobOutputStream.this.lastError = e;
                        }
                    }
                    catch (final StorageException e) {
                        synchronized (BlobOutputStream.this.lastErrorLock) {
                            BlobOutputStream.this.streamFaulted = true;
                            BlobOutputStream.this.lastError = Utility.initIOException(e);
                        }
                    }
                    return null;
                }
            };
        }
        else if (this.streamType == BlobType.PAGE_BLOB) {
            final CloudPageBlob blobRef = (CloudPageBlob) this.parentBlobRef;
            long tempOffset = this.currentPageOffset;
            long tempLength = writeLength;

            if (this.options.getUseSparsePageBlob()) {
                if (this.lastNonZeroBufferedByte == -1) {
                    // All zero page range, reset buffer and return
                    this.firstNonZeroBufferedByte = -1;
                    this.lastNonZeroBufferedByte = -1;
                    this.currentBufferedBytes = 0;
                    this.currentPageOffset += writeLength;
                    this.outBuffer = new ByteArrayOutputStream();
                    return;
                }

                // Offset is currentOffset - extra data to page align write
                final long bufferOffset = this.firstNonZeroBufferedByte - this.firstNonZeroBufferedByte
                        % BlobConstants.PAGE_SIZE;

                tempOffset = this.currentPageOffset + bufferOffset;

                // Find end of last full page - to do this Calculate the end of
                // last full page of non zero
                // data and subtract the bufferStarting offset calculated above
                tempLength = (this.lastNonZeroBufferedByte - bufferOffset)
                        + (BlobConstants.PAGE_SIZE - (this.lastNonZeroBufferedByte % BlobConstants.PAGE_SIZE));

                // Reset buffer markers.
                this.firstNonZeroBufferedByte = -1;
                this.lastNonZeroBufferedByte = -1;

                // Fast forward buffer past zero data if applicable
                if (bufferOffset > 0) {
                    if (bufferOffset != bufferRef.skip(bufferOffset)) {
                        // Since this buffer is a byte array buffer this should
                        // always skip the correct number
                        // of bytes.
                        throw Utility.initIOException(Utility.generateNewUnexpectedStorageException(null));
                    }
                }
            }

            final long opWriteLength = tempLength;
            final long opOffset = tempOffset;
            this.currentPageOffset += writeLength;

            worker = new Callable<Void>() {
                @Override
                public Void call() {
                    try {
                        blobRef.uploadPages(bufferRef, opOffset, opWriteLength, BlobOutputStream.this.accessCondition,
                                BlobOutputStream.this.options, BlobOutputStream.this.opContext);
                    }
                    catch (final IOException e) {
                        synchronized (BlobOutputStream.this.lastErrorLock) {
                            BlobOutputStream.this.streamFaulted = true;
                            BlobOutputStream.this.lastError = e;
                        }
                    }
                    catch (final StorageException e) {
                        synchronized (BlobOutputStream.this.lastErrorLock) {
                            BlobOutputStream.this.streamFaulted = true;
                            BlobOutputStream.this.lastError = Utility.initIOException(e);
                        }
                    }
                    return null;
                }
            };
        }

        // Do work and rest buffer.
        this.completionService.submit(worker);
        this.outstandingRequests++;
        this.currentBufferedBytes = 0;
        this.outBuffer = new ByteArrayOutputStream();
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out. If any data remains in the
     * buffer it is committed to the service.
     */
    @Override
    @DoesServiceRequest
    public synchronized void flush() throws IOException {
        this.checkStreamState();

        if (this.streamType == BlobType.PAGE_BLOB && this.currentBufferedBytes > 0
                && (this.currentBufferedBytes % BlobConstants.PAGE_SIZE != 0)) {
            throw new IOException(String.format(
                    "Page data must be a multiple of 512 bytes, buffer currently contains %d bytes.",
                    this.currentBufferedBytes));

            // Non 512 byte remainder, uncomment to pad with bytes and commit.
            /*
             * byte[] nullBuff = new byte[BlobConstants.PageSize - this.currentBufferedBytes % BlobConstants.PageSize];
             * this.write(nullBuff);
             */
        }

        this.dispatchWrite(this.currentBufferedBytes);
    }

    /**
     * Waits for one task to complete
     * 
     * @throws IOException
     *             if an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     */
    private void waitForTaskToComplete() throws IOException {
        try {
            final Future<Void> future = this.completionService.take();
            future.get();
        }
        catch (final InterruptedException e) {
            throw Utility.initIOException(e);
        }
        catch (final ExecutionException e) {
            throw Utility.initIOException(e);
        }

        this.outstandingRequests--;
    }

    /**
     * Writes b.length bytes from the specified byte array to this output stream.
     * 
     * @param data
     *            the byte array to write.
     * 
     * @throws IOException
     *             if an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     */
    @Override
    @DoesServiceRequest
    public void write(final byte[] data) throws IOException {
        this.write(data, 0, data.length);
    }

    /**
     * Writes length bytes from the specified byte array starting at offset to this output stream.
     * 
     * @param data
     *            the byte array to write.
     * @param offset
     *            the start offset in the data.
     * @param length
     *            the number of bytes to write.
     * @throws IOException
     *             if an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     */
    @Override
    @DoesServiceRequest
    public void write(final byte[] data, final int offset, final int length) throws IOException {
        if (offset < 0 || length < 0 || length > data.length - offset) {
            throw new IndexOutOfBoundsException();
        }

        this.writeInternal(data, offset, length);
    }

    /**
     * Writes all data from the InputStream to the Blob.
     * 
     * @param sourceStream
     *            the InputStram to consume.
     * @throws IOException
     *             if an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     * @throws StorageException
     */
    @DoesServiceRequest
    public void write(final InputStream sourceStream, final long writeLength) throws IOException, StorageException {
        Utility.writeToOutputStream(sourceStream, this, writeLength, false, false, null, this.opContext);
    }

    /**
     * Writes the specified byte to this output stream. The general contract for write is that one byte is written to
     * the output stream. The byte to be written is the eight low-order bits of the argument b. The 24 high-order bits
     * of b are ignored.
     * 
     * @param byteVal
     *            the byteValue to write.
     * @throws IOException
     *             if an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
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
     *            the byte array to write.
     * @param offset
     *            the start offset in the data.
     * @param length
     *            the number of bytes to write.
     * @throws IOException
     *             if an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     */
    @DoesServiceRequest
    private synchronized void writeInternal(final byte[] data, int offset, int length) throws IOException {
        while (length > 0) {
            this.checkStreamState();
            final int availableBufferBytes = this.internalWriteThreshold - this.currentBufferedBytes;
            final int nextWrite = Math.min(availableBufferBytes, length);

            // If we need to set MD5 then update the digest accordingly
            if (this.options.getStoreBlobContentMD5()) {
                this.md5Digest.update(data, offset, nextWrite);
            }

            // If Page blob and UseSparsePageBlob is true, then track first and
            // last non zero bytes.
            if (this.options.getUseSparsePageBlob()) {
                for (int m = 0; m < nextWrite; m++) {
                    if (data[m + offset] != 0) {
                        if (this.firstNonZeroBufferedByte == -1) {
                            this.firstNonZeroBufferedByte = this.currentBufferedBytes + m;
                        }

                        this.lastNonZeroBufferedByte = this.currentBufferedBytes + m;
                    }
                }
            }

            this.outBuffer.write(data, offset, nextWrite);
            this.currentBufferedBytes += nextWrite;
            offset += nextWrite;
            length -= nextWrite;

            if (this.currentBufferedBytes == this.internalWriteThreshold) {
                this.dispatchWrite(this.internalWriteThreshold);
            }
        }
    }
}
