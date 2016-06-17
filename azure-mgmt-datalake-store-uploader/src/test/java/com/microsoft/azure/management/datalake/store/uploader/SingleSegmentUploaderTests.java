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
import java.io.IOException;

public class SingleSegmentUploaderTests {
    private static byte[] _smallFileContents = new byte[10 * 1024]; //10KB file
    private static String _smallFilePath;

    private static byte[] _largeFileContents = new byte[10 * 1024 * 1024]; //10MB file
    private static String _largeFilePath;

    private static byte[] _textFileContents = new byte[20 * 1024 * 1024]; //20MB file
    private static String _textFilePath;

    private static byte[] _badTextFileContents = new byte[10 * 1024 * 1024]; //10MB file
    private static String _badTextFilePath;

    private static final String StreamPath = "abc";

    @BeforeClass
    public static void Setup() throws IOException {
        _smallFilePath = TestHelpers.GenerateFileData(_smallFileContents);
        _largeFilePath = TestHelpers.GenerateFileData(_largeFileContents);
        _textFilePath = TestHelpers.GenerateTextFileData(_textFileContents, 1, SingleSegmentUploader.BUFFER_LENGTH);
        _badTextFilePath = TestHelpers.GenerateTextFileData(_badTextFileContents, SingleSegmentUploader.BUFFER_LENGTH + 1, SingleSegmentUploader.BUFFER_LENGTH + 2);
    }

    @AfterClass
    public static void Teardown()
    {
        File largeFile = new File(_largeFilePath);
        File smallFile = new File(_smallFilePath);
        File textFile = new File(_textFilePath);
        File badFile = new File(_badTextFilePath);
        if (largeFile.exists())
        {
            largeFile.delete();
        }

        if (smallFile.exists())
        {
            smallFile.delete();
        }

        if (textFile.exists())
        {
            textFile.delete();
        }

        if (badFile.exists())
        {
            badFile.delete();
        }
    }

    /**
     * Tests a simple upload consisting of a single block (the file is small enough to be uploaded without splitting into smaller buffers)
     *
     * @throws Exception
     */
    @Test
    public void SingleSegmentUploader_UploadSingleBlockStream() throws Exception {
        InMemoryFrontEnd fe = new InMemoryFrontEnd();

        UploadMetadata metadata = CreateMetadata(_smallFilePath, _smallFileContents.length);
        SingleSegmentUploader ssu = new SingleSegmentUploader(0, metadata, fe);
        ssu.setUseBackOffRetryStrategy(false);
        ssu.upload();

        byte[] actualContents = fe.GetStreamContents(StreamPath);
        Assert.assertArrayEquals("Unexpected uploaded stream contents.", _smallFileContents, actualContents);
    }

    /**
     * Tests an uploading consisting of a larger file, which will need to be uploaded in sequential buffers.
     *
     * @throws Exception
     */
    @Test
    public void SingleSegmentUploader_UploadMultiBlockStream() throws Exception {
        InMemoryFrontEnd fe = new InMemoryFrontEnd();

        UploadMetadata metadata = CreateMetadata(_largeFilePath, _largeFileContents.length);
        
        SingleSegmentUploader ssu = new SingleSegmentUploader(0, metadata, fe);
        ssu.setUseBackOffRetryStrategy(false);
        ssu.upload();

        byte[] actualContents = fe.GetStreamContents(StreamPath);
        Assert.assertArrayEquals("Unexpected uploaded stream contents.", _largeFileContents, actualContents);
    }

    /**
     * Tests the case when only a part of the file is to be uploaded (i.e., all other cases feed in the entire file)
     *
     * @throws Exception
     */
    @Test
    public void SingleSegmentUploader_UploadFileRange() throws Exception {
        int length = _smallFileContents.length / 3;

        InMemoryFrontEnd fe = new InMemoryFrontEnd();

        UploadMetadata metadata = CreateMetadata(_smallFilePath, length);
        
        SingleSegmentUploader ssu = new SingleSegmentUploader(0, metadata, fe);
        ssu.setUseBackOffRetryStrategy(false);
        ssu.upload();

        byte[] actualContents = fe.GetStreamContents(StreamPath);
        byte[] expectedContents = new byte[length];
        System.arraycopy(_smallFileContents, 0, expectedContents, 0, length);
        Assert.assertArrayEquals("Unexpected uploaded stream contents.", expectedContents, actualContents);
        
    }

    /**
     * Tests the case when an existing stream with the same name already exists on the server. That stream needs to be fully replaced with the new data.
     *
     * @throws Exception
     */
    @Test
    public void SingleSegmentUploader_TargetStreamExists() throws Exception {
        InMemoryFrontEnd fe = new InMemoryFrontEnd();

        //load up an existing stream
        fe.createStream(StreamPath, true, null, 0);
        byte[] data = "random".getBytes();
        fe.appendToStream(StreamPath, data, 0, data.length);

        //force a re-upload of the stream
        UploadMetadata metadata = CreateMetadata(_smallFilePath, _smallFileContents.length);
        SingleSegmentUploader ssu = new SingleSegmentUploader(0, metadata, fe);
        ssu.setUseBackOffRetryStrategy(false);
        ssu.upload();

        byte[] actualContents = fe.GetStreamContents(StreamPath);
        Assert.assertArrayEquals("Unexpected uploaded stream contents.", _smallFileContents, actualContents);
    }

