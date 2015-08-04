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
package com.microsoft.azure.storage.blob;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.DoesServiceRequest;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.Base64;
import com.microsoft.azure.storage.core.Logger;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * The class is an append-only stream for writing into storage.
 */
public final class BlobOutputStream extends OutputStream {

    /**
     * Holds the {@link AccessCondition} object that represents the access conditions for the blob.
     */
    private AccessCondition accessCondition;

    /**
     * Used for block blobs, holds the block id prefix.
     */
    private String blockIdPrefix;

    /**
     * Used for block blobs, holds the block list.
     */
    private ArrayList<BlockEntry> blockList;

    /**
     * The CompletionService used to await task completion for this stream.
     */
    private final ExecutorCompletionService<Void> completionService;
    
    /**
     * Holds the futures of the executing tasks. The starting size of the set is a multiple of the concurrent request
     * count to reduce the cost of resizing the set later.
     */
    private final Set<Future<Void>> futureSet;

    /**
     * Holds the write threshold of number of bytes to buffer prior to dispatching a write. For block blob this is the
     * block size, for page blob this is the Page commit size.
     */
    private int internalWriteThreshold = -1;
    
    /**
     * Holds the last exception this stream encountered.
     */
    private volatile IOException lastError = null;

    /**
     * Holds the reference to the MD5 digest for the blob.
     */
    private MessageDigest md5Digest;

    /**
     * Holds the OperationContext for the current stream.
     */
    private final OperationContext opContext;

    /**
     * Holds the options for the current stream.
     */
    private final BlobRequestOptions options;
    

    private long currentBlobOffset;

    /**
     * A private buffer to store data prior to committing to the cloud.
     */
    private volatile ByteArrayOutputStream outBuffer;

    /**
     * Holds the reference to the blob this stream is associated with.
     */
    private final CloudBlob parentBlobRef;
     
    /**
     * Determines if this stream is used against a page blob or block blob.
     */
    private BlobType streamType = BlobType.UNSPECIFIED;

    /**
     * The ExecutorService used to schedule tasks for this stream.
     */
    private final ThreadPoolExecutor threadExecutor;

