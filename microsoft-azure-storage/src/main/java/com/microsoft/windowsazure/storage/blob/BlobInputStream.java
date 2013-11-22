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
package com.microsoft.windowsazure.storage.blob;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.microsoft.windowsazure.storage.AccessCondition;
import com.microsoft.windowsazure.storage.Constants;
import com.microsoft.windowsazure.storage.DoesServiceRequest;
import com.microsoft.windowsazure.storage.OperationContext;
import com.microsoft.windowsazure.storage.StorageErrorCode;
import com.microsoft.windowsazure.storage.StorageErrorCodeStrings;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.core.Base64;
import com.microsoft.windowsazure.storage.core.SR;
import com.microsoft.windowsazure.storage.core.Utility;

/**
 * Provides an input stream to read a given blob resource.
 */
public final class BlobInputStream extends InputStream {
    /**
     * Holds the reference to the blob this stream is associated with.
     */
    private final CloudBlob parentBlobRef;

    /**
     * Holds the reference to the MD5 digest for the blob.
     */
    private MessageDigest md5Digest;

    /**
     * A flag to determine if the stream is faulted, if so the lasterror will be thrown on next operation.
     */
    private volatile boolean streamFaulted;

    /**
     * Holds the last exception this stream encountered.
     */
    private IOException lastError;

    /**
     * Holds the OperationContext for the current stream.
     */
    private final OperationContext opContext;

    /**
     * Holds the options for the current stream
     */
    private final BlobRequestOptions options;

    /**
     * Holds the stream length.
     */
    private long streamLength = -1;

    /**
     * Holds the stream read size for both block and page blobs.
     */
    private final int readSize;

    /**
     * A flag indicating if the Blob MD5 should be validated.
     */
    private boolean validateBlobMd5;

    /**
     * Holds the Blob MD5.
     */
    private final String retrievedContentMD5Value;

    /**
     * Holds the reference to the current buffered data.
     */
    private ByteArrayInputStream currentBuffer;

    /**
     * Holds an absolute byte position for the mark feature.
     */
    private long markedPosition;

    /**
     * Holds the mark delta for which the mark position is expired.
     */
    private int markExpiry;

    /**
     * Holds an absolute byte position of the current read position.
     */
    private long currentAbsoluteReadPosition;

    /**
     * Holds the absolute byte position of the start of the current buffer.
     */
    private long bufferStartOffset;

    /**
     * Holds the length of the current buffer in bytes.
     */
    private int bufferSize;

    /**
     * Holds the {@link AccessCondition} object that represents the access conditions for the blob.
     */
    private AccessCondition accessCondition = null;

    /**
     * Initializes a new instance of the BlobInputStream class.
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
    @DoesServiceRequest
    protected BlobInputStream(final CloudBlob parentBlob, final AccessCondition accessCondition,
            final BlobRequestOptions options, final OperationContext opContext) throws StorageException {
        this.parentBlobRef = parentBlob;
        this.parentBlobRef.assertCorrectBlobType();
        this.options = new BlobRequestOptions(options);
        this.opContext = opContext;
        this.streamFaulted = false;
        this.currentAbsoluteReadPosition = 0;
        this.readSize = parentBlob.getStreamMinimumReadSizeInBytes();

        if (options.getUseTransactionalContentMD5() && this.readSize > 4 * Constants.MB) {
            throw new IllegalArgumentException(SR.INVALID_RANGE_CONTENT_MD5_HEADER);
        }

        parentBlob.downloadAttributes(accessCondition, this.options, this.opContext);

        final HttpURLConnection attributesRequest = this.opContext.getCurrentRequestObject();

        this.retrievedContentMD5Value = attributesRequest.getHeaderField(Constants.HeaderConstants.CONTENT_MD5);

        // Will validate it if it was returned
        this.validateBlobMd5 = !options.getDisableContentMD5Validation()
                && !Utility.isNullOrEmpty(this.retrievedContentMD5Value);

        // Validates the first option, and sets future requests to use if match
        // request option.

        // If there is an existing conditional validate it, as we intend to
        // replace if for future requests.
        String previousLeaseId = null;
        if (accessCondition != null) {
            previousLeaseId = accessCondition.getLeaseID();

            if (!accessCondition.verifyConditional(this.parentBlobRef.getProperties().getEtag(), this.parentBlobRef
                    .getProperties().getLastModified())) {
                throw new StorageException(StorageErrorCode.CONDITION_FAILED.toString(),
                        SR.INVALID_CONDITIONAL_HEADERS, HttpURLConnection.HTTP_PRECON_FAILED, null, null);
            }
        }

        this.accessCondition = AccessCondition.generateIfMatchCondition(this.parentBlobRef.getProperties().getEtag());
        this.accessCondition.setLeaseID(previousLeaseId);

        this.streamLength = parentBlob.getProperties().getLength();

        if (this.validateBlobMd5) {
            try {
                this.md5Digest = MessageDigest.getInstance("MD5");
            }
            catch (final NoSuchAlgorithmException e) {
                // This wont happen, throw fatal.
                throw Utility.generateNewUnexpectedStorageException(e);
            }
        }

        this.reposition(0);
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream without
     * blocking by the next invocation of a method for this input stream. The next invocation might be the same thread
     * or another thread. A single read or skip of this many bytes will not block, but may read or skip fewer bytes.
     * 
     * @throws IOException
     *             - if an I/O error occurs.
     * @return an estimate of the number of bytes that can be read (or skipped over) from this input stream without
     *         blocking or 0 when it reaches the end of the input stream.
     */
    @Override
    public synchronized int available() throws IOException {
        return this.bufferSize - (int) (this.currentAbsoluteReadPosition - this.bufferStartOffset);
    }

