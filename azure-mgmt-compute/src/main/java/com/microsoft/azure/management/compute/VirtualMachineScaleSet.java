package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.implementation.VirtualMachineScaleSetInner;
import com.microsoft.azure.management.network.Backend;
import com.microsoft.azure.management.network.InboundNatPool;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.storage.StorageAccount;

import java.io.IOException;
import java.util.Map;

/**
 * An immutable client-side representation of an Azure virtual machine scale set.
 */
public interface VirtualMachineScaleSet extends
        GroupableResource,
        Refreshable<VirtualMachineScaleSet>,
        Wrapper<VirtualMachineScaleSetInner>,
        Updatable<VirtualMachineScaleSet.Update> {
    // Actions
    //
    /**
     * @return  available skus for the virtual machine scale set including the minimum and maximum vm instances
     *          allowed for a particular sku.
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     */
    PagedList<VirtualMachineScaleSetSku> availableSkus() throws CloudException, IOException;

    /**
     * Shuts down the Virtual Machine in the scale set and releases the compute resources.
     * <p>
     * You are not billed for the compute resources that the Virtual Machines uses
     *
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void deallocate() throws CloudException, IOException, InterruptedException;

    /**
     * Power off (stop) the virtual machines in the scale set.
     * <p>
     * You will be billed for the compute resources that the Virtual Machines uses.
     *
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void powerOff() throws CloudException, IOException, InterruptedException;

    /**
     * Restart the virtual machines in the scale set.
     *
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void restart() throws CloudException, IOException, InterruptedException;

    /**
     * Start the virtual machines  in the scale set.
     *
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void start() throws CloudException, IOException, InterruptedException;

    /**
     * Re-image (update the version of the installed operating system) the virtual machines in the scale set.
     *
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when the operation is interrupted
     */
    void reimage() throws CloudException, IOException, InterruptedException;

    // Getters
    //

    /**
     * @return the name prefix of the virtual machines in the scale set.
     */
    String computerNamePrefix();

    /**
     * @return the operating system of the virtual machines in the scale set.
     */
    OperatingSystemTypes osType();

    /**
     * @return the operating system disk caching type, valid values are 'None', 'ReadOnly', 'ReadWrite'
     */
    CachingTypes osDiskCachingType();

    /**
     * @return gets the name of the OS disk of virtual machines in the scale set.
     */
    String osDiskName();

    /**
     * @return the upgradePolicy
     */
    UpgradePolicy upgradePolicy();

    /**
     * @return true if over provision is enabled for the virtual machines, false otherwise.
     */
    boolean overProvisionEnabled();

    /**
     * @return the sku of the virtual machines in the scale set.
     */
    VirtualMachineScaleSetSkuTypes sku();

    /**
     * @return the internet facing load balancer associated with the primary network interface of
     * the virtual machines in the scale set.
     *
     * @throws IOException the IO exception
     */
    LoadBalancer primaryInternetFacingLoadBalancer() throws IOException;

    /**
     * @return the internet facing load balancer's backends associated with the primary network interface
     * of the virtual machines in the scale set.
     *
     * @throws IOException the IO exception
     */
    Map<String, Backend> primaryInternetFacingLoadBalancerBackEnds() throws IOException;

    /**
     * @return the internet facing load balancer's inbound NAT pool associated with the primary network interface
     * of the virtual machines in the scale set.
     *
     * @throws IOException the IO exception
     */
    Map<String, InboundNatPool> primaryInternetFacingLoadBalancerInboundNatPools() throws IOException;

    /**
     * @return the internal load balancer associated with the primary network interface of
     * the virtual machines in the scale set.
     *
     * @throws IOException the IO exception
     */
    LoadBalancer primaryInternalLoadBalancer() throws IOException;

    /**
     * @return the internal load balancer's backends associated with the primary network interface
     * of the virtual machines in the scale set.
     *
     * @throws IOException the IO exception
     */
    Map<String, Backend> primaryInternalLoadBalancerBackEnds() throws IOException;

    /**
     * @return the internal load balancer's inbound NAT pool associated with the primary network interface
     * of the virtual machines in the scale set.
     *
     * @throws IOException the IO exception
     */
    Map<String, InboundNatPool> primaryInternalLoadBalancerInboundNatPools() throws IOException;

    /**
     * @return the storage profile.
     */
    VirtualMachineScaleSetStorageProfile storageProfile();

    /**
     * @return the network profile
     */
    VirtualMachineScaleSetNetworkProfile networkProfile();

    /**
     * @return the extensions attached to the Virtual Machines in the scale set.
     */
    Map<String, VirtualMachineScaleSetExtension> extensions();

    /**
     * The entirety of the load balancer definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithSku,
            DefinitionStages.WithNetwork,
            DefinitionStages.WithSubnet,
            DefinitionStages.WithPrimaryInternetFacingLoadBalancer,
            DefinitionStages.WithPrimaryInternalLoadBalancer,
            DefinitionStages.WithPrimaryInternetFacingLoadBalancerBackendOrNatPool,
            DefinitionStages.WithInternalLoadBalancerBackendOrNatPool,
            DefinitionStages.WithPrimaryInternetFacingLoadBalancerNatPool,
            DefinitionStages.WithInternalInternalLoadBalancerNatPool,
            DefinitionStages.WithOS,
            DefinitionStages.WithAdminUserName,
            DefinitionStages.WithRootUserName,
            DefinitionStages.WithLinuxCreate,
            DefinitionStages.WithWindowsCreate,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of virtual machine scale set definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a virtual machine scale set definition.
         */
        interface Blank
                extends GroupableResource.DefinitionWithRegion<VirtualMachineScaleSet.DefinitionStages.WithGroup> {
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify the resource group.
         */
        interface WithGroup
                extends GroupableResource.DefinitionStages.WithGroup<WithSku> {
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify Sku for the virtual machines.
         */
        interface WithSku {
            /**
             * Specifies sku for the virtual machines in the scale set.
             *
             * @param skuType the sku type
             * @return the stage representing creatable VM scale set definition
             */
            WithNetwork withSku(VirtualMachineScaleSetSkuTypes skuType);

            /**
             * Specifies sku for the virtual machines in the scale set.
             *
             * @param sku a sku from the list of available sizes for the virtual machines in this scale set
             * @return the stage representing creatable VM scale set definition
             */
            WithNetwork withSku(VirtualMachineScaleSetSku sku);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify virtual network for the  primary
         * network configuration.
         */
        interface WithNetwork {
            /**
             * Create a new virtual network to associate with the primary network interface of virtual machines in the
             * virtual machine scale set, based on the provided definition.
             *
             * @param creatable a creatable definition for a new virtual network
             * @return the next stage of the virtual machine scale set definition
             */
            WithPrimaryInternetFacingLoadBalancer withNewPrimaryNetwork(Creatable<Network> creatable);

            /**
             * Creates a new virtual network to associate with the primary network interface of the virtual machines
             * in the scale set.
             * <p>
             * the virtual network will be created in the same resource group and region as of virtual machine scale set,
             * it will be reated with the specified address space and a default subnet covering the entirety of the
             * network IP address space.
             * </p>
             * @param addressSpace the address space for the virtual network
             * @return the next stage of the virtual machine scale set definition
             */
            WithPrimaryInternetFacingLoadBalancer withNewPrimaryNetwork(String addressSpace);

            /**
             * Associate an existing virtual network with the primary network interface of the virtual machines
             * in the scale set.
             *
             * @param network an existing virtual network
             * @return the next stage of the virtual machine scale set definition
             */
            WithSubnet withExistingPrimaryNetwork(Network network);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify virtual network subnet for the
         * virtual machine's primary network interface in the scale set.
         *
         */
        interface WithSubnet {
            /**
             * Associates a subnet with the primary network interface of virtual machines in the scale set.
             *
             * @param name the subnet name
             * @return the next stage of the definition
             */
            WithPrimaryInternetFacingLoadBalancer withSubnet(String name);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify a public load balancer for
         * the primary network interface configuration.
         */
        interface WithPrimaryInternetFacingLoadBalancer {
            /**
             * Specify the public load balancer where it's backends and/or NAT pools can be assigned to the primary network
             * interface configuration of virtual machine scale set.
             * <p>
             * By default all the backend and inbound NAT pool of the load balancer will be associated with the primary
             * network interface configuration unless one of them is selected in the next stage
             * {@link WithInternalLoadBalancerBackendOrNatPool}.
             * <p>
             * @param loadBalancer an existing public load balancer
             * @return the next stage of the definition
             */
            WithPrimaryInternetFacingLoadBalancerBackendOrNatPool withPrimaryInternetFacingLoadBalancer(LoadBalancer loadBalancer);

            /**
             * Specifies that no public load balancer needs to be associated with virtual machine scale set.
             *
             * @return the next stage of the virtual machine scale set definition
             */
            WithPrimaryInternalLoadBalancer withoutPrimaryInternetFacingLoadBalancer();
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify an internal load balancer for
         * the primary network interface configuration.
         */
        interface WithPrimaryInternalLoadBalancer {
            /**
             * Specify the internal load balancer where it's backends and/or NAT pools can be assigned to the primary
             * network interface configuration of the virtual machine scale set.
             * <p>
             * By default all the backend and inbound NAT pool of the load balancer will be associated with the primary
             * network interface configuration unless one of them is selected in the next stage
             * {@link WithInternalLoadBalancerBackendOrNatPool}.
             * <p>
             * @param loadBalancer an existing internal load balancer
             * @return the next stage of the definition
             */
            WithInternalLoadBalancerBackendOrNatPool withPrimaryInternalLoadBalancer(LoadBalancer loadBalancer);

            /**
             * Specifies that no internal load balancer needs to be associated with virtual machine scale set.
             *
             * @return the next stage of the virtual machine scale set definition
             */
            WithOS withoutPrimaryInternalLoadBalancer();
        }

        /**
         * The stage of the virtual machine scale set definition allowing to associate backend pool and/or inbound NAT pool
         * of the internet facing load balancer selected in the previous state {@link WithPrimaryInternetFacingLoadBalancer}
         * with the primary network interface configuration.
         */
        interface WithPrimaryInternetFacingLoadBalancerBackendOrNatPool extends WithPrimaryInternetFacingLoadBalancerNatPool {
            /**
             * Associate internet facing load balancer backends with the primary network interface configuration of the
             * virtual machine scale set.
             *
             * @param backendNames the backend names
             * @return the next stage of the virtual machine scale set definition
             */
            WithPrimaryInternetFacingLoadBalancerNatPool withPrimaryInternetFacingLoadBalancerBackends(String ...backendNames);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to associate inbound NAT pool of the internet
         * facing load balancer selected in the previous state {@link WithPrimaryInternetFacingLoadBalancer} with the
         * primary network interface configuration.
         */
        interface WithPrimaryInternetFacingLoadBalancerNatPool extends WithPrimaryInternalLoadBalancer {
            /**
             * Associate internet facing load balancer inbound NAT pools with the to the primary network interface
             * configuration of the virtual machine scale set.
             *
             * @param natPoolNames the inbound NAT pool names
             * @return the next stage of the virtual machine scale set definition
             */
            WithPrimaryInternalLoadBalancer withPrimaryInternetFacingLoadBalancerInboundNatPools(String ...natPoolNames);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to associate backend pool and/or inbound NAT pool
         * of the internal load balancer selected in the previous state {@link WithPrimaryInternalLoadBalancer} with the
         * primary network interface configuration.
         */
        interface WithInternalLoadBalancerBackendOrNatPool extends WithCreate {
            /**
             * Associates internal load balancer backend pools with the primary network interface configuration
             * of the virtual machine scale set.
             *
             * @param backendNames the backend names
             * @return the next stage of the virtual machine scale set definition
             */
            WithInternalInternalLoadBalancerNatPool withPrimaryInternalLoadBalancerBackends(String ...backendNames);
         }

        /**
         * The stage of the virtual machine scale set definition allowing to assign inbound NAT pool of the internal
         * load balancer selected in the previous state {@link WithPrimaryInternalLoadBalancer} with the
         * primary network interface configuration.
         */
        interface WithInternalInternalLoadBalancerNatPool extends WithOS {
            /**
             * Associates internal load balancer inbound NAT pools with the primary network interface configuration
             * of the virtual machine scale set.
             *
             * @param natPoolNames inbound NAT pool names
             * @return the next stage of the virtual machine scale set definition
             */
            WithOS withPrimaryInternalLoadBalancerInboundNatPools(String ...natPoolNames);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify the Operation System image.
         */
        interface WithOS {
            /**
             * Specifies the known marketplace Windows image used as OS for virtual machines in the scale set.
             *
             * @param knownImage enum value indicating known market-place image
             * @return the next stage of the virtual machine scale set definition
             */
            WithAdminUserName withPopularWindowsImage(KnownWindowsVirtualMachineImage knownImage);

            /**
             * Specifies that the latest version of a marketplace Windows image needs to be used.
             *
             * @param publisher specifies the publisher of the image
             * @param offer specifies the offer of the image
             * @param sku specifies the SKU of the image
             * @return the next stage of the virtual machine scale set definition
             */
            WithAdminUserName withLatestWindowsImage(String publisher, String offer, String sku);

            /**
             * Specifies the version of a marketplace Windows image needs to be used.
             *
             * @param imageReference describes publisher, offer, sku and version of the market-place image
             * @return the next stage of the virtual machine scale set definition
             */
            WithAdminUserName withSpecificWindowsImageVersion(ImageReference imageReference);

            /**
             * Specifies the user (custom) Windows image used for as the OS for virtual machines in the
             * scale set.
             * <p>
             * Custom images are currently limited to single storage account and the number of virtual machines
             * in the scale set that can be created using custom image is limited to 40 when over provision
             * is disabled {@link WithOverProvision} and up to 20 when enabled.
             * </p>
             * @param imageUrl the url the the VHD
             * @return the next stage of the virtual machine scale set definition
             */
            WithAdminUserName withStoredWindowsImage(String imageUrl);

            /**
             * Specifies the known marketplace Linux image used for the virtual machine's OS.
             *
             * @param knownImage enum value indicating known market-place image
             * @return the next stage of the virtual machine scale set definition
             */
            WithRootUserName withPopularLinuxImage(KnownLinuxVirtualMachineImage knownImage);

            /**
             * Specifies that the latest version of a marketplace Linux image needs to be used.
             *
             * @param publisher specifies the publisher of the image
             * @param offer specifies the offer of the image
             * @param sku specifies the SKU of the image
             * @return the next stage of the virtual machine scale set definition
             */
            WithRootUserName withLatestLinuxImage(String publisher, String offer, String sku);

            /**
             * Specifies the version of a market-place Linux image needs to be used.
             *
             * @param imageReference describes publisher, offer, sku and version of the market-place image
             * @return the next stage of the virtual machine scale set definition
             */
            WithRootUserName withSpecificLinuxImageVersion(ImageReference imageReference);

            /**
             * Specifies the user (custom) Linux image used for the virtual machine's OS.
             * <p>
             * Custom images are currently limited to single storage account and the number of virtual machines
             * in the scale set that can be created using custom image is limited to 40 when over provision
             * is disabled {@link WithOverProvision} and up to 20 when enabled.
             * </p>
             * @param imageUrl the url the the VHD
             * @return the next stage of the virtual machine scale set definition
             */
            WithRootUserName withStoredLinuxImage(String imageUrl);
        }

        /**
         * The stage of the Linux virtual machine scale set definition allowing to specify root user name.
         */
        interface WithRootUserName {
            /**
             * Specifies the root user name for the Linux virtual machines in the scale set.
             *
             * @param rootUserName the Linux root user name. This must follow the required naming convention for Linux user name
             * @return the next stage of the Linux virtual machine scale set definition
             */
            WithLinuxCreate withRootUserName(String rootUserName);
        }

        /**
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
         */
        interface WithAdminUserName {
            /**
             * Specifies the administrator user name for the Windows virtual machines in the scale set.
             *
             * @param adminUserName the Windows administrator user name. This must follow the required naming convention for Windows user name.
             * @return the stage representing creatable Windows VM scale set definition
             */
            WithWindowsCreate withAdminUserName(String adminUserName);
        }

        /**
         * The stage of the Linux virtual machine scale set definition which contains all the minimum required inputs
         * for the resource to be created (via {@link WithCreate#create()}), but also allows for any other optional
         * settings to be specified.
         *
         */
        interface WithLinuxCreate extends WithCreate {
            /**
             * Specifies the SSH public key.
             * <p>
             * Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the stage representing creatable Linux VM scale set definition
             */
            WithLinuxCreate withSsh(String publicKey);
        }

        /**
         * The stage of the Windows virtual machine scale set definition which contains all the minimum required
         * inputs for the resource to be created (via {@link WithCreate#create()}, but also allows for any other
         * optional settings to be specified.
         *
         */
        interface WithWindowsCreate extends WithCreate {
            /**
             * Specifies that VM Agent should not be provisioned.
             *
             * @return the stage representing creatable Windows VM scale set definition
             */
            WithWindowsCreate disableVmAgent();

            /**
             * Specifies that automatic updates should be disabled.
             *
             * @return the stage representing creatable Windows VM scale set definition
             */
            WithWindowsCreate disableAutoUpdate();

            /**
             * Specifies the time-zone.
             *
             * @param timeZone the timezone
             * @return the stage representing creatable Windows VM scale set definition
             */
            WithWindowsCreate withTimeZone(String timeZone);

            /**
             * Specifies the WINRM listener.
             * <p>
             * Each call to this method adds the given listener to the list of VM's WinRM listeners.
             *
             * @param listener the WinRmListener
             * @return the stage representing creatable Windows VM scale set definition
             */
            WithWindowsCreate withWinRm(WinRMListener listener);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify password.
         */
        interface WithPassword {
            /**
             * Specifies the password for the virtual machines in the scale set.
             *
             * @param password the password. This must follow the criteria for Azure VM password.
             * @return the stage representing creatable VM scale set definition
             */
            WithCreate withPassword(String password);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify the computer name prefix.
         */
        interface WithComputerNamePrefix {
            /**
             * Specifies the bane prefix for the virtual machines in the scale set.
             *
             * @param namePrefix the prefix for the name of virtual machines in the scale set.
             * @return the stage representing creatable VM scale set definition
             */
            WithCreate withComputerNamePrefix(String namePrefix);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify number of
         * virtual machines in the scale set.
         */
        interface WithCapacity {
            /**
             * Specifies the number of virtual machines in the scale set.
             *
             * @param capacity the virtual machine capacity
             * @return the stage representing creatable VM scale set definition
             */
            WithCreate withCapacity(long capacity);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify upgrade policy.
         */
        interface WithUpgradePolicy {
            /**
             * Specifies virtual machine scale set upgrade policy.
             *
             * @param upgradePolicy upgrade policy
             * @return the stage representing creatable VM scale set definition
             */
            WithCreate withUpgradePolicy(UpgradePolicy upgradePolicy);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify whether
         * or not over provision virtual machines in the scale set.
         */
        interface WithOverProvision {
            /**
             * Enable or disable over provisioning of virtual machines in the scale set.
             *
             * @param enabled true to enable over provisioning of virtual machines in the
             *                scale set.
             * @return Enable over provision of virtual machines.
             */
            WithCreate withOverProvision(boolean enabled);

            /**
             * Enable over provision of virtual machines.
             *
             * @return the stage representing creatable VM scale set definition
             */
            WithCreate withOverProvisionEnabled();

            /**
             * Disable over provision of virtual machines.
             *
             * @return the stage representing creatable VM scale set definition
             */
            WithCreate withOverProvisionDisabled();
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify OS disk configurations.
         */
        interface WithOsDiskSettings {
            /**
             * Specifies the caching type for the Operating System disk.
             *
             * @param cachingType the caching type.
             * @return the stage representing creatable VM scale set definition
             */
            WithCreate withOsDiskCaching(CachingTypes cachingType);

            /**
             * Specifies the name for the OS Disk.
             *
             * @param name the OS Disk name.
             * @return the stage representing creatable VM scale set definition
             */
            WithCreate withOsDiskName(String name);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify storage account.
         */
        interface WithStorageAccount {
            /**
             * Specifies the name of a new storage account to put the OS and data disk VHD of the virtual machines
             * in the scale set.
             *
             * @param name the name of the storage account
             * @return the stage representing creatable VM scale set definition
             */
            WithCreate withNewStorageAccount(String name);

            /**
             * Specifies definition of a not-yet-created storage account definition
             * to put OS and data disk VHDs of virtual machines in the scale set.
             *
             * @param creatable the storage account in creatable stage
             * @return the stage representing creatable VM scale set definition
             */
            WithCreate withNewStorageAccount(Creatable<StorageAccount> creatable);

            /**
             * Specifies an existing {@link StorageAccount} storage account to put the OS and data disk VHD of
             * virtual machines in the scale set.
             *
             * @param storageAccount an existing storage account
             * @return the stage representing creatable VM scale set definition
             */
            WithCreate withExistingStorageAccount(StorageAccount storageAccount);
        }

        /**
         * The stage of the virtual machine definition allowing to specify extensions.
         */
        interface WithExtension {
            /**
             * Specifies definition of an extension to be attached to the virtual machines in the scale set.
             *
             * @param name the reference name for the extension
             * @return the stage representing configuration for the extension
             */
            VirtualMachineScaleSetExtension.DefinitionStages.Blank<WithCreate> defineNewExtension(String name);
        }

        /**
         * The stage of a virtual machine scale set definition containing all the required inputs for the resource
         * to be created (via {@link WithCreate#create()}), but also allowing for any other optional settings
         * to be specified.
         */
        interface WithCreate extends
                Creatable<VirtualMachineScaleSet>,
                DefinitionStages.WithPassword,
                DefinitionStages.WithOsDiskSettings,
                DefinitionStages.WithComputerNamePrefix,
                DefinitionStages.WithCapacity,
                DefinitionStages.WithOverProvision,
                DefinitionStages.WithStorageAccount,
                DefinitionStages.WithExtension,
                Resource.DefinitionWithTags<VirtualMachineScaleSet.DefinitionStages.WithCreate> {
        }
    }

    /**
     * Grouping of virtual machine scale set update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the virtual machine scale set definition allowing to update Sku for the virtual machines in the scale set.
         */
        interface WithSku {
            /**
             * Specifies sku for the virtual machines in the scale set.
             *
             * @param skuType the sku type
             * @return the next stage of the virtual machine scale set update
             */
            Update withSku(VirtualMachineScaleSetSkuTypes skuType);

            /**
             * Specifies sku for the virtual machines in the scale set.
             *
             * @param sku a sku from the list of available sizes for the virtual machines in this scale set
             * @return the next stage of the virtual machine scale set update
             */
            Update withSku(VirtualMachineScaleSetSku sku);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify number of
         * virtual machines in the scale set.
         */
        interface WithCapacity {
            /**
             * Specifies the new number of virtual machines in the scale set.
             *
             * @param capacity the virtual machine capacity
             * @return the next stage of the virtual machine scale set update
             */
            Update withCapacity(long capacity);
        }

        /**
         * The stage of the virtual machine definition allowing to specify extensions.
         */
        interface WithExtension {
            /**
             * Specifies definition of an extension to be attached to the virtual machines in the scale set.
             *
             * @param name the reference name for the extension
             * @return the stage representing configuration for the extension
             */
            VirtualMachineScaleSetExtension
                    .UpdateDefinitionStages
                    .Blank<Update> defineNewExtension(String name);

            /**
             * Begins the description of an update of an existing extension assigned to the virtual machines in the scale set.
             *
             * @param name the reference name for the extension
             * @return the stage representing updatable extension definition
             */
            VirtualMachineScaleSetExtension.Update updateExtension(String name);

            /**
             * Detaches an extension with the given name from the virtual machines in the scale set.
             *
             * @param name the reference name for the extension to be removed/uninstalled
             * @return the stage representing updatable VM scale set definition
             */
            Update withoutExtension(String name);
        }

        /**
         * Stage of the virtual machine scale set update allowing to remove public and internal load balancer
         * from the primary network interface configuration.
         */
        interface WithoutPrimaryLoadBalancer {
            /**
             * Remove the internet facing load balancer associated to the primary network interface configuration.
             * <p>
             * This removes the association between primary network interface configuration and all backend and
             * inbound NAT pools in the load balancer.
             * </p>
             *
             * @return the next stage of the virtual machine scale set update
             */
            Update withoutPrimaryInternetFacingLoadBalancer();

            /**
             * Remove the internal load balancer associated to the primary network interface configuration.
             * <p>
             * This removes the association between primary network interface configuration and all backend and
             * inbound NAT pools in the load balancer.
             * </p>
             *
             * @return the next stage of the virtual machine scale set update
             */
            Update withoutPrimaryInternalLoadBalancer();
        }

        /**
         * Stage of the virtual machine scale set update allowing to remove association between the primary network interface
         * configuration and backend of the load balancer.
         */
        interface WithoutPrimaryLoadBalancerBackend {
            /**
             * Removes association between the primary network interface configuration and backend of the internet facing
             * load balancer.
             *
             * @param backendNames the existing backend names to remove
             * @return the next stage of the virtual machine scale set update
             */
            Update withoutPrimaryInternetFacingLoadBalancerBackends(String ...backendNames);

            /**
             * Removes association between the primary network interface configuration and backend of the internal load balancer.
             *
             * @param backendNames the existing backend names to remove
             * @return the next stage of the virtual machine scale set update
             */
            Update withoutPrimaryInternalLoadBalancerBackends(String ...backendNames);
        }

        /**
         * Stage of the virtual machine scale set update allowing to remove association between the primary network interface
         * configuration and inbound NAT pool of the load balancer.
         */
        interface WithoutPrimaryLoadBalancerNatPool {
            /**
             * Removes association between the primary network interface configuration and inbound NAT pool of the
             * internet facing load balancer.
             *
             * @param natPoolNames the name of an existing inbound NAT pools to remove
             * @return the next stage of the virtual machine scale set update
             */
            Update withoutPrimaryInternetFacingLoadBalancerNatPools(String ...natPoolNames);

            /**
             * Removes association between the primary network interface configuration and inbound NAT pool of the
             * internal load balancer.
             *
             * @param natPoolNames the name of an existing inbound NAT pools to remove
             * @return the next stage of the virtual machine scale set update
             */
            Update withoutPrimaryInternalLoadBalancerNatPools(String ...natPoolNames);
        }
    }

    /**
     * The entirety of the load balancer update.
     */
    interface Update extends
            Appliable<VirtualMachineScaleSet>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithSku,
            UpdateStages.WithCapacity,
            UpdateStages.WithExtension,
            UpdateStages.WithoutPrimaryLoadBalancer,
            UpdateStages.WithoutPrimaryLoadBalancerBackend,
            UpdateStages.WithoutPrimaryLoadBalancerNatPool {
    }
}