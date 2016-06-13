package com.microsoft.azure.management.datalake.store;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.datalake.store.models.AclStatusResult;
import com.microsoft.azure.management.datalake.store.models.DataLakeStoreAccount;
import com.microsoft.azure.management.datalake.store.models.FileOperationResult;
import com.microsoft.azure.management.datalake.store.models.FileStatusProperties;
import com.microsoft.azure.management.datalake.store.models.FileStatusResult;
import com.microsoft.azure.management.datalake.store.models.FileStatusesResult;
import com.microsoft.azure.management.datalake.store.models.FileType;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DataLakeStoreFilesystemOperationsTests extends DataLakeStoreManagementTestBase {
    // constants
    private static String folderToCreate = "SDKTestFolder01";
    private static String fileToCreateWithContents = "SDKTestFile02.txt";
    private static String fileToCopy = "SDKTestCopyFile01.txt";
    private static String fileToConcatTo = "SDKTestConcatFile01.txt";

    private static String fileContentsToAdd = "These are some random test contents 1234!@";

    private static String rgName = generateName("javaadlsrg");
    private static String adlsAcct = generateName("javaadlsacct");


    @BeforeClass
    public static void setup() throws Exception {
        createClients();
        ResourceGroupInner group = new ResourceGroupInner();
        String location = "eastus2";
        group.withLocation(location);
        resourceManagementClient.resourceGroups().createOrUpdate(rgName, group);

        // create storage and ADLS accounts, setting the accessKey
        DataLakeStoreAccount adlsAccount = new DataLakeStoreAccount();
        adlsAccount.withLocation(location);
        adlsAccount.withName(adlsAcct);
        dataLakeStoreAccountManagementClient.accounts().create(rgName, adlsAcct, adlsAccount);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
            resourceManagementClient.resourceGroups().delete(rgName);
        }
        catch (Exception e) {
            // ignore failures during cleanup, as it is best effort
        }
    }

    // tests
    @Test
    public void DataLakeStoreFileSystemFolderCreate() throws Exception
    {
        String folderPath = CreateFolder(adlsAcct, true);
        GetAndCompareFileOrFolder(adlsAcct, folderPath,
                FileType.DIRECTORY, 0);
    }

    /*
    TODO: Re-enable code when Expiry is live on the server again
    @Test
    public void DataLakeStoreFileSystemSetAndRemoveExpiry()
    {
        const long maxTimeInMilliseconds = 253402300800000;
        var filePath = CreateFile(adlsAcct, false, true);
        GetAndCompareFileOrFolder(adlsAcct, filePath, FileType.FILE, 0);

        // verify it does not have an expiration
        var fileInfo = dataLakeStoreFileSystemManagementClient.fileSystems().GetFileInfo(adlsAcct, filePath);
        Assert.assertTrue(fileInfo.FileInfo.ExpirationTime <= 0 || fileInfo.FileInfo.ExpirationTime == maxTimeInMilliseconds, "Expiration time was not equal to 0 or DateTime.MaxValue.Ticks! Actual value reported: " + fileInfo.FileInfo.ExpirationTime);

        // set the expiration time as an absolute value

        var toSetAbsolute = ToUnixTimeStampMs(HttpMockServer.GetVariable("absoluteTime", DateTime.Now.AddSeconds(120).ToString()));
        dataLakeStoreFileSystemManagementClient.fileSystems().SetFileExpiry(adlsAcct, filePath, ExpiryOptionType.Absolute, toSetAbsolute);
        fileInfo = dataLakeStoreFileSystemManagementClient.fileSystems().GetFileInfo(adlsAcct, filePath);
        VerifyTimeInAcceptableRange(toSetAbsolute, fileInfo.FileInfo.ExpirationTime.Value);

        // set the expiration time relative to now
        var toSetRelativeToNow = ToUnixTimeStampMs(HttpMockServer.GetVariable("relativeTime", DateTime.Now.AddSeconds(120).ToString()));
        dataLakeStoreFileSystemManagementClient.fileSystems().SetFileExpiry(adlsAcct, filePath, ExpiryOptionType.RelativeToNow, 120 * 1000);
        fileInfo = dataLakeStoreFileSystemManagementClient.fileSystems().GetFileInfo(adlsAcct, filePath);
        VerifyTimeInAcceptableRange(toSetRelativeToNow, fileInfo.FileInfo.ExpirationTime.Value);

        // set expiration time relative to the creation time
        var toSetRelativeCreationTime = fileInfo.FileInfo.CreationTime.Value + (120 * 1000);
        dataLakeStoreFileSystemManagementClient.fileSystems().SetFileExpiry(adlsAcct, filePath, ExpiryOptionType.RelativeToCreationDate, 120 * 1000);
        fileInfo = dataLakeStoreFileSystemManagementClient.fileSystems().GetFileInfo(adlsAcct, filePath);
        VerifyTimeInAcceptableRange(toSetRelativeCreationTime, fileInfo.FileInfo.ExpirationTime.Value);

        // reset expiration time to never
        dataLakeStoreFileSystemManagementClient.fileSystems().SetFileExpiry(adlsAcct, filePath, ExpiryOptionType.NeverExpire);
        fileInfo = dataLakeStoreFileSystemManagementClient.fileSystems().GetFileInfo(adlsAcct, filePath);
        Assert.assertTrue(fileInfo.FileInfo.ExpirationTime <= 0 || fileInfo.FileInfo.ExpirationTime == maxTimeInMilliseconds, "Expiration time was not equal to 0 or DateTime.MaxValue.Ticks! Actual value reported: " + fileInfo.FileInfo.ExpirationTime);
    }

    @Test
    public void DataLakeStoreFileSystemNegativeExpiry()
    {
        const long maxTimeInMilliseconds = 253402300800000;
        var filePath = CreateFile(adlsAcct, false, true);
        GetAndCompareFileOrFolder(adlsAcct, filePath, FileType.FILE, 0);

        // verify it does not have an expiration
        var fileInfo = dataLakeStoreFileSystemManagementClient.fileSystems().GetFileInfo(adlsAcct, filePath);
        Assert.assertTrue(fileInfo.FileInfo.ExpirationTime <= 0 || fileInfo.FileInfo.ExpirationTime == maxTimeInMilliseconds, "Expiration time was not equal to 0 or DateTime.MaxValue.Ticks! Actual value reported: " + fileInfo.FileInfo.ExpirationTime);

        // set the expiration time as an absolute value that is less than the creation time
        var toSetAbsolute = ToUnixTimeStampMs(HttpMockServer.GetVariable("absoluteNegativeTime", DateTime.Now.AddSeconds(-120).ToString()));
        Assert.assertThrows<CloudException>(() => dataLakeStoreFileSystemManagementClient.fileSystems().SetFileExpiry(adlsAcct, filePath, ExpiryOptionType.Absolute, toSetAbsolute));

        // set the expiration time as an absolute value that is greater than max allowed time
        toSetAbsolute = ToUnixTimeStampMs(DateTime.MaxValue.ToString()) + 1000;
        Assert.assertThrows<CloudException>(() => dataLakeStoreFileSystemManagementClient.fileSystems().SetFileExpiry(adlsAcct, filePath, ExpiryOptionType.Absolute, toSetAbsolute));

        // reset expiration time to never with a value and confirm the value is not honored
        dataLakeStoreFileSystemManagementClient.fileSystems().SetFileExpiry(adlsAcct, filePath, ExpiryOptionType.NeverExpire, 400);
        fileInfo = dataLakeStoreFileSystemManagementClient.fileSystems().GetFileInfo(adlsAcct, filePath);
        Assert.assertTrue(fileInfo.FileInfo.ExpirationTime <= 0 || fileInfo.FileInfo.ExpirationTime == maxTimeInMilliseconds, "Expiration time was not equal to 0 or DateTime.MaxValue.Ticks! Actual value reported: " + fileInfo.FileInfo.ExpirationTime);
    }
    */

    @Test
    public void DataLakeStoreFileSystemListFolderContents() throws Exception
    {
        String folderPath = CreateFolder(adlsAcct, true);
        GetAndCompareFileOrFolder(adlsAcct, folderPath,
                FileType.DIRECTORY, 0);

        String filePath = CreateFile(adlsAcct, false, true, folderPath);
        GetAndCompareFileOrFolder(adlsAcct, filePath, FileType.FILE, 0);

        // List all the contents in the folder
        FileStatusesResult listFolderResponse = dataLakeStoreFileSystemManagementClient.fileSystems().listFileStatus(adlsAcct, folderPath).getBody();

        // We know that this directory is brand new, so the contents should only be the one file.
        Assert.assertEquals(1, listFolderResponse.fileStatuses().fileStatus().size());
        Assert.assertEquals(FileType.FILE, listFolderResponse.fileStatuses().fileStatus().get(0).type());
    }

    @Test
    public void DataLakeStoreFileSystemEmptyFileCreate() throws Exception
    {
        String filePath = CreateFile(adlsAcct, false, true, folderToCreate);
        GetAndCompareFileOrFolder(adlsAcct, filePath, FileType.FILE, 0);
    }

    @Test
    public void DataLakeStoreFileSystemFileCreateWithContents() throws Exception
    {
        String filePath = CreateFile(adlsAcct, true, true, folderToCreate);
        GetAndCompareFileOrFolder(adlsAcct, filePath, FileType.FILE,
                fileContentsToAdd.length());
        CompareFileContents(adlsAcct, filePath,
                fileContentsToAdd);
    }

    @Test
    public void DataLakeStoreFileSystemAppendToFile() throws Exception
    {
        String filePath = CreateFile(adlsAcct, false, true, folderToCreate);
        GetAndCompareFileOrFolder(adlsAcct, filePath, FileType.FILE, 0);

        // Append to the file that we created
        String fileContentsToAppend = "More test contents, that were appended!";
        dataLakeStoreFileSystemManagementClient.fileSystems().append(adlsAcct, filePath, fileContentsToAppend.getBytes());

        GetAndCompareFileOrFolder(adlsAcct, filePath, FileType.FILE,
                fileContentsToAppend.length());
    }

    @Test
    public void DataLakeStoreFileSystemConcatenateFiles() throws Exception
    {
        String filePath1 = CreateFile(adlsAcct, true, true, folderToCreate);
        GetAndCompareFileOrFolder(adlsAcct, filePath1, FileType.FILE,
                fileContentsToAdd.length());

        String filePath2 = CreateFile(adlsAcct, true, true, folderToCreate);
        GetAndCompareFileOrFolder(adlsAcct, filePath2, FileType.FILE,
                fileContentsToAdd.length());

        String targetFolder = CreateFolder(adlsAcct, true);

        dataLakeStoreFileSystemManagementClient.fileSystems().concat(
                adlsAcct,
                String.format("%s/%s", targetFolder, fileToConcatTo),
                Arrays.asList(new String[]{filePath1, filePath2})
        );

        GetAndCompareFileOrFolder(adlsAcct,
                String.format("%s/%s", targetFolder, fileToConcatTo),
                FileType.FILE,
                fileContentsToAdd.length() * 2);

        // Attempt to get the files that were concatted together, which should fail and throw
        try {
            dataLakeStoreFileSystemManagementClient.fileSystems().getFileStatus(adlsAcct, filePath1);
            Assert.assertTrue("Able to get the old file after concat", false);
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof CloudException);
        }

        try {
            dataLakeStoreFileSystemManagementClient.fileSystems().getFileStatus(adlsAcct, filePath2);
            Assert.assertTrue("Able to get the old file after concat", false);
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof CloudException);
        }
    }

    @Test
    public void DataLakeStoreFileSystemMsConcatenateFiles() throws Exception
    {
        String filePath1 = CreateFile(adlsAcct, true, true, folderToCreate);
        GetAndCompareFileOrFolder(adlsAcct, filePath1, FileType.FILE,
                fileContentsToAdd.length());

        String filePath2 = CreateFile(adlsAcct, true, true, folderToCreate);
        GetAndCompareFileOrFolder(adlsAcct, filePath2, FileType.FILE,
                fileContentsToAdd.length());

        String targetFolder = CreateFolder(adlsAcct, true);

        dataLakeStoreFileSystemManagementClient.fileSystems().msConcat(
                adlsAcct,
                String.format("%s/%s", targetFolder, fileToConcatTo),
                String.format("sources=%s,%s", filePath1, filePath2).getBytes(),
                false);

        GetAndCompareFileOrFolder(adlsAcct,
                String.format("%s/%s", targetFolder, fileToConcatTo),
                FileType.FILE,
                fileContentsToAdd.length() * 2);

        // Attempt to get the files that were concatted together, which should fail and throw
        try {
            dataLakeStoreFileSystemManagementClient.fileSystems().getFileStatus(adlsAcct, filePath1);
            Assert.assertTrue("Able to get the old file after concat", false);
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof CloudException);
        }

        try {
            dataLakeStoreFileSystemManagementClient.fileSystems().getFileStatus(adlsAcct, filePath2);
            Assert.assertTrue("Able to get the old file after concat", false);
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof CloudException);
        }
    }

    @Test
    public void DataLakeStoreFileSystemMsConcatDeleteDir() throws Exception
    {
        String concatFolderPath = String.format("%s/%s", folderToCreate,
                "msconcatFolder");
        String filePath1 = CreateFile(adlsAcct, true, true,
                concatFolderPath);
        GetAndCompareFileOrFolder(adlsAcct, filePath1, FileType.FILE,
                fileContentsToAdd.length());

        String filePath2 = CreateFile(adlsAcct, true, true,
                concatFolderPath);
        GetAndCompareFileOrFolder(adlsAcct, filePath2, FileType.FILE,
                fileContentsToAdd.length());

        String targetFolder = CreateFolder(adlsAcct, true);

        String destination = String.format("%s/%s", targetFolder, fileToConcatTo);

        dataLakeStoreFileSystemManagementClient.fileSystems().msConcat(
                adlsAcct,
                destination,
                String.format("sources=%s,%s", filePath1, filePath2).getBytes(),
                true);

        GetAndCompareFileOrFolder(adlsAcct,
                String.format("%s/%s", targetFolder, fileToConcatTo),
                FileType.FILE,
                fileContentsToAdd.length()*2);

        // Attempt to get the files that were concatted together, which should fail and throw
        try {
            dataLakeStoreFileSystemManagementClient.fileSystems().getFileStatus(adlsAcct, filePath1);
            Assert.assertTrue("Able to get the old file after concat", false);
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof CloudException);
        }

        try {
            dataLakeStoreFileSystemManagementClient.fileSystems().getFileStatus(adlsAcct, filePath2);
            Assert.assertTrue("Able to get the old file after concat", false);
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof CloudException);
        }

        // Attempt to get the folder that was created for concat, which should fail and be deleted.
        try {
            dataLakeStoreFileSystemManagementClient.fileSystems().getFileStatus(adlsAcct, concatFolderPath);
            Assert.assertTrue("Able to get the old folder after concat", false);
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof CloudException);
        }
    }

    @Test
    public void DataLakeStoreFileSystemMoveFileAndFolder() throws Exception
    {
        String filePath = CreateFile(adlsAcct, true, true, folderToCreate);
        GetAndCompareFileOrFolder(adlsAcct, filePath, FileType.FILE,
                fileContentsToAdd.length());

        String targetFolder1 = CreateFolder(adlsAcct, true);
        String folderToMove = "SDKTestMoveFolder01";
        String targetFolder2 = generateName(folderToMove);

        // Move file first
        String fileToMove = "SDKTestMoveFile01.txt";
        FileOperationResult moveFileResponse = dataLakeStoreFileSystemManagementClient.fileSystems().rename(
                adlsAcct,
                filePath,
                String.format("%s/%s", targetFolder1, fileToMove)).getBody();
        Assert.assertTrue(moveFileResponse.operationResult());
        GetAndCompareFileOrFolder(adlsAcct,
                String.format("%s/%s", targetFolder1, fileToMove),
                FileType.FILE,
                fileContentsToAdd.length());

        // Ensure the old file is gone
        try {
            dataLakeStoreFileSystemManagementClient.fileSystems().getFileStatus(adlsAcct, filePath);
            Assert.assertTrue("Able to get the old file after rename", false);
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof CloudException);
        }

        // Now move folder completely.
        FileOperationResult moveFolderResponse = dataLakeStoreFileSystemManagementClient.fileSystems().rename(
                adlsAcct, 
                targetFolder1,
                targetFolder2).getBody();
        Assert.assertTrue(moveFolderResponse.operationResult());

        GetAndCompareFileOrFolder(adlsAcct, targetFolder2,
                FileType.DIRECTORY, 0);

        // ensure all the contents of the folder moved
        // List all the contents in the folder
        FileStatusesResult listFolderResponse = dataLakeStoreFileSystemManagementClient.fileSystems().listFileStatus(
                adlsAcct, 
                targetFolder2).getBody();

        // We know that this directory is brand new, so the contents should only be the one file.
        Assert.assertEquals(1, listFolderResponse.fileStatuses().fileStatus().size());
        Assert.assertEquals(FileType.FILE, listFolderResponse.fileStatuses().fileStatus().get(0).type());

        try {
            dataLakeStoreFileSystemManagementClient.fileSystems().getFileStatus(adlsAcct, targetFolder1);
            Assert.assertTrue("Able to get the old folder after rename", false);
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof CloudException);
        }
    }

    @Test
    public void DataLakeStoreFileSystemDeleteFolder() throws Exception
    {
        String folderPath = CreateFolder(adlsAcct, true);
        GetAndCompareFileOrFolder(adlsAcct, folderPath,
                FileType.DIRECTORY, 0);
        DeleteFolder(adlsAcct, folderPath, true, false);
        //WORK AROUND: Bug 4717659 makes it so even empty folders have contents.

        // delete again expecting failure.
        DeleteFolder(adlsAcct, folderPath, false, true);

        // delete a folder with contents
        String filePath = CreateFile(adlsAcct, true, true, folderPath);
        GetAndCompareFileOrFolder(adlsAcct, filePath, FileType.FILE,
                fileContentsToAdd.length());

        // should fail if recurse is not set
        DeleteFolder(adlsAcct, folderPath, false, true);

        // Now actually delete
        DeleteFolder(adlsAcct, folderPath, true, false);

        // delete again expecting failure.
        DeleteFolder(adlsAcct, folderPath, true, true);
    }

    @Test
    public void DataLakeStoreFileSystemDeleteFile() throws Exception
    {
        String filePath = CreateFile(adlsAcct, true, true, folderToCreate);
        GetAndCompareFileOrFolder(adlsAcct, filePath, FileType.FILE,
                fileContentsToAdd.length());
        DeleteFile(adlsAcct, filePath, false);

        // try to delete it again, which should fail
        DeleteFile(adlsAcct, filePath, true);
    }

    @Test
    public void DataLakeStoreFileSystemGetAndSetAcl() throws Exception
    {
        AclStatusResult currentAcl = dataLakeStoreFileSystemManagementClient.fileSystems().getAclStatus(adlsAcct, "/").getBody();

        List<String> aclToReplaceWith = new ArrayList<String>(currentAcl.aclStatus().entries());
        String originalOther = "";
        String toReplace = "other::rwx";
        for (int i = 0; i < aclToReplaceWith.size(); i++)
        {
            if (aclToReplaceWith.get(i).startsWith("other"))
            {
                originalOther = aclToReplaceWith.get(i);
                aclToReplaceWith.set(i, toReplace);
                break;
            }
        }

        Assert.assertFalse(originalOther == null || StringUtils.isEmpty(originalOther));

        // Set the other acl to RWX
        dataLakeStoreFileSystemManagementClient.fileSystems().setAcl(adlsAcct, "/",
                StringUtils.join(aclToReplaceWith, ","));

        AclStatusResult newAcl = dataLakeStoreFileSystemManagementClient.fileSystems().getAclStatus(adlsAcct, "/").getBody();
        // verify the ACL actually changed

        // Check the access first and assert that it returns OK (note: this is currently only for the user making the request, so it is not testing "other")
        dataLakeStoreFileSystemManagementClient.fileSystems().checkAccess(
                adlsAcct, 
                "/",
                "rwx");

        boolean foundIt = false;
        for (String entry: newAcl.aclStatus().entries())
        {
            if(entry.startsWith("other")) {
                Assert.assertEquals(toReplace, entry);
                foundIt = true;
                break;
            }
        }

        Assert.assertTrue(foundIt);

        // Set it back using specific entry
        dataLakeStoreFileSystemManagementClient.fileSystems().modifyAclEntries(
                adlsAcct, 
                "/",
                originalOther);

        // Now confirm that it equals the original ACL
        List<String> finalEntries = dataLakeStoreFileSystemManagementClient.fileSystems().getAclStatus(adlsAcct, "/").getBody()
                .aclStatus().entries();
        for (String entry: finalEntries)
        {
            boolean found = false;
            for(String curEntry: currentAcl.aclStatus().entries()) {
                if(curEntry.toUpperCase().equals(entry.toUpperCase())) {
                    found = true;
                    break;
                }
            }

            Assert.assertTrue(found);
        }

        Assert.assertEquals(finalEntries.size(), currentAcl.aclStatus().entries().size());
    }

    @Test
    public void DataLakeStoreFileSystemSetFileProperties() throws Exception
    {
        // This test simply tests that all bool/empty return actions return successfully

        String filePath = CreateFile(adlsAcct, true, true, folderToCreate);
        FileStatusProperties originalFileStatus =
                dataLakeStoreFileSystemManagementClient.fileSystems().getFileStatus(adlsAcct, filePath).getBody().fileStatus();
        // TODO: Set replication on file, this has been removed until it is confirmed as a supported API.
            /*
            var replicationResponse = dataLakeStoreFileSystemManagementClient.fileSystems().SetReplication(adlsAcct, filePath, 3);
            Assert.assertTrue(replicationResponse.Boolean);
            */

            /*
         * This API is available but all values put into it are ignored. Commenting this out until this API is fully functional.
        Assert.assertEquals(3,
            dataLakeFileSystemClient.FileSystem.getFileStatus(filePath)
                .FileStatus.Replication);
        */

        // set the time on the file
        // We use a static date for now since we aren't interested in whether the value is set properly, only that the method returns a 200.
            /* TODO: Re enable once supported.
            var timeToSet = new DateTime(2015, 10, 26, 14, 30, 0).Ticks;
            dataLakeStoreFileSystemManagementClient.fileSystems().SetTimes(adlsAcct, filePath, timeToSet, timeToSet);

            var fileStatusAfterTime =
                dataLakeStoreFileSystemManagementClient.fileSystems().getFileStatus(adlsAcct, filePath).FileStatus;
            */

            /*
         * This API is available but all values put into it are ignored. Commenting this out until this API is fully functional.
        Assert.assertTrue(
            fileStatusAfterTime.ModificationTime == timeToSet && fileStatusAfterTime.AccessTime == timeToSet);
        */

        // TODO: Symlink creation is explicitly not supported, but when it is this should be enabled.
            /*
            var symLinkName = generateName("testPath/symlinktest1");
            Assert.assertThrows<CloudException>(() => dataLakeStoreFileSystemManagementClient.fileSystems().CreateSymLink(adlsAcct, filePath, symLinkName, true));
            */

        // Once symlinks are available, remove the throws test and uncomment out this code.
        // Assert.assertTrue(createSymLinkResponse.StatusCode == HttpStatusCode.OK);
        // Assert.assertDoesNotThrow(() => dataLakeFileSystemClient.FileSystem.getFileStatus(symLinkName));
    }

    @Test
    public void DataLakeStoreFileSystemGetAcl() throws Exception
    {
        AclStatusResult aclGetResponse = dataLakeStoreFileSystemManagementClient.fileSystems().getAclStatus(adlsAcct, "/").getBody();

        Assert.assertNotNull(aclGetResponse.aclStatus());
        Assert.assertTrue(aclGetResponse.aclStatus().entries().size() > 0);
        Assert.assertTrue(aclGetResponse.aclStatus().owner() != null && StringUtils.isNotEmpty(aclGetResponse.aclStatus().owner()));
        Assert.assertTrue(aclGetResponse.aclStatus().group() != null && StringUtils.isNotEmpty(aclGetResponse.aclStatus().group()));
    }

    @Test
    public void DataLakeStoreFileSystemSetAcl() throws Exception
    {
        AclStatusResult aclGetResponse = dataLakeStoreFileSystemManagementClient.fileSystems().getAclStatus(adlsAcct, "/").getBody();

        Assert.assertNotNull(aclGetResponse.aclStatus());
        Assert.assertTrue(aclGetResponse.aclStatus().entries().size() > 0);

        int currentCount = aclGetResponse.aclStatus().entries().size();

        // add an entry to the ACL Entries
        String newAcls = StringUtils.join(aclGetResponse.aclStatus().entries(), ",");
        String aclUserId = UUID.randomUUID().toString();
        newAcls += String.format(",user:%s:rwx", aclUserId);

        dataLakeStoreFileSystemManagementClient.fileSystems().setAcl(adlsAcct, 
                "/",
                newAcls);

        // retrieve the ACL again and confirm the new entry is present
        aclGetResponse = dataLakeStoreFileSystemManagementClient.fileSystems().getAclStatus(adlsAcct, "/").getBody();

        Assert.assertNotNull(aclGetResponse.aclStatus());
        Assert.assertTrue(aclGetResponse.aclStatus().entries().size() > 0);
        Assert.assertEquals(currentCount + 1, aclGetResponse.aclStatus().entries().size());

        boolean found = false;
        for (String entry:  aclGetResponse.aclStatus().entries()) {
            if(entry.contains(aclUserId)) {
                found = true;
                break;
            }
        }

        Assert.assertTrue(found);
    }

    @Test
    public void DataLakeStoreFileSystemSetDeleteAclEntry() throws Exception
    {
        AclStatusResult aclGetResponse = dataLakeStoreFileSystemManagementClient.fileSystems().getAclStatus(adlsAcct, "/").getBody();

        Assert.assertNotNull(aclGetResponse.aclStatus());
        Assert.assertTrue(aclGetResponse.aclStatus().entries().size() > 0);

        int currentCount = aclGetResponse.aclStatus().entries().size();
        // add an entry to the ACL Entries
        String aclUserId = UUID.randomUUID().toString();
        String newAce = String.format("user:%s:rwx", aclUserId);

        dataLakeStoreFileSystemManagementClient.fileSystems().modifyAclEntries(adlsAcct, "",
                newAce);

        // retrieve the ACL again and confirm the new entry is present
        aclGetResponse = dataLakeStoreFileSystemManagementClient.fileSystems().getAclStatus(adlsAcct, "/").getBody();

        Assert.assertNotNull(aclGetResponse.aclStatus());
        Assert.assertTrue(aclGetResponse.aclStatus().entries().size() > 0);
        Assert.assertEquals(currentCount + 1, aclGetResponse.aclStatus().entries().size());

        boolean found = false;
        for (String entry:  aclGetResponse.aclStatus().entries()) {
            if(entry.contains(aclUserId)) {
                found = true;
                break;
            }
        }

        Assert.assertTrue(found);

        // now remove the entry
        String aceToRemove = String.format(",user:%s", aclUserId);
        dataLakeStoreFileSystemManagementClient.fileSystems().removeAclEntries(
                adlsAcct, 
                "/",
                aceToRemove);

        // retrieve the ACL again and confirm the new entry is present
        aclGetResponse = dataLakeStoreFileSystemManagementClient.fileSystems().getAclStatus(adlsAcct, "/").getBody();

        Assert.assertNotNull(aclGetResponse.aclStatus());
        Assert.assertTrue(aclGetResponse.aclStatus().entries().size() > 0);
        Assert.assertEquals(currentCount, aclGetResponse.aclStatus().entries().size());

        found = false;
        for (String entry:  aclGetResponse.aclStatus().entries()) {
            if(entry.contains(aclUserId)) {
                found = true;
                break;
            }
        }

        Assert.assertFalse(found);
    }
    
    // helper methods
    private String CreateFolder(String caboAccountName, boolean randomName) throws Exception
    {
        // Create a folder
        String folderPath = randomName
                ? generateName(folderToCreate)
                : folderToCreate;

        FileOperationResult response = dataLakeStoreFileSystemManagementClient.fileSystems().mkdirs(caboAccountName, folderPath).getBody();
        Assert.assertTrue(response.operationResult());

        return folderPath;
    }

    private String CreateFile(String caboAccountName, boolean withContents, boolean randomName, String folderName) throws Exception
    {
        String fileToCreate = "SDKTestFile01.txt";
        String filePath = randomName ? generateName(String.format("%s/%s", folderName, fileToCreate)) : String.format("%s/%s", folderName, fileToCreate);

        if (!withContents)
        {
            dataLakeStoreFileSystemManagementClient.fileSystems().create(
                    caboAccountName,
                    filePath);
        }
        else
        {
            dataLakeStoreFileSystemManagementClient.fileSystems().create(
                    caboAccountName,
                    filePath,
                    fileContentsToAdd.getBytes(),
                    true);
        }

        return filePath;
    }

    private FileStatusResult GetAndCompareFileOrFolder(String caboAccountName, String fileOrFolderPath, FileType expectedType, long expectedLength) throws Exception
    {
        FileStatusResult getResponse = dataLakeStoreFileSystemManagementClient.fileSystems().getFileStatus(caboAccountName, fileOrFolderPath).getBody();
        Assert.assertEquals(expectedLength, (long) getResponse.fileStatus().length());
        Assert.assertEquals(expectedType, getResponse.fileStatus().type());

        return getResponse;
    }

    private void CompareFileContents(String caboAccountName, String filePath, String expectedContents) throws Exception
    {
        // download a file and ensure they are equal
        InputStream openResponse = dataLakeStoreFileSystemManagementClient.fileSystems().open(caboAccountName, filePath).getBody();
        Assert.assertNotNull(openResponse);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(
                    new InputStreamReader(openResponse, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        }
        finally {
            openResponse.close();
        }
        String fileContents =  writer.toString();
        Assert.assertEquals(expectedContents, fileContents);
    }

    private void DeleteFolder(String caboAccountName, String folderPath, boolean recursive, boolean failureExpected) throws Exception
    {
        if (failureExpected)
        {
            // try to delete a folder that doesn't exist or should fail
            try
            {
                FileOperationResult deleteFolderResponse = dataLakeStoreFileSystemManagementClient.fileSystems().delete(caboAccountName, folderPath, recursive).getBody();
                Assert.assertTrue(!deleteFolderResponse.operationResult());
            }
            catch (Exception e)
            {
                Assert.assertTrue(e instanceof CloudException);
            }
        }
        else
        {
            // Delete a folder
            FileOperationResult deleteFolderResponse = dataLakeStoreFileSystemManagementClient.fileSystems().delete(caboAccountName, folderPath, recursive).getBody();
            Assert.assertTrue(deleteFolderResponse.operationResult());
        }
    }

    private void DeleteFile(String caboAccountName, String filePath, boolean failureExpected) throws Exception
    {
        if (failureExpected)
        {
            // try to delete a file that doesn't exist
            try
            {
                FileOperationResult deleteFileResponse = dataLakeStoreFileSystemManagementClient.fileSystems().delete(caboAccountName, filePath, false).getBody();
                Assert.assertTrue(!deleteFileResponse.operationResult());
            }
            catch (Exception e)
            {
                Assert.assertTrue(e instanceof CloudException);
            }
        }
        else
        {
            // Delete a file
            FileOperationResult deleteFileResponse = dataLakeStoreFileSystemManagementClient.fileSystems().delete(caboAccountName, filePath, false).getBody();
            Assert.assertTrue(deleteFileResponse.operationResult());
        }
    }
}
