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
                .withNewNetwork("10.0.0.0/28")
                .withPrivateIpAddressDynamic()
                .withNewPublicIpAddress("pipdns" + this.testId)
                .withIPForwardingEnabled()
                .create();
    }

    @Override
    public NetworkInterface updateResource(NetworkInterface resource) throws Exception {
        resource =  resource.update()
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .withIPForwardingDisabled()
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
                .append("\n\tInternal DNS name label: ").append(resource.internalDNSNameLabel())
                .append("\n\tInternal FQDN: ").append(resource.internalFQDN())
                .append("\n\tDNS server IPs: ");

        // Output dns servers
        for(String dnsServerIp : resource.dnsServers()) {
            info.append("\n\t\t").append(dnsServerIp);
        }
        info.append("\n\t IP forwarding enabled: ").append(resource.isIPForwardingEnabled())
                .append("\n\tIs Primary:").append(resource.isPrimary())
                .append("\n\tMAC Address:").append(resource.macAddress())
                .append("\n\tPrivate IP:").append(resource.privateIp())
                .append("\n\tPrivate allocation method:").append(resource.privateIpAllocationMethod())
                .append("\n\tSubnet Id:").append(resource.subnetId())
                .append("\n\tPublicIP Id:").append(resource.publicIpAddressId());

        System.out.println(info.toString());
    }
}
