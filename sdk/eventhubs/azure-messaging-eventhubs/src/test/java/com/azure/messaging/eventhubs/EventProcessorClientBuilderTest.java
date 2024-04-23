// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.Configuration;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link EventProcessorClientBuilder}.
 */
public class EventProcessorClientBuilderTest {

    private static final String NAMESPACE_NAME = "dummyNamespaceName";
    private static final String DEFAULT_DOMAIN_NAME = Configuration.getGlobalConfiguration()
        .get("AZURE_EVENTHUBS_ENDPOINT_SUFFIX", ".servicebus.windows.net").substring(1).substring(1) + "/";

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

    @Test
    public void initialOffsetProviderSetMoreThanOne() {
        Map<String, EventPosition> eventPositionMap = new HashMap<>();
        Function<String, EventPosition> eventPositionFunction = id -> EventPosition.fromOffset(20L);
        EventProcessorClientBuilder builder = new EventProcessorClientBuilder()
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
            .checkpointStore(new SampleCheckpointStore());

        if (eventPositionMap != null) {
            builder.initialPartitionEventPosition(eventPositionMap);
        }
        if (eventPositionFunction != null) {
            builder.initialPartitionEventPosition(eventPositionFunction);
        }

        assertThrows(IllegalArgumentException.class, () -> builder.buildEventProcessorClient());
    }

    /**
     * Tests that the correct EventPosition is returned when
     * {@link EventProcessorClientBuilder#initialPartitionEventPosition(Function)} is used.
     */
    @Test
    public void initialEventPositionProvider() {
        // Arrange
        String partitionId = "1";
        EventPosition expected = EventPosition.fromOffset(222L);
        Function<String, EventPosition> eventPositionFunction = id -> id.equals(partitionId)
            ? expected
            : EventPosition.earliest();

        // Act
        EventProcessorClient client = new EventProcessorClientBuilder()
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
            .initialPartitionEventPosition(eventPositionFunction)
            .buildEventProcessorClient();

        // Assert
        EventProcessorClientOptions options = client.getEventProcessorClientOptions();
        Function<String, EventPosition> function = options.getInitialEventPositionProvider();

        assertNotNull(function, "'initialEventPositionProvider' should not be null.");

        EventPosition actual = function.apply(partitionId);
        assertEquals(expected, actual);
    }

    /**
     * Tests that the correct EventPosition is returned when
     * {@link EventProcessorClientBuilder#initialPartitionEventPosition(Map)} is used.
     */
    @Test
    public void initialEventPositionMap() {
        // Arrange
        String partitionId = "1";
        EventPosition expected = EventPosition.fromOffset(222L);
        Map<String, EventPosition> eventPositionMap = new HashMap<>();
        eventPositionMap.put(partitionId, expected);

        // Act
        EventProcessorClient client = new EventProcessorClientBuilder()
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
            .initialPartitionEventPosition(eventPositionMap)
            .buildEventProcessorClient();

        // Assert
        EventProcessorClientOptions options = client.getEventProcessorClientOptions();
        Function<String, EventPosition> function = options.getInitialEventPositionProvider();

        assertNotNull(function, "'initialEventPositionProvider' should not be null.");

        EventPosition actual = function.apply(partitionId);
        assertEquals(expected, actual);

        // Should not exist in the map.
        assertNull(function.apply("non-existent-id"));
    }
}
