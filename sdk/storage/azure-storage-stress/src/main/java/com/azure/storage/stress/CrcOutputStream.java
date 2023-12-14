package com.azure.storage.stress;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.zip.CRC32;

public class CrcOutputStream extends OutputStream {
    private final Sinks.One<ContentInfo> sink = Sinks.one();
    private final CRC32 crc = new CRC32();
    private long length = 0;
    private final byte[] head = new byte[1024];
    @Override
    public synchronized void write(int b) {
        crc.update(b);
        if (length < head.length) {
            head[(int)length] = (byte)b;
        }
        length ++;
    }

    public synchronized void write(byte b[], int off, int len) {
        crc.update(b, off, len);
        if (length < head.length) {
            System.arraycopy(b, off, head, (int)length, Math.min(len, head.length - (int)length));
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
