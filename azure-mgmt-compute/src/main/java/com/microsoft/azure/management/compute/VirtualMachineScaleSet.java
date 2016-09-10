package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.storage.StorageAccount;

/**
 * An immutable client-side representation of an Azure virtual machine scale set.
 */
public interface VirtualMachineScaleSet {

    /**
     * The entirety of the load balancer definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithNetwork,
            DefinitionStages.WithPrimaryInternetFacingLoadBalancer,
            DefinitionStages.WithPrimaryInternalLoadBalancer,
            DefinitionStages.WithPrimaryInternetFacingLoadBalancerBackendOrNatPool,
            DefinitionStages.WithInternalLoadBalancerBackendOrNatPool,
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
                extends GroupableResource.DefinitionStages.WithGroup<WithNetwork> {
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
            WithPrimaryInternetFacingLoadBalancerNatPool withPrimaryInternetFacingLoadBalancerBackend(String ...backendNames);
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
            WithPrimaryInternalLoadBalancer withPrimaryInternetFacingLoadBalancerInboundNatPool(String ...natPoolNames);
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
            WithInternalInternalLoadBalancerNatPool withPrimaryInternalLoadBalancerBackend(String ...backendNames);
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
            WithOS withPrimaryInternalLoadBalancerInboundNatPool(String ...natPoolNames);
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
             * Specifies the user (generalized) Windows image used for as the OS for virtual machines in the
             * scale set.
             *
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
             * Specifies the user (generalized) Linux image used for the virtual machine's OS.
             *
             * @param imageUrl the url the the VHD
             * @return the next stage of the virtual machine scale set definition
             */
            WithRootUserName withStoredLinuxImage(String imageUrl);

            /**
             * Specifies the specialized operating system disk to be attached to the virtual machines in the
             * scale set.
             *
             * @param osDiskUrl osDiskUrl the url to the OS disk in the Azure Storage account
             * @param osType the OS type
             * @return the next stage of the Windows virtual machine scale set definition
             */
            WithCreate withOsDisk(String osDiskUrl, OperatingSystemTypes osType);
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
         * The stage of the virtual machine scale set definition allowing to specify VM size.
         */
        interface WithVMSize {
            /**
             * Specifies size for the virtual machines in the scale set.
             *
             * @param sizeName the name of the size for the virtual machine as text
             * @return the stage representing creatable VM scale set definition
             */
            WithCreate withSize(String sizeName);

            /**
             * Specifies size for the virtual machines in the scale set.
             *
             * @param size a size from the list of available sizes for the virtual machine
             * @return the stage representing creatable VM scale set definition
             */
            WithCreate withSize(VirtualMachineSizeTypes size);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to upgrade policy.
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
            VirtualMachineExtension.DefinitionStages.Blank<WithCreate> defineNewExtension(String name);
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
                DefinitionStages.WithVMSize,
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
         * Stage of the virtual machine scale set update allowing to associate a backend from the load balancer
         * with the primary network interface configuration.
         */
        interface WithPrimaryLoadBalancerBackend {
            /**
             * Associate a backend of the internet facing load balancer with the the primary network interface configuration.
             *
             * @param backendName the name of an existing backend
             * @return the next stage of the virtual machine scale set update
             */
            Update withPrimaryInternetFacingLoadBalancerBackend(String backendName);

            /**
             * Associate a backend of the internal load balancer with the the primary network interface configuration.
             *
             * @param backendName the name of an existing backend
             * @return the next stage of the virtual machine scale set update
             */
            Update withPrimaryInternalLoadBalancerBackend(String backendName);
        }

        /**
         * Stage of the virtual machine scale set update allowing to associate a inbound NAT pool from the load balancer
         * with the primary network interface configuration.
         */
        interface WithPrimaryLoadBalancerNatPoold {
            /**
             * Associate an inbound NAT pool of the internet facing load balancer with the the primary network interface configuration.
             *
             * @param natPoolName the name of an existing inbound NAT pool
             * @return the next stage of the virtual machine scale set update
             */
            Update withPrimaryInternetFacingLoadBalancerNatPool(String natPoolName);

            /**
             * Associate an inbound NAT pool of the internal load balancer with the the primary network interface configuration.
             *
             * @param natPoolName the name of an existing inbound NAT pool
             * @return the next stage of the virtual machine scale set update
             */
            Update withPrimaryInternalLoadBalancerNatPool(String natPoolName);
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
             * @param backendName the name of an existing backend
             * @return the next stage of the virtual machine scale set update
             */
            Update withoutPrimaryInternetFacingLoadBalancerBackend(String backendName);

            /**
             * Removes association between the primary network interface configuration and backend of the internal load balancer.
             *
             * @param backendName the name of an existing backend
             * @return the next stage of the virtual machine scale set update
             */
            Update withoutPrimaryInternalLoadBalancerBackend(String backendName);
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
             * @param natPoolName the name of an existing inbound NAT pool
             * @return the next stage of the virtual machine scale set update
             */
            Update withoutPrimaryInternetFacingLoadBalancerNatPool(String natPoolName);

            /**
             * Removes association between the primary network interface configuration and inbound NAT pool of the
             * internal load balancer.
             *
             * @param natPoolName the name of an existing inbound NAT pool
             * @return the next stage of the virtual machine scale set update
             */
            Update withoutPrimaryInternalLoadBalancerNatPool(String natPoolName);
        }

        interface Update {
        }
    }
}