// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.Configuration;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import java.time.Duration;

import com.azure.messaging.eventhubs.models.EventPosition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    public static Stream<Arguments> initialOffsetProviderSetMoreThanOne() {
        EventPosition defaultEventPosition = EventPosition.fromOffset(10L);
        Map<String, EventPosition> eventPositionMap = new HashMap<>();
        Function<String, EventPosition> eventPositionFunction = id -> EventPosition.fromOffset(20L);

        return Stream.of(
            Arguments.of(defaultEventPosition, eventPositionMap, eventPositionFunction),
            Arguments.of(defaultEventPosition, eventPositionMap, null),
            Arguments.of(defaultEventPosition, null, eventPositionFunction),
            Arguments.of(null, eventPositionMap, eventPositionFunction),
            Arguments.of(null, null, eventPositionFunction));
    }

    @MethodSource
    @ParameterizedTest
    public void initialOffsetProviderSetMoreThanOne(EventPosition defaultEventPosition,
        Map<String, EventPosition> eventPositionMap, Function<String, EventPosition> eventPositionFunction) {

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

        if (defaultEventPosition != null) {
            builder.initialPartitionEventPosition(defaultEventPosition);
        }
        if (eventPositionMap != null) {
            builder.initialPartitionEventPosition(eventPositionMap);
        }
        if (eventPositionFunction != null) {
            builder.initialPartitionEventPosition(eventPositionFunction);
        }

        assertThrows(IllegalArgumentException.class, () -> {
            builder.buildEventProcessorClient();
        });
    }
}
