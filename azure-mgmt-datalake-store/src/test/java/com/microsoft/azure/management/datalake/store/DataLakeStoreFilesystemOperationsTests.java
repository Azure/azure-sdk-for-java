package com.microsoft.azure.management.datalake.store;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.datalake.store.models.FileOperationResult;
import com.microsoft.azure.management.datalake.store.models.FileStatusResult;
import com.microsoft.azure.management.datalake.store.models.FileType;
import com.microsoft.azure.management.resources.models.ResourceGroup;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import sun.misc.IOUtils;

public class DataLakeStoreFilesystemOperationsTests extends DataLakeStoreManagementTestBase {
    // constants
    private static String folderToCreate = "SDKTestFolder01";
    private static String folderToMove = "SDKTestMoveFolder01";
    private static String fileToCreate = "SDKTestFile01.txt";
    private static String fileToCreateWithContents = "SDKTestFile02.txt";
    private static String fileToCopy = "SDKTestCopyFile01.txt";
    private static String fileToConcatTo = "SDKTestConcatFile01.txt";
    private static String fileToMove = "SDKTestMoveFile01.txt";

    private static String fileContentsToAdd = "These are some random test contents 1234!@";
    private static String fileContentsToAppend = "More test contents, that were appended!";
    
    // helper methods
    private String CreateFolder(String caboAccountName, boolean randomName) throws Exception
    {
        // Create a folder
        String folderPath = randomName
                ? generateName(folderToCreate)
                : folderToCreate;

        FileOperationResult response = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().mkdirs(folderPath, caboAccountName, null).getBody();
        Assert.assertTrue(response.getBooleanProperty());

        return folderPath;
    }

    private String CreateFile(String caboAccountName, boolean withContents, boolean randomName, String folderName) throws Exception
    {
        String filePath = randomName ? generateName(String.format("{0}/{1}", folderName, fileToCreate)) : String.format("{0}/{1}", folderName, fileToCreate);

        if (!withContents)
        {
            dataLakeStoreFileSystemManagementClient.getFileSystemOperations().create(
                    filePath,
                    caboAccountName,
                    null,
                    "true",
                    new ByteArrayInputStream(new byte[]{}), null);
        }
        else
        {
            dataLakeStoreFileSystemManagementClient.getFileSystemOperations().create(
                    filePath,
                    caboAccountName,
                    null,
                    "true",
                    new ByteArrayInputStream(fileContentsToAdd.getBytes()), null);
        }

        return filePath;
    }

    private FileStatusResult GetAndCompareFileOrFolder(String caboAccountName, String fileOrFolderPath, FileType expectedType, long expectedLength) throws Exception
    {
        FileStatusResult getResponse = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getFileStatus(fileOrFolderPath, caboAccountName, null).getBody();
        Assert.assertEquals(expectedLength, (long)getResponse.getFileStatus().getLength());
        Assert.assertEquals(expectedType, getResponse.getFileStatus().getType());

        return getResponse;
    }

    private void CompareFileContents(String caboAccountName, String filePath, String expectedContents) throws Exception
    {
        // download a file and ensure they are equal
        InputStream openResponse = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().open(filePath, caboAccountName, null, null, null, null).getBody();
        Assert.assertNotNull(openResponse);

        String toCompare = new String(IOUtils.readFully(openResponse, -1, true));
        Assert.assertEquals(expectedContents, toCompare);
    }

    private void DeleteFolder(String caboAccountName, String folderPath, boolean recursive, boolean failureExpected) throws Exception
    {
        if (failureExpected)
        {
            // try to delete a folder that doesn't exist or should fail
            try
            {
                FileOperationResult deleteFolderResponse = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().delete(folderPath, caboAccountName, null, recursive).getBody();
                Assert.assertTrue(!deleteFolderResponse.getBooleanProperty());
            }
            catch (Exception e)
            {
                Assert.assertTrue(e instanceof CloudException);
            }
        }
        else
        {
            // Delete a folder
            FileOperationResult deleteFolderResponse = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().delete(folderPath, caboAccountName, null, recursive).getBody();
            Assert.assertTrue(deleteFolderResponse.getBooleanProperty());
        }
    }

    private void DeleteFile(String caboAccountName, String filePath, boolean failureExpected) throws Exception
    {
        if (failureExpected)
        {
            // try to delete a file that doesn't exist
            try
            {
                FileOperationResult deleteFileResponse = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().delete(filePath, caboAccountName, null, false).getBody();
                Assert.assertTrue(!deleteFileResponse.getBooleanProperty());
            }
            catch (Exception e)
            {
                Assert.assertTrue(e instanceof CloudException);
            }
        }
        else
        {
            // Delete a file
            FileOperationResult deleteFileResponse = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().delete(filePath, caboAccountName, null, false).getBody();
            Assert.assertTrue(deleteFileResponse.getBooleanProperty());
        }
    }
}
