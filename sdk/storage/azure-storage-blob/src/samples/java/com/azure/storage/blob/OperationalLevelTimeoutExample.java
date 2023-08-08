// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.blob.options.BlobContainerCreateOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;

/**
 * This example shows how to use connection level timeouts. These timeouts relate to the entire operation to complete
 * and return, including retries, so any TimeoutException thrown will not be retried. It may be thought of as the amount
 * of time a given sync api call may wait before timing out and allowing the application to proceed. This is
 * highest-level form of retries.
 */
public class OperationalLevelTimeoutExample {

    /**
     * Entry point into the basic examples for Storage blobs.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException If an I/O error occurs
     * @throws RuntimeException If the downloaded data doesn't match the uploaded data
     */
    public static void main(String[] args) throws IOException {

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
         * Create a BlobServiceClient object that wraps the service endpoint, credential, policy with
         * timeout per call, and a request pipeline.
         * Note: this is not necessary to implement timeouts. This is only here to allow the sample to be independently
         * runnable and demonstrate behavior.
         */
        BlobServiceClient storageClient = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .addPolicy(new TimeoutPolicy())
            .buildClient();

        /*
         * This example shows how to pass timeouts in call operations.
         */

        /*
         * Create a client that references a to-be-created container in your Azure Storage account. This returns a
         * ContainerClient object that wraps the container's endpoint, credential and a request pipeline (inherited from storageClient).
         * Note that container names require lowercase.
         */
        BlobContainerClient blobContainerClient = storageClient.getBlobContainerClient("myjavacontainerbasic" + System.currentTimeMillis());

        /*
         * Create a container in Storage blob account with a timeout duration of 3 seconds. A timeout exception will
         * occur if the blob create container call takes longer than 3 seconds to complete as this could not throw if
         * it took 2 seconds.
         */
        try {
            blobContainerClient.createIfNotExistsWithResponse(new BlobContainerCreateOptions(), Duration.ofSeconds(3L), Context.NONE);
        } catch (Exception ex) {
            System.out.println("Creation failed due to timeout: " + ex.getMessage());
        }

        /*
         * Create a client that references a to-be-created container in your Azure Storage account. This returns a
         * ContainerClient object that wraps the container's endpoint, credential and a request pipeline (inherited from storageClient).
         * Note that container names require lowercase.
         */
        blobContainerClient = storageClient.getBlobContainerClient("myjavacontainerbasic" + System.currentTimeMillis());

        /*
         * Create a container in Storage blob account with a timeout duration of 10 seconds, greater the timeout duration
         * passed in the policy. This will succeed.
         */
        Response<Boolean> response = blobContainerClient.createIfNotExistsWithResponse(new BlobContainerCreateOptions(), Duration.ofSeconds(10L), Context.NONE);
        if (response.getValue()) {
            System.out.println("Blob container successfully created.");
        }

        /*
         * Delete the container we created earlier.
         */
        blobContainerClient.delete();
    }

    /**
     * A simple policy that sets duration timeout per call of 5 seconds.
     */
    static class TimeoutPolicy implements HttpPipelinePolicy {
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return next.process().delayElement(Duration.ofSeconds(5L));
        }

        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_CALL;
        }
    }
}
