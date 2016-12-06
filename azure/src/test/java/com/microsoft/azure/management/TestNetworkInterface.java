/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import java.util.List;

import org.junit.Assert;

import com.microsoft.azure.management.network.LoadBalancerBackend;
import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkInterfaces;
import com.microsoft.azure.management.network.NicIpConfiguration;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

public class TestNetworkInterface extends TestTemplate<NetworkInterface, NetworkInterfaces> {

    @Override
    public NetworkInterface createResource(NetworkInterfaces networkInterfaces) throws Exception {
        final String newName = "nic" + this.testId;
        return networkInterfaces.define(newName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withNewPrimaryPublicIpAddress("pipdns" + this.testId)
                .withIpForwarding()
                .create();
    }

    @Override
    public NetworkInterface updateResource(NetworkInterface resource) throws Exception {
        resource =  resource.update()
                .withoutIpForwarding()
                .updateIpConfiguration("primary") // Updating the primary ip configuration
                    .withPrivateIpAddressDynamic() // Equivalent to ..update().withPrimaryPrivateIpAddressDynamic()
                    .withoutPublicIpAddress()      // Equivalent to ..update().withoutPrimaryPublicIpAddress()
                    .parent()
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();
        Assert.assertTrue(resource.tags().containsKey("tag1"));
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

        info.append("\n\tIP forwarding enabled: ").append(resource.isIpForwardingEnabled())
                .append("\n\tMAC Address:").append(resource.macAddress())
                .append("\n\tPrivate IP:").append(resource.primaryPrivateIp())
                .append("\n\tPrivate allocation method:").append(resource.primaryPrivateIpAllocationMethod())
                .append("\n\tPrimary virtual network ID: ").append(resource.primaryIpConfiguration().networkId())
                .append("\n\tPrimary subnet name: ").append(resource.primaryIpConfiguration().subnetName())
                .append("\n\tIP configurations: ");

        // Output IP configs
        for (NicIpConfiguration ipConfig : resource.ipConfigurations().values()) {
            info.append("\n\t\tName: ").append(ipConfig.name())
                .append("\n\t\tPrivate IP: ").append(ipConfig.privateIpAddress())
                .append("\n\t\tPrivate IP allocation method: ").append(ipConfig.privateIpAllocationMethod().toString())
                .append("\n\t\tPrivate IP version: ").append(ipConfig.privateIpAddressVersion().toString())
                .append("\n\t\tPIP id: ").append(ipConfig.publicIpAddressId())
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
