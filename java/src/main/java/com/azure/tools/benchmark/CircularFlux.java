package com.azure.tools.benchmark;


import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

public class CircularFlux {

    public static Flux<ByteBuffer> create(ByteBuffer byteBuffer, long size) {

        int remaining = byteBuffer.remaining();



        int quotient = (int) size / remaining;

        int remainder = (int) size % remaining;



        return Flux.range(0, quotient)

                .map(i -> byteBuffer.duplicate())

                .concatWithValues((ByteBuffer) byteBuffer.duplicate().limit(remainder));
    }

}