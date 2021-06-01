// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.management.Region;
import com.azure.resourcemanager.network.models.ApplicationSecurityGroup;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.SecurityRuleProtocol;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

public class NetworkSecurityGroupTests extends NetworkManagementTest {

    @Test
    public void canCRUDNetworkSecurityGroup()  {

        final String asgName = generateRandomResourceName("asg", 8);
        final String asgName2 = generateRandomResourceName("asg", 8);
        final String asgName3 = generateRandomResourceName("asg", 8);
        final String asgName4 = generateRandomResourceName("asg", 8);
        final String asgName5 = generateRandomResourceName("asg", 8);
        final String asgName6 = generateRandomResourceName("asg", 8);
        final String nsgName = generateRandomResourceName("nsg", 8);

        final Region region = Region.US_SOUTH_CENTRAL;

        ApplicationSecurityGroup asg = networkManager.applicationSecurityGroups().define(asgName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .create();

        ApplicationSecurityGroup asg2 = networkManager.applicationSecurityGroups().define(asgName2)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .create();

        ApplicationSecurityGroup asg3 = networkManager.applicationSecurityGroups().define(asgName3)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .create();

        ApplicationSecurityGroup asg4 = networkManager.applicationSecurityGroups().define(asgName4)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .create();

        NetworkSecurityGroup nsg = networkManager.networkSecurityGroups().define(nsgName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .defineRule("rule1")
                .allowOutbound()
                .fromAnyAddress()
                .fromAnyPort()
                .toAnyAddress()
                .toPort(80)
                .withProtocol(SecurityRuleProtocol.TCP)
                .attach()
            .defineRule("rule2")
                .allowInbound()
                .withSourceApplicationSecurityGroup(asg.id(), asg2.id())
                .fromAnyPort()
                .toAnyAddress()
                .toPortRange(22, 25)
                .withAnyProtocol()
                .withPriority(200)
                .withDescription("foo!!")
                .attach()
            .defineRule("rule3")
                .denyInbound()
                .fromAnyAddress()
                .fromAnyPort()
                .withDestinationApplicationSecurityGroup(asg3.id(), asg4.id())
                .toPort(22)
                .withAnyProtocol()
                .withPriority(300)
                .attach()
            .create();

        Assertions.assertEquals(2, nsg.securityRules().get("rule2").sourceApplicationSecurityGroupIds().size());
        Assertions.assertEquals(2, nsg.securityRules().get("rule3").destinationApplicationSecurityGroupIds().size());
        Assertions.assertEquals(new HashSet<>(Arrays.asList(asg.id(), asg2.id())), nsg.securityRules().get("rule2").sourceApplicationSecurityGroupIds());
        Assertions.assertEquals(new HashSet<>(Arrays.asList(asg3.id(), asg4.id())), nsg.securityRules().get("rule3").destinationApplicationSecurityGroupIds());

        ApplicationSecurityGroup asg5 = networkManager.applicationSecurityGroups().define(asgName5)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .create();

        ApplicationSecurityGroup asg6 = networkManager.applicationSecurityGroups().define(asgName6)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .create();

        nsg.update()
            .updateRule("rule2")
                .withoutSourceApplicationSecurityGroup(asg2.id())
                .withSourceApplicationSecurityGroup(asg5.id())
                .parent()
            .updateRule("rule3")
                .withoutDestinationApplicationSecurityGroup(asg4.id())
                .withDestinationApplicationSecurityGroup(asg6.id())
                .parent()
            .apply();

        Assertions.assertEquals(2, nsg.securityRules().get("rule2").sourceApplicationSecurityGroupIds().size());
        Assertions.assertEquals(2, nsg.securityRules().get("rule3").destinationApplicationSecurityGroupIds().size());
        Assertions.assertEquals(new HashSet<>(Arrays.asList(asg.id(), asg5.id())), nsg.securityRules().get("rule2").sourceApplicationSecurityGroupIds());
        Assertions.assertEquals(new HashSet<>(Arrays.asList(asg3.id(), asg6.id())), nsg.securityRules().get("rule3").destinationApplicationSecurityGroupIds());

        networkManager.networkSecurityGroups().deleteById(nsg.id());
    }
}
