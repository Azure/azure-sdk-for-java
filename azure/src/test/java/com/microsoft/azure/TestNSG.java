/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure;

import org.junit.Assert;

import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityGroups;
import com.microsoft.azure.management.network.NetworkSecurityRule;
import com.microsoft.azure.management.network.NetworkSecurityRule.Protocol;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * Test for network security group CRUD.
 */
public class TestNSG extends TestTemplate<NetworkSecurityGroup, NetworkSecurityGroups> {

    @Override
    public NetworkSecurityGroup createResource(NetworkSecurityGroups nsgs) throws Exception {
        final String newName = "nsg" + this.testId;
        return nsgs.define(newName)
                .withRegion(Region.US_WEST)
                .withNewGroup()
                .defineRule("rule1")
                    .allowOutbound()
                    .fromAnyAddress()
                    .fromPort(80)
                    .toAnyAddress()
                    .toPort(80)
                    .withProtocol(Protocol.TCP)
                    .attach()
                .defineRule("rule2")
                    .allowInbound()
                    .fromAnyAddress()
                    .fromAnyPort()
                    .toAnyAddress()
                    .toPortRange(22, 25)
                    .withAnyProtocol()
                    .withPriority(200)
                    .attach()
                .create();
    }

    @Override
    public NetworkSecurityGroup updateResource(NetworkSecurityGroup resource) throws Exception {
        resource =  resource.update()
                .withoutRule("rule1")
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();
        Assert.assertTrue(resource.tags().containsKey("tag1"));
        return resource;
    }

    @Override
    public void print(NetworkSecurityGroup resource) {
        StringBuilder info = new StringBuilder();
        info.append("NSG: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags());

        // Output security rules
        for (NetworkSecurityRule rule : resource.securityRules()) {
            info.append("\n\tRule: ").append(rule.name())
                .append("\n\t\tAccess: ").append(rule.access())
                .append("\n\t\tDirection: ").append(rule.direction())
                .append("\n\t\tFrom address: ").append(rule.sourceAddressPrefix())
                .append("\n\t\tFrom port range: ").append(rule.sourcePortRange())
                .append("\n\t\tTo address: ").append(rule.destinationAddressPrefix())
                .append("\n\t\tTo port: ").append(rule.destinationPortRange())
                .append("\n\t\tProtocol: ").append(rule.protocol())
                .append("\n\t\tPriority: ").append(rule.priority());
        }

        System.out.println(info.toString());
    }
}
