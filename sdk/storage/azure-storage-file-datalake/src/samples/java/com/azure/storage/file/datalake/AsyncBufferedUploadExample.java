// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Random;

/**
 * This example shows how to use the buffered upload method on DataLakeFileAsyncClient.
 *
 * Note that the use of .block() in the method is only used to enable the sample to run effectively in isolation. It is
 * not recommended for use in async environments.
 */
public class AsyncBufferedUploadExample {
    /**
     * Entry point into the basic examples for Storage datalake.
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
        String endpoint = String.format(Locale.ROOT, "https://%s.dfs.core.windows.net", accountName);
        String fileSystemName = "myjavafilesystembufferedupload" + System.currentTimeMillis();
        DataLakeServiceAsyncClient storageClient = new DataLakeServiceClientBuilder().endpoint(endpoint).credential(credential)
            .buildAsyncClient();

        DataLakeFileSystemAsyncClient fileSystemClient = storageClient.getFileSystemAsyncClient(fileSystemName);
        fileSystemClient.create().block();

        uploadSourceFile(endpoint, credential, fileSystemName);
        DataLakeFileAsyncClient fileClient = fileSystemClient.getFileAsyncClient("HelloWorld.txt");


        /*
        sourceData has a network stream as its source and therefore likely does not support multiple subscribers. Even
        if it did support multiple subscribers, it would not produce the same data each time it was subscribed to. While
        we could inspect the http headers for the content-length, let us suppose that this information is unavailable
        at this time. All three of these factors would individually make the use of the standard upload method
        impossible--the first two because retries would not work and the third one because we could not satisfy the
        argument list.
         */
        Flux<ByteBuffer> sourceData = getSourceFileClient(endpoint, credential, fileSystemName).read()
            // Perform some unpredicatable transformation.
            .map(AsyncBufferedUploadExample::randomTransformation);

        /*
        This upload overload permits the use of such unreliable data sources. The length need not be specified, but
        the tradeoff is that data must be buffered, so a buffer size and maximum concurrency is required instead. The
        Javadoc on the method will give more detailed information on the significance of these parameters, but they are
        likely context dependent.
         */
        long blockSize = 10 * 1024;
        int maxConcurrency = 5;
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(maxConcurrency);
        fileClient.upload(sourceData, parallelTransferOptions).block();
    }

    @SuppressWarnings("cast")
    private static ByteBuffer randomTransformation(ByteBuffer buffer) {
        // The JDK changed the return type of ByteBuffer#limit between 8 and 9. In 8 and below it returns Buffer, whereas
        // in JDK 9 and later, it returns ByteBuffer. To compile on both, we explicitly cast the returned value to
        // ByteBuffer.
        // See https://bugs-stage.openjdk.java.net/browse/JDK-8062376
        return (ByteBuffer) buffer.limit(new Random().nextInt(buffer.limit()));
    }

    private static void uploadSourceFile(String endpoint, StorageSharedKeyCredential credential, String containerName) {
        getSourceFileClient(endpoint, credential, containerName)
            .upload(Flux.just(ByteBuffer.wrap("Hello world".getBytes(Charset.defaultCharset()))), null).block();
    }

    private static DataLakeFileAsyncClient getSourceFileClient(String endpoint, StorageSharedKeyCredential credential,
        String containerName) {
        return new DataLakeServiceClientBuilder().endpoint(endpoint).credential(credential).buildAsyncClient()
            .getFileSystemAsyncClient(containerName).getFileAsyncClient("sourceFile");
    }
}
