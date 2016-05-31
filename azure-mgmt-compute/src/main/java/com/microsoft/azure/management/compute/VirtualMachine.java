package com.microsoft.azure.management.compute;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.compute.implementation.KnownVirtualMachineImage;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineInner;
import com.microsoft.azure.management.compute.implementation.api.Plan;
import com.microsoft.azure.management.compute.implementation.api.HardwareProfile;
import com.microsoft.azure.management.compute.implementation.api.StorageProfile;
import com.microsoft.azure.management.compute.implementation.api.OSProfile;
import com.microsoft.azure.management.compute.implementation.api.NetworkProfile;
import com.microsoft.azure.management.compute.implementation.api.DiagnosticsProfile;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineInstanceView;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineExtensionInner;
import com.microsoft.azure.management.compute.implementation.api.OperatingSystemTypes;
import com.microsoft.azure.management.compute.implementation.api.ImageReference;
import com.microsoft.azure.management.compute.implementation.api.WinRMListener;
import com.microsoft.azure.management.compute.implementation.api.CachingTypes;
import com.microsoft.azure.management.compute.implementation.api.DiskEncryptionSettings;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineSizeTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.storage.StorageAccount;

import java.util.List;

/**
 * An immutable client-side representation of an Azure virtual machine.
 */
