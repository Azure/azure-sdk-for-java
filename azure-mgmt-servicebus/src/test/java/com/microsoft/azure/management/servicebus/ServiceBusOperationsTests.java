/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.servicebus.implementation.ServiceBusManager;
import com.microsoft.rest.RestClient;
import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Test;
import java.util.concurrent.TimeUnit;

public class ServiceBusOperationsTests extends TestBase {
    protected static ResourceManager resourceManager;
    protected static ServiceBusManager serviceBusManager;
    protected static String RG_NAME = "";

    @Override
    protected RestClient buildRestClient(RestClient.Builder builder, boolean isMocked) {
        if (!isMocked) {
            return super.buildRestClient(builder, isMocked);
        }
        return super.buildRestClient(builder.withReadTimeout(100, TimeUnit.SECONDS), isMocked);
    }

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javasb", 20);

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(defaultSubscription);

        serviceBusManager = ServiceBusManager
                .authenticate(restClient, defaultSubscription);
    }

    @Override
    protected void cleanUpResources() {
        if (RG_NAME != null) {
            resourceManager.resourceGroups().deleteByName(RG_NAME);
        }
    }

    @Test
    public void canCRUDOnSimpleNamespace() {
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> rgCreatable = resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(region);

        String namespaceDNSLabel = generateRandomResourceName("jvsbns", 15);
        serviceBusManager.namespaces()
                .define(namespaceDNSLabel)
                .withRegion(region)
                .withNewResourceGroup(rgCreatable)
                .withSku(NamespaceSku.PREMIUM_CAPACITY1)
                .create();

        ServiceBusNamespace namespace = serviceBusManager.namespaces()
                .getByResourceGroup(RG_NAME, namespaceDNSLabel);
        Assert.assertNotNull(namespace);
        Assert.assertNotNull(namespace.inner());

        PagedList<ServiceBusNamespace> namespaces = serviceBusManager.namespaces().listByResourceGroup(RG_NAME);
        Assert.assertNotNull(namespaces);
        Assert.assertTrue(namespaces.size() > 0);
        boolean found = false;
        for (ServiceBusNamespace n : namespaces) {
            if (n.name().equalsIgnoreCase(namespace.name())) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);

        Assert.assertNotNull(namespace.dnsLabel());
        Assert.assertTrue(namespace.dnsLabel().equalsIgnoreCase(namespaceDNSLabel));
        Assert.assertNotNull(namespace.fqdn());
        Assert.assertTrue(namespace.fqdn().contains(namespaceDNSLabel));
        Assert.assertNotNull(namespace.sku());
        Assert.assertTrue(namespace.sku().equals(NamespaceSku.PREMIUM_CAPACITY1));
        Assert.assertNotNull(namespace.region());
        Assert.assertTrue(namespace.region().equals(region));
        Assert.assertNotNull(namespace.resourceGroupName());
        Assert.assertTrue(namespace.resourceGroupName().equalsIgnoreCase(RG_NAME));
        Assert.assertNotNull(namespace.createdAt());
        Assert.assertNotNull(namespace.queues());
        Assert.assertEquals(0, namespace.queues().list().size());
        Assert.assertNotNull(namespace.topics());
        Assert.assertEquals(0, namespace.topics().list().size());
        Assert.assertNotNull(namespace.authorizationRules());
        PagedList<NamespaceAuthorizationRule> defaultNsRules = namespace.authorizationRules().list();
        Assert.assertEquals(1, defaultNsRules.size());
        NamespaceAuthorizationRule defaultNsRule = defaultNsRules.get(0);
        Assert.assertTrue(defaultNsRule.name().equalsIgnoreCase("RootManageSharedAccessKey"));
        Assert.assertNotNull(defaultNsRule.rights());
        Assert.assertNotNull(defaultNsRule.namespaceName());
        Assert.assertTrue(defaultNsRule.namespaceName().equalsIgnoreCase(namespaceDNSLabel));
        Assert.assertNotNull(defaultNsRule.resourceGroupName());
        Assert.assertTrue(defaultNsRule.resourceGroupName().equalsIgnoreCase(RG_NAME));
        namespace.update()
                .withSku(NamespaceSku.PREMIUM_CAPACITY2)
                .apply();
        Assert.assertTrue(namespace.sku().equals(NamespaceSku.PREMIUM_CAPACITY2));
        // TODO: There is a bug in LRO implementation of ServiceBusNamespace DELETE operation (Last poll returns 404, reported this to RP]
        //
        // serviceBusManager.namespaces().deleteByGroup(RG_NAME, namespace.name());
    }

    @Test
    public void canCreateNamespaceThenCRUDOnQueue() {
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> rgCreatable = resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(region);

        String namespaceDNSLabel = generateRandomResourceName("jvsbns", 15);
        ServiceBusNamespace namespace = serviceBusManager.namespaces()
                .define(namespaceDNSLabel)
                .withRegion(region)
                .withNewResourceGroup(rgCreatable)
                .withSku(NamespaceSku.PREMIUM_CAPACITY1)
                .create();
        Assert.assertNotNull(namespace);
        Assert.assertNotNull(namespace.inner());

        String queueName = generateRandomResourceName("queue1-", 15);
        Queue queue = namespace.queues()
                .define(queueName)
                .create();

        Assert.assertNotNull(queue);
        Assert.assertNotNull(queue.inner());
        Assert.assertNotNull(queue.name());
        Assert.assertTrue(queue.name().equalsIgnoreCase(queueName));
        // Default lock duration is 1 minute, assert TimeSpan("00:01:00") parsing
        //
        Assert.assertEquals("00:01:00", queue.inner().lockDuration());
        Assert.assertEquals(60, queue.lockDurationInSeconds());

        Period dupDetectionDuration = queue.duplicateMessageDetectionHistoryDuration();
        Assert.assertNotNull(dupDetectionDuration);
        Assert.assertEquals(10, dupDetectionDuration.getMinutes());
        // Default message TTL is TimeSpan.Max, assert parsing
        //
        Assert.assertEquals("10675199.02:48:05.4775807", queue.inner().defaultMessageTimeToLive());
        Period msgTtlDuration = queue.defaultMessageTtlDuration();
        Assert.assertNotNull(msgTtlDuration);
        // Assert the default ttl TimeSpan("10675199.02:48:05.4775807") parsing
        //
        Assert.assertEquals(10675199, msgTtlDuration.getDays());
        Assert.assertEquals(2, msgTtlDuration.getHours());
        Assert.assertEquals(48, msgTtlDuration.getMinutes());
        // Assert the default max size In MB
        //
        Assert.assertEquals(1024, queue.maxSizeInMB());

        PagedList<Queue> queuesInNamespace = namespace.queues().list();
        Assert.assertNotNull(queuesInNamespace);
        Assert.assertTrue(queuesInNamespace.size() > 0);
        Queue foundQueue = null;
        for (Queue q : queuesInNamespace) {
            if (q.name().equalsIgnoreCase(queueName)) {
                foundQueue = q;
                break;
            }
        }
        Assert.assertNotNull(foundQueue);
        // Dead lettering disabled by default
        //
        Assert.assertFalse(foundQueue.isDeadLetteringEnabledForExpiredMessages());
        foundQueue = foundQueue.update()
                .withMessageLockDurationInSeconds(120)
                .withDefaultMessageTTL(new Period().withMinutes(20))
                .withExpiredMessageMovedToDeadLetterQueue()
                .withMessageMovedToDeadLetterQueueOnMaxDeliveryCount(25)
                .apply();
        Assert.assertEquals(120, foundQueue.lockDurationInSeconds());
        Assert.assertTrue(foundQueue.isDeadLetteringEnabledForExpiredMessages());
        Assert.assertEquals(25, foundQueue.maxDeliveryCountBeforeDeadLetteringMessage());
        namespace.queues().deleteByName(foundQueue.name());
    }

    @Test
    public void canCreateDeleteQueueWithNamespace() {
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> rgCreatable = resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(region);

        String namespaceDNSLabel = generateRandomResourceName("jvsbns", 15);
        String queueName = generateRandomResourceName("queue1-", 15);
        // Create NS with Queue
        //
        ServiceBusNamespace namespace = serviceBusManager.namespaces()
                .define(namespaceDNSLabel)
                .withRegion(region)
                .withNewResourceGroup(rgCreatable)
                .withSku(NamespaceSku.PREMIUM_CAPACITY1)
                .withNewQueue(queueName, 1024)
                .create();
        Assert.assertNotNull(namespace);
        Assert.assertNotNull(namespace.inner());
        // Lookup queue
        //
        PagedList<Queue> queuesInNamespace = namespace.queues().list();
        Assert.assertNotNull(queuesInNamespace);
        Assert.assertEquals(1, queuesInNamespace.size());
        Queue foundQueue = null;
        for (Queue q : queuesInNamespace) {
            if (q.name().equalsIgnoreCase(queueName)) {
                foundQueue = q;
                break;
            }
        }
        Assert.assertNotNull(foundQueue);
        // Remove Queue
        //
        namespace.update()
                .withoutQueue(queueName)
                .apply();
        queuesInNamespace = namespace.queues().list();
        Assert.assertNotNull(queuesInNamespace);
        Assert.assertEquals(0, queuesInNamespace.size());
    }

    @Test
    public void canCreateNamespaceThenCRUDOnTopic() {
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> rgCreatable = resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(region);

        String namespaceDNSLabel = generateRandomResourceName("jvsbns", 15);
        ServiceBusNamespace namespace = serviceBusManager.namespaces()
                .define(namespaceDNSLabel)
                .withRegion(region)
                .withNewResourceGroup(rgCreatable)
                .withSku(NamespaceSku.STANDARD)
                .create();
        Assert.assertNotNull(namespace);
        Assert.assertNotNull(namespace.inner());

        String topicName = generateRandomResourceName("topic1-", 15);
        Topic topic = namespace.topics()
                .define(topicName)
                .create();

        Assert.assertNotNull(topic);
        Assert.assertNotNull(topic.inner());
        Assert.assertNotNull(topic.name());
        Assert.assertTrue(topic.name().equalsIgnoreCase(topicName));

        Period dupDetectionDuration = topic.duplicateMessageDetectionHistoryDuration();
        Assert.assertNotNull(dupDetectionDuration);
        Assert.assertEquals(10, dupDetectionDuration.getMinutes());
        // Default message TTL is TimeSpan.Max, assert parsing
        //
        Assert.assertEquals("10675199.02:48:05.4775807", topic.inner().defaultMessageTimeToLive());
        Period msgTtlDuration = topic.defaultMessageTtlDuration();
        Assert.assertNotNull(msgTtlDuration);
        // Assert the default ttl TimeSpan("10675199.02:48:05.4775807") parsing
        //
        Assert.assertEquals(10675199, msgTtlDuration.getDays());
        Assert.assertEquals(2, msgTtlDuration.getHours());
        Assert.assertEquals(48, msgTtlDuration.getMinutes());
        // Assert the default max size In MB
        //
        Assert.assertEquals(1024, topic.maxSizeInMB());

        PagedList<Topic> topicsInNamespace = namespace.topics().list();
        Assert.assertNotNull(topicsInNamespace);
        Assert.assertTrue(topicsInNamespace.size() > 0);
        Topic foundTopic = null;
        for (Topic t : topicsInNamespace) {
            if (t.name().equalsIgnoreCase(topic.name())) {
                foundTopic = t;
                break;
            }
        }
        Assert.assertNotNull(foundTopic);
        foundTopic = foundTopic.update()
                .withDefaultMessageTTL(new Period().withMinutes(20))
                .withDuplicateMessageDetectionHistoryDuration(new Period().withMinutes(15))
                .withDeleteOnIdleDurationInMinutes(25)
                .apply();
        Period ttlDuration = foundTopic.defaultMessageTtlDuration();
        Assert.assertNotNull(ttlDuration);
        Assert.assertEquals(20, ttlDuration.getMinutes());
        Period duplicateDetectDuration = foundTopic.duplicateMessageDetectionHistoryDuration();
        Assert.assertNotNull(duplicateDetectDuration);
        Assert.assertEquals(15, duplicateDetectDuration.getMinutes());
        Assert.assertEquals(25, foundTopic.deleteOnIdleDurationInMinutes());
        // Delete
        namespace.topics().deleteByName(foundTopic.name());
    }

    @Test
    public void canCreateDeleteTopicWithNamespace() {
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> rgCreatable = resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(region);

        String namespaceDNSLabel = generateRandomResourceName("jvsbns", 15);
        String topicName = generateRandomResourceName("topic1-", 15);
        // Create NS with Topic
        //
        ServiceBusNamespace namespace = serviceBusManager.namespaces()
                .define(namespaceDNSLabel)
                .withRegion(region)
                .withNewResourceGroup(rgCreatable)
                .withSku(NamespaceSku.PREMIUM_CAPACITY1)
                .withNewTopic(topicName, 1024)
                .create();
        Assert.assertNotNull(namespace);
        Assert.assertNotNull(namespace.inner());
        // Lookup topic
        //
        PagedList<Topic> topicsInNamespace = namespace.topics().list();
        Assert.assertNotNull(topicsInNamespace);
        Assert.assertEquals(1, topicsInNamespace.size());
        Topic foundTopic = null;
        for (Topic t : topicsInNamespace) {
            if (t.name().equalsIgnoreCase(topicName)) {
                foundTopic = t;
                break;
            }
        }
        Assert.assertNotNull(foundTopic);
        // Remove Topic
        //
        namespace.update()
                .withoutTopic(topicName)
                .apply();
        topicsInNamespace = namespace.topics().list();
        Assert.assertNotNull(topicsInNamespace);
        Assert.assertEquals(0, topicsInNamespace.size());
    }

    @Test
    public void canOperateOnAuthorizationRules() {
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> rgCreatable = resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(region);

        String namespaceDNSLabel = generateRandomResourceName("jvsbns", 15);
        String queueName = generateRandomResourceName("queue1-", 15);
        String topicName = generateRandomResourceName("topic1-", 15);
        String nsRuleName = generateRandomResourceName("nsrule1-", 15);
        // Create NS with Queue, Topic and authorization rule
        //
        ServiceBusNamespace namespace = serviceBusManager.namespaces()
                .define(namespaceDNSLabel)
                .withRegion(region)
                .withNewResourceGroup(rgCreatable)
                .withSku(NamespaceSku.PREMIUM_CAPACITY1)
                .withNewQueue(queueName, 1024)
                .withNewTopic(topicName, 1024)
                .withNewManageRule(nsRuleName)
                .create();
        // Lookup ns authorization rule
        //
        PagedList<NamespaceAuthorizationRule> rulesInNamespace = namespace.authorizationRules().list();
        Assert.assertNotNull(rulesInNamespace);
        Assert.assertEquals(2, rulesInNamespace.size());    // Default + one explicit

        NamespaceAuthorizationRule foundNsRule = null;
        for (NamespaceAuthorizationRule rule : rulesInNamespace) {
            if (rule.name().equalsIgnoreCase(nsRuleName)) {
                foundNsRule = rule;
                break;
            }
        }
        Assert.assertNotNull(foundNsRule);
        AuthorizationKeys nsRuleKeys = foundNsRule.getKeys();
        Assert.assertNotNull(nsRuleKeys);
        Assert.assertNotNull(nsRuleKeys.inner());
        String primaryKey = nsRuleKeys.primaryKey();
        Assert.assertNotNull(primaryKey);
        Assert.assertNotNull(nsRuleKeys.secondaryKey());
        Assert.assertNotNull(nsRuleKeys.primaryConnectionString());
        Assert.assertNotNull(nsRuleKeys.secondaryConnectionString());
        nsRuleKeys = foundNsRule.regenerateKey(Policykey.PRIMARY_KEY);
        Assert.assertNotEquals(nsRuleKeys.primaryKey(), primaryKey);
        // Lookup queue & operate on auth rules
        //
        PagedList<Queue> queuesInNamespace = namespace.queues().list();
        Assert.assertNotNull(queuesInNamespace);
        Assert.assertEquals(1, queuesInNamespace.size());
        Queue queue = queuesInNamespace.get(0);
        Assert.assertNotNull(queue);
        Assert.assertNotNull(queue.inner());

        QueueAuthorizationRule qRule = queue.authorizationRules()
                .define("rule1")
                .withListeningEnabled()
                .create();
        Assert.assertNotNull(qRule);
        Assert.assertNotNull(qRule.rights().contains(AccessRights.LISTEN));
        qRule = qRule.update()
                .withManagementEnabled()
                .apply();
        Assert.assertNotNull(qRule.rights().contains(AccessRights.MANAGE));
        PagedList<QueueAuthorizationRule> rulesInQueue = queue.authorizationRules().list();
        Assert.assertTrue(rulesInQueue.size() > 0);
        boolean foundQRule = false;
        for (QueueAuthorizationRule r : rulesInQueue) {
            if (r.name().equalsIgnoreCase(qRule.name())) {
                foundQRule = true;
                break;
            }
        }
        Assert.assertTrue(foundQRule);
        queue.authorizationRules().deleteByName(qRule.name());
        // Lookup topic & operate on auth rules
        //
        PagedList<Topic> topicsInNamespace = namespace.topics().list();
        Assert.assertNotNull(topicsInNamespace);
        Assert.assertEquals(1, topicsInNamespace.size());
        Topic topic = topicsInNamespace.get(0);
        Assert.assertNotNull(topic);
        Assert.assertNotNull(topic.inner());
        TopicAuthorizationRule tRule = topic.authorizationRules()
                .define("rule2")
                .withSendingEnabled()
                .create();
        Assert.assertNotNull(tRule);
        Assert.assertNotNull(tRule.rights().contains(AccessRights.SEND));
        tRule = tRule.update()
                .withManagementEnabled()
                .apply();
        Assert.assertNotNull(tRule.rights().contains(AccessRights.MANAGE));
        PagedList<TopicAuthorizationRule> rulesInTopic = topic.authorizationRules().list();
        Assert.assertTrue(rulesInTopic.size() > 0);
        boolean foundTRule = false;
        for (TopicAuthorizationRule r : rulesInTopic) {
            if (r.name().equalsIgnoreCase(tRule.name())) {
                foundTRule = true;
                break;
            }
        }
        Assert.assertTrue(foundTRule);
        topic.authorizationRules().deleteByName(tRule.name());
    }

    @Test
    public void canPerformOnNamespaceActions() {
        RG_NAME = null;
        String namespaceDNSLabel = generateRandomResourceName("jvsbns", 15);
        CheckNameAvailabilityResult availabilityResult = serviceBusManager
                .namespaces()
                .checkNameAvailability(namespaceDNSLabel);
        Assert.assertNotNull(availabilityResult);
        if (!availabilityResult.isAvailable()) {
            Assert.assertNotNull(availabilityResult.unavailabilityReason());
            Assert.assertNotNull(availabilityResult.unavailabilityMessage());
        }
    }

    @Test
    public void canPerformCRUDOnSubscriptions() {
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> rgCreatable = resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(region);

        String namespaceDNSLabel = generateRandomResourceName("jvsbns", 15);
        String topicName = generateRandomResourceName("topic1-", 15);
        String subscriptionName = generateRandomResourceName("sub1-", 15);
        // Create NS with Topic
        //
        ServiceBusNamespace namespace = serviceBusManager.namespaces()
                .define(namespaceDNSLabel)
                .withRegion(region)
                .withNewResourceGroup(rgCreatable)
                .withSku(NamespaceSku.PREMIUM_CAPACITY1)
                .withNewTopic(topicName, 1024)
                .create();
        // Create Topic subscriptions and list it
        //
        Topic topic = namespace.topics().getByName(topicName);
        ServiceBusSubscription subscription = topic.subscriptions().define(subscriptionName)
                .withDefaultMessageTTL(new Period().withMinutes(20))
                .create();
        Assert.assertNotNull(subscription);
        Assert.assertNotNull(subscription.inner());
        Assert.assertEquals(20, subscription.defaultMessageTtlDuration().getMinutes());
        subscription = topic.subscriptions().getByName(subscriptionName);
        Assert.assertNotNull(subscription);
        Assert.assertNotNull(subscription.inner());
        PagedList<ServiceBusSubscription> subscriptionsInTopic = topic.subscriptions().list();
        Assert.assertTrue(subscriptionsInTopic.size() > 0);
        boolean foundSubscription = false;
        for (ServiceBusSubscription s : subscriptionsInTopic) {
            if (s.name().equalsIgnoreCase(subscription.name())) {
                foundSubscription = true;
                break;
            }
        }
        Assert.assertTrue(foundSubscription);
        topic.subscriptions().deleteByName(subscriptionName);
        subscriptionsInTopic = topic.subscriptions().list();
        Assert.assertTrue(subscriptionsInTopic.size() == 0);
    }

    //@Test
    //TODO To be revisited in the future
    //public void canListServiceBusOperations() {
    //    RG_NAME = null;
    //    PagedList<ServiceBusOperation> operations = serviceBusManager.operations()
    //            .list();
    //    Assert.assertTrue(operations.size() > 0);
    //    for (ServiceBusOperation op : operations) {
    //        Assert.assertNotNull(op.name());
    //    }
    //}
}