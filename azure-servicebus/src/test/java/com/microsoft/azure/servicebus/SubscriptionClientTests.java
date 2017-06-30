package com.microsoft.azure.servicebus;

import java.util.HashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.microsoft.azure.servicebus.primitives.MessagingEntityAlreadyExistsException;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.rules.CorrelationFilter;
import com.microsoft.azure.servicebus.rules.RuleDescription;
import com.microsoft.azure.servicebus.rules.SqlFilter;
import com.microsoft.azure.servicebus.rules.SqlRuleAction;
import com.microsoft.azure.servicebus.rules.TrueFilter;

public class SubscriptionClientTests {
	private TopicClient topicClient;
	private TopicClient sessionfulTopicClient;
	private SubscriptionClient subscriptionClient;
	private SubscriptionClient sessionfulSubscriptionClient;
	
	@Before
	public void setup()
	{		
		
	}	
	
	@After
	public void tearDown() throws ServiceBusException, InterruptedException
	{
		if(this.topicClient != null)
		{
			this.topicClient.close();
		}
		if(this.sessionfulTopicClient != null)
		{
			this.sessionfulTopicClient.close();
		}
		if(this.subscriptionClient != null)
		{
			this.subscriptionClient.close();
			TestCommons.drainAllMessages(TestUtils.getNonPartitionedSubscriptionConnectionStringBuilder());
		}
			
		if(this.sessionfulSubscriptionClient != null)
		{
			this.sessionfulSubscriptionClient.close();
			TestCommons.drainAllSessions(TestUtils.getNonPartitionedSessionfulSubscriptionConnectionStringBuilder(), false);					
		}			
	}	
	
	private void createSubscriptionClient() throws InterruptedException, ServiceBusException
	{
		this.createSubscriptionClient(ReceiveMode.PEEKLOCK);
	}
	
	private void createSessionfulSubscriptionClient() throws InterruptedException, ServiceBusException
	{
		this.createSessionfulSubscriptionClient(ReceiveMode.PEEKLOCK);
	}
	
	private void createSubscriptionClient(ReceiveMode receiveMode) throws InterruptedException, ServiceBusException
	{
		this.topicClient = new TopicClient(TestUtils.getNonPartitionedTopicConnectionStringBuilder());
		this.subscriptionClient = new SubscriptionClient(TestUtils.getNonPartitionedSubscriptionConnectionStringBuilder(), receiveMode);
	}
	
	private void createSessionfulSubscriptionClient(ReceiveMode receiveMode) throws InterruptedException, ServiceBusException
	{
		this.sessionfulTopicClient = new TopicClient(TestUtils.getNonPartitionedSessionfulTopicConnectionStringBuilder());
		this.sessionfulSubscriptionClient = new SubscriptionClient(TestUtils.getNonPartitionedSessionfulSubscriptionConnectionStringBuilder(), receiveMode);
	}
	
	@Test
	public void testAddRemoveRules() throws InterruptedException, ServiceBusException
	{
		this.createSubscriptionClient();
		
		// Simple rule
		RuleDescription trueFilterRule = new RuleDescription("customRule1", TrueFilter.DEFAULT);
		this.subscriptionClient.addRule(trueFilterRule);
		try
		{
			this.subscriptionClient.addRule(trueFilterRule);
			Assert.fail("A rule with duplicate name is added.");
		}
		catch(MessagingEntityAlreadyExistsException e)
		{
			// Expected
		}		
		this.subscriptionClient.removeRule(trueFilterRule.getName());
		
		// Custom SQL Filter rule
		SqlFilter sqlFilter = new SqlFilter("1=1");
		SqlRuleAction action = new SqlRuleAction("set FilterTag = 'true'");
		RuleDescription sqlRule = new RuleDescription("customRule2", sqlFilter);
		sqlRule.setAction(action);
		this.subscriptionClient.addRule(sqlRule);		
		this.subscriptionClient.removeRule(sqlRule.getName());
		
		// Correlation Filter rule
		CorrelationFilter correlationFilter = new CorrelationFilter();
		correlationFilter.setCorrelationId("TestCorrelationId");
		correlationFilter.setMessageId("TestMessageId");
		correlationFilter.setReplyTo("testReplyTo");
		correlationFilter.setLabel("testLabel");
		correlationFilter.setTo("testTo");
		correlationFilter.setReplyTo("testReplyTo");
		HashMap<String, Object> properties = new HashMap<>();
		properties.put("testKey1", "testValue1");
		properties.put("testKey2", null);
		correlationFilter.setProperties(properties);		
		RuleDescription correlationRule = new RuleDescription("customRule3", correlationFilter);
		correlationRule.setAction(action);
		this.subscriptionClient.addRule(correlationRule);		
		this.subscriptionClient.removeRule(correlationRule.getName());
	}
	
