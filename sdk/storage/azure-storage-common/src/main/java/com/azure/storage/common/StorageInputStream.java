// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Provides an input stream to read a given storage resource.
 */
public abstract class StorageInputStream extends InputStream {
    private static final String MARK_EXPIRED = "Stream mark expired.";
    private static final String UNEXPECTED_STREAM_READ_ERROR =
        "Unexpected error. Stream returned unexpected number of bytes.";

    private final ClientLogger logger = new ClientLogger(StorageInputStream.class);

    /**
     * A flag to determine if the stream is faulted, if so the last error will be thrown on next operation.
     */
    protected volatile boolean streamFaulted;

    /**
     * Holds the last exception this stream encountered.
     */
    protected IOException lastError;


    /**
     * Holds the reference to the current buffered data.
     */
    private ByteBuffer currentBuffer;

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
    protected long bufferStartOffset;

    /**
     * Holds the length of the current buffer in bytes.
     */
    protected int bufferSize;

    /**
     * Offset of the source blob this class is configured to stream from.
     */
    private final long rangeOffset;

    /**
     * Holds the stream read size.
     */
    private final int chunkSize;

    /**
     * Holds the stream length.
     */
    private final long streamLength;

    /**
     * Initializes a new instance of the StorageInputStream class.
     *
     * @param chunkSize the size of chunk allowed to pass for storage service request.
     * @param contentLength the actual content length for input data.
     */
    protected StorageInputStream(final int chunkSize, final long contentLength) {
        this(0, null, chunkSize, contentLength);
    }

    /**
     * Initializes a new instance of the StorageInputStream class.
     *
     * @param rangeOffset The offset of the data to begin stream.
     * @param rangeLength How much data the stream should return after blobRangeOffset.
     * @param chunkSize Holds the stream read size.
     * @param contentLength The length of the stream to be transferred.
     * @throws IndexOutOfBoundsException when range offset is less than 0 or rangeLength exists but les than or
     * equal to 0.
     */
    protected StorageInputStream(long rangeOffset, final Long rangeLength,
                                 final int chunkSize, final long contentLength) {
        this.rangeOffset = rangeOffset;
        this.streamFaulted = false;
        this.currentAbsoluteReadPosition = rangeOffset;
        this.chunkSize = chunkSize;
        this.streamLength = rangeLength == null ? contentLength - this.rangeOffset
            : Math.min(contentLength - this.rangeOffset, rangeLength);
        if (rangeOffset < 0 || (rangeLength != null && rangeLength <= 0)) {
            throw logger.logExceptionAsError(new IndexOutOfBoundsException());
        }

        this.reposition(rangeOffset);
    }

