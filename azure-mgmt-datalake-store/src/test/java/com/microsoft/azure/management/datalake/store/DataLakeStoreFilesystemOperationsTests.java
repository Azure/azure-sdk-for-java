package com.microsoft.azure.management.datalake.store;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.datalake.store.models.AclStatusResult;
import com.microsoft.azure.management.datalake.store.models.DataLakeStoreAccount;
import com.microsoft.azure.management.datalake.store.models.FileOperationResult;
import com.microsoft.azure.management.datalake.store.models.FileStatusProperties;
import com.microsoft.azure.management.datalake.store.models.FileStatusResult;
import com.microsoft.azure.management.datalake.store.models.FileStatusesResult;
import com.microsoft.azure.management.datalake.store.models.FileType;
import com.microsoft.azure.management.resources.models.ResourceGroup;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
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

    private static String rgName = generateName("javaadlsrg");
    private static String location = "eastus2";
    private static String adlsAcct = generateName("javaadlsacct");
    private static String aclUserId = "027c28d5-c91d-49f0-98c5-d10134b169b3";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
        ResourceGroup group = new ResourceGroup();
        group.setLocation(location);
        resourceManagementClient.getResourceGroupsOperations().createOrUpdate(rgName, group);

        // create storage and ADLS accounts, setting the accessKey
        DataLakeStoreAccount adlsAccount = new DataLakeStoreAccount();
        adlsAccount.setLocation(location);
        adlsAccount.setName(adlsAcct);
        dataLakeStoreAccountManagementClient.getAccountOperations().create(rgName, adlsAcct, adlsAccount);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
            resourceManagementClient.getResourceGroupsOperations().delete(rgName);
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
        var fileInfo = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().GetFileInfo(filePath, adlsAcct);
        Assert.assertTrue(fileInfo.FileInfo.ExpirationTime <= 0 || fileInfo.FileInfo.ExpirationTime == maxTimeInMilliseconds, "Expiration time was not equal to 0 or DateTime.MaxValue.Ticks! Actual value reported: " + fileInfo.FileInfo.ExpirationTime);

        // set the expiration time as an absolute value

        var toSetAbsolute = ToUnixTimeStampMs(HttpMockServer.GetVariable("absoluteTime", DateTime.Now.AddSeconds(120).ToString()));
        dataLakeStoreFileSystemManagementClient.getFileSystemOperations().SetFileExpiry(filePath, ExpiryOptionType.Absolute, adlsAcct, toSetAbsolute);
        fileInfo = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().GetFileInfo(filePath, adlsAcct);
        VerifyTimeInAcceptableRange(toSetAbsolute, fileInfo.FileInfo.ExpirationTime.Value);

        // set the expiration time relative to now
        var toSetRelativeToNow = ToUnixTimeStampMs(HttpMockServer.GetVariable("relativeTime", DateTime.Now.AddSeconds(120).ToString()));
        dataLakeStoreFileSystemManagementClient.getFileSystemOperations().SetFileExpiry(filePath, ExpiryOptionType.RelativeToNow, adlsAcct, 120 * 1000);
        fileInfo = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().GetFileInfo(filePath, adlsAcct);
        VerifyTimeInAcceptableRange(toSetRelativeToNow, fileInfo.FileInfo.ExpirationTime.Value);

        // set expiration time relative to the creation time
        var toSetRelativeCreationTime = fileInfo.FileInfo.CreationTime.Value + (120 * 1000);
        dataLakeStoreFileSystemManagementClient.getFileSystemOperations().SetFileExpiry(filePath, ExpiryOptionType.RelativeToCreationDate, adlsAcct, 120 * 1000);
        fileInfo = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().GetFileInfo(filePath, adlsAcct);
        VerifyTimeInAcceptableRange(toSetRelativeCreationTime, fileInfo.FileInfo.ExpirationTime.Value);

        // reset expiration time to never
        dataLakeStoreFileSystemManagementClient.getFileSystemOperations().SetFileExpiry(filePath, ExpiryOptionType.NeverExpire, adlsAcct);
        fileInfo = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().GetFileInfo(filePath, adlsAcct);
        Assert.assertTrue(fileInfo.FileInfo.ExpirationTime <= 0 || fileInfo.FileInfo.ExpirationTime == maxTimeInMilliseconds, "Expiration time was not equal to 0 or DateTime.MaxValue.Ticks! Actual value reported: " + fileInfo.FileInfo.ExpirationTime);
    }

    @Test
    public void DataLakeStoreFileSystemNegativeExpiry()
    {
        const long maxTimeInMilliseconds = 253402300800000;
        var filePath = CreateFile(adlsAcct, false, true);
        GetAndCompareFileOrFolder(adlsAcct, filePath, FileType.FILE, 0);

        // verify it does not have an expiration
        var fileInfo = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().GetFileInfo(filePath, adlsAcct);
        Assert.assertTrue(fileInfo.FileInfo.ExpirationTime <= 0 || fileInfo.FileInfo.ExpirationTime == maxTimeInMilliseconds, "Expiration time was not equal to 0 or DateTime.MaxValue.Ticks! Actual value reported: " + fileInfo.FileInfo.ExpirationTime);

        // set the expiration time as an absolute value that is less than the creation time
        var toSetAbsolute = ToUnixTimeStampMs(HttpMockServer.GetVariable("absoluteNegativeTime", DateTime.Now.AddSeconds(-120).ToString()));
        Assert.assertThrows<CloudException>(() => dataLakeStoreFileSystemManagementClient.getFileSystemOperations().SetFileExpiry(filePath, ExpiryOptionType.Absolute, adlsAcct, toSetAbsolute));

        // set the expiration time as an absolute value that is greater than max allowed time
        toSetAbsolute = ToUnixTimeStampMs(DateTime.MaxValue.ToString()) + 1000;
        Assert.assertThrows<CloudException>(() => dataLakeStoreFileSystemManagementClient.getFileSystemOperations().SetFileExpiry(filePath, ExpiryOptionType.Absolute, adlsAcct, toSetAbsolute));

        // reset expiration time to never with a value and confirm the value is not honored
        dataLakeStoreFileSystemManagementClient.getFileSystemOperations().SetFileExpiry(filePath, ExpiryOptionType.NeverExpire, adlsAcct, 400);
        fileInfo = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().GetFileInfo(filePath, adlsAcct);
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
        FileStatusesResult listFolderResponse = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().listFileStatus(folderPath, adlsAcct, null).getBody();

        // We know that this directory is brand new, so the contents should only be the one file.
        Assert.assertEquals(1, listFolderResponse.getFileStatuses().getFileStatus().size());
        Assert.assertEquals(FileType.FILE, listFolderResponse.getFileStatuses().getFileStatus().get(0).getType());
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
        dataLakeStoreFileSystemManagementClient.getFileSystemOperations().append(filePath,
            adlsAcct,
            new ByteArrayInputStream(fileContentsToAdd.getBytes()), null, "true");

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

        dataLakeStoreFileSystemManagementClient.getFileSystemOperations().concat(
                String.format("{0}/{1}", targetFolder, fileToConcatTo),
                adlsAcct,
                Arrays.asList(new String[]{filePath1, filePath2}), null
        );

        GetAndCompareFileOrFolder(adlsAcct,
                String.format("{0}/{1}", targetFolder, fileToConcatTo),
                FileType.FILE,
                fileContentsToAdd.length() * 2);

        // Attempt to get the files that were concatted together, which should fail and throw
        try {
            dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getFileStatus(filePath1,
                    adlsAcct, null);
            Assert.assertTrue("Able to get the old file after concat", false);
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof CloudException);
        }

        try {
            dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getFileStatus(filePath2,
                    adlsAcct, null);
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

        dataLakeStoreFileSystemManagementClient.getFileSystemOperations().msConcat(
                String.format("{0}/{1}", targetFolder, fileToConcatTo),
                adlsAcct,
                new ByteArrayInputStream(String.format("sources={0},{1}", filePath1, filePath2).getBytes()),
                null,
                false);

        GetAndCompareFileOrFolder(adlsAcct,
                String.format("{0}/{1}", targetFolder, fileToConcatTo),
                FileType.FILE,
                fileContentsToAdd.length() * 2);

        // Attempt to get the files that were concatted together, which should fail and throw
        try {
            dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getFileStatus(filePath1,
                    adlsAcct, null);
            Assert.assertTrue("Able to get the old file after concat", false);
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof CloudException);
        }

        try {
            dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getFileStatus(filePath2,
                    adlsAcct, null);
            Assert.assertTrue("Able to get the old file after concat", false);
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof CloudException);
        }
    }

    @Test
    public void DataLakeStoreFileSystemMsConcatDeleteDir() throws Exception
    {
        String concatFolderPath = String.format("{0}/{1}", folderToCreate,
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

        dataLakeStoreFileSystemManagementClient.getFileSystemOperations().msConcat(
                String.format("{0}/{1}", targetFolder, fileToConcatTo),
                adlsAcct,
                new ByteArrayInputStream(String.format("sources={0},{1}", filePath1, filePath2).getBytes()),
                null,
                true);

        GetAndCompareFileOrFolder(adlsAcct,
                String.format("{0}/{1}", targetFolder, fileToConcatTo),
                FileType.FILE,
                fileContentsToAdd.length()*2);

        // Attempt to get the files that were concatted together, which should fail and throw
        try {
            dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getFileStatus(filePath1, adlsAcct, null);
            Assert.assertTrue("Able to get the old file after concat", false);
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof CloudException);
        }

        try {
            dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getFileStatus(filePath2, adlsAcct, null);
            Assert.assertTrue("Able to get the old file after concat", false);
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof CloudException);
        }

        // Attempt to get the folder that was created for concat, which should fail and be deleted.
        try {
            dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getFileStatus(concatFolderPath, adlsAcct, null);
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
        String targetFolder2 = generateName(folderToMove);

        // Move file first
        FileOperationResult moveFileResponse = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().rename(filePath,
                adlsAcct,
                String.format("{0}/{1}", targetFolder1, fileToMove),
                null).getBody();
        Assert.assertTrue(moveFileResponse.getBooleanProperty());
        GetAndCompareFileOrFolder(adlsAcct,
                String.format("{0}/{1}", targetFolder1, fileToMove),
                FileType.FILE,
                fileContentsToAdd.length());

        // Ensure the old file is gone
        try {
            dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getFileStatus(filePath, adlsAcct, null);
            Assert.assertTrue("Able to get the old file after rename", false);
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof CloudException);
        }

        // Now move folder completely.
        FileOperationResult moveFolderResponse = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().rename(targetFolder1,
                adlsAcct,
                targetFolder2,
                null).getBody();
        Assert.assertTrue(moveFolderResponse.getBooleanProperty());

        GetAndCompareFileOrFolder(adlsAcct, targetFolder2,
                FileType.DIRECTORY, 0);

        // ensure all the contents of the folder moved
        // List all the contents in the folder
        FileStatusesResult listFolderResponse = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().listFileStatus(targetFolder2,
                adlsAcct, null).getBody();

        // We know that this directory is brand new, so the contents should only be the one file.
        Assert.assertEquals(1, listFolderResponse.getFileStatuses().getFileStatus().size());
        Assert.assertEquals(FileType.FILE, listFolderResponse.getFileStatuses().getFileStatus().get(0).getType());

        try {
            dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getFileStatus(targetFolder1, adlsAcct, null);
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
        AclStatusResult currentAcl = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getAclStatus("/",
                adlsAcct, null).getBody();

        List<String> aclToReplaceWith = new ArrayList<String>(currentAcl.getAclStatus().getEntries());
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
        dataLakeStoreFileSystemManagementClient.getFileSystemOperations().setAcl("/",
                adlsAcct,
                String.join(",", aclToReplaceWith),
                null);

        AclStatusResult newAcl = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getAclStatus("/",
                adlsAcct,
                null).getBody();
        // verify the ACL actually changed

        // Check the access first and assert that it returns OK (note: this is currently only for the user making the request, so it is not testing "other")
        dataLakeStoreFileSystemManagementClient.getFileSystemOperations().checkAccess("/",
                adlsAcct, null, "rwx");

        boolean foundIt = false;
        for (String entry: newAcl.getAclStatus().getEntries())
        {
            if(entry.startsWith("other")) {
                Assert.assertEquals(toReplace, entry);
                foundIt = true;
                break;
            }
        }

        Assert.assertTrue(foundIt);

        // Set it back using specific entry
        dataLakeStoreFileSystemManagementClient.getFileSystemOperations().modifyAclEntries("/",
                adlsAcct, originalOther, null);

        // Now confirm that it equals the original ACL
        List<String> finalEntries = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getAclStatus("/",
                adlsAcct,
                null).getBody()
                .getAclStatus().getEntries();
        for (String entry: finalEntries)
        {
            boolean found = false;
            for(String curEntry: currentAcl.getAclStatus().getEntries()) {
                if(curEntry.toUpperCase().equals(entry.toUpperCase())) {
                    found = true;
                    break;
                }
            }

            Assert.assertTrue(found);
        }

        Assert.assertEquals(finalEntries.size(), currentAcl.getAclStatus().getEntries().size());
    }

    @Test
    public void DataLakeStoreFileSystemSetFileProperties() throws Exception
    {
        // This test simply tests that all bool/empty return actions return successfully

        String filePath = CreateFile(adlsAcct, true, true, folderToCreate);
        FileStatusProperties originalFileStatus =
                dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getFileStatus(filePath,
                        adlsAcct, null).getBody().getFileStatus();
        // TODO: Set replication on file, this has been removed until it is confirmed as a supported API.
            /*
            var replicationResponse = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().SetReplication(filePath,
                adlsAcct, 3);
            Assert.assertTrue(replicationResponse.Boolean);
            */

            /*
         * This API is available but all values put into it are ignored. Commenting this out until this API is fully functional.
        Assert.assertEquals(3,
            dataLakeFileSystemClient.FileSystem.getFileStatus(filePath, adlsAcct)
                .FileStatus.Replication);
        */

        // set the time on the file
        // We use a static date for now since we aren't interested in whether the value is set properly, only that the method returns a 200.
            /* TODO: Re enable once supported.
            var timeToSet = new DateTime(2015, 10, 26, 14, 30, 0).Ticks;
            dataLakeStoreFileSystemManagementClient.getFileSystemOperations().SetTimes(filePath,
                adlsAcct, timeToSet, timeToSet);

            var fileStatusAfterTime =
                dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getFileStatus(filePath,
                    adlsAcct)
                    .FileStatus;
            */

            /*
         * This API is available but all values put into it are ignored. Commenting this out until this API is fully functional.
        Assert.assertTrue(
            fileStatusAfterTime.ModificationTime == timeToSet && fileStatusAfterTime.AccessTime == timeToSet);
        */

        // TODO: Symlink creation is explicitly not supported, but when it is this should be enabled.
            /*
            var symLinkName = generateName("testPath/symlinktest1");
            Assert.assertThrows<CloudException>(() => dataLakeStoreFileSystemManagementClient.getFileSystemOperations().CreateSymLink(filePath,
                adlsAcct, symLinkName, true));
            */

        // Once symlinks are available, remove the throws test and uncomment out this code.
        // Assert.assertTrue(createSymLinkResponse.StatusCode == HttpStatusCode.OK);
        // Assert.assertDoesNotThrow(() => dataLakeFileSystemClient.FileSystem.getFileStatus(symLinkName, adlsAcct));
    }

    @Test
    public void DataLakeStoreFileSystemGetAcl() throws Exception
    {
        AclStatusResult aclGetResponse = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getAclStatus("/",
                adlsAcct, null).getBody();

        Assert.assertNotNull(aclGetResponse.getAclStatus());
        Assert.assertTrue(aclGetResponse.getAclStatus().getEntries().size() > 0);
        Assert.assertTrue(aclGetResponse.getAclStatus().getOwner() != null && StringUtils.isNotEmpty(aclGetResponse.getAclStatus().getOwner()));
        Assert.assertTrue(aclGetResponse.getAclStatus().getGroup() != null && StringUtils.isNotEmpty(aclGetResponse.getAclStatus().getGroup()));
    }

    @Test
    public void DataLakeStoreFileSystemSetAcl() throws Exception
    {
        AclStatusResult aclGetResponse = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getAclStatus("/", adlsAcct, null).getBody();

        Assert.assertNotNull(aclGetResponse.getAclStatus());
        Assert.assertTrue(aclGetResponse.getAclStatus().getEntries().size() > 0);

        int currentCount = aclGetResponse.getAclStatus().getEntries().size();

        // add an entry to the ACL Entries
        String newAcls = String.join(",", aclGetResponse.getAclStatus().getEntries());
        newAcls += String.format(",user:{0}:rwx", aclUserId);

        dataLakeStoreFileSystemManagementClient.getFileSystemOperations().setAcl("/",
                adlsAcct,
                newAcls,
                null);

        // retrieve the ACL again and confirm the new entry is present
        aclGetResponse = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getAclStatus("/",
                adlsAcct, null).getBody();

        Assert.assertNotNull(aclGetResponse.getAclStatus());
        Assert.assertTrue(aclGetResponse.getAclStatus().getEntries().size() > 0);
        Assert.assertEquals(currentCount + 1, aclGetResponse.getAclStatus().getEntries().size());

        boolean found = false;
        for (String entry:  aclGetResponse.getAclStatus().getEntries()) {
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
        AclStatusResult aclGetResponse = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getAclStatus("/", adlsAcct, null).getBody();

        Assert.assertNotNull(aclGetResponse.getAclStatus());
        Assert.assertTrue(aclGetResponse.getAclStatus().getEntries().size() > 0);

        int currentCount = aclGetResponse.getAclStatus().getEntries().size();
        // add an entry to the ACL Entries
        String newAce = String.format("user:{0}:rwx", aclUserId);

        dataLakeStoreFileSystemManagementClient.getFileSystemOperations().modifyAclEntries("/",
                adlsAcct,
                newAce,
                null);

        // retrieve the ACL again and confirm the new entry is present
        aclGetResponse = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getAclStatus("/",
                adlsAcct,
                null).getBody();

        Assert.assertNotNull(aclGetResponse.getAclStatus());
        Assert.assertTrue(aclGetResponse.getAclStatus().getEntries().size() > 0);
        Assert.assertEquals(currentCount + 1, aclGetResponse.getAclStatus().getEntries().size());

        boolean found = false;
        for (String entry:  aclGetResponse.getAclStatus().getEntries()) {
            if(entry.contains(aclUserId)) {
                found = true;
                break;
            }
        }

        Assert.assertTrue(found);

        // now remove the entry
        String aceToRemove = String.format(",user:{0}", aclUserId);
        dataLakeStoreFileSystemManagementClient.getFileSystemOperations().removeAclEntries("/",
                adlsAcct,
                aceToRemove,
                null);

        // retrieve the ACL again and confirm the new entry is present
        aclGetResponse = dataLakeStoreFileSystemManagementClient.getFileSystemOperations().getAclStatus("/",
                adlsAcct,
                null).getBody();

        Assert.assertNotNull(aclGetResponse.getAclStatus());
        Assert.assertTrue(aclGetResponse.getAclStatus().getEntries().size() > 0);
        Assert.assertEquals(currentCount, aclGetResponse.getAclStatus().getEntries().size());

        found = false;
        for (String entry:  aclGetResponse.getAclStatus().getEntries()) {
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
        Assert.assertEquals(expectedLength, (long) getResponse.getFileStatus().getLength());
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
