// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineScaleSetVMInner;
import com.azure.resourcemanager.network.models.VirtualMachineScaleSetNetworkInterface;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of a virtual machine instance in an Azure virtual machine scale set. */
@Fluent
public interface VirtualMachineScaleSetVM
    extends Resource, ChildResource<VirtualMachineScaleSet>, Refreshable<VirtualMachineScaleSetVM>,
    Updatable<VirtualMachineScaleSetVM.Update>, HasInnerModel<VirtualMachineScaleSetVMInner> {
    /**
     * Gets the instance ID assigned to this virtual machine instance.
     *
     * @return the instance ID assigned to this virtual machine instance
     */
    String instanceId();

    /**
     * Gets the SKU of the virtual machine instance.
     *
     * @return the SKU of the virtual machine instance, this will be SKU used while creating the parent virtual machine
     *     scale set
     */
    Sku sku();

    /**
     * Gets virtual machine instance size.
     *
     * @return virtual machine instance size
     */
    VirtualMachineSizeTypes size();

    /**
     * Checks whether the latest scale set model changes are applied to the virtual machine instance.
     *
     * @return true if the latest scale set model changes are applied to the virtual machine instance
     */
    boolean isLatestScaleSetUpdateApplied();

    /**
     * Checks whether the operating system of the virtual machine instance is based on platform image.
     *
     * @return true if the operating system of the virtual machine instance is based on platform image
     */
    boolean isOSBasedOnPlatformImage();

    /**
     * Checks whether the operating system of the virtual machine instance is based on custom image.
     *
     * @return true if the operating system of the virtual machine instance is based on custom image
     */
    boolean isOSBasedOnCustomImage();

    /**
     * Checks whether the operating system of the virtual machine instance is based on stored image.
     *
     * @return true if the operating system of the virtual machine instance is based on stored image
     */
    boolean isOSBasedOnStoredImage();

    /**
     * Gets reference to the platform image.
     *
     * @return reference to the platform image that the virtual machine instance operating system is based on, null will
     *     be returned if the operating system is based on custom image
     */
    ImageReference platformImageReference();

    /**
     * Gets the platform image.
     *
     * @return the platform image that the virtual machine instance operating system is based on, null be returned
     *     otherwise
     */
    VirtualMachineImage getOSPlatformImage();

    /**
     * Gets the custom image.
     *
     * @return the custom image that the virtual machine instance operating system is based on, null be returned
     *     otherwise
     */
    VirtualMachineCustomImage getOSCustomImage();

    /**
     * Gets VHD URI of the custom image.
     *
     * @return VHD URI of the custom image that the virtual machine instance operating system is based on, null will be
     *     returned if the operating system is based on platform image
     */
    String storedImageUnmanagedVhdUri();

    /**
     * Gets the name of the operating system disk.
     *
     * @return the name of the operating system disk
     */
    String osDiskName();

    /**
     * Gets VHD URI to the operating system disk.
     *
     * @return VHD URI to the operating system disk
     */
    String osUnmanagedDiskVhdUri();

    /**
     * Gets resource ID of the managed disk backing OS disk.
     *
     * @return resource ID of the managed disk backing OS disk
     */
    String osDiskId();

    /**
     * Gets the unmanaged data disks associated with this virtual machine instance.
     *
     * @return the unmanaged data disks associated with this virtual machine instance, indexed by LUN
     */
    Map<Integer, VirtualMachineUnmanagedDataDisk> unmanagedDataDisks();

    /**
     * Gets the managed data disks associated with this virtual machine instance.
     *
     * @return the managed data disks associated with this virtual machine instance, indexed by LUN
     */
    Map<Integer, VirtualMachineDataDisk> dataDisks();

    /**
     * Gets the caching type of the operating system disk.
     *
     * @return the caching type of the operating system disk
     */
    CachingTypes osDiskCachingType();

    /**
     * Gets the size of the operating system disk.
     *
     * @return the size of the operating system disk
     */
    int osDiskSizeInGB();

    /**
     * Gets the virtual machine instance computer name with the VM scale set prefix.
     *
     * @return the virtual machine instance computer name with the VM scale set prefix.
     */
    String computerName();

    /**
     * Gets the name of the admin user.
     *
     * @return the name of the admin user
     */
    String administratorUserName();

    /**
     * Gets the operating system type.
     *
     * @return the operating system type
     */
    OperatingSystemTypes osType();

    /**
     * Checks whether this is a Linux virtual machine and password based login is enabled.
     *
     * @return true if this is a Linux virtual machine and password based login is enabled, false otherwise
     */
    boolean isLinuxPasswordAuthenticationEnabled();

    /**
     * Checks whether this is a Windows virtual machine and VM agent is provisioned.
     *
     * @return true if this is a Windows virtual machine and VM agent is provisioned, false otherwise
     */
    boolean isWindowsVMAgentProvisioned();

    /**
     * Checks whether this is a Windows virtual machine and automatic update is turned on.
     *
     * @return true if this is a Windows virtual machine and automatic update is turned on, false otherwise
     */
    boolean isWindowsAutoUpdateEnabled();

    /**
     * Gets the time zone of the Windows virtual machine.
     *
     * @return the time zone of the Windows virtual machine
     */
    String windowsTimeZone();

    /**
     * Checks whether the boot diagnostic is enabled.
     *
     * @return true if the boot diagnostic is enabled, false otherwise
     */
    boolean bootDiagnosticEnabled();

    /**
     * Gets the URI to the storage account storing boot diagnostics log.
     *
     * @return the URI to the storage account storing boot diagnostics log
     */
    String bootDiagnosticStorageAccountUri();

    /**
     * Gets the resource ID of the availability set that this virtual machine instance belongs to.
     *
     * @return the resource ID of the availability set that this virtual machine instance belongs to
     */
    String availabilitySetId();

    /**
     * Gets the list of resource ID of network interface associated with the virtual machine instance.
     *
     * @return the list of resource ID of network interface associated with the virtual machine instance
     */
    List<String> networkInterfaceIds();

    /**
     * Gets resource ID of primary network interface associated with virtual machine instance.
     *
     * @return resource ID of primary network interface associated with virtual machine instance
     */
    String primaryNetworkInterfaceId();

    /**
     * Gets the extensions associated with the virtual machine instance, indexed by name.
     *
     * @return the extensions associated with the virtual machine instance, indexed by name
     */
    Map<String, VirtualMachineScaleSetVMInstanceExtension> extensions();

    /**
     * Gets the storage profile of the virtual machine instance.
     *
     * @return the storage profile of the virtual machine instance
     */
    StorageProfile storageProfile();

    /**
     * Gets the operating system profile of an virtual machine instance.
     *
     * @return the operating system profile of an virtual machine instance
     */
    OSProfile osProfile();

    /**
     * Gets the diagnostics profile of the virtual machine instance.
     *
     * @return the diagnostics profile of the virtual machine instance
     */
    DiagnosticsProfile diagnosticsProfile();

    /**
     * Checks whether managed disk is used for the virtual machine's disks (os, data).
     *
     * @return true if managed disk is used for the virtual machine's disks (os, data)
     */
    boolean isManagedDiskEnabled();

    /** Shuts down the virtual machine instance, move them to new node, and powers them back on. */
    void redeploy();

    /**
     * Shuts down the virtual machine instance, move them to new node, and powers them back on.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> redeployAsync();

    /** Updates the version of the installed operating system in the virtual machine instance. */
    void reimage();

    /**
     * Updates the version of the installed operating system in the virtual machine instance.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> reimageAsync();

    /** Shuts down the virtual machine instance and releases the associated compute resources. */
    void deallocate();

    /**
     * Shuts down the virtual machine instance and releases the associated compute resources.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> deallocateAsync();

    /** Stops the virtual machine instance. */
    void powerOff();

    /**
     * Stops the virtual machine instance.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> powerOffAsync();

    /**
     * Stops the virtual machine instance.
     *
     * @param skipShutdown power off without graceful shutdown
     */
    void powerOff(boolean skipShutdown);

    /**
     * Stops the virtual machine instances.
     *
     * @param skipShutdown power off without graceful shutdown
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> powerOffAsync(boolean skipShutdown);

    /** Starts the virtual machine instance. */
    void start();

    /**
     * Starts the virtual machine instance.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> startAsync();

    /** Restarts the virtual machine instance. */
    void restart();

    /**
     * Restarts the virtual machine instance.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> restartAsync();

    /** Deletes the virtual machine instance. */
    void delete();

    /**
     * Deletes the virtual machine instance.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> deleteAsync();

    /**
     * Gets the instance view of the virtual machine instance.
     *
     * <p>To get the latest instance view use <code>refreshInstanceView()</code>.
     *
     * @return the instance view
     */
    VirtualMachineInstanceView instanceView();

    /**
     * Refreshes the instance view.
     *
     * @return the instance view
     */
    VirtualMachineInstanceView refreshInstanceView();

    /**
     * Refreshes the instance view.
     *
     * @return an observable that emits the instance view of the virtual machine instance.
     */
    Mono<VirtualMachineInstanceView> refreshInstanceViewAsync();

    /**
     * Gets the power state of the virtual machine instance.
     *
     * @return the power state of the virtual machine instance
     */
    PowerState powerState();

    /**
     * Gets a network interface associated with this virtual machine instance.
     *
     * @param name the name of the network interface
     * @return the network interface
     */
    VirtualMachineScaleSetNetworkInterface getNetworkInterface(String name);

    /**
     * Gets a network interface associated with this virtual machine instance.
     *
     * @param name the name of the network interface
     * @return the network interface
     */
    Mono<VirtualMachineScaleSetNetworkInterface> getNetworkInterfaceAsync(String name);

    /**
     * Gets the network interfaces associated with this virtual machine instance.
     *
     * @return the network interfaces associated with this virtual machine instance.
     */
    PagedIterable<VirtualMachineScaleSetNetworkInterface> listNetworkInterfaces();

    /**
     * Gets the network interfaces associated with this virtual machine instance.
     *
     * @return the network interfaces associated with this virtual machine instance.
     */
    PagedFlux<VirtualMachineScaleSetNetworkInterface> listNetworkInterfacesAsync();

    /**
     * Gets applied model from the virtual machine.
     *
     * @return Get specifies whether the model applied to the virtual machine is the model of the virtual machine scale
     *     set or the customized model for the virtual machine.
     */
    String modelDefinitionApplied();

    /**
     * Gets the specific protection policy for the vm.
     *
     * @return The specific protection policy for the vm.
     */
    VirtualMachineScaleSetVMProtectionPolicy protectionPolicy();

    /**
     * Gets the network profile config for the vm.
     *
     * @return The network profile config for the vm.
     */
    VirtualMachineScaleSetVMNetworkProfileConfiguration networkProfileConfiguration();

    /**
     * Gets the time at which the Virtual Machine resource was created.
     *
     * @return the time at which the Virtual Machine resource was created
     */
    OffsetDateTime timeCreated();

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update extends Appliable<VirtualMachineScaleSetVM> {
        /**
         * Attaches an existing data disk to this VMSS virtual machine.
         *
         * @param dataDisk data disk, need to be in DiskState.UNATTACHED state
         * @param lun the disk LUN, cannot conflict with existing LUNs
         * @param cachingTypes the caching type
         * @return the next stage of the update
         */
        Update withExistingDataDisk(Disk dataDisk, int lun, CachingTypes cachingTypes);

        /**
         * Attaches an existing data disk to this VMSS virtual machine.
         *
         * @param dataDisk data disk, need to be in DiskState.UNATTACHED state
         * @param lun the disk LUN, cannot conflict with existing LUNs
         * @param cachingTypes the caching type
         * @param storageAccountTypes the storage account type
         * @return the next stage of the update
         */
        Update withExistingDataDisk(Disk dataDisk, int lun, CachingTypes cachingTypes,
            StorageAccountTypes storageAccountTypes);

        /**
         * Detaches an existing data disk from this VMSS virtual machine.
         *
         * @param lun the disk LUN
         * @return the next stage of the update
         */
        Update withoutDataDisk(int lun);
    }
}
