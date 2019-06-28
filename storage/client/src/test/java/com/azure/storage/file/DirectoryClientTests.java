package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.test.TestMode;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.models.FileRef;
import com.azure.storage.file.models.StorageErrorException;
import java.time.Duration;
import org.junit.AfterClass;
import org.junit.Assert;

import java.util.Iterator;
import org.junit.BeforeClass;

import static com.azure.storage.file.FileTestHelpers.setupClient;

public class DirectoryClientTests extends DirectoryClientTestBase {
    private final ClientLogger directoryLogger = new ClientLogger(DirectoryClientTests.class);
    private static String shareName = "dirsharename";
    private static ShareClient shareClient;
    private DirectoryClient directoryClient;

    @Override
    public void beforeTest() {
        beforeDirectoryTest();
        if (interceptorManager.isPlaybackMode()) {
            directoryClient = setupClient((connectionString, endpoint) -> DirectoryClient.builder()
                             .connectionString(connectionString)
                             .endpoint(endpoint)
                             .shareName(shareName)
                             .directoryName(dirName)
                             .httpClient(interceptorManager.getPlaybackClient())
                             .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                             .build(), true, directoryLogger);
        } else {
            directoryClient = setupClient((connectionString, endpoint) -> DirectoryClient.builder()
                             .connectionString(connectionString)
                             .endpoint(endpoint)
                             .shareName(shareName)
                             .directoryName(dirName)
                             .httpClient(HttpClient.createDefault().wiretap(true))
                             .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                             .addPolicy(interceptorManager.getRecordPolicy())
                             .build(), false, directoryLogger);
        }
    }

    @BeforeClass
    public static void beforeClass() {
        if (FileTestHelpers.getTestMode() == TestMode.PLAYBACK) {
            return;
        }
        FileServiceClient fileServiceClient = FileServiceClient.builder()
                                                  .connectionString(ConfigurationManager.getConfiguration().get("AZURE_STORAGE_CONNECTION_STRING"))
                                                  .build();
        shareClient = fileServiceClient.getShareClient(shareName);
        shareClient.create();
    }

    @AfterClass
    public static void tearDown() {
        if (FileTestHelpers.getTestMode() == TestMode.PLAYBACK) {
            return;
        }
        shareClient.delete(null);
        FileTestHelpers.sleep(Duration.ofSeconds(45));
    }

    @Override
    public void urlFromDirClient() {
        if (interceptorManager.isPlaybackMode()) {
            azureStorageFileEndpoint = "https://teststorage.file.core.windows.net/";
        }
        UrlBuilder urlBuilder = UrlBuilder.parse(azureStorageFileEndpoint);
        String endpointURL = new UrlBuilder().scheme(urlBuilder.scheme()).host(urlBuilder.host()).toString();
        Assert.assertEquals(endpointURL, directoryClient.getDirectoryUrl());
    }

    @Override
    public void createMinFromDirClient() {
        FileTestHelpers.assertResponseStatusCode(directoryClient.create(), 201);
    }

    @Override
    public void createTwiceFromDirClient() {
        directoryClient.create();
        thrown.expect(StorageErrorException.class);
        thrown.expectMessage("ResourceAlreadyExists");
        directoryClient.create();
    }

    @Override
    public void createWithMetadataFromDirClient() {
        FileTestHelpers.assertResponseStatusCode(directoryClient.create(basicMetadata), 201);
    }

    @Override
    public void getFileClientFromDirClient() {
        Assert.assertNotNull(directoryClient.getFileClient(testResourceNamer.randomName("getFileSync", 16)));
    }

    @Override
    public void getSubDirectoryClient() {
        Assert.assertNotNull(directoryClient.getSubDirectoryClient("getSubdirectorySync"));
    }

    @Override
    public void deleteFromDirClient() {
        directoryClient.create();
        FileTestHelpers.assertResponseStatusCode(directoryClient.delete(), 202);
    }

    @Override
    public void deleteNotExistFromDirClient() {
        thrown.expect(StorageErrorException.class);
        thrown.expectMessage("ResourceNotFound");
        directoryClient.delete();
    }

    @Override
    public void getPropertiesFromDirClient() {
        directoryClient.create();
        FileTestHelpers.assertResponseStatusCode(directoryClient.getProperties(), 200);
    }

    @Override
    public void clearMetadataFromDirClient() {
        directoryClient.create();
        FileTestHelpers.assertResponseStatusCode(directoryClient.setMetadata(null), 200);
    }

