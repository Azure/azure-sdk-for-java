package com.azure.storage.stress;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class CrcOutputStream extends OutputStream {
    private final Sinks.One<ContentInfo> sink = Sinks.one();
    private final CRC32 crc = new CRC32();
    private long length = 0;
    private final ByteBuffer head = ByteBuffer.allocate(1024);
    private static final ClientLogger LOGGER = new ClientLogger(CrcOutputStream.class);

    @Override
    public synchronized void write(int b) {
        crc.update(b);
        if (head.hasRemaining()) {
            head.put((byte) b);
        }
        length++;
    }

    public synchronized void write(byte[] buf, int off, int len) {
        crc.update(buf, off, len);
        if (head.hasRemaining()) {
            head.put(buf, off, Math.min(len, head.remaining()));
        }
        length += len;
    }

    // Uses tryEmitValue so that double-close (e.g. explicit close + try-with-resources)
    // doesn't throw on the second call.
    @Override
    public synchronized void close() throws IOException {
        String baseErrorMessage = "Failed to emit content because";
        Sinks.EmitResult emitResult = sink.tryEmitValue(new ContentInfo(crc.getValue(), length, head));
        switch (emitResult) {
            case OK:
            case FAIL_TERMINATED:
                // Expected successful outcomes; nothing further to do.
                break;
            case FAIL_CANCELLED:
                throw LOGGER.logExceptionAsError(new RuntimeException(baseErrorMessage
                    + " the sink was previously interrupted by its consumer: " + emitResult));
            case FAIL_OVERFLOW:
                throw LOGGER.logExceptionAsError(new RuntimeException(baseErrorMessage
                    + " the buffer is full: " + emitResult));
            case FAIL_NON_SERIALIZED:
                throw LOGGER.logExceptionAsError(new RuntimeException(baseErrorMessage
                    + " two threads called emit at once: " + emitResult));
            case FAIL_ZERO_SUBSCRIBER:
                throw LOGGER.logExceptionAsError(new RuntimeException(baseErrorMessage
                    + " the sink requires a subscriber: " + emitResult));
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(baseErrorMessage
                    + " an unexpected emit result was returned: " + emitResult));
        }
        super.close();
    }

    public Mono<ContentInfo> getContentInfo() {
        return sink.asMono();
    }
}
