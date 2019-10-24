package com.azure.perfstress;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Random;

public class RandomStream {
    private static final byte[] _randomBytes;

    static {
        // _randomBytes = new byte[1024 * 1024];
        _randomBytes = new byte[Integer.MAX_VALUE];
        (new Random(0)).nextBytes(_randomBytes);
    }
    
    public static InputStream create(long size) {
        // Workaround for Azure/azure-sdk-for-java#6020
        // return CircularStream.create(_randomBytes, size);
        return new ByteArrayInputStream(_randomBytes, 0, (int)size);
    }
}
