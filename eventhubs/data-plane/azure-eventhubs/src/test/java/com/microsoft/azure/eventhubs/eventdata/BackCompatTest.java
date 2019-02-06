/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.eventdata;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.eventhubs.impl.MessageSender;
import com.microsoft.azure.eventhubs.impl.MessagingFactory;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.message.Message;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class BackCompatTest extends ApiTestBase {
    static final String partitionId = "0";
    static final Message originalMessage = Proton.message();
    static final String applicationProperty = "firstProp";
    static final String intApplicationProperty = "intProp";
    static final String msgAnnotation = "message-annotation-1";
    static final String payload = "testmsg";
    static EventHubClient ehClient;
    static MessagingFactory msgFactory;
    static PartitionReceiver receiver;
    static MessageSender partitionMsgSender;
    static EventData receivedEvent;

    final Consumer<EventData> validateAmqpPropertiesInEventData = new Consumer<EventData>() {
        @Override
        public void accept(EventData eData) {
            Assert.assertTrue(eData.getProperties().containsKey(applicationProperty)
                    && eData.getProperties().get(applicationProperty).equals(originalMessage.getApplicationProperties().getValue().get(applicationProperty)));

            Assert.assertTrue(eData.getProperties().containsKey(intApplicationProperty)
                    && eData.getProperties().get(intApplicationProperty).equals(originalMessage.getApplicationProperties().getValue().get(intApplicationProperty)));

            Assert.assertTrue(eData.getProperties().size() == 2);

            Assert.assertTrue(new String(eData.getBytes()).equals(payload));
        }
    };

    @BeforeClass
    public static void initialize() throws EventHubException, IOException, InterruptedException, ExecutionException {
        final ConnectionStringBuilder connStrBuilder = TestContext.getConnectionString();
        final String connectionString = connStrBuilder.toString();

        ehClient = EventHubClient.createSync(connectionString, TestContext.EXECUTOR_SERVICE);
        msgFactory = MessagingFactory.createFromConnectionString(connectionString, TestContext.EXECUTOR_SERVICE).get();
        receiver = ehClient.createReceiverSync(TestContext.getConsumerGroupName(), partitionId, EventPosition.fromEnqueuedTime(Instant.now()));
        partitionMsgSender = MessageSender.create(msgFactory, "link1", connStrBuilder.getEventHubName() + "/partitions/" + partitionId).get();

        // until version 0.10.0 - we used to have Properties as HashMap<String,String>
        // This specific combination is intended to test the back compat - with the new Properties type as HashMap<String, Object>
        final HashMap<String, Object> appProperties = new HashMap<>();
        appProperties.put(applicationProperty, "value1");
        appProperties.put(intApplicationProperty, "3");
        // back compat end

        final ApplicationProperties applicationProperties = new ApplicationProperties(appProperties);
        originalMessage.setApplicationProperties(applicationProperties);

        originalMessage.setBody(new Data(Binary.create(ByteBuffer.wrap(payload.getBytes()))));

        partitionMsgSender.send(originalMessage).get();
        receivedEvent = receiver.receiveSync(10).iterator().next();
    }

    @AfterClass
    public static void cleanup() throws EventHubException {
        if (partitionMsgSender != null)
            partitionMsgSender.closeSync();

        if (receiver != null)
            receiver.closeSync();

        if (ehClient != null)
            ehClient.closeSync();

        if (msgFactory != null)
            msgFactory.closeSync();
    }

    @Test
    public void backCompatWithJavaSDKOlderThan_0_11_0() {
        validateAmqpPropertiesInEventData.accept(receivedEvent);
    }
}