    /**
     * Initializes a new instance of the BlobOutputStream class.
     * 
     * @param parentBlob
     *            A {@link CloudBlob} object which represents the blob that this stream is associated with.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object which specifies any additional options for the request.
     * @param opContext
     *            An {@link OperationContext} object which is used to track the execution of the operation.
     * 
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    private BlobOutputStream(final CloudBlob parentBlob, final AccessCondition accessCondition,
            final BlobRequestOptions options, final OperationContext opContext) throws StorageException {
        this.accessCondition = accessCondition;
        this.parentBlobRef = parentBlob;
        this.parentBlobRef.assertCorrectBlobType();
        this.options = new BlobRequestOptions(options);
        this.outBuffer = new ByteArrayOutputStream();
        this.opContext = opContext;

        if (this.options.getConcurrentRequestCount() < 1) {
            throw new IllegalArgumentException("ConcurrentRequestCount");
        }
        
        this.futureSet = Collections.newSetFromMap(new ConcurrentHashMap<Future<Void>, Boolean>(
                this.options.getConcurrentRequestCount() == null ? 1 : this.options.getConcurrentRequestCount() * 2));

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
        this.threadExecutor = new ThreadPoolExecutor(
                this.options.getConcurrentRequestCount(),
                this.options.getConcurrentRequestCount(),
                10, 
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        this.completionService = new ExecutorCompletionService<Void>(this.threadExecutor);
    }

    /**
     * Initializes a new instance of the BlobOutputStream class for a CloudBlockBlob
     * 
     * @param parentBlob
     *            A {@link CloudBlockBlob} object which represents the blob that this stream is associated with.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object which specifies any additional options for the request.
     * @param opContext
     *            An {@link OperationContext} object which is used to track the execution of the operation.
     * 
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    protected BlobOutputStream(final CloudBlockBlob parentBlob, final AccessCondition accessCondition,
            final BlobRequestOptions options, final OperationContext opContext) throws StorageException {
        this((CloudBlob) parentBlob, accessCondition, options, opContext);

        this.blockList = new ArrayList<BlockEntry>();
        this.blockIdPrefix = UUID.randomUUID().toString() + "-";
        
        this.streamType = BlobType.BLOCK_BLOB;
        this.internalWriteThreshold = this.parentBlobRef.getStreamWriteSizeInBytes();
    }

    /**
     * Initializes a new instance of the BlobOutputStream class for a CloudPageBlob
     * 
     * @param parentBlob
     *            A {@link CloudPageBlob} object which represents the blob that this stream is associated with.
     * @param length
     *            A <code>long</code> which represents the length of the page blob in bytes, which must be a multiple of
     *            512.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object which specifies any additional options for the request
     * @param opContext
     *            An {@link OperationContext} object which is used to track the execution of the operation
     * 
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    protected BlobOutputStream(final CloudPageBlob parentBlob, final long length,
            final AccessCondition accessCondition, final BlobRequestOptions options, final OperationContext opContext)
            throws StorageException {
        this(parentBlob, accessCondition, options, opContext);
        this.streamType = BlobType.PAGE_BLOB;
        
        this.internalWriteThreshold = (int) Math.min(this.parentBlobRef.getStreamWriteSizeInBytes(), length);
    }
    
    /**
     * Initializes a new instance of the BlobOutputStream class for a CloudAppendBlob
     * 
     * @param parentBlob
     *            A {@link CloudAppendBlob} object which represents the blob that this stream is associated with.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object which specifies any additional options for the request
     * @param opContext
     *            An {@link OperationContext} object which is used to track the execution of the operation
     * 
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    protected BlobOutputStream(final CloudAppendBlob parentBlob, final AccessCondition accessCondition, 
            final BlobRequestOptions options, final OperationContext opContext)
            throws StorageException {
        this((CloudBlob)parentBlob, accessCondition, options, opContext);
        this.streamType = BlobType.APPEND_BLOB;
        
        this.accessCondition = accessCondition != null ? accessCondition : new AccessCondition();
        if (this.accessCondition.getIfAppendPositionEqual() != null) {
            this.currentBlobOffset = this.accessCondition.getIfAppendPositionEqual();
        } 
        else {
            // If this is an existing blob, we've done a downloadProperties to get the length
            // If this is a new blob, getLength will correctly return 0
            this.currentBlobOffset = parentBlob.getProperties().getLength();
        }
        
        this.internalWriteThreshold = this.parentBlobRef.getStreamWriteSizeInBytes();
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
    @DoesServiceRequest
    public synchronized void close() throws IOException {
        try {
            // if the user has already closed the stream, this will throw a STREAM_CLOSED exception
            // if an exception was thrown by any thread in the threadExecutor, realize it now
            this.checkStreamState();

            // flush any remaining data
            this.flush();

            // shut down the ExecutorService.
            this.threadExecutor.shutdown();

            // try to commit the blob
            try {
                this.commit();
            }
            catch (final StorageException e) {
                throw Utility.initIOException(e);
            }
        }
        finally {
            // if close() is called again, an exception will be thrown
            this.lastError = new IOException(SR.STREAM_CLOSED);

            // if an exception was thrown and the executor was not yet closed, call shutDownNow() to cancel all tasks 
            // and shutdown the ExecutorService
            if (!this.threadExecutor.isShutdown()) {
                this.threadExecutor.shutdownNow();
            }
        }
    }

    /**
     * Commits the blob, for block blob this uploads the block list.
     * 
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    private synchronized void commit() throws StorageException {
        if (this.options.getStoreBlobContentMD5()) {
            this.parentBlobRef.getProperties().setContentMD5(Base64.encode(this.md5Digest.digest()));
        }

        if (this.streamType == BlobType.BLOCK_BLOB) {
            // wait for all blocks to finish
            final CloudBlockBlob blobRef = (CloudBlockBlob) this.parentBlobRef;
            blobRef.commitBlockList(this.blockList, this.accessCondition, this.options, this.opContext);
        }
        else if (this.options.getStoreBlobContentMD5()) {
            this.parentBlobRef.uploadProperties(this.accessCondition, this.options, this.opContext);
        }
    }
    
    /**
     * Dispatches a write operation for a given length.
     * 
     * @param writeLength
     *            An <code>int</code> which represents the length of the data to write, this is the write threshold that
     *            triggered the write.
     * 
     * @throws IOException
     *             If an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     */
    @DoesServiceRequest
    private synchronized void dispatchWrite() throws IOException {
        final int writeLength = this.outBuffer.size();
        if (writeLength == 0) {
            return;
        }
        
        if (this.streamType == BlobType.PAGE_BLOB && (writeLength % Constants.PAGE_SIZE != 0)) {
            throw new IOException(String.format(SR.INVALID_NUMBER_OF_BYTES_IN_THE_BUFFER, writeLength));
        }

        Callable<Void> worker = null;

        if (this.threadExecutor.getQueue().size() >= this.options.getConcurrentRequestCount() * 2) {
            this.waitForTaskToComplete();
        } 
        
        if (this.futureSet.size() >= this.options.getConcurrentRequestCount() * 2) {
            this.clearCompletedFutures();
        }

        final ByteArrayInputStream bufferRef = new ByteArrayInputStream(this.outBuffer.toByteArray());

        if (this.streamType == BlobType.BLOCK_BLOB) {
            final String blockID = this.getCurrentBlockId();

            this.blockList.add(new BlockEntry(blockID, BlockSearchMode.LATEST));

            worker = new Callable<Void>() {
                @Override
                public Void call() {
                    BlobOutputStream.this.writeBlock(bufferRef, blockID, writeLength);
                    return null;
                }
            };
        }
        else if (this.streamType == BlobType.PAGE_BLOB) {
            final long opOffset = this.currentBlobOffset;
            this.currentBlobOffset += writeLength;

            worker = new Callable<Void>() {
                @Override
                public Void call() {
                    BlobOutputStream.this.writePages(bufferRef, opOffset, writeLength);
                    return null;
                }
            };
        }
        else if (this.streamType == BlobType.APPEND_BLOB) {
            final long opOffset = this.currentBlobOffset;
            this.currentBlobOffset += writeLength;

            // We cannot differentiate between max size condition failing only in the retry versus failing in the 
            // first attempt and retry even for a single writer scenario. So we will eliminate the latter and handle 
            // the former in the append block method.
            if (this.accessCondition.getIfMaxSizeLessThanOrEqual() != null
                    && this.currentBlobOffset > this.accessCondition.getIfMaxSizeLessThanOrEqual()) {
                this.lastError = new IOException(SR.INVALID_BLOCK_SIZE);
                throw this.lastError;
            }
            
            worker = new Callable<Void>() {
                @Override
                public Void call() {
                    BlobOutputStream.this.appendBlock(bufferRef, opOffset, writeLength);
                    return null;
                }
            };
        }

        // Add future to set
        this.futureSet.add(this.completionService.submit(worker));
        
        // Reset buffer.
        this.outBuffer = new ByteArrayOutputStream();
    }
    
