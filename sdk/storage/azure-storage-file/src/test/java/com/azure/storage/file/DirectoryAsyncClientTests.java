// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.test.TestMode;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.models.FileProperty;
import com.azure.storage.file.models.FileRef;
import com.azure.storage.file.models.HandleItem;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import reactor.test.StepVerifier;

import static com.azure.storage.file.FileTestHelpers.setupClient;

public class DirectoryAsyncClientTests extends DirectoryClientTestBase {
    private final ClientLogger directoryAsyncLogger = new ClientLogger(DirectoryAsyncClientTests.class);
    private static ShareClient shareClient;
    private static String shareName = "dirsharename";
    private DirectoryAsyncClient client;

    @Override
    public void beforeTest() {
        beforeDirectoryTest();
        if (interceptorManager.isPlaybackMode()) {
            client = setupClient((connectionString, endpoint) -> new DirectoryClientBuilder()
                             .connectionString(connectionString)
                             .shareName(shareName)
                             .directoryPath(dirName)
                             .httpClient(interceptorManager.getPlaybackClient())
                             .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                             .buildAsyncClient(), true, directoryAsyncLogger);
        } else {
            client = setupClient((connectionString, endpoint) -> new DirectoryClientBuilder()
                             .connectionString(connectionString)
                             .shareName(shareName)
                             .directoryPath(dirName)
                             .httpClient(HttpClient.createDefault().wiretap(true))
                             .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                             .addPolicy(interceptorManager.getRecordPolicy())
                             .buildAsyncClient(), false, directoryAsyncLogger);
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
        Assert.assertTrue(endpointURL.equals(client.getDirectoryUrl().toString()));
    }

    @Override
    public void createMinFromDirClient() {
        StepVerifier.create(client.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void createTwiceFromDirClient() {
        client.create().block();
        StepVerifier.create(client.create())
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 409));
    }

    @Override
    public void createWithMetadataFromDirClient() {
        StepVerifier.create(client.create(basicMetadata))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void getFileClientFromDirClient() {
        FileAsyncClient fileClient = client.getFileClient(testResourceNamer.randomName("getFileAsync", 16));
        Assert.assertNotNull(fileClient);
    }

    @Override
    public void getSubDirectoryClient() {
        Assert.assertNotNull(client.getSubDirectoryClient("getSubdirectoryASync"));
    }

    @Override
    public void deleteFromDirClient() {
        StepVerifier.create(client.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
        StepVerifier.create(client.delete())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 202))
            .verifyComplete();
    }

    @Override
    public void deleteNotExistFromDirClient() {
        StepVerifier.create(client.delete())
            .verifyErrorSatisfies(exception -> FileTestHelpers.assertExceptionErrorMessage(exception, "ResourceNotFound"));

    }

    @Override
    public void getPropertiesFromDirClient() {
        StepVerifier.create(client.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
        StepVerifier.create(client.getProperties())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 200))
            .verifyComplete();
    }

