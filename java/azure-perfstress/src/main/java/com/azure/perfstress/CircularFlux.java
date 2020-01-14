package com.azure.perfstress;

import java.nio.ByteBuffer;

import reactor.core.publisher.Flux;

public class CircularFlux {
    public static Flux<ByteBuffer> create(ByteBuffer byteBuffer, long size) {
        int remaining = byteBuffer.remaining();
        
        int quotient = (int) size / remaining;
        int remainder = (int) size % remaining;

        return Flux.range(0, quotient)
            .map(i -> byteBuffer.duplicate())
            .concatWithValues(byteBuffer.duplicate().limit(remainder));
    }
}
