package com.azure.perfstress;

import java.nio.ByteBuffer;
import java.util.Random;

import reactor.core.publisher.Flux;

public class RandomFlux {
    private static final byte[] _randomBytes;
    private static final ByteBuffer _randomByteBuffer;

    static {
        _randomBytes = new byte[1024 * 1024];
        (new Random(0)).nextBytes(_randomBytes);
        _randomByteBuffer = ByteBuffer.wrap(_randomBytes).asReadOnlyBuffer();
    }
    
    public static Flux<ByteBuffer> create(int size) {
        return CircularFlux.create(_randomByteBuffer, size);
    }
}
