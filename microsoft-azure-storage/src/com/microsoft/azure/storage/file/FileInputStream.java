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
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.DoesServiceRequest;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageErrorCode;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.Base64;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * Provides an input stream to read a given file resource.
 */
public class FileInputStream extends InputStream {
    /**
     * Holds the reference to the file this stream is associated with.
     */
    private final CloudFile parentFileRef;

    /**
     * Holds the reference to the MD5 digest for the file.
     */
    private MessageDigest md5Digest;

    /**
     * A flag to determine if the stream is faulted, if so the last error will be thrown on next operation.
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
    private final FileRequestOptions options;

    /**
     * Holds the stream length.
     */
    private long streamLength = -1;

    /**
     * Holds the stream read size.
     */
    private final int readSize;

    /**
     * A flag indicating if the File MD5 should be validated.
     */
    private boolean validateFileMd5;

    /**
     * Holds the File MD5.
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
     * Holds the {@link AccessCondition} object that represents the access conditions for the file.
     */
    private AccessCondition accessCondition = null;

    /**
     * Initializes a new instance of the FileInputStream class.
     * 
     * @param parentFile
     *            A {@link CloudFile} object which represents the file that this stream is associated with.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object which represents that specifies any additional options for the
     *            request.
     * @param opContext
     *            An {@link OperationContext} object which is used to track the execution of the operation.
     * 
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    protected FileInputStream(final CloudFile parentFile, final AccessCondition accessCondition,
            final FileRequestOptions options, final OperationContext opContext) throws StorageException {
        this.parentFileRef = parentFile;
        this.options = new FileRequestOptions(options);
        this.opContext = opContext;
        this.streamFaulted = false;
        this.currentAbsoluteReadPosition = 0;
        this.readSize = parentFile.getStreamMinimumReadSizeInBytes();

        if (options.getUseTransactionalContentMD5() && this.readSize > 4 * Constants.MB) {
            throw new IllegalArgumentException(SR.INVALID_RANGE_CONTENT_MD5_HEADER);
        }

        parentFile.downloadAttributes(accessCondition, this.options, this.opContext);

        this.retrievedContentMD5Value = parentFile.getProperties().getContentMD5();

        // Will validate it if it was returned
        this.validateFileMd5 = !options.getDisableContentMD5Validation()
                && !Utility.isNullOrEmpty(this.retrievedContentMD5Value);

        String previousLeaseId = null;
        if (accessCondition != null) {
            previousLeaseId = accessCondition.getLeaseID();
        }

        this.accessCondition = AccessCondition.generateIfMatchCondition(this.parentFileRef.getProperties().getEtag());
        this.accessCondition.setLeaseID(previousLeaseId);

        this.streamLength = parentFile.getProperties().getLength();

        if (this.validateFileMd5) {
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
     * @return An <code>int</code> which represents an estimate of the number of bytes that can be read (or skipped
     *         over)
     *         from this input stream without blocking, or 0 when it reaches the end of the input stream.
     * 
     * @throws IOException
     *             If an I/O error occurs.
     */
    @Override
    public synchronized int available() throws IOException {
        return this.bufferSize - (int) (this.currentAbsoluteReadPosition - this.bufferStartOffset);
    }

    /**
     * Helper function to check if the stream is faulted, if it is it surfaces the exception.
     * 
     * @throws IOException
     *             If an I/O error occurs. In particular, an IOException may be thrown if the output stream has been
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
     *             If an I/O error occurs.
     */
    @Override
    public synchronized void close() throws IOException {
        this.currentBuffer = null;
        this.streamFaulted = true;
        this.lastError = new IOException(SR.STREAM_CLOSED);
    }

