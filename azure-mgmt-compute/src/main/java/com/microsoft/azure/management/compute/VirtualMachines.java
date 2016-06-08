package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
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
        SupportsCreating<VirtualMachine.DefinitionBlank>,
        SupportsDeleting,
        SupportsDeletingByGroup {
    /**
     * Lists all available virtual machine sizes in a region.
     *
     * @param region The region upon which virtual-machine-sizes is queried.
     * @return the List&lt;VirtualMachineSize&gt; if successful.
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException thrown for IO exception.
     */
    PagedList<VirtualMachineSize> listSizes(String region) throws CloudException, IOException;

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

    // Future implement capture
}
