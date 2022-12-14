package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.options.BlobContainerCreateOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.ServiceTimeoutPolicy;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;

/**
 * This example shows how to use service level timeouts. These timeouts are set on the service operation. If the server
 * timeout interval elapses before the service has finished processing the request, the service returns an error.
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
        String endpoint = String.format(Locale.ROOT, "https://%s.blob.preprod.core.windows.net", accountName);

        /*
         * Create a BlobServiceClient object that wraps the service endpoint, credential, policy with service level
         * timeout per call.
         */
        BlobServiceClient storageClient = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .addPolicy(new ServiceTimeoutPolicy(Duration.ofSeconds(3)))
            .buildClient();

        BlobContainerClient blobContainerClient = storageClient.getBlobContainerClient("myjavacontainerbasic" + System.currentTimeMillis());

        try {
            blobContainerClient.createIfNotExistsWithResponse(new BlobContainerCreateOptions(), Duration.ofSeconds(3L), Context.NONE);
            System.out.println("Created");
        } catch (Exception ex) {
            System.out.println("Creation failed due to timeout: " + ex.getMessage());
        }

    }
}
