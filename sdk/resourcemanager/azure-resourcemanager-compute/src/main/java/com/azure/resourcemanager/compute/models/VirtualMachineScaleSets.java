// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.fluent.VirtualMachineScaleSetsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import java.io.IOException;
import java.util.List;
import reactor.core.publisher.Mono;

/** Entry point to virtual machine scale set management API. */
@Fluent
public interface VirtualMachineScaleSets
    extends SupportsListing<VirtualMachineScaleSet>,
        SupportsListingByResourceGroup<VirtualMachineScaleSet>,
        SupportsGettingByResourceGroup<VirtualMachineScaleSet>,
        SupportsGettingById<VirtualMachineScaleSet>,
        SupportsCreating<VirtualMachineScaleSet.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<VirtualMachineScaleSet>,
        SupportsBatchDeletion,
        HasManager<ComputeManager>,
        HasInner<VirtualMachineScaleSetsClient> {
    /**
     * Shuts down the virtual machines in the scale set and releases the compute resources.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @throws ManagementException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void deallocate(String groupName, String name) throws ManagementException, IOException, InterruptedException;

    /**
     * Shuts down the virtual machines in the scale set and releases the compute resources asynchronously.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> deallocateAsync(String groupName, String name);

    /**
     * Powers off (stops) the virtual machines in the scale set.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @throws ManagementException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void powerOff(String groupName, String name) throws ManagementException, IOException, InterruptedException;

    /**
     * Powers off (stops) the virtual machines in the scale set asynchronously.
     *
     * @param groupName the name of the resource group the virtual machine in the scale set is in
     * @param name the name of the virtual machine scale set
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> powerOffAsync(String groupName, String name);

    /**
     * Restarts the virtual machines in the scale set.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @throws ManagementException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void restart(String groupName, String name) throws ManagementException, IOException, InterruptedException;

    /**
     * Restarts the virtual machines in the scale set asynchronously.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the virtual machine scale set name
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> restartAsync(String groupName, String name);

    /**
     * Starts the virtual machines in the scale set.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @throws ManagementException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void start(String groupName, String name) throws ManagementException, IOException, InterruptedException;

    /**
     * Starts the virtual machines in the scale set asynchronously.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> startAsync(String groupName, String name);

    /**
     * Re-images (updates the version of the installed operating system) the virtual machines in the scale set.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @throws ManagementException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void reimage(String groupName, String name) throws ManagementException, IOException, InterruptedException;

    /**
     * Re-images (updates the version of the installed operating system) the virtual machines in the scale set
     * asynchronously.
     *
     * @param groupName the name of the resource group the virtual machine scale set is in
     * @param name the name of the virtual machine scale set
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> reimageAsync(String groupName, String name);

    /**
     * Run PowerShell script in a virtual machine instance in a scale set.
     *
     * @param groupName the resource group name
     * @param scaleSetName the virtual machine scale set name
     * @param vmId the virtual machine instance id
     * @param scriptLines PowerShell script lines
     * @param scriptParameters script parameters
     * @return result of PowerShell script execution
     */
    RunCommandResult runPowerShellScriptInVMInstance(
        String groupName,
        String scaleSetName,
        String vmId,
        List<String> scriptLines,
        List<RunCommandInputParameter> scriptParameters);

    /**
     * Run PowerShell in a virtual machine instance in a scale set asynchronously.
     *
     * @param groupName the resource group name
     * @param scaleSetName the virtual machine scale set name
     * @param vmId the virtual machine instance id
     * @param scriptLines PowerShell script lines
     * @param scriptParameters script parameters
     * @return handle to the asynchronous execution
     */
    Mono<RunCommandResult> runPowerShellScriptInVMInstanceAsync(
        String groupName,
        String scaleSetName,
        String vmId,
        List<String> scriptLines,
        List<RunCommandInputParameter> scriptParameters);

    /**
     * Run shell script in a virtual machine instance in a scale set.
     *
     * @param groupName the resource group name
     * @param scaleSetName the virtual machine scale set name
     * @param vmId the virtual machine instance id
     * @param scriptLines shell script lines
     * @param scriptParameters script parameters
     * @return result of shell script execution
     */
    RunCommandResult runShellScriptInVMInstance(
        String groupName,
        String scaleSetName,
        String vmId,
        List<String> scriptLines,
        List<RunCommandInputParameter> scriptParameters);

    /**
     * Run shell script in a virtual machine instance in a scale set asynchronously.
     *
     * @param groupName the resource group name
     * @param scaleSetName the virtual machine scale set name
     * @param vmId the virtual machine instance id
     * @param scriptLines shell script lines
     * @param scriptParameters script parameters
     * @return handle to the asynchronous execution
     */
    Mono<RunCommandResult> runShellScriptInVMInstanceAsync(
        String groupName,
        String scaleSetName,
        String vmId,
        List<String> scriptLines,
        List<RunCommandInputParameter> scriptParameters);

    /**
     * Run commands in a virtual machine instance in a scale set.
     *
     * @param groupName the resource group name
     * @param scaleSetName the virtual machine scale set name
     * @param vmId the virtual machine instance id
     * @param inputCommand command input
     * @return result of execution
     */
    RunCommandResult runCommandInVMInstance(
        String groupName, String scaleSetName, String vmId, RunCommandInput inputCommand);

    /**
     * Run commands in a virtual machine instance in a scale set asynchronously.
     *
     * @param groupName the resource group name
     * @param scaleSetName the virtual machine scale set name
     * @param vmId the virtual machine instance id
     * @param inputCommand command input
     * @return handle to the asynchronous execution
     */
    Mono<RunCommandResult> runCommandVMInstanceAsync(
        String groupName, String scaleSetName, String vmId, RunCommandInput inputCommand);
}
