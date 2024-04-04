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
                        while (head.hasRemaining() && dup.hasRemaining()) {
                            head.put(dup.get());
                        }
                    }
                    crc.update(bb);
                    return crc;
                })
            .map(CRC32::getValue);
//        Mono<Long> crcMono = data
//            .reduce(new CRC32(),
//                (crc, bb) -> {
//                    int remaining = bb.remaining();
//                    length.getAndAdd(remaining);
//
//                    if (head.hasRemaining()) {
//                        // Calculate how much of the ByteBuffer we can read (the minimum of what's left in 'head' and 'bb')
//                        int toRead = Math.min(head.remaining(), remaining);
//                        // Create a temporary array to hold the data
//                        byte[] temp = new byte[toRead];
//                        // Copy data from 'bb' to 'temp'
//                        bb.get(temp, 0, toRead);
//                        // Then put that data into 'head'
//                        head.put(temp);
//                    }
//
//                    // Update the CRC with the entire buffer
//                    // Note: The CRC32.update() method requires a rewind of the buffer if we read from it
////                    bb.rewind(); // Rewind so the CRC calculation includes all data read from this ByteBuffer
//                    bb.position(0);
//                    byte[] crcTemp = new byte[remaining];
//                    bb.get(crcTemp);
//                    crc.update(crcTemp, 0, remaining);
//
//                    return crc;
//                })
//            .map(CRC32::getValue);

        return crcMono.map(crc -> new ContentInfo(crc, length.get(), head));
    }

    public static ContentInfo fromBinaryData(BinaryData data) {
        // Convert BinaryData to ByteBuffer for consistency with the original method
        ByteBuffer bb = data.toByteBuffer();
        long length = bb.remaining();
        ByteBuffer head = ByteBuffer.allocate(1024);
        // Prepare the CRC computation
        CRC32 crc = new CRC32();
        if (length > 0) {
            if (head.remaining() > 0) {
                // Calculate how much of the ByteBuffer we can read (the minimum of what's left in 'head' and 'bb')
                int toRead = Math.min(head.remaining(), bb.remaining());
                byte[] temp = new byte[toRead];
                // Copy data from 'bb' to 'temp'
                ByteBuffer dup = bb.duplicate();
                dup.get(temp); // no need to pass 0 and toRead as that is implicit with the byte[] API
                // Then put that data into 'head'
                head.put(temp);
            }
            // Update the CRC with the entire buffer
            // Since we've already read from the buffer, rewind before updating CRC to cover all data
            crc.update(bb);
        }

        // Directly return the ContentInfo object since we're working synchronously
        return new ContentInfo(crc.getValue(), length, head);
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
