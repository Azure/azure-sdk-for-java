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

import javax.management.OperationsException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Unit tests for the uploader.
 */
public class DataLakeUploaderTests {
    private static final int LargeFileLength = 50 * 1024 * 1024; // 50mb
    private static byte[] _largeFileData = new byte[LargeFileLength];
    private static String _largeFilePath;
    private static final int SmallFileLength = 128;
    private static byte[] _smallFileData = new byte[SmallFileLength];
    private static String _smallFilePath;
    private static final int ThreadCount = 1;
    private static final String TargetStreamPath = "1";

    private static String curMetadataPath;

    @BeforeClass
    public static void Setup() throws IOException {
        _largeFilePath = TestHelpers.GenerateFileData(_largeFileData);
        _smallFilePath = TestHelpers.GenerateFileData(_smallFileData);
    }

    @AfterClass
    public static void Teardown()
    {
        File large = new File(_largeFilePath);
        File small = new File(_smallFilePath);
        if (large.exists())
        {
            large.delete();
        }

        if (small.exists())
        {
            small.delete();
        }
    }

    /**
     * Tests the case when invalid parameters are being passed to the uploader.
     *
     * @throws Exception
     */
    @Test
    public void DataLakeUploader_InvalidParameters() throws Exception
    {
        //invalid file path
        File invalidFilePath = File.createTempFile("adlsUploader", "noneexistent");
        invalidFilePath.delete();
        Assert.assertFalse("Unit test error: generated temp file actually exists", invalidFilePath.exists());

        try {
            new DataLakeStoreUploader(new UploadParameters(invalidFilePath.toString(), "1", "foo", 1, false, false, true, 4 * 1024 * 1024, null),new InMemoryFrontEnd());
            Assert.assertTrue("Expected a file not found exception but no exception was thrown!", false);
        }
        catch (FileNotFoundException e) {
            // do nothing this is expected
        }

        //no target stream
        try {
            new DataLakeStoreUploader(new UploadParameters(_largeFilePath, null, "foo", 1, false, false, true, 4 * 1024 * 1024, null),new InMemoryFrontEnd());
            Assert.assertTrue("Expected a file not found exception but no exception was thrown!", false);
        }
        catch (IllegalArgumentException e) {
            // do nothing this is expected
        }

        //target stream ends in '/'
        try {
            new DataLakeStoreUploader(new UploadParameters(_largeFilePath, "1/", "foo", 1, false, false, true, 4 * 1024 * 1024, null),new InMemoryFrontEnd());
            Assert.assertTrue("Expected exception for invalid target stream but no exception was thrown!", false);
        }
        catch (IllegalArgumentException e) {
            // do nothing this is expected
        }

        //no account name
        try {
            new DataLakeStoreUploader(new UploadParameters(_largeFilePath, "1", null, 1, false, false, true, 4 * 1024 * 1024, null),new InMemoryFrontEnd());
            Assert.assertTrue("Expected exception for null account name but no exception was thrown!", false);
        }
        catch (IllegalArgumentException e) {
            // do nothing this is expected
        }

        //bad thread count
        try {
            new DataLakeStoreUploader(new UploadParameters(_largeFilePath, "1", "foo", 0, false, false, true, 4 * 1024 * 1024, null),new InMemoryFrontEnd());
            Assert.assertTrue("Expected an exception for invalid thread count but no exception was thrown!", false);
        }
        catch (IllegalArgumentException e) {
            // do nothing this is expected
        }

        try {
            new DataLakeStoreUploader(new UploadParameters(_largeFilePath, "1", "foo", DataLakeStoreUploader.MaxAllowedThreads + 1, false, false, true, 4 * 1024 * 1024, null),new InMemoryFrontEnd());
            Assert.assertTrue("Expected an exception for invalid thread count but no exception was thrown!", false);
        }
        catch (IllegalArgumentException e) {
            // do nothing this is expected
        }
    }

