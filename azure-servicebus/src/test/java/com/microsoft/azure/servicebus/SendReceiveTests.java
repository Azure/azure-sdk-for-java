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
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public abstract class SendReceiveTests extends Tests {
    private static String entityNameCreatedForAllTests = null;
    private static String receiveEntityPathForAllTest = null;
	private ConnectionStringBuilder sendBuilder;
	private ConnectionStringBuilder receiveBuilder;
	private MessagingFactory factory;
	private IMessageSender sender;
	private IMessageReceiver receiver;
	private String entityName;
	private final String sessionId = null;
	
	@BeforeClass
    public static void init()
    {
	    SendReceiveTests.entityNameCreatedForAllTests = null;
	    SendReceiveTests.receiveEntityPathForAllTest = null;
    }
	
	@Before
	public void setup() throws InterruptedException, ExecutionException, ServiceBusException, ManagementException
	{
	    if(this.shouldCreateEntityForEveryTest() || SendReceiveTests.entityNameCreatedForAllTests == null)
	    {
	         // Create entity
	        this.entityName = TestUtils.randomizeEntityName(this.getEntityNamePrefix());
	        ConnectionStringBuilder managementConnectionStringBuilder = new ConnectionStringBuilder(TestUtils.getNamespaceConnectionString());
	        if(this.isEntityQueue())
	        {
	            QueueDescription queueDescription = new QueueDescription(this.entityName);
	            queueDescription.setEnablePartitioning(this.isEntityPartitioned());
	            EntityManager.createEntity(managementConnectionStringBuilder, queueDescription);
	            this.sendBuilder = new ConnectionStringBuilder(TestUtils.getNamespaceConnectionString(), this.entityName);
	            this.receiveBuilder = this.sendBuilder;
	            if(!this.shouldCreateEntityForEveryTest())
	            {
	                SendReceiveTests.entityNameCreatedForAllTests = entityName;
	                SendReceiveTests.receiveEntityPathForAllTest = entityName;
	            }
	        }
	        else
	        {
	            TopicDescription topicDescription = new TopicDescription(this.entityName);
                topicDescription.setEnablePartitioning(this.isEntityPartitioned());
                EntityManager.createEntity(managementConnectionStringBuilder, topicDescription);
                this.sendBuilder = new ConnectionStringBuilder(TestUtils.getNamespaceConnectionString(), this.entityName);
                SubscriptionDescription subDescription = new SubscriptionDescription(this.entityName, TestUtils.FIRST_SUBSCRIPTION_NAME);
                EntityManager.createEntity(managementConnectionStringBuilder, subDescription);
                this.receiveBuilder = new ConnectionStringBuilder(TestUtils.getNamespaceConnectionString(), subDescription.getPath());
                if(!this.shouldCreateEntityForEveryTest())
                {
                    SendReceiveTests.entityNameCreatedForAllTests = entityName;
                    SendReceiveTests.receiveEntityPathForAllTest = subDescription.getPath();
                }
	        }
	    }
	    else
	    {
	        this.sendBuilder = new ConnectionStringBuilder(TestUtils.getNamespaceConnectionString(), SendReceiveTests.entityNameCreatedForAllTests);
	        this.receiveBuilder = new ConnectionStringBuilder(TestUtils.getNamespaceConnectionString(), SendReceiveTests.receiveEntityPathForAllTest);
	    }
	    
		this.factory = MessagingFactory.createFromConnectionStringBuilder(this.sendBuilder);
		this.sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(this.sendBuilder);
	}
	
	@After
	public void tearDown() throws ServiceBusException, InterruptedException, ExecutionException, ManagementException
	{
	    if(this.shouldCreateEntityForEveryTest())
	    {
	        ConnectionStringBuilder managementConnectionStringBuilder = new ConnectionStringBuilder(TestUtils.getNamespaceConnectionString());
	        EntityManager.deleteEntity(managementConnectionStringBuilder, this.entityName);
	    }
	    else
	    {
	        this.drainAllMessages();
	    }		
		
		this.sender.close();
		if(this.receiver != null)
			this.receiver.close();
		this.factory.close();
	}
	
	@AfterClass
	public static void cleanupAfterAllTest() throws ManagementException
	{
	    if(SendReceiveTests.entityNameCreatedForAllTests != null)
	    {
	        ConnectionStringBuilder managementConnectionStringBuilder = new ConnectionStringBuilder(TestUtils.getNamespaceConnectionString());
            EntityManager.deleteEntity(managementConnectionStringBuilder, SendReceiveTests.entityNameCreatedForAllTests);
	    }
	}
	
	@Test
	public void testBasicReceiveAndDelete() throws InterruptedException, ServiceBusException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.RECEIVEANDDELETE);
		TestCommons.testBasicReceiveAndDelete(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveBatchAndDelete() throws InterruptedException, ServiceBusException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.RECEIVEANDDELETE);
		TestCommons.testBasicReceiveBatchAndDelete(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveAndComplete() throws InterruptedException, ServiceBusException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveAndComplete(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveAndAbandon() throws InterruptedException, ServiceBusException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveAndAbandon(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveAndDeadLetter() throws InterruptedException, ServiceBusException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveAndDeadLetter(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveAndRenewLock() throws InterruptedException, ServiceBusException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveAndRenewLock(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveAndRenewLockBatch() throws InterruptedException, ServiceBusException, ExecutionException
	{		
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveAndRenewLockBatch(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveBatchAndComplete() throws InterruptedException, ServiceBusException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveBatchAndComplete(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testSendSceduledMessageAndReceive() throws InterruptedException, ServiceBusException
	{	
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.RECEIVEANDDELETE);
		TestCommons.testSendSceduledMessageAndReceive(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testSendSceduledMessageAndCancel() throws InterruptedException, ServiceBusException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.RECEIVEANDDELETE);
		TestCommons.testSendSceduledMessageAndCancel(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testPeekMessage() throws InterruptedException, ServiceBusException
	{		
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, receiveBuilder.getEntityPath(), ReceiveMode.PEEKLOCK);
		TestCommons.testPeekMessage(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testPeekMessageBatch() throws InterruptedException, ServiceBusException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, receiveBuilder.getEntityPath(), ReceiveMode.PEEKLOCK);
		TestCommons.testPeekMessageBatch(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndComplete() throws InterruptedException, ServiceBusException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PEEKLOCK);
		TestCommons.testReceiveBySequenceNumberAndComplete(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndAbandon() throws InterruptedException, ServiceBusException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PEEKLOCK);
		TestCommons.testReceiveBySequenceNumberAndAbandon(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndDefer() throws InterruptedException, ServiceBusException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PEEKLOCK);
		TestCommons.testReceiveBySequenceNumberAndDefer(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndDeadletter() throws InterruptedException, ServiceBusException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PEEKLOCK);
		TestCommons.testReceiveBySequenceNumberAndDeadletter(this.sender, this.sessionId, this.receiver);
	}	
	
	private void drainAllMessages() throws InterruptedException, ServiceBusException
	{
		if(this.receiver != null)
		{
			TestCommons.drainAllMessagesFromReceiver(this.receiver);
		}		
	}	
}
