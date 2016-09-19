package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

import java.io.IOException;

/**
 *  Entry point to virtual machine scale set management API.
 */
@LangDefinition(ContainerName = "~/")
public interface VirtualMachineScaleSets extends
        SupportsListing<VirtualMachineScaleSet>,
        SupportsListingByGroup<VirtualMachineScaleSet>,
        SupportsGettingByGroup<VirtualMachineScaleSet>,
        SupportsGettingById<VirtualMachineScaleSet>,
        SupportsCreating<VirtualMachineScaleSet.DefinitionStages.Blank>,
        SupportsDeleting,
        SupportsDeletingByGroup,
        SupportsBatchCreation<VirtualMachineScaleSet> {
    /**
     * Shuts down the Virtual Machine in the scale set and releases the compute resources.
     * <p>
     * You are not billed for the compute resources that the Virtual Machines uses
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void deallocate(String groupName, String name) throws CloudException, IOException, InterruptedException;

    /**
     * Power off (stop) the virtual machines in the scale set.
     * <p>
     * You will be billed for the compute resources that the Virtual Machines uses.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void powerOff(String groupName, String name) throws CloudException, IOException, InterruptedException;

    /**
     * Restart the virtual machines in the scale set.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void restart(String groupName, String name) throws CloudException, IOException, InterruptedException;

    /**
     * Start the virtual machines  in the scale set.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void start(String groupName, String name) throws CloudException, IOException, InterruptedException;

    /**
     * Re-image (update the version of the installed operating system) the virtual machines in the scale set.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void reimage(String groupName, String name) throws CloudException, IOException, InterruptedException;
}