public interface VirtualMachine extends
        GroupableResource,
        Refreshable<VirtualMachine>,
        Wrapper<VirtualMachineInner> {
    /**
     * @return the plan value
     */
    Plan plan();

    /**
     * Returns the hardware profile of an Azure virtual machine.
     * <p>
     * Currently the profile contains only virtual machine size information.
     *
     * @return the hardwareProfile value
     */
    HardwareProfile hardwareProfile();

    /**
     * Returns the storage profile of an Azure virtual machine.
     * <p>
     * The storage profile contains information such as the details of the VM image or user image
     * from which this virtual machine is created, the Azure storage account where the operating system
     * disk is stored, details of the data disk attached to the virtual machine.
     *
     * @return the storageProfile value
     */
    StorageProfile storageProfile();

    /**
     * Returns the operating system profile of an Azure virtual machine.
     *
     * @return the osProfile value
     */
    OSProfile osProfile();

    /**
     * Returns the network profile of an Azure virtual machine.
     * <p>
     * The network profile describes the network interfaces associated with the virtual machine.
     *
     * @return the networkProfile value
     */
    NetworkProfile networkProfile();

    /**
     * Returns the diagnostics profile of an Azure virtual machine.
     * <p>
     * Enabling diagnostic features in a virtual machine enable you to easily diagnose and recover
     * virtual machine from boot failures.
     *
     * @return the diagnosticsProfile value
     */
    DiagnosticsProfile diagnosticsProfile();

    /**
     * Returns reference to the availability set an Azure virtual machine associated with.
     * <p>
     * Having a set of virtual machines in an availability set ensures that during maintenance
     * event at least one virtual machine will be available.
     *
     * @return the availabilitySet reference
     */
    SubResource availabilitySet();

    /**
     * @return the provisioningState value
     */
    String provisioningState();

    /**
     * @return the instanceView value
     */
    VirtualMachineInstanceView instanceView();

    /**
     * @return the licenseType value
     */
    String licenseType();

    /**
     * @return the resources value
     */
    List<VirtualMachineExtensionInner> resources();

    /**
     * The first stage of a virtual machine definition.
     */
    interface DefinitionBlank extends GroupableResource.DefinitionWithRegion<DefinitionWithGroup> {
    }

    /**
     * The stage of the virtual machine definition allowing to specify the resource group.
     */
    interface DefinitionWithGroup extends GroupableResource.DefinitionWithGroup<DefinitionWithPrimaryNetworkInterface> {
    }

    /**
     * The stage of the virtual machine definition allowing to specify the primary network interface.
     */
    interface DefinitionWithPrimaryNetworkInterface {
        /**
         * Specifies the name of the new primary network interface for the virtual machine.
         *
         * @param name the name for the new network interface
         * @return The next stage of the virtual machine definition
         */
        DefinitionWithOS withNewPrimaryNetworkInterface(String name);
        /**
        DefinitionWithOS withNewPrimaryNetworkInterface(NetworkInterface.creatable creatable);
        DefinitionWithOS withExistingPrimaryNetworkInterface(String name);
        DefinitionWithOS withExistingPrimaryNetworkInterface(NetworkInterface networkInterface);
        **/
    }

    /**
     * The stage of the virtual machine definition allowing to specify the Operation System.
     */
    interface DefinitionWithOS {
        /**
         * Specifies the market-place image used for the virtual machine's OS.
         *
         * @return the next stage of the virtual machine definition
         */
        DefinitionWithMarketplaceImage withMarketplaceImage();

        /**
         * Specifies the user (generalized) image used for the virtual machine's OS.
         *
         * @param imageUrl the url the the VHD
         * @return the next stage of the virtual machine definition
         */
        DefinitionWithOSType withStoredImage(String imageUrl);

        /**
         * Specifies the specialized operating system disk to be attached to the virtual machine.
         *
         * @param osDiskUrl the url to the OS disk in the Azure Storage account
         * @return the next stage of the Windows virtual machine definition
         */
        DefinitionCreatable withOSDisk(String osDiskUrl, OperatingSystemTypes osType);
    }

    /**
     * The stage of the virtual machine definition allowing to specify marketplace (platform) image.
     */
    interface DefinitionWithMarketplaceImage {
        /**
         * Specifies the version of image.
         *
         * @param imageReference describes publisher, offer, sku and version of the market-place image
         * @return the next stage of the virtual machine definition
         */
        DefinitionWithOSType version(ImageReference imageReference);

        /**
         * Specifies that the latest version of the image needs to be used.
         *
         * @param publisher specifies the publisher of the image
         * @param offer specifies the offer of the image
         * @param sku specifies the SKU of the image
         * @return the next stage of the virtual machine definition
         */
        DefinitionWithOSType latest(String publisher, String offer, String sku);

        /**
         * Specifies the known image to be used.
         *
         * @param knownImage enum value indicating known market-place image
         * @return the next stage of the virtual machine definition
         */
        DefinitionWithOSType popular(KnownVirtualMachineImage knownImage);
    }

    /**
     * The stage of the virtual machine definition allowing to specify Operating system type.
     */
    interface DefinitionWithOSType {
        /**
         * Specifies the OS type of the virtual machine as Linux.
         *
         * @return the next stage of the Linux virtual machine definition
         */
        DefinitionWithRootUserName withLinuxOS();

        /**
         * Specifies the OS type as Windows.
         *
         * @return the next stage of the Windows virtual machine definition
         */
        DefinitionWithAdminUserName withWindowsOS();
    }

    /**
     * The stage of the Linux virtual machine definition allowing to specify root user name.
     */
    interface DefinitionWithRootUserName {
        /**
         * Specifies the root user name for the Linux virtual machine.
         *
         * @param rootUserName the Linux root user name. This must follow the required naming convention for Linux user name
         * @return the next stage of the Linux virtual machine definition
         */
        DefinitionLinuxCreatable withRootUserName(String rootUserName);
    }

    /**
     * The stage of the Windows virtual machine definition allowing to specify administrator user name.
     */
    interface DefinitionWithAdminUserName {
        /**
         * Specifies the administrator user name for the Windows virtual machine.
         *
         * @param adminUserName the Windows administrator user name. This must follow the required naming convention for Windows user name.
         * @return the stage representing creatable Linux VM definition
         */
        DefinitionWindowsCreatable withAdminUserName(String adminUserName);
    }

    /**
     * The stage of the Linux virtual machine definition which contains all the minimum required inputs for
     * the resource to be created (via {@link DefinitionCreatable#create()}), but also allows
     * for any other optional settings to be specified.
     */
    interface DefinitionLinuxCreatable extends DefinitionCreatable {
        /**
         * Specifies the SSH public key.
         * <p/>
         * each call to this method adds the given public key to the list of VM's public keys.
         *
         * @param publicKey the SSH public key in PEM format.
         * @return the stage representing creatable Linux VM definition
         */
        DefinitionLinuxCreatable withSsh(String publicKey);
    }

    /**
     * The stage of the Windows virtual machine definition which contains all the minimum required inputs for
     * the resource to be created (via {@link DefinitionCreatable#create()}), but also allows
     * for any other optional settings to be specified.
     */
    interface DefinitionWindowsCreatable extends DefinitionCreatable {
        /**
         * Specifies that VM Agent should not be provisioned.
         *
         * @return the stage representing creatable Windows VM definition
         */
        DefinitionWindowsCreatable disableVMAgent();

        /**
         * Specifies that automatic updates should be disabled.
         *
         * @return the stage representing creatable Windows VM definition
         */
        DefinitionWindowsCreatable disableAutoUpdate();

        /**
         * Specifies the time-zone.
         *
         * @return the stage representing creatable Windows VM definition
         */
        DefinitionWindowsCreatable withTimeZone(String timeZone);

        /**
         * Specifies the WINRM listener.
         * <p/>
         * Each call to this method adds the given listener to the list of VM's WinRM listeners.
         *
         * @return the stage representing creatable Windows VM definition
         */
        DefinitionWindowsCreatable withWinRM(WinRMListener listener);
    }

    /**
     * The stage of the virtual machine definition allowing to specify password.
     *
     * @param <T> the virtual machine definition in creatable stage.
     */
    interface DefinitionPassword<T extends DefinitionCreatable> {
        /**
         * Specifies the password for the virtual machine.
         *
         * @param password the password. This must follow the criteria for Azure VM password.
         * @return the stage representing creatable VM definition
         */
        T withPassword(String password);
    }

    /**
     * The stage of the virtual machine definition allowing to specify OS disk configurations.
     *
     * @param <T> the virtual machine definition in creatable stage.
     */
    interface DefinitionOSDiskSettings<T extends DefinitionCreatable> {
        /**
         * Specifies the caching type for the Operating System disk.
         *
         * @param cachingType the caching type.
         * @return the stage representing creatable VM definition
         */
        T withOSDiskCaching(CachingTypes cachingType);

        /**
         * Specifies the name of the OS Disk Vhd file and it's parent container.
         *
         * @param containerName the name of the container in the selected storage account.
         * @param vhdName the name for the OS Disk vhd.
         * @return the stage representing creatable VM definition
         */
        T withOSDiskVhdLocation(String containerName, String vhdName);

        /**
         * Specifies the encryption settings for the OS Disk.
         *
         * @param settings the encryption settings.
         * @return the stage representing creatable VM definition
         */
        T withOSDiskEncryptionSettings(DiskEncryptionSettings settings);

        /**
         * Specifies the size of the OSDisk in GB.
         *
         * @param size the VHD size.
         * @return the stage representing creatable VM definition
         */
        T withOSDiskSizeInGB(Integer size);

        /**
         * Specifies the name for the OS Disk.
         *
         * @param name the OS Disk name.
         * @return the stage representing creatable VM definition
         */
        T withOSDiskName(String name);
    }

    /**
     * The stage of the virtual machine definition allowing to specify VM size.
     *
     * @param <T> the virtual machine definition in creatable stage.
     */
    interface DefinitionWithVMSize<T extends DefinitionCreatable> {
        /**
         * Specifies the virtual machine size.
         *
         * @param sizeName the name of the size for the virtual machine as text
         * @return the stage representing creatable VM definition
         */
        T withSize(String sizeName);

        /**
         * Specifies the virtual machine size.
         *
         * @param size a size from the list of available sizes for the virtual machine
         * @return the stage representing creatable VM definition
         */
        T withSize(VirtualMachineSizeTypes size);
    }

    /**
     * The stage of the virtual machine definition allowing to specify data disk configurations.
     *
     * @param <T> the virtual machine definition in creatable stage.
     */
    interface ConfigureDataDisk<T extends DefinitionCreatable> {
        /**
         * Specifies the logical unit number for the data disk.
         *
         * @param lun the logical unit number
         * @return the stage representing optional additional configurations for the attachable data disk
         */
        ConfigureDataDisk<T> withLun(Integer lun);

        /**
         * Specifies the caching type for the data disk.
         *
         * @param cachingType the disk caching type. Possible values include: 'None', 'ReadOnly', 'ReadWrite'
         * @return the stage representing optional additional configurations for the attachable data disk
         */
        ConfigureDataDisk<T> withCaching(CachingTypes cachingType);

        /**
         * Adds the data disk to the list of virtual machine's data disks.
         *
         * @return the stage representing creatable VM definition
         */
        T attach();
    }

    /**
     * The stage of the virtual machine definition allowing to specify data disk target location.
     *
     * @param <T> the virtual machine definition in creatable stage.
     */
    interface ConfigureNewDataDiskWithStoreAt<T extends DefinitionCreatable> extends ConfigureDataDisk<T> {
        /**
         * Specifies where the VHD associated with the new blank data disk needs to be stored.
         *
         * @param storageAccountName the storage account name
         * @param containerName the name of the container to hold the new VHD file
         * @param vhdName the name for the new VHD file
         * @return the stage representing optional additional configurations for the attachable data disk
         */
        ConfigureDataDisk<T> storeAt(String storageAccountName, String containerName, String vhdName);
    }

    /**
     * The stage of the virtual machine definition allowing to specify new data disk configuration.
     *
     * @param <T> the virtual machine definition in creatable stage.
     */
    interface ConfigureNewDataDisk<T extends DefinitionCreatable> {
        /**
         * Specifies the initial disk size in GB for new blank data disk.
         *
         * @param size the disk size in GB
         * @return the stage representing optional additional configurations for the attachable data disk
         */
        ConfigureNewDataDiskWithStoreAt<T> withSizeInGB(Integer size);
    }

    /**
     * The stage of the virtual machine definition allowing to specify existing data disk configuration.
     *
     * @param <T> the virtual machine definition in creatable stage.
     */
    interface ConfigureExistingDataDisk<T extends DefinitionCreatable> {
        /**
         * Specifies an existing VHD that needs to be attached to the virtual machine as data disk.
         *
         * @param storageAccountName the storage account name
         * @param containerName the name of the container holding the VHD file
         * @param vhdName the name for the VHD file
         * @return the stage representing optional additional configurations for the attachable data disk
         */
        ConfigureDataDisk<T> from(String storageAccountName, String containerName, String vhdName);
    }

    /**
     * The stage of the virtual machine definition allowing to specify data disk configuration.
     *
     * @param <T> the virtual machine definition in creatable stage.
     */
    interface DefinitionWithDataDisk<T extends DefinitionCreatable> {
        /**
         * Specifies that a new blank data disk needs to be attached to virtual machine.
         *
         * @param sizeInGB the disk size in GB
         * @return the stage representing creatable VM definition
         */
        T withNewDataDisk(Integer sizeInGB);

        /**
         * Specifies an existing VHD that needs to be attached to the virtual machine as data disk.
         *
         * @param storageAccountName the storage account name
         * @param containerName the name of the container holding the VHD file
         * @param vhdName the name for the VHD file
         * @return the stage representing creatable VM definition
         */
        T withExistingDataDisk(String storageAccountName, String containerName, String vhdName);

        /**
         * Specifies a new blank data disk to be attached to the virtual machine along with it's configuration.
         *
         * @param name the name for the data disk
         * @return the stage representing configuration for the data disk
         */
        ConfigureNewDataDisk<T> defineNewDataDisk(String name);

        /**
         * Specifies an existing VHD that needs to be attached to the virtual machine as data disk along with
         * it's configuration.
         *
         * @param name the name for the data disk
         * @return the stage representing configuration for the data disk
         */
        ConfigureExistingDataDisk<T> defineExistingDataDisk(String name);
    }

    /**
     * The stage of the virtual machine definition allowing to specify availability set.
     *
     * @param <T> the virtual machine definition in creatable stage.
     */
    interface DefinitionWithAvailabilitySet<T extends DefinitionCreatable> {
        /**
         * Specifies the name of a new availability set to associate the virtual machine with.
         * <p>
         * Adding virtual machines running your application to an availability set ensures that during
         * maintenance event at least one virtual machine will be available.
         *
         * @param name the name of the availability set
         * @return the stage representing creatable VM definition
         */
        T withNewAvailabilitySet(String name);

        /**
         * Specifies definition of a not-yet-created {@link AvailabilitySet.DefinitionCreatable} availability set
         * to associate the virtual machine with.
         * <p>
         * Adding virtual machines running your application to an availability set ensures that during
         * maintenance event at least one virtual machine will be available.
         *
         * @param creatable the availability set in creatable stage
         * @return the stage representing creatable VM definition
         */
        T withNewAvailabilitySet(AvailabilitySet.DefinitionCreatable creatable);

        /**
         * Specifies the name of an existing availability set to to associate the virtual machine with.
         * <p>
         * Adding virtual machines running your application to an availability set ensures that during
         * maintenance event at least one virtual machine will be available.
         *
         * @param name the name of an existing availability set
         * @return the stage representing creatable VM definition
         */
        T withExistingAvailabilitySet(String name);
    }

    /**
     * The stage of the virtual machine definition allowing to specify storage account.
     *
     * @param <T> the virtual machine definition in creatable stage.
     */
    interface DefinitionStorageAccount<T extends DefinitionCreatable> {
        /**
         * Specifies the name of a new storage account to put the VM's OS disk VHD in.
         * <p>
         * Only the OS disk based on marketplace image will be stored in the new storage account,
         * an OS disk based on user image will be stored in the same storage account as user image.
         *
         * @param name the name of the storage account
         * @return the stage representing creatable VM definition
         */
        T withNewStorageAccount(String name);

        /**
         * Specifies definition of a not-yet-created {@link StorageAccount.DefinitionCreatable} storage account
         * to put the VM's OS disk VHD in.
         * <p>
         * Only the OS disk based on marketplace image will be stored in the new storage account,
         * an OS disk based on user image will be stored in the same storage account as user image.
         *
         * @param creatable the storage account in creatable stage
         * @return the stage representing creatable VM definition
         */
        T withNewStorageAccount(StorageAccount.DefinitionCreatable creatable);

        /**
         * Specifies the name of an existing storage account to put the VM's OS disk in.
         * <p>
         * An OS disk based on marketplace or user image (generalized image) will be stored in this
         * storage account.
         *
         * @param name the name of an existing storage account
         * @return the stage representing creatable VM definition
         */
        T withExistingStorageAccount(String name);
    }

    /**
    interface DefinitionWithNetworkInterface<T extends DefinitionCreatable> {
        T withNewNetworkInterface(String name);
        T withNewNetworkInterface(NetworkInterface.DefinitionCreatable creatable);
        T withExistingNetworkInterface(String name);
        T withExistingNetworkInterface(NetworkInterface networkInterface);
    }
    **/

    /**
     * The stage of the virtual machine definition which contains all the minimum required inputs for
     * the resource to be created (via {@link DefinitionCreatable#create()}), but also allows
     * for any other optional settings to be specified.
     */
    interface DefinitionCreatable extends
            DefinitionPassword<DefinitionCreatable>,
            DefinitionOSDiskSettings<DefinitionCreatable>,
            DefinitionWithVMSize<DefinitionCreatable>,
            DefinitionStorageAccount<DefinitionCreatable>,
            DefinitionWithDataDisk<DefinitionCreatable>,
            DefinitionWithAvailabilitySet<DefinitionCreatable>,
            Creatable<VirtualMachine> {
    }
}
