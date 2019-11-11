// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.RequestConditions;
import com.azure.storage.blob.BlobClientBuilder;

import java.time.OffsetDateTime;


public class LeaseAsyncClientJavaDocCodeSnippets {
    private BlobLeaseAsyncClient client = new BlobLeaseClientBuilder()
        .blobAsyncClient(new BlobClientBuilder().blobName("blob").buildAsyncClient())
        .buildAsyncClient();

    /**
     * Code snippets for {@link BlobLeaseAsyncClient#acquireLease(int)}
     */
    public void acquireLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.acquireLease#int
        client.acquireLease(60).subscribe(response -> System.out.printf("Lease ID is %s%n", response));
        // END: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.acquireLease#int
    }

    /**
     * Code snippets for {@link BlobLeaseAsyncClient#renewLease()}
     */
    public void renewLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.renewLease
        client.renewLease().subscribe(response -> System.out.printf("Renewed lease ID is %s%n", response));
        // END: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.renewLease
    }

    /**
     * Code snippets for {@link BlobLeaseAsyncClient#releaseLease()}
     */
    public void releaseLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.releaseLease
        client.releaseLease().subscribe(response -> System.out.println("Completed release lease"));
        // END: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.releaseLease
    }

    /**
     * Code snippets for {@link BlobLeaseAsyncClient#breakLease()}
     */
    public void breakLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.breakLease
        client.breakLease().subscribe(response ->
            System.out.printf("The broken lease has %d seconds remaining on the lease", response));
        // END: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.breakLease
    }

    /**
     * Code snippets for {@link BlobLeaseAsyncClient#changeLease(String)}
     */
    public void changeLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.changeLease#String
        client.changeLease("proposedId").subscribe(response -> System.out.printf("Changed lease ID is %s%n", response));
        // END: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.changeLease#String
    }

    /**
     * Code snippets for {@link BlobLeaseAsyncClient#acquireLeaseWithResponse(int, RequestConditions)}
     */
    public void acquireLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.acquireLeaseWithResponse#int-RequestConditions
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfModifiedSince(OffsetDateTime.now().minusDays(3));

        client.acquireLeaseWithResponse(60, modifiedRequestConditions).subscribe(response ->
            System.out.printf("Lease ID is %s%n", response.getValue()));
        // END: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.acquireLeaseWithResponse#int-RequestConditions
    }

    /**
     * Code snippets for {@link BlobLeaseAsyncClient#renewLeaseWithResponse(RequestConditions)}
     */
    public void renewLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.renewLeaseWithResponse#RequestConditions
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.renewLeaseWithResponse(modifiedRequestConditions).subscribe(response ->
            System.out.printf("Renewed lease ID is %s%n", response.getValue()));
        // END: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.renewLeaseWithResponse#RequestConditions
    }

    /**
     * Code snippets for {@link BlobLeaseAsyncClient#releaseLeaseWithResponse(RequestConditions)}
     */
    public void releaseLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.releaseLeaseWithResponse#RequestConditions
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.releaseLeaseWithResponse(modifiedRequestConditions).subscribe(response ->
            System.out.printf("Release lease completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.releaseLeaseWithResponse#RequestConditions
    }

    /**
     * Code snippets for {@link BlobLeaseAsyncClient#breakLeaseWithResponse(Integer, RequestConditions)}
     */
    public void breakLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.breakLeaseWithResponse#Integer-RequestConditions
        Integer retainLeaseInSeconds = 5;
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.breakLeaseWithResponse(retainLeaseInSeconds, modifiedRequestConditions).subscribe(response ->
            System.out.printf("The broken lease has %d seconds remaining on the lease", response.getValue()));
        // END: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.breakLeaseWithResponse#Integer-RequestConditions
    }

    /**
     * Code snippets for {@link BlobLeaseAsyncClient#changeLeaseWithResponse(String, RequestConditions)}
     */
    public void changeLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.changeLeaseWithResponse#String-RequestConditions
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.changeLeaseWithResponse("proposedId", modifiedRequestConditions).subscribe(response ->
            System.out.printf("Changed lease ID is %s%n", response.getValue()));
        // END: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.changeLeaseWithResponse#String-RequestConditions
    }
}
