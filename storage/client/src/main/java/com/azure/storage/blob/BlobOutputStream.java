// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.PageRange;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;


public class BlobOutputStream extends OutputStream {
    /**
     * Holds the {@link BlobAccessConditions} object that represents the access conditions for the blob.
     */
    private BlobAccessConditions accessCondition;

    /**
     * Used for block blobs, holds the block id prefix.
     */
    private String blockIdPrefix;

    /**
     * Used for block blobs, holds the block list.
     */
    private ArrayList<String> blockList;

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
     * Holds the Context for the current stream.
     */
    private final Context opContext;


    private long currentBlobOffset;

    /**
     * A private buffer to store data prior to committing to the cloud.
     */
    private volatile ByteArrayOutputStream outBuffer;

    /**
     * Holds the reference to the blob this stream is associated with.
     */
    private final BlobAsyncClient blobClient;

    /**
     * Determines if this stream is used against a page blob or block blob.
     */
    private BlobType streamType = BlobType.BLOCK_BLOB;

    private List<Mono<Void>> completables = new ArrayList<>();

    /**
     * Initializes a new instance of the BlobOutputStream class.
     *
     * @param parentBlob
     *            A {@link BlobAsyncClient} object which represents the blob that this stream is associated with.
     * @param accessCondition
     *            An {@link BlobAccessConditions} object which represents the access conditions for the blob.
     * @param opContext
     *            An {@link Context} object which is used to track the execution of the operation.
     *
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    private BlobOutputStream(final BlobAsyncClient parentBlob, final BlobAccessConditions accessCondition, final Context opContext) throws StorageException {
        this.accessCondition = accessCondition;
        this.blobClient = parentBlob;
        this.outBuffer = new ByteArrayOutputStream();
        this.opContext = opContext;

        this.futureSet = new HashSet<>();
    }

    /**
     * Initializes a new instance of the BlobOutputStream class for a CloudBlockBlob
     *
     * @param parentBlob
     *            A {@link BlobAsyncClient} object which represents the blob that this stream is associated with.
     * @param accessCondition
     *            An {@link BlobAccessConditions} object which represents the access conditions for the blob.
     * @param opContext
     *            An {@link Context} object which is used to track the execution of the operation.
     *
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    BlobOutputStream(final BlockBlobAsyncClient parentBlob, final BlobAccessConditions accessCondition, final Context opContext) throws StorageException {
        this((BlobAsyncClient) parentBlob, accessCondition, opContext);

        this.blockList = new ArrayList<String>();
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
     * @param opContext
     *            An {@link Context} object which is used to track the execution of the operation
     *
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    BlobOutputStream(final PageBlobAsyncClient parentBlob, final long length, final BlobAccessConditions accessCondition, final Context opContext)
        throws StorageException {
        this((BlobAsyncClient) parentBlob, accessCondition, opContext);
        this.streamType = BlobType.PAGE_BLOB;

        this.internalWriteThreshold = (int) Math.min(BlockBlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE, length);
    }

    /**
     * Initializes a new instance of the BlobOutputStream class for a CloudAppendBlob
     *
     * @param parentBlob
     *            A {@link AppendBlobAsyncClient} object which represents the blob that this stream is associated with.
     * @param accessCondition
     *            An {@link BlobAccessConditions} object which represents the access conditions for the blob.
     * @param opContext
     *            An {@link Context} object which is used to track the execution of the operation
     *
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    BlobOutputStream(final AppendBlobAsyncClient parentBlob, final BlobAccessConditions accessCondition, final Context opContext)
        throws StorageException {
        this((BlobAsyncClient) parentBlob, accessCondition, opContext);
        this.streamType = BlobType.APPEND_BLOB;

        this.accessCondition = accessCondition != null ? accessCondition : new BlobAccessConditions();
//        if (accessCondition.getIfAppendPositionEqual() != null) {
//            this.currentBlobOffset = this.accessCondition.getIfAppendPositionEqual();
//        }
//        else {
//            // If this is an existing blob, we've done a downloadProperties to get the length
//            // If this is a new blob, getLength will correctly return 0
//            this.currentBlobOffset = parentBlob.getProperties().getLength();
//        }

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
            }
            catch (final StorageException e) {
                throw new IOException(e);
            }
        }
        finally {
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
            final BlockBlobAsyncClient blobRef = (BlockBlobAsyncClient) this.blobClient;
            blobRef.commitBlockList(this.blockList, null, null, this.accessCondition).block();
        }
//        else if (this.options.getStoreBlobContentMD5()) {
//            this.blobClient.uploadProperties(this.accessCondition, this.options, this.opContext);
//        }
    }

    /**
     * Dispatches a write operation for a given length.
     *
     * @throws IOException
     *             If an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     */
    private synchronized void dispatchWrite() throws IOException {
        final int writeLength = this.outBuffer.size();
        if (writeLength == 0) {
            return;
        }

        if (this.streamType == BlobType.PAGE_BLOB && (writeLength % Constants.PAGE_SIZE != 0)) {
            throw new IOException(String.format(SR.INVALID_NUMBER_OF_BYTES_IN_THE_BUFFER, writeLength));
        }

        Mono<Void> worker = null;

        this.clearCompletedFutures();

        final Flux<ByteBuf> bufferRef = ByteBufFlux.fromInbound(Mono.just(outBuffer.toByteArray()));

        if (this.streamType == BlobType.BLOCK_BLOB) {
            final String blockID = this.getCurrentBlockId();

            this.blockList.add(blockID);

            worker = BlobOutputStream.this.writeBlock(bufferRef, blockID, writeLength);
        }
        else if (this.streamType == BlobType.PAGE_BLOB) {
            final long opOffset = this.currentBlobOffset;
            this.currentBlobOffset += writeLength;

            worker = BlobOutputStream.this.writePages(bufferRef, opOffset, writeLength);
        }
        else if (this.streamType == BlobType.APPEND_BLOB) {
            final long opOffset = this.currentBlobOffset;
            this.currentBlobOffset += writeLength;

            // We cannot differentiate between max size condition failing only in the retry versus failing in the
            // first attempt and retry even for a single writer scenario. So we will eliminate the latter and handle
            // the former in the append block method.
//            if (this.accessCondition.getIfMaxSizeLessThanOrEqual() != null
//                && this.currentBlobOffset > this.accessCondition.getIfMaxSizeLessThanOrEqual()) {
//                this.lastError = new IOException(SR.INVALID_BLOCK_SIZE);
//                throw this.lastError;
//            }

            worker = BlobOutputStream.this.appendBlock(bufferRef, opOffset, writeLength);
        }

        // Add future to set
        this.futureSet.add(worker.toFuture());

        // Reset buffer.
        this.outBuffer = new ByteArrayOutputStream();
    }

