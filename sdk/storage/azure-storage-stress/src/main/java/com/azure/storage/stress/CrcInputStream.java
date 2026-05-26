// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.stress;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.perf.test.core.RepeatingInputStream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class CrcInputStream extends InputStream {
    private final static ClientLogger LOGGER = new ClientLogger(CrcInputStream.class);
    private final InputStream inputStream;
    private final CRC32 crc = new CRC32();
    private final ByteBuffer head = ByteBuffer.allocate(1024);
    private final boolean markSupported;
    private long markPosition = -1;
    private long length = 0;
    private long size;

    public CrcInputStream(BinaryData source, long size) {
        this.inputStream = new RepeatingInputStream(source, size);
        this.markSupported = true;
        this.size = size;
    }

    public CrcInputStream(InputStream source) {
        this.inputStream = source;
        this.markSupported = source.markSupported();
    }

    @Override
    public synchronized int read() throws IOException {
        int b = inputStream.read();
        if (b < 0) {
            return b;
        }

        crc.update(b);
        if (head.hasRemaining()) {
            head.put((byte) b);
        }
        length++;
        return b;
    }

    @Override
    public synchronized int read(byte buf[], int off, int len) throws IOException {
        int read = inputStream.read(buf, off, len);
        if (read < 0) {
            return read;
        }

        crc.update(buf, off, read);
        if (head.hasRemaining()) {
            head.put(buf, off, Math.min(read, head.remaining()));
        }
        length += read;
        return read;
    }

    @Override
    public synchronized void mark(int readLimit) {
        if (markSupported) {
            inputStream.mark(readLimit);
            markPosition = length;
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        if (markPosition != -1) {
            inputStream.reset();
            length = markPosition; // Reset length to markPosition
            crc.reset(); // Reset CRC32 to recalculate from the markPosition
            head.clear(); // Clear the head buffer
        } else {
            throw new IOException("Mark/reset not supported or mark not set");
        }
    }

    @Override
    public boolean markSupported() {
        return markSupported;
    }

    /**
     * Returns a {@link Mono} that, on subscription, captures a snapshot of the stream's
     * current CRC, byte count and head buffer.
     *
     * <p>The returned Mono is intentionally lazy: it does <strong>not</strong> wait for EOF or
     * for any sink to be signaled. Callers are therefore responsible for subscribing only
     * <em>after</em> the stream has been fully consumed (for example, after the SDK upload
     * call has returned for synchronous flows, or via {@code .then(data.getContentInfo())}
     * for reactive flows). Subscribing before the stream is done will produce a snapshot of
     * whatever has been read so far.</p>
     *
     * <p>This contract avoids the previous design's dependence on the SDK reading past EOF
     * (which never happened on known-length uploads and could leave the legacy sink waiting
     * indefinitely) and naturally tolerates SDK retries: the snapshot reflects the bytes
     * that were actually delivered on the final, successful pass.</p>
     *
     * @return a cold Mono that emits a {@link ContentInfo} snapshot on each subscription.
     */
    public Mono<ContentInfo> getContentInfo() {
        return Mono.fromCallable(() -> {
            synchronized (this) {
                // duplicate() shares the underlying byte[] but gives the caller an independent
                // position/limit so subsequent reads on this stream don't perturb the snapshot.
                return new ContentInfo(crc.getValue(), length, head.duplicate());
            }
        });
    }

    @Override
    public void close() {
        try {
            inputStream.close();
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    public Flux<ByteBuffer> convertStreamToByteBuffer() {
        int blockSize = 4 * 1024 * 1024;
        inputStream.mark(Integer.MAX_VALUE);

        if (size == 0) {
            try {
                if (inputStream.read() != -1) {
                    long totalLength = 1 + inputStream.available();
                    return FluxUtil.fluxError(LOGGER, new UnexpectedLengthException(String.format(
                        "Request body emitted %d bytes, more than the expected %d bytes.", totalLength, size),
                        totalLength, size));
                }
            } catch (IOException e) {
                return FluxUtil.fluxError(LOGGER, new UncheckedIOException(e));
            }
        }

        return Flux.defer(() -> {
            /*
             * If the request needs to be retried, the flux will be resubscribed to. The stream and counter must be
             * reset in order to correctly return the same data again.
             */
            try {
                inputStream.reset();
            } catch (IOException e) {
                return FluxUtil.fluxError(LOGGER, new UncheckedIOException(e));
            }

            // Reset CRC tracking state so resubscriptions (SDK retries or verification)
            // compute the correct checksum from scratch.
            crc.reset();
            length = 0;
            head.clear();

            final long[] currentTotalLength = new long[1];
            return Flux.generate(() -> inputStream, (is, sink) -> {
                long pos = currentTotalLength[0];

                long count = (pos + blockSize) > size ? (size - pos) : blockSize;
                byte[] cache = new byte[(int) count];

                int numOfBytes = 0;
                int offset = 0;
                // Revise the casting if the max allowed network data transmission is over 2G.
                int len = (int) count;

                while (numOfBytes != -1 && offset < count) {
                    try {
                        numOfBytes = inputStream.read(cache, offset, len);
                        if (numOfBytes != -1) {
                            offset += numOfBytes;
                            len -= numOfBytes;
                            currentTotalLength[0] += numOfBytes;
                        }
                    } catch (IOException e) {
                        sink.error(e);
                        return is;
                    }
                }

                if (numOfBytes == -1 && currentTotalLength[0] < size) {
                    sink.error(LOGGER.logExceptionAsError(new UnexpectedLengthException(String.format(
                        "Request body emitted %d bytes, less than the expected %d bytes.",
                        currentTotalLength[0], size), currentTotalLength[0], size)));
                    return is;
                }

                // Validate that stream isn't longer.
                if (currentTotalLength[0] >= size) {
                    try {
                        if (inputStream.read() != -1) {
                            long totalLength = 1 + currentTotalLength[0] + inputStream.available();
                            sink.error(LOGGER.logExceptionAsError(new UnexpectedLengthException(
                                String.format("Request body emitted %d bytes, more than the expected %d bytes.",
                                    totalLength, size), totalLength, size)));
                            return is;
                        } else if (currentTotalLength[0] > size) {
                            sink.error(LOGGER.logExceptionAsError(new IllegalStateException(
                                String.format("Read more data than was requested. Size of data read: %d. Size of data"
                                    + " requested: %d", currentTotalLength[0], size))));
                            return is;
                        }
                    } catch (IOException e) {
                        sink.error(LOGGER.logExceptionAsError(new RuntimeException("I/O errors occurred", e)));
                        return is;
                    }
                }

                sink.next(ByteBuffer.wrap(cache, 0, offset));
                if (currentTotalLength[0] == size) {
                    sink.complete();
                }
                return is;
            });
        });
    }
}