    /**
     * Helper function to check if the stream is faulted, if it is it surfaces the exception.
     * 
     * @throws IOException
     *             if an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
     *             closed.
     */
    private synchronized void checkStreamState() throws IOException {
        if (this.streamFaulted) {
            throw this.lastError;
        }
    }

    /**
     * Closes this input stream and releases any system resources associated with the stream.
     * 
     * @throws IOException
     *             - if an I/O error occurs.
     */
    @Override
    public synchronized void close() throws IOException {
        this.currentBuffer = null;
        this.streamFaulted = true;
        this.lastError = new IOException("Stream is closed");
    }

    /**
     * Dispatches a read operation of N bytes. When using sparspe page blobs the page ranges are evaluated and zero
     * bytes may be generated on the client side for some ranges that do not exist.
     * 
     * @param readLength
     *            the number of bytes to read.
     * @throws IOException
     *             if an I/O error occurs.
     */
    @DoesServiceRequest
    private synchronized void dispatchRead(final int readLength) throws IOException {
        try {
            final byte[] byteBuffer = new byte[readLength];

            this.parentBlobRef.downloadRangeInternal(this.currentAbsoluteReadPosition, (long) readLength, byteBuffer,
                    0, this.accessCondition, this.options, this.opContext);

            this.currentBuffer = new ByteArrayInputStream(byteBuffer);
            this.bufferSize = readLength;
            this.bufferStartOffset = this.currentAbsoluteReadPosition;
        }
        catch (final StorageException e) {
            this.streamFaulted = true;
            this.lastError = Utility.initIOException(e);
            throw this.lastError;
        }
    }

    /**
     * Gets a value indicating if MD5 should be validated.
     * 
     * @return a value indicating if MD5 should be validated.
     */
    protected synchronized boolean getValidateBlobMd5() {
        return this.validateBlobMd5;
    }

    /**
     * Marks the current position in this input stream. A subsequent call to the reset method repositions this stream at
     * the last marked position so that subsequent reads re-read the same bytes.
     * 
     * @param readlimit
     *            - the maximum limit of bytes that can be read before the mark position becomes invalid.
     */
    @Override
    public synchronized void mark(final int readlimit) {
        this.markedPosition = this.currentAbsoluteReadPosition;
        this.markExpiry = readlimit;
    }

    /**
     * Tests if this input stream supports the mark and reset methods. Whether or not mark and reset are supported is an
     * invariant property of a particular input stream instance. The markSupported method of InputStream returns false.
     * 
     * @return <Code>True</Code> if this stream instance supports the mark and reset methods; <Code>False</Code>
     *         otherwise.
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is returned as an int in the range 0 to 255. If
     * no byte is available because the end of the stream has been reached, the value -1 is returned. This method blocks
     * until input data is available, the end of the stream is detected, or an exception is thrown.
     * 
     * @return the next byte of data, or -1 if the end of the stream is reached.
     * @throws IOException
     *             - if an I/O error occurs.
     */
    @Override
    @DoesServiceRequest
    public int read() throws IOException {
        final byte[] tBuff = new byte[1];
        final int numberOfBytesRead = this.read(tBuff, 0, 1);

        if (numberOfBytesRead > 0) {
            return tBuff[0] & 0xFF;
        }
        else if (numberOfBytesRead == 0) {
            throw new IOException(SR.UNEXPECTED_STREAM_READ_ERROR);
        }
        else {
            return -1;
        }
    }

