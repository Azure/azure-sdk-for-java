// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import static org.junit.Assert.assertNotNull;

import com.azure.messaging.eventhubs.implementation.ClientConstants;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import org.junit.Test;
import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link EventProcessorBuilder}.
 */
public class EventProcessorBuilderTest {

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

    @Test(expected = NullPointerException.class)
    public void testEventProcessorBuilderMissingProperties() {
        EventProcessor eventProcessor = new EventProcessorBuilder()
            .processEvent((eventData, partitionContext, checkpointManager) -> System.out.println(eventData.sequenceNumber()))
            .buildEventProcessor();
    }

    @Test(expected = IllegalStateException.class)
    public void testEventProcessorBuilderMissingBothProcessors() {
        EventHubAsyncClient eventHubAsyncClient = new EventHubClientBuilder()
            .connectionString(CORRECT_CONNECTION_STRING)
            .buildAsyncClient();
        EventProcessor eventProcessor = new EventProcessorBuilder()
            .consumerGroup("test")
            .eventHubClient(eventHubAsyncClient)
            .buildEventProcessor();
    }

    @Test(expected = IllegalStateException.class)
    public void testEventProcessorBuilderWithFactoryAndProcessEvent() {
        EventHubAsyncClient eventHubAsyncClient = new EventHubClientBuilder()
            .connectionString(CORRECT_CONNECTION_STRING)
            .buildAsyncClient();

        EventProcessor eventProcessor = new EventProcessorBuilder()
            .consumerGroup("consumer-group")
            .eventHubClient(eventHubAsyncClient)
            .processEvent((eventData, partitionContext, checkpointManager) -> System.out.println(eventData.sequenceNumber()))
            .partitionProcessorFactory(
                ((partitionContext, checkpointManager) -> new PartitionProcessor(partitionContext, checkpointManager) {
                    @Override
                    public Mono<Void> processEvent(EventData eventData) {
                        return Mono.fromRunnable(() -> System.out.println(eventData.sequenceNumber()));
                    }
                }))
            .buildEventProcessor();
        assertNotNull(eventProcessor);
    }

    @Test
    public void testEventProcessorBuilderWithLambda() {
        EventHubAsyncClient eventHubAsyncClient = new EventHubClientBuilder()
            .connectionString(CORRECT_CONNECTION_STRING)
            .buildAsyncClient();

        EventProcessor eventProcessor = new EventProcessorBuilder()
            .consumerGroup("consumer-group")
            .eventHubClient(eventHubAsyncClient)
            .processEvent((eventData, partitionContext, checkpointManager) -> System.out.println(eventData.sequenceNumber()))
            .buildEventProcessor();
        assertNotNull(eventProcessor);
    }

    @Test
    public void testEventProcessorBuilderWithFactory() {
        EventHubAsyncClient eventHubAsyncClient = new EventHubClientBuilder()
            .connectionString(CORRECT_CONNECTION_STRING)
            .buildAsyncClient();

        EventProcessor eventProcessor = new EventProcessorBuilder()
            .consumerGroup("consumer-group")
            .eventHubClient(eventHubAsyncClient)
            .partitionProcessorFactory(
                ((partitionContext, checkpointManager) -> new PartitionProcessor(partitionContext, checkpointManager) {
                    @Override
                    public Mono<Void> processEvent(EventData eventData) {
                        return Mono.fromRunnable(() -> System.out.println(eventData.sequenceNumber()));
                    }
                }))
            .buildEventProcessor();
        assertNotNull(eventProcessor);
    }

}
