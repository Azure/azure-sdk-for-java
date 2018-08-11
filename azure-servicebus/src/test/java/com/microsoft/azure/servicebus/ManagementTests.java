package com.microsoft.azure.servicebus;

import com.microsoft.azure.servicebus.management.*;
import com.microsoft.azure.servicebus.primitives.*;
import com.microsoft.azure.servicebus.rules.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ManagementTests {

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

        ((CorrelationFilter)(rule2.getFilter())).setCorrelationId("correlationIdModified");
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
        } catch (MessagingEntityNotFoundException e) {}

        try {
            Utils.completeFuture(this.managementClientAsync.getTopicAsync("NonExistingPath"));
        } catch (MessagingEntityNotFoundException e) {}

        try {
            Utils.completeFuture(this.managementClientAsync.getSubscriptionAsync("NonExistingTopic", "NonExistingPath"));
        } catch (MessagingEntityNotFoundException e) {}

        try {
            Utils.completeFuture(this.managementClientAsync.updateQueueAsync(new QueueDescription("NonExistingPath")));
        } catch (MessagingEntityNotFoundException e) {}

        try {
            Utils.completeFuture(this.managementClientAsync.updateTopicAsync(new TopicDescription("NonExistingPath")));
        } catch (MessagingEntityNotFoundException e) {}

        try {
            Utils.completeFuture(this.managementClientAsync.updateSubscriptionAsync(new SubscriptionDescription("NonExistingTopic", "NonExistingPath")));
        } catch (MessagingEntityNotFoundException e) {}

        try {
            Utils.completeFuture(this.managementClientAsync.deleteQueueAsync("NonExistingPath"));
        } catch (MessagingEntityNotFoundException e) {}

        try {
            Utils.completeFuture(this.managementClientAsync.deleteTopicAsync("NonExistingPath"));
        } catch (MessagingEntityNotFoundException e) {}

        try {
            Utils.completeFuture(this.managementClientAsync.deleteSubscriptionAsync("nonExistingTopic", "NonExistingPath"));
        } catch (MessagingEntityNotFoundException e) {}

        String queueName = UUID.randomUUID().toString().substring(0, 8);
        String topicName = UUID.randomUUID().toString().substring(0, 8);

        this.managementClientAsync.createQueueAsync(queueName).get();
        this.managementClientAsync.createTopicAsync(topicName).get();

        try {
            Utils.completeFuture(this.managementClientAsync.getQueueAsync(topicName));
        } catch (MessagingEntityNotFoundException e) {}

        try {
            Utils.completeFuture(this.managementClientAsync.getTopicAsync(queueName));
        } catch (MessagingEntityNotFoundException e) {}

        try {
            Utils.completeFuture(this.managementClientAsync.getSubscriptionAsync(topicName, "NonExistingSubscription"));
        } catch (MessagingEntityNotFoundException e) {}

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
        } catch (MessagingEntityAlreadyExistsException e) {}

        try {
            Utils.completeFuture(this.managementClientAsync.createTopicAsync(topicName));
        } catch (MessagingEntityAlreadyExistsException e) {}

        try {
            Utils.completeFuture(this.managementClientAsync.createSubscriptionAsync(topicName, subscriptionName));
        } catch (MessagingEntityAlreadyExistsException e) {}

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
}
