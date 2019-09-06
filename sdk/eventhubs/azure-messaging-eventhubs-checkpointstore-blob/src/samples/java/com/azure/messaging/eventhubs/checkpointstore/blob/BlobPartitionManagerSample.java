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
import java.util.concurrent.TimeUnit;

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

        SASTokenCredential sasTokenCredential = SASTokenCredential.fromSASTokenString(
            "?sv=2018-03-28&ss=bfqt&srt=sco&sp=rwdlacup&se=2019-09-06T17:16:43Z&st=2019-09-06T09:16:43Z&spr=https&sig=V3PJKKUnAujEmNK9ao3l%2FaTHmqWUfQPmVcFZ9AstdtU%3D");

        ContainerAsyncClient containerAsyncClient = new ContainerClientBuilder()
            .connectionString(
                "DefaultEndpointsProtocol=https;AccountName=srnagarstorage;AccountKey=YXCbf2ImDwCExM58k5Ub8+p054jjZopP9y7nIZvqUEhUfkF89smXN4fAA/v61lGDCSaQDqFC3rbE5VS8NjhAyQ==;EndpointSuffix=core.windows.net")
            .containerName("srnagartestcontainer")
            .credential(sasTokenCredential)
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .buildAsyncClient();

        BlobPartitionManager blobPartitionManager = new BlobPartitionManager(containerAsyncClient);
        blobPartitionManager.listOwnership("abc", "xyz")
            .subscribe(BlobPartitionManagerSample::printPartitionOwnership);

        TimeUnit.SECONDS.sleep(10);

        System.out.println("Updating checkpoint");
        Checkpoint checkpoint = new Checkpoint()
            .consumerGroupName("xyz")
            .eventHubName("abc")
            .ownerId("owner1")
            .partitionId("0")
            .eTag("0x8D732AC01E51ED7")
            .sequenceNumber(2L)
            .offset("250");
        blobPartitionManager.updateCheckpoint(checkpoint)
            .subscribe(etag -> System.out.println(etag), error -> System.out
                .println("PRINTING ERROR " + error + " message " + error.getMessage()));

        TimeUnit.SECONDS.sleep(10);

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
        System.out.println("CLAIMING ownership");
        blobPartitionManager.claimOwnership(pos).subscribe(BlobPartitionManagerSample::printPartitionOwnership,
            System.out::println);
        TimeUnit.SECONDS.sleep(30);
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

