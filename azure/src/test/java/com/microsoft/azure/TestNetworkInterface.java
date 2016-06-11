/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure;

import com.microsoft.azure.management.network.*;
import org.junit.Assert;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;

public class TestNetworkInterface extends TestTemplate<NetworkInterface, NetworkInterfaces> {

    @Override
    public NetworkInterface createResource(NetworkInterfaces networkInterfaces) throws Exception {
        final String newName = "nic" + this.testId;
        return networkInterfaces.define(newName)
                .withRegion(Region.US_EAST)
                .withNewGroup()
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
                .updateIpConfiguration("primary-nic-config") // Updating the primary ip configuration
                    .withPrivateIpAddressDynamic() // Equivalent to ..update().withPrimaryPrivateIpAddressDynamic()
                    .withoutPublicIpAddress()      // Equivalent to ..update().withoutPrimaryPublicIpAddress()
                    .apply()
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();
        Assert.assertTrue(resource.tags().containsKey("tag1"));
        return resource;
    }

    @Override
    public void print(NetworkInterface resource) {
        StringBuilder info = new StringBuilder();
        info.append("NetworkInterface: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tInternal DNS name label: ").append(resource.internalDnsNameLabel())
                .append("\n\tInternal FQDN: ").append(resource.internalFqdn())
                .append("\n\tDNS server IPs: ");

        // Output dns servers
        for(String dnsServerIp : resource.dnsServers()) {
            info.append("\n\t\t").append(dnsServerIp);
        }
        info.append("\n\t IP forwarding enabled: ").append(resource.isIpForwardingEnabled())
                .append("\n\tIs Primary:").append(resource.isPrimary())
                .append("\n\tMAC Address:").append(resource.macAddress())
                .append("\n\tPrivate IP:").append(resource.primaryPrivateIp())
                .append("\n\tPrivate allocation method:").append(resource.primaryPrivateIpAllocationMethod())
                .append("\n\tSubnet Id:").append(resource.primarySubnetId());

        System.out.println(info.toString());
    }
}
