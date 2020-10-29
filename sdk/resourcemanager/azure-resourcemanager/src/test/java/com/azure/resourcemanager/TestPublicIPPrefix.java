// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.models.IpVersion;
import com.azure.resourcemanager.network.models.PublicIpPrefix;
import com.azure.resourcemanager.network.models.PublicIpPrefixSku;
import com.azure.resourcemanager.network.models.PublicIpPrefixSkuName;
import com.azure.resourcemanager.network.models.PublicIpPrefixes;
import com.azure.core.management.Region;
import org.junit.jupiter.api.Assertions;

/** Tests public Prefixes. */
public class TestPublicIPPrefix extends TestTemplate<PublicIpPrefix, PublicIpPrefixes> {
    @Override
    public PublicIpPrefix createResource(PublicIpPrefixes pips) throws Exception {
        final String newPipName = pips.manager().resourceManager().internalContext().randomResourceName("pip", 10);

        PublicIpPrefix pip = pips.define(newPipName)
            .withRegion(Region.US_WEST)
            .withNewResourceGroup()
            .withPrefixLength(28)
            .withSku(new PublicIpPrefixSku().withName(PublicIpPrefixSkuName.STANDARD))
            .create();

        Assertions.assertEquals(pip.prefixLength(), (Integer) 28);
        Assertions.assertEquals(pip.sku().name().toString(), "Standard");
        Assertions.assertTrue(pip.publicIpAddressVersion() == IpVersion.IPV4);
        return pip;
    }

    @Override
    public PublicIpPrefix updateResource(PublicIpPrefix resource) throws Exception {
        resource = resource.update()
            .withTag("tag1", "value1")
            .withTag("tag2", "value2")
            .apply();
        Assertions.assertEquals("value1", resource.tags().get("tag1"));
        Assertions.assertEquals("value2", resource.tags().get("tag2"));

        return resource;
    }

    @Override
    public void print(PublicIpPrefix pip) {
        TestPublicIPPrefix.printPIP(pip);
    }

    public static void printPIP(PublicIpPrefix resource) {
        StringBuilder info = new StringBuilder().append("Public IP Address: ").append(resource.id())
            .append("\n\tName: ").append(resource.name())
            .append("\n\tResource group: ").append(resource.resourceGroupName())
            .append("\n\tRegion: ").append(resource.region())
            .append("\n\tTags: ").append(resource.tags())
            .append("\n\tAvailability Zones: ").append(resource.availabilityZones())
            .append("\n\tLeaf domain label: ").append(resource.sku())
            .append("\n\tFQDN: ").append(resource.publicIpAddresses())
            .append("\n\tReverse FQDN: ").append(resource.publicIpAddressVersion());

        // Show the associated load balancer if any
        info.append("\n\tLoad balancer association: ");
        if (resource.loadBalancerFrontendIpConfiguration() != null) {
            final SubResource frontend = resource.loadBalancerFrontendIpConfiguration();
            info.append("\n\t\tLoad balancer ID: ").append(frontend.id());
        } else {
            info.append("(None)");
        }

        System.out.println(info.toString());
    }
}
