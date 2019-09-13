package com.azure.storage.blob;

import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.specialized.LeaseAsyncClient;

import java.time.OffsetDateTime;

public class LeaseAsyncClientJavaDocCodeSnippets {
    private LeaseAsyncClient client = new LeaseAsyncClient(JavaDocCodeSnippetsHelpers.getBlobAsyncClient("blob"));

    /**
     * Code snippets for {@link LeaseAsyncClient#LeaseAsyncClient(BlobAsyncClient)}
     */
    public void constructLeaseAsyncClientFromBlobClient() {
        BlobAsyncClient blobAsyncClient = JavaDocCodeSnippetsHelpers.getBlobAsyncClient("blob");
        // BEGIN: com.azure.storage.blob.specialized.LeaseAsyncClient.initializeWithBlob
        LeaseAsyncClient leaseAsyncClient = new LeaseAsyncClient(blobAsyncClient);
        // END: com.azure.storage.blob.specialized.LeaseAsyncClient.initializeWithBlob
    }

    /**
     * Code snippets for {@link LeaseAsyncClient#LeaseAsyncClient(BlobAsyncClient, String)}
     */
    public void constructLeaseAsyncClientFromBlobClientWithLeaseId() {
        BlobAsyncClient blobAsyncClient = JavaDocCodeSnippetsHelpers.getBlobAsyncClient("blob");
        // BEGIN: com.azure.storage.blob.specialized.LeaseAsyncClient.initializeWithBlobAndLeaseId
        LeaseAsyncClient leaseAsyncClient = new LeaseAsyncClient(blobAsyncClient, "leaseId");
        // END: com.azure.storage.blob.specialized.LeaseAsyncClient.initializeWithBlobAndLeaseId
    }

    /**
     * Code snippets for {@link LeaseAsyncClient#LeaseAsyncClient(ContainerAsyncClient)}
     */
    public void constructLeaseAsyncClientFromContainerClient() {
        ContainerAsyncClient containerAsyncClient = JavaDocCodeSnippetsHelpers.getContainerAsyncClient();
        // BEGIN: com.azure.storage.blob.specialized.LeaseAsyncClient.initializeWithContainer
        LeaseAsyncClient leaseAsyncClient = new LeaseAsyncClient(containerAsyncClient);
        // END: com.azure.storage.blob.specialized.LeaseAsyncClient.initializeWithContainer
    }

    /**
     * Code snippets for {@link LeaseAsyncClient#LeaseAsyncClient(ContainerAsyncClient, String)}
     */
    public void constructLeaseAsyncClientFromContainerClientWithLeaseId() {
        ContainerAsyncClient containerAsyncClient = JavaDocCodeSnippetsHelpers.getContainerAsyncClient();
        // BEGIN: com.azure.storage.blob.specialized.LeaseAsyncClient.initializeWithContainerAndLeaseId
        LeaseAsyncClient leaseAsyncClient = new LeaseAsyncClient(containerAsyncClient, "leaseId");
        // END: com.azure.storage.blob.specialized.LeaseAsyncClient.initializeWithContainerAndLeaseId
    }

    /**
     * Code snippets for {@link LeaseAsyncClient#acquireLease(int)}
     */
    public void acquireLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseAsyncClient.acquireLease#int
        client.acquireLease(60).subscribe(response -> System.out.printf("Lease ID is %s%n", response));
        // END: com.azure.storage.blob.specialized.LeaseAsyncClient.acquireLease#int
    }

    /**
     * Code snippets for {@link LeaseAsyncClient#renewLease()}
     */
    public void renewLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseAsyncClient.renewLease
        client.renewLease().subscribe(response -> System.out.printf("Renewed lease ID is %s%n", response));
        // END: com.azure.storage.blob.specialized.LeaseAsyncClient.renewLease
    }

    /**
     * Code snippets for {@link LeaseAsyncClient#releaseLease()}
     */
    public void releaseLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseAsyncClient.releaseLease
        client.releaseLease().subscribe(response -> System.out.println("Completed release lease"));
        // END: com.azure.storage.blob.specialized.LeaseAsyncClient.releaseLease
    }

    /**
     * Code snippets for {@link LeaseAsyncClient#breakLease()}
     */
    public void breakLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseAsyncClient.breakLease
        client.breakLease().subscribe(response ->
            System.out.printf("The broken lease has %d seconds remaining on the lease", response));
        // END: com.azure.storage.blob.specialized.LeaseAsyncClient.breakLease
    }

    /**
     * Code snippets for {@link LeaseAsyncClient#changeLease(String)}
     */
    public void changeLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseAsyncClient.changeLease#String
        client.changeLease("proposedId").subscribe(response -> System.out.printf("Changed lease ID is %s%n", response));
        // END: com.azure.storage.blob.specialized.LeaseAsyncClient.changeLease#String
    }

    /**
     * Code snippets for {@link LeaseAsyncClient#acquireLeaseWithResponse(int, ModifiedAccessConditions)}
     */
    public void acquireLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseAsyncClient.acquireLeaseWithResponse#int-ModifiedAccessConditions
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfModifiedSince(OffsetDateTime.now().minusDays(3));

        client.acquireLeaseWithResponse(60, modifiedAccessConditions).subscribe(response ->
            System.out.printf("Lease ID is %s%n", response.getValue()));
        // END: com.azure.storage.blob.specialized.LeaseAsyncClient.acquireLeaseWithResponse#int-ModifiedAccessConditions
    }

    /**
     * Code snippets for {@link LeaseAsyncClient#renewLeaseWithResponse(ModifiedAccessConditions)}
     */
    public void renewLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseAsyncClient.renewLeaseWithResponse#ModifiedAccessConditions
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.renewLeaseWithResponse(modifiedAccessConditions).subscribe(response ->
            System.out.printf("Renewed lease ID is %s%n", response.getValue()));
        // END: com.azure.storage.blob.specialized.LeaseAsyncClient.renewLeaseWithResponse#ModifiedAccessConditions
    }

    /**
     * Code snippets for {@link LeaseAsyncClient#releaseLeaseWithResponse(ModifiedAccessConditions)}
     */
    public void releaseLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseAsyncClient.releaseLeaseWithResponse#ModifiedAccessConditions
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.releaseLeaseWithResponse(modifiedAccessConditions).subscribe(response ->
            System.out.printf("Release lease completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.LeaseAsyncClient.releaseLeaseWithResponse#ModifiedAccessConditions
    }

    /**
     * Code snippets for {@link LeaseAsyncClient#breakLeaseWithResponse(Integer, ModifiedAccessConditions)}
     */
    public void breakLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseAsyncClient.breakLeaseWithResponse#Integer-ModifiedAccessConditions
        Integer retainLeaseInSeconds = 5;
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.breakLeaseWithResponse(retainLeaseInSeconds, modifiedAccessConditions).subscribe(response ->
            System.out.printf("The broken lease has %d seconds remaining on the lease", response.getValue()));
        // END: com.azure.storage.blob.specialized.LeaseAsyncClient.breakLeaseWithResponse#Integer-ModifiedAccessConditions
    }

    /**
     * Code snippets for {@link LeaseAsyncClient#changeLeaseWithResponse(String, ModifiedAccessConditions)}
     */
    public void changeLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseAsyncClient.changeLeaseWithResponse#String-ModifiedAccessConditions
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.changeLeaseWithResponse("proposedId", modifiedAccessConditions).subscribe(response ->
            System.out.printf("Changed lease ID is %s%n", response.getValue()));
        // END: com.azure.storage.blob.specialized.LeaseAsyncClient.changeLeaseWithResponse#String-ModifiedAccessConditions
    }
}
