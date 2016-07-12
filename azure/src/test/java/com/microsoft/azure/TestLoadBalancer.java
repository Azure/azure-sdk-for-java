/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure;

import org.junit.Assert;

import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancers;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * Test of virtual network management.
 */
public class TestLoadBalancer extends TestTemplate<LoadBalancer, LoadBalancers> {

    @Override
    public LoadBalancer createResource(LoadBalancers networks) throws Exception {
        final String newName = "lb" + this.testId;
        Region region = Region.US_WEST;
        String groupName = "rg" + this.testId;

        // Create a load balancer
        return networks.define(newName)
                .withRegion(region)
                .withNewResourceGroup(groupName)
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

        System.out.println(info.toString());
    }
}