    @Override
    public void setMetadataFromDirClient() {
        directoryClient.create();
        FileTestHelpers.assertResponseStatusCode(directoryClient.setMetadata(basicMetadata), 200);
    }

    @Override
    public void setMetadataInvalidKeyFromDirClient() {
        directoryClient.create();
        thrown.expect(StorageErrorException.class);
        thrown.expectMessage("InvalidMetadata");
        directoryClient.setMetadata(invalidMetadata);
    }

    @Override
    public void listFilesAndDirectoriesFromDirClient() {
        FileTestHelpers.assertResponseStatusCode(directoryClient.create(), 201);
        int repeatTimes = 3;
        // generate 100 directories
        for (int i = 0; i < repeatTimes; i++) {
            FileTestHelpers.assertResponseStatusCode(directoryClient.createSubDirectory(dirName + i), 201);
            //TODO: create files
        }
        Iterable<FileRef> fileRefs = directoryClient.listFilesAndDirectories(dirName, null);
        int count = 0;
        Iterator<FileRef> it = fileRefs.iterator();
        while (it.hasNext()) {
            count++;
            Assert.assertNotNull(it.next());
        }
        System.out.println(count);
        Assert.assertEquals(count, repeatTimes);
    }

    @Override
    public void getHandlesFromDirClient() {
        // TODO: Need to open channel and create handlers first.
//        StorageTestBase.assertResponseStatusCode(directoryClient.create(), 201);
//        Assert.assertTrue(directoryClient.getHandles(null, true).iterator().hasNext());
    }

    @Override
    public void forceCloseHandlesFromDirClient() {
        FileTestHelpers.assertResponseStatusCode(directoryClient.create(), 201);
        directoryClient.getHandles(null, true).forEach(
            handleItem -> {
                directoryClient.forceCloseHandles(handleItem.handleId(), true).forEach(
                    numberOfClosedHandles -> Assert.assertTrue(numberOfClosedHandles > 0)
                );
            }
        );
    }

    @Override
    public void createSubDirectory() {
        FileTestHelpers.assertResponseStatusCode(directoryClient.create(), 201);
        FileTestHelpers.assertResponseStatusCode(directoryClient.createSubDirectory(testResourceNamer.randomName("directory", 16)), 201);
    }

    @Override
    public void createSubDirectoryWithMetadata() {
        FileTestHelpers.assertResponseStatusCode(directoryClient.create(), 201);
        FileTestHelpers.assertResponseStatusCode(directoryClient.createSubDirectory(testResourceNamer.randomName("directory", 16), basicMetadata), 201);
    }

    @Override
    public void createSubDirectoryTwiceSameMetadata() {
        directoryClient.create();
        String dirName = testResourceNamer.randomName("dir", 16);
        FileTestHelpers.assertResponseStatusCode(directoryClient.createSubDirectory(dirName, basicMetadata), 201);
        thrown.expect(StorageErrorException.class);
        thrown.expectMessage("ResourceAlreadyExists");
        directoryClient.createSubDirectory(dirName, basicMetadata);
    }


    @Override
    public void deleteSubDirectory() {
        directoryClient.create();
        String dirName = testResourceNamer.randomName("dir", 16);
        FileTestHelpers.assertResponseStatusCode(directoryClient.createSubDirectory(dirName), 201);
        FileTestHelpers.assertResponseStatusCode(directoryClient.deleteSubDirectory(dirName), 202);
    }

    @Override
    public void createFileFromDirClient() {
        directoryClient.create();
        FileTestHelpers.assertResponseStatusCode(directoryClient.createFile("testfile", 1024), 201);
    }

    @Override
    public void createFileWithoutCreateDirFromDirClient() {
        thrown.expect(StorageErrorException.class);
        thrown.expectMessage("ParentNotFound");
        directoryClient.createFile("testfile", 1024);
    }

    @Override
    public void deleteFileFromDirClient() {
        directoryClient.create();
        directoryClient.createFile("testfile", 1024);
        FileTestHelpers.assertResponseStatusCode(directoryClient.deleteFile("testfile"), 202);
    }

    @Override
    public void deleteFileWithoutCreateFileFromDirClient() {
        directoryClient.create();
        thrown.expect(StorageErrorException.class);
        thrown.expectMessage("ResourceNotFound");
        directoryClient.deleteFile("testfile");
    }

}
