// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.management.Region;
import com.azure.resourcemanager.network.fluent.models.NatGatewayInner;
import com.azure.resourcemanager.network.models.ApplicationSecurityGroup;
import com.azure.resourcemanager.network.models.DeleteOptions;
import com.azure.resourcemanager.network.models.NatGatewaySku;
import com.azure.resourcemanager.network.models.NatGatewaySkuName;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NetworkInterfaces;
import com.azure.resourcemanager.network.models.Networks;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.CreatedResources;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class NetworkInterfaceOperationsTests extends NetworkManagementTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void canUseMultipleIPConfigs() throws Exception {
        String networkName = generateRandomResourceName("net", 15);
        String[] nicNames = new String[3];
        for (int i = 0; i < nicNames.length; i++) {
            nicNames[i] = generateRandomResourceName("nic", 15);
        }

        Network network =
            networkManager
                .networks()
                .define(networkName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAddressSpace("10.0.0.0/27")
                .withSubnet("subnet1", "10.0.0.0/28")
                .withSubnet("subnet2", "10.0.0.16/28")
                .create();

        List<Creatable<NetworkInterface>> nicDefinitions =
            Arrays
                .asList(
                    // 0 - NIC that starts with one IP config and ends with two
                    (Creatable<NetworkInterface>)
                        (networkManager
                            .networkInterfaces()
                            .define(nicNames[0])
                            .withRegion(Region.US_EAST)
                            .withNewResourceGroup(rgName)
                            .withExistingPrimaryNetwork(network)
                            .withSubnet("subnet1")
                            .withPrimaryPrivateIPAddressDynamic()),

                    // 1 - NIC that starts with two IP configs and ends with one
                    networkManager
                        .networkInterfaces()
                        .define(nicNames[1])
                        .withRegion(Region.US_EAST)
                        .withNewResourceGroup(rgName)
                        .withExistingPrimaryNetwork(network)
                        .withSubnet("subnet1")
                        .withPrimaryPrivateIPAddressDynamic()
                        .defineSecondaryIPConfiguration("nicip2")
                        .withExistingNetwork(network)
                        .withSubnet("subnet1")
                        .withPrivateIpAddressDynamic()
                        .attach(),

                    // 2 - NIC that starts with two IP configs and ends with two
                    networkManager
                        .networkInterfaces()
                        .define(nicNames[2])
                        .withRegion(Region.US_EAST)
                        .withNewResourceGroup(rgName)
                        .withExistingPrimaryNetwork(network)
                        .withSubnet("subnet1")
                        .withPrimaryPrivateIPAddressDynamic()
                        .defineSecondaryIPConfiguration("nicip2")
                        .withExistingNetwork(network)
                        .withSubnet("subnet1")
                        .withPrivateIpAddressDynamic()
                        .attach());

        // Create the NICs in parallel
        CreatedResources<NetworkInterface> createdNics = networkManager.networkInterfaces().create(nicDefinitions);

        NetworkInterface[] nics = new NetworkInterface[nicDefinitions.size()];
        for (int i = 0; i < nicDefinitions.size(); i++) {
            nics[i] = createdNics.get(nicDefinitions.get(i).key());
        }

        NicIpConfiguration primaryIPConfig, secondaryIPConfig;
        NetworkInterface nic;

        // Verify NIC0
        nic = nics[0];
        Assertions.assertNotNull(nic);
        primaryIPConfig = nic.primaryIPConfiguration();
        Assertions.assertNotNull(primaryIPConfig);
        Assertions.assertTrue("subnet1".equalsIgnoreCase(primaryIPConfig.subnetName()));
        Assertions.assertTrue(network.id().equalsIgnoreCase(primaryIPConfig.networkId()));

        // Verify NIC1
        nic = nics[1];
        Assertions.assertNotNull(nic);
        Assertions.assertEquals(2, nic.ipConfigurations().size());

        primaryIPConfig = nic.primaryIPConfiguration();
        Assertions.assertNotNull(primaryIPConfig);
        Assertions.assertTrue("subnet1".equalsIgnoreCase(primaryIPConfig.subnetName()));
        Assertions.assertTrue(network.id().equalsIgnoreCase(primaryIPConfig.networkId()));

        secondaryIPConfig = nic.ipConfigurations().get("nicip2");
        Assertions.assertNotNull(secondaryIPConfig);
        Assertions.assertTrue("subnet1".equalsIgnoreCase(primaryIPConfig.subnetName()));
        Assertions.assertTrue(network.id().equalsIgnoreCase(secondaryIPConfig.networkId()));

        // Verify NIC2
        nic = nics[2];
        Assertions.assertNotNull(nic);
        Assertions.assertEquals(2, nic.ipConfigurations().size());

        primaryIPConfig = nic.primaryIPConfiguration();
        Assertions.assertNotNull(primaryIPConfig);
        Assertions.assertTrue("subnet1".equalsIgnoreCase(primaryIPConfig.subnetName()));
        Assertions.assertTrue(network.id().equalsIgnoreCase(primaryIPConfig.networkId()));

        secondaryIPConfig = nic.ipConfigurations().get("nicip2");
        Assertions.assertNotNull(secondaryIPConfig);
        Assertions.assertTrue("subnet1".equalsIgnoreCase(secondaryIPConfig.subnetName()));
        Assertions.assertTrue(network.id().equalsIgnoreCase(secondaryIPConfig.networkId()));

        nic = null;

        List<Mono<NetworkInterface>> nicUpdates =
            Arrays
                .asList(
                    // Update NIC0
                    nics[0]
                        .update()
                        .defineSecondaryIPConfiguration("nicip2")
                        .withExistingNetwork(network)
                        .withSubnet("subnet1")
                        .withPrivateIpAddressDynamic()
                        .attach()
                        .applyAsync(),

                    // Update NIC2
                    nics[1]
                        .update()
                        .withoutIPConfiguration("nicip2")
                        .updateIPConfiguration("primary")
                        .withSubnet("subnet2")
                        .parent()
                        .applyAsync(),

                    // Update NIC3
                    nics[2]
                        .update()
                        .withoutIPConfiguration("nicip2")
                        .defineSecondaryIPConfiguration("nicip3")
                        .withExistingNetwork(network)
                        .withSubnet("subnet1")
                        .withPrivateIpAddressDynamic()
                        .attach()
                        .applyAsync());

        List<NetworkInterface> updatedNics =
            Flux
                .mergeDelayError(32, (Mono<NetworkInterface>[]) nicUpdates.toArray(new Mono[0]))
                .collectList()
                .block();

        // Verify updated NICs
        for (NetworkInterface n : updatedNics) {
            Assertions.assertNotNull(n);
            if (n.id().equalsIgnoreCase(nics[0].id())) {
                // Verify NIC0
                Assertions.assertEquals(2, n.ipConfigurations().size());

                primaryIPConfig = n.primaryIPConfiguration();
                Assertions.assertNotNull(primaryIPConfig);
                Assertions.assertTrue("subnet1".equalsIgnoreCase(primaryIPConfig.subnetName()));
                Assertions.assertTrue(network.id().equalsIgnoreCase(primaryIPConfig.networkId()));

                secondaryIPConfig = n.ipConfigurations().get("nicip2");
                Assertions.assertNotNull(secondaryIPConfig);
                Assertions.assertTrue("subnet1".equalsIgnoreCase(secondaryIPConfig.subnetName()));
                Assertions.assertTrue(network.id().equalsIgnoreCase(secondaryIPConfig.networkId()));

            } else if (n.id().equals(nics[1].id())) {
                // Verify NIC1
                Assertions.assertEquals(1, n.ipConfigurations().size());
                primaryIPConfig = n.primaryIPConfiguration();
                Assertions.assertNotNull(primaryIPConfig);
                Assertions.assertNotEquals("nicip2", primaryIPConfig.name());
                Assertions.assertTrue("subnet2".equalsIgnoreCase(primaryIPConfig.subnetName()));
                Assertions.assertTrue(network.id().equalsIgnoreCase(primaryIPConfig.networkId()));

            } else if (n.id().equals(nics[2].id())) {
                // Verify NIC
                Assertions.assertEquals(2, n.ipConfigurations().size());

                primaryIPConfig = n.primaryIPConfiguration();
                Assertions.assertNotNull(primaryIPConfig);
                Assertions.assertNotEquals("nicip2", primaryIPConfig.name());
                Assertions.assertNotEquals("nicip3", primaryIPConfig.name());
                Assertions.assertTrue("subnet1".equalsIgnoreCase(primaryIPConfig.subnetName()));
                Assertions.assertTrue(network.id().equalsIgnoreCase(primaryIPConfig.networkId()));

                secondaryIPConfig = n.ipConfigurations().get("nicip3");
                Assertions.assertNotNull(secondaryIPConfig);
                Assertions.assertTrue("subnet1".equalsIgnoreCase(secondaryIPConfig.subnetName()));
                Assertions.assertTrue(network.id().equalsIgnoreCase(secondaryIPConfig.networkId()));
            } else {
                Assertions.assertTrue(false, "Unrecognized NIC ID");
            }
        }
    }

    @Test
    public void canCreateBatchOfNetworkInterfaces() throws Exception {
        ResourceGroups resourceGroups = resourceManager.resourceGroups();
        Networks networks = networkManager.networks();
        NetworkInterfaces networkInterfaces = networkManager.networkInterfaces();

        Creatable<ResourceGroup> resourceGroupCreatable = resourceGroups.define(rgName).withRegion(Region.US_EAST);

        final String vnetName = "vnet1212";
        Creatable<Network> networkCreatable =
            networks
                .define(vnetName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withAddressSpace("10.0.0.0/28");

        // Prepare a batch of nics
        //
        final String nic1Name = "nic1";
        Creatable<NetworkInterface> networkInterface1Creatable =
            networkInterfaces
                .define(nic1Name)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIPAddressStatic("10.0.0.5");

        final String nic2Name = "nic2";
        Creatable<NetworkInterface> networkInterface2Creatable =
            networkInterfaces
                .define(nic2Name)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIPAddressStatic("10.0.0.6");

        final String nic3Name = "nic3";
        Creatable<NetworkInterface> networkInterface3Creatable =
            networkInterfaces
                .define(nic3Name)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIPAddressStatic("10.0.0.7");

        final String nic4Name = "nic4";
        Creatable<NetworkInterface> networkInterface4Creatable =
            networkInterfaces
                .define(nic4Name)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIPAddressStatic("10.0.0.8");

        @SuppressWarnings("unchecked")
        Collection<NetworkInterface> batchNics =
            networkInterfaces
                .create(
                    networkInterface1Creatable,
                    networkInterface2Creatable,
                    networkInterface3Creatable,
                    networkInterface4Creatable)
                .values();

        Assertions.assertTrue(batchNics.size() == 4);
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
        Assertions.assertTrue(found.size() == 4);
    }

    @Test
    public void canCreateNicWithApplicationSecurityGroup() {
        Network network =
            networkManager
                .networks()
                .define("vnet1")
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAddressSpace("10.0.0.0/27")
                .withSubnet("subnet1", "10.0.0.0/28")
                .withSubnet("subnet2", "10.0.0.16/28")
                .create();

        ApplicationSecurityGroup asg1 = networkManager.applicationSecurityGroups().define("asg1")
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .create();

        NetworkInterface nic = networkManager.networkInterfaces().define("nic1")
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withExistingPrimaryNetwork(network)
            .withSubnet("subnet1")
            .withPrimaryPrivateIPAddressDynamic()
            .withExistingApplicationSecurityGroup(asg1)
            .create();

        List<ApplicationSecurityGroup> applicationSecurityGroups = nic.primaryIPConfiguration().listAssociatedApplicationSecurityGroups();
        Assertions.assertEquals(1, applicationSecurityGroups.size());
        Assertions.assertEquals("asg1", applicationSecurityGroups.iterator().next().name());

        ApplicationSecurityGroup asg2 = networkManager.applicationSecurityGroups().define("asg2")
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .create();

        nic.update()
            .withoutApplicationSecurityGroup(asg1.name())
            .withExistingApplicationSecurityGroup(asg2)
            .defineSecondaryIPConfiguration("nicip2")
                .withExistingNetwork(network)
                .withSubnet("subnet1")
                .withPrivateIpAddressDynamic()
                .attach()
            .apply();

        applicationSecurityGroups = nic.primaryIPConfiguration().listAssociatedApplicationSecurityGroups();
        Assertions.assertEquals(1, applicationSecurityGroups.size());
        Assertions.assertEquals("asg2", applicationSecurityGroups.iterator().next().name());

        nic.update()
            .withoutApplicationSecurityGroup(asg1.name())
            .withExistingApplicationSecurityGroup(asg1)
            .apply();

        Assertions.assertEquals(2, nic.ipConfigurations().get("nicip2").innerModel().applicationSecurityGroups().size());
        Assertions.assertEquals(
            new HashSet<>(Arrays.asList("asg1", "asg2")),
            nic.ipConfigurations().get("nicip2").innerModel().applicationSecurityGroups().stream().map(inner -> ResourceUtils.nameFromResourceId(inner.id())).collect(Collectors.toSet()));
        if (!isPlaybackMode()) {
            // avoid concurrent request in playback
            applicationSecurityGroups = nic.ipConfigurations().get("nicip2").listAssociatedApplicationSecurityGroups();
            Assertions.assertEquals(2, applicationSecurityGroups.size());
            Assertions.assertEquals(
                new HashSet<>(Arrays.asList("asg1", "asg2")),
                applicationSecurityGroups.stream().map(ApplicationSecurityGroup::name).collect(Collectors.toSet()));
        }
    }

    @Test
    @Disabled("Deadlock from CountDownLatch")
    public void canDeleteNetworkWithServiceCallBack() {
        String vnetName = generateRandomResourceName("vnet", 15);
        networkManager
            .networks()
            .define(vnetName)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .withAddressSpace("172.16.0.0/16")
            .defineSubnet("Front-end")
            .withAddressPrefix("172.16.1.0/24")
            .attach()
            .defineSubnet("Back-end")
            .withAddressPrefix("172.16.3.0/24")
            .attach()
            .create();

        // TODO: Fix deadlock
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger counter = new AtomicInteger(0);
        networkManager
            .networks()
            .deleteByResourceGroupAsync(rgName, vnetName)
            .doOnSuccess(
                aVoid -> {
                    counter.incrementAndGet();
                    latch.countDown();
                })
            .doOnError(throwable -> latch.countDown());

        try {
            latch.await();
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
        Assertions.assertEquals(counter.intValue(), 1);
    }

    @Test
    public void canListSubnetAvailableIpAddresses() {
        String networkName = generateRandomResourceName("vnet", 10);
        String subnetName = "subnet1";
        String nicName = generateRandomResourceName("nic", 10);

        Network network = networkManager.networks()
            .define(networkName)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/24")
            .withSubnet(subnetName, "10.0.0.0/29")
            .create();

        Subnet subnet = network.subnets().get(subnetName);
        Set<String> availableIps = subnet.listAvailablePrivateIPAddresses();
        Assertions.assertTrue(availableIps.size() > 0);

        String availableIp = availableIps.iterator().next();

        // occupy the available ip address
        NetworkInterface nic = networkManager.networkInterfaces()
            .define(nicName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withExistingPrimaryNetwork(network)
            .withSubnet(subnetName)
            .withPrimaryPrivateIPAddressStatic(availableIp)
            .create();

        availableIps = subnet.listAvailablePrivateIPAddresses();
        Assertions.assertFalse(availableIps.contains(availableIp));
    }

    @Test
    public void canAssociateNatGateway() {
        String networkName = generateRandomResourceName("vnet", 10);
        String subnetName = "subnet1";
        String subnet2Name = "subnet2";

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName)
            .withRegion(Region.US_EAST)
            .create();

        NatGatewayInner gateway1 = createNatGateway();

        Network network = networkManager.networks()
            .define(networkName)
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(resourceGroup)
            .withAddressSpace("10.0.0.0/16")
            .defineSubnet(subnetName)
                .withAddressPrefix("10.0.0.0/24")
                .withExistingNatGateway(gateway1.id())
                .attach()
            .create();

        Subnet subnet = network.subnets().get(subnetName);
        Assertions.assertEquals(gateway1.id(), subnet.natGatewayId());

        NatGatewayInner gateway2 = createNatGateway();

        network.update()
            .updateSubnet(subnetName)
                .withExistingNatGateway(gateway2.id())
                .parent()
            .defineSubnet(subnet2Name)
                .withAddressPrefix("10.0.1.0/24")
                .withExistingNatGateway(gateway2.id())
                .attach()
            .apply();

        subnet = network.subnets().get(subnetName);
        Assertions.assertEquals(gateway2.id(), subnet.natGatewayId());

        Subnet subnet2 = network.subnets().get(subnet2Name);
        Assertions.assertEquals(gateway2.id(), subnet2.natGatewayId());
    }

    @Test
    public void canCreateAndUpdateNicWithMultipleDeleteOptions() {
        String subnetName = generateRandomResourceName("subnet-", 15);
        resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();
        Network vnet = networkManager.networks()
            .define(generateRandomResourceName("vnet-", 15))
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/28")
            .withSubnet(subnetName, "10.0.0.0/28")
            .create();

        NetworkInterface nic = networkManager.networkInterfaces()
            .define(generateRandomResourceName("nic-", 15))
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withExistingPrimaryNetwork(vnet)
            .withSubnet(subnetName)
            .withPrimaryPrivateIPAddressDynamic()
            .withNewPrimaryPublicIPAddress()
            .withPrimaryPublicIPAddressDeleteOptions(DeleteOptions.DELETE)
            .defineSecondaryIPConfiguration("secondary1")
                .withExistingNetwork(vnet)
                .withSubnet(subnetName)
                .withPrivateIpAddressDynamic()
                .withNewPublicIpAddress()
                .withPublicIPAddressDeleteOptions(DeleteOptions.DETACH)
                .attach()
            .defineSecondaryIPConfiguration("secondary2")
                .withExistingNetwork(vnet)
                .withSubnet(subnetName)
                .withPrivateIpAddressDynamic()
                .withNewPublicIpAddress()
                .withPublicIPAddressDeleteOptions(DeleteOptions.DETACH)
                .attach()
            .create();

        nic.refresh();
        Assertions.assertEquals(DeleteOptions.DELETE, nic.primaryIPConfiguration().innerModel().publicIpAddress().deleteOption());
        Assertions.assertEquals(DeleteOptions.DETACH, nic.ipConfigurations().get("secondary1").innerModel().publicIpAddress().deleteOption());
        Assertions.assertEquals(DeleteOptions.DETACH, nic.ipConfigurations().get("secondary2").innerModel().publicIpAddress().deleteOption());

        String existingPrimaryIpAddressId = nic.primaryIPConfiguration().publicIpAddressId();
        nic.update().withNewPrimaryPublicIPAddress().withPrimaryPublicIPAddressDeleteOptions(DeleteOptions.DETACH).apply();
        nic.refresh();
        Assertions.assertFalse(existingPrimaryIpAddressId.equalsIgnoreCase(nic.primaryIPConfiguration().publicIpAddressId()));
        Assertions.assertEquals(DeleteOptions.DETACH, nic.primaryIPConfiguration().innerModel().publicIpAddress().deleteOption());

        String existingSecondary1IpAddressId = nic.ipConfigurations().get("secondary1").publicIpAddressId();
        nic.update()
            .withPrimaryPublicIPAddressDeleteOptions(DeleteOptions.DELETE)
            .updateIPConfiguration("secondary1")
            .withNewPublicIpAddress()
            .withPublicIPAddressDeleteOptions(DeleteOptions.DELETE)
            .parent()
            .updateIPConfiguration("secondary2")
            .withPublicIPAddressDeleteOptions(DeleteOptions.DELETE)
            .parent()
            .defineSecondaryIPConfiguration("secondary3")
            .withExistingNetwork(vnet)
            .withSubnet(subnetName)
            .withPrivateIpAddressDynamic()
            .withNewPublicIpAddress()
            .withPublicIPAddressDeleteOptions(DeleteOptions.DELETE)
            .attach()
            .apply();
        nic.refresh();
        Assertions.assertFalse(existingSecondary1IpAddressId.equalsIgnoreCase(nic.ipConfigurations().get("secondary1").publicIpAddressId()));
        Assertions.assertEquals(DeleteOptions.DELETE, nic.primaryIPConfiguration().innerModel().publicIpAddress().deleteOption());
        Assertions.assertEquals(DeleteOptions.DELETE, nic.ipConfigurations().get("secondary1").innerModel().publicIpAddress().deleteOption());
        Assertions.assertEquals(DeleteOptions.DELETE, nic.ipConfigurations().get("secondary2").innerModel().publicIpAddress().deleteOption());
        Assertions.assertEquals(DeleteOptions.DELETE, nic.ipConfigurations().get("secondary3").innerModel().publicIpAddress().deleteOption());
    }

    private NatGatewayInner createNatGateway() {
        String natGatewayName = generateRandomResourceName("natgw", 10);
        return networkManager.serviceClient()
            .getNatGateways()
            .createOrUpdate(
                rgName,
                natGatewayName,
                new NatGatewayInner()
                    .withLocation(Region.US_EAST.toString())
                    .withSku(new NatGatewaySku().withName(NatGatewaySkuName.STANDARD))
            );
    }
}
