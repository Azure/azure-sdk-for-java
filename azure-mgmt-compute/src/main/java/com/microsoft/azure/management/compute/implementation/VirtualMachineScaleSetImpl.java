package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.DiskCreateOptionTypes;
import com.microsoft.azure.management.compute.ImageReference;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.LinuxConfiguration;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.SshConfiguration;
import com.microsoft.azure.management.compute.SshPublicKey;
import com.microsoft.azure.management.compute.UpgradePolicy;
import com.microsoft.azure.management.compute.VirtualHardDisk;
import com.microsoft.azure.management.compute.VirtualMachineExtension;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetNetworkProfile;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetOSDisk;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetOSProfile;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetSku;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetSkuTypes;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetStorageProfile;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetVMProfile;
import com.microsoft.azure.management.compute.WinRMConfiguration;
import com.microsoft.azure.management.compute.WinRMListener;
import com.microsoft.azure.management.compute.WindowsConfiguration;
import com.microsoft.azure.management.network.Backend;
import com.microsoft.azure.management.network.InboundNatPool;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import rx.Observable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Implementation of {@link VirtualMachineScaleSet}.
 */
public class VirtualMachineScaleSetImpl
        extends GroupableResourceImpl<
        VirtualMachineScaleSet,
        VirtualMachineScaleSetInner,
        VirtualMachineScaleSetImpl,
        ComputeManager>
        implements
        VirtualMachineScaleSet,
        VirtualMachineScaleSet.Definition {

    // Clients
    private final VirtualMachineScaleSetsInner client;
    private final StorageManager storageManager;
    private final NetworkManager networkManager;
    // the name of the virtual machine scale set
    private final String scaleSetName;
    // used to generate unique name for any dependency resources
    private final ResourceNamer namer;
    private boolean isMarketplaceLinuxImage = false;

    VirtualMachineScaleSetImpl(String name,
                        VirtualMachineScaleSetInner innerModel,
                        VirtualMachineScaleSetsInner client,
                        final ComputeManager computeManager,
                        final StorageManager storageManager,
                        final NetworkManager networkManager) {
        super(name, innerModel, computeManager);
        this.client = client;
        this.storageManager = storageManager;
        this.networkManager = networkManager;
        this.scaleSetName = name;
        this.namer = new ResourceNamer(this.scaleSetName);
    }

    static VirtualMachineScaleSetImpl create(String name) {
        VirtualMachineScaleSetInner inner = new VirtualMachineScaleSetInner();

        inner.withVirtualMachineProfile(new VirtualMachineScaleSetVMProfile());
        inner.virtualMachineProfile()
                .withStorageProfile(new VirtualMachineScaleSetStorageProfile()
                    .withOsDisk(new VirtualMachineScaleSetOSDisk().withVhdContainers(new ArrayList<String>())));
        inner.virtualMachineProfile()
                .withOsProfile(new VirtualMachineScaleSetOSProfile());
        inner.virtualMachineProfile()
                .withNetworkProfile(new VirtualMachineScaleSetNetworkProfile());
        return new VirtualMachineScaleSetImpl(name,
                inner,
                null,
                null,
                null,
                null);
    }

    @Override
    public PagedList<VirtualMachineScaleSetSku> availableSkus() throws CloudException, IOException {
        return null;
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
    public UpgradePolicy upgradePolicy() {
        return this.inner().upgradePolicy();
    }

    @Override
    public boolean overProvisionEnabled() {
        return this.inner().overProvision();
    }

    @Override
    public VirtualMachineScaleSetSkuTypes sku() {
        return new VirtualMachineScaleSetSkuTypes(this.inner().sku());
    }

    @Override
    public LoadBalancer primaryInternetFacingLoadBalancer() {
        return null;
    }

    @Override
    public Map<String, Backend> primaryInternetFacingLoadBalancerBackEnds() {
        return null;
    }

    @Override
    public Map<String, InboundNatPool> primaryInternetFacingLoadBalancerInboundNatPools() {
        return null;
    }

    @Override
    public LoadBalancer primaryInternalLoadBalancer() {
        return null;
    }

    @Override
    public Map<String, Backend> primaryInternalLoadBalancerBackEnds() {
        return null;
    }

    @Override
    public Map<String, InboundNatPool> primaryInternalLoadBalancerInboundNatPools() {
        return null;
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
    public Map<String, VirtualMachineExtension> extensions() {
        return null;
    }

    @Override
    public VirtualMachineScaleSetImpl withAdminUserName(String adminUserName) {
        this.inner().virtualMachineProfile()
                .osProfile()
                .withAdminPassword(adminUserName);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPrimaryInternalLoadBalancerBackend(String... backendNames) {
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withSsh(String publicKeyData) {
        VirtualMachineScaleSetOSProfile osProfile = this.inner()
                .virtualMachineProfile()
                .osProfile();
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
    public VirtualMachineScaleSetImpl withCapacity(long capacity) {
        this.inner().sku().withCapacity(capacity);
        return this;
    }

    @Override
    public VirtualMachineExtension.DefinitionStages.Blank<VirtualMachineScaleSet.DefinitionStages.WithCreate> defineNewExtension(String name) {
        return null;
    }

    @Override
    public VirtualMachineScaleSetImpl withNewPrimaryNetwork(Creatable<Network> creatable) {
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withNewPrimaryNetwork(String addressSpace) {
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withExistingPrimaryNetwork(Network network) {
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withSubnet(String name) {
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPopularWindowsImage(KnownWindowsVirtualMachineImage knownImage) {
        return withSpecificWindowsImageVersion(knownImage.imageReference());
    }

    @Override
    public VirtualMachineScaleSetImpl withLatestWindowsImage(String publisher, String offer, String sku) {
        ImageReference imageReference = new ImageReference()
                .withPublisher(publisher)
                .withOffer(offer)
                .withSku(sku)
                .withVersion("latest");
        return withSpecificWindowsImageVersion(imageReference);
    }

    @Override
    public VirtualMachineScaleSetImpl withSpecificWindowsImageVersion(ImageReference imageReference) {
        this.inner()
                .virtualMachineProfile()
                .storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner()
                .virtualMachineProfile()
                .storageProfile().withImageReference(imageReference);
        this.inner()
                .virtualMachineProfile()
                .osProfile().withWindowsConfiguration(new WindowsConfiguration());
        // sets defaults for "Stored(Custom)Image" or "VM(Platform)Image"
        this.inner()
                .virtualMachineProfile()
                .osProfile().windowsConfiguration().withProvisionVMAgent(true);
        this.inner()
                .virtualMachineProfile()
                .osProfile().windowsConfiguration().withEnableAutomaticUpdates(true);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withStoredWindowsImage(String imageUrl) {
        VirtualHardDisk userImageVhd = new VirtualHardDisk();
        userImageVhd.withUri(imageUrl);
        this.inner()
                .virtualMachineProfile()
                .storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner()
                .virtualMachineProfile()
                .storageProfile().osDisk().withImage(userImageVhd);
        // For platform image osType will be null, azure will pick it from the image metadata.
        this.inner()
                .virtualMachineProfile()
                .storageProfile().osDisk().withOsType(OperatingSystemTypes.WINDOWS);
        this.inner()
                .virtualMachineProfile()
                .osProfile().withWindowsConfiguration(new WindowsConfiguration());
        // sets defaults for "Stored(Custom)Image" or "VM(Platform)Image"
        this.inner()
                .virtualMachineProfile()
                .osProfile().windowsConfiguration().withProvisionVMAgent(true);
        this.inner()
                .virtualMachineProfile()
                .osProfile().windowsConfiguration().withEnableAutomaticUpdates(true);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPopularLinuxImage(KnownLinuxVirtualMachineImage knownImage) {
        return withSpecificLinuxImageVersion(knownImage.imageReference());
    }

    @Override
    public VirtualMachineScaleSetImpl withLatestLinuxImage(String publisher, String offer, String sku) {
        ImageReference imageReference = new ImageReference()
            .withPublisher(publisher)
            .withOffer(offer)
            .withSku(sku)
            .withVersion("latest");
        return withSpecificLinuxImageVersion(imageReference);
    }

    @Override
    public VirtualMachineScaleSetImpl withSpecificLinuxImageVersion(ImageReference imageReference) {
        this.inner()
                .virtualMachineProfile()
                .storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner()
                .virtualMachineProfile()
                .storageProfile().withImageReference(imageReference);
        this.inner()
                .virtualMachineProfile()
                .osProfile().withLinuxConfiguration(new LinuxConfiguration());
        this.isMarketplaceLinuxImage = true;
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withStoredLinuxImage(String imageUrl) {
        VirtualHardDisk userImageVhd = new VirtualHardDisk();
        userImageVhd.withUri(imageUrl);
        this.inner()
                .virtualMachineProfile()
                .storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner()
                .virtualMachineProfile()
                .storageProfile().osDisk().withImage(userImageVhd);
        // For platform image osType will be null, azure will pick it from the image metadata.
        this.inner()
                .virtualMachineProfile()
                .storageProfile().osDisk().withOsType(OperatingSystemTypes.LINUX);
        this.inner()
                .virtualMachineProfile()
                .osProfile().withLinuxConfiguration(new LinuxConfiguration());
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl disableVmAgent() {
        this.inner()
                .virtualMachineProfile()
                .osProfile().windowsConfiguration().withProvisionVMAgent(false);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl disableAutoUpdate() {
        this.inner()
                .virtualMachineProfile()
                .osProfile().windowsConfiguration().withEnableAutomaticUpdates(false);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withTimeZone(String timeZone) {
        this.inner()
                .virtualMachineProfile()
                .osProfile().windowsConfiguration().withTimeZone(timeZone);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withWinRm(WinRMListener listener) {
        if (this.inner().virtualMachineProfile().osProfile().windowsConfiguration().winRM() == null) {
            WinRMConfiguration winRMConfiguration = new WinRMConfiguration();
            this.inner()
                    .virtualMachineProfile()
                    .osProfile().windowsConfiguration().withWinRM(winRMConfiguration);
        }
        this.inner()
                .virtualMachineProfile()
                .osProfile()
                .windowsConfiguration()
                .winRM()
                .listeners()
                .add(listener);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withOsDiskCaching(CachingTypes cachingType) {
        this.inner()
                .virtualMachineProfile()
                .storageProfile().osDisk().withCaching(cachingType);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withOverProvision(boolean enabled) {
        this.inner().withOverProvision(enabled);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withOverProvisionEnabled() {
        return this.withOverProvision(true);
    }

    @Override
    public VirtualMachineScaleSetImpl withOverProvisionDisabled() {
        return this.withOverProvision(false);
    }

    @Override
    public VirtualMachineScaleSetImpl withPassword(String password) {
        this.inner()
                .virtualMachineProfile()
                .osProfile().withAdminPassword(password);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPrimaryInternalLoadBalancer(LoadBalancer loadBalancer) {
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutPrimaryInternalLoadBalancer() {
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPrimaryInternetFacingLoadBalancer(LoadBalancer loadBalancer) {
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withoutPrimaryInternetFacingLoadBalancer() {
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPrimaryInternetFacingLoadBalancerBackend(String... backendNames) {
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPrimaryInternetFacingLoadBalancerInboundNatPool(String... natPoolNames) {
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPrimaryInternalLoadBalancerInboundNatPool(String... natPoolNames) {
        return null;
    }

    @Override
    public VirtualMachineScaleSetImpl withRootUserName(String rootUserName) {
        return this.withAdminUserName(rootUserName);
    }

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
    public VirtualMachineScaleSetImpl withNewStorageAccount(String name) {
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withNewStorageAccount(Creatable<StorageAccount> creatable) {
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withExistingStorageAccount(StorageAccount storageAccount) {
        return this;
    }

    @Override
    public Observable<VirtualMachineScaleSet> applyAsync() {
        return null;
    }

    @Override
    public Observable<VirtualMachineScaleSet> createResourceAsync() {
        return null;
    }

    @Override
    public VirtualMachineScaleSetImpl refresh() throws Exception {
        return this;
    }
}
