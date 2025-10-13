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
import java.util.Locale;
import java.util.Set;

public class NetworkSecurityGroupTests extends NetworkManagementTest {

    @Test
    public void canCRUDNetworkSecurityGroup() {

        final String asgName = generateRandomResourceName("asg", 8).toUpperCase(Locale.ROOT);
        final String asgName2 = generateRandomResourceName("asg", 8).toUpperCase(Locale.ROOT);
        final String asgName3 = generateRandomResourceName("asg", 8).toUpperCase(Locale.ROOT);
        final String asgName4 = generateRandomResourceName("asg", 8).toUpperCase(Locale.ROOT);
        final String asgName5 = generateRandomResourceName("asg", 8).toUpperCase(Locale.ROOT);
        final String asgName6 = generateRandomResourceName("asg", 8).toUpperCase(Locale.ROOT);
        final String nsgName = generateRandomResourceName("nsg", 8);

        final Region region = Region.US_SOUTH_CENTRAL;

        ApplicationSecurityGroup asg = networkManager.applicationSecurityGroups()
            .define(asgName)
            .withRegion(region)
            .withNewResourceGroup(rgName.toUpperCase(Locale.ROOT))
            .create();

        ApplicationSecurityGroup asg2 = networkManager.applicationSecurityGroups()
            .define(asgName2)
            .withRegion(region)
            .withExistingResourceGroup(rgName.toUpperCase(Locale.ROOT))
            .create();

        ApplicationSecurityGroup asg3 = networkManager.applicationSecurityGroups()
            .define(asgName3)
            .withRegion(region)
            .withExistingResourceGroup(rgName.toUpperCase(Locale.ROOT))
            .create();

        ApplicationSecurityGroup asg4 = networkManager.applicationSecurityGroups()
            .define(asgName4)
            .withRegion(region)
            .withExistingResourceGroup(rgName.toUpperCase(Locale.ROOT))
            .create();

        NetworkSecurityGroup nsg = networkManager.networkSecurityGroups()
            .define(nsgName)
            .withRegion(region)
            .withExistingResourceGroup(rgName.toUpperCase(Locale.ROOT))
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

        Set<String> sourceApplicationSecurityGroupIds = new HashSet<>();
        nsg.securityRules()
            .get("rule2")
            .sourceApplicationSecurityGroupIds()
            .forEach(id -> sourceApplicationSecurityGroupIds.add(id.toLowerCase(Locale.ROOT)));
        Set<String> destinationApplicationSecurityGroupIds = new HashSet<>();
        nsg.securityRules()
            .get("rule3")
            .destinationApplicationSecurityGroupIds()
            .forEach(id -> destinationApplicationSecurityGroupIds.add(id.toLowerCase(Locale.ROOT)));

        Assertions.assertEquals(2, nsg.securityRules().get("rule2").sourceApplicationSecurityGroupIds().size());
        Assertions.assertEquals(2, nsg.securityRules().get("rule3").destinationApplicationSecurityGroupIds().size());
        Assertions.assertEquals(
            new HashSet<>(Arrays.asList(asg.id().toLowerCase(Locale.ROOT), asg2.id().toLowerCase(Locale.ROOT))),
            sourceApplicationSecurityGroupIds);
        Assertions.assertEquals(
            new HashSet<>(Arrays.asList(asg3.id().toLowerCase(Locale.ROOT), asg4.id().toLowerCase(Locale.ROOT))),
            destinationApplicationSecurityGroupIds);

        ApplicationSecurityGroup asg5 = networkManager.applicationSecurityGroups()
            .define(asgName5)
            .withRegion(region)
            .withExistingResourceGroup(rgName.toUpperCase(Locale.ROOT))
            .create();

        ApplicationSecurityGroup asg6 = networkManager.applicationSecurityGroups()
            .define(asgName6)
            .withRegion(region)
            .withExistingResourceGroup(rgName.toUpperCase(Locale.ROOT))
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

        sourceApplicationSecurityGroupIds.clear();
        nsg.securityRules()
            .get("rule2")
            .sourceApplicationSecurityGroupIds()
            .forEach(id -> sourceApplicationSecurityGroupIds.add(id.toLowerCase(Locale.ROOT)));
        destinationApplicationSecurityGroupIds.clear();
        nsg.securityRules()
            .get("rule3")
            .destinationApplicationSecurityGroupIds()
            .forEach(id -> destinationApplicationSecurityGroupIds.add(id.toLowerCase(Locale.ROOT)));

        Assertions.assertEquals(2, nsg.securityRules().get("rule2").sourceApplicationSecurityGroupIds().size());
        Assertions.assertEquals(2, nsg.securityRules().get("rule3").destinationApplicationSecurityGroupIds().size());
        Assertions.assertEquals(
            new HashSet<>(Arrays.asList(asg.id().toLowerCase(Locale.ROOT), asg5.id().toLowerCase(Locale.ROOT))),
            sourceApplicationSecurityGroupIds);
        Assertions.assertEquals(
            new HashSet<>(Arrays.asList(asg3.id().toLowerCase(Locale.ROOT), asg6.id().toLowerCase(Locale.ROOT))),
            destinationApplicationSecurityGroupIds);

        nsg.update()
            .updateRule("rule2")
            .fromAddress("Internet")
            .parent()
            .updateRule("rule3")
            .toAddress("Storage.WestUS")
            .parent()
            .apply();

        Assertions.assertEquals(0, nsg.securityRules().get("rule2").sourceApplicationSecurityGroupIds().size());
        Assertions.assertEquals("Internet", nsg.securityRules().get("rule2").sourceAddressPrefix());
        Assertions.assertEquals(0, nsg.securityRules().get("rule3").destinationApplicationSecurityGroupIds().size());
        Assertions.assertEquals("Storage.WestUS", nsg.securityRules().get("rule3").destinationAddressPrefix());

        networkManager.networkSecurityGroups().deleteById(nsg.id());
    }

    @Test
    public void canListAssociatedNetwork() {
        final String nsgName = generateRandomResourceName("nsg", 8);

        final Region region = Region.US_SOUTH_CENTRAL;

        NetworkSecurityGroup nsg = networkManager.networkSecurityGroups()
            .define(nsgName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .defineRule("rule1")
            .allowOutbound()
            .fromAnyAddress()
            .fromAnyPort()
            .toAnyAddress()
            .toPort(80)
            .withProtocol(SecurityRuleProtocol.TCP)
            .attach()
            .create();

        Assertions.assertEquals(0, nsg.listAssociatedSubnets().size());
        String subnetName = generateRandomResourceName("sb", 15);

        networkManager.networks()
            .define(generateRandomResourceName("nw", 15))
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/24")
            .defineSubnet(subnetName)
            .withAddressPrefix("10.0.0.0/28")
            .withExistingNetworkSecurityGroup(nsg)
            .attach()
            .create();

        nsg.refresh();

        Assertions.assertEquals(1, nsg.listAssociatedSubnets().size());
        Assertions.assertEquals(subnetName.toLowerCase(Locale.ROOT),
            nsg.listAssociatedSubnets().iterator().next().name().toLowerCase(Locale.ROOT));
    }
}
