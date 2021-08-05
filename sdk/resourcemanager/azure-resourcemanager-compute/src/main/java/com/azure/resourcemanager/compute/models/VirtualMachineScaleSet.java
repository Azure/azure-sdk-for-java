// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineScaleSetInner;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.network.models.ApplicationSecurityGroup;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerBackend;
import com.azure.resourcemanager.network.models.LoadBalancerInboundNatPool;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.VirtualMachineScaleSetNetworkInterface;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.storage.models.StorageAccount;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** An immutable client-side representation of an Azure virtual machine scale set. */
@Fluent
public interface VirtualMachineScaleSet
    extends GroupableResource<ComputeManager, VirtualMachineScaleSetInner>,
        Refreshable<VirtualMachineScaleSet>,
        Updatable<VirtualMachineScaleSet.UpdateStages.WithPrimaryLoadBalancer> {
    // Actions
    /** @return entry point to manage virtual machine instances in the scale set. */
    VirtualMachineScaleSetVMs virtualMachines();

    /**
     * @return available SKUs for the virtual machine scale set, including the minimum and maximum virtual machine
     *     instances allowed for a particular SKU
     */
    PagedIterable<VirtualMachineScaleSetSku> listAvailableSkus();

    /** Shuts down the virtual machines in the scale set and releases its compute resources. */
    void deallocate();

    /**
     * Shuts down the virtual machines in the scale set and releases its compute resources asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> deallocateAsync();

    /** Powers off (stops) the virtual machines in the scale set. */
    void powerOff();

    /**
     * Powers off (stops) the virtual machines in the scale set asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> powerOffAsync();

    /** Restarts the virtual machines in the scale set. */
    void restart();

    /**
     * Restarts the virtual machines in the scale set asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> restartAsync();

    /** Starts the virtual machines in the scale set. */
    void start();

    /**
     * Starts the virtual machines in the scale set asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> startAsync();

    /** Re-images (updates the version of the installed operating system) the virtual machines in the scale set. */
    void reimage();

    /**
     * Re-images (updates the version of the installed operating system) the virtual machines in the scale set
     * asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> reimageAsync();

    /**
     * Run PowerShell script in a virtual machine instance in a scale set.
     *
     * @param vmId the virtual machine instance id
     * @param scriptLines PowerShell script lines
     * @param scriptParameters script parameters
     * @return result of PowerShell script execution
     */
    RunCommandResult runPowerShellScriptInVMInstance(
        String vmId, List<String> scriptLines, List<RunCommandInputParameter> scriptParameters);

    /**
     * Run PowerShell in a virtual machine instance in a scale set asynchronously.
     *
     * @param vmId the virtual machine instance id
     * @param scriptLines PowerShell script lines
     * @param scriptParameters script parameters
     * @return handle to the asynchronous execution
     */
    Mono<RunCommandResult> runPowerShellScriptInVMInstanceAsync(
        String vmId, List<String> scriptLines, List<RunCommandInputParameter> scriptParameters);

    /**
     * Run shell script in a virtual machine instance in a scale set.
     *
     * @param vmId the virtual machine instance id
     * @param scriptLines shell script lines
     * @param scriptParameters script parameters
     * @return result of shell script execution
     */
    RunCommandResult runShellScriptInVMInstance(
        String vmId, List<String> scriptLines, List<RunCommandInputParameter> scriptParameters);

    /**
     * Run shell script in a virtual machine instance in a scale set asynchronously.
     *
     * @param vmId the virtual machine instance id
     * @param scriptLines shell script lines
     * @param scriptParameters script parameters
     * @return handle to the asynchronous execution
     */
    Mono<RunCommandResult> runShellScriptInVMInstanceAsync(
        String vmId, List<String> scriptLines, List<RunCommandInputParameter> scriptParameters);

    /**
     * Run commands in a virtual machine instance in a scale set.
     *
     * @param vmId the virtual machine instance id
     * @param inputCommand command input
     * @return result of execution
     */
    RunCommandResult runCommandInVMInstance(String vmId, RunCommandInput inputCommand);

    /**
     * Run commands in a virtual machine instance in a scale set asynchronously.
     *
     * @param vmId the virtual machine instance id
     * @param inputCommand command input
     * @return handle to the asynchronous execution
     */
    Mono<RunCommandResult> runCommandVMInstanceAsync(String vmId, RunCommandInput inputCommand);

    // Getters
    /** @return the name prefix of the virtual machines in the scale set */
    String computerNamePrefix();

    /** @return the operating system of the virtual machines in the scale set */
    OperatingSystemTypes osType();

    /** @return the operating system disk caching type */
    CachingTypes osDiskCachingType();

    /** @return the name of the OS disk of virtual machines in the scale set */
    String osDiskName();

    /** @return the upgrade model */
    UpgradeMode upgradeModel();

    /** @return true if over provision is enabled for the virtual machines, false otherwise */
    boolean overProvisionEnabled();

    /** @return the SKU of the virtual machines in the scale set */
    VirtualMachineScaleSetSkuTypes sku();

    /** @return the number of virtual machine instances in the scale set */
    int capacity();

    /**
     * @return the virtual network associated with the primary network interfaces of the virtual machines in the scale
     *     set.
     *     <p>A primary internal load balancer associated with the primary network interfaces of the scale set virtual
     *     machine will be also belong to this network
     * @throws IOException the IO exception
     */
    Network getPrimaryNetwork() throws IOException;

    /**
     * @return the Internet-facing load balancer associated with the primary network interface of the virtual machines
     *     in the scale set.
     * @throws IOException the IO exception
     */
    LoadBalancer getPrimaryInternetFacingLoadBalancer() throws IOException;

    /**
     * @return the Internet-facing load balancer's backends associated with the primary network interface of the virtual
     *     machines in the scale set
     * @throws IOException the IO exception
     */
    Map<String, LoadBalancerBackend> listPrimaryInternetFacingLoadBalancerBackends() throws IOException;

    /**
     * @return the Internet-facing load balancer's inbound NAT pool associated with the primary network interface of the
     *     virtual machines in the scale set
     * @throws IOException the IO exception
     */
    Map<String, LoadBalancerInboundNatPool> listPrimaryInternetFacingLoadBalancerInboundNatPools() throws IOException;

    /**
     * @return the internal load balancer associated with the primary network interface of the virtual machines in the
     *     scale set
     * @throws IOException the IO exception
     */
    LoadBalancer getPrimaryInternalLoadBalancer() throws IOException;

    /**
     * @return the internal load balancer's backends associated with the primary network interface of the virtual
     *     machines in the scale set
     * @throws IOException the IO exception
     */
    Map<String, LoadBalancerBackend> listPrimaryInternalLoadBalancerBackends() throws IOException;

    /**
     * @return the inbound NAT pools of the internal load balancer associated with the primary network interface of the
     *     virtual machines in the scale set, if any.
     * @throws IOException the IO exception
     */
    Map<String, LoadBalancerInboundNatPool> listPrimaryInternalLoadBalancerInboundNatPools() throws IOException;

    /**
     * @return the list of IDs of the public IP addresses associated with the primary Internet-facing load balancer of
     *     the scale set
     * @throws IOException the IO exception
     */
    List<String> primaryPublicIpAddressIds() throws IOException;

    /** @return the URL to storage containers that store the VHDs of the virtual machines in the scale set */
    List<String> vhdContainers();

    /** @return the storage profile */
    VirtualMachineScaleSetStorageProfile storageProfile();

    /** @return the network profile */
    VirtualMachineScaleSetNetworkProfile networkProfile();

    /** @return the extensions attached to the virtual machines in the scale set */
    Map<String, VirtualMachineScaleSetExtension> extensions();

    /** @return the priority of virtual machines in the scale set. */
    VirtualMachinePriorityTypes virtualMachinePriority();

    /** @return the billing related details of the low priority virtual machines in the scale set. */
    BillingProfile billingProfile();

    /** @return the eviction policy of the virtual machines in the scale set. */
    VirtualMachineEvictionPolicyTypes virtualMachineEvictionPolicy();

    /**
     * Gets a network interface associated with a virtual machine scale set instance.
     *
     * @param instanceId the virtual machine scale set vm instance ID
     * @param name the network interface name
     * @return the network interface
     */
    VirtualMachineScaleSetNetworkInterface getNetworkInterfaceByInstanceId(String instanceId, String name);

    /** @return the network interfaces associated with all virtual machine instances in a scale set */
    PagedIterable<VirtualMachineScaleSetNetworkInterface> listNetworkInterfaces();

    /**
     * Lists the network interface associated with a specific virtual machine instance in the scale set.
     *
     * @param virtualMachineInstanceId the instance ID
     * @return the network interfaces
     */
    PagedIterable<VirtualMachineScaleSetNetworkInterface> listNetworkInterfacesByInstanceId(
        String virtualMachineInstanceId);

    /**
     * Lists the network interface associated with a specific virtual machine instance in the scale set asynchronously.
     *
     * @param virtualMachineInstanceId the instance ID
     * @return the network interfaces
     */
    PagedFlux<VirtualMachineScaleSetNetworkInterface> listNetworkInterfacesByInstanceIdAsync(
        String virtualMachineInstanceId);

    /** @return true if managed disk is used for the virtual machine scale set's disks (os, data) */
    boolean isManagedDiskEnabled();

    /** @return true if Managed Service Identity is enabled for the virtual machine scale set */
    boolean isManagedServiceIdentityEnabled();

    /**
     * @return the System Assigned (Local) Managed Service Identity specific Active Directory tenant ID assigned to the
     *     virtual machine scale set.
     */
    String systemAssignedManagedServiceIdentityTenantId();

    /**
     * @return the System Assigned (Local) Managed Service Identity specific Active Directory service principal ID
     *     assigned to the virtual machine scale set.
     */
    String systemAssignedManagedServiceIdentityPrincipalId();

    /** @return the type of Managed Service Identity used for the virtual machine scale set. */
    ResourceIdentityType managedServiceIdentityType();

    /**
     * @return the resource ids of User Assigned Managed Service Identities associated with the virtual machine scale
     *     set.
     */
    Set<String> userAssignedManagedServiceIdentityIds();

    /** @return the availability zones assigned to virtual machine scale set. */
    Set<AvailabilityZoneId> availabilityZones();

    /** @return true if boot diagnostics is enabled for the virtual machine scale set. */
    boolean isBootDiagnosticsEnabled();

    /** @return the storage blob endpoint uri if boot diagnostics is enabled for the virtual machine scale set. */
    String bootDiagnosticsStorageUri();

    /**
     * @return the storage account type of the OS managed disk. A null value will be returned if the virtual machine
     *     scale set is based on un-managed disk.
     */
    StorageAccountTypes managedOSDiskStorageAccountType();

    /** @return the public ip configuration of virtual machines in the scale set. */
    VirtualMachineScaleSetPublicIpAddressConfiguration virtualMachinePublicIpConfig();

    /** @return true if ip forwarding is enabled for the virtual machine scale set. */
    boolean isIpForwardingEnabled();

    /** @return true if accelerated networking is enabled for the virtual machine scale set. */
    boolean isAcceleratedNetworkingEnabled();

    /** @return the network security group ARM id. */
    String networkSecurityGroupId();

    /** @return true if single placement group is enabled for the virtual machine scale set. */
    boolean isSinglePlacementGroupEnabled();

    /** @return the list of application gateway backend pool associated with the virtual machine scale set. */
    List<String> applicationGatewayBackendAddressPoolsIds();

    /** @return the list of application security groups associated with the virtual machine scale set. */
    List<String> applicationSecurityGroupIds();

    /**
     * @return When Overprovision is enabled, extensions are launched only on the requested number of VMs which are
     *     finally kept. This property will hence ensure that the extensions do not run on the extra overprovisioned
     *     VMs.
     */
    Boolean doNotRunExtensionsOnOverprovisionedVMs();

    /**
     * Get specifies information about the proximity placement group that the virtual machine scale set should be
     * assigned to.
     *
     * @return the proximityPlacementGroup.
     */
    ProximityPlacementGroup proximityPlacementGroup();

    /**
     * Get specifies additional capabilities enabled or disabled on the Virtual Machines in the Virtual Machine Scale
     * Set. For instance: whether the Virtual Machines have the capability to support attaching managed data disks with
     * UltraSSD_LRS storage account type.
     *
     * @return the additionalCapabilities value
     */
    AdditionalCapabilities additionalCapabilities();

    /**
     * The virtual machine scale set stages shared between managed and unmanaged based virtual machine scale set
     * definitions.
     */
    interface DefinitionShared
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithSku,
            DefinitionStages.WithProximityPlacementGroup,
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

    /** The entirety of the virtual machine scale set definition. */
    interface DefinitionManagedOrUnmanaged
        extends DefinitionShared,
            DefinitionStages.WithLinuxRootUsernameManagedOrUnmanaged,
            DefinitionStages.WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged,
            DefinitionStages.WithWindowsAdminUsernameManagedOrUnmanaged,
            DefinitionStages.WithWindowsAdminPasswordManagedOrUnmanaged,
            DefinitionStages.WithLinuxCreateManagedOrUnmanaged,
            DefinitionStages.WithWindowsCreateManagedOrUnmanaged,
            DefinitionStages.WithManagedCreate,
            DefinitionStages.WithUnmanagedCreate {
    }

    /** The entirety of the managed disk based virtual machine scale set definition. */
    interface DefinitionManaged
        extends DefinitionShared,
            DefinitionStages.WithLinuxRootUsernameManaged,
            DefinitionStages.WithLinuxRootPasswordOrPublicKeyManaged,
            DefinitionStages.WithWindowsAdminUsernameManaged,
            DefinitionStages.WithWindowsAdminPasswordManaged,
            DefinitionStages.WithLinuxCreateManaged,
            DefinitionStages.WithWindowsCreateManaged,
            DefinitionStages.WithManagedCreate {
    }

    /** The entirety of the unmanaged disk based virtual machine scale set definition. */
    interface DefinitionUnmanaged
        extends DefinitionShared,
            DefinitionStages.WithLinuxRootUsernameUnmanaged,
            DefinitionStages.WithLinuxRootPasswordOrPublicKeyUnmanaged,
            DefinitionStages.WithWindowsAdminUsernameUnmanaged,
            DefinitionStages.WithWindowsAdminPasswordUnmanaged,
            DefinitionStages.WithLinuxCreateUnmanaged,
            DefinitionStages.WithWindowsCreateUnmanaged,
            DefinitionStages.WithUnmanagedCreate {
    }

    /** Grouping of virtual machine scale set definition stages. */
    interface DefinitionStages {
        /** The first stage of a virtual machine scale set definition. */
        interface Blank
            extends GroupableResource.DefinitionWithRegion<VirtualMachineScaleSet.DefinitionStages.WithGroup> {
        }

        /** The stage of a virtual machine scale set definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithSku> {
        }

        /** The stage of a virtual machine scale set definition allowing to specify SKU for the virtual machines. */
        interface WithSku {
            /**
             * Specifies the SKU for the virtual machines in the scale set.
             *
             * @param skuType the SKU type
             * @return the next stage of the definition
             */
            WithProximityPlacementGroup withSku(VirtualMachineScaleSetSkuTypes skuType);

            /**
             * Specifies the SKU for the virtual machines in the scale set.
             *
             * @param sku a SKU from the list of available sizes for the virtual machines in this scale set
             * @return the next stage of the definition
             */
            WithProximityPlacementGroup withSku(VirtualMachineScaleSetSku sku);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to set information about the proximity placement
         * group that the virtual machine scale set should be assigned to.
         */
        interface WithProximityPlacementGroup extends WithDoNotRunExtensionsOnOverprovisionedVms {
            /**
             * Set information about the proximity placement group that the virtual machine scale set should be assigned
             * to.
             *
             * @param proximityPlacementGroupId The Id of the proximity placement group subResource.
             * @return the next stage of the definition.
             */
            WithDoNotRunExtensionsOnOverprovisionedVms withProximityPlacementGroup(String proximityPlacementGroupId);

            /**
             * Creates a new proximity placement gruup witht he specified name and then adds it to the VM scale set.
             *
             * @param proximityPlacementGroupName The name of the group to be created.
             * @param type the type of the group
             * @return the next stage of the definition.
             */
            WithDoNotRunExtensionsOnOverprovisionedVms withNewProximityPlacementGroup(
                String proximityPlacementGroupName, ProximityPlacementGroupType type);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to set when Overprovision is enabled, extensions
         * are launched only on the requested number of VMs which are finally kept.
         */
        interface WithDoNotRunExtensionsOnOverprovisionedVms extends WithAdditionalCapabilities {
            /**
             * Set when Overprovision is enabled, extensions are launched only on the requested number of VMs which are
             * finally kept. This property will hence ensure that the extensions do not run on the extra overprovisioned
             * VMs.
             *
             * @param doNotRunExtensionsOnOverprovisionedVMs the doNotRunExtensionsOnOverprovisionedVMs value to set
             * @return the next stage of the definition.
             */
            WithAdditionalCapabilities withDoNotRunExtensionsOnOverprovisionedVMs(
                Boolean doNotRunExtensionsOnOverprovisionedVMs);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to set specifies additional capabilities enabled
         * or disabled on the Virtual Machines in the Virtual Machine Scale Set.
         */
        interface WithAdditionalCapabilities extends WithNetworkSubnet {
            /**
             * Set specifies additional capabilities enabled or disabled on the Virtual Machines in the Virtual Machine
             * Scale Set. For instance: whether the Virtual Machines have the capability to support attaching managed
             * data disks with UltraSSD_LRS storage account type.
             *
             * @param additionalCapabilities the additionalCapabilities value to set
             * @return the next stage of the definition.
             */
            WithNetworkSubnet withAdditionalCapabilities(AdditionalCapabilities additionalCapabilities);
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
             *
             * <p>By default, all the backends and inbound NAT pools of the load balancer will be associated with the
             * primary network interface of the scale set virtual machines.
             *
             * <p>
             *
             * @param loadBalancer an existing Internet-facing load balancer
             * @return the next stage of the definition
             */
            WithPrimaryInternetFacingLoadBalancerBackendOrNatPool withExistingPrimaryInternetFacingLoadBalancer(
                LoadBalancer loadBalancer);

            /**
             * Specifies that no public load balancer should be associated with the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithPrimaryInternalLoadBalancer withoutPrimaryInternetFacingLoadBalancer();
        }

        /**
         * The stage of a virtual machine scale set definition allowing to specify an internal load balancer for the
         * primary network interface of the virtual machines in the scale set.
         */
        interface WithPrimaryInternalLoadBalancer {
            /**
             * Specifies the internal load balancer whose backends and/or NAT pools can be assigned to the primary
             * network interface of the virtual machines in the scale set.
             *
             * <p>By default all the backends and inbound NAT pools of the load balancer will be associated with the
             * primary network interface of the virtual machines in the scale set, unless subset of them is selected in
             * the next stages.
             *
             * <p>
             *
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
         * The stage of a virtual machine scale set definition allowing to associate a backend pool and/or an inbound
         * NAT pool of the selected Internet-facing load balancer with the primary network interface of the virtual
         * machines in the scale set.
         */
        interface WithPrimaryInternetFacingLoadBalancerBackendOrNatPool
            extends WithPrimaryInternetFacingLoadBalancerNatPool {
            /**
             * Associates the specified backends of the selected load balancer with the primary network interface of the
             * virtual machines in the scale set.
             *
             * @param backendNames the names of existing backends in the selected load balancer
             * @return the next stage of the definition
             */
            WithPrimaryInternetFacingLoadBalancerNatPool withPrimaryInternetFacingLoadBalancerBackends(
                String... backendNames);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to associate an inbound NAT pool of the selected
         * Internet-facing load balancer with the primary network interface of the virtual machines in the scale set.
         */
        interface WithPrimaryInternetFacingLoadBalancerNatPool extends WithPrimaryInternalLoadBalancer {
            /**
             * Associates the specified inbound NAT pools of the selected internal load balancer with the primary
             * network interface of the virtual machines in the scale set.
             *
             * @param natPoolNames inbound NAT pools names existing on the selected load balancer
             * @return the next stage of the definition
             */
            WithPrimaryInternalLoadBalancer withPrimaryInternetFacingLoadBalancerInboundNatPools(
                String... natPoolNames);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to associate backend pools and/or inbound NAT
         * pools of the selected internal load balancer with the primary network interface of the virtual machines in
         * the scale set.
         */
        interface WithInternalLoadBalancerBackendOrNatPool extends WithInternalInternalLoadBalancerNatPool {
            /**
             * Associates the specified backends of the selected load balancer with the primary network interface of the
             * virtual machines in the scale set.
             *
             * @param backendNames names of existing backends in the selected load balancer
             * @return the next stage of the definition
             */
            WithInternalInternalLoadBalancerNatPool withPrimaryInternalLoadBalancerBackends(String... backendNames);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to associate inbound NAT pools of the selected
         * internal load balancer with the primary network interface of the virtual machines in the scale set.
         */
        interface WithInternalInternalLoadBalancerNatPool extends WithOS {
            /**
             * Associate internal load balancer inbound NAT pools with the the primary network interface of the scale
             * set virtual machine.
             *
             * @param natPoolNames inbound NAT pool names
             * @return the next stage of the definition
             */
            WithOS withPrimaryInternalLoadBalancerInboundNatPools(String... natPoolNames);
        }

        /** The stage of the virtual machine scale set definition allowing to specify the operating system image. */
        interface WithOS {
            /**
             * Specifies a known marketplace Windows image used as the operating system for the virtual machines in the
             * scale set.
             *
             * @param knownImage a known market-place image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManagedOrUnmanaged withPopularWindowsImage(
                KnownWindowsVirtualMachineImage knownImage);

            /**
             * Specifies that the latest version of the specified marketplace Windows image should be used.
             *
             * @param publisher specifies the publisher of the image
             * @param offer specifies the offer of the image
             * @param sku specifies the SKU of the image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManagedOrUnmanaged withLatestWindowsImage(
                String publisher, String offer, String sku);

            /**
             * Specifies the specific version of a marketplace Windows image needs to be used.
             *
             * @param imageReference describes publisher, offer, SKU and version of the marketplace image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManagedOrUnmanaged withSpecificWindowsImageVersion(ImageReference imageReference);

            /**
             * Specifies the ID of a generalized Windows custom image to be used.
             *
             * @param customImageId the resource ID of the custom image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManaged withGeneralizedWindowsCustomImage(String customImageId);

            /**
             * Specifies the ID of a specialized Windows custom image to be used.
             *
             * @param customImageId the resource ID of the custom image
             * @return the next stage of the definition
             */
            WithWindowsCreateManaged withSpecializedWindowsCustomImage(String customImageId);

            /**
             * Specifies the user (custom) Windows image to be used as the operating system for the virtual machines in
             * the scale set.
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
             * Specifies the ID of a generalized Linux custom image to be used.
             *
             * @param customImageId the resource ID of the custom image
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameManaged withGeneralizedLinuxCustomImage(String customImageId);

            /**
             * Specifies the ID of a specialized Linux custom image to be used.
             *
             * @param customImageId the resource ID of the custom image
             * @return the next stage of the definition
             */
            WithLinuxCreateManaged withSpecializedLinuxCustomImage(String customImageId);

            /**
             * Specifies the user (custom) Linux image used as the virtual machine's operating system.
             *
             * @param imageUrl the URL the the VHD
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameUnmanaged withStoredLinuxImage(String imageUrl);
        }

        /** The stage of the Linux virtual machine scale set definition allowing to specify SSH root user name. */
        interface WithLinuxRootUsernameManagedOrUnmanaged {
            /**
             * Specifies the SSH root user name for the Linux virtual machine.
             *
             * @param rootUserName a root user name following the required naming convention for Linux user names
             * @return the next stage of the definition
             */
            WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged withRootUsername(String rootUserName);
        }

        /** The stage of the Linux virtual machine scale set definition allowing to specify SSH root user name. */
        interface WithLinuxRootUsernameManaged {
            /**
             * Specifies the SSH root user name for the Linux virtual machine.
             *
             * @param rootUserName a root user name following the required naming conventions for Linux user names
             * @return the next stage of the definition
             */
            WithLinuxRootPasswordOrPublicKeyManaged withRootUsername(String rootUserName);
        }

        /** The stage of the Linux virtual machine scale set definition allowing to specify SSH root user name. */
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
         * The stage of the Linux virtual machine scale set definition allowing to specify SSH root password or public
         * key.
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
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the next stage of the definition
             */
            WithLinuxCreateManagedOrUnmanaged withSsh(String publicKey);
        }

        /**
         * The stage of the Linux virtual machine scale set definition allowing to specify SSH root password or public
         * key.
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
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the next stage of the definition
             */
            WithLinuxCreateManaged withSsh(String publicKey);
        }

        /**
         * The stage of the Linux virtual machine scale set definition allowing to specify SSH root password or public
         * key.
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
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
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
             * @param adminUserName the Windows administrator user name. This must follow the required naming convention
             *     for Windows user name.
             * @return the next stage of the definition
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
             * @param adminUserName the Windows administrator user name. This must follow the required naming convention
             *     for Windows user name.
             * @return the next stage of the definition
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
             * @param adminUserName the Windows administrator user name. This must follow the required naming convention
             *     for Windows user name.
             * @return the next stage of the definition
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
             * @param adminPassword the administrator password. This must follow the criteria for Azure Windows VM
             *     password.
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
             * @param adminPassword the administrator password. This must follow the criteria for Azure Windows VM
             *     password.
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
             * @param adminPassword the administrator password. This must follow the criteria for Azure Windows VM
             *     password.
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsCreateUnmanaged withAdminPassword(String adminPassword);
        }

        /**
         * The stage of a Linux virtual machine scale set definition which contains all the minimum required inputs for
         * the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithLinuxCreateManagedOrUnmanaged extends WithManagedCreate {
            /**
             * Specifies the SSH public key.
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey an SSH public key in the PEM format.
             * @return the next stage of the definition
             */
            WithLinuxCreateManagedOrUnmanaged withSsh(String publicKey);

            /** @return the next stage of a unmanaged disk based virtual machine scale set definition */
            WithUnmanagedCreate withUnmanagedDisks();
        }

        /**
         * The stage of a Linux virtual machine scale set definition which contains all the minimum required inputs for
         * the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithLinuxCreateManaged extends WithManagedCreate {
            /**
             * Specifies the SSH public key.
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey an SSH public key in the PEM format.
             * @return the next stage of the definition
             */
            WithLinuxCreateManaged withSsh(String publicKey);
        }

        /**
         * The stage of a Linux virtual machine scale set definition which contains all the minimum required inputs for
         * the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithLinuxCreateUnmanaged extends WithUnmanagedCreate {
            /**
             * Specifies the SSH public key.
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey an SSH public key in the PEM format.
             * @return the next stage of the definition
             */
            WithLinuxCreateUnmanaged withSsh(String publicKey);
        }

        /**
         * The stage of a Windows virtual machine scale set definition which contains all the minimum required inputs
         * for the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithWindowsCreateManagedOrUnmanaged extends WithWindowsCreateManaged {
            WithWindowsCreateUnmanaged withUnmanagedDisks();
        }

        /**
         * The stage of a Windows virtual machine scale set definition which contains all the minimum required inputs
         * for the resource to be created, but also allows for any other optional settings to be specified.
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
             *
             * <p>Each call to this method adds the given listener to the list of VM's WinRM listeners.
             *
             * @param listener a WinRM listener
             * @return the next stage of the definition
             */
            WithWindowsCreateManaged withWinRM(WinRMListener listener);
        }

        /**
         * The stage of a Windows virtual machine scale set definition which contains all the minimum required inputs
         * for the resource to be created, but also allows for any other optional settings to be specified.
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
             *
             * <p>Each call to this method adds the given listener to the list of VM's WinRM listeners.
             *
             * @param listener a WinRM listener
             * @return the next stage of the definition
             */
            WithWindowsCreateUnmanaged withWinRM(WinRMListener listener);
        }

        /** The stage of a virtual machine scale set definition allowing to specify managed data disks. */
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
            WithManagedCreate withNewDataDisk(
                int sizeInGB, int lun, CachingTypes cachingType, StorageAccountTypes storageAccountType);

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
            WithManagedCreate withNewDataDiskFromImage(int imageLun, int newSizeInGB, CachingTypes cachingType);

            /**
             * Specifies the data disk to be created from the data disk image in the virtual machine image.
             *
             * @param imageLun the LUN of the source data disk image
             * @param newSizeInGB the new size that overrides the default size specified in the data disk image
             * @param cachingType the caching type
             * @param storageAccountType the storage account type
             * @return the next stage of virtual machine definition
             */
            WithManagedCreate withNewDataDiskFromImage(
                int imageLun, int newSizeInGB, CachingTypes cachingType, StorageAccountTypes storageAccountType);
        }

        /** The optionals applicable only for managed disks. */
        interface WithManagedDiskOptionals {
            /**
             * Specifies the storage account type for managed OS disk.
             *
             * @param accountType the storage account type
             * @return the stage representing creatable VM definition
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

        /** The stage of the virtual machine scale set definition allowing to specify availability zone. */
        interface WithAvailabilityZone {
            /**
             * Specifies the availability zone for the virtual machine scale set.
             *
             * @param zoneId the zone identifier.
             * @return the next stage of the definition
             */
            WithManagedCreate withAvailabilityZone(AvailabilityZoneId zoneId);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the VM scale set to be created
         * and optionally allow managed data disks specific settings to be specified.
         */
        interface WithManagedCreate
            extends WithManagedDataDisk, WithManagedDiskOptionals, WithAvailabilityZone, WithCreate {
        }

        /** The stage of the virtual machine scale set definition allowing to specify unmanaged data disk. */
        interface WithUnmanagedDataDisk {
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the VM scale set to be created
         * and optionally allow unmanaged data disks specific settings to be specified.
         */
        interface WithUnmanagedCreate extends WithUnmanagedDataDisk, WithCreate {
        }

        /** The stage of a virtual machine scale set definition allowing to specify the computer name prefix. */
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
         * The stage of the virtual machine scale set definition allowing to specify number of virtual machines in the
         * scale set.
         */
        interface WithCapacity {
            /**
             * Specifies the maximum number of virtual machines in the scale set.
             *
             * @param capacity number of virtual machines
             * @return the next stage of the definition
             */
            WithCreate withCapacity(long capacity);
        }

        /** The stage of a virtual machine scale set definition allowing to specify the upgrade policy. */
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
         * The stage of a virtual machine scale set definition allowing to specify whether or not to over-provision
         * virtual machines in the scale set.
         */
        interface WithOverProvision {
            /**
             * Enables or disables over-provisioning of virtual machines in the scale set.
             *
             * @param enabled true if enabling over-0provisioning of virtual machines in the scale set, otherwise false
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

        /** The stage of a virtual machine scale set definition allowing to specify OS disk configurations. */
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

        /** The stage of a virtual machine scale set definition allowing to specify the storage account. */
        interface WithStorageAccount {
            /**
             * Specifies a new storage account for the OS and data disk VHDs of the virtual machines in the scale set.
             *
             * @param name the name of the storage account
             * @return the next stage of the definition
             */
            WithCreate withNewStorageAccount(String name);

            /**
             * Specifies a new storage account for the OS and data disk VHDs of the virtual machines in the scale set.
             *
             * @param creatable the storage account definition in a creatable stage
             * @return the next stage in the definition
             */
            WithCreate withNewStorageAccount(Creatable<StorageAccount> creatable);

            /**
             * Specifies an existing storage account for the OS and data disk VHDs of the virtual machines in the scale
             * set.
             *
             * @param storageAccount an existing storage account
             * @return the next stage in the definition
             */
            WithCreate withExistingStorageAccount(StorageAccount storageAccount);
        }

        /** The stage of the virtual machine scale set definition allowing to specify the custom data. */
        interface WithCustomData {
            /**
             * Specifies the custom data for the virtual machine scale set.
             *
             * @param base64EncodedCustomData the base64 encoded custom data
             * @return the next stage in the definition
             */
            WithCreate withCustomData(String base64EncodedCustomData);
        }

        /** The stage of the virtual machine scale set definition allowing to specify the secrets. */
        interface WithSecrets {
            /**
             * Specifies set of certificates that should be installed onto the virtual machine.
             *
             * @param secrets the secrets value to set
             * @return the next stage in the definition he secrets value to set
             */
            WithCreate withSecrets(List<VaultSecretGroup> secrets);
        }

        /** The stage of a virtual machine scale set definition allowing to specify extensions. */
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
         * The stage of the virtual machine scale set definition allowing to enable System Assigned (Local) Managed
         * Service Identity.
         */
        interface WithSystemAssignedManagedServiceIdentity {
            /**
             * Specifies that System Assigned (Local) Managed Service Identity needs to be enabled in the virtual
             * machine scale set.
             *
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrCreate withSystemAssignedManagedServiceIdentity();
        }

        /**
         * The stage of the System Assigned (Local) Managed Service Identity enabled virtual machine scale set allowing
         * to set access for the identity.
         */
        interface WithSystemAssignedIdentityBasedAccessOrCreate extends WithCreate {
            /**
             * Specifies that virtual machine scale set's system assigned (local) identity should have the given access
             * (described by the role) on an ARM resource identified by the resource ID. Applications running on the
             * scale set VM instance will have the same permission (role) on the ARM resource.
             *
             * @param resourceId the ARM identifier of the resource
             * @param role access role to assigned to the scale set local identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrCreate withSystemAssignedIdentityBasedAccessTo(
                String resourceId, BuiltInRole role);

            /**
             * Specifies that virtual machine scale set's local identity should have the given access (described by the
             * role) on the resource group that virtual machine resides. Applications running on the scale set VM
             * instance will have the same permission (role) on the resource group.
             *
             * @param role access role to assigned to the scale set local identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrCreate withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(
                BuiltInRole role);

            /**
             * Specifies that virtual machine scale set's system assigned (local) identity should have the access
             * (described by the role definition) on an ARM resource identified by the resource ID. Applications running
             * on the scale set VM instance will have the same permission (role) on the ARM resource.
             *
             * @param resourceId scope of the access represented in ARM resource ID format
             * @param roleDefinitionId access role definition to assigned to the scale set local identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrCreate withSystemAssignedIdentityBasedAccessTo(
                String resourceId, String roleDefinitionId);

            /**
             * Specifies that virtual machine scale set's system assigned (local) identity should have the access
             * (described by the role definition) on the resource group that virtual machine resides. Applications
             * running on the scale set VM instance will have the same permission (role) on the resource group.
             *
             * @param roleDefinitionId access role definition to assigned to the scale set local identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrCreate withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(
                String roleDefinitionId);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify User Assigned (External) Managed
         * Service Identities.
         */
        interface WithUserAssignedManagedServiceIdentity {
            /**
             * Specifies the definition of a not-yet-created user assigned identity to be associated with the virtual
             * machine scale set.
             *
             * @param creatableIdentity a creatable identity definition
             * @return the next stage of the virtual machine scale set definition
             */
            WithCreate withNewUserAssignedManagedServiceIdentity(Creatable<Identity> creatableIdentity);

            /**
             * Specifies an existing user assigned identity to be associated with the virtual machine scale set.
             *
             * @param identity the identity
             * @return the next stage of the virtual machine scale set definition
             */
            WithCreate withExistingUserAssignedManagedServiceIdentity(Identity identity);
        }

        /** The stage of the virtual machine scale set definition allowing to enable boot diagnostics. */
        interface WithBootDiagnostics {
            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine.
             * Managed storage account is used.
             *
             * @return the next stage of the definition
             */
            WithCreate withBootDiagnosticsOnManagedStorageAccount();

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithCreate withBootDiagnostics();

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine scale set.
             *
             * @param creatable the storage account to be created and used for store the boot diagnostics
             * @return the next stage of the definition
             */
            WithCreate withBootDiagnostics(Creatable<StorageAccount> creatable);

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine scale set.
             *
             * @param storageAccount an existing storage account to be uses to store the boot diagnostics
             * @return the next stage of the definition
             */
            WithCreate withBootDiagnostics(StorageAccount storageAccount);

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine scale set.
             *
             * @param storageAccountBlobEndpointUri a storage account blob endpoint to store the boot diagnostics
             * @return the next stage of the definition
             */
            WithCreate withBootDiagnostics(String storageAccountBlobEndpointUri);
        }

        /** The stage of the virtual machine definition allowing to specify billing profile. */
        interface WithBillingProfile {

            /**
             * Set the billing related details of the low priority virtual machines in the scale set. This price is in
             * US Dollars.
             *
             * @param maxPrice the maxPrice value to set
             * @return the next stage of the definition
             */
            WithCreate withMaxPrice(Double maxPrice);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify priority for vms in the scale-set.
         */
        interface WithVMPriority {
            /**
             * Specifies the priority of the virtual machines in the scale set.
             *
             * @param priority the priority
             * @return the next stage of the definition
             */
            WithCreate withVirtualMachinePriority(VirtualMachinePriorityTypes priority);

            /**
             * Specify that virtual machines in the scale set should be low priority VMs.
             *
             * @return the next stage of the definition
             */
            WithCreate withLowPriorityVirtualMachine();

            /**
             * Specify that virtual machines in the scale set should be low priority VMs with provided eviction policy.
             *
             * @param policy eviction policy for the virtual machines in the scale set.
             * @return the next stage of the definition
             */
            WithCreate withLowPriorityVirtualMachine(VirtualMachineEvictionPolicyTypes policy);

            /**
             * Specify that virtual machines in the scale set should be spot priority VMs.
             *
             * @return the next stage of the definition
             */
            WithCreate withSpotPriorityVirtualMachine();

            /**
             * Specify that virtual machines in the scale set should be spot priority VMs with provided eviction policy.
             *
             * @param policy eviction policy for the virtual machines in the scale set.
             * @return the next stage of the definition
             */
            WithCreate withSpotPriorityVirtualMachine(VirtualMachineEvictionPolicyTypes policy);
        }

        /** The stage of the virtual machine scale set definition allowing to enable public ip for vm instances. */
        interface WithVirtualMachinePublicIp {
            /**
             * Specify that virtual machines in the scale set should have public ip address.
             *
             * @return the next stage of the definition
             */
            WithCreate withVirtualMachinePublicIp();

            /**
             * Specify that virtual machines in the scale set should have public ip address.
             *
             * @param leafDomainLabel the domain name label
             * @return the next stage of the definition
             */
            WithCreate withVirtualMachinePublicIp(String leafDomainLabel);

            /**
             * Specify that virtual machines in the scale set should have public ip address.
             *
             * @param ipConfig the public ip address configuration
             * @return the next stage of the definition
             */
            WithCreate withVirtualMachinePublicIp(VirtualMachineScaleSetPublicIpAddressConfiguration ipConfig);
        }

        /** The stage of the virtual machine scale set definition allowing to configure accelerated networking. */
        interface WithAcceleratedNetworking {
            /**
             * Specify that accelerated networking should be enabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithCreate withAcceleratedNetworking();

            /**
             * Specify that accelerated networking should be disabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithCreate withoutAcceleratedNetworking();
        }

        /** The stage of the virtual machine scale set definition allowing to configure ip forwarding. */
        interface WithIpForwarding {
            /**
             * Specify that ip forwarding should be enabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithCreate withIpForwarding();

            /**
             * Specify that ip forwarding should be disabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithCreate withoutIpForwarding();
        }

        /** The stage of the virtual machine scale set definition allowing to configure network security group. */
        interface WithNetworkSecurityGroup {
            /**
             * Specifies the network security group for the virtual machine scale set.
             *
             * @param networkSecurityGroup the network security group to associate
             * @return the next stage of the definition
             */
            WithCreate withExistingNetworkSecurityGroup(NetworkSecurityGroup networkSecurityGroup);

            /**
             * Specifies the network security group for the virtual machine scale set.
             *
             * @param networkSecurityGroupId the network security group to associate
             * @return the next stage of the definition
             */
            WithCreate withExistingNetworkSecurityGroupId(String networkSecurityGroupId);
        }

        /** The stage of the virtual machine scale set definition allowing to configure single placement group. */
        interface WithSinglePlacementGroup {
            /**
             * Specify that single placement group should be enabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithCreate withSinglePlacementGroup();

            /**
             * Specify that single placement group should be disabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithCreate withoutSinglePlacementGroup();
        }

        /** The stage of the virtual machine scale set definition allowing to configure application gateway. */
        interface WithApplicationGateway {
            /**
             * Specify that an application gateway backend pool should be associated with virtual machine scale set.
             *
             * @param backendPoolId an existing backend pool id of the gateway
             * @return the next stage of the definition
             */
            WithCreate withExistingApplicationGatewayBackendPool(String backendPoolId);
        }

        /** The stage of the virtual machine scale set definition allowing to configure application security group. */
        interface WithApplicationSecurityGroup {
            /**
             * Specifies that provided application security group should be associated with the virtual machine scale
             * set.
             *
             * @param applicationSecurityGroup the application security group
             * @return the next stage of the definition
             */
            WithCreate withExistingApplicationSecurityGroup(ApplicationSecurityGroup applicationSecurityGroup);

            /**
             * Specifies that provided application security group should be associated with the virtual machine scale
             * set.
             *
             * @param applicationSecurityGroupId the application security group id
             * @return the next stage of the definition
             */
            WithCreate withExistingApplicationSecurityGroupId(String applicationSecurityGroupId);
        }

        /**
         * The stage of a virtual machine scale set definition containing all the required inputs for the resource to be
         * created, but also allowing for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<VirtualMachineScaleSet>,
                DefinitionStages.WithOSDiskSettings,
                DefinitionStages.WithComputerNamePrefix,
                DefinitionStages.WithCapacity,
                DefinitionStages.WithUpgradePolicy,
                DefinitionStages.WithOverProvision,
                DefinitionStages.WithStorageAccount,
                DefinitionStages.WithCustomData,
                DefinitionStages.WithExtension,
                DefinitionStages.WithSystemAssignedManagedServiceIdentity,
                DefinitionStages.WithUserAssignedManagedServiceIdentity,
                DefinitionStages.WithBootDiagnostics,
                DefinitionStages.WithBillingProfile,
                DefinitionStages.WithVMPriority,
                DefinitionStages.WithVirtualMachinePublicIp,
                DefinitionStages.WithAcceleratedNetworking,
                DefinitionStages.WithIpForwarding,
                DefinitionStages.WithNetworkSecurityGroup,
                DefinitionStages.WithSinglePlacementGroup,
                DefinitionStages.WithApplicationGateway,
                DefinitionStages.WithApplicationSecurityGroup,
                DefinitionStages.WithSecrets,
                Resource.DefinitionWithTags<VirtualMachineScaleSet.DefinitionStages.WithCreate> {
        }
    }

    /** Grouping of virtual machine scale set update stages. */
    interface UpdateStages {
        /**
         * The stage of a virtual machine scale set update allowing to specify load balancers for the primary network
         * interface of the scale set virtual machines.
         */
        interface WithPrimaryLoadBalancer extends WithPrimaryInternalLoadBalancer {
            /**
             * Specifies the load balancer to be used as the Internet-facing load balancer for the virtual machines in
             * the scale set.
             *
             * <p>This will replace the current Internet-facing load balancer associated with the virtual machines in
             * the scale set (if any). By default all the backend and inbound NAT pool of the load balancer will be
             * associated with the primary network interface of the virtual machines unless a subset of them is selected
             * in the next stages
             *
             * @param loadBalancer the primary Internet-facing load balancer
             * @return the next stage of the update
             */
            WithPrimaryInternetFacingLoadBalancerBackendOrNatPool withExistingPrimaryInternetFacingLoadBalancer(
                LoadBalancer loadBalancer);
        }

        /**
         * The stage of a virtual machine scale set update allowing to associate a backend pool and/or inbound NAT pool
         * of the selected Internet-facing load balancer with the primary network interface of the virtual machines in
         * the scale set.
         */
        interface WithPrimaryInternetFacingLoadBalancerBackendOrNatPool
            extends WithPrimaryInternetFacingLoadBalancerNatPool {
            /**
             * Associates the specified Internet-facing load balancer backends with the primary network interface of the
             * virtual machines in the scale set.
             *
             * @param backendNames the backend names
             * @return the next stage of the update
             */
            WithPrimaryInternetFacingLoadBalancerNatPool withPrimaryInternetFacingLoadBalancerBackends(
                String... backendNames);
        }

        /**
         * The stage of a virtual machine scale set update allowing to associate an inbound NAT pool of the selected
         * Internet-facing load balancer with the primary network interface of the virtual machines in the scale set.
         */
        interface WithPrimaryInternetFacingLoadBalancerNatPool extends WithPrimaryInternalLoadBalancer {
            /**
             * Associates inbound NAT pools of the selected Internet-facing load balancer with the primary network
             * interface of the virtual machines in the scale set.
             *
             * @param natPoolNames the names of existing inbound NAT pools on the selected load balancer
             * @return the next stage of the update
             */
            WithPrimaryInternalLoadBalancer withPrimaryInternetFacingLoadBalancerInboundNatPools(
                String... natPoolNames);
        }

        /**
         * The stage of a virtual machine scale set update allowing to specify an internal load balancer for the primary
         * network interface of the scale set virtual machines.
         */
        interface WithPrimaryInternalLoadBalancer extends WithApply {
            /**
             * Specifies the load balancer to be used as the internal load balancer for the virtual machines in the
             * scale set.
             *
             * <p>This will replace the current internal load balancer associated with the virtual machines in the scale
             * set (if any). By default all the backends and inbound NAT pools of the load balancer will be associated
             * with the primary network interface of the virtual machines in the scale set unless subset of them is
             * selected in the next stages.
             *
             * @param loadBalancer the primary Internet-facing load balancer
             * @return the next stage of the update
             */
            WithPrimaryInternalLoadBalancerBackendOrNatPool withExistingPrimaryInternalLoadBalancer(
                LoadBalancer loadBalancer);
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
            WithPrimaryInternalLoadBalancerNatPool withPrimaryInternalLoadBalancerBackends(String... backendNames);
        }

        /**
         * The stage of a virtual machine scale set update allowing to associate inbound NAT pools of the selected
         * internal load balancer with the primary network interface of the virtual machines in the scale set.
         */
        interface WithPrimaryInternalLoadBalancerNatPool extends WithApply {
            /**
             * Associates the specified internal load balancer inbound NAT pools with the the primary network interface
             * of the virtual machines in the scale set.
             *
             * @param natPoolNames the names of existing inbound NAT pools in the selected load balancer
             * @return the next stage of the update
             */
            WithApply withPrimaryInternalLoadBalancerInboundNatPools(String... natPoolNames);
        }

        /**
         * The stage of a virtual machine scale set update allowing to set specifies additional capabilities enabled or
         * disabled on the Virtual Machines in the Virtual Machine Scale Set.
         */
        interface WithAdditionalCapabilities {
            /**
             * Set specifies additional capabilities enabled or disabled on the Virtual Machines in the Virtual Machine
             * Scale Set. For instance: whether the Virtual Machines have the capability to support attaching managed
             * data disks with UltraSSD_LRS storage account type.
             *
             * @param additionalCapabilities the additionalCapabilities value to set
             * @return the next stage of the definition.
             */
            WithApply withAdditionalCapabilities(AdditionalCapabilities additionalCapabilities);
        }

        /**
         * The stage of a virtual machine scale set update allowing to change the SKU for the virtual machines in the
         * scale set.
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
         * The stage of a virtual machine scale set definition allowing to specify the number of virtual machines in the
         * scale set.
         */
        interface WithCapacity {
            /**
             * Specifies the new number of virtual machines in the scale set.
             *
             * @param capacity the virtual machine capacity of the scale set
             * @return the next stage of the update
             */
            WithApply withCapacity(long capacity);
        }

        /** The stage of the virtual machine scale set definition allowing to specify the custom data. */
        interface WithCustomData {
            /**
             * Specifies the custom data for the virtual machine scale set.
             *
             * @param base64EncodedCustomData the base64 encoded custom data
             * @return the next stage in the definition
             */
            WithApply withCustomData(String base64EncodedCustomData);
        }

        /** The stage of the virtual machine definition allowing to specify extensions. */
        interface WithSecrets {
            /**
             * The stage of a virtual machine scale set definition allowing to update secrets from virtual machines in
             * the scale set.
             *
             * @param secrets the list of secrets
             * @return the next stage of update
             */
            WithApply withSecrets(List<VaultSecretGroup> secrets);

            /**
             * The stage of a virtual machine scale set definition allowing to remove secrets from virtual machines in
             * the scale set.
             *
             * @return the next stage of update
             */
            WithApply withoutSecrets();
        }

        /** The stage of the virtual machine definition allowing to specify extensions. */
        interface WithExtension {
            /**
             * Begins the definition of an extension reference to be attached to the virtual machines in the scale set.
             *
             * @param name the reference name for an extension
             * @return the first stage of the extension reference definition
             */
            VirtualMachineScaleSetExtension.UpdateDefinitionStages.Blank<WithApply> defineNewExtension(String name);

            /**
             * Begins the description of an update of an existing extension assigned to the virtual machines in the
             * scale set.
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
         * The stage of a virtual machine scale set update allowing to remove the public and internal load balancer from
         * the primary network interface configuration.
         */
        interface WithoutPrimaryLoadBalancer {
            /**
             * Removes the association between the Internet-facing load balancer and the primary network interface
             * configuration.
             *
             * <p>This removes the association between primary network interface configuration and all the backends and
             * inbound NAT pools in the load balancer.
             *
             * @return the next stage of the update
             */
            WithApply withoutPrimaryInternetFacingLoadBalancer();

            /**
             * Removes the association between the internal load balancer and the primary network interface
             * configuration.
             *
             * <p>This removes the association between primary network interface configuration and all the backends and
             * inbound NAT pools in the load balancer.
             *
             * @return the next stage of the update
             */
            WithApply withoutPrimaryInternalLoadBalancer();
        }

        /**
         * The stage of a virtual machine scale set update allowing to remove the association between the primary
         * network interface configuration and a backend of a load balancer.
         */
        interface WithoutPrimaryLoadBalancerBackend {
            /**
             * Removes the associations between the primary network interface configuration and the specfied backends of
             * the Internet-facing load balancer.
             *
             * @param backendNames existing backend names
             * @return the next stage of the update
             */
            WithApply withoutPrimaryInternetFacingLoadBalancerBackends(String... backendNames);

            /**
             * Removes the associations between the primary network interface configuration and the specified backends
             * of the internal load balancer.
             *
             * @param backendNames existing backend names
             * @return the next stage of the update
             */
            WithApply withoutPrimaryInternalLoadBalancerBackends(String... backendNames);
        }

        /**
         * A stage of the virtual machine scale set update allowing to remove the associations between the primary
         * network interface configuration and the specified inbound NAT pools of the load balancer.
         */
        interface WithoutPrimaryLoadBalancerNatPool {
            /**
             * Removes the associations between the primary network interface configuration and the specified inbound
             * NAT pools of an Internet-facing load balancer.
             *
             * @param natPoolNames the names of existing inbound NAT pools
             * @return the next stage of the update
             */
            WithApply withoutPrimaryInternetFacingLoadBalancerNatPools(String... natPoolNames);

            /**
             * Removes the associations between the primary network interface configuration and the specified inbound
             * NAT pools of the internal load balancer.
             *
             * @param natPoolNames the names of existing inbound NAT pools
             * @return the next stage of the update
             */
            WithApply withoutPrimaryInternalLoadBalancerNatPools(String... natPoolNames);
        }

        /**
         * The stage of the virtual machine scale set update allowing to enable System Assigned (Local) Managed Service
         * Identity.
         */
        interface WithSystemAssignedManagedServiceIdentity {
            /**
             * Specifies that System assigned (Local) Managed Service Identity needs to be enabled in the virtual
             * machine scale set.
             *
             * @return the next stage of the update
             */
            WithSystemAssignedIdentityBasedAccessOrApply withSystemAssignedManagedServiceIdentity();

            /**
             * Specifies that System assigned (Local) Managed Service Identity needs to be disabled in the virtual
             * machine scale set.
             *
             * @return the next stage of the update
             */
            WithSystemAssignedIdentityBasedAccessOrApply withoutSystemAssignedManagedServiceIdentity();
        }

        /**
         * The stage of the System Assigned (Local) Managed Service Identity enabled virtual machine scale set allowing
         * to set access for the identity.
         */
        interface WithSystemAssignedIdentityBasedAccessOrApply extends WithApply {
            /**
             * Specifies that virtual machine's system assigned (local) identity should have the given access (described
             * by the role) on an ARM resource identified by the resource ID. Applications running on the scale set VM
             * instance will have the same permission (role) on the ARM resource.
             *
             * @param resourceId the ARM identifier of the resource
             * @param role access role to assigned to the scale set local identity
             * @return the next stage of the update
             */
            WithSystemAssignedIdentityBasedAccessOrApply withSystemAssignedIdentityBasedAccessTo(
                String resourceId, BuiltInRole role);

            /**
             * Specifies that virtual machine scale set's system assigned (local) identity should have the given access
             * (described by the role) on the resource group that virtual machine resides. Applications running on the
             * scale set VM instance will have the same permission (role) on the resource group.
             *
             * @param role access role to assigned to the scale set local identity
             * @return the next stage of the update
             */
            WithSystemAssignedIdentityBasedAccessOrApply withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(
                BuiltInRole role);

            /**
             * Specifies that virtual machine scale set 's system assigned (local) identity should have the access
             * (described by the role definition) on an ARM resource identified by the resource ID. Applications running
             * on the scale set VM instance will have the same permission (role) on the ARM resource.
             *
             * @param resourceId scope of the access represented in ARM resource ID format
             * @param roleDefinitionId access role definition to assigned to the scale set local identity
             * @return the next stage of the update
             */
            WithSystemAssignedIdentityBasedAccessOrApply withSystemAssignedIdentityBasedAccessTo(
                String resourceId, String roleDefinitionId);

            /**
             * Specifies that virtual machine scale set's system assigned (local) identity should have the access
             * (described by the role definition) on the resource group that virtual machine resides. Applications
             * running on the scale set VM instance will have the same permission (role) on the resource group.
             *
             * @param roleDefinitionId access role definition to assigned to the scale set local identity
             * @return the next stage of the update
             */
            WithSystemAssignedIdentityBasedAccessOrApply withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(
                String roleDefinitionId);
        }

        /**
         * The stage of the virtual machine update allowing to add or remove User Assigned (External) Managed Service
         * Identities.
         */
        interface WithUserAssignedManagedServiceIdentity {
            /**
             * Specifies the definition of a not-yet-created user assigned identity to be associated with the virtual
             * machine.
             *
             * @param creatableIdentity a creatable identity definition
             * @return the next stage of the virtual machine scale set update
             */
            WithApply withNewUserAssignedManagedServiceIdentity(Creatable<Identity> creatableIdentity);

            /**
             * Specifies an existing user assigned identity to be associated with the virtual machine.
             *
             * @param identity the identity
             * @return the next stage of the virtual machine scale set update
             */
            WithApply withExistingUserAssignedManagedServiceIdentity(Identity identity);

            /**
             * Specifies that an user assigned identity associated with the virtual machine should be removed.
             *
             * @param identityId ARM resource id of the identity
             * @return the next stage of the virtual machine scale set update
             */
            WithApply withoutUserAssignedManagedServiceIdentity(String identityId);
        }

        /** The stage of the virtual machine scale set definition allowing to enable boot diagnostics. */
        interface WithBootDiagnostics {
            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine.
             * Managed storage account is used.
             *
             * @return the next stage of the update
             */
            Update withBootDiagnosticsOnManagedStorageAccount();

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine scale set.
             *
             * @return the next stage of the update
             */
            Update withBootDiagnostics();

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine scale set.
             *
             * @param creatable the storage account to be created and used for store the boot diagnostics
             * @return the next stage of the update
             */
            Update withBootDiagnostics(Creatable<StorageAccount> creatable);

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine scale set.
             *
             * @param storageAccount an existing storage account to be uses to store the boot diagnostics
             * @return the next stage of the update
             */
            Update withBootDiagnostics(StorageAccount storageAccount);

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine scale set.
             *
             * @param storageAccountBlobEndpointUri a storage account blob endpoint to store the boot diagnostics
             * @return the next stage of the update
             */
            Update withBootDiagnostics(String storageAccountBlobEndpointUri);

            /**
             * Specifies that boot diagnostics needs to be disabled in the virtual machine scale set.
             *
             * @return the next stage of the update
             */
            Update withoutBootDiagnostics();
        }

        /** The stage of the virtual machine scale set update allowing to specify billing profile. */
        interface WithBillingProfile {
            /**
             * Set the billing related details of the low priority virtual machines in the scale set.
             *
             * @param maxPrice max price to set
             * @return the next stage of update
             */
            Update withMaxPrice(Double maxPrice);
        }

        /** The stage of the virtual machine scale set definition allowing to specify unmanaged data disk. */
        interface WithUnmanagedDataDisk {
        }

        /** The stage of a virtual machine scale set update allowing to specify managed data disks. */
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
            WithApply withNewDataDisk(
                int sizeInGB, int lun, CachingTypes cachingType, StorageAccountTypes storageAccountType);

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
             *
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

        /** The stage of the virtual machine scale set update allowing to specify availability zone. */
        interface WithAvailabilityZone {
            /**
             * Specifies the availability zone for the virtual machine scale set.
             *
             * @param zoneId the zone identifier.
             * @return the next stage of the update
             */
            WithApply withAvailabilityZone(AvailabilityZoneId zoneId);
        }

        /** The stage of the virtual machine scale set update allowing to enable public ip for vm instances. */
        interface WithVirtualMachinePublicIp {
            /**
             * Specify that virtual machines in the scale set should have public ip address.
             *
             * @return the next stage of the update
             */
            WithApply withVirtualMachinePublicIp();

            /**
             * Specify that virtual machines in the scale set should have public ip address.
             *
             * @param leafDomainLabel the domain name label
             * @return the next stage of the update
             */
            WithApply withVirtualMachinePublicIp(String leafDomainLabel);

            /**
             * Specify that virtual machines in the scale set should have public ip address.
             *
             * @param ipConfig the public ip address configuration
             * @return the next stage of the update
             */
            WithApply withVirtualMachinePublicIp(VirtualMachineScaleSetPublicIpAddressConfiguration ipConfig);
        }

        /** The stage of the virtual machine scale set update allowing to configure accelerated networking. */
        interface WithAcceleratedNetworking {
            /**
             * Specify that accelerated networking should be enabled for the virtual machine scale set.
             *
             * @return the next stage of the update
             */
            WithApply withAcceleratedNetworking();

            /**
             * Specify that accelerated networking should be disabled for the virtual machine scale set.
             *
             * @return the next stage of the update
             */
            WithApply withoutAcceleratedNetworking();
        }

        /** The stage of the virtual machine scale set update allowing to configure ip forwarding. */
        interface WithIpForwarding {
            /**
             * Specify that ip forwarding should be enabled for the virtual machine scale set.
             *
             * @return the next stage of the update
             */
            WithApply withIpForwarding();

            /**
             * Specify that ip forwarding should be disabled for the virtual machine scale set.
             *
             * @return the next stage of the update
             */
            WithApply withoutIpForwarding();
        }

        /** The stage of the virtual machine scale set update allowing to configure network security group. */
        interface WithNetworkSecurityGroup {
            /**
             * Specifies the network security group for the virtual machine scale set.
             *
             * @param networkSecurityGroup the network security group to associate
             * @return the next stage of the update
             */
            WithApply withExistingNetworkSecurityGroup(NetworkSecurityGroup networkSecurityGroup);

            /**
             * Specifies the network security group for the virtual machine scale set.
             *
             * @param networkSecurityGroupId the network security group to associate
             * @return the next stage of the update
             */
            WithApply withExistingNetworkSecurityGroupId(String networkSecurityGroupId);

            /**
             * Specifies that network security group association should be removed if exists.
             *
             * @return the next stage of the update
             */
            WithApply withoutNetworkSecurityGroup();
        }

        /** The stage of the virtual machine scale set update allowing to configure single placement group. */
        interface WithSinglePlacementGroup {
            /**
             * Specify that single placement group should be enabled for the virtual machine scale set.
             *
             * @return the next stage of the update
             */
            WithApply withSinglePlacementGroup();

            /**
             * Specify that single placement group should be disabled for the virtual machine scale set.
             *
             * @return the next stage of the update
             */
            WithApply withoutSinglePlacementGroup();
        }

        /** The stage of the virtual machine scale set update allowing to configure application gateway. */
        interface WithApplicationGateway {
            /**
             * Specify that an application gateway backend pool should be associated with virtual machine scale set.
             *
             * @param backendPoolId an existing backend pool id of the gateway
             * @return the next stage of the update
             */
            WithApply withExistingApplicationGatewayBackendPool(String backendPoolId);

            /**
             * Specify an existing application gateway associated should be removed from the virtual machine scale set.
             *
             * @param backendPoolId an existing backend pool id of the gateway
             * @return the next stage of the update
             */
            WithApply withoutApplicationGatewayBackendPool(String backendPoolId);
        }

        /** The stage of the virtual machine scale set update allowing to configure application security group. */
        interface WithApplicationSecurityGroup {
            /**
             * Specifies that provided application security group should be associated with the virtual machine scale
             * set.
             *
             * @param applicationSecurityGroup the application security group
             * @return the next stage of the update
             */
            WithApply withExistingApplicationSecurityGroup(ApplicationSecurityGroup applicationSecurityGroup);

            /**
             * Specifies that provided application security group should be associated with the virtual machine scale
             * set.
             *
             * @param applicationSecurityGroupId the application security group id
             * @return the next stage of the update
             */
            WithApply withExistingApplicationSecurityGroupId(String applicationSecurityGroupId);

            /**
             * Specifies that provided application security group should be removed from the virtual machine scale set.
             *
             * @param applicationSecurityGroupId the application security group id
             * @return the next stage of the update
             */
            WithApply withoutApplicationSecurityGroup(String applicationSecurityGroupId);
        }

        /** The stage of a virtual machine scale set update containing inputs for the resource to be updated. */
        interface WithApply
            extends Appliable<VirtualMachineScaleSet>,
                Resource.UpdateWithTags<WithApply>,
                UpdateStages.WithManagedDataDisk,
                UpdateStages.WithUnmanagedDataDisk,
                UpdateStages.WithSku,
                UpdateStages.WithAdditionalCapabilities,
                UpdateStages.WithCapacity,
                UpdateStages.WithCustomData,
                UpdateStages.WithSecrets,
                UpdateStages.WithExtension,
                UpdateStages.WithoutPrimaryLoadBalancer,
                UpdateStages.WithoutPrimaryLoadBalancerBackend,
                UpdateStages.WithoutPrimaryLoadBalancerNatPool,
                UpdateStages.WithSystemAssignedManagedServiceIdentity,
                UpdateStages.WithUserAssignedManagedServiceIdentity,
                UpdateStages.WithBootDiagnostics,
                UpdateStages.WithBillingProfile,
                UpdateStages.WithAvailabilityZone,
                UpdateStages.WithVirtualMachinePublicIp,
                UpdateStages.WithAcceleratedNetworking,
                UpdateStages.WithIpForwarding,
                UpdateStages.WithNetworkSecurityGroup,
                UpdateStages.WithSinglePlacementGroup,
                UpdateStages.WithApplicationGateway,
                UpdateStages.WithApplicationSecurityGroup {
        }
    }

    /** The entirety of the virtual machine scale set update. */
    interface Update
        extends UpdateStages.WithPrimaryLoadBalancer,
            UpdateStages.WithPrimaryInternetFacingLoadBalancerBackendOrNatPool,
            UpdateStages.WithPrimaryInternalLoadBalancerBackendOrNatPool {
    }
}
