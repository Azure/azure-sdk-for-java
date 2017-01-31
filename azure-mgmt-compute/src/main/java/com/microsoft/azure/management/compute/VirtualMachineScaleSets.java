/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

import java.io.IOException;

/**
 *  Entry point to virtual machine scale set management API.
 */
@Fluent
public interface VirtualMachineScaleSets extends
        SupportsListing<VirtualMachineScaleSet>,
        SupportsListingByGroup<VirtualMachineScaleSet>,
        SupportsGettingByGroup<VirtualMachineScaleSet>,
        SupportsGettingById<VirtualMachineScaleSet>,
        SupportsCreating<VirtualMachineScaleSet.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsDeletingByGroup,
        SupportsBatchCreation<VirtualMachineScaleSet>,
        HasManager<ComputeManager> {
    /**
     * Shuts down the virtual machine in the scale set and releases the compute resources.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void deallocate(String groupName, String name) throws CloudException, IOException, InterruptedException;

    /**
     * Powers off (stops) the virtual machines in the scale set.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void powerOff(String groupName, String name) throws CloudException, IOException, InterruptedException;

    /**
     * Restarts the virtual machines in the scale set.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void restart(String groupName, String name) throws CloudException, IOException, InterruptedException;

    /**
     * Starts the virtual machines in the scale set.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void start(String groupName, String name) throws CloudException, IOException, InterruptedException;

    /**
     * Re-images (updates the version of the installed operating system) the virtual machines in the scale set.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void reimage(String groupName, String name) throws CloudException, IOException, InterruptedException;
}
