package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.servicebus.management.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public abstract class ClientTests extends Tests{
    private static String entityNameCreatedForAllTests = null;
    private static String receiveEntityPathForAllTest = null;
    protected static ManagementClientAsync managementClientAsync;

    private String entityName;
    private String receiveEntityPath;
    protected IMessageSender sendClient;
    protected IMessageAndSessionPump receiveClient;
    
    @BeforeClass
    public static void init()
    {
        ClientTests.entityNameCreatedForAllTests = null;
        ClientTests.receiveEntityPathForAllTest = null;
        URI namespaceEndpointURI = TestUtils.getNamespaceEndpointURI();
        ClientSettings managementClientSettings = TestUtils.getManagementClientSettings();
        managementClientAsync = new ManagementClientAsync(namespaceEndpointURI, managementClientSettings);
    }
    
    @Before
    public void setup() throws ExecutionException, InterruptedException {
        if(this.shouldCreateEntityForEveryTest() || ClientTests.entityNameCreatedForAllTests == null)
        {
             // Create entity
            this.entityName = TestUtils.randomizeEntityName(this.getEntityNamePrefix());
            if(this.isEntityQueue())
            {
                this.receiveEntityPath = this.entityName;
                QueueDescription queueDescription = new QueueDescription(this.entityName);
                queueDescription.setEnablePartitioning(this.isEntityPartitioned());
                managementClientAsync.createQueueAsync(queueDescription).get();
                if(!this.shouldCreateEntityForEveryTest())
                {
                    ClientTests.entityNameCreatedForAllTests = entityName;
                    ClientTests.receiveEntityPathForAllTest = entityName;
                }
            }
            else
            {
                TopicDescription topicDescription = new TopicDescription(this.entityName);
                topicDescription.setEnablePartitioning(this.isEntityPartitioned());
                managementClientAsync.createTopicAsync(topicDescription).get();
                SubscriptionDescription subDescription = new SubscriptionDescription(this.entityName, TestUtils.FIRST_SUBSCRIPTION_NAME);
                managementClientAsync.createSubscriptionAsync(subDescription).get();
                this.receiveEntityPath = EntityNameHelper.formatSubscriptionPath(subDescription.getTopicPath(), subDescription.getSubscriptionName());
                if(!this.shouldCreateEntityForEveryTest())
                {
                    ClientTests.entityNameCreatedForAllTests = entityName;
                    ClientTests.receiveEntityPathForAllTest = this.receiveEntityPath;
                }
            }
        }
        else
        {
            this.entityName = ClientTests.entityNameCreatedForAllTests;
            this.receiveEntityPath = ClientTests.receiveEntityPathForAllTest;
        }
    }
    
    @After
    public void tearDown() throws ServiceBusException, InterruptedException, ExecutionException {
        if(this.sendClient != null)
        {
            this.sendClient.close();
        }
        if(this.receiveClient != null)
        {
            if(this.receiveClient instanceof SubscriptionClient)
            {
                ((SubscriptionClient)this.receiveClient).close();
            }
            else
            {
                ((QueueClient)this.receiveClient).close();
            }
        }
        
        if(this.shouldCreateEntityForEveryTest())
        {
            managementClientAsync.deleteQueueAsync(this.entityName).get();
        }
        else
        {
            TestCommons.drainAllMessages(this.receiveEntityPath);
        }
    }
    
    @AfterClass
    public static void cleanupAfterAllTest() throws ExecutionException, InterruptedException, IOException {
        if(ClientTests.entityNameCreatedForAllTests != null)
        {
            managementClientAsync.deleteQueueAsync(ClientTests.entityNameCreatedForAllTests).get();
        }

        managementClientAsync.close();
    }
    
    protected void createClients(ReceiveMode receiveMode) throws InterruptedException, ServiceBusException
    {
        if(this.isEntityQueue())
        {
            this.sendClient = new QueueClient(TestUtils.getNamespaceEndpointURI(), this.entityName, TestUtils.getClientSettings(), receiveMode);
            this.receiveClient = (QueueClient)this.sendClient;
        }
        else
        {
            this.sendClient = new TopicClient(TestUtils.getNamespaceEndpointURI(), this.entityName, TestUtils.getClientSettings());
            this.receiveClient = new SubscriptionClient(TestUtils.getNamespaceEndpointURI(), this.receiveEntityPath, TestUtils.getClientSettings(), receiveMode);
        }
    }
    
    @Test
    public void testMessagePumpAutoComplete() throws InterruptedException, ServiceBusException
    {
        this.createClients(ReceiveMode.PEEKLOCK);
        MessageAndSessionPumpTests.testMessagePumpAutoComplete(this.sendClient, this.receiveClient);
    }
    
    @Test
    public void testReceiveAndDeleteMessagePump() throws InterruptedException, ServiceBusException
    {
        this.createClients(ReceiveMode.RECEIVEANDDELETE);
        MessageAndSessionPumpTests.testMessagePumpAutoComplete(this.sendClient, this.receiveClient);
    }
    
    @Test
    public void testMessagePumpClientComplete() throws InterruptedException, ServiceBusException
    {
        this.createClients(ReceiveMode.PEEKLOCK);
        MessageAndSessionPumpTests.testMessagePumpClientComplete(this.sendClient, this.receiveClient);
    }
    
    @Test
    public void testMessagePumpAbandonOnException() throws InterruptedException, ServiceBusException
    {
        this.createClients(ReceiveMode.PEEKLOCK);
        MessageAndSessionPumpTests.testMessagePumpAbandonOnException(this.sendClient, this.receiveClient);
    }
    
    @Test
    public void testMessagePumpRenewLock() throws InterruptedException, ServiceBusException
    {
        this.createClients(ReceiveMode.PEEKLOCK);
        MessageAndSessionPumpTests.testMessagePumpRenewLock(this.sendClient, this.receiveClient);
    }
    
    @Test
    public void testRegisterAnotherHandlerAfterMessageHandler() throws InterruptedException, ServiceBusException
    {
        this.createClients(ReceiveMode.PEEKLOCK);
        MessageAndSessionPumpTests.testRegisterAnotherHandlerAfterMessageHandler(this.receiveClient);
    }
}
