/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.servicebus.implementation.NamespaceResourceInner;
import com.microsoft.azure.management.servicebus.implementation.QueueResourceInner;
import com.microsoft.azure.management.servicebus.implementation.ServiceBusManager;
import com.microsoft.rest.RestClient;
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
        RG_NAME = generateRandomResourceName("javasbrg", 20);

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(defaultSubscription);

        serviceBusManager = ServiceBusManager
                .authenticate(restClient, defaultSubscription);
    }


    @Override
    protected void cleanUpResources() {
   //     resourceManager.resourceGroups().deleteByName(RG_NAME);
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

        NamespaceResourceInner namespaceResourceInner = serviceBusManager.inner().namespaces().get(rgName, sbName);
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
        String sbName = "sb-08d63135a0-2";
        String qName = "q-08d63135a0-2";

        resourceManager.resourceGroups().define(RG_NAME)
                .withRegion(Region.US_EAST2)
                .create();

        NamespaceResourceInner namespaceResourceInner = new NamespaceResourceInner();
        namespaceResourceInner.withLocation(Region.US_EAST2.toString());
        namespaceResourceInner
                .withSku(new Sku())
                .sku()
                .withName(SkuName.PREMIUM)
                .withTier(SkuTier.PREMIUM)
                .withCapacity(4);

       // namespaceResourceInner = serviceBusManager.inner().namespaces().createOrUpdate(RG_NAME,
       //         sbName, namespaceResourceInner);

        QueueResourceInner queueResourceInner = new QueueResourceInner();
        queueResourceInner.withLocation(Region.US_EAST2.toString());
        queueResourceInner.withEnablePartitioning(false);
        queueResourceInner.withRequiresSession(false);
        queueResourceInner.withSupportOrdering(true);
        queueResourceInner = serviceBusManager.inner().queues().get(RG_NAME, sbName, qName + "3");
//        queueResourceInner.withEnableExpress(false);
//        queueResourceInner.withEnableBatchedOperations(true);
//        queueResourceInner.withSupportOrdering(true);

        queueResourceInner.withRequiresDuplicateDetection(false);
        queueResourceInner.withDuplicateDetectionHistoryTimeWindow("00:20:00");
        queueResourceInner.withDeadLetteringOnMessageExpiration(true);
        queueResourceInner = serviceBusManager.inner().queues().createOrUpdate(RG_NAME,
                sbName,
                qName + "3",
                queueResourceInner);
    }

}
