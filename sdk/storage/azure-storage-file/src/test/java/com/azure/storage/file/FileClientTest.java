// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.models.FileCopyInfo;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.FileInfo;
import com.azure.storage.file.models.FileProperties;
import com.azure.storage.file.models.FileRange;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import static com.azure.storage.file.FileTestHelpers.assertTwoFilesAreSame;
import static com.azure.storage.file.FileTestHelpers.setupClient;

public class FileClientTest extends FileClientTestBase {
    private final ClientLogger fileLogger = new ClientLogger(FileClientTest.class);
    private static String shareName = "filesharename";
    private static String dirName = "testdir/";
    private static ShareClient shareClient;
    String filePath;

    private FileClient fileClient;

    @Override
    public void beforeTest() {
        filePath = dirName + testResourceNamer.randomName("file", 16);
        if (interceptorManager.isPlaybackMode()) {
            fileClient = setupClient((connectionString, endpoint) -> new FileClientBuilder()
                             .connectionString(connectionString)
                             .shareName(shareName)
                             .filePath(filePath)
                             .httpClient(interceptorManager.getPlaybackClient())
                             .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                             .buildClient(), true, fileLogger);
        } else {
            fileClient = setupClient((connectionString, endpoint) -> new FileClientBuilder()
                             .connectionString(connectionString)
                             .shareName(shareName)
                             .filePath(filePath)
                             .httpClient(HttpClient.createDefault().wiretap(true))
                             .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                             .addPolicy(interceptorManager.getRecordPolicy())
                             .buildClient(), false, fileLogger);
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
        FileTestHelpers.assertResponseStatusCode(fileClient.create(1024), 201);
        Assert.assertTrue(fileClient.getProperties().value().contentLength() == 1024);
    }

    @Override
    public void createExcessMaxSizeFromFileClient() {
        FileTestHelpers.assertResponseStatusCode(fileClient.create(1024 * 1024 * 1024 * 1024, null, null), 201);
        Assert.assertTrue(fileClient.getProperties().value().contentLength() == 0);
    }

    @Override
    public void startCopy() throws Exception {
        FileTestHelpers.assertResponseStatusCode(fileClient.create(1024, null, null), 201);
        String sourceURL = fileClient.getFileUrl().toString() + "/" + shareName + "/" + filePath;
        Response<FileCopyInfo> copyInfoResponse = fileClient.startCopy(sourceURL, null);
        FileTestHelpers.assertResponseStatusCode(copyInfoResponse, 202);
        Assert.assertTrue(copyInfoResponse.value().copyId() != null);
    }

    @Override
    public void abortCopy() {
        // TODO: Need to construct or mock a copy with pending status
    }

    @Override
    public void downloadWithProperties() {
        fileClient.create(1024, null, null);
        FileRange range = new FileRange(0, 1024L);
        FileTestHelpers.assertResponseListStatusCode(fileClient.downloadWithProperties(range, null), Arrays.asList(200, 206));
    }

    @Override
    public void uploadToStorageAndDownloadToFile() throws Exception {
        URL fileFolder = FileClientTestBase.class.getClassLoader().getResource("testfiles");
        File uploadFile = new File(fileFolder.getPath() + "/helloworld");
        File downloadFile = new File(fileFolder.getPath() + "/testDownload");

        if (!Files.exists(downloadFile.toPath())) {
            downloadFile.createNewFile();
        }

        fileClient.create(uploadFile.length());
        fileClient.uploadFromFile(uploadFile.toString());
        fileClient.downloadToFile(downloadFile.toString());
        assertTwoFilesAreSame(uploadFile, downloadFile);
    }

    @Override
    public void deleteFromFileClient() {
        fileClient.create(1024, null, null);
        FileTestHelpers.assertResponseStatusCode(fileClient.delete(), 202);
    }

    @Override
    public void getPropertiesFromFileClient() {
        fileClient.create(1024, null, null);
        Response<FileProperties> propertiesResponse = fileClient.getProperties();
        Assert.assertNotNull(propertiesResponse.value());
        Assert.assertNotNull(propertiesResponse.value().contentLength() == 1024);
        Assert.assertNotNull(propertiesResponse.value().eTag());
        Assert.assertNotNull(propertiesResponse.value().lastModified());
    }

    @Override
    public void setHttpHeadersFromFileClient() {
        fileClient.create(1024, null, null);
        FileHTTPHeaders headers = new FileHTTPHeaders();
        headers.fileContentMD5(new byte[0]);
        Response<FileInfo> response = fileClient.setHttpHeaders(1024, headers);
        FileTestHelpers.assertResponseStatusCode(response, 200);
        Assert.assertNotNull(response.value().eTag());
    }

    @Override
    public void setMeatadataFromFileClient() {
        fileClient.create(1024, null, null);
        FileTestHelpers.assertResponseStatusCode(fileClient.setMetadata(basicMetadata), 200);
    }

    @Override
    public void upload() {
        fileClient.create(1024 * 5, null, null);
        FileTestHelpers.assertResponseStatusCode(fileClient.upload(defaultData, defaultData.readableBytes()), 201);
    }

    @Override
    public void listRangesFromFileClient() {
        fileClient.create(1024, null, null);
        fileClient.upload(defaultData, defaultData.readableBytes());
        fileClient.listRanges(new FileRange(0, 511L)).forEach(
            fileRangeInfo -> {
                Assert.assertTrue(fileRangeInfo.start() == 0);
                Assert.assertTrue(fileRangeInfo.end() == 511);
            }
        );
    }

    @Override
    public void listHandlesFromFileClient() {
        //TODO: need to find a way to create handles.
        fileClient.create(1024);
        fileClient.listHandles().forEach(
            handleItem -> {
                Assert.assertNotNull(handleItem.handleId());
            }
        );
    }

    @Override
    public void forceCloseHandlesFromFileClient() {
        fileClient.create(1024, null, null);
        fileClient.listHandles(10).forEach(
            handleItem -> {
                Assert.assertNotNull(fileClient.forceCloseHandles(handleItem.handleId()));
            }
        );
    }
}
