/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityGroups;
import com.microsoft.azure.management.network.NetworkSecurityRule;
import com.microsoft.azure.management.network.SecurityRuleProtocol;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import okhttp3.logging.HttpLoggingInterceptor;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import rx.Subscriber;

/**
 * Test for network security group CRUD.
 */
public class TestNSG extends TestTemplate<NetworkSecurityGroup, NetworkSecurityGroups> {

    @Override
    public NetworkSecurityGroup createResource(NetworkSecurityGroups nsgs) throws Exception {
        final String newName = "nsg" + this.testId;
        Region region = Region.US_WEST;
        final SettableFuture<NetworkSecurityGroup> nsgFuture = SettableFuture.create();
        // Create
        nsgs.define(newName)
                .withRegion(region)
                .withNewResourceGroup()
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
                    .fromAnyAddress()
                    .fromAnyPort()
                    .toAnyAddress()
                    .toPortRange(22, 25)
                    .withAnyProtocol()
                    .withPriority(200)
                    .withDescription("foo!!")
                    .attach()
                .createAsync()
                .subscribe(new Subscriber<NetworkSecurityGroup>() {
                       @Override
                       public void onCompleted() {
                            System.out.print("completed");
                       }

                       @Override
                       public void onError(Throwable throwable) {
                            nsgFuture.setException(throwable);
                       }

                       @Override
                       public void onNext(NetworkSecurityGroup networkSecurityGroup) {
                            nsgFuture.set(networkSecurityGroup);
                       }
                   });
        NetworkSecurityGroup nsg = nsgFuture.get();

        // Verify
        Assert.assertTrue(nsg.region().equals(region));
        Assert.assertTrue(nsg.securityRules().size() == 2);

        return nsg;
    }

    @Override
    public NetworkSecurityGroup updateResource(NetworkSecurityGroup resource) throws Exception {
        resource =  resource.update()
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
                    .fromAddress("100.0.0.0/29")
                    .fromPort(88)
                    .withPriority(300)
                    .withDescription("bar!!!")
                    .parent()
                .apply();
        Assert.assertTrue(resource.tags().containsKey("tag1"));
        return resource;
    }

    private static StringBuilder printRule(NetworkSecurityRule rule, StringBuilder info) {
        info.append("\n\t\tRule: ").append(rule.name())
            .append("\n\t\t\tAccess: ").append(rule.access())
            .append("\n\t\t\tDirection: ").append(rule.direction())
            .append("\n\t\t\tFrom address: ").append(rule.sourceAddressPrefix())
            .append("\n\t\t\tFrom port range: ").append(rule.sourcePortRange())
            .append("\n\t\t\tTo address: ").append(rule.destinationAddressPrefix())
            .append("\n\t\t\tTo port: ").append(rule.destinationPortRange())
            .append("\n\t\t\tProtocol: ").append(rule.protocol())
            .append("\n\t\t\tPriority: ").append(rule.priority())
            .append("\n\t\t\tDescription: ").append(rule.description());
        return info;
    }

    public static void printNSG(NetworkSecurityGroup resource) {
        StringBuilder info = new StringBuilder();
        info.append("NSG: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags());

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
                info.append("\n\t\tNetwork ID: ").append(subnet.parent().id())
                    .append("\n\t\tSubnet name: ").append(subnet.name());
            }
        }

        System.out.println(info.toString());
    }

    @Override
    public void print(NetworkSecurityGroup resource) {
        printNSG(resource);
    }

    @Test
    public void run() throws Exception {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("client-id"),
                System.getenv("domain"),
                System.getenv("secret"),
                null);

        Azure azure = Azure.configure()
                .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                .authenticate(credentials)
                .withDefaultSubscription();
        runTest(azure.networkSecurityGroups(), azure.resourceGroups());
    }
}
