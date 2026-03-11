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
    private final static ClientLogger LOGGER = new ClientLogger(CrcOutputStream.class);

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
    public void close() throws IOException {
        Sinks.EmitResult emitResult = sink.tryEmitValue(new ContentInfo(crc.getValue(), length, head));

        switch (emitResult) {
            case OK:
                break;
            case FAIL_TERMINATED:
                throw LOGGER.logExceptionAsError(new RuntimeException("Sink was terminated before emitting content: " +
                    "info: " + emitResult));
            case FAIL_CANCELLED:
                throw LOGGER.logExceptionAsError(new RuntimeException("The subscriber cancelled before the value was " +
                    "emitted: " + emitResult));
            case FAIL_OVERFLOW:
                throw LOGGER.logExceptionAsError(new RuntimeException("Buffer full: " + emitResult));
            case FAIL_NON_SERIALIZED:
                throw LOGGER.logExceptionAsError(new RuntimeException("Two threads call emit at once: " + emitResult));
            case FAIL_ZERO_SUBSCRIBER:
                throw LOGGER.logExceptionAsError(new RuntimeException("Sink requires a subscriber: " + emitResult));
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException("Unknown emit result: " + emitResult));
        }
        super.close();
    }

    public Mono<ContentInfo> getContentInfo() {
        return sink.asMono();
    }
}
