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

        String namespaceDNSLabel = generateRandomResourceName("javasb", 15);
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
