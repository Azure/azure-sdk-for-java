/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.VirtualMachineSize;
import org.junit.Assert;

import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

public class TestAvailabilitySet extends TestTemplate<AvailabilitySet, AvailabilitySets> {

    @Override
    public AvailabilitySet createResource(AvailabilitySets availabilitySets) throws Exception {
        final String newName = "as" + this.testId;
        AvailabilitySet aset = availabilitySets.define(newName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withFaultDomainCount(2)
                .withUpdateDomainCount(4)
                .withTag("tag1", "value1")
                .create();
        PagedList<VirtualMachineSize> vmSizes = aset.listVirtualMachineSizes();
        Assert.assertTrue(vmSizes.size() > 0);
        for (VirtualMachineSize vmSize : vmSizes) {
            Assert.assertNotNull(vmSize.name());
        }
        return aset;
    }

    @Override
    public AvailabilitySet updateResource(AvailabilitySet resource) throws Exception {
        // Modify existing availability set
        resource =  resource.update()
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .apply();
        Assert.assertTrue(resource.tags().containsKey("tag2"));
        Assert.assertTrue(!resource.tags().containsKey("tag1"));
        return resource;
    }

    @Override
    public void print(AvailabilitySet resource) {
        System.out.println(new StringBuilder().append("Availability Set: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tFault domain count: ").append(resource.faultDomainCount())
                .append("\n\tUpdate domain count: ").append(resource.updateDomainCount())
                .toString());
    }
}
