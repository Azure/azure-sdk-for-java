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
            msu.setUseSegmentBlockBackOffRetryStrategy(false);
            msu.upload();
            VerifyTargetStreamsAreComplete(metadata, fe);
        }
        finally
        {
            metadata.deleteFile();
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
            msu.setUseSegmentBlockBackOffRetryStrategy(false);
            msu.upload();
            VerifyTargetStreamsAreComplete(metadata, fe);
        }
        finally
        {
            metadata.deleteFile();
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
        int threadCount = metadata.getSegmentCount() * 10; //intentionally setting this higher than the # of segments
        try
        {
            MultipleSegmentUploader msu = new MultipleSegmentUploader(metadata, threadCount, fe);
            msu.setUseSegmentBlockBackOffRetryStrategy(false);
            msu.upload();
            VerifyTargetStreamsAreComplete(metadata, fe);
        }
        finally
        {
            metadata.deleteFile();
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
            msu.setUseSegmentBlockBackOffRetryStrategy(false);
            msu.upload();
            VerifyTargetStreamsAreComplete(metadata, fe);

            //delete about 50% of segments
            for (int i = 0; i < metadata.getSegmentCount(); i++)
            {
                UploadSegmentMetadata currentSegment = metadata.getSegments()[i];
                if (i % 2 == 0)
                {
                    currentSegment.setStatus(SegmentUploadStatus.Pending);
                    fe.deleteStream(currentSegment.getPath(), false);
                }
            }

            //re-upload everything
            msu = new MultipleSegmentUploader(metadata, 1, fe);
            msu.upload();
            VerifyTargetStreamsAreComplete(metadata, fe);
        }
        finally
        {
            metadata.deleteFile();
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
        int actualfailCount = segmentFailCount * SingleSegmentUploader.MAX_BUFFER_UPLOAD_ATTEMPT_COUNT;
        boolean expectSuccess = segmentFailCount < MultipleSegmentUploader.MAX_UPLOAD_ATTEMPT_COUNT;

        int callCount = 0;

        //create a mock front end sitting on top of a working front end that simulates some erros for some time
        InMemoryFrontEnd workingFrontEnd = new InMemoryFrontEnd();
        MsuMockFrontEnd fe = new MsuMockFrontEnd(workingFrontEnd, true, actualfailCount);

        UploadMetadata metadata = CreateMetadata(1);
        try
        {
            MultipleSegmentUploader msu = new MultipleSegmentUploader(metadata, 1, fe);
            msu.setUseSegmentBlockBackOffRetryStrategy(false);

            if (expectSuccess)
            {
                //the upload method should not throw any exceptions in this case
                msu.upload();

                //if we are expecting success, verify that both the metadata and the target streams are complete
                VerifyTargetStreamsAreComplete(metadata, workingFrontEnd);
            }
            else
            {
                //the upload method should throw an aggregate exception in this case
                try {
                    msu.upload();
                    Assert.assertTrue("An aggregate upload exception was expected but no exception was thrown.", false);
                }
                catch (AggregateUploadException ex) {
                    // do nothing, expected
                }

                //if we do not expect success, verify that at least 1 segment was marked as Failed
                boolean foundFailedSegment = false;
                for (UploadSegmentMetadata s: metadata.getSegments()) {
                    if(s.getStatus() == SegmentUploadStatus.Failed) {
                        foundFailedSegment = true;
                        break;
                    }
                }
                Assert.assertTrue("Could not find any failed segments", foundFailedSegment);

                //for every other segment, verify it was completed OK
                for (UploadSegmentMetadata segment: metadata.getSegments())
                {
                    if( segment.getStatus() != SegmentUploadStatus.Failed) {
                        VerifyTargetStreamIsComplete(segment, metadata, workingFrontEnd);
                    }
                }
            }
        }
        finally
        {
            metadata.deleteFile();
        }
    }

    private void VerifyTargetStreamsAreComplete(UploadMetadata metadata, InMemoryFrontEnd fe) throws Exception {
        for (UploadSegmentMetadata segment: metadata.getSegments())
        {
            VerifyTargetStreamIsComplete(segment, metadata, fe);
        }
    }

    private void VerifyTargetStreamIsComplete(UploadSegmentMetadata segmentMetadata, UploadMetadata metadata, InMemoryFrontEnd frontEnd) throws Exception {
        Assert.assertEquals(SegmentUploadStatus.Complete, segmentMetadata.getStatus());
        Assert.assertTrue(MessageFormat.format("Segment {0} was not uploaded", segmentMetadata.getSegmentNumber()), frontEnd.streamExists(segmentMetadata.getPath()));
        Assert.assertEquals(segmentMetadata.getLength(), frontEnd.getStreamLength(segmentMetadata.getPath()));

        byte[] actualContents = frontEnd.GetStreamContents(segmentMetadata.getPath());
        byte[] expectedContents = GetExpectedContents(segmentMetadata, metadata);
        Assert.assertArrayEquals(MessageFormat.format("Segment {0} has unexpected contents", segmentMetadata.getSegmentNumber()), expectedContents, actualContents);
    }


    private byte[] GetExpectedContents(UploadSegmentMetadata segment, UploadMetadata metadata)
    {
        byte[] result = new byte[(int)segment.getLength()];
        System.arraycopy(_smallFileContents, (int) (segment.getSegmentNumber() * metadata.getSegmentLength()), result, 0, (int)segment.getLength());
        return result;
    }

    private UploadMetadata CreateMetadata(int segmentCount) throws IOException {
        File path = File.createTempFile("adlsmsumetadata", ".xml");
        UploadMetadata metadata = new UploadMetadata();

        metadata.setMetadataFilePath(path.getAbsolutePath());
        metadata.setInputFilePath(_smallFilePath);
        metadata.setFileLength(_smallFileContents.length);
        metadata.setSegmentCount(segmentCount);
        metadata.setSegmentLength(UploadSegmentMetadata.calculateSegmentLength(_smallFileContents.length, segmentCount));

        metadata.setTargetStreamPath("abc");
        metadata.setUploadId("123");
        metadata.setBinary(true);

        UploadSegmentMetadata[] toSet = new UploadSegmentMetadata[segmentCount];
        long offset = 0;
        for (int i = 0; i < segmentCount; i++)
        {
            long length = UploadSegmentMetadata.calculateSegmentLength(i, metadata);
            toSet[i] = new UploadSegmentMetadata();

            toSet[i].setSegmentNumber(i);
            toSet[i].setOffset(offset);
            toSet[i].setStatus(SegmentUploadStatus.Pending);
            toSet[i].setLength(length);
            toSet[i].setPath(MessageFormat.format("{0}.{1}.segment{2}", metadata.getTargetStreamPath(), metadata.getUploadId(), i));

            offset += length;
        }

        metadata.setSegments(toSet);
        return metadata;
    }
}
