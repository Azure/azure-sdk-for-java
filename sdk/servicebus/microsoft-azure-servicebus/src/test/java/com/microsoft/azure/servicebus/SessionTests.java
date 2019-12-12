// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.servicebus.management.ManagementClientAsync;
import com.microsoft.azure.servicebus.management.QueueDescription;
import com.microsoft.azure.servicebus.management.SubscriptionDescription;
import com.microsoft.azure.servicebus.management.TopicDescription;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.TimeoutException;

public abstract class SessionTests extends Tests {
    private static String entityNameCreatedForAllTests = null;
    private static String receiveEntityPathForAllTest = null;
    private static ManagementClientAsync managementClient;

    MessagingFactory factory;
    IMessageSender sender;
    IMessageSession session;
    private String entityName;
    String receiveEntityPath;

    @BeforeClass
    public static void init() {
        SessionTests.entityNameCreatedForAllTests = null;
        SessionTests.receiveEntityPathForAllTest = null;
        URI namespaceEndpointURI = TestUtils.getNamespaceEndpointURI();
        ClientSettings managementClientSettings = TestUtils.getManagementClientSettings();
        managementClient = new ManagementClientAsync(namespaceEndpointURI, managementClientSettings);
    }

    @Before
    public void setup() throws InterruptedException, ExecutionException, ServiceBusException {
        if (this.shouldCreateEntityForEveryTest() || SessionTests.entityNameCreatedForAllTests == null) {
             // Create entity
            this.entityName = TestUtils.randomizeEntityName(this.getEntityNamePrefix());
            if (this.isEntityQueue()) {
                this.receiveEntityPath = this.entityName;
                QueueDescription queueDescription = new QueueDescription(this.entityName);
                queueDescription.setEnablePartitioning(this.isEntityPartitioned());
                queueDescription.setRequiresSession(true);
                managementClient.createQueueAsync(queueDescription).get();
                if (!this.shouldCreateEntityForEveryTest()) {
                    SessionTests.entityNameCreatedForAllTests = entityName;
                    SessionTests.receiveEntityPathForAllTest = entityName;
                }
            } else {
                TopicDescription topicDescription = new TopicDescription(this.entityName);
                topicDescription.setEnablePartitioning(this.isEntityPartitioned());
                managementClient.createTopicAsync(topicDescription).get();
                SubscriptionDescription subDescription = new SubscriptionDescription(this.entityName, TestUtils.FIRST_SUBSCRIPTION_NAME);
                subDescription.setRequiresSession(true);
                managementClient.createSubscriptionAsync(subDescription).get();
                this.receiveEntityPath = subDescription.getPath();
                if (!this.shouldCreateEntityForEveryTest()) {
                    SessionTests.entityNameCreatedForAllTests = entityName;
                    SessionTests.receiveEntityPathForAllTest = subDescription.getPath();
                }
            }
        } else {
            this.entityName = SessionTests.entityNameCreatedForAllTests;
            this.receiveEntityPath = SessionTests.receiveEntityPathForAllTest;
        }

        this.factory = MessagingFactory.createFromNamespaceEndpointURI(TestUtils.getNamespaceEndpointURI(), TestUtils.getClientSettings());
        this.sender = ClientFactory.createMessageSenderFromEntityPath(this.factory, this.entityName);
    }

