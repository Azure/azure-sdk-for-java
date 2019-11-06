package com.azure.tools.benchmark;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Random;

public class RandomStream {

    private static final int _size = (1024 * 1024 * 1024) + 1;

    private static final byte[] _randomBytes;



    static {

        // _randomBytes = new byte[1024 * 1024];

        _randomBytes = new byte[_size];

        (new Random(0)).nextBytes(_randomBytes);

    }



    public static InputStream create(long size) {

        if (size > _size) {
            throw new RuntimeException("size must be <= " + _size);
        }

        // Workaround for Azure/azure-sdk-for-java#6020
        // return CircularStream.create(_randomBytes, size);
        return new ByteArrayInputStream(_randomBytes, 0, (int)size);
    }

}
