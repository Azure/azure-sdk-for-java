// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import java.util.StringJoiner;

/**
 * Sample that demonstrates the use {@link BlobEventProcessorStore} for storing and updating partition ownership records
 * in Storage Blobs.
 */
public class BlobEventProcessorStoreSample {

    /**
     * The main method to run the sample.
     *
     * @param args Unused arguments to the sample.
     * @throws Exception If there are any errors while running the sample.
     */
    public static void main(String[] args) throws Exception {
        String sasToken = "";
        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
            .connectionString("")
            .containerName("")
            .sasToken(sasToken)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildAsyncClient();

        BlobEventProcessorStore blobEventProcessorStore = new BlobEventProcessorStore(blobContainerAsyncClient);
        blobEventProcessorStore.listOwnership("namespace", "abc", "xyz")
            .subscribe(BlobEventProcessorStoreSample::printPartitionOwnership);

        System.out.println("Updating checkpoint");
        Checkpoint checkpoint = new Checkpoint()
            .setConsumerGroupName("xyz")
            .setEventHubName("abc")
            .setOwnerId("owner1")
            .setPartitionId("0")
            .setETag("")
            .setSequenceNumber(2L)
            .setOffset(250L);
        blobEventProcessorStore.updateCheckpoint(checkpoint)
            .subscribe(etag -> System.out.println(etag), error -> System.out
                .println(error.getMessage()));

        PartitionOwnership[] pos = new PartitionOwnership[5];
        for (int i = 0; i < 5; i++) {
            PartitionOwnership po = new PartitionOwnership()
                .setEventHubName("abc")
                .setConsumerGroupName("xyz")
                .setOwnerId("owner1")
                .setPartitionId(String.valueOf(i))
                .setOwnerLevel(0);
            pos[i] = po;
        }
        blobEventProcessorStore.claimOwnership(pos).subscribe(BlobEventProcessorStoreSample::printPartitionOwnership,
            System.out::println);
    }

    static void printPartitionOwnership(PartitionOwnership partitionOwnership) {
        String po =
            new StringJoiner(",")
                .add("pid=" + partitionOwnership.getPartitionId())
                .add("ownerId=" + partitionOwnership.getOwnerId())
                .add("cg=" + partitionOwnership.getConsumerGroupName())
                .add("eh=" + partitionOwnership.getEventHubName())
                .add("offset=" + partitionOwnership.getOffset())
                .add("etag=" + partitionOwnership.getETag())
                .add("lastModified=" + partitionOwnership.getLastModifiedTime())
                .toString();
        System.out.println(po);
    }

}