    @After
    public void tearDown() throws ServiceBusException, InterruptedException, ExecutionException {
        if (!this.shouldCreateEntityForEveryTest()) {
            this.drainSession();
        }
        
        if (this.sender != null) {
        	this.sender.close();
        }
        
        if (this.session != null) {
            this.session.close();
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
        if (managementClient != null) {
            if (SessionTests.entityNameCreatedForAllTests != null) {
                managementClient.deleteQueueAsync(SessionTests.entityNameCreatedForAllTests).get();
            }

            managementClient.close();
        }
    }

    @Test
    public void testBasicReceiveAndDeleteWithValueData() throws InterruptedException, ServiceBusException, ExecutionException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.RECEIVEANDDELETE);
        TestCommons.testBasicReceiveAndDeleteWithValueData(this.sender, sessionId, this.session);
    }

    @Test
    public void testBasicReceiveAndDeleteWithBinaryData() throws InterruptedException, ServiceBusException, ExecutionException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.RECEIVEANDDELETE);
        TestCommons.testBasicReceiveAndDeleteWithBinaryData(this.sender, sessionId, this.session);
    }

    @Test
    public void testBasicReceiveAndCompleteWithLargeBinaryData() throws InterruptedException, ServiceBusException, ExecutionException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
        TestCommons.testBasicReceiveAndCompleteWithLargeBinaryData(this.sender, sessionId, this.session);
    }

    @Test
    public void testBasicReceiveAndDeleteWithSequenceData() throws InterruptedException, ServiceBusException, ExecutionException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.RECEIVEANDDELETE);
        TestCommons.testBasicReceiveAndDeleteWithSequenceData(this.sender, sessionId, this.session);
    }

    @Test
    public void testBasicReceiveBatchAndDelete() throws InterruptedException, ServiceBusException, ExecutionException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.RECEIVEANDDELETE);
        TestCommons.testBasicReceiveBatchAndDelete(this.sender, sessionId, this.session, this.isEntityPartitioned());
    }

    @Test
    public void testBasicReceiveAndComplete() throws InterruptedException, ServiceBusException, ExecutionException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
        TestCommons.testBasicReceiveAndComplete(this.sender, sessionId, this.session);
    }

    @Test
    public void testBasicReceiveAndCompleteMessageWithProperties() throws InterruptedException, ServiceBusException, ExecutionException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
        TestCommons.testBasicReceiveAndCompleteMessageWithProperties(this.sender, sessionId, this.session);
    }

    @Test
    public void testBasicReceiveAndAbandon() throws InterruptedException, ServiceBusException, ExecutionException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
        TestCommons.testBasicReceiveAndAbandon(this.sender, sessionId, this.session);
    }

    @Test
    public void testBasicReceiveAndDeadLetter() throws InterruptedException, ServiceBusException, ExecutionException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
        TestCommons.testBasicReceiveAndDeadLetter(this.sender, sessionId, this.session);
    }

    @Test
    public void testBasicReceiveBatchAndComplete() throws InterruptedException, ServiceBusException, ExecutionException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
        TestCommons.testBasicReceiveBatchAndComplete(this.sender, sessionId, this.session, this.isEntityPartitioned());
    }

    @Test
    public void testSendSceduledMessageAndReceive() throws InterruptedException, ServiceBusException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.RECEIVEANDDELETE);
        TestCommons.testSendSceduledMessageAndReceive(this.sender, sessionId, this.session);
    }

    @Test
    public void testSendSceduledMessageAndCancel() throws InterruptedException, ServiceBusException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.RECEIVEANDDELETE);
        TestCommons.testSendSceduledMessageAndCancel(this.sender, sessionId, this.session);
    }

    @Test
    public void testPeekMessage() throws InterruptedException, ServiceBusException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
        TestCommons.testPeekMessage(this.sender, sessionId, this.session);
    }

    @Test
    public void testPeekMessageBatch() throws InterruptedException, ServiceBusException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
        TestCommons.testPeekMessageBatch(this.sender, sessionId, this.session, this.isEntityPartitioned());
    }

    @Test
    public void testReceiveBySequenceNumberAndComplete() throws InterruptedException, ServiceBusException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
        TestCommons.testReceiveBySequenceNumberAndComplete(this.sender, sessionId, this.session);
    }

    @Test
    public void testReceiveBySequenceNumberAndAbandon() throws InterruptedException, ServiceBusException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
        TestCommons.testReceiveBySequenceNumberAndAbandon(this.sender, sessionId, this.session);
    }

    @Test
    public void testReceiveBySequenceNumberAndDefer() throws InterruptedException, ServiceBusException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
        TestCommons.testReceiveBySequenceNumberAndDefer(this.sender, sessionId, this.session);
    }

    @Test
    public void testReceiveBySequenceNumberAndDeadletter() throws InterruptedException, ServiceBusException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
        TestCommons.testReceiveBySequenceNumberAndDeadletter(this.sender, sessionId, this.session);
    }

    @Test
    public void testSendReceiveMessageWithVariousPropertyTypes() throws InterruptedException, ServiceBusException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.RECEIVEANDDELETE);
        TestCommons.testSendReceiveMessageWithVariousPropertyTypes(this.sender, sessionId, this.session);
    }

    @Test
    public void testAcceptAnySession() throws InterruptedException, ServiceBusException {
        String sessionId = TestUtils.getRandomString();
        String messageId = TestUtils.getRandomString();
        Message message = new Message("AMQP message");
        message.setMessageId(messageId);
        message.setSessionId(sessionId);
        this.sender.send(message);

        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, null, ReceiveMode.PEEKLOCK);
        Assert.assertNotNull("Did not receive a session", this.session);
    }

    @Test
    public void testRenewSessionLock() throws InterruptedException, ServiceBusException {
    	String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
		Instant initialValidity = this.session.getLockedUntilUtc();
		Thread.sleep(1000);
		this.session.renewSessionLock();
		Instant renewedValidity = this.session.getLockedUntilUtc();
		Assert.assertTrue("RenewSessionLock did not renew session lockeduntil time. Before :" + initialValidity.toString() + ", After:" + renewedValidity.toString(), renewedValidity.isAfter(initialValidity));
		Thread.sleep(1000);
		this.session.renewSessionLock();
		Instant renewedValidity2 = this.session.getLockedUntilUtc();
		Assert.assertTrue("RenewSessionLock did not renew session lockeduntil time. Before :" + renewedValidity.toString() + ", After:" + renewedValidity2.toString(), renewedValidity2.isAfter(renewedValidity));
    }

    @Test
    public void testGetAndSetState() throws InterruptedException, ServiceBusException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
        byte[] initialState = this.session.getState();
        Assert.assertNull("Session state is not null for a new session", initialState);
        byte[] customState = "Custom Session State".getBytes();
        this.session.setState(customState);
        byte[] updatedState = this.session.getState();
        Assert.assertArrayEquals("Session state not updated properly", customState, updatedState);
        this.session.setState(null);
        updatedState = this.session.getState();
        Assert.assertNull("Session state is not removed by setting a null state", updatedState);
        this.session.setState(customState);
        updatedState = this.session.getState();
        Assert.assertArrayEquals("Session state not updated properly", customState, updatedState);
    }

    @Test
    public void testAcceptSessionTimeoutShouldNotLockSession() throws InterruptedException, ServiceBusException {
        ClientSettings commonClientSettings = TestUtils.getClientSettings();
        // Timeout should be less than default session wait timeout on the service
        ClientSettings shortTimeoutClientSettings = new ClientSettings(commonClientSettings.getTokenProvider(), commonClientSettings.getRetryPolicy(), Duration.ofSeconds(10));
        try {
            this.session = ClientFactory.acceptSessionFromEntityPath(TestUtils.getNamespaceEndpointURI(), this.receiveEntityPath, null, shortTimeoutClientSettings, ReceiveMode.PEEKLOCK);
            Assert.fail("Session " + this.session.getSessionId() + " accepted even though there is no such session on the entity.");
        } catch (TimeoutException te) {
            // Expected..
        }

        // Create session now
        String sessionId = TestUtils.getRandomString();
        Message message = new Message("AMQP message");
        message.setSessionId(sessionId);
        this.sender.send(message);
        this.session = ClientFactory.acceptSessionFromEntityPath(TestUtils.getNamespaceEndpointURI(), this.receiveEntityPath, null, shortTimeoutClientSettings, ReceiveMode.PEEKLOCK);
        Assert.assertEquals("Accepted an unexpceted session.", sessionId, this.session.getSessionId());
    }

    @Test
    public void testRequestResponseLinkRequestLimit() throws InterruptedException, ServiceBusException, ExecutionException {
        int limitToTest = 5000;
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(TestUtils.getNamespaceEndpointURI(), this.receiveEntityPath, sessionId, TestUtils.getClientSettings(), ReceiveMode.PEEKLOCK);
        CompletableFuture[] futures = new CompletableFuture[limitToTest];
        for (int i = 0; i < limitToTest; i++) {
            CompletableFuture<Void> future = this.session.renewSessionLockAsync();
            futures[i] = future;
        }

        CompletableFuture.allOf(futures).get();

        this.session.renewSessionLock();
    }

    private void drainSession() throws InterruptedException, ServiceBusException {
        if (this.session != null) {
            TestCommons.drainAllMessagesFromReceiver(this.session);
            session.setState(null);
        }
    }
}
