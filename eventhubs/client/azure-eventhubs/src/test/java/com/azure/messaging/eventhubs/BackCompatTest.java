// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ApiTestBase;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
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

public class BackCompatTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    private static final String APPLICATION_PROPERTY = "firstProp";
    private static final String INT_APPLICATION_PROPERTY = "intProp";
    private static final String PAYLOAD = "testmsg";

    private final ClientLogger logger = new ClientLogger(BackCompatTest.class);

    private EventHubClient client;
    private EventHubProducer producer;
    private EventHubConsumer consumer;
    private ReactorHandlerProvider handlerProvider;

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        logger.asInfo().log("[{}]: Performing test set-up.", testName.getMethodName());

        handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        client = new EventHubClient(getConnectionOptions(), getReactorProvider(), handlerProvider);
        consumer = client.createConsumer(getConsumerGroupName(), PARTITION_ID, EventPosition.latest());
        producer = client.createProducer(new EventHubProducerOptions().partitionId(PARTITION_ID).retry(Retry.getNoRetry()).timeout(Duration.ofSeconds(30)));
    }

    @Override
    protected void afterTest() {
        logger.asInfo().log("[{}]: Performing test clean-up.", testName.getMethodName());
        closeClient(client, producer, consumer, testName, logger);
    }

    /**
     * Verifies test work with versions before 0.11.0.
     */
    @Ignore
    @Test
    public void backCompatWithJavaSDKOlderThan0110() {
        skipIfNotRecordMode();

        // Arrange
        final Message originalMessage = Proton.message();

        // until version 0.10.0 - we used to have Properties as HashMap<String,String>
        // This specific combination is intended to test the back compat - with the new Properties type as HashMap<String, Object>
        final HashMap<String, Object> appProperties = new HashMap<>();
        appProperties.put(APPLICATION_PROPERTY, "value1");
        appProperties.put(INT_APPLICATION_PROPERTY, "3");
        // back compat end
        final ApplicationProperties applicationProperties = new ApplicationProperties(appProperties);

        originalMessage.setApplicationProperties(applicationProperties);
        originalMessage.setBody(new Data(Binary.create(ByteBuffer.wrap(PAYLOAD.getBytes()))));
        originalMessage.setMessageAnnotations(new MessageAnnotations(new HashMap<>()));
        final EventData msgEvent = new EventData(originalMessage);

        // Act & Assert
        StepVerifier.create(consumer.receive())
            .then(() -> producer.send(msgEvent).block(TIMEOUT))
            .assertNext(event -> validateAmqpPropertiesInEventData(event, originalMessage))
            .verifyComplete();
    }

    private void validateAmqpPropertiesInEventData(EventData eData, Message originalMessage) {
        Assert.assertTrue(eData.properties().containsKey(APPLICATION_PROPERTY));
        Assert.assertEquals(originalMessage.getApplicationProperties().getValue().get(APPLICATION_PROPERTY), eData.properties().get(APPLICATION_PROPERTY));

        Assert.assertTrue(eData.properties().containsKey(INT_APPLICATION_PROPERTY));
        Assert.assertEquals(originalMessage.getApplicationProperties().getValue().get(INT_APPLICATION_PROPERTY), eData.properties().get(INT_APPLICATION_PROPERTY));

        Assert.assertEquals(2, eData.properties().size());
        Assert.assertEquals(PAYLOAD, UTF_8.decode(eData.body()).toString());
    }
}
