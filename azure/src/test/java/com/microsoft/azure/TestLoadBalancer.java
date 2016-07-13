/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure;

import java.util.List;

import org.junit.Assert;

import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancers;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * Test of virtual network management.
 */
public class TestLoadBalancer extends TestTemplate<LoadBalancer, LoadBalancers> {

    private final PublicIpAddresses pips;
    public TestLoadBalancer(PublicIpAddresses pips) {
        this.pips = pips;
    }

    @Override
    public LoadBalancer createResource(LoadBalancers resources) throws Exception {
        final String newName = "lb" + this.testId;
        Region region = Region.US_WEST;
        String groupName = "rg" + this.testId;
        String pipName = "pip" + this.testId;

        // Create a pip
        PublicIpAddress pip = this.pips.define(pipName)
            .withRegion(region)
            .withNewResourceGroup(groupName)
            .withLeafDomainLabel(pipName)
            .create();

        PublicIpAddress pip2 = this.pips.define(pipName + "b")
                .withRegion(region)
                .withExistingResourceGroup(groupName)
                .withLeafDomainLabel(pipName + "b")
                .create();

        // Create a load balancer
        return resources.define(newName)
                .withRegion(region)
                .withNewResourceGroup(groupName)
                .withExistingPublicIpAddress(pip)
                .withExistingPublicIpAddress(pip2)
                .create();
    }

    @Override
    public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
        resource =  resource.update()
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();
        Assert.assertTrue(resource.tags().containsKey("tag1"));

        return resource;
    }

    @Override
    public void print(LoadBalancer resource) {
        StringBuilder info = new StringBuilder();
        info.append("Load balancer: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags());

        // Show public IP addresses
        info.append("\n\tPublic IP address IDs:");
        List<String> pipIds = resource.publicIpAddressIds();
        if (pipIds == null || pipIds.size() == 0) {
            info.append(" (None)");
        } else {
            for (String pipId : resource.publicIpAddressIds()) {
                info.append("\n\t\tPIP id: ").append(pipId);
            }
        }

        System.out.println(info.toString());
    }
}