    /**
     * Reads some number of bytes from the input stream and stores them into the buffer array b. The number of bytes
     * actually read is returned as an integer. This method blocks until input data is available, end of file is
     * detected, or an exception is thrown. If the length of b is zero, then no bytes are read and 0 is returned;
     * otherwise, there is an attempt to read at least one byte. If no byte is available because the stream is at the
     * end of the file, the value -1 is returned; otherwise, at least one byte is read and stored into b.
     * 
     * The first byte read is stored into element b[0], the next one into b[1], and so on. The number of bytes read is,
     * at most, equal to the length of b. Let k be the number of bytes actually read; these bytes will be stored in
     * elements b[0] through b[k-1], leaving elements b[k] through b[b.length-1] unaffected.
     * 
     * The read(b) method for class InputStream has the same effect as:
     * 
     * read(b, 0, b.length)
     * 
     * @param b
     *            - the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or -1 is there is no more data because the end of the
     *         stream has been reached.
     * 
     * @throws IOException
     *             - If the first byte cannot be read for any reason other than the end of the file, if the input stream
     *             has been closed, or if some other I/O error occurs.
     * @throws NullPointerException
     *             - if b is null.
     */
    @Override
    @DoesServiceRequest
    public int read(final byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    /**
     * Reads up to len bytes of data from the input stream into an array of bytes. An attempt is made to read as many as
     * len bytes, but a smaller number may be read. The number of bytes actually read is returned as an integer. This
     * method blocks until input data is available, end of file is detected, or an exception is thrown.
     * 
     * If len is zero, then no bytes are read and 0 is returned; otherwise, there is an attempt to read at least one
     * byte. If no byte is available because the stream is at end of file, the value -1 is returned; otherwise, at least
     * one byte is read and stored into b.
     * 
     * The first byte read is stored into element b[off], the next one into b[off+1], and so on. The number of bytes
     * read is, at most, equal to len. Let k be the number of bytes actually read; these bytes will be stored in
     * elements b[off] through b[off+k-1], leaving elements b[off+k] through b[off+len-1] unaffected.
     * 
     * In every case, elements b[0] through b[off] and elements b[off+len] through b[b.length-1] are unaffected.
     * 
     * The read(b, off, len) method for class InputStream simply calls the method read() repeatedly. If the first such
     * call results in an IOException, that exception is returned from the call to the read(b, off, len) method. If any
     * subsequent call to read() results in a IOException, the exception is caught and treated as if it were end of
     * file; the bytes read up to that point are stored into b and the number of bytes read before the exception
     * occurred is returned. The default implementation of this method blocks until the requested amount of input data
     * len has been read, end of file is detected, or an exception is thrown. Subclasses are encouraged to provide a
     * more efficient implementation of this method.
     * 
     * @param b
     *            the buffer into which the data is read.
     * @param off
     *            the start offset in array b at which the data is written.
     * @param len
     *            the maximum number of bytes to read.
     * @return the total number of bytes read into the buffer, or -1 if there is no more data because the end of the
     *         stream has been reached.
     * @throws IOException
     *             If the first byte cannot be read for any reason other than end of file, or if the input stream has
     *             been closed, or if some other I/O error occurs.
     * @throws NullPointerException
     *             If b is null.
     * @throws IndexOutOfBoundsException
     *             If off is negative, len is negative, or len is greater than b.length - off
     */
    @Override
    @DoesServiceRequest
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }

