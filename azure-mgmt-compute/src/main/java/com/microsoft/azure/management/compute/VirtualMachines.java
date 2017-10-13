/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.compute.implementation.VirtualMachinesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;

/**
 *  Entry point to virtual machine management API.
 */
@Fluent
public interface VirtualMachines extends
        SupportsListing<VirtualMachine>,
        SupportsListingByResourceGroup<VirtualMachine>,
        SupportsGettingByResourceGroup<VirtualMachine>,
        SupportsGettingById<VirtualMachine>,
        SupportsCreating<VirtualMachine.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<VirtualMachine>,
        SupportsBatchDeletion,
        HasManager<ComputeManager>,
        HasInner<VirtualMachinesInner> {

    /**
     * @return available virtual machine sizes
     */
    VirtualMachineSizes sizes();

    /**
     * Shuts down the virtual machine and releases the compute resources.
     *
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     */
    void deallocate(String groupName, String name);

    /**
     * Shuts down the virtual machine and releases the compute resources asynchronously.
     *
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     *
     * @return a representation of the deferred computation of this call
     */
    Completable deallocateAsync(String groupName, String name);

    /**
     * Shuts down the virtual machine and releases the compute resources asynchronously.
     *
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> deallocateAsync(String groupName, String name, ServiceCallback<Void> callback);

    /**
     * Generalizes the virtual machine.
     *
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     */
    void generalize(String groupName, String name);

    /**
     * Generalizes the virtual machine asynchronously.
     *
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     * @return a representation of the deferred computation of this call
     */
    Completable generalizeAsync(String groupName, String name);

    /**
     * Generalizes the virtual machine asynchronously.
     *
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> generalizeAsync(String groupName, String name, ServiceCallback<Void> callback);

    /**
     * Powers off (stops) a virtual machine.
     *
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     */
    void powerOff(String groupName, String name);

    /**
     * Powers off (stops) the virtual machine asynchronously.
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     *
     * @return a representation of the deferred computation of this call
     */
    Completable powerOffAsync(String groupName, String name);

    /**
     * Powers off (stop) the virtual machine asynchronously.
     *
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> powerOffAsync(String groupName, String name, ServiceCallback<Void> callback);

    /**
     * Restarts a virtual machine.
     *
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     */
    void restart(String groupName, String name);

    /**
     * Restarts the virtual machine asynchronously.
     *
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     * @return a representation of the deferred computation of this call
     */
    Completable restartAsync(String groupName, String name);

    /**
     * Restarts the virtual machine asynchronously.
     *
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> restartAsync(String groupName, String name, ServiceCallback<Void> callback);

    /**
     * Starts a virtual machine.
     *
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     */
    void start(String groupName, String name);

    /**
     * Starts the virtual machine asynchronously.
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     *
     * @return a representation of the deferred computation of this call
     */
    Completable startAsync(String groupName, String name);

    /**
     * Starts the virtual machine asynchronously.
     *
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> startAsync(String groupName, String name, ServiceCallback<Void> callback);

    /**
     * Redeploys a virtual machine.
     *
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     */
    void redeploy(String groupName, String name);

    /**
     * Redeploys the virtual machine asynchronously.
     *
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     * @return a representation of the deferred computation of this call
     */
    Completable redeployAsync(String groupName, String name);

    /**
     * Redeploys the virtual machine asynchronously.
     *
     * @param groupName the name of the resource group the virtual machine is in
     * @param name the virtual machine name
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> redeployAsync(String groupName, String name, ServiceCallback<Void> callback);

    /**
     * Captures the virtual machine by copying virtual hard disks of the VM and returns template as a JSON
     * string that can be used to create similar VMs.
     *
     * @param groupName the resource group name
     * @param name the virtual machine name
     * @param containerName destination container name to store the captured VHD
     * @param vhdPrefix the prefix for the VHD holding captured image
     * @param overwriteVhd whether to overwrites destination VHD if it exists
     * @return the template as JSON string
     */
    String capture(String groupName, String name, String containerName, String vhdPrefix, boolean overwriteVhd);

    /**
     * Captures the virtual machine by copying virtual hard disks of the VM asynchronously.
     *
     * @param groupName the resource group name
     * @param name the virtual machine name
     * @param containerName destination container name to store the captured VHD
     * @param vhdPrefix the prefix for the VHD holding captured image
     * @param overwriteVhd whether to overwrites destination VHD if it exists
     * @return a representation of the deferred computation of this call
     */
    Observable<String> captureAsync(String groupName, String name, String containerName, String vhdPrefix, boolean overwriteVhd);

    /**
     * Captures the virtual machine by copying virtual hard disks of the VM asynchronously.
     *
     * @param groupName the resource group name
     * @param name the virtual machine name
     * @param containerName destination container name to store the captured VHD
     * @param vhdPrefix the prefix for the VHD holding captured image
     * @param overwriteVhd whether to overwrites destination VHD if it exists
     * @param callback the callback to call on success or failure
     * @return a representation of the deferred computation of this call
     */
    ServiceFuture<String> captureAsync(String groupName, String name, String containerName, String vhdPrefix, boolean overwriteVhd, ServiceCallback<String> callback);

    /**
     * Migrates the virtual machine with unmanaged disks to use managed disks.
     *
     * @param groupName the resource group name
     * @param name the virtual machine name
     */
    void migrateToManaged(String groupName, String name);

    /**
     * Converts (migrates) the virtual machine with un-managed disks to use managed disk asynchronously.
     *
     * @param groupName the resource group name
     * @param name the virtual machine name
     * @return a representation of the deferred computation of this call
     */
    Completable migrateToManagedAsync(String groupName, String name);

    /**
     * Converts (migrates) the virtual machine with un-managed disks to use managed disk asynchronously.
     *
     * @param groupName the resource group name
     * @param name the virtual machine name
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> migrateToManagedAsync(String groupName, String name, ServiceCallback<Void> callback);
}
