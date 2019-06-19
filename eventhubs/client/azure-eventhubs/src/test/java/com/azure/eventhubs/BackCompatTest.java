// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.implementation.ApiTestBase;
import com.azure.eventhubs.implementation.ReactorHandlerProvider;
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
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.HashMap;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BackCompatTest extends ApiTestBase {
    private final ServiceLogger logger = new ServiceLogger(BackCompatTest.class);

    private static final String PARTITION_ID = "0";
    private static final Message ORIGINAL_MESSAGE = Proton.message();
    private static final String APPLICATION_PROPERTY = "firstProp";
    private static final String INT_APPLICATION_PROPERTY = "intProp";
    private static final String PAYLOAD = "testmsg";

    private EventHubClient client;
    private EventSender sender;
    private EventReceiver receiver;
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
    }

    @Override
    protected void afterTest() {
        logger.asInfo().log("[{}]: Performing test clean-up.", testName.getMethodName());

        if (client != null) {
            client.close();
        }

        if (sender != null) {
            try {
                sender.close();
            } catch (IOException e) {
                logger.asError().log("[{}]: Sender doesn't close properly", testName.getMethodName());
            }
        }

        if (receiver != null) {
            try {
                receiver.close();
            } catch (IOException e) {
                logger.asError().log("[{}]: Receiver doesn't close properly", testName.getMethodName());
            }
        }
    }

    /**
     * Verifies test work with versions before 0.11.0.
     */
    @Ignore
    @Test
    public void backCompatWithJavaSDKOlderThan0110() {
        // Arrange
        receiver = client.createReceiver(PARTITION_ID, EventPosition.latest(),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName()));
        // until version 0.10.0 - we used to have Properties as HashMap<String,String>
        // This specific combination is intended to test the back compat - with the new Properties type as HashMap<String, Object>
        final HashMap<String, Object> appProperties = new HashMap<>();
        appProperties.put(APPLICATION_PROPERTY, "value1");
        appProperties.put(INT_APPLICATION_PROPERTY, "3");
        // back compat end
        final ApplicationProperties applicationProperties = new ApplicationProperties(appProperties);

        ORIGINAL_MESSAGE.setApplicationProperties(applicationProperties);
        ORIGINAL_MESSAGE.setBody(new Data(Binary.create(ByteBuffer.wrap(PAYLOAD.getBytes()))));
        ORIGINAL_MESSAGE.setMessageAnnotations(new MessageAnnotations(new HashMap<>()));
        final EventData msgEvent = new EventData(ORIGINAL_MESSAGE);


        // Action & Assert
        Flux<EventData> receivedEventData = receiver.receive();
        sender = client.createSender(new EventSenderOptions().partitionId(PARTITION_ID).retry(Retry.getNoRetry()).timeout(Duration.ofSeconds(30)));
        sender.send(msgEvent);
        StepVerifier.create(receivedEventData)
            .expectNextMatches(event -> {
                validateAmqpPropertiesInEventData.accept(event);
                return true;
            })
            .verifyComplete();
    }

    private final Consumer<EventData> validateAmqpPropertiesInEventData = eData -> {
        Assert.assertTrue(eData.properties().containsKey(APPLICATION_PROPERTY));
        Assert.assertEquals(ORIGINAL_MESSAGE.getApplicationProperties().getValue().get(APPLICATION_PROPERTY), eData.properties().get(APPLICATION_PROPERTY));

        Assert.assertTrue(eData.properties().containsKey(INT_APPLICATION_PROPERTY));
        Assert.assertEquals(ORIGINAL_MESSAGE.getApplicationProperties().getValue().get(INT_APPLICATION_PROPERTY), eData.properties().get(INT_APPLICATION_PROPERTY));

        Assert.assertTrue(eData.properties().size() == 2);
        Assert.assertTrue(PAYLOAD.equals(UTF_8.decode(eData.body()).toString()));
    };
}
