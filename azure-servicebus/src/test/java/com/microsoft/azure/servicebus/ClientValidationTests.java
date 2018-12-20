package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.microsoft.azure.servicebus.management.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public class ClientValidationTests
{

	private static final String ENTITY_NAME_PREFIX = "ClientValidationTests";

	private static String queuePath;
	private static String sessionfulQueuePath;
	private static String topicPath;
	private static String subscriptionPath;
	private static String sessionfulSubscriptionPath;
	private static ManagementClientAsync managementClient;

	@BeforeClass
	public static void createEntities() throws ExecutionException, InterruptedException {
		// Create a queue, a topic and a subscription
		queuePath = TestUtils.randomizeEntityName(ENTITY_NAME_PREFIX);
		sessionfulQueuePath = TestUtils.randomizeEntityName(ENTITY_NAME_PREFIX);
		topicPath = TestUtils.randomizeEntityName(ENTITY_NAME_PREFIX);
		URI namespaceEndpointURI = TestUtils.getNamespaceEndpointURI();
		ClientSettings managementClientSettings = TestUtils.getManagementClientSettings();
		managementClient = new ManagementClientAsync(namespaceEndpointURI, managementClientSettings);

		QueueDescription queueDescription = new QueueDescription(queuePath);
		queueDescription.setEnablePartitioning(false);
		managementClient.createQueueAsync(queueDescription).get();

		QueueDescription queueDescription2 = new QueueDescription(sessionfulQueuePath);
		queueDescription2.setEnablePartitioning(false);
		queueDescription2.setRequiresSession(true);
		managementClient.createQueueAsync(queueDescription2).get();

		TopicDescription topicDescription = new TopicDescription(topicPath);
		topicDescription.setEnablePartitioning(false);
		managementClient.createTopicAsync(topicDescription).get();
		SubscriptionDescription subDescription = new SubscriptionDescription(topicPath, TestUtils.FIRST_SUBSCRIPTION_NAME);
		subscriptionPath = subDescription.getPath();
		managementClient.createSubscriptionAsync(subDescription).get();
		SubscriptionDescription subDescription2 = new SubscriptionDescription(topicPath, "subscription2");
		subDescription2.setRequiresSession(true);
		sessionfulSubscriptionPath = subDescription2.getPath();
		managementClient.createSubscriptionAsync(subDescription2).get();
	}

	@AfterClass
	public static void deleteEntities() throws ExecutionException, InterruptedException, IOException {
		managementClient.deleteQueueAsync(queuePath).get();
		managementClient.deleteQueueAsync(sessionfulQueuePath).get();
		managementClient.deleteTopicAsync(topicPath).get();
		managementClient.close();
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
				}, MessageAndSessionPumpTests.EXECUTOR_SERVICE);
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
				}, MessageAndSessionPumpTests.EXECUTOR_SERVICE);
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
				}, MessageAndSessionPumpTests.EXECUTOR_SERVICE);

				Thread.sleep(2000); // Sleep for two seconds for the exception
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
				}, MessageAndSessionPumpTests.EXECUTOR_SERVICE);

				Thread.sleep(2000); // Sleep for two seconds for the exception
				Assert.assertTrue("SubscriptionClient created to a queue which shouldn't be allowed.", unsupportedExceptionOccured.get());
			} finally {
				sc.close();
			}

		} catch (UnsupportedOperationException e) {
			// Expected
		}
	}

}