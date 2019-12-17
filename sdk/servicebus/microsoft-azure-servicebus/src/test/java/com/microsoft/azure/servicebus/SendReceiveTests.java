// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.servicebus.management.ManagementClientAsync;
import com.microsoft.azure.servicebus.management.QueueDescription;
import com.microsoft.azure.servicebus.management.SubscriptionDescription;
import com.microsoft.azure.servicebus.management.TopicDescription;

import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class SendReceiveTests extends Tests {
    static ManagementClientAsync managementClient = null;
    private static String entityNameCreatedForAllTests = null;
    private static String receiveEntityPathForAllTest = null;
    
    MessagingFactory factory;
    IMessageSender sender;
    IMessageReceiver receiver;
    String receiveEntityPath;

    private String entityName;
    private final String sessionId = null;

    @BeforeClass
    public static void init() {
        SendReceiveTests.entityNameCreatedForAllTests = null;
        SendReceiveTests.receiveEntityPathForAllTest = null;
        URI namespaceEndpointURI = TestUtils.getNamespaceEndpointURI();
        ClientSettings managementClientSettings = TestUtils.getManagementClientSettings();
        managementClient = new ManagementClientAsync(namespaceEndpointURI, managementClientSettings);
    }

    @Before
    public void setup() throws InterruptedException, ExecutionException, ServiceBusException {
        if (this.shouldCreateEntityForEveryTest() || SendReceiveTests.entityNameCreatedForAllTests == null) {
             // Create entity
            this.entityName = TestUtils.randomizeEntityName(this.getEntityNamePrefix());
            if (this.isEntityQueue()) {
                this.receiveEntityPath = this.entityName;
                QueueDescription queueDescription = new QueueDescription(this.entityName);
                queueDescription.setEnablePartitioning(this.isEntityPartitioned());
                managementClient.createQueueAsync(queueDescription).get();
                if (!this.shouldCreateEntityForEveryTest()) {
                    SendReceiveTests.entityNameCreatedForAllTests = entityName;
                    SendReceiveTests.receiveEntityPathForAllTest = entityName;
                }
            } else {
                TopicDescription topicDescription = new TopicDescription(this.entityName);
                topicDescription.setEnablePartitioning(this.isEntityPartitioned());
                managementClient.createTopicAsync(topicDescription).get();
                SubscriptionDescription subDescription = new SubscriptionDescription(this.entityName, TestUtils.FIRST_SUBSCRIPTION_NAME);
                managementClient.createSubscriptionAsync(subDescription).get();
                this.receiveEntityPath = subDescription.getPath();
                if (!this.shouldCreateEntityForEveryTest()) {
                    SendReceiveTests.entityNameCreatedForAllTests = entityName;
                    SendReceiveTests.receiveEntityPathForAllTest = subDescription.getPath();
                }
            }
        } else {
            this.entityName = SendReceiveTests.entityNameCreatedForAllTests;
            this.receiveEntityPath = SendReceiveTests.receiveEntityPathForAllTest;
        }

        this.factory = MessagingFactory.createFromNamespaceEndpointURI(TestUtils.getNamespaceEndpointURI(), TestUtils.getClientSettings());
        this.sender = ClientFactory.createMessageSenderFromEntityPath(this.factory, this.entityName);
    }

    @After
    public void tearDown() throws ServiceBusException, InterruptedException, ExecutionException {
        if (!this.shouldCreateEntityForEveryTest()) {
            this.drainAllMessages();
        }

        if (this.sender != null) {
        	this.sender.close();
        }
        
        if (this.receiver != null) {
            this.receiver.close();
        }
        
        if (this.factory != null) {
        	this.factory.close();
        }        

        if (this.shouldCreateEntityForEveryTest()) {
            managementClient.deleteQueueAsync(this.entityName).get();
        }
    }

    @AfterClass
    public static void cleanupAfterAllTest() throws ExecutionException, InterruptedException, IOException {
        if (managementClient == null) {
            return;
        }
        if (SendReceiveTests.entityNameCreatedForAllTests != null) {
            managementClient.deleteQueueAsync(SendReceiveTests.entityNameCreatedForAllTests).get();
            managementClient.close();
        }
    }

    @Test
    public void testBasicReceiveAndDeleteWithValueData() throws InterruptedException, ServiceBusException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.RECEIVEANDDELETE);
        TestCommons.testBasicReceiveAndDeleteWithValueData(this.sender, this.sessionId, this.receiver);
    }

    @Test
    public void testBasicReceiveAndDeleteWithBinaryData() throws InterruptedException, ServiceBusException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.RECEIVEANDDELETE);
        TestCommons.testBasicReceiveAndDeleteWithBinaryData(this.sender, this.sessionId, this.receiver);
    }

    @Test
    public void testBasicReceiveAndCompleteWithLargeBinaryData() throws InterruptedException, ServiceBusException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
        TestCommons.testBasicReceiveAndCompleteWithLargeBinaryData(this.sender, this.sessionId, this.receiver);
    }

    @Test
    public void testBasicReceiveAndDeleteWithSequenceData() throws InterruptedException, ServiceBusException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.RECEIVEANDDELETE);
        TestCommons.testBasicReceiveAndDeleteWithSequenceData(this.sender, this.sessionId, this.receiver);
    }

    @Test
    public void testBasicReceiveBatchAndDelete() throws InterruptedException, ServiceBusException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.RECEIVEANDDELETE);
        TestCommons.testBasicReceiveBatchAndDelete(this.sender, this.sessionId, this.receiver, this.isEntityPartitioned());
    }

    @Test
    public void testBasicReceiveAndComplete() throws InterruptedException, ServiceBusException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
        TestCommons.testBasicReceiveAndComplete(this.sender, this.sessionId, this.receiver);
    }

    @Test
    public void testBasicReceiveAndCompleteMessageWithProperties() throws InterruptedException, ServiceBusException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
        TestCommons.testBasicReceiveAndCompleteMessageWithProperties(this.sender, this.sessionId, this.receiver);
    }

    @Test
    public void testBasicReceiveAndAbandon() throws InterruptedException, ServiceBusException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
        TestCommons.testBasicReceiveAndAbandon(this.sender, this.sessionId, this.receiver);
    }

    @Test
    public void testBasicReceiveAndDeadLetter() throws InterruptedException, ServiceBusException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
        TestCommons.testBasicReceiveAndDeadLetter(this.sender, this.sessionId, this.receiver);
    }

    @Test
    public void testBasicReceiveAndRenewLock() throws InterruptedException, ServiceBusException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
        TestCommons.testBasicReceiveAndRenewLock(this.sender, this.sessionId, this.receiver);
    }

    @Test
    public void testBasicReceiveBatchAndComplete() throws InterruptedException, ServiceBusException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
        TestCommons.testBasicReceiveBatchAndComplete(this.sender, this.sessionId, this.receiver, this.isEntityPartitioned());
    }

    @Test
    public void testSendSceduledMessageAndReceive() throws InterruptedException, ServiceBusException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.RECEIVEANDDELETE);
        TestCommons.testSendSceduledMessageAndReceive(this.sender, this.sessionId, this.receiver);
    }

    @Test
    public void testSendSceduledMessageAndCancel() throws InterruptedException, ServiceBusException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.RECEIVEANDDELETE);
        TestCommons.testSendSceduledMessageAndCancel(this.sender, this.sessionId, this.receiver);
    }

    @Test
    public void testPeekMessage() throws InterruptedException, ServiceBusException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
        TestCommons.testPeekMessage(this.sender, this.sessionId, this.receiver);
    }

    @Test
    public void testPeekMessageBatch() throws InterruptedException, ServiceBusException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
        TestCommons.testPeekMessageBatch(this.sender, this.sessionId, this.receiver, this.isEntityPartitioned());
    }

    @Test
    public void testReceiveBySequenceNumberAndComplete() throws InterruptedException, ServiceBusException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
        TestCommons.testReceiveBySequenceNumberAndComplete(this.sender, this.sessionId, this.receiver);
    }

    @Test
    public void testReceiveBySequenceNumberAndAbandon() throws InterruptedException, ServiceBusException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
        TestCommons.testReceiveBySequenceNumberAndAbandon(this.sender, this.sessionId, this.receiver);
    }

    @Test
    public void testReceiveBySequenceNumberAndDefer() throws InterruptedException, ServiceBusException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
        TestCommons.testReceiveBySequenceNumberAndDefer(this.sender, this.sessionId, this.receiver);
    }

    @Test
    public void testReceiveBySequenceNumberAndDeadletter() throws InterruptedException, ServiceBusException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
        TestCommons.testReceiveBySequenceNumberAndDeadletter(this.sender, this.sessionId, this.receiver);
    }

    @Test
    public void testSendReceiveMessageWithVariousPropertyTypes() throws InterruptedException, ServiceBusException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.RECEIVEANDDELETE);
        TestCommons.testSendReceiveMessageWithVariousPropertyTypes(this.sender, this.sessionId, this.receiver);
    }

    private void drainAllMessages() throws InterruptedException, ServiceBusException {
        if (this.receiver != null) {
            TestCommons.drainAllMessagesFromReceiver(this.receiver);
        }
    }
}
