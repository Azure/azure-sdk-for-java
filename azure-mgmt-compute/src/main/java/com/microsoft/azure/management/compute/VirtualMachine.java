package com.microsoft.azure.management.compute;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.compute.implementation.KnownVirtualMachineImage;
import com.microsoft.azure.management.compute.implementation.api.*;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.storage.StorageAccount;

import java.util.List;

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

    interface DefinitionBlank extends GroupableResource.DefinitionWithRegion<DefinitionWithGroup> {
    }

    interface DefinitionWithGroup extends GroupableResource.DefinitionWithGroup<DefinitionWithOS> {
    }

    interface DefinitionWithOS {
        /**
         * Specifies the market-place image used for the virtual machine's OS.
         * @return The next stage of the virtual machine definition.
         */
        DefinitionWithMarketplaceImage withMarketplaceImage();

        /**
         * Specifies the user (generalized) image used for the virtual machine's OS.
         * @param imageUrl The url the the VHD
         * @return The next stage of the virtual machine definition.
         */
        DefinitionWithOSType withStoredImage(String imageUrl);

        /**
         * Specifies the specialized operating system disk to be attached to the virtual machine.
         * @param osDiskUrl The url to the OS disk in the Azure Storage account.
         * @return The next stage of the Windows virtual machine definition.
         */
        DefinitionCreatable withOSDisk(String osDiskUrl, OperatingSystemTypes osType);
    }

    interface DefinitionWithMarketplaceImage {
        /**
         * Specifies the version of image.
         * @param imageReference describes publisher, offer, sku and version of the market-place image.
         * @return The next stage of the virtual machine definition.
         */
        DefinitionWithOSType version(ImageReference imageReference);

        /**
         * Specifies that the latest version of the image needs to be used.
         * @param publisher Specifies the publisher of the image
         * @param offer Specifies the offer of the image
         * @param sku Specifies the SKU of the image
         * @return The next stage of the virtual machine definition.
         */
        DefinitionWithOSType latest(String publisher, String offer, String sku);

        /**
         * Specifies the known image to be used.
         * @param knownImage Enum value indicating known market-place image.
         * @return The next stage of the virtual machine definition.
         */
        DefinitionWithOSType popular(KnownVirtualMachineImage knownImage);
    }

    interface DefinitionWithOSType {
        /**
         * Specifies the OS type of the virtual machine as Linux.
         * @return The next stage of the Linux virtual machine definition.
         */
        DefinitionWithRootUserName withLinuxOS();

        /**
         * Specifies the OS type as Windows.
         * @return The next stage of the Windows virtual machine definition.
         */
        DefinitionWithAdminUserName withWindowsOS();
    }

    interface DefinitionWithRootUserName {
        /**
         * Specifies the root user name for the Linux virtual machine.
         * @param rootUserName The Linux root user name. This must follow the required naming convention for Linux user name.
         * @return The next stage of the Linux virtual machine definition.
         */
        DefinitionLinuxCreatable withRootUserName(String rootUserName);
    }

    interface DefinitionWithAdminUserName {
        /**
         * Specifies the administrator user name for the Windows virtual machine.
         * @param adminUserName The Windows administrator user name. This must follow the required naming convention for Windows user name.
         * @return The stage representing creatable Linux VM definition
         */
        DefinitionWindowsCreatable withAdminUserName(String adminUserName);
    }

    interface DefinitionLinuxCreatable extends DefinitionCreatable {
        /**
         * Specifies the SSH public key, each call to this method adds the given public key to the list
         * of VM's public keys.
         *
         * @param publicKey The SSH public key in PEM format.
         * @return The stage representing creatable Linux VM definition
         */
        DefinitionLinuxCreatable withSsh(String publicKey);
    }

    interface DefinitionWindowsCreatable extends DefinitionCreatable {
        /**
         * Specifies that VM Agent should not be provisioned.
         * @return The stage representing creatable Windows VM definition
         */
        DefinitionWindowsCreatable disableVMAgent();

        /**
         * Specifies that automatic updates should be disabled.
         * @return The stage representing creatable Windows VM definition
         */
        DefinitionWindowsCreatable disableAutoUpdate();

        /**
         * Specifies the time-zone.
         * @return The stage representing creatable Windows VM definition
         */
        DefinitionWindowsCreatable withTimeZone(String timeZone);

        /**
         * Specifies the WINRM listener, each call to this method adds the given listener to the list
         * of VM's WinRM listeners.
         * @return The stage representing creatable Windows VM definition
         */
        DefinitionWindowsCreatable withWinRM(WinRMListener listener);
    }

    interface DefinitionPassword<T extends DefinitionCreatable> {
        /**
         * Specifies the password for the virtual machine.
         * @param password The password. This must follow the criteria for Azure VM password.
         * @return The stage representing creatable VM definition
         */
        T withPassword(String password);
    }

    interface DefinitionOSDiskSettings<T extends DefinitionCreatable> {
        /**
         * Specifies the caching type for the Operating System disk.
         * @param cachingType The caching type.
         * @return The stage representing creatable VM definition
         */
        T withOSDiskCaching(CachingTypes cachingType);

        /**
         * Specifies the name of the OS Disk Vhd file and it's parent container.
         * @param containerName The name of the container in the selected storage account.
         * @param vhdName The name for the OS Disk vhd.
         * @return The stage representing creatable VM definition
         */
        T withOSDiskVhdLocation(String containerName, String vhdName);

        /**
         * Specifies the encryption settings for the OS Disk.
         * @param settings The encryption settings.
         * @return The stage representing creatable VM definition
         */
        T withOSDiskEncryptionSettings(DiskEncryptionSettings settings);

        /**
         * Specifies the size of the OSDisk in GB.
         * @param size The VHD size.
         * @return The stage representing creatable VM definition
         */
        T withOSDiskSizeInGB(Integer size);

        /**
         * Specifies the name for the OS Disk.
         * @param name The OS Disk name.
         * @return The stage representing creatable VM definition
         */
        T withOSDiskName(String name);
    }

    interface DefinitionStorageAccount<T extends DefinitionCreatable> {
        /**
         * Specifies the name of the storage account to create, the OS disk for VM created from a market-place
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

    interface DefinitionCreatable extends
            DefinitionPassword<DefinitionCreatable>,
            DefinitionOSDiskSettings<DefinitionCreatable>,
            DefinitionStorageAccount<DefinitionCreatable>,
            Creatable<VirtualMachine> {
    }
}
