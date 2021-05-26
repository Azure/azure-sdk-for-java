// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.management;

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.ClientSettings;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.TestBase;
import com.microsoft.azure.servicebus.TestUtils;
import com.microsoft.azure.servicebus.Utils;
import com.microsoft.azure.servicebus.management.AccessRights;
import com.microsoft.azure.servicebus.management.AuthorizationRule;
import com.microsoft.azure.servicebus.management.EntityNameHelper;
import com.microsoft.azure.servicebus.management.ManagementClientAsync;
import com.microsoft.azure.servicebus.management.NamespaceInfo;
import com.microsoft.azure.servicebus.management.NamespaceType;
import com.microsoft.azure.servicebus.management.QueueDescription;
import com.microsoft.azure.servicebus.management.QueueRuntimeInfo;
import com.microsoft.azure.servicebus.management.SharedAccessAuthorizationRule;
import com.microsoft.azure.servicebus.management.SubscriptionDescription;
import com.microsoft.azure.servicebus.management.SubscriptionRuntimeInfo;
import com.microsoft.azure.servicebus.management.TopicDescription;
import com.microsoft.azure.servicebus.management.TopicRuntimeInfo;
import com.microsoft.azure.servicebus.primitives.MessagingEntityAlreadyExistsException;
import com.microsoft.azure.servicebus.primitives.MessagingEntityNotFoundException;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.rules.CorrelationFilter;
import com.microsoft.azure.servicebus.rules.FalseFilter;
import com.microsoft.azure.servicebus.rules.RuleDescription;
import com.microsoft.azure.servicebus.rules.SqlFilter;
import com.microsoft.azure.servicebus.rules.SqlRuleAction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ManagementTests extends TestBase {

    private ManagementClientAsync managementClientAsync;

    @Before
    public void setup() {
        URI namespaceEndpointURI = TestUtils.getNamespaceEndpointURI();
        ClientSettings managementClientSettings = TestUtils.getManagementClientSettings();
        managementClientAsync = new ManagementClientAsync(namespaceEndpointURI, managementClientSettings);
    }

    @Test
    public void basicQueueCrudTest() throws InterruptedException, ExecutionException {
        String queueName = UUID.randomUUID().toString().substring(0, 8);
        QueueDescription q = new QueueDescription(queueName);
        q.setAutoDeleteOnIdle(Duration.ofHours(1));
        q.setDefaultMessageTimeToLive(Duration.ofDays(2));
        q.setDuplicationDetectionHistoryTimeWindow(Duration.ofMinutes(1));
        q.setEnableBatchedOperations(false);
        q.setEnableDeadLetteringOnMessageExpiration(true);
        q.setEnablePartitioning(false);
        q.setForwardTo(null);
        q.setForwardDeadLetteredMessagesTo(null);
        q.setLockDuration(Duration.ofSeconds(45));
        q.setMaxDeliveryCount(8);
        q.setMaxSizeInMB(2048);
        q.setRequiresDuplicateDetection(true);
        q.setRequiresSession(true);
        q.setUserMetadata("basicQueueCrudTest");

        ArrayList<AuthorizationRule> rules = new ArrayList<>();
        ArrayList<AccessRights> rights = new ArrayList<>();
        rights.add(AccessRights.Send);
        rights.add(AccessRights.Listen);
        rights.add(AccessRights.Manage);
        rules.add(new SharedAccessAuthorizationRule("allClaims", rights));
        q.setAuthorizationRules(rules);

        QueueDescription qCreated = this.managementClientAsync.createQueueAsync(q).get();
        Assert.assertEquals(q, qCreated);

        QueueDescription queue = this.managementClientAsync.getQueueAsync(queueName).get();
        Assert.assertEquals(qCreated, queue);

        queue.setEnableBatchedOperations(false);
        queue.setMaxDeliveryCount(9);
        queue.getAuthorizationRules().clear();
        rights = new ArrayList<>();
        rights.add(AccessRights.Send);
        rights.add(AccessRights.Listen);
        queue.getAuthorizationRules().add(new SharedAccessAuthorizationRule("noManage", rights));

        QueueDescription updatedQ = this.managementClientAsync.updateQueueAsync(queue).get();
        Assert.assertEquals(queue, updatedQ);

        Boolean exists = this.managementClientAsync.queueExistsAsync(queueName).get();
        Assert.assertTrue(exists);

        List<QueueDescription> queues = this.managementClientAsync.getQueuesAsync().get();
        Assert.assertTrue(queues.size() > 0);
        AtomicBoolean found = new AtomicBoolean(false);
        queues.forEach(queueDescription -> {
            if (queueDescription.getPath().equalsIgnoreCase(queueName)) {
                found.set(true);
            }
        });
        Assert.assertTrue(found.get());

        this.managementClientAsync.deleteQueueAsync(queueName).get();

        exists = this.managementClientAsync.queueExistsAsync(queueName).get();
        Assert.assertFalse(exists);
    }

    @Test
    public void basicTopicCrudTest() throws InterruptedException, ExecutionException {
        String topicName = UUID.randomUUID().toString().substring(0, 8);
        TopicDescription td = new TopicDescription(topicName);
        td.setAutoDeleteOnIdle(Duration.ofHours(1));
        td.setDefaultMessageTimeToLive(Duration.ofDays(2));
        td.setDuplicationDetectionHistoryTimeWindow(Duration.ofMinutes(1));
        td.setEnableBatchedOperations(false);
        td.setEnablePartitioning(false);
        td.setMaxSizeInMB(2048);
        td.setRequiresDuplicateDetection(true);
        td.setUserMetadata("basicTopicCrudTest");
        td.setSupportOrdering(true);

        ArrayList<AuthorizationRule> rules = new ArrayList<>();
        ArrayList<AccessRights> rights = new ArrayList<>();
        rights.add(AccessRights.Send);
        rights.add(AccessRights.Listen);
        rights.add(AccessRights.Manage);
        rules.add(new SharedAccessAuthorizationRule("allClaims", rights));
        td.setAuthorizationRules(rules);

        TopicDescription tCreated = this.managementClientAsync.createTopicAsync(td).get();
        Assert.assertEquals(td, tCreated);

        TopicDescription topic = this.managementClientAsync.getTopicAsync(topicName).get();
        Assert.assertEquals(tCreated, topic);

        topic.setEnableBatchedOperations(false);
        topic.setDefaultMessageTimeToLive(Duration.ofDays(3));
        topic.getAuthorizationRules().clear();
        rights = new ArrayList<>();
        rights.add(AccessRights.Send);
        rights.add(AccessRights.Listen);
        topic.getAuthorizationRules().add(new SharedAccessAuthorizationRule("noManage", rights));

        TopicDescription updatedT = this.managementClientAsync.updateTopicAsync(topic).get();
        Assert.assertEquals(topic, updatedT);

        Boolean exists = this.managementClientAsync.topicExistsAsync(topicName).get();
        Assert.assertTrue(exists);

        List<TopicDescription> topics = this.managementClientAsync.getTopicsAsync().get();
        Assert.assertTrue(topics.size() > 0);
        AtomicBoolean found = new AtomicBoolean(false);
        topics.forEach(topicDescription -> {
            if (topicDescription.getPath().equalsIgnoreCase(topicName)) {
                found.set(true);
            }
        });
        Assert.assertTrue(found.get());

        this.managementClientAsync.deleteTopicAsync(topicName).get();

        exists = this.managementClientAsync.topicExistsAsync(topicName).get();
        Assert.assertFalse(exists);
    }

    @Test
    public void basicSubscriptionCrudTest() throws InterruptedException, ExecutionException {
        String topicName = UUID.randomUUID().toString().substring(0, 8);
        this.managementClientAsync.createTopicAsync(topicName).get();

        String subscriptionName = UUID.randomUUID().toString().substring(0, 8);
        SubscriptionDescription subscriptionDescription = new SubscriptionDescription(topicName, subscriptionName);
        subscriptionDescription.setAutoDeleteOnIdle(Duration.ofHours(1));
        subscriptionDescription.setDefaultMessageTimeToLive(Duration.ofDays(2));
        subscriptionDescription.setEnableBatchedOperations(false);
        subscriptionDescription.setEnableDeadLetteringOnMessageExpiration(true);
        subscriptionDescription.setEnableDeadLetteringOnFilterEvaluationException(false);
        subscriptionDescription.setForwardTo(null);
        subscriptionDescription.setForwardDeadLetteredMessagesTo(null);
        subscriptionDescription.setLockDuration(Duration.ofSeconds(45));
        subscriptionDescription.setMaxDeliveryCount(8);
        subscriptionDescription.setRequiresSession(true);
        subscriptionDescription.setUserMetadata("basicSubscriptionCrudTest");

        SubscriptionDescription createdS = this.managementClientAsync.createSubscriptionAsync(subscriptionDescription).get();
        Assert.assertEquals(subscriptionDescription, createdS);

        SubscriptionDescription getS = this.managementClientAsync.getSubscriptionAsync(topicName, subscriptionName).get();
        Assert.assertEquals(createdS, getS);

        getS.setEnableBatchedOperations(false);
        getS.setMaxDeliveryCount(9);

        SubscriptionDescription updatedQ = this.managementClientAsync.updateSubscriptionAsync(getS).get();
        Assert.assertEquals(getS, updatedQ);

        Boolean exists = this.managementClientAsync.subscriptionExistsAsync(topicName, subscriptionName).get();
        Assert.assertTrue(exists);

        List<SubscriptionDescription> subscriptions = this.managementClientAsync.getSubscriptionsAsync(topicName).get();
        Assert.assertEquals(1, subscriptions.size());
        Assert.assertEquals(subscriptionName, subscriptions.get(0).getSubscriptionName());

        this.managementClientAsync.deleteSubscriptionAsync(topicName, subscriptionName).get();

        exists = this.managementClientAsync.subscriptionExistsAsync(topicName, subscriptionName).get();
        Assert.assertFalse(exists);

        this.managementClientAsync.deleteTopicAsync(topicName).get();

        exists = this.managementClientAsync.subscriptionExistsAsync(topicName, subscriptionName).get();
        Assert.assertFalse(exists);
    }

    @Test
    public void basicRulesCrudTest() throws InterruptedException, ExecutionException {
        String topicName = UUID.randomUUID().toString().substring(0, 8);
        String subscriptionName = UUID.randomUUID().toString().substring(0, 8);

        this.managementClientAsync.createTopicAsync(topicName).get();
        this.managementClientAsync.createSubscriptionAsync(
                new SubscriptionDescription(topicName, subscriptionName),
                new RuleDescription("rule0", new FalseFilter())).get();

        //SqlFilter sqlFilter = new SqlFilter("stringValue = @stringParam AND intValue = @intParam AND longValue = @longParam AND dateValue = @dateParam");
        SqlFilter sqlFilter = new SqlFilter("1=1");
        /*
        todo
        sqlFilter.Parameters.Add("@stringParam", "string");
            sqlFilter.Parameters.Add("@intParam", (int)1);
            sqlFilter.Parameters.Add("@longParam", (long)12);
            sqlFilter.Parameters.Add("@dateParam", DateTime.UtcNow);
         */
        RuleDescription rule1 = new RuleDescription("rule1");
        rule1.setFilter(sqlFilter);
        rule1.setAction(new SqlRuleAction("SET a='b'"));
        this.managementClientAsync.createRuleAsync(topicName, subscriptionName, rule1).get();

        CorrelationFilter correlationFilter = new CorrelationFilter();
        correlationFilter.setContentType("contentType");
        correlationFilter.setCorrelationId("correlationId");
        correlationFilter.setLabel("label");
        correlationFilter.setMessageId("messageId");
        correlationFilter.setReplyTo("replyTo");
        correlationFilter.setReplyToSessionId("replyToSessionId");
        correlationFilter.setSessionId("sessionId");
        correlationFilter.setTo("to");
        // todo
        // correlationFilter.Properties.Add("customKey", "customValue");
        RuleDescription rule2 = new RuleDescription("rule2");
        rule2.setFilter(correlationFilter);
        this.managementClientAsync.createRuleAsync(topicName, subscriptionName, rule2).get();

        List<RuleDescription> rules = this.managementClientAsync.getRulesAsync(topicName, subscriptionName).get();
        Assert.assertEquals(3, rules.size());
        Assert.assertEquals("rule0", rules.get(0).getName());
        Assert.assertEquals(rule1, rules.get(1));
        Assert.assertEquals(rule2, rules.get(2));

        ((CorrelationFilter) (rule2.getFilter())).setCorrelationId("correlationIdModified");
        RuleDescription updatedRule2 = this.managementClientAsync.updateRuleAsync(topicName, subscriptionName, rule2).get();
        Assert.assertEquals(rule2, updatedRule2);

        RuleDescription defaultRule = this.managementClientAsync.getRuleAsync(topicName, subscriptionName, "rule0").get();
        Assert.assertNotNull(defaultRule);
        this.managementClientAsync.deleteRuleAsync(topicName, subscriptionName, "rule0").get();
        try {
            this.managementClientAsync.getRuleAsync(topicName, subscriptionName, "rule0").get();
            Assert.fail("Get rule0 should have thrown.");
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof ExecutionException);
            Throwable cause = ex.getCause();
            Assert.assertTrue(cause instanceof MessagingEntityNotFoundException);
        }

        Assert.assertFalse(this.managementClientAsync.ruleExistsAsync(topicName, subscriptionName, "rule0").get());
        this.managementClientAsync.deleteTopicAsync(topicName).get();
    }

    @Test
    public void getQueueRuntimeInfoTest() throws ExecutionException, InterruptedException, ServiceBusException {
        String queueName = UUID.randomUUID().toString().substring(0, 8);

        // Setting created time
        QueueDescription qd = this.managementClientAsync.createQueueAsync(queueName).get();

        // Changing last updated time
        qd.setAutoDeleteOnIdle(Duration.ofHours(2));
        QueueDescription updatedQd = this.managementClientAsync.updateQueueAsync(qd).get();

        // Populating 1 active, 1 dead and 1 scheduled message.
        // Changing last accessed time.
        MessagingFactory factory = MessagingFactory.createFromNamespaceEndpointURI(TestUtils.getNamespaceEndpointURI(), TestUtils.getClientSettings());
        IMessageSender sender = ClientFactory.createMessageSenderFromEntityPath(factory, queueName);
        IMessageReceiver receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, queueName);

        sender.send(new Message("m1"));
        sender.send(new Message("m2"));
        sender.scheduleMessage(new Message("m3"), Instant.now().plusSeconds(1000));

        IMessage msg = receiver.receive();
        receiver.deadLetter(msg.getLockToken());

        QueueRuntimeInfo runtimeInfo = this.managementClientAsync.getQueueRuntimeInfoAsync(queueName).get();

        Assert.assertEquals(queueName, runtimeInfo.getPath());
        Assert.assertTrue(runtimeInfo.getCreatedAt().isBefore(runtimeInfo.getUpdatedAt()));
        Assert.assertTrue(runtimeInfo.getUpdatedAt().isBefore(runtimeInfo.getAccessedAt()));
        Assert.assertEquals(1, runtimeInfo.getMessageCountDetails().getActiveMessageCount());
        Assert.assertEquals(1, runtimeInfo.getMessageCountDetails().getDeadLetterMessageCount());
        Assert.assertEquals(1, runtimeInfo.getMessageCountDetails().getScheduledMessageCount());
        Assert.assertEquals(3, runtimeInfo.getMessageCount());
        Assert.assertTrue(runtimeInfo.getSizeInBytes() > 0);

        this.managementClientAsync.deleteQueueAsync(queueName).get();
        receiver.close();
        sender.close();
        factory.close();
    }

    @Test
    public void getTopicAndSubscriptionRuntimeInfoTest() throws ExecutionException, InterruptedException, ServiceBusException {
        String topicName = UUID.randomUUID().toString().substring(0, 8);
        String subscriptionName = UUID.randomUUID().toString().substring(0, 8);

        // Setting created time
        TopicDescription td = this.managementClientAsync.createTopicAsync(topicName).get();

        // Changing last updated time
        td.setAutoDeleteOnIdle(Duration.ofHours(2));
        TopicDescription updatedTd = this.managementClientAsync.updateTopicAsync(td).get();

        SubscriptionDescription sd = this.managementClientAsync.createSubscriptionAsync(topicName, subscriptionName).get();

        // Changing Last updated time for subscription.
        sd.setAutoDeleteOnIdle(Duration.ofHours(2));
        SubscriptionDescription updatedSd = this.managementClientAsync.updateSubscriptionAsync(sd).get();

        // Populating 1 active, 1 dead and 1 scheduled message.
        // Changing last accessed time.
        MessagingFactory factory = MessagingFactory.createFromNamespaceEndpointURI(TestUtils.getNamespaceEndpointURI(), TestUtils.getClientSettings());
        IMessageSender sender = ClientFactory.createMessageSenderFromEntityPath(factory, topicName);
        IMessageReceiver receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, EntityNameHelper.formatSubscriptionPath(topicName, subscriptionName));

        sender.send(new Message("m1"));
        sender.send(new Message("m2"));
        sender.scheduleMessage(new Message("m3"), Instant.now().plusSeconds(1000));

        IMessage msg = receiver.receive();
        receiver.deadLetter(msg.getLockToken());

        TopicRuntimeInfo topicRuntimeInfo = this.managementClientAsync.getTopicRuntimeInfoAsync(topicName).get();
        SubscriptionRuntimeInfo subscriptionRuntimeInfo = this.managementClientAsync.getSubscriptionRuntimeInfoAsync(topicName, subscriptionName).get();

        Assert.assertEquals(topicName, topicRuntimeInfo.getPath());
        Assert.assertEquals(topicName, subscriptionRuntimeInfo.getTopicPath());
        Assert.assertEquals(subscriptionName, subscriptionRuntimeInfo.getSubscriptionName());

        Assert.assertEquals(0, topicRuntimeInfo.getMessageCountDetails().getActiveMessageCount());
        Assert.assertEquals(0, topicRuntimeInfo.getMessageCountDetails().getDeadLetterMessageCount());
        Assert.assertEquals(1, topicRuntimeInfo.getMessageCountDetails().getScheduledMessageCount());
        Assert.assertEquals(1, subscriptionRuntimeInfo.getMessageCountDetails().getActiveMessageCount());
        Assert.assertEquals(1, subscriptionRuntimeInfo.getMessageCountDetails().getDeadLetterMessageCount());
        Assert.assertEquals(0, subscriptionRuntimeInfo.getMessageCountDetails().getScheduledMessageCount());
        Assert.assertEquals(2, subscriptionRuntimeInfo.getMessageCount());
        Assert.assertEquals(1, topicRuntimeInfo.getSubscriptionCount());
        Assert.assertTrue(topicRuntimeInfo.getSizeInBytes() > 0);

        Assert.assertTrue(topicRuntimeInfo.getCreatedAt().isBefore(topicRuntimeInfo.getUpdatedAt()));
        Assert.assertTrue(topicRuntimeInfo.getUpdatedAt().isBefore(topicRuntimeInfo.getAccessedAt()));
        Assert.assertTrue(subscriptionRuntimeInfo.getCreatedAt().isBefore(subscriptionRuntimeInfo.getUpdatedAt()));
        Assert.assertTrue(subscriptionRuntimeInfo.getUpdatedAt().isBefore(subscriptionRuntimeInfo.getAccessedAt()));
        Assert.assertTrue(topicRuntimeInfo.getUpdatedAt().isBefore(subscriptionRuntimeInfo.getUpdatedAt()));

        this.managementClientAsync.deleteSubscriptionAsync(topicName, subscriptionName).get();
        this.managementClientAsync.deleteTopicAsync(topicName).get();
        receiver.close();
        sender.close();
        factory.close();
    }

    @Test
    public void messagingEntityNotFoundExceptionTest() throws ServiceBusException, InterruptedException, ExecutionException {
        try {
            Utils.completeFuture(this.managementClientAsync.getQueueAsync("NonExistingPath"));
        } catch (MessagingEntityNotFoundException e) { }

        try {
            Utils.completeFuture(this.managementClientAsync.getTopicAsync("NonExistingPath"));
        } catch (MessagingEntityNotFoundException e) { }

        try {
            Utils.completeFuture(this.managementClientAsync.getSubscriptionAsync("NonExistingTopic", "NonExistingPath"));
        } catch (MessagingEntityNotFoundException e) { }

        try {
            Utils.completeFuture(this.managementClientAsync.updateQueueAsync(new QueueDescription("NonExistingPath")));
        } catch (MessagingEntityNotFoundException e) { }

        try {
            Utils.completeFuture(this.managementClientAsync.updateTopicAsync(new TopicDescription("NonExistingPath")));
        } catch (MessagingEntityNotFoundException e) { }

        try {
            Utils.completeFuture(this.managementClientAsync.updateSubscriptionAsync(new SubscriptionDescription("NonExistingTopic", "NonExistingPath")));
        } catch (MessagingEntityNotFoundException e) { }

        try {
            Utils.completeFuture(this.managementClientAsync.deleteQueueAsync("NonExistingPath"));
        } catch (MessagingEntityNotFoundException e) { }

        try {
            Utils.completeFuture(this.managementClientAsync.deleteTopicAsync("NonExistingPath"));
        } catch (MessagingEntityNotFoundException e) { }

        try {
            Utils.completeFuture(this.managementClientAsync.deleteSubscriptionAsync("nonExistingTopic", "NonExistingPath"));
        } catch (MessagingEntityNotFoundException e) { }

        String queueName = UUID.randomUUID().toString().substring(0, 8);
        String topicName = UUID.randomUUID().toString().substring(0, 8);

        this.managementClientAsync.createQueueAsync(queueName).get();
        this.managementClientAsync.createTopicAsync(topicName).get();

        try {
            Utils.completeFuture(this.managementClientAsync.getQueueAsync(topicName));
        } catch (MessagingEntityNotFoundException e) { }

        try {
            Utils.completeFuture(this.managementClientAsync.getTopicAsync(queueName));
        } catch (MessagingEntityNotFoundException e) { }

        try {
            Utils.completeFuture(this.managementClientAsync.getSubscriptionAsync(topicName, "NonExistingSubscription"));
        } catch (MessagingEntityNotFoundException e) { }

        this.managementClientAsync.deleteQueueAsync(queueName).get();
        this.managementClientAsync.deleteTopicAsync(topicName).get();
    }

    @Test
    public void messagingEntityAlreadyExistsExceptionTest() throws ServiceBusException, InterruptedException, ExecutionException {
        String queueName = UUID.randomUUID().toString().substring(0, 8);
        String topicName = UUID.randomUUID().toString().substring(0, 8);
        String subscriptionName = UUID.randomUUID().toString().substring(0, 8);
        this.managementClientAsync.createQueueAsync(queueName).get();
        this.managementClientAsync.createTopicAsync(topicName).get();
        this.managementClientAsync.createSubscriptionAsync(topicName, subscriptionName).get();

        try {
            Utils.completeFuture(this.managementClientAsync.createQueueAsync(queueName));
        } catch (MessagingEntityAlreadyExistsException e) { }

        try {
            Utils.completeFuture(this.managementClientAsync.createTopicAsync(topicName));
        } catch (MessagingEntityAlreadyExistsException e) { }

        try {
            Utils.completeFuture(this.managementClientAsync.createSubscriptionAsync(topicName, subscriptionName));
        } catch (MessagingEntityAlreadyExistsException e) { }

        this.managementClientAsync.deleteQueueAsync(queueName).get();
        this.managementClientAsync.deleteSubscriptionAsync(topicName, subscriptionName).get();
        this.managementClientAsync.deleteTopicAsync(topicName).get();
    }

    @Test
    public void forwardingEntitySetupTest() throws ServiceBusException, InterruptedException {
        // queueName -- fwdTo --> destinationName -- fwd dlqTo --> dlqDestinationName
        String queueName = UUID.randomUUID().toString().substring(0, 8);
        String destinationName = UUID.randomUUID().toString().substring(0, 8);
        String dlqDestinationName = UUID.randomUUID().toString().substring(0, 8);

        QueueDescription dqlDestinationQ = Utils.completeFuture(this.managementClientAsync.createQueueAsync(dlqDestinationName));
        QueueDescription destinationQToCreate = new QueueDescription(destinationName);
        destinationQToCreate.setForwardDeadLetteredMessagesTo(dlqDestinationName);
        QueueDescription destinationQ = Utils.completeFuture(this.managementClientAsync.createQueueAsync(destinationQToCreate));

        QueueDescription qd = new QueueDescription(queueName);
        qd.setForwardTo(destinationName);
        QueueDescription baseQ = Utils.completeFuture(this.managementClientAsync.createQueueAsync(qd));

        MessagingFactory factory = MessagingFactory.createFromNamespaceEndpointURI(TestUtils.getNamespaceEndpointURI(), TestUtils.getClientSettings());
        IMessageSender sender = ClientFactory.createMessageSenderFromEntityPath(factory, queueName);
        IMessage message = new Message();
        message.setMessageId("mid");
        sender.send(message);
        sender.close();

        IMessageReceiver receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, destinationName);
        IMessage msg = receiver.receive();
        Assert.assertNotNull(msg);
        Assert.assertEquals("mid", msg.getMessageId());
        receiver.deadLetter(msg.getLockToken());
        receiver.close();

        receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, dlqDestinationName);
        msg = receiver.receive();
        Assert.assertNotNull(msg);
        Assert.assertEquals("mid", msg.getMessageId());
        receiver.complete(msg.getLockToken());
        receiver.close();

        this.managementClientAsync.deleteQueueAsync(queueName);
        this.managementClientAsync.deleteQueueAsync(destinationName);
        this.managementClientAsync.deleteQueueAsync(dlqDestinationName);
    }
    
    @Test
    public void subscriptionForwardToCreationTest() throws ServiceBusException, InterruptedException {
        String sourceName = UUID.randomUUID().toString().substring(0, 8);
        String destinationName = UUID.randomUUID().toString().substring(0, 8);
        String subscriptionName = "subscription1";
        
        TopicDescription destinationTopicDesc = new TopicDescription(destinationName);
        Utils.completeFuture(this.managementClientAsync.createTopicAsync(destinationTopicDesc));
        SubscriptionDescription subDesc = new SubscriptionDescription(destinationName, subscriptionName);
        Utils.completeFuture(this.managementClientAsync.createSubscriptionAsync(subDesc));

        TopicDescription sourceTopicDesc = new TopicDescription(sourceName);
        Utils.completeFuture(this.managementClientAsync.createTopicAsync(sourceTopicDesc));
        SubscriptionDescription sourceSubDesc = new SubscriptionDescription(sourceName, subscriptionName);
        sourceSubDesc.setForwardTo(destinationName);
        Utils.completeFuture(this.managementClientAsync.createSubscriptionAsync(sourceSubDesc));

        MessagingFactory factory = MessagingFactory.createFromNamespaceEndpointURI(TestUtils.getNamespaceEndpointURI(), TestUtils.getClientSettings());
        IMessageSender sender = ClientFactory.createMessageSenderFromEntityPath(factory, sourceName);
        IMessage message = new Message();
        message.setMessageId("mid");
        sender.send(message);
        sender.close();

        IMessageReceiver receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, destinationName + "/subscriptions/" + subscriptionName);
        IMessage msg = receiver.receive();
        Assert.assertNotNull(msg);
        Assert.assertEquals("mid", msg.getMessageId());
        receiver.complete(msg.getLockToken());
        receiver.close();

        this.managementClientAsync.deleteTopicAsync(sourceName);
        this.managementClientAsync.deleteTopicAsync(destinationName);
    }

    @Test
    public void authRulesEqualityCheckTest() {
        QueueDescription qd = new QueueDescription("a");
        SharedAccessAuthorizationRule rule1 = new SharedAccessAuthorizationRule("sendListen", new ArrayList<>(Arrays.asList(AccessRights.Listen, AccessRights.Send)));
        SharedAccessAuthorizationRule rule2 = new SharedAccessAuthorizationRule("manage", new ArrayList<>(Arrays.asList(AccessRights.Listen, AccessRights.Send, AccessRights.Manage)));
        qd.setAuthorizationRules(new ArrayList<>(Arrays.asList(rule1, rule2)));

        QueueDescription qd2 = new QueueDescription("a");
        AuthorizationRule rule11 = new SharedAccessAuthorizationRule(rule2.getKeyName(), rule2.getPrimaryKey(), rule2.getSecondaryKey(),  rule2.getRights());
        AuthorizationRule rule22 = new SharedAccessAuthorizationRule(rule1.getKeyName(), rule1.getPrimaryKey(), rule1.getSecondaryKey(), rule1.getRights());
        qd2.setAuthorizationRules(new ArrayList<>(Arrays.asList(rule11, rule22)));

        Assert.assertTrue(qd.equals(qd2));
    }

    @Test
    public void getNamespaceInfoTest() throws ExecutionException, InterruptedException {
        NamespaceInfo nsInfo = this.managementClientAsync.getNamespaceInfoAsync().get();
        Assert.assertNotNull(nsInfo);
        Assert.assertTrue(nsInfo.getNamespaceType() == NamespaceType.ServiceBus || nsInfo.getNamespaceType() == NamespaceType.Mixed);
    }
    
    @Test
    public void unknownQueueDescriptionElementsTest() throws Exception {
    	String queueDescriptionXml = "<entry xmlns=\"http://www.w3.org/2005/Atom\">" +
                "<title xmlns=\"http://www.w3.org/2005/Atom\">testqueue1</title>" +
                "<content xmlns=\"http://www.w3.org/2005/Atom\">" +
                "<QueueDescription xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\">" +
                "<LockDuration>PT1M</LockDuration>" +
                "<MaxSizeInMegabytes>1024</MaxSizeInMegabytes>" +
                "<RequiresDuplicateDetection>true</RequiresDuplicateDetection>" +
                "<RequiresSession>true</RequiresSession>" +
                "<DefaultMessageTimeToLive>PT1H</DefaultMessageTimeToLive>" +
                "<DeadLetteringOnMessageExpiration>false</DeadLetteringOnMessageExpiration>" +
                "<DuplicateDetectionHistoryTimeWindow>PT2M</DuplicateDetectionHistoryTimeWindow>" +
                "<MaxDeliveryCount>10</MaxDeliveryCount>" +
                "<EnableBatchedOperations>true</EnableBatchedOperations>" +
                "<IsAnonymousAccessible>false</IsAnonymousAccessible>" +
                "<Status>Active</Status>" +
                "<ForwardTo>fq1</ForwardTo>" +
                "<UserMetadata>abcd</UserMetadata>" +
                "<SupportOrdering>true</SupportOrdering>" +
                "<AutoDeleteOnIdle>PT1H</AutoDeleteOnIdle>" +
                "<EnablePartitioning>false</EnablePartitioning>" +
                "<EnableExpress>false</EnableExpress>" +
                "<UnknownElement1>prop1</UnknownElement1>" +
                "<UnknownElement2>prop2</UnknownElement2>" +
                "<UnknownElement3>prop3</UnknownElement3>" +
                "<UnknownElement4>prop4</UnknownElement4>" +
                "<UnknownElement5><PropertyValue>prop5</PropertyValue></UnknownElement5>" +
                "</QueueDescription>" +
                "</content>" +
                "</entry>";
    	
    	QueueDescription queueDesc = QueueDescriptionSerializer.parseFromContent(queueDescriptionXml);
    	String serializedXml = QueueDescriptionSerializer.serialize(queueDesc);
    	
    	// Compare xml nodes
    	Document expectedDoc = loadXmlFromString(queueDescriptionXml);
    	Element expectedElement = (Element) expectedDoc.getElementsByTagNameNS("http://schemas.microsoft.com/netservices/2010/10/servicebus/connect", "QueueDescription").item(0);
    	Document serializedDoc = loadXmlFromString(serializedXml);
    	Element serializedElement = (Element) serializedDoc.getElementsByTagNameNS("http://schemas.microsoft.com/netservices/2010/10/servicebus/connect", "QueueDescription").item(0);
    	Assert.assertTrue("QueueDescrition parsing and serialization combo didn't work as expected", elementEquals(expectedElement, serializedElement));
    }
    
    @Test
    public void unknownSubscriptionDescriptionElementsTest() throws Exception {
    	String subscriptionDescriptionXml = "<entry xmlns=\"http://www.w3.org/2005/Atom\">" +
                "<title xmlns=\"http://www.w3.org/2005/Atom\">testqueue1</title>" +
                "<content xmlns=\"http://www.w3.org/2005/Atom\">" +
                "<SubscriptionDescription xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\">" +
                "<LockDuration>PT1M</LockDuration>" +
                "<RequiresSession>true</RequiresSession>" +
                "<DefaultMessageTimeToLive>PT1H</DefaultMessageTimeToLive>" +
                "<DeadLetteringOnMessageExpiration>false</DeadLetteringOnMessageExpiration>" +
                "<DeadLetteringOnFilterEvaluationExceptions>false</DeadLetteringOnFilterEvaluationExceptions>" +
                "<MaxDeliveryCount>10</MaxDeliveryCount>" +
                "<EnableBatchedOperations>true</EnableBatchedOperations>" +
                "<Status>Active</Status>" +
                "<ForwardTo>fq1</ForwardTo>" +
                "<UserMetadata>abcd</UserMetadata>" +
                "<AutoDeleteOnIdle>PT1H</AutoDeleteOnIdle>" +
                "<IsClientAffine>prop1</IsClientAffine>" +
                "<ClientAffineProperties><ClientId>xyz</ClientId><IsDurable>false</IsDurable><IsShared>true</IsShared></ClientAffineProperties>" +
                "<UnknownElement2>prop2</UnknownElement2>" +
                "<UnknownElement3>prop3</UnknownElement3>" +
                "<UnknownElement4>prop4</UnknownElement4>" +
                "</SubscriptionDescription>" +
                "</content>" +
                "</entry>";
    	
    	SubscriptionDescription queueDesc = SubscriptionDescriptionSerializer.parseFromContent("abcd", subscriptionDescriptionXml);
    	String serializedXml = SubscriptionDescriptionSerializer.serialize(queueDesc);
    	
    	// Compare xml nodes
    	Document expectedDoc = loadXmlFromString(subscriptionDescriptionXml);
    	Element expectedElement = (Element) expectedDoc.getElementsByTagNameNS("http://schemas.microsoft.com/netservices/2010/10/servicebus/connect", "SubscriptionDescription").item(0);
    	Document serializedDoc = loadXmlFromString(serializedXml);
    	Element serializedElement = (Element) serializedDoc.getElementsByTagNameNS("http://schemas.microsoft.com/netservices/2010/10/servicebus/connect", "SubscriptionDescription").item(0);
    	Assert.assertTrue("SubscriptionDescrition parsing and serialization combo didn't work as expected", elementEquals(expectedElement, serializedElement));
    }
    
    @Test
    public void unknownTopicDescriptionElementsTest() throws Exception {
    	String topicDescriptionXml = "<entry xmlns=\"http://www.w3.org/2005/Atom\">" +
                "<title xmlns=\"http://www.w3.org/2005/Atom\">testqueue1</title>" +
                "<content xmlns=\"http://www.w3.org/2005/Atom\">" +
                "<TopicDescription xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\">" +
                "<DefaultMessageTimeToLive>PT1H</DefaultMessageTimeToLive>" +                
                "<MaxSizeInMegabytes>1024</MaxSizeInMegabytes>" +
                "<RequiresDuplicateDetection>true</RequiresDuplicateDetection>" +
                "<DuplicateDetectionHistoryTimeWindow>PT2M</DuplicateDetectionHistoryTimeWindow>" +
                "<EnableBatchedOperations>true</EnableBatchedOperations>" +
                "<FilteringMessagesBeforePublishing>false</FilteringMessagesBeforePublishing>" +
                "<IsAnonymousAccessible>false</IsAnonymousAccessible>" +
                "<Status>Active</Status>" +
                "<UserMetadata>abcd</UserMetadata>" +
                "<SupportOrdering>true</SupportOrdering>" +
                "<AutoDeleteOnIdle>PT1H</AutoDeleteOnIdle>" +
                "<EnablePartitioning>false</EnablePartitioning>" +
                "<EnableSubscriptionPartitioning>false</EnableSubscriptionPartitioning>" +
                "<EnableExpress>false</EnableExpress>" +
                "<UnknownElement1>prop1</UnknownElement1>" +
                "<UnknownElement2>prop2</UnknownElement2>" +
                "<UnknownElement3>prop3</UnknownElement3>" +
                "<UnknownElement4>prop4</UnknownElement4>" +
                "<UnknownElement5><PropertyValue>prop5</PropertyValue></UnknownElement5>" +
                "</TopicDescription>" +
                "</content>" +
                "</entry>";
    	
    	TopicDescription topicDesc = TopicDescriptionSerializer.parseFromContent(topicDescriptionXml);
    	String serializedXml = TopicDescriptionSerializer.serialize(topicDesc);
    	
    	// Compare xml nodes
    	Document expectedDoc = loadXmlFromString(topicDescriptionXml);
    	Element expectedElement = (Element) expectedDoc.getElementsByTagNameNS("http://schemas.microsoft.com/netservices/2010/10/servicebus/connect", "TopicDescription").item(0);
    	Document serializedDoc = loadXmlFromString(serializedXml);
    	Element serializedElement = (Element) serializedDoc.getElementsByTagNameNS("http://schemas.microsoft.com/netservices/2010/10/servicebus/connect", "TopicDescription").item(0);
    	Assert.assertTrue("TopicDescrition parsing and serialization combo didn't work as expected", elementEquals(expectedElement, serializedElement));
    }
    
    private static Document loadXmlFromString(String xml) throws Exception {    	
            DocumentBuilderFactory dbf = SerializerUtil.getDocumentBuilderFactory();
            dbf.setIgnoringComments(true);
            dbf.setIgnoringElementContentWhitespace(true);
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(new ByteArrayInputStream(xml.getBytes("utf-8")));            
            dom.normalize();
            return dom;
    }
    
    // Basic comparison just sufficient for our test
    private static boolean elementEquals(Node first, Node second) {    	
    	if (!first.getLocalName().equals(second.getLocalName()))
    	{
    		return false;
    	}
    	
    	NamedNodeMap firstAttributes = first.getAttributes();
    	NamedNodeMap secondAttributes = second.getAttributes();
    	if (firstAttributes != null && secondAttributes != null) {
    		if (firstAttributes.getLength() != secondAttributes.getLength()) {
    			return false;
    		}
    		
    		for (int i = 0; i < firstAttributes.getLength(); i++) {
    			Attr firstAttr = (Attr) firstAttributes.item(i);
    			Attr secondAttr = (Attr) secondAttributes.getNamedItem(firstAttr.getName());
    			if (secondAttr == null) {
    				return false;
    			}
    			
    			if (!firstAttr.getValue().equals(secondAttr.getValue())) {
    				return false;
    			}
    		}
    	}
    	
    	NodeList firstChildren = first.getChildNodes();
    	NodeList secondChildren = second.getChildNodes();
    	if (firstChildren.getLength() != secondChildren.getLength()) {
    		return  false;
    	}
    	
    	for (int i = 0; i < firstChildren.getLength(); i++) {
    		Node childFirst = firstChildren.item(i);
    		Node childSecond = secondChildren.item(i);
    		
    		if (childFirst.getNodeType() != childSecond.getNodeType()) {
    			return false;
    		}
    		
    		if (childFirst.getNodeType() == Node.TEXT_NODE) {
    			if (!textEquals((Text)childFirst, (Text)childSecond)) {
    				return false;
    			}
    		}
    		else if (childFirst.getNodeType() == Node.ELEMENT_NODE){
    			if (!elementEquals((Element)childFirst, (Element)childSecond)) {
    				return false;
    			}
    		}
    			
    	}
    	
    	return true;
    }
    
    private static boolean textEquals(Text first, Text second) {
    	if (first == null ^ second == null)
    	{
    		return false;
    	}
    	
    	return first.getNodeValue().equals(second.getNodeValue());
    }
    
}
