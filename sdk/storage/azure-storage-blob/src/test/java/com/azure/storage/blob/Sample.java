// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ContainerItem;
import com.azure.storage.common.credentials.SharedKeyCredential;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Sample {

    private static final String ACCOUNT_ENDPOINT = "";
    private static final String ACCOUNT_NAME = "";
    private static final String ACCOUNT_KEY = "";

    //@Test
    public void sample() throws IOException {
        // get service client
        BlobServiceClient serviceClient = new BlobServiceClientBuilder().endpoint(ACCOUNT_ENDPOINT)
            .credential(new SharedKeyCredential(ACCOUNT_NAME, ACCOUNT_KEY))
            .httpClient(HttpClient.createDefault()/*.setProxy(() -> new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))*/)
            .buildClient();

        // create 5 containers
        ContainerClient containerClient = null;
        for (int i = 0; i < 5; i++) {
            String name = "uxtesting" + UUID.randomUUID();
            containerClient = serviceClient.getContainerClient(name);
            containerClient.create();
            System.out.println("Created container: " + name);
        }
        System.out.println();

        // list containers in account
        System.out.println("Listing containers in account:");
        for (ContainerItem item : serviceClient.listContainers()) {
            System.out.println(item.getName());
        }
        System.out.println();

        // in the last container, create 5 blobs
        for (int i = 0; i < 5; i++) {
            BlockBlobClient blobClient = containerClient.getBlockBlobClient("testblob-" + i);
            ByteArrayInputStream testdata = new ByteArrayInputStream(("test data" + i).getBytes(StandardCharsets.UTF_8));

            blobClient.upload(testdata, testdata.available());
            System.out.println("Uploaded blob.");
        }
        System.out.println();

        // list blobs and download results
        System.out.println("Listing/downloading blobs:");
        for (BlobItem item : containerClient.listBlobs()) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            containerClient.getBlobClient(item.getName()).download(stream);
            System.out.println(item.getName() + ": " + new String(stream.toByteArray()));
        }
        System.out.println();

        // cleanup
        for (ContainerItem item : serviceClient.listContainers()) {
            containerClient = serviceClient.getContainerClient(item.getName());
            containerClient.delete();
            System.out.println("Deleted container: " + item.getName());
        }
    }

    //@Test
    public void asyncSample() throws IOException {
        // get service client
        BlobServiceAsyncClient serviceClient = new BlobServiceClientBuilder().endpoint(ACCOUNT_ENDPOINT)
            .credential(new SharedKeyCredential(ACCOUNT_NAME, ACCOUNT_KEY))
            .httpClient(HttpClient.createDefault()/*.setProxy(() -> new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))*/)
            .buildAsyncClient();

        // create 5 containers
        ContainerAsyncClient containerClient = null;
        Mono<Void> createContainerTask = Mono.empty();
        for (int i = 0; i < 5; i++) {
            String name = "uxtesting" + UUID.randomUUID();
            containerClient = serviceClient.getContainerAsyncClient(name);

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
                return serviceClient.listContainers()
                    .flatMap(containerItem -> {
                        System.out.println(containerItem.getName());
                        return Mono.empty();
                    });
            }))
            // in the last container, create 5 blobs
            .then(Mono.defer(() -> {
                Mono<Void> finished = Mono.empty();
                for (int i = 0; i < 5; i++) {
                    BlockBlobAsyncClient blobClient = finalContainerClient.getBlockBlobAsyncClient("testblob-" + i);
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
                return finalContainerClient.listBlobs();
            }))
            // download results
            .flatMap(listItem ->
                finalContainerClient.getBlobAsyncClient(listItem.getName())
                    .download()
                    .flatMapMany(flux -> flux)
                    .map(buffer -> new String(buffer.array()))
                    .doOnNext(string -> System.out.println(listItem.getName() + ": " + string)))
            // cleanup
            .thenMany(serviceClient.listContainers())
            .flatMap(containerItem -> serviceClient
                .getContainerAsyncClient(containerItem.getName())
                .delete())
            .blockLast();
    }

    //@Test
    public void uploadDownloadFromFile() throws IOException {
        final String data = "TEST DATA" + UUID.randomUUID();
        final String folderPath = "C:/Users/jaschrep/Desktop/temp";

        // make start file
        File startFile = new File(folderPath, "startFile" + UUID.randomUUID());
        FileOutputStream fstream = new FileOutputStream(startFile);
        fstream.write(data.getBytes());
        fstream.close();

        // get service client
        BlobServiceClient serviceClient = new BlobServiceClientBuilder().endpoint(ACCOUNT_ENDPOINT)
            .credential(new SharedKeyCredential(ACCOUNT_NAME, ACCOUNT_KEY))
            .httpClient(HttpClient.createDefault()/*.setProxy(() -> new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))*/)
            .buildClient();

        // make container
        ContainerClient containerClient = serviceClient.getContainerClient("uxstudy" + UUID.randomUUID());
        containerClient.create();

        // upload data
        BlockBlobClient blobClient = containerClient.getBlockBlobClient("testblob_" + UUID.randomUUID());
        blobClient.uploadFromFile(startFile.getAbsolutePath());

        // download data
        File endFile = new File(folderPath, "endFile" + UUID.randomUUID());
        blobClient.downloadToFile(endFile.getAbsolutePath());
    }

    //@Test
    public void uploadDownloadFromFileAsync() throws IOException {
        final String data = "TEST DATA" + UUID.randomUUID();
        final String folderPath = "C:/Users/jaschrep/Desktop/temp";

        // make start file
        File startFile = new File(folderPath, "startFile" + UUID.randomUUID());
        FileOutputStream fstream = new FileOutputStream(startFile);
        fstream.write(data.getBytes());
        fstream.close();

        // get service client
        BlobServiceAsyncClient serviceClient = new BlobServiceClientBuilder().endpoint(ACCOUNT_ENDPOINT)
            .credential(new SharedKeyCredential(ACCOUNT_NAME, ACCOUNT_KEY))
            .httpClient(HttpClient.createDefault()/*.setProxy(() -> new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))*/)
            .buildAsyncClient();

        // make container
        ContainerAsyncClient containerClient = serviceClient.getContainerAsyncClient("uxstudy" + UUID.randomUUID());
        containerClient.create()

            // upload data
            .then(Mono.defer(() -> {
                BlockBlobAsyncClient blobClient = containerClient.getBlockBlobAsyncClient("testblob_" + UUID.randomUUID());
                return blobClient.uploadFromFile(startFile.getAbsolutePath())
                    .then(Mono.just(blobClient));
            }))

            // download data
            .flatMap(blobClient ->
                blobClient.downloadToFile(new File(folderPath, "endFile" + UUID.randomUUID()).getAbsolutePath()))

            .block();

    }
}
