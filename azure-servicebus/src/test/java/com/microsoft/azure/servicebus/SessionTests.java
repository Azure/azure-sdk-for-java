package com.microsoft.azure.servicebus;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.servicebus.management.EntityManager;
import com.microsoft.azure.servicebus.management.ManagementException;
import com.microsoft.azure.servicebus.management.QueueDescription;
import com.microsoft.azure.servicebus.management.SubscriptionDescription;
import com.microsoft.azure.servicebus.management.TopicDescription;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.TimeoutException;

public abstract class SessionTests extends Tests {
    private static String entityNameCreatedForAllTests = null;
    private static String receiveEntityPathForAllTest = null;
	
	protected MessagingFactory factory;
	private IMessageSender sender;
	private IMessageSession session;
	private String entityName;
	private String receiveEntityPath;
	
	@BeforeClass
	public static void init()
	{
	    Logger.getLogger("com.microsoft.azure.servicebus").setLevel(Level.OFF);
	    SessionTests.entityNameCreatedForAllTests = null;
	    SessionTests.receiveEntityPathForAllTest = null;
	}
	
	@Before
	public void setup() throws InterruptedException, ExecutionException, ServiceBusException, ManagementException
	{
	    URI namespaceEndpointURI = TestUtils.getNamespaceEndpointURI();
        ClientSettings clientSettings = TestUtils.getClientSettings();
        
	    if(this.shouldCreateEntityForEveryTest() || SessionTests.entityNameCreatedForAllTests == null)
        {
             // Create entity
            this.entityName = TestUtils.randomizeEntityName(this.getEntityNamePrefix());
            if(this.isEntityQueue())
            {
                this.receiveEntityPath = this.entityName;
                QueueDescription queueDescription = new QueueDescription(this.entityName);
                queueDescription.setEnablePartitioning(this.isEntityPartitioned());
                queueDescription.setRequiresSession(true);
                EntityManager.createEntity(namespaceEndpointURI, clientSettings, queueDescription);
                if(!this.shouldCreateEntityForEveryTest())
                {
                    SessionTests.entityNameCreatedForAllTests = entityName;
                    SessionTests.receiveEntityPathForAllTest = entityName;
                }
            }
            else
            {
                TopicDescription topicDescription = new TopicDescription(this.entityName);
                topicDescription.setEnablePartitioning(this.isEntityPartitioned());
                EntityManager.createEntity(namespaceEndpointURI, clientSettings, topicDescription);
                SubscriptionDescription subDescription = new SubscriptionDescription(this.entityName, TestUtils.FIRST_SUBSCRIPTION_NAME);
                subDescription.setRequiresSession(true);
                EntityManager.createEntity(namespaceEndpointURI, clientSettings, subDescription);
                this.receiveEntityPath = subDescription.getPath();
                if(!this.shouldCreateEntityForEveryTest())
                {
                    SessionTests.entityNameCreatedForAllTests = entityName;
                    SessionTests.receiveEntityPathForAllTest = subDescription.getPath();
                }
            }
        }
        else
        {
            this.entityName = SessionTests.entityNameCreatedForAllTests;
            this.receiveEntityPath = SessionTests.receiveEntityPathForAllTest;
        }
        
        this.factory = MessagingFactory.createFromNamespaceEndpointURI(namespaceEndpointURI, clientSettings);
        this.sender = ClientFactory.createMessageSenderFromEntityPath(namespaceEndpointURI, this.entityName, clientSettings);
	}
	
	@After
	public void tearDown() throws ServiceBusException, InterruptedException, ManagementException
	{
		if(!this.shouldCreateEntityForEveryTest())
        {
		    this.drainSession();
        }
		
		this.sender.close();
        if(this.session != null)
            this.session.close();
        this.factory.close();
        
        if(this.shouldCreateEntityForEveryTest())
        {
            EntityManager.deleteEntity(TestUtils.getNamespaceEndpointURI(), TestUtils.getClientSettings(), this.entityName);
        }
	}
	
    @AfterClass
    public static void cleanupAfterAllTest() throws ManagementException
    {
        if(SessionTests.entityNameCreatedForAllTests != null)
        {
            EntityManager.deleteEntity(TestUtils.getNamespaceEndpointURI(), TestUtils.getClientSettings(), SessionTests.entityNameCreatedForAllTests);
        }
    }
    
