/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Random;

/**
 * Represents a class of unit tests targeting the {@link MultipleSegmentUploader}
 */
public class MultipleSegmentUploaderTests {
    private static  byte[] _smallFileContents = new byte[10 * 1024]; //10KB file
    private static String _smallFilePath;

    @BeforeClass
    public static void Setup() throws IOException {
        _smallFilePath = GenerateFileData(_smallFileContents);
    }

    private static String GenerateFileData(byte[] contents) throws IOException {
        File tempFile = File.createTempFile("adlmsu", ".data");

        Random rnd = new Random(0);
        rnd.nextBytes(contents);
        Assert.assertTrue("The temp file at the following path was not created: " + tempFile.getAbsolutePath(), tempFile.exists());

        try (FileOutputStream stream = new FileOutputStream(tempFile)) {
            stream.write(contents);
        }

        return tempFile.getAbsolutePath();
    }

    @AfterClass
    public static void Teardown()
    {
        File smallFile = new File(_smallFilePath);
        if (smallFile.exists())
        {
            smallFile.delete();
        }
    }

    /**
     * Tests an uneventful upload from scratch made of 1 segment.
     *
     * @throws Exception
     */
    @Test
    public void MultipleSegmentUploader_OneSegment() throws Exception {
        InMemoryFrontEnd fe = new InMemoryFrontEnd();
        UploadMetadata metadata = CreateMetadata(1);
        try
        {
            MultipleSegmentUploader msu = new MultipleSegmentUploader(metadata, 1, fe);
            msu.UseSegmentBlockBackOffRetryStrategy = false;
            msu.Upload();
            VerifyTargetStreamsAreComplete(metadata, fe);
        }
        finally
        {
            metadata.DeleteFile();
        }
    }

    /**
     * Tests an uneventful upload from scratch made of several segments
     *
     * @throws Exception
     */
    @Test
    public void MultipleSegmentUploader_MultipleSegments() throws Exception
    {
        InMemoryFrontEnd fe = new InMemoryFrontEnd();
        UploadMetadata metadata = CreateMetadata(10);
        try
        {
            MultipleSegmentUploader msu = new MultipleSegmentUploader(metadata, 1, fe);
            msu.UseSegmentBlockBackOffRetryStrategy = false;
            msu.Upload();
            VerifyTargetStreamsAreComplete(metadata, fe);
        }
        finally
        {
            metadata.DeleteFile();
        }
    }

    /**
     * Tests an uneventful upload from scratch made of several segments
     *
     * @throws Exception
     */
    @Test
    public void MultipleSegmentUploader_MultipleSegmentsAndMultipleThreads() throws Exception
    {
        InMemoryFrontEnd fe = new InMemoryFrontEnd();
        UploadMetadata metadata = CreateMetadata(10);
        int threadCount = metadata.SegmentCount * 10; //intentionally setting this higher than the # of segments
        try
        {
            MultipleSegmentUploader msu = new MultipleSegmentUploader(metadata, threadCount, fe);
            msu.UseSegmentBlockBackOffRetryStrategy = false;
            msu.Upload();
            VerifyTargetStreamsAreComplete(metadata, fe);
        }
        finally
        {
            metadata.DeleteFile();
        }
    }

    /**
     * Tests an uneventful upload from resume made of several segments
     *
     * @throws Exception
     */
    @Test
    public void MultipleSegmentUploader_ResumedUploadWithMultipleSegments() throws Exception
    {
        //the strategy here is to upload everything, then delete a set of the segments, and verify that a resume will pick up the slack

        InMemoryFrontEnd fe = new InMemoryFrontEnd();
        UploadMetadata metadata = CreateMetadata(10);

        try
        {
            MultipleSegmentUploader msu = new MultipleSegmentUploader(metadata, 1, fe);
            msu.UseSegmentBlockBackOffRetryStrategy = false;
            msu.Upload();
            VerifyTargetStreamsAreComplete(metadata, fe);

            //delete about 50% of segments
            for (int i = 0; i < metadata.SegmentCount; i++)
            {
                UploadSegmentMetadata currentSegment = metadata.Segments[i];
                if (i % 2 == 0)
                {
                    currentSegment.Status = SegmentUploadStatus.Pending;
                    fe.DeleteStream(currentSegment.Path, false);
                }
            }

            //re-upload everything
            msu = new MultipleSegmentUploader(metadata, 1, fe);
            msu.Upload();
            VerifyTargetStreamsAreComplete(metadata, fe);
        }
        finally
        {
            metadata.DeleteFile();
        }
    }

    /**
     * Tests an upload made of several segments, where
     *  some fail a couple of times => upload can finish.
     *  some fail too many times => upload will not finish
     *
     * @throws Exception
     */
    @Test
    public void MultipleSegmentUploader_SegmentInstability() throws Exception
    {
        TestRetry(0);
        TestRetry(1);
        TestRetry(2);
        TestRetry(3);
        TestRetry(4);
        TestRetry(5);
    }

