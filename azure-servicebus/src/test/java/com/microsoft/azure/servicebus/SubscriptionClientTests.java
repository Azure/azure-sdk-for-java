package com.microsoft.azure.servicebus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
	public void testGetAddRemoveRules() throws InterruptedException, ServiceBusException
	{
		this.createSubscriptionClient();

		this.subscriptionClient.removeRule(SubscriptionClient.DEFAULT_RULE_NAME);

		RuleDescription[] rules = this.subscriptionClient.getRules().toArray(new RuleDescription[0]);
		Assert.assertEquals(0, rules.length);

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
		rules = this.subscriptionClient.getRules().toArray(new RuleDescription[0]);
		Assert.assertEquals("More than one rules are present", 1, rules.length);
		Assert.assertEquals("Returned rule name doesn't match", trueFilterRule.getName(), rules[0].getName());
		Assert.assertTrue(rules[0].getFilter() instanceof SqlFilter);
		this.subscriptionClient.removeRule(trueFilterRule.getName());
		
		// Custom SQL Filter rule
		SqlFilter sqlFilter = new SqlFilter("1=1");
		SqlRuleAction action = new SqlRuleAction("set FilterTag = 'true'");
		RuleDescription sqlRule = new RuleDescription("customRule2", sqlFilter);
		sqlRule.setAction(action);
		this.subscriptionClient.addRule(sqlRule);
		rules = this.subscriptionClient.getRules().toArray(new RuleDescription[0]);
		Assert.assertEquals("More than one rules are present", 1, rules.length);
		RuleDescription returnedRule = rules[0];
		Assert.assertEquals("Returned rule name doesn't match", sqlRule.getName(), returnedRule.getName());
		Assert.assertTrue(returnedRule.getFilter() instanceof SqlFilter);
		Assert.assertEquals(sqlFilter.getSqlExpression(), ((SqlFilter)returnedRule.getFilter()).getSqlExpression());
		Assert.assertTrue(returnedRule.getAction() instanceof SqlRuleAction);
		Assert.assertEquals(action.getSqlExpression(), ((SqlRuleAction)returnedRule.getAction()).getSqlExpression());
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
		rules = this.subscriptionClient.getRules().toArray(new RuleDescription[0]);
		Assert.assertEquals("More than one rules are present", 1, rules.length);
		returnedRule = rules[0];
		Assert.assertEquals("Returned rule name doesn't match", correlationRule.getName(), returnedRule.getName());
		Assert.assertTrue(returnedRule.getAction() instanceof SqlRuleAction);
		Assert.assertEquals(action.getSqlExpression(), ((SqlRuleAction)returnedRule.getAction()).getSqlExpression());
		Assert.assertTrue(returnedRule.getFilter() instanceof CorrelationFilter);
		CorrelationFilter returnedFilter = (CorrelationFilter) returnedRule.getFilter();
		Assert.assertEquals(correlationFilter.getCorrelationId(), returnedFilter.getCorrelationId());
		Assert.assertEquals(correlationFilter.getMessageId(), returnedFilter.getMessageId());
		Assert.assertEquals(correlationFilter.getReplyTo(), returnedFilter.getReplyTo());
		Assert.assertEquals(correlationFilter.getLabel(), returnedFilter.getLabel());
		Assert.assertEquals(correlationFilter.getTo(), returnedFilter.getTo());
		Assert.assertEquals(correlationFilter.getReplyTo(), returnedFilter.getReplyTo());
		for (Map.Entry<String, Object> entry : properties.entrySet())
		{
			Assert.assertTrue(returnedFilter.getProperties().containsKey(entry.getKey()));
			Assert.assertEquals(entry.getValue(), returnedFilter.getProperties().get(entry.getKey()));
		}
		this.subscriptionClient.removeRule(correlationRule.getName());
		this.subscriptionClient.addRule(SubscriptionClient.DEFAULT_RULE_NAME, TrueFilter.DEFAULT);
	}

	@Test
	public void testGetRulesForMultipleRules() throws InterruptedException, ServiceBusException
	{
		this.createSubscriptionClient();

		RuleDescription trueFilterRule = new RuleDescription("getRules1", TrueFilter.DEFAULT);
		this.subscriptionClient.addRule(trueFilterRule);

		RuleDescription trueFilterRule2 = new RuleDescription("getRules2", TrueFilter.DEFAULT);
		this.subscriptionClient.addRule(trueFilterRule2);

		SqlFilter sqlFilter = new SqlFilter("1=1");
		RuleDescription sqlRule = new RuleDescription("getRules3", sqlFilter);
		this.subscriptionClient.addRule(sqlRule);

		CorrelationFilter correlationFilter = new CorrelationFilter();
		correlationFilter.setCorrelationId("TestCorrelationId");
		RuleDescription correlationRule = new RuleDescription("getRules4", correlationFilter);
		this.subscriptionClient.addRule(correlationRule);

		Collection<RuleDescription> rules = this.subscriptionClient.getRules();
		Assert.assertEquals(5, rules.size());

		subscriptionClient.removeRule(trueFilterRule.getName());
		subscriptionClient.removeRule(trueFilterRule2.getName());
		subscriptionClient.removeRule(sqlRule.getName());
		subscriptionClient.removeRule(correlationRule.getName());
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