    /**
     * Dispatches a read operation of N bytes.
     * 
     * @param readLength
     *            An <code>int</code> which represents the number of bytes to read.
     * 
     * @throws IOException
     *             If an I/O error occurs.
     */
    @DoesServiceRequest
    private synchronized void dispatchRead(final int readLength) throws IOException {
        try {
            final byte[] byteBuffer = new byte[readLength];

            this.parentFileRef.downloadRangeInternal(this.currentAbsoluteReadPosition, (long) readLength, byteBuffer,
                    0, null /* this.accessCondition */, this.options, this.opContext);

            // Check Etag manually for now -- use access condition once conditional headers supported.
            if (this.accessCondition != null) {
                if (!this.accessCondition.getIfMatch().equals(this.parentFileRef.getProperties().getEtag())) {
                    throw new StorageException(StorageErrorCode.CONDITION_FAILED.toString(),
                            SR.INVALID_CONDITIONAL_HEADERS, HttpURLConnection.HTTP_PRECON_FAILED, null, null);
                }
            }

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
     * Marks the current position in this input stream. A subsequent call to the reset method repositions this stream at
     * the last marked position so that subsequent reads re-read the same bytes.
     * 
     * @param readlimit
     *            An <code>int</code> which represents the maximum limit of bytes that can be read before the mark
     *            position becomes invalid.
     */
    @Override
    public synchronized void mark(final int readlimit) {
        this.markedPosition = this.currentAbsoluteReadPosition;
        this.markExpiry = readlimit;
    }

    /**
     * Tests if this input stream supports the mark and reset methods. Whether or not mark and reset are supported is an
     * invariant property of a particular input stream instance. The markSupported method of {@link InputStream} returns
     * false.
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
     * @return An <code>int</code> which represents the total number of bytes read into the buffer, or -1 if
     *         there is no more data because the end of the stream has been reached.
     * 
     * @throws IOException
     *             If an I/O error occurs.
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
     * Reads some number of bytes from the input stream and stores them into the buffer array <code>b</code>. The number
     * of bytes actually read is returned as an integer. This method blocks until input data is available, end of file
     * is detected, or an exception is thrown. If the length of <code>b</code> is zero, then no bytes are read and 0 is
     * returned; otherwise, there is an attempt to read at least one byte. If no byte is available because the stream is
     * at the end of the file, the value -1 is returned; otherwise, at least one byte is read and stored into
     * <code>b</code>.
     * 
     * The first byte read is stored into element <code>b[0]</code>, the next one into <code>b[1]</code>, and so on. The
     * number of bytes read is, at most, equal to the length of <code>b</code>. Let <code>k</code> be the number of
     * bytes actually read; these bytes will be stored in elements <code>b[0]</code> through <code>b[k-1]</code>,
     * leaving elements <code>b[k]</code> through <code>b[b.length-1]</code> unaffected.
     * 
     * The <code>read(b) method for class {@link InputStream} has the same effect as:
     * 
     * <code>read(b, 0, b.length)</code>
     * 
     * @param b
     *            A <code>byte</code> array which represents the buffer into which the data is read.
     * 
     * @throws IOException
     *             If the first byte cannot be read for any reason other than the end of the file, if the input stream
     *             has been closed, or if some other I/O error occurs.
     * @throws NullPointerException
     *             If the <code>byte</code> array <code>b</code> is null.
     */
    @Override
    @DoesServiceRequest
    public int read(final byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into an array of bytes. An attempt is made to
     * read as many as <code>len</code> bytes, but a smaller number may be read. The number of bytes actually read is
     * returned as an integer. This method blocks until input data is available, end of file is detected, or an
     * exception is thrown.
     * 
     * If <code>len</code> is zero, then no bytes are read and 0 is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the stream is at end of file, the value -1 is returned;
     * otherwise, at least one byte is read and stored into <code>b</code>.
     * 
     * The first byte read is stored into element <code>b[off]</code>, the next one into <code>b[off+1]</code>, and so
     * on. The number of bytes read is, at most, equal to <code>len</code>. Let <code>k</code> be the number of bytes
     * actually read; these bytes will be stored in elements <code>b[off]</code> through <code>b[off+k-1]</code>,
     * leaving elements <code>b[off+k]</code> through <code>b[off+len-1]</code> unaffected.
     * 
     * In every case, elements <code>b[0]</code> through <code>b[off]</code> and elements <code>b[off+len]</code>
     * through <code>b[b.length-1]</code> are unaffected.
     * 
     * The <code>read(b, off, len)</code> method for class {@link InputStream} simply calls the method
     * <code>read()</code> repeatedly. If the first such call results in an <code>IOException</code>, that exception is
     * returned from the call to the <code>read(b, off, len)</code> method. If any subsequent call to
     * <code>read()</code> results in a <code>IOException</code>, the exception is caught and treated
     * as if it were end of file; the bytes read up to that point are stored into <code>b</code> and the number of bytes
     * read before the exception occurred is returned. The default implementation of this method blocks until the
     * requested amount of input data <code>len</code> has been read, end of file is detected, or an exception is
     * thrown. Subclasses are encouraged to provide a more efficient implementation of this method.
     * 
     * @param b
     *            A <code>byte</code> array which represents the buffer into which the data is read.
     * @param off
     *            An <code>int</code> which represents the start offset in the <code>byte</code> array at which the data
     *            is written.
     * @param len
     *            An <code>int</code> which represents the maximum number of bytes to read.
     * 
     * @return An <code>int</code> which represents the total number of bytes read into the buffer, or -1 if
     *         there is no more data because the end of the stream has been reached.
     * 
     * @throws IOException
     *             If the first byte cannot be read for any reason other than end of file, or if the input stream has
     *             been closed, or if some other I/O error occurs.
     * @throws NullPointerException
     *             If the <code>byte</code> array <code>b</code> is null.
     * @throws IndexOutOfBoundsException
     *             If <code>off</code> is negative, <code>len</code> is negative, or <code>len</code> is greater than
     *             <code>b.length - off</code>.
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
     *            A <code>byte</code> array which represents the buffer into which the data is read.
     * @param off
     *            An <code>int</code> which represents the start offset in the <code>byte</code> array <code>b</code> at
     *            which the data is written.
     * @param len
     *            An <code>int</code> which represents the maximum number of bytes to read.
     * 
     * @return An <code>int</code> which represents the total number of bytes read into the buffer, or -1 if
     *         there is no more data because the end of the stream has been reached.
     * 
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

            if (this.validateFileMd5) {
                this.md5Digest.update(b, off, numberOfBytesRead);

                if (this.currentAbsoluteReadPosition == this.streamLength) {
                    // Reached end of stream, validate md5.
                    final String calculatedMd5 = Base64.encode(this.md5Digest.digest());
                    if (!calculatedMd5.equals(this.retrievedContentMD5Value)) {
                        this.lastError = Utility
                                .initIOException(new StorageException(
                                        StorageErrorCodeStrings.INVALID_MD5,
                                        String.format(
                                                "File data corrupted (integrity check failed), Expected value is %s, retrieved %s",
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
     *            A <code>long</code> which represents the absolute byte offset withitn the stream reposition.
     */
    private synchronized void reposition(final long absolutePosition) {
        this.currentAbsoluteReadPosition = absolutePosition;
        this.currentBuffer = new ByteArrayInputStream(new byte[0]);
    }

    /**
     * Repositions this stream to the position at the time the mark method was last called on this input stream. Note
     * repositioning the file read stream will disable file MD5 checking.
     * 
     * @throws IOException
     *             If this stream has not been marked or if the mark has been invalidated.
     */
    @Override
    public synchronized void reset() throws IOException {
        if (this.markedPosition + this.markExpiry < this.currentAbsoluteReadPosition) {
            throw new IOException(SR.MARK_EXPIRED);
        }

        this.validateFileMd5 = false;
        this.md5Digest = null;
        this.reposition(this.markedPosition);
    }

    /**
     * Skips over and discards n bytes of data from this input stream. The skip method may, for a variety of reasons,
     * end up skipping over some smaller number of bytes, possibly 0. This may result from any of a number of
     * conditions; reaching end of file before n bytes have been skipped is only one possibility. The actual number of
     * bytes skipped is returned. If n is negative, no bytes are skipped.
     * 
     * Note repositioning the file read stream will disable file MD5 checking.
     * 
     * @param n
     *            A <code>long</code> which represents the number of bytes to skip.
     */
    @Override
    public synchronized long skip(final long n) throws IOException {
        if (n == 0) {
            return 0;
        }

        if (n < 0 || this.currentAbsoluteReadPosition + n > this.streamLength) {
            throw new IndexOutOfBoundsException();
        }

        this.validateFileMd5 = false;
        this.md5Digest = null;
        this.reposition(this.currentAbsoluteReadPosition + n);
        return n;
    }
}
