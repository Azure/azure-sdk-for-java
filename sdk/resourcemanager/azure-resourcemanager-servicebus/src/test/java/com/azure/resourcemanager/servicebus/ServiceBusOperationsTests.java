// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.servicebus.models.AccessRights;
import com.azure.resourcemanager.servicebus.models.AuthorizationKeys;
import com.azure.resourcemanager.servicebus.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.servicebus.models.KeyType;
import com.azure.resourcemanager.servicebus.models.NamespaceAuthorizationRule;
import com.azure.resourcemanager.servicebus.models.NamespaceSku;
import com.azure.resourcemanager.servicebus.models.Queue;
import com.azure.resourcemanager.servicebus.models.QueueAuthorizationRule;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscription;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.resourcemanager.servicebus.models.TopicAuthorizationRule;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ServiceBusOperationsTests extends ResourceManagerTestBase {
    private ResourceManager resourceManager;
    private ServiceBusManager serviceBusManager;
    private String rgName = "";

    @Override
    protected HttpPipeline buildHttpPipeline(
        TokenCredential credential,
        AzureProfile profile,
        HttpLogOptions httpLogOptions,
        List<HttpPipelinePolicy> policies,
        HttpClient httpClient) {
        return HttpPipelineProvider.buildHttpPipeline(
            credential,
            profile,
            null,
            httpLogOptions,
            null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS),
            policies,
            httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javasb", 20);

        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        serviceBusManager = buildManager(ServiceBusManager.class, httpPipeline, profile);
        resourceManager = serviceBusManager.resourceManager();
    }

    @Override
    protected void cleanUpResources() {
        if (rgName != null && resourceManager != null) {
            resourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    @Test
    public void canCRUDOnSimpleNamespace() {
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> rgCreatable = resourceManager.resourceGroups()
                .define(rgName)
                .withRegion(region);

        String namespaceDNSLabel = generateRandomResourceName("jvsbns", 15);
        serviceBusManager.namespaces()
                .define(namespaceDNSLabel)
                .withRegion(region)
                .withNewResourceGroup(rgCreatable)
                .withSku(NamespaceSku.BASIC)
                .create();

        ServiceBusNamespace namespace = serviceBusManager.namespaces()
                .getByResourceGroup(rgName, namespaceDNSLabel);
        Assertions.assertNotNull(namespace);
        Assertions.assertNotNull(namespace.innerModel());

        PagedIterable<ServiceBusNamespace> namespaces = serviceBusManager.namespaces().listByResourceGroup(rgName);
        Assertions.assertNotNull(namespaces);
        Assertions.assertTrue(TestUtilities.getSize(namespaces) > 0);
        boolean found = false;
        for (ServiceBusNamespace n : namespaces) {
            if (n.name().equalsIgnoreCase(namespace.name())) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);

        Assertions.assertNotNull(namespace.dnsLabel());
        Assertions.assertTrue(namespace.dnsLabel().equalsIgnoreCase(namespaceDNSLabel));
        Assertions.assertNotNull(namespace.fqdn());
        Assertions.assertTrue(namespace.fqdn().contains(namespaceDNSLabel));
        Assertions.assertNotNull(namespace.sku());
        Assertions.assertTrue(namespace.sku().name().equals(NamespaceSku.BASIC.name()));
        Assertions.assertNotNull(namespace.region());
        Assertions.assertTrue(namespace.region().equals(region));
        Assertions.assertNotNull(namespace.resourceGroupName());
        Assertions.assertTrue(namespace.resourceGroupName().equalsIgnoreCase(rgName));
        Assertions.assertNotNull(namespace.createdAt());
        Assertions.assertNotNull(namespace.queues());
        Assertions.assertEquals(0, TestUtilities.getSize(namespace.queues().list()));
        Assertions.assertNotNull(namespace.topics());
        Assertions.assertEquals(0, TestUtilities.getSize(namespace.topics().list()));
        Assertions.assertNotNull(namespace.authorizationRules());
        PagedIterable<NamespaceAuthorizationRule> defaultNsRules = namespace.authorizationRules().list();
        Assertions.assertEquals(1, TestUtilities.getSize(defaultNsRules));
        NamespaceAuthorizationRule defaultNsRule = defaultNsRules.iterator().next();
        Assertions.assertTrue(defaultNsRule.name().equalsIgnoreCase("RootManageSharedAccessKey"));
        Assertions.assertNotNull(defaultNsRule.rights());
        Assertions.assertNotNull(defaultNsRule.namespaceName());
        Assertions.assertTrue(defaultNsRule.namespaceName().equalsIgnoreCase(namespaceDNSLabel));
        Assertions.assertNotNull(defaultNsRule.resourceGroupName());
        Assertions.assertTrue(defaultNsRule.resourceGroupName().equalsIgnoreCase(rgName));
        namespace.update()
                .withSku(NamespaceSku.STANDARD)
                .apply();
        Assertions.assertTrue(namespace.sku().name().equals(NamespaceSku.STANDARD.name()));
        serviceBusManager.namespaces().deleteByResourceGroup(rgName, namespace.name());
    }

    @Test
    public void canCreateNamespaceThenCRUDOnQueue() {
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> rgCreatable = resourceManager.resourceGroups()
                .define(rgName)
                .withRegion(region);

        String namespaceDNSLabel = generateRandomResourceName("jvsbns", 15);
        ServiceBusNamespace namespace = serviceBusManager.namespaces()
                .define(namespaceDNSLabel)
                .withRegion(region)
                .withNewResourceGroup(rgCreatable)
                .withSku(NamespaceSku.STANDARD)
                .create();
        Assertions.assertNotNull(namespace);
        Assertions.assertNotNull(namespace.innerModel());

        String queueName = generateRandomResourceName("queue1-", 15);
        Queue queue = namespace.queues()
                .define(queueName)
                .create();

        Assertions.assertNotNull(queue);
        Assertions.assertNotNull(queue.innerModel());
        Assertions.assertNotNull(queue.name());
        Assertions.assertTrue(queue.name().equalsIgnoreCase(queueName));
        // Default lock duration is 1 minute, assert TimeSpan("00:01:00") parsing
        //
        Assertions.assertEquals(Duration.ofMinutes(1), queue.innerModel().lockDuration());
        Assertions.assertEquals(60, queue.lockDurationInSeconds());

        Duration dupDetectionDuration = queue.duplicateMessageDetectionHistoryDuration();
        Assertions.assertNotNull(dupDetectionDuration);
        Assertions.assertEquals(10 * 60, dupDetectionDuration.getSeconds());
        // Default message TTL is TimeSpan.Max, assert parsing
        //
        //Assertions.assertEquals("10675199.02:48:05.4775807", queue.innerModel().defaultMessageTimeToLive());
        Duration msgTtlDuration = queue.defaultMessageTtlDuration();
        Assertions.assertNotNull(msgTtlDuration);
        // Assertions the default ttl TimeSpan("10675199.02:48:05.4775807") parsing
        //
        verifyDefaultDuration(msgTtlDuration);
        // Assertions the default max size In MB
        //
        Assertions.assertEquals(1024, queue.maxSizeInMB());

        PagedIterable<Queue> queuesInNamespace = namespace.queues().list();
        Assertions.assertNotNull(queuesInNamespace);
        Assertions.assertTrue(TestUtilities.getSize(queuesInNamespace) > 0);
        Queue foundQueue = null;
        for (Queue q : queuesInNamespace) {
            if (q.name().equalsIgnoreCase(queueName)) {
                foundQueue = q;
                break;
            }
        }
        Assertions.assertNotNull(foundQueue);
        // Dead lettering disabled by default
        //
        Assertions.assertFalse(foundQueue.isDeadLetteringEnabledForExpiredMessages());
        foundQueue = foundQueue.update()
                .withMessageLockDurationInSeconds(120)
                .withDefaultMessageTTL(Duration.ofMinutes(20))
                .withExpiredMessageMovedToDeadLetterQueue()
                .withMessageMovedToDeadLetterQueueOnMaxDeliveryCount(25)
                .apply();
        Assertions.assertEquals(120, foundQueue.lockDurationInSeconds());
        Assertions.assertTrue(foundQueue.isDeadLetteringEnabledForExpiredMessages());
        Assertions.assertEquals(25, foundQueue.maxDeliveryCountBeforeDeadLetteringMessage());
        namespace.queues().deleteByName(foundQueue.name());
    }

    @Test
    public void canCreateDeleteQueueWithNamespace() {
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> rgCreatable = resourceManager.resourceGroups()
                .define(rgName)
                .withRegion(region);

        String namespaceDNSLabel = generateRandomResourceName("jvsbns", 15);
        String queueName = generateRandomResourceName("queue1-", 15);
        // Create NS with Queue
        //
        ServiceBusNamespace namespace = serviceBusManager.namespaces()
                .define(namespaceDNSLabel)
                .withRegion(region)
                .withNewResourceGroup(rgCreatable)
                .withSku(NamespaceSku.STANDARD)
                .withNewQueue(queueName, 1024)
                .create();
        Assertions.assertNotNull(namespace);
        Assertions.assertNotNull(namespace.innerModel());
        // Lookup queue
        //
        PagedIterable<Queue> queuesInNamespace = namespace.queues().list();
        Assertions.assertNotNull(queuesInNamespace);
        Assertions.assertEquals(1, TestUtilities.getSize(queuesInNamespace));
        Queue foundQueue = null;
        for (Queue q : queuesInNamespace) {
            if (q.name().equalsIgnoreCase(queueName)) {
                foundQueue = q;
                break;
            }
        }
        Assertions.assertNotNull(foundQueue);
        // Remove Queue
        //
        namespace.update()
                .withoutQueue(queueName)
                .apply();
        queuesInNamespace = namespace.queues().list();
        Assertions.assertNotNull(queuesInNamespace);
        Assertions.assertEquals(0, TestUtilities.getSize(queuesInNamespace));
    }

    @Test
    public void canCreateNamespaceThenCRUDOnTopic() {
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> rgCreatable = resourceManager.resourceGroups()
                .define(rgName)
                .withRegion(region);

        String namespaceDNSLabel = generateRandomResourceName("jvsbns", 15);
        ServiceBusNamespace namespace = serviceBusManager.namespaces()
                .define(namespaceDNSLabel)
                .withRegion(region)
                .withNewResourceGroup(rgCreatable)
                .withSku(NamespaceSku.STANDARD)
                .create();
        Assertions.assertNotNull(namespace);
        Assertions.assertNotNull(namespace.innerModel());

        String topicName = generateRandomResourceName("topic1-", 15);
        Topic topic = namespace.topics()
                .define(topicName)
                .create();

        Assertions.assertNotNull(topic);
        Assertions.assertNotNull(topic.innerModel());
        Assertions.assertNotNull(topic.name());
        Assertions.assertTrue(topic.name().equalsIgnoreCase(topicName));

        Duration dupDetectionDuration = topic.duplicateMessageDetectionHistoryDuration();
        Assertions.assertNotNull(dupDetectionDuration);
        Assertions.assertEquals(10, dupDetectionDuration.toMinutes());
        // Default message TTL is TimeSpan.Max, assert parsing
        //
        //Assertions.assertEquals("10675199.02:48:05.4775807", topic.innerModel().defaultMessageTimeToLive());
        Duration msgTtlDuration = topic.defaultMessageTtlDuration();
        Assertions.assertNotNull(msgTtlDuration);
        // Assertions the default ttl TimeSpan("10675199.02:48:05.4775807") parsing
        //
        verifyDefaultDuration(msgTtlDuration);
        // Assertions the default max size In MB
        //
        Assertions.assertEquals(1024, topic.maxSizeInMB());

        PagedIterable<Topic> topicsInNamespace = namespace.topics().list();
        Assertions.assertNotNull(topicsInNamespace);
        Assertions.assertTrue(TestUtilities.getSize(topicsInNamespace) > 0);
        Topic foundTopic = null;
        for (Topic t : topicsInNamespace) {
            if (t.name().equalsIgnoreCase(topic.name())) {
                foundTopic = t;
                break;
            }
        }
        Assertions.assertNotNull(foundTopic);
        foundTopic = foundTopic.update()
                .withDefaultMessageTTL(Duration.ofMinutes(20))
                .withDuplicateMessageDetectionHistoryDuration(Duration.ofMinutes(15))
                .withDeleteOnIdleDurationInMinutes(25)
                .apply();
        Duration ttlDuration = foundTopic.defaultMessageTtlDuration();
        Assertions.assertNotNull(ttlDuration);
        Assertions.assertEquals(20, ttlDuration.toMinutes());
        Duration duplicateDetectDuration = foundTopic.duplicateMessageDetectionHistoryDuration();
        Assertions.assertNotNull(duplicateDetectDuration);
        Assertions.assertEquals(15, duplicateDetectDuration.toMinutes());
        Assertions.assertEquals(25, foundTopic.deleteOnIdleDurationInMinutes());
        // Delete
        namespace.topics().deleteByName(foundTopic.name());
    }

    @Test
    public void canCreateDeleteTopicWithNamespace() {
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> rgCreatable = resourceManager.resourceGroups()
                .define(rgName)
                .withRegion(region);

        String namespaceDNSLabel = generateRandomResourceName("jvsbns", 15);
        String topicName = generateRandomResourceName("topic1-", 15);
        // Create NS with Topic
        //
        ServiceBusNamespace namespace = serviceBusManager.namespaces()
                .define(namespaceDNSLabel)
                .withRegion(region)
                .withNewResourceGroup(rgCreatable)
                .withSku(NamespaceSku.STANDARD)
                .withNewTopic(topicName, 1024)
                .create();
        Assertions.assertNotNull(namespace);
        Assertions.assertNotNull(namespace.innerModel());
        // Lookup topic
        //
        PagedIterable<Topic> topicsInNamespace = namespace.topics().list();
        Assertions.assertNotNull(topicsInNamespace);
        Assertions.assertEquals(1, TestUtilities.getSize(topicsInNamespace));
        Topic foundTopic = null;
        for (Topic t : topicsInNamespace) {
            if (t.name().equalsIgnoreCase(topicName)) {
                foundTopic = t;
                break;
            }
        }
        Assertions.assertNotNull(foundTopic);
        // Remove Topic
        //
        namespace.update()
                .withoutTopic(topicName)
                .apply();
        topicsInNamespace = namespace.topics().list();
        Assertions.assertNotNull(topicsInNamespace);
        Assertions.assertEquals(0, TestUtilities.getSize(topicsInNamespace));
    }

    @Test
    public void canOperateOnAuthorizationRules() {
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> rgCreatable = resourceManager.resourceGroups()
                .define(rgName)
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
                .withSku(NamespaceSku.STANDARD)
                .withNewQueue(queueName, 1024)
                .withNewTopic(topicName, 1024)
                .withNewManageRule(nsRuleName)
                .create();
        // Lookup ns authorization rule
        //
        PagedIterable<NamespaceAuthorizationRule> rulesInNamespace = namespace.authorizationRules().list();
        Assertions.assertNotNull(rulesInNamespace);
        Assertions.assertEquals(2, TestUtilities.getSize(rulesInNamespace));    // Default + one explicit

        NamespaceAuthorizationRule foundNsRule = null;
        for (NamespaceAuthorizationRule rule : rulesInNamespace) {
            if (rule.name().equalsIgnoreCase(nsRuleName)) {
                foundNsRule = rule;
                break;
            }
        }
        Assertions.assertNotNull(foundNsRule);
        AuthorizationKeys nsRuleKeys = foundNsRule.getKeys();
        Assertions.assertNotNull(nsRuleKeys);
        Assertions.assertNotNull(nsRuleKeys.innerModel());
        String primaryKey = nsRuleKeys.primaryKey();
        Assertions.assertNotNull(primaryKey);
        Assertions.assertNotNull(nsRuleKeys.secondaryKey());
        Assertions.assertNotNull(nsRuleKeys.primaryConnectionString());
        Assertions.assertNotNull(nsRuleKeys.secondaryConnectionString());
        nsRuleKeys = foundNsRule.regenerateKey(KeyType.PRIMARY_KEY);
        if (!isPlaybackMode()) {
            Assertions.assertNotEquals(nsRuleKeys.primaryKey(), primaryKey);
        }
        // Lookup queue & operate on auth rules
        //
        PagedIterable<Queue> queuesInNamespace = namespace.queues().list();
        Assertions.assertNotNull(queuesInNamespace);
        Assertions.assertEquals(1, TestUtilities.getSize(queuesInNamespace));
        Queue queue = queuesInNamespace.iterator().next();
        Assertions.assertNotNull(queue);
        Assertions.assertNotNull(queue.innerModel());

        QueueAuthorizationRule qRule = queue.authorizationRules()
                .define("rule1")
                .withListeningEnabled()
                .create();
        Assertions.assertNotNull(qRule);
        Assertions.assertNotNull(qRule.rights().contains(AccessRights.LISTEN));
        qRule = qRule.update()
                .withManagementEnabled()
                .apply();
        Assertions.assertNotNull(qRule.rights().contains(AccessRights.MANAGE));
        PagedIterable<QueueAuthorizationRule> rulesInQueue = queue.authorizationRules().list();
        Assertions.assertTrue(TestUtilities.getSize(rulesInQueue) > 0);
        boolean foundQRule = false;
        for (QueueAuthorizationRule r : rulesInQueue) {
            if (r.name().equalsIgnoreCase(qRule.name())) {
                foundQRule = true;
                break;
            }
        }
        Assertions.assertTrue(foundQRule);
        queue.authorizationRules().deleteByName(qRule.name());
        // Lookup topic & operate on auth rules
        //
        PagedIterable<Topic> topicsInNamespace = namespace.topics().list();
        Assertions.assertNotNull(topicsInNamespace);
        Assertions.assertEquals(1, TestUtilities.getSize(topicsInNamespace));
        Topic topic = topicsInNamespace.iterator().next();
        Assertions.assertNotNull(topic);
        Assertions.assertNotNull(topic.innerModel());
        TopicAuthorizationRule tRule = topic.authorizationRules()
                .define("rule2")
                .withSendingEnabled()
                .create();
        Assertions.assertNotNull(tRule);
        Assertions.assertNotNull(tRule.rights().contains(AccessRights.SEND));
        tRule = tRule.update()
                .withManagementEnabled()
                .apply();
        Assertions.assertNotNull(tRule.rights().contains(AccessRights.MANAGE));
        PagedIterable<TopicAuthorizationRule> rulesInTopic = topic.authorizationRules().list();
        Assertions.assertTrue(TestUtilities.getSize(rulesInTopic) > 0);
        boolean foundTRule = false;
        for (TopicAuthorizationRule r : rulesInTopic) {
            if (r.name().equalsIgnoreCase(tRule.name())) {
                foundTRule = true;
                break;
            }
        }
        Assertions.assertTrue(foundTRule);
        topic.authorizationRules().deleteByName(tRule.name());
    }

    @Test
    public void canPerformOnNamespaceActions() {
        rgName = null;
        String namespaceDNSLabel = generateRandomResourceName("jvsbns", 15);
        CheckNameAvailabilityResult availabilityResult = serviceBusManager
                .namespaces()
                .checkNameAvailability(namespaceDNSLabel);
        Assertions.assertNotNull(availabilityResult);
        if (!availabilityResult.isAvailable()) {
            Assertions.assertNotNull(availabilityResult.unavailabilityReason());
            Assertions.assertNotNull(availabilityResult.unavailabilityMessage());
        }
    }

    @Test
    public void canPerformCRUDOnSubscriptions() {
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> rgCreatable = resourceManager.resourceGroups()
                .define(rgName)
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
                .withSku(NamespaceSku.STANDARD)
                .withNewTopic(topicName, 1024)
                .create();
        // Create Topic subscriptions and list it
        //
        Topic topic = namespace.topics().getByName(topicName);
        ServiceBusSubscription subscription = topic.subscriptions().define(subscriptionName)
                .withDefaultMessageTTL(Duration.ofMinutes(20))
                .create();
        Assertions.assertNotNull(subscription);
        Assertions.assertNotNull(subscription.innerModel());
        Assertions.assertEquals(20, subscription.defaultMessageTtlDuration().toMinutes());
        subscription = topic.subscriptions().getByName(subscriptionName);
        Assertions.assertNotNull(subscription);
        Assertions.assertNotNull(subscription.innerModel());
        PagedIterable<ServiceBusSubscription> subscriptionsInTopic = topic.subscriptions().list();
        Assertions.assertTrue(TestUtilities.getSize(subscriptionsInTopic) > 0);
        boolean foundSubscription = false;
        for (ServiceBusSubscription s : subscriptionsInTopic) {
            if (s.name().equalsIgnoreCase(subscription.name())) {
                foundSubscription = true;
                break;
            }
        }
        Assertions.assertTrue(foundSubscription);
        topic.subscriptions().deleteByName(subscriptionName);
        subscriptionsInTopic = topic.subscriptions().list();
        Assertions.assertTrue(TestUtilities.getSize(subscriptionsInTopic) == 0);
    }

    private static final int HOURS_PER_DAY = 24;
    private static final int MINUTES_PER_HOUR = 60;

    private static void verifyDefaultDuration(Duration duration) {
        Assertions.assertEquals(10675199, duration.toDays());
        Assertions.assertEquals(2, duration.toHours() % HOURS_PER_DAY);
        Assertions.assertEquals(48, duration.toMinutes() % MINUTES_PER_HOUR);
    }

    @Test
    public void canCRUDQueryWithSlashInName() {
        Region region = Region.US_EAST;
        String namespaceDNSLabel = generateRandomResourceName("jvsbns", 15);
        String queueName = "order~created";

        ServiceBusNamespace serviceBusNamespace = serviceBusManager.namespaces()
            .define(namespaceDNSLabel)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withSku(NamespaceSku.BASIC)
            .create();

        Queue queue = serviceBusNamespace.queues().define(queueName)
            .create();

        Assertions.assertEquals(1, serviceBusNamespace.queues().list().stream().count());

        queue.refresh();

        Assertions.assertEquals(queueName, queue.name());

        serviceBusNamespace.queues().deleteByName(queueName);
    }
}
