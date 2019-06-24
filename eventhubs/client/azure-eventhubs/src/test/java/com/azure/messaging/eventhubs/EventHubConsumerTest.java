// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.MessageConstant;
import com.azure.core.amqp.Retry;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ApiTestBase;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class EventHubConsumerTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    private static final String PAYLOAD = "TestMessage1";
    private static final String PROPERTY1 = "property1";
    private static final String PROPERTY_VALUE1 = "something1";
    private static final String PROPERTY2 = MessageConstant.MESSAGE_ID.getValue(); // TODO: need to verify it
    private static final String PROPERTY_VALUE2 = "something2";

    private EventHubClient client;
    private EventHubProducer producer;
    private EventHubConsumer consumer;
    private EventData resendEventData;

    public EventHubConsumerTest() {
        super(new ClientLogger(EventHubConsumerTest.class));
    }

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        client = new EventHubClient(getConnectionOptions(), getReactorProvider(), handlerProvider);
        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(PARTITION_ID).retry(Retry.getNoRetry()).timeout(Duration.ofSeconds(30));
        producer = client.createProducer(producerOptions);
        consumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.latest());
    }

    @Override
    protected void afterTest() {
        dispose(consumer, producer, client);
    }

    /**
     * Test for received body and properties of a same event
     */
    @Ignore
    @Test
    public void testReceivedBodyAndProperties() {
        skipIfNotRecordMode();

        // Arrange
        final EventData event = new EventData(PAYLOAD.getBytes());
        event.properties().put(PROPERTY1, PROPERTY_VALUE1);
        event.properties().put(PROPERTY2, PROPERTY_VALUE2);

        // Act & Assert
        StepVerifier.create(consumer.receive().take(1))
            .then(() -> producer.send(event).block())
            .assertNext(data -> {
                validateReceivedEvent(data);
                resendEventData = data;
            })
            .verifyComplete();

        StepVerifier.create(consumer.receive().take(1))
            .then(() -> producer.send(resendEventData).block())
            .assertNext(data -> validateReceivedEvent(data))
            .verifyComplete();
    }

    private void validateReceivedEvent(EventData eventData) {
        Objects.requireNonNull(eventData);
        Assert.assertEquals(PAYLOAD, new String(eventData.body().array()));

        final Map<String, Object> propertiesMap = eventData.properties();
        Assert.assertTrue(propertiesMap.containsKey(PROPERTY1));
        Assert.assertEquals(PROPERTY_VALUE1, propertiesMap.get(PROPERTY1));
        Assert.assertTrue(propertiesMap.containsKey(PROPERTY2));
        Assert.assertEquals(PROPERTY_VALUE2, propertiesMap.get(PROPERTY2));

        eventData.systemProperties();
        Assert.assertNotNull(eventData.offset());
        Assert.assertNotNull(eventData.enqueuedTime());

        Assert.assertTrue(eventData.sequenceNumber() > 0);
        Assert.assertNull(eventData.partitionKey());
    }
}
