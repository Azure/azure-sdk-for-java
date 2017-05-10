/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network;

import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceCallback;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkManagementTest extends TestBase {
    protected static ResourceManager resourceManager;
    protected static NetworkManager networkManager;
    protected static String RG_NAME = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javanwmrg", 15);

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(defaultSubscription);

        networkManager = NetworkManager
                .authenticate(restClient, defaultSubscription);
    }

    @Test
    public void testNetworkDeleteServiceCallBack() {
        String vnetName = generateRandomResourceName("vnet", 15);
        networkManager.networks().define(vnetName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(RG_NAME)
                .withAddressSpace("172.16.0.0/16")
                .defineSubnet("Front-end")
                .withAddressPrefix("172.16.1.0/24")
                .attach()
                .defineSubnet("Back-end")
                .withAddressPrefix("172.16.3.0/24")
                .attach()
                .create();

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger counter = new AtomicInteger(0);
        networkManager.networks().deleteByResourceGroupAsync(RG_NAME, vnetName, new ServiceCallback<Void>() {
            @Override
            public void failure(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void success(Void aVoid) {
                counter.incrementAndGet();
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
        Assert.assertEquals(counter.intValue(), 1);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }
}