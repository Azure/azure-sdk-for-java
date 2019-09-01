// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.storage.common.credentials.SharedKeyCredential;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Random;

/**
 * This example shows how to use the buffered upload method on BlockBlobAsyncClient.
 *
 * Note that the use of .block() in the method is only used to enable the sample to run effectively in isolation. It is
 * not recommended for use in async environments.
 */
public class AsyncBufferedUploadExample {
    /**
     * Entry point into the basic examples for Storage blobs.
     * @param args Unused. Arguments to the program.
     * @throws IOException If an I/O error occurs
     * @throws RuntimeException If the downloaded data doesn't match the uploaded data
     */
    public static void main(String[] args) throws IOException {

        /*
         * For more information on this setup, please refer to the BasicExample.
         */
        String accountName = SampleHelper.getAccountName();
        String accountKey = SampleHelper.getAccountKey();
        SharedKeyCredential credential = new SharedKeyCredential(accountName, accountKey);
        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);
        String containerName = "myjavacontainerbufferedupload" + System.currentTimeMillis();
        BlobServiceAsyncClient storageClient = new BlobServiceClientBuilder().endpoint(endpoint).credential(credential)
            .buildAsyncClient();

        ContainerAsyncClient containerClient = storageClient.getContainerAsyncClient(containerName);
        containerClient.create().block();

        uploadSourceBlob(endpoint, credential, containerName);
        BlockBlobAsyncClient blobClient = containerClient.getBlockBlobAsyncClient("HelloWorld.txt");


        /*
        sourceData has a network stream as its source and therefore likely does not support multiple subscribers. Even
        if it did support multiple subscribers, it would not produce the same data each time it was subscribed to. While
        we could inspect the http headers for the content-length, let us suppose that this information is unavailable
        at this time. All three of these factors would individually make the use of the standard upload method
        impossible--the first two because retries would not work and the third one because we could not satisfy the
        argument list.
         */
        // The JDK changed the return type of ByteBuffer#limit between 8 and 9. In 8 and below it returns Buffer, whereas
        // in JDK 9 and later, it returns ByteBuffer. To compile on both, we explicitly cast the returned value to
        // ByteBuffer.
        @SuppressWarnings("all")
        Flux<ByteBuffer> sourceData = getSourceBlobClient(endpoint, credential, containerName).download()
            .flatMapMany(flux -> flux)
            // Perform some unpredicatable transformation.
            .map(buffer -> (ByteBuffer) buffer.limit(new Random().nextInt(buffer.limit())));

        /*
        This upload overload permits the use of such unreliable data sources. The length need not be specified, but
        the tradeoff is that data must be buffered, so a buffer size and number of buffers is required instead. The
        Javadoc on the method will give more detailed information on the significance of these parameters, but they are
        likely context dependent.
         */
        int blockSize = 10 * 1024;
        int numBuffers = 5;
        blobClient.upload(sourceData, blockSize, numBuffers).block();
    }

    private static void uploadSourceBlob(String endpoint, SharedKeyCredential credential, String containerName) {
        getSourceBlobClient(endpoint, credential, containerName)
            .upload(Flux.just(ByteBuffer.wrap("Hello world".getBytes(Charset.defaultCharset()))), "Hello world".length()).block();
    }

    private static BlockBlobAsyncClient getSourceBlobClient(String endpoint, SharedKeyCredential credential,
        String containerName) {
        return new BlobServiceClientBuilder().endpoint(endpoint).credential(credential).buildAsyncClient()
            .getContainerAsyncClient(containerName).getBlockBlobAsyncClient("sourceBlob");
    }
}
