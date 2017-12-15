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

public abstract class ClientSessionTests extends Tests
{
    private static String entityNameCreatedForAllTests = null;
    private static String receiveEntityPathForAllTest = null;
    
    private String entityName;
    private String receiveEntityPath;
    private IMessageSender sendClient;
    private IMessageAndSessionPump receiveClient;
    
    @BeforeClass
    public static void init()
    {
        ClientSessionTests.entityNameCreatedForAllTests = null;
        ClientSessionTests.receiveEntityPathForAllTest = null;
    }
    
    @Before
    public void setup() throws InterruptedException, ExecutionException, ServiceBusException, ManagementException
    {
        URI namespaceEndpointURI = TestUtils.getNamespaceEndpointURI();
        ClientSettings managementClientSettings = TestUtils.getManagementClientSettings();
        
        if(this.shouldCreateEntityForEveryTest() || ClientSessionTests.entityNameCreatedForAllTests == null)
        {
             // Create entity
            this.entityName = TestUtils.randomizeEntityName(this.getEntityNamePrefix());
            if(this.isEntityQueue())
            {
                this.receiveEntityPath = this.entityName;
                QueueDescription queueDescription = new QueueDescription(this.entityName);
                queueDescription.setEnablePartitioning(this.isEntityPartitioned());
                queueDescription.setRequiresSession(true);
                EntityManager.createEntity(namespaceEndpointURI, managementClientSettings, queueDescription);
                if(!this.shouldCreateEntityForEveryTest())
                {
                    ClientSessionTests.entityNameCreatedForAllTests = entityName;
                    ClientSessionTests.receiveEntityPathForAllTest = entityName;
                }
            }
            else
            {
                TopicDescription topicDescription = new TopicDescription(this.entityName);
                topicDescription.setEnablePartitioning(this.isEntityPartitioned());
                EntityManager.createEntity(namespaceEndpointURI, managementClientSettings, topicDescription);
                SubscriptionDescription subDescription = new SubscriptionDescription(this.entityName, TestUtils.FIRST_SUBSCRIPTION_NAME);
                subDescription.setRequiresSession(true);
                EntityManager.createEntity(namespaceEndpointURI, managementClientSettings, subDescription);
                this.receiveEntityPath = subDescription.getPath();
                if(!this.shouldCreateEntityForEveryTest())
                {
                    ClientSessionTests.entityNameCreatedForAllTests = entityName;
                    ClientSessionTests.receiveEntityPathForAllTest = subDescription.getPath();
                }
            }
        }
        else
        {
            this.entityName = ClientSessionTests.entityNameCreatedForAllTests;
            this.receiveEntityPath = ClientSessionTests.receiveEntityPathForAllTest;
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
            EntityManager.deleteEntity(TestUtils.getNamespaceEndpointURI(), TestUtils.getManagementClientSettings(), this.entityName);
        }
        else
        {
            TestCommons.drainAllSessions(this.receiveEntityPath, this.isEntityQueue());
        }
    }
    
    @AfterClass
    public static void cleanupAfterAllTest() throws ManagementException
    {
        if(ClientSessionTests.entityNameCreatedForAllTests != null)
        {
            EntityManager.deleteEntity(TestUtils.getNamespaceEndpointURI(), TestUtils.getManagementClientSettings(), ClientSessionTests.entityNameCreatedForAllTests);
        }
    }
    
    private void createClients(ReceiveMode receiveMode) throws InterruptedException, ServiceBusException
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
    public void testRegisterAnotherHandlerAfterSessionHandler() throws InterruptedException, ServiceBusException
    {
        this.createClients(ReceiveMode.PEEKLOCK);
        MessageAndSessionPumpTests.testRegisterAnotherHandlerAfterSessionHandler(this.receiveClient);
    }
    
    @Test
    public void testGetMessageSessions() throws InterruptedException, ServiceBusException
    {
        this.createClients(ReceiveMode.PEEKLOCK);
        TestCommons.testGetMessageSessions(this.sendClient, this.receiveClient);
    }
    
    @Test
    public void testSessionPumpAutoCompleteWithOneConcurrentCallPerSession() throws InterruptedException, ServiceBusException
    {
        this.createClients(ReceiveMode.PEEKLOCK);
        MessageAndSessionPumpTests.testSessionPumpAutoCompleteWithOneConcurrentCallPerSession(this.sendClient, this.receiveClient);
    }
    
    @Test
    public void testReceiveAndDeleteSessionPump() throws InterruptedException, ServiceBusException
    {
        this.createClients(ReceiveMode.RECEIVEANDDELETE);
        MessageAndSessionPumpTests.testSessionPumpAutoCompleteWithOneConcurrentCallPerSession(this.sendClient, this.receiveClient);
    }
    
    @Test
    public void testSessionPumpAutoCompleteWithMultipleConcurrentCallsPerSession() throws InterruptedException, ServiceBusException
    {
        this.createClients(ReceiveMode.PEEKLOCK);
        MessageAndSessionPumpTests.testSessionPumpAutoCompleteWithMultipleConcurrentCallsPerSession(this.sendClient, this.receiveClient);
    }
    
    @Test
    public void testSessionPumpClientComplete() throws InterruptedException, ServiceBusException
    {
        this.createClients(ReceiveMode.PEEKLOCK);
        MessageAndSessionPumpTests.testSessionPumpClientComplete(this.sendClient, this.receiveClient);
    }
    
    @Test
    public void testSessionPumpAbandonOnException() throws InterruptedException, ServiceBusException
    {
        this.createClients(ReceiveMode.PEEKLOCK);
        MessageAndSessionPumpTests.testSessionPumpAbandonOnException(this.sendClient, this.receiveClient);
    }
    
    @Test
    public void testSessionPumpRenewLock() throws InterruptedException, ServiceBusException
    {
        this.createClients(ReceiveMode.PEEKLOCK);
        MessageAndSessionPumpTests.testSessionPumpRenewLock(this.sendClient, this.receiveClient);
    }
}
