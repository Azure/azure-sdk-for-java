// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.MessageConstant;
import com.azure.core.amqp.RetryOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ApiTestBase;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
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
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.messaging.eventhubs.TestUtils.MESSAGE_TRACKING_ID;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;
import static java.nio.charset.StandardCharsets.UTF_8;

public class InteropAmqpPropertiesTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    private static final String PAYLOAD = "test-message";

    private EventHubAsyncClient client;
    private EventHubProducer producer;
    private EventHubConsumer consumer;

    @Rule
    public TestName testName = new TestName();

    public InteropAmqpPropertiesTest() {
        super(new ClientLogger(InteropAmqpPropertiesTest.class));
    }

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        client = new EventHubAsyncClient(getConnectionOptions(), getReactorProvider(), handlerProvider);

        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(PARTITION_ID)
            .retry(new RetryOptions().tryTimeout(Duration.ofSeconds(30)));
        producer = client.createProducer(producerOptions);
        consumer = client.createConsumer(EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.latest());
    }

    @Override
    protected void afterTest() {
        dispose(producer, consumer, client);
    }

    /**
     * Test for interoperable with Direct Proton AMQP messaging
     */
    @Test
    public void interoperableWithDirectProtonAmqpMessage() {
        skipIfNotRecordMode();

        // Arrange
        final AtomicReference<EventData> receivedEventData = new AtomicReference<>();
        final String messageTrackingValue = UUID.randomUUID().toString();

        final HashMap<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(MESSAGE_TRACKING_ID, messageTrackingValue);
        applicationProperties.put("first-property", "value-1");

        final Message message = Proton.message();
        message.setApplicationProperties(new ApplicationProperties(applicationProperties));

        message.setMessageId("id1");
        message.setUserId("user1".getBytes());
        message.setAddress("event-hub-address");
        message.setSubject("sub");
        message.setReplyTo("replyingTo");
        message.setExpiryTime(456L);
        message.setGroupSequence(5555L);
        message.setContentType("events");
        message.setContentEncoding("UTF-8");
        message.setCorrelationId("correlation-id-1");
        message.setCreationTime(345L);
        message.setGroupId("group-id");
        message.setReplyToGroupId("replyToGroupId");

        final Map<Symbol, Object> messageAnnotations = new HashMap<>();
        messageAnnotations.put(Symbol.getSymbol("message-annotation-1"), "messageAnnotationValue");
        message.setMessageAnnotations(new MessageAnnotations(messageAnnotations));

        message.setBody(new Data(Binary.create(ByteBuffer.wrap(PAYLOAD.getBytes()))));
        final EventData msgEvent = new EventData(message);

        // Act & Assert
        // We're setting a tracking identifier because we don't want to receive some random operations. We want to
        // receive the event we sent.
        StepVerifier.create(consumer.receive().filter(event -> isMatchingEvent(event, messageTrackingValue)).take(1))
            .then(() -> producer.send(msgEvent).block(TIMEOUT))
            .assertNext(event -> {
                validateAmqpProperties(message, messageAnnotations, applicationProperties, event);
                receivedEventData.set(event);
            })
            .verifyComplete();

        Assert.assertNotNull(receivedEventData.get());

        StepVerifier.create(consumer.receive().filter(event -> isMatchingEvent(event, messageTrackingValue)).take(1))
            .then(() -> producer.send(receivedEventData.get()).block(TIMEOUT))
            .assertNext(event -> validateAmqpProperties(message, messageAnnotations, applicationProperties, event))
            .verifyComplete();
    }

    private void validateAmqpProperties(Message message, Map<Symbol, Object> messageAnnotations,
                                        Map<String, Object> applicationProperties, EventData actual) {
        Assert.assertTrue(actual.systemProperties().containsKey(MessageConstant.MESSAGE_ID.getValue()));
        Assert.assertEquals(message.getMessageId(), actual.systemProperties().get(MessageConstant.MESSAGE_ID.getValue()));

        Assert.assertTrue(actual.systemProperties().containsKey(MessageConstant.USER_ID.getValue()));
        Assert.assertEquals(new String(message.getUserId()), new String((byte[]) actual.systemProperties().get(MessageConstant.USER_ID.getValue())));

        Assert.assertTrue(actual.systemProperties().containsKey(MessageConstant.TO.getValue()));
        Assert.assertEquals(message.getAddress(), actual.systemProperties().get(MessageConstant.TO.getValue()));

        Assert.assertTrue(actual.systemProperties().containsKey(MessageConstant.CONTENT_TYPE.getValue()));
        Assert.assertEquals(message.getContentType(), actual.systemProperties().get(MessageConstant.CONTENT_TYPE.getValue()));

        Assert.assertTrue(actual.systemProperties().containsKey(MessageConstant.CONTENT_ENCODING.getValue()));
        Assert.assertEquals(message.getContentEncoding(), actual.systemProperties().get(MessageConstant.CONTENT_ENCODING.getValue()));

        Assert.assertTrue(actual.systemProperties().containsKey(MessageConstant.CORRELATION_ID.getValue()));
        Assert.assertEquals(message.getCorrelationId(), actual.systemProperties().get(MessageConstant.CORRELATION_ID.getValue()));

        Assert.assertTrue(actual.systemProperties().containsKey(MessageConstant.CREATION_TIME.getValue()));
        Assert.assertEquals(message.getCreationTime(), actual.systemProperties().get(MessageConstant.CREATION_TIME.getValue()));

        Assert.assertTrue(actual.systemProperties().containsKey(MessageConstant.SUBJECT.getValue()));
        Assert.assertEquals(message.getSubject(), actual.systemProperties().get(MessageConstant.SUBJECT.getValue()));

        Assert.assertTrue(actual.systemProperties().containsKey(MessageConstant.GROUP_ID.getValue()));
        Assert.assertEquals(message.getGroupId(), actual.systemProperties().get(MessageConstant.GROUP_ID.getValue()));

        Assert.assertTrue(actual.systemProperties().containsKey(MessageConstant.REPLY_TO_GROUP_ID.getValue()));
        Assert.assertEquals(message.getReplyToGroupId(), actual.systemProperties().get(MessageConstant.REPLY_TO_GROUP_ID.getValue()));

        Assert.assertTrue(actual.systemProperties().containsKey(MessageConstant.REPLY_TO.getValue()));
        Assert.assertEquals(message.getReplyTo(), actual.systemProperties().get(MessageConstant.REPLY_TO.getValue()));

        Assert.assertTrue(actual.systemProperties().containsKey(MessageConstant.ABSOLUTE_EXPIRY_TIME.getValue()));
        Assert.assertEquals(message.getExpiryTime(), actual.systemProperties().get(MessageConstant.ABSOLUTE_EXPIRY_TIME.getValue()));

        Assert.assertEquals(PAYLOAD, UTF_8.decode(actual.body()).toString());

        messageAnnotations.forEach((key, value) -> {
            Assert.assertTrue(actual.systemProperties().containsKey(key.toString()));
            Assert.assertEquals(value, actual.systemProperties().get(key.toString()));
        });

        Assert.assertEquals(applicationProperties.size(), actual.properties().size());
        applicationProperties.forEach((key, value) -> {
            Assert.assertTrue(actual.properties().containsKey(key));
            Assert.assertEquals(value, actual.properties().get(key));
        });

    }
}
