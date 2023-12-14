package com.azure.storage.stress;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.CRC32;

public class ContentInfo {
    private final long length;
    private final long crc;
    private final ByteBuffer head;

    public ContentInfo(long crc, long length, ByteBuffer head) {
        this.crc = crc;
        this.length = length;
        this.head = head;
    }

    public static Mono<ContentInfo> fromFluxByteBuffer(Flux<ByteBuffer> data) {
        AtomicLong length = new AtomicLong(0);
        ByteBuffer head = ByteBuffer.allocate(1024);
        Mono<Long> crcMono = data
                .reduce(new CRC32(),
                        (crc, bb) -> {
                            length.getAndAdd(bb.remaining());
                            if (head.hasRemaining())
                            {
                                ByteBuffer dup = bb.duplicate();
                                while (head.hasRemaining()) {
                                    head.put(dup.get());
                                }
                            }
                            crc.update(bb);
                            return crc;
                        })
                .map(CRC32::getValue);

        return crcMono.map(crc -> new ContentInfo(crc, length.get(), head));
    }

    public long getLength() {
        return length;
    }

    public long getCrc() {
        return crc;
    }

    public ByteBuffer getHead() {
        return head;
    }
}
