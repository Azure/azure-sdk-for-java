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

    /**
     * Get the update domain count of availability set, an update domain represents the group of virtual
     * machines and underlying physical hardware that can be rebooted at the same time.
     *
     * @return the platformUpdateDomainCount value
     */
    Integer updateDomainCount();

    /**
     * Get the fault domain count of availability set., a fault domain represents the group of virtual
     * machines that shares common power source and network switch.
     *
     * @return the platformUpdateDomainCount value
     */
    Integer FaultDomainCount();

    /**
     * Get the list of ids of virtual machines in the availability set.
     *
     * @return the virtualMachineIds value
     */
    List<String> virtualMachineIds();

    /**
     * Get the list of virtual machines in the availability set.
     *
     * @return the virtualMachineIds value
     */
    List<VirtualMachine> virtualMachines() throws Exception;

    /**
     * Get the statuses value.
     *
     * @return the statuses value
     */
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
