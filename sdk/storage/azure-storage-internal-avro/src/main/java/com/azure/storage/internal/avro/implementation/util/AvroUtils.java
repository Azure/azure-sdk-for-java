package com.azure.storage.internal.avro.implementation.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class AvroUtils {
    /**
     * Converts a List of ByteBuffers into a byte array.
     * @param bytes The buffers to convert.
     * @return The byte array.
     */
    public static byte[] getBytes(List<ByteBuffer> bytes) {
        List<Byte> bytesArr = new ArrayList<>();
        bytes.forEach(i -> {
            while (i.hasRemaining()) {
                bytesArr.add(i.get());
            }
        });

        byte[] ret = new byte[bytesArr.size()];
        for (int i = 0; i < bytesArr.size(); i++) {
            ret [i] = bytesArr.get(i);
        }

        return ret;
    }
}
