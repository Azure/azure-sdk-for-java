package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.TestHelpers;
import com.azure.storage.file.models.CopyStatusType;
import com.azure.storage.file.models.FileCopyInfo;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.FileInfo;
import com.azure.storage.file.models.FileMetadataInfo;
import com.azure.storage.file.models.FileProperties;
import com.azure.storage.file.models.FileRange;
import com.azure.storage.file.models.FileRangeWriteType;
import com.azure.storage.file.models.FilesGetPropertiesResponse;
import com.azure.storage.file.models.FilesListHandlesResponse;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import org.junit.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FileClientTest extends FileClientTestBase{
    String shareName = "storagefiletests";
    String filePath;

    private FileClient client;

    @Override
    public void beforeTest() {
        filePath = "testdir/" + generateName("file");
        if (interceptorManager.isPlaybackMode()) {
            client = setupClient((connectionString, endpoint) -> FileClient.builder()
                             .connectionString(connectionString)
                             .shareName(shareName)
                             .filePath(filePath)
                             .endpoint(endpoint)
                             .httpClient(interceptorManager.getPlaybackClient())
                             .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                             .build());
        } else {
            client = setupClient((connectionString, endpoint) -> FileClient.builder()
                             .connectionString(connectionString)
                             .shareName(shareName)
                             .filePath(filePath)
                             .endpoint(endpoint)
                            // .httpClient(HttpClient.createDefault().wiretap(true))
                                                                     .httpClient(HttpClient.createDefault().proxy(() -> new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888))).wiretap(true))
                             .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                             .addPolicy(interceptorManager.getRecordPolicy())
                             .build());
        }
    }

    @Override
    public void create() {
        TestHelpers.assertResponseStatusCode(client.create(1024), 201);
        Assert.assertTrue(client.getProperties().value().contentLength() == 1024);
    }

    @Override
    public void createExcessMaxSize() {
        TestHelpers.assertResponseStatusCode(client.create(1024 * 1024 * 1024 * 1024, null, null), 201);
        Assert.assertTrue(client.getProperties().value().contentLength() == 0);
    }

    @Override
    public void startCopy() throws Exception {
        TestHelpers.assertResponseStatusCode(client.create(1024, null, null), 201);
        String sourceURL = client.url().toString() + "/" + shareName + "/" + filePath;
        Response<FileCopyInfo> copyInfoResponse = client.startCopy(sourceURL, null);
        TestHelpers.assertResponseStatusCode(copyInfoResponse, 202);
        Assert.assertTrue(copyInfoResponse.value().copyId() != null);
    }

    @Override
    public void abortCopy() {
        // TODO: Need to construct or mock a copy with pending status
    }

    @Override
    public void downloadWithProperties() {
        // TODO: More tests cover after upload function well.
        client.create(1024, null, null);
        FileRange range = new FileRange(0, 1024);
        TestHelpers.assertResponseStatusCode(client.downloadWithProperties(range, null), 206);
    }

    @Override
    public void delete() {
        client.create(1024, null, null);
        client.delete();
    }

    @Override
    public void getProperties() {
        client.create(1024, null, null);
        Response<FileProperties> propertiesResponse = client.getProperties();
        Assert.assertNotNull(propertiesResponse.value());
        Assert.assertNotNull(propertiesResponse.value().contentLength() == 1024);
        Assert.assertNotNull(propertiesResponse.value().eTag());
        Assert.assertNotNull(propertiesResponse.value().lastModified());
    }

    @Override
    public void setHttpHeaders() {
        client.create(1024, null, null);
        FileHTTPHeaders headers = new FileHTTPHeaders();
        headers.fileContentMD5(new byte[0]);
        Response<FileInfo> response = client.setHttpHeaders(1024, headers);
        TestHelpers.assertResponseStatusCode(response, 200);
        Assert.assertNotNull(response.value().eTag());
    }

    @Override
    public void setMeatadata() {
        client.create(1024, null, null);
        TestHelpers.assertResponseStatusCode(client.setMeatadata(basicMetadata), 200);
    }

    @Override
    public void upload() {
        client.create(1024 * 5, null, null);
        TestHelpers.assertResponseStatusCode(client.upload(defaultData, defaultData.readableBytes()), 201);
    }

    @Override
    public void listRanges() {
        client.create(1024, null, null);
        client.upload(defaultData, defaultData.readableBytes());
        client.listRanges(new FileRange(0, 511)).forEach(
            fileRangeInfo -> {
                Assert.assertTrue(fileRangeInfo.start() == 0);
                Assert.assertTrue(fileRangeInfo.end() == 511);
            }
        );
    }

    @Override
    public void listHandles() {
        //TODO: need to find a way to create handles.
        client.create(1024);
        client.listHandles().forEach(
            handleItem -> {
                Assert.assertNotNull(handleItem.handleId());
            }
        );
    }

    @Override
    public void forceCloseHandles() {
        client.create(1024, null, null);
        client.listHandles(10).forEach(
            handleItem -> {
                Assert.assertNotNull(client.forceCloseHandles(handleItem.handleId()));
            }
        );
    }
}
