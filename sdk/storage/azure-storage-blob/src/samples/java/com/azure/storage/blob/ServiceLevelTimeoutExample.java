// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobContainerCreateOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.ServiceTimeoutPolicy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

/**
 * This example shows how to use service level timeouts. These timeouts are set on the service operation. If the server
 * timeout interval elapses before the service has finished processing the request, the service returns an error.
 * For more information on setting service timeouts, see here:
 * <a href="https://learn.microsoft.com/rest/api/storageservices/setting-timeouts-for-blob-service-operations">Setting timeouts for blob service operations</a>
 * <a href="https://learn.microsoft.com/en-us/rest/api/storageservices/setting-timeouts-for-file-service-operations">Setting timeouts for file service operations</a>
 * <a href="https://learn.microsoft.com/en-us/rest/api/storageservices/setting-timeouts-for-queue-service-operations">Setting timeouts for queue service operations</a>
 */
public class ServiceLevelTimeoutExample {

    /**
     * Entry point into the service level timeout examples for Storage blobs.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException If an I/O error occurs
     * @throws RuntimeException If any failure (come back to this)
     */
    public static void main(String[] args) {

        /*
         * From the Azure portal, get your Storage account's name and account key.
         */
        String accountName = SampleHelper.getAccountName();
        String accountKey = SampleHelper.getAccountKey();

        /*
         * Use your Storage account's name and key to create a credential object; this is used to access your account.
         */
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

        /*
         * From the Azure portal, get your Storage account blob service URL endpoint.
         * The URL typically looks like this:
         */
        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

        /*
         * Create a BlobServiceClient object that wraps the service endpoint, credential, policy with service level
         * timeout per call.
         * For this example, we'll set the service timeout to 3 seconds.
         */
        BlobServiceClient storageClient = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .addPolicy(new ServiceTimeoutPolicy(Duration.ofSeconds(3)))
            .buildClient();

        BlobContainerClient blobContainerClient = storageClient.getBlobContainerClient("myjavacontainerbasic" + System.currentTimeMillis());

        /*
         * First create the container with the specified timeout. Since create operations are fast, timeout should not
         * exceed the 3 seconds specified above.
         */
        try {
            blobContainerClient.createIfNotExistsWithResponse(new BlobContainerCreateOptions(), null, Context.NONE);
            System.out.println("Created");
        } catch (Exception ex) {
            System.out.println("Creation failed due to timeout: " + ex.getMessage());
        }

        BlobClient blobClient = blobContainerClient.getBlobClient("myblob" + System.currentTimeMillis());

        //Create a dataset that is guaranteed to take longer than the specified timeout of 3 seconds
        byte[] randomData = getRandomByteArray(16 * Constants.MB);
        InputStream input = new ByteArrayInputStream(randomData);
        ParallelTransferOptions pto = new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB);

        /*
         * making the call to upload will fail since we are using a large dataset which will take longer than the
         * specified timeout.
         */
        try {
            blobClient.uploadWithResponse(new BlobParallelUploadOptions(input).setParallelTransferOptions(pto), null, null);
            System.out.println("Upload succeeded.");
        } catch (Exception ex) {
            System.out.println("Creation failed due to timeout: " + ex.getMessage());
        }
    }

    static byte[] getRandomByteArray(int size) {
        long seed = UUID.fromString(UUID.randomUUID().toString()).getMostSignificantBits() & Long.MAX_VALUE;
        Random rand = new Random(seed);
        byte[] data = new byte[size];
        rand.nextBytes(data);
        return data;
    }
}