    private Mono<Void> writeBlock(Flux<ByteBuf> blockData, String blockId, long writeLength) {
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
        final PageBlobAsyncClient blobRef = (PageBlobAsyncClient) this.blobClient;

        PageBlobAccessConditions pageBlobAccessConditions = accessCondition == null ? null : new PageBlobAccessConditions().leaseAccessConditions(accessCondition.leaseAccessConditions()).modifiedAccessConditions(accessCondition.modifiedAccessConditions());

        return blobRef.uploadPages(new PageRange().start(offset).end(writeLength), pageData, pageBlobAccessConditions)
            .then()
            .onErrorResume(t -> t instanceof StorageException, e -> {
                this.lastError = new IOException(e);
                return null;
            });
    }

    private Mono<Void> appendBlock(Flux<ByteBuf> blockData, long offset, long writeLength) {
        final AppendBlobAsyncClient blobRef = (AppendBlobAsyncClient) this.blobClient;
//        this.accessCondition.setIfAppendPositionEqual(offset);

//        int previousResultsCount = this.opContext.getRequestResults().size();
//        try {

            AppendBlobAccessConditions appendBlobAccessConditions = accessCondition == null ? null : new AppendBlobAccessConditions().leaseAccessConditions(accessCondition.leaseAccessConditions()).modifiedAccessConditions(accessCondition.modifiedAccessConditions());
            return blobRef.appendBlock(blockData, writeLength, appendBlobAccessConditions)
                .then()
                .onErrorResume(t -> t instanceof StorageException, e -> {
                    this.lastError = new IOException(e);
                    return null;
                });
//        }
//        catch (final IOException e) {
//            this.lastError = e;
//        }
//        catch (final StorageException e) {
//            if (this.options.getAbsorbConditionalErrorsOnRetry()
//                && e.statusCode() == HttpURLConnection.HTTP_PRECON_FAILED
//                && e.getExtendedErrorInformation() != null
//                && e.errorCode() != null
//                && (e.errorCode()
//                .equals(StorageErrorCode.INVALID_APPEND_POSITION) ||
//                e.getErrorCode().equals(StorageErrorCodeStrings.INVALID_MAX_BLOB_SIZE_CONDITION))
//                && (this.opContext.getRequestResults().size() - previousResultsCount > 1)) {
//
            // TODO
//                // Pre-condition failure on a retry should be ignored in a single writer scenario since
//                // the request succeeded in the first attempt.
//                Logger.info(this.opContext, SR.PRECONDITION_FAILURE_IGNORED);
//            }
//            else {
//                this.lastError = Utility.initIOException(e);
//            }
//        }
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

        this.dispatchWrite();

        // Waits for all submitted tasks to complete
        Set<Future<Void>> requests = new HashSet<Future<Void>>(this.futureSet);
        for (Future<Void> request : requests) {
            // wait for the future to complete
            try {
                request.get();
            }
            catch (Exception e) {
                throw new IOException(e);
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

        return Base64.getEncoder().encodeToString(blockIdInBytes);
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

//    /**
//     * Writes all data from the InputStream to the Blob.
//     * <p>
//     * If you are using {@link CloudAppendBlob} and are certain of a single writer scenario, please look at
//     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag to
//     * <code>true</code> is acceptable for you.
//     *
//     * @param sourceStream
//     *            An {@link InputStream} object which species the data to write to the Blob.
//     *
//     * @throws IOException
//     *             If an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
//     *             closed.
//     * @throws StorageException
//     *             An exception representing any error which occurred during the operation.
//     */
//    public void write(final InputStream sourceStream, final long writeLength) throws IOException, StorageException {
//        Utility.writeToOutputStream(sourceStream, this, writeLength, false, false, this.opContext, this.options, false);
//    }

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

    private synchronized void writeInternal(final byte[] data, int offset, int length) throws IOException {
        while (length > 0) {
            this.checkStreamState();

            final int availableBufferBytes = this.internalWriteThreshold - this.outBuffer.size();
            final int nextWrite = Math.min(availableBufferBytes, length);

            // If we need to set MD5 then update the digest accordingly
//            if (this.options.getStoreBlobContentMD5()) {
//                this.md5Digest.update(data, offset, nextWrite);
//            }

            this.outBuffer.write(data, offset, nextWrite);
            offset += nextWrite;
            length -= nextWrite;

            if (this.outBuffer.size() == this.internalWriteThreshold) {
                this.dispatchWrite();
            }
        }
    }
}
