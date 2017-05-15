/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network;

import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.rest.ServiceCallback;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkInterfaceOperationsTests extends NetworkManagementTest {

    @Test
    public void canCreateBatchOfNetworkInterfaces() throws Exception {
        ResourceGroups resourceGroups = resourceManager.resourceGroups();
        Networks networks = networkManager.networks();
        NetworkInterfaces networkInterfaces = networkManager.networkInterfaces();

        Creatable<ResourceGroup> resourceGroupCreatable = resourceGroups
                .define(RG_NAME)
                .withRegion(Region.US_EAST);

        final String vnetName = "vnet1212";
        Creatable<Network> networkCreatable = networks
                .define(vnetName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withAddressSpace("10.0.0.0/28");

        // Prepare a batch of nics
        //
        final String nic1Name = "nic1";
        Creatable<NetworkInterface> networkInterface1Creatable = networkInterfaces
                .define(nic1Name)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIPAddressStatic("10.0.0.5");

        final String nic2Name = "nic2";
        Creatable<NetworkInterface> networkInterface2Creatable = networkInterfaces
                .define(nic2Name)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIPAddressStatic("10.0.0.6");

        final String nic3Name = "nic3";
        Creatable<NetworkInterface> networkInterface3Creatable = networkInterfaces
                .define(nic3Name)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIPAddressStatic("10.0.0.7");

        final String nic4Name = "nic4";
        Creatable<NetworkInterface> networkInterface4Creatable = networkInterfaces
                .define(nic4Name)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIPAddressStatic("10.0.0.8");

        @SuppressWarnings("unchecked")
        Collection<NetworkInterface> batchNics = networkInterfaces.create(networkInterface1Creatable,
                networkInterface2Creatable,
                networkInterface3Creatable,
                networkInterface4Creatable).values();

        Assert.assertTrue(batchNics.size() == 4);
        HashMap<String, Boolean> found = new LinkedHashMap<>();
        for (NetworkInterface nic : batchNics) {
            if (nic.name().equalsIgnoreCase(nic1Name)) {
                found.put(nic1Name, true);
            }
            if (nic.name().equalsIgnoreCase(nic2Name)) {
                found.put(nic2Name, true);
            }
            if (nic.name().equalsIgnoreCase(nic3Name)) {
                found.put(nic3Name, true);
            }
            if (nic.name().equalsIgnoreCase(nic4Name)) {
                found.put(nic4Name, true);
            }
        }
        Assert.assertTrue(found.size() == 4);
    }

    @Test
    public void canDeleteNetworkWithServiceCallBack() {
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

}
