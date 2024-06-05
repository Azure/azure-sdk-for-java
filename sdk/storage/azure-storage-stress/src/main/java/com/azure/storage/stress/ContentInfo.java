// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.stress;

import com.azure.core.util.BinaryData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.CRC32;

public class ContentInfo {
    private final long length;
    private final long crc;
    private final ByteBuffer head;

    ContentInfo(long crc, long length, ByteBuffer head) {
        this.crc = crc;
        this.length = length;
        this.head = head;
    }

    // Helper method to update the CRC, length, and head
    private static void processBuffer(ByteBuffer bb, CRC32 crc, AtomicLong length, ByteBuffer head) {
        int remaining = bb.remaining();
        length.getAndAdd(remaining);

        if (head.hasRemaining()) {
            int toRead = Math.min(head.remaining(), remaining);
            byte[] temp = new byte[toRead];
            // Read bytes from bb into temp
            bb.get(temp, 0, toRead);
            head.put(temp);
        }

        // Rewind the buffer to update CRC with all data
        bb.rewind();
        byte[] crcTemp = new byte[bb.remaining()];
        bb.get(crcTemp);
        crc.update(crcTemp, 0, crcTemp.length);
    }

    public static Mono<ContentInfo> fromFluxByteBuffer(Flux<ByteBuffer> data) {
        AtomicLong length = new AtomicLong(0);
        CRC32 crc = new CRC32();
        ByteBuffer head = ByteBuffer.allocate(1024);

        return data
            .doOnNext(bb -> processBuffer(bb.duplicate(), crc, length, head))
            .then(Mono.fromCallable(() -> new ContentInfo(crc.getValue(), length.get(), (ByteBuffer) head.flip())));
    }

    public static ContentInfo fromBinaryData(BinaryData data) {
        ByteBuffer bb = data.toByteBuffer();
        AtomicLong length = new AtomicLong(0);
        CRC32 crc = new CRC32();
        ByteBuffer head = ByteBuffer.allocate(1024);

        processBuffer(bb, crc, length, head);

        return new ContentInfo(crc.getValue(), length.get(), (ByteBuffer) head.flip());
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
