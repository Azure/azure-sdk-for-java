package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.Response;
import com.azure.storage.TestHelpers;
import com.azure.storage.file.models.FileCopyInfo;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.FileRangeWriteType;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import org.junit.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class FileAsyncClientTest extends FileClientTestBase {
    String shareName = "storagefiletests";
    String filePath;

    private FileAsyncClient client;

    @Override
    public void beforeTest() {
        filePath = "testdir/" + generateName("file");
        if (interceptorManager.isPlaybackMode()) {
            client = setupClient((connectionString, endpoint) -> FileAsyncClient.builder()
                                                                     .connectionString(connectionString)
                                                                     .shareName(shareName)
                                                                     .filePath(filePath)
                                                                     .endpoint(endpoint)
                                                                     .httpClient(interceptorManager.getPlaybackClient())
                                                                     .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                                                                     .buildAsync());
        } else {
            client = setupClient((connectionString, endpoint) -> FileAsyncClient.builder()
                                                                     .connectionString(connectionString)
                                                                     .shareName(shareName)
                                                                     .filePath(filePath)
                                                                     .endpoint(endpoint)
                                                                   //  .httpClient(HttpClient.createDefault().proxy(() -> new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888))).wiretap(true))
                                                                     .httpClient(HttpClient.createDefault())
                                                                     .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                                                                     .addPolicy(interceptorManager.getRecordPolicy())
                                                                     .buildAsync());
        }
    }

    @Override
    public void create() {
        StepVerifier.create(client.create(1024, null, null))
            .assertNext(response -> TestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void createExcessMaxSize() {
        StepVerifier.create(client.create(1024 * 1024 * 1024 * 1024, null, null))
            .assertNext(response -> TestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void startCopy() throws Exception {
        client.create(1024).block();
        String sourceURL = client.url().toString() + "/" + shareName + "/" + filePath;
        StepVerifier.create(client.startCopy(sourceURL, null))
            .assertNext(response ->
            {
                TestHelpers.assertResponseStatusCode(response, 202);
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
        client.create(1024, null, null).block();
        StepVerifier.create(client.downloadWithProperties())
            .assertNext(response -> TestHelpers.assertResponseStatusCode(response, 206))
            .verifyComplete();
    }

    @Override
    public void delete() {
        client.create(1024, null, null).subscribe();
        client.delete().subscribe();
    }

    @Override
    public void getProperties() {
        client.create(1024).block();
        StepVerifier.create(client.getProperties())
            .assertNext(response -> TestHelpers.assertResponseStatusCode(response, 200))
            .verifyComplete();
    }

    @Override
    public void setHttpHeaders() {
        client.create(1024).block();
        FileHTTPHeaders headers = new FileHTTPHeaders();
        headers.fileContentMD5(new byte[0]);
        StepVerifier.create(client.setHttpHeaders(1024, headers))
            .assertNext(response -> TestHelpers.assertResponseStatusCode(response, 200))
            .verifyComplete();
    }

    @Override
    public void setMeatadata() {
        client.create(1024).block();
        StepVerifier.create(client.setMeatadata(basicMetadata))
            .assertNext(response -> TestHelpers.assertResponseStatusCode(response, 200))
            .verifyComplete();
    }

    @Override
    public void upload() {
        client.create(1024 * 5, null, null).block();
        StepVerifier.create(client.upload(Flux.just(defaultData), defaultData.readableBytes()))
            .assertNext(response -> TestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void listRanges() {
        client.create(512, null, null).block();
        client.upload(Flux.just(defaultData), defaultData.readableBytes()).block();
        StepVerifier.create(client.listRanges())
            .assertNext(response -> Assert.assertTrue(response.start() == 0 && response.end() == 511))
            .verifyComplete();
    }

    @Override
    public void listHandles() {
        //TODO: need to create a handle first
        client.create(1024).block();
        StepVerifier.create(client.listHandles())
            .verifyComplete();
    }

    @Override
    public void forceCloseHandles() {
        //TODO: need to create a handle first
        client.create(1024).block();
        CountDownLatch latch = new CountDownLatch(1);
        client.listHandles().subscribe(
          response -> {
              StepVerifier.create(client.forceCloseHandles(response.handleId()))
                  .assertNext(forceCloseHandles -> Assert.assertTrue(forceCloseHandles > 0))
                  .verifyComplete();

              latch.countDown();
          }
        );
    }
}
