// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.RequestConditions;
import com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient;
import com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder;

import java.time.OffsetDateTime;


public class LeaseAsyncClientJavaDocCodeSnippets {
    private DataLakeLeaseAsyncClient client = new DataLakeLeaseClientBuilder()
        .fileAsyncClient(JavaDocCodeSnippetsHelpers.getFileAsyncClient("fileName"))
        .buildAsyncClient();

    /**
     * Code snippets for {@link DataLakeLeaseAsyncClient#acquireLease(int)}
     */
    public void acquireLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.acquireLease#int
        client.acquireLease(60).subscribe(response -> System.out.printf("Lease ID is %s%n", response));
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.acquireLease#int
    }

    /**
     * Code snippets for {@link DataLakeLeaseAsyncClient#renewLease()}
     */
    public void renewLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.renewLease
        client.renewLease().subscribe(response -> System.out.printf("Renewed lease ID is %s%n", response));
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.renewLease
    }

    /**
     * Code snippets for {@link DataLakeLeaseAsyncClient#releaseLease()}
     */
    public void releaseLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.releaseLease
        client.releaseLease().subscribe(response -> System.out.println("Completed release lease"));
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.releaseLease
    }

    /**
     * Code snippets for {@link DataLakeLeaseAsyncClient#breakLease()}
     */
    public void breakLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.breakLease
        client.breakLease().subscribe(response ->
            System.out.printf("The broken lease has %d seconds remaining on the lease", response));
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.breakLease
    }

    /**
     * Code snippets for {@link DataLakeLeaseAsyncClient#changeLease(String)}
     */
    public void changeLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.changeLease#String
        client.changeLease("proposedId").subscribe(response -> System.out.printf("Changed lease ID is %s%n", response));
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.changeLease#String
    }

    /**
     * Code snippets for {@link DataLakeLeaseAsyncClient#acquireLeaseWithResponse(int, RequestConditions)}
     */
    public void acquireLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.acquireLeaseWithResponse#int-RequestConditions
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfModifiedSince(OffsetDateTime.now().minusDays(3));

        client.acquireLeaseWithResponse(60, modifiedRequestConditions).subscribe(response ->
            System.out.printf("Lease ID is %s%n", response.getValue()));
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.acquireLeaseWithResponse#int-RequestConditions
    }

    /**
     * Code snippets for {@link DataLakeLeaseAsyncClient#renewLeaseWithResponse(RequestConditions)}
     */
    public void renewLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.renewLeaseWithResponse#RequestConditions
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.renewLeaseWithResponse(modifiedRequestConditions).subscribe(response ->
            System.out.printf("Renewed lease ID is %s%n", response.getValue()));
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.renewLeaseWithResponse#RequestConditions
    }

    /**
     * Code snippets for {@link DataLakeLeaseAsyncClient#releaseLeaseWithResponse(RequestConditions)}
     */
    public void releaseLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.releaseLeaseWithResponse#RequestConditions
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.releaseLeaseWithResponse(modifiedRequestConditions).subscribe(response ->
            System.out.printf("Release lease completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.releaseLeaseWithResponse#RequestConditions
    }

    /**
     * Code snippets for {@link DataLakeLeaseAsyncClient#breakLeaseWithResponse(Integer, RequestConditions)}
     */
    public void breakLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.breakLeaseWithResponse#Integer-RequestConditions
        Integer retainLeaseInSeconds = 5;
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.breakLeaseWithResponse(retainLeaseInSeconds, modifiedRequestConditions).subscribe(response ->
            System.out.printf("The broken lease has %d seconds remaining on the lease", response.getValue()));
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.breakLeaseWithResponse#Integer-RequestConditions
    }

    /**
     * Code snippets for {@link DataLakeLeaseAsyncClient#changeLeaseWithResponse(String, RequestConditions)}
     */
    public void changeLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.changeLeaseWithResponse#String-RequestConditions
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.changeLeaseWithResponse("proposedId", modifiedRequestConditions).subscribe(response ->
            System.out.printf("Changed lease ID is %s%n", response.getValue()));
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.changeLeaseWithResponse#String-RequestConditions
    }
}
