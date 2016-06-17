/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import com.google.common.io.CountingOutputStream;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Unit tests that target the {@link UploadMetadataGenerator} class
 */
public class UploadMetadataGeneratorTests {
    private static final int MaxAppendLength = 4 * 1024 * 1024;
    private static final byte[] NewLine = "\r\n".getBytes();
    private static final List<? extends Number> FileLengthsMB = Arrays.asList(2, 4, 10, 14.123456, 20.123456, 23.456789, 30.987654, 37.897643, 50.546213, 53.456789, 123.456789 );

    @Test
    public void UploadMetadataGenerator_AlignSegmentsToRecordBoundaries() throws IOException, UploadFailedException, InvalidMetadataException {
        //We keep creating a file, by appending a number of bytes to it (taken from FileLengthsInMB). 
        //At each iteration, we append a new blob of data, and then run the whole test on the entire file
        Random rnd = new Random(0);
        File folderPath = new File(MessageFormat.format("{0}\\uploadtest", new File(".").getAbsoluteFile()));
        File filePath = new File(folderPath, "verifymetadata.txt");
        try
        {
            if (!folderPath.exists())
            {
                folderPath.mkdirs();
            }

            if (filePath.exists())
            {
                filePath.delete();
            }

            for (Number lengthMB: FileLengthsMB)
            {
                int appendLength = (int)(lengthMB.doubleValue()*1024*1024);
                AppendToFile(filePath.getAbsolutePath(), appendLength, rnd, 0, MaxAppendLength);
                String metadataFilePath = filePath + ".metadata.txt";

                UploadParameters up = new UploadParameters(filePath.getAbsolutePath(), filePath.getAbsolutePath(), null, 1, false, false, false, 4*1024*1024, null);
                UploadMetadataGenerator mg = new UploadMetadataGenerator(up, MaxAppendLength);
                UploadMetadata metadata = mg.createNewMetadata(metadataFilePath);

                VerifySegmentsAreOnRecordBoundaries(metadata, filePath.getAbsolutePath());
            }
        }
        finally
        {
            if (folderPath.exists())
            {
                FileUtils.deleteQuietly(folderPath);
            }
        }
    }

    @Test
    public void UploadMetadataGenerator_AlignSegmentsToRecordBoundariesTooLargeRecord() throws IOException {
        //We keep creating a file, by appending a number of bytes to it (taken from FileLengthsInMB). 
        //At each iteration, we append a new blob of data, and then run the whole test on the entire file
        Random rnd = new Random(0);
        File folderPath = new File(MessageFormat.format("{0}\\uploadtest", new File(".").getAbsolutePath()));
        File filePath = new File(folderPath, "verifymetadata.txt");
        try
        {
            if (!folderPath.exists())
            {
                folderPath.mkdirs();
            }

            if (filePath.exists())
            {
                filePath.delete();
            }
            for (Number lengthMB: FileLengthsMB)
            {
                if(lengthMB.intValue() > MaxAppendLength) {
                    int length = lengthMB.intValue() * 1024 * 1024;
                    AppendToFile(filePath.getAbsolutePath(), length, rnd, MaxAppendLength + 1, MaxAppendLength + 10);
                    String metadataFilePath = filePath + ".metadata.txt";

                    UploadParameters up = new UploadParameters(filePath.getAbsolutePath(), filePath.getAbsolutePath(), null, 1, false, false, false, 4 * 1024 * 1024, null);
                    UploadMetadataGenerator mg = new UploadMetadataGenerator(up, MaxAppendLength);

                    try {
                        mg.createNewMetadata(metadataFilePath);
                        Assert.assertTrue("Method createNewMetadata should fail due to record boundaries being being too large for the record, but didn't", false);
                    }
                    catch(Exception e) {
                        // do nothing, expected
                    }
                }
            }
        }
        finally
        {
            if (folderPath.exists())
            {
                FileUtils.deleteQuietly(folderPath);
            }
        }
    }

    private void VerifySegmentsAreOnRecordBoundaries(UploadMetadata metadata, String filePath) throws IOException {
        try(RandomAccessFile stream = new RandomAccessFile(filePath, "r"))
        {
            for (UploadSegmentMetadata segment: metadata.getSegments())
            {
                if (segment.getSegmentNumber() > 0)
                {
                    //verify that each segment starts with a non-newline and that the 2 previous characters before that offset are newline characters

                    //2 characters behind: newline
                    // always seek from the file origin
                    stream.seek(0);
                    stream.seek(segment.getOffset() - 2);
                    char c1 = (char)stream.read();
                    Assert.assertTrue(MessageFormat.format("Expecting a newline at offset {0}", segment.getOffset() - 2), IsNewline(c1));

                    //1 character behind: newline
                    char c2 = (char)stream.read();
                    Assert.assertTrue(MessageFormat.format("Expecting a newline at offset {0}", segment.getOffset() - 2), IsNewline(c2));

                    //by test design, we never have two consecutive newlines that are the same; we'd always have \r\n, but never \r\r or \r\n
                    char c3 = (char)stream.read();
                    Assert.assertNotEquals(c2, c3);
                }
            }
        }
    }

    private boolean IsNewline(char c)
    {
        return c == '\r' || c == '\n';
    }

    private String AppendToFile(String filePath, int length, Random random, int minRecordLength, int maxRecordLength) throws IOException {
        try (CountingOutputStream stream =  new CountingOutputStream(new FileOutputStream(filePath)))
        {
            int newLength = (int) (new File(filePath).length() + length);
            while (true)
            {
                int recordLength = minRecordLength + random.nextInt(maxRecordLength - minRecordLength);
                if (stream.getCount() + recordLength + NewLine.length > newLength)
                {
                    recordLength = newLength - NewLine.length - (int)stream.getCount();
                    if (recordLength < 0)
                    {
                        stream.write(NewLine, 0, NewLine.length);
                        break;
                    }
                }
                WriteRecord(stream, recordLength);
                stream.write(NewLine, 0, NewLine.length);
            }
        }

        return filePath;
    }

    private void WriteRecord(CountingOutputStream stream, int count) throws IOException {
        byte[] record = new byte[count];
        for (int i = 0; i < count; i++)
        {
            record[i] = (byte)('a' + i % 25);
        }
        stream.write(record, 0, record.length);
    }
}
