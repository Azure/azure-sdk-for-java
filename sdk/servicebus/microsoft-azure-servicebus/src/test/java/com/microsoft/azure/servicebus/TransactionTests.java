package com.microsoft.azure.servicebus;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.servicebus.management.ManagementClientAsync;
import com.microsoft.azure.servicebus.management.QueueDescription;
import com.microsoft.azure.servicebus.management.SubscriptionDescription;
import com.microsoft.azure.servicebus.management.TopicDescription;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public class TransactionTests {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMillis(2000);
    private static final Duration DEFAULT_MESSAGE_TTL = Duration.ofMinutes(1);
    private static final ConnectionStringBuilder builder = new ConnectionStringBuilder("Endpoint=sb://contoso.servicebus.onebox.windows-int.net/;SharedAccessKeyName=DefaultNamespaceSasAllKeyName;SharedAccessKey=8864/auVd3qDC75iTjBL1GJ4D2oXC6bIttRd0jzDZ+g=");
    private static MessagingFactory factory;
    private static ManagementClientAsync managementClient;
    private String guid;
    private TransactionContext transaction;
    private IMessageSender viaEntityTestSender; // Not transacted, used only to send test messages
    private IMessageReceiver viaEntityTestReceiver; // Not transacted, used only to verify messages are received
    private IMessageReceiver destinationEntityTestReceiver; // Not transacted, used only to verify messages are received
    private String viaEntitySendPath;
    private String viaEntityReceivePath;
    private String destinationEntitySendPath;
    private String destinationEntityReceivePath;
    private IMessageSender viaEntitySender;
    private IMessageSender destinationEntitySender;
    private IMessageReceiver viaEntityReceiver;
    
    @BeforeClass
    public static void classInit() throws InterruptedException, ExecutionException {
        managementClient = new ManagementClientAsync(builder);
        factory = MessagingFactory.createFromConnectionStringBuilder(builder);
    }
    
    @Before
    public void testInit() throws InterruptedException, ServiceBusException, ExecutionException {
        guid = UUID.randomUUID().toString().substring(0, 10);
        System.out.println("Creating transaction");
        this.transaction = factory.startTransactionAsync().get();
        System.out.println("Created transaction");
    }

    @AfterClass
    public static void classCleanup() throws IOException, ServiceBusException {
        factory.close();
        managementClient.close();
    }
    
    @Test
    public void viaQueueSenderDestinationQueueCommitTest() throws Exception {
        sendviaTest("viaQueueSenderDestinationQueueSenderCommitTest", false, false, true, true, true, true, true);
    }
    
    @Test
    public void viaQueueSenderDestinationQueueRollbackTest() throws Exception {
        sendviaTest("viaQueueSenderDestinationQueueSenderRollbackTest", false, false, true, true, true, true, false);
    }
    
    @Test
    public void viaQueueReceiverDestinationQueueCommitTest() throws Exception {
        sendviaTest("viaQueueReceiverDestinationQueueCommitTest", false, false, true, true, false, true, true);
    }
    
    @Test
    public void viaQueueReceiverDestinationQueueRollbackTest() throws Exception {
        sendviaTest("viaQueueReceiverDestinationQueueRollbackTest", false, false, true, true, false, true, false);
    }
    
    @Test
    public void viaQueueSenderDestinationTopicCommitTest() throws Exception {
        sendviaTest("viaQueueSenderDestinationTopicCommitTest", false, false, true, false, true, true, true);
    }
    
    @Test
    public void viaQueueSenderDestinationTopicRollbackTest() throws Exception {
        sendviaTest("viaQueueSenderDestinationTopicRollbackTest", false, false, true, false, true, true, false);
    }
    
    @Test
    public void viaQueueReceiverDestinationTopicCommitTest() throws Exception {
        sendviaTest("viaQueueReceiverDestinationTopicCommitTest", false, false, true, false, false, true, true);
    }
    
    @Test
    public void viaQueueReceiverDestinationTopicRollbackTest() throws Exception {
        sendviaTest("viaQueueReceiverDestinationTopicRollbackTest", false, false, true, false, false, true, false);
    }
    
    @Test
    public void viaTopicSenderDestinationQueueCommitTest() throws Exception {
        sendviaTest("viaTopicSenderDestinationQueueCommitTest", false, false, false, true, true, true, true);
    }
    
    @Test
    public void viaTopicSenderDestinationQueueRollbackTest() throws Exception {
        sendviaTest("viaTopicSenderDestinationQueueRollbackTest", false, false, false, true, true, true, false);
    }
    
    @Test
    public void viaTopicReceiverDestinationQueueCommitTest() throws Exception {
        sendviaTest("viaTopicReceiverDestinationQueueCommitTest", false, false, false, true, false, true, true);
    }
    
    @Test
    public void viaTopicReceiverDestinationQueueRollbackTest() throws Exception {
        sendviaTest("viaTopicReceiverDestinationQueueRollbackTest", false, false, false, true, false, true, false);
    }
    
    @Test
    public void viaTopicSenderDestinationTopicCommitTest() throws Exception {
        sendviaTest("viaTopicSenderDestinationTopicCommitTest", false, false, false, false, true, true, true);
    }
    
    @Test
    public void viaTopicSenderDestinationTopicRollbackTest() throws Exception {
        sendviaTest("viaTopicSenderDestinationTopicRollbackTest", false, false, false, false, true, true, false);
    }
    
    @Test
    public void viaTopicReceiverDestinationTopicCommitTest() throws Exception {
        sendviaTest("viaTopicReceiverDestinationTopicCommitTest", false, false, false, false, false, true, true);
    }
    
    @Test
    public void viaTopicReceiverDestinationTopicRollbackTest() throws Exception {
        sendviaTest("viaTopicReceiverDestinationTopicRollbackTest", false, false, false, false, true, true, false);
    }
    
    @Test
    public void viaPartitionedQueueSenderDestinationQueueTest() throws Exception {
        sendviaTest("viaPartitionedQueueSenderDestinationQueueTest", true, false, true, true, true, true, true);
    }
    
    @Test
    public void viaPartitionedTopicSenderDestinationQueueTest() throws Exception {
        sendviaTest("viaPartitionedTopicSenderDestinationQueueTest", true, false, false, true, true, true, true);
    }
    
    @Test
    public void viaPartitionedQueueSenderDestinationTopicTest() throws Exception {
        sendviaTest("viaPartitionedQueueSenderDestinationTopicTest", true, false, true, false, true, true, true);
    }
    
    @Test
    public void viaPartitionedTopicSenderDestinationTopicTest() throws Exception {
        sendviaTest("viaPartitionedTopicSenderDestinationTopicTest", true, false, false, false, true, true, true);
    }
    
    @Test
    public void viaQueueSenderPartitionedDestinationQueueTest() throws Exception {
        sendviaTest("viaQueueSenderPartitionedDestinationQueueTest", false, true, true, true, true, true, true);
    }
    
    @Test
    public void viaTopicSenderPartitionedDestinationQueueTest() throws Exception {
        sendviaTest("viaTopicSenderPartitionedDestinationQueueTest", false, true, false, true, true, true, true);
    }
    
    @Test
    public void viaQueueSenderPartitionedDestinationTopicTest() throws Exception {
        sendviaTest("viaQueueSenderPartitionedDestinationTopicTest", false, true, true, false, true, true, true);
    }
    
    @Test
    public void viaTopicSenderPartitionedDestinationTopicTest() throws Exception {
        sendviaTest("viaTopicSenderPartitionedDestinationTopicTest", false, true, false, false, true, true, true);
    }
    
    @Test
    public void viaPartitionedQueueSenderPartitionedDestinationQueueTest() throws Exception {
        sendviaTest("viaPartitionedQueueSenderPartitionedDestinationQueueTest", true, true, true, true, true, true, true);
    }
    
    @Test
    public void viaPartitionedTopicSenderPartitionedDestinationQueueTest() throws Exception {
        sendviaTest("viaPartitionedTopicSenderPartitionedDestinationQueueTest", true, true, false, true, true, true, true);
    }
    
    @Test
    public void viaPartitionedQueueSenderPartitionedDestinationTopicTest() throws Exception {
        sendviaTest("viaPartitionedQueueSenderPartitionedDestinationTopicTest", true, true, true, false, true, true, true);
    }
    
    @Test
    public void viaPartitionedTopicSenderPartitionedDestinationTopicTest() throws Exception {
        sendviaTest("viaPartitionedTopicSenderPartitionedDestinationTopicTest", true, true, false, false, true, true, true);
    }
    
    @Test
    public void sameSubscriptionsTransactionalReceiversTest() throws Exception {
        sameEntityTransactionalReceiversTest("sameSubscriptionsTransactionalReceiversTest", true);
    }
    
    @Test
    public void differentSubscriptionsOnSameTopicTransactionalReceiversTest() throws Exception {
        sameEntityTransactionalReceiversTest("sameSubscriptionsTransactionalReceiversTest", false);
    }
    
    // Test all scenarios of creating a receiver to the destination destination after the send via entity has been already establshed.
    @Test
    public void DestinationEntityReceiverTest() throws Exception {
        String expectedErrMsg = "Local transactions cannot span multiple top-level entities such as queue or topic.";
        
        for (int i = 0; i < 8; i++) {
            boolean isViaEntityQueue = (i & 4) > 0;
            boolean isDestinationEntityQueue = (i & 2) > 0;
            boolean isViaSender = (i & 1) > 0;
            
            System.out.println("Creating transaction");
            this.transaction = factory.startTransactionAsync().get();
            System.out.println("Created transaction");
            try {
                System.out.println(String.format(
                        "Running DestinationReceiverTest with isViaEntityQueue=%s, isDestinationEntityQueue=%s, isViaSender=%s", 
                        String.valueOf(isViaEntityQueue), 
                        String.valueOf(isDestinationEntityQueue), 
                        String.valueOf(isViaSender)));
                
                sendviaTest("DestinationReceiverTest-" + i, false, false, isViaEntityQueue, isDestinationEntityQueue, isViaSender, false, true);
                fail(String.format("Should have thrown exception saying '%s'", expectedErrMsg));
            } catch (Exception e) {
                assertTrue(String.format("Did not get the expected error message. Expected: %s. Actual: %s", expectedErrMsg, e.getMessage()), e.getMessage().contains(expectedErrMsg));
                System.out.println("Failed with the expected exception.");
            }
        }
    }
    
    private void createEntities(boolean isViaEntityQueue, boolean isDestinationEntityQueue, boolean isViaEntityPartitioned, boolean isDestinationEntityPartitioned) {
        if (isViaEntityQueue) {
            QueueDescription qd = new QueueDescription(this.viaEntitySendPath);
            qd.setDefaultMessageTimeToLive(DEFAULT_MESSAGE_TTL);
            qd.setEnablePartitioning(isViaEntityPartitioned);
            managementClient.createQueueAsync(qd).join();
            this.viaEntityReceivePath = this.viaEntitySendPath;
        } else {
            TopicDescription td = new TopicDescription(this.viaEntitySendPath);
            td.setDefaultMessageTimeToLive(DEFAULT_MESSAGE_TTL);
            td.setEnablePartitioning(isViaEntityPartitioned);
            managementClient.createTopicAsync(td).join();
            SubscriptionDescription sd = managementClient.createSubscriptionAsync(this.viaEntitySendPath, "ViaSubscription-" + guid).join();
            this.viaEntityReceivePath = sd.getPath();
        }
        
        if (isDestinationEntityQueue) {
            QueueDescription qd = new QueueDescription(this.destinationEntitySendPath);
            qd.setDefaultMessageTimeToLive(DEFAULT_MESSAGE_TTL);
            qd.setEnablePartitioning(isDestinationEntityPartitioned);
            managementClient.createQueueAsync(qd).join();
            this.destinationEntityReceivePath = this.destinationEntitySendPath;
        } else {
            TopicDescription td = new TopicDescription(this.destinationEntitySendPath);
            td.setDefaultMessageTimeToLive(DEFAULT_MESSAGE_TTL);
            td.setEnablePartitioning(isDestinationEntityPartitioned);
            managementClient.createTopicAsync(td).join();
            SubscriptionDescription sd = managementClient.createSubscriptionAsync(destinationEntitySendPath, "DestinationSubscription-" + guid).join();
            this.destinationEntityReceivePath = sd.getPath();
        }
    }
    
    // No need to create test sender for destination entities because transacted receivers are not allowed for destination entities
    private void createTestSendersAndReceivers(boolean isViaSender, boolean isDestinationSender) {
        System.out.println("Creating test senders and receivers");
        this.destinationEntityTestReceiver = ClientFactory.createMessageReceiverFromEntityPathAsync(factory, this.destinationEntityReceivePath, ReceiveMode.PEEKLOCK).join();
        this.viaEntityTestReceiver = ClientFactory.createMessageReceiverFromEntityPathAsync(factory, this.viaEntityReceivePath, ReceiveMode.PEEKLOCK).join();
        this.viaEntityTestSender = ClientFactory.createMessageSenderFromEntityPathAsync(factory, this.viaEntitySendPath, null).join();
        System.out.println("Created test senders and receivers");
    }
    
    private void createTransactedSendersAndReceivers(boolean isViaSender, boolean isDestinationSender) {
        if (isViaSender) {
            System.out.println("Creating transacted sender on the via entity");
            this.viaEntitySender = ClientFactory.createTransactedMessageSenderFromEntityPathAsync(factory, this.viaEntitySendPath, this.transaction).join();
            System.out.println("Created transacted sender on the via entity");
        } else {
            System.out.println("Creating transacted receiver on the via entity");
            this.viaEntityReceiver = ClientFactory.createTransactedMessageReceiverFromEntityPathAsync(factory, this.viaEntityReceivePath, this.transaction, ReceiveMode.PEEKLOCK).join();
            System.out.println("Created transacted receiver on the via entity");
        }
        
        if (isDestinationSender) {
            System.out.println("Creating transacted sender on the destination entity");
            this.destinationEntitySender = ClientFactory.createTransactedMessageSenderFromEntityPathAsync(factory, this.destinationEntitySendPath, this.transaction).join();
            System.out.println("Created transacted sender on the destination entity");
        } else {
            System.out.println("Creating transacted receiver on the destination entity. Should fail.");
            ClientFactory.createTransactedMessageReceiverFromEntityPathAsync(factory, this.destinationEntityReceivePath, this.transaction, ReceiveMode.PEEKLOCK).join();
            System.out.println("Created transacted receiver on the destination entity");
        }
    }
    
    public void sameEntityTransactionalReceiversTest(String testName, boolean sameSubscriptions) throws Exception {
        TopicDescription td = new TopicDescription(testName + guid);
        td.setDefaultMessageTimeToLive(DEFAULT_MESSAGE_TTL);
        managementClient.createTopicAsync(td).join();
        SubscriptionDescription sd = managementClient.createSubscriptionAsync(td.getPath(), testName).join();
        SubscriptionDescription sd2 = sameSubscriptions ? sd : managementClient.createSubscriptionAsync(td.getPath(), testName + "2").join();
        
        System.out.println("Creating test sender");
        IMessageSender testSender = ClientFactory.createMessageSenderFromEntityPathAsync(factory, td.getPath(), null).join();
        System.out.println("Creating first receiver");
        IMessageReceiver transactedReceiver = ClientFactory.createTransactedMessageReceiverFromEntityPathAsync(factory, sd.getPath(), this.transaction, ReceiveMode.PEEKLOCK).join();
        System.out.println("Creating second receiver");
        IMessageReceiver transactedReceiver2 = ClientFactory.createTransactedMessageReceiverFromEntityPathAsync(factory, sd2.getPath(), this.transaction, ReceiveMode.PEEKLOCK).join();
        
        com.microsoft.azure.servicebus.Message message = new com.microsoft.azure.servicebus.Message("my message");
        message.setTimeToLive(DEFAULT_MESSAGE_TTL);
        testSender.send(message);
        if (sameSubscriptions) {
            testSender.send(message);
        }
 
        IMessage received = transactedReceiver.receive(DEFAULT_TIMEOUT);
        assertNotNull(received);
        transactedReceiver.complete(received.getLockToken());
        IMessage received2 = transactedReceiver2.receive(DEFAULT_TIMEOUT);
        assertNotNull(received2);
        transactedReceiver2.complete(received2.getLockToken());
        
        System.out.println("Committing transaction");
        this.transaction.commit();
        System.out.println("Committed transaction");
        
        // Just test that there is no more messages
        assertNull(transactedReceiver.peek());
        assertNull(transactedReceiver2.peek());
    }
    
    private void sendviaTest(
            String testName,
            boolean isViaEntityPartitioned,
            boolean isDestinationEntityPartitioned,
            boolean isViaEntityQueue,
            boolean isDestinationEntityQueue,
            boolean isViaSender,
            boolean isDestinationSender,
            boolean isCommit) throws Exception {
        
        this.viaEntitySendPath = testName + "-Via-" + guid;
        this.destinationEntitySendPath = testName + "-Destination-" + guid;
        
        try {
            this.createEntities(isViaEntityQueue, isDestinationEntityQueue, isViaEntityPartitioned, isDestinationEntityPartitioned);
            this.createTestSendersAndReceivers(isViaSender, isDestinationSender);
            this.createTransactedSendersAndReceivers(isViaSender, isDestinationSender);
            
            com.microsoft.azure.servicebus.Message destinationEntityMessage = new com.microsoft.azure.servicebus.Message("message for destination entity");
            com.microsoft.azure.servicebus.Message viaEntityMessage = new com.microsoft.azure.servicebus.Message("message for via entity");
            destinationEntityMessage.setTimeToLive(DEFAULT_MESSAGE_TTL);
            viaEntityMessage.setTimeToLive(DEFAULT_MESSAGE_TTL);
            destinationEntityMessage.setViaPartitionKey(new String(transaction.getTransactionId().array()));  
            viaEntityMessage.setViaPartitionKey(new String(transaction.getTransactionId().array()));

            this.destinationEntitySender.send(destinationEntityMessage);
            if (isViaSender) {
                this.viaEntitySender.send(viaEntityMessage);
            } else {
                this.viaEntityTestSender.send(viaEntityMessage);
                IMessage received = this.viaEntityReceiver.receive(DEFAULT_TIMEOUT);
                assertNotNull("The via entity did not receive the message.", received);
                this.viaEntityReceiver.complete(received.getLockToken());
            }
            
            if (isCommit) {
                System.out.println("Committing transaction");
                this.transaction.commit();
                System.out.println("Committed transaction");
                
                assertNotNull("Should have received message from destination entity.", this.destinationEntityTestReceiver.receive(DEFAULT_TIMEOUT));
                if (isViaSender) {
                    assertNotNull("Should have received message from via entity.", this.viaEntityTestReceiver.receive(DEFAULT_TIMEOUT));
                } else {
                    assertNull("Should not have received message from via entity.", this.viaEntityTestReceiver.receive(DEFAULT_TIMEOUT));
                }
            } else {
                System.out.println("Rolling back transaction");
                this.transaction.rollback();
                System.out.println("Rolled back transaction");
                
                assertNull("Should not have received message from destination entity.", this.destinationEntityTestReceiver.receive(DEFAULT_TIMEOUT));
                if (isViaSender) {
                    assertNull("Should not have received message from via entity.", this.viaEntityTestReceiver.receive(DEFAULT_TIMEOUT));
                } else {
                    assertNotNull("Should have received message from via entity.", this.viaEntityTestReceiver.peek());
                }
            }
        } finally {
            // No need to wait for the delete CompletableFutures, if exception happens nothing we can do about them.
            if (isViaEntityQueue) {
                managementClient.deleteQueueAsync(this.viaEntitySendPath);
            } else {
                managementClient.deleteTopicAsync(this.viaEntitySendPath);
            }
            
            if (isDestinationEntityQueue) {
                managementClient.deleteQueueAsync(this.destinationEntitySendPath);
            } else {
                managementClient.deleteQueueAsync(this.destinationEntitySendPath);
            }
        }
    }
}
