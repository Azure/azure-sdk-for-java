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
package com.microsoft.azure.storage.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.DoesServiceRequest;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.Base64;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * The class is an append-only stream for writing into storage.
 */
public class FileOutputStream extends OutputStream {

    /**
     * Holds the reference to the file this stream is associated with.
     */
    private final CloudFile parentFileRef;

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
    FileRequestOptions options;

    /**
     * Holds the reference to the MD5 digest for the file.
     */
    private MessageDigest md5Digest;

    /**
     * Holds the currentOffset the stream is writing to.
     */
    private long currentOffset;

    /**
     * A private buffer to store data prior to committing to the cloud.
     */
    private ByteArrayOutputStream outBuffer;

    /**
     * Holds the number of currently buffered bytes.
     */
    private int currentBufferedBytes;

    /**
     * Holds the write threshold of number of bytes to buffer prior to dispatching a write.
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
    private final ExecutorCompletionService<Void> completionService;

    /**
     * Holds the {@link AccessCondition} object that represents the access conditions for the file.
     */
    AccessCondition accessCondition = null;

    /**
     * Initializes a new instance of the FileOutputStream class.
     * 
     * @param parentFile
     *            A {@link CloudFile} object which represents the file that this stream is associated with.
     * @param length
     *            A <code>long</code> which represents the length of the file in bytes.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object which specifies any additional options for the request.
     * @param opContext
     *            An {@link OperationContext} object which is used to track the execution of the operation
     * 
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    protected FileOutputStream(final CloudFile parentFile, final long length, final AccessCondition accessCondition,
            final FileRequestOptions options, final OperationContext opContext) throws StorageException {
        this.accessCondition = accessCondition;
        this.parentFileRef = parentFile;
        this.options = new FileRequestOptions(options);
        this.outBuffer = new ByteArrayOutputStream();
        this.opContext = opContext;
        this.streamFaulted = false;

        if (this.options.getConcurrentRequestCount() < 1) {
            throw new IllegalArgumentException("ConcurrentRequestCount");
        }

        if (this.options.getStoreFileContentMD5()) {
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
        this.internalWriteThreshold = (int) Math.min(this.parentFileRef.getStreamWriteSizeInBytes(), length);
    }

    /**
     * Helper function to check if the stream is faulted, if it is it surfaces the exception.
     * 
     * @throws IOException
     *             If an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
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
     * 
     * @throws IOException
     *             If an I/O error occurs.
     */
    @Override
    @DoesServiceRequest
    public void close() throws IOException {
        try {
            // if the user has already closed the stream, this will throw a STREAM_CLOSED exception
            // if an exception was thrown by any thread in the threadExecutor, realize it now
            this.checkStreamState();

            // flush any remaining data
            this.flush();

            // shut down the ExecutorService.
            this.threadExecutor.shutdown();

            // try to commit the file
            try {
                this.commit();
            }
            catch (final StorageException e) {
                throw Utility.initIOException(e);
            }
        }
        finally {
            // if close() is called again, an exception will be thrown
            synchronized (this.lastErrorLock) {
                this.streamFaulted = true;
                this.lastError = new IOException(SR.STREAM_CLOSED);
            }

            // if an exception was thrown and the executor was not yet closed, call shutDownNow() to cancel all tasks 
            // and shutdown the ExecutorService
            if (!this.threadExecutor.isShutdown()) {
                this.threadExecutor.shutdownNow();
            }
        }
    }

    /**
     * Commits the file.
     * 
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    private void commit() throws StorageException {
        if (this.options.getStoreFileContentMD5()) {
            this.parentFileRef.getProperties().setContentMD5(Base64.encode(this.md5Digest.digest()));
        }

        this.parentFileRef.uploadProperties(this.accessCondition, this.options, this.opContext);
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
    private synchronized void dispatchWrite(final int writeLength) throws IOException {
        if (writeLength == 0) {
            return;
        }

        Callable<Void> worker = null;

        if (this.outstandingRequests > this.options.getConcurrentRequestCount() * 2) {
            this.waitForTaskToComplete();
        }

        final ByteArrayInputStream bufferRef = new ByteArrayInputStream(this.outBuffer.toByteArray());
        final CloudFile fileRef = this.parentFileRef;
        long tempOffset = this.currentOffset;
        long tempLength = writeLength;

        final long opWriteLength = tempLength;
        final long opOffset = tempOffset;
        this.currentOffset += writeLength;

        worker = new Callable<Void>() {
            @Override
            public Void call() {
                try {
                    fileRef.uploadRange(bufferRef, opOffset, opWriteLength, FileOutputStream.this.accessCondition,
                            FileOutputStream.this.options, FileOutputStream.this.opContext);
                }
                catch (final IOException e) {
                    synchronized (FileOutputStream.this.lastErrorLock) {
                        FileOutputStream.this.streamFaulted = true;
                        FileOutputStream.this.lastError = e;
                    }
                }
                catch (final StorageException e) {
                    synchronized (FileOutputStream.this.lastErrorLock) {
                        FileOutputStream.this.streamFaulted = true;
                        FileOutputStream.this.lastError = Utility.initIOException(e);
                    }
                }
                return null;
            }
        };

        // Do work and reset buffer.
        this.completionService.submit(worker);
        this.outstandingRequests++;
        this.currentBufferedBytes = 0;
        this.outBuffer = new ByteArrayOutputStream();
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
    public synchronized void flush() throws IOException {
        this.checkStreamState();
        
        // Dispatch a write for the current bytes in the buffer
        this.dispatchWrite(this.currentBufferedBytes);
        
        // Waits for all submitted tasks to complete
        while (this.outstandingRequests > 0) {
            // Wait for a task to complete
            this.waitForTaskToComplete();
            
            // If that task threw an error, fail fast
            this.checkStreamState();
        }
    }

    /**
     * Waits for one task to complete
     * 
     * @throws IOException
     *             If an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
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
     * Writes <code>b.length</code> bytes from the specified byte array to this output stream.
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
     * Writes all data from the InputStream to the File.
     * 
     * @param sourceStream
     *            An {@link InputStream} object which species the data to write to the File.
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

            final int availableBufferBytes = this.internalWriteThreshold - this.currentBufferedBytes;
            final int nextWrite = Math.min(availableBufferBytes, length);

            // If we need to set MD5 then update the digest accordingly
            if (this.options.getStoreFileContentMD5()) {
                this.md5Digest.update(data, offset, nextWrite);
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
