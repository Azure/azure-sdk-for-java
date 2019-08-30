// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import reactor.core.publisher.Mono;

/**
 * Code snippets for {@link PartitionProcessorFactory}.
 */
public class PartitionProcessorFactoryJavaDocCodeSamples {

    /**
     * Code snippet for creating an instance of {@link PartitionProcessorFactory}.
     */
    public void partitionProcessorFactory() {

        // BEGIN: com.azure.messaging.eventhubs.partitionprocessorfactory.instantiation
        PartitionProcessorFactory partitionProcessorFactory = (partitionContext, checkpointManager) -> {
            return new PartitionProcessor(partitionContext, checkpointManager) {
                @Override
                public Mono<Void> processEvent(EventData eventData) {
                    System.out.println("Processing event with sequence number " + eventData.sequenceNumber());
                    return checkpointManager().updateCheckpoint(eventData);
                }
            };
        };
        // END: com.azure.messaging.eventhubs.partitionprocessorfactory.instantiation

    }

}
