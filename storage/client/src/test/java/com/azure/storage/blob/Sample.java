package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlockBlobUploadHeaders;
import com.azure.storage.blob.models.ContainerItem;
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

    final static String accountEndpoint = "http://jamesschreppler.blob.core.windows.net";
    final static String accountName = "";
    final static String accountKey = "";

    @Test
    public void sample() throws IOException {
        // get service client
        BlobServiceClient serviceClient = new BlobServiceClientBuilder().endpoint(accountEndpoint)
            .credentials(new SharedKeyCredentials(accountName, accountKey))
            .httpClient(HttpClient.createDefault()/*.proxy(() -> new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))*/)
            .buildClient();

        // create 5 containers
        ContainerClient containerClient = null;
        for (int i = 0; i < 5; i++) {
            String name = "uxtesting" + UUID.randomUUID();
            containerClient = serviceClient.createContainerClient(name);
            containerClient.create();
            System.out.println("Created container: " + name);
        }
        System.out.println();

        // list containers in account
        System.out.println("Listing containers in account:");
        for (ContainerItem item : serviceClient.listContainers(new ListContainersOptions())) {
            System.out.println(item.name());
        }
        System.out.println();

        // in the last container, create 5 blobs
        for (int i = 0; i < 5; i++) {
            BlockBlobClient blobClient = containerClient.createBlockBlobClient("testblob-" + i);
            ByteArrayInputStream testdata = new ByteArrayInputStream(("test data" + i).getBytes(StandardCharsets.UTF_8));

            blobClient.upload(testdata, testdata.available());
            System.out.println("Uploaded blob.");
        }
        System.out.println();

        // list blobs and download results
        System.out.println("Listing/downloading blobs:");
        for (BlobItem item : containerClient.listBlobsFlat(new ListBlobsOptions())) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            containerClient.createBlobClient(item.name()).download(stream);
            System.out.println(item.name() + ": " + new String(stream.toByteArray()));
        }
        System.out.println();

        // cleanup
        for (ContainerItem item : serviceClient.listContainers(new ListContainersOptions())) {
            containerClient = serviceClient.createContainerClient(item.name());
            containerClient.delete();
            System.out.println("Deleted container: " + item.name());
        }
    }

    @Test
    public void asyncSample() throws IOException {
        // get service client
        BlobServiceAsyncClient serviceClient = new BlobServiceClientBuilder().endpoint(accountEndpoint)
            .credentials(new SharedKeyCredentials(accountName, accountKey))
            .httpClient(HttpClient.createDefault()/*.proxy(() -> new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))*/)
            .buildAsyncClient();

        // create 5 containers
        ContainerAsyncClient containerClient = null;
        Mono<Void> createContainerTask = Mono.empty();
        for (int i = 0; i < 5; i++) {
            String name = "uxtesting" + UUID.randomUUID();
            containerClient = serviceClient.createContainerAsyncClient(name);

            createContainerTask = createContainerTask.and(containerClient.create().then(Mono.defer(() -> {
                System.out.println("Created container: " + name);
                return Mono.empty();
            })));
        }
        ContainerAsyncClient finalContainerClient = containerClient; // final variable for lambda usage

        createContainerTask
            // list containers
            .thenMany(Flux.defer(() -> {
                System.out.println("Listing containers in account:");
                return serviceClient.listContainers(new ListContainersOptions())
                    .flatMap(containerItem -> {
                        System.out.println(containerItem.name());
                        return Mono.empty();
                    });
            }))
            // in the last container, create 5 blobs
            .then(Mono.defer(() -> {
                Mono<Void> finished = Mono.empty();
                for (int i = 0; i < 5; i++) {
                    BlockBlobAsyncClient blobClient = finalContainerClient.createBlockBlobAsyncClient("testblob-" + i);
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
            // list blobs
            .thenMany(Flux.defer(() -> {
                System.out.println();
                System.out.println("Listing/downloading blobs:");
                return finalContainerClient.listBlobsFlat(new ListBlobsOptions());
            }))
            // download results
            .flatMap(listItem ->
                finalContainerClient.createBlobAsyncClient(listItem.name())
                    .download()
                    .map(buffer -> new String(buffer.array()))
                    .doOnNext(string -> System.out.println(listItem.name() + ": " + string)))
            // cleanup
            .thenMany(serviceClient.listContainers(new ListContainersOptions()))
            .flatMap(containerItem -> serviceClient
                .createContainerAsyncClient(containerItem.name())
                .delete())
            .blockLast();
    }
}
