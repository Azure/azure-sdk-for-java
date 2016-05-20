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
 * The type representing Azure virtual machine.
 */
public interface VirtualMachine extends
        GroupableResource,
        Refreshable<VirtualMachine>,
        Wrapper<VirtualMachineInner> {
    /**
     * Get the plan value.
     *
     * @return the plan value
     */
    Plan plan();

    /**
     * Get the hardwareProfile value.
     *
     * @return the hardwareProfile value
     */
    HardwareProfile hardwareProfile();

    /**
     * Get the storageProfile value.
     *
     * @return the storageProfile value
     */
    StorageProfile storageProfile();

    /**
     * Get the osProfile value.
     *
     * @return the osProfile value
     */
    OSProfile osProfile();

    /**
     * Get the networkProfile value.
     *
     * @return the networkProfile value
     */
    NetworkProfile networkProfile();

    /**
     * Get the diagnosticsProfile value.
     *
     * @return the diagnosticsProfile value
     */
    DiagnosticsProfile diagnosticsProfile();

    /**
     * Get the availabilitySet value.
     *
     * @return the availabilitySet value
     */
    SubResource availabilitySet();

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    String provisioningState();

    /**
     * Get the instanceView value.
     *
     * @return the instanceView value
     */
    VirtualMachineInstanceView instanceView();

    /**
     * Get the licenseType value.
     *
     * @return the licenseType value
     */
    String licenseType();

    /**
     * Get the resources value.
     *
     * @return the resources value
     */
    List<VirtualMachineExtensionInner> resources();

    /**
     * The initial stage representing virtual machine definition.
     */
    interface DefinitionBlank extends GroupableResource.DefinitionWithRegion<DefinitionWithGroup> {
    }

    /**
     * The virtual machine definition stage with resource group.
     */
    interface DefinitionWithGroup extends GroupableResource.DefinitionWithGroup<DefinitionWithOS> {
    }

    /**
     * The virtual machine definition stage with Operation System.
     */
    interface DefinitionWithOS {
        /**
         * Specifies the market-place image used for the virtual machine's OS.
         *
         * @return The next stage of the virtual machine definition
         */
        DefinitionWithMarketplaceImage withMarketplaceImage();

        /**
         * Specifies the user (generalized) image used for the virtual machine's OS.
         *
         * @param imageUrl The url the the VHD
         * @return The next stage of the virtual machine definition
         */
        DefinitionWithOSType withStoredImage(String imageUrl);

        /**
         * Specifies the specialized operating system disk to be attached to the virtual machine.
         *
         * @param osDiskUrl The url to the OS disk in the Azure Storage account
         * @return The next stage of the Windows virtual machine definition
         */
        DefinitionCreatable withOSDisk(String osDiskUrl, OperatingSystemTypes osType);
    }

    /**
     * The virtual machine definition stage with marketplace (platform) image.
     */
    interface DefinitionWithMarketplaceImage {
        /**
         * Specifies the version of image.
         *
         * @param imageReference describes publisher, offer, sku and version of the market-place image
         * @return The next stage of the virtual machine definition
         */
        DefinitionWithOSType version(ImageReference imageReference);

        /**
         * Specifies that the latest version of the image needs to be used.
         *
         * @param publisher Specifies the publisher of the image
         * @param offer Specifies the offer of the image
         * @param sku Specifies the SKU of the image
         * @return The next stage of the virtual machine definition
         */
        DefinitionWithOSType latest(String publisher, String offer, String sku);

        /**
         * Specifies the known image to be used.
         *
         * @param knownImage Enum value indicating known market-place image
         * @return The next stage of the virtual machine definition
         */
        DefinitionWithOSType popular(KnownVirtualMachineImage knownImage);
    }

    /**
     * The virtual machine definition stage with Operating system type.
     */
    interface DefinitionWithOSType {
        /**
         * Specifies the OS type of the virtual machine as Linux.
         *
         * @return The next stage of the Linux virtual machine definition
         */
        DefinitionWithRootUserName withLinuxOS();

        /**
         * Specifies the OS type as Windows.
         *
         * @return The next stage of the Windows virtual machine definition
         */
        DefinitionWithAdminUserName withWindowsOS();
    }

    /**
     * The Linux virtual machine definition stage with root user name.
     */
    interface DefinitionWithRootUserName {
        /**
         * Specifies the root user name for the Linux virtual machine.
         *
         * @param rootUserName The Linux root user name. This must follow the required naming convention for Linux user name
         * @return The next stage of the Linux virtual machine definition
         */
        DefinitionLinuxCreatable withRootUserName(String rootUserName);
    }

    /**
     * The Windows virtual machine definition stage with root user name.
     */
    interface DefinitionWithAdminUserName {
        /**
         * Specifies the administrator user name for the Windows virtual machine.
         *
         * @param adminUserName The Windows administrator user name. This must follow the required naming convention for Windows user name.
         * @return The stage representing creatable Linux VM definition
         */
        DefinitionWindowsCreatable withAdminUserName(String adminUserName);
    }

    /**
     * The Linux virtual machine in creatable stage.
     */
    interface DefinitionLinuxCreatable extends DefinitionCreatable {
        /**
         * Specifies the SSH public key.
         * <p/>
         * each call to this method adds the given public key to the list of VM's public keys.
         *
         * @param publicKey The SSH public key in PEM format.
         * @return The stage representing creatable Linux VM definition
         */
        DefinitionLinuxCreatable withSsh(String publicKey);
    }

    /**
     * The Windows virtual machine in cretable stage.
     */
    interface DefinitionWindowsCreatable extends DefinitionCreatable {
        /**
         * Specifies that VM Agent should not be provisioned.
         *
         * @return The stage representing creatable Windows VM definition
         */
        DefinitionWindowsCreatable disableVMAgent();

        /**
         * Specifies that automatic updates should be disabled.
         *
         * @return The stage representing creatable Windows VM definition
         */
        DefinitionWindowsCreatable disableAutoUpdate();

        /**
         * Specifies the time-zone.
         *
         * @return The stage representing creatable Windows VM definition
         */
        DefinitionWindowsCreatable withTimeZone(String timeZone);

        /**
         * Specifies the WINRM listener.
         * <p/>
         * each call to this method adds the given listener to the list of VM's WinRM listeners.
         *
         * @return The stage representing creatable Windows VM definition
         */
        DefinitionWindowsCreatable withWinRM(WinRMListener listener);
    }

    /**
     * The virtual machine definition stage with password.
     *
     * @param <T> The virtual machine definition in creatable stage.
     */
    interface DefinitionPassword<T extends DefinitionCreatable> {
        /**
         * Specifies the password for the virtual machine.
         *
         * @param password The password. This must follow the criteria for Azure VM password.
         * @return The stage representing creatable VM definition
         */
        T withPassword(String password);
    }

    /**
     * The virtual machine definition stage with OS disk configurations.
     *
     * @param <T> The virtual machine definition in creatable stage.
     */
    interface DefinitionOSDiskSettings<T extends DefinitionCreatable> {
        /**
         * Specifies the caching type for the Operating System disk.
         *
         * @param cachingType The caching type.
         * @return The stage representing creatable VM definition
         */
        T withOSDiskCaching(CachingTypes cachingType);

        /**
         * Specifies the name of the OS Disk Vhd file and it's parent container.
         *
         * @param containerName The name of the container in the selected storage account.
         * @param vhdName The name for the OS Disk vhd.
         * @return The stage representing creatable VM definition
         */
        T withOSDiskVhdLocation(String containerName, String vhdName);

        /**
         * Specifies the encryption settings for the OS Disk.
         *
         * @param settings The encryption settings.
         * @return The stage representing creatable VM definition
         */
        T withOSDiskEncryptionSettings(DiskEncryptionSettings settings);

        /**
         * Specifies the size of the OSDisk in GB.
         *
         * @param size The VHD size.
         * @return The stage representing creatable VM definition
         */
        T withOSDiskSizeInGB(Integer size);

        /**
         * Specifies the name for the OS Disk.
         *
         * @param name The OS Disk name.
         * @return The stage representing creatable VM definition
         */
        T withOSDiskName(String name);
    }

    /**
     * The virtual machine definition stage with size.
     *
     * @param <T> The virtual machine definition in creatable stage.
     */
    interface DefinitionWithVMSize<T extends DefinitionCreatable> {
        /**
         * Specifies the virtual machine size.
         *
         * @param sizeName The name of the size for the virtual machine as text
         * @return The stage representing creatable VM definition
         */
        T withSize(String sizeName);

        /**
         * Specifies the virtual machine size.
         *
         * @param size A size from the list of available sizes for the virtual machine
         * @return The stage representing creatable VM definition
         */
        T withSize(VirtualMachineSizeTypes size);
    }

    /**
     * The virtual machine definition stage with data disk configurations.
     *
     * @param <T> The virtual machine definition in creatable stage.
     */
    interface ConfigureDataDisk<T extends DefinitionCreatable> {
        /**
         * Specifies the logical unit number for the data disk.
         *
         * @param lun The logical unit number
         * @return The stage representing optional additional configurations for the attachable data disk
         */
        ConfigureDataDisk<T> withLun(Integer lun);

        /**
         * Specifies the caching type for the data disk.
         *
         * @param cachingType The disk caching type. Possible values include: 'None', 'ReadOnly', 'ReadWrite'
         * @return The stage representing optional additional configurations for the attachable data disk
         */
        ConfigureDataDisk<T> withCaching(CachingTypes cachingType);

        /**
         * Adds the data disk to the list of virtual machine's data disks.
         *
         * @return The stage representing creatable VM definition
         */
        T attach();
    }

    /**
     * The virtual machine definition stage with data disk target location.
     *
     * @param <T> The virtual machine definition in creatable stage.
     */
    interface ConfigureNewDataDiskWithStoreAt<T extends DefinitionCreatable> extends ConfigureDataDisk<T> {
        /**
         * Specifies where the VHD associated with the new blank data disk needs to be stored.
         *
         * @param storageAccountName The storage account name
         * @param containerName The name of the container to hold the new VHD file
         * @param vhdName The name for the new VHD file
         * @return The stage representing optional additional configurations for the attachable data disk
         */
        ConfigureDataDisk<T> storeAt(String storageAccountName, String containerName, String vhdName);
    }

    /**
     * The virtual machine definition stage with new data disk configuration.
     *
     * @param <T> The virtual machine definition in creatable stage.
     */
    interface ConfigureNewDataDisk<T extends DefinitionCreatable> {
        /**
         * Specifies the initial disk size in GB for new blank data disk.
         *
         * @param size The disk size in GB
         * @return The stage representing optional additional configurations for the attachable data disk
         */
        ConfigureNewDataDiskWithStoreAt<T> withSizeInGB(Integer size);
    }

    /**
     * The virtual machine definition stage with existing data disk configuration.
     *
     * @param <T> The virtual machine definition in creatable stage.
     */
    interface ConfigureExistingDataDisk<T extends DefinitionCreatable> {
        /**
         * Specifies an existing VHD that needs to be attached to the virtual machine as data disk.
         *
         * @param storageAccountName The storage account name
         * @param containerName The name of the container holding the VHD file
         * @param vhdName The name for the VHD file
         * @return The stage representing optional additional configurations for the attachable data disk
         */
        ConfigureDataDisk<T> from(String storageAccountName, String containerName, String vhdName);
    }

    /**
     * The virtual machine definition stage with data disk configuration.
     *
     * @param <T> The virtual machine definition in creatable stage.
     */
    interface DefinitionWithDataDisk<T extends DefinitionCreatable> {
        /**
         * Specifies that a new blank data disk needs to be attached to virtual machine.
         *
         * @param sizeInGB The disk size in GB
         * @return The stage representing creatable VM definition
         */
        T withNewDataDisk(Integer sizeInGB);

        /**
         * Specifies an existing VHD that needs to be attached to the virtual machine as data disk.
         *
         * @param storageAccountName The storage account name
         * @param containerName The name of the container holding the VHD file
         * @param vhdName The name for the VHD file
         * @return The stage representing creatable VM definition
         */
        T withExistingDataDisk(String storageAccountName, String containerName, String vhdName);

        /**
         * Specifies a new blank data disk to be attached to the virtual machine along with it's configuration.
         *
         * @param name The name for the data disk
         * @return The stage representing configuration for the data disk
         */
        ConfigureNewDataDisk<T> defineNewDataDisk(String name);

        /**
         * Specifies an existing VHD that needs to be attached to the virtual machine as data disk along with it's configuration.
         *
         * @param name The name for the data disk
         * @return The stage representing configuration for the data disk
         */
        ConfigureExistingDataDisk<T> defineExistingDataDisk(String name);
    }

    /**
     * The virtual machine definition stage with storage account.
     *
     * @param <T> The virtual machine definition in creatable stage.
     */
    interface DefinitionStorageAccount<T extends DefinitionCreatable> {
        /**
         * Specifies the name of the storage account to create, the OS disk for VM created from a market-place.
         * image will be stored in this account.
         *
         * @param name The name of the storage account
         * @return The stage representing creatable VM definition
         */
        T withNewStorageAccount(String name);

        /**
         * Specifies an instance of StorageAccount.DefinitionCreatable representing the storage account to be
         * created, the OS disk for VM created from a market-place image will be stored in this account.
         *
         * @param creatable The name of the storage account
         * @return The stage representing creatable VM definition
         */
        T withNewStorageAccount(StorageAccount.DefinitionCreatable creatable);

        /**
         * Specifies the name of an existing storage account where the OS disk for VM created from market-place
         * or user image (generalized image) needs be stored.
         *
         * @param name The name of an existing storage account
         * @return The stage representing creatable VM definition
         */
        T withExistingStorageAccount(String name);
    }

    /**
     * The virtual machine definition in cretable stage.
     */
    interface DefinitionCreatable extends
            DefinitionPassword<DefinitionCreatable>,
            DefinitionOSDiskSettings<DefinitionCreatable>,
            DefinitionWithVMSize<DefinitionCreatable>,
            DefinitionStorageAccount<DefinitionCreatable>,
            DefinitionWithDataDisk<DefinitionCreatable>,
            Creatable<VirtualMachine> {
    }
}
