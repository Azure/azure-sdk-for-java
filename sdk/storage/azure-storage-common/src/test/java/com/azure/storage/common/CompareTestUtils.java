// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.storage.common.implementation.Constants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class CompareTestUtils {
    /**
     * Compares two files for having equivalent content.
     *
     * @param file1
     *     File used to upload data to the service
     * @param file2
     *     File used to download data from the service
     * @param offset
     *     Write offset from the upload file
     * @param count
     *     Size of the download from the service
     *
     * @return Whether the files have equivalent content based on offset and read count
     */
    public static Boolean compareFiles(File file1, File file2, long offset, long count) throws IOException {
        Long pos = 0L;
        int readBuffer = 8 * Constants.KB;
        FileInputStream stream1 = new FileInputStream(file1);
        stream1.skip(offset);
        FileInputStream stream2 = new FileInputStream(file2);

        try {
            while (pos < count) {
                Integer bufferSize = (int) Math.min(readBuffer, count - pos);
                byte[] buffer1 = new byte[bufferSize];
                byte[] buffer2 = new byte[bufferSize];

                int readCount1 = stream1.read(buffer1);
                int readCount2 = stream2.read(buffer2);

                assert readCount1 == readCount2 && Arrays.equals(buffer1, buffer2);

                pos += bufferSize;
            }


            int verificationRead = stream2.read();
            return pos == count && verificationRead == -1;
        } finally {
            stream1.close();
            stream2.close();
        }

    }

    public static byte[] convertInputStreamToByteArray(InputStream inputStream) {
        int b;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            while ((b = inputStream.read()) != -1) {
                outputStream.write(b);
            }

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }


        return outputStream.toByteArray();
    }

    public static Boolean compareListToBuffer(List<ByteBuffer> buffers, ByteBuffer result) {
        result.position(0);
        for (ByteBuffer buffer : buffers) {
            buffer.position(0);
            result.limit(result.position() + buffer.remaining());
            if (!buffer.equals(result)) {
                return false;
            }

            result.position(result.position() + buffer.remaining());
        }

        return result.remaining() == 0;
    }

}
