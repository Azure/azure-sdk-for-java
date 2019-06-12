// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.eventhubs.implementation.ApiTestBase;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BackCompatTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    private static final Message ORIGINAL_MESSAGE = Proton.message();
    private static final String APPLICATION_PROPERTY = "firstProp";
    private static final String INT_APPLICATION_PROPERTY = "intProp";
    private static final String PAYLOAD = "testmsg";

    private static EventHubClient ehClient;
    private static EventReceiver receiver;
    private static EventSender sender;
    private static Flux<EventData> receivedEventData;
    private static EventData msgEvent;

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @BeforeClass
    public static void initialize() {
        ehClient = ApiTestBase.getEventHubClientBuilder().build();
        sender = ehClient.createSender(new EventSenderOptions().partitionId(PARTITION_ID));
        receiver = ehClient.createReceiver(PARTITION_ID, new EventReceiverOptions()
            .consumerGroup(ApiTestBase.getConsumerGroupName())
            .beginReceivingAt(EventPosition.newEventsOnly()));

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

        msgEvent = new EventData(ORIGINAL_MESSAGE);

        //TODO: delete it after receive() is fully implemented
        receiver.setTestEventData(msgEvent);

        receivedEventData = receiver.receive();
    }

    @AfterClass
    public static void cleanup() {
        if (ehClient != null) {
            ehClient.close();
        }
    }

    /**
     * Verifies test work with versions before 0.11.0.
     */
    @Test
    public void backCompatWithJavaSDKOlderThan0110() {
        StepVerifier.create(receivedEventData)
            .then(() -> sender.send(Mono.just(msgEvent)))
            .expectNextMatches(event -> {
                validateAmqpPropertiesInEventData.accept(event);
                return true;
            })
            .verifyComplete();
    }

    private final Consumer<EventData> validateAmqpPropertiesInEventData = eData -> {
        Assert.assertTrue(eData.properties().containsKey(APPLICATION_PROPERTY)
            && eData.properties().get(APPLICATION_PROPERTY).equals(ORIGINAL_MESSAGE.getApplicationProperties().getValue().get(APPLICATION_PROPERTY)));
        Assert.assertTrue(eData.properties().containsKey(INT_APPLICATION_PROPERTY)
            && eData.properties().get(INT_APPLICATION_PROPERTY).equals(ORIGINAL_MESSAGE.getApplicationProperties().getValue().get(INT_APPLICATION_PROPERTY)));
        Assert.assertTrue(eData.properties().size() == 2);
        Assert.assertTrue(PAYLOAD.equals(UTF_8.decode(eData.body()).toString()));
    };
}
