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

    interface DefinitionWithGroup extends GroupableResource.DefinitionWithGroup<DefinitionWithStorageAccount> {
    }

    interface DefinitionWithStorageAccount {
        DefinitionWithOS withNewStorageAccount(String name);
        DefinitionWithOS withNewStorageAccount(StorageAccount.DefinitionCreatable creatable);
        DefinitionWithOS withExistingStorageAccount(String name);
    }

    interface DefinitionWithOS {
        /**
         * Specifies the platform image to create the virtual machine from.
         * @param imageReference describes publisher, offer, sku and version of the platform image.
         * @return The next stage of the virtual machine definition.
         */
        DefinitionWithOSType withImage(ImageReference imageReference);

        /**
         * Specifies the platform image to create the virtual machine from. The latest version of
         * the image will be used.
         * @param publisher Specifies the publisher of the image
         * @param offer Specifies the offer of the image
         * @param sku Specifies the SKU of the image
         * @return The next stage of the virtual machine definition.
         */
        DefinitionWithOSType withLatestImage(String publisher, String offer, String sku);

        /**
         * Specifies the platform image to create the virtual machine from.
         * @param knownImage Enum indicating the platform image.
         * @return The next stage of the virtual machine definition.
         */
        DefinitionWithOSType withKnownImage(KnownVirtualMachineImage knownImage);

        /**
         * Specifies the generalized image to create the virtual machine from.
         * @param userImageUrl The url the the VHD
         * @return The next stage of the virtual machine definition.
         */
        DefinitionWithOSType withImage(String userImageUrl);

        /**
         * Specifies the specialized Windows OS disk to be attached to the virtual machine.
         * @param osDiskUrl The url to the OS disk in the Azure Storage account.
         * @return The next stage of the Windows virtual machine definition.
         */
        DefinitionWithNextTODO withWindowsOSDisk(String osDiskUrl);

        /**
         * Specifies the specialized Linux OS disk to be attached to the virtual machine.
         * @param osDiskUrl The url to the OS disk in the Azure Storage account.
         * @return The next stage of the Linux virtual machine definition.
         */
        DefinitionWithNextTODO withLinuxOSDisk(String osDiskUrl);
    }


    interface DefinitionWithOSType {
        /**
         * Specifies the OS type of the virtual machine as Linux.
         * @return The next stage of the Windows virtual machine definition.
         */
        DefinitionWithRootUserName withLinuxOS();

        /**
         * Specifies the OS type as Windows.
         * @return The next stage allowing optionally setting the Windows configuration.
         */
        DefinitionWithOptionalWindowsConfiguration withWindowsOS();
    }

    interface DefinitionWithOptionalWindowsConfiguration {
        /**
         * Specifies the configuration for the Windows virtual machine.
         * @return The next stage for setting the configuration.
         */
        DefinitionWithWindowsConfiguration defineConfiguration();

        /**
         * Specifies the administrator user name for the Windows virtual machine.
         * @param userName The Windows administrator user name to use. This must follow the required naming convention for Windows user name.
         * @return The next stage of the Windows virtual machine definition.
         */
        DefinitionWithPassword withAdminUserName(String userName);
    }

    interface DefinitionWithWindowsConfiguration {
        /**
         * Specifies that VM Agent should not be provisioned.
         * @return The optional windows configurations
         */
        DefinitionWithWindowsConfiguration disableVMAgent();

        /**
         * Specifies that automatic updates should be disabled.
         * @return The optional windows configurations
         */
        DefinitionWithWindowsConfiguration disableAutoUpdate();

        /**
         * Specifies the time-zone.
         * @return The optional windows configurations
         */
        DefinitionWithWindowsConfiguration withTimeZone(String timeZone);

        /**
         * Specifies the list of VM RM listeners.
         * @return The optional windows configurations
         */
        DefinitionWithWindowsConfiguration withWinRM(List<WinRMListener> listeners);

        /**
         * applies the Windows configuration.
         * @return The next stage of the Windows virtual machine definition.
         */
        DefinitionWithAdminUserName apply();
    }

    interface DefinitionWithRootUserName {
        /**
         * Specifies the root user name for the Linux virtual machine.
         * @param rootUserName The Linux root user name to use. This must follow the required naming convention for Linux user name.
         * @return The next stage of the Linux virtual machine definition.
         */
        DefinitionWithOptionalSsh withRootUserName(String rootUserName);
    }

    interface DefinitionWithAdminUserName {
        /**
         * Specifies the administrator user name for the Windows virtual machine.
         * @param adminUserName The Windows administrator user name to use. This must follow the required naming convention for Windows user name.
         * @return The next stage of the Windows virtual machine definition.
         */
        DefinitionWithPassword withAdminUserName(String adminUserName);
    }

    interface DefinitionWithOptionalSsh {
        /**
         * Specifies the SSH public key
         * @param publicKey The SSH public key in PEM format.
         * @return The next stage of the Linux virtual machine definition.
         */
        DefinitionWithOptionalPassword withSsh(String publicKey);

        /**
         * Specifies the list of SSH public keys.
         * @param publicKeys The list of SSH public keys in PEM format.
         * @return The next stage of the Linux virtual machine definition.
         */
        DefinitionWithOptionalPassword withSsh(List<String> publicKeys);

        /**
         * Ensure that no SSH login with public key possible.
         * @return The next stage of the Linux virtual machine definition.
         */
        DefinitionWithPassword withoutSsh();
    }

    interface DefinitionWithPassword {
        /**
         * Specifies the password for the virtual machine.
         * @param password The password. This must follow the criteria for Azure VM password.
         * @return The next stage of the Linux virtual machine definition.
         */
        DefinitionWithNextTODO withPassword(String password);
    }

    interface DefinitionWithoutPassword {
        /**
         * Ensure that no SSH login with password possible.
         * @return The next stage of the Linux virtual machine definition.
         */
        DefinitionWithNextTODO withoutPassword();
    }

    interface DefinitionWithOptionalPassword extends DefinitionWithPassword, DefinitionWithoutPassword {
    }

    interface DefinitionWithNextTODO {
        DefinitionCreatable moreVMRequiredParameters();
    }

    interface DefinitionCreatable extends
            Creatable<VirtualMachine> {
        /**
         * Specifies the caching type for the Operating System disk.
         * @param cachingType The caching type.
         * @return The stage with optional parameters for virtual machine definition.
         */
        DefinitionCreatable withOSDiskCaching(CachingTypes cachingType);

        /**
         * Specifies the name of the OS Disk Vhd file and it's parent container.
         * @param containerName The name of the container in the selected storage account.
         * @param vhdName The name for the OS Disk vhd.
         * @return The stage with optional parameters for virtual machine definition.
         */
        DefinitionCreatable withOSDiskVhdLocation(String containerName, String vhdName);

        /**
         * Specifies the encryption settings for the OS Disk.
         * @param settings The encryption settings.
         * @return The stage with optional parameters for virtual machine definition.
         */
        DefinitionCreatable withOSDiskEncryptionSettings(DiskEncryptionSettings settings);

        /**
         * Specifies the size of the OSDisk in GB.
         * @param size The VHD size.
         * @return The stage with optional parameters for virtual machine definition.
         */
        DefinitionCreatable withOSDiskSizeInGB(Integer size);

        /**
         * Specifies the name for the OS Disk.
         * @param name The OS Disk name.
         * @return The stage with optional parameters for virtual machine definition.
         */
        DefinitionCreatable withOSDiskName(String name);
    }
}
