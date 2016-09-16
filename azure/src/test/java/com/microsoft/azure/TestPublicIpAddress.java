/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure;

import org.junit.Assert;

import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * Tests public IPs.
 */
public class TestPublicIpAddress extends TestTemplate<PublicIpAddress, PublicIpAddresses> {

    @Override
    public PublicIpAddress createResource(PublicIpAddresses pips) throws Exception {
        final String newPipName = "pip" + this.testId;
        PublicIpAddress pip = pips.define(newPipName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup()
                .withDynamicIp()
                .withLeafDomainLabel(newPipName)
                .withIdleTimeoutInMinutes(10)
                .create();
        return pip;
    }

    @Override
    public PublicIpAddress updateResource(PublicIpAddress resource) throws Exception {
        final String updatedDnsName = resource.leafDomainLabel() + "xx";
        final int updatedIdleTimeout = 15;
        resource =  resource.update()
                .withStaticIp()
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
    public void print(PublicIpAddress pip) {
        TestPublicIpAddress.printPIP(pip);
    }

    public static void printPIP(PublicIpAddress resource) {
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

        // Show the associated load balancer frontend if any
        info.append("\n\tAssociated load balancer frontend name: ");
        if (resource.hasAssignedLoadBalancerFrontend()) {
            info.append(resource.getAssignedLoadBalancerFrontend().name());
        } else {
            info.append("(None)");
        }

        System.out.println(info.toString());
    }
}