    /**
     * Initializes a new instance of the StorageInputStream class.
     *
     * @param rangeOffset The offset of the data to begin stream.
     * @param rangeLength How much data the stream should return after blobRangeOffset.
     * @param chunkSize Holds the stream read size.
     * @param contentLength The length of the stream to be transferred.
     * @throws IndexOutOfBoundsException when range offset is less than 0 or rangeLength exists but les than or
     * equal to 0.
     */
    protected StorageInputStream(long rangeOffset, final Long rangeLength,
        final int chunkSize, final long contentLength, ByteBuffer initialBuffer) {
        this.rangeOffset = rangeOffset;
        this.streamFaulted = false;
        this.currentAbsoluteReadPosition = rangeOffset;
        this.chunkSize = chunkSize;
        this.streamLength = rangeLength == null ? contentLength - this.rangeOffset
            : Math.min(contentLength - this.rangeOffset, rangeLength);
        if (rangeOffset < 0 || (rangeLength != null && rangeLength <= 0)) {
            throw logger.logExceptionAsError(new IndexOutOfBoundsException());
        }

        this.currentBuffer = initialBuffer;
        this.bufferStartOffset = rangeOffset;
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream without
     * blocking by the next invocation of a method for this input stream. The next invocation might be the same thread
     * or another thread. A single read or skip of this many bytes will not block, but may read or skip fewer bytes.
     *
     * @return An <code>int</code> which represents an estimate of the number of bytes that can be read (or skipped
     * over) from this input stream without blocking, or 0 when it reaches the end of the input stream.
     */
    @Override
    public synchronized int available() {
        return this.bufferSize - (int) (this.currentAbsoluteReadPosition - this.bufferStartOffset);
    }

    /**
     * Helper function to check if the stream is faulted, if it is it surfaces the exception.
     *
     * @throws RuntimeException If an I/O error occurs. In particular, an IOException may be thrown if the output stream
     * has been closed.
     */
    private synchronized void checkStreamState() {
        if (this.streamFaulted) {
            throw logger.logExceptionAsError(new RuntimeException(this.lastError.getMessage()));
        }
    }

    /**
     * Closes this input stream and releases any system resources associated with the stream.
     */
    @Override
    public synchronized void close() {
        this.currentBuffer = null;
        this.streamFaulted = true;
        this.lastError = new IOException(Constants.STREAM_CLOSED);
    }

    /**
     * Dispatches a read operation of N bytes and updates stream state accordingly.
     *
     * @param readLength An <code>int</code> which represents the number of bytes to read.
     * @param offset The start point of data to be acquired.
     * @return The bytebuffer which store one chunk size of data.
     * @throws IOException If an I/O error occurs.
     */
    protected abstract ByteBuffer dispatchRead(int readLength, long offset) throws IOException;

    /**
     * Marks the current position in this input stream. A subsequent call to the reset method repositions this stream at
     * the last marked position so that subsequent reads re-read the same bytes.
     *
     * @param readlimit An <code>int</code> which represents the maximum limit of bytes that can be read before the mark
     * position becomes invalid.
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
     * otherwise.
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
     * @return An <code>int</code> which represents the total number of bytes read into the buffer, or -1 if there is no
     * more data because the end of the stream has been reached.
     * @throws RuntimeException when no available bytes to read.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        final byte[] tBuff = new byte[1];
        final int numberOfBytesRead = this.read(tBuff, 0, 1);

        if (numberOfBytesRead > 0) {
            return tBuff[0] & 0xFF;
        } else if (numberOfBytesRead == 0) {
            throw logger.logExceptionAsError(new RuntimeException(UNEXPECTED_STREAM_READ_ERROR));
        } else {
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
     * leaving elements <code>b[k]</code> through
     * <code>b[b.length-1]</code> unaffected.
     *
     * The <code>read(b)</code> method for class {@link InputStream} has the same effect as:
     *
     * <code>read(b, 0, b.length)</code>
     *
     * @param b A <code>byte</code> array which represents the buffer into which the data is read.
     * @throws IOException If the first byte cannot be read for any reason other than the end of the file, if the input
     * stream has been closed, or if some other I/O error occurs.
     * @throws NullPointerException If the <code>byte</code> array <code>b</code> is null.
     */
    @Override
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
     * leaving elements <code>b[off+k]</code> through
     * <code>b[off+len-1]</code> unaffected.
     *
     * In every case, elements <code>b[0]</code> through <code>b[off]</code> and elements <code>b[off+len]</code>
     * through <code>b[b.length-1]</code> are unaffected.
     *
     * The <code>read(b, off, len)</code> method for class {@link InputStream} simply calls the method
     * <code>read()</code> repeatedly. If the first such
     * call results in an <code>IOException</code>, that exception is returned from the call to the
     * <code>read(b, off, len)</code> method. If any
     * subsequent call to <code>read()</code> results in a <code>IOException</code>, the exception is caught and treated
     * as if it were end of file; the bytes read up to that point are stored into <code>b</code> and the number of bytes
     * read before the exception occurred is returned. The default implementation of this method blocks until the
     * requested amount of input data
     * <code>len</code> has been read, end of file is detected, or an exception is thrown. Subclasses are encouraged to
     * provide a more efficient implementation of this method.
     *
     * @param b A <code>byte</code> array which represents the buffer into which the data is read.
     * @param off An <code>int</code> which represents the start offset in the <code>byte</code> array at which the data
     * is written.
     * @param len An <code>int</code> which represents the maximum number of bytes to read.
     * @return An <code>int</code> which represents the total number of bytes read into the buffer, or -1 if there is no
     * more data because the end of the stream has been reached.
     * @throws IOException If the first byte cannot be read for any reason other than end of file, or if the input
     * stream has been closed, or if some other I/O error occurs.
     * @throws NullPointerException If the <code>byte</code> array <code>b</code> is null.
     * @throws IndexOutOfBoundsException If <code>off</code> is negative, <code>len</code> is negative, or
     * <code>len</code> is greater than
     * <code>b.length - off</code>.
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (off < 0 || len < 0 || len > b.length - off) {
            throw logger.logExceptionAsError(new IndexOutOfBoundsException());
        }

        int chunks = (int) (Math.ceil((double) len / (double) this.chunkSize));
        int numOfBytesRead = 0;
        for (int i = 0; i < chunks; i++) {
            int results = this.readInternal(b, off + numOfBytesRead, len - numOfBytesRead);
            if (results == -1) {
                return numOfBytesRead == 0 ? -1 : numOfBytesRead;
            }
            numOfBytesRead += results;
        }
        return numOfBytesRead;
    }

