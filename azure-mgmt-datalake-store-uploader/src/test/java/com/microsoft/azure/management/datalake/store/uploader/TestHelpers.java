package com.microsoft.azure.management.datalake.store.uploader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created by begoldsm on 4/14/2016.
 */
public class TestHelpers {
    /// <summary>
    /// Generates some random data and writes it out to a temp file and to an in-memory array
    /// </summary>
    /// <param name="contents">The array to write random data to (the length of this array will be the size of the file).</param>
    /// <param name="filePath">This will contain the path of the file that will be created.</param>
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

    /// <summary>
    /// Generates some random data and writes it out to a temp file and to an in-memory array
    /// </summary>
    /// <param name="contents">The array to write random data to (the length of this array will be the size of the file).</param>
    /// <param name="filePath">This will contain the path of the file that will be created.</param>
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
