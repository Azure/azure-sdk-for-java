package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.compute.implementation.api.AvailabilitySetInner;
import com.microsoft.azure.management.compute.implementation.api.InstanceViewStatus;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Provisionable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;

public interface AvailabilitySet extends
        GroupableResource,
        Refreshable<AvailabilitySet>,
        Wrapper<AvailabilitySetInner> {

    /***********************************************************
     * Getters
     ***********************************************************/

    Integer updateDomainCount();
    Integer FaultDomainCount();
    List<String> virtualMachineIds();
    List<VirtualMachine> virtualMachines() throws Exception;
    List<InstanceViewStatus> statuses();

    /**************************************************************
     * Fluent interfaces to provision an AvailabilitySet
     **************************************************************/

    interface DefinitionBlank extends GroupableResource.DefinitionWithRegion<DefinitionWithGroup> {
    }

    interface DefinitionWithGroup extends GroupableResource.DefinitionWithGroup<DefinitionProvisionable> {
    }

    interface DefinitionProvisionable extends Provisionable<AvailabilitySet> {
        DefinitionProvisionable withUpdateDomainCount(Integer updateDomainCount);
        DefinitionProvisionable withFaultDomainCount(Integer faultDomainCount);
    }
}