    /**
     * Performs internal read to the given byte buffer.
     *
     * @param b A <code>byte</code> array which represents the buffer into which the data is read.
     * @param off An <code>int</code> which represents the start offset in the <code>byte</code> array <code>b</code> at
     * which the data is written.
     * @param len An <code>int</code> which represents the maximum number of bytes to read.
     * @return An <code>int</code> which represents the total number of bytes read into the buffer, or -1 if there is no
     * more data because the end of the stream has been reached.
     * @throws IOException If the first byte cannot be read for any reason other than end of file, or if the input
     * stream has been closed, or if some other I/O error occurs.
     */
    private synchronized int readInternal(final byte[] b, final int off, int len) throws IOException {
        this.checkStreamState();

        // if buffer is empty do next get operation
        if ((this.currentBuffer == null || this.currentBuffer.remaining() == 0)
            && this.currentAbsoluteReadPosition < this.streamLength + this.rangeOffset) {
            this.currentBuffer = this.dispatchRead((int) Math.min(this.chunkSize,
                this.streamLength + this.rangeOffset - this.currentAbsoluteReadPosition),
                this.currentAbsoluteReadPosition);
        }

        len = Math.min(len, this.chunkSize);

        final int numberOfBytesRead;
        if (currentBuffer.remaining() == 0) {
            numberOfBytesRead = -1;
        } else {
            numberOfBytesRead = Math.min(len, this.currentBuffer.remaining());
            // do read from buffer
            this.currentBuffer = this.currentBuffer.get(b, off, numberOfBytesRead);
        }

        if (numberOfBytesRead > 0) {
            this.currentAbsoluteReadPosition += numberOfBytesRead;
        }

        // update markers
        if (this.markExpiry > 0 && this.markedPosition + this.markExpiry < this.currentAbsoluteReadPosition) {
            this.markedPosition = this.rangeOffset;
            this.markExpiry = 0;
        }

        return numberOfBytesRead;
    }

    /**
     * Repositions the stream to the given absolute byte offset.
     *
     * @param absolutePosition A <code>long</code> which represents the absolute byte offset withitn the stream
     * reposition.
     */
    private synchronized void reposition(final long absolutePosition) {
        this.currentAbsoluteReadPosition = absolutePosition;
        this.currentBuffer = ByteBuffer.allocate(0);
        this.bufferStartOffset = absolutePosition;
    }

    /**
     * Repositions this stream to the position at the time the mark method was last called on this input stream. Note
     * repositioning the blob read stream will disable blob MD5 checking.
     *
     * @throws RuntimeException If this stream has not been marked or if the mark has been invalidated.
     */
    @Override
    public synchronized void reset() {
        if (this.markedPosition + this.markExpiry < this.currentAbsoluteReadPosition) {
            throw logger.logExceptionAsError(new RuntimeException(MARK_EXPIRED));
        }
        this.reposition(this.markedPosition);
    }

    /**
     * Skips over and discards n bytes of data from this input stream. The skip method may, for a variety of reasons,
     * end up skipping over some smaller number of bytes, possibly 0. This may result from any of a number of
     * conditions; reaching end of file before n bytes have been skipped is only one possibility. The actual number of
     * bytes skipped is returned. If n is negative, no bytes are skipped.
     *
     * Note repositioning the blob read stream will disable blob MD5 checking.
     *
     * @param n A <code>long</code> which represents the number of bytes to skip.
     */
    @Override
    public synchronized long skip(final long n) {
        if (n == 0) {
            return 0;
        }

        if (n < 0 || this.currentAbsoluteReadPosition + n > this.streamLength + this.rangeOffset) {
            throw logger.logExceptionAsError(new IndexOutOfBoundsException());
        }

        this.reposition(this.currentAbsoluteReadPosition + n);
        return n;
    }
}
