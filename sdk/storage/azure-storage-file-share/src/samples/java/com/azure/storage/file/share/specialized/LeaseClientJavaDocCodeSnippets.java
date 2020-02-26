// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.specialized;

import com.azure.core.util.Context;
import com.azure.storage.file.share.ShareFileClientBuilder;

import java.time.Duration;

public class LeaseClientJavaDocCodeSnippets {
    private ShareLeaseClient client = new ShareLeaseClientBuilder()
        .fileClient(new ShareFileClientBuilder().resourcePath("file").buildFileClient())
        .buildClient();
    private Duration timeout = Duration.ofSeconds(30);
    private String key = "key";
    private String value = "value";

    /**
     * Code snippets for {@link ShareLeaseClient#acquireLease()}
     */
    public void acquireLease() {
        // BEGIN: com.azure.storage.file.share.specialized.ShareLeaseClient.acquireLease
        System.out.printf("Lease ID is %s%n", client.acquireLease());
        // END: com.azure.storage.file.share.specialized.ShareLeaseClient.acquireLease
    }

    /**
     * Code snippets for {@link ShareLeaseClient#releaseLease()}
     */
    public void releaseLease() {
        // BEGIN: com.azure.storage.file.share.specialized.ShareLeaseClient.releaseLease
        client.releaseLease();
        System.out.println("Release lease completed");
        // END: com.azure.storage.file.share.specialized.ShareLeaseClient.releaseLease
    }

    /**
     * Code snippets for {@link ShareLeaseClient#breakLease()}
     */
    public void breakLease() {
        // BEGIN: com.azure.storage.file.share.specialized.ShareLeaseClient.breakLease
        client.breakLease();
        System.out.println("The lease has been successfully broken");
        // END: com.azure.storage.file.share.specialized.ShareLeaseClient.breakLease
    }

    /**
     * Code snippets for {@link ShareLeaseClient#changeLease(String)}
     */
    public void changeLease() {
        // BEGIN: com.azure.storage.file.share.specialized.ShareLeaseClient.changeLease#String
        System.out.printf("Changed lease ID is %s%n", client.changeLease("proposedId"));
        // END: com.azure.storage.file.share.specialized.ShareLeaseClient.changeLease#String
    }

    /**
     * Code snippets for {@link ShareLeaseClient#acquireLeaseWithResponse(Duration, Context)}
     */
    public void acquireLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.share.specialized.ShareLeaseClient.acquireLeaseWithResponse#Duration-Context
        System.out.printf("Lease ID is %s%n", client
            .acquireLeaseWithResponse(timeout, new Context(key, value))
            .getValue());
        // END: com.azure.storage.file.share.specialized.ShareLeaseClient.acquireLeaseWithResponse#Duration-Context
    }

    /**
     * Code snippets for {@link ShareLeaseClient#releaseLeaseWithResponse(Duration, Context)}
     */
    public void releaseLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.share.specialized.ShareLeaseClient.releaseLeaseWithResponse#Duration-Context
        System.out.printf("Release lease completed with status %d%n",
            client.releaseLeaseWithResponse(timeout, new Context(key, value))
                .getStatusCode());
        // END: com.azure.storage.file.share.specialized.ShareLeaseClient.releaseLeaseWithResponse#Duration-Context
    }

    /**
     * Code snippets for {@link ShareLeaseClient#breakLeaseWithResponse(Duration, Context)}
     */
    public void breakLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.share.specialized.ShareLeaseClient.breakLeaseWithResponse#Duration-Context
        client.breakLeaseWithResponse(timeout, new Context(key, value));
        System.out.println("The lease has been successfully broken");
        // END: com.azure.storage.file.share.specialized.ShareLeaseClient.breakLeaseWithResponse#Duration-Context
    }

    /**
     * Code snippets for {@link ShareLeaseClient#changeLeaseWithResponse(String, Duration, Context)}
     */
    public void changeLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.share.specialized.ShareLeaseClient.changeLeaseWithResponse#String-Duration-Context
        System.out.printf("Changed lease ID is %s%n",
            client.changeLeaseWithResponse("proposedId", timeout, new Context(key, value))
                .getValue());
        // END: com.azure.storage.file.share.specialized.ShareLeaseClient.changeLeaseWithResponse#String-Duration-Context
    }
}
