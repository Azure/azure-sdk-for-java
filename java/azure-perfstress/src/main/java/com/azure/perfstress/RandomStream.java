package com.azure.perfstress;

import java.io.InputStream;
import java.util.Random;

public class RandomStream {
    private static final byte[] _randomBytes;

    static {
        _randomBytes = new byte[1024 * 1024];
        (new Random(0)).nextBytes(_randomBytes);
    }
    
    public static InputStream create(long size) {
        return CircularStream.create(_randomBytes, size);
    }
}
