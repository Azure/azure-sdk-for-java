// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.messaging.eventhubs.EventProcessor;
import com.azure.messaging.eventhubs.EventProcessorBuilder;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import reactor.core.publisher.Mono;

/**
 * Sample for using {@link BlobEventProcessorStore} with {@link EventProcessor}.
 */
public class EventProcessorBlobEventProcessorStoreSample {

    private static final String EH_CONNECTION_STRING = "";
    private static final String SAS_TOKEN_STRING = "";
    private static final String STORAGE_CONNECTION_STRING = "";

    public static final Function<PartitionEvent, Mono<Void>> PARTITION_PROCESSOR = partitionEvent -> {
        System.out.printf("Processing event from partition %s with sequence number %d %n",
            partitionEvent.getPartitionContext().getPartitionId(), partitionEvent.getEventData().getSequenceNumber());

        if (partitionEvent.getEventData().getSequenceNumber() % 10 == 0) {
            return partitionEvent.getPartitionContext().updateCheckpoint(partitionEvent.getEventData());
        }
        return Mono.empty();
    };

    /**
     * The main method to run the sample.
     *
     * @param args Unused arguments to the sample.
     * @throws Exception if there are any errors while running the sample program.
     */
    public static void main(String[] args) throws Exception {
        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
            .connectionString(STORAGE_CONNECTION_STRING)
            .containerName("<< CONTAINER NAME >>")
            .sasToken(SAS_TOKEN_STRING)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildAsyncClient();

        EventProcessorBuilder eventProcessorBuilder = new EventProcessorBuilder()
            .connectionString(EH_CONNECTION_STRING)
            .consumerGroup("<< CONSUMER GROUP NAME >>")
            .processEvent(PARTITION_PROCESSOR)
            .eventProcessorStore(new BlobEventProcessorStore(blobContainerAsyncClient));

        EventProcessor eventProcessor = eventProcessorBuilder.buildEventProcessor();
        eventProcessor.start();
        TimeUnit.MINUTES.sleep(5);
        eventProcessor.stop();
    }

}
