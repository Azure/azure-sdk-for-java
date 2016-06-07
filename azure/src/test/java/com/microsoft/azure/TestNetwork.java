/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure;

import org.junit.Assert;

import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.Networks;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * Test of virtual network management.
 */
public class TestNetwork extends TestTemplate<Network, Networks> {

    @Override
    public Network createResource(Networks networks) throws Exception {
        final String newName = "net" + this.testId;
        return networks.define(newName)
                .withRegion(Region.US_WEST)
                .withNewGroup()
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnetA", "10.0.0.0/29")
                .defineSubnet("subnetB")
                    .withAddressPrefix("10.0.0.8/29")
                    .attach()
                .create();
    }

    @Override
    public Network updateResource(Network resource) throws Exception {
        resource =  resource.update()
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .withAddressSpace("141.25.0.0/16")
                .withSubnet("subnetC", "141.25.0.0/29")
                .withoutSubnet("subnetA")
                .apply();
        Assert.assertTrue(resource.tags().containsKey("tag1"));

        return resource;
    }

    @Override
    public void print(Network resource) {
        StringBuilder info = new StringBuilder();
        info.append("Network: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tAddress spaces: ").append(resource.addressSpaces())
                .append("\n\tDNS server IPs: ").append(resource.dnsServerIPs());

        // Output subnets
        for (Subnet subnet : resource.subnets()) {
            info.append("\n\tSubnet: ").append(subnet.name())
                .append("\n\t\tAddress prefix: ").append(subnet.addressPrefix());
        }

        System.out.println(info.toString());
    }
}
