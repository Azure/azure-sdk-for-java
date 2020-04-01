/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management;

import com.azure.core.http.rest.PagedIterable;
import com.azure.management.compute.AvailabilitySet;
import com.azure.management.compute.AvailabilitySets;
import com.azure.management.compute.VirtualMachineSize;
import com.azure.management.resources.core.TestUtilities;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;

public class TestAvailabilitySet extends TestTemplate<AvailabilitySet, AvailabilitySets> {
    @Override
    public AvailabilitySet createResource(AvailabilitySets availabilitySets) throws Exception {
        final String newName = availabilitySets.manager().getSdkContext().randomResourceName("as", 10);
        AvailabilitySet aset = availabilitySets.define(newName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withFaultDomainCount(2)
                .withUpdateDomainCount(4)
                .withTag("tag1", "value1")
                .create();
        PagedIterable<VirtualMachineSize> vmSizes = aset.listVirtualMachineSizes();
        Assertions.assertTrue(TestUtilities.getSize(vmSizes) > 0);
        for (VirtualMachineSize vmSize : vmSizes) {
            Assertions.assertNotNull(vmSize.name());
        }
        return aset;
    }

    @Override
    public AvailabilitySet updateResource(AvailabilitySet resource) throws Exception {
        // Modify existing availability set
        resource = resource.update()
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .apply();
        Assertions.assertTrue(resource.tags().containsKey("tag2"));
        Assertions.assertTrue(!resource.tags().containsKey("tag1"));
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