	@Test
	public void testMessagePumpAutoComplete() throws InterruptedException, ServiceBusException
	{
		this.createSubscriptionClient();
		MessageAndSessionPumpTests.testMessagePumpAutoComplete(this.topicClient, this.subscriptionClient);
	}
	
	@Test
	public void testReceiveAndDeleteMessagePump() throws InterruptedException, ServiceBusException
	{
		this.createSubscriptionClient(ReceiveMode.RECEIVEANDDELETE);
		MessageAndSessionPumpTests.testMessagePumpAutoComplete(this.topicClient, this.subscriptionClient);
	}
	
	@Test
	public void testMessagePumpClientComplete() throws InterruptedException, ServiceBusException
	{
		this.createSubscriptionClient();
		MessageAndSessionPumpTests.testMessagePumpClientComplete(this.topicClient, this.subscriptionClient);
	}
	
	@Test
	public void testMessagePumpAbandonOnException() throws InterruptedException, ServiceBusException
	{
		this.createSubscriptionClient();
		MessageAndSessionPumpTests.testMessagePumpAbandonOnException(this.topicClient, this.subscriptionClient);
	}
	
	@Test
	public void testMessagePumpRenewLock() throws InterruptedException, ServiceBusException
	{
		this.createSubscriptionClient();
		MessageAndSessionPumpTests.testMessagePumpRenewLock(this.topicClient, this.subscriptionClient);
	}
	
	@Test
	public void testRegisterAnotherHandlerAfterMessageHandler() throws InterruptedException, ServiceBusException
	{
		this.createSubscriptionClient();
		MessageAndSessionPumpTests.testRegisterAnotherHandlerAfterMessageHandler(this.subscriptionClient);
	}
	
	@Test
	public void testRegisterAnotherHandlerAfterSessionHandler() throws InterruptedException, ServiceBusException
	{
		this.createSessionfulSubscriptionClient();
		MessageAndSessionPumpTests.testRegisterAnotherHandlerAfterSessionHandler(this.sessionfulSubscriptionClient);
	}
	
	@Test
	public void testGetMessageSessions() throws InterruptedException, ServiceBusException
	{
		this.createSessionfulSubscriptionClient();
		TestCommons.testGetMessageSessions(this.sessionfulTopicClient, this.sessionfulSubscriptionClient);
	}
	
	@Test
	public void testSessionPumpAutoCompleteWithOneConcurrentCallPerSession() throws InterruptedException, ServiceBusException
	{
		this.createSessionfulSubscriptionClient();
		MessageAndSessionPumpTests.testSessionPumpAutoCompleteWithOneConcurrentCallPerSession(this.sessionfulTopicClient, this.sessionfulSubscriptionClient);
	}
	
	@Test
	public void testReceiveAndDeleteSessionPump() throws InterruptedException, ServiceBusException
	{
		this.createSessionfulSubscriptionClient(ReceiveMode.RECEIVEANDDELETE);
		MessageAndSessionPumpTests.testSessionPumpAutoCompleteWithOneConcurrentCallPerSession(this.sessionfulTopicClient, this.sessionfulSubscriptionClient);
	}
	
	@Test
	public void testSessionPumpAutoCompleteWithMultipleConcurrentCallsPerSession() throws InterruptedException, ServiceBusException
	{
		this.createSessionfulSubscriptionClient();
		MessageAndSessionPumpTests.testSessionPumpAutoCompleteWithMultipleConcurrentCallsPerSession(this.sessionfulTopicClient, this.sessionfulSubscriptionClient);
	}
	
	@Test
	public void testSessionPumpClientComplete() throws InterruptedException, ServiceBusException
	{
		this.createSessionfulSubscriptionClient();
		MessageAndSessionPumpTests.testSessionPumpClientComplete(this.sessionfulTopicClient, this.sessionfulSubscriptionClient);
	}
	
	@Test
	public void testSessionPumpAbandonOnException() throws InterruptedException, ServiceBusException
	{
		this.createSessionfulSubscriptionClient();
		MessageAndSessionPumpTests.testSessionPumpAbandonOnException(this.sessionfulTopicClient, this.sessionfulSubscriptionClient);
	}
	
	@Test
	public void testSessionPumpRenewLock() throws InterruptedException, ServiceBusException
	{
		this.createSessionfulSubscriptionClient();
		MessageAndSessionPumpTests.testSessionPumpRenewLock(this.sessionfulTopicClient, this.sessionfulSubscriptionClient);
	}
	
	@Test
    public void testSubscriptionNameSplitting() throws InterruptedException, ServiceBusException
    {
	    this.subscriptionClient = new SubscriptionClient(TestUtils.getNonPartitionedSubscriptionConnectionStringBuilder(), ReceiveMode.PEEKLOCK);
        Assert.assertEquals("Wrong subscription name returned.", TestUtils.getProperty(TestUtils.SUBSCRIPTION_NAME_PROPERTY), this.subscriptionClient.getSubscriptionName());
    }
}
