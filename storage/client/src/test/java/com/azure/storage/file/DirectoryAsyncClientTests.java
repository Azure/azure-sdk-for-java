package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.test.TestMode;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.models.FileRef;
import java.time.Duration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import reactor.test.StepVerifier;

import java.util.LinkedList;

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
            client = setupClient((connectionString, endpoint) -> DirectoryAsyncClient.builder()
                             .connectionString(connectionString)
                             .endpoint(endpoint)
                             .shareName(shareName)
                             .directoryName(dirName)
                             .httpClient(interceptorManager.getPlaybackClient())
                             .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                             .buildAsync(), true, directoryAsyncLogger);
        } else {
            client = setupClient((connectionString, endpoint) -> DirectoryAsyncClient.builder()
                             .connectionString(connectionString)
                             .endpoint(endpoint)
                             .shareName(shareName)
                             .directoryName(dirName)
                             .httpClient(HttpClient.createDefault().wiretap(true))
                             .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                             .addPolicy(interceptorManager.getRecordPolicy())
                             .buildAsync(), false, directoryAsyncLogger);
        }
    }

    @BeforeClass
    public static void beforeClass() {
        if (FileTestHelpers.getTestMode() == TestMode.PLAYBACK) {
            return;
        }
        FileServiceClient fileServiceClient = FileServiceClient.builder()
                                .connectionString(ConfigurationManager.getConfiguration().get("AZURE_STORAGE_CONNECTION_STRING"))
                                .buildSync();
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
        String endpointURL = new UrlBuilder().withScheme(urlBuilder.scheme()).withHost(urlBuilder.host()).toString();
        Assert.assertTrue(endpointURL.equals(client.url()));
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
            .expectErrorMessage("ResourceNotFound");

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
        LinkedList<FileRef> fileRefs = new LinkedList<>();
        int repeatTimes = 3;
        // generate some subdirectories
        for (int i = 0; i < repeatTimes; i++) {
            String directoryName = dirName + i;
            StepVerifier.create(client.createSubDirectory(directoryName))
                .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
                .verifyComplete();
            fileRefs.add(new FileRef(directoryName, true, null));
        }

        StepVerifier.create(client.listFilesAndDirectories(dirName, null))
            .assertNext(fileRef -> FileTestHelpers.assertFileRefsAreEqual(fileRefs.pop(), fileRef))
            .assertNext(fileRef -> FileTestHelpers.assertFileRefsAreEqual(fileRefs.pop(), fileRef))
            .assertNext(fileRef -> FileTestHelpers.assertFileRefsAreEqual(fileRefs.pop(), fileRef))
            .verifyComplete();
    }

    @Override
    public void getHandlesFromDirClient() {
        // TODO: Need to open channel and create handlers first.
//        StepVerifier.create(client.create())
//            .assertNext(response -> StorageTestBase.assertResponseStatusCode(response, 201))
//            .verifyComplete();
//        StepVerifier.create(client.getHandles(null, true))
//            .assertNext(response -> Assert.assertNotNull(response))
//            .verifyComplete();
    }

    @Override
    public void forceCloseHandlesFromDirClient() {
        StepVerifier.create(client.create())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
        client.getHandles(null, true).doOnEach(
            handleItem -> {
                client.forceCloseHandles(handleItem.get().handleId(), true).doOnEach(
                    numOfClosedHandles -> Assert.assertTrue(numOfClosedHandles.get() > 0)
                );
            }
        );
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
            .expectErrorMessage("ResourceAlreadyExists");
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
