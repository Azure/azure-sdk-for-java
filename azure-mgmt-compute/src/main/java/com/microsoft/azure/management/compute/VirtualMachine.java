/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.compute.implementation.VirtualMachineInner;
import com.microsoft.azure.management.graphrbac.BuiltInRole;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.model.HasNetworkInterfaces;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;

import java.util.Map;

/**
 * An immutable client-side representation of an Azure virtual machine.
 */
@Fluent
public interface VirtualMachine extends
        GroupableResource<ComputeManager, VirtualMachineInner>,
        Refreshable<VirtualMachine>,
        Updatable<VirtualMachine.Update>,
        HasNetworkInterfaces {
    // Actions
    /**
     * Shuts down the virtual machine and releases the compute resources.
     */
    void deallocate();

    /**
     * Shuts down the virtual machine and releases the compute resources asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Completable deallocateAsync();

    /**
     * Shuts down the virtual machine and releases the compute resources asynchronously.
     *
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> deallocateAsync(ServiceCallback<Void> callback);

    /**
     * Generalizes the virtual machine.
     */
    void generalize();

    /**
     * Generalizes the virtual machine asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Completable generalizeAsync();

    /**
     * Generalizes the virtual machine asynchronously.
     *
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> generalizeAsync(ServiceCallback<Void> callback);

    /**
     * Powers off (stops) the virtual machine.
     */
    void powerOff();

    /**
     * Powers off (stops) the virtual machine asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Completable powerOffAsync();

    /**
     * Powers off (stop) the virtual machine asynchronously.
     *
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> powerOffAsync(ServiceCallback<Void> callback);

    /**
     * Restarts the virtual machine.
     */
    void restart();

    /**
     * Restarts the virtual machine asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Completable restartAsync();

    /**
     * Restarts the virtual machine asynchronously.
     *
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> restartAsync(ServiceCallback<Void> callback);

    /**
     * Starts the virtual machine.
     */
    void start();

    /**
     * Starts the virtual machine asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Completable startAsync();

    /**
     * Starts the virtual machine asynchronously.
     *
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> startAsync(ServiceCallback<Void> callback);

    /**
     * Redeploys the virtual machine.
     */
    void redeploy();

    /**
     * Redeploys the virtual machine asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Completable redeployAsync();

    /**
     * Redeploys the virtual machine asynchronously.
     *
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> redeployAsync(ServiceCallback<Void> callback);

    /**
     * @return entry point to enabling, disabling and querying disk encryption
     */
    VirtualMachineEncryption diskEncryption();

    /**
     * Converts (migrates) the virtual machine with un-managed disks to use managed disk.
     */
    void convertToManaged();

    /**
     * Converts (migrates) the virtual machine with un-managed disks to use managed disk asynchronously.
     *
     *  @return a representation of the deferred computation of this call
     */
    @Beta(Beta.SinceVersion.V1_2_0)
    Completable convertToManagedAsync();

    /**
     * Converts (migrates) the virtual machine with un-managed disks to use managed disk asynchronously.
     *
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    @Beta(Beta.SinceVersion.V1_2_0)
    ServiceFuture<Void> convertToManagedAsync(ServiceCallback<Void> callback);

    /**
     * Lists all available virtual machine sizes this virtual machine can resized to.
     *
     * @return the virtual machine sizes
     */
    @Method
    PagedList<VirtualMachineSize> availableSizes();

    /**
     * Captures the virtual machine by copying virtual hard disks of the VM.
     *
     * @param containerName destination container name to store the captured VHD
     * @param vhdPrefix the prefix for the VHD holding captured image
     * @param overwriteVhd whether to overwrites destination VHD if it exists
     * @return the JSON template for creating more such virtual machines
     */
    String capture(String containerName, String vhdPrefix, boolean overwriteVhd);

    /**
     * Captures the virtual machine by copying virtual hard disks of the VM asynchronously.
     *
     * @param containerName destination container name to store the captured VHD
     * @param vhdPrefix the prefix for the VHD holding captured image
     * @param overwriteVhd whether to overwrites destination VHD if it exists
     * @return a representation of the deferred computation of this call
     */
    @Beta(Beta.SinceVersion.V1_2_0)
    Observable<String> captureAsync(String containerName, String vhdPrefix, boolean overwriteVhd);

    /**
     * Captures the virtual machine by copying virtual hard disks of the VM asynchronously.
     *
     * @param containerName destination container name to store the captured VHD
     * @param vhdPrefix the prefix for the VHD holding captured image
     * @param overwriteVhd whether to overwrites destination VHD if it exists
     * @param callback the callback to call on success or failure
     * @return a representation of the deferred computation of this call
     */
    @Beta(Beta.SinceVersion.V1_2_0)
    ServiceFuture<String> captureAsync(String containerName, String vhdPrefix, boolean overwriteVhd, ServiceCallback<String> callback);

    /**
     * Refreshes the virtual machine instance view to sync with Azure.
     * <p>
     * The instance view will be cached for later retrieval using <code>instanceView</code>.
     *
     * @return the refreshed instance view
     */
    @Method
    VirtualMachineInstanceView refreshInstanceView();

    /**
     * Refreshes the virtual machine instance view to sync with Azure.
     *
     * @return an observable that emits the instance view of the virtual machine.
     */
    @Method
    Observable<VirtualMachineInstanceView> refreshInstanceViewAsync();

    // Getters
    //

    /**
     * @return true if managed disks are used for the virtual machine's disks (OS, data)
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
     * @return the URI to the VHD file backing this virtual machine's operating system disk
     */
    String osUnmanagedDiskVhdUri();

    /**
     * @return the operating system disk caching type
     */
    CachingTypes osDiskCachingType();

    /**
     * @return the size of the operating system disk in GB
     */
    int osDiskSize();

    /**
     * @return the storage account type of the managed disk backing OS disk
     */
    StorageAccountTypes osDiskStorageAccountType();

    /**
     * @return resource ID of the managed disk backing the OS disk
     */
    String osDiskId();

    /**
     * @return the unmanaged data disks associated with this virtual machine, indexed by LUN number
     */
    Map<Integer, VirtualMachineUnmanagedDataDisk> unmanagedDataDisks();

    /**
     * @return the managed data disks associated with this virtual machine, indexed by LUN
     */
    Map<Integer, VirtualMachineDataDisk> dataDisks();

    /**
     * Gets the public IP address associated with this virtual machine's primary network interface.
     * <p>
     * Note that this method makes a rest API call to fetch the resource.
     *
     * @return the public IP of the primary network interface
     */
    PublicIPAddress getPrimaryPublicIPAddress();

    /**
     * @return the resource ID of the public IP address associated with this virtual machine's primary network interface
     */
    String getPrimaryPublicIPAddressId();

    /**
     * @return the resource ID of the availability set associated with this virtual machine
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
     * @return a representation of the deferred computation of this call, returning extensions attached to the virtual machine
     */
    Observable<VirtualMachineExtension> listExtensionsAsync();

    /**
     * @return extensions attached to the virtual machine
     */
    Map<String, VirtualMachineExtension> listExtensions();

    /**
     * @return the plan value
     */
    Plan plan();

    /**
     * Returns the storage profile of an Azure virtual machine.
     *
     * @return the storageProfile value
     */
    StorageProfile storageProfile();

    /**
     * @return the operating system profile
     */
    OSProfile osProfile();

    /**
     * @return the diagnostics profile
     */
    DiagnosticsProfile diagnosticsProfile();

    /**
     * @return the virtual machine unique ID.
     */
    String vmId();

    /**
     * @return the power state of the virtual machine
     */
    PowerState powerState();

    /**
     * Get the virtual machine instance view.
     * <p>
     * The instance view will be cached for later retrieval using <code>instanceView</code>.
     *
     * @return the virtual machine's instance view
     */
    VirtualMachineInstanceView instanceView();

    /**
     * @return true if boot diagnostics is enabled for the virtual machine
     */
    @Beta(Beta.SinceVersion.V1_2_0)
    boolean isBootDiagnosticsEnabled();

    /**
     * @return the storage blob endpoint uri if boot diagnostics is enabled for the virtual machine
     */
    @Beta(Beta.SinceVersion.V1_2_0)
    String bootDiagnosticsStorageUri();

    /**
     * @return true if Managed Service Identity is enabled for the virtual machine
     */
    @Beta(Beta.SinceVersion.V1_2_0)
    boolean isManagedServiceIdentityEnabled();

    /**
     * @return the Managed Service Identity specific Active Directory tenant ID assigned to the
     * virtual machine.
     */
    @Beta(Beta.SinceVersion.V1_2_0)
    String managedServiceIdentityTenantId();

    /**
     * @return the Managed Service Identity specific Active Directory service principal ID assigned
     * to the virtual machine.
     */
    @Beta(Beta.SinceVersion.V1_2_0)
    String managedServiceIdentityPrincipalId();

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
            DefinitionStages.WithPrivateIP,
            DefinitionStages.WithPublicIPAddress,
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
            DefinitionStages.WithPrivateIP,
            DefinitionStages.WithPublicIPAddress,
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
         * The stage of a virtual machine definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithNetwork> {
        }

        /**
         * The stage of a virtual machine definition allowing to specify a virtual network with the new primary network interface.
         */
        interface WithNetwork extends WithPrimaryNetworkInterface {
            /**
             * Creates a new virtual network to associate with the virtual machine's primary network interface, based on
             * the provided definition.
             *
             * @param creatable a creatable definition for a new virtual network
             * @return the next stage of the definition
             */
            WithPrivateIP withNewPrimaryNetwork(Creatable<Network> creatable);

            /**
             * Creates a new virtual network to associate with the virtual machine's primary network interface.
             * <p>
             * The virtual network will be created in the same resource group and region as of virtual machine, it will be
             * created with the specified address space and a default subnet covering the entirety of the network IP address space.
             *
             * @param addressSpace the address space for the virtual network
             * @return the next stage of the definition
             */
            WithPrivateIP withNewPrimaryNetwork(String addressSpace);

            /**
             * Associates an existing virtual network with the virtual machine's primary network interface.
             *
             * @param network an existing virtual network
             * @return the next stage of the definition
             */
            WithSubnet withExistingPrimaryNetwork(Network network);
        }

        /**
         * The stage of a virtual machine definition allowing to specify the virtual network subnet for a new primary network interface.
         *
         */
        interface WithSubnet {
            /**
             * Associates a subnet with the virtual machine's primary network interface.
             *
             * @param name the subnet name
             * @return the next stage of the definition
             */
            WithPrivateIP withSubnet(String name);
        }

        /**
         * The stage of a virtual machine definition allowing to specify a private IP address within a virtual network subnet.
         */
        interface WithPrivateIP {
            /**
             * Enables dynamic private IP address allocation within the specified existing virtual network subnet for
             * the VM's primary network interface.
             *
             * @return the next stage of the definition
             */
            WithPublicIPAddress withPrimaryPrivateIPAddressDynamic();

            /**
             * Assigns the specified static private IP address within the specified existing virtual network subnet to the
             * VM's primary network interface.
             *
             * @param staticPrivateIPAddress a static IP address within the specified subnet
             * @return the next stage of the definition
             */
            WithPublicIPAddress withPrimaryPrivateIPAddressStatic(String staticPrivateIPAddress);
        }

        /**
         * The stage of a virtual machine definition allowing to associate a public IP address with its primary network interface.
         */
        interface WithPublicIPAddress {
            /**
             * Creates a new public IP address to associate with the VM's primary network interface.
             *
             * @param creatable a creatable definition for a new public IP
             * @return the next stage of the definition
             */
            WithOS withNewPrimaryPublicIPAddress(Creatable<PublicIPAddress> creatable);

            /**
             * Creates a new public IP address in the same region and resource group as the resource, with the specified DNS label
             * and associates it with the VM's primary network interface.
             * <p>
             * The internal name for the public IP address will be derived from the DNS label.
             *
             * @param leafDnsLabel a leaf domain label
             * @return the next stage of the definition
             */
            WithOS withNewPrimaryPublicIPAddress(String leafDnsLabel);

            /**
             * Associates an existing public IP address with the VM's primary network interface.
             *
             * @param publicIPAddress an existing public IP address
             * @return the next stage of the definition
             */
            WithOS withExistingPrimaryPublicIPAddress(PublicIPAddress publicIPAddress);

            /**
             * Specifies that the VM should not have a public IP address.
             *
             * @return the next stage of the definition
             */
            WithOS withoutPrimaryPublicIPAddress();
        }

        /**
         * The stage of a virtual machine definition allowing to specify the primary network interface.
         */
        interface WithPrimaryNetworkInterface {
            /**
             * Creates a new network interface to associate with the virtual machine as its primary network interface,
             * based on the provided definition.
             *
             * @param creatable a creatable definition for a new network interface
             * @return the next stage of the definition
             */
            WithOS withNewPrimaryNetworkInterface(Creatable<NetworkInterface> creatable);

            /**
             * Associates an existing network interface with the virtual machine as its primary network interface.
             *
             * @param networkInterface an existing network interface
             * @return the next stage of the definition
             */
            WithOS withExistingPrimaryNetworkInterface(NetworkInterface networkInterface);
        }

        /**
         * The stage of a virtual machine definition allowing to specify the operating system image.
         */
        interface WithOS {
            /**
             * Specifies a known marketplace Windows image to be used for the virtual machine's OS.
             *
             * @param knownImage a known market-place image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManagedOrUnmanaged withPopularWindowsImage(KnownWindowsVirtualMachineImage knownImage);

            /**
             * Specifies that the latest version of a marketplace Windows image should to be used as the virtual machine's OS.
             *
             * @param publisher specifies the publisher of the image
             * @param offer specifies the offer of the image
             * @param sku specifies the SKU of the image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManagedOrUnmanaged withLatestWindowsImage(String publisher, String offer, String sku);

            /**
             * Specifies a version of a marketplace Windows image to be used as the virtual machine's OS.
             *
             * @param imageReference describes publisher, offer, SKU and version of the market-place image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManagedOrUnmanaged withSpecificWindowsImageVersion(ImageReference imageReference);

            /**
             * Specifies the resource ID of a Windows custom image to be used as the virtual machine's OS.
             *
             * @param customImageId the resource ID of the custom image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManaged withWindowsCustomImage(String customImageId);

            /**
             * Specifies the user (generalized) Windows image to be used for the virtual machine's OS.
             *
             * @param imageUrl the URL of a VHD
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameUnmanaged withStoredWindowsImage(String imageUrl);

            /**
             * Specifies a known marketplace Linux image to be used for the virtual machine's OS.
             *
             * @param knownImage a known market-place image
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameManagedOrUnmanaged withPopularLinuxImage(KnownLinuxVirtualMachineImage knownImage);

            /**
             * Specifies that the latest version of a marketplace Linux image is to be used as the virtual machine's OS.
             *
             * @param publisher specifies the publisher of an image
             * @param offer specifies an offer of the image
             * @param sku specifies a SKU of the image
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameManagedOrUnmanaged withLatestLinuxImage(String publisher, String offer, String sku);

            /**
             * Specifies a version of a market-place Linux image to be used as the virtual machine's OS.
             *
             * @param imageReference describes the publisher, offer, SKU and version of the market-place image
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameManagedOrUnmanaged withSpecificLinuxImageVersion(ImageReference imageReference);

            /**
             * Specifies the resource ID of a Linux custom image to be used as the virtual machines' OS.
             *
             * @param customImageId the resource ID of a custom image
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameManaged withLinuxCustomImage(String customImageId);

            /**
             * Specifies a user (generalized) Linux image to be used for the virtual machine's OS.
             *
             * @param imageUrl the URL of a VHD
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameUnmanaged withStoredLinuxImage(String imageUrl);

            /**
             * Specifies a specialized operating system unmanaged disk to be attached to the virtual machine.
             *
             * @param osDiskUrl osDiskUrl the URL to the OS disk in the Azure Storage account
             * @param osType the OS type
             * @return the next stage of the definition
             */
            WithUnmanagedCreate withSpecializedOSUnmanagedDisk(String osDiskUrl, OperatingSystemTypes osType);

            /**
             * Specifies a specialized operating system managed disk to be attached to the virtual machine.
             *
             * @param disk the managed disk to attach
             * @param osType the OS type
             * @return the next stage of the definition
             */
            WithManagedCreate withSpecializedOSDisk(Disk disk, OperatingSystemTypes osType);
        }

        /**
         * The stage of a Linux virtual machine definition allowing to specify an SSH root user name.
         */
        interface WithLinuxRootUsernameManagedOrUnmanaged {
            /**
             * Specifies an SSH root user name for the Linux virtual machine.
             *
             * @param rootUserName a user name following the required naming convention for Linux user names
             * @return the next stage of the definition
             */
            WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged withRootUsername(String rootUserName);
        }

        /**
         * The stage of a Linux virtual machine definition allowing to specify an SSH root user name.
         */
        interface WithLinuxRootUsernameManaged {
            /**
             * Specifies an SSH root user name for the Linux virtual machine.
             *
             * @param rootUserName a user name following the required naming convention for Linux user names
             * @return the next stage of the definition
             */
            WithLinuxRootPasswordOrPublicKeyManaged withRootUsername(String rootUserName);
        }

        /**
         * The stage of a Linux virtual machine definition allowing to specify an SSH root user name.
         */
        interface WithLinuxRootUsernameUnmanaged {
            /**
             * Specifies an SSH root user name for the Linux virtual machine.
             *
             * @param rootUserName a user name following the required naming convention for Linux user names
             * @return the next stage of the definition
             */
            WithLinuxRootPasswordOrPublicKeyUnmanaged withRootUsername(String rootUserName);
        }

        /**
         * The stage of a Linux virtual machine definition allowing to specify an SSH root password or public key.
         */
        interface WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged {
            /**
             * Specifies the SSH root password for the Linux virtual machine.
             *
             * @param rootPassword a password following the complexity criteria for Azure Linux VM passwords.
             * @return the next stage of the definition
             */
            WithLinuxCreateManagedOrUnmanaged withRootPassword(String rootPassword);

            /**
             * Specifies the SSH public key.
             * @param publicKey an SSH public key in the PEM format.
             * @return the next stage of the definition
             */
            WithLinuxCreateManagedOrUnmanaged withSsh(String publicKey);
        }

        /**
         * The stage of a Linux virtual machine definition allowing to specify an SSH root password or public key.
         */
        interface WithLinuxRootPasswordOrPublicKeyManaged {
            /**
             * Specifies the SSH root password for the Linux virtual machine.
             *
             * @param rootPassword a password, following the complexity criteria for Azure Linux VM passwords.
             * @return the next stage of the definition
             */
            WithLinuxCreateManaged withRootPassword(String rootPassword);

            /**
             * Specifies an SSH public key.
             * @param publicKey an SSH public key in the PEM format.
             * @return the next stage of the definition
             */
            WithLinuxCreateManaged withSsh(String publicKey);
        }

        /**
         * The stage of a Linux virtual machine definition allowing to specify an SSH root password or public key.
         */
        interface WithLinuxRootPasswordOrPublicKeyUnmanaged {
            /**
             * Specifies an SSH root password for the Linux virtual machine.
             *
             * @param rootPassword a password following the complexity criteria for Azure Linux VM passwords.
             * @return the next stage of the definition
             */
            WithLinuxCreateUnmanaged withRootPassword(String rootPassword);

            /**
             * Specifies an SSH public key.
             * @param publicKey an SSH public key in the PEM format.
             * @return the next stage of the definition
             */
            WithLinuxCreateUnmanaged withSsh(String publicKey);
        }

        /**
         * The stage of a Windows virtual machine definition allowing to specify an administrator user name.
         */
        interface WithWindowsAdminUsernameManagedOrUnmanaged {
            /**
             * Specifies the administrator user name for the Windows virtual machine.
             *
             * @param adminUserName a user name following the required naming convention for Windows user names.
             * @return the next stage of the definition
             */
            WithWindowsAdminPasswordManagedOrUnmanaged withAdminUsername(String adminUserName);
        }

        /**
         * The stage of a Windows virtual machine definition allowing to specify an administrator user name.
         */
        interface WithWindowsAdminUsernameManaged {
            /**
             * Specifies the administrator user name for the Windows virtual machine.
             *
             * @param adminUserName a user name followinmg the required naming convention for Windows user names
             * @return the next stage of the definition
             */
            WithWindowsAdminPasswordManaged withAdminUsername(String adminUserName);
        }

        /**
         * The stage of the Windows virtual machine definition allowing to specify an administrator user name.
         */
        interface WithWindowsAdminUsernameUnmanaged {
            /**
             * Specifies the administrator user name for the Windows virtual machine.
             *
             * @param adminUserName a user name following the required naming convention for Windows user names
             * @return the next stage of the definition
             */
            WithWindowsAdminPasswordUnmanaged withAdminUsername(String adminUserName);
        }

        /**
         * The stage of a Windows virtual machine definition allowing to specify an administrator password.
         */
        interface WithWindowsAdminPasswordManagedOrUnmanaged {
            /**
             * Specifies the administrator password for the Windows virtual machine.
             *
             * @param adminPassword a password following the complexity criteria for Azure Windows VM passwords
             * @return the next stage of the definition
             */
            WithWindowsCreateManagedOrUnmanaged withAdminPassword(String adminPassword);
        }

        /**
         * The stage of a Windows virtual machine definition allowing to specify an administrator user name.
         */
        interface WithWindowsAdminPasswordManaged {
            /**
             * Specifies the administrator password for the Windows virtual machine.
             *
             * @param adminPassword a password following the complexity criteria for Azure Windows VM passwords.
             * @return the next stage of the definition
             */
            WithWindowsCreateManaged withAdminPassword(String adminPassword);
        }

        /**
         * The stage of a Windows virtual machine definition allowing to specify an administrator password.
         */
        interface WithWindowsAdminPasswordUnmanaged {
            /**
             * Specifies the administrator password for the Windows virtual machine.
             *
             * @param adminPassword a password following the criteria for Azure Windows VM passwords.
             * @return the next stage of the definition
             */
            WithWindowsCreateUnmanaged withAdminPassword(String adminPassword);
        }

        /**
         * The stage of a virtual machine definition containing various settings when virtual machine is created from image.
         */
        interface WithFromImageCreateOptionsManagedOrUnmanaged extends WithFromImageCreateOptionsManaged {
            /**
             * Specifies that unmanaged disks will be used.
             * @return the next stage of the definition
             */
            WithFromImageCreateOptionsUnmanaged withUnmanagedDisks();
        }

        /**
         * The stage of a virtual machine definition containing various settings when virtual machine is created from image.
         */
        interface WithFromImageCreateOptionsManaged extends WithManagedCreate {

            /**
             * Specifies the custom data for the virtual machine.
             *
             * @param base64EncodedCustomData the base64 encoded custom data
             * @return the next stage of the definition
             */
            WithFromImageCreateOptionsManaged withCustomData(String base64EncodedCustomData);

            /**
             * Specifies the computer name for the virtual machine.
             *
             * @param computerName a name for the computer
             * @return the next stage stage of the definition
             */
            WithFromImageCreateOptionsManaged withComputerName(String computerName);
        }

        /**
         * The stage of a virtual machine definition containing various settings when virtual machine is created from image.
         */
        interface WithFromImageCreateOptionsUnmanaged extends WithUnmanagedCreate {
            /**
             * Specifies the custom data for the virtual machine.
             *
             * @param base64EncodedCustomData base64 encoded custom data
             * @return the next stage of the definition
             */
            WithFromImageCreateOptionsUnmanaged withCustomData(String base64EncodedCustomData);

            /**
             * Specifies the computer name for the virtual machine.
             *
             * @param computerName a computer name
             * @return the next stage of the definition
             */
            WithFromImageCreateOptionsUnmanaged withComputerName(String computerName);
        }

        /**
         * The stage of the Linux virtual machine definition which contains all the minimum required inputs for
         * the resource to be created, but also allows
         * for any other optional settings to be specified.
         */
        interface WithLinuxCreateManagedOrUnmanaged extends WithFromImageCreateOptionsManagedOrUnmanaged {
            /**
             * Specifies an SSH public key.
             * @param publicKey an SSH public key in the PEM format.
             * @return the next stage of the definition
             */
            WithLinuxCreateManagedOrUnmanaged withSsh(String publicKey);
        }

        /**
         * The stage of a Linux virtual machine definition which contains all the minimum required inputs for
         * the resource to be created, but also allows for any other optional settings to be specified.
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
         * The stage of a Linux virtual machine definition which contains all the minimum required inputs for
         * the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithLinuxCreateUnmanaged extends WithFromImageCreateOptionsUnmanaged {
            /**
             * Specifies an SSH public key.
             * @param publicKey an SSH public key in the PEM format.
             * @return the next stage of the definition
             */
            WithLinuxCreateUnmanaged withSsh(String publicKey);
        }

        /**
         * The stage of the Windows virtual machine definition allowing to enable unmanaged disks
         * or continue the definition of the VM with managed disks only.
         */
        interface WithWindowsCreateManagedOrUnmanaged extends WithWindowsCreateManaged {
            /**
             * Enables unmanaged disk support on this virtual machine.
             * @return the next stage of the definition
             */
            WithWindowsCreateUnmanaged withUnmanagedDisks();
        }

        /**
         * The stage of a Windows virtual machine definition which contains all the minimum required inputs for
         * the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithWindowsCreateManaged extends WithFromImageCreateOptionsManaged {
            /**
             * Prevents the provisioning of a VM agent.
             *
             * @return the next stage of the definition
             */
            @Method
            WithWindowsCreateManaged withoutVMAgent();

            /**
             * Disables automatic updates.
             *
             * @return the next stage of the definition
             */
            @Method
            WithWindowsCreateManaged withoutAutoUpdate();

            /**
             * Specifies the time-zone.
             *
             * @param timeZone a time zone
             * @return the next stage of the definition
             */
            WithWindowsCreateManaged withTimeZone(String timeZone);

            /**
             * Specifies  WinRM listener.
             * <p>
             * Each call to this method adds the given listener to the list of the VM's WinRM listeners.
             *
             * @param listener a WinRM listener
             * @return the next stage of the definition
             */
            WithWindowsCreateManaged withWinRM(WinRMListener listener);
        }

        /**
         * The stage of the Windows virtual machine definition which contains all the minimum required inputs for
         * the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithWindowsCreateUnmanaged extends WithFromImageCreateOptionsUnmanaged {
            /**
             * Specifies that VM Agent should not be provisioned.
             *
             * @return the stage representing creatable Windows VM definition
             */
            @Method
            WithWindowsCreateUnmanaged withoutVMAgent();

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
             * @param listener the WinRMListener
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsCreateUnmanaged withWinRM(WinRMListener listener);
        }

        /**
         * The stage of a virtual machine definition allowing to specify OS disk configurations.
         */
        interface WithOSDiskSettings {
            /**
             * Specifies the caching type for the OS disk.
             *
             * @param cachingType a caching type
             * @return the next stage of the definition
             */
            WithCreate withOSDiskCaching(CachingTypes cachingType);

            /**
             * Specifies the encryption settings for the OS Disk.
             *
             * @param settings the encryption settings
             * @return the next stage of the definition
             */
            WithCreate withOSDiskEncryptionSettings(DiskEncryptionSettings settings);

            /**
             * Specifies the size of the OSDisk in GB.
             *
             * @param size the VHD size
             * @return the next stage of the definition
             */
            WithCreate withOSDiskSizeInGB(Integer size);

            /**
             * Specifies the size of the OSDisk in GB.
             *
             * @param size the VHD size
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithCreate withOSDiskSizeInGB(int size);

            /**
             * Specifies the name for the OS Disk.
             *
             * @param name an OS disk name
             * @return the next stage of the definition
             */
            WithCreate withOSDiskName(String name);
        }

        /**
         * The stage of a virtual machine definition allowing to select a VM size.
         */
        interface WithVMSize {
            /**
             * Selects the size of the virtual machine.
             *
             * @param sizeName the name of a size for the virtual machine as text
             * @return the next stage of the definition
             */
            WithCreate withSize(String sizeName);

            /**
             * Specifies the size of the virtual machine.
             *
             * @param size a size from the list of available sizes for the virtual machine
             * @return the next stage of the definition
             */
            WithCreate withSize(VirtualMachineSizeTypes size);
        }

        /**
         * The stage of a virtual machine definition allowing to add an unmanaged data disk.
         */
        interface WithUnmanagedDataDisk {
            /**
             * Attaches a new blank unmanaged data disk to the virtual machine.
             *
             * @param sizeInGB the disk size in GB
             * @return the next stage of the definition
             */
            WithUnmanagedCreate withNewUnmanagedDataDisk(Integer sizeInGB);

            /**
             * Attaches an existing unmanaged VHD as a data disk to the virtual machine.
             *
             * @param storageAccountName a storage account name
             * @param containerName the name of the container holding the VHD file
             * @param vhdName the name for the VHD file
             * @return the next stage of the definition
             */
            WithUnmanagedCreate withExistingUnmanagedDataDisk(String storageAccountName, String containerName, String vhdName);

            /**
             * Begins definition of an unmanaged data disk to be attached to the virtual machine.
             *
             * @param name the name for the data disk
             * @return the first stage of an unmanaged data disk definition
             */
            VirtualMachineUnmanagedDataDisk.DefinitionStages.Blank<WithUnmanagedCreate> defineUnmanagedDataDisk(String name);
        }

        /**
         * The stage of a virtual machine definition allowing to specify a managed data disk.
         */
        interface WithManagedDataDisk {
            /**
             * Specifies that a managed disk should be created explicitly with the given definition and
             * attached to the virtual machine as a data disk.
             *
             * @param creatable a creatable disk definition
             * @return the next stage of the definition
             */
            WithManagedCreate withNewDataDisk(Creatable<Disk> creatable);

            /**
             * Specifies that a managed disk needs to be created explicitly with the given definition and
             * attach to the virtual machine as data disk.
             *
             * @param creatable a creatable disk
             * @param lun the data disk LUN
             * @param cachingType a data disk caching type
             * @return the next stage of the definition
             */
            WithManagedCreate withNewDataDisk(Creatable<Disk> creatable,
                                              int lun,
                                              CachingTypes cachingType);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given size.
             *
             * @param sizeInGB the size of the managed disk in GB
             * @return the next stage of the definition
             */
            WithManagedCreate withNewDataDisk(int sizeInGB);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given settings.
             *
             * @param sizeInGB the size of the managed disk in GB
             * @param lun the disk LUN
             * @param cachingType the caching type
             * @return the next stage of the definition
             */
            WithManagedCreate withNewDataDisk(int sizeInGB, int lun, CachingTypes cachingType);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given settings.
             *
             * @param sizeInGB the size of the managed disk in GB
             * @param lun the disk LUN
             * @param cachingType the caching type
             * @param storageAccountType the storage account type
             * @return the next stage of the definition
             */
            WithManagedCreate withNewDataDisk(int sizeInGB,
                                              int lun,
                                              CachingTypes cachingType,
                                              StorageAccountTypes storageAccountType);

            /**
             * Associates an existing source managed disk with the virtual machine.
             *
             * @param disk an existing managed disk
             * @return the next stage of the definition
             */
            WithManagedCreate withExistingDataDisk(Disk disk);

            /**
             * Associates an existing source managed disk with the virtual machine and specifies additional settings.
             *
             * @param disk a managed disk
             * @param lun the disk LUN
             * @param cachingType a caching type
             * @return the next stage of the definition
             */
            WithManagedCreate withExistingDataDisk(Disk disk,
                                                   int lun,
                                                   CachingTypes cachingType);

            /**
             * Associates an existing source managed disk with the virtual machine and specifies additional settings.
             *
             * @param disk a managed disk
             * @param newSizeInGB the disk resize size in GB
             * @param lun the disk LUN
             * @param cachingType a caching type
             * @return the next stage of the definition
             */
            WithManagedCreate withExistingDataDisk(Disk disk,
                                                   int newSizeInGB,
                                                   int lun,
                                                   CachingTypes cachingType);

            /**
             * Specifies the data disk to be created from the data disk image in the virtual machine image.
             *
             * @param imageLun the LUN of the source data disk image
             * @return the next stage of the definition
             */
            WithManagedCreate withNewDataDiskFromImage(int imageLun);

            /**
             * Specifies the data disk to be created from the data disk image in the virtual machine image.
             *
             * @param imageLun the LUN of the source data disk image
             * @param newSizeInGB the new size that overrides the default size specified in the data disk image
             * @param cachingType a caching type
             * @return the next stage of the definition
             */
            WithManagedCreate withNewDataDiskFromImage(int imageLun,
                                                       int newSizeInGB,
                                                       CachingTypes cachingType);

            /**
             * Specifies the data disk to be created from the data disk image in the virtual machine image.
             *
             * @param imageLun the LUN of the source data disk image
             * @param newSizeInGB the new size that overrides the default size specified in the data disk image
             * @param cachingType a caching type
             * @param storageAccountType a storage account type
             * @return the next stage of the definition
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
             * Specifies the name of a new availability set to associate with the virtual machine.
             * @param name the name of an availability set
             * @return the next stage of the definition
             */
            WithCreate withNewAvailabilitySet(String name);

            /**
             * Specifies definition of a not-yet-created availability set definition
             * to associate the virtual machine with.
             * @param creatable a creatable availability set definition
             * @return the next stage of the definition
             */
            WithCreate withNewAvailabilitySet(Creatable<AvailabilitySet> creatable);

            /**
             * Specifies an existing availability set to associate with the virtual machine.
             * @param availabilitySet an existing availability set
             * @return the next stage of the definition
             */
            WithCreate withExistingAvailabilitySet(AvailabilitySet availabilitySet);
        }

        /**
         * The stage of a virtual machine definition allowing to specify a storage account.
         */
        interface WithStorageAccount {
            /**
             * Specifies the name of a new storage account to put the VM's OS and data disk VHD into.
             * <p>
             * Only an OS disk based on a marketplace image will be stored in the new storage account.
             * An OS disk based on a user image will be stored in the same storage account as the user image.
             *
             * @param name the name for a new storage account
             * @return the next stage of the definition
             */
            WithCreate withNewStorageAccount(String name);

            /**
             * Specifies the definition of a not-yet-created storage account
             * to put the VM's OS and data disk VHDs into.
             * <p>
             * Only the OS disk based on a marketplace image will be stored in the new storage account.
             * An OS disk based on a user image will be stored in the same storage account as the user image.
             *
             * @param creatable a creatable storage account definition
             * @return the next stage of the definition
             */
            WithCreate withNewStorageAccount(Creatable<StorageAccount> creatable);

            /**
             * Specifies an existing storage account to put the VM's OS and data disk VHD in.
             * <p>
             * An OS disk based on a marketplace or a user image (generalized image) will be stored in this
             * storage account.
             *
             * @param storageAccount an existing storage account
             * @return the next stage of the definition
             */
            WithCreate withExistingStorageAccount(StorageAccount storageAccount);
        }

        /**
         * The stage of a virtual machine definition allowing to specify additional network interfaces.
         */
        interface WithSecondaryNetworkInterface {
            /**
             * Creates a new network interface to associate with the virtual machine, based on the
             * provided definition.
             * <p>
             * Note this method's effect is additive, i.e. each time it is used, a new secondary
             * network interface added to the virtual machine.
             *
             * @param creatable a creatable definition for a new network interface
             * @return the next stage of the definition
             */
            WithCreate withNewSecondaryNetworkInterface(Creatable<NetworkInterface> creatable);

            /**
             * Associates an existing network interface with the virtual machine.
             * <p>
             * Note this method's effect is additive, i.e. each time it is used, the new secondary
             * network interface added to the virtual machine.
             *
             * @param networkInterface an existing network interface
             * @return the next stage of the definition
             */
            WithCreate withExistingSecondaryNetworkInterface(NetworkInterface networkInterface);
        }

        /**
         * The stage of the virtual machine definition allowing to specify extensions.
         */
        interface WithExtension {
            /**
             * Starts the definition of an extension to be attached to the virtual machine.
             *
             * @param name the reference name for the extension
             * @return the first stage stage of an extension definition
             */
            VirtualMachineExtension.DefinitionStages.Blank<WithCreate> defineNewExtension(String name);
        }

        /**
         * The stage of a virtual machine definition allowing to specify a purchase plan.
         */
        interface WithPlan {
            /**
             * Specifies the purchase plan for the virtual machine.
             *
             * @param plan a purchase plan
             * @return the next stage of the definition
             */
            WithCreate withPlan(PurchasePlan plan);

            /**
             * Specifies the purchase plan for the virtual machine.
             *
             * @param plan a purchase plan
             * @param promotionCode a promotion code
             * @return the next stage of the definition
             */
            WithCreate withPromotionalPlan(PurchasePlan plan, String promotionCode);
        }

        /**
         * The stage of the virtual machine definition allowing to enable boot diagnostics.
         */
        @Beta(Beta.SinceVersion.V1_2_0)
        interface WithBootDiagnostics {
            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine.
             *
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithCreate withBootDiagnostics();

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine.
             *
             * @param creatable the storage account to be created and used for store the boot diagnostics
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithCreate withBootDiagnostics(Creatable<StorageAccount> creatable);

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine.
             *
             * @param storageAccount an existing storage account to be uses to store the boot diagnostics
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithCreate withBootDiagnostics(StorageAccount storageAccount);

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine.
             *
             * @param storageAccountBlobEndpointUri a storage account blob endpoint to store the boot diagnostics
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithCreate withBootDiagnostics(String storageAccountBlobEndpointUri);
        }

        /**
         * The stage of the virtual machine definition allowing to enable Managed Service Identity.
         */
        @Beta(Beta.SinceVersion.V1_2_0)
        interface WithManagedServiceIdentity {
            /**
             * Specifies that Managed Service Identity needs to be enabled in the virtual machine.
             *
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrCreate withManagedServiceIdentity();

            /**
             * Specifies that Managed Service Identity needs to be enabled in the virtual machine.
             *
             * @param tokenPort the port on the virtual machine where access token is available
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrCreate withManagedServiceIdentity(int tokenPort);
        }

        /**
         * The stage of the Managed Service Identity enabled virtual machine allowing to set role
         * assignment for a scope.
         */
        @Beta(Beta.SinceVersion.V1_2_0)
        interface WithRoleAndScopeOrCreate extends WithCreate {
            /**
             * Specifies that applications running on the virtual machine requires the given access role
             * with scope of access limited to the ARM resource identified by the resource ID specified
             * in the scope parameter.
             *
             * @param scope scope of the access represented in ARM resource ID format
             * @param asRole access role to assigned to the virtual machine
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrCreate withRoleBasedAccessTo(String scope, BuiltInRole asRole);

            /**
             * Specifies that applications running on the virtual machine requires the given access role
             * with scope of access limited to the current resource group that the virtual machine
             * resides.
             *
             * @param asRole access role to assigned to the virtual machine
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrCreate withRoleBasedAccessToCurrentResourceGroup(BuiltInRole asRole);

            /**
             * Specifies that applications running on the virtual machine requires the access described
             * in the given role definition with scope of access limited to the ARM resource identified
             * by the resource ID specified in the scope parameter.
             *
             * @param scope scope of the access represented in ARM resource ID format
             * @param roleDefinitionId access role definition to assigned to the virtual machine
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrCreate withRoleDefinitionBasedAccessTo(String scope, String roleDefinitionId);

            /**
             * Specifies that applications running on the virtual machine requires the access described
             * in the given role definition with scope of access limited to the current resource group that
             * the virtual machine resides.
             *
             * @param roleDefinitionId access role definition to assigned to the virtual machine
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrCreate withRoleDefinitionBasedAccessToCurrentResourceGroup(String roleDefinitionId);
        }

        /**
         * The stage of the VM definition allowing to specify availability zone.
         */
        @Beta(Beta.SinceVersion.V1_3_0)
        interface WithAvailabilityZone {
            /**
             * Specifies the availability zone for the virtual machine.
             *
             * @param zoneId the zone identifier. The valid values are "1", "2", and "3"
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_3_0)
            WithManagedCreate withAvailabilityZone(String zoneId);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the VM using managed OS disk
         * to be created and optionally allow managed data disks specific settings to be specified.
         */
        interface WithManagedCreate extends
                WithManagedDataDisk,
                WithAvailabilityZone,
                WithCreate {
            /**
             * Specifies the storage account type for the managed OS disk.
             *
             * @param accountType storage account type
             * @return the next stage of the definition
             */
            WithManagedCreate withOSDiskStorageAccountType(StorageAccountTypes accountType);

            /**
             * Specifies the default caching type for the managed data disks.
             *
             * @param cachingType a caching type
             * @return the next stage of teh definition
             */
            WithManagedCreate withDataDiskDefaultCachingType(CachingTypes cachingType);

            /**
             * Specifies the default caching type for managed data disks.
             *
             * @param storageAccountType a storage account type
             * @return the next stage of the definition
             */
            WithManagedCreate withDataDiskDefaultStorageAccountType(StorageAccountTypes storageAccountType);
        }

        /**
         * The stage of a virtual machine definition which contains all the minimum required inputs for the VM using
         * storage account (unmanaged based OS disk to be created and optionally allow unmanaged data disk and settings
         * specific to unmanaged OS disk to be specified.
         */
        interface WithUnmanagedCreate extends
                WithUnmanagedDataDisk,
                WithCreate {
            /**
             * Specifies the name of an OS disk VHD file and its parent container.
             *
             * @param containerName the name of the container in the selected storage account
             * @param vhdName the name for the OS disk VHD.
             * @return the next stage of the definition
             */
            WithUnmanagedCreate withOSDiskVhdLocation(String containerName, String vhdName);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created, but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<VirtualMachine>,
                Resource.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithOSDiskSettings,
                DefinitionStages.WithVMSize,
                DefinitionStages.WithStorageAccount,
                DefinitionStages.WithAvailabilitySet,
                DefinitionStages.WithSecondaryNetworkInterface,
                DefinitionStages.WithExtension,
                DefinitionStages.WithPlan,
                DefinitionStages.WithBootDiagnostics,
                DefinitionStages.WithManagedServiceIdentity {
        }
    }

    /**
     * Grouping of virtual machine update stages.
     */
    interface UpdateStages {
        /**
         * The stage of a virtual machine definition allowing to specify unmanaged data disk configuration.
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
             * Begins the definition of a blank unmanaged data disk to be attached to the virtual machine along with its configuration.
             *
             * @param name the name for the data disk
             * @return the first stage of the data disk definition
             */
            VirtualMachineUnmanagedDataDisk.UpdateDefinitionStages.Blank<Update> defineUnmanagedDataDisk(String name);

            /**
             * Begins the description of an update of an existing unmanaged data disk of this virtual machine.
             *
             * @param name the name of an existing disk
             * @return the first stage of the data disk update
             */
            VirtualMachineUnmanagedDataDisk.Update updateUnmanagedDataDisk(String name);

            /**
             * Detaches an unmanaged data disk from the virtual machine.
             *
             * @param name the name of an existing data disk to remove
             * @return the next stage of the update
             */
            Update withoutUnmanagedDataDisk(String name);

            /**
             * Detaches a unmanaged data disk from the virtual machine.
             *
             * @param lun the logical unit number of the data disk to remove
             * @return the next stage of the update
             */
            Update withoutUnmanagedDataDisk(int lun);
        }

        /**
         * The stage of a virtual machine update allowing to specify a managed data disk.
         */
        interface WithManagedDataDisk {
            /**
             * Specifies that a managed disk needs to be created explicitly with the given definition and
             * attached to the virtual machine as a data disk.
             *
             * @param creatable a creatable disk definition
             * @return the next stage of the update
             */
            Update withNewDataDisk(Creatable<Disk> creatable);

            /**
             * Specifies that a managed disk needs to be created explicitly with the given definition and
             * attached to the virtual machine as a data disk.
             *
             * @param creatable a creatable disk definition
             * @param lun the data disk LUN
             * @param cachingType a data disk caching type
             * @return the next stage of the update
             */
            Update withNewDataDisk(Creatable<Disk> creatable, int lun, CachingTypes cachingType);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given size.
             *
             * @param sizeInGB the size of the managed disk
             * @return the next stage of the update
             */
            Update withNewDataDisk(int sizeInGB);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given settings.
             *
             * @param sizeInGB the size of the managed disk
             * @param lun the disk LUN
             * @param cachingType a caching type
             * @return the next stage of the update
             */
            Update withNewDataDisk(int sizeInGB, int lun, CachingTypes cachingType);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given settings.
             *
             * @param sizeInGB the size of the managed disk
             * @param lun the disk LUN
             * @param cachingType a caching type
             * @param storageAccountType a storage account type
             * @return the next stage of the update
             */
            Update withNewDataDisk(int sizeInGB,
                                   int lun,
                                   CachingTypes cachingType,
                                   StorageAccountTypes storageAccountType);

            /**
             * Associates an existing source managed disk with the VM.
             *
             * @param disk a managed disk
             * @return the next stage of the update
             */
            Update withExistingDataDisk(Disk disk);

            /**
             * Specifies an existing source managed disk and settings.
             *
             * @param disk the managed disk
             * @param lun the disk LUN
             * @param cachingType a caching type
             * @return the next stage of the update
             */
            Update withExistingDataDisk(Disk disk,
                                        int lun,
                                        CachingTypes cachingType);

            /**
             * Specifies an existing source managed disk and settings.
             *
             * @param disk a managed disk
             * @param newSizeInGB the disk resize size in GB
             * @param lun the disk LUN
             * @param cachingType a caching type
             * @return the next stage of the update
             */
            Update withExistingDataDisk(Disk disk,
                                        int newSizeInGB,
                                        int lun,
                                        CachingTypes cachingType);

            /**
             * Detaches a managed data disk with the given LUN from the virtual machine.
             *
             * @param lun the disk LUN
             * @return the next stage of the update
             */
            Update withoutDataDisk(int lun);

            /**
             * Updates the size of a managed data disk with the given LUN.
             *
             * @param lun the disk LUN
             * @param newSizeInGB the new size of the disk
             * @return the next stage of the update
             */
            // TODO: This has been disabled by the Azure REST API
            // Update withDataDiskUpdated(int lun, int newSizeInGB);

            /**
             * Updates the size and caching type of a managed data disk with the given LUN.
             *
             * @param lun the disk LUN
             * @param newSizeInGB the new size of the disk
             * @param cachingType a caching type
             * @return the next stage of the update
             */
            // TODO: This has been disabled by the Azure REST API
            //Update withDataDiskUpdated(int lun, int newSizeInGB, CachingTypes cachingType);

            /**
             * Updates the size, caching type and storage account type of a managed data disk with the given LUN.
             * @param lun the disk LUN
             * @param newSizeInGB the new size of the disk
             * @param cachingType a caching type
             * @param storageAccountType a storage account type
             * @return the next stage of the update
             */
            // TODO: This has been disabled by the Azure REST API
            //Update withDataDiskUpdated(int lun,
            //                              int newSizeInGB,
            //                              CachingTypes cachingType,
            //                              StorageAccountTypes storageAccountType);
        }

        /**
         * The stage of a virtual machine update allowing to specify additional network interfaces.
         */
        interface WithSecondaryNetworkInterface {
            /**
             * Creates a new network interface to associate with the virtual machine.
             * <p>
             * Note this method's effect is additive, i.e. each time it is used, the new secondary
             * network interface added to the virtual machine.
             *
             * @param creatable a creatable definition for a new network interface
             * @return the next stage of the update
             */
            Update withNewSecondaryNetworkInterface(Creatable<NetworkInterface> creatable);

            /**
             * Associates an existing network interface with the virtual machine.
             *
             * Note this method's effect is additive, i.e. each time it is used, the new secondary
             * network interface added to the virtual machine.
             *
             * @param networkInterface an existing network interface
             * @return the next stage of the update
             */
            Update withExistingSecondaryNetworkInterface(NetworkInterface networkInterface);

            /**
             * Removes a secondary network interface from the virtual machine.
             *
             * @param name the name of a secondary network interface to remove
             * @return the next stage of the update
             */
            Update withoutSecondaryNetworkInterface(String name);
        }

        /**
         * The stage of a virtual machine update allowing to specify extensions.
         */
        interface WithExtension {
            /**
             * Begins the definition of an extension to be attached to the virtual machine.
             *
             * @param name a reference name for the extension
             * @return the first stage of an extension definition
             */
            VirtualMachineExtension
                    .UpdateDefinitionStages
                    .Blank<Update> defineNewExtension(String name);

            /**
             * Begins the description of an update of an existing extension of this virtual machine.
             *
             * @param name the reference name of an existing extension
             * @return the first stage of an extension update
             */
            VirtualMachineExtension.Update updateExtension(String name);

            /**
             * Detaches an extension from the virtual machine.
             *
             * @param name the reference name of the extension to be removed/uninstalled
             * @return the next stage of the update
             */
            Update withoutExtension(String name);
        }

        /**
         * The stage of the virtual machine definition allowing to enable boot diagnostics.
         */
        @Beta(Beta.SinceVersion.V1_2_0)
        interface WithBootDiagnostics {
            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine.
             *
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            Update withBootDiagnostics();

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine.
             *
             * @param creatable the storage account to be created and used for store the boot diagnostics
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            Update withBootDiagnostics(Creatable<StorageAccount> creatable);

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine.
             *
             * @param storageAccount an existing storage account to be uses to store the boot diagnostics
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            Update withBootDiagnostics(StorageAccount storageAccount);

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine.
             *
             * @param storageAccountBlobEndpointUri a storage account blob endpoint to store the boot diagnostics
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            Update withBootDiagnostics(String storageAccountBlobEndpointUri);

            /**
             * Specifies that boot diagnostics needs to be disabled in the virtual machine.
             *
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            Update withoutBootDiagnostics();
        }

        /**
         * The stage of the virtual machine update allowing to enable Managed Service Identity.
         */
        @Beta(Beta.SinceVersion.V1_2_0)
        interface WithManagedServiceIdentity {
            /**
             * Specifies that Managed Service Identity needs to be enabled in the virtual machine.
             *
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrUpdate withManagedServiceIdentity();

            /**
             * Specifies that Managed Service Identity needs to be enabled in the virtual machine.
             *
             * @param tokenPort the port on the virtual machine where access token is available
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrUpdate withManagedServiceIdentity(int tokenPort);
        }

        /**
         * The stage of the Managed Service Identity enabled virtual machine allowing to set role
         * assignment for a scope.
         */
        @Beta(Beta.SinceVersion.V1_2_0)
        interface WithRoleAndScopeOrUpdate extends Update {
            /**
             * Specifies that applications running on the virtual machine requires the given access role
             * with scope of access limited to the ARM resource identified by the resource ID specified
             * in the scope parameter.
             *
             * @param scope scope of the access represented in ARM resource ID format
             * @param asRole access role to assigned to the virtual machine
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrUpdate withRoleBasedAccessTo(String scope, BuiltInRole asRole);

            /**
             * Specifies that applications running on the virtual machine requires the given access role
             * with scope of access limited to the current resource group that the virtual machine
             * resides.
             *
             * @param asRole access role to assigned to the virtual machine
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrUpdate withRoleBasedAccessToCurrentResourceGroup(BuiltInRole asRole);

            /**
             * Specifies that applications running on the virtual machine requires the given access role
             * definition with scope of access limited to the ARM resource identified by the resource id
             * specified in the scope parameter.
             *
             * @param scope scope of the access represented in ARM resource ID format
             * @param roleDefinitionId access role definition to assigned to the virtual machine
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrUpdate withRoleDefinitionBasedAccessTo(String scope, String roleDefinitionId);

            /**
             * Specifies that applications running on the virtual machine requires the given access role
             * definition with scope of access limited to the current resource group that the virtual
             * machine resides.
             *
             * @param roleDefinitionId access role definition to assigned to the virtual machine
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrUpdate withRoleDefinitionBasedAccessToCurrentResourceGroup(String roleDefinitionId);
        }
    }

    /**
     * The template for an update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<VirtualMachine>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithUnmanagedDataDisk,
            UpdateStages.WithManagedDataDisk,
            UpdateStages.WithSecondaryNetworkInterface,
            UpdateStages.WithExtension,
            UpdateStages.WithBootDiagnostics,
            UpdateStages.WithManagedServiceIdentity {

        /**
         * Specifies the encryption settings for the OS Disk.
         *
         * @param settings the encryption settings.
         * @return the stage representing creatable VM update
         */
        Update withOSDiskEncryptionSettings(DiskEncryptionSettings settings);

        /**
         * Specifies the default caching type for the managed data disks.
         *
         * @param cachingType a caching type
         * @return the next stage of the update
         */
        Update withDataDiskDefaultCachingType(CachingTypes cachingType);

        /**
         * Specifies a storage account type.
         *
         * @param storageAccountType a storage account type
         * @return the next stage of the update
         */
        Update withDataDiskDefaultStorageAccountType(StorageAccountTypes storageAccountType);

        /**
         * Specifies the caching type for the OS disk.
         *
         * @param cachingType a caching type.
         * @return the next stage of the update
         */
        Update withOSDiskCaching(CachingTypes cachingType);

        /**
         * Specifies the size of the OS disk in GB.
         * <p>
         * Only unmanaged disks may be resized as part of a VM update. Managed disks must be resized separately, using managed disk API.
         *
         * @param size a disk size.
         * @return the next stage of the update
         */
        Update withOSDiskSizeInGB(Integer size);

        /**
         * Specifies the size of the OS disk in GB.
         * <p>
         * Only unmanaged disks may be resized as part of a VM update. Managed disks must be resized separately, using managed disk API.
         *
         * @param size a disk size.
         * @return the next stage of the update
         */
        @Beta(Beta.SinceVersion.V1_2_0)
        Update withOSDiskSizeInGB(int size);

        /**
         * Specifies a new size for the virtual machine.
         *
         * @param sizeName the name of a size for the virtual machine as text
         * @return the next stage of the update
         */
        Update withSize(String sizeName);

        /**
         * Specifies a new size for the virtual machine.
         *
         * @param size a size from the list of available sizes for the virtual machine
         * @return the next stage of the definition
         */
        Update withSize(VirtualMachineSizeTypes size);
    }
}