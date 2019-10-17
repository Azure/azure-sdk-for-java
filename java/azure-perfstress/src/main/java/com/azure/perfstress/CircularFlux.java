package com.azure.perfstress;

import java.nio.ByteBuffer;

import reactor.core.publisher.Flux;

public class CircularFlux {
    public static Flux<ByteBuffer> create(ByteBuffer byteBuffer, int size) {
        int remaining = byteBuffer.remaining();
        
        int quotient = size / remaining;
        int remainder = size % remaining;

        return Flux.range(0, quotient)
            .map(i -> byteBuffer.duplicate())
            .concatWithValues(byteBuffer.duplicate().limit(remainder));
    }
}
