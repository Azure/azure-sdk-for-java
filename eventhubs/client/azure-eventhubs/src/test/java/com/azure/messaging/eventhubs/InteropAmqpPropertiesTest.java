// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.MessageConstant;
import com.azure.core.amqp.Retry;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ApiTestBase;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

public class InteropAmqpPropertiesTest extends ApiTestBase {
    private final ClientLogger logger = new ClientLogger(InteropAmqpPropertiesTest.class);

    private static final String PARTITION_ID = "0";
    private static final String APPLICATION_PROPERTY = "firstProp";
    private static final String MESSAGE_ANNOTATION = "message-annotation-1";
    private static final String PAYLOAD = "testmsg";

    private EventHubClient client;
    private EventHubProducer producer;
    private EventHubConsumer consumer;
    private EventData resendEventData;

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        logger.asInfo().log("[{}]: Performing test set-up.", testName.getMethodName());

        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        client = new EventHubClient(getConnectionOptions(), getReactorProvider(), handlerProvider);

        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(PARTITION_ID).retry(Retry.getNoRetry()).timeout(Duration.ofSeconds(30));
        producer = client.createProducer(producerOptions);
        consumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.latest());
    }

    @Override
    protected void afterTest() {
        logger.asInfo().log("[{}]: Performing test clean-up.", testName.getMethodName());
        closeClient(client, producer, consumer, testName, logger);
    }

    /**
     * Test for interoperable with Direct Proton Amqp messaging
     */
    @Ignore
    @Test
    public void interoperableWithDirectProtonAmqpMessage() {
        skipIfNotRecordMode();

        // Arrange
        final Message originalMessage = Proton.message();
        final HashMap<String, Object> appProperties = new HashMap<>();

        appProperties.put(APPLICATION_PROPERTY, "value1");
        final ApplicationProperties applicationProperties = new ApplicationProperties(appProperties);
        originalMessage.setApplicationProperties(applicationProperties);
        originalMessage.setMessageId("id1");
        originalMessage.setUserId("user1".getBytes());
        originalMessage.setAddress("eventhub1");
        originalMessage.setSubject("sub");
        originalMessage.setReplyTo("replyingTo");
        originalMessage.setExpiryTime(456L);
        originalMessage.setGroupSequence(5555L);
        originalMessage.setContentType("events");
        originalMessage.setContentEncoding("UTF-8");
        originalMessage.setCorrelationId("corid1");
        originalMessage.setCreationTime(345L);
        originalMessage.setGroupId("gid");
        originalMessage.setReplyToGroupId("replyToGroupId");
        originalMessage.setMessageAnnotations(new MessageAnnotations(new HashMap<>()));
        originalMessage.getMessageAnnotations().getValue().put(Symbol.getSymbol(MESSAGE_ANNOTATION), "messageAnnotationValue");
        originalMessage.setBody(new Data(Binary.create(ByteBuffer.wrap(PAYLOAD.getBytes()))));
        final EventData msgEvent = new EventData(originalMessage);

        // Act & Assert
        StepVerifier.create(consumer.receive().take(1))
            .then(() -> producer.send(msgEvent).block(TIMEOUT))
            .assertNext(event -> {
                validateAmqpPropertiesInEventData(event, originalMessage);
                resendEventData = event;
            })
            .verifyComplete();

        StepVerifier.create(consumer.receive().take(1))
            .then(() -> producer.send(resendEventData).block(TIMEOUT))
            .assertNext(event -> validateAmqpPropertiesInEventData(event, originalMessage))
            .verifyComplete();
    }

    private void validateAmqpPropertiesInEventData(EventData eData, Message originalMessage) {
        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.MESSAGE_ID.getValue()));
        Assert.assertEquals(originalMessage.getMessageId(), eData.systemProperties().get(MessageConstant.MESSAGE_ID.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.USER_ID.getValue()));
        Assert.assertEquals(new String(originalMessage.getUserId()), new String((byte[]) eData.systemProperties().get(MessageConstant.USER_ID.getValue())));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.TO.getValue()));
        Assert.assertEquals(originalMessage.getAddress(), eData.systemProperties().get(MessageConstant.TO.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.CONTENT_TYPE.getValue()));
        Assert.assertEquals(originalMessage.getContentType(), eData.systemProperties().get(MessageConstant.CONTENT_TYPE.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.CONTENT_ENCODING.getValue()));
        Assert.assertEquals(originalMessage.getContentEncoding(), eData.systemProperties().get(MessageConstant.CONTENT_ENCODING.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.CORRELATION_ID.getValue()));
        Assert.assertEquals(originalMessage.getCorrelationId(), eData.systemProperties().get(MessageConstant.CORRELATION_ID.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.CREATION_TIME.getValue()));
        Assert.assertEquals(originalMessage.getCreationTime(), eData.systemProperties().get(MessageConstant.CREATION_TIME.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.SUBJECT.getValue()));
        Assert.assertEquals(originalMessage.getSubject(), eData.systemProperties().get(MessageConstant.SUBJECT.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.GROUP_ID.getValue()));
        Assert.assertEquals(originalMessage.getGroupId(), eData.systemProperties().get(MessageConstant.GROUP_ID.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.REPLY_TO_GROUP_ID.getValue()));
        Assert.assertEquals(originalMessage.getReplyToGroupId(), eData.systemProperties().get(MessageConstant.REPLY_TO_GROUP_ID.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.REPLY_TO.getValue()));
        Assert.assertEquals(originalMessage.getReplyTo(), eData.systemProperties().get(MessageConstant.REPLY_TO.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MessageConstant.ABSOLUTE_EXPRITY_TIME.getValue()));
        Assert.assertEquals(originalMessage.getExpiryTime(), eData.systemProperties().get(MessageConstant.ABSOLUTE_EXPRITY_TIME.getValue()));

        Assert.assertTrue(eData.systemProperties().containsKey(MESSAGE_ANNOTATION));
        Assert.assertEquals(originalMessage.getMessageAnnotations().getValue().get(Symbol.getSymbol(MESSAGE_ANNOTATION)), eData.systemProperties().get(MESSAGE_ANNOTATION));

        Assert.assertTrue(eData.properties().containsKey(APPLICATION_PROPERTY));
        Assert.assertEquals(originalMessage.getApplicationProperties().getValue().get(APPLICATION_PROPERTY), eData.properties().get(APPLICATION_PROPERTY));

        Assert.assertEquals(1, eData.properties().size());
        Assert.assertEquals(PAYLOAD, UTF_8.decode(eData.body()).toString());
    }
}
