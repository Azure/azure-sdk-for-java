/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import org.junit.Assert;

import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.network.LoadBalancerPublicFrontend;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.PublicIPAddresses;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * Tests public IPs.
 */
public class TestPublicIPAddress extends TestTemplate<PublicIPAddress, PublicIPAddresses> {
    @Override
    public PublicIPAddress createResource(PublicIPAddresses pips) throws Exception {
        final String newPipName = "pip" + this.testId;
        PublicIPAddress pip = pips.define(newPipName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup()
                .withDynamicIP()
                .withLeafDomainLabel(newPipName)
                .withIdleTimeoutInMinutes(10)
                .create();
        return pip;
    }

    @Override
    public PublicIPAddress updateResource(PublicIPAddress resource) throws Exception {
        final String updatedDnsName = resource.leafDomainLabel() + "xx";
        final int updatedIdleTimeout = 15;
        resource =  resource.update()
                .withStaticIP()
                .withLeafDomainLabel(updatedDnsName)
                .withReverseFqdn(resource.leafDomainLabel() + "." + resource.region() + ".cloudapp.azure.com")
                .withIdleTimeoutInMinutes(updatedIdleTimeout)
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();
        Assert.assertTrue(resource.leafDomainLabel().equalsIgnoreCase(updatedDnsName));
        Assert.assertTrue(resource.idleTimeoutInMinutes() == updatedIdleTimeout);
        return resource;
    }

    @Override
    public void print(PublicIPAddress pip) {
        TestPublicIPAddress.printPIP(pip);
    }

    public static void printPIP(PublicIPAddress resource) {
        StringBuilder info = new StringBuilder().append("Public IP Address: ").append(resource.id())
                .append("\n\tName: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tIP Address: ").append(resource.ipAddress())
                .append("\n\tLeaf domain label: ").append(resource.leafDomainLabel())
                .append("\n\tFQDN: ").append(resource.fqdn())
                .append("\n\tReverse FQDN: ").append(resource.reverseFqdn())
                .append("\n\tIdle timeout (minutes): ").append(resource.idleTimeoutInMinutes())
                .append("\n\tIP allocation method: ").append(resource.ipAllocationMethod().toString())
                .append("\n\tIP version: ").append(resource.version().toString());

        // Show the associated load balancer if any
        info.append("\n\tLoad balancer association: ");
        if (resource.hasAssignedLoadBalancer()) {
            final LoadBalancerPublicFrontend frontend = resource.getAssignedLoadBalancerFrontend();
            final LoadBalancer lb = frontend.parent();
            info.append("\n\t\tLoad balancer ID: ").append(lb.id())
                .append("\n\t\tFrontend name: ").append(frontend.name());
        } else {
            info.append("(None)");
        }

        // Show the associated NIC if any
        info.append("\n\tNetwork interface association: ");
        if (resource.hasAssignedNetworkInterface()) {
            final NicIPConfiguration nicIp = resource.getAssignedNetworkInterfaceIPConfiguration();
            final NetworkInterface nic = nicIp.parent();
            info.append("\n\t\tNetwork interface ID: ").append(nic.id())
                .append("\n\t\tIP config name: ").append(nicIp.name());
        } else {
            info.append("(None)");
        }

        System.out.println(info.toString());
    }
}
