/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure;

import org.junit.Assert;

import com.microsoft.azure.implementation.Azure;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

public class TestPublicIpAddress extends TestTemplate<PublicIpAddress, PublicIpAddresses> {

    @Override
    public PublicIpAddress createResource(Azure azure) throws Exception {
        final String newPipName = "pip" + this.testId;
        return azure.publicIpAddresses().define(newPipName)
                .withRegion(Region.US_WEST)
                .withNewGroup()
                .withDynamicIp()
                .withLeafDomainLabel(newPipName)
                .withIdleTimeoutInMinutes(10)
                .create();
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
        Assert.assertTrue(resource.idleTimeoutInMinutes()==updatedIdleTimeout);
        return resource;
    }

    @Override
    public void print(PublicIpAddress resource) {
        System.out.println(new StringBuilder().append("Public IP Address: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tIP Address: ").append(resource.ipAddress())
                .append("\n\tLeaf domain label: ").append(resource.leafDomainLabel())
                .append("\n\tFQDN: ").append(resource.fqdn())
                .append("\n\tReverse FQDN: ").append(resource.reverseFqdn())
                .append("\n\tIdle timeout (minutes): ").append(resource.idleTimeoutInMinutes())
                .append("\n\tIP allocation method: ").append(resource.ipAllocationMethod())
                .toString());
    }
}
