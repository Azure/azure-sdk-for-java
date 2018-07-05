package com.microsoft.azure.servicebus;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import com.microsoft.azure.servicebus.management.EntityManager;
import com.microsoft.azure.servicebus.management.ManagementException;
import com.microsoft.azure.servicebus.management.QueueDescription;
import com.microsoft.azure.servicebus.management.SubscriptionDescription;
import com.microsoft.azure.servicebus.management.TopicDescription;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public class ClientValidationTests
{
	
	private static final String ENTITY_NAME_PREFIX = "ClientValidationTests";
	
	private static String queuePath;
	private static String sessionfulQueuePath;
	private static String topicPath;
	private static String subscriptionPath;
	private static String sessionfulSubscriptionPath;	
	
	@BeforeClass
    public static void createEntities() throws ManagementException
    {
		// Create a queue, a topic and a subscription
		queuePath = TestUtils.randomizeEntityName(ENTITY_NAME_PREFIX);
		sessionfulQueuePath = TestUtils.randomizeEntityName(ENTITY_NAME_PREFIX);
		topicPath = TestUtils.randomizeEntityName(ENTITY_NAME_PREFIX);
		URI namespaceEndpointURI = TestUtils.getNamespaceEndpointURI();
		ClientSettings managementClientSettings = TestUtils.getManagementClientSettings();
		
		QueueDescription queueDescription = new QueueDescription(queuePath);
		queueDescription.setEnablePartitioning(false);
		EntityManager.createEntity(namespaceEndpointURI, managementClientSettings, queueDescription);
		
		QueueDescription queueDescription2 = new QueueDescription(sessionfulQueuePath);
		queueDescription2.setEnablePartitioning(false);
		queueDescription2.setRequiresSession(true);
		EntityManager.createEntity(namespaceEndpointURI, managementClientSettings, queueDescription2);
		
		TopicDescription topicDescription = new TopicDescription(topicPath);
		topicDescription.setEnablePartitioning(false);
		EntityManager.createEntity(namespaceEndpointURI, managementClientSettings, topicDescription);
		SubscriptionDescription subDescription = new SubscriptionDescription(topicPath, TestUtils.FIRST_SUBSCRIPTION_NAME);
		subscriptionPath = subDescription.getPath();
        EntityManager.createEntity(namespaceEndpointURI, managementClientSettings, subDescription);
        SubscriptionDescription subDescription2 = new SubscriptionDescription(topicPath, "subscription2");
        subDescription2.setRequiresSession(true);
		sessionfulSubscriptionPath = subDescription2.getPath();
        EntityManager.createEntity(namespaceEndpointURI, managementClientSettings, subDescription2);
    }
	
	@AfterClass
	public static void deleteEntities() throws ManagementException
	{
		EntityManager.deleteEntity(TestUtils.getNamespaceEndpointURI(), TestUtils.getManagementClientSettings(), queuePath);
		EntityManager.deleteEntity(TestUtils.getNamespaceEndpointURI(), TestUtils.getManagementClientSettings(), sessionfulQueuePath);
		EntityManager.deleteEntity(TestUtils.getNamespaceEndpointURI(), TestUtils.getManagementClientSettings(), topicPath);
	}
	
	@Test
	public void testTopicClientCreationToQueue() throws InterruptedException, ServiceBusException
	{				
		try
		{
			TopicClient tc = new TopicClient(TestUtils.getNamespaceEndpointURI(), queuePath, TestUtils.getManagementClientSettings());
			try
			{
				Message msg = new Message("test message");
				tc.send(msg);
				Assert.fail("TopicClient created to a queue which shouldn't be allowed.");
			}
			finally
			{
				tc.close();
			}						
		} catch (UnsupportedOperationException e) {
			// Expected
		}
	}
	
	@Test
	public void testQueueClientCreationToTopic() throws InterruptedException, ServiceBusException
	{				
		try {
			QueueClient qc = new QueueClient(TestUtils.getNamespaceEndpointURI(), topicPath, TestUtils.getManagementClientSettings(), ReceiveMode.PEEKLOCK);
			try {
				Message msg = new Message("test message");
				qc.send(msg);
				Assert.fail("QueueClient created to a topic which shouldn't be allowed.");
			}
			finally {
				qc.close();
			}			
		} catch (UnsupportedOperationException e) {
			// Expected
		}
	}
	
	@Test
	public void testQueueClientCreationToSubscription() throws InterruptedException, ServiceBusException
	{				
		try {
			QueueClient qc = new QueueClient(TestUtils.getNamespaceEndpointURI(), subscriptionPath, TestUtils.getManagementClientSettings(), ReceiveMode.PEEKLOCK);
			try
			{
				qc.registerMessageHandler(new IMessageHandler() {				
					@Override
					public CompletableFuture<Void> onMessageAsync(IMessage message) {
						return CompletableFuture.completedFuture(null);
					}
					
					@Override
					public void notifyException(Throwable exception, ExceptionPhase phase) {					
					}
				});
				Assert.fail("QueueClient created to a subscription which shouldn't be allowed.");
			}
			finally
			{
				qc.close();
			}			
		} catch (UnsupportedOperationException e) {
			// Expected
		}
	}
	
	@Test
	public void testSubscriptionClientCreationToQueue() throws InterruptedException, ServiceBusException
	{				
		try {
			SubscriptionClient sc = new SubscriptionClient(TestUtils.getNamespaceEndpointURI(), queuePath, TestUtils.getManagementClientSettings(), ReceiveMode.PEEKLOCK);
			try {
				sc.registerMessageHandler(new IMessageHandler() {				
					@Override
					public CompletableFuture<Void> onMessageAsync(IMessage message) {
						return CompletableFuture.completedFuture(null);
					}
					
					@Override
					public void notifyException(Throwable exception, ExceptionPhase phase) {					
					}
				});
				Assert.fail("SubscriptionClient created to a queue which shouldn't be allowed.");
			} finally {
				sc.close();
			}			
		} catch (UnsupportedOperationException e) {
			// Expected
		}
	}
	
	@Test
	public void testQueueClientCreationToSessionfulSubscription() throws InterruptedException, ServiceBusException
	{
		try {
			QueueClient qc = new QueueClient(TestUtils.getNamespaceEndpointURI(), sessionfulSubscriptionPath, TestUtils.getManagementClientSettings(), ReceiveMode.PEEKLOCK);
			try {
				final AtomicBoolean unsupportedExceptionOccured = new AtomicBoolean(false);
				qc.registerSessionHandler(new ISessionHandler() {
					
					@Override
					public CompletableFuture<Void> onMessageAsync(IMessageSession session, IMessage message) {
						return CompletableFuture.completedFuture(null);
					}
					
					@Override
					public void notifyException(Throwable exception, ExceptionPhase phase) {
						if(exception instanceof UnsupportedOperationException && phase == ExceptionPhase.ACCEPTSESSION)
						{
							unsupportedExceptionOccured.set(true);
						}
					}
					
					@Override
					public CompletableFuture<Void> OnCloseSessionAsync(IMessageSession session) {
						return CompletableFuture.completedFuture(null);
					}
				});
				
				Thread.sleep(1000); // Sleep for a second for the exception				
				Assert.assertTrue("QueueClient created to a subscription which shouldn't be allowed.", unsupportedExceptionOccured.get());
			} finally {
				qc.close();
			}			
		} catch (UnsupportedOperationException e) {
			// Expected
		}
	}
	
	@Test
	public void testSubscriptionClientCreationToSessionfulQueue() throws InterruptedException, ServiceBusException
	{
		try {
			SubscriptionClient sc = new SubscriptionClient(TestUtils.getNamespaceEndpointURI(), sessionfulQueuePath, TestUtils.getManagementClientSettings(), ReceiveMode.PEEKLOCK);
			try {
				final AtomicBoolean unsupportedExceptionOccured = new AtomicBoolean(false);
				sc.registerSessionHandler(new ISessionHandler() {
					
					@Override
					public CompletableFuture<Void> onMessageAsync(IMessageSession session, IMessage message) {
						return CompletableFuture.completedFuture(null);
					}
					
					@Override
					public void notifyException(Throwable exception, ExceptionPhase phase) {
						if(exception instanceof UnsupportedOperationException && phase == ExceptionPhase.ACCEPTSESSION)
						{
							unsupportedExceptionOccured.set(true);
						}
					}
					
					@Override
					public CompletableFuture<Void> OnCloseSessionAsync(IMessageSession session) {
						return CompletableFuture.completedFuture(null);
					}
				});
				
				Thread.sleep(1000); // Sleep for a second for the exception				
				Assert.assertTrue("SubscriptionClient created to a queue which shouldn't be allowed.", unsupportedExceptionOccured.get());
			} finally {
				sc.close();
			}
			
		} catch (UnsupportedOperationException e) {
			// Expected
		}
	}
	
}
