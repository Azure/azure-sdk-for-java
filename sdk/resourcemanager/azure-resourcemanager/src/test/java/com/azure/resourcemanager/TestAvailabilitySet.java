// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.models.AvailabilitySet;
import com.azure.resourcemanager.compute.models.AvailabilitySets;
import com.azure.resourcemanager.compute.models.VirtualMachineSize;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.core.management.Region;
import org.junit.jupiter.api.Assertions;

public class TestAvailabilitySet extends TestTemplate<AvailabilitySet, AvailabilitySets> {
    @Override
    public AvailabilitySet createResource(AvailabilitySets availabilitySets) throws Exception {
        final String newName = availabilitySets.manager().resourceManager().internalContext().randomResourceName("as", 10);
        AvailabilitySet aset =
            availabilitySets
                .define(newName)
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
        resource = resource.update().withTag("tag2", "value2").withTag("tag3", "value3").withoutTag("tag1").apply();
        Assertions.assertTrue(resource.tags().containsKey("tag2"));
        Assertions.assertTrue(!resource.tags().containsKey("tag1"));
        return resource;
    }

    @Override
    public void print(AvailabilitySet resource) {
        System
            .out
            .println(
                new StringBuilder()
                    .append("Availability Set: ")
                    .append(resource.id())
                    .append("Name: ")
                    .append(resource.name())
                    .append("\n\tResource group: ")
                    .append(resource.resourceGroupName())
                    .append("\n\tRegion: ")
                    .append(resource.region())
                    .append("\n\tTags: ")
                    .append(resource.tags())
                    .append("\n\tFault domain count: ")
                    .append(resource.faultDomainCount())
                    .append("\n\tUpdate domain count: ")
                    .append(resource.updateDomainCount())
                    .toString());
    }
}
