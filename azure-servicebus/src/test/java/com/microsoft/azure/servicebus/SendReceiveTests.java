package com.microsoft.azure.servicebus;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.transaction.Discharge;
import org.junit.*;

import com.microsoft.azure.servicebus.management.EntityManager;
import com.microsoft.azure.servicebus.management.ManagementException;
import com.microsoft.azure.servicebus.management.QueueDescription;
import com.microsoft.azure.servicebus.management.SubscriptionDescription;
import com.microsoft.azure.servicebus.management.TopicDescription;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public abstract class SendReceiveTests extends Tests {
    private static String entityNameCreatedForAllTests = null;
    private static String receiveEntityPathForAllTest = null;
    
	protected MessagingFactory factory;
	protected IMessageSender sender;
	protected IMessageReceiver receiver;
	protected String entityName;
	protected final String sessionId = null;
	protected String receiveEntityPath;
	
	@BeforeClass
    public static void init()
    {
	    SendReceiveTests.entityNameCreatedForAllTests = null;
	    SendReceiveTests.receiveEntityPathForAllTest = null;
    }
	
	@Before
	public void setup() throws InterruptedException, ExecutionException, ServiceBusException, ManagementException
	{
	    URI namespaceEndpointURI = TestUtils.getNamespaceEndpointURI();
        ClientSettings managementClientSettings = TestUtils.getManagementClientSettings();
        
	    if(this.shouldCreateEntityForEveryTest() || SendReceiveTests.entityNameCreatedForAllTests == null)
	    {
	         // Create entity
	        this.entityName = TestUtils.randomizeEntityName(this.getEntityNamePrefix());
	        if(this.isEntityQueue())
	        {
	            this.receiveEntityPath = this.entityName;
	            QueueDescription queueDescription = new QueueDescription(this.entityName);
	            queueDescription.setEnablePartitioning(this.isEntityPartitioned());
	            EntityManager.createEntity(namespaceEndpointURI, managementClientSettings, queueDescription);
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
                EntityManager.createEntity(namespaceEndpointURI, managementClientSettings, topicDescription);
                SubscriptionDescription subDescription = new SubscriptionDescription(this.entityName, TestUtils.FIRST_SUBSCRIPTION_NAME);
                EntityManager.createEntity(namespaceEndpointURI, managementClientSettings, subDescription);
                this.receiveEntityPath = subDescription.getPath();
                if(!this.shouldCreateEntityForEveryTest())
                {
                    SendReceiveTests.entityNameCreatedForAllTests = entityName;
                    SendReceiveTests.receiveEntityPathForAllTest = subDescription.getPath();
                }
	        }
	    }
	    else
	    {
	        this.entityName = SendReceiveTests.entityNameCreatedForAllTests;
            this.receiveEntityPath = SendReceiveTests.receiveEntityPathForAllTest;
	    }
	    
	    this.factory = MessagingFactory.createFromNamespaceEndpointURI(namespaceEndpointURI, TestUtils.getClientSettings());
        this.sender = ClientFactory.createMessageSenderFromEntityPath(this.factory, this.entityName);
	}
	
	@After
	public void tearDown() throws ServiceBusException, InterruptedException, ExecutionException, ManagementException
	{
	    if(!this.shouldCreateEntityForEveryTest())
	    {
	        this.drainAllMessages();
	    }
		
		this.sender.close();
		if(this.receiver != null)
			this.receiver.close();
		this.factory.close();
		
		if(this.shouldCreateEntityForEveryTest())
        {
		    EntityManager.deleteEntity(TestUtils.getNamespaceEndpointURI(), TestUtils.getManagementClientSettings(), this.entityName);
        }
	}
	
	@AfterClass
	public static void cleanupAfterAllTest() throws ManagementException
	{
	    if(SendReceiveTests.entityNameCreatedForAllTests != null)
	    {
	        EntityManager.deleteEntity(TestUtils.getNamespaceEndpointURI(), TestUtils.getManagementClientSettings(), SendReceiveTests.entityNameCreatedForAllTests);
	    }
	}

	@Test
	public void testBasicReceiveAndDelete() throws InterruptedException, ServiceBusException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.RECEIVEANDDELETE);
		TestCommons.testBasicReceiveAndDelete(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveBatchAndDelete() throws InterruptedException, ServiceBusException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.RECEIVEANDDELETE);
		TestCommons.testBasicReceiveBatchAndDelete(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveAndComplete() throws InterruptedException, ServiceBusException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveAndComplete(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveAndAbandon() throws InterruptedException, ServiceBusException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveAndAbandon(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveAndDeadLetter() throws InterruptedException, ServiceBusException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveAndDeadLetter(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveAndRenewLock() throws InterruptedException, ServiceBusException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveAndRenewLock(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveAndRenewLockBatch() throws InterruptedException, ServiceBusException, ExecutionException
	{		
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveAndRenewLockBatch(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveBatchAndComplete() throws InterruptedException, ServiceBusException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveBatchAndComplete(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testSendSceduledMessageAndReceive() throws InterruptedException, ServiceBusException
	{	
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.RECEIVEANDDELETE);
		TestCommons.testSendSceduledMessageAndReceive(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testSendSceduledMessageAndCancel() throws InterruptedException, ServiceBusException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.RECEIVEANDDELETE);
		TestCommons.testSendSceduledMessageAndCancel(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testPeekMessage() throws InterruptedException, ServiceBusException
	{		
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
		TestCommons.testPeekMessage(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testPeekMessageBatch() throws InterruptedException, ServiceBusException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
		TestCommons.testPeekMessageBatch(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndComplete() throws InterruptedException, ServiceBusException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
		TestCommons.testReceiveBySequenceNumberAndComplete(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndAbandon() throws InterruptedException, ServiceBusException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
		TestCommons.testReceiveBySequenceNumberAndAbandon(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndDefer() throws InterruptedException, ServiceBusException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
		TestCommons.testReceiveBySequenceNumberAndDefer(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndDeadletter() throws InterruptedException, ServiceBusException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);
		TestCommons.testReceiveBySequenceNumberAndDeadletter(this.sender, this.sessionId, this.receiver);
	}	
	
	@Test
	public void testSendReceiveMessageWithVariousPropertyTypes() throws InterruptedException, ServiceBusException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.RECEIVEANDDELETE);
		TestCommons.testSendReceiveMessageWithVariousPropertyTypes(this.sender, this.sessionId, this.receiver);
	}
	
	private void drainAllMessages() throws InterruptedException, ServiceBusException
	{
		if(this.receiver != null)
		{
			TestCommons.drainAllMessagesFromReceiver(this.receiver);
		}
	}	
}
