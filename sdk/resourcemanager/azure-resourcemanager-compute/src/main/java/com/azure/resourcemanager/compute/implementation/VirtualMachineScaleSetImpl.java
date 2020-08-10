// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.SubResource;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.AdditionalCapabilities;
import com.azure.resourcemanager.compute.models.ApiEntityReference;
import com.azure.resourcemanager.compute.models.BillingProfile;
import com.azure.resourcemanager.compute.models.BootDiagnostics;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.DiagnosticsProfile;
import com.azure.resourcemanager.compute.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.compute.models.ImageReference;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.LinuxConfiguration;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.ProximityPlacementGroup;
import com.azure.resourcemanager.compute.models.ProximityPlacementGroupType;
import com.azure.resourcemanager.compute.models.ResourceIdentityType;
import com.azure.resourcemanager.compute.models.RunCommandInput;
import com.azure.resourcemanager.compute.models.RunCommandInputParameter;
import com.azure.resourcemanager.compute.models.RunCommandResult;
import com.azure.resourcemanager.compute.models.SshConfiguration;
import com.azure.resourcemanager.compute.models.SshPublicKey;
import com.azure.resourcemanager.compute.models.StorageAccountTypes;
import com.azure.resourcemanager.compute.models.UpgradeMode;
import com.azure.resourcemanager.compute.models.UpgradePolicy;
import com.azure.resourcemanager.compute.models.VaultSecretGroup;
import com.azure.resourcemanager.compute.models.VirtualHardDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImage;
import com.azure.resourcemanager.compute.models.VirtualMachineEvictionPolicyTypes;
import com.azure.resourcemanager.compute.models.VirtualMachinePriorityTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetExtension;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetExtensionProfile;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetIpConfiguration;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetManagedDiskParameters;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetNetworkConfiguration;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetNetworkProfile;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetOSDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetOSProfile;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetPublicIpAddressConfiguration;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetPublicIpAddressConfigurationDnsSettings;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSku;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSkuTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetStorageProfile;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetUpdate;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVMs;
import com.azure.resourcemanager.compute.models.WinRMConfiguration;
import com.azure.resourcemanager.compute.models.WinRMListener;
import com.azure.resourcemanager.compute.models.WindowsConfiguration;
import com.azure.resourcemanager.compute.fluent.inner.ProximityPlacementGroupInner;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineScaleSetExtensionInner;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineScaleSetInner;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.implementation.RoleAssignmentHelper;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.network.models.ApplicationSecurityGroup;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerBackend;
import com.azure.resourcemanager.network.models.LoadBalancerInboundNatPool;
import com.azure.resourcemanager.network.models.LoadBalancerPrivateFrontend;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.VirtualMachineScaleSetNetworkInterface;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceNamer;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.StorageManager;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/** Implementation of VirtualMachineScaleSet. */
public class VirtualMachineScaleSetImpl
    extends GroupableParentResourceImpl<
        VirtualMachineScaleSet, VirtualMachineScaleSetInner, VirtualMachineScaleSetImpl, ComputeManager>
    implements VirtualMachineScaleSet,
        VirtualMachineScaleSet.DefinitionManagedOrUnmanaged,
        VirtualMachineScaleSet.DefinitionManaged,
        VirtualMachineScaleSet.DefinitionUnmanaged,
        VirtualMachineScaleSet.Update,
        VirtualMachineScaleSet.DefinitionStages.WithSystemAssignedIdentityBasedAccessOrCreate,
        VirtualMachineScaleSet.DefinitionStages.WithUserAssignedManagedServiceIdentity,
        VirtualMachineScaleSet.UpdateStages.WithSystemAssignedIdentityBasedAccessOrApply,
        VirtualMachineScaleSet.UpdateStages.WithUserAssignedManagedServiceIdentity {
    // Clients
    private final StorageManager storageManager;
    private final NetworkManager networkManager;
    // used to generate unique name for any dependency resources
    private final ResourceNamer namer;
    private boolean isMarketplaceLinuxImage = false;
    // name of an existing subnet in the primary network to use
    private String existingPrimaryNetworkSubnetNameToAssociate;
    // unique key of a creatable storage accounts to be used for virtual machines child resources that
    // requires storage [OS disk]
    private List<String> creatableStorageAccountKeys = new ArrayList<>();
    // reference to an existing storage account to be used for virtual machines child resources that
    // requires storage [OS disk]
    private List<StorageAccount> existingStorageAccountsToAssociate = new ArrayList<>();
    // the child resource extensions
    private Map<String, VirtualMachineScaleSetExtension> extensions;
    // reference to the primary and internal Internet facing load balancer
    private LoadBalancer primaryInternetFacingLoadBalancer;
    private LoadBalancer primaryInternalLoadBalancer;
    // Load balancer specific variables used during update
    private boolean removePrimaryInternetFacingLoadBalancerOnUpdate;
    private boolean removePrimaryInternalLoadBalancerOnUpdate;
    private LoadBalancer primaryInternetFacingLoadBalancerToAttachOnUpdate;
    private LoadBalancer primaryInternalLoadBalancerToAttachOnUpdate;
    private List<String> primaryInternetFacingLBBackendsToRemoveOnUpdate = new ArrayList<>();
    private List<String> primaryInternetFacingLBInboundNatPoolsToRemoveOnUpdate = new ArrayList<>();
    private List<String> primaryInternalLBBackendsToRemoveOnUpdate = new ArrayList<>();
    private List<String> primaryInternalLBInboundNatPoolsToRemoveOnUpdate = new ArrayList<>();
    private List<String> primaryInternetFacingLBBackendsToAddOnUpdate = new ArrayList<>();
    private List<String> primaryInternetFacingLBInboundNatPoolsToAddOnUpdate = new ArrayList<>();
    private List<String> primaryInternalLBBackendsToAddOnUpdate = new ArrayList<>();
    private List<String> primaryInternalLBInboundNatPoolsToAddOnUpdate = new ArrayList<>();
    // Flag indicates native disk is selected for OS and Data disks
    private boolean isUnmanagedDiskSelected;
    // To track the managed data disks
    private final ManagedDataDiskCollection managedDataDisks;
    // Utility to setup MSI for the virtual machine scale set
    VirtualMachineScaleSetMsiHandler virtualMachineScaleSetMsiHandler;
    // To manage boot diagnostics specific operations
    private final BootDiagnosticsHandler bootDiagnosticsHandler;
    // Name of the new proximity placement group
    private String newProximityPlacementGroupName;
    // Type fo the new proximity placement group
    private ProximityPlacementGroupType newProximityPlacementGroupType;
    // To manage OS profile
    private boolean removeOsProfile;
    private final ClientLogger logger = new ClientLogger(VirtualMachineScaleSetImpl.class);

    VirtualMachineScaleSetImpl(
        String name,
        VirtualMachineScaleSetInner innerModel,
        final ComputeManager computeManager,
        final StorageManager storageManager,
        final NetworkManager networkManager,
        final AuthorizationManager authorizationManager) {
        super(name, innerModel, computeManager);
        this.storageManager = storageManager;
        this.networkManager = networkManager;
        this.namer = this.manager().sdkContext().getResourceNamerFactory().createResourceNamer(this.name());
        this.managedDataDisks = new ManagedDataDiskCollection(this);
        this.virtualMachineScaleSetMsiHandler = new VirtualMachineScaleSetMsiHandler(authorizationManager, this);
        this.bootDiagnosticsHandler = new BootDiagnosticsHandler(this);
        this.newProximityPlacementGroupName = null;
        this.newProximityPlacementGroupType = null;
    }

    @Override
    protected void initializeChildrenFromInner() {
        this.extensions = new HashMap<>();
        if (this.inner().virtualMachineProfile().extensionProfile() != null) {
            if (this.inner().virtualMachineProfile().extensionProfile().extensions() != null) {
                for (VirtualMachineScaleSetExtensionInner inner
                    : this.inner().virtualMachineProfile().extensionProfile().extensions()) {
                    this.extensions.put(inner.name(), new VirtualMachineScaleSetExtensionImpl(inner, this));
                }
            }
        }
    }

    @Override
    public VirtualMachineScaleSetVMs virtualMachines() {
        return new VirtualMachineScaleSetVMsImpl(
            this, this.manager().inner().getVirtualMachineScaleSetVMs(), this.myManager);
    }

    @Override
    public PagedIterable<VirtualMachineScaleSetSku> listAvailableSkus() {
        return this
            .manager()
            .inner()
            .getVirtualMachineScaleSets()
            .listSkus(this.resourceGroupName(), this.name())
            .mapPage(VirtualMachineScaleSetSkuImpl::new);
    }

    @Override
    public void deallocate() {
        this.deallocateAsync().block();
    }

    @Override
    public Mono<Void> deallocateAsync() {
        return this
            .manager()
            .inner()
            .getVirtualMachineScaleSets()
            .deallocateAsync(this.resourceGroupName(), this.name(), null)
            .map(aVoid -> this.refreshAsync())
            .then();
    }

    @Override
    public void powerOff() {
        this.powerOffAsync().block();
    }

    @Override
    public Mono<Void> powerOffAsync() {
        return this
            .manager()
            .inner()
            .getVirtualMachineScaleSets()
            .powerOffAsync(this.resourceGroupName(), this.name(), null, null);
    }

    @Override
    public void restart() {
        this.restartAsync().block();
    }

    @Override
    public Mono<Void> restartAsync() {
        return this
            .manager()
            .inner()
            .getVirtualMachineScaleSets()
            .restartAsync(this.resourceGroupName(), this.name(), null);
    }

    @Override
    public void start() {
        this.startAsync().block();
    }

    @Override
    public Mono<Void> startAsync() {
        return this.manager().inner().getVirtualMachineScaleSets()
            .startAsync(this.resourceGroupName(), this.name(), null);
    }

    @Override
    public void reimage() {
        this.reimageAsync().block();
    }

    @Override
    public Mono<Void> reimageAsync() {
        return this
            .manager()
            .inner()
            .getVirtualMachineScaleSets()
            .reimageAsync(this.resourceGroupName(), this.name(), null);
    }

    @Override
    public RunCommandResult runPowerShellScriptInVMInstance(
        String vmId, List<String> scriptLines, List<RunCommandInputParameter> scriptParameters) {
        return this
            .manager()
            .virtualMachineScaleSets()
            .runPowerShellScriptInVMInstance(
                this.resourceGroupName(), this.name(), vmId, scriptLines, scriptParameters);
    }

    @Override
    public Mono<RunCommandResult> runPowerShellScriptInVMInstanceAsync(
        String vmId, List<String> scriptLines, List<RunCommandInputParameter> scriptParameters) {
        return this
            .manager()
            .virtualMachineScaleSets()
            .runPowerShellScriptInVMInstanceAsync(
                this.resourceGroupName(), this.name(), vmId, scriptLines, scriptParameters);
    }

    @Override
    public RunCommandResult runShellScriptInVMInstance(
        String vmId, List<String> scriptLines, List<RunCommandInputParameter> scriptParameters) {
        return this
            .manager()
            .virtualMachineScaleSets()
            .runShellScriptInVMInstance(this.resourceGroupName(), this.name(), vmId, scriptLines, scriptParameters);
    }

    @Override
    public Mono<RunCommandResult> runShellScriptInVMInstanceAsync(
        String vmId, List<String> scriptLines, List<RunCommandInputParameter> scriptParameters) {
        return this
            .manager()
            .virtualMachineScaleSets()
            .runShellScriptInVMInstanceAsync(
                this.resourceGroupName(), this.name(), vmId, scriptLines, scriptParameters);
    }

    @Override
    public RunCommandResult runCommandInVMInstance(String vmId, RunCommandInput inputCommand) {
        return this
            .manager()
            .virtualMachineScaleSets()
            .runCommandInVMInstance(this.resourceGroupName(), this.name(), vmId, inputCommand);
    }

    @Override
    public Mono<RunCommandResult> runCommandVMInstanceAsync(String vmId, RunCommandInput inputCommand) {
        return this
            .manager()
            .virtualMachineScaleSets()
            .runCommandVMInstanceAsync(this.resourceGroupName(), this.name(), vmId, inputCommand);
    }

    @Override
    public String computerNamePrefix() {
        return this.inner().virtualMachineProfile().osProfile().computerNamePrefix();
    }

    @Override
    public OperatingSystemTypes osType() {
        return this.inner().virtualMachineProfile().storageProfile().osDisk().osType();
    }

    @Override
    public CachingTypes osDiskCachingType() {
        return this.inner().virtualMachineProfile().storageProfile().osDisk().caching();
    }

    @Override
    public String osDiskName() {
        return this.inner().virtualMachineProfile().storageProfile().osDisk().name();
    }

    @Override
    public UpgradeMode upgradeModel() {
        // upgradePolicy is a required property so no null check
        return this.inner().upgradePolicy().mode();
    }

    @Override
    public boolean overProvisionEnabled() {
        return this.inner().overprovision();
    }

    @Override
    public VirtualMachineScaleSetSkuTypes sku() {
        return VirtualMachineScaleSetSkuTypes.fromSku(this.inner().sku());
    }

    @Override
    public int capacity() {
        return Utils.toPrimitiveInt(this.inner().sku().capacity());
    }

    @Override
    public Network getPrimaryNetwork() throws IOException {
        String subnetId = primaryNicDefaultIpConfiguration().subnet().id();
        String virtualNetworkId = ResourceUtils.parentResourceIdFromResourceId(subnetId);
        return this.networkManager.networks().getById(virtualNetworkId);
    }

    @Override
    public LoadBalancer getPrimaryInternetFacingLoadBalancer() throws IOException {
        if (this.primaryInternetFacingLoadBalancer == null) {
            loadCurrentPrimaryLoadBalancersIfAvailableAsync().block();
        }
        return this.primaryInternetFacingLoadBalancer;
    }

    @Override
    public Map<String, LoadBalancerBackend> listPrimaryInternetFacingLoadBalancerBackends() throws IOException {
        if (this.getPrimaryInternetFacingLoadBalancer() != null) {
            return getBackendsAssociatedWithIpConfiguration(
                this.primaryInternetFacingLoadBalancer, primaryNicDefaultIpConfiguration());
        }
        return new HashMap<>();
    }

    @Override
    public Map<String, LoadBalancerInboundNatPool> listPrimaryInternetFacingLoadBalancerInboundNatPools()
        throws IOException {
        if (this.getPrimaryInternetFacingLoadBalancer() != null) {
            return getInboundNatPoolsAssociatedWithIpConfiguration(
                this.primaryInternetFacingLoadBalancer, primaryNicDefaultIpConfiguration());
        }
        return new HashMap<>();
    }

    @Override
    public LoadBalancer getPrimaryInternalLoadBalancer() throws IOException {
        if (this.primaryInternalLoadBalancer == null) {
            loadCurrentPrimaryLoadBalancersIfAvailableAsync().block();
        }
        return this.primaryInternalLoadBalancer;
    }

    @Override
    public Map<String, LoadBalancerBackend> listPrimaryInternalLoadBalancerBackends() throws IOException {
        if (this.getPrimaryInternalLoadBalancer() != null) {
            return getBackendsAssociatedWithIpConfiguration(
                this.primaryInternalLoadBalancer, primaryNicDefaultIpConfiguration());
        }
        return new HashMap<>();
    }

    @Override
    public Map<String, LoadBalancerInboundNatPool> listPrimaryInternalLoadBalancerInboundNatPools() throws IOException {
        if (this.getPrimaryInternalLoadBalancer() != null) {
            return getInboundNatPoolsAssociatedWithIpConfiguration(
                this.primaryInternalLoadBalancer, primaryNicDefaultIpConfiguration());
        }
        return new HashMap<>();
    }

    @Override
    public List<String> primaryPublicIpAddressIds() throws IOException {
        LoadBalancer loadBalancer = this.getPrimaryInternetFacingLoadBalancer();
        if (loadBalancer != null) {
            return loadBalancer.publicIpAddressIds();
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> vhdContainers() {
        if (this.storageProfile() != null
            && this.storageProfile().osDisk() != null
            && this.storageProfile().osDisk().vhdContainers() != null) {
            return this.storageProfile().osDisk().vhdContainers();
        }
        return new ArrayList<>();
    }

    @Override
    public VirtualMachineScaleSetStorageProfile storageProfile() {
        return this.inner().virtualMachineProfile().storageProfile();
    }

    @Override
    public VirtualMachineScaleSetNetworkProfile networkProfile() {
        return this.inner().virtualMachineProfile().networkProfile();
    }

    @Override
    public Map<String, VirtualMachineScaleSetExtension> extensions() {
        return Collections.unmodifiableMap(this.extensions);
    }

    @Override
    public VirtualMachinePriorityTypes virtualMachinePriority() {
        if (this.inner().virtualMachineProfile() != null) {
            return this.inner().virtualMachineProfile().priority();
        } else {
            return null;
        }
    }

    @Override
    public BillingProfile billingProfile() {
        if (this.inner().virtualMachineProfile() != null) {
            return this.inner().virtualMachineProfile().billingProfile();
        } else {
            return null;
        }
    }

    @Override
    public VirtualMachineScaleSetPublicIpAddressConfiguration virtualMachinePublicIpConfig() {
        VirtualMachineScaleSetIpConfiguration nicConfig = this.primaryNicDefaultIpConfiguration();
        return nicConfig.publicIpAddressConfiguration();
    }

    @Override
    public VirtualMachineEvictionPolicyTypes virtualMachineEvictionPolicy() {
        if (this.inner() != null &&  this.inner().virtualMachineProfile() != null) {
            return this.inner().virtualMachineProfile().evictionPolicy();
        } else {
            return null;
        }
    }

    @Override
    public boolean isIpForwardingEnabled() {
        VirtualMachineScaleSetNetworkConfiguration nicConfig = primaryNicConfiguration();
        if (nicConfig.enableIpForwarding() != null) {
            return nicConfig.enableIpForwarding();
        } else {
            return false;
        }
    }

    @Override
    public boolean isAcceleratedNetworkingEnabled() {
        VirtualMachineScaleSetNetworkConfiguration nicConfig = primaryNicConfiguration();
        if (nicConfig.enableAcceleratedNetworking() != null) {
            return nicConfig.enableAcceleratedNetworking();
        } else {
            return false;
        }
    }

    @Override
    public String networkSecurityGroupId() {
        VirtualMachineScaleSetNetworkConfiguration nicConfig = primaryNicConfiguration();
        if (nicConfig.networkSecurityGroup() != null) {
            return nicConfig.networkSecurityGroup().id();
        } else {
            return null;
        }
    }

    @Override
    public boolean isSinglePlacementGroupEnabled() {
        if (this.inner().singlePlacementGroup() != null) {
            return this.inner().singlePlacementGroup();
        } else {
            return false;
        }
    }

    @Override
    public List<String> applicationGatewayBackendAddressPoolsIds() {
        VirtualMachineScaleSetIpConfiguration nicIpConfig = this.primaryNicDefaultIpConfiguration();
        List<SubResource> backendPools = nicIpConfig.applicationGatewayBackendAddressPools();
        List<String> result = new ArrayList<>();
        if (backendPools != null) {
            for (SubResource backendPool : backendPools) {
                result.add(backendPool.id());
            }
        }
        return result;
    }

    @Override
    public List<String> applicationSecurityGroupIds() {
        VirtualMachineScaleSetIpConfiguration nicIpConfig = this.primaryNicDefaultIpConfiguration();
        List<String> asgIds = new ArrayList<>();
        if (nicIpConfig.applicationSecurityGroups() != null) {
            for (SubResource asg : nicIpConfig.applicationSecurityGroups()) {
                asgIds.add(asg.id());
            }
        }
        return asgIds;
    }

    @Override
    public Boolean doNotRunExtensionsOnOverprovisionedVMs() {
        return this.inner().doNotRunExtensionsOnOverprovisionedVMs();
    }

    @Override
    public ProximityPlacementGroup proximityPlacementGroup() {
        if (inner().proximityPlacementGroup() == null) {
            return null;
        } else {
            ResourceId id = ResourceId.fromString(inner().proximityPlacementGroup().id());
            ProximityPlacementGroupInner plgInner =
                manager().inner().getProximityPlacementGroups().getByResourceGroup(id.resourceGroupName(), id.name());
            if (plgInner == null) {
                return null;
            } else {
                return new ProximityPlacementGroupImpl(plgInner);
            }
        }
    }

    @Override
    public AdditionalCapabilities additionalCapabilities() {
        return this.inner().additionalCapabilities();
    }

    @Override
    public VirtualMachineScaleSetNetworkInterface getNetworkInterfaceByInstanceId(String instanceId, String name) {
        return this
            .networkManager
            .networkInterfaces()
            .getByVirtualMachineScaleSetInstanceId(this.resourceGroupName(), this.name(), instanceId, name);
    }

    @Override
    public PagedIterable<VirtualMachineScaleSetNetworkInterface> listNetworkInterfaces() {
        return this
            .networkManager
            .networkInterfaces()
            .listByVirtualMachineScaleSet(this.resourceGroupName(), this.name());
    }

    @Override
    public PagedIterable<VirtualMachineScaleSetNetworkInterface> listNetworkInterfacesByInstanceId(
        String virtualMachineInstanceId) {
        return this
            .networkManager
            .networkInterfaces()
            .listByVirtualMachineScaleSetInstanceId(this.resourceGroupName(), this.name(), virtualMachineInstanceId);
    }

    @Override
    public PagedFlux<VirtualMachineScaleSetNetworkInterface> listNetworkInterfacesByInstanceIdAsync(
        String virtualMachineInstanceId) {
        return this.
            networkManager
            .networkInterfaces()
            .listByVirtualMachineScaleSetInstanceIdAsync(
                this.resourceGroupName(), this.name(), virtualMachineInstanceId);
    }

    // Fluent setters

    @Override
    public VirtualMachineScaleSetImpl withSku(VirtualMachineScaleSetSkuTypes skuType) {
        this.inner().withSku(skuType.sku());
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withSku(VirtualMachineScaleSetSku sku) {
        return this.withSku(sku.skuType());
    }

    @Override
    public VirtualMachineScaleSetImpl withExistingPrimaryNetworkSubnet(Network network, String subnetName) {
        this.existingPrimaryNetworkSubnetNameToAssociate = mergePath(network.id(), "subnets", subnetName);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withExistingPrimaryInternetFacingLoadBalancer(LoadBalancer loadBalancer) {
        if (loadBalancer.publicIpAddressIds().isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Parameter loadBalancer must be an Internet facing load balancer"));
        }

        if (isInCreateMode()) {
            this.primaryInternetFacingLoadBalancer = loadBalancer;
            associateLoadBalancerToIpConfiguration(
                this.primaryInternetFacingLoadBalancer, this.primaryNicDefaultIpConfiguration());
        } else {
            this.primaryInternetFacingLoadBalancerToAttachOnUpdate = loadBalancer;
        }
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPrimaryInternetFacingLoadBalancerBackends(String... backendNames) {
        if (this.isInCreateMode()) {
            VirtualMachineScaleSetIpConfiguration defaultPrimaryIpConfig = this.primaryNicDefaultIpConfiguration();
            removeAllBackendAssociationFromIpConfiguration(
                this.primaryInternetFacingLoadBalancer, defaultPrimaryIpConfig);
            associateBackEndsToIpConfiguration(
                this.primaryInternetFacingLoadBalancer.id(), defaultPrimaryIpConfig, backendNames);
        } else {
            addToList(this.primaryInternetFacingLBBackendsToAddOnUpdate, backendNames);
        }
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPrimaryInternetFacingLoadBalancerInboundNatPools(String... natPoolNames) {
        if (this.isInCreateMode()) {
            VirtualMachineScaleSetIpConfiguration defaultPrimaryIpConfig = this.primaryNicDefaultIpConfiguration();
            removeAllInboundNatPoolAssociationFromIpConfiguration(
                this.primaryInternetFacingLoadBalancer, defaultPrimaryIpConfig);
            associateInboundNATPoolsToIpConfiguration(
                this.primaryInternetFacingLoadBalancer.id(), defaultPrimaryIpConfig, natPoolNames);
        } else {
            addToList(this.primaryInternetFacingLBInboundNatPoolsToAddOnUpdate, natPoolNames);
        }
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withExistingPrimaryInternalLoadBalancer(LoadBalancer loadBalancer) {
        if (!loadBalancer.publicIpAddressIds().isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Parameter loadBalancer must be an internal load balancer"));
        }
        String lbNetworkId = null;
        for (LoadBalancerPrivateFrontend frontEnd : loadBalancer.privateFrontends().values()) {
            if (frontEnd.networkId() != null) {
                lbNetworkId = frontEnd.networkId();
            }
        }

        if (isInCreateMode()) {
            String vmNICNetworkId =
                ResourceUtils.parentResourceIdFromResourceId(this.existingPrimaryNetworkSubnetNameToAssociate);
            // Azure has a really wired BUG that - it throws exception when vnet of VMSS and LB are not same
            // (code: NetworkInterfaceAndInternalLoadBalancerMustUseSameVnet) but at the same time Azure update
            // the VMSS's network section to refer this invalid internal LB. This makes VMSS un-usable and portal
            // will show a error above VMSS profile page.
            //
            if (!vmNICNetworkId.equalsIgnoreCase(lbNetworkId)) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Virtual network associated with scale set virtual machines"
                        + " and internal load balancer must be same. "
                        + "'"
                        + vmNICNetworkId
                        + "'"
                        + "'"
                        + lbNetworkId));
            }

            this.primaryInternalLoadBalancer = loadBalancer;
            associateLoadBalancerToIpConfiguration(
                this.primaryInternalLoadBalancer, this.primaryNicDefaultIpConfiguration());
        } else {
            String vmNicVnetId =
                ResourceUtils.parentResourceIdFromResourceId(primaryNicDefaultIpConfiguration().subnet().id());
            if (!vmNicVnetId.equalsIgnoreCase(lbNetworkId)) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Virtual network associated with scale set virtual machines"
                        + " and internal load balancer must be same. "
                        + "'"
                        + vmNicVnetId
                        + "'"
                        + "'"
                        + lbNetworkId));
            }
            this.primaryInternalLoadBalancerToAttachOnUpdate = loadBalancer;
        }
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPrimaryInternalLoadBalancerBackends(String... backendNames) {
        if (this.isInCreateMode()) {
            VirtualMachineScaleSetIpConfiguration defaultPrimaryIpConfig = primaryNicDefaultIpConfiguration();
            removeAllBackendAssociationFromIpConfiguration(this.primaryInternalLoadBalancer, defaultPrimaryIpConfig);
            associateBackEndsToIpConfiguration(
                this.primaryInternalLoadBalancer.id(), defaultPrimaryIpConfig, backendNames);
        } else {
            addToList(this.primaryInternalLBBackendsToAddOnUpdate, backendNames);
        }
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPrimaryInternalLoadBalancerInboundNatPools(String... natPoolNames) {
        if (this.isInCreateMode()) {
            VirtualMachineScaleSetIpConfiguration defaultPrimaryIpConfig = this.primaryNicDefaultIpConfiguration();
            removeAllInboundNatPoolAssociationFromIpConfiguration(
                this.primaryInternalLoadBalancer, defaultPrimaryIpConfig);
            associateInboundNATPoolsToIpConfiguration(
                this.primaryInternalLoadBalancer.id(), defaultPrimaryIpConfig, natPoolNames);
        } else {
            addToList(this.primaryInternalLBInboundNatPoolsToAddOnUpdate, natPoolNames);
        }
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutPrimaryInternalLoadBalancer() {
        if (this.isInUpdateMode()) {
            this.removePrimaryInternalLoadBalancerOnUpdate = true;
        }
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutPrimaryInternetFacingLoadBalancer() {
        if (this.isInUpdateMode()) {
            this.removePrimaryInternetFacingLoadBalancerOnUpdate = true;
        }
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutPrimaryInternetFacingLoadBalancerBackends(String... backendNames) {
        addToList(this.primaryInternetFacingLBBackendsToRemoveOnUpdate, backendNames);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutPrimaryInternalLoadBalancerBackends(String... backendNames) {
        addToList(this.primaryInternalLBBackendsToRemoveOnUpdate, backendNames);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutPrimaryInternetFacingLoadBalancerNatPools(String... natPoolNames) {
        addToList(this.primaryInternetFacingLBInboundNatPoolsToRemoveOnUpdate, natPoolNames);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutPrimaryInternalLoadBalancerNatPools(String... natPoolNames) {
        addToList(this.primaryInternalLBInboundNatPoolsToRemoveOnUpdate, natPoolNames);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPopularWindowsImage(KnownWindowsVirtualMachineImage knownImage) {
        return withSpecificWindowsImageVersion(knownImage.imageReference());
    }

    @Override
    public VirtualMachineScaleSetImpl withLatestWindowsImage(String publisher, String offer, String sku) {
        ImageReference imageReference =
            new ImageReference().withPublisher(publisher).withOffer(offer).withSku(sku).withVersion("latest");
        return withSpecificWindowsImageVersion(imageReference);
    }

    @Override
    public VirtualMachineScaleSetImpl withSpecificWindowsImageVersion(ImageReference imageReference) {
        this
            .inner()
            .virtualMachineProfile()
            .storageProfile()
            .osDisk()
            .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().virtualMachineProfile().storageProfile().withImageReference(imageReference);
        this.inner().virtualMachineProfile().osProfile().withWindowsConfiguration(new WindowsConfiguration());
        // sets defaults for "Stored(Custom)Image" or "VM(Platform)Image"
        this.inner().virtualMachineProfile().osProfile().windowsConfiguration().withProvisionVMAgent(true);
        this.inner().virtualMachineProfile().osProfile().windowsConfiguration().withEnableAutomaticUpdates(true);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withGeneralizedWindowsCustomImage(String customImageId) {
        ImageReference imageReferenceInner = new ImageReference();
        imageReferenceInner.withId(customImageId);
        this
            .inner()
            .virtualMachineProfile()
            .storageProfile()
            .osDisk()
            .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().virtualMachineProfile().storageProfile().withImageReference(imageReferenceInner);
        this.inner().virtualMachineProfile().osProfile().withWindowsConfiguration(new WindowsConfiguration());
        // sets defaults for "Stored(Custom)Image" or "VM(Platform)Image"
        this.inner().virtualMachineProfile().osProfile().windowsConfiguration().withProvisionVMAgent(true);
        this.inner().virtualMachineProfile().osProfile().windowsConfiguration().withEnableAutomaticUpdates(true);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withSpecializedWindowsCustomImage(String customImageId) {
        this.withGeneralizedWindowsCustomImage(customImageId);
        this.removeOsProfile = true;
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withStoredWindowsImage(String imageUrl) {
        VirtualHardDisk userImageVhd = new VirtualHardDisk();
        userImageVhd.withUri(imageUrl);
        this
            .inner()
            .virtualMachineProfile()
            .storageProfile()
            .osDisk()
            .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().virtualMachineProfile().storageProfile().osDisk().withImage(userImageVhd);
        // For platform image osType will be null, azure will pick it from the image metadata.
        this.inner().virtualMachineProfile().storageProfile().osDisk().withOsType(OperatingSystemTypes.WINDOWS);
        this.inner().virtualMachineProfile().osProfile().withWindowsConfiguration(new WindowsConfiguration());
        // sets defaults for "Stored(Custom)Image" or "VM(Platform)Image"
        this.inner().virtualMachineProfile().osProfile().windowsConfiguration().withProvisionVMAgent(true);
        this.inner().virtualMachineProfile().osProfile().windowsConfiguration().withEnableAutomaticUpdates(true);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPopularLinuxImage(KnownLinuxVirtualMachineImage knownImage) {
        return withSpecificLinuxImageVersion(knownImage.imageReference());
    }

    @Override
    public VirtualMachineScaleSetImpl withLatestLinuxImage(String publisher, String offer, String sku) {
        ImageReference imageReference =
            new ImageReference().withPublisher(publisher).withOffer(offer).withSku(sku).withVersion("latest");
        return withSpecificLinuxImageVersion(imageReference);
    }

    @Override
    public VirtualMachineScaleSetImpl withSpecificLinuxImageVersion(ImageReference imageReference) {
        this
            .inner()
            .virtualMachineProfile()
            .storageProfile()
            .osDisk()
            .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().virtualMachineProfile().storageProfile().withImageReference(imageReference);
        this.inner().virtualMachineProfile().osProfile().withLinuxConfiguration(new LinuxConfiguration());
        this.isMarketplaceLinuxImage = true;
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withGeneralizedLinuxCustomImage(String customImageId) {
        ImageReference imageReferenceInner = new ImageReference();
        imageReferenceInner.withId(customImageId);
        this
            .inner()
            .virtualMachineProfile()
            .storageProfile()
            .osDisk()
            .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().virtualMachineProfile().storageProfile().withImageReference(imageReferenceInner);
        this.inner().virtualMachineProfile().osProfile().withLinuxConfiguration(new LinuxConfiguration());
        this.isMarketplaceLinuxImage = true;
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withSpecializedLinuxCustomImage(String customImageId) {
        this.withGeneralizedLinuxCustomImage(customImageId);
        this.removeOsProfile = true;
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withStoredLinuxImage(String imageUrl) {
        VirtualHardDisk userImageVhd = new VirtualHardDisk();
        userImageVhd.withUri(imageUrl);
        this
            .inner()
            .virtualMachineProfile()
            .storageProfile()
            .osDisk()
            .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().virtualMachineProfile().storageProfile().osDisk().withImage(userImageVhd);
        // For platform image osType will be null, azure will pick it from the image metadata.
        this.inner().virtualMachineProfile().storageProfile().osDisk().withOsType(OperatingSystemTypes.LINUX);
        this.inner().virtualMachineProfile().osProfile().withLinuxConfiguration(new LinuxConfiguration());
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withAdminUsername(String adminUserName) {
        this.inner().virtualMachineProfile().osProfile().withAdminUsername(adminUserName);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withRootUsername(String adminUserName) {
        this.inner().virtualMachineProfile().osProfile().withAdminUsername(adminUserName);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withAdminPassword(String password) {
        this.inner().virtualMachineProfile().osProfile().withAdminPassword(password);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withRootPassword(String password) {
        this.inner().virtualMachineProfile().osProfile().withAdminPassword(password);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withSsh(String publicKeyData) {
        VirtualMachineScaleSetOSProfile osProfile = this.inner().virtualMachineProfile().osProfile();
        if (osProfile.linuxConfiguration().ssh() == null) {
            SshConfiguration sshConfiguration = new SshConfiguration();
            sshConfiguration.withPublicKeys(new ArrayList<SshPublicKey>());
            osProfile.linuxConfiguration().withSsh(sshConfiguration);
        }
        SshPublicKey sshPublicKey = new SshPublicKey();
        sshPublicKey.withKeyData(publicKeyData);
        sshPublicKey.withPath("/home/" + osProfile.adminUsername() + "/.ssh/authorized_keys");
        osProfile.linuxConfiguration().ssh().publicKeys().add(sshPublicKey);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withVMAgent() {
        this.inner().virtualMachineProfile().osProfile().windowsConfiguration().withProvisionVMAgent(true);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutVMAgent() {
        this.inner().virtualMachineProfile().osProfile().windowsConfiguration().withProvisionVMAgent(false);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withAutoUpdate() {
        this.inner().virtualMachineProfile().osProfile().windowsConfiguration().withEnableAutomaticUpdates(true);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutAutoUpdate() {
        this.inner().virtualMachineProfile().osProfile().windowsConfiguration().withEnableAutomaticUpdates(false);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withTimeZone(String timeZone) {
        this.inner().virtualMachineProfile().osProfile().windowsConfiguration().withTimeZone(timeZone);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withWinRM(WinRMListener listener) {
        if (this.inner().virtualMachineProfile().osProfile().windowsConfiguration().winRM() == null) {
            WinRMConfiguration winRMConfiguration = new WinRMConfiguration();
            this.inner().virtualMachineProfile().osProfile().windowsConfiguration().withWinRM(winRMConfiguration);
        }
        this.inner().virtualMachineProfile().osProfile().windowsConfiguration().winRM().listeners().add(listener);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withOSDiskCaching(CachingTypes cachingType) {
        this.inner().virtualMachineProfile().storageProfile().osDisk().withCaching(cachingType);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withOSDiskName(String name) {
        this.inner().virtualMachineProfile().storageProfile().osDisk().withName(name);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withComputerNamePrefix(String namePrefix) {
        this.inner().virtualMachineProfile().osProfile().withComputerNamePrefix(namePrefix);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withUpgradeMode(UpgradeMode upgradeMode) {
        if (this.inner().upgradePolicy() == null) {
            this.inner().withUpgradePolicy(new UpgradePolicy());
        }
        this.inner().upgradePolicy().withMode(upgradeMode);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withOverProvision(boolean enabled) {
        this.inner().withOverprovision(enabled);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withOverProvisioning() {
        return this.withOverProvision(true);
    }

    @Override
    public VirtualMachineScaleSetImpl withoutOverProvisioning() {
        return this.withOverProvision(false);
    }

    @Override
    public VirtualMachineScaleSetImpl withCapacity(long capacity) {
        this.inner().sku().withCapacity(capacity);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withNewStorageAccount(String name) {
        StorageAccount.DefinitionStages.WithGroup definitionWithGroup =
            this.storageManager.storageAccounts().define(name).withRegion(this.regionName());
        Creatable<StorageAccount> definitionAfterGroup;
        if (this.creatableGroup != null) {
            definitionAfterGroup = definitionWithGroup.withNewResourceGroup(this.creatableGroup);
        } else {
            definitionAfterGroup = definitionWithGroup.withExistingResourceGroup(this.resourceGroupName());
        }
        return withNewStorageAccount(definitionAfterGroup);
    }

    @Override
    public VirtualMachineScaleSetImpl withNewStorageAccount(Creatable<StorageAccount> creatable) {
        this.creatableStorageAccountKeys.add(this.addDependency(creatable));
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withExistingStorageAccount(StorageAccount storageAccount) {
        this.existingStorageAccountsToAssociate.add(storageAccount);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withCustomData(String base64EncodedCustomData) {
        this.inner().virtualMachineProfile().osProfile().withCustomData(base64EncodedCustomData);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withSecrets(List<VaultSecretGroup> secrets) {
        this.inner().virtualMachineProfile().osProfile().withSecrets(secrets);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutSecrets() {
        this.inner().virtualMachineProfile().osProfile().withSecrets(new ArrayList<VaultSecretGroup>());
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl defineNewExtension(String name) {
        return new VirtualMachineScaleSetExtensionImpl(new VirtualMachineScaleSetExtensionInner().withName(name), this);
    }

    protected VirtualMachineScaleSetImpl withExtension(VirtualMachineScaleSetExtensionImpl extension) {
        this.extensions.put(extension.name(), extension);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl updateExtension(String name) {
        return (VirtualMachineScaleSetExtensionImpl) this.extensions.get(name);
    }

    @Override
    public VirtualMachineScaleSetImpl withoutExtension(String name) {
        if (this.extensions.containsKey(name)) {
            this.extensions.remove(name);
        }
        return this;
    }

    @Override
    public boolean isManagedDiskEnabled() {
        VirtualMachineScaleSetStorageProfile storageProfile = this.inner().virtualMachineProfile().storageProfile();
        if (isOsDiskFromCustomImage(storageProfile)) {
            return true;
        }
        if (isOSDiskFromStoredImage(storageProfile)) {
            return false;
        }
        if (isOSDiskFromPlatformImage(storageProfile)) {
            if (this.isUnmanagedDiskSelected) {
                return false;
            }
        }
        if (isInCreateMode()) {
            return true;
        } else {
            List<String> vhdContainers = storageProfile.osDisk().vhdContainers();
            return vhdContainers == null || vhdContainers.size() == 0;
        }
    }

    @Override
    public boolean isManagedServiceIdentityEnabled() {
        ResourceIdentityType type = this.managedServiceIdentityType();
        return type != null && !type.equals(ResourceIdentityType.NONE);
    }

    @Override
    public String systemAssignedManagedServiceIdentityTenantId() {
        if (this.inner().identity() != null) {
            return this.inner().identity().tenantId();
        }
        return null;
    }

    @Override
    public String systemAssignedManagedServiceIdentityPrincipalId() {
        if (this.inner().identity() != null) {
            return this.inner().identity().principalId();
        }
        return null;
    }

    @Override
    public ResourceIdentityType managedServiceIdentityType() {
        if (this.inner().identity() != null) {
            return this.inner().identity().type();
        }
        return null;
    }

    @Override
    public Set<String> userAssignedManagedServiceIdentityIds() {
        if (this.inner().identity() != null && this.inner().identity().userAssignedIdentities() != null) {
            return Collections
                .unmodifiableSet(new HashSet<String>(this.inner().identity().userAssignedIdentities().keySet()));
        }
        return Collections.unmodifiableSet(new HashSet<String>());
    }

    @Override
    public Set<AvailabilityZoneId> availabilityZones() {
        Set<AvailabilityZoneId> zones = new HashSet<>();
        if (this.inner().zones() != null) {
            for (String zone : this.inner().zones()) {
                zones.add(AvailabilityZoneId.fromString(zone));
            }
        }
        return Collections.unmodifiableSet(zones);
    }

    @Override
    public boolean isBootDiagnosticsEnabled() {
        return this.bootDiagnosticsHandler.isBootDiagnosticsEnabled();
    }

    @Override
    public String bootDiagnosticsStorageUri() {
        return this.bootDiagnosticsHandler.bootDiagnosticsStorageUri();
    }

    @Override
    public StorageAccountTypes managedOSDiskStorageAccountType() {
        if (this.inner().virtualMachineProfile() != null
            && this.inner().virtualMachineProfile().storageProfile() != null
            && this.inner().virtualMachineProfile().storageProfile().osDisk() != null
            && this.inner().virtualMachineProfile().storageProfile().osDisk().managedDisk() != null) {
            return this.inner().virtualMachineProfile().storageProfile().osDisk().managedDisk().storageAccountType();
        }
        return null;
    }

    @Override
    public VirtualMachineScaleSetImpl withUnmanagedDisks() {
        this.isUnmanagedDiskSelected = true;
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withNewDataDisk(int sizeInGB) {
        throwIfManagedDiskDisabled(ManagedUnmanagedDiskErrors.VMSS_BOTH_UNMANAGED_AND_MANAGED_DISK_NOT_ALLOWED);
        this
            .managedDataDisks
            .implicitDisksToAssociate
            .add(new VirtualMachineScaleSetDataDisk().withLun(-1).withDiskSizeGB(sizeInGB));
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withNewDataDisk(int sizeInGB, int lun, CachingTypes cachingType) {
        throwIfManagedDiskDisabled(ManagedUnmanagedDiskErrors.VMSS_BOTH_UNMANAGED_AND_MANAGED_DISK_NOT_ALLOWED);
        this
            .managedDataDisks
            .implicitDisksToAssociate
            .add(new VirtualMachineScaleSetDataDisk().withLun(lun).withDiskSizeGB(sizeInGB).withCaching(cachingType));
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withNewDataDisk(
        int sizeInGB, int lun, CachingTypes cachingType, StorageAccountTypes storageAccountType) {
        throwIfManagedDiskDisabled(ManagedUnmanagedDiskErrors.VMSS_BOTH_UNMANAGED_AND_MANAGED_DISK_NOT_ALLOWED);
        VirtualMachineScaleSetManagedDiskParameters managedDiskParameters =
            new VirtualMachineScaleSetManagedDiskParameters();
        managedDiskParameters.withStorageAccountType(storageAccountType);
        this
            .managedDataDisks
            .implicitDisksToAssociate
            .add(
                new VirtualMachineScaleSetDataDisk()
                    .withLun(lun)
                    .withDiskSizeGB(sizeInGB)
                    .withCaching(cachingType)
                    .withManagedDisk(managedDiskParameters));
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutDataDisk(int lun) {
        if (!isManagedDiskEnabled()) {
            return this;
        }
        this.managedDataDisks.diskLunsToRemove.add(lun);
        return this;
    }

    /* TODO: Broken by change in Azure API behavior
    @Override
    public VirtualMachineScaleSetImpl withDataDiskUpdated(int lun, int newSizeInGB) {
        throwIfManagedDiskDisabled(ManagedUnmanagedDiskErrors.VMSS_NO_MANAGED_DISK_TO_UPDATE);
        VirtualMachineScaleSetDataDisk dataDisk = getDataDiskInner(lun);
        if (dataDisk == null) {
            throw new RuntimeException(String.format("A data disk with lun '%d' not found", lun));
        }
        dataDisk
            .withDiskSizeGB(newSizeInGB);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withDataDiskUpdated(int lun, int newSizeInGB, CachingTypes cachingType) {
        throwIfManagedDiskDisabled(ManagedUnmanagedDiskErrors.VMSS_NO_MANAGED_DISK_TO_UPDATE);
        VirtualMachineScaleSetDataDisk dataDisk = getDataDiskInner(lun);
        if (dataDisk == null) {
            throw new RuntimeException(String.format("A data disk with lun '%d' not found", lun));
        }
        dataDisk
            .withDiskSizeGB(newSizeInGB)
            .withCaching(cachingType);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withDataDiskUpdated(int lun,
        int newSizeInGB,
        CachingTypes cachingType,
        StorageAccountTypes storageAccountType) {
            throwIfManagedDiskDisabled(ManagedUnmanagedDiskErrors.VMSS_NO_MANAGED_DISK_TO_UPDATE);
            VirtualMachineScaleSetDataDisk dataDisk = getDataDiskInner(lun);
            if (dataDisk == null) {
                throw new RuntimeException(String.format("A data disk with lun '%d' not found", lun));
            }
            dataDisk
                .withDiskSizeGB(newSizeInGB)
                .withCaching(cachingType)
                .managedDisk()
                .withStorageAccountType(storageAccountType);
            return this;
    }

    private VirtualMachineScaleSetDataDisk getDataDiskInner(int lun) {
        VirtualMachineScaleSetStorageProfile storageProfile = this
                .inner()
                .virtualMachineProfile()
                .storageProfile();
        List<VirtualMachineScaleSetDataDisk> dataDisks = storageProfile
                .dataDisks();
        if (dataDisks == null) {
            return null;
        }
        for (VirtualMachineScaleSetDataDisk dataDisk : dataDisks) {
            if (dataDisk.lun() == lun) {
                return dataDisk;
            }
        }
        return null;
    }
    */

    @Override
    public VirtualMachineScaleSetImpl withNewDataDiskFromImage(int imageLun) {
        this.managedDataDisks.newDisksFromImage.add(new VirtualMachineScaleSetDataDisk().withLun(imageLun));
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withNewDataDiskFromImage(
        int imageLun, int newSizeInGB, CachingTypes cachingType) {
        this
            .managedDataDisks
            .newDisksFromImage
            .add(
                new VirtualMachineScaleSetDataDisk()
                    .withLun(imageLun)
                    .withDiskSizeGB(newSizeInGB)
                    .withCaching(cachingType));
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withNewDataDiskFromImage(
        int imageLun, int newSizeInGB, CachingTypes cachingType, StorageAccountTypes storageAccountType) {
        VirtualMachineScaleSetManagedDiskParameters managedDiskParameters =
            new VirtualMachineScaleSetManagedDiskParameters();
        managedDiskParameters.withStorageAccountType(storageAccountType);
        this
            .managedDataDisks
            .newDisksFromImage
            .add(
                new VirtualMachineScaleSetDataDisk()
                    .withLun(imageLun)
                    .withDiskSizeGB(newSizeInGB)
                    .withManagedDisk(managedDiskParameters)
                    .withCaching(cachingType));
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withOSDiskStorageAccountType(StorageAccountTypes accountType) {
        // withers is limited to VMSS based on ManagedDisk.
        this
            .inner()
            .virtualMachineProfile()
            .storageProfile()
            .osDisk()
            .withManagedDisk(new VirtualMachineScaleSetManagedDiskParameters().withStorageAccountType(accountType));
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withDataDiskDefaultCachingType(CachingTypes cachingType) {
        this.managedDataDisks.setDefaultCachingType(cachingType);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withDataDiskDefaultStorageAccountType(StorageAccountTypes storageAccountType) {
        this.managedDataDisks.setDefaultStorageAccountType(storageAccountType);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withSystemAssignedManagedServiceIdentity() {
        this.virtualMachineScaleSetMsiHandler.withLocalManagedServiceIdentity();
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutSystemAssignedManagedServiceIdentity() {
        this.virtualMachineScaleSetMsiHandler.withoutLocalManagedServiceIdentity();
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withSystemAssignedIdentityBasedAccessTo(String resourceId, BuiltInRole role) {
        this.virtualMachineScaleSetMsiHandler.withAccessTo(resourceId, role);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(BuiltInRole asRole) {
        this.virtualMachineScaleSetMsiHandler.withAccessToCurrentResourceGroup(asRole);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withSystemAssignedIdentityBasedAccessTo(String scope, String roleDefinitionId) {
        this.virtualMachineScaleSetMsiHandler.withAccessTo(scope, roleDefinitionId);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(
        String roleDefinitionId) {
        this.virtualMachineScaleSetMsiHandler.withAccessToCurrentResourceGroup(roleDefinitionId);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withNewUserAssignedManagedServiceIdentity(Creatable<Identity> creatableIdentity) {
        this.virtualMachineScaleSetMsiHandler.withNewExternalManagedServiceIdentity(creatableIdentity);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withExistingUserAssignedManagedServiceIdentity(Identity identity) {
        this.virtualMachineScaleSetMsiHandler.withExistingExternalManagedServiceIdentity(identity);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutUserAssignedManagedServiceIdentity(String identityId) {
        this.virtualMachineScaleSetMsiHandler.withoutExternalManagedServiceIdentity(identityId);
        return this;
    }

    // Create Update specific methods
    //
    @Override
    protected void beforeCreating() {
        if (this.extensions.size() > 0) {
            this
                .inner()
                .virtualMachineProfile()
                .withExtensionProfile(new VirtualMachineScaleSetExtensionProfile())
                .extensionProfile()
                .withExtensions(innersFromWrappers(this.extensions.values()));
        }
    }

    @Override
    protected Mono<VirtualMachineScaleSetInner> createInner() {
        if (isInCreateMode()) {
            this.setOSProfileDefaults();
            this.setOSDiskDefault();
        }
        this.setPrimaryIpConfigurationSubnet();
        return this
            .setPrimaryIpConfigurationBackendsAndInboundNatPoolsAsync()
            .flatMap(
                virtualMachineScaleSet -> {
                    if (isManagedDiskEnabled()) {
                        this.managedDataDisks.setDataDisksDefaults();
                    } else {
                        List<VirtualMachineScaleSetDataDisk> dataDisks =
                            this.inner().virtualMachineProfile().storageProfile().dataDisks();
                        VirtualMachineScaleSetUnmanagedDataDiskImpl.setDataDisksDefaults(dataDisks, this.name());
                    }
                    this.handleUnManagedOSDiskContainers();
                    this.bootDiagnosticsHandler.handleDiagnosticsSettings();
                    this.virtualMachineScaleSetMsiHandler.processCreatedExternalIdentities();
                    this.virtualMachineScaleSetMsiHandler.handleExternalIdentities();
                    this.createNewProximityPlacementGroup();
                    return this
                        .manager()
                        .inner()
                        .getVirtualMachineScaleSets()
                        .createOrUpdateAsync(resourceGroupName(), name(), inner());
                });
    }

    @Override
    protected void afterCreating() {
        this.clearCachedProperties();
        this.virtualMachineScaleSetMsiHandler.clear();
    }

    @Override
    public Mono<VirtualMachineScaleSet> updateResourceAsync() {
        if (this.extensions.size() > 0) {
            this
                .inner()
                .virtualMachineProfile()
                .withExtensionProfile(new VirtualMachineScaleSetExtensionProfile())
                .extensionProfile()
                .withExtensions(innersFromWrappers(this.extensions.values()));
        }
        this.setPrimaryIpConfigurationSubnet();
        final VirtualMachineScaleSetImpl self = this;
        return this
            .setPrimaryIpConfigurationBackendsAndInboundNatPoolsAsync()
            .map(
                virtualMachineScaleSet -> {
                    if (isManagedDiskEnabled()) {
                        this.managedDataDisks.setDataDisksDefaults();
                    } else {
                        List<VirtualMachineScaleSetDataDisk> dataDisks =
                            this.inner().virtualMachineProfile().storageProfile().dataDisks();
                        VirtualMachineScaleSetUnmanagedDataDiskImpl.setDataDisksDefaults(dataDisks, this.name());
                    }
                    this.handleUnManagedOSDiskContainers();
                    this.bootDiagnosticsHandler.handleDiagnosticsSettings();
                    this.virtualMachineScaleSetMsiHandler.processCreatedExternalIdentities();
                    //
                    VirtualMachineScaleSetUpdate updateParameter = VMSSPatchPayload.preparePatchPayload(this);
                    //
                    this.virtualMachineScaleSetMsiHandler.handleExternalIdentities(updateParameter);
                    return updateParameter;
                })
            .flatMap(
                updateParameter ->
                    this
                        .manager()
                        .inner()
                        .getVirtualMachineScaleSets()
                        .updateAsync(resourceGroupName(), name(), updateParameter)
                        .map(
                            vmssInner -> {
                                setInner(vmssInner);
                                self.clearCachedProperties();
                                self.initializeChildrenFromInner();
                                self.virtualMachineScaleSetMsiHandler.clear();
                                return self;
                            }));
    }

    @Override
    public Mono<VirtualMachineScaleSet> refreshAsync() {
        return super
            .refreshAsync()
            .map(
                scaleSet -> {
                    VirtualMachineScaleSetImpl impl = (VirtualMachineScaleSetImpl) scaleSet;
                    impl.clearCachedProperties();
                    impl.initializeChildrenFromInner();
                    return impl;
                });
    }

    @Override
    protected Mono<VirtualMachineScaleSetInner> getInnerAsync() {
        return this
            .manager()
            .inner()
            .getVirtualMachineScaleSets()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    // Helpers
    //

    private boolean isInUpdateMode() {
        return !this.isInCreateMode();
    }

    private void setOSProfileDefaults() {
        if (isInUpdateMode()) {
            return;
        }
        if (this.inner().sku().capacity() == null) {
            this.withCapacity(2);
        }
        if (this.inner().upgradePolicy() == null || this.inner().upgradePolicy().mode() == null) {
            this.inner().withUpgradePolicy(new UpgradePolicy().withMode(UpgradeMode.AUTOMATIC));
        }
        VirtualMachineScaleSetOSProfile osProfile = this.inner().virtualMachineProfile().osProfile();
        VirtualMachineScaleSetOSDisk osDisk = this.inner().virtualMachineProfile().storageProfile().osDisk();
        if (!removeOsProfile && isOSDiskFromImage(osDisk)) {
            // ODDisk CreateOption: FROM_IMAGE
            //
            if (this.osType() == OperatingSystemTypes.LINUX || this.isMarketplaceLinuxImage) {
                if (osProfile.linuxConfiguration() == null) {
                    osProfile.withLinuxConfiguration(new LinuxConfiguration());
                }
                osProfile.linuxConfiguration().withDisablePasswordAuthentication(osProfile.adminPassword() == null);
            }
            if (this.computerNamePrefix() == null) {
                // VM name cannot contain only numeric values and cannot exceed 15 chars
                if (this.name().matches("[0-9]+")) {
                    withComputerNamePrefix(this.namer.randomName("vmss-vm", 12));
                } else if (this.name().length() <= 12) {
                    withComputerNamePrefix(this.name() + "-vm");
                } else {
                    withComputerNamePrefix(this.namer.randomName("vmss-vm", 12));
                }
            }
        } else {
            // NOP [ODDisk CreateOption: ATTACH, ATTACH is not supported for VMSS]
            this.inner().virtualMachineProfile().withOsProfile(null);
        }
    }

    private void setOSDiskDefault() {
        if (isInUpdateMode()) {
            return;
        }
        VirtualMachineScaleSetStorageProfile storageProfile = this.inner().virtualMachineProfile().storageProfile();
        VirtualMachineScaleSetOSDisk osDisk = storageProfile.osDisk();
        if (isOSDiskFromImage(osDisk)) {
            // ODDisk CreateOption: FROM_IMAGE
            //
            if (isManagedDiskEnabled()) {
                // Note:
                // Managed disk
                //     Supported: PlatformImage and CustomImage
                //     UnSupported: StoredImage
                //
                if (osDisk.managedDisk() == null) {
                    osDisk.withManagedDisk(new VirtualMachineScaleSetManagedDiskParameters());
                }
                if (osDisk.managedDisk().storageAccountType() == null) {
                    osDisk.managedDisk().withStorageAccountType(StorageAccountTypes.STANDARD_LRS);
                }
                osDisk.withVhdContainers(null);
                // We won't set osDisk.name() explicitly for managed disk, if it is null CRP generates unique
                // name for the disk resource within the resource group.
            } else {
                // Note:
                // Native (un-managed) disk
                //     Supported: PlatformImage and StoredImage
                //     UnSupported: CustomImage
                //
                osDisk.withManagedDisk(null);
                if (osDisk.name() == null) {
                    withOSDiskName(this.name() + "-os-disk");
                }
            }
        }
        if (this.osDiskCachingType() == null) {
            withOSDiskCaching(CachingTypes.READ_WRITE);
        }
    }

    @Override
    public void beforeGroupCreateOrUpdate() {
        // Adding delayed storage account dependency if needed
        //
        this.prepareOSDiskContainers();
        this.bootDiagnosticsHandler.prepare();
    }

    protected void prepareOSDiskContainers() {
        if (isManagedDiskEnabled()) {
            return;
        }
        final VirtualMachineScaleSetStorageProfile storageProfile = inner().virtualMachineProfile().storageProfile();
        if (isOSDiskFromStoredImage(storageProfile)) {
            // There is a restriction currently that virtual machine's disk cannot be stored in multiple storage
            // accounts if scale set is based on stored image. Remove this check once azure start supporting it.
            //
            return;
        }
        if (this.isInCreateMode()
            && this.creatableStorageAccountKeys.isEmpty()
            && this.existingStorageAccountsToAssociate.isEmpty()) {
            String accountName = this.namer.randomName("stg", 24).replace("-", "");
            Creatable<StorageAccount> storageAccountCreatable;
            if (this.creatableGroup != null) {
                storageAccountCreatable =
                    this
                        .storageManager
                        .storageAccounts()
                        .define(accountName)
                        .withRegion(this.regionName())
                        .withNewResourceGroup(this.creatableGroup);
            } else {
                storageAccountCreatable =
                    this
                        .storageManager
                        .storageAccounts()
                        .define(accountName)
                        .withRegion(this.regionName())
                        .withExistingResourceGroup(this.resourceGroupName());
            }
            this.creatableStorageAccountKeys.add(this.addDependency(storageAccountCreatable));
        }
    }

    private void handleUnManagedOSDiskContainers() {
        final VirtualMachineScaleSetStorageProfile storageProfile = inner().virtualMachineProfile().storageProfile();
        if (isManagedDiskEnabled()) {
            storageProfile.osDisk().withVhdContainers(null);
            return;
        }
        if (isOSDiskFromStoredImage(storageProfile)) {
            // There is a restriction currently that virtual machine's disk cannot be stored in multiple storage
            // accounts if scale set is based on stored image. Remove this check once azure start supporting it.
            //
            storageProfile.osDisk().vhdContainers().clear();
            return;
        }

        String containerName = null;
        for (String containerUrl : storageProfile.osDisk().vhdContainers()) {
            containerName = containerUrl.substring(containerUrl.lastIndexOf("/") + 1);
            break;
        }

        if (containerName == null) {
            containerName = "vhds";
        }

        if (isInCreateMode()
            && this.creatableStorageAccountKeys.isEmpty()
            && this.existingStorageAccountsToAssociate.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Expected storage account(s) for VMSS OS disk containers not found"));
        }

        for (String storageAccountKey : this.creatableStorageAccountKeys) {
            StorageAccount storageAccount = this.<StorageAccount>taskResult(storageAccountKey);
            storageProfile
                .osDisk()
                .vhdContainers()
                .add(mergePath(storageAccount.endPoints().primary().blob(), containerName));
        }

        for (StorageAccount storageAccount : this.existingStorageAccountsToAssociate) {
            storageProfile
                .osDisk()
                .vhdContainers()
                .add(mergePath(storageAccount.endPoints().primary().blob(), containerName));
        }
        this.creatableStorageAccountKeys.clear();
        this.existingStorageAccountsToAssociate.clear();
    }

    private void setPrimaryIpConfigurationSubnet() {
        if (isInUpdateMode()) {
            return;
        }

        VirtualMachineScaleSetIpConfiguration ipConfig = this.primaryNicDefaultIpConfiguration();
        ipConfig.withSubnet(new ApiEntityReference().withId(this.existingPrimaryNetworkSubnetNameToAssociate));
        this.existingPrimaryNetworkSubnetNameToAssociate = null;
    }

    private Mono<VirtualMachineScaleSetImpl> setPrimaryIpConfigurationBackendsAndInboundNatPoolsAsync() {
        if (isInCreateMode()) {
            return Mono.just(this);
        }

        try {
            return this
                .loadCurrentPrimaryLoadBalancersIfAvailableAsync()
                .map(
                    virtualMachineScaleSet -> {
                        VirtualMachineScaleSetIpConfiguration primaryIpConfig = primaryNicDefaultIpConfiguration();
                        if (this.primaryInternetFacingLoadBalancer != null) {
                            removeBackendsFromIpConfiguration(
                                this.primaryInternetFacingLoadBalancer.id(),
                                primaryIpConfig,
                                this.primaryInternetFacingLBBackendsToRemoveOnUpdate.toArray(new String[0]));

                            associateBackEndsToIpConfiguration(
                                primaryInternetFacingLoadBalancer.id(),
                                primaryIpConfig,
                                this.primaryInternetFacingLBBackendsToAddOnUpdate.toArray(new String[0]));

                            removeInboundNatPoolsFromIpConfiguration(
                                this.primaryInternetFacingLoadBalancer.id(),
                                primaryIpConfig,
                                this.primaryInternetFacingLBInboundNatPoolsToRemoveOnUpdate.toArray(new String[0]));

                            associateInboundNATPoolsToIpConfiguration(
                                primaryInternetFacingLoadBalancer.id(),
                                primaryIpConfig,
                                this.primaryInternetFacingLBInboundNatPoolsToAddOnUpdate.toArray(new String[0]));
                        }

                        if (this.primaryInternalLoadBalancer != null) {
                            removeBackendsFromIpConfiguration(
                                this.primaryInternalLoadBalancer.id(),
                                primaryIpConfig,
                                this.primaryInternalLBBackendsToRemoveOnUpdate.toArray(new String[0]));

                            associateBackEndsToIpConfiguration(
                                primaryInternalLoadBalancer.id(),
                                primaryIpConfig,
                                this.primaryInternalLBBackendsToAddOnUpdate.toArray(new String[0]));

                            removeInboundNatPoolsFromIpConfiguration(
                                this.primaryInternalLoadBalancer.id(),
                                primaryIpConfig,
                                this.primaryInternalLBInboundNatPoolsToRemoveOnUpdate.toArray(new String[0]));

                            associateInboundNATPoolsToIpConfiguration(
                                primaryInternalLoadBalancer.id(),
                                primaryIpConfig,
                                this.primaryInternalLBInboundNatPoolsToAddOnUpdate.toArray(new String[0]));
                        }

                        if (this.removePrimaryInternetFacingLoadBalancerOnUpdate) {
                            if (this.primaryInternetFacingLoadBalancer != null) {
                                removeLoadBalancerAssociationFromIpConfiguration(
                                    this.primaryInternetFacingLoadBalancer, primaryIpConfig);
                            }
                        }

                        if (this.removePrimaryInternalLoadBalancerOnUpdate) {
                            if (this.primaryInternalLoadBalancer != null) {
                                removeLoadBalancerAssociationFromIpConfiguration(
                                    this.primaryInternalLoadBalancer, primaryIpConfig);
                            }
                        }

                        if (this.primaryInternetFacingLoadBalancerToAttachOnUpdate != null) {
                            if (this.primaryInternetFacingLoadBalancer != null) {
                                removeLoadBalancerAssociationFromIpConfiguration(
                                    this.primaryInternetFacingLoadBalancer, primaryIpConfig);
                            }
                            associateLoadBalancerToIpConfiguration(
                                this.primaryInternetFacingLoadBalancerToAttachOnUpdate, primaryIpConfig);
                            if (!this.primaryInternetFacingLBBackendsToAddOnUpdate.isEmpty()) {
                                removeAllBackendAssociationFromIpConfiguration(
                                    this.primaryInternetFacingLoadBalancerToAttachOnUpdate, primaryIpConfig);
                                associateBackEndsToIpConfiguration(
                                    this.primaryInternetFacingLoadBalancerToAttachOnUpdate.id(),
                                    primaryIpConfig,
                                    this.primaryInternetFacingLBBackendsToAddOnUpdate.toArray(new String[0]));
                            }
                            if (!this.primaryInternetFacingLBInboundNatPoolsToAddOnUpdate.isEmpty()) {
                                removeAllInboundNatPoolAssociationFromIpConfiguration(
                                    this.primaryInternetFacingLoadBalancerToAttachOnUpdate, primaryIpConfig);
                                associateInboundNATPoolsToIpConfiguration(
                                    this.primaryInternetFacingLoadBalancerToAttachOnUpdate.id(),
                                    primaryIpConfig,
                                    this.primaryInternetFacingLBInboundNatPoolsToAddOnUpdate.toArray(new String[0]));
                            }
                        }

                        if (this.primaryInternalLoadBalancerToAttachOnUpdate != null) {
                            if (this.primaryInternalLoadBalancer != null) {
                                removeLoadBalancerAssociationFromIpConfiguration(
                                    this.primaryInternalLoadBalancer, primaryIpConfig);
                            }
                            associateLoadBalancerToIpConfiguration(
                                this.primaryInternalLoadBalancerToAttachOnUpdate, primaryIpConfig);
                            if (!this.primaryInternalLBBackendsToAddOnUpdate.isEmpty()) {
                                removeAllBackendAssociationFromIpConfiguration(
                                    this.primaryInternalLoadBalancerToAttachOnUpdate, primaryIpConfig);
                                associateBackEndsToIpConfiguration(
                                    this.primaryInternalLoadBalancerToAttachOnUpdate.id(),
                                    primaryIpConfig,
                                    this.primaryInternalLBBackendsToAddOnUpdate.toArray(new String[0]));
                            }

                            if (!this.primaryInternalLBInboundNatPoolsToAddOnUpdate.isEmpty()) {
                                removeAllInboundNatPoolAssociationFromIpConfiguration(
                                    this.primaryInternalLoadBalancerToAttachOnUpdate, primaryIpConfig);
                                associateInboundNATPoolsToIpConfiguration(
                                    this.primaryInternalLoadBalancerToAttachOnUpdate.id(),
                                    primaryIpConfig,
                                    this.primaryInternalLBInboundNatPoolsToAddOnUpdate.toArray(new String[0]));
                            }
                        }

                        this.removePrimaryInternetFacingLoadBalancerOnUpdate = false;
                        this.removePrimaryInternalLoadBalancerOnUpdate = false;
                        this.primaryInternetFacingLoadBalancerToAttachOnUpdate = null;
                        this.primaryInternalLoadBalancerToAttachOnUpdate = null;
                        this.primaryInternetFacingLBBackendsToRemoveOnUpdate.clear();
                        this.primaryInternetFacingLBInboundNatPoolsToRemoveOnUpdate.clear();
                        this.primaryInternalLBBackendsToRemoveOnUpdate.clear();
                        this.primaryInternalLBInboundNatPoolsToRemoveOnUpdate.clear();
                        this.primaryInternetFacingLBBackendsToAddOnUpdate.clear();
                        this.primaryInternetFacingLBInboundNatPoolsToAddOnUpdate.clear();
                        this.primaryInternalLBBackendsToAddOnUpdate.clear();
                        this.primaryInternalLBInboundNatPoolsToAddOnUpdate.clear();
                        return this;
                    });
        } catch (IOException ioException) {
            throw logger.logExceptionAsError(new RuntimeException(ioException));
        }
    }

    private void clearCachedProperties() {
        this.primaryInternetFacingLoadBalancer = null;
        this.primaryInternalLoadBalancer = null;
    }

    private Mono<VirtualMachineScaleSetImpl> loadCurrentPrimaryLoadBalancersIfAvailableAsync() throws IOException {
        Mono<VirtualMachineScaleSetImpl> self = Mono.just(this);
        if (this.primaryInternetFacingLoadBalancer != null && this.primaryInternalLoadBalancer != null) {
            return self;
        }

        String firstLoadBalancerId = null;
        VirtualMachineScaleSetIpConfiguration ipConfig = primaryNicDefaultIpConfiguration();
        if (!ipConfig.loadBalancerBackendAddressPools().isEmpty()) {
            firstLoadBalancerId =
                ResourceUtils.parentResourceIdFromResourceId(ipConfig.loadBalancerBackendAddressPools().get(0).id());
        }

        if (firstLoadBalancerId == null && !ipConfig.loadBalancerInboundNatPools().isEmpty()) {
            firstLoadBalancerId =
                ResourceUtils.parentResourceIdFromResourceId(ipConfig.loadBalancerInboundNatPools().get(0).id());
        }

        if (firstLoadBalancerId == null) {
            return self;
        }

        self =
            self
                .concatWith(
                    Mono
                        .just(firstLoadBalancerId)
                        .flatMap(
                            id ->
                                this
                                    .networkManager
                                    .loadBalancers()
                                    .getByIdAsync(id)
                                    .map(
                                        loadBalancer1 -> {
                                            if (loadBalancer1.publicIpAddressIds() != null
                                                && loadBalancer1.publicIpAddressIds().size() > 0) {
                                                this.primaryInternetFacingLoadBalancer = loadBalancer1;
                                            } else {
                                                this.primaryInternalLoadBalancer = loadBalancer1;
                                            }
                                            return this;
                                        })))
                .last();

        String secondLoadBalancerId = null;
        for (SubResource subResource : ipConfig.loadBalancerBackendAddressPools()) {
            if (!subResource.id().toLowerCase(Locale.ROOT)
                    .startsWith(firstLoadBalancerId.toLowerCase(Locale.ROOT))) {
                secondLoadBalancerId = ResourceUtils.parentResourceIdFromResourceId(subResource.id());
                break;
            }
        }

        if (secondLoadBalancerId == null) {
            for (SubResource subResource : ipConfig.loadBalancerInboundNatPools()) {
                if (!subResource.id().toLowerCase(Locale.ROOT)
                        .startsWith(firstLoadBalancerId.toLowerCase(Locale.ROOT))) {
                    secondLoadBalancerId = ResourceUtils.parentResourceIdFromResourceId(subResource.id());
                    break;
                }
            }
        }

        if (secondLoadBalancerId == null) {
            return self;
        }

        return self
            .concatWith(
                Mono
                    .just(secondLoadBalancerId)
                    .flatMap(
                        id ->
                            networkManager
                                .loadBalancers()
                                .getByIdAsync(id)
                                .map(
                                    loadBalancer2 -> {
                                        if (loadBalancer2.publicIpAddressIds() != null
                                            && loadBalancer2.publicIpAddressIds().size() > 0) {
                                            this.primaryInternetFacingLoadBalancer = loadBalancer2;
                                        } else {
                                            this.primaryInternalLoadBalancer = loadBalancer2;
                                        }
                                        return this;
                                    })))
            .last();
    }

    private VirtualMachineScaleSetIpConfiguration primaryNicDefaultIpConfiguration() {
        List<VirtualMachineScaleSetNetworkConfiguration> nicConfigurations =
            this.inner().virtualMachineProfile().networkProfile().networkInterfaceConfigurations();

        for (VirtualMachineScaleSetNetworkConfiguration nicConfiguration : nicConfigurations) {
            if (nicConfiguration.primary()) {
                if (nicConfiguration.ipConfigurations().size() > 0) {
                    VirtualMachineScaleSetIpConfiguration ipConfig = nicConfiguration.ipConfigurations().get(0);
                    if (ipConfig.loadBalancerBackendAddressPools() == null) {
                        ipConfig.withLoadBalancerBackendAddressPools(new ArrayList<>());
                    }
                    if (ipConfig.loadBalancerInboundNatPools() == null) {
                        ipConfig.withLoadBalancerInboundNatPools(new ArrayList<>());
                    }
                    return ipConfig;
                }
            }
        }
        throw logger.logExceptionAsError(new RuntimeException(
            "Could not find the primary nic configuration or an IP configuration in it"));
    }

    private VirtualMachineScaleSetNetworkConfiguration primaryNicConfiguration() {
        List<VirtualMachineScaleSetNetworkConfiguration> nicConfigurations =
            this.inner().virtualMachineProfile().networkProfile().networkInterfaceConfigurations();

        for (VirtualMachineScaleSetNetworkConfiguration nicConfiguration : nicConfigurations) {
            if (nicConfiguration.primary()) {
                return nicConfiguration;
            }
        }
        throw logger.logExceptionAsError(new RuntimeException("Could not find the primary nic configuration"));
    }

    private static void associateBackEndsToIpConfiguration(
        String loadBalancerId, VirtualMachineScaleSetIpConfiguration ipConfig, String... backendNames) {
        List<SubResource> backendSubResourcesToAssociate = new ArrayList<>();
        for (String backendName : backendNames) {
            String backendPoolId = mergePath(loadBalancerId, "backendAddressPools", backendName);
            boolean found = false;
            for (SubResource subResource : ipConfig.loadBalancerBackendAddressPools()) {
                if (subResource.id().equalsIgnoreCase(backendPoolId)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                backendSubResourcesToAssociate.add(new SubResource().withId(backendPoolId));
            }
        }

        for (SubResource backendSubResource : backendSubResourcesToAssociate) {
            ipConfig.loadBalancerBackendAddressPools().add(backendSubResource);
        }
    }

    private static void associateInboundNATPoolsToIpConfiguration(
        String loadBalancerId, VirtualMachineScaleSetIpConfiguration ipConfig, String... inboundNatPools) {
        List<SubResource> inboundNatPoolSubResourcesToAssociate = new ArrayList<>();
        for (String inboundNatPool : inboundNatPools) {
            String inboundNatPoolId = mergePath(loadBalancerId, "inboundNatPools", inboundNatPool);
            boolean found = false;
            for (SubResource subResource : ipConfig.loadBalancerInboundNatPools()) {
                if (subResource.id().equalsIgnoreCase(inboundNatPoolId)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                inboundNatPoolSubResourcesToAssociate.add(new SubResource().withId(inboundNatPoolId));
            }
        }

        for (SubResource backendSubResource : inboundNatPoolSubResourcesToAssociate) {
            ipConfig.loadBalancerInboundNatPools().add(backendSubResource);
        }
    }

    private static Map<String, LoadBalancerBackend> getBackendsAssociatedWithIpConfiguration(
        LoadBalancer loadBalancer, VirtualMachineScaleSetIpConfiguration ipConfig) {
        String loadBalancerId = loadBalancer.id();
        Map<String, LoadBalancerBackend> attachedBackends = new HashMap<>();
        Map<String, LoadBalancerBackend> lbBackends = loadBalancer.backends();
        for (LoadBalancerBackend lbBackend : lbBackends.values()) {
            String backendId = mergePath(loadBalancerId, "backendAddressPools", lbBackend.name());
            for (SubResource subResource : ipConfig.loadBalancerBackendAddressPools()) {
                if (subResource.id().equalsIgnoreCase(backendId)) {
                    attachedBackends.put(lbBackend.name(), lbBackend);
                }
            }
        }
        return attachedBackends;
    }

    private static Map<String, LoadBalancerInboundNatPool> getInboundNatPoolsAssociatedWithIpConfiguration(
        LoadBalancer loadBalancer, VirtualMachineScaleSetIpConfiguration ipConfig) {
        String loadBalancerId = loadBalancer.id();
        Map<String, LoadBalancerInboundNatPool> attachedInboundNatPools = new HashMap<>();
        Map<String, LoadBalancerInboundNatPool> lbInboundNatPools = loadBalancer.inboundNatPools();
        for (LoadBalancerInboundNatPool lbInboundNatPool : lbInboundNatPools.values()) {
            String inboundNatPoolId = mergePath(loadBalancerId, "inboundNatPools", lbInboundNatPool.name());
            for (SubResource subResource : ipConfig.loadBalancerInboundNatPools()) {
                if (subResource.id().equalsIgnoreCase(inboundNatPoolId)) {
                    attachedInboundNatPools.put(lbInboundNatPool.name(), lbInboundNatPool);
                }
            }
        }
        return attachedInboundNatPools;
    }

    private static void associateLoadBalancerToIpConfiguration(
        LoadBalancer loadBalancer, VirtualMachineScaleSetIpConfiguration ipConfig) {
        Collection<LoadBalancerBackend> backends = loadBalancer.backends().values();
        String[] backendNames = new String[backends.size()];
        int i = 0;
        for (LoadBalancerBackend backend : backends) {
            backendNames[i] = backend.name();
            i++;
        }

        associateBackEndsToIpConfiguration(loadBalancer.id(), ipConfig, backendNames);

        Collection<LoadBalancerInboundNatPool> inboundNatPools = loadBalancer.inboundNatPools().values();
        String[] natPoolNames = new String[inboundNatPools.size()];
        i = 0;
        for (LoadBalancerInboundNatPool inboundNatPool : inboundNatPools) {
            natPoolNames[i] = inboundNatPool.name();
            i++;
        }

        associateInboundNATPoolsToIpConfiguration(loadBalancer.id(), ipConfig, natPoolNames);
    }

    private static void removeLoadBalancerAssociationFromIpConfiguration(
        LoadBalancer loadBalancer, VirtualMachineScaleSetIpConfiguration ipConfig) {
        removeAllBackendAssociationFromIpConfiguration(loadBalancer, ipConfig);
        removeAllInboundNatPoolAssociationFromIpConfiguration(loadBalancer, ipConfig);
    }

    private static void removeAllBackendAssociationFromIpConfiguration(
        LoadBalancer loadBalancer, VirtualMachineScaleSetIpConfiguration ipConfig) {
        List<SubResource> toRemove = new ArrayList<>();
        for (SubResource subResource : ipConfig.loadBalancerBackendAddressPools()) {
            if (subResource.id().toLowerCase(Locale.ROOT)
                    .startsWith(loadBalancer.id().toLowerCase(Locale.ROOT) + "/")) {
                toRemove.add(subResource);
            }
        }

        for (SubResource subResource : toRemove) {
            ipConfig.loadBalancerBackendAddressPools().remove(subResource);
        }
    }

    private static void removeAllInboundNatPoolAssociationFromIpConfiguration(
        LoadBalancer loadBalancer, VirtualMachineScaleSetIpConfiguration ipConfig) {
        List<SubResource> toRemove = new ArrayList<>();
        for (SubResource subResource : ipConfig.loadBalancerInboundNatPools()) {
            if (subResource.id().toLowerCase(Locale.ROOT)
                    .startsWith(loadBalancer.id().toLowerCase(Locale.ROOT) + "/")) {
                toRemove.add(subResource);
            }
        }

        for (SubResource subResource : toRemove) {
            ipConfig.loadBalancerInboundNatPools().remove(subResource);
        }
    }

    private static void removeBackendsFromIpConfiguration(
        String loadBalancerId, VirtualMachineScaleSetIpConfiguration ipConfig, String... backendNames) {
        List<SubResource> toRemove = new ArrayList<>();
        for (String backendName : backendNames) {
            String backendPoolId = mergePath(loadBalancerId, "backendAddressPools", backendName);
            for (SubResource subResource : ipConfig.loadBalancerBackendAddressPools()) {
                if (subResource.id().equalsIgnoreCase(backendPoolId)) {
                    toRemove.add(subResource);
                    break;
                }
            }
        }

        for (SubResource subResource : toRemove) {
            ipConfig.loadBalancerBackendAddressPools().remove(subResource);
        }
    }

    private static void removeInboundNatPoolsFromIpConfiguration(
        String loadBalancerId, VirtualMachineScaleSetIpConfiguration ipConfig, String... inboundNatPoolNames) {
        List<SubResource> toRemove = new ArrayList<>();
        for (String natPoolName : inboundNatPoolNames) {
            String inboundNatPoolId = mergePath(loadBalancerId, "inboundNatPools", natPoolName);
            for (SubResource subResource : ipConfig.loadBalancerInboundNatPools()) {
                if (subResource.id().equalsIgnoreCase(inboundNatPoolId)) {
                    toRemove.add(subResource);
                    break;
                }
            }
        }

        for (SubResource subResource : toRemove) {
            ipConfig.loadBalancerInboundNatPools().remove(subResource);
        }
    }

    private static <T> void addToList(List<T> list, T[] items) {
        list.addAll(Arrays.asList(items));
    }

    private static String mergePath(String... segments) {
        StringBuilder builder = new StringBuilder();
        for (String segment : segments) {
            while (segment.length() > 1 && segment.endsWith("/")) {
                segment = segment.substring(0, segment.length() - 1);
            }

            if (segment.length() > 0) {
                builder.append(segment);
                builder.append("/");
            }
        }

        String merged = builder.toString();
        if (merged.endsWith("/")) {
            merged = merged.substring(0, merged.length() - 1);
        }
        return merged;
    }

    RoleAssignmentHelper.IdProvider idProvider() {
        return new RoleAssignmentHelper.IdProvider() {
            @Override
            public String principalId() {
                if (inner() != null && inner().identity() != null) {
                    return inner().identity().principalId();
                } else {
                    return null;
                }
            }

            @Override
            public String resourceId() {
                if (inner() != null) {
                    return inner().id();
                } else {
                    return null;
                }
            }
        };
    }

    protected VirtualMachineScaleSetImpl withUnmanagedDataDisk(
        VirtualMachineScaleSetUnmanagedDataDiskImpl unmanagedDisk) {
        if (this.inner().virtualMachineProfile().storageProfile().dataDisks() == null) {
            this
                .inner()
                .virtualMachineProfile()
                .storageProfile()
                .withDataDisks(new ArrayList<VirtualMachineScaleSetDataDisk>());
        }
        List<VirtualMachineScaleSetDataDisk> dataDisks =
            this.inner().virtualMachineProfile().storageProfile().dataDisks();
        dataDisks.add(unmanagedDisk.inner());
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withAvailabilityZone(AvailabilityZoneId zoneId) {
        // Note: Only for virtual machine scale set, new zone can be specified, hence
        // this option is available for both definition and update cases.
        //
        //
        if (this.inner().zones() == null) {
            this.inner().withZones(new ArrayList<>());
        }
        this.inner().zones().add(zoneId.toString());
        return this;
    }

    /**
     * Checks whether the OS disk is based on an image (image from PIR or custom image [captured, bringYourOwnFeature]).
     *
     * @param osDisk the osDisk value in the storage profile
     * @return true if the OS disk is configured to use image from PIR or custom image
     */
    private boolean isOSDiskFromImage(VirtualMachineScaleSetOSDisk osDisk) {
        return osDisk.createOption() == DiskCreateOptionTypes.FROM_IMAGE;
    }

    /**
     * Checks whether the OS disk is based on a CustomImage.
     *
     * <p>A custom image is represented by {@link VirtualMachineCustomImage}.
     *
     * @param storageProfile the storage profile
     * @return true if the OS disk is configured to be based on custom image.
     */
    private boolean isOsDiskFromCustomImage(VirtualMachineScaleSetStorageProfile storageProfile) {
        ImageReference imageReference = storageProfile.imageReference();
        return isOSDiskFromImage(storageProfile.osDisk()) && imageReference != null && imageReference.id() != null;
    }

    /**
     * Checks whether the OS disk is based on an platform image (image in PIR).
     *
     * @param storageProfile the storage profile
     * @return true if the OS disk is configured to be based on platform image.
     */
    private boolean isOSDiskFromPlatformImage(VirtualMachineScaleSetStorageProfile storageProfile) {
        ImageReference imageReference = storageProfile.imageReference();
        return isOSDiskFromImage(storageProfile.osDisk())
            && imageReference != null
            && imageReference.publisher() != null
            && imageReference.offer() != null
            && imageReference.sku() != null
            && imageReference.version() != null;
    }

    /**
     * Checks whether the OS disk is based on a stored image ('captured' or 'bring your own feature').
     *
     * @param storageProfile the storage profile
     * @return true if the OS disk is configured to use custom image ('captured' or 'bring your own feature')
     */
    private boolean isOSDiskFromStoredImage(VirtualMachineScaleSetStorageProfile storageProfile) {
        VirtualMachineScaleSetOSDisk osDisk = storageProfile.osDisk();
        return isOSDiskFromImage(osDisk) && osDisk.image() != null && osDisk.image().uri() != null;
    }

    private void throwIfManagedDiskDisabled(String message) {
        if (!this.isManagedDiskEnabled()) {
            throw logger.logExceptionAsError(new UnsupportedOperationException(message));
        }
    }

    @Override
    public VirtualMachineScaleSetImpl withBootDiagnostics() {
        this.bootDiagnosticsHandler.withBootDiagnostics();
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withBootDiagnostics(Creatable<StorageAccount> creatable) {
        this.bootDiagnosticsHandler.withBootDiagnostics(creatable);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withBootDiagnostics(StorageAccount storageAccount) {
        this.bootDiagnosticsHandler.withBootDiagnostics(storageAccount);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withBootDiagnostics(String storageAccountBlobEndpointUri) {
        this.bootDiagnosticsHandler.withBootDiagnostics(storageAccountBlobEndpointUri);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutBootDiagnostics() {
        this.bootDiagnosticsHandler.withoutBootDiagnostics();
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withMaxPrice(Double maxPrice) {
        this.inner().virtualMachineProfile().withBillingProfile(new BillingProfile().withMaxPrice(maxPrice));
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withVirtualMachinePriority(VirtualMachinePriorityTypes priority) {
        this.inner().virtualMachineProfile().withPriority(priority);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withLowPriorityVirtualMachine() {
        this.withVirtualMachinePriority(VirtualMachinePriorityTypes.LOW);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withLowPriorityVirtualMachine(VirtualMachineEvictionPolicyTypes policy) {
        this.withLowPriorityVirtualMachine();
        this.inner().virtualMachineProfile().withEvictionPolicy(policy);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withSpotPriorityVirtualMachine() {
        this.withVirtualMachinePriority(VirtualMachinePriorityTypes.SPOT);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withSpotPriorityVirtualMachine(VirtualMachineEvictionPolicyTypes policy) {
        this.withSpotPriorityVirtualMachine();
        this.inner().virtualMachineProfile().withEvictionPolicy(policy);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withVirtualMachinePublicIp() {
        VirtualMachineScaleSetIpConfiguration nicIpConfig = this.primaryNicDefaultIpConfiguration();
        if (nicIpConfig.publicIpAddressConfiguration() != null) {
            return this;
        } else {
            VirtualMachineScaleSetPublicIpAddressConfiguration pipConfig =
                new VirtualMachineScaleSetPublicIpAddressConfiguration();
            pipConfig.withName("pip1");
            pipConfig.withIdleTimeoutInMinutes(15);
            //
            nicIpConfig.withPublicIpAddressConfiguration(pipConfig);
            return this;
        }
    }

    @Override
    public VirtualMachineScaleSetImpl withVirtualMachinePublicIp(String leafDomainLabel) {
        VirtualMachineScaleSetIpConfiguration nicIpConfig = this.primaryNicDefaultIpConfiguration();
        if (nicIpConfig.publicIpAddressConfiguration() != null) {
            if (nicIpConfig.publicIpAddressConfiguration().dnsSettings() != null) {
                nicIpConfig.publicIpAddressConfiguration().dnsSettings().withDomainNameLabel(leafDomainLabel);
            } else {
                nicIpConfig
                    .publicIpAddressConfiguration()
                    .withDnsSettings(new VirtualMachineScaleSetPublicIpAddressConfigurationDnsSettings());
                nicIpConfig.publicIpAddressConfiguration().dnsSettings().withDomainNameLabel(leafDomainLabel);
            }
        } else {
            VirtualMachineScaleSetPublicIpAddressConfiguration pipConfig =
                new VirtualMachineScaleSetPublicIpAddressConfiguration();
            pipConfig.withName("pip1");
            pipConfig.withIdleTimeoutInMinutes(15);
            pipConfig.withDnsSettings(new VirtualMachineScaleSetPublicIpAddressConfigurationDnsSettings());
            pipConfig.dnsSettings().withDomainNameLabel(leafDomainLabel);
            nicIpConfig.withPublicIpAddressConfiguration(pipConfig);
        }
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withVirtualMachinePublicIp(
        VirtualMachineScaleSetPublicIpAddressConfiguration pipConfig) {
        VirtualMachineScaleSetIpConfiguration nicIpConfig = this.primaryNicDefaultIpConfiguration();
        nicIpConfig.withPublicIpAddressConfiguration(pipConfig);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withAcceleratedNetworking() {
        VirtualMachineScaleSetNetworkConfiguration nicConfig = this.primaryNicConfiguration();
        nicConfig.withEnableAcceleratedNetworking(true);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutAcceleratedNetworking() {
        VirtualMachineScaleSetNetworkConfiguration nicConfig = this.primaryNicConfiguration();
        nicConfig.withEnableAcceleratedNetworking(false);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withIpForwarding() {
        VirtualMachineScaleSetNetworkConfiguration nicConfig = this.primaryNicConfiguration();
        nicConfig.withEnableIpForwarding(true);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutIpForwarding() {
        VirtualMachineScaleSetNetworkConfiguration nicConfig = this.primaryNicConfiguration();
        nicConfig.withEnableIpForwarding(false);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withExistingNetworkSecurityGroup(NetworkSecurityGroup networkSecurityGroup) {
        VirtualMachineScaleSetNetworkConfiguration nicConfig = this.primaryNicConfiguration();
        nicConfig.withNetworkSecurityGroup(new SubResource().withId(networkSecurityGroup.id()));
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withExistingNetworkSecurityGroupId(String networkSecurityGroupId) {
        VirtualMachineScaleSetNetworkConfiguration nicConfig = this.primaryNicConfiguration();
        nicConfig.withNetworkSecurityGroup(new SubResource().withId(networkSecurityGroupId));
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutNetworkSecurityGroup() {
        VirtualMachineScaleSetNetworkConfiguration nicConfig = this.primaryNicConfiguration();
        nicConfig.withNetworkSecurityGroup(null);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withSinglePlacementGroup() {
        this.inner().withSinglePlacementGroup(true);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutSinglePlacementGroup() {
        this.inner().withSinglePlacementGroup(false);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withExistingApplicationGatewayBackendPool(String backendPoolId) {
        VirtualMachineScaleSetIpConfiguration nicIpConfig = primaryNicDefaultIpConfiguration();
        if (nicIpConfig.applicationGatewayBackendAddressPools() == null) {
            nicIpConfig.withApplicationGatewayBackendAddressPools(new ArrayList<>());
        }
        boolean found = false;
        for (SubResource backendPool : nicIpConfig.applicationGatewayBackendAddressPools()) {
            if (backendPool.id().equalsIgnoreCase(backendPoolId)) {
                found = true;
                break;
            }
        }
        if (!found) {
            nicIpConfig.applicationGatewayBackendAddressPools().add(new SubResource().withId(backendPoolId));
        }
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutApplicationGatewayBackendPool(String backendPoolId) {
        VirtualMachineScaleSetIpConfiguration nicIpConfig = primaryNicDefaultIpConfiguration();
        if (nicIpConfig.applicationGatewayBackendAddressPools() == null) {
            return this;
        } else {
            int foundIndex = -1;
            int index = -1;
            for (SubResource backendPool : nicIpConfig.applicationGatewayBackendAddressPools()) {
                index = index + 1;
                if (backendPool.id().equalsIgnoreCase(backendPoolId)) {
                    foundIndex = index;
                    break;
                }
            }
            if (foundIndex != -1) {
                nicIpConfig.applicationGatewayBackendAddressPools().remove(foundIndex);
            }
            return this;
        }
    }

    @Override
    public VirtualMachineScaleSetImpl withExistingApplicationSecurityGroup(
        ApplicationSecurityGroup applicationSecurityGroup) {
        return withExistingApplicationSecurityGroupId(applicationSecurityGroup.id());
    }

    @Override
    public VirtualMachineScaleSetImpl withExistingApplicationSecurityGroupId(String applicationSecurityGroupId) {
        VirtualMachineScaleSetIpConfiguration nicIpConfig = primaryNicDefaultIpConfiguration();
        if (nicIpConfig.applicationSecurityGroups() == null) {
            nicIpConfig.withApplicationSecurityGroups(new ArrayList<>());
        }
        boolean found = false;
        for (SubResource asg : nicIpConfig.applicationSecurityGroups()) {
            if (asg.id().equalsIgnoreCase(applicationSecurityGroupId)) {
                found = true;
                break;
            }
        }
        if (!found) {
            nicIpConfig.applicationSecurityGroups().add(new SubResource().withId(applicationSecurityGroupId));
        }
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutApplicationSecurityGroup(String applicationSecurityGroupId) {
        VirtualMachineScaleSetIpConfiguration nicIpConfig = primaryNicDefaultIpConfiguration();
        if (nicIpConfig.applicationSecurityGroups() == null) {
            return this;
        } else {
            int foundIndex = -1;
            int index = -1;
            for (SubResource asg : nicIpConfig.applicationSecurityGroups()) {
                index = index + 1;
                if (asg.id().equalsIgnoreCase(applicationSecurityGroupId)) {
                    foundIndex = index;
                    break;
                }
            }
            if (foundIndex != -1) {
                nicIpConfig.applicationSecurityGroups().remove(foundIndex);
            }
            return this;
        }
    }

    @Override
    public VirtualMachineScaleSetImpl withProximityPlacementGroup(String proximityPlacementGroupId) {
        this.inner().withProximityPlacementGroup(new SubResource().withId(proximityPlacementGroupId));
        this.newProximityPlacementGroupName = null;
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withNewProximityPlacementGroup(
        String proximityPlacementGroupName, ProximityPlacementGroupType type) {
        this.newProximityPlacementGroupName = proximityPlacementGroupName;
        this.newProximityPlacementGroupType = type;

        this.inner().withProximityPlacementGroup(null);

        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withDoNotRunExtensionsOnOverprovisionedVMs(
        Boolean doNotRunExtensionsOnOverprovisionedVMs) {
        this.inner().withDoNotRunExtensionsOnOverprovisionedVMs(doNotRunExtensionsOnOverprovisionedVMs);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withAdditionalCapabilities(AdditionalCapabilities additionalCapabilities) {
        this.inner().withAdditionalCapabilities(additionalCapabilities);
        return this;
    }

    private void createNewProximityPlacementGroup() {
        if (isInCreateMode()) {
            if (this.newProximityPlacementGroupName != null && !this.newProximityPlacementGroupName.isEmpty()) {
                ProximityPlacementGroupInner plgInner = new ProximityPlacementGroupInner();
                plgInner.withProximityPlacementGroupType(this.newProximityPlacementGroupType);
                plgInner.withLocation(this.inner().location());
                plgInner =
                    this
                        .manager()
                        .inner()
                        .getProximityPlacementGroups()
                        .createOrUpdate(this.resourceGroupName(), this.newProximityPlacementGroupName, plgInner);

                this.inner().withProximityPlacementGroup((new SubResource().withId(plgInner.id())));
            }
        }
    }

    /** Class to manage Data Disk collection. */
    private class ManagedDataDiskCollection {
        private final List<VirtualMachineScaleSetDataDisk> implicitDisksToAssociate = new ArrayList<>();
        private final List<Integer> diskLunsToRemove = new ArrayList<>();
        private final List<VirtualMachineScaleSetDataDisk> newDisksFromImage = new ArrayList<>();
        private final VirtualMachineScaleSetImpl vmss;
        private CachingTypes defaultCachingType;
        private StorageAccountTypes defaultStorageAccountType;

        ManagedDataDiskCollection(VirtualMachineScaleSetImpl vmss) {
            this.vmss = vmss;
        }

        void setDefaultCachingType(CachingTypes cachingType) {
            this.defaultCachingType = cachingType;
        }

        void setDefaultStorageAccountType(StorageAccountTypes defaultStorageAccountType) {
            this.defaultStorageAccountType = defaultStorageAccountType;
        }

        void setDataDisksDefaults() {
            VirtualMachineScaleSetStorageProfile storageProfile =
                this.vmss.inner().virtualMachineProfile().storageProfile();
            if (isPending()) {
                if (storageProfile.dataDisks() == null) {
                    storageProfile.withDataDisks(new ArrayList<>());
                }
                List<VirtualMachineScaleSetDataDisk> dataDisks = storageProfile.dataDisks();
                final List<Integer> usedLuns = new ArrayList<>();
                // Get all used luns
                //
                for (VirtualMachineScaleSetDataDisk dataDisk : dataDisks) {
                    if (dataDisk.lun() != -1) {
                        usedLuns.add(dataDisk.lun());
                    }
                }
                for (VirtualMachineScaleSetDataDisk dataDisk : this.implicitDisksToAssociate) {
                    if (dataDisk.lun() != -1) {
                        usedLuns.add(dataDisk.lun());
                    }
                }
                for (VirtualMachineScaleSetDataDisk dataDisk : this.newDisksFromImage) {
                    if (dataDisk.lun() != -1) {
                        usedLuns.add(dataDisk.lun());
                    }
                }
                // Func to get the next available lun
                //
                Callable<Integer> nextLun =
                    () -> {
                        Integer lun = 0;
                        while (usedLuns.contains(lun)) {
                            lun++;
                        }
                        usedLuns.add(lun);
                        return lun;
                    };
                try {
                    setImplicitDataDisks(nextLun);
                } catch (Exception ex) {
                    throw logger.logExceptionAsError(Exceptions.propagate(ex));
                }
                setImageBasedDataDisks();
                removeDataDisks();
            }
            if (storageProfile.dataDisks() != null && storageProfile.dataDisks().size() == 0) {
                if (vmss.isInCreateMode()) {
                    // If there is no data disks at all, then setting it to null rather than [] is necessary.
                    // This is for take advantage of CRP's implicit creation of the data disks if the image has
                    // more than one data disk image(s).
                    //
                    storageProfile.withDataDisks(null);
                }
            }
            this.clear();
        }

        private void clear() {
            implicitDisksToAssociate.clear();
            diskLunsToRemove.clear();
            newDisksFromImage.clear();
        }

        private boolean isPending() {
            return implicitDisksToAssociate.size() > 0 || diskLunsToRemove.size() > 0 || newDisksFromImage.size() > 0;
        }

        private void setImplicitDataDisks(Callable<Integer> nextLun) throws Exception {
            VirtualMachineScaleSetStorageProfile storageProfile =
                this.vmss.inner().virtualMachineProfile().storageProfile();
            List<VirtualMachineScaleSetDataDisk> dataDisks = storageProfile.dataDisks();
            for (VirtualMachineScaleSetDataDisk dataDisk : this.implicitDisksToAssociate) {
                dataDisk.withCreateOption(DiskCreateOptionTypes.EMPTY);
                if (dataDisk.lun() == -1) {
                    dataDisk.withLun(nextLun.call());
                }
                if (dataDisk.managedDisk() == null) {
                    dataDisk.withManagedDisk(new VirtualMachineScaleSetManagedDiskParameters());
                }
                if (dataDisk.caching() == null) {
                    dataDisk.withCaching(getDefaultCachingType());
                }
                if (dataDisk.managedDisk().storageAccountType() == null) {
                    dataDisk.managedDisk().withStorageAccountType(getDefaultStorageAccountType());
                }
                dataDisk.withName(null);
                dataDisks.add(dataDisk);
            }
        }

        private void setImageBasedDataDisks() {
            VirtualMachineScaleSetStorageProfile storageProfile =
                this.vmss.inner().virtualMachineProfile().storageProfile();
            List<VirtualMachineScaleSetDataDisk> dataDisks = storageProfile.dataDisks();
            for (VirtualMachineScaleSetDataDisk dataDisk : this.newDisksFromImage) {
                dataDisk.withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
                // Don't set default storage account type for the disk, either user has to specify it explicitly or let
                // CRP pick it from the image
                dataDisk.withName(null);
                dataDisks.add(dataDisk);
            }
        }

        private void removeDataDisks() {
            VirtualMachineScaleSetStorageProfile storageProfile =
                this.vmss.inner().virtualMachineProfile().storageProfile();
            List<VirtualMachineScaleSetDataDisk> dataDisks = storageProfile.dataDisks();
            for (Integer lun : this.diskLunsToRemove) {
                int indexToRemove = 0;
                for (VirtualMachineScaleSetDataDisk dataDisk : dataDisks) {
                    if (dataDisk.lun() == lun) {
                        dataDisks.remove(indexToRemove);
                        break;
                    }
                    indexToRemove++;
                }
            }
        }

        private CachingTypes getDefaultCachingType() {
            if (defaultCachingType == null) {
                return CachingTypes.READ_WRITE;
            }
            return defaultCachingType;
        }

        private StorageAccountTypes getDefaultStorageAccountType() {
            if (defaultStorageAccountType == null) {
                return StorageAccountTypes.STANDARD_LRS;
            }
            return defaultStorageAccountType;
        }
    }

    /** Class to manage VMSS boot diagnostics settings. */
    private class BootDiagnosticsHandler {
        private final VirtualMachineScaleSetImpl vmssImpl;
        private String creatableDiagnosticsStorageAccountKey;
        private String creatableStorageAccountKey;
        private StorageAccount existingStorageAccountToAssociate;

        BootDiagnosticsHandler(VirtualMachineScaleSetImpl vmssImpl) {
            this.vmssImpl = vmssImpl;
        }

        public boolean isBootDiagnosticsEnabled() {
            DiagnosticsProfile diagnosticsProfile = this.vmssInner().virtualMachineProfile().diagnosticsProfile();
            if (diagnosticsProfile != null
                && diagnosticsProfile.bootDiagnostics() != null
                && diagnosticsProfile.bootDiagnostics().enabled() != null) {
                return diagnosticsProfile.bootDiagnostics().enabled();
            }
            return false;
        }

        public String bootDiagnosticsStorageUri() {
            DiagnosticsProfile diagnosticsProfile = this.vmssInner().virtualMachineProfile().diagnosticsProfile();
            // Even though diagnostics can disabled azure still keep the storage uri
            if (diagnosticsProfile != null && diagnosticsProfile.bootDiagnostics() != null) {
                return diagnosticsProfile.bootDiagnostics().storageUri();
            }
            return null;
        }

        BootDiagnosticsHandler withBootDiagnostics() {
            // Diagnostics storage uri will be set later by this.handleDiagnosticsSettings(..)
            //
            this.enableDisable(true);
            return this;
        }

        BootDiagnosticsHandler withBootDiagnostics(Creatable<StorageAccount> creatable) {
            // Diagnostics storage uri will be set later by this.handleDiagnosticsSettings(..)
            //
            this.enableDisable(true);
            this.creatableDiagnosticsStorageAccountKey = this.vmssImpl.addDependency(creatable);
            return this;
        }

        BootDiagnosticsHandler withBootDiagnostics(String storageAccountBlobEndpointUri) {
            this.enableDisable(true);
            this
                .vmssInner()
                .virtualMachineProfile()
                .diagnosticsProfile()
                .bootDiagnostics()
                .withStorageUri(storageAccountBlobEndpointUri);
            return this;
        }

        BootDiagnosticsHandler withBootDiagnostics(StorageAccount storageAccount) {
            return this.withBootDiagnostics(storageAccount.endPoints().primary().blob());
        }

        BootDiagnosticsHandler withoutBootDiagnostics() {
            this.enableDisable(false);
            return this;
        }

        void prepare() {
            this.creatableStorageAccountKey = null;
            this.existingStorageAccountToAssociate = null;

            DiagnosticsProfile diagnosticsProfile = this.vmssInner().virtualMachineProfile().diagnosticsProfile();
            if (diagnosticsProfile == null
                || diagnosticsProfile.bootDiagnostics() == null
                || diagnosticsProfile.bootDiagnostics().storageUri() != null) {
                return;
            }
            boolean enableBD = Utils.toPrimitiveBoolean(diagnosticsProfile.bootDiagnostics().enabled());
            if (!enableBD) {
                return;
            }

            if (this.creatableDiagnosticsStorageAccountKey != null) {
                return;
            }
            if (!this.vmssImpl.creatableStorageAccountKeys.isEmpty()) {
                this.creatableStorageAccountKey = this.vmssImpl.creatableStorageAccountKeys.get(0);
                return;
            }
            if (!this.vmssImpl.existingStorageAccountsToAssociate.isEmpty()) {
                this.existingStorageAccountToAssociate = this.vmssImpl.existingStorageAccountsToAssociate.get(0);
                return;
            }

            String accountName = this.vmssImpl.namer.randomName("stg", 24).replace("-", "");
            Creatable<StorageAccount> storageAccountCreatable;
            if (this.vmssImpl.creatableGroup != null) {
                storageAccountCreatable =
                    this
                        .vmssImpl
                        .storageManager
                        .storageAccounts()
                        .define(accountName)
                        .withRegion(this.vmssImpl.regionName())
                        .withNewResourceGroup(this.vmssImpl.creatableGroup);
            } else {
                storageAccountCreatable =
                    this
                        .vmssImpl
                        .storageManager
                        .storageAccounts()
                        .define(accountName)
                        .withRegion(this.vmssImpl.regionName())
                        .withExistingResourceGroup(this.vmssImpl.resourceGroupName());
            }
            this.creatableDiagnosticsStorageAccountKey = this.vmssImpl.addDependency(storageAccountCreatable);
        }

        void handleDiagnosticsSettings() {
            DiagnosticsProfile diagnosticsProfile = this.vmssInner().virtualMachineProfile().diagnosticsProfile();
            if (diagnosticsProfile == null
                || diagnosticsProfile.bootDiagnostics() == null
                || diagnosticsProfile.bootDiagnostics().storageUri() != null) {
                return;
            }
            boolean enableBD = Utils.toPrimitiveBoolean(diagnosticsProfile.bootDiagnostics().enabled());
            if (!enableBD) {
                return;
            }
            StorageAccount storageAccount = null;
            if (creatableDiagnosticsStorageAccountKey != null) {
                storageAccount = this.vmssImpl.taskResult(creatableDiagnosticsStorageAccountKey);
            } else if (this.creatableStorageAccountKey != null) {
                storageAccount = this.vmssImpl.taskResult(this.creatableStorageAccountKey);
            } else if (this.existingStorageAccountToAssociate != null) {
                storageAccount = this.existingStorageAccountToAssociate;
            }
            if (storageAccount == null) {
                throw logger.logExceptionAsError(new IllegalStateException(
                    "Unable to retrieve expected storageAccount instance for BootDiagnostics"));
            }
            vmssInner()
                .virtualMachineProfile()
                .diagnosticsProfile()
                .bootDiagnostics()
                .withStorageUri(storageAccount.endPoints().primary().blob());
        }

        private VirtualMachineScaleSetInner vmssInner() {
            // Inner cannot be cached as parent VirtualMachineScaleSetImpl can refresh the inner in various cases
            //
            return this.vmssImpl.inner();
        }

        private void enableDisable(boolean enable) {
            if (this.vmssInner().virtualMachineProfile().diagnosticsProfile() == null) {
                this.vmssInner().virtualMachineProfile().withDiagnosticsProfile(new DiagnosticsProfile());
            }
            if (this.vmssInner().virtualMachineProfile().diagnosticsProfile().bootDiagnostics() == null) {
                this
                    .vmssInner()
                    .virtualMachineProfile()
                    .diagnosticsProfile()
                    .withBootDiagnostics(new BootDiagnostics());
            }
            if (enable) {
                this.vmssInner().virtualMachineProfile().diagnosticsProfile().bootDiagnostics().withEnabled(true);
            } else {
                this.vmssInner().virtualMachineProfile().diagnosticsProfile().bootDiagnostics().withEnabled(false);
                this.vmssInner().virtualMachineProfile().diagnosticsProfile().bootDiagnostics().withStorageUri(null);
            }
        }
    }
}
