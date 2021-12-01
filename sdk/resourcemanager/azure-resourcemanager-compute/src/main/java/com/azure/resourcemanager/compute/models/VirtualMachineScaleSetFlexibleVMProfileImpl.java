package com.azure.resourcemanager.compute.models;

import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.compute.implementation.VirtualMachineScaleSetImpl;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.network.models.ApplicationSecurityGroup;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.storage.models.StorageAccount;

import java.util.List;
import java.util.Map;

/**
 */
public class VirtualMachineScaleSetFlexibleVMProfileImpl implements
    VirtualMachineScaleSetFlexibleVMProfile,
    VirtualMachineScaleSetFlexibleVMProfile.UpdateAttachStages.DefinitionShared,
    VirtualMachineScaleSetFlexibleVMProfile.UpdateAttachStages.DefinitionManagedOrUnmanaged,
    VirtualMachineScaleSetFlexibleVMProfile.UpdateAttachStages.DefinitionManaged,
    VirtualMachineScaleSetFlexibleVMProfile.UpdateAttachStages.DefinitionUnmanaged,
    VirtualMachineScaleSetFlexibleVMProfile.UpdateAttachStages.WithSystemAssignedIdentityBasedAccessOrAttach,
    VirtualMachineScaleSetFlexibleVMProfile.UpdateAttachStages.WithUserAssignedManagedServiceIdentity
{

    private final VirtualMachineScaleSetImpl parent;

    public VirtualMachineScaleSetFlexibleVMProfileImpl(VirtualMachineScaleSetImpl parent) {
        this.parent = parent;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withExistingPrimaryNetworkSubnet(Network network, String subnetName) {
        parent.withExistingPrimaryNetworkSubnet(network, subnetName);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withExistingPrimaryInternetFacingLoadBalancer(LoadBalancer loadBalancer) {
        parent.withExistingPrimaryInternetFacingLoadBalancer(loadBalancer);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withoutPrimaryInternetFacingLoadBalancer() {
        parent.withoutPrimaryInternetFacingLoadBalancer();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withPrimaryInternetFacingLoadBalancerBackends(String... backendNames) {
        parent.withPrimaryInternetFacingLoadBalancerBackends(backendNames);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withPrimaryInternetFacingLoadBalancerInboundNatPools(String... natPoolNames) {
        parent.withPrimaryInternetFacingLoadBalancerInboundNatPools(natPoolNames);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withExistingPrimaryInternalLoadBalancer(LoadBalancer loadBalancer) {
        parent.withExistingPrimaryInternalLoadBalancer(loadBalancer);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withoutPrimaryInternalLoadBalancer() {
        parent.withoutPrimaryInternalLoadBalancer();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withPrimaryInternalLoadBalancerBackends(String... backendNames) {
        parent.withPrimaryInternalLoadBalancerBackends(backendNames);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withPrimaryInternalLoadBalancerInboundNatPools(String... natPoolNames) {
        parent.withPrimaryInternalLoadBalancerInboundNatPools(natPoolNames);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withPopularWindowsImage(KnownWindowsVirtualMachineImage knownImage) {
        parent.withPopularWindowsImage(knownImage);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withLatestWindowsImage(String publisher, String offer, String sku) {
        parent.withLatestWindowsImage(publisher, offer, sku);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withSpecificWindowsImageVersion(ImageReference imageReference) {
        parent.withSpecificWindowsImageVersion(imageReference);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withGeneralizedWindowsCustomImage(String customImageId) {
        parent.withGeneralizedWindowsCustomImage(customImageId);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withSpecializedWindowsCustomImage(String customImageId) {
        parent.withSpecializedWindowsCustomImage(customImageId);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withStoredWindowsImage(String imageUrl) {
        parent.withStoredWindowsImage(imageUrl);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withPopularLinuxImage(KnownLinuxVirtualMachineImage knownImage) {
        parent.withPopularLinuxImage(knownImage);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withLatestLinuxImage(String publisher, String offer, String sku) {
        parent.withLatestLinuxImage(publisher, offer, sku);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withSpecificLinuxImageVersion(ImageReference imageReference) {
        parent.withSpecificLinuxImageVersion(imageReference);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withGeneralizedLinuxCustomImage(String customImageId) {
        parent.withGeneralizedLinuxCustomImage(customImageId);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withSpecializedLinuxCustomImage(String customImageId) {
        parent.withSpecializedLinuxCustomImage(customImageId);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withStoredLinuxImage(String imageUrl) {
        parent.withStoredLinuxImage(imageUrl);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withRootUsername(String rootUserName) {
        parent.withRootUsername(rootUserName);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withRootPassword(String rootPassword) {
        parent.withRootPassword(rootPassword);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withSsh(String publicKey) {
        parent.withSsh(publicKey);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withAdminUsername(String adminUserName) {
        parent.withAdminUsername(adminUserName);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withAdminPassword(String adminPassword) {
        parent.withAdminPassword(adminPassword);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withUnmanagedDisks() {
        parent.withUnmanagedDisks();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withVMAgent() {
        parent.withVMAgent();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withoutVMAgent() {
        parent.withoutVMAgent();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withAutoUpdate() {
        parent.withAutoUpdate();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withoutAutoUpdate() {
        parent.withoutAutoUpdate();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withTimeZone(String timeZone) {
        parent.withTimeZone(timeZone);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withWinRM(WinRMListener listener) {
        parent.withWinRM(listener);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withNewDataDisk(int sizeInGB) {
        parent.withNewDataDisk(sizeInGB);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withNewDataDisk(int sizeInGB, int lun, CachingTypes cachingType) {
        parent.withNewDataDisk(sizeInGB, lun, cachingType);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withNewDataDisk(int sizeInGB, int lun, CachingTypes cachingType, StorageAccountTypes storageAccountType) {
        parent.withNewDataDisk(sizeInGB, lun, cachingType, storageAccountType);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withNewDataDiskFromImage(int imageLun) {
        parent.withNewDataDiskFromImage(imageLun);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withNewDataDiskFromImage(int imageLun, int newSizeInGB, CachingTypes cachingType) {
        parent.withNewDataDiskFromImage(imageLun, newSizeInGB, cachingType);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withNewDataDiskFromImage(int imageLun, int newSizeInGB, CachingTypes cachingType, StorageAccountTypes storageAccountType) {
        parent.withNewDataDiskFromImage(imageLun, newSizeInGB, cachingType, storageAccountType);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withOSDiskStorageAccountType(StorageAccountTypes accountType) {
        parent.withOSDiskStorageAccountType(accountType);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withDataDiskDefaultCachingType(CachingTypes cachingType) {
        parent.withDataDiskDefaultCachingType(cachingType);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withDataDiskDefaultStorageAccountType(StorageAccountTypes storageAccountType) {
        parent.withDataDiskDefaultStorageAccountType(storageAccountType);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withAvailabilityZone(AvailabilityZoneId zoneId) {
        parent.withAvailabilityZone(zoneId);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withComputerNamePrefix(String namePrefix) {
        parent.withComputerNamePrefix(namePrefix);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withCapacity(long capacity) {
        parent.withCapacity(capacity);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withOSDiskCaching(CachingTypes cachingType) {
        parent.withOSDiskCaching(cachingType);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withOSDiskName(String name) {
        parent.withOSDiskName(name);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withNewStorageAccount(String name) {
        parent.withNewStorageAccount(name);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withNewStorageAccount(Creatable<StorageAccount> creatable) {
        parent.withNewStorageAccount(creatable);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withExistingStorageAccount(StorageAccount storageAccount) {
        parent.withExistingStorageAccount(storageAccount);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withCustomData(String base64EncodedCustomData) {
        parent.withCustomData(base64EncodedCustomData);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withSecrets(List<VaultSecretGroup> secrets) {
        parent.withSecrets(secrets);
        return this;
    }

//    @Override
//    public VirtualMachineScaleSetExtension.DefinitionStages.Blank<DefinitionStages.WithAttach<ParentT>> defineNewExtension(String name) {
//        // TODO (xiaofeicao, 2021-11-30 13:27)
//        throw new UnsupportedOperationException("method [defineNewExtension] not implemented in class [com.azure.resourcemanager.compute.models.VirtualMachineScaleSetFlexibleVMProfileImpl]");
//    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withSystemAssignedManagedServiceIdentity() {
        parent.withSystemAssignedManagedServiceIdentity();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withNewUserAssignedManagedServiceIdentity(Creatable<Identity> creatableIdentity) {
        parent.withNewUserAssignedManagedServiceIdentity(creatableIdentity);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withExistingUserAssignedManagedServiceIdentity(Identity identity) {
        parent.withExistingUserAssignedManagedServiceIdentity(identity);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withBootDiagnosticsOnManagedStorageAccount() {
        parent.withBootDiagnosticsOnManagedStorageAccount();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withBootDiagnostics() {
        parent.withBootDiagnostics();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withBootDiagnostics(Creatable<StorageAccount> creatable) {
        parent.withBootDiagnostics(creatable);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withBootDiagnostics(StorageAccount storageAccount) {
        parent.withBootDiagnostics(storageAccount);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withBootDiagnostics(String storageAccountBlobEndpointUri) {
        parent.withBootDiagnostics(storageAccountBlobEndpointUri);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withMaxPrice(Double maxPrice) {
        parent.withMaxPrice(maxPrice);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withVirtualMachinePriority(VirtualMachinePriorityTypes priority) {
        parent.withVirtualMachinePriority(priority);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withLowPriorityVirtualMachine() {
        parent.withLowPriorityVirtualMachine();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withLowPriorityVirtualMachine(VirtualMachineEvictionPolicyTypes policy) {
        parent.withLowPriorityVirtualMachine(policy);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withSpotPriorityVirtualMachine() {
        parent.withSpotPriorityVirtualMachine();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withSpotPriorityVirtualMachine(VirtualMachineEvictionPolicyTypes policy) {
        parent.withSpotPriorityVirtualMachine(policy);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withVirtualMachinePublicIp() {
        parent.withVirtualMachinePublicIp();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withVirtualMachinePublicIp(String leafDomainLabel) {
        parent.withVirtualMachinePublicIp(leafDomainLabel);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withVirtualMachinePublicIp(VirtualMachineScaleSetPublicIpAddressConfiguration ipConfig) {
        parent.withVirtualMachinePublicIp(ipConfig);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withAcceleratedNetworking() {
        parent.withAcceleratedNetworking();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withoutAcceleratedNetworking() {
        parent.withoutAcceleratedNetworking();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withIpForwarding() {
        parent.withIpForwarding();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withoutIpForwarding() {
        parent.withoutIpForwarding();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withExistingNetworkSecurityGroup(NetworkSecurityGroup networkSecurityGroup) {
        parent.withExistingNetworkSecurityGroup(networkSecurityGroup);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withExistingNetworkSecurityGroupId(String networkSecurityGroupId) {
        parent.withExistingNetworkSecurityGroupId(networkSecurityGroupId);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withSinglePlacementGroup() {
        parent.withSinglePlacementGroup();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withoutSinglePlacementGroup() {
        parent.withoutSinglePlacementGroup();
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withExistingApplicationGatewayBackendPool(String backendPoolId) {
        parent.withExistingApplicationGatewayBackendPool(backendPoolId);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withExistingApplicationSecurityGroup(ApplicationSecurityGroup applicationSecurityGroup) {
        parent.withExistingApplicationSecurityGroup(applicationSecurityGroup);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withExistingApplicationSecurityGroupId(String applicationSecurityGroupId) {
        parent.withExistingApplicationSecurityGroupId(applicationSecurityGroupId);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withPlan(PurchasePlan plan) {
        parent.withPlan(plan);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withTags(Map<String, String> tags) {
        parent.withTags(tags);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withTag(String key, String value) {
        parent.withTag(key, value);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withSystemAssignedIdentityBasedAccessTo(String resourceId, BuiltInRole role) {
        parent.withSystemAssignedIdentityBasedAccessTo(resourceId, role);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(BuiltInRole role) {
        parent.withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(role);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withSystemAssignedIdentityBasedAccessTo(String resourceId, String roleDefinitionId) {
        parent.withSystemAssignedIdentityBasedAccessTo(resourceId, roleDefinitionId);
        return this;
    }

    @Override
    public VirtualMachineScaleSetFlexibleVMProfileImpl withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(String roleDefinitionId) {
        parent.withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(roleDefinitionId);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl attach() {
        return parent;
    }
}
