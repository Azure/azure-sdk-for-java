package com.azure.storage.stress;

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
    @Override
    public synchronized void write(int b) {
        crc.update(b);
        if (head.hasRemaining()) {
            head.put((byte)b);
        }
        length ++;
    }

    public synchronized void write(byte buf[], int off, int len) {
        crc.update(buf, off, len);
        if (head.hasRemaining()) {
            head.put(buf, off, Math.min(len, head.remaining()));
        }
        length += len;
    }

    @Override
    public void close() throws IOException {
        sink.emitValue(new ContentInfo(crc.getValue(), length, head), Sinks.EmitFailureHandler.FAIL_FAST);
        super.close();
    }

    public Mono<ContentInfo> getContentInfo() {
        return sink.asMono();
    }
}