	@Test
	public void testBasicReceiveAndDelete() throws InterruptedException, ServiceBusException, ExecutionException
	{
		String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.RECEIVEANDDELETE);
		TestCommons.testBasicReceiveAndDelete(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testBasicReceiveBatchAndDelete() throws InterruptedException, ServiceBusException, ExecutionException
	{
		String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.RECEIVEANDDELETE);
		TestCommons.testBasicReceiveBatchAndDelete(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testBasicReceiveAndComplete() throws InterruptedException, ServiceBusException, ExecutionException
	{
		String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveAndComplete(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testBasicReceiveAndAbandon() throws InterruptedException, ServiceBusException, ExecutionException
	{
		String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveAndAbandon(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testBasicReceiveAndDeadLetter() throws InterruptedException, ServiceBusException, ExecutionException
	{
		String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveAndDeadLetter(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testBasicReceiveAndRenewLock() throws InterruptedException, ServiceBusException, ExecutionException
	{
		String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveAndRenewLock(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testBasicReceiveAndRenewLockBatch() throws InterruptedException, ServiceBusException, ExecutionException
	{
		String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveAndRenewLockBatch(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testBasicReceiveBatchAndComplete() throws InterruptedException, ServiceBusException, ExecutionException
	{
		String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
		TestCommons.testBasicReceiveBatchAndComplete(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testSendSceduledMessageAndReceive() throws InterruptedException, ServiceBusException
	{
		String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.RECEIVEANDDELETE);
		TestCommons.testSendSceduledMessageAndReceive(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testSendSceduledMessageAndCancel() throws InterruptedException, ServiceBusException
	{
		String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.RECEIVEANDDELETE);
		TestCommons.testSendSceduledMessageAndCancel(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testPeekMessage() throws InterruptedException, ServiceBusException
	{		
		String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
		TestCommons.testPeekMessage(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testPeekMessageBatch() throws InterruptedException, ServiceBusException
	{
		String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
		TestCommons.testPeekMessageBatch(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndComplete() throws InterruptedException, ServiceBusException
	{
		String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
		TestCommons.testReceiveBySequenceNumberAndComplete(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndAbandon() throws InterruptedException, ServiceBusException
	{
		String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
		TestCommons.testReceiveBySequenceNumberAndAbandon(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndDefer() throws InterruptedException, ServiceBusException
	{
		String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
		TestCommons.testReceiveBySequenceNumberAndDefer(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndDeadletter() throws InterruptedException, ServiceBusException
	{
		String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
		TestCommons.testReceiveBySequenceNumberAndDeadletter(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testAcceptAnySession() throws InterruptedException, ServiceBusException
	{
		String sessionId = TestUtils.getRandomString();
		String messageId = TestUtils.getRandomString();
		Message message = new Message("AMQP message");
		message.setMessageId(messageId);
		if(sessionId != null)
		{
			message.setSessionId(sessionId);
		}
		this.sender.send(message);
		
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, null, ReceiveMode.PEEKLOCK);
		Assert.assertNotNull("Did not receive a session", this.session);
	}
	
	@Test
	public void testRenewSessionLock() throws InterruptedException, ServiceBusException
	{
		String sessionId = TestUtils.getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);
		Instant initialValidity = this.session.getLockedUntilUtc();
		this.session.renewSessionLock();
		Instant renewedValidity = this.session.getLockedUntilUtc();
		Assert.assertTrue("RenewSessionLock did not renew session lockeduntil time.", renewedValidity.isAfter(initialValidity));
		this.session.renewSessionLock();
		Instant renewedValidity2 = this.session.getLockedUntilUtc();
		Assert.assertTrue("RenewSessionLock did not renew session lockeduntil time.", renewedValidity2.isAfter(renewedValidity));
	}
		
	@Test
	public void testGetAndSetState() throws InterruptedException, ServiceBusException
	{
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
	public void testAcceptSessionTimeoutShouldNotLockSession() throws InterruptedException, ServiceBusException
	{
	    ClientSettings commonClientSettings = TestUtils.getClientSettings();
	    // Timeout should be less than default session wait timeout on the service
	    ClientSettings shortTimeoutClientSettings = new ClientSettings(commonClientSettings.getTokenProvider(), commonClientSettings.getRetryPolicy(), Duration.ofSeconds(10));
	    try
	    {
	        this.session = ClientFactory.acceptSessionFromEntityPath(TestUtils.getNamespaceEndpointURI(), this.receiveEntityPath, null, shortTimeoutClientSettings, ReceiveMode.PEEKLOCK);
	        Assert.fail("Session " + this.session.getSessionId() + " accepted even though there is no such session on the entity.");
	    }
	    catch(TimeoutException te)
	    {
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
    public void testRequestResponseLinkRequestLimit() throws InterruptedException, ServiceBusException
    {	    
	    int limitToTest = 5000;
	    String sessionId = TestUtils.getRandomString();
	    this.session = ClientFactory.acceptSessionFromEntityPath(TestUtils.getNamespaceEndpointURI(), this.receiveEntityPath, sessionId, TestUtils.getClientSettings(), ReceiveMode.PEEKLOCK);
	    for(int i=0; i<limitToTest; i++)
	    {
	        this.session.renewSessionLock();
	    }
	    
	    this.session.renewSessionLock();
    }
	
	private void drainSession() throws InterruptedException, ServiceBusException
	{
		if(this.session != null)
		{
			TestCommons.drainAllMessagesFromReceiver(this.session);
			session.setState(null);
		}
	}	
}
