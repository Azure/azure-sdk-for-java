// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpClientOptions;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.InitializationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link EventProcessorClientBuilder}.
 */
public class EventProcessorClientBuilderTest {
    private static final ClientLogger LOGGER = new ClientLogger(EventProcessorClientBuilderTest.class);

    private static final String NAMESPACE_NAME = "dummyNamespaceName";
    private static final String DEFAULT_DOMAIN_NAME = Configuration.getGlobalConfiguration()
        .get("AZURE_EVENTHUBS_ENDPOINT_SUFFIX", ".servicebus.windows.net");
    private static final String EVENT_HUB_NAME = "eventHubName-demo";
    private static final String SHARED_ACCESS_KEY_NAME = "dummySasKeyName";
    private static final String SHARED_ACCESS_KEY = "dummySasKey";
    private static final String ENDPOINT = getURI().toString();

    private static final String CORRECT_CONNECTION_STRING = String
        .format("Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s;EntityPath=%s",
            ENDPOINT, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY, EVENT_HUB_NAME);
    private static final String CORRECT_NAMESPACE_CONNECTION_STRING = String
        .format("Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s",
            ENDPOINT, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY);

    private static URI getURI() {
        try {
            final String suffix = DEFAULT_DOMAIN_NAME.substring(1);
            final String urlToTry = String.format(Locale.US, ClientConstants.ENDPOINT_FORMAT, NAMESPACE_NAME, suffix);
            return new URI(urlToTry);
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "Invalid namespace name: %s", NAMESPACE_NAME), exception);
        }
    }

    @Test
    public void testEventProcessorBuilderMissingProperties() {
        assertThrows(NullPointerException.class, () -> {
            EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
                .checkpointStore(new SampleCheckpointStore())
                .processEvent(eventContext -> {
                    LOGGER.log(LogLevel.VERBOSE, () -> "Partition id = "
                        + eventContext.getPartitionContext().getPartitionId() + " and sequence number of event = "
                        + eventContext.getEventData().getSequenceNumber());
                })
                .processError(errorContext -> {
                    LOGGER.log(LogLevel.VERBOSE, () -> String.format(
                        "Error occurred in partition processor for partition %s, %s%n",
                        errorContext.getPartitionContext().getPartitionId(), errorContext.getThrowable()));
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
                LOGGER.log(LogLevel.VERBOSE, () -> "Partition id = "
                    + eventContext.getPartitionContext().getPartitionId() + " and sequence number of event = "
                    + eventContext.getEventData().getSequenceNumber());
            })
            .processError(errorContext -> {
                LOGGER.log(LogLevel.VERBOSE, () -> String.format(
                    "Error occurred in partition processor for partition %s, %s%n",
                    errorContext.getPartitionContext().getPartitionId(), errorContext.getThrowable()));
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
                    LOGGER.log(LogLevel.VERBOSE, () -> ("Partition id = "
                        + eventContext.getPartitionContext().getPartitionId() + " and sequence number of event = "
                        + eventContext.getEventData().getSequenceNumber()));
                })
                .processEventBatch(eventBatchContext -> {
                    eventBatchContext.getEvents().forEach(event -> {
                        LOGGER.log(LogLevel.VERBOSE, () -> "Partition id = "
                            + eventBatchContext.getPartitionContext().getPartitionId()
                            + " and sequence number of event = " + event.getSequenceNumber());
                    });
                }, 5, Duration.ofSeconds(1))
                .processError(errorContext -> {
                    LOGGER.log(LogLevel.VERBOSE, () -> String.format(
                        "Error occurred in partition processor for partition %s, %s%n",
                        errorContext.getPartitionContext().getPartitionId(), errorContext.getThrowable()));
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
                    LOGGER.log(LogLevel.VERBOSE, () -> String.format(
                        "Error occurred in partition processor for partition %s, %s%n",
                        errorContext.getPartitionContext().getPartitionId(), errorContext.getThrowable()));
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
                    LOGGER.log(LogLevel.VERBOSE, () -> ("Partition id = "
                        + eventBatchContext.getPartitionContext().getPartitionId() + " and sequence number of event = "
                        + event.getSequenceNumber()));
                });
            }, 5, Duration.ofSeconds(1))
            .processError(errorContext -> {
                LOGGER.log(LogLevel.VERBOSE, () -> String.format(
                    "Error occurred in partition processor for partition %s, %s%n",
                    errorContext.getPartitionContext().getPartitionId(), errorContext.getThrowable()));
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
                    LOGGER.log(LogLevel.VERBOSE, () -> "Partition id = "
                        + eventBatchContext.getPartitionContext().getPartitionId() + " and sequence number of event = "
                        + event.getSequenceNumber());
                });
            }, 5, Duration.ofSeconds(1))
            .processError(errorContext -> {
                LOGGER.log(LogLevel.VERBOSE, () -> String.format(
                    "Error occurred in partition processor for partition %s, %s%n",
                    errorContext.getPartitionContext().getPartitionId(), errorContext.getThrowable()));
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
                    LOGGER.log(LogLevel.VERBOSE, () -> "Partition id = "
                        + eventBatchContext.getPartitionContext().getPartitionId() + " and sequence number of event = "
                        + event.getSequenceNumber());
                });
            }, 5, Duration.ofSeconds(1))
            .processError(errorContext -> {
                LOGGER.log(LogLevel.VERBOSE, () -> String.format(
                    "Error occurred in partition processor for partition %s, %s%n",
                    errorContext.getPartitionContext().getPartitionId(), errorContext.getThrowable()));
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
                    LOGGER.log(LogLevel.VERBOSE, () -> "Partition id = "
                        + eventBatchContext.getPartitionContext().getPartitionId() + " and sequence number of event = "
                        + event.getSequenceNumber());
                });
            }, 5, Duration.ofSeconds(1))
            .processError(errorContext -> {
                LOGGER.log(LogLevel.VERBOSE, () -> String.format(
                    "Error occurred in partition processor for partition %s, %s%n",
                    errorContext.getPartitionContext().getPartitionId(), errorContext.getThrowable()));
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

    /**
     * Check that the information is copied from source builder to second instance of builder.
     */
    @Test
    public void createSecondProcessor() {
        // Arrange
        final Consumer<CloseContext> closeContextConsumer = close -> {};
        final Consumer<InitializationContext> initializationContextConsumer = initial -> {};
        final Consumer<ErrorContext> errorContextConsumer = errorContext -> {};
        final Consumer<EventContext> eventContextConsumer = eventContext -> {};
        final String identifier1 = "test-processor-identifier";
        final ClientOptions clientOptions = new AmqpClientOptions()
            .setIdentifier(identifier1)
            .setTracingOptions(TracingOptions.fromConfiguration(Configuration.NONE));
        final String consumerGroup = "consumer-group-1";
        final int prefetch = 103;
        final String customEndpoint = "https://my.redirected.endpoint.com";
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions()
            .setTryTimeout(Duration.ofSeconds(10))
            .setMaxRetries(1);
        final String expectedFullyQualifiedNamespace = (NAMESPACE_NAME + DEFAULT_DOMAIN_NAME).toLowerCase(Locale.ROOT);
        final String expectedEventHubName = EVENT_HUB_NAME.toLowerCase(Locale.ROOT);

        final EventProcessorClientBuilder builder = new EventProcessorClientBuilder()
            .clientOptions(clientOptions)
            .connectionString(CORRECT_CONNECTION_STRING)
            .consumerGroup(consumerGroup)
            .customEndpointAddress(customEndpoint)
            .prefetchCount(prefetch)
            .processError(errorContextConsumer)
            .processEvent(eventContextConsumer)
            .processPartitionClose(closeContextConsumer)
            .processPartitionInitialization(initializationContextConsumer)
            .retryOptions(retryOptions)
            .checkpointStore(new SampleCheckpointStore());

        // Act
        final EventProcessorClient processor1 = builder.buildEventProcessorClient();

        // Assert
        final EventProcessorClientOptions processor1Options = processor1.getEventProcessorClientOptions();

        Assertions.assertEquals(consumerGroup, processor1Options.getConsumerGroup());
        Assertions.assertEquals(consumerGroup, processor1.getConsumerGroup());

        Assertions.assertEquals(identifier1, processor1.getIdentifier());

        Assertions.assertEquals(expectedEventHubName, processor1.getEventHubName());
        Assertions.assertEquals(expectedFullyQualifiedNamespace, processor1.getFullyQualifiedNamespace());

        // Arrange
        final String consumerGroup2 = "consumer-group-2";
        final String identifier2 = "test-processor-identifier-1";
        final ClientOptions clientOptions2 = new AmqpClientOptions().setIdentifier(identifier2);
        final AmqpRetryOptions retryOptions2 = new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(5));

        // Act

        // Update some options like the consumer group and create another EventProcessorClient
        builder.clientOptions(clientOptions2)
            .consumerGroup(consumerGroup2)
            .retryOptions(retryOptions2);

        final EventProcessorClient processor2 = builder.buildEventProcessorClient();

        // Assert
        final EventProcessorClientOptions processor2Options = processor2.getEventProcessorClientOptions();

        Assertions.assertEquals(consumerGroup2, processor2Options.getConsumerGroup());
        Assertions.assertEquals(consumerGroup2, processor2.getConsumerGroup());

        Assertions.assertEquals(identifier2, processor2.getIdentifier());

        Assertions.assertEquals(expectedEventHubName, processor2.getEventHubName());
        Assertions.assertEquals(expectedFullyQualifiedNamespace, processor2.getFullyQualifiedNamespace());
    }
}
