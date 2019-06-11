package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlockBlobUploadHeaders;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Sample {

    @Test
    public void sample() throws IOException {
        BlobServiceClient serviceClient = new BlobServiceClientBuilder().endpoint("http://jamesschreppler.blob.core.windows.net")
            .credentials(new SharedKeyCredentials("jamesschreppler", "8lw98WpdBjDmdCkcUc3PRc8VKx0t58cTDKCdJ9SifKzH9Ef54pNIvDQKyluSpcicjx3P1P9KRZNykPTMb7RSVw=="))
            .httpClient(HttpClient.createDefault()/*.proxy(() -> new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))*/)
            .buildClient();

        ContainerClient containerClient = serviceClient.createContainerClient("uxtesting" + UUID.randomUUID());
        containerClient.create();

        for (int i = 0; i < 5; i++) {
            BlockBlobClient blobClient = containerClient.createBlockBlobClient("testblob-" + i);
            ByteArrayInputStream testdata = new ByteArrayInputStream(("test data" + i).getBytes(StandardCharsets.UTF_8));

            blobClient.upload(testdata, testdata.available());
            System.out.println("Uploaded blob.");
        }

        System.out.println();

        System.out.println("Listing/downloading blobs:");
        for (BlobItem item : containerClient.listBlobsFlat(new ListBlobsOptions())) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            containerClient.createBlobClient(item.name()).download(stream);
            System.out.println(item.name() + ": " + new String(stream.toByteArray()));
        }
    }

    @Test
    public void asyncSample() throws IOException {
        BlobServiceAsyncClient serviceClient = new BlobServiceClientBuilder().endpoint("http://jamesschreppler.blob.core.windows.net")
            .credentials(new SharedKeyCredentials("jamesschreppler", "8lw98WpdBjDmdCkcUc3PRc8VKx0t58cTDKCdJ9SifKzH9Ef54pNIvDQKyluSpcicjx3P1P9KRZNykPTMb7RSVw=="))
            .httpClient(HttpClient.createDefault()/*.proxy(() -> new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))*/)
            .buildAsyncClient();

        ContainerAsyncClient containerClient = serviceClient.createContainerAsyncClient("uxtesting" + UUID.randomUUID());
        containerClient.create()
            .then(Mono.defer(() -> {
                Mono<Void> finished = Mono.empty();
                for (int i = 0; i < 5; i++) {
                    BlockBlobAsyncClient blobClient = containerClient.createBlockBlobAsyncClient("testblob-" + i);
                    byte[] message = ("test data" + i).getBytes(StandardCharsets.UTF_8);
                    Flux<ByteBuffer> testdata = Flux.just(ByteBuffer.wrap(message));


                    finished = finished.and(blobClient.upload(testdata, message.length)
                        .then(Mono.defer(() -> {
                            System.out.println("Uploaded blob.");
                            return Mono.empty();
                        })));
                }

                return finished;
            }))
            .thenMany(Flux.defer(() -> {
                System.out.println();
                System.out.println("Listing/downloading blobs:");
                return containerClient.listBlobsFlat(new ListBlobsOptions());
            }))
            .flatMap(listItem ->
                containerClient.createBlobAsyncClient(listItem.name())
                    .download()
                    .map(buffer -> new String(buffer.array()))
                    .doOnNext(string -> System.out.println(listItem.name() + ": " + string)))
            .blockLast();
    }
}
