// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.test.TestMode;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.HandleItem;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static com.azure.storage.file.FileTestHelpers.assertTwoFilesAreSame;
import static com.azure.storage.file.FileTestHelpers.setupClient;

public class FileAsyncClientTest extends FileClientTestBase {
    private final ClientLogger fileAsyncLogger = new ClientLogger(FileAsyncClientTest.class);
    String filePath;
    private static String shareName = "filesharename";
    private static String dirName = "testdir/";
    private static ShareClient shareClient;
    private FileAsyncClient fileAsyncClient;

    @Override
    public void beforeTest() {
        filePath = dirName + testResourceNamer.randomName("file", 16);
        if (interceptorManager.isPlaybackMode()) {
            fileAsyncClient = setupClient((connectionString, endpoint) -> new FileClientBuilder()
                                                                     .connectionString(connectionString)
                                                                     .shareName(shareName)
                                                                     .filePath(filePath)
                                                                     .httpClient(interceptorManager.getPlaybackClient())
                                                                     .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                                                                     .buildAsyncClient(), true, fileAsyncLogger);
        } else {
            fileAsyncClient = setupClient((connectionString, endpoint) -> new FileClientBuilder()
                                                                     .connectionString(connectionString)
                                                                     .shareName(shareName)
                                                                     .filePath(filePath)
                                                                     .httpClient(HttpClient.createDefault().wiretap(true))
                                                                     .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                                                                     .addPolicy(interceptorManager.getRecordPolicy())
                                                                     .buildAsyncClient(), false, fileAsyncLogger);
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
        shareClient.createDirectory(dirName);
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
    public void createFromFileClient() {
        StepVerifier.create(fileAsyncClient.create(1024))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
        StepVerifier.create(fileAsyncClient.getProperties())
            .assertNext(filePropertiesResponse -> Assert.assertTrue(filePropertiesResponse.value().contentLength() == 1024))
            .verifyComplete();
    }

    @Override
    public void createExcessMaxSizeFromFileClient() {
        StepVerifier.create(fileAsyncClient.create(1024 * 1024 * 1024 * 1024, null, null))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
        StepVerifier.create(fileAsyncClient.getProperties())
            .assertNext(filePropertiesResponse -> Assert.assertTrue(filePropertiesResponse.value().contentLength() == 0))
            .verifyComplete();
    }

    @Override
    public void startCopy() throws Exception {
        fileAsyncClient.create(1024).block();
        String sourceURL = fileAsyncClient.getFileUrl().toString() + "/" + shareName + "/" + filePath;
        StepVerifier.create(fileAsyncClient.startCopy(sourceURL, null))
            .assertNext(response -> {
                FileTestHelpers.assertResponseStatusCode(response, 202);
                Assert.assertTrue(response.value().copyId() != null);
            })
            .verifyComplete();
    }

    @Override
    public void abortCopy() {
        // TODO: need to mock a pending copy process.
    }

    @Override
    public void downloadWithProperties() {
        fileAsyncClient.create(1024, null, null).block();
        StepVerifier.create(fileAsyncClient.downloadWithProperties())
            .assertNext(response -> FileTestHelpers.assertResponseListStatusCode(response, Arrays.asList(200, 206)))
            .verifyComplete();
    }

    @Override
    public void uploadToStorageAndDownloadToFile() throws Exception {
        URL fileFolder = FileClientTestBase.class.getClassLoader().getResource("testfiles");
        File uploadFile = new File(fileFolder.getPath() + "/helloworld");
        File downloadFile = new File(fileFolder.getPath() + "/testDownload");

        if (!Files.exists(downloadFile.toPath())) {
            downloadFile.createNewFile();
        }
        fileAsyncClient.create(uploadFile.length()).block();
        StepVerifier.create(fileAsyncClient.uploadFromFile(uploadFile.toString()))
                    .verifyComplete();
        StepVerifier.create(fileAsyncClient.downloadToFile(downloadFile.toString()))
                    .verifyComplete();

        assertTwoFilesAreSame(uploadFile, downloadFile);
    }

    @Override
    public void deleteFromFileClient() {
        fileAsyncClient.create(1024, null, null).block();
        StepVerifier.create(fileAsyncClient.delete())
                .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 202))
                .verifyComplete();
    }

    @Override
    public void getPropertiesFromFileClient() {
        fileAsyncClient.create(1024).block();
        StepVerifier.create(fileAsyncClient.getProperties())
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 200))
            .verifyComplete();
    }

    @Override
    public void setHttpHeadersFromFileClient() {
        fileAsyncClient.create(1024).block();
        FileHTTPHeaders headers = new FileHTTPHeaders();
        headers.fileContentMD5(new byte[0]);
        StepVerifier.create(fileAsyncClient.setHttpHeaders(1024, headers))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 200))
            .verifyComplete();
    }

    @Override
    public void setMeatadataFromFileClient() {
        fileAsyncClient.create(1024).block();
        StepVerifier.create(fileAsyncClient.setMetadata(basicMetadata))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 200))
            .verifyComplete();
    }

    @Override
    public void upload() {
        fileAsyncClient.create(1024 * 5, null, null).block();
        StepVerifier.create(fileAsyncClient.upload(Flux.just(defaultData), defaultData.readableBytes()))
            .assertNext(response -> FileTestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void listRangesFromFileClient() {
        fileAsyncClient.create(512, null, null).block();
        fileAsyncClient.upload(Flux.just(defaultData), defaultData.readableBytes()).block();
        StepVerifier.create(fileAsyncClient.listRanges())
            .assertNext(response -> Assert.assertTrue(response.start() == 0 && response.end() == 511))
            .verifyComplete();
    }

    @Override
    public void listHandlesFromFileClient() {
        //TODO: need to create a handle first
        fileAsyncClient.create(1024).block();
        StepVerifier.create(fileAsyncClient.listHandles())
            .verifyComplete();
    }

    @Override
    public void forceCloseHandlesFromFileClient() {
        //TODO: need to figureout create a handle first
        fileAsyncClient.create(1024).block();
        Iterable<HandleItem> handles = fileAsyncClient.listHandles(10).toIterable();
        handles.forEach(
            response -> {
                StepVerifier.create(fileAsyncClient.forceCloseHandles(response.handleId()))
                    .assertNext(forceCloseHandles -> Assert.assertTrue(forceCloseHandles > 0))
                    .verifyComplete();
            }
        );
    }
}
