// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.storage.blob.ContainerAsyncClient;
import com.azure.storage.blob.ContainerClientBuilder;
import com.azure.storage.common.credentials.SASTokenCredential;
import java.util.StringJoiner;

/**
 * Sample that demonstrates the use {@link BlobPartitionManager} for storing and updating partition ownership records in
 * Storage Blobs.
 */
public class BlobPartitionManagerSample {

    /**
     * The main method to run the sample.
     *
     * @param args Unused arguments to the sample.
     * @throws Exception If there are any errors while running the sample.
     */
    public static void main(String[] args) throws Exception {
        SASTokenCredential sasTokenCredential = SASTokenCredential.fromSASTokenString("");
        ContainerAsyncClient containerAsyncClient = new ContainerClientBuilder()
            .connectionString("")
            .containerName("")
            .credential(sasTokenCredential)
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .buildAsyncClient();

        BlobPartitionManager blobPartitionManager = new BlobPartitionManager(containerAsyncClient);
        blobPartitionManager.listOwnership("abc", "xyz")
            .subscribe(BlobPartitionManagerSample::printPartitionOwnership);

        System.out.println("Updating checkpoint");
        Checkpoint checkpoint = new Checkpoint()
            .consumerGroupName("xyz")
            .eventHubName("abc")
            .ownerId("owner1")
            .partitionId("0")
            .eTag("")
            .sequenceNumber(2L)
            .offset("250");
        blobPartitionManager.updateCheckpoint(checkpoint)
            .subscribe(etag -> System.out.println(etag), error -> System.out
                .println(error.getMessage()));

        PartitionOwnership[] pos = new PartitionOwnership[5];
        for (int i = 0; i < 5; i++) {
            PartitionOwnership po = new PartitionOwnership()
                .eventHubName("abc")
                .consumerGroupName("xyz")
                .ownerId("owner1")
                .partitionId(String.valueOf(i))
                .ownerLevel(0);
            pos[i] = po;
        }
        blobPartitionManager.claimOwnership(pos).subscribe(BlobPartitionManagerSample::printPartitionOwnership,
            System.out::println);
    }

    static void printPartitionOwnership(PartitionOwnership partitionOwnership) {
        String po =
            new StringJoiner(",")
                .add("pid=" + partitionOwnership.partitionId())
                .add("ownerId=" + partitionOwnership.ownerId())
                .add("cg=" + partitionOwnership.consumerGroupName())
                .add("eh=" + partitionOwnership.eventHubName())
                .add("offset=" + partitionOwnership.offset())
                .add("etag=" + partitionOwnership.eTag())
                .add("lastModified=" + partitionOwnership.lastModifiedTime())
                .toString();
        System.out.println(po);
    }

}

