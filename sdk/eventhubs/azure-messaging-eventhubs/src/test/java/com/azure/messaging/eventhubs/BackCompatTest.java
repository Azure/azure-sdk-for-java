// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.messaging.eventhubs.TestUtils.MESSAGE_ID;
import static com.azure.messaging.eventhubs.TestUtils.getSymbol;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Integration test that verifies backwards compatibility with a previous version of the SDK.
 */
@Tag(TestUtils.INTEGRATION)
@Execution(ExecutionMode.SAME_THREAD)
public class BackCompatTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "2";
    private static final String PAYLOAD = "test-message";

    private final MessageSerializer serializer = new EventHubMessageSerializer();
    private EventHubProducerAsyncClient producer;
    private EventHubConsumerAsyncClient consumer;
    private SendOptions sendOptions;

    public BackCompatTest() {
        super(new ClientLogger(BackCompatTest.class));
    }

    @Override
    protected void beforeTest() {
        consumer = toClose(createBuilder().consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient());

        sendOptions = new SendOptions().setPartitionId(PARTITION_ID);
        producer = toClose(createBuilder().buildAsyncProducerClient());
    }

    /**
     * Verifies test work with SDK versions before 0.11.0.
     */
    @Test
    public void backCompatWithJavaSDKOlderThan0110() {
        // Arrange
        final Duration timeout = Duration.ofSeconds(30);
        final String messageTrackingValue = UUID.randomUUID().toString();
        final PartitionProperties properties = consumer.getPartitionProperties(PARTITION_ID).block(timeout);

        Assertions.assertNotNull(properties);
        final EventPosition position = EventPosition.fromSequenceNumber(properties.getLastEnqueuedSequenceNumber(), true);

        // until version 0.10.0 - we used to have Properties as HashMap<String,String>
        // This specific combination is intended to test the back compat - with the new Properties type as HashMap<String, Object>
        final HashMap<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put("firstProperty", "value1");
        applicationProperties.put("intProperty", "3");

        // We want to ensure that we fetch the event data corresponding to this test and not some other test case.
        applicationProperties.put(MESSAGE_ID, messageTrackingValue);

        final Map<Symbol, Object> systemProperties = new HashMap<>();
        systemProperties.put(getSymbol(OFFSET_ANNOTATION_NAME), "100");
        systemProperties.put(getSymbol(ENQUEUED_TIME_UTC_ANNOTATION_NAME), Date.from(Instant.now()));
        systemProperties.put(getSymbol(SEQUENCE_NUMBER_ANNOTATION_NAME), 15L);

        final Message message = Proton.message();
        message.setApplicationProperties(new ApplicationProperties(applicationProperties));
        message.setBody(new Data(Binary.create(ByteBuffer.wrap(PAYLOAD.getBytes(UTF_8)))));
        message.setMessageAnnotations(new MessageAnnotations(systemProperties));

        final EventData eventData = serializer.deserialize(message, EventData.class);

        // Act & Assert
        producer.send(eventData, sendOptions).block(TIMEOUT);

        StepVerifier.create(consumer.receiveFromPartition(PARTITION_ID, position)
            .filter(received -> isMatchingEvent(received, messageTrackingValue)).take(1))
            .assertNext(event -> validateAmqpProperties(applicationProperties, event.getData()))
            .expectComplete()
            .verify(timeout);
    }

    private void validateAmqpProperties(Map<String, Object> expected, EventData event) {
        Assertions.assertEquals(expected.size(), event.getProperties().size());
        Assertions.assertEquals(PAYLOAD, event.getBodyAsString());

        expected.forEach((key, value) -> {
            Assertions.assertTrue(event.getProperties().containsKey(key));
            Assertions.assertEquals(value, event.getProperties().get(key));
        });
    }
}
