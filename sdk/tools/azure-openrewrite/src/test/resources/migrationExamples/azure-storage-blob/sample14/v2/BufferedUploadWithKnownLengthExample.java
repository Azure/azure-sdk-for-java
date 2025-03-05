// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * This example shows how to use the buffered upload method on BlockBlobAsyncClient with a known length.
 *
 * Note that the use of .block() in the method is only used to enable the sample to run effectively in isolation. It is
 * not recommended for use in async environments.
 */
public class BufferedUploadWithKnownLengthExample {
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
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);
        String containerName = "myjavacontainerbuffereduploadlength" + System.currentTimeMillis();
        BlobServiceAsyncClient storageClient = new BlobServiceClientBuilder().endpoint(endpoint).credential(credential)
            .buildAsyncClient();

        BlobContainerAsyncClient containerClient = storageClient.getBlobContainerAsyncClient(containerName);
        containerClient.create().block();

        uploadSourceBlob(endpoint, credential, containerName);
        BlobAsyncClient blobClient = containerClient.getBlobAsyncClient("HelloWorld.txt");


        /*
        sourceData has a network stream as its source and therefore likely does not support multiple subscribers. Even
        if it did support multiple subscribers, it would not produce the same data each time it was subscribed to. While
        we could inspect the http headers for the content-length, let us suppose that this information is unavailable
        at this time. All three of these factors would individually make the use of the standard upload method
        impossible--the first two because retries would not work and the third one because we could not satisfy the
        argument list.
         */
        Flux<ByteBuffer> sourceData = getSourceBlobClient(endpoint, credential, containerName).downloadStream()
            // Perform transformation with length of 1 GB.
            .map(BufferedUploadWithKnownLengthExample::bufferTransformation);

        /*
        Although this upload overload permits the use of such unreliable data sources, with known length we can speed
        up the upload process. A buffer size and maximum concurrency can still be passed in to achieve optimized upload.
         */
        long length = 10;
        long blockSize = 10 * 1024;
        int maxConcurrency = 5;
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(maxConcurrency);

        // Since we already know the size of our buffered bytes, we can pass the ByteBuffer and length to the BinaryData.
        // This will internally convert the BinaryData to a Flux<ByteBuffer>, but with known length we can optimize the
        // upload speed.
        // Need to use BinaryData.fromFlux(Flux<ByteBuffer>, Long, Boolean) with bufferContent set to false, this allows
        // us to configure the BinaryData to have a specified length set without the BinaryData being infinitely
        // subscribed to the Flux<ByteBuffer>.
        BinaryData.fromFlux(sourceData, length, false).flatMap(binaryData ->
            blobClient.uploadWithResponse(new BlobParallelUploadOptions(binaryData)
                .setParallelTransferOptions(parallelTransferOptions)));
    }

    @SuppressWarnings("cast")
    private static ByteBuffer bufferTransformation(ByteBuffer buffer) {
        // The JDK changed the return type of ByteBuffer#limit between 8 and 9. In 8 and below it returns Buffer, whereas
        // in JDK 9 and later, it returns ByteBuffer. To compile on both, we explicitly cast the returned value to
        // ByteBuffer.
        // See https://bugs-stage.openjdk.java.net/browse/JDK-8062376
        int length = 10;
        return (ByteBuffer) buffer.limit(length);
    }

    private static void uploadSourceBlob(String endpoint, StorageSharedKeyCredential credential, String containerName) {
        getSourceBlobClient(endpoint, credential, containerName)
            .upload(Flux.just(ByteBuffer.wrap("Hello world".getBytes(Charset.defaultCharset()))), "Hello world".length()).block();
    }

    private static BlockBlobAsyncClient getSourceBlobClient(String endpoint, StorageSharedKeyCredential credential,
        String containerName) {
        return new BlobServiceClientBuilder().endpoint(endpoint).credential(credential).buildAsyncClient()
            .getBlobContainerAsyncClient(containerName).getBlobAsyncClient("sourceBlob").getBlockBlobAsyncClient();
    }
}