        return this.readInternal(b, off, len);
    }

    /**
     * Performs internal read to the given byte buffer.
     * 
     * @param b
     *            the buffer into which the data is read.
     * @param off
     *            the start offset in array b at which the data is written.
     * @param len
     *            the maximum number of bytes to read.
     * @return the total number of bytes read into the buffer, or -1 if there is no more data because the end of the
     *         stream has been reached.
     * @throws IOException
     *             If the first byte cannot be read for any reason other than end of file, or if the input stream has
     *             been closed, or if some other I/O error occurs.
     */
    @DoesServiceRequest
    private synchronized int readInternal(final byte[] b, final int off, int len) throws IOException {
        this.checkStreamState();

        // if buffer is empty do next get operation
        if ((this.currentBuffer == null || this.currentBuffer.available() == 0)
                && this.currentAbsoluteReadPosition < this.streamLength) {
            this.dispatchRead((int) Math.min(this.readSize, this.streamLength - this.currentAbsoluteReadPosition));
        }

        len = Math.min(len, this.readSize);

        // do read from buffer
        final int numberOfBytesRead = this.currentBuffer.read(b, off, len);

        if (numberOfBytesRead > 0) {
            this.currentAbsoluteReadPosition += numberOfBytesRead;

            if (this.validateBlobMd5) {
                this.md5Digest.update(b, off, numberOfBytesRead);

                if (this.currentAbsoluteReadPosition == this.streamLength) {
                    // Reached end of stream, validate md5.
                    final String calculatedMd5 = Base64.encode(this.md5Digest.digest());
                    if (!calculatedMd5.equals(this.retrievedContentMD5Value)) {
                        this.lastError = Utility
                                .initIOException(new StorageException(
                                        StorageErrorCodeStrings.INVALID_MD5,
                                        String.format(
                                                "Blob data corrupted (integrity check failed), Expected value is %s, retrieved %s",
                                                this.retrievedContentMD5Value, calculatedMd5),
                                        Constants.HeaderConstants.HTTP_UNUSED_306, null, null));
                        this.streamFaulted = true;
                        throw this.lastError;
                    }
                }
            }
        }

        // update markers
        if (this.markExpiry > 0 && this.markedPosition + this.markExpiry < this.currentAbsoluteReadPosition) {
            this.markedPosition = 0;
            this.markExpiry = 0;
        }

        return numberOfBytesRead;
    }

    /**
     * Repositions the stream to the given absolute byte offset.
     * 
     * @param absolutePosition
     *            the absolute byte offset to reposition to.
     */
    private synchronized void reposition(final long absolutePosition) {
        this.currentAbsoluteReadPosition = absolutePosition;
        this.currentBuffer = new ByteArrayInputStream(new byte[0]);
    }

    /**
     * Repositions this stream to the position at the time the mark method was last called on this input stream. Note
     * repositioning the blob read stream will disable blob MD5 checking.
     * 
     * @throws IOException
     *             if this stream has not been marked or if the mark has been invalidated.
     */
    @Override
    public synchronized void reset() throws IOException {
        if (this.markedPosition + this.markExpiry < this.currentAbsoluteReadPosition) {
            throw new IOException(SR.MARK_EXPIRED);
        }

        this.validateBlobMd5 = false;
        this.md5Digest = null;
        this.reposition(this.markedPosition);
    }

    /**
     * Sets a value indicating if MD5 should be validated.
     * 
     * @param validateBlobMd5
     *            a value indicating if MD5 should be validated.
     */
    protected synchronized void setValidateBlobMd5(final boolean validateBlobMd5) {
        this.validateBlobMd5 = validateBlobMd5;
    }

    /**
     * Skips over and discards n bytes of data from this input stream. The skip method may, for a variety of reasons,
     * end up skipping over some smaller number of bytes, possibly 0. This may result from any of a number of
     * conditions; reaching end of file before n bytes have been skipped is only one possibility. The actual number of
     * bytes skipped is returned. If n is negative, no bytes are skipped.
     * 
     * Note repositioning the blob read stream will disable blob MD5 checking.
     * 
     * @param n
     *            the number of bytes to skip
     */
    @Override
    public synchronized long skip(final long n) throws IOException {
        if (n == 0) {
            return 0;
        }

        if (n < 0 || this.currentAbsoluteReadPosition + n > this.streamLength) {
            throw new IndexOutOfBoundsException();
        }

        this.validateBlobMd5 = false;
        this.md5Digest = null;
        this.reposition(this.currentAbsoluteReadPosition + n);
        return n;
    }

    /**
     * Writes the entire blob contents from the Windows Azure Blob service to the given output stream.
     * 
     * @param outStream
     *            the output stream to write to.
     * @return the number of bytes written.
     * @throws IOException
     *             if an I/O Error occurs
     */
    @DoesServiceRequest
    protected long writeTo(final OutputStream outStream) throws IOException {
        final byte[] buffer = new byte[Constants.BUFFER_COPY_LENGTH];
        long total = 0;
        int count = this.read(buffer);

        while (count != -1) {
            outStream.write(buffer, 0, count);
            total += count;
            count = this.read(buffer);
        }

        return total;
    }
}
