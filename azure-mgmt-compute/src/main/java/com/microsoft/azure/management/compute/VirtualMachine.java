/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.compute.implementation.VirtualMachineInner;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.model.HasNetworkInterfaces;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.storage.StorageAccount;

import java.util.Map;

/**
 * An immutable client-side representation of an Azure virtual machine.
 */
@Fluent
public interface VirtualMachine extends
        GroupableResource<ComputeManager>,
        Refreshable<VirtualMachine>,
        Wrapper<VirtualMachineInner>,
        Updatable<VirtualMachine.Update>,
        HasNetworkInterfaces {
    // Actions
    /**
     * Shuts down the Virtual Machine and releases the compute resources.
     * <p>
     * You are not billed for the compute resources that this Virtual Machine uses
     */
    void deallocate();

    /**
     * Generalize the Virtual Machine.
     */
    void generalize();

    /**
     * Power off (stop) the virtual machine.
     * <p>
     * You will be billed for the compute resources that this Virtual Machine uses.
     */
    void powerOff();

    /**
     * Restart the virtual machine.
     */
    void restart();

    /**
     * Start the virtual machine.
     */
    void start();

    /**
     * Redeploy the virtual machine.
     */
    void redeploy();

    /**
     * Convert (migrate) the virtual machine with un-managed disks to use managed disk.
     */
    void convertToManaged();

    /**
     * List of all available virtual machine sizes this virtual machine can resized to.
     *
     * @return the virtual machine sizes
     */
    @Method
    PagedList<VirtualMachineSize> availableSizes();

    /**
     * Captures the virtual machine by copying virtual hard disks of the VM and returns template as json
     * string that can be used to create similar VMs.
     *
     * @param containerName destination container name to store the captured Vhd
     * @param vhdPrefix the prefix for the vhd holding captured image
     * @param overwriteVhd whether to overwrites destination vhd if it exists
     * @return the template as json string
     */
    String capture(String containerName, String vhdPrefix, boolean overwriteVhd);

    /**
     * Refreshes the virtual machine instance view to sync with Azure.
     * <p>
     * this will caches the instance view which can be later retrieved using {@link VirtualMachine#instanceView()}.
     *
     * @return the refreshed instance view
     */
    @Method
    VirtualMachineInstanceView refreshInstanceView();

    // Getters
    //

    /**
     * @return true if managed disk is used for the virtual machine's disks (os, data)
     */
    boolean isManagedDiskEnabled();

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
    String osUnmanagedDiskVhdUri();

    /**
     * @return the operating system disk caching type, valid values are 'None', 'ReadOnly', 'ReadWrite'
     */
    CachingTypes osDiskCachingType();

    /**
     * @return the size of the operating system disk in GB
     */
    int osDiskSize();

    /**
     * @return the storage account type of the managed disk backing Os disk
     */
    StorageAccountTypes osDiskStorageAccountType();

    /**
     * @return resource id of the managed disk backing OS disk
     */
    String osDiskId();

    /**
     * @return the unmanaged data disks associated with this virtual machine, indexed by lun
     */
    Map<Integer, VirtualMachineUnmanagedDataDisk> unmanagedDataDisks();

    /**
     * @return the managed data disks associated with this virtual machine, indexed by lun
     */
    Map<Integer, VirtualMachineDataDisk> dataDisks();

    /**
     * Gets the public IP address associated with this virtual machine's primary network interface.
     * <p>
     * note that this method makes a rest API call to fetch the resource.
     *
     * @return the public IP of the primary network interface
     */
    PublicIpAddress getPrimaryPublicIpAddress();

    /**
     * @return the resource ID of the public IP address associated with this virtual machine's primary network interface
     */
    String getPrimaryPublicIpAddressId();

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
     */
    VirtualMachineInstanceView instanceView();

    // Setters
    //

    /**
     * The virtual machine scale set stages shared between managed and unmanaged based
     * virtual machine definitions.
     */
    interface DefinitionShared extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithNetwork,
            DefinitionStages.WithSubnet,
            DefinitionStages.WithPrivateIp,
            DefinitionStages.WithPublicIpAddress,
            DefinitionStages.WithPrimaryNetworkInterface,
            DefinitionStages.WithOS,
            DefinitionStages.WithCreate {
    }

    /**
     * The entirety of the virtual machine definition.
     */
    interface DefinitionManagedOrUnmanaged extends
            DefinitionShared,
            DefinitionStages.WithLinuxRootUsernameManagedOrUnmanaged,
            DefinitionStages.WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged,
            DefinitionStages.WithWindowsAdminUsernameManagedOrUnmanaged,
            DefinitionStages.WithWindowsAdminPasswordManagedOrUnmanaged,
            DefinitionStages.WithFromImageCreateOptionsManagedOrUnmanaged,
            DefinitionStages.WithLinuxCreateManagedOrUnmanaged,
            DefinitionStages.WithWindowsCreateManagedOrUnmanaged,
            DefinitionStages.WithManagedCreate,
            DefinitionStages.WithUnmanagedCreate {
    }

    /**
     * The entirety of the managed disk based virtual machine definition.
     */
    interface DefinitionManaged extends
            DefinitionShared,
            DefinitionStages.WithLinuxRootUsernameManaged,
            DefinitionStages.WithLinuxRootPasswordOrPublicKeyManaged,
            DefinitionStages.WithWindowsAdminUsernameManaged,
            DefinitionStages.WithWindowsAdminPasswordManaged,
            DefinitionStages.WithFromImageCreateOptionsManaged,
            DefinitionStages.WithLinuxCreateManaged,
            DefinitionStages.WithWindowsCreateManaged,
            DefinitionStages.WithManagedCreate {
    }

    /**
     * The entirety of the unmanaged disk based virtual machine definition.
     */
    interface DefinitionUnmanaged extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithNetwork,
            DefinitionStages.WithSubnet,
            DefinitionStages.WithPrivateIp,
            DefinitionStages.WithPublicIpAddress,
            DefinitionStages.WithPrimaryNetworkInterface,
            DefinitionStages.WithOS,
            DefinitionStages.WithLinuxRootUsernameUnmanaged,
            DefinitionStages.WithLinuxRootPasswordOrPublicKeyUnmanaged,
            DefinitionStages.WithWindowsAdminUsernameUnmanaged,
            DefinitionStages.WithWindowsAdminPasswordUnmanaged,
            DefinitionStages.WithFromImageCreateOptionsUnmanaged,
            DefinitionStages.WithLinuxCreateUnmanaged,
            DefinitionStages.WithWindowsCreateUnmanaged,
            DefinitionStages.WithUnmanagedCreate {
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
            WithWindowsAdminUsernameManagedOrUnmanaged withPopularWindowsImage(KnownWindowsVirtualMachineImage knownImage);

            /**
             * Specifies that the latest version of a marketplace Windows image needs to be used.
             *
             * @param publisher specifies the publisher of the image
             * @param offer specifies the offer of the image
             * @param sku specifies the SKU of the image
             * @return the next stage of the virtual machine definition
             */
            WithWindowsAdminUsernameManagedOrUnmanaged withLatestWindowsImage(String publisher, String offer, String sku);

            /**
             * Specifies the version of a marketplace Windows image needs to be used.
             *
             * @param imageReference describes publisher, offer, sku and version of the market-place image
             * @return the next stage of the virtual machine definition
             */
            WithWindowsAdminUsernameManagedOrUnmanaged withSpecificWindowsImageVersion(ImageReference imageReference);

            /**
             * Specifies the id of a Windows custom image to be used.
             *
             * @param customImageId the resource id of the custom image
             * @return the next stage of the virtual machine definition
             */
            WithWindowsAdminUsernameManaged withWindowsCustomImage(String customImageId);

            /**
             * Specifies the user (generalized) Windows image used for the virtual machine's OS.
             *
             * @param imageUrl the url the the VHD
             * @return the next stage of the virtual machine definition
             */
            WithWindowsAdminUsernameUnmanaged withStoredWindowsImage(String imageUrl);

            /**
             * Specifies the known marketplace Linux image used for the virtual machine's OS.
             *
             * @param knownImage enum value indicating known market-place image
             * @return the next stage of the virtual machine definition
             */
            WithLinuxRootUsernameManagedOrUnmanaged withPopularLinuxImage(KnownLinuxVirtualMachineImage knownImage);

            /**
             * Specifies that the latest version of a marketplace Linux image needs to be used.
             *
             * @param publisher specifies the publisher of the image
             * @param offer specifies the offer of the image
             * @param sku specifies the SKU of the image
             * @return the next stage of the virtual machine definition
             */
            WithLinuxRootUsernameManagedOrUnmanaged withLatestLinuxImage(String publisher, String offer, String sku);

            /**
             * Specifies the version of a market-place Linux image needs to be used.
             *
             * @param imageReference describes publisher, offer, sku and version of the market-place image
             * @return the next stage of the virtual machine definition
             */
            WithLinuxRootUsernameManagedOrUnmanaged withSpecificLinuxImageVersion(ImageReference imageReference);

            /**
             * Specifies the id of a Linux custom image to be used.
             *
             * @param customImageId the resource id of the custom image
             * @return the next stage of the virtual machine definition
             */
            WithLinuxRootUsernameManaged withLinuxCustomImage(String customImageId);

            /**
             * Specifies the user (generalized) Linux image used for the virtual machine's OS.
             *
             * @param imageUrl the url the the VHD
             * @return the next stage of the virtual machine definition
             */
            WithLinuxRootUsernameUnmanaged withStoredLinuxImage(String imageUrl);

            /**
             * Specifies the specialized operating system unmanaged disk to be attached to the virtual machine.
             *
             * @param osDiskUrl osDiskUrl the url to the OS disk in the Azure Storage account
             * @param osType the OS type
             * @return the next stage of the Windows virtual machine definition
             */
            WithUnmanagedCreate withSpecializedOsUnmanagedDisk(String osDiskUrl, OperatingSystemTypes osType);

            /**
             * Specifies the specialized operating system managed disk to be attached to the virtual machine.
             *
             * @param disk the managed disk to attach
             * @param osType the OS type
             * @return the next stage of the Windows virtual machine definition
             */
            WithManagedCreate withSpecializedOsDisk(Disk disk, OperatingSystemTypes osType);
        }

        /**
         * The stage of the Linux virtual machine definition allowing to specify SSH root user name.
         */
        interface WithLinuxRootUsernameManagedOrUnmanaged {
            /**
             * Specifies the SSH root user name for the Linux virtual machine.
             *
             * @param rootUserName the Linux SSH root user name. This must follow the required naming convention for Linux user name
             * @return the next stage of the Linux virtual machine definition
             */
            WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged withRootUsername(String rootUserName);
        }

        /**
         * The stage of the Linux virtual machine definition allowing to specify SSH root user name.
         */
        interface WithLinuxRootUsernameManaged {
            /**
             * Specifies the SSH root user name for the Linux virtual machine.
             *
             * @param rootUserName the Linux SSH root user name. This must follow the required naming convention for Linux user name
             * @return the next stage of the Linux virtual machine definition
             */
            WithLinuxRootPasswordOrPublicKeyManaged withRootUsername(String rootUserName);
        }

        /**
         * The stage of the Linux virtual machine definition allowing to specify SSH root user name.
         */
        interface WithLinuxRootUsernameUnmanaged {
            /**
             * Specifies the SSH root user name for the Linux virtual machine.
             *
             * @param rootUserName the Linux SSH root user name. This must follow the required naming convention for Linux user name
             * @return the next stage of the Linux virtual machine definition
             */
            WithLinuxRootPasswordOrPublicKeyUnmanaged withRootUsername(String rootUserName);
        }

        /**
         * The stage of the Linux virtual machine definition allowing to specify SSH root password or public key.
         */
        interface WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged {
            /**
             * Specifies the SSH root password for the Linux virtual machine.
             *
             * @param rootPassword the SSH root password. This must follow the criteria for Azure Linux VM password.
             * @return the next stage of the Linux virtual machine definition
             */
            WithLinuxCreateManagedOrUnmanaged withRootPassword(String rootPassword);

            /**
             * Specifies the SSH public key.
             * <p>
             * Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the next stage of the Linux virtual machine definition
             */
            WithLinuxCreateManagedOrUnmanaged withSsh(String publicKey);
        }

        /**
         * The stage of the Linux virtual machine definition allowing to specify SSH root password or public key.
         */
        interface WithLinuxRootPasswordOrPublicKeyManaged {
            /**
             * Specifies the SSH root password for the Linux virtual machine.
             *
             * @param rootPassword the SSH root password. This must follow the criteria for Azure Linux VM password.
             * @return the next stage of the Linux virtual machine definition
             */
            WithLinuxCreateManaged withRootPassword(String rootPassword);

            /**
             * Specifies the SSH public key.
             * <p>
             * Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the next stage of the Linux virtual machine definition
             */
            WithLinuxCreateManaged withSsh(String publicKey);
        }

        /**
         * The stage of the Linux virtual machine definition allowing to specify SSH root password or public key.
         */
        interface WithLinuxRootPasswordOrPublicKeyUnmanaged {
            /**
             * Specifies the SSH root password for the Linux virtual machine.
             *
             * @param rootPassword the SSH root password. This must follow the criteria for Azure Linux VM password.
             * @return the next stage of the Linux virtual machine definition
             */
            WithLinuxCreateUnmanaged withRootPassword(String rootPassword);

            /**
             * Specifies the SSH public key.
             * <p>
             * Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the next stage of the Linux virtual machine definition
             */
            WithLinuxCreateUnmanaged withSsh(String publicKey);
        }

        /**
         * The stage of the Windows virtual machine definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminUsernameManagedOrUnmanaged {
            /**
             * Specifies the administrator user name for the Windows virtual machine.
             *
             * @param adminUserName the Windows administrator user name. This must follow the required naming convention for Windows user name.
             * @return the stage representing creatable Linux VM definition
             */
            WithWindowsAdminPasswordManagedOrUnmanaged withAdminUsername(String adminUserName);
        }

        /**
         * The stage of the Windows virtual machine definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminUsernameManaged {
            /**
             * Specifies the administrator user name for the Windows virtual machine.
             *
             * @param adminUserName the Windows administrator user name. This must follow the required naming convention for Windows user name.
             * @return the stage representing creatable Linux VM definition
             */
            WithWindowsAdminPasswordManaged withAdminUsername(String adminUserName);
        }

        /**
         * The stage of the Windows virtual machine definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminUsernameUnmanaged {
            /**
             * Specifies the administrator user name for the Windows virtual machine.
             *
             * @param adminUserName the Windows administrator user name. This must follow the required naming convention for Windows user name.
             * @return the stage representing creatable Linux VM definition
             */
            WithWindowsAdminPasswordUnmanaged withAdminUsername(String adminUserName);
        }

        /**
         * The stage of the Windows virtual machine definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminPasswordManagedOrUnmanaged {
            /**
             * Specifies the administrator password for the Windows virtual machine.
             *
             * @param adminPassword the administrator password. This must follow the criteria for Azure Windows VM password.
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsCreateManagedOrUnmanaged withAdminPassword(String adminPassword);
        }

        /**
         * The stage of the Windows virtual machine definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminPasswordManaged {
            /**
             * Specifies the administrator password for the Windows virtual machine.
             *
             * @param adminPassword the administrator password. This must follow the criteria for Azure Windows VM password.
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsCreateManaged withAdminPassword(String adminPassword);
        }

        /**
         * The stage of the Windows virtual machine definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminPasswordUnmanaged {
            /**
             * Specifies the administrator password for the Windows virtual machine.
             *
             * @param adminPassword the administrator password. This must follow the criteria for Azure Windows VM password.
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsCreateUnmanaged withAdminPassword(String adminPassword);
        }

        /**
         * The stages contains OS agnostics settings when virtual machine is created from image.
         */
        interface WithFromImageCreateOptionsManagedOrUnmanaged extends WithFromImageCreateOptionsManaged {
            /**
             * @return the next stage of a unmanaged disk based virtual machine definition
             */
            WithFromImageCreateOptionsUnmanaged withUnmanagedDisks();
        }

        /**
         * The stages contains OS agnostics settings when virtual machine is created from image.
         */
        interface WithFromImageCreateOptionsManaged extends WithManagedCreate {
            /**
             * Specifies the custom data for the virtual machine.
             *
             * @param base64EncodedCustomData the base64 encoded custom data
             * @return the stage representing creatable Windows VM definition
             */
            WithFromImageCreateOptionsManaged withCustomData(String base64EncodedCustomData);

            /**
             * Specifies the computer name for the virtual machine.
             *
             * @param computerName the computer name
             * @return the stage representing creatable VM definition
             */
            WithFromImageCreateOptionsManaged withComputerName(String computerName);
        }

        /**
         * The stages contains OS agnostics settings when virtual machine is created from image.
         */
        interface WithFromImageCreateOptionsUnmanaged extends WithUnmanagedCreate {
            /**
             * Specifies the custom data for the virtual machine.
             *
             * @param base64EncodedCustomData the base64 encoded custom data
             * @return the stage representing creatable Windows VM definition
             */
            WithFromImageCreateOptionsUnmanaged withCustomData(String base64EncodedCustomData);

            /**
             * Specifies the computer name for the virtual machine.
             *
             * @param computerName the computer name
             * @return the stage representing creatable VM definition
             */
            WithFromImageCreateOptionsUnmanaged withComputerName(String computerName);
        }

        /**
         * The stage of the Linux virtual machine definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithLinuxCreateManagedOrUnmanaged extends WithFromImageCreateOptionsManagedOrUnmanaged {
            /**
             * Specifies the SSH public key.
             * <p>
             * Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the stage representing creatable Linux VM definition
             */
            WithLinuxCreateManagedOrUnmanaged withSsh(String publicKey);
        }

        /**
         * The stage of the Linux virtual machine definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithLinuxCreateManaged extends WithFromImageCreateOptionsManaged {
            /**
             * Specifies the SSH public key.
             * <p>
             * Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the stage representing creatable Linux VM definition
             */
            WithLinuxCreateManaged withSsh(String publicKey);
        }

        /**
         * The stage of the Linux virtual machine definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithLinuxCreateUnmanaged extends WithFromImageCreateOptionsUnmanaged {
            /**
             * Specifies the SSH public key.
             * <p>
             * Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the stage representing creatable Linux VM definition
             */
            WithLinuxCreateUnmanaged withSsh(String publicKey);
        }

        /**
         * The stage of the Windows virtual machine definition allowing to optionally choose unmanaged disk
         * or continue definition of vm based on managed disk.
         */
        interface WithWindowsCreateManagedOrUnmanaged extends WithWindowsCreateManaged {
            WithWindowsCreateUnmanaged withUnmanagedDisks();
        }

        /**
         * The stage of the Windows virtual machine definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}, but also allows
         * for any other optional settings to be specified.
         */
        interface WithWindowsCreateManaged extends WithFromImageCreateOptionsManaged {
            /**
             * Specifies that VM Agent should not be provisioned.
             *
             * @return the stage representing creatable Windows VM definition
             */
            @Method
            WithWindowsCreateManaged withoutVmAgent();

            /**
             * Specifies that automatic updates should be disabled.
             *
             * @return the stage representing creatable Windows VM definition
             */
            @Method
            WithWindowsCreateManaged withoutAutoUpdate();

            /**
             * Specifies the time-zone.
             *
             * @param timeZone the timezone
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsCreateManaged withTimeZone(String timeZone);

            /**
             * Specifies the WINRM listener.
             * <p>
             * Each call to this method adds the given listener to the list of VM's WinRM listeners.
             *
             * @param listener the WinRmListener
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsCreateManaged withWinRm(WinRMListener listener);
        }

        /**
         * The stage of the Windows virtual machine definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}, but also allows
         * for any other optional settings to be specified.
         */
        interface WithWindowsCreateUnmanaged extends WithFromImageCreateOptionsUnmanaged {
            /**
             * Specifies that VM Agent should not be provisioned.
             *
             * @return the stage representing creatable Windows VM definition
             */
            @Method
            WithWindowsCreateUnmanaged withoutVmAgent();

            /**
             * Specifies that automatic updates should be disabled.
             *
             * @return the stage representing creatable Windows VM definition
             */
            @Method
            WithWindowsCreateUnmanaged withoutAutoUpdate();

            /**
             * Specifies the time-zone.
             *
             * @param timeZone the timezone
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsCreateUnmanaged withTimeZone(String timeZone);

            /**
             * Specifies the WINRM listener.
             * <p>
             * Each call to this method adds the given listener to the list of VM's WinRM listeners.
             *
             * @param listener the WinRmListener
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsCreateUnmanaged withWinRm(WinRMListener listener);
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
            WithCreate withOSDiskCaching(CachingTypes cachingType);

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
            WithCreate withOSDiskSizeInGB(Integer size);

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
         * The stage of the virtual machine definition allowing to specify unmanaged data disk.
         */
        interface WithUnmanagedDataDisk {
            /**
             * Specifies that a new blank unmanaged data disk needs to be attached to virtual machine.
             *
             * @param sizeInGB the disk size in GB
             * @return the stage representing creatable VM definition
             */
            WithUnmanagedCreate withNewUnmanagedDataDisk(Integer sizeInGB);

            /**
             * Specifies an existing unmanaged VHD that needs to be attached to the virtual machine as data disk.
             *
             * @param storageAccountName the storage account name
             * @param containerName the name of the container holding the VHD file
             * @param vhdName the name for the VHD file
             * @return the stage representing creatable VM definition
             */
            WithUnmanagedCreate withExistingUnmanagedDataDisk(String storageAccountName, String containerName, String vhdName);

            /**
             * Begins definition of a unmanaged data disk to be attached to the virtual machine.
             *
             * @param name the name for the data disk
             * @return the stage representing configuration for the unmanaged data disk
             */
            VirtualMachineUnmanagedDataDisk.DefinitionStages.Blank<WithUnmanagedCreate> defineUnmanagedDataDisk(String name);
        }

        /**
         * The stage of the virtual machine definition allowing to specify managed data disk.
         */
        interface WithManagedDataDisk {
            /**
             * Specifies that a managed disk needs to be created explicitly with the given definition and
             * attach to the virtual machine as data disk.
             *
             * @param creatable the creatable disk
             * @return the next stage of virtual machine definition
             */
            WithManagedCreate withNewDataDisk(Creatable<Disk> creatable);

            /**
             * Specifies that a managed disk needs to be created explicitly with the given definition and
             * attach to the virtual machine as data disk.
             *
             * @param creatable the creatable disk
             * @param lun the data disk lun
             * @param cachingType the data disk caching type
             * @return the next stage of virtual machine definition
             */
            WithManagedCreate withNewDataDisk(Creatable<Disk> creatable,
                                              int lun,
                                              CachingTypes cachingType);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given size.
             *
             * @param sizeInGB the size of the managed disk
             * @return the next stage of virtual machine definition
             */
            WithManagedCreate withNewDataDisk(int sizeInGB);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given settings.
             *
             * @param sizeInGB the size of the managed disk
             * @param lun the disk lun
             * @param cachingType the caching type
             * @return the next stage of virtual machine definition
             */
            WithManagedCreate withNewDataDisk(int sizeInGB, int lun, CachingTypes cachingType);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given settings.
             *
             * @param sizeInGB the size of the managed disk
             * @param lun the disk lun
             * @param cachingType the caching type
             * @param storageAccountType the storage account type
             * @return the next stage of virtual machine definition
             */
            WithManagedCreate withNewDataDisk(int sizeInGB,
                                              int lun,
                                              CachingTypes cachingType,
                                              StorageAccountTypes storageAccountType);

            /**
             * Specifies an existing source managed disk.
             *
             * @param disk the managed disk
             * @return the next stage of virtual machine definition
             */
            WithManagedCreate withExistingDataDisk(Disk disk);

            /**
             * Specifies an existing source managed disk and settings.
             *
             * @param disk the managed disk
             * @param lun the disk lun
             * @return the next stage of virtual machine definition
             */
            WithManagedCreate withExistingDataDisk(Disk disk,
                                                   int lun,
                                                   CachingTypes cachingType);

            /**
             * Specifies an existing source managed disk and settings.
             *
             * @param disk the managed disk
             * @param newSizeInGB the disk resize size in GB
             * @param lun the disk lun
             * @return the next stage of virtual machine definition
             */
            WithManagedCreate withExistingDataDisk(Disk disk,
                                        int newSizeInGB,
                                        int lun,
                                        CachingTypes cachingType);

            /**
             * Specifies the data disk to be created from the data disk image in the virtual machine image.
             *
             * @param imageLun the lun of the source data disk image
             * @return the next stage of virtual machine definition
             */
            WithManagedCreate withNewDataDiskFromImage(int imageLun);

            /**
             * Specifies the data disk to be created from the data disk image in the virtual machine image.
             *
             * @param imageLun the lun of the source data disk image
             * @param newSizeInGB the new size that overrides the default size specified in the data disk image
             * @param cachingType the caching type
             * @return the next stage of virtual machine definition
             */
            WithManagedCreate withNewDataDiskFromImage(int imageLun,
                                                       int newSizeInGB,
                                                       CachingTypes cachingType);

            /**
             * Specifies the data disk to be created from the data disk image in the virtual machine image.
             *
             * @param imageLun the lun of the source data disk image
             * @param newSizeInGB the new size that overrides the default size specified in the data disk image
             * @param cachingType the caching type
             * @param storageAccountType the storage account type
             * @return the next stage of virtual machine definition
             */
            WithManagedCreate withNewDataDiskFromImage(int imageLun,
                                                       int newSizeInGB,
                                                       CachingTypes cachingType,
                                                       StorageAccountTypes storageAccountType);
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
         * The stage of the virtual machine definition allowing to specify purchase plan.
         */
        interface WithPlan {
            /**
             * Specifies the plan for the virtual machine.
             *
             * @param plan describes the purchase plan
             * @return the stage representing creatable VM definition
             */
            WithCreate withPlan(PurchasePlan plan);

            /**
             * Specifies the plan for the virtual machine.
             *
             * @param plan describes the purchase plan
             * @param promotionCode the promotion code
             * @return the stage representing creatable VM definition
             */
            WithCreate withPromotionalPlan(PurchasePlan plan, String promotionCode);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the VM to be created and optionally allow managed data disks specific settings to
         * be specified.
         */
        interface WithManagedCreate extends
                WithManagedDataDisk,
                WithCreate {
            /**
             * Specifies the storage account type for managed Os disk.
             *
             * @param accountType the storage account type
             * @return  the stage representing creatable VM definition
             */
            WithManagedCreate withOsDiskStorageAccountType(StorageAccountTypes accountType);

            /**
             * Specifies the default caching type for the managed data disks.
             *
             * @param cachingType the caching type
             * @return the stage representing creatable VM definition
             */
            WithManagedCreate withDataDiskDefaultCachingType(CachingTypes cachingType);

            /**
             * Specifies the default caching type for the managed data disks.
             *
             * @param storageAccountType the storage account type
             * @return the stage representing creatable VM definition
             */
            WithManagedCreate withDataDiskDefaultStorageAccountType(StorageAccountTypes storageAccountType);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the VM to be created and optionally allow unmanaged data disk and settings specific to
         * unmanaged os disk to be specified.
         */
        interface WithUnmanagedCreate extends
                WithUnmanagedDataDisk,
                WithCreate {
            /**
             * Specifies the name of the OS Disk Vhd file and it's parent container.
             *
             * @param containerName the name of the container in the selected storage account.
             * @param vhdName the name for the OS Disk vhd.
             * @return the stage representing creatable VM definition
             */
            WithUnmanagedCreate withOsDiskVhdLocation(String containerName, String vhdName);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<VirtualMachine>,
                Resource.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithOsDiskSettings,
                DefinitionStages.WithVMSize,
                DefinitionStages.WithStorageAccount,
                DefinitionStages.WithAvailabilitySet,
                DefinitionStages.WithSecondaryNetworkInterface,
                DefinitionStages.WithExtension,
                DefinitionStages.WithPlan {
        }
    }

    /**
     * Grouping of virtual machine update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the virtual machine definition allowing to specify unmanaged data disk configuration.
         */
        interface WithUnmanagedDataDisk {
            /**
             * Specifies that a new blank unmanaged data disk needs to be attached to virtual machine.
             *
             * @param sizeInGB the disk size in GB
             * @return the stage representing creatable VM definition
             */
            Update withNewUnmanagedDataDisk(Integer sizeInGB);

            /**
             * Specifies an existing VHD that needs to be attached to the virtual machine as data disk.
             *
             * @param storageAccountName the storage account name
             * @param containerName the name of the container holding the VHD file
             * @param vhdName the name for the VHD file
             * @return the stage representing creatable VM definition
             */
            Update withExistingUnmanagedDataDisk(String storageAccountName, String containerName, String vhdName);

            /**
             * Specifies a new blank unmanaged data disk to be attached to the virtual machine along with it's configuration.
             *
             * @param name the name for the data disk
             * @return the stage representing configuration for the data disk
             */
            VirtualMachineUnmanagedDataDisk.UpdateDefinitionStages.Blank<Update> defineUnmanagedDataDisk(String name);

            /**
             * Begins the description of an update of an existing unmanaged data disk of this virtual machine.
             *
             * @param name the name of the disk
             * @return the stage representing updating configuration for  data disk
             */
            VirtualMachineUnmanagedDataDisk.Update updateUnmanagedDataDisk(String name);

            /**
             * Detaches a unmanaged data disk with the given name from the virtual machine.
             *
             * @param name the name of the data disk to remove
             * @return the stage representing updatable VM definition
             */
            Update withoutUnmanagedDataDisk(String name);

            /**
             * Detaches a unmanaged data disk with the given logical unit number from the virtual machine.
             *
             * @param lun the logical unit number of the data disk to remove
             * @return the stage representing updatable VM definition
             */
            Update withoutUnmanagedDataDisk(int lun);
        }

        /**
         * The stage of the virtual machine update allowing to specify managed data disk.
         */
        interface WithManagedDataDisk {
            /**
             * Specifies that a managed disk needs to be created explicitly with the given definition and
             * attach to the virtual machine as data disk.
             *
             * @param creatable the creatable disk
             * @return the next stage of virtual machine update
             */
            Update withNewDataDisk(Creatable<Disk> creatable);

            /**
             * Specifies that a managed disk needs to be created explicitly with the given definition and
             * attach to the virtual machine as data disk.
             *
             * @param creatable the creatable disk
             * @param lun the data disk lun
             * @param cachingType the data disk caching type
             * @return the next stage of virtual machine update
             */
            Update withNewDataDisk(Creatable<Disk> creatable, int lun, CachingTypes cachingType);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given size.
             *
             * @param sizeInGB the size of the managed disk
             * @return the next stage of virtual machine update
             */
            Update withNewDataDisk(int sizeInGB);

            /**
             * pecifies that a managed disk needs to be created implicitly with the given settings.
             *
             * @param sizeInGB the size of the managed disk
             * @param lun the disk lun
             * @param cachingType the caching type
             * @return the next stage of virtual machine update
             */
            Update withNewDataDisk(int sizeInGB, int lun, CachingTypes cachingType);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given settings.
             *
             * @param sizeInGB the size of the managed disk
             * @param lun the disk lun
             * @param cachingType the caching type
             * @param storageAccountType the storage account type
             * @return the next stage of virtual machine update
             */
            Update withNewDataDisk(int sizeInGB,
                                   int lun,
                                   CachingTypes cachingType,
                                   StorageAccountTypes storageAccountType);

            /**
             * Specifies an existing source managed disk.
             *
             * @param disk the managed disk
             * @return the next stage of virtual machine update
             */
            Update withExistingDataDisk(Disk disk);

            /**
             * Specifies an existing source managed disk and settings.
             *
             * @param disk the managed disk
             * @param lun the disk lun
             * @return the next stage of virtual machine update
             */
            Update withExistingDataDisk(Disk disk,
                                        int lun,
                                        CachingTypes cachingType);

            /**
             * Specifies an existing source managed disk and settings.
             *
             * @param disk the managed disk
             * @param newSizeInGB the disk resize size in GB
             * @param lun the disk lun
             * @return the next stage of virtual machine update
             */
            Update withExistingDataDisk(Disk disk,
                                        int newSizeInGB,
                                        int lun,
                                        CachingTypes cachingType);

            /**
             * Detaches managed data disk with the given lun from the virtual machine.
             *
             * @param lun the disk lun
             * @return the next stage of virtual machine update
             */
            Update withoutDataDisk(int lun);

            /**
             * Updates the size of a managed data disk with the given lun.
             *
             * @param lun the disk lun
             * @param newSizeInGB the new size of the disk
             * @return the next stage of virtual machine update
             */
            Update withDataDiskUpdated(int lun, int newSizeInGB);

            /**
             * Updates the size and caching type of a managed data disk with the given lun.
             *
             * @param lun the disk lun
             * @param newSizeInGB the new size of the disk
             * @param cachingType the caching type
             * @return the next stage of virtual machine update
             */
            Update withDataDiskUpdated(int lun, int newSizeInGB, CachingTypes cachingType);

            /**
             * Updates the size, caching type and storage account type of a managed data disk with the given lun.
             * @param lun the disk lun
             * @param newSizeInGB the new size of the disk
             * @param cachingType the caching type
             * @param storageAccountType the storage account type
             * @return the next stage of virtual machine update
             */
            Update withDataDiskUpdated(int lun,
                                          int newSizeInGB,
                                          CachingTypes cachingType,
                                          StorageAccountTypes storageAccountType);
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
            UpdateStages.WithUnmanagedDataDisk,
            UpdateStages.WithManagedDataDisk,
            UpdateStages.WithSecondaryNetworkInterface,
            UpdateStages.WithExtension {
        /**
         * Specifies the default caching type for the managed data disks.
         *
         * @param cachingType the caching type
         * @return the stage representing updatable VM definition
         */
        Update withDataDiskDefaultCachingType(CachingTypes cachingType);

        /**
         * Specifies the default caching type for the managed data disks.
         *
         * @param storageAccountType the storage account type
         * @return the stage representing updatable VM definition
         */
        Update withDataDiskDefaultStorageAccountType(StorageAccountTypes storageAccountType);

        /**
         * Specifies the caching type for the Operating System disk.
         *
         * @param cachingType the caching type.
         * @return the stage representing updatable VM definition
         */
        Update withOSDiskCaching(CachingTypes cachingType);

        /**
         * Specifies the size of the OSDisk in GB.
         *
         * @param size the disk size.
         * @return the stage representing updatable VM definition
         */
        Update withOSDiskSizeInGB(Integer size);

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