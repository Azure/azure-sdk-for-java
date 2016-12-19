package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.compute.implementation.VirtualMachineScaleSetVMInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import rx.Observable;

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
        Wrapper<VirtualMachineScaleSetVMInner> {
    /**
     * @return the instance id assigned to this virtual machine instance
     */
    String instanceId();

    /**
     * @return the sku of the virtual machine instance, this will be sku used while creating the parent
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
     * @return true if the operating system of the virtual machine instance is based on platform image,
     * false if based on custom image
     */
    boolean isOsBasedOnPlatformImage();

    /**
     * @return reference to the platform image that the virtual machine instance operating system is based on,
     * null will be returned if the operating system is based on custom image
     */
    ImageReference platformImageReference();

    /**
     * @return the platform image that the virtual machine instance operating system is based on, null be
     * returned if the operating system is based on custom image
     */
    VirtualMachineImage getPlatformImage();

    /**
     * @return vhd uri of the custom image that the virtual machine instance operating system is based on,
     * null will be returned if the operating system is based on platform image
     */
    String customImageVhdUri();

    /**
     * @return the name of the operating system disk
     */
    String osDiskName();

    /**
     * @return vhd uri to the operating system disk
     */
    String osDiskVhdUri();

    /**
     * @return the caching type of the operating system disk
     */
    CachingTypes osDiskCachingType();

    /**
     * @return the size of the operating system disk
     */
    int osDiskSizeInGB();

    /**
     * @return the virtual machine instance computer name with prefix {@link VirtualMachineScaleSet#computerNamePrefix()}
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
     * @return true if this is a Windows virtual machine and Vm agent is provisioned, false otherwise
     */
    boolean isWindowsVmAgentProvisioned();

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
     * @return the uri to the storage account storing boot diagnostics log
     */
    String bootDiagnosticStorageAccountUri();

    /**
     * @return the resource id of the availability set that this virtual machine instance belongs to
     */
    String availabilitySetId();

    /**
     * @return the list of resource id of network interface associated with the virtual machine instance
     */
    List<String> networkInterfaceIds();

    /**
     * @return resource id of primary network interface associated with virtual machine instance
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
     * Updates the version of the installed operating system in the virtual machine instance.
     */
    void reimage();

    /**
     * Updates the version of the installed operating system in the virtual machine instance.
     *
     * @return the observable to the reimage action
     */
    @Method
    Observable<Void> reimageAsync();

    /**
     * Shuts down the virtual machine instance and releases the associated compute resources.
     */
    void deallocate();

    /**
     * Shuts down the virtual machine instance and releases the associated compute resources.
     *
     * @return the observable to the deallocate action
     */
    @Method
    Observable<Void> deallocateAsync();

    /**
     * Stops the virtual machine instance.
     */
    void powerOff();

    /**
     * Stops the virtual machine instance.
     *
     * @return the observable to the poweroff action
     */
    @Method
    Observable<Void> powerOffAsync();

    /**
     * Starts the virtual machine instance.
     */
    void start();

    /**
     * Starts the virtual machine instance.
     *
     * @return the observable to the start action
     */
    @Method
    Observable<Void> startAsync();

    /**
     * Restarts the virtual machine instance.
     */
    void restart();

    /**
     * Restarts the virtual machine instance.
     *
     * @return the observable to the restart action
     */
    @Method
    Observable<Void> restartAsync();

    /**
     * Deletes the virtual machine instance.
     */
    void delete();

    /**
     * Deletes the virtual machine instance.
     *
     * @return the observable to the delete action
     */
    @Method
    Observable<Void> deleteAsync();

    /**
     * Gets the instance view of the virtual machine instance.
     * <p>
     * To get the latest instance view use {@link VirtualMachineScaleSetVM#refreshInstanceView()}.
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
}
