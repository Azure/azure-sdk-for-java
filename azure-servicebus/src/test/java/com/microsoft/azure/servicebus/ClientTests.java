package com.microsoft.azure.servicebus;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.servicebus.management.EntityManager;
import com.microsoft.azure.servicebus.management.ManagementException;
import com.microsoft.azure.servicebus.management.QueueDescription;
import com.microsoft.azure.servicebus.management.SubscriptionDescription;
import com.microsoft.azure.servicebus.management.TopicDescription;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public abstract class ClientTests extends Tests{
    private static String entityNameCreatedForAllTests = null;
    private static String receiveEntityPathForAllTest = null;
    
    private String entityName;
    private String receiveEntityPath;
    protected IMessageSender sendClient;
    protected IMessageAndSessionPump receiveClient;
    
    @BeforeClass
    public static void init()
    {
        ClientTests.entityNameCreatedForAllTests = null;
        ClientTests.receiveEntityPathForAllTest = null;
    }
    
    @Before
    public void setup() throws InterruptedException, ExecutionException, ServiceBusException, ManagementException
    {
        URI namespaceEndpointURI = TestUtils.getNamespaceEndpointURI();
        ClientSettings clientSettings = TestUtils.getClientSettings();
        
        if(this.shouldCreateEntityForEveryTest() || ClientTests.entityNameCreatedForAllTests == null)
        {
             // Create entity
            this.entityName = TestUtils.randomizeEntityName(this.getEntityNamePrefix());
            if(this.isEntityQueue())
            {
                this.receiveEntityPath = this.entityName;
                QueueDescription queueDescription = new QueueDescription(this.entityName);
                queueDescription.setEnablePartitioning(this.isEntityPartitioned());
                EntityManager.createEntity(namespaceEndpointURI, clientSettings, queueDescription);
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
                EntityManager.createEntity(namespaceEndpointURI, clientSettings, topicDescription);
                SubscriptionDescription subDescription = new SubscriptionDescription(this.entityName, TestUtils.FIRST_SUBSCRIPTION_NAME);
                EntityManager.createEntity(namespaceEndpointURI, clientSettings, subDescription);
                this.receiveEntityPath = subDescription.getPath();
                if(!this.shouldCreateEntityForEveryTest())
                {
                    ClientTests.entityNameCreatedForAllTests = entityName;
                    ClientTests.receiveEntityPathForAllTest = subDescription.getPath();
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
    public void tearDown() throws ServiceBusException, InterruptedException, ExecutionException, ManagementException
    {
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
            EntityManager.deleteEntity(TestUtils.getNamespaceEndpointURI(), TestUtils.getClientSettings(), this.entityName);
        }
        else
        {
            TestCommons.drainAllMessages(this.receiveEntityPath);
        }
    }
    
    @AfterClass
    public static void cleanupAfterAllTest() throws ManagementException
    {
        if(ClientTests.entityNameCreatedForAllTests != null)
        {
            EntityManager.deleteEntity(TestUtils.getNamespaceEndpointURI(), TestUtils.getClientSettings(), ClientTests.entityNameCreatedForAllTests);
        }
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
