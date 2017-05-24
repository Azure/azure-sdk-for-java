/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.compute.implementation.VirtualMachineScaleSetVMInner;
import com.microsoft.azure.management.network.VirtualMachineScaleSetNetworkInterface;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import rx.Completable;

import java.util.List;
import java.util.Map;

/**
 * An immutable client-side representation of a virtual machine instance in an Azure virtual machine scale set.
 */
@Fluent
public interface VirtualMachineScaleSetVM extends
        Resource,
        ChildResource<VirtualMachineScaleSet>,
        Refreshable<VirtualMachineScaleSetVM>,
        HasInner<VirtualMachineScaleSetVMInner> {
    /**
     * @return the instance ID assigned to this virtual machine instance
     */
    String instanceId();

    /**
     * @return the SKU of the virtual machine instance, this will be SKU used while creating the parent
     * virtual machine scale set
     */
    Sku sku();

    /**
     * @return virtual machine instance size
     */
    VirtualMachineSizeTypes size();

    /**
     * @return true if the latest scale set model changes are applied to the virtual machine instance
     */
    boolean isLatestScaleSetUpdateApplied();

    /**
     * @return true if the operating system of the virtual machine instance is based on platform image
     */
    boolean isOSBasedOnPlatformImage();

    /**
     * @return true if the operating system of the virtual machine instance is based on custom image
     */
    boolean isOSBasedOnCustomImage();

    /**
     * @return true if the operating system of the virtual machine instance is based on stored image
     */
    boolean isOSBasedOnStoredImage();

    /**
     * @return reference to the platform image that the virtual machine instance operating system is based on,
     * null will be returned if the operating system is based on custom image
     */
    ImageReference platformImageReference();

    /**
     * @return the platform image that the virtual machine instance operating system is based on, null be
     * returned otherwise
     */
    VirtualMachineImage getOSPlatformImage();

    /**
     * @return the custom image that the virtual machine instance operating system is based on, null be
     * returned otherwise
     */
    VirtualMachineCustomImage getOSCustomImage();

    /**
     * @return VHD URI of the custom image that the virtual machine instance operating system is based on,
     * null will be returned if the operating system is based on platform image
     */
    String storedImageUnmanagedVhdUri();

    /**
     * @return the name of the operating system disk
     */
    String osDiskName();

    /**
     * @return VHD URI to the operating system disk
     */
    String osUnmanagedDiskVhdUri();

    /**
     * @return resource ID of the managed disk backing OS disk
     */
    String osDiskId();

    /**
     * @return the unmanaged data disks associated with this virtual machine instance, indexed by LUN
     */
    Map<Integer, VirtualMachineUnmanagedDataDisk> unmanagedDataDisks();

    /**
     * @return the managed data disks associated with this virtual machine instance, indexed by LUN
     */
    Map<Integer, VirtualMachineDataDisk> dataDisks();

    /**
     * @return the caching type of the operating system disk
     */
    CachingTypes osDiskCachingType();

    /**
     * @return the size of the operating system disk
     */
    int osDiskSizeInGB();

    /**
     * @return the virtual machine instance computer name with the VM scale set prefix.
     */
    String computerName();

    /**
     * @return the name of the admin user
     */
    String administratorUserName();

    /**
     * @return the operating system type
     */
    OperatingSystemTypes osType();

    /**
     * @return true if this is a Linux virtual machine and password based login is enabled, false otherwise
     */
    boolean isLinuxPasswordAuthenticationEnabled();

    /**
     * @return true if this is a Windows virtual machine and VM agent is provisioned, false otherwise
     */
    boolean isWindowsVMAgentProvisioned();

    /**
     * @return true if this is a Windows virtual machine and automatic update is turned on, false otherwise
     */
    boolean isWindowsAutoUpdateEnabled();

    /**
     * @return the time zone of the Windows virtual machine
     */
    String windowsTimeZone();

    /**
     * @return true if the boot diagnostic is enabled, false otherwise
     */
    boolean bootDiagnosticEnabled();

    /**
     * @return the URI to the storage account storing boot diagnostics log
     */
    String bootDiagnosticStorageAccountUri();

    /**
     * @return the resource ID of the availability set that this virtual machine instance belongs to
     */
    String availabilitySetId();

    /**
     * @return the list of resource ID of network interface associated with the virtual machine instance
     */
    List<String> networkInterfaceIds();

    /**
     * @return resource ID of primary network interface associated with virtual machine instance
     */
    String primaryNetworkInterfaceId();

    /**
     * @return the extensions associated with the virtual machine instance, indexed by name
     */
    Map<String, VirtualMachineScaleSetVMInstanceExtension> extensions();

    /**
     * @return the storage profile of the virtual machine instance
     */
    StorageProfile storageProfile();

    /**
     * @return the operating system profile of an virtual machine instance
     */
    OSProfile osProfile();

    /**
     * @return the diagnostics profile of the virtual machine instance
     */
    DiagnosticsProfile diagnosticsProfile();

    /**
     * @return true if managed disk is used for the virtual machine's disks (os, data)
     */
    boolean isManagedDiskEnabled();

    /**
     * Updates the version of the installed operating system in the virtual machine instance.
     */
    void reimage();

    /**
     * Updates the version of the installed operating system in the virtual machine instance.
     *
     * @return a representation of the deferred computation of this call
     */
    @Method
    Completable reimageAsync();

    /**
     * Shuts down the virtual machine instance and releases the associated compute resources.
     */
    void deallocate();

    /**
     * Shuts down the virtual machine instance and releases the associated compute resources.
     *
     * @return a representation of the deferred computation of this call
     */
    @Method
    Completable deallocateAsync();

    /**
     * Stops the virtual machine instance.
     */
    void powerOff();

    /**
     * Stops the virtual machine instance.
     *
     * @return a representation of the deferred computation of this call
     */
    @Method
    Completable powerOffAsync();

    /**
     * Starts the virtual machine instance.
     */
    void start();

    /**
     * Starts the virtual machine instance.
     *
     * @return a representation of the deferred computation of this call
     */
    @Method
    Completable startAsync();

    /**
     * Restarts the virtual machine instance.
     */
    void restart();

    /**
     * Restarts the virtual machine instance.
     *
     * @return a representation of the deferred computation of this call
     */
    @Method
    Completable restartAsync();

    /**
     * Deletes the virtual machine instance.
     */
    void delete();

    /**
     * Deletes the virtual machine instance.
     *
     * @return a representation of the deferred computation of this call
     */
    @Method
    Completable deleteAsync();

    /**
     * Gets the instance view of the virtual machine instance.
     * <p>
     * To get the latest instance view use <code>refreshInstanceView()</code>.
     *
     * @return the instance view
     */
    VirtualMachineInstanceView instanceView();

    /**
     * Refreshes the instance view.
     *
     * @return the instance view
     */
    @Method
    VirtualMachineInstanceView refreshInstanceView();

    /**
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
     * @return the network interfaces associated with this virtual machine instance.
     */
    PagedList<VirtualMachineScaleSetNetworkInterface> listNetworkInterfaces();
}