    @Override
    public void clearMetadataFromDirClient() {
        StepVerifier.create(client.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
        StepVerifier.create(client.setMetadata(null))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 200))
            .verifyComplete();
    }

    @Override
    public void setMetadataFromDirClient() {
        client.create().block();
        StepVerifier.create(client.setMetadata(basicMetadata))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 200))
            .verifyComplete();
    }

    @Override
    public void setMetadataInvalidKeyFromDirClient() {
        client.create().block();
        StepVerifier.create(client.setMetadata(invalidMetadata))
            .verifyErrorSatisfies(throwable -> FileTestHelpers.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void listFilesAndDirectoriesFromDirClient() {
        StepVerifier.create(client.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
        List<FileRef> fileRefs = new ArrayList<>();
        int repeatTimes = 3;
        // generate some subdirectories
        String fileNameSameLayer = testResourceNamer.randomName("sameLayer", 16);
        String fileNameNextLayer = testResourceNamer.randomName("nextLayer", 16);

        for (int i = 0; i < repeatTimes; i++) {
            String directoryName = dirName + i;
            DirectoryAsyncClient subDirectoryClient = client.getSubDirectoryClient(directoryName);
            StepVerifier.create(subDirectoryClient.create())
                .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
                .verifyComplete();
            fileRefs.add(new FileRef(directoryName, true, null));

            fileNameSameLayer = fileNameSameLayer + i;
            fileNameNextLayer = fileNameNextLayer + i;

            StepVerifier.create(client.createFile(fileNameSameLayer, 1024))
                .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
                .verifyComplete();
            fileRefs.add(new FileRef(fileNameSameLayer, false, new FileProperty().contentLength(1024)));
            StepVerifier.create(subDirectoryClient.createFile(fileNameNextLayer, 1024))
                .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
                .verifyComplete();

        }

        Collections.sort(fileRefs, new LexicographicComparator());
        StepVerifier.create(client.listFilesAndDirectories(null, null))
            .assertNext(fileRef -> FileTestHelpers.assertFileRefsAreEqual(fileRefs.get(0), fileRef))
            .assertNext(fileRef -> FileTestHelpers.assertFileRefsAreEqual(fileRefs.get(1), fileRef))
            .assertNext(fileRef -> FileTestHelpers.assertFileRefsAreEqual(fileRefs.get(2), fileRef))
            .assertNext(fileRef -> FileTestHelpers.assertFileRefsAreEqual(fileRefs.get(3), fileRef))
            .assertNext(fileRef -> FileTestHelpers.assertFileRefsAreEqual(fileRefs.get(4), fileRef))
            .assertNext(fileRef -> FileTestHelpers.assertFileRefsAreEqual(fileRefs.get(5), fileRef))
            .verifyComplete();
    }

    class LexicographicComparator implements Comparator<FileRef> {
        @Override
        public int compare(FileRef a, FileRef b) {
            return a.name().compareToIgnoreCase(b.name());
        }
    }

    @Override
    public void getHandlesFromDirClient() {
        // TODO: Need to figure out way of creating handlers first.

//        StepVerifier.create(client.create())
//            .assertNext(response -> StorageTestBase.assertResponseStatusCode(response, 201))
//            .verifyComplete();
//        StepVerifier.create(client.getHandles(null, true))
//            .assertNext(response -> Assert.assertNotNull(response))
//            .verifyComplete();
    }

    @Override
    public void forceCloseHandlesFromDirClient() {
        client.create().block();
        client.createFile("test", 1024);
        Iterable<HandleItem> handleItems = client.getHandles(null, true).toIterable();
        handleItems.forEach(handleItem -> {
            StepVerifier.create(client.forceCloseHandles(handleItem.handleId(), true))
                .assertNext(numOfClosedHandles -> Assert.assertTrue(numOfClosedHandles.longValue() > 0))
                .verifyComplete();
        });
    }

    @Override
    public void createSubDirectory() {
        StepVerifier.create(client.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
        StepVerifier.create(client.createSubDirectory(testResourceNamer.randomName("dir", 16)))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void createSubDirectoryWithMetadata() {
        StepVerifier.create(client.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
        StepVerifier.create(client.createSubDirectory(testResourceNamer.randomName("dir", 16), basicMetadata))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void createSubDirectoryTwiceSameMetadata() {
        StepVerifier.create(client.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
        String dirName = testResourceNamer.randomName("dir", 16);
        StepVerifier.create(client.createSubDirectory(dirName, basicMetadata))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
        StepVerifier.create(client.createSubDirectory(dirName, basicMetadata))
            .verifyErrorSatisfies(exception -> FileTestHelpers.assertExceptionErrorMessage(exception, "ResourceAlreadyExists"));
    }

    @Override
    public void deleteSubDirectory() {
        client.create().block();
        String dirName = testResourceNamer.randomName("dir", 16);
        StepVerifier.create(client.createSubDirectory(dirName))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
        StepVerifier.create(client.deleteSubDirectory(dirName))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 202))
            .verifyComplete();
    }

    @Override
    public void createFileFromDirClient() {
        client.create().block();
        StepVerifier.create(client.createFile("testfile", 1024))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void createFileWithoutCreateDirFromDirClient() {
        StepVerifier.create(client.createFile("testfile", 1024))
            .verifyErrorSatisfies(response -> FileTestHelpers.assertExceptionStatusCode(response, 404));
    }

    @Override
    public void deleteFileFromDirClient() {
        client.create().block();
        client.createFile("testfile", 1024).block();
        StepVerifier.create(client.deleteFile("testfile"))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 202))
            .verifyComplete();
    }

    @Override
    public void deleteFileWithoutCreateFileFromDirClient() {
        client.create().block();
        StepVerifier.create(client.deleteFile("testfile"))
            .verifyErrorSatisfies(response -> FileTestHelpers.assertExceptionStatusCode(response, 404));
    }

}
