package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.compute.implementation.api.AvailabilitySetInner;
import com.microsoft.azure.management.compute.implementation.api.InstanceViewStatus;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;

/**
 * An immutable client-side representation of an Azure availability set. 
 */
public interface AvailabilitySet extends
        GroupableResource,
        Refreshable<AvailabilitySet>,
        Wrapper<AvailabilitySetInner>,
        Updatable<AvailabilitySet.Update> {

    /**
     * Returns the update domain count of an availability set.
     * <p>
     * An update domain represents the group of virtual
     * machines and underlying physical hardware that can be rebooted at the same time.
     *
     * @return the update domain count
     */
    int updateDomainCount();

    /**
     * Returns the fault domain count of availability set.
     * <p>
     * A fault domain represents the group of virtual
     * machines that shares common power source and network switch.
     *
     * @return the fault domain count
     */
    int faultDomainCount();

    /**
     * Lists the resource IDs of the virtual machines in the availability set.
     *
     * @return list of resource ID strings
     */
    List<String> virtualMachineIds();

    /**
     * Lists the virtual machines in the availability set.
     *
     * @return list of virtual machines
     */
    List<VirtualMachine> virtualMachines() throws Exception;

    /**
     * Lists the statuses of the existing virtual machines in the availability set.
     *
     * @return list of virtual machine statuses
     */
    List<InstanceViewStatus> statuses();


    /**************************************************************
     * Fluent interfaces to provision an AvailabilitySet
     **************************************************************/

    /**
     * The first stage of an availability set definition
     */
    interface DefinitionBlank extends GroupableResource.DefinitionWithRegion<DefinitionWithGroup> {
    }

    /**
     * The stage of the availability set definition allowing to specify the resource group
     */
    interface DefinitionWithGroup extends GroupableResource.DefinitionWithGroup<DefinitionCreatable> {
    }

    /**
     * The stage of an availability set definition which contains all the minimum required inputs for
     * the resource to be created (via {@link DefinitionCreatable#create()}), but also allows 
     * for any other optional settings to be specified.
     */     
    interface DefinitionCreatable extends 
        Creatable<AvailabilitySet>,
        Resource.DefinitionWithTags<DefinitionCreatable> {
        /**
         * Specifies the update domain count for the availability set.
         * @param updateDomainCount update domain count
         * @return the next stage of the resource definition
         */
        DefinitionCreatable withUpdateDomainCount(int updateDomainCount);
        
        /**
         * Specifies the fault domain count for the availability set.
         * @param faultDomainCount fault domain count
         * @return the next stage of the resource definition
         */
        DefinitionCreatable withFaultDomainCount(int faultDomainCount);
    }
    
    /**
     * The template for an availability set update operation, containing all the settings that 
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends 
        Appliable<AvailabilitySet>,
        Resource.UpdateWithTags<Update> {
    }
}
