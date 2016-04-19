/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class TestHelpers {
    /**
     * Generates some random data and writes it out to a temp file and to an in-memory array
     *
     * @param contents The array to write random data to (the length of this array will be the size of the file).
     * @return The path of the file that will be created.
     * @throws IOException
     */
    static String GenerateFileData(byte[] contents) throws IOException {
        File filePath = File.createTempFile("adlUploader", "test.data");

        Random rnd = new Random(0);
        rnd.nextBytes(contents);
        if (filePath.exists())
        {
            filePath.delete();
        }

        FileOutputStream writer = new FileOutputStream(filePath);
        writer.write(contents);
        writer.flush();
        writer.close();
        return filePath.toString();
    }

    /**
     * Generates some random data and writes it out to a temp file and to an in-memory array
     *
     * @param contents The array to write random data to (the length of this array will be the size of the file).
     * @param minRecordLength The minimum amount of data to write (inclusive)
     * @param maxRecordLength The maximum amount of data to write (exclusive)
     * @return The path of the file that will be created.
     * @throws IOException
     */
    static String GenerateTextFileData(byte[] contents, int minRecordLength, int maxRecordLength) throws IOException {
        File filePath = File.createTempFile("adlUploader", "test.data");
        int offset = 0;
        while (offset < contents.length)
        {
            int recordLength = minRecordLength + (int)(Math.random()*((maxRecordLength - minRecordLength) + 1));
            recordLength = Math.min(recordLength, contents.length - offset - 2);

            int recordEndPos = offset + recordLength;
            while (offset < recordEndPos)
            {
                contents[offset] = (byte)((int)'a' + (int)(Math.random()*(((int)'z' - (int)'a') + 1)));
                offset++;
            }
            contents[offset++] = (byte)'\r';
            contents[offset++] = (byte)'\n';
        }
        if (filePath.exists())
        {
            filePath.delete();
        }

        FileOutputStream writer = new FileOutputStream(filePath);
        writer.write(contents);
        writer.flush();
        writer.close();
        return filePath.toString();
    }
}
