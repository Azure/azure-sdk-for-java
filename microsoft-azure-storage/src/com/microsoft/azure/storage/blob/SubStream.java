/**
 * Copyright Microsoft Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.core.SR;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SubStream extends InputStream {
    // A mutal exclusion lock shared between other related substream instances.
    private final Object lock;

    // Stream to be wrapped.
    private InputStream wrappedStream;

    // The current, relative position in the substream.
    private long substreamCurrentIndex;

    // The position in the wrapped stream (relative to the last mark) where the substream should logically begin.
    private long streamBeginIndex;

    // The length of the substream.
    private long streamLength;

    // Tracks the marked position in the substream.
    private long markIndex;

    // Buffer for read requests.
    private byte[] readBuffer;

    private ByteArrayInputStream readBufferStream;

    // Keeps track of the remaining valid bytes available in the read buffer.
    private int readBufferLength;

    /**
     * Creates a new substream instance that partitions the wrapped stream <code>source</code> from
     * <code>startIndex</code> up to <code>streamLength</code>. Each substream instance that wraps the same
     * underlying <code>InputStream</code> must share the same mutual exclusion  <code>lock</code> to avoid race
     * conditions from concurrent operations.
     *
     * @param source       The markable InputStream to be wrapped.
     * @param startIndex   A valid index in the wrapped stream where the substream should logically begin.
     * @param streamLength The length of the substream.
     * @param lock         An intrinsic lock to ensure thread-safe, concurrent operations
     *                     on substream instances wrapping the same InputStream.
     */
    public SubStream(InputStream source, long startIndex, long streamLength, Object lock)  {
        if (startIndex < 0 || streamLength < 1) {
            throw new IndexOutOfBoundsException();
        }
        else if (source == null) {
            throw new NullPointerException("Source stream is null.");
        }
        else if (!source.markSupported()) {
            throw new IllegalArgumentException("The source stream to be wrapped must be markable.");
        }

        this.wrappedStream = source;
        this.streamBeginIndex = startIndex;
        this.substreamCurrentIndex = 0;
        this.streamLength = streamLength;
        this.lock = lock;
        this.readBuffer = new byte[Constants.SUBSTREAM_BUFFER_SIZE];
        this.readBufferStream = new ByteArrayInputStream(this.readBuffer);

        // Set empty read buffer to force refresh upon first read.
        this.readBufferLength = 0;

        // By default, mark the beginning of the stream.
        this.markIndex = 0;
        this.readBufferStream.mark(Integer.MAX_VALUE);
    }

    public InputStream getInputStream() {
        return this.wrappedStream;
    }

    public long getLength() {
        return this.streamLength;
    }

    /**
     * Reads the next byte of data from the wrapped stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the substream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     * substream is reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        throw new IOException();
    }

    /**
     * Reads some number of bytes from the wrapped stream and stores them into
     * the buffer array <code>b</code>. The number of bytes actually read is
     * returned as an integer.  This method blocks until input data is
     * available, end of file is detected, or an exception is thrown.
     * <p>
     * <p> If the length of <code>b</code> is zero, then no bytes are read and
     * <code>0</code> is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the substream is at the
     * end of the file, the value <code>-1</code> is returned; otherwise, at
     * least one byte is read and stored into <code>b</code>.
     * <p>
     * <p> The first byte read is stored into element <code>b[0]</code>, the
     * next one into <code>b[1]</code>, and so on. The number of bytes read is,
     * at most, equal to the length of <code>b</code>. Let <i>k</i> be the
     * number of bytes actually read; these bytes will be stored in elements
     * <code>b[0]</code> through <code>b[</code><i>k</i><code>-1]</code>,
     * leaving elements <code>b[</code><i>k</i><code>]</code> through
     * <code>b[b.length-1]</code> unaffected.
     * <p>
     * <p> The <code>read(b)</code> method for class <code>SubStream</code>
     * has the same effect as: <pre><code> read(b, 0, b.length) </code></pre>
     *
     * @param b the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or
     * <code>-1</code> if there is no more data because the end of
     * the stream has been reached.
     * @throws IOException          If the first byte cannot be read for any reason
     *                              other than the end of the file, if the wrapped stream has been closed, or
     *                              if some other I/O error occurs.
     * @throws NullPointerException if <code>b</code> is <code>null</code>.
     * @see SubStream#read(byte[], int, int)
     */
    @Override
    public synchronized int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    /**
     * Reads up to <code>len</code> bytes of data from the substream.  Buffers data from the wrapped stream
     * in order to minimize skip and read overhead. The wrappedstream will only be invoked if the readBuffer
     * cannot fulfil the the read request.
     * In order to ensure valid results, the wrapped stream must be marked prior to reading from the substream.
     * This allows us to reset to the relative substream position in the wrapped stream.
     * The number of bytes actually read is returned as an integer. All these operations are done
     * synchronously within an intrinsic lock to ensure other concurrent requests by substream instances
     * do not result in race conditions.
     * <p>
     * <p> The underlying call to the read of the wrapped stream will blocks until input data
     * is available, end of file is detected, or an exception is thrown.
     * <p>
     * <p> If <code>len</code> is zero, then no bytes are read and
     * <code>0</code> is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the substream is at end of
     * file, the value <code>-1</code> is returned; otherwise, at least one
     * byte is read and stored into <code>b</code>.
     *
     * @param b   the buffer into which the data is read.
     * @param off the start offset in array <code>b</code>
     *            at which the data is written.
     * @param len the maximum number of bytes to read.
     * @return the total number of bytes read into the buffer, or
     * <code>-1</code> if there is no more data because the end of
     * the stream has been reached.
     * @throws IOException               If the first byte cannot be read for any reason
     *                                   other than end of file, or if the wrapped stream has been closed, or if
     *                                   some other I/O error occurs.
     * @throws NullPointerException      If <code>b</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException If <code>off</code> is negative,
     *                                   <code>len</code> is negative, or <code>len</code> is greater than
     *                                   <code>b.length - off</code>
     * @see SubStream#read()
     */
    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }
        else if (len == 0) {
            return 0;
        }

        int bytesRead = -1;
        int readLength = len;

        // Ensure we read within the substream bounds.
        if (this.substreamCurrentIndex + len > this.streamLength) {
            readLength = (int) (this.streamLength - this.substreamCurrentIndex);
        }

        // Read from previously buffered data and only up until the valid bytes available in the buffer.
        int bytesFromBuffer = readBufferStream.read(b, off, Math.min(this.readBufferLength, readLength));
        bytesRead = Math.max(0, bytesFromBuffer);
        this.readBufferLength -= bytesRead;

        // Read request was fully satisfied.
        if (bytesFromBuffer == readLength) {
            this.substreamCurrentIndex += bytesRead;
            return bytesRead;
        }
        else if (bytesFromBuffer < readLength) {
            // Refresh the buffer to fulfil request.
            this.readBufferStream.reset();
            this.readBufferLength = this.readHelper(this.readBuffer, 0, readBuffer.length);
            if (this.readBufferLength == -1) {
                this.readBufferLength = 0;
            }
        }

        // Read the remaining bytes from the read buffer.
        bytesFromBuffer = readBufferStream.read(b, bytesRead + off, Math.min(this.readBufferLength, readLength - bytesRead));

        if (bytesFromBuffer != -1) {
            bytesRead += bytesFromBuffer;
            this.readBufferLength -= bytesFromBuffer;
        }

        this.substreamCurrentIndex += bytesRead;
        return bytesRead;
    }

    private int readHelper(byte[] b, int off, int len) throws IOException {
        synchronized (this.lock) {
            wrappedStream.reset();

            long bytesSkipped = 0;
            byte failSkipCount = 0;

            long streamCurrentIndex = this.streamBeginIndex + this.substreamCurrentIndex;
            // Must be done in a loop as skip may return less than the requested number of bytes.
            do {
                if (failSkipCount > 7) {
                    throw new IOException(SR.STREAM_SKIP_FAILED);
                }

                long skipped = wrappedStream.skip(streamCurrentIndex - bytesSkipped);
                if (skipped == 0) {
                    failSkipCount++;
                }
                else {
                    failSkipCount = 0;
                    bytesSkipped += skipped;
                }
            }
            while (bytesSkipped != streamCurrentIndex);

            return wrappedStream.read(b, off, len);
        }
    }

    /**
     * Advances the current position of the substream by <code>n</code>.
     * The <code>skip</code> method does not invoke the underlying  <code>skip</code> method
     * of the wrapped stream class. The actual skipping of bytes will be accounted for
     * during subsequent substream read operations.
     *
     * @param n the number of bytes to be effectively skipped.
     * @return the actual number of bytes skipped.
     */
    @Override
    public long skip(long n) {
        if (this.substreamCurrentIndex + n > this.streamLength) {
            n = this.streamLength - this.substreamCurrentIndex;
        }

        this.substreamCurrentIndex += n;
        this.readBufferLength = (int) Math.max(0, this.readBufferLength - n);
        return n;
    }

    /**
     * Marks the current position in the substream. A subsequent call to
     * the <code>reset</code> method will reposition the stream to this stored position.
     *
     * @param readlimit the maximum limit of bytes that can be read before
     *                  the mark position becomes invalid.
     * @see SubStream#reset()
     */
    @Override
    public synchronized void mark(int readlimit) {
        this.markIndex = this.substreamCurrentIndex;
    }

    /**
     * Repositions the substream position to the index where the <code>mark</code> method
     * was last called.
     * <p>
     * The new reset position on substream does not take effect until subsequent reads.
     *
     * @see SubStream#mark(int)
     */
    @Override
    public synchronized void reset() {
        this.substreamCurrentIndex = this.markIndex;
    }

    /**
     * The substream wrapper class is only compatible with markable input streams and hence
     * will always return true. This requirement is enforced in the class constructor.
     *
     * @return <code>true</code>
     * @see SubStream#mark(int)
     * @see SubStream#reset()
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Closes the substream.
     */
    @Override
    public void close() throws IOException {
        this.wrappedStream = null;
        this.readBuffer = null;
        this.readBufferStream.close();
        this.readBufferStream = null;
    }
}