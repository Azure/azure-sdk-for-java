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
import com.microsoft.azure.management.servicebus.implementation.NamespaceResourceInner;
import com.microsoft.azure.management.servicebus.implementation.QueueResourceInner;
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
        resourceManager.resourceGroups().deleteByName(RG_NAME);
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

        Namespace namespace = serviceBusManager.namespaces()
                .getByGroup(RG_NAME, namespaceDNSLabel);
        Assert.assertNotNull(namespace);
        Assert.assertNotNull(namespace.inner());

        PagedList<Namespace> namespaces = serviceBusManager.namespaces().listByGroup(RG_NAME);
        Assert.assertNotNull(namespaces);
        Assert.assertTrue(namespaces.size() > 0);
        boolean found = false;
        for (Namespace n : namespaces) {
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
        serviceBusManager.namespaces().deleteByGroup(RG_NAME, namespace.name());
    }

    @Test
    public void canCreateNamespaceThenCRUDOnQueue() {
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> rgCreatable = resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(region);

        String namespaceDNSLabel = generateRandomResourceName("jvsbns", 15);
        Namespace namespace = serviceBusManager.namespaces()
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
        // Assert the default auto delete idle duration TimeSpan("10675199.02:48:05.4775807")
        // TODO
        // Assert.assertEquals(48, queue.deleteOnIdleDurationInMinutes());

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
        Namespace namespace = serviceBusManager.namespaces()
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
    public void canOperateOnNamespace() {
        String rgName = "javasbrga4878671a3";
        String sbName = "javasbrg08d63135a0-1";

        resourceManager.resourceGroups().define(rgName)
        .withRegion(Region.US_EAST2)
        .create();

//        NamespaceResourceInner namespaceResourceInner = new NamespaceResourceInner();
//        namespaceResourceInner.withLocation(Region.US_EAST2.toString());

        NamespaceResourceInner namespaceResourceInner = serviceBusManager.inner().namespaces().getByResourceGroup(rgName, sbName);
        namespaceResourceInner.withSku(new Sku())
        .sku()
        .withName(SkuName.PREMIUM)
        .withTier(SkuTier.PREMIUM)
        .withCapacity(6);

        // namespaceResourceInner.sku().withCapacity(null);
        namespaceResourceInner = serviceBusManager.inner().namespaces().createOrUpdate(rgName,
                sbName, namespaceResourceInner);

        System.out.println(namespaceResourceInner);
    }

    @Test
    public void canOperateOnQueue() {
        String RG_NAME = "javasbrga4878671a3-3";
        String basicSBName = "basic-sb-08d63135a0";
        String standardSBName = "standard-sb-08d63135a0";
        String premiumSBName = "premium-sb-08d63135a0";

        String qName = "myqueue-5";

        resourceManager.resourceGroups().define(RG_NAME)
                .withRegion(Region.US_EAST2)
                .create();

//        createBasicSB(RG_NAME, basicSBName);
//        createStandardSB(RG_NAME, standardSBName);
//        createPremiumSB(RG_NAME, premiumSBName);

        QueueResourceInner queueResourceInner = serviceBusManager.inner().queues().get(RG_NAME,
                premiumSBName,
                qName);

//        QueueResourceInner queueResourceInner = new QueueResourceInner();
//        queueResourceInner.withLocation(Region.US_EAST2.toString());
//        queueResourceInner.withEnablePartitioning(false);
        queueResourceInner.withLockDuration("00:02:05");
        queueResourceInner = serviceBusManager.inner().queues().createOrUpdate(RG_NAME,
                premiumSBName,
                qName,
                queueResourceInner);

//        queueResourceInner.withRequiresSession(false);
//        queueResourceInner.withSupportOrdering(true);
//        queueResourceInner = serviceBusManager.inner().queues().get(RG_NAME, sbName, qName + "4");
//        queueResourceInner.withEnableExpress(false);
//        queueResourceInner.withEnableBatchedOperations(true);
//        queueResourceInner.withSupportOrdering(true);

//        queueResourceInner.withRequiresDuplicateDetection(false);
//        queueResourceInner.withDuplicateDetectionHistoryTimeWindow("00:20:00");
//        queueResourceInner.withDeadLetteringOnMessageExpiration(true);

    }


    private void createBasicSB(String rgName, String sbName) {
        NamespaceResourceInner namespaceResourceInner = new NamespaceResourceInner();
        namespaceResourceInner.withLocation(Region.US_EAST2.toString());
        namespaceResourceInner
                .withSku(new Sku())
                .sku()
                .withName(SkuName.BASIC)
                .withTier(SkuTier.BASIC);
        serviceBusManager.inner().namespaces().createOrUpdate(rgName,
                sbName, namespaceResourceInner);
    }

    private void createStandardSB(String rgName, String sbName) {
        NamespaceResourceInner namespaceResourceInner = new NamespaceResourceInner();
        namespaceResourceInner.withLocation(Region.US_EAST2.toString());
        namespaceResourceInner
                .withSku(new Sku())
                .sku()
                .withName(SkuName.STANDARD)
                .withTier(SkuTier.STANDARD);
        serviceBusManager.inner().namespaces().createOrUpdate(rgName,
                sbName, namespaceResourceInner);
    }

    private void createPremiumSB(String rgName, String sbName) {
        NamespaceResourceInner namespaceResourceInner = new NamespaceResourceInner();
        namespaceResourceInner.withLocation(Region.US_EAST2.toString());
        namespaceResourceInner
                .withSku(new Sku())
                .sku()
                .withName(SkuName.PREMIUM)
                .withTier(SkuTier.PREMIUM);
        serviceBusManager.inner().namespaces().createOrUpdate(rgName,
                sbName, namespaceResourceInner);
    }
}
