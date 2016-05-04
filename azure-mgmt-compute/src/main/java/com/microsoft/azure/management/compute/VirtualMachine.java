package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.compute.implementation.api.AvailabilitySetInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

public interface VirtualMachine extends
        GroupableResource,
        Refreshable<AvailabilitySet>,
        Wrapper<AvailabilitySetInner> {
}
