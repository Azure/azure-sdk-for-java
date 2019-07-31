// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
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
import java.util.Iterator;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import static com.azure.storage.file.FileTestHelpers.setupClient;
import static org.junit.Assert.assertTrue;

public class DirectoryClientTests extends DirectoryClientTestBase {
    private final ClientLogger directoryLogger = new ClientLogger(DirectoryClientTests.class);
    private static String shareName = "dirsharename";
    private static ShareClient shareClient;
    private DirectoryClient directoryClient;

    @Override
    public void beforeTest() {
        beforeDirectoryTest();
        if (interceptorManager.isPlaybackMode()) {
            directoryClient = setupClient((connectionString, endpoint) -> new DirectoryClientBuilder()
                             .connectionString(connectionString)
                             .shareName(shareName)
                             .directoryPath(dirName)
                             .httpClient(interceptorManager.getPlaybackClient())
                             .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                             .buildClient(), true, directoryLogger);
        } else {
            directoryClient = setupClient((connectionString, endpoint) -> new DirectoryClientBuilder()
                             .connectionString(connectionString)
                             .shareName(shareName)
                             .directoryPath(dirName)
                             .httpClient(HttpClient.createDefault().wiretap(true))
                             .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                             .addPolicy(interceptorManager.getRecordPolicy())
                             .buildClient(), false, directoryLogger);
        }
    }

    @BeforeClass
    public static void beforeClass() {
        if (FileTestHelpers.getTestMode() == TestMode.PLAYBACK) {
            return;
        }
        FileServiceClient fileServiceClient = new FileServiceClientBuilder()
                                                  .connectionString(ConfigurationManager.getConfiguration().get("AZURE_STORAGE_CONNECTION_STRING"))
                                                  .buildClient();
        shareClient = fileServiceClient.getShareClient(shareName);
        shareClient.create();
    }

    @AfterClass
    public static void tearDown() {
        if (FileTestHelpers.getTestMode() == TestMode.PLAYBACK) {
            return;
        }
        shareClient.delete();
        FileTestHelpers.sleepInRecordMode(Duration.ofSeconds(45));
    }

    @Override
    public void urlFromDirClient() {
        if (interceptorManager.isPlaybackMode()) {
            azureStorageFileEndpoint = "https://teststorage.file.core.windows.net/";
        }
        UrlBuilder urlBuilder = UrlBuilder.parse(azureStorageFileEndpoint);
        String endpointURL = new UrlBuilder().scheme(urlBuilder.scheme()).host(urlBuilder.host()).toString();
        Assert.assertEquals(endpointURL, directoryClient.getDirectoryUrl().toString());
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
        String fileNameSameLayer = testResourceNamer.randomName("sameLayer", 16);
        String fileNameNextLayer = testResourceNamer.randomName("nextLayer", 16);
        for (int i = 0; i < repeatTimes; i++) {
            DirectoryClient subDirClient = directoryClient.getSubDirectoryClient(dirName + i);
            FileTestHelpers.assertResponseStatusCode(subDirClient.create(), 201);
            fileNameSameLayer = fileNameSameLayer + i;
            fileNameNextLayer = fileNameNextLayer + i;
            FileTestHelpers.assertResponseStatusCode(directoryClient.createFile(fileNameSameLayer, 1024), 201);
            FileTestHelpers.assertResponseStatusCode(subDirClient.createFile(fileNameNextLayer, 1024), 201);
        }

        Iterable<FileRef> fileRefs = directoryClient.listFilesAndDirectories();

        int count = 0;
        Iterator<FileRef> it = fileRefs.iterator();
        while (it.hasNext()) {
            count++;
            FileRef ref = it.next();
            Assert.assertNotNull(ref);
            if (!ref.isDirectory()) {
                assertTrue("It is supposed to list the files in same layer.", ref.name().contains("samelayer"));
            }
        }
        Assert.assertEquals(repeatTimes * 2, count);
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
                    numberOfClosedHandles -> assertTrue(numberOfClosedHandles > 0)
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
