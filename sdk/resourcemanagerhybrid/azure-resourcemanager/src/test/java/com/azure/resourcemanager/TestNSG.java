// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager;

import com.azure.resourcemanager.network.models.ApplicationSecurityGroup;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.NetworkSecurityGroups;
import com.azure.resourcemanager.network.models.NetworkSecurityRule;
import com.azure.resourcemanager.network.models.SecurityRuleProtocol;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.core.management.Region;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Mono;

import java.util.List;

/** Test for network security group CRUD. */
public class TestNSG extends TestTemplate<NetworkSecurityGroup, NetworkSecurityGroups> {
    @Override
    public NetworkSecurityGroup createResource(NetworkSecurityGroups nsgs) throws Exception {
        String postFix = nsgs.manager().resourceManager().internalContext().randomResourceName("", 8);
        final String newName = "nsg" + postFix;
        final String resourceGroupName = "rg" + postFix;
        final String nicName = "nic" + postFix;
        final String asgName = nsgs.manager().resourceManager().internalContext().randomResourceName("asg", 8);
        final Region region = Region.US_WEST;

        ApplicationSecurityGroup asg =
            nsgs
                .manager()
                .applicationSecurityGroups()
                .define(asgName)
                .withRegion(region)
                .withNewResourceGroup(resourceGroupName)
                .create();
        // Create
        Mono<NetworkSecurityGroup> resourceStream =
            nsgs
                .define(newName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroupName)
                .defineRule("rule1")
                .allowOutbound()
                .fromAnyAddress()
                .fromPort(80)
                .toAnyAddress()
                .toPort(80)
                .withProtocol(SecurityRuleProtocol.TCP)
                .attach()
                .defineRule("rule2")
                .allowInbound()
                .withSourceApplicationSecurityGroup(asg.id())
                .fromAnyPort()
                .toAnyAddress()
                .toPortRange(22, 25)
                .withAnyProtocol()
                .withPriority(200)
                .withDescription("foo!!")
                .attach()
                .createAsync();

        resourceStream
            .doOnSuccess((_ignore) -> System.out.print("completed"));

        NetworkSecurityGroup nsg = resourceStream.block();

        NetworkInterface nic =
            nsgs
                .manager()
                .networkInterfaces()
                .define(nicName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroupName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withExistingNetworkSecurityGroup(nsg)
                .create();

        nsg.refresh();

        // Verify
        Assertions.assertTrue(nsg.region().equals(region));
        Assertions.assertTrue(nsg.securityRules().size() == 2);

        // Confirm NIC association
        Assertions.assertEquals(1, nsg.networkInterfaceIds().size());
        Assertions.assertTrue(nsg.networkInterfaceIds().contains(nic.id()));

        Assertions.assertEquals(1, nsg.securityRules().get("rule2").sourceApplicationSecurityGroupIds().size());
        Assertions
            .assertEquals(
                asg.id(), nsg.securityRules().get("rule2").sourceApplicationSecurityGroupIds().iterator().next());

        return nsg;
    }

    @Override
    public NetworkSecurityGroup updateResource(NetworkSecurityGroup resource) throws Exception {
        resource =
            resource
                .update()
                .withoutRule("rule1")
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .defineRule("rule3")
                .allowInbound()
                .fromAnyAddress()
                .fromAnyPort()
                .toAnyAddress()
                .toAnyPort()
                .withProtocol(SecurityRuleProtocol.UDP)
                .attach()
                .withoutRule("rule1")
                .updateRule("rule2")
                .denyInbound()
                .fromAddresses("100.0.0.0/29", "100.1.0.0/29")
                .fromPortRanges("88-90")
                .withPriority(300)
                .withDescription("bar!!!")
                .parent()
                .apply();
        Assertions.assertTrue(resource.tags().containsKey("tag1"));
        Assertions.assertTrue(resource.securityRules().get("rule2").sourceApplicationSecurityGroupIds().isEmpty());
        Assertions.assertNull(resource.securityRules().get("rule2").sourceAddressPrefix());
        Assertions.assertEquals(2, resource.securityRules().get("rule2").sourceAddressPrefixes().size());
        Assertions.assertTrue(resource.securityRules().get("rule2").sourceAddressPrefixes().contains("100.1.0.0/29"));
        Assertions.assertEquals(1, resource.securityRules().get("rule2").sourcePortRanges().size());
        Assertions.assertEquals("88-90", resource.securityRules().get("rule2").sourcePortRanges().get(0));

        resource.updateTags().withTag("tag3", "value3").withoutTag("tag1").applyTags();
        Assertions.assertEquals("value3", resource.tags().get("tag3"));
        Assertions.assertFalse(resource.tags().containsKey("tag1"));
        return resource;
    }

    private static StringBuilder printRule(NetworkSecurityRule rule, StringBuilder info) {
        info
            .append("\n\t\tRule: ")
            .append(rule.name())
            .append("\n\t\t\tAccess: ")
            .append(rule.access())
            .append("\n\t\t\tDirection: ")
            .append(rule.direction())
            .append("\n\t\t\tFrom address: ")
            .append(rule.sourceAddressPrefix())
            .append("\n\t\t\tFrom port range: ")
            .append(rule.sourcePortRange())
            .append("\n\t\t\tTo address: ")
            .append(rule.destinationAddressPrefix())
            .append("\n\t\t\tTo port: ")
            .append(rule.destinationPortRange())
            .append("\n\t\t\tProtocol: ")
            .append(rule.protocol())
            .append("\n\t\t\tPriority: ")
            .append(rule.priority())
            .append("\n\t\t\tDescription: ")
            .append(rule.description());
        return info;
    }

    public static void printNSG(NetworkSecurityGroup resource) {
        StringBuilder info = new StringBuilder();
        info
            .append("NSG: ")
            .append(resource.id())
            .append("Name: ")
            .append(resource.name())
            .append("\n\tResource group: ")
            .append(resource.resourceGroupName())
            .append("\n\tRegion: ")
            .append(resource.region())
            .append("\n\tTags: ")
            .append(resource.tags());

        // Output security rules
        info.append("\n\tCustom security rules:");
        for (NetworkSecurityRule rule : resource.securityRules().values()) {
            info = printRule(rule, info);
        }

        // Output default security rules
        info.append("\n\tDefault security rules:");
        for (NetworkSecurityRule rule : resource.defaultSecurityRules().values()) {
            info = printRule(rule, info);
        }

        // Output associated NIC IDs
        info.append("\n\tNICs: ").append(resource.networkInterfaceIds());

        // Output associated subnets
        info.append("\n\tAssociated subnets: ");
        List<Subnet> subnets = resource.listAssociatedSubnets();
        if (subnets == null || subnets.size() == 0) {
            info.append("(None)");
        } else {
            for (Subnet subnet : subnets) {
                info
                    .append("\n\t\tNetwork ID: ")
                    .append(subnet.parent().id())
                    .append("\n\t\tSubnet name: ")
                    .append(subnet.name());
            }
        }

        System.out.println(info.toString());
    }

    @Override
    public void print(NetworkSecurityGroup resource) {
        printNSG(resource);
    }
}