    /**
     * Tests the case when the target stream exists and we haven't set the overwrite flag.
     *
     * @throws Exception
     */
    @Test
    public void DataLakeUploader_TargetExistsNoOverwrite() throws Exception {
        InMemoryFrontEnd frontEnd = new InMemoryFrontEnd();
        frontEnd.CreateStream(TargetStreamPath, true, null, 0);

        //no resume, no overwrite
        UploadParameters up = CreateParameters(false, false, _smallFilePath, true);
        DataLakeStoreUploader uploader = new DataLakeStoreUploader(up, frontEnd);
        try {
            uploader.Execute();
            Assert.assertTrue("Expected an exception for no overwrite when file exists but no exception was thrown!", false);
        }
        catch (OperationsException e) {
            // expected
        }

        //resume, no overwrite
        up = CreateParameters(true, false, _smallFilePath, false);
        uploader = new DataLakeStoreUploader(up, frontEnd);
        try {
            uploader.Execute();
            Assert.assertTrue("Expected an exception for no overwrite when file exists but no exception was thrown!", false);
        }
        catch (OperationsException e) {
            // expected
        }

        //resume, overwrite
        up = CreateParameters(true, true, _smallFilePath, false);
        uploader = new DataLakeStoreUploader(up, frontEnd);
        uploader.Execute();


        //no resume, overwrite
        up = CreateParameters(false, true, _smallFilePath, true);
        uploader = new DataLakeStoreUploader(up, frontEnd);
        uploader.Execute();
    }

    /**
     * Tests the case of a fresh upload with multiple segments.\
     *
     * @throws Exception
     */
    @Test
    public void DataLakeUploader_FreshUpload() throws Exception {
        InMemoryFrontEnd frontEnd = new InMemoryFrontEnd();
        UploadParameters up = CreateParameters(false, false, null, true);
        DataLakeStoreUploader uploader = new DataLakeStoreUploader(up, frontEnd);

        uploader.Execute();

        VerifyFileUploadedSuccessfully(up, frontEnd);
    }

    /**
     * Tests the resume upload when the metadata indicates all files are uploaded but no files exist on the server.
     *
     * @throws Exception
     */
    @Test
    public void DataLakeUploader_ResumeUploadWithAllMissingFiles() throws Exception {
        //this scenario is achieved by refusing to execute the concat command on the front end for the initial upload (which will interrupt it)
        //and then resuming the upload against a fresh front-end (which obviously has no files there)

        InMemoryFrontEnd backingFrontEnd1 = new InMemoryFrontEnd();
        UploaderFrontEndMock frontEnd1 = new UploaderFrontEndMock(backingFrontEnd1, true, false);

        //attempt full upload
        UploadParameters up = CreateParameters(false, false, null, true);
        DataLakeStoreUploader uploader = new DataLakeStoreUploader(up, frontEnd1);
        uploader.DeleteMetadataFile();

        try {
            uploader.Execute();
            Assert.assertTrue("Expected an intentional exception during concat but none was thrown!", false);
        }
        catch (IntentionalException e) {
            // expected
        }

        Assert.assertFalse("Target stream should not have been created", frontEnd1.StreamExists(up.getTargetStreamPath()));
        Assert.assertTrue("No temporary streams seem to have been created", 0 < backingFrontEnd1.getStreamCount());

        //attempt to resume the upload
        InMemoryFrontEnd frontEnd2 = new InMemoryFrontEnd();
        up = CreateParameters(true, false, null, false);
        uploader = new DataLakeStoreUploader(up, frontEnd2);

        //at this point the metadata exists locally but there are no target files in frontEnd2
        try
        {
            uploader.Execute();
        }
        finally
        {
            uploader.DeleteMetadataFile();
        }

        VerifyFileUploadedSuccessfully(up, frontEnd2);
    }

