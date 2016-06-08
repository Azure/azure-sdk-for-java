package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.implementation.KnownVirtualMachineImage;
import com.microsoft.azure.management.compute.implementation.api.*;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.*;
import com.microsoft.azure.management.storage.StorageAccount;

import java.io.IOException;
import java.util.List;

/**
 * An immutable client-side representation of an Azure virtual machine.
 */
public interface VirtualMachine extends
        GroupableResource,
        Refreshable<VirtualMachine>,
        Wrapper<VirtualMachineInner>,
        Updatable<VirtualMachine.Update> {
    // Actions
    //

    /**
     * Shuts down the Virtual Machine and releases the compute resources.
     * <p>
     * You are not billed for the compute resources that this Virtual Machine uses
     *
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException thrown for IO exception.
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void deallocate() throws CloudException, IOException, InterruptedException;

    /**
     * Generalize the Virtual Machine.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     */
    void generalize() throws CloudException, IOException;

    /**
     * power off (stop) the virtual machine.
     * <p>
     * You will be billed for the compute resources that this Virtual Machine uses
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void powerOff() throws CloudException, IOException, InterruptedException;

    /**
     * Restart the virtual machine.
     *
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void restart() throws CloudException, IOException, InterruptedException;

    /**
     * Start the virtual machine.
     *
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void start() throws CloudException, IOException, InterruptedException;

    /**
     * Redeploy the virtual machine.
     *
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void redeploy() throws CloudException, IOException, InterruptedException;

    // Getters
    //

    /**
     * @return name of this virtual machine
     */
    String computerName();

    /**
     * @return the virtual machine size
     */
    String size();

    /**
     * @return the operating system of this virtual machine
     */
    OperatingSystemTypes osType();

    /**
     * @return the uri to the vhd file backing this virtual machine's operating system disk
     */
    String osDiskVhdUri();

    /**
     * @return the operating system disk caching type, valid values are 'None', 'ReadOnly', 'ReadWrite'
     */
    CachingTypes osDiskCachingType();

    /**
     * @return the size of the operating system disk in GB
     */
    Integer osDiskSize();

    /**
     * @return the list of data disks attached to this virtual machine
     */
    List<DataDisk> dataDisks();

    /**
     * Gets the primary network interface of this virtual machine.
     * <p>
     * note that this method makes a rest API call to fetch the network interface
     *
     * @return the primary network interface associated with this network interface.
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    NetworkInterface primaryNetworkInterface() throws CloudException, IOException;

    /**
     * Gets the public IP address associated with this virtual machine's primary network interface.
     * <p>
     * note that this method makes a rest API call to fetch the resource
     *
     * @return the public IP of the primary network interface
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    PublicIpAddress primaryPublicIpAddress()  throws CloudException, IOException;

    /**
     * @return the list of reference ids of the network interfaces associated with this virtual machine
     */
    List<String> networkInterfaceIds();

    /**
     * @return the reference id of the primary network interface of this virtual machine
     */
    String primaryNetworkInterfaceId();

    /**
     * Returns id to the availability set this virtual machine associated with.
     * <p>
     * Having a set of virtual machines in an availability set ensures that during maintenance
     * event at least one virtual machine will be available.
     *
     * @return the availabilitySet reference id
     */
    String availabilitySetId();

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
     * @return the plan value
     */
    Plan plan();

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
     * Gets the operating system profile of an Azure virtual machine.
     *
     * @return the osProfile value
     */
    OSProfile osProfile();

    /**
     * Returns the diagnostics profile of an Azure virtual machine.
     * <p>
     * Enabling diagnostic features in a virtual machine enable you to easily diagnose and recover
     * virtual machine from boot failures.
     *
     * @return the diagnosticsProfile value
     */
    DiagnosticsProfile diagnosticsProfile();

    // Setters
    //

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
         * Create a new network interface to associate with the virtual machine as it's primary network interface.
         *
         * @param name the name for the new network interface
         * @return The next stage of the virtual machine definition
         */
        DefinitionWithOS withNewPrimaryNetworkInterface(String name);

        /**
         * Create a new network interface to associate the virtual machine with as it's primary network interface,
         * based on the provided definition.
         *
         * @param creatable a creatable definition for a new network interface
         * @return The next stage of the virtual machine definition
         */
        DefinitionWithOS withNewPrimaryNetworkInterface(NetworkInterface.DefinitionCreatable creatable);

        /**
         * Associate an existing network interface as the virtual machine with as it's primary network interface.
         *
         * @param networkInterface an existing network interface
         * @return The next stage of the virtual machine definition
         */
        DefinitionWithOS withExistingPrimaryNetworkInterface(NetworkInterface networkInterface);
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
        DataDisk.DefinitionAttachNewDataDisk<T> defineNewDataDisk(String name);

        /**
         * Specifies an existing VHD that needs to be attached to the virtual machine as data disk along with
         * it's configuration.
         *
         * @param name the name for the data disk
         * @return the stage representing configuration for the data disk
         */
        DataDisk.DefinitionAttachExistingDataDisk<T> defineExistingDataDisk(String name);
    }

    /**
     * The stage of the virtual machine definition allowing to specify availability set.
     *
     * @param <T> the virtual machine definition in creatable stage
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
         * Specifies an existing {@link AvailabilitySet} availability set to to associate the virtual machine with.
         * <p>
         * Adding virtual machines running your application to an availability set ensures that during
         * maintenance event at least one virtual machine will be available.
         *
         * @param availabilitySet an existing availability set
         * @return the stage representing creatable VM definition
         */
        T withExistingAvailabilitySet(AvailabilitySet availabilitySet);
    }

    /**
     * The stage of the virtual machine definition allowing to specify storage account.
     *
     * @param <T> the virtual machine definition in creatable stage
     */
    interface DefinitionStorageAccount<T extends DefinitionCreatable> {
        /**
         * Specifies the name of a new storage account to put the VM's OS and data disk VHD in.
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
         * to put the VM's OS and data disk VHD in.
         * <p>
         * Only the OS disk based on marketplace image will be stored in the new storage account,
         * an OS disk based on user image will be stored in the same storage account as user image.
         *
         * @param creatable the storage account in creatable stage
         * @return the stage representing creatable VM definition
         */
        T withNewStorageAccount(StorageAccount.DefinitionCreatable creatable);

        /**
         * Specifies an existing {@link StorageAccount} storage account to put the VM's OS and data disk VHD in.
         * <p>
         * An OS disk based on marketplace or user image (generalized image) will be stored in this
         * storage account.
         *
         * @param storageAccount an existing storage account
         * @return the stage representing creatable VM definition
         */
        T withExistingStorageAccount(StorageAccount storageAccount);
    }

    /**
     * The stage of virtual machine definition allowing to specify additional network interfaces.
     *
     * @param <T> the virtual machine definition in creatable stage
     */
    interface DefinitionWithSecondaryNetworkInterface<T extends DefinitionCreatable> {
        /**
         * Create a new network interface to associate with the virtual machine, based on the
         * provided definition.
         *
         * <p>
         * Note this method's effect is additive, i.e. each time it is used, the new secondary
         * network interface added to the virtual machine.
         *
         * @param creatable a creatable definition for a new network interface
         * @return the stage representing creatable VM definition
         */
        T withNewSecondaryNetworkInterface(NetworkInterface.DefinitionCreatable creatable);

        /**
         * Associate an existing network interface with the virtual machine.
         *
         * Note this method's effect is additive, i.e. each time it is used, the new secondary
         * network interface added to the virtual machine.
         *
         * @param networkInterface an existing network interface
         * @return the stage representing creatable VM definition
         */
        T withExistingSecondaryNetworkInterface(NetworkInterface networkInterface);
    }

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
            DefinitionWithSecondaryNetworkInterface<DefinitionCreatable>,
            Creatable<VirtualMachine> {
    }

    /**
     * Container interface for all the definitions.
     */
    interface Definitions extends
            VirtualMachine.DefinitionBlank,
            VirtualMachine.DefinitionWithGroup,
            VirtualMachine.DefinitionWithPrimaryNetworkInterface,
            VirtualMachine.DefinitionWithOS,
            VirtualMachine.DefinitionWithMarketplaceImage,
            VirtualMachine.DefinitionWithOSType,
            VirtualMachine.DefinitionWithRootUserName,
            VirtualMachine.DefinitionWithAdminUserName,
            VirtualMachine.DefinitionLinuxCreatable,
            VirtualMachine.DefinitionWindowsCreatable,
            VirtualMachine.DefinitionCreatable {
    }

    /**
     * The template for a virtual machine update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<VirtualMachine>,
            Resource.UpdateWithTags<Update>  {
        /**
         * Specifies the new size for the virtual machine.
         *
         * @param sizeName the name of the size for the virtual machine as text
         * @return the stage representing updatable VM definition
         */
        Update withSize(String sizeName);

        /**
         * Specifies the new size for the virtual machine.
         *
         * @param size a size from the list of available sizes for the virtual machine
         * @return the stage representing updatable VM definition
         */
        Update withSize(VirtualMachineSizeTypes size);

        /**
         * Specifies that a new blank data disk needs to be attached to virtual machine.
         *
         * @param sizeInGB the disk size in GB
         * @return the stage representing updatable VM definition
         */
        Update withNewDataDisk(Integer sizeInGB);

        /**
         * Specifies an existing VHD that needs to be attached to the virtual machine as data disk.
         *
         * @param storageAccountName the storage account name
         * @param containerName the name of the container holding the VHD file
         * @param vhdName the name for the VHD file
         * @return the stage representing updatable VM definition
         */
        Update withExistingDataDisk(String storageAccountName, String containerName, String vhdName);

        /**
         * Specifies a new blank data disk to be attached to the virtual machine along with it's configuration.
         *
         * @param name the name for the data disk
         * @return the stage representing configuration for the data disk
         */
        DataDisk.DefinitionAttachNewDataDisk<Update> defineNewDataDisk(String name);

        /**
         * Specifies an existing VHD that needs to be attached to the virtual machine as data disk along with
         * it's configuration.
         *
         * @param name the name for the data disk
         * @return the stage representing configuration for the data disk
         */
        DataDisk.DefinitionAttachExistingDataDisk<Update> defineExistingDataDisk(String name);

        /**
         * Detaches a data disk with the given name from the virtual machine.
         *
         * @param name the name of the data disk to remove
         * @return the stage representing updatable VM definition
         */
        Update withoutDataDisk(String name);

        /**
         * Detaches a data disk with the given logical unit number from the virtual machine.
         *
         * @param lun the logical unit number of the data disk to remove
         * @return the stage representing updatable VM definition
         */
        Update withoutDataDisk(int lun);

        /**
         * Create a new network interface to associate with the virtual machine, based on the
         * provided definition.
         *
         * <p>
         * Note this method's effect is additive, i.e. each time it is used, the new secondary
         * network interface added to the virtual machine.
         *
         * @param creatable a creatable definition for a new network interface
         * @return the stage representing updatable VM definition
         */
        Update withNewSecondaryNetworkInterface(NetworkInterface.DefinitionCreatable creatable);

        /**
         * Associate an existing network interface with the virtual machine.
         *
         * Note this method's effect is additive, i.e. each time it is used, the new secondary
         * network interface added to the virtual machine.
         *
         * @param networkInterface an existing network interface
         * @return the stage representing updatable VM definition
         */
        Update withExistingSecondaryNetworkInterface(NetworkInterface networkInterface);

        /**
         * Removes a network interface associated with virtual machine.
         *
         * @param name the name of the secondary network interface to remove
         * @return the stage representing updatable VM definition
         */
        Update withoutSecondaryNetworkInterface(String name);
    }
}
