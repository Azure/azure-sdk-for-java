// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.microsoft.azure.servicebus.primitives.MessagingEntityAlreadyExistsException;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.rules.CorrelationFilter;
import com.microsoft.azure.servicebus.rules.RuleDescription;
import com.microsoft.azure.servicebus.rules.SqlFilter;
import com.microsoft.azure.servicebus.rules.SqlRuleAction;
import com.microsoft.azure.servicebus.rules.TrueFilter;

public class SubscriptionClientTests extends ClientTests {
    @Override
    public String getEntityNamePrefix() {
        return "SubscriptionClientTests";
    }

    @Override
    public boolean isEntityQueue() {
        return false;
    }

    @Override
    public boolean isEntityPartitioned() {
        return false;
    }

    @Override
    public boolean shouldCreateEntityForEveryTest() {
        return TestUtils.shouldCreateEntityForEveryTest();
    }

    @Test
    public void testGetAddRemoveRules() throws InterruptedException, ServiceBusException {
        this.createClients(ReceiveMode.PEEKLOCK);
        SubscriptionClient subscriptionClient = (SubscriptionClient) this.receiveClient;
        subscriptionClient.removeRule(SubscriptionClient.DEFAULT_RULE_NAME);

        RuleDescription[] rules = subscriptionClient.getRules().toArray(new RuleDescription[0]);
        Assertions.assertEquals(0, rules.length);

        // Simple rule
        RuleDescription trueFilterRule = new RuleDescription("customRule1", TrueFilter.DEFAULT);
        subscriptionClient.addRule(trueFilterRule);
        try {
            subscriptionClient.addRule(trueFilterRule);
            Assertions.fail("A rule with duplicate name is added.");
        } catch (MessagingEntityAlreadyExistsException e) {
            // Expected
        }
        rules = subscriptionClient.getRules().toArray(new RuleDescription[0]);
        Assertions.assertEquals(1, rules.length, "More than one rules are present");
        Assertions.assertEquals(trueFilterRule.getName(), rules[0].getName(), "Returned rule name doesn't match");
        Assertions.assertTrue(rules[0].getFilter() instanceof SqlFilter);
        subscriptionClient.removeRule(trueFilterRule.getName());

        // Custom SQL Filter rule
        SqlFilter sqlFilter = new SqlFilter("1=1");
        SqlRuleAction action = new SqlRuleAction("set FilterTag = 'true'");
        RuleDescription sqlRule = new RuleDescription("customRule2", sqlFilter);
        sqlRule.setAction(action);
        subscriptionClient.addRule(sqlRule);
        rules = subscriptionClient.getRules().toArray(new RuleDescription[0]);
        Assertions.assertEquals(1, rules.length, "More than one rules are present");
        RuleDescription returnedRule = rules[0];
        Assertions.assertEquals(sqlRule.getName(), returnedRule.getName(), "Returned rule name doesn't match");
        Assertions.assertTrue(returnedRule.getFilter() instanceof SqlFilter);
        Assertions.assertEquals(sqlFilter.getSqlExpression(), ((SqlFilter) returnedRule.getFilter()).getSqlExpression());
        Assertions.assertTrue(returnedRule.getAction() instanceof SqlRuleAction);
        Assertions.assertEquals(action.getSqlExpression(), ((SqlRuleAction) returnedRule.getAction()).getSqlExpression());
        subscriptionClient.removeRule(sqlRule.getName());

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
        subscriptionClient.addRule(correlationRule);
        rules = subscriptionClient.getRules().toArray(new RuleDescription[0]);
        Assertions.assertEquals(1, rules.length, "More than one rules are present");
        returnedRule = rules[0];
        Assertions.assertEquals(correlationRule.getName(), returnedRule.getName(), "Returned rule name doesn't match");
        Assertions.assertTrue(returnedRule.getAction() instanceof SqlRuleAction);
        Assertions.assertEquals(action.getSqlExpression(), ((SqlRuleAction) returnedRule.getAction()).getSqlExpression());
        Assertions.assertTrue(returnedRule.getFilter() instanceof CorrelationFilter);
        CorrelationFilter returnedFilter = (CorrelationFilter) returnedRule.getFilter();
        Assertions.assertEquals(correlationFilter.getCorrelationId(), returnedFilter.getCorrelationId());
        Assertions.assertEquals(correlationFilter.getMessageId(), returnedFilter.getMessageId());
        Assertions.assertEquals(correlationFilter.getReplyTo(), returnedFilter.getReplyTo());
        Assertions.assertEquals(correlationFilter.getLabel(), returnedFilter.getLabel());
        Assertions.assertEquals(correlationFilter.getTo(), returnedFilter.getTo());
        Assertions.assertEquals(correlationFilter.getReplyTo(), returnedFilter.getReplyTo());
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            Assertions.assertTrue(returnedFilter.getProperties().containsKey(entry.getKey()));
            Assertions.assertEquals(entry.getValue(), returnedFilter.getProperties().get(entry.getKey()));
        }
        subscriptionClient.removeRule(correlationRule.getName());
        subscriptionClient.addRule(SubscriptionClient.DEFAULT_RULE_NAME, TrueFilter.DEFAULT);
    }

    @Test
    public void testGetRulesForMultipleRules() throws InterruptedException, ServiceBusException {
        this.createClients(ReceiveMode.PEEKLOCK);
        SubscriptionClient subscriptionClient = (SubscriptionClient) this.receiveClient;
        RuleDescription trueFilterRule = new RuleDescription("getRules1", TrueFilter.DEFAULT);
        subscriptionClient.addRule(trueFilterRule);

        RuleDescription trueFilterRule2 = new RuleDescription("getRules2", TrueFilter.DEFAULT);
        subscriptionClient.addRule(trueFilterRule2);

        SqlFilter sqlFilter = new SqlFilter("1=1");
        RuleDescription sqlRule = new RuleDescription("getRules3", sqlFilter);
        subscriptionClient.addRule(sqlRule);

        CorrelationFilter correlationFilter = new CorrelationFilter();
        correlationFilter.setCorrelationId("TestCorrelationId");
        RuleDescription correlationRule = new RuleDescription("getRules4", correlationFilter);
        subscriptionClient.addRule(correlationRule);

        Collection<RuleDescription> rules = subscriptionClient.getRules();
        Assertions.assertEquals(5, rules.size());

        subscriptionClient.removeRule(trueFilterRule.getName());
        subscriptionClient.removeRule(trueFilterRule2.getName());
        subscriptionClient.removeRule(sqlRule.getName());
        subscriptionClient.removeRule(correlationRule.getName());
    }

    @Test
    public void testSubscriptionNameSplitting() throws InterruptedException, ServiceBusException {
        this.createClients(ReceiveMode.RECEIVEANDDELETE);
        Assertions.assertEquals(TestUtils.FIRST_SUBSCRIPTION_NAME, ((SubscriptionClient) this.receiveClient).getSubscriptionName(), "Wrong subscription name returned.");
    }
}
