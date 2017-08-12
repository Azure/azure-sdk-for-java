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
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.rest.ServiceCallback;

import rx.Observable;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkInterfaceOperationsTests extends NetworkManagementTest {

    @Test
    public void canUseMultipleIPConfigs() throws Exception {
        String networkName = SdkContext.randomResourceName("net", 15);
        String nic1Name = SdkContext.randomResourceName("nic", 15);
        String nic2Name = SdkContext.randomResourceName("nic", 15);
        String nic3Name = SdkContext.randomResourceName("nic", 15);

        Network network = networkManager.networks().define(networkName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(RG_NAME)
                .withAddressSpace("10.0.0.0/27")
                .withSubnet("subnet1", "10.0.0.0/28")
                .withSubnet("subnet2", "10.0.0.16/28")
                .create();

        // NIC that starts with one IP config and ends with two
        Creatable<NetworkInterface> nic1Definition = networkManager.networkInterfaces().define(nic1Name)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(RG_NAME)
                .withExistingPrimaryNetwork(network)
                .withSubnet("subnet1")
                .withPrimaryPrivateIPAddressDynamic();

        // NIC that starts with two IP configs and ends with one
        Creatable<NetworkInterface> nic2Definition = networkManager.networkInterfaces().define(nic2Name)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(RG_NAME)
                .withExistingPrimaryNetwork(network)
                .withSubnet("subnet1")
                .withPrimaryPrivateIPAddressDynamic()
                .defineSecondaryIPConfiguration("nicip2")
                    .withExistingNetwork(network)
                    .withSubnet("subnet1")
                    .withPrivateIPAddressDynamic()
                    .attach();

        // NIC that starts with two IP configs and ends with two
        Creatable<NetworkInterface> nic3Definition = networkManager.networkInterfaces().define(nic3Name)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(RG_NAME)
                .withExistingPrimaryNetwork(network)
                .withSubnet("subnet1")
                .withPrimaryPrivateIPAddressDynamic()
                .defineSecondaryIPConfiguration("nicip2")
                    .withExistingNetwork(network)
                    .withSubnet("subnet1")
                    .withPrivateIPAddressDynamic()
                    .attach();

        // Create the NICs in parallel
        CreatedResources<NetworkInterface> createdNics = networkManager.networkInterfaces().create(nic1Definition, nic2Definition, nic3Definition);

        NetworkInterface nic1 = createdNics.get(nic1Definition.key());
        NetworkInterface nic2 = createdNics.get(nic2Definition.key());
        NetworkInterface nic3 = createdNics.get(nic3Definition.key());

        NicIPConfiguration primaryIPConfig, secondaryIPConfig;

        // Verify NIC1
        Assert.assertNotNull(nic1);
        primaryIPConfig = nic1.primaryIPConfiguration();
        Assert.assertNotNull(primaryIPConfig);
        Assert.assertTrue("subnet1".equalsIgnoreCase(primaryIPConfig.subnetName()));
        Assert.assertTrue(network.id().equalsIgnoreCase(primaryIPConfig.networkId()));

        // Verify NIC2
        Assert.assertNotNull(nic2);
        Assert.assertEquals(2, nic2.ipConfigurations().size());

        primaryIPConfig = nic2.primaryIPConfiguration();
        Assert.assertNotNull(primaryIPConfig);
        Assert.assertTrue("subnet1".equalsIgnoreCase(primaryIPConfig.subnetName()));
        Assert.assertTrue(network.id().equalsIgnoreCase(primaryIPConfig.networkId()));

        secondaryIPConfig = nic2.ipConfigurations().get("nicip2");
        Assert.assertNotNull(secondaryIPConfig);
        Assert.assertTrue("subnet1".equalsIgnoreCase(primaryIPConfig.subnetName()));
        Assert.assertTrue(network.id().equalsIgnoreCase(secondaryIPConfig.networkId()));

        // Verify NIC3
        Assert.assertNotNull(nic3);
        Assert.assertEquals(2,  nic3.ipConfigurations().size());

        primaryIPConfig = nic3.primaryIPConfiguration();
        Assert.assertNotNull(primaryIPConfig);
        Assert.assertTrue("subnet1".equalsIgnoreCase(primaryIPConfig.subnetName()));
        Assert.assertTrue(network.id().equalsIgnoreCase(primaryIPConfig.networkId()));

        secondaryIPConfig = nic2.ipConfigurations().get("nicip2");
        Assert.assertNotNull(secondaryIPConfig);
        Assert.assertTrue("subnet1".equalsIgnoreCase(secondaryIPConfig.subnetName()));
        Assert.assertTrue(network.id().equalsIgnoreCase(secondaryIPConfig.networkId()));

        // Update NIC1
        Observable<NetworkInterface> nic1Update = nic1.update()
            .defineSecondaryIPConfiguration("nicip2")
                .withExistingNetwork(network)
                .withSubnet("subnet1")
                .withPrivateIPAddressDynamic()
                .attach()
            .applyAsync();

        // Update NIC2
        Observable<NetworkInterface> nic2Update = nic2.update()
            .withoutIPConfiguration("nicip2")
            .updateIPConfiguration("primary")
                .withSubnet("subnet2")
                .parent()
            .applyAsync();

        // Update NIC3
        Observable<NetworkInterface> nic3Update = nic3.update()
            .withoutIPConfiguration("nicip2")
            .defineSecondaryIPConfiguration("nicip3")
                .withExistingNetwork(network)
                .withSubnet("subnet1")
                .withPrivateIPAddressDynamic()
                .attach()
            .applyAsync();

        List<NetworkInterface> updatedNics = Observable.mergeDelayError(nic1Update, nic2Update, nic3Update).toList().toBlocking().single();

        // Verify updated NICs
        for (NetworkInterface nic : updatedNics) {
            Assert.assertNotNull(nic);
            if (nic.id().equalsIgnoreCase(nic1.id())) {
                // Verify NIC1
                Assert.assertEquals(2, nic.ipConfigurations().size());

                primaryIPConfig = nic.primaryIPConfiguration();
                Assert.assertNotNull(primaryIPConfig);
                Assert.assertTrue("subnet1".equalsIgnoreCase(primaryIPConfig.subnetName()));
                Assert.assertTrue(network.id().equalsIgnoreCase(primaryIPConfig.networkId()));

                secondaryIPConfig = nic.ipConfigurations().get("nicip2");
                Assert.assertNotNull(secondaryIPConfig);
                Assert.assertTrue("subnet1".equalsIgnoreCase(secondaryIPConfig.subnetName()));
                Assert.assertTrue(network.id().equalsIgnoreCase(secondaryIPConfig.networkId()));

            } else if (nic.id().equals(nic2.id())) {
                // Verify NIC2
                Assert.assertEquals(1, nic.ipConfigurations().size());
                primaryIPConfig = nic.primaryIPConfiguration();
                Assert.assertNotNull(primaryIPConfig);
                Assert.assertNotEquals("nicip2", primaryIPConfig.name());
                Assert.assertTrue("subnet2".equalsIgnoreCase(primaryIPConfig.subnetName()));
                Assert.assertTrue(network.id().equalsIgnoreCase(primaryIPConfig.networkId()));

            } else if (nic.id().equals(nic3.id())) {
                // Verify NIC3
                Assert.assertEquals(2, nic.ipConfigurations().size());

                primaryIPConfig = nic.primaryIPConfiguration();
                Assert.assertNotNull(primaryIPConfig);
                Assert.assertNotEquals("nicip2", primaryIPConfig.name());
                Assert.assertNotEquals("nicip3", primaryIPConfig.name());
                Assert.assertTrue("subnet1".equalsIgnoreCase(primaryIPConfig.subnetName()));
                Assert.assertTrue(network.id().equalsIgnoreCase(primaryIPConfig.networkId()));

                secondaryIPConfig = nic.ipConfigurations().get("nicip3");
                Assert.assertNotNull(secondaryIPConfig);
                Assert.assertTrue("subnet1".equalsIgnoreCase(secondaryIPConfig.subnetName()));
                Assert.assertTrue(network.id().equalsIgnoreCase(secondaryIPConfig.networkId()));
            } else {
                Assert.assertTrue("Unrecognized NIC ID", false);
            }
        }
    }

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
