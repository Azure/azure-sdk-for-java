// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager;

import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerPublicFrontend;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.PublicIpAddresses;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;

/** Tests public IPs. */
public class TestPublicIPAddress extends TestTemplate<PublicIpAddress, PublicIpAddresses> {
    @Override
    public PublicIpAddress createResource(PublicIpAddresses pips) throws Exception {
        final String newPipName = pips.manager().sdkContext().randomResourceName("pip", 10);

        PublicIpAddress pip =
            pips
                .define(newPipName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup()
                .withDynamicIP()
                .withLeafDomainLabel(newPipName)
                .withIdleTimeoutInMinutes(10)
                .create();
        return pip;
    }

    @Override
    public PublicIpAddress updateResource(PublicIpAddress resource) throws Exception {
        final String updatedDnsName = resource.leafDomainLabel() + "xx";
        final int updatedIdleTimeout = 15;
        resource =
            resource
                .update()
                .withStaticIP()
                .withLeafDomainLabel(updatedDnsName)
                .withReverseFqdn(resource.leafDomainLabel() + "." + resource.region() + ".cloudapp.azure.com")
                .withIdleTimeoutInMinutes(updatedIdleTimeout)
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();
        Assertions.assertTrue(resource.leafDomainLabel().equalsIgnoreCase(updatedDnsName));
        Assertions.assertTrue(resource.idleTimeoutInMinutes() == updatedIdleTimeout);
        Assertions.assertEquals("value2", resource.tags().get("tag2"));

        resource.updateTags().withoutTag("tag1").withTag("tag3", "value3").applyTags();
        Assertions.assertFalse(resource.tags().containsKey("tag1"));
        Assertions.assertEquals("value3", resource.tags().get("tag3"));
        return resource;
    }

    @Override
    public void print(PublicIpAddress pip) {
        TestPublicIPAddress.printPIP(pip);
    }

    public static void printPIP(PublicIpAddress resource) {
        StringBuilder info =
            new StringBuilder()
                .append("Public IP Address: ")
                .append(resource.id())
                .append("\n\tName: ")
                .append(resource.name())
                .append("\n\tResource group: ")
                .append(resource.resourceGroupName())
                .append("\n\tRegion: ")
                .append(resource.region())
                .append("\n\tTags: ")
                .append(resource.tags())
                .append("\n\tIP Address: ")
                .append(resource.ipAddress())
                .append("\n\tLeaf domain label: ")
                .append(resource.leafDomainLabel())
                .append("\n\tFQDN: ")
                .append(resource.fqdn())
                .append("\n\tReverse FQDN: ")
                .append(resource.reverseFqdn())
                .append("\n\tIdle timeout (minutes): ")
                .append(resource.idleTimeoutInMinutes())
                .append("\n\tIP allocation method: ")
                .append(resource.ipAllocationMethod().toString())
                .append("\n\tIP version: ")
                .append(resource.version().toString());

        // Show the associated load balancer if any
        info.append("\n\tLoad balancer association: ");
        if (resource.hasAssignedLoadBalancer()) {
            final LoadBalancerPublicFrontend frontend = resource.getAssignedLoadBalancerFrontend();
            final LoadBalancer lb = frontend.parent();
            info
                .append("\n\t\tLoad balancer ID: ")
                .append(lb.id())
                .append("\n\t\tFrontend name: ")
                .append(frontend.name());
        } else {
            info.append("(None)");
        }

        // Show the associated NIC if any
        info.append("\n\tNetwork interface association: ");
        if (resource.hasAssignedNetworkInterface()) {
            final NicIpConfiguration nicIp = resource.getAssignedNetworkInterfaceIPConfiguration();
            final NetworkInterface nic = nicIp.parent();
            info
                .append("\n\t\tNetwork interface ID: ")
                .append(nic.id())
                .append("\n\t\tIP config name: ")
                .append(nicIp.name());
        } else {
            info.append("(None)");
        }

        System.out.println(info.toString());
    }
}
