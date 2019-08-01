// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ApiTestBase;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.azure.messaging.eventhubs.TestUtils.MESSAGE_TRACKING_ID;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Integration test that verifies backwards compatibility with a previous version of the SDK.
 */
public class BackCompatTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    private static final String PAYLOAD = "test-message";

    private EventHubAsyncClient client;
    private EventHubProducer producer;
    private EventHubConsumer consumer;

    public BackCompatTest() {
        super(new ClientLogger(BackCompatTest.class));
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

        client = new EventHubAsyncClient(getConnectionOptions(), getReactorProvider(), handlerProvider);
        consumer = client.createConsumer(EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.latest());

        final EventHubProducerOptions producerOptions = new EventHubProducerOptions()
            .partitionId(PARTITION_ID);
        producer = client.createProducer(producerOptions);
    }

    @Override
    protected void afterTest() {
        dispose(consumer, producer, client);
    }

    /**
     * Verifies test work with SDK versions before 0.11.0.
     */
    @Test
    public void backCompatWithJavaSDKOlderThan0110() {
        skipIfNotRecordMode();

        // Arrange
        final String messageTrackingValue = UUID.randomUUID().toString();

        // until version 0.10.0 - we used to have Properties as HashMap<String,String>
        // This specific combination is intended to test the back compat - with the new Properties type as HashMap<String, Object>
        final HashMap<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put("firstProperty", "value1");
        applicationProperties.put("intProperty", "3");

        // We want to ensure that we fetch the event data corresponding to this test and not some other test case.
        applicationProperties.put(MESSAGE_TRACKING_ID, messageTrackingValue);

        final Message message = Proton.message();
        message.setApplicationProperties(new ApplicationProperties(applicationProperties));
        message.setBody(new Data(Binary.create(ByteBuffer.wrap(PAYLOAD.getBytes(UTF_8)))));
        message.setMessageAnnotations(new MessageAnnotations(new HashMap<>()));

        final EventData eventData = new EventData(message);

        // Act & Assert
        StepVerifier.create(consumer.receive().filter(received -> isMatchingEvent(received, messageTrackingValue)).take(1))
            .then(() -> producer.send(eventData).block(TIMEOUT))
            .assertNext(event -> validateAmqpProperties(applicationProperties, event))
            .verifyComplete();
    }

    private void validateAmqpProperties(Map<String, Object> expected, EventData event) {
        Assert.assertEquals(expected.size(), event.properties().size());
        Assert.assertEquals(PAYLOAD, UTF_8.decode(event.body()).toString());

        expected.forEach((key, value) -> {
            Assert.assertTrue(event.properties().containsKey(key));
            Assert.assertEquals(value, event.properties().get(key));
        });
    }
}
