package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
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
 *  Entry point to virtual machine management API.
 */
public interface VirtualMachines extends
        SupportsListing<VirtualMachine>,
        SupportsListingByGroup<VirtualMachine>,
        SupportsGettingByGroup<VirtualMachine>,
        SupportsGettingById<VirtualMachine>,
        SupportsCreating<VirtualMachine.DefinitionStages.Blank>,
        SupportsDeleting,
        SupportsDeletingByGroup,
        SupportsBatchCreation<VirtualMachine> {

    /**
     * @return entry point to virtual machine sizes
     */
    VirtualMachineSizes sizes();

    /**
     * Shuts down the Virtual Machine and releases the compute resources.
     * <p>
     * You are not billed for the compute resources that this Virtual Machine uses
     *
     * @param groupName the resource group name
     * @param name the virtual machine name
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException thrown for IO exception.
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void deallocate(String groupName, String name) throws CloudException, IOException, InterruptedException;

    /**
     * Generalize the Virtual Machine.
     *
     * @param groupName the resource group name
     * @param name the virtual machine name
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     */
    void generalize(String groupName, String name) throws CloudException, IOException;

    /**
     * Power off (stop) a virtual machine.
     * <p>
     * You will be billed for the compute resources that this Virtual Machine uses
     *
     * @param groupName the resource group name
     * @param name the virtual machine name
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void powerOff(String groupName, String name) throws CloudException, IOException, InterruptedException;

    /**
     * Restart a virtual machine.
     *
     * @param groupName the resource group name
     * @param name the virtual machine name
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void restart(String groupName, String name) throws CloudException, IOException, InterruptedException;

    /**
     * Start a virtual machine.
     *
     * @param groupName the resource group name
     * @param name the virtual machine name
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void start(String groupName, String name) throws CloudException, IOException, InterruptedException;

    /**
     * Redeploy a virtual machine.
     *
     * @param groupName the resource group name
     * @param name the virtual machine name
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void redeploy(String groupName, String name) throws CloudException, IOException, InterruptedException;

    /**
     * Captures the virtual machine by copying virtual hard disks of the VM and returns template as json
     * string that can be used to create similar VMs.
     *
     * @param groupName the resource group name
     * @param name the virtual machine name
     * @param containerName destination container name to store the captured Vhd
     * @param overwriteVhd whether to overwrites destination vhd if it exists
     * @return the template as json string
     * @throws CloudException thrown for an invalid response from the service
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    String capture(String groupName, String name, String containerName, boolean overwriteVhd) throws CloudException, IOException, InterruptedException;
}
