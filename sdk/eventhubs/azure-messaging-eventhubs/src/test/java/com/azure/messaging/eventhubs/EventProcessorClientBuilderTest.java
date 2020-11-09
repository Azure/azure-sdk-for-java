// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.util.Configuration;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link EventProcessorClientBuilder}.
 */
public class EventProcessorClientBuilderTest {

    private EventProcessorClientBuilder eventProcessorClientBuilder;

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
                    System.out.printf("Error occurred in partition processor for partition {}, {}",
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
                System.out.printf("Error occurred in partition processor for partition %s, %s",
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
                    System.out.printf("Error occurred in partition processor for partition {}, {}",
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
                    System.out.printf("Error occurred in partition processor for partition {}, {}",
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
                System.out.printf("Error occurred in partition processor for partition %s, %s",
                    errorContext.getPartitionContext().getPartitionId(),
                    errorContext.getThrowable());
            })
            .checkpointStore(new SampleCheckpointStore())
            .buildEventProcessorClient();

        assertNotNull(eventProcessorClient);
    }

    @Test
    public void connectionStringTest() {
        eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .connectionString(CORRECT_CONNECTION_STRING, EVENT_HUB_NAME);
        Assertions.assertNotNull(eventProcessorClientBuilder);
    }

    @Test
    public void configurationTest() {
        eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .configuration(Configuration.getGlobalConfiguration());
        Assertions.assertNotNull(eventProcessorClientBuilder);
    }

    @Test
    public void proxyOptionTest() {
        eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .proxyOptions(ProxyOptions.SYSTEM_DEFAULTS);
        Assertions.assertNotNull(eventProcessorClientBuilder);
    }

    @Test
    public void transportTypeTest() {
        eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .transportType(AmqpTransportType.AMQP);
        Assertions.assertNotNull(eventProcessorClientBuilder);
    }

    @Test
    public void retryTest() {

        int maxRetries = 10;
        Duration maxDelay = Duration.ofSeconds(120);
        Duration delay = Duration.ofSeconds(20);
        AmqpRetryOptions options = new AmqpRetryOptions()
            .setMaxRetries(maxRetries)
            .setMaxDelay(maxDelay)
            .setDelay(delay);

        eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .retry(options);
        Assertions.assertNotNull(eventProcessorClientBuilder);
    }

    @Test
    public void loadBalancingUpdateIntervalTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            eventProcessorClientBuilder = new EventProcessorClientBuilder()
                .loadBalancingUpdateInterval(Duration.ofSeconds(0));
            Assertions.assertNotNull(eventProcessorClientBuilder);
        });
    }

    @Test
    public void partitionOwnershipExpirationIntervalTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            eventProcessorClientBuilder = new EventProcessorClientBuilder()
                .partitionOwnershipExpirationInterval(Duration.ZERO);
            Assertions.assertNotNull(eventProcessorClientBuilder);
        });
    }

    @Test
    public void loadBalancingStrategyTest() {
        eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .loadBalancingStrategy(LoadBalancingStrategy.BALANCED);
        Assertions.assertNotNull(eventProcessorClientBuilder);
    }

    @Test
    public void processEventBatchTest() {
        eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .processEventBatch(EventBatchContext::updateCheckpoint, 1);
        Assertions.assertNotNull(eventProcessorClientBuilder);
    }

    @Test
    public void processEventBatchSecondTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            eventProcessorClientBuilder = new EventProcessorClientBuilder()
                .processEventBatch(EventBatchContext::updateCheckpoint, 0, null);
            Assertions.assertNotNull(eventProcessorClientBuilder);
        });

    }

    @Test
    public void processPartitionInitializationTest() {
        Consumer<InitializationContext> initializePartition = new Consumer<InitializationContext>() {
            @Override
            public void accept(InitializationContext initializationContext) {

            }
        };
        eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .processPartitionInitialization(initializePartition);
        Assertions.assertNotNull(eventProcessorClientBuilder);
    }

    @Test
    public void processPartitionCloseTest() {
        Consumer<CloseContext> closePartition = new Consumer<CloseContext>() {
            @Override
            public void accept(CloseContext closeContext) {

            }
        };
        eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .processPartitionClose(closePartition);
        Assertions.assertNotNull(eventProcessorClientBuilder);
    }

    @Test
    public void trackLastEnqueuedEventPropertiesTest() {
        eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .trackLastEnqueuedEventProperties(false);
        Assertions.assertNotNull(eventProcessorClientBuilder);
    }

    @Test
    public void initialPartitionEventPositionTest() {

        Map<String, EventPosition> eventMap = new HashMap<>();
        eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .initialPartitionEventPosition(eventMap);
        Assertions.assertNotNull(eventProcessorClientBuilder);
    }
}