    /**
     * Tests the resume upload when only some segments were uploaded previously
     *
     * @throws Exception
     */
    @Test
    public void DataLakeUploader_ResumePartialUpload() throws Exception {
        //attempt to load the file fully, but only allow creating 1 target stream
        InMemoryFrontEnd backingFrontEnd = new InMemoryFrontEnd();
        UploaderFrontEndMock frontEnd = new UploaderFrontEndMock(backingFrontEnd, false, true);

        UploadParameters up = CreateParameters(false, false, null, true);
        DataLakeStoreUploader uploader = new DataLakeStoreUploader(up, frontEnd);
        uploader.DeleteMetadataFile();

        try {
            uploader.Execute();
            Assert.assertTrue("Expected an aggregate exception during upload due to failing out creating more than one stream but no exception was thrown!", false);
        }
        catch (AggregateUploadException e) {
            // expected
        }

        Assert.assertFalse("Target stream should not have been created", frontEnd.StreamExists(up.getTargetStreamPath()));
        Assert.assertEquals(1, backingFrontEnd.getStreamCount());

        //resume the upload but point it to the real back-end, which doesn't throw exceptions
        up = CreateParameters(true, false, null, false);
        uploader = new DataLakeStoreUploader(up, backingFrontEnd);

        try
        {
            uploader.Execute();
        }
        finally
        {
            uploader.DeleteMetadataFile();
        }

        VerifyFileUploadedSuccessfully(up, backingFrontEnd);
    }

    /**
     * Tests the upload case with only 1 segment (since that is an optimization of the broader case).
     *
     * @throws Exception
     */
    @Test
    public void DataLakeUploader_UploadSingleSegment() throws Exception {
        InMemoryFrontEnd frontEnd = new InMemoryFrontEnd();
        File fileToFolder = File.createTempFile("adlsUploader", "segmentTest");
        fileToFolder.delete();
        fileToFolder.mkdirs();
        UploadParameters up = new UploadParameters(
                _smallFilePath,
                "1",
                "foo",
                ThreadCount,
                false,
                false,
                true,
                4 * 1024 * 1024,
                fileToFolder.getAbsolutePath());

        FileOutputStream writer = new FileOutputStream(_smallFilePath);
        writer.write(_smallFileData);
        writer.flush();
        writer.close();

        DataLakeStoreUploader uploader = new DataLakeStoreUploader(up, frontEnd);
        uploader.Execute();

        VerifyFileUploadedSuccessfully(up, frontEnd, _smallFileData);
    }

    /**
     * Creates a parameter object.
     *
     * @param isResume Whether to resume.
     * @param isOverwrite Whether to enable overwrite.
     * @param filePath The file path.
     * @param createNewFolder indicates that we should create a new folder location where the data should be placed.
     * @return A {@link UploadParameters} object.
     * @throws IOException
     */
    private UploadParameters CreateParameters(boolean isResume, boolean isOverwrite, String filePath, boolean createNewFolder) throws IOException {
        if (filePath == null)
        {
            filePath = _largeFilePath;
        }

        File fileToFolder = File.createTempFile("adlsUploader", "metadata");
        if(createNewFolder) {
            fileToFolder.delete();
            fileToFolder.mkdirs();
            curMetadataPath = fileToFolder.getAbsolutePath();
        }

        return new UploadParameters(
            filePath,
            "1",
            "foo",
            false,
            ThreadCount,
            isOverwrite,
            isResume,
            true,
            4 * 1024 * 1024,
            curMetadataPath);
    }

    /**
     * Verifies the file was successfully uploaded.
     *
     * @param up The upload parameters.
     * @param frontEnd The front end to use.
     * @throws Exception
     */
    private void VerifyFileUploadedSuccessfully(UploadParameters up, InMemoryFrontEnd frontEnd) throws Exception {
        VerifyFileUploadedSuccessfully(up, frontEnd, _largeFileData);
    }

    /**
     * Verifies the file was successfully uploaded.
     * @param up The upload parameters.
     * @param frontEnd The front end to use.
     * @param fileContents The file contents.
     * @throws Exception
     */
    private void VerifyFileUploadedSuccessfully(UploadParameters up, InMemoryFrontEnd frontEnd, byte[] fileContents) throws Exception {
        Assert.assertTrue("Uploaded stream does not exist", frontEnd.StreamExists(up.getTargetStreamPath()));
        Assert.assertEquals(1, frontEnd.getStreamCount());
        Assert.assertEquals(fileContents.length, frontEnd.GetStreamLength(up.getTargetStreamPath()));

        byte[] uploadedData = frontEnd.GetStreamContents(up.getTargetStreamPath());
        Assert.assertArrayEquals("Uploaded stream is not binary identical to input file", fileContents, uploadedData);
    }
}
