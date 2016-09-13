package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.compute.ApiEntityReference;
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
import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetExtension;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetExtensionProfile;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetNetworkProfile;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetOSProfile;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetSku;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetSkuTypes;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetStorageProfile;
import com.microsoft.azure.management.compute.WinRMConfiguration;
import com.microsoft.azure.management.compute.WinRMListener;
import com.microsoft.azure.management.compute.WindowsConfiguration;
import com.microsoft.azure.management.network.Backend;
import com.microsoft.azure.management.network.InboundNatPool;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import rx.Observable;
import rx.functions.Func1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link VirtualMachineScaleSet}.
 */
public class VirtualMachineScaleSetImpl
        extends GroupableParentResourceImpl<
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
    // used to generate unique name for any dependency resources
    private final ResourceNamer namer;
    // the name of the virtual machine scale set
    private final String scaleSetName;
    // Name of the container to store virtual machines disks
    private String vhdContainerName;
    private boolean isMarketplaceLinuxImage = false;
    // reference to the primary internet facing load balancer
    private LoadBalancer primaryInternetFacingLoadBalancer;
    // reference to the primary internal load balancer
    private LoadBalancer primaryInternalLoadBalancer;
    // unique key of a creatable network that needs to be used in virtual machine's primary network interface
    private String creatablePrimaryNetworkKey;
    // reference to an existing network that needs to be used in virtual machine's primary network interface
    private Network existingPrimaryNetworkToAssociate;
    // name of an existing subnet in the network to use
    private String existingSubnetNameToAssociate;
    // unique key of a creatable storage accounts to be used for virtual machines child resources that
    // requires storage [OS disk]
    private List<String> creatableStorageAccountKeys = new ArrayList<>();
    // reference to an existing storage account to be used for virtual machines child resources that
    // requires storage [OS disk]
    private List<StorageAccount> existingStorageAccountsToAssociate = new ArrayList<>();
    private Map<String, VirtualMachineScaleSetExtension> extensions;
    private PagedListConverter<VirtualMachineScaleSetSkuInner, VirtualMachineScaleSetSku> skuConverter;
    // Load balancer specific variables used during update
    //
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
        this.skuConverter = new PagedListConverter<VirtualMachineScaleSetSkuInner, VirtualMachineScaleSetSku>() {
            @Override
            public VirtualMachineScaleSetSku typeConvert(VirtualMachineScaleSetSkuInner inner) {
                return new VirtualMachineScaleSetSkuImpl(inner);
            }
        };
    }

    @Override
    protected void initializeChildrenFromInner() {
        this.extensions = new HashMap<>();
        if (this.inner().virtualMachineProfile().extensionProfile() != null) {
            if (this.inner().virtualMachineProfile().extensionProfile().extensions() != null) {
                for (VirtualMachineScaleSetExtensionInner inner : this.inner().virtualMachineProfile().extensionProfile().extensions()) {
                    this.extensions.put(inner.name(), new VirtualMachineScaleSetExtensionImpl(inner, this));
                }
            }
        }
    }

    @Override
    public PagedList<VirtualMachineScaleSetSku> availableSkus() throws CloudException, IOException {
        return this.skuConverter.convert(this.client.listSkus(this.resourceGroupName(), this.name()));
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
    public LoadBalancer primaryInternetFacingLoadBalancer() throws IOException {
        loadCurrentPrimaryLoadBalancersIfAvailable();
        return this.primaryInternetFacingLoadBalancer;
    }

    @Override
    public Map<String, Backend> primaryInternetFacingLoadBalancerBackEnds() throws IOException {
        if (this.primaryInternetFacingLoadBalancer() != null) {
            return getBackendsAssociatedWithIpConfiguration(this.primaryInternetFacingLoadBalancer,
                    primaryNicDefaultIPConfiguration());
        }
        return new HashMap<>();
    }

    @Override
    public Map<String, InboundNatPool> primaryInternetFacingLoadBalancerInboundNatPools() throws IOException {
        if (this.primaryInternetFacingLoadBalancer() != null) {
            return getInboundNatPoolsAssociatedWithIpConfiguration(this.primaryInternetFacingLoadBalancer,
                    primaryNicDefaultIPConfiguration());
        }
        return new HashMap<>();
    }

    @Override
    public LoadBalancer primaryInternalLoadBalancer() throws IOException {
        loadCurrentPrimaryLoadBalancersIfAvailable();
        return this.primaryInternalLoadBalancer;
    }

    @Override
    public Map<String, Backend> primaryInternalLoadBalancerBackEnds() throws IOException {
        if (this.primaryInternalLoadBalancer() != null) {
            return getBackendsAssociatedWithIpConfiguration(this.primaryInternalLoadBalancer,
                    primaryNicDefaultIPConfiguration());
        }
        return new HashMap<>();
    }

    @Override
    public Map<String, InboundNatPool> primaryInternalLoadBalancerInboundNatPools() throws IOException {
        if (this.primaryInternalLoadBalancer() != null) {
            return getInboundNatPoolsAssociatedWithIpConfiguration(this.primaryInternalLoadBalancer,
                    primaryNicDefaultIPConfiguration());
        }
        return new HashMap<>();
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

    // Fluent setters

    @Override
    public VirtualMachineScaleSetImpl withSku(VirtualMachineScaleSetSkuTypes skuType) {
        this.inner()
                .withSku(skuType.sku());
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withSku(VirtualMachineScaleSetSku sku) {
        return this.withSku(sku.skuType());
    }

    @Override
    public VirtualMachineScaleSetImpl withNewPrimaryNetwork(Creatable<Network> creatable) {
        this.addCreatableDependency(creatable);
        this.creatablePrimaryNetworkKey = creatable.key();
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withNewPrimaryNetwork(String addressSpace) {
        Network.DefinitionStages.WithGroup definitionWithGroup = this.networkManager
                .networks()
                .define(this.namer.randomName("vnet", 20))
                .withRegion(this.region());
        Network.DefinitionStages.WithCreate definitionAfterGroup;
        if (this.creatableGroup != null) {
            definitionAfterGroup = definitionWithGroup.withNewResourceGroup(this.creatableGroup);
        } else {
            definitionAfterGroup = definitionWithGroup.withExistingResourceGroup(this.resourceGroupName());
        }
        return withNewPrimaryNetwork(definitionAfterGroup.withAddressSpace(addressSpace));
    }

    @Override
    public VirtualMachineScaleSetImpl withExistingPrimaryNetwork(Network network) {
        this.existingPrimaryNetworkToAssociate = network;
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withSubnet(String name) {
        this.existingSubnetNameToAssociate = name;
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPrimaryInternetFacingLoadBalancer(LoadBalancer loadBalancer) {
        if (loadBalancer.publicIpAddressIds().isEmpty()) {
            throw new IllegalArgumentException("Parameter loadBalancer must be an internet facing load balancer");
        }
        if (isInCreateMode()) {
            this.primaryInternetFacingLoadBalancer = loadBalancer;
            associateLoadBalancerToIpConfiguration(this.primaryInternetFacingLoadBalancer,
                    this.primaryNicDefaultIPConfiguration());
        } else {
            this.primaryInternetFacingLoadBalancerToAttachOnUpdate = loadBalancer;
        }
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPrimaryInternetFacingLoadBalancerBackends(String... backendNames) {
        if (this.isInCreateMode()) {
            VirtualMachineScaleSetIPConfigurationInner defaultPrimaryIpConfig = this.primaryNicDefaultIPConfiguration();
            removeAllBackendAssociationFromIpConfiguration(this.primaryInternetFacingLoadBalancer, defaultPrimaryIpConfig);
            associateBackEndsToIpConfiguration(this.primaryInternetFacingLoadBalancer.id(),
                    defaultPrimaryIpConfig,
                    backendNames);
        } else {
            addToList(this.primaryInternetFacingLBBackendsToAddOnUpdate, backendNames);
        }
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPrimaryInternetFacingLoadBalancerInboundNatPools(String... natPoolNames) {
        if (this.isInCreateMode()) {
            VirtualMachineScaleSetIPConfigurationInner defaultPrimaryIpConfig = this.primaryNicDefaultIPConfiguration();
            removeAllInboundNatPoolAssociationFromIpConfiguration(this.primaryInternetFacingLoadBalancer,
                    defaultPrimaryIpConfig);
            associateInboundNATPoolsToIpConfiguration(this.primaryInternetFacingLoadBalancer.id(),
                    defaultPrimaryIpConfig,
                    natPoolNames);
        } else {
            addToList(this.primaryInternetFacingLBInboundNatPoolsToAddOnUpdate, natPoolNames);
        }
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPrimaryInternalLoadBalancer(LoadBalancer loadBalancer) {
        if (!loadBalancer.publicIpAddressIds().isEmpty()) {
            throw new IllegalArgumentException("Parameter loadBalancer must be an internal load balancer");
        }
        if (isInCreateMode()) {
            this.primaryInternalLoadBalancer = loadBalancer;
            associateLoadBalancerToIpConfiguration(this.primaryInternalLoadBalancer,
                    this.primaryNicDefaultIPConfiguration());
        } else {
            this.primaryInternalLoadBalancerToAttachOnUpdate = loadBalancer;
        }
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPrimaryInternalLoadBalancerBackends(String... backendNames) {
        if (this.isInCreateMode()) {
            VirtualMachineScaleSetIPConfigurationInner defaultPrimaryIpConfig = primaryNicDefaultIPConfiguration();
            this.removeAllBackendAssociationFromIpConfiguration(this.primaryInternalLoadBalancer,
                    defaultPrimaryIpConfig);
            this.associateBackEndsToIpConfiguration(this.primaryInternalLoadBalancer.id(),
                    defaultPrimaryIpConfig,
                    backendNames);
        } else {
            addToList(this.primaryInternalLBBackendsToAddOnUpdate, backendNames);
        }
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withPrimaryInternalLoadBalancerInboundNatPools(String... natPoolNames) {
        if (this.isInCreateMode()) {
            VirtualMachineScaleSetIPConfigurationInner defaultPrimaryIpConfig = this.primaryNicDefaultIPConfiguration();
            this.removeAllInboundNatPoolAssociationFromIpConfiguration(this.primaryInternalLoadBalancer,
                    defaultPrimaryIpConfig);
            this.associateInboundNATPoolsToIpConfiguration(this.primaryInternalLoadBalancer.id(),
                    defaultPrimaryIpConfig,
                    natPoolNames);
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
    public VirtualMachineScaleSetImpl withAdminUserName(String adminUserName) {
        this.inner()
                .virtualMachineProfile()
                .osProfile()
                .withAdminPassword(adminUserName);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withRootUserName(String rootUserName) {
        return this.withAdminUserName(rootUserName);
    }

    @Override
    public VirtualMachineScaleSetImpl withPassword(String password) {
        this.inner()
                .virtualMachineProfile()
                .osProfile().withAdminPassword(password);
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
    public VirtualMachineScaleSetImpl withOsDiskName(String name) {
        this.inner()
                .virtualMachineProfile()
                .storageProfile().osDisk().withName(name);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withOverProvision(boolean enabled) {
        this.inner()
                .withOverProvision(enabled);
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
    public VirtualMachineScaleSetImpl withCapacity(long capacity) {
        this.inner()
                .sku().withCapacity(capacity);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withNewStorageAccount(String name) {
        StorageAccount.DefinitionStages.WithGroup definitionWithGroup = this.storageManager
                .storageAccounts()
                .define(name)
                .withRegion(this.regionName());
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
        this.creatableStorageAccountKeys.add(creatable.key());
        this.addCreatableDependency(creatable);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl withExistingStorageAccount(StorageAccount storageAccount) {
        this.existingStorageAccountsToAssociate.add(storageAccount);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl defineNewExtension(String name) {
        return new VirtualMachineScaleSetExtensionImpl(new VirtualMachineScaleSetExtensionInner(), this);
    }

    protected VirtualMachineScaleSetImpl withExtension(VirtualMachineScaleSetExtensionImpl extension) {
        this.extensions.put(extension.name(), extension);
        return this;
    }

    // Create Update specific methods
    //
    @Override
    protected void beforeCreating() {
        if (this.extensions.size() > 0) {
            this.inner()
                    .virtualMachineProfile()
                    .withExtensionProfile(new VirtualMachineScaleSetExtensionProfile())
                    .extensionProfile()
                    .withExtensions(innersFromWrappers(this.extensions.values()));
        }
    }

    @Override
    protected Observable<VirtualMachineScaleSetInner> createInner() {
        this.setOSDiskAndOSProfileDefaults();
        this.setPrimaryIpConfigurationSubnet();
        this.setPrimaryIpConfigurationBackendsAndInboundNatPools();
        return this.handleOSDiskContainersAsync()
                .flatMap(new Func1<Void, Observable<VirtualMachineScaleSetInner>>() {
                    @Override
                    public Observable<VirtualMachineScaleSetInner> call(Void aVoid) {
                        return client.createOrUpdateAsync(resourceGroupName(), scaleSetName, inner());
                    }
                });
    }

    @Override
    protected void afterCreating() {
        this.clearCachedProperties();
        this.initializeChildrenFromInner();
    }

    @Override
    public Observable<VirtualMachineScaleSet> applyAsync() {
        return this.createAsync();
    }

    @Override
    public VirtualMachineScaleSetImpl refresh() throws Exception {
        VirtualMachineScaleSetInner inner = this.client.get(this.resourceGroupName(), this.name());
        this.setInner(inner);
        this.clearCachedProperties();
        this.initializeChildrenFromInner();
        return this;
    }

    // Helpers
    //

    private boolean isInUpdateMode() {
        return !this.isInCreateMode();
    }

    private void setOSDiskAndOSProfileDefaults() {
        if (isInUpdateMode()) {
            return;
        }

        VirtualMachineScaleSetOSProfile osProfile = this.inner()
                .virtualMachineProfile()
                .osProfile();
        // linux image: Custom or marketplace linux image
        if (this.osType() == OperatingSystemTypes.LINUX || this.isMarketplaceLinuxImage) {
            if (osProfile.linuxConfiguration() == null) {
                osProfile.withLinuxConfiguration(new LinuxConfiguration());
            }
            osProfile
                .linuxConfiguration()
                .withDisablePasswordAuthentication(osProfile.adminPassword() == null);
        }

        if (this.osDiskCachingType() == null) {
            withOsDiskCaching(CachingTypes.READ_WRITE);
        }

        if (this.osDiskName() == null) {
            withOsDiskName(this.scaleSetName + "-os-disk");
        }

        if (osProfile.computerNamePrefix() == null) {
            // VM name cannot contain only numeric values and cannot exceed 15 chars
            if (this.scaleSetName.matches("[0-9]+")) {
                osProfile.withComputerNamePrefix(ResourceNamer.randomResourceName("vmss-vm", 12));
            } else if (this.scaleSetName.length() <= 12) {
                osProfile.withComputerNamePrefix(this.scaleSetName + "-vm");
            } else {
                osProfile.withComputerNamePrefix(ResourceNamer.randomResourceName("vmss-vm", 12));
            }
        }
    }

    private boolean isCustomImage(VirtualMachineScaleSetStorageProfile storageProfile) {
        return storageProfile.osDisk().image() == null
                || storageProfile.osDisk().image().uri() == null
                || storageProfile.osDisk().image().uri() == "";
    }

    private Observable<Void> handleOSDiskContainersAsync() {
        final VirtualMachineScaleSetStorageProfile storageProfile = inner()
                .virtualMachineProfile()
                .storageProfile();
        if (isCustomImage(storageProfile)) {
            // There is a restriction currently that virtual machine's disk cannot be stored in multiple storage accounts
            // if scale set is based on custom image. Remove this check once azure start supporting it.
            storageProfile.osDisk()
                    .vhdContainers()
                    .clear();
            return Observable.just(null);
        }

        if (this.isInCreateMode()
                && this.creatableStorageAccountKeys.isEmpty()
                && this.existingStorageAccountsToAssociate.isEmpty()) {
            return this.storageManager.storageAccounts()
                    .define(this.namer.randomName("stg", 24))
                    .withRegion(this.regionName())
                    .withExistingResourceGroup(this.resourceGroupName())
                    .createAsync()
                    .map(new Func1<StorageAccount, Void>() {
                        @Override
                        public Void call(StorageAccount storageAccount) {
                            String containerName = vhdContainerName;
                            if (containerName == null) {
                                containerName = "vhds";
                            }
                            storageProfile.osDisk()
                                    .vhdContainers()
                                    .add(storageAccount.endPoints().primary().blob() + "/" + containerName);
                            vhdContainerName = null;
                            return null;
                        }
                    });
        } else {
            String containerName = this.vhdContainerName;
            if (containerName == null) {
                for (String containerUrl : storageProfile.osDisk().vhdContainers()) {
                    containerName = containerUrl.substring(containerUrl.lastIndexOf("/") + 1);
                    break;
                }
            }

            if (containerName == null) {
                containerName = "vhds";
            }

            for (String storageAccountKey : this.creatableStorageAccountKeys) {
                StorageAccount storageAccount = (StorageAccount) createdResource(storageAccountKey);
                storageProfile.osDisk()
                        .vhdContainers()
                        .add(storageAccount.endPoints().primary().blob() + "/" + containerName);
            }

            for (StorageAccount storageAccount : this.existingStorageAccountsToAssociate) {
                storageProfile.osDisk()
                        .vhdContainers()
                        .add(storageAccount.endPoints().primary().blob() + "/" + containerName);
            }

            this.vhdContainerName = null;
            this.creatableStorageAccountKeys.clear();
            this.existingStorageAccountsToAssociate.clear();
            return Observable.just(null);
        }
    }

    private void setPrimaryIpConfigurationSubnet() {
        if (isInUpdateMode()) {
            return;
        }

        VirtualMachineScaleSetIPConfigurationInner ipConfig = this.primaryNicDefaultIPConfiguration();
        if (this.creatablePrimaryNetworkKey != null) {
            Network primaryNetwork = (Network) this.createdResource(this.creatablePrimaryNetworkKey);
            for (Subnet subnet : primaryNetwork.subnets().values()) {
                ipConfig.withSubnet(new ApiEntityReference().withId(subnet.inner().id()));
            }
        } else if (this.existingPrimaryNetworkToAssociate != null) {
            ipConfig.withSubnet(new ApiEntityReference().withId(this.existingPrimaryNetworkToAssociate.id()
                    + "/"
                    + "subnets"
                    + "/"
                    + existingSubnetNameToAssociate));
        }
        this.creatablePrimaryNetworkKey = null;
        this.existingPrimaryNetworkToAssociate = null;
    }

    private void setPrimaryIpConfigurationBackendsAndInboundNatPools() {
        if (isInCreateMode()) {
            return;
        }

        try {
            this.loadCurrentPrimaryLoadBalancersIfAvailable();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }

        VirtualMachineScaleSetIPConfigurationInner primaryIpConfig = primaryNicDefaultIPConfiguration();
        if (this.primaryInternetFacingLoadBalancer != null) {
            removeBackendsFromIpConfiguration(this.primaryInternetFacingLoadBalancer.id(),
                    primaryIpConfig,
                    this.primaryInternetFacingLBBackendsToRemoveOnUpdate.toArray(new String[0]));

            associateBackEndsToIpConfiguration(primaryInternetFacingLoadBalancer.id(),
                    primaryIpConfig,
                    this.primaryInternetFacingLBBackendsToAddOnUpdate.toArray(new String[0]));

            removeInboundNatPoolsFromIpConfiguration(this.primaryInternetFacingLoadBalancer.id(),
                    primaryIpConfig,
                    this.primaryInternetFacingLBInboundNatPoolsToRemoveOnUpdate.toArray(new String[0]));

            associateInboundNATPoolsToIpConfiguration(primaryInternetFacingLoadBalancer.id(),
                    primaryIpConfig,
                    this.primaryInternetFacingLBInboundNatPoolsToAddOnUpdate.toArray(new String[0]));
        }

        if (this.primaryInternalLoadBalancer != null) {
            removeBackendsFromIpConfiguration(this.primaryInternalLoadBalancer.id(),
                    primaryIpConfig,
                    this.primaryInternalLBBackendsToRemoveOnUpdate.toArray(new String[0]));

            associateBackEndsToIpConfiguration(primaryInternalLoadBalancer.id(),
                    primaryIpConfig,
                    this.primaryInternalLBBackendsToAddOnUpdate.toArray(new String[0]));

            removeInboundNatPoolsFromIpConfiguration(this.primaryInternalLoadBalancer.id(),
                    primaryIpConfig,
                    this.primaryInternalLBInboundNatPoolsToRemoveOnUpdate.toArray(new String[0]));

            associateInboundNATPoolsToIpConfiguration(primaryInternalLoadBalancer.id(),
                    primaryIpConfig,
                    this.primaryInternalLBInboundNatPoolsToAddOnUpdate.toArray(new String[0]));
        }

        if (this.removePrimaryInternetFacingLoadBalancerOnUpdate) {
            if (this.primaryInternetFacingLoadBalancer != null) {
                removeLoadBalancerAssociationFromIpConfiguration(this.primaryInternetFacingLoadBalancer, primaryIpConfig);
            }
        }

        if (this.removePrimaryInternalLoadBalancerOnUpdate) {
            if (this.primaryInternalLoadBalancer != null) {
                removeLoadBalancerAssociationFromIpConfiguration(this.primaryInternalLoadBalancer, primaryIpConfig);
            }
        }

        if (this.primaryInternetFacingLoadBalancerToAttachOnUpdate != null) {
            if (this.primaryInternetFacingLoadBalancer != null) {
                removeLoadBalancerAssociationFromIpConfiguration(this.primaryInternetFacingLoadBalancer, primaryIpConfig);
            }
            associateLoadBalancerToIpConfiguration(this.primaryInternetFacingLoadBalancerToAttachOnUpdate, primaryIpConfig);
            if (!this.primaryInternetFacingLBBackendsToAddOnUpdate.isEmpty()) {
                removeAllBackendAssociationFromIpConfiguration(this.primaryInternetFacingLoadBalancerToAttachOnUpdate, primaryIpConfig);
                associateBackEndsToIpConfiguration(this.primaryInternetFacingLoadBalancerToAttachOnUpdate.id(),
                        primaryIpConfig,
                        this.primaryInternetFacingLBBackendsToAddOnUpdate.toArray(new String[0]));
            }
            if (!this.primaryInternetFacingLBInboundNatPoolsToAddOnUpdate.isEmpty()) {
                removeAllInboundNatPoolAssociationFromIpConfiguration(this.primaryInternetFacingLoadBalancerToAttachOnUpdate, primaryIpConfig);
                associateInboundNATPoolsToIpConfiguration(this.primaryInternetFacingLoadBalancerToAttachOnUpdate.id(),
                        primaryIpConfig,
                        this.primaryInternetFacingLBInboundNatPoolsToAddOnUpdate.toArray(new String[0]));
            }
        }

        if (this.primaryInternalLoadBalancerToAttachOnUpdate != null) {
            if (this.primaryInternalLoadBalancer != null) {
                removeLoadBalancerAssociationFromIpConfiguration(this.primaryInternalLoadBalancer, primaryIpConfig);
            }
            associateLoadBalancerToIpConfiguration(this.primaryInternalLoadBalancerToAttachOnUpdate, primaryIpConfig);
            if (!this.primaryInternalLBBackendsToAddOnUpdate.isEmpty()) {
                removeAllBackendAssociationFromIpConfiguration(this.primaryInternalLoadBalancerToAttachOnUpdate, primaryIpConfig);
                associateBackEndsToIpConfiguration(this.primaryInternalLoadBalancerToAttachOnUpdate.id(),
                        primaryIpConfig,
                        this.primaryInternalLBBackendsToAddOnUpdate.toArray(new String[0]));
            }

            if (!this.primaryInternalLBInboundNatPoolsToAddOnUpdate.isEmpty()) {
                removeAllInboundNatPoolAssociationFromIpConfiguration(this.primaryInternalLoadBalancerToAttachOnUpdate, primaryIpConfig);
                associateInboundNATPoolsToIpConfiguration(this.primaryInternalLoadBalancerToAttachOnUpdate.id(),
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
    }

    private void clearCachedProperties() {
        this.primaryInternetFacingLoadBalancer = null;
        this.primaryInternalLoadBalancer = null;
    }

    private void loadCurrentPrimaryLoadBalancersIfAvailable() throws IOException {
        if (this.primaryInternetFacingLoadBalancer != null && this.primaryInternalLoadBalancer != null) {
            return;
        }

        String firstLoadBalancerId = null;
        VirtualMachineScaleSetIPConfigurationInner ipConfig = primaryNicDefaultIPConfiguration();
        if (!ipConfig.loadBalancerBackendAddressPools().isEmpty()) {
            firstLoadBalancerId = ResourceUtils
                    .parentResourcePathFromResourceId(ipConfig.loadBalancerBackendAddressPools().get(0).id());
        }

        if (firstLoadBalancerId == null && !ipConfig.loadBalancerInboundNatPools().isEmpty()) {
            firstLoadBalancerId = ResourceUtils
                    .parentResourcePathFromResourceId(ipConfig.loadBalancerInboundNatPools().get(0).id());
        }

        if (firstLoadBalancerId == null) {
            return;
        }

        LoadBalancer loadBalancer1 = this.networkManager
                .loadBalancers()
                .getById(firstLoadBalancerId);
        if (loadBalancer1.publicIpAddressIds() != null && loadBalancer1.publicIpAddressIds().size() > 0) {
            this.primaryInternetFacingLoadBalancer = loadBalancer1;
        } else {
            this.primaryInternalLoadBalancer = loadBalancer1;
        }

        String secondLoadBalancerId = null;
        for (SubResource subResource: ipConfig.loadBalancerBackendAddressPools()) {
                secondLoadBalancerId = ResourceUtils
                            .parentResourcePathFromResourceId(subResource.id());
                break;
        }

        if (secondLoadBalancerId == null) {
            for (SubResource subResource: ipConfig.loadBalancerInboundNatPools()) {
                if (!subResource.id().toLowerCase().startsWith(firstLoadBalancerId.toLowerCase())) {
                    secondLoadBalancerId = ResourceUtils
                            .parentResourcePathFromResourceId(subResource.id());
                    break;
                }
            }
        }

        if (secondLoadBalancerId == null) {
            return;
        }

        LoadBalancer loadBalancer2 = this.networkManager
            .loadBalancers()
            .getById(secondLoadBalancerId);
        if (loadBalancer2.publicIpAddressIds() != null && loadBalancer2.publicIpAddressIds().size() > 0) {
            this.primaryInternetFacingLoadBalancer = loadBalancer2;
         } else {
            this.primaryInternalLoadBalancer = loadBalancer2;
         }
    }

    private VirtualMachineScaleSetIPConfigurationInner primaryNicDefaultIPConfiguration() {
        List<VirtualMachineScaleSetNetworkConfigurationInner> nicConfigurations = this.inner()
                .virtualMachineProfile()
                .networkProfile()
                .networkInterfaceConfigurations();

        for (VirtualMachineScaleSetNetworkConfigurationInner nicConfiguration : nicConfigurations) {
            if (nicConfiguration.primary()) {
                if (nicConfiguration.ipConfigurations().size() > 0) {
                    VirtualMachineScaleSetIPConfigurationInner ipConfig = nicConfiguration.ipConfigurations().get(0);
                    if (ipConfig.loadBalancerBackendAddressPools() == null) {
                        ipConfig.withLoadBalancerBackendAddressPools(new ArrayList<SubResource>());
                    }
                    if (ipConfig.loadBalancerInboundNatPools() == null) {
                        ipConfig.withLoadBalancerInboundNatPools(new ArrayList<SubResource>());
                    }
                }
            }
        }
        throw new RuntimeException("Could not find the primary nic configuration or an IP configuration in it");
    }

    private static void associateBackEndsToIpConfiguration(String loadBalancerId,
                                                    VirtualMachineScaleSetIPConfigurationInner ipConfig,
                                                    String... backendNames) {
        List<SubResource> backendSubResourcesToAssociate = new ArrayList<>();
        for (String backendName : backendNames) {
            String backendPoolId = loadBalancerId + "/" + "backendAddressPools" + "/" + backendName;
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

    private static void associateInboundNATPoolsToIpConfiguration(String loadBalancerId,
                                                    VirtualMachineScaleSetIPConfigurationInner ipConfig,
                                                    String... inboundNatPools) {
        List<SubResource> inboundNatPoolSubResourcesToAssociate = new ArrayList<>();
        for (String inboundNatPool : inboundNatPools) {
            String inboundNatPoolId = loadBalancerId + "/" + "inboundNatPools" + "/" + inboundNatPool;
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

    private static Map<String, Backend> getBackendsAssociatedWithIpConfiguration(LoadBalancer loadBalancer,
                                                                                 VirtualMachineScaleSetIPConfigurationInner ipConfig) {
        String loadBalancerId = loadBalancer.id();
        Map<String, Backend> attachedBackends = new HashMap<>();
        Map<String, Backend> lbBackends = loadBalancer.backends();
        for (Backend lbBackend : lbBackends.values()) {
            String backendId =  loadBalancerId + "/" + "backendAddressPools" + "/" + lbBackend.name();
            for (SubResource subResource : ipConfig.loadBalancerBackendAddressPools()) {
                if (subResource.id().equalsIgnoreCase(backendId)) {
                    attachedBackends.put(lbBackend.name(), lbBackend);
                }
            }
        }
        return attachedBackends;
    }

    private static Map<String, InboundNatPool> getInboundNatPoolsAssociatedWithIpConfiguration(LoadBalancer loadBalancer,
                                                                                               VirtualMachineScaleSetIPConfigurationInner ipConfig) {
        String loadBalancerId = loadBalancer.id();
        Map<String, InboundNatPool> attachedInboundNatPools = new HashMap<>();
        Map<String, InboundNatPool> lbInboundNatPools = loadBalancer.inboundNatPools();
        for (InboundNatPool lbInboundNatPool : lbInboundNatPools.values()) {
            String inboundNatPoolId =  loadBalancerId + "/" + "inboundNatPools" + "/" + lbInboundNatPool.name();
            for (SubResource subResource : ipConfig.loadBalancerInboundNatPools()) {
                if (subResource.id().equalsIgnoreCase(inboundNatPoolId)) {
                    attachedInboundNatPools.put(lbInboundNatPool.name(), lbInboundNatPool);
                }
            }
        }
        return attachedInboundNatPools;
    }

    private static void associateLoadBalancerToIpConfiguration(LoadBalancer loadBalancer,
                                                               VirtualMachineScaleSetIPConfigurationInner ipConfig) {
        Collection<Backend> backends = loadBalancer.backends().values();
        String[] backendNames = new String[backends.size()];
        int i = 0;
        for (Backend backend : backends) {
            backendNames[i] = backend.name();
            i++;
        }

        associateBackEndsToIpConfiguration(loadBalancer.id(),
                ipConfig,
                backendNames);

        Collection<InboundNatPool> inboundNatPools = loadBalancer.inboundNatPools().values();
        String[] natPoolNames = new String[inboundNatPools.size()];
        i = 0;
        for (InboundNatPool inboundNatPool : inboundNatPools) {
            natPoolNames[i] = inboundNatPool.name();
            i++;
        }

        associateInboundNATPoolsToIpConfiguration(loadBalancer.id(),
                ipConfig,
                natPoolNames);
    }

    private static void removeLoadBalancerAssociationFromIpConfiguration(LoadBalancer loadBalancer,
                                                                         VirtualMachineScaleSetIPConfigurationInner ipConfig) {
        removeAllBackendAssociationFromIpConfiguration(loadBalancer, ipConfig);
        removeAllInboundNatPoolAssociationFromIpConfiguration(loadBalancer, ipConfig);
    }

    private static void removeAllBackendAssociationFromIpConfiguration(LoadBalancer loadBalancer,
                                                                       VirtualMachineScaleSetIPConfigurationInner ipConfig) {
        List<Integer> toRemoveIndicies = new ArrayList<>();
        int i = 0;
        for (SubResource subResource : ipConfig.loadBalancerBackendAddressPools()) {
            if (subResource.id().toLowerCase().startsWith(loadBalancer.id().toLowerCase() + "/")) {
                toRemoveIndicies.add(i);
            }
            i++;
        }

        for (Integer index : toRemoveIndicies) {
            ipConfig.loadBalancerBackendAddressPools().remove(index);
        }
    }

    private static void removeAllInboundNatPoolAssociationFromIpConfiguration(LoadBalancer loadBalancer,
                                                                              VirtualMachineScaleSetIPConfigurationInner ipConfig) {
        List<Integer> toRemoveIndices = new ArrayList<>();
        int i = 0;
        for (SubResource subResource : ipConfig.loadBalancerInboundNatPools()) {
            if (subResource.id().toLowerCase().startsWith(loadBalancer.id().toLowerCase() + "/")) {
                toRemoveIndices.add(i);
            }
            i++;
        }

        for (Integer index : toRemoveIndices) {
            ipConfig.loadBalancerInboundNatPools().remove(index);
        }
    }

    private static void removeBackendsFromIpConfiguration(String loadBalancerId,
                                                   VirtualMachineScaleSetIPConfigurationInner ipConfig,
                                                   String... backendNames) {
        List<Integer> toRemoveIndices = new ArrayList<>();
        for (String backendName : backendNames) {
            String backendPoolId = loadBalancerId + "/" + "backendAddressPools" + "/" + backendName;
            int index = -1;
            for (SubResource subResource : ipConfig.loadBalancerBackendAddressPools()) {
                index++;
                if (subResource.id().equalsIgnoreCase(backendPoolId)) {
                    toRemoveIndices.add(index);
                    break;
                }
            }
        }

        for (Integer index : toRemoveIndices) {
            ipConfig.loadBalancerBackendAddressPools().remove(index);
        }
    }

    private static void removeInboundNatPoolsFromIpConfiguration(String loadBalancerId,
                                                          VirtualMachineScaleSetIPConfigurationInner ipConfig,
                                                          String... inboundNatPoolNames) {
        List<Integer> toRemoveIndices = new ArrayList<>();
        for (String natPoolName : inboundNatPoolNames) {
            String inboundNatPoolId = loadBalancerId + "/" + "inboundNatPools" + "/" + natPoolName;
            int index = -1;
            for (SubResource subResource : ipConfig.loadBalancerInboundNatPools()) {
                index++;
                if (subResource.id().equalsIgnoreCase(inboundNatPoolId)) {
                    toRemoveIndices.add(index);
                    break;
                }
            }
        }

        for (Integer index : toRemoveIndices) {
            ipConfig.loadBalancerInboundNatPools().remove(index);
        }
    }

    private static <T> void addToList(List<T> list, T...items) {
        for (T item : items) {
            list.add(item);
        }
    }
}
