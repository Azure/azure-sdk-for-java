// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.models.ModifiedAccessConditions;

import java.time.Duration;
import java.time.OffsetDateTime;

public class LeaseClientJavaDocCodeSnippets {
    private LeaseClient client = new LeaseClientBuilder()
        .blobClient(new BlobClientBuilder().blobName("blob").buildBlobClient())
        .buildClient();
    private Duration timeout = Duration.ofSeconds(30);
    private String key = "key";
    private String value = "value";

    /**
     * Code snippets for {@link LeaseClient#acquireLease(int)}
     */
    public void acquireLease() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClient.acquireLease#int
        System.out.printf("Lease ID is %s%n", client.acquireLease(60));
        // END: com.azure.storage.blob.specialized.LeaseClient.acquireLease#int
    }

    /**
     * Code snippets for {@link LeaseClient#renewLease()}
     */
    public void renewLease() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClient.renewLease
        System.out.printf("Renewed lease ID is %s%n", client.renewLease());
        // END: com.azure.storage.blob.specialized.LeaseClient.renewLease
    }

    /**
     * Code snippets for {@link LeaseClient#releaseLease()}
     */
    public void releaseLease() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClient.releaseLease
        client.releaseLease();
        System.out.println("Release lease completed");
        // END: com.azure.storage.blob.specialized.LeaseClient.releaseLease
    }

    /**
     * Code snippets for {@link LeaseClient#breakLease()}
     */
    public void breakLease() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClient.breakLease
        System.out.printf("The broken lease has %d seconds remaining on the lease", client.breakLease());
        // END: com.azure.storage.blob.specialized.LeaseClient.breakLease
    }

    /**
     * Code snippets for {@link LeaseClient#changeLease(String)}
     */
    public void changeLease() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClient.changeLease#String
        System.out.printf("Changed lease ID is %s%n", client.changeLease("proposedId"));
        // END: com.azure.storage.blob.specialized.LeaseClient.changeLease#String
    }

    /**
     * Code snippets for {@link LeaseClient#acquireLeaseWithResponse(int, ModifiedAccessConditions, Duration, Context)}
     */
    public void acquireLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClient.acquireLeaseWithResponse#int-ModifiedAccessConditions-Duration-Context
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfModifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("Lease ID is %s%n", client
            .acquireLeaseWithResponse(60, modifiedAccessConditions, timeout, new Context(key, value))
            .getValue());
        // END: com.azure.storage.blob.specialized.LeaseClient.acquireLeaseWithResponse#int-ModifiedAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link LeaseClient#renewLeaseWithResponse(ModifiedAccessConditions, Duration, Context)}
     */
    public void renewLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClient.renewLeaseWithResponse#ModifiedAccessConditions-Duration-Context
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("Renewed lease ID is %s%n",
            client.renewLeaseWithResponse(modifiedAccessConditions, timeout, new Context(key, value))
                .getValue());
        // END: com.azure.storage.blob.specialized.LeaseClient.renewLeaseWithResponse#ModifiedAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link LeaseClient#releaseLeaseWithResponse(ModifiedAccessConditions, Duration, Context)}
     */
    public void releaseLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClient.releaseLeaseWithResponse#ModifiedAccessConditions-Duration-Context
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("Release lease completed with status %d%n",
            client.releaseLeaseWithResponse(modifiedAccessConditions, timeout, new Context(key, value))
                .getStatusCode());
        // END: com.azure.storage.blob.specialized.LeaseClient.releaseLeaseWithResponse#ModifiedAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link LeaseClient#breakLeaseWithResponse(Integer, ModifiedAccessConditions, Duration,
     * Context)}
     */
    public void breakLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClient.breakLeaseWithResponse#Integer-ModifiedAccessConditions-Duration-Context
        Integer retainLeaseInSeconds = 5;
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("The broken lease has %d seconds remaining on the lease", client
            .breakLeaseWithResponse(retainLeaseInSeconds, modifiedAccessConditions, timeout, new Context(key, value))
            .getValue());
        // END: com.azure.storage.blob.specialized.LeaseClient.breakLeaseWithResponse#Integer-ModifiedAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link LeaseClient#changeLeaseWithResponse(String, ModifiedAccessConditions, Duration,
     * Context)}
     */
    public void changeLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClient.changeLeaseWithResponse#String-ModifiedAccessConditions-Duration-Context
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("Changed lease ID is %s%n",
            client.changeLeaseWithResponse("proposedId", modifiedAccessConditions, timeout, new Context(key, value))
                .getValue());
        // END: com.azure.storage.blob.specialized.LeaseClient.changeLeaseWithResponse#String-ModifiedAccessConditions-Duration-Context
    }
}
