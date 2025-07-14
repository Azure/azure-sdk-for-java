// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.storage.blob.perf;

import reactor.core.publisher.Mono;

import java.util.UUID;

public class UploadBlobTest extends ServiceTest<BlobPerfStressOptions> {
    private String containerName;
    private String blobName;
    private byte[] uploadData;

    public UploadBlobTest(BlobPerfStressOptions options) {
        super(options);

        this.containerName = System.getenv("STORAGE_CONTAINER_NAME");
        this.blobName = generateRandomName("testfile");

        int size = 1024 * 1024; // 1 MiB = 1024 * 1024 bytes

        // Create a byte array of size 1 MiB
        uploadData = new byte[size];

        // Optionally, initialize the array with random data
        java.util.Random random = new java.util.Random();
        random.nextBytes(uploadData);
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        try {
            io.clientcore.core.models.binarydata.BinaryData binaryData
                = io.clientcore.core.models.binarydata.BinaryData.fromBytes(uploadData);
            blockBlobClient.upload(containerName, blobName, uploadData.length, binaryData, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.error(new RuntimeException("Async is a thing of past."));
    }

    public static String generateRandomName(String prefix) {
        // Generate a random UUID and take only the first part
        String randomSuffix = UUID.randomUUID().toString().split("-")[0];
        return prefix + randomSuffix;
    }
}
