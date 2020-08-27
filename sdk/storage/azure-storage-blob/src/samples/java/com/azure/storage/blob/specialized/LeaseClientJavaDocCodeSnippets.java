// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.RequestConditions;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.models.BlobLeaseRequestConditions;
import com.azure.storage.blob.options.BlobAcquireLeaseOptions;
import com.azure.storage.blob.options.BlobBreakLeaseOptions;
import com.azure.storage.blob.options.BlobChangeLeaseOptions;
import com.azure.storage.blob.options.BlobReleaseLeaseOptions;
import com.azure.storage.blob.options.BlobRenewLeaseOptions;

import java.time.Duration;
import java.time.OffsetDateTime;

public class LeaseClientJavaDocCodeSnippets {
    private BlobLeaseClient client = new BlobLeaseClientBuilder()
        .blobClient(new BlobClientBuilder().blobName("blob").buildClient())
        .buildClient();
    private Duration timeout = Duration.ofSeconds(30);
    private String key = "key";
    private String value = "value";

    /**
     * Code snippets for {@link BlobLeaseClient#acquireLease(int)}
     */
    public void acquireLease() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClient.acquireLease#int
        System.out.printf("Lease ID is %s%n", client.acquireLease(60));
        // END: com.azure.storage.blob.specialized.BlobLeaseClient.acquireLease#int
    }

    /**
     * Code snippets for {@link BlobLeaseClient#renewLease()}
     */
    public void renewLease() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClient.renewLease
        System.out.printf("Renewed lease ID is %s%n", client.renewLease());
        // END: com.azure.storage.blob.specialized.BlobLeaseClient.renewLease
    }

    /**
     * Code snippets for {@link BlobLeaseClient#releaseLease()}
     */
    public void releaseLease() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClient.releaseLease
        client.releaseLease();
        System.out.println("Release lease completed");
        // END: com.azure.storage.blob.specialized.BlobLeaseClient.releaseLease
    }

    /**
     * Code snippets for {@link BlobLeaseClient#breakLease()}
     */
    public void breakLease() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClient.breakLease
        System.out.printf("The broken lease has %d seconds remaining on the lease", client.breakLease());
        // END: com.azure.storage.blob.specialized.BlobLeaseClient.breakLease
    }

    /**
     * Code snippets for {@link BlobLeaseClient#changeLease(String)}
     */
    public void changeLease() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClient.changeLease#String
        System.out.printf("Changed lease ID is %s%n", client.changeLease("proposedId"));
        // END: com.azure.storage.blob.specialized.BlobLeaseClient.changeLease#String
    }

    /**
     * Code snippets for {@link BlobLeaseClient#acquireLeaseWithResponse(int, com.azure.core.http.RequestConditions, Duration, Context)}
     */
    public void acquireLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClient.acquireLeaseWithResponse#int-RequestConditions-Duration-Context
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfModifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("Lease ID is %s%n", client
            .acquireLeaseWithResponse(60, modifiedRequestConditions, timeout, new Context(key, value))
            .getValue());
        // END: com.azure.storage.blob.specialized.BlobLeaseClient.acquireLeaseWithResponse#int-RequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobLeaseClient#acquireLeaseWithResponse(BlobAcquireLeaseOptions, Duration, Context)}
     */
    public void acquireLeaseWithResponseCodeSnippets2() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClient.acquireLeaseWithResponse#BlobAcquireLeaseOptions-Duration-Context
        BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions()
            .setIfModifiedSince(OffsetDateTime.now().minusDays(3));

        BlobAcquireLeaseOptions options = new BlobAcquireLeaseOptions(60)
            .setRequestConditions(requestConditions);

        System.out.printf("Lease ID is %s%n", client
            .acquireLeaseWithResponse(options, timeout, new Context(key, value))
            .getValue());
        // END: com.azure.storage.blob.specialized.BlobLeaseClient.acquireLeaseWithResponse#BlobAcquireLeaseOptions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobLeaseClient#renewLeaseWithResponse(RequestConditions, Duration, Context)}
     */
    public void renewLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClient.renewLeaseWithResponse#RequestConditions-Duration-Context
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("Renewed lease ID is %s%n",
            client.renewLeaseWithResponse(modifiedRequestConditions, timeout, new Context(key, value))
                .getValue());
        // END: com.azure.storage.blob.specialized.BlobLeaseClient.renewLeaseWithResponse#RequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobLeaseClient#renewLeaseWithResponse(BlobRenewLeaseOptions, Duration, Context)}
     */
    public void renewLeaseWithResponseCodeSnippets2() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClient.renewLeaseWithResponse#BlobRenewLeaseOptions-Duration-Context
        BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions()
            .setIfModifiedSince(OffsetDateTime.now().minusDays(3));

        BlobRenewLeaseOptions options = new BlobRenewLeaseOptions()
            .setRequestConditions(requestConditions);

        System.out.printf("Renewed lease ID is %s%n",
            client.renewLeaseWithResponse(options, timeout, new Context(key, value))
                .getValue());
        // END: com.azure.storage.blob.specialized.BlobLeaseClient.renewLeaseWithResponse#BlobRenewLeaseOptions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobLeaseClient#releaseLeaseWithResponse(RequestConditions, Duration, Context)}
     */
    public void releaseLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClient.releaseLeaseWithResponse#RequestConditions-Duration-Context
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("Release lease completed with status %d%n",
            client.releaseLeaseWithResponse(modifiedRequestConditions, timeout, new Context(key, value))
                .getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobLeaseClient.releaseLeaseWithResponse#RequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobLeaseClient#releaseLeaseWithResponse(BlobReleaseLeaseOptions, Duration, Context)}
     */
    public void releaseLeaseWithResponseCodeSnippets2() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClient.releaseLeaseWithResponse#BlobReleaseLeaseOptions-Duration-Context
        BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions()
            .setIfModifiedSince(OffsetDateTime.now().minusDays(3));

        BlobReleaseLeaseOptions options = new BlobReleaseLeaseOptions()
            .setRequestConditions(requestConditions);

        System.out.printf("Release lease completed with status %d%n",
            client.releaseLeaseWithResponse(options, timeout, new Context(key, value))
                .getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobLeaseClient.releaseLeaseWithResponse#BlobReleaseLeaseOptions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobLeaseClient#breakLeaseWithResponse(Integer, RequestConditions, Duration, Context)}
     */
    public void breakLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClient.breakLeaseWithResponse#Integer-RequestConditions-Duration-Context
        Integer retainLeaseInSeconds = 5;
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("The broken lease has %d seconds remaining on the lease", client
            .breakLeaseWithResponse(retainLeaseInSeconds, modifiedRequestConditions, timeout, new Context(key, value))
            .getValue());
        // END: com.azure.storage.blob.specialized.BlobLeaseClient.breakLeaseWithResponse#Integer-RequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobLeaseClient#breakLeaseWithResponse(BlobBreakLeaseOptions, Duration, Context)}
     */
    public void breakLeaseWithResponseCodeSnippets2() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClient.breakLeaseWithResponse#BlobBreakLeaseOptions-Duration-Context
        Integer retainLeaseInSeconds = 5;
        BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        BlobBreakLeaseOptions options = new BlobBreakLeaseOptions()
            .setBreakPeriod(Duration.ofSeconds(retainLeaseInSeconds))
            .setRequestConditions(requestConditions);

        System.out.printf("The broken lease has %d seconds remaining on the lease", client
            .breakLeaseWithResponse(options, timeout, new Context(key, value))
            .getValue());
        // END: com.azure.storage.blob.specialized.BlobLeaseClient.breakLeaseWithResponse#BlobBreakLeaseOptions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobLeaseClient#changeLeaseWithResponse(String, RequestConditions, Duration, Context)}
     */
    public void changeLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClient.changeLeaseWithResponse#String-RequestConditions-Duration-Context
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("Changed lease ID is %s%n",
            client.changeLeaseWithResponse("proposedId", modifiedRequestConditions, timeout, new Context(key, value))
                .getValue());
        // END: com.azure.storage.blob.specialized.BlobLeaseClient.changeLeaseWithResponse#String-RequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobLeaseClient#changeLeaseWithResponse(BlobChangeLeaseOptions, Duration, Context)}
     */
    public void changeLeaseWithResponseCodeSnippets2() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClient.changeLeaseWithResponse#BlobChangeLeaseOptions-Duration-Context
        BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        BlobChangeLeaseOptions options = new BlobChangeLeaseOptions("proposedId")
            .setRequestConditions(requestConditions);

        System.out.printf("Changed lease ID is %s%n",
            client.changeLeaseWithResponse(options, timeout, new Context(key, value))
                .getValue());
        // END: com.azure.storage.blob.specialized.BlobLeaseClient.changeLeaseWithResponse#BlobChangeLeaseOptions-Duration-Context
    }
}
