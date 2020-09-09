// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.RequestConditions;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.models.BlobLeaseRequestConditions;
import com.azure.storage.blob.options.BlobAcquireLeaseOptions;
import com.azure.storage.blob.options.BlobBreakLeaseOptions;
import com.azure.storage.blob.options.BlobChangeLeaseOptions;
import com.azure.storage.blob.options.BlobReleaseLeaseOptions;
import com.azure.storage.blob.options.BlobRenewLeaseOptions;

import java.time.Duration;
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
     * Code snippets for {@link BlobLeaseAsyncClient#acquireLeaseWithResponse(BlobAcquireLeaseOptions)}
     */
    public void acquireLeaseWithResponseCodeSnippets2() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.acquireLeaseWithResponse#BlobAcquireLeaseOptions
        BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions()
            .setIfModifiedSince(OffsetDateTime.now().minusDays(3));

        BlobAcquireLeaseOptions options = new BlobAcquireLeaseOptions(60)
            .setRequestConditions(requestConditions);

        client.acquireLeaseWithResponse(options).subscribe(response ->
            System.out.printf("Lease ID is %s%n", response.getValue()));
        // END: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.acquireLeaseWithResponse#BlobAcquireLeaseOptions
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
     * Code snippets for {@link BlobLeaseAsyncClient#renewLeaseWithResponse(BlobRenewLeaseOptions)}
     */
    public void renewLeaseWithResponseCodeSnippets2() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.renewLeaseWithResponse#BlobRenewLeaseOptions
        BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions()
            .setIfModifiedSince(OffsetDateTime.now().minusDays(3));

        BlobRenewLeaseOptions options = new BlobRenewLeaseOptions()
            .setRequestConditions(requestConditions);

        client.renewLeaseWithResponse(options).subscribe(response ->
            System.out.printf("Lease ID is %s%n", response.getValue()));
        // END: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.renewLeaseWithResponse#BlobRenewLeaseOptions
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
     * Code snippets for {@link BlobLeaseAsyncClient#releaseLeaseWithResponse(RequestConditions)}
     */
    public void releaseLeaseWithResponseCodeSnippets2() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.releaseLeaseWithResponse#BlobReleaseLeaseOptions
        BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions()
            .setIfModifiedSince(OffsetDateTime.now().minusDays(3));

        BlobReleaseLeaseOptions options = new BlobReleaseLeaseOptions()
            .setRequestConditions(requestConditions);

        client.releaseLeaseWithResponse(options).subscribe(response ->
            System.out.printf("Release lease completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.releaseLeaseWithResponse#BlobReleaseLeaseOptions
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
     * Code snippets for {@link BlobLeaseAsyncClient#breakLeaseWithResponse(BlobBreakLeaseOptions)}
     */
    public void breakLeaseWithResponseCodeSnippets2() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.breakLeaseWithResponse#BlobBreakLeaseOptions
        Integer retainLeaseInSeconds = 5;
        BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        BlobBreakLeaseOptions options = new BlobBreakLeaseOptions()
            .setBreakPeriod(Duration.ofSeconds(retainLeaseInSeconds))
            .setRequestConditions(requestConditions);

        client.breakLeaseWithResponse(options).subscribe(response ->
            System.out.printf("The broken lease has %d seconds remaining on the lease", response.getValue()));
        // END: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.breakLeaseWithResponse#BlobBreakLeaseOptions
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

    /**
     * Code snippets for {@link BlobLeaseAsyncClient#changeLeaseWithResponse(BlobChangeLeaseOptions)}
     */
    public void changeLeaseWithResponseCodeSnippets2() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.changeLeaseWithResponse#BlobChangeLeaseOptions
        BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        BlobChangeLeaseOptions options = new BlobChangeLeaseOptions("proposedId")
            .setRequestConditions(requestConditions);

        client.changeLeaseWithResponse(options).subscribe(response ->
            System.out.printf("Changed lease ID is %s%n", response.getValue()));
        // END: com.azure.storage.blob.specialized.BlobLeaseAsyncClient.changeLeaseWithResponse#BlobChangeLeaseOptions
    }
}