    private void writeBlock(ByteArrayInputStream blockData, String blockId, long writeLength) {
        final CloudBlockBlob blobRef = (CloudBlockBlob) this.parentBlobRef;

        try {
            blobRef.uploadBlock(blockId, blockData, writeLength, this.accessCondition, this.options, this.opContext);
        }
        catch (final IOException e) {
            this.lastError = e;
        }
        catch (final StorageException e) {
            this.lastError = Utility.initIOException(e);
        }
    }

    private void writePages(ByteArrayInputStream pageData, long offset, long writeLength) {
        final CloudPageBlob blobRef = (CloudPageBlob) this.parentBlobRef;

        try {
            blobRef.uploadPages(pageData, offset, writeLength, this.accessCondition, this.options, this.opContext);
        }
        catch (final IOException e) {
            this.lastError = e;
        }
        catch (final StorageException e) {
            this.lastError = Utility.initIOException(e);
        }
    }

    private void appendBlock(ByteArrayInputStream blockData, long offset, long writeLength) {
        final CloudAppendBlob blobRef = (CloudAppendBlob) this.parentBlobRef;
        this.accessCondition.setIfAppendPositionEqual(offset);

        int previousResultsCount = this.opContext.getRequestResults().size();
        try {
            blobRef.appendBlock(blockData, writeLength, this.accessCondition, this.options, this.opContext);
        }
        catch (final IOException e) {
            this.lastError = e;
        }
        catch (final StorageException e) {
            if (this.options.getAbsorbConditionalErrorsOnRetry()
                    && e.getHttpStatusCode() == HttpURLConnection.HTTP_PRECON_FAILED
                    && e.getExtendedErrorInformation() != null
                    && e.getExtendedErrorInformation().getErrorCode() != null
                    && (e.getExtendedErrorInformation().getErrorCode()
                            .equals(StorageErrorCodeStrings.INVALID_APPEND_POSITION) || e.getExtendedErrorInformation()
                            .getErrorCode().equals(StorageErrorCodeStrings.INVALID_MAX_BLOB_SIZE_CONDITION))
                    && (this.opContext.getRequestResults().size() - previousResultsCount > 1)) {

                // Pre-condition failure on a retry should be ignored in a single writer scenario since 
                // the request succeeded in the first attempt.
                Logger.info(this.opContext, SR.PRECONDITION_FAILURE_IGNORED);
            }
            else {
                this.lastError = Utility.initIOException(e);
            }
        }
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out. If any data remains in the
     * buffer it is committed to the service.
     * 
     * @throws IOException
     *             If an I/O error occurs.
     */
    @Override
    @DoesServiceRequest
    public void flush() throws IOException {
        this.checkStreamState();

        this.dispatchWrite();

        // Waits for all submitted tasks to complete
        Set<Future<Void>> requests = new HashSet<Future<Void>>(this.futureSet);
        for (Future<Void> request : requests) {
            // wait for the future to complete
            try {
                request.get();
            }
            catch (Exception e) {
                throw Utility.initIOException(e);
            }

            // If that task threw an error, fail fast
            this.checkStreamState();
        }
    }
   
    /**
     * Generates a new block ID to be used for PutBlock.
     * 
     * @return Base64 encoded block ID
     * @throws IOException
     */
    private String getCurrentBlockId() throws IOException
    {
        String blockIdSuffix = String.format("%06d", this.blockList.size());
        
        byte[] blockIdInBytes;
        try {
            blockIdInBytes = (this.blockIdPrefix + blockIdSuffix).getBytes(Constants.UTF8_CHARSET);
        } catch (UnsupportedEncodingException e) {
            // this should never happen, UTF8 is a default charset
            throw new IOException(e);
        }
        
        return Base64.encode(blockIdInBytes);
    }

    /**
     * Waits for at least one task to complete.
     * 
     * @throws IOException
     *             If an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     */
    private void waitForTaskToComplete() throws IOException {
        boolean completed = false;
        while (this.completionService.poll() != null) {
            completed = true;
        }
        
        if (!completed) {
            try {
                this.completionService.take();
            }
            catch (final InterruptedException e) {
                throw Utility.initIOException(e);
            }
        }
    }
    
    
    /**
     * Removes futures which are done from the future set.
     */
    private void clearCompletedFutures() {
        for (Future<Void> request : this.futureSet) {
            if (request.isDone()) {
                this.futureSet.remove(request);
            }
        }
    }

    /**
     * Writes <code>b.length</code> bytes from the specified byte array to this output stream.
     * <p>
     * If you are using {@link CloudAppendBlob} and are certain of a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag to 
     * <code>true</code> is acceptable for you.
     * 
     * @param data
     *            A <code>byte</code> array which represents the data to write.
     * 
     * @throws IOException
     *             If an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     */
    @Override
    @DoesServiceRequest
    public void write(final byte[] data) throws IOException {
        this.write(data, 0, data.length);
    }

    /**
     * Writes length bytes from the specified byte array starting at offset to this output stream.
     * <p>
     * If you are using {@link CloudAppendBlob} and are certain of a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag to 
     * <code>true</code> is acceptable for you.
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
    @DoesServiceRequest
    public void write(final byte[] data, final int offset, final int length) throws IOException {
        if (offset < 0 || length < 0 || length > data.length - offset) {
            throw new IndexOutOfBoundsException();
        }

        this.writeInternal(data, offset, length);
    }

    /**
     * Writes all data from the InputStream to the Blob.
     * <p>
     * If you are using {@link CloudAppendBlob} and are certain of a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag to 
     * <code>true</code> is acceptable for you.
     * 
     * @param sourceStream
     *            An {@link InputStream} object which species the data to write to the Blob.
     * 
     * @throws IOException
     *             If an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    public void write(final InputStream sourceStream, final long writeLength) throws IOException, StorageException {
        Utility.writeToOutputStream(sourceStream, this, writeLength, false, false, this.opContext, this.options);
    }

    /**
     * Writes the specified byte to this output stream. The general contract for write is that one byte is written to
     * the output stream. The byte to be written is the eight low-order bits of the argument b. The 24 high-order bits
     * of b are ignored.
     * <p>
     * If you are using {@link CloudAppendBlob} and are certain of a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag to 
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
    @DoesServiceRequest
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

    @DoesServiceRequest
    private synchronized void writeInternal(final byte[] data, int offset, int length) throws IOException {
        while (length > 0) {
            this.checkStreamState();
            
            final int availableBufferBytes = this.internalWriteThreshold - this.outBuffer.size();
            final int nextWrite = Math.min(availableBufferBytes, length);

            // If we need to set MD5 then update the digest accordingly
            if (this.options.getStoreBlobContentMD5()) {
                this.md5Digest.update(data, offset, nextWrite);
            }

            this.outBuffer.write(data, offset, nextWrite);
            offset += nextWrite;
            length -= nextWrite;

            if (this.outBuffer.size() == this.internalWriteThreshold) {
                this.dispatchWrite();
            }
        }
    }
}
