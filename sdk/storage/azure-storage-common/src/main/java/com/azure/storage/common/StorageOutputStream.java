// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;

/**
 * StorageOutputStream allows for uploading data to an Azure Storage service using stream concepts.
 */
public abstract class StorageOutputStream extends OutputStream {
    final ClientLogger logger = new ClientLogger(StorageOutputStream.class);

    /*
     * Holds the write threshold of number of bytes to buffer prior to dispatching a write. For block blob this is the
     * block size, for page blob this is the Page commit size.
     */
    private final int writeThreshold;

    /*
     * Holds the last exception this stream encountered.
     */
    protected volatile IOException lastError;

    protected abstract Mono<Void> dispatchWrite(byte[] data, int writeLength, long offset);

    protected StorageOutputStream(final int writeThreshold) {
        this.writeThreshold = writeThreshold;
    }

    /**
     * Writes the data to the buffer and triggers writes to the service as needed.
     *
     * @param data A <code>byte</code> array which represents the data to write.
     * @param offset An <code>int</code> which represents the start offset in the data.
     * @param length An <code>int</code> which represents the number of bytes to write.
     */
    private void writeInternal(final byte[] data, int offset, int length) {
        int chunks = (int) (Math.ceil((double) length / (double) this.writeThreshold));
        Flux.range(0, chunks).map(c -> offset + c * this.writeThreshold)
            .concatMap(pos -> processChunk(data, pos, offset, length))
            .then()
            .block();
    }

    private Mono<Void> processChunk(byte[] data, int position, int offset, int length) {
        int chunkLength = this.writeThreshold;

        if (position + chunkLength > offset + length) {
            chunkLength = offset + length - position;
        }

        // Flux<ByteBuffer> chunkData = new ByteBufferStreamFromByteArray(data, writeThreshold, position, chunkLength);
        return dispatchWrite(data, chunkLength, position)
            .doOnError(t -> {
                if (t instanceof IOException) {
                    lastError = (IOException) t;
                } else {
                    lastError = new IOException(t);
                }
            });
    }

    /**
     * Helper function to check if the stream is faulted, if it is it surfaces the exception.
     *
     * @throws RuntimeException If an I/O error occurs. In particular, an IOException may be thrown
     * if the output stream has been closed.
     */
    protected void checkStreamState()  {
        if (this.lastError != null) {
            throw logger.logExceptionAsError(new RuntimeException(this.lastError.getMessage()));
        }
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out. If any data remains in the
     * buffer it is committed to the service.
     */
    @Override
    public void flush() {
        this.checkStreamState();
    }

    /**
     * Writes <code>b.length</code> bytes from the specified byte array to this output stream.
     * <p>
     *
     * @param data A <code>byte</code> array which represents the data to write.
     */
    @Override
    public void write(@NonNull final byte[] data) {
        this.write(data, 0, data.length);
    }

    /**
     * Writes length bytes from the specified byte array starting at offset to this output stream.
     * <p>
     *
     * @param data A <code>byte</code> array which represents the data to write.
     * @param offset An <code>int</code> which represents the start offset in the data.
     * @param length An <code>int</code> which represents the number of bytes to write.
     * @throws IndexOutOfBoundsException when access the bytes out of the bound.
     */
    @Override
    public void write(@NonNull final byte[] data, final int offset, final int length) {
        if (offset < 0 || length < 0 || length > data.length - offset) {
            throw logger.logExceptionAsError(new IndexOutOfBoundsException());
        }

        this.writeInternal(data, offset, length);
    }

    /**
     * Writes the specified byte to this output stream. The general contract for write is that one byte is written to
     * the output stream. The byte to be written is the eight low-order bits of the argument b. The 24 high-order bits
     * of b are ignored.
     * <p>
     * <code>true</code> is acceptable for you.
     *
     * @param byteVal An <code>int</code> which represents the bye value to write.
     */
    @Override
    public void write(final int byteVal) {
        this.write(new byte[]{(byte) (byteVal & 0xFF)});
    }

    /**
     * Closes this output stream and releases any system resources associated with this stream. If any data remains in
     * the buffer it is committed to the service.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public synchronized void close() throws IOException {
        try {
            // if the user has already closed the stream, this will throw a STREAM_CLOSED exception
            // if an exception was thrown by any thread in the threadExecutor, realize it now
            this.checkStreamState();

            // flush any remaining data
            this.flush();
        } finally {
            // if close() is called again, an exception will be thrown
            this.lastError = new IOException(Constants.STREAM_CLOSED);
        }
    }

}
