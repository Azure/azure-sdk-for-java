package com.microsoft.azure.servicebus;

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
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public abstract class ClientTests extends Tests{
    private static String entityNameCreatedForAllTests = null;
    private static String receiveEntityPathForAllTest = null;
    
    private String entityName;
    protected IMessageSender sendClient;
    protected IMessageAndSessionPump receiveClient;
    private ConnectionStringBuilder sendBuilder;
    private ConnectionStringBuilder receiveBuilder;
    
    @BeforeClass
    public static void init()
    {
        ClientTests.entityNameCreatedForAllTests = null;
        ClientTests.receiveEntityPathForAllTest = null;
    }
    
    @Before
    public void setup() throws InterruptedException, ExecutionException, ServiceBusException, ManagementException
    {
        if(this.shouldCreateEntityForEveryTest() || ClientTests.entityNameCreatedForAllTests == null)
        {
             // Create entity
            this.entityName = TestUtils.randomizeEntityName(this.getEntityNamePrefix());
            ConnectionStringBuilder managementConnectionStringBuilder = new ConnectionStringBuilder(TestUtils.getNamespaceConnectionString());
            if(this.isEntityQueue())
            {
                QueueDescription queueDescription = new QueueDescription(this.entityName);
                queueDescription.setEnablePartitioning(this.isEntityPartitioned());
                EntityManager.createEntity(managementConnectionStringBuilder, queueDescription);
                if(!this.shouldCreateEntityForEveryTest())
                {
                    ClientTests.entityNameCreatedForAllTests = entityName;
                    ClientTests.receiveEntityPathForAllTest = entityName;
                }
                this.sendBuilder = new ConnectionStringBuilder(TestUtils.getNamespaceConnectionString(), this.entityName);
                this.receiveBuilder = this.sendBuilder;
            }
            else
            {
                TopicDescription topicDescription = new TopicDescription(this.entityName);
                topicDescription.setEnablePartitioning(this.isEntityPartitioned());
                EntityManager.createEntity(managementConnectionStringBuilder, topicDescription);
                SubscriptionDescription subDescription = new SubscriptionDescription(this.entityName, TestUtils.FIRST_SUBSCRIPTION_NAME);
                EntityManager.createEntity(managementConnectionStringBuilder, subDescription);
                if(!this.shouldCreateEntityForEveryTest())
                {
                    ClientTests.entityNameCreatedForAllTests = entityName;
                    ClientTests.receiveEntityPathForAllTest = subDescription.getPath();
                }
                this.sendBuilder = new ConnectionStringBuilder(TestUtils.getNamespaceConnectionString(), this.entityName);
                this.receiveBuilder = new ConnectionStringBuilder(TestUtils.getNamespaceConnectionString(), subDescription.getPath());
            }
        }
        else
        {
            this.sendBuilder = new ConnectionStringBuilder(TestUtils.getNamespaceConnectionString(), ClientTests.entityNameCreatedForAllTests);
            this.receiveBuilder = new ConnectionStringBuilder(TestUtils.getNamespaceConnectionString(), ClientTests.receiveEntityPathForAllTest);
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
            ConnectionStringBuilder managementConnectionStringBuilder = new ConnectionStringBuilder(TestUtils.getNamespaceConnectionString());
            EntityManager.deleteEntity(managementConnectionStringBuilder, this.entityName);
        }
        else
        {
            TestCommons.drainAllMessages(this.receiveBuilder);
        }
    }
    
    @AfterClass
    public static void cleanupAfterAllTest() throws ManagementException
    {
        if(ClientTests.entityNameCreatedForAllTests != null)
        {
            ConnectionStringBuilder managementConnectionStringBuilder = new ConnectionStringBuilder(TestUtils.getNamespaceConnectionString());
            EntityManager.deleteEntity(managementConnectionStringBuilder, ClientTests.entityNameCreatedForAllTests);
        }
    }
    
    protected void createClients(ReceiveMode receiveMode) throws InterruptedException, ServiceBusException
    {
        if(this.isEntityQueue())
        {
            this.sendClient = new QueueClient(this.sendBuilder, receiveMode);
            this.receiveClient = (QueueClient)this.sendClient;
        }
        else
        {
            this.sendClient = new TopicClient(this.sendBuilder);
            this.receiveClient = new SubscriptionClient(this.receiveBuilder, receiveMode);
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
