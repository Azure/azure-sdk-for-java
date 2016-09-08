package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.implementation.VirtualMachineInner;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.model.HasNetworkInterfaces;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.storage.StorageAccount;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * An immutable client-side representation of an Azure virtual machine.
 */
public interface VirtualMachine extends
        GroupableResource,
        Refreshable<VirtualMachine>,
        Wrapper<VirtualMachineInner>,
        Updatable<VirtualMachine.Update>,
        HasNetworkInterfaces {

    // Actions

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
     * Power off (stop) the virtual machine.
     * <p>
     * You will be billed for the compute resources that this Virtual Machine uses.
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

    /**
     * List of all available virtual machine sizes this virtual machine can resized to.
     *
     * @return the virtual machine sizes
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     */
    PagedList<VirtualMachineSize> availableSizes() throws CloudException, IOException;

    /**
     * Captures the virtual machine by copying virtual hard disks of the VM and returns template as json
     * string that can be used to create similar VMs.
     *
     * @param containerName destination container name to store the captured Vhd
     * @param overwriteVhd whether to overwrites destination vhd if it exists
     * @return the template as json string
     * @throws CloudException thrown for an invalid response from the service
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    String capture(String containerName, boolean overwriteVhd) throws CloudException, IOException, InterruptedException;

    /**
     * Refreshes the virtual machine instance view to sync with Azure.
     * <p>
     * this will caches the instance view which can be later retrieved using {@link VirtualMachine#instanceView()}.
     *
     * @return the refreshed instance view
     * @throws CloudException thrown for an invalid response from the service
     * @throws IOException exception thrown from serialization/deserialization
     */
    VirtualMachineInstanceView refreshInstanceView() throws CloudException, IOException;

    // Getters
    //

    /**
     * @return name of this virtual machine
     */
    String computerName();

    /**
     * @return the virtual machine size
     */
    VirtualMachineSizeTypes size();

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
    int osDiskSize();

    /**
     * @return the list of data disks attached to this virtual machine
     */
    List<VirtualMachineDataDisk> dataDisks();

    /**
     * Gets the public IP address associated with this virtual machine's primary network interface.
     * <p>
     * note that this method makes a rest API call to fetch the resource.
     *
     * @return the public IP of the primary network interface
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    PublicIpAddress primaryPublicIpAddress()  throws CloudException, IOException;

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
     * @return the licenseType value
     */
    String licenseType();

    /**
     * @return the extensions attached to the Azure Virtual Machine
     */
    Map<String, VirtualMachineExtension> extensions();

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

    /**
     * @return the virtual machine unique id.
     */
    String vmId();

    /**
     * @return the power state of the virtual machine
     */
    PowerState powerState();

    /**
     * Get the virtual machine instance view.
     * <p>
     * this method returns the cached instance view, to refresh the cache call {@link VirtualMachine#refreshInstanceView()}.
     *
     * @return the virtual machine instance view
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    VirtualMachineInstanceView instanceView() throws CloudException, IOException;

    // Setters
    //

    /**
     * The entirety of the virtual machine definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithNetwork,
            DefinitionStages.WithSubnet,
            DefinitionStages.WithPrivateIp,
            DefinitionStages.WithPublicIpAddress,
            DefinitionStages.WithPrimaryNetworkInterface,
            DefinitionStages.WithOS,
            DefinitionStages.WithRootUserName,
            DefinitionStages.WithAdminUserName,
            DefinitionStages.WithLinuxCreate,
            DefinitionStages.WithWindowsCreate,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of virtual machine definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a virtual machine definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the virtual machine definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithNetwork> {
        }

        /**
         * The stage of the virtual machine definition allowing to specify virtual network for the new primary network
         * interface or to use a creatable or existing network interface.
         */
        interface WithNetwork extends WithPrimaryNetworkInterface {
            /**
             * Create a new virtual network to associate with the virtual machine's primary network interface, based on
             * the provided definition.
             *
             * @param creatable a creatable definition for a new virtual network
             * @return the next stage of the virtual machine definition
             */
            WithPrivateIp withNewPrimaryNetwork(Creatable<Network> creatable);

            /**
             * Creates a new virtual network to associate with the virtual machine's primary network interface.
             * <p>
             * the virtual network will be created in the same resource group and region as of virtual machine, it will be
             * created with the specified address space and a default subnet covering the entirety of the network IP address space.
             *
             * @param addressSpace the address space for the virtual network
             * @return the next stage of the virtual machine definition
             */
            WithPrivateIp withNewPrimaryNetwork(String addressSpace);

            /**
             * Associate an existing virtual network with the the virtual machine's primary network interface.
             *
             * @param network an existing virtual network
             * @return the next stage of the virtual machine definition
             */
            WithSubnet withExistingPrimaryNetwork(Network network);
        }

        /**
         * The stage of the virtual machine definition allowing to specify virtual network subnet for the new primary network interface.
         *
         */
        interface WithSubnet {
            /**
             * Associates a subnet with the virtual machine's primary network interface.
             *
             * @param name the subnet name
             * @return the next stage of the definition
             */
            WithPrivateIp withSubnet(String name);
        }

        /**
         * The stage of the virtual machine definition allowing to specify private IP address within a virtual network subnet.
         */
        interface WithPrivateIp {
            /**
             * Enables dynamic private IP address allocation within the specified existing virtual network subnet for
             * virtual machine's primary network interface.
             *
             * @return the next stage of the virtual machine definition
             */
            WithPublicIpAddress withPrimaryPrivateIpAddressDynamic();

            /**
             * Assigns the specified static private IP address within the specified existing virtual network subnet to the
             * virtual machine's primary network interface.
             *
             * @param staticPrivateIpAddress the static IP address within the specified subnet to assign to
             *                               the network interface
             * @return the next stage of the virtual machine definition
             */
            WithPublicIpAddress withPrimaryPrivateIpAddressStatic(String staticPrivateIpAddress);
        }

        /**
         * The stage of the virtual machine definition allowing to associate public IP address with it's primary network interface.
         */
        interface WithPublicIpAddress {
            /**
             * Create a new public IP address to associate with virtual machine primary network interface, based on the
             * provided definition.
             *
             * @param creatable a creatable definition for a new public IP
             * @return the next stage of the virtual machine definition
             */
            WithOS withNewPrimaryPublicIpAddress(Creatable<PublicIpAddress> creatable);

            /**
             * Creates a new public IP address in the same region and group as the resource, with the specified DNS label
             * and associate it with the virtual machine's primary network interface.
             * <p>
             * the internal name for the public IP address will be derived from the DNS label.
             *
             * @param leafDnsLabel the leaf domain label
             * @return the next stage of the virtual machine definition
             */
            WithOS withNewPrimaryPublicIpAddress(String leafDnsLabel);

            /**
             * Associates an existing public IP address with the virtual machine's primary network interface.
             *
             * @param publicIpAddress an existing public IP address
             * @return the next stage of the virtual machine definition
             */
            WithOS withExistingPrimaryPublicIpAddress(PublicIpAddress publicIpAddress);

            /**
             * Specifies that no public IP needs to be associated with virtual machine.
             *
             * @return the next stage of the virtual machine definition
             */
            WithOS withoutPrimaryPublicIpAddress();
        }

        /**
         * The stage of the virtual machine definition allowing to specify the primary network interface.
         */
        interface WithPrimaryNetworkInterface {
            /**
             * Create a new network interface to associate the virtual machine with as it's primary network interface,
             * based on the provided definition.
             *
             * @param creatable a creatable definition for a new network interface
             * @return The next stage of the virtual machine definition
             */
            WithOS withNewPrimaryNetworkInterface(Creatable<NetworkInterface> creatable);

            /**
             * Associate an existing network interface as the virtual machine with as it's primary network interface.
             *
             * @param networkInterface an existing network interface
             * @return The next stage of the virtual machine definition
             */
            WithOS withExistingPrimaryNetworkInterface(NetworkInterface networkInterface);
        }

        /**
         * The stage of the virtual machine definition allowing to specify the Operation System image.
         */
        interface WithOS {
            /**
             * Specifies the known marketplace Windows image used for the virtual machine's OS.
             *
             * @param knownImage enum value indicating known market-place image
             * @return the next stage of the virtual machine definition
             */
            WithAdminUserName withPopularWindowsImage(KnownWindowsVirtualMachineImage knownImage);

            /**
             * Specifies that the latest version of a marketplace Windows image needs to be used.
             *
             * @param publisher specifies the publisher of the image
             * @param offer specifies the offer of the image
             * @param sku specifies the SKU of the image
             * @return the next stage of the virtual machine definition
             */
            WithAdminUserName withLatestWindowsImage(String publisher, String offer, String sku);

            /**
             * Specifies the version of a marketplace Windows image needs to be used.
             *
             * @param imageReference describes publisher, offer, sku and version of the market-place image
             * @return the next stage of the virtual machine definition
             */
            WithAdminUserName withSpecificWindowsImageVersion(ImageReference imageReference);

            /**
             * Specifies the user (generalized) Windows image used for the virtual machine's OS.
             *
             * @param imageUrl the url the the VHD
             * @return the next stage of the virtual machine definition
             */
            WithAdminUserName withStoredWindowsImage(String imageUrl);

            /**
             * Specifies the known marketplace Linux image used for the virtual machine's OS.
             *
             * @param knownImage enum value indicating known market-place image
             * @return the next stage of the virtual machine definition
             */
            WithRootUserName withPopularLinuxImage(KnownLinuxVirtualMachineImage knownImage);

            /**
             * Specifies that the latest version of a marketplace Linux image needs to be used.
             *
             * @param publisher specifies the publisher of the image
             * @param offer specifies the offer of the image
             * @param sku specifies the SKU of the image
             * @return the next stage of the virtual machine definition
             */
            WithRootUserName withLatestLinuxImage(String publisher, String offer, String sku);

            /**
             * Specifies the version of a market-place Linux image needs to be used.
             *
             * @param imageReference describes publisher, offer, sku and version of the market-place image
             * @return the next stage of the virtual machine definition
             */
            WithRootUserName withSpecificLinuxImageVersion(ImageReference imageReference);

            /**
             * Specifies the user (generalized) Linux image used for the virtual machine's OS.
             *
             * @param imageUrl the url the the VHD
             * @return the next stage of the virtual machine definition
             */
            WithRootUserName withStoredLinuxImage(String imageUrl);

            /**
             * Specifies the specialized operating system disk to be attached to the virtual machine.
             *
             * @param osDiskUrl osDiskUrl the url to the OS disk in the Azure Storage account
             * @param osType the OS type
             * @return the next stage of the Windows virtual machine definition
             */
            WithCreate withOsDisk(String osDiskUrl, OperatingSystemTypes osType);
        }

        /**
         * The stage of the Linux virtual machine definition allowing to specify root user name.
         */
        interface WithRootUserName {
            /**
             * Specifies the root user name for the Linux virtual machine.
             *
             * @param rootUserName the Linux root user name. This must follow the required naming convention for Linux user name
             * @return the next stage of the Linux virtual machine definition
             */
            WithLinuxCreate withRootUserName(String rootUserName);
        }

        /**
         * The stage of the Windows virtual machine definition allowing to specify administrator user name.
         */
        interface WithAdminUserName {
            /**
             * Specifies the administrator user name for the Windows virtual machine.
             *
             * @param adminUserName the Windows administrator user name. This must follow the required naming convention for Windows user name.
             * @return the stage representing creatable Linux VM definition
             */
            WithWindowsCreate withAdminUserName(String adminUserName);
        }

        /**
         * The stage of the Linux virtual machine definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithLinuxCreate extends WithCreate {
            /**
             * Specifies the SSH public key.
             * <p>
             * Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the stage representing creatable Linux VM definition
             */
            WithLinuxCreate withSsh(String publicKey);
        }

        /**
         * The stage of the Windows virtual machine definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}, but also allows
         * for any other optional settings to be specified.
         */
        interface WithWindowsCreate extends WithCreate {
            /**
             * Specifies that VM Agent should not be provisioned.
             *
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsCreate disableVmAgent();

            /**
             * Specifies that automatic updates should be disabled.
             *
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsCreate disableAutoUpdate();

            /**
             * Specifies the time-zone.
             *
             * @param timeZone the timezone
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsCreate withTimeZone(String timeZone);

            /**
             * Specifies the WINRM listener.
             * <p>
             * Each call to this method adds the given listener to the list of VM's WinRM listeners.
             *
             * @param listener the WinRmListener
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsCreate withWinRm(WinRMListener listener);
        }

        /**
         * The stage of the virtual machine definition allowing to specify password.
         */
        interface WithPassword {
            /**
             * Specifies the password for the virtual machine.
             *
             * @param password the password. This must follow the criteria for Azure VM password.
             * @return the stage representing creatable VM definition
             */
            WithCreate withPassword(String password);
        }

        /**
         * The stage of the virtual machine definition allowing to specify OS disk configurations.
         */
        interface WithOsDiskSettings {
            /**
             * Specifies the caching type for the Operating System disk.
             *
             * @param cachingType the caching type.
             * @return the stage representing creatable VM definition
             */
            WithCreate withOsDiskCaching(CachingTypes cachingType);

            /**
             * Specifies the name of the OS Disk Vhd file and it's parent container.
             *
             * @param containerName the name of the container in the selected storage account.
             * @param vhdName the name for the OS Disk vhd.
             * @return the stage representing creatable VM definition
             */
            WithCreate withOsDiskVhdLocation(String containerName, String vhdName);

            /**
             * Specifies the encryption settings for the OS Disk.
             *
             * @param settings the encryption settings.
             * @return the stage representing creatable VM definition
             */
            WithCreate withOsDiskEncryptionSettings(DiskEncryptionSettings settings);

            /**
             * Specifies the size of the OSDisk in GB.
             *
             * @param size the VHD size.
             * @return the stage representing creatable VM definition
             */
            WithCreate withOsDiskSizeInGb(Integer size);

            /**
             * Specifies the name for the OS Disk.
             *
             * @param name the OS Disk name.
             * @return the stage representing creatable VM definition
             */
            WithCreate withOsDiskName(String name);
        }

        /**
         * The stage of the virtual machine definition allowing to specify VM size.
         */
        interface WithVMSize {
            /**
             * Specifies the virtual machine size.
             *
             * @param sizeName the name of the size for the virtual machine as text
             * @return the stage representing creatable VM definition
             */
            WithCreate withSize(String sizeName);

            /**
             * Specifies the virtual machine size.
             *
             * @param size a size from the list of available sizes for the virtual machine
             * @return the stage representing creatable VM definition
             */
            WithCreate withSize(VirtualMachineSizeTypes size);
        }

        /**
         * The stage of the virtual machine definition allowing to specify data disk configuration.
         */
        interface WithDataDisk {
            /**
             * Specifies that a new blank data disk needs to be attached to virtual machine.
             *
             * @param sizeInGB the disk size in GB
             * @return the stage representing creatable VM definition
             */
            WithCreate withNewDataDisk(Integer sizeInGB);

            /**
             * Specifies an existing VHD that needs to be attached to the virtual machine as data disk.
             *
             * @param storageAccountName the storage account name
             * @param containerName the name of the container holding the VHD file
             * @param vhdName the name for the VHD file
             * @return the stage representing creatable VM definition
             */
            WithCreate withExistingDataDisk(String storageAccountName, String containerName, String vhdName);

            /**
             * Specifies a new blank data disk to be attached to the virtual machine along with it's configuration.
             *
             * @param name the name for the data disk
             * @return the stage representing configuration for the data disk
             */
            VirtualMachineDataDisk.DefinitionStages.AttachNewDataDisk<WithCreate> defineNewDataDisk(String name);

            /**
             * Specifies an existing VHD that needs to be attached to the virtual machine as data disk along with
             * it's configuration.
             *
             * @param name the name for the data disk
             * @return the stage representing configuration for the data disk
             */
            VirtualMachineDataDisk.DefinitionStages.AttachExistingDataDisk<WithCreate> defineExistingDataDisk(String name);
        }

        /**
         * The stage of the virtual machine definition allowing to specify availability set.
         */
        interface WithAvailabilitySet {
            /**
             * Specifies the name of a new availability set to associate the virtual machine with.
             * <p>
             * Adding virtual machines running your application to an availability set ensures that during
             * maintenance event at least one virtual machine will be available.
             *
             * @param name the name of the availability set
             * @return the stage representing creatable VM definition
             */
            WithCreate withNewAvailabilitySet(String name);

            /**
             * Specifies definition of a not-yet-created availability set definition
             * to associate the virtual machine with.
             * <p>
             * Adding virtual machines running your application to an availability set ensures that during
             * maintenance event at least one virtual machine will be available.
             *
             * @param creatable the availability set in creatable stage
             * @return the stage representing creatable VM definition
             */
            WithCreate withNewAvailabilitySet(Creatable<AvailabilitySet> creatable);

            /**
             * Specifies an existing {@link AvailabilitySet} availability set to to associate the virtual machine with.
             * <p>
             * Adding virtual machines running your application to an availability set ensures that during
             * maintenance event at least one virtual machine will be available.
             *
             * @param availabilitySet an existing availability set
             * @return the stage representing creatable VM definition
             */
            WithCreate withExistingAvailabilitySet(AvailabilitySet availabilitySet);
        }

        /**
         * The stage of the virtual machine definition allowing to specify storage account.
         */
        interface WithStorageAccount {
            /**
             * Specifies the name of a new storage account to put the VM's OS and data disk VHD in.
             * <p>
             * Only the OS disk based on marketplace image will be stored in the new storage account,
             * an OS disk based on user image will be stored in the same storage account as user image.
             *
             * @param name the name of the storage account
             * @return the stage representing creatable VM definition
             */
            WithCreate withNewStorageAccount(String name);

            /**
             * Specifies definition of a not-yet-created storage account definition
             * to put the VM's OS and data disk VHDs in.
             * <p>
             * Only the OS disk based on marketplace image will be stored in the new storage account.
             * An OS disk based on user image will be stored in the same storage account as user image.
             *
             * @param creatable the storage account in creatable stage
             * @return the stage representing creatable VM definition
             */
            WithCreate withNewStorageAccount(Creatable<StorageAccount> creatable);

            /**
             * Specifies an existing {@link StorageAccount} storage account to put the VM's OS and data disk VHD in.
             * <p>
             * An OS disk based on marketplace or user image (generalized image) will be stored in this
             * storage account.
             *
             * @param storageAccount an existing storage account
             * @return the stage representing creatable VM definition
             */
            WithCreate withExistingStorageAccount(StorageAccount storageAccount);
        }

        /**
         * The stage of virtual machine definition allowing to specify additional network interfaces.
         */
        interface WithSecondaryNetworkInterface {
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
            WithCreate withNewSecondaryNetworkInterface(Creatable<NetworkInterface> creatable);

            /**
             * Associate an existing network interface with the virtual machine.
             *
             * Note this method's effect is additive, i.e. each time it is used, the new secondary
             * network interface added to the virtual machine.
             *
             * @param networkInterface an existing network interface
             * @return the stage representing creatable VM definition
             */
            WithCreate withExistingSecondaryNetworkInterface(NetworkInterface networkInterface);
        }

        /**
         * The stage of the virtual machine definition allowing to specify extensions.
         */
        interface WithExtension {
            /**
             * Specifies definition of an extension to be attached to the virtual machine.
             *
             * @param name the reference name for the extension
             * @return the stage representing configuration for the extension
             */
            VirtualMachineExtension.DefinitionStages.Blank<WithCreate> defineNewExtension(String name);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<VirtualMachine>,
                Resource.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithPassword,
                DefinitionStages.WithOsDiskSettings,
                DefinitionStages.WithVMSize,
                DefinitionStages.WithStorageAccount,
                DefinitionStages.WithDataDisk,
                DefinitionStages.WithAvailabilitySet,
                DefinitionStages.WithSecondaryNetworkInterface,
                DefinitionStages.WithExtension {
        }
    }

    /**
     * Grouping of virtual machine update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the virtual machine definition allowing to specify data disk configuration.
         */
        interface WithDataDisk {
            /**
             * Specifies that a new blank data disk needs to be attached to virtual machine.
             *
             * @param sizeInGB the disk size in GB
             * @return the stage representing creatable VM definition
             */
            Update withNewDataDisk(Integer sizeInGB);

            /**
             * Specifies an existing VHD that needs to be attached to the virtual machine as data disk.
             *
             * @param storageAccountName the storage account name
             * @param containerName the name of the container holding the VHD file
             * @param vhdName the name for the VHD file
             * @return the stage representing creatable VM definition
             */
            Update withExistingDataDisk(String storageAccountName, String containerName, String vhdName);

            /**
             * Specifies a new blank data disk to be attached to the virtual machine along with it's configuration.
             *
             * @param name the name for the data disk
             * @return the stage representing configuration for the data disk
             */
            VirtualMachineDataDisk.UpdateDefinitionStages.AttachNewDataDisk<Update> defineNewDataDisk(String name);

            /**
             * Specifies an existing VHD that needs to be attached to the virtual machine as data disk along with
             * it's configuration.
             *
             * @param name the name for the data disk
             * @return the stage representing configuration for the data disk
             */
            VirtualMachineDataDisk
                    .UpdateDefinitionStages
                    .AttachExistingDataDisk<Update> defineExistingDataDisk(String name);

            /**
             * Begins the description of an update of an existing data disk of this virtual machine.
             *
             * @param name the name of the disk
             * @return the stage representing updating configuration for  data disk
             */
            VirtualMachineDataDisk.Update updateDataDisk(String name);

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
        }

        /**
         * The stage of virtual machine definition allowing to specify additional network interfaces.
         */
        interface WithSecondaryNetworkInterface {
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
            Update withNewSecondaryNetworkInterface(Creatable<NetworkInterface> creatable);

            /**
             * Associate an existing network interface with the virtual machine.
             *
             * Note this method's effect is additive, i.e. each time it is used, the new secondary
             * network interface added to the virtual machine.
             *
             * @param networkInterface an existing network interface
             * @return the stage representing creatable VM definition
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

        /**
         * The stage of the virtual machine definition allowing to specify extensions.
         */
        interface WithExtension {
            /**
             * Specifies definition of an extension to be attached to the virtual machine.
             *
             * @param name the reference name for the extension
             * @return the stage representing configuration for the extension
             */
            VirtualMachineExtension
                    .UpdateDefinitionStages
                    .Blank<Update> defineNewExtension(String name);

            /**
             * Begins the description of an update of an existing extension of this virtual machine.
             *
             * @param name the reference name for the extension
             * @return the stage representing updatable VM definition
             */
            VirtualMachineExtension.Update updateExtension(String name);

            /**
             * Detaches an extension with the given name from the virtual machine.
             *
             * @param name the reference name for the extension to be removed/uninstalled
             * @return the stage representing updatable VM definition
             */
            Update withoutExtension(String name);
        }
    }

    /**
     * The template for an update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<VirtualMachine>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithDataDisk,
            UpdateStages.WithSecondaryNetworkInterface,
            UpdateStages.WithExtension {
        /**
         * Specifies the caching type for the Operating System disk.
         *
         * @param cachingType the caching type.
         * @return the stage representing updatable VM definition
         */
        Update withOsDiskCaching(CachingTypes cachingType);

        /**
         * Specifies the size of the OSDisk in GB.
         *
         * @param size the VHD size.
         * @return the stage representing updatable VM definition
         */
        Update withOsDiskSizeInGb(Integer size);

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
    }
}