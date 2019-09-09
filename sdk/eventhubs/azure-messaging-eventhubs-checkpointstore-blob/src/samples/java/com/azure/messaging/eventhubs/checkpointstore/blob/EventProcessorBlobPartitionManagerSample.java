// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessor;
import com.azure.messaging.eventhubs.EventProcessorBuilder;
import com.azure.messaging.eventhubs.PartitionProcessor;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.storage.blob.ContainerAsyncClient;
import com.azure.storage.blob.ContainerClientBuilder;
import com.azure.storage.common.credentials.SASTokenCredential;
import java.util.concurrent.TimeUnit;
import reactor.core.publisher.Mono;

/**
 * Sample for using {@link BlobPartitionManager} with {@link EventProcessor}.
 */
public class EventProcessorBlobPartitionManagerSample {

    private static final String EH_CONNECTION_STRING = "";
    private static final String SAS_TOKEN_STRING = "";
    private static final String STORAGE_CONNECTION_STRING = "";
    public static final PartitionProcessor PARTITION_PROCESSOR = new PartitionProcessor() {

        @Override
        public Mono<Void> processEvent(
            PartitionContext partitionContext, EventData eventData) {
            System.out.printf("Processing event from partition %s with sequence number %d %n",
                partitionContext.partitionId(), eventData.sequenceNumber());

            if (eventData.sequenceNumber() % 10 == 0) {
                return partitionContext.updateCheckpoint(eventData);
            }
            return Mono.empty();
        }
    };

    /**
     * The main method to run the sample.
     *
     * @param args Unused arguments to the sample.
     * @throws Exception if there are any errors while running the sample program.
     */
    public static void main(String[] args) throws Exception {
        EventHubAsyncClient eventHubAsyncClient = new EventHubClientBuilder()
            .connectionString(EH_CONNECTION_STRING)
            .buildAsyncClient();

        SASTokenCredential sasTokenCredential = SASTokenCredential.fromSASTokenString(SAS_TOKEN_STRING);
        ContainerAsyncClient containerAsyncClient = new ContainerClientBuilder()
            .connectionString(STORAGE_CONNECTION_STRING)
            .containerName("")
            .credential(sasTokenCredential)
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .buildAsyncClient();

        EventProcessorBuilder eventProcessorBuilder = new EventProcessorBuilder()
            .consumerGroup("")
            .eventHubClient(eventHubAsyncClient)
            .partitionProcessorFactory(() -> PARTITION_PROCESSOR)
            .partitionManager(new BlobPartitionManager(containerAsyncClient));

        EventProcessor ep1 = eventProcessorBuilder.buildEventProcessor();
        EventProcessor ep2 = eventProcessorBuilder.buildEventProcessor();
        ep1.start();
        ep2.start();
        TimeUnit.MINUTES.sleep(5);
        ep1.stop();
        ep2.stop();
    }

}
