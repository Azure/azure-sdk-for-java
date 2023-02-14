package com.azure.storage.common.test.shared

import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel

class TestUtility {
    /**
     * Copies the InputStream contents to the destination byte channel.
     * @param src Bytes source.
     * @param dst Bytes destination.
     * @param copySize Size of array to copy contents with.
     * @return Total number of bytes read from src.
     */
    static int copy(InputStream src, SeekableByteChannel dst, int copySize) {
        int read
        int totalRead = 0
        def temp = new byte[copySize]
        while ((read = src.read(temp)) != -1) {
            totalRead += read
            dst.write(ByteBuffer.wrap(temp, 0, read))
        }
        return totalRead
    }
}
