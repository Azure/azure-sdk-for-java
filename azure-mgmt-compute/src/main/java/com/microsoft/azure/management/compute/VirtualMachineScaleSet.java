/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.compute.implementation.VirtualMachineScaleSetInner;
import com.microsoft.azure.management.graphrbac.BuiltInRole;
import com.microsoft.azure.management.network.LoadBalancerBackend;
import com.microsoft.azure.management.network.LoadBalancerInboundNatPool;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.VirtualMachineScaleSetNetworkInterface;
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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An immutable client-side representation of an Azure virtual machine scale set.
 */
@Fluent
public interface VirtualMachineScaleSet extends
        GroupableResource<ComputeManager, VirtualMachineScaleSetInner>,
        Refreshable<VirtualMachineScaleSet>,
        Updatable<VirtualMachineScaleSet.UpdateStages.WithPrimaryLoadBalancer> {
    // Actions
    //

    /**
     * @return entry point to manage virtual machine instances in the scale set.
     */
    VirtualMachineScaleSetVMs virtualMachines();

    /**
     * @return  available SKUs for the virtual machine scale set, including the minimum and maximum virtual machine instances
     *          allowed for a particular SKU
     */
    PagedList<VirtualMachineScaleSetSku> listAvailableSkus();

    /**
     * Shuts down the virtual machines in the scale set and releases its compute resources.
     */
    void deallocate();

    /**
     * Shuts down the virtual machines in the scale set and releases its compute resources asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Completable deallocateAsync();

    /**
     * Shuts down the virtual machines in the scale set and releases its compute resources asynchronously.
     *
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> deallocateAsync(ServiceCallback<Void> callback);

    /**
     * Powers off (stops) the virtual machines in the scale set.
     */
    void powerOff();

    /**
     * Powers off (stops) the virtual machines in the scale set asynchronously.
     * @return a representation of the deferred computation of this call
     */
    Completable powerOffAsync();

    /**
     * Powers off (stops) the virtual machines in the scale set asynchronously.
     *
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> powerOffAsync(ServiceCallback<Void> callback);

    /**
     * Restarts the virtual machines in the scale set.
     */
    void restart();

    /**
     * Restarts the virtual machines in the scale set asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Completable restartAsync();

    /**
     * Restarts the virtual machines in the scale set asynchronously.
     *
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> restartAsync(ServiceCallback<Void> callback);

    /**
     * Starts the virtual machines in the scale set.
     */
    void start();

    /**
     * Starts the virtual machines in the scale set asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Completable startAsync();

    /**
     * Starts the virtual machines in the scale set asynchronously.
     *
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> startAsync(ServiceCallback<Void> callback);

    /**
     * Re-images (updates the version of the installed operating system) the virtual machines in the scale set.
     */
    void reimage();

    /**
     * Re-images (updates the version of the installed operating system) the virtual machines in the scale set asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Completable reimageAsync();

    /**
     * Re-images (updates the version of the installed operating system) the virtual machines in the scale set asynchronously.
     *
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> reimageAsync(ServiceCallback<Void> callback);

    // Getters
    //

    /**
     * @return the name prefix of the virtual machines in the scale set
     */
    String computerNamePrefix();

    /**
     * @return the operating system of the virtual machines in the scale set
     */
    OperatingSystemTypes osType();

    /**
     * @return the operating system disk caching type
     */
    CachingTypes osDiskCachingType();

    /**
     * @return the name of the OS disk of virtual machines in the scale set
     */
    String osDiskName();

    /**
     * @return the upgrade model
     */
    UpgradeMode upgradeModel();

    /**
     * @return true if over provision is enabled for the virtual machines, false otherwise
     */
    boolean overProvisionEnabled();

    /**
     * @return the SKU of the virtual machines in the scale set
     */
    VirtualMachineScaleSetSkuTypes sku();

    /**
     * @return the number of virtual machine instances in the scale set
     */
    int capacity();

    /**
     * @return the virtual network associated with the primary network interfaces of the virtual machines
     * in the scale set.
     * <p>
     * A primary internal load balancer associated with the primary network interfaces of the scale set
     * virtual machine will be also belong to this network
     * </p>
     * @throws IOException the IO exception
     */
    Network getPrimaryNetwork() throws IOException;

    /**
     * @return the Internet-facing load balancer associated with the primary network interface of
     * the virtual machines in the scale set.
     *
     * @throws IOException the IO exception
     */
    LoadBalancer getPrimaryInternetFacingLoadBalancer() throws IOException;

    /**
     * @return the Internet-facing load balancer's backends associated with the primary network interface
     * of the virtual machines in the scale set
     *
     * @throws IOException the IO exception
     */
    Map<String, LoadBalancerBackend> listPrimaryInternetFacingLoadBalancerBackends() throws IOException;

    /**
     * @return the Internet-facing load balancer's inbound NAT pool associated with the primary network interface
     * of the virtual machines in the scale set
     *
     * @throws IOException the IO exception
     */
    Map<String, LoadBalancerInboundNatPool> listPrimaryInternetFacingLoadBalancerInboundNatPools() throws IOException;

    /**
     * @return the internal load balancer associated with the primary network interface of
     * the virtual machines in the scale set
     *
     * @throws IOException the IO exception
     */
    LoadBalancer getPrimaryInternalLoadBalancer() throws IOException;

    /**
     * @return the internal load balancer's backends associated with the primary network interface
     * of the virtual machines in the scale set
     *
     * @throws IOException the IO exception
     */
    Map<String, LoadBalancerBackend> listPrimaryInternalLoadBalancerBackends() throws IOException;

    /**
     * @return the inbound NAT pools of the internal load balancer associated with the primary network interface
     * of the virtual machines in the scale set, if any.
     *
     * @throws IOException the IO exception
     */
    Map<String, LoadBalancerInboundNatPool> listPrimaryInternalLoadBalancerInboundNatPools() throws IOException;

    /**
     * @return the list of IDs of the public IP addresses associated with the primary Internet-facing load balancer
     * of the scale set
     * @throws IOException the IO exception
     */
    List<String> primaryPublicIPAddressIds() throws IOException;

    /**
     * @return the URL to storage containers that store the VHDs of the virtual machines in the scale set
     */
    List<String> vhdContainers();

    /**
     * @return the storage profile
     */
    VirtualMachineScaleSetStorageProfile storageProfile();

    /**
     * @return the network profile
     */
    VirtualMachineScaleSetNetworkProfile networkProfile();

    /**
     * @return the extensions attached to the virtual machines in the scale set
     */
    Map<String, VirtualMachineScaleSetExtension> extensions();

    /**
     * Gets a network interface associated with a virtual machine scale set instance.
     *
     * @param instanceId the virtual machine scale set vm instance ID
     * @param name the network interface name
     * @return the network interface
     */
    VirtualMachineScaleSetNetworkInterface getNetworkInterfaceByInstanceId(String instanceId, String name);

    /**
     * @return the network interfaces associated with all virtual machine instances in a scale set
     */
    PagedList<VirtualMachineScaleSetNetworkInterface> listNetworkInterfaces();

    /**
     * Lists the network interface associated with a specific virtual machine instance in the scale set.
     *
     * @param virtualMachineInstanceId the instance ID
     * @return the network interfaces
     */
    PagedList<VirtualMachineScaleSetNetworkInterface> listNetworkInterfacesByInstanceId(String virtualMachineInstanceId);

    /**
     * @return true if managed disk is used for the virtual machine scale set's disks (os, data)
     */
    boolean isManagedDiskEnabled();

    /**
     * @return true if Managed Service Identity is enabled for the virtual machine scale set
     */
    @Beta(Beta.SinceVersion.V1_2_0)
    boolean isManagedServiceIdentityEnabled();

    /**
     * @return the Managed Service Identity specific Active Directory tenant ID assigned to the
     * virtual machine scale set.
     */
    @Beta(Beta.SinceVersion.V1_2_0)
    String managedServiceIdentityTenantId();

    /**
     * @return the Managed Service Identity specific Active Directory service principal ID assigned
     * to the virtual machine scale set.
     */
    @Beta(Beta.SinceVersion.V1_2_0)
    String managedServiceIdentityPrincipalId();

    /**
     * @return the availability zones assigned to virtual machine scale set.
     */
    @Beta(Beta.SinceVersion.V1_3_0)
    Set<String> availabilityZones();

    /**
     * The virtual machine scale set stages shared between managed and unmanaged based
     * virtual machine scale set definitions.
     */
    interface DefinitionShared extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithSku,
            DefinitionStages.WithNetworkSubnet,
            DefinitionStages.WithPrimaryInternetFacingLoadBalancer,
            DefinitionStages.WithPrimaryInternalLoadBalancer,
            DefinitionStages.WithPrimaryInternetFacingLoadBalancerBackendOrNatPool,
            DefinitionStages.WithInternalLoadBalancerBackendOrNatPool,
            DefinitionStages.WithPrimaryInternetFacingLoadBalancerNatPool,
            DefinitionStages.WithInternalInternalLoadBalancerNatPool,
            DefinitionStages.WithOS,
            DefinitionStages.WithCreate {
    }

    /**
     * The entirety of the virtual machine scale set definition.
     */
    interface DefinitionManagedOrUnmanaged
            extends
            DefinitionShared,
            DefinitionStages.WithLinuxRootUsernameManagedOrUnmanaged,
            DefinitionStages.WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged,
            DefinitionStages.WithWindowsAdminUsernameManagedOrUnmanaged,
            DefinitionStages.WithWindowsAdminPasswordManagedOrUnmanaged,
            DefinitionStages.WithLinuxCreateManagedOrUnmanaged,
            DefinitionStages.WithWindowsCreateManagedOrUnmanaged,
            DefinitionStages.WithManagedCreate,
            DefinitionStages.WithUnmanagedCreate {
    }

    /**
     * The entirety of the managed disk based virtual machine scale set definition.
     */
    interface DefinitionManaged
            extends
            DefinitionShared,
            DefinitionStages.WithLinuxRootUsernameManaged,
            DefinitionStages.WithLinuxRootPasswordOrPublicKeyManaged,
            DefinitionStages.WithWindowsAdminUsernameManaged,
            DefinitionStages.WithWindowsAdminPasswordManaged,
            DefinitionStages.WithLinuxCreateManaged,
            DefinitionStages.WithWindowsCreateManaged,
            DefinitionStages.WithManagedCreate {
    }

    /**
     * The entirety of the unmanaged disk based virtual machine scale set definition.
     */
    interface DefinitionUnmanaged
            extends
            DefinitionShared,
            DefinitionStages.WithLinuxRootUsernameUnmanaged,
            DefinitionStages.WithLinuxRootPasswordOrPublicKeyUnmanaged,
            DefinitionStages.WithWindowsAdminUsernameUnmanaged,
            DefinitionStages.WithWindowsAdminPasswordUnmanaged,
            DefinitionStages.WithLinuxCreateUnmanaged,
            DefinitionStages.WithWindowsCreateUnmanaged,
            DefinitionStages.WithUnmanagedCreate {
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
         * The stage of a virtual machine scale set definition allowing to specify the resource group.
         */
        interface WithGroup
                extends GroupableResource.DefinitionStages.WithGroup<WithSku> {
        }

        /**
         * The stage of a virtual machine scale set definition allowing to specify SKU for the virtual machines.
         */
        interface WithSku {
            /**
             * Specifies the SKU for the virtual machines in the scale set.
             *
             * @param skuType the SKU type
             * @return the next stage of the definition
             */
            WithNetworkSubnet withSku(VirtualMachineScaleSetSkuTypes skuType);

            /**
             * Specifies the SKU for the virtual machines in the scale set.
             *
             * @param sku a SKU from the list of available sizes for the virtual machines in this scale set
             * @return the next stage of the definition
             */
            WithNetworkSubnet withSku(VirtualMachineScaleSetSku sku);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to specify the virtual network subnet for the
         * primary network configuration.
         */
        interface WithNetworkSubnet {
            /**
             * Associate an existing virtual network subnet with the primary network interface of the virtual machines
             * in the scale set.
             *
             * @param network an existing virtual network
             * @param subnetName the subnet name
             * @return the next stage of the definition
             */
            WithPrimaryInternetFacingLoadBalancer withExistingPrimaryNetworkSubnet(Network network, String subnetName);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to specify an Internet-facing load balancer for
         * the primary network interface of the virtual machines in the scale set.
         */
        interface WithPrimaryInternetFacingLoadBalancer {
            /**
             * Specifies an Internet-facing load balancer whose backends and/or NAT pools can be assigned to the primary
             * network interfaces of the virtual machines in the scale set.
             * <p>
             * By default, all the backends and inbound NAT pools of the load balancer will be associated with the primary
             * network interface of the scale set virtual machines.
             * <p>
             * @param loadBalancer an existing Internet-facing load balancer
             * @return the next stage of the definition
             */
            WithPrimaryInternetFacingLoadBalancerBackendOrNatPool withExistingPrimaryInternetFacingLoadBalancer(LoadBalancer loadBalancer);

            /**
             * Specifies that no public load balancer should be associated with the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithPrimaryInternalLoadBalancer withoutPrimaryInternetFacingLoadBalancer();
        }

        /**
         * The stage of a virtual machine scale set definition allowing to specify an internal load balancer for
         * the primary network interface of the virtual machines in the scale set.
         */
        interface WithPrimaryInternalLoadBalancer {
            /**
             * Specifies the internal load balancer whose backends and/or NAT pools can be assigned to the primary network
             * interface of the virtual machines in the scale set.
             * <p>
             * By default all the backends and inbound NAT pools of the load balancer will be associated with the primary
             * network interface of the virtual machines in the scale set, unless subset of them is selected in the next stages.
             * <p>
             * @param loadBalancer an existing internal load balancer
             * @return the next stage of the definition
             */
            WithInternalLoadBalancerBackendOrNatPool withExistingPrimaryInternalLoadBalancer(LoadBalancer loadBalancer);

            /**
             * Specifies that no internal load balancer should be associated with the primary network interfaces of the
             * virtual machines in the scale set.
             *
             * @return the next stage of the definition
             */
            WithOS withoutPrimaryInternalLoadBalancer();
        }

        /**
         * The stage of a virtual machine scale set definition allowing to associate a backend pool and/or an inbound NAT pool
         * of the selected Internet-facing load balancer with the primary network interface of the virtual machines in the scale set.
         */
        interface WithPrimaryInternetFacingLoadBalancerBackendOrNatPool extends WithPrimaryInternetFacingLoadBalancerNatPool {
            /**
             * Associates the specified backends of the selected load balancer with the primary network interface of the
             * virtual machines in the scale set.
             *
             * @param backendNames the names of existing backends in the selected load balancer
             * @return the next stage of the definition
             */
            WithPrimaryInternetFacingLoadBalancerNatPool withPrimaryInternetFacingLoadBalancerBackends(String...backendNames);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to associate an inbound NAT pool of the selected
         * Internet-facing load balancer with the primary network interface of the virtual machines in the scale set.
         */
        interface WithPrimaryInternetFacingLoadBalancerNatPool extends WithPrimaryInternalLoadBalancer {
            /**
             * Associates the specified inbound NAT pools of the selected internal load balancer with the primary network
             * interface of the virtual machines in the scale set.
             *
             * @param natPoolNames inbound NAT pools names existing on the selected load balancer
             * @return the next stage of the definition
             */
            WithPrimaryInternalLoadBalancer withPrimaryInternetFacingLoadBalancerInboundNatPools(String...natPoolNames);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to associate backend pools and/or inbound NAT pools
         * of the selected internal load balancer with the primary network interface of the virtual machines in the scale set.
         */
        interface WithInternalLoadBalancerBackendOrNatPool extends WithInternalInternalLoadBalancerNatPool {
            /**
             * Associates the specified backends of the selected load balancer with the primary network interface of the
             * virtual machines in the scale set.
             *
             * @param backendNames names of existing backends in the selected load balancer
             * @return the next stage of the definition
             */
            WithInternalInternalLoadBalancerNatPool withPrimaryInternalLoadBalancerBackends(String...backendNames);
         }

        /**
         * The stage of the virtual machine scale set definition allowing to associate inbound NAT pools of the selected
         * internal load balancer with the primary network interface of the virtual machines in the scale set.
         */
        interface WithInternalInternalLoadBalancerNatPool extends WithOS {
            /**
             * Associate internal load balancer inbound NAT pools with the the primary network interface of the
             * scale set virtual machine.
             *
             *
             * @param natPoolNames inbound NAT pool names
             * @return the next stage of the definition
             */
            WithOS withPrimaryInternalLoadBalancerInboundNatPools(String...natPoolNames);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify the operating system image.
         */
        interface WithOS {
            /**
             * Specifies a known marketplace Windows image used as the operating system for the virtual machines in the scale set.
             *
             * @param knownImage a known market-place image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManagedOrUnmanaged withPopularWindowsImage(KnownWindowsVirtualMachineImage knownImage);

            /**
             * Specifies that the latest version of the specified marketplace Windows image should be used.
             *
             * @param publisher specifies the publisher of the image
             * @param offer specifies the offer of the image
             * @param sku specifies the SKU of the image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManagedOrUnmanaged withLatestWindowsImage(String publisher, String offer, String sku);

            /**
             * Specifies the specific version of a marketplace Windows image needs to be used.
             *
             * @param imageReference describes publisher, offer, SKU and version of the marketplace image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManagedOrUnmanaged withSpecificWindowsImageVersion(ImageReference imageReference);

            /**
             * Specifies the ID of a Windows custom image to be used.
             *
             * @param customImageId the resource ID of the custom image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManaged withWindowsCustomImage(String customImageId);

            /**
             * Specifies the user (custom) Windows image to be used as the operating system for the virtual machines in the
             * scale set.
             *
             * @param imageUrl the URL of the VHD
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameUnmanaged withStoredWindowsImage(String imageUrl);

            /**
             * Specifies a known marketplace Linux image used as the virtual machine's operating system.
             *
             * @param knownImage a known market-place image
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameManagedOrUnmanaged withPopularLinuxImage(KnownLinuxVirtualMachineImage knownImage);

            /**
             * Specifies that the latest version of a marketplace Linux image should be used.
             *
             * @param publisher the publisher of the image
             * @param offer the offer of the image
             * @param sku the SKU of the image
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameManagedOrUnmanaged withLatestLinuxImage(String publisher, String offer, String sku);

            /**
             * Specifies the specific version of a market-place Linux image that should be used.
             *
             * @param imageReference describes the publisher, offer, SKU and version of the market-place image
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameManagedOrUnmanaged withSpecificLinuxImageVersion(ImageReference imageReference);

            /**
             * Specifies the ID of a Linux custom image to be used.
             *
             * @param customImageId the resource ID of the custom image
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameManaged withLinuxCustomImage(String customImageId);

            /**
             * Specifies the user (custom) Linux image used as the virtual machine's operating system.
             *
             * @param imageUrl the URL the the VHD
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameUnmanaged withStoredLinuxImage(String imageUrl);
        }

        /**
         * The stage of the Linux virtual machine scale set definition allowing to specify SSH root user name.
         */
        interface WithLinuxRootUsernameManagedOrUnmanaged {
            /**
             * Specifies the SSH root user name for the Linux virtual machine.
             *
             * @param rootUserName a root user name following the required naming convention for Linux user names
             * @return the next stage of the definition
             */
            WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged withRootUsername(String rootUserName);
        }

        /**
         * The stage of the Linux virtual machine scale set definition allowing to specify SSH root user name.
         */
        interface WithLinuxRootUsernameManaged {
            /**
             * Specifies the SSH root user name for the Linux virtual machine.
             *
             * @param rootUserName a root user name following the required naming conventions for Linux user names
             * @return the next stage of the definition
             */
            WithLinuxRootPasswordOrPublicKeyManaged withRootUsername(String rootUserName);
        }

        /**
         * The stage of the Linux virtual machine scale set definition allowing to specify SSH root user name.
         */
        interface WithLinuxRootUsernameUnmanaged {
            /**
             * Specifies the SSH root user name for the Linux virtual machine.
             *
             * @param rootUserName a root user name following the required naming convention for Linux user names
             * @return the next stage of the definition
             */
            WithLinuxRootPasswordOrPublicKeyUnmanaged withRootUsername(String rootUserName);
        }

        /**
         * The stage of the Linux virtual machine scale set definition allowing to specify SSH root password or public key.
         */
        interface WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged {
            /**
             * Specifies the SSH root password for the Linux virtual machine.
             *
             * @param rootPassword a password following the complexity criteria for Azure Linux VM passwords
             * @return the next stage of the definition
             */
            WithLinuxCreateManagedOrUnmanaged withRootPassword(String rootPassword);

            /**
             * Specifies the SSH public key.
             * <p>
             * Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the next stage of the definition
             */
            WithLinuxCreateManagedOrUnmanaged withSsh(String publicKey);
        }

        /**
         * The stage of the Linux virtual machine scale set definition allowing to specify SSH root password or public key.
         */
        interface WithLinuxRootPasswordOrPublicKeyManaged {
            /**
             * Specifies the SSH root password for the Linux virtual machine.
             *
             * @param rootPassword a password following the complexity criteria for Azure Linux VM passwords
             * @return the next stage of the definition
             */
            WithLinuxCreateManaged withRootPassword(String rootPassword);

            /**
             * Specifies the SSH public key.
             * <p>
             * Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the next stage of the definition
             */
            WithLinuxCreateManaged withSsh(String publicKey);
        }

        /**
         * The stage of the Linux virtual machine scale set definition allowing to specify SSH root password or public key.
         */
        interface WithLinuxRootPasswordOrPublicKeyUnmanaged {
            /**
             * Specifies the SSH root password for the Linux virtual machine.
             *
             * @param rootPassword a password following the complexity criteria for Azure Linux VM passwords
             * @return the next stage of the definition
             */
            WithLinuxCreateUnmanaged withRootPassword(String rootPassword);

            /**
             * Specifies the SSH public key.
             * <p>
             * Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the next stage of the definition
             */
            WithLinuxCreateUnmanaged withSsh(String publicKey);
        }

        /**
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
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
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
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
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
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
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
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
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
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
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
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
         * The stage of a Linux virtual machine scale set definition which contains all the minimum required inputs
         * for the resource to be created, but also allows for any other optional
         * settings to be specified.
         *
         */
        interface WithLinuxCreateManagedOrUnmanaged extends WithManagedCreate {
            /**
             * Specifies the SSH public key.
             * <p>
             * Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey an SSH public key in the PEM format.
             * @return the next stage of the definition
             */
            WithLinuxCreateManagedOrUnmanaged withSsh(String publicKey);

            /**
             * @return the next stage of a unmanaged disk based virtual machine scale set definition
             */
            WithUnmanagedCreate withUnmanagedDisks();
        }

        /**
         * The stage of a Linux virtual machine scale set definition which contains all the minimum required inputs
         * for the resource to be created, but also allows for any other optional
         * settings to be specified.
         *
         */
        interface WithLinuxCreateManaged extends WithManagedCreate {
            /**
             * Specifies the SSH public key.
             * <p>
             * Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey an SSH public key in the PEM format.
             * @return the next stage of the definition
             */
            WithLinuxCreateManaged withSsh(String publicKey);
        }

        /**
         * The stage of a Linux virtual machine scale set definition which contains all the minimum required inputs
         * for the resource to be created, but also allows for any other optional
         * settings to be specified.
         *
         */
        interface WithLinuxCreateUnmanaged extends WithUnmanagedCreate {
            /**
             * Specifies the SSH public key.
             * <p>
             * Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey an SSH public key in the PEM format.
             * @return the next stage of the definition
             */
            WithLinuxCreateUnmanaged withSsh(String publicKey);
        }

        /**
         * The stage of a Windows virtual machine scale set definition which contains all the minimum required
         * inputs for the resource to be created, but also allows for any other
         * optional settings to be specified.
         *
         */
        interface WithWindowsCreateManagedOrUnmanaged extends WithWindowsCreateManaged {
            WithWindowsCreateUnmanaged withUnmanagedDisks();
        }

        /**
         * The stage of a Windows virtual machine scale set definition which contains all the minimum required
         * inputs for the resource to be created, but also allows for any other
         * optional settings to be specified.
         *
         */
        interface WithWindowsCreateManaged extends WithManagedCreate {
            /**
             * Enables the VM agent.
             *
             * @return the next stage of the definition
             */
            WithWindowsCreateManaged withVMAgent();

            /**
             * Disables the VM agent.
             *
             * @return the next stage of the definition
             */
            WithWindowsCreateManaged withoutVMAgent();

            /**
             * Enables automatic updates.
             *
             * @return the next stage of the definition
             */
            WithWindowsCreateManaged withAutoUpdate();

            /**
             * Disables automatic updates.
             *
             * @return the next stage of the definition
             */
            WithWindowsCreateManaged withoutAutoUpdate();

            /**
             * Specifies the time zone for the virtual machines to use.
             *
             * @param timeZone a time zone
             * @return the next stage of the definition
             */
            WithWindowsCreateManaged withTimeZone(String timeZone);

            /**
             * Specifies the WinRM listener.
             * <p>
             * Each call to this method adds the given listener to the list of VM's WinRM listeners.
             *
             * @param listener a WinRM listener
             * @return the next stage of the definition
             */
            WithWindowsCreateManaged withWinRM(WinRMListener listener);
        }

        /**
         * The stage of a Windows virtual machine scale set definition which contains all the minimum required
         * inputs for the resource to be created, but also allows for any other
         * optional settings to be specified.
         *
         */
        interface WithWindowsCreateUnmanaged extends WithUnmanagedCreate {
            /**
             * Enables the VM agent.
             *
             * @return the next stage of the definition
             */
            WithWindowsCreateUnmanaged withVMAgent();

            /**
             * Disables the VM agent.
             *
             * @return the next stage of the definition
             */
            WithWindowsCreateUnmanaged withoutVMAgent();

            /**
             * Enables automatic updates.
             *
             * @return the next stage of the definition
             */
            WithWindowsCreateUnmanaged withAutoUpdate();

            /**
             * Disables automatic updates.
             *
             * @return the next stage of the definition
             */
            WithWindowsCreateUnmanaged withoutAutoUpdate();

            /**
             * Specifies the time zone for the virtual machines to use.
             *
             * @param timeZone a time zone
             * @return the next stage of the definition
             */
            WithWindowsCreateUnmanaged withTimeZone(String timeZone);

            /**
             * Specifies the WinRM listener.
             * <p>
             * Each call to this method adds the given listener to the list of VM's WinRM listeners.
             *
             * @param listener a WinRM listener
             * @return the next stage of the definition
             */
            WithWindowsCreateUnmanaged withWinRM(WinRMListener listener);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to specify managed data disks.
         */
        interface WithManagedDataDisk {
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
             * @param lun the disk LUN
             * @param cachingType the caching type
             * @return the next stage of virtual machine definition
             */
            WithManagedCreate withNewDataDisk(int sizeInGB, int lun, CachingTypes cachingType);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given settings.
             *
             * @param sizeInGB the size of the managed disk
             * @param lun the disk LUN
             * @param cachingType the caching type
             * @param storageAccountType the storage account type
             * @return the next stage of virtual machine definition
             */
            WithManagedCreate withNewDataDisk(int sizeInGB,
                                              int lun,
                                              CachingTypes cachingType,
                                              StorageAccountTypes storageAccountType);

            /**
             * Specifies the data disk to be created from the data disk image in the virtual machine image.
             *
             * @param imageLun the LUN of the source data disk image
             * @return the next stage of virtual machine definition
             */
            WithManagedCreate withNewDataDiskFromImage(int imageLun);

            /**
             * Specifies the data disk to be created from the data disk image in the virtual machine image.
             *
             * @param imageLun the LUN of the source data disk image
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
             * @param imageLun the LUN of the source data disk image
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
         * The optionals applicable only for managed disks.
         */
        interface WithManagedDiskOptionals {
            /**
             * Specifies the storage account type for managed OS disk.
             *
             * @param accountType the storage account type
             * @return  the stage representing creatable VM definition
             */
            WithManagedCreate withOSDiskStorageAccountType(StorageAccountTypes accountType);

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
         * The stage of the virtual machine scale set definition allowing to specify availability zone.
         */
        @Beta(Beta.SinceVersion.V1_3_0)
        interface WithAvailabilityZone {
            /**
             * Specifies the availability zone for the virtual machine scale set.
             *
             * @param zoneId the zone identifier. The valid values are "1", "2", and "3"
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_3_0)
            WithManagedCreate withAvailabilityZone(String zoneId);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the VM scale set to be
         * created and optionally allow managed data disks specific settings to be specified.
         */
        interface WithManagedCreate
                extends
                WithManagedDataDisk,
                WithManagedDiskOptionals,
                WithAvailabilityZone,
                WithCreate {
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify unmanaged data disk.
         */
        interface WithUnmanagedDataDisk {
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the VM scale set to be
         * created and optionally allow unmanaged data disks specific settings to be specified.
         */
        interface WithUnmanagedCreate
                extends WithUnmanagedDataDisk, WithCreate {
        }

        /**
         * The stage of a virtual machine scale set definition allowing to specify the computer name prefix.
         */
        interface WithComputerNamePrefix {
            /**
             * Specifies the name prefix to use for auto-generating the names for the virtual machines in the scale set.
             *
             * @param namePrefix the prefix for the auto-generated names of the virtual machines in the scale set
             * @return the next stage of the definition
             */
            WithCreate withComputerNamePrefix(String namePrefix);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify number of
         * virtual machines in the scale set.
         */
        interface WithCapacity {
            /**
             * Specifies the maximum number of virtual machines in the scale set.
             *
             * @param capacity number of virtual machines
             * @return the next stage of the definition
             */
            WithCreate withCapacity(int capacity);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to specify the upgrade policy.
         */
        interface WithUpgradePolicy {
            /**
             * Specifies the virtual machine scale set upgrade policy mode.
             *
             * @param upgradeMode an upgrade policy mode
             * @return the next stage of the definition
             */
            WithCreate withUpgradeMode(UpgradeMode upgradeMode);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to specify whether
         * or not to over-provision virtual machines in the scale set.
         */
        interface WithOverProvision {
            /**
             * Enables or disables over-provisioning of virtual machines in the scale set.
             *
             * @param enabled true if enabling over-0provisioning of virtual machines in the
             *                scale set, otherwise false
             * @return the next stage of the definition
             */
            WithCreate withOverProvision(boolean enabled);

            /**
             * Enables over-provisioning of virtual machines.
             *
             * @return the next stage of the definition
             */
            WithCreate withOverProvisioning();

            /**
             * Disables over-provisioning of virtual machines.
             *
             * @return the next stage of the definition
             */
            WithCreate withoutOverProvisioning();
        }

        /**
         * The stage of a virtual machine scale set definition allowing to specify OS disk configurations.
         */
        interface WithOSDiskSettings {
            /**
             * Specifies the caching type for the operating system disk.
             *
             * @param cachingType the caching type
             * @return the next stage of the definition
             */
            WithCreate withOSDiskCaching(CachingTypes cachingType);

            /**
             * Specifies the name for the OS disk.
             *
             * @param name the OS disk name
             * @return the next stage of the definition
             */
            WithCreate withOSDiskName(String name);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to specify the storage account.
         */
        interface WithStorageAccount {
            /**
             * Specifies a new storage account for the OS and data disk VHDs of the virtual machines
             * in the scale set.
             *
             * @param name the name of the storage account
             * @return the next stage of the definition
             */
            WithCreate withNewStorageAccount(String name);

            /**
             * Specifies a new storage account for the OS and data disk VHDs of the virtual machines
             * in the scale set.
             *
             * @param creatable the storage account definition in a creatable stage
             * @return the next stage in the definition
             */
            WithCreate withNewStorageAccount(Creatable<StorageAccount> creatable);

            /**
             * Specifies an existing storage account for the OS and data disk VHDs of
             * the virtual machines in the scale set.
             *
             * @param storageAccount an existing storage account
             * @return the next stage in the definition
             */
            WithCreate withExistingStorageAccount(StorageAccount storageAccount);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify the custom data.
         */
        interface WithCustomData {
            /**
             * Specifies the custom data for the virtual machine scale set.
             *
             * @param base64EncodedCustomData the base64 encoded custom data
             * @return the next stage in the definition
             */
            WithCreate withCustomData(String base64EncodedCustomData);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to specify extensions.
         */
        interface WithExtension {
            /**
             * Begins the definition of an extension reference to be attached to the virtual machines in the scale set.
             *
             * @param name the reference name for the extension
             * @return the first stage of the extension reference definition
             */
            VirtualMachineScaleSetExtension.DefinitionStages.Blank<WithCreate> defineNewExtension(String name);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to enable Managed Service Identity.
         */
        @Beta(Beta.SinceVersion.V1_2_0)
        interface WithManagedServiceIdentity {
            /**
             * Specifies that Managed Service Identity needs to be enabled in the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrCreate withManagedServiceIdentity();

            /**
             * Specifies that Managed Service Identity needs to be enabled in the virtual machine scale set.
             *
             * @param tokenPort the port on the virtual machine scale set instance where access token is available
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrCreate withManagedServiceIdentity(int tokenPort);
        }

        /**
         * The stage of the Managed Service Identity enabled virtual machine scale set allowing to set role
         * assignment for a scope.
         */
        @Beta(Beta.SinceVersion.V1_2_0)
        interface WithRoleAndScopeOrCreate extends WithCreate {
            /**
             * Specifies that applications running on the virtual machine scale set instance requires the given
             * access role with scope of access limited to the ARM resource identified by the resource id
             * specified in the scope parameter.
             *
             * @param scope scope of the access represented in ARM resource ID format
             * @param asRole access role to assigned to the virtual machine scale set
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrCreate withRoleBasedAccessTo(String scope, BuiltInRole asRole);

            /**
             * Specifies that applications running on the virtual machine scale set instance requires the given access
             * role with scope of access limited to the current resource group that the virtual machine scale set resides.
             *
             * @param asRole access role to assigned to the virtual machine scale set
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrCreate withRoleBasedAccessToCurrentResourceGroup(BuiltInRole asRole);

            /**
             * Specifies that applications running on the virtual machine scale set instance requires the access
             * described in the given role definition with scope of access limited to the ARM resource identified by
             * the resource ID specified in the scope parameter.
             *
             * @param scope scope of the access represented in ARM resource ID format
             * @param roleDefinitionId role definition to assigned to the virtual machine scale set
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrCreate withRoleDefinitionBasedAccessTo(String scope, String roleDefinitionId);

            /**
             * Specifies that applications running on the virtual machine scale set instance requires the access
             * described in the given role definition with scope of access limited to the current resource group
             * that the virtual machine scale set resides.
             *
             * @param roleDefinitionId role definition to assigned to the virtual machine scale set
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrCreate withRoleDefinitionBasedAccessToCurrentResourceGroup(String roleDefinitionId);
        }

        /**
         * The stage of a virtual machine scale set definition containing all the required inputs for the resource
         * to be created, but also allowing for any other optional settings
         * to be specified.
         */
        interface WithCreate extends
                Creatable<VirtualMachineScaleSet>,
                DefinitionStages.WithOSDiskSettings,
                DefinitionStages.WithComputerNamePrefix,
                DefinitionStages.WithCapacity,
                DefinitionStages.WithUpgradePolicy,
                DefinitionStages.WithOverProvision,
                DefinitionStages.WithStorageAccount,
                DefinitionStages.WithCustomData,
                DefinitionStages.WithExtension,
                DefinitionStages.WithManagedServiceIdentity,
                Resource.DefinitionWithTags<VirtualMachineScaleSet.DefinitionStages.WithCreate> {
        }
    }

    /**
     * Grouping of virtual machine scale set update stages.
     */
    interface UpdateStages {
        /**
         * The stage of a virtual machine scale set update allowing to specify load balancers for the primary
         * network interface of the scale set virtual machines.
         */
        interface WithPrimaryLoadBalancer extends WithPrimaryInternalLoadBalancer {
            /**
             * Specifies the load balancer to be used as the Internet-facing load balancer for the virtual machines in the
             * scale set.
             * <p>
             * This will replace the current Internet-facing load balancer associated with the virtual machines in the
             * scale set (if any).
             * By default all the backend and inbound NAT pool of the load balancer will be associated with the primary
             * network interface of the virtual machines unless a subset of them is selected in the next stages
             * @param loadBalancer the primary Internet-facing load balancer
             * @return the next stage of the update
             */
            WithPrimaryInternetFacingLoadBalancerBackendOrNatPool withExistingPrimaryInternetFacingLoadBalancer(LoadBalancer loadBalancer);
        }

        /**
         * The stage of a virtual machine scale set update allowing to associate a backend pool and/or inbound NAT pool
         * of the selected Internet-facing load balancer with the primary network interface of the virtual machines in
         * the scale set.
         */
        interface WithPrimaryInternetFacingLoadBalancerBackendOrNatPool extends WithPrimaryInternetFacingLoadBalancerNatPool {
            /**
             * Associates the specified Internet-facing load balancer backends with the primary network interface of the
             * virtual machines in the scale set.
             *
             * @param backendNames the backend names
             * @return the next stage of the update
             */
            WithPrimaryInternetFacingLoadBalancerNatPool withPrimaryInternetFacingLoadBalancerBackends(String...backendNames);
        }

        /**
         * The stage of a virtual machine scale set update allowing to associate an inbound NAT pool of the selected
         * Internet-facing load balancer with the primary network interface of the virtual machines in the scale set.
         */
        interface WithPrimaryInternetFacingLoadBalancerNatPool extends WithPrimaryInternalLoadBalancer {
            /**
             * Associates inbound NAT pools of the selected Internet-facing load balancer with the primary network interface
             * of the virtual machines in the scale set.
             *
             * @param natPoolNames the names of existing inbound NAT pools on the selected load balancer
             * @return the next stage of the update
             */
            WithPrimaryInternalLoadBalancer withPrimaryInternetFacingLoadBalancerInboundNatPools(String...natPoolNames);
        }

        /**
         * The stage of a virtual machine scale set update allowing to specify an internal load balancer for
         * the primary network interface of the scale set virtual machines.
         */
        interface WithPrimaryInternalLoadBalancer extends WithApply {
            /**
             * Specifies the load balancer to be used as the internal load balancer for the virtual machines in the
             * scale set.
             * <p>
             * This will replace the current internal load balancer associated with the virtual machines in the
             * scale set (if any).
             * By default all the backends and inbound NAT pools of the load balancer will be associated with the primary
             * network interface of the virtual machines in the scale set unless subset of them is selected in the next stages.
             * </p>
             * @param loadBalancer the primary Internet-facing load balancer
             * @return the next stage of the update
             */
            WithPrimaryInternalLoadBalancerBackendOrNatPool withExistingPrimaryInternalLoadBalancer(LoadBalancer loadBalancer);
        }

        /**
         * The stage of a virtual machine scale set update allowing to associate backend pools and/or inbound NAT pools
         * of the selected internal load balancer with the primary network interface of the scale set virtual machines.
         */
        interface WithPrimaryInternalLoadBalancerBackendOrNatPool extends WithPrimaryInternalLoadBalancerNatPool {
            /**
             * Associates the specified internal load balancer backends with the primary network interface of the
             * virtual machines in the scale set.
             *
             * @param backendNames the names of existing backends on the selected load balancer
             * @return the next stage of the update
             */
            WithPrimaryInternalLoadBalancerNatPool withPrimaryInternalLoadBalancerBackends(String...backendNames);
        }

        /**
         * The stage of a virtual machine scale set update allowing to associate inbound NAT pools of the selected internal
         * load balancer with the primary network interface of the virtual machines in the scale set.
         */
        interface WithPrimaryInternalLoadBalancerNatPool  extends WithApply {
            /**
             * Associates the specified internal load balancer inbound NAT pools with the the primary network interface of
             * the virtual machines in the scale set.
             *
             * @param natPoolNames the names of existing inbound NAT pools in the selected load balancer
             * @return the next stage of the update
             */
            WithApply withPrimaryInternalLoadBalancerInboundNatPools(String...natPoolNames);
        }

        /**
         * The stage of a virtual machine scale set update allowing to change the SKU for the virtual machines
         * in the scale set.
         */
        interface WithSku {
            /**
             * Specifies the SKU for the virtual machines in the scale set.
             *
             * @param skuType the SKU type
             * @return the next stage of the update
             */
            WithApply withSku(VirtualMachineScaleSetSkuTypes skuType);

            /**
             * Specifies the SKU for the virtual machines in the scale set.
             *
             * @param sku a SKU from the list of available sizes for the virtual machines in this scale set
             * @return the next stage of the update
             */
            WithApply withSku(VirtualMachineScaleSetSku sku);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to specify the number of
         * virtual machines in the scale set.
         */
        interface WithCapacity {
            /**
             * Specifies the new number of virtual machines in the scale set.
             *
             * @param capacity the virtual machine capacity of the scale set
             * @return the next stage of the update
             */
            WithApply withCapacity(int capacity);
        }

        /**
         * The stage of the virtual machine definition allowing to specify extensions.
         */
        interface WithExtension {
            /**
             * Begins the definition of an extension reference to be attached to the virtual machines in the scale set.
             *
             * @param name the reference name for an extension
             * @return the first stage of the extension reference definition
             */
            VirtualMachineScaleSetExtension
                    .UpdateDefinitionStages
                    .Blank<WithApply> defineNewExtension(String name);

            /**
             * Begins the description of an update of an existing extension assigned to the virtual machines in the scale set.
             *
             * @param name the reference name for the extension
             * @return the first stage of the extension reference update
             */
            VirtualMachineScaleSetExtension.Update updateExtension(String name);

            /**
             * Removes the extension with the specified name from the virtual machines in the scale set.
             *
             * @param name the reference name of the extension to be removed/uninstalled
             * @return the next stage of the update
             */
            WithApply withoutExtension(String name);
        }

        /**
         * The stage of a virtual machine scale set update allowing to remove the public and internal load balancer
         * from the primary network interface configuration.
         */
        interface WithoutPrimaryLoadBalancer {
            /**
             * Removes the association between the Internet-facing load balancer and the primary network interface configuration.
             * <p>
             * This removes the association between primary network interface configuration and all the backends and
             * inbound NAT pools in the load balancer.
             *
             * @return the next stage of the update
             */
            WithApply withoutPrimaryInternetFacingLoadBalancer();

            /**
             * Removes the association between the internal load balancer and the primary network interface configuration.
             * <p>
             * This removes the association between primary network interface configuration and all the backends and
             * inbound NAT pools in the load balancer.
             *
             * @return the next stage of the update
             */
            WithApply withoutPrimaryInternalLoadBalancer();
        }

        /**
         * The stage of a virtual machine scale set update allowing to remove the association between the primary network
         * interface configuration and a backend of a load balancer.
         */
        interface WithoutPrimaryLoadBalancerBackend {
            /**
             * Removes the associations between the primary network interface configuration and the specfied backends
             * of the Internet-facing load balancer.
             *
             * @param backendNames existing backend names
             * @return the next stage of the update
             */
            WithApply withoutPrimaryInternetFacingLoadBalancerBackends(String...backendNames);

            /**
             * Removes the associations between the primary network interface configuration and the specified backends
             * of the internal load balancer.
             *
             * @param backendNames existing backend names
             * @return the next stage of the update
             */
            WithApply withoutPrimaryInternalLoadBalancerBackends(String...backendNames);
        }

        /**
         * A stage of the virtual machine scale set update allowing to remove the associations between the primary network
         * interface configuration and the specified inbound NAT pools of the load balancer.
         */
        interface WithoutPrimaryLoadBalancerNatPool {
            /**
             * Removes the associations between the primary network interface configuration and the specified inbound NAT pools
             * of an Internet-facing load balancer.
             *
             * @param natPoolNames the names of existing inbound NAT pools
             * @return the next stage of the update
             */
            WithApply withoutPrimaryInternetFacingLoadBalancerNatPools(String...natPoolNames);

            /**
             * Removes the associations between the primary network interface configuration and the specified inbound NAT pools
             * of the internal load balancer.
             *
             * @param natPoolNames the names of existing inbound NAT pools
             * @return the next stage of the update
             */
            WithApply withoutPrimaryInternalLoadBalancerNatPools(String...natPoolNames);
        }

        /**
         * The stage of the virtual machine scale set update allowing to enable Managed Service Identity.
         */
        @Beta(Beta.SinceVersion.V1_2_0)
        interface WithManagedServiceIdentity {
            /**
             * Specifies that Managed Service Identity needs to be enabled in the virtual machine scale set.
             *
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrApply withManagedServiceIdentity();

            /**
             * Specifies that Managed Service Identity needs to be enabled in the virtual machine scale set.
             *
             * @param tokenPort the port on the virtual machine scale set instance where access token is available
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrApply withManagedServiceIdentity(int tokenPort);
        }

        /**
         * The stage of the Managed Service Identity enabled virtual machine allowing to set role
         * assignment for a scope.
         */
        @Beta(Beta.SinceVersion.V1_2_0)
        interface WithRoleAndScopeOrApply extends WithApply {
            /**
             * Specifies that applications running on the virtual machine scale set instance requires
             * the given access role with scope of access limited to the ARM resource identified by
             * the resource ID specified in the scope parameter.
             *
             * @param scope scope of the access represented in ARM resource ID format
             * @param asRole access role to assigned to the virtual machine scale set
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrApply withRoleBasedAccessTo(String scope, BuiltInRole asRole);

            /**
             * Specifies that applications running on the virtual machine scale set instance requires
             * the given access role with scope of access limited to the current resource group that
             * the virtual machine scale set resides.
             *
             * @param asRole access role to assigned to the virtual machine scale set
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrApply withRoleBasedAccessToCurrentResourceGroup(BuiltInRole asRole);

            /**
             * Specifies that applications running on the virtual machine scale set instance requires the access
             * described in the given role definition with scope of access limited to the ARM resource identified by
             * the resource ID specified in the scope parameter.
             *
             * @param scope scope of the access represented in ARM resource ID format
             * @param roleDefinitionId role definition to assigned to the virtual machine scale set
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrApply withRoleDefinitionBasedAccessTo(String scope, String roleDefinitionId);

            /**
             * Specifies that applications running on the virtual machine scale set instance requires the access
             * described in the given role definition with scope of access limited to the current resource group
             * that the virtual machine scale set resides.
             *
             * @param roleDefinitionId role definition to assigned to the virtual machine scale set
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_2_0)
            WithRoleAndScopeOrApply withRoleDefinitionBasedAccessToCurrentResourceGroup(String roleDefinitionId);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify unmanaged data disk.
         */
        interface WithUnmanagedDataDisk {
        }

        /**
         * The stage of a virtual machine scale set update allowing to specify managed data disks.
         */
        interface WithManagedDataDisk {
            /**
             * Specifies that a managed disk needs to be created implicitly with the given size.
             *
             * @param sizeInGB the size of the managed disk
             * @return the next stage of virtual machine scale set update
             */
            WithApply withNewDataDisk(int sizeInGB);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given settings.
             *
             * @param sizeInGB the size of the managed disk
             * @param lun the disk LUN
             * @param cachingType the caching type
             * @return the next stage of virtual machine scale set update
             */
            WithApply withNewDataDisk(int sizeInGB, int lun, CachingTypes cachingType);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given settings.
             *
             * @param sizeInGB the size of the managed disk
             * @param lun the disk LUN
             * @param cachingType the caching type
             * @param storageAccountType the storage account type
             * @return the next stage of virtual machine scale set update
             */
            WithApply withNewDataDisk(int sizeInGB,
                                      int lun,
                                      CachingTypes cachingType,
                                      StorageAccountTypes storageAccountType);

            /**
             * Detaches managed data disk with the given LUN from the virtual machine scale set instances.
             *
             * @param lun the disk LUN
             * @return the next stage of virtual machine scale set update
             */
            WithApply withoutDataDisk(int lun);

            /**
             * Updates the size of a managed data disk with the given LUN.
             *
             * @param lun the disk LUN
             * @param newSizeInGB the new size of the disk
             * @return the next stage of virtual machine scale set update
             */
            // TODO: Broken by Azure REST API
            // WithApply withDataDiskUpdated(int lun, int newSizeInGB);

            /**
             * Updates the size and caching type of a managed data disk with the given LUN.
             *
             * @param lun the disk LUN
             * @param newSizeInGB the new size of the disk
             * @param cachingType the caching type
             * @return the next stage of virtual machine scale set update
             */
            // TODO: Broken by Azure REST API
            // WithApply withDataDiskUpdated(int lun, int newSizeInGB, CachingTypes cachingType);

            /**
             * Updates the size, caching type and storage account type of a managed data disk with the given LUN.
             * @param lun the disk LUN
             * @param newSizeInGB the new size of the disk
             * @param cachingType the caching type
             * @param storageAccountType the storage account type
             * @return the next stage of virtual machine scale set update
             */
            // TODO: Broken by Azure REST API
            // WithApply withDataDiskUpdated(int lun,
            //                              int newSizeInGB,
            //                              CachingTypes cachingType,
            //                              StorageAccountTypes storageAccountType);
        }

        /**
         * The stage of the virtual machine scale set update allowing to specify availability zone.
         */
        @Beta(Beta.SinceVersion.V1_3_0)
        interface WithAvailabilityZone {
            /**
             * Specifies the availability zone for the virtual machine scale set.
             *
             * @param zoneId the zone identifier. The valid values are "1", "2", and "3"
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_3_0)
            WithApply withAvailabilityZone(String zoneId);
        }

        /**
         * The stage of a virtual machine scale set update containing inputs for the resource to be updated.
         */
        interface WithApply extends
                Appliable<VirtualMachineScaleSet>,
                Resource.UpdateWithTags<WithApply>,
                UpdateStages.WithManagedDataDisk,
                UpdateStages.WithUnmanagedDataDisk,
                UpdateStages.WithSku,
                UpdateStages.WithCapacity,
                UpdateStages.WithExtension,
                UpdateStages.WithoutPrimaryLoadBalancer,
                UpdateStages.WithoutPrimaryLoadBalancerBackend,
                UpdateStages.WithoutPrimaryLoadBalancerNatPool,
                UpdateStages.WithManagedServiceIdentity,
                UpdateStages.WithAvailabilityZone {
        }
    }

    /**
     * The entirety of the virtual machine scale set update.
     */
    interface Update extends
            UpdateStages.WithPrimaryLoadBalancer,
            UpdateStages.WithPrimaryInternetFacingLoadBalancerBackendOrNatPool,
            UpdateStages.WithPrimaryInternalLoadBalancerBackendOrNatPool {
    }
}