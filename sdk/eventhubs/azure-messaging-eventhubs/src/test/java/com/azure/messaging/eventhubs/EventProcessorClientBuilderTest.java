// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.implementation.ClientConstants;
import java.time.Duration;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link EventProcessorClientBuilder}.
 */
public class EventProcessorClientBuilderTest {

    private static final String NAMESPACE_NAME = "dummyNamespaceName";
    private static final String DEFAULT_DOMAIN_NAME = "servicebus.windows.net/";

    private static final String EVENT_HUB_NAME = "eventHubName";
    private static final String SHARED_ACCESS_KEY_NAME = "dummySasKeyName";
    private static final String SHARED_ACCESS_KEY = "dummySasKey";
    private static final String ENDPOINT = getURI(ClientConstants.ENDPOINT_FORMAT, NAMESPACE_NAME, DEFAULT_DOMAIN_NAME)
        .toString();

    private static final String CORRECT_CONNECTION_STRING = String
        .format("Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s;EntityPath=%s",
            ENDPOINT, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY, EVENT_HUB_NAME);

    private static URI getURI(String endpointFormat, String namespace, String domainName) {
        try {
            return new URI(String.format(Locale.US, endpointFormat, namespace, domainName));
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "Invalid namespace name: %s", namespace), exception);
        }
    }

    @Test
    public void testEventProcessorBuilderMissingProperties() {
        assertThrows(NullPointerException.class, () -> {
            EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
                .checkpointStore(new SampleCheckpointStore())
                .processEvent(eventContext -> {
                    System.out.println("Partition id = " + eventContext.getPartitionContext().getPartitionId() + " and "
                        + "sequence number of event = " + eventContext.getEventData().getSequenceNumber());
                })
                .processError(errorContext -> {
                    System.out.printf("Error occurred in partition processor for partition %s, %s%n",
                        errorContext.getPartitionContext().getPartitionId(),
                        errorContext.getThrowable());
                })
                .buildEventProcessorClient();
        });
    }

    @Test
    public void testEventProcessorBuilderWithProcessEvent() {
        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .connectionString(CORRECT_CONNECTION_STRING)
            .consumerGroup("consumer-group")
            .processEvent(eventContext -> {
                System.out.println("Partition id = " + eventContext.getPartitionContext().getPartitionId() + " and "
                    + "sequence number of event = " + eventContext.getEventData().getSequenceNumber());
            })
            .processError(errorContext -> {
                System.out.printf("Error occurred in partition processor for partition %s, %s%n",
                    errorContext.getPartitionContext().getPartitionId(),
                    errorContext.getThrowable());
            })
            .checkpointStore(new SampleCheckpointStore())
            .buildEventProcessorClient();

        assertNotNull(eventProcessorClient);
    }

    @Test
    public void testEventProcessorBuilderWithBothSingleAndBatchConsumers() {
        assertThrows(IllegalArgumentException.class, () -> {
            EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
                .checkpointStore(new SampleCheckpointStore())
                .consumerGroup("consumer-group")
                .processEvent(eventContext -> {
                    System.out.println("Partition id = " + eventContext.getPartitionContext().getPartitionId() + " and "
                        + "sequence number of event = " + eventContext.getEventData().getSequenceNumber());
                })
                .processEventBatch(eventBatchContext -> {
                    eventBatchContext.getEvents().forEach(event -> {
                        System.out
                            .println(
                                "Partition id = " + eventBatchContext.getPartitionContext().getPartitionId() + " and "
                                    + "sequence number of event = " + event.getSequenceNumber());
                    });
                }, 5, Duration.ofSeconds(1))
                .processError(errorContext -> {
                    System.out.printf("Error occurred in partition processor for partition %s, %s%n",
                        errorContext.getPartitionContext().getPartitionId(),
                        errorContext.getThrowable());
                })
                .buildEventProcessorClient();
        });
    }

    @Test
    public void testEventProcessorBuilderWithNoProcessEventConsumer() {
        assertThrows(IllegalArgumentException.class, () -> {
            EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
                .checkpointStore(new SampleCheckpointStore())
                .consumerGroup("consumer-group")
                .processError(errorContext -> {
                    System.out.printf("Error occurred in partition processor for partition %s, %s%n",
                        errorContext.getPartitionContext().getPartitionId(),
                        errorContext.getThrowable());
                })
                .buildEventProcessorClient();
        });
    }

    @Test
    public void testEventProcessorBuilderWithProcessEventBatch() {
        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .connectionString(CORRECT_CONNECTION_STRING)
            .consumerGroup("consumer-group")
            .processEventBatch(eventBatchContext -> {
                eventBatchContext.getEvents().forEach(event -> {
                    System.out
                        .println("Partition id = " + eventBatchContext.getPartitionContext().getPartitionId() + " and "
                            + "sequence number of event = " + event.getSequenceNumber());
                });
            }, 5, Duration.ofSeconds(1))
            .processError(errorContext -> {
                System.out.printf("Error occurred in partition processor for partition %s, %s%n",
                    errorContext.getPartitionContext().getPartitionId(),
                    errorContext.getThrowable());
            })
            .checkpointStore(new SampleCheckpointStore())
            .buildEventProcessorClient();

        assertNotNull(eventProcessorClient);
    }

}