    private void TestRetry(int segmentFailCount) throws Exception
    {
        //we only have access to the underlying FrontEnd, so we need to simulate many exceptions in order to force a segment to fail the upload (multiply by SingleSegmentUploader.MaxBufferUploadAttemptAccount)
        //this only works because we have a small file, which we know will fit in only one buffer (for a larger file, more complex operations are necessary)
        int actualfailCount = segmentFailCount * SingleSegmentUploader.MaxBufferUploadAttemptCount;
        boolean expectSuccess = segmentFailCount < MultipleSegmentUploader.MaxUploadAttemptCount;

        int callCount = 0;

        //create a mock front end sitting on top of a working front end that simulates some erros for some time
        InMemoryFrontEnd workingFrontEnd = new InMemoryFrontEnd();
        MsuMockFrontEnd fe = new MsuMockFrontEnd(workingFrontEnd, true, actualfailCount);

        UploadMetadata metadata = CreateMetadata(1);
        try
        {
            MultipleSegmentUploader msu = new MultipleSegmentUploader(metadata, 1, fe);
            msu.UseSegmentBlockBackOffRetryStrategy = false;

            if (expectSuccess)
            {
                //the Upload method should not throw any exceptions in this case
                msu.Upload();

                //if we are expecting success, verify that both the metadata and the target streams are complete
                VerifyTargetStreamsAreComplete(metadata, workingFrontEnd);
            }
            else
            {
                //the Upload method should throw an aggregate exception in this case
                try {
                    msu.Upload();
                    Assert.assertTrue("An aggregate upload exception was expected but no exception was thrown.", false);
                }
                catch (AggregateUploadException ex) {
                    // do nothing, expected
                }

                //if we do not expect success, verify that at least 1 segment was marked as Failed
                boolean foundFailedSegment = false;
                for (UploadSegmentMetadata s: metadata.Segments) {
                    if(s.Status == SegmentUploadStatus.Failed) {
                        foundFailedSegment = true;
                        break;
                    }
                }
                Assert.assertTrue("Could not find any failed segments", foundFailedSegment);

                //for every other segment, verify it was completed OK
                for (UploadSegmentMetadata segment: metadata.Segments)
                {
                    if( segment.Status != SegmentUploadStatus.Failed) {
                        VerifyTargetStreamIsComplete(segment, metadata, workingFrontEnd);
                    }
                }
            }
        }
        finally
        {
            metadata.DeleteFile();
        }
    }

    private void VerifyTargetStreamsAreComplete(UploadMetadata metadata, InMemoryFrontEnd fe) throws Exception {
        for (UploadSegmentMetadata segment: metadata.Segments)
        {
            VerifyTargetStreamIsComplete(segment, metadata, fe);
        }
    }

    private void VerifyTargetStreamIsComplete(UploadSegmentMetadata segmentMetadata, UploadMetadata metadata, InMemoryFrontEnd frontEnd) throws Exception {
        Assert.assertEquals(SegmentUploadStatus.Complete, segmentMetadata.Status);
        Assert.assertTrue(MessageFormat.format("Segment {0} was not uploaded", segmentMetadata.SegmentNumber), frontEnd.StreamExists(segmentMetadata.Path));
        Assert.assertEquals(segmentMetadata.Length, frontEnd.GetStreamLength(segmentMetadata.Path));

        byte[] actualContents = frontEnd.GetStreamContents(segmentMetadata.Path);
        byte[] expectedContents = GetExpectedContents(segmentMetadata, metadata);
        Assert.assertArrayEquals(MessageFormat.format("Segment {0} has unexpected contents", segmentMetadata.SegmentNumber), expectedContents, actualContents);
    }


    private byte[] GetExpectedContents(UploadSegmentMetadata segment, UploadMetadata metadata)
    {
        byte[] result = new byte[(int)segment.Length];
        System.arraycopy(_smallFileContents, (int) (segment.SegmentNumber * metadata.SegmentLength), result, 0, (int)segment.Length);
        return result;
    }

    private UploadMetadata CreateMetadata(int segmentCount) throws IOException {
        File path = File.createTempFile("adlsmsumetadata", ".xml");
        UploadMetadata metadata = new UploadMetadata();

        metadata.MetadataFilePath = path.getAbsolutePath();
        metadata.InputFilePath = _smallFilePath;
        metadata.FileLength = _smallFileContents.length;
        metadata.SegmentCount = segmentCount;
        metadata.SegmentLength = UploadSegmentMetadata.CalculateSegmentLength(_smallFileContents.length, segmentCount);
        metadata.Segments = new UploadSegmentMetadata[segmentCount];
        metadata.TargetStreamPath = "abc";
        metadata.UploadId = "123";
        metadata.IsBinary = true;


        long offset = 0;
        for (int i = 0; i < segmentCount; i++)
        {
            long length = UploadSegmentMetadata.CalculateSegmentLength(i, metadata);
            metadata.Segments[i] = new UploadSegmentMetadata();

            metadata.Segments[i].SegmentNumber = i;
            metadata.Segments[i].Offset = offset;
            metadata.Segments[i].Status = SegmentUploadStatus.Pending;
            metadata.Segments[i].Length = length;
            metadata.Segments[i].Path = MessageFormat.format("{0}.{1}.segment{2}", metadata.TargetStreamPath, metadata.UploadId, i);

            offset += length;
        }

        return metadata;
    }
}
