// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager;

import com.azure.resourcemanager.network.models.LoadBalancerBackend;
import com.azure.resourcemanager.network.models.LoadBalancerInboundNatRule;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NetworkInterfaces;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Assertions;

public class TestNetworkInterface extends TestTemplate<NetworkInterface, NetworkInterfaces> {
    @Override
    public NetworkInterface createResource(NetworkInterfaces networkInterfaces) throws Exception {

        String postfix = networkInterfaces.manager().sdkContext().randomResourceName("", 8);
        final String nicName = "nic" + postfix;
        final String vnetName = "net" + postfix;
        final String pipName = "pip" + postfix;
        final Region region = Region.US_EAST;

        Network network =
            networkInterfaces
                .manager()
                .networks()
                .define(vnetName)
                .withRegion(region)
                .withNewResourceGroup()
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/29")
                .withSubnet("subnet2", "10.0.0.8/29")
                .create();

        NetworkInterface nic =
            networkInterfaces
                .define(nicName)
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
        Assertions.assertTrue(nic.isAcceleratedNetworkingEnabled());
        Assertions.assertTrue(nic.isIPForwardingEnabled());

        // Verify IP configs
        NicIpConfiguration ipConfig = nic.primaryIPConfiguration();
        Assertions.assertNotNull(ipConfig);
        network = ipConfig.getNetwork();
        Assertions.assertNotNull(network);
        Subnet subnet = network.subnets().get(ipConfig.subnetName());
        Assertions.assertNotNull(subnet);
        Assertions.assertEquals(1, subnet.networkInterfaceIPConfigurationCount());
        Collection<NicIpConfiguration> ipConfigs = subnet.listNetworkInterfaceIPConfigurations();
        Assertions.assertNotNull(ipConfigs);
        Assertions.assertEquals(1, ipConfigs.size());
        NicIpConfiguration ipConfig2 = null;
        for (NicIpConfiguration i : ipConfigs) {
            if (i.name().equalsIgnoreCase(ipConfig.name())) {
                ipConfig2 = i;
                break;
            }
        }
        Assertions.assertNotNull(ipConfig2);
        Assertions.assertTrue(ipConfig.name().equalsIgnoreCase(ipConfig2.name()));

        return nic;
    }

    @Override
    public NetworkInterface updateResource(NetworkInterface resource) throws Exception {
        resource =
            resource
                .update()
                .withoutIPForwarding()
                .withoutAcceleratedNetworking()
                .withSubnet("subnet2")
                .updateIPConfiguration("primary") // Updating the primary IP configuration
                .withPrivateIpAddressDynamic() // Equivalent to ..update().withPrimaryPrivateIPAddressDynamic()
                .withoutPublicIpAddress() // Equivalent to ..update().withoutPrimaryPublicIPAddress()
                .parent()
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();

        // Verifications
        Assertions.assertFalse(resource.isAcceleratedNetworkingEnabled());
        Assertions.assertFalse(resource.isIPForwardingEnabled());
        NicIpConfiguration primaryIpConfig = resource.primaryIPConfiguration();
        Assertions.assertNotNull(primaryIpConfig);
        Assertions.assertTrue(primaryIpConfig.isPrimary());
        Assertions.assertTrue("subnet2".equalsIgnoreCase(primaryIpConfig.subnetName()));
        Assertions.assertNull(primaryIpConfig.publicIpAddressId());
        Assertions.assertTrue(resource.tags().containsKey("tag1"));

        Assertions.assertEquals(1, resource.ipConfigurations().size());

        resource.updateTags().withoutTag("tag1").withTag("tag3", "value3").applyTags();
        Assertions.assertFalse(resource.tags().containsKey("tag1"));
        Assertions.assertEquals("value3", resource.tags().get("tag3"));
        return resource;
    }

    public static void printNic(NetworkInterface resource) {
        StringBuilder info = new StringBuilder();
        info
            .append("NetworkInterface: ")
            .append(resource.id())
            .append("Name: ")
            .append(resource.name())
            .append("\n\tResource group: ")
            .append(resource.resourceGroupName())
            .append("\n\tRegion: ")
            .append(resource.region())
            .append("\n\tTags: ")
            .append(resource.tags())
            .append("\n\tInternal DNS name label: ")
            .append(resource.internalDnsNameLabel())
            .append("\n\tInternal FQDN: ")
            .append(resource.internalFqdn())
            .append("\n\tInternal domain name suffix: ")
            .append(resource.internalDomainNameSuffix())
            .append("\n\tVirtual machine ID: ")
            .append(resource.virtualMachineId())
            .append("\n\tApplied DNS servers: ")
            .append(resource.appliedDnsServers().toString())
            .append("\n\tDNS server IPs: ");

        // Output dns servers
        for (String dnsServerIp : resource.dnsServers()) {
            info.append("\n\t\t").append(dnsServerIp);
        }

        info
            .append("\n\tIP forwarding enabled? ")
            .append(resource.isIPForwardingEnabled())
            .append("\n\tAccelerated networking enabled? ")
            .append(resource.isAcceleratedNetworkingEnabled())
            .append("\n\tMAC Address:")
            .append(resource.macAddress())
            .append("\n\tPrivate IP:")
            .append(resource.primaryPrivateIP())
            .append("\n\tPrivate allocation method:")
            .append(resource.primaryPrivateIpAllocationMethod())
            .append("\n\tPrimary virtual network ID: ")
            .append(resource.primaryIPConfiguration().networkId())
            .append("\n\tPrimary subnet name: ")
            .append(resource.primaryIPConfiguration().subnetName())
            .append("\n\tIP configurations: ");

        // Output IP configs
        for (NicIpConfiguration ipConfig : resource.ipConfigurations().values()) {
            info
                .append("\n\t\tName: ")
                .append(ipConfig.name())
                .append("\n\t\tPrivate IP: ")
                .append(ipConfig.privateIpAddress())
                .append("\n\t\tPrivate IP allocation method: ")
                .append(ipConfig.privateIpAllocationMethod().toString())
                .append("\n\t\tPrivate IP version: ")
                .append(ipConfig.privateIpAddressVersion().toString())
                .append("\n\t\tPIP id: ")
                .append(ipConfig.publicIpAddressId())
                .append("\n\t\tAssociated network ID: ")
                .append(ipConfig.networkId())
                .append("\n\t\tAssociated subnet name: ")
                .append(ipConfig.subnetName());

            // Show associated load balancer backends
            final List<LoadBalancerBackend> backends = ipConfig.listAssociatedLoadBalancerBackends();
            info.append("\n\t\tAssociated load balancer backends: ").append(backends.size());
            for (LoadBalancerBackend backend : backends) {
                info
                    .append("\n\t\t\tLoad balancer ID: ")
                    .append(backend.parent().id())
                    .append("\n\t\t\t\tBackend name: ")
                    .append(backend.name());
            }

            // Show associated load balancer inbound NAT rules
            final List<LoadBalancerInboundNatRule> natRules = ipConfig.listAssociatedLoadBalancerInboundNatRules();
            info.append("\n\t\tAssociated load balancer inbound NAT rules: ").append(natRules.size());
            for (LoadBalancerInboundNatRule natRule : natRules) {
                info
                    .append("\n\t\t\tLoad balancer ID: ")
                    .append(natRule.parent().id())
                    .append("\n\t\t\tInbound NAT rule name: ")
                    .append(natRule.name());
            }
        }

        System.out.println(info.toString());
    }

    @Override
    public void print(NetworkInterface resource) {
        printNic(resource);
    }
}