    /**
     * Tests the case when the upload did "succeed", but the server reports back a different stream length than expected.
     *
     * @throws Exception
     */
    @Test
    public void SingleSegmentUploader_VerifyUploadStreamFails() throws Exception {
        //create a mock front end which doesn't do anything
        SsuMockFrontEnd fe = new SsuMockFrontEnd(new InMemoryFrontEnd(), true, false , -1);

        //upload some data
        UploadMetadata metadata = CreateMetadata(_smallFilePath, _smallFileContents.length);
        SingleSegmentUploader ssu = new SingleSegmentUploader(0, metadata, fe);
        ssu.setUseBackOffRetryStrategy(false);

        //the upload method should fail if it cannot verify that the stream was uploaded after the upload (i.e., it will get a length of 0 at the end)
        try {
            ssu.upload();
            Assert.assertTrue("the upload method should fail if it cannot verify that the stream was uploaded, but it succeeded!", false);
        }
        catch (UploadFailedException ex) {
            // do nothing, expected
        }
    }

    /**
     * Tests the case when the SingleSegmentUploader should upload a non-binary file (i.e., split on record boundaries).
     *
     * @throws Exception
     */
    @Test
    public void SingleSegmentUploader_UploadNonBinaryFile() throws Exception {
        InMemoryFrontEnd fe = new InMemoryFrontEnd();

        UploadMetadata metadata = CreateMetadata(_textFilePath, _textFileContents.length);
        metadata.setBinary(false);
        
        SingleSegmentUploader ssu = new SingleSegmentUploader(0, metadata, fe);
        ssu.setUseBackOffRetryStrategy(false);
        ssu.upload();

        //verify the entire file is identical to the source file
        byte[] actualContents = fe.GetStreamContents(StreamPath);
        Assert.assertArrayEquals("Unexpected uploaded stream contents.", _textFileContents, actualContents);

        //verify the append blocks start/end on record boundaries
        Iterable<byte[]> appendBlocks = fe.GetAppendBlocks(StreamPath);
        int lengthSoFar = 0;
        for (byte[] append: appendBlocks)
        {
            lengthSoFar += append.length;
            if (lengthSoFar < actualContents.length)
            {
                Assert.assertEquals('\n', (char)append[append.length - 1]);
            }
        }
    }

    /**
     * Tests the case when the SingleSegmentUploader tries upload a non-binary file (i.e., split on record boundaries), but at least one record is larger than the max allowed size.
     *
     * @throws Exception
     */
    @Test
    public void SingleSegmentUploader_UploadNonBinaryFileTooLargeRecord() throws Exception {
        InMemoryFrontEnd fe = new InMemoryFrontEnd();

        UploadMetadata metadata = CreateMetadata(_badTextFilePath, _badTextFileContents.length);
        metadata.setBinary(false);
        
        SingleSegmentUploader ssu = new SingleSegmentUploader(0, metadata, fe);
        ssu.setUseBackOffRetryStrategy(false);

        try {
            ssu.upload();
            Assert.assertTrue("Should fail when a record is too large to fit within a single record boundary when splitting on boundaries, but didn't!", false);
        }
        catch (UploadFailedException ex) {
            // do nothing, expected
        }
    }

    /**
     * Tests various scenarios where the upload will fail repeatedly; verifies that the uploader will retry a certain number of times before finally giving up
     *
     * @throws Exception
     */
    @Test
    public void SingleSegmentUploader_RetryBlock() throws Exception {
        TestRetryBlock(0);
        TestRetryBlock(1);
        TestRetryBlock(2);
        TestRetryBlock(3);
        TestRetryBlock(4);
        TestRetryBlock(5);
    }

    public void TestRetryBlock(int failCount) throws Exception {
        boolean expectSuccess = failCount < SingleSegmentUploader.MAX_BUFFER_UPLOAD_ATTEMPT_COUNT;

        int callCount = 0;

        InMemoryFrontEnd workingFrontEnd = new InMemoryFrontEnd();
        SsuMockFrontEnd fe = new SsuMockFrontEnd(workingFrontEnd, false, true, failCount);

        UploadMetadata metadata = CreateMetadata(_smallFilePath, _smallFileContents.length);
        
        SingleSegmentUploader ssu = new SingleSegmentUploader(0, metadata, fe);
        ssu.setUseBackOffRetryStrategy(false);

        if (expectSuccess)
        {
            ssu.upload();
            byte[] actualContents = workingFrontEnd.GetStreamContents(StreamPath);
            Assert.assertArrayEquals("Unexpected uploaded stream contents.", _smallFileContents, actualContents);
        }
        else
        {
            try {
                ssu.upload();
                Assert.assertTrue("upload should have failed due to too many retries but didn't!", false);
            }
            catch (Exception ex) {
                Assert.assertTrue("Expected an intentional exception and got: " + ex, IntentionalException.class.isInstance(ex));
            }
        }
    }

    private UploadMetadata CreateMetadata(String filePath, long filelength)
    {
        UploadMetadata metadata = new UploadMetadata();
        metadata.setInputFilePath(filePath);
        metadata.setFileLength(filelength);
        metadata.setTargetStreamPath(StreamPath);
        metadata.setSegmentCount(1);
        metadata.setSegmentLength(UploadSegmentMetadata.calculateSegmentLength(filelength, 1));
        metadata.setBinary(true);

        UploadSegmentMetadata[] toSet = new UploadSegmentMetadata[1];
        toSet[0] = new UploadSegmentMetadata(0, metadata);
        toSet[0].setPath(metadata.getTargetStreamPath());
        metadata.setSegments(toSet);
        return metadata;
    }
}
