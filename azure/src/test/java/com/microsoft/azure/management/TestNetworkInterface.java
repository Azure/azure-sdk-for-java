/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import java.util.List;
import java.util.Set;

import org.junit.Assert;

import com.microsoft.azure.management.network.LoadBalancerBackend;
import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkInterfaces;
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

public class TestNetworkInterface extends TestTemplate<NetworkInterface, NetworkInterfaces> {
    @Override
    public NetworkInterface createResource(NetworkInterfaces networkInterfaces) throws Exception {
        final String nicName = "nic" + this.testId;
        final String vnetName = "net" + this.testId;
        final String pipName = "pip" + this.testId;
        final Region region = Region.US_EAST;

        Network network = networkInterfaces.manager().networks().define(vnetName)
                .withRegion(region)
                .withNewResourceGroup()
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/29")
                .withSubnet("subnet2", "10.0.0.8/29")
                .create();

        NetworkInterface nic = networkInterfaces.define(nicName)
                .withRegion(region)
                .withExistingResourceGroup(network.resourceGroupName())
                .withExistingPrimaryNetwork(network)
                .withSubnet("subnet1")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(pipName)
                .withIPForwarding()
                .withAcceleratedNetworking()
                .create();

        // Verify NIC settings
        Assert.assertTrue(nic.isAcceleratedNetworkingEnabled());
        Assert.assertTrue(nic.isIPForwardingEnabled());

        // Verify IP configs
        NicIPConfiguration ipConfig = nic.primaryIPConfiguration();
        Assert.assertNotNull(ipConfig);
        network = ipConfig.getNetwork();
        Assert.assertNotNull(network);
        Subnet subnet = network.subnets().get(ipConfig.subnetName());
        Assert.assertNotNull(subnet);
        Assert.assertEquals(1, subnet.networkInterfaceIPConfigurationCount());
        Set<NicIPConfiguration> ipConfigs = subnet.listNetworkInterfaceIPConfigurations();
        Assert.assertNotNull(ipConfigs);
        Assert.assertEquals(1, ipConfigs.size());
        NicIPConfiguration ipConfig2 = null;
        for (NicIPConfiguration i : ipConfigs) {
            if (i.name().equalsIgnoreCase(ipConfig.name())) {
                ipConfig2 = i;
                break;
            }
        }
        Assert.assertNotNull(ipConfig2);
        Assert.assertTrue(ipConfig.name().equalsIgnoreCase(ipConfig2.name()));

        return nic;
    }

    @Override
    public NetworkInterface updateResource(NetworkInterface resource) throws Exception {
        resource =  resource.update()
                .withoutIPForwarding()
                .withoutAcceleratedNetworking()
                .withSubnet("subnet2")
                .updateIPConfiguration("primary")  // Updating the primary ip configuration
                    .withPrivateIPAddressDynamic() // Equivalent to ..update().withPrimaryPrivateIPAddressDynamic()
                    .withoutPublicIPAddress()      // Equivalent to ..update().withoutPrimaryPublicIPAddress()
                    .parent()
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();

        // Verifications
        Assert.assertFalse(resource.isAcceleratedNetworkingEnabled());
        Assert.assertFalse(resource.isIPForwardingEnabled());
        NicIPConfiguration primaryIpConfig = resource.primaryIPConfiguration();
        Assert.assertNotNull(primaryIpConfig);
        Assert.assertTrue(primaryIpConfig.isPrimary());
        Assert.assertTrue("subnet2".equalsIgnoreCase(primaryIpConfig.subnetName()));
        Assert.assertNull(primaryIpConfig.publicIPAddressId());
        Assert.assertTrue(resource.tags().containsKey("tag1"));

        Assert.assertEquals(1,  resource.ipConfigurations().size());
        return resource;
    }

    public static void printNic(NetworkInterface resource) {
        StringBuilder info = new StringBuilder();
        info.append("NetworkInterface: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tInternal DNS name label: ").append(resource.internalDnsNameLabel())
                .append("\n\tInternal FQDN: ").append(resource.internalFqdn())
                .append("\n\tInternal domain name suffix: ").append(resource.internalDomainNameSuffix())
                .append("\n\tVirtual machine ID: ").append(resource.virtualMachineId())
                .append("\n\tApplied DNS servers: ").append(resource.appliedDnsServers().toString())
                .append("\n\tDNS server IPs: ");

        // Output dns servers
        for (String dnsServerIp : resource.dnsServers()) {
            info.append("\n\t\t").append(dnsServerIp);
        }

        info.append("\n\tIP forwarding enabled? ").append(resource.isIPForwardingEnabled())
                .append("\n\tAccelerated networking enabled? ").append(resource.isAcceleratedNetworkingEnabled())
                .append("\n\tMAC Address:").append(resource.macAddress())
                .append("\n\tPrivate IP:").append(resource.primaryPrivateIP())
                .append("\n\tPrivate allocation method:").append(resource.primaryPrivateIPAllocationMethod())
                .append("\n\tPrimary virtual network ID: ").append(resource.primaryIPConfiguration().networkId())
                .append("\n\tPrimary subnet name: ").append(resource.primaryIPConfiguration().subnetName())
                .append("\n\tIP configurations: ");

        // Output IP configs
        for (NicIPConfiguration ipConfig : resource.ipConfigurations().values()) {
            info.append("\n\t\tName: ").append(ipConfig.name())
                .append("\n\t\tPrivate IP: ").append(ipConfig.privateIPAddress())
                .append("\n\t\tPrivate IP allocation method: ").append(ipConfig.privateIPAllocationMethod().toString())
                .append("\n\t\tPrivate IP version: ").append(ipConfig.privateIPAddressVersion().toString())
                .append("\n\t\tPIP id: ").append(ipConfig.publicIPAddressId())
                .append("\n\t\tAssociated network ID: ").append(ipConfig.networkId())
                .append("\n\t\tAssociated subnet name: ").append(ipConfig.subnetName());

            // Show associated load balancer backends
            final List<LoadBalancerBackend> backends = ipConfig.listAssociatedLoadBalancerBackends();
            info.append("\n\t\tAssociated load balancer backends: ").append(backends.size());
            for (LoadBalancerBackend backend : backends) {
                info.append("\n\t\t\tLoad balancer ID: ").append(backend.parent().id())
                    .append("\n\t\t\t\tBackend name: ").append(backend.name());
            }

            // Show associated load balancer inbound NAT rules
            final List<LoadBalancerInboundNatRule> natRules = ipConfig.listAssociatedLoadBalancerInboundNatRules();
            info.append("\n\t\tAssociated load balancer inbound NAT rules: ").append(natRules.size());
            for (LoadBalancerInboundNatRule natRule : natRules) {
                info.append("\n\t\t\tLoad balancer ID: ").append(natRule.parent().id())
                    .append("\n\t\t\tInbound NAT rule name: ").append(natRule.name());
            }
        }

        System.out.println(info.toString());
    }

    @Override
    public void print(NetworkInterface resource) {
        printNic(resource);
    }
}
