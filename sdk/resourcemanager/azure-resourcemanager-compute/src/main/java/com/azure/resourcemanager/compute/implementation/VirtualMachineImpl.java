// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.SubResource;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.AvailabilitySet;
import com.azure.resourcemanager.compute.models.AvailabilitySetSkuTypes;
import com.azure.resourcemanager.compute.models.BillingProfile;
import com.azure.resourcemanager.compute.models.BootDiagnostics;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.DataDisk;
import com.azure.resourcemanager.compute.models.DiagnosticsProfile;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.compute.models.DiskEncryptionSettings;
import com.azure.resourcemanager.compute.models.HardwareProfile;
import com.azure.resourcemanager.compute.models.ImageReference;
import com.azure.resourcemanager.compute.models.InstanceViewTypes;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.LinuxConfiguration;
import com.azure.resourcemanager.compute.models.ManagedDiskParameters;
import com.azure.resourcemanager.compute.models.NetworkInterfaceReference;
import com.azure.resourcemanager.compute.models.OSDisk;
import com.azure.resourcemanager.compute.models.OSProfile;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.Plan;
import com.azure.resourcemanager.compute.models.PowerState;
import com.azure.resourcemanager.compute.models.ProximityPlacementGroup;
import com.azure.resourcemanager.compute.models.ProximityPlacementGroupType;
import com.azure.resourcemanager.compute.models.PurchasePlan;
import com.azure.resourcemanager.compute.models.ResourceIdentityType;
import com.azure.resourcemanager.compute.models.RunCommandInput;
import com.azure.resourcemanager.compute.models.RunCommandInputParameter;
import com.azure.resourcemanager.compute.models.RunCommandResult;
import com.azure.resourcemanager.compute.models.SshConfiguration;
import com.azure.resourcemanager.compute.models.SshPublicKey;
import com.azure.resourcemanager.compute.models.StorageAccountTypes;
import com.azure.resourcemanager.compute.models.StorageProfile;
import com.azure.resourcemanager.compute.models.VirtualHardDisk;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineCaptureParameters;
import com.azure.resourcemanager.compute.models.VirtualMachineDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineEncryption;
import com.azure.resourcemanager.compute.models.VirtualMachineEvictionPolicyTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineExtension;
import com.azure.resourcemanager.compute.models.VirtualMachineInstanceView;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImage;
import com.azure.resourcemanager.compute.models.VirtualMachinePriorityTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineSize;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineUnmanagedDataDisk;
import com.azure.resourcemanager.compute.models.WinRMConfiguration;
import com.azure.resourcemanager.compute.models.WinRMListener;
import com.azure.resourcemanager.compute.models.WindowsConfiguration;
import com.azure.resourcemanager.compute.fluent.inner.ProximityPlacementGroupInner;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineInner;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineUpdateInner;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.implementation.RoleAssignmentHelper;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.AcceptedImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceNamer;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.StorageManager;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

/** The implementation for VirtualMachine and its create and update interfaces. */
class VirtualMachineImpl
    extends GroupableResourceImpl<VirtualMachine, VirtualMachineInner, VirtualMachineImpl, ComputeManager>
    implements VirtualMachine,
        VirtualMachine.DefinitionManagedOrUnmanaged,
        VirtualMachine.DefinitionManaged,
        VirtualMachine.DefinitionUnmanaged,
        VirtualMachine.Update,
        VirtualMachine.DefinitionStages.WithSystemAssignedIdentityBasedAccessOrCreate,
        VirtualMachine.UpdateStages.WithSystemAssignedIdentityBasedAccessOrUpdate {
    // Clients
    private final StorageManager storageManager;
    private final NetworkManager networkManager;
    private final AuthorizationManager authorizationManager;
    // the name of the virtual machine
    private final String vmName;
    // used to generate unique name for any dependency resources
    private final ResourceNamer namer;
    // unique key of a creatable storage account to be used for virtual machine child resources that
    // requires storage [OS disk, data disk, boot diagnostics etc..]
    private String creatableStorageAccountKey;
    // unique key of a creatable availability set that this virtual machine to put
    private String creatableAvailabilitySetKey;
    // unique key of a creatable network interface that needs to be used as virtual machine's primary network interface
    private String creatablePrimaryNetworkInterfaceKey;
    // unique key of a creatable network interfaces that needs to be used as virtual machine's secondary network
    // interface
    private List<String> creatableSecondaryNetworkInterfaceKeys;
    // reference to an existing storage account to be used for virtual machine child resources that
    // requires storage [OS disk, data disk, boot diagnostics etc..]
    private StorageAccount existingStorageAccountToAssociate;
    // reference to an existing availability set that this virtual machine to put
    private AvailabilitySet existingAvailabilitySetToAssociate;
    // reference to an existing network interface that needs to be used as virtual machine's primary network interface
    private NetworkInterface existingPrimaryNetworkInterfaceToAssociate;
    // reference to a list of existing network interfaces that needs to be used as virtual machine's secondary network
    // interface
    private List<NetworkInterface> existingSecondaryNetworkInterfacesToAssociate;
    private VirtualMachineInstanceView virtualMachineInstanceView;
    private boolean isMarketplaceLinuxImage;
    // Intermediate state of network interface definition to which private IP can be associated
    private NetworkInterface.DefinitionStages.WithPrimaryPrivateIP nicDefinitionWithPrivateIp;
    // Intermediate state of network interface definition to which subnet can be associated
    private NetworkInterface.DefinitionStages.WithPrimaryNetworkSubnet nicDefinitionWithSubnet;
    // Intermediate state of network interface definition to which public IP can be associated
    private NetworkInterface.DefinitionStages.WithCreate nicDefinitionWithCreate;
    // The entry point to manage extensions associated with the virtual machine
    private VirtualMachineExtensionsImpl virtualMachineExtensions;
    // Flag indicates native disk is selected for OS and Data disks
    private boolean isUnmanagedDiskSelected;
    // Error messages
    // The native data disks associated with the virtual machine
    private List<VirtualMachineUnmanagedDataDisk> unmanagedDataDisks;
    // To track the managed data disks
    private final ManagedDataDiskCollection managedDataDisks;
    // To manage boot diagnostics specific operations
    private final BootDiagnosticsHandler bootDiagnosticsHandler;
    // Utility to setup MSI for the virtual machine
    private VirtualMachineMsiHandler virtualMachineMsiHandler;
    // Reference to the PublicIp creatable that is implicitly created
    private PublicIpAddress.DefinitionStages.WithCreate implicitPipCreatable;
    // Name of the new proximity placement group
    private String newProximityPlacementGroupName;
    // Type fo the new proximity placement group
    private ProximityPlacementGroupType newProximityPlacementGroupType;
    // To manage OS profile
    private boolean removeOsProfile;
    private final ClientLogger logger = new ClientLogger(VirtualMachineImpl.class);
    private final ObjectMapper mapper;
    private static final JacksonAnnotationIntrospector ANNOTATION_INTROSPECTOR = new JacksonAnnotationIntrospector() {
        @Override
        public JsonProperty.Access findPropertyAccess(Annotated annotated) {
            JsonProperty.Access access = super.findPropertyAccess(annotated);
            if (access == JsonProperty.Access.WRITE_ONLY) {
                return JsonProperty.Access.AUTO;
            }
            return access;
        }
    };

    VirtualMachineImpl(
        String name,
        VirtualMachineInner innerModel,
        final ComputeManager computeManager,
        final StorageManager storageManager,
        final NetworkManager networkManager,
        final AuthorizationManager authorizationManager) {
        super(name, innerModel, computeManager);
        this.storageManager = storageManager;
        this.networkManager = networkManager;
        this.authorizationManager = authorizationManager;
        this.vmName = name;
        this.isMarketplaceLinuxImage = false;
        this.namer = this.manager().sdkContext().getResourceNamerFactory().createResourceNamer(this.vmName);
        this.creatableSecondaryNetworkInterfaceKeys = new ArrayList<>();
        this.existingSecondaryNetworkInterfacesToAssociate = new ArrayList<>();
        this.virtualMachineExtensions =
            new VirtualMachineExtensionsImpl(computeManager.inner().getVirtualMachineExtensions(), this);

        this.managedDataDisks = new ManagedDataDiskCollection(this);
        initializeDataDisks();
        this.bootDiagnosticsHandler = new BootDiagnosticsHandler(this);
        this.virtualMachineMsiHandler = new VirtualMachineMsiHandler(authorizationManager, this);
        this.newProximityPlacementGroupName = null;
        this.newProximityPlacementGroupType = null;
        this.mapper = new ObjectMapper();
        this.mapper.setAnnotationIntrospector(ANNOTATION_INTROSPECTOR);
    }

    // Verbs

    @Override
    public Mono<VirtualMachine> refreshAsync() {
        return super
            .refreshAsync()
            .map(
                virtualMachine -> {
                    reset(virtualMachine.inner());
                    virtualMachineExtensions.refresh();
                    return virtualMachine;
                });
    }

    @Override
    protected Mono<VirtualMachineInner> getInnerAsync() {
        return this.manager().inner().getVirtualMachines()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
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
            .getVirtualMachines()
            .deallocateAsync(this.resourceGroupName(), this.name())
            // Refresh after deallocate to ensure the inner is updatable (due to a change in behavior in Managed Disks)
            .map(aVoid -> this.refreshAsync())
            .then();
    }

    @Override
    public void generalize() {
        this.generalizeAsync().block();
    }

    @Override
    public Mono<Void> generalizeAsync() {
        return this.manager().inner().getVirtualMachines().generalizeAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public void powerOff() {
        this.powerOffAsync().block();
    }

    @Override
    public Mono<Void> powerOffAsync() {
        return this.manager().inner().getVirtualMachines().powerOffAsync(this.resourceGroupName(), this.name(), null);
    }

    @Override
    public void restart() {
        this.restartAsync().block();
    }

    @Override
    public Mono<Void> restartAsync() {
        return this.manager().inner().getVirtualMachines().restartAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public void start() {
        this.startAsync().block();
    }

    @Override
    public Mono<Void> startAsync() {
        return this.manager().inner().getVirtualMachines().startAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public void redeploy() {
        this.redeployAsync().block();
    }

    @Override
    public Mono<Void> redeployAsync() {
        return this.manager().inner().getVirtualMachines().redeployAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public void simulateEviction() {
        this.simulateEvictionAsync().block();
    }

    @Override
    public Mono<Void> simulateEvictionAsync() {
        return this.manager().inner().getVirtualMachines().simulateEvictionAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public void convertToManaged() {
        this.manager().inner().getVirtualMachines().convertToManagedDisks(this.resourceGroupName(), this.name());
        this.refresh();
    }

    @Override
    public Mono<Void> convertToManagedAsync() {
        return this
            .manager()
            .inner()
            .getVirtualMachines()
            .convertToManagedDisksAsync(this.resourceGroupName(), this.name())
            .flatMap(aVoid -> refreshAsync())
            .then();
    }

    @Override
    public VirtualMachineEncryption diskEncryption() {
        return new VirtualMachineEncryptionImpl(this);
    }

    @Override
    public PagedIterable<VirtualMachineSize> availableSizes() {
        return this
            .manager()
            .inner()
            .getVirtualMachines()
            .listAvailableSizes(this.resourceGroupName(), this.name())
            .mapPage(VirtualMachineSizeImpl::new);
    }

    @Override
    public String capture(String containerName, String vhdPrefix, boolean overwriteVhd) {
        return this.captureAsync(containerName, vhdPrefix, overwriteVhd).block();
    }

    @Override
    public Mono<String> captureAsync(String containerName, String vhdPrefix, boolean overwriteVhd) {
        VirtualMachineCaptureParameters parameters = new VirtualMachineCaptureParameters();
        parameters.withDestinationContainerName(containerName);
        parameters.withOverwriteVhds(overwriteVhd);
        parameters.withVhdPrefix(vhdPrefix);
        return this
            .manager()
            .inner()
            .getVirtualMachines()
            .captureAsync(this.resourceGroupName(), this.name(), parameters)
            .map(
                captureResultInner -> {
                    try {
                        return mapper.writeValueAsString(captureResultInner);
                    } catch (JsonProcessingException ex) {
                        throw logger.logExceptionAsError(Exceptions.propagate(ex));
                    }
                });
    }

    @Override
    public VirtualMachineInstanceView refreshInstanceView() {
        return refreshInstanceViewAsync().block();
    }

    @Override
    public Mono<VirtualMachineInstanceView> refreshInstanceViewAsync() {
        return this
            .manager()
            .inner()
            .getVirtualMachines()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name(), InstanceViewTypes.INSTANCE_VIEW)
            .map(
                inner -> {
                    virtualMachineInstanceView = new VirtualMachineInstanceViewImpl(inner.instanceView());
                    return virtualMachineInstanceView;
                })
            .switchIfEmpty(
                Mono
                    .defer(
                        () -> {
                            virtualMachineInstanceView = null;
                            return Mono.empty();
                        }));
    }

    @Override
    public RunCommandResult runPowerShellScript(
        List<String> scriptLines, List<RunCommandInputParameter> scriptParameters) {
        return this
            .manager()
            .virtualMachines()
            .runPowerShellScript(this.resourceGroupName(), this.name(), scriptLines, scriptParameters);
    }

    @Override
    public Mono<RunCommandResult> runPowerShellScriptAsync(
        List<String> scriptLines, List<RunCommandInputParameter> scriptParameters) {
        return this
            .manager()
            .virtualMachines()
            .runPowerShellScriptAsync(this.resourceGroupName(), this.name(), scriptLines, scriptParameters);
    }

    @Override
    public RunCommandResult runShellScript(List<String> scriptLines, List<RunCommandInputParameter> scriptParameters) {
        return this
            .manager()
            .virtualMachines()
            .runShellScript(this.resourceGroupName(), this.name(), scriptLines, scriptParameters);
    }

    @Override
    public Mono<RunCommandResult> runShellScriptAsync(
        List<String> scriptLines, List<RunCommandInputParameter> scriptParameters) {
        return this
            .manager()
            .virtualMachines()
            .runShellScriptAsync(this.resourceGroupName(), this.name(), scriptLines, scriptParameters);
    }

    @Override
    public RunCommandResult runCommand(RunCommandInput inputCommand) {
        return this.manager().virtualMachines().runCommand(this.resourceGroupName(), this.name(), inputCommand);
    }

    @Override
    public Mono<RunCommandResult> runCommandAsync(RunCommandInput inputCommand) {
        return this.manager().virtualMachines().runCommandAsync(this.resourceGroupName(), this.name(), inputCommand);
    }

    // SETTERS

    // Fluent methods for defining virtual network association for the new primary network interface
    @Override
    public VirtualMachineImpl withNewPrimaryNetwork(Creatable<Network> creatable) {
        this.nicDefinitionWithPrivateIp =
            this.preparePrimaryNetworkInterface(this.namer.randomName("nic", 20))
                .withNewPrimaryNetwork(creatable);
        return this;
    }

    @Override
    public VirtualMachineImpl withNewPrimaryNetwork(String addressSpace) {
        this.nicDefinitionWithPrivateIp =
            this.preparePrimaryNetworkInterface(this.namer.randomName("nic", 20))
                .withNewPrimaryNetwork(addressSpace);
        return this;
    }

    @Override
    public VirtualMachineImpl withExistingPrimaryNetwork(Network network) {
        this.nicDefinitionWithSubnet =
            this.preparePrimaryNetworkInterface(this.namer.randomName("nic", 20))
                .withExistingPrimaryNetwork(network);
        return this;
    }

    @Override
    public VirtualMachineImpl withSubnet(String name) {
        this.nicDefinitionWithPrivateIp = this.nicDefinitionWithSubnet.withSubnet(name);
        return this;
    }

    // Fluent methods for defining private IP association for the new primary network interface
    @Override
    public VirtualMachineImpl withPrimaryPrivateIPAddressDynamic() {
        this.nicDefinitionWithCreate = this.nicDefinitionWithPrivateIp.withPrimaryPrivateIPAddressDynamic();
        return this;
    }

    @Override
    public VirtualMachineImpl withPrimaryPrivateIPAddressStatic(String staticPrivateIPAddress) {
        this.nicDefinitionWithCreate =
            this.nicDefinitionWithPrivateIp.withPrimaryPrivateIPAddressStatic(staticPrivateIPAddress);
        return this;
    }

    // Fluent methods for defining public IP association for the new primary network interface
    @Override
    public VirtualMachineImpl withNewPrimaryPublicIPAddress(Creatable<PublicIpAddress> creatable) {
        Creatable<NetworkInterface> nicCreatable =
            this.nicDefinitionWithCreate.withNewPrimaryPublicIPAddress(creatable);
        this.creatablePrimaryNetworkInterfaceKey = this.addDependency(nicCreatable);
        return this;
    }

    @Override
    public VirtualMachineImpl withNewPrimaryPublicIPAddress(String leafDnsLabel) {
        PublicIpAddress.DefinitionStages.WithGroup definitionWithGroup =
            this
                .networkManager
                .publicIpAddresses()
                .define(this.namer.randomName("pip", 15))
                .withRegion(this.regionName());
        PublicIpAddress.DefinitionStages.WithCreate definitionAfterGroup;
        if (this.creatableGroup != null) {
            definitionAfterGroup = definitionWithGroup.withNewResourceGroup(this.creatableGroup);
        } else {
            definitionAfterGroup = definitionWithGroup.withExistingResourceGroup(this.resourceGroupName());
        }
        this.implicitPipCreatable = definitionAfterGroup.withLeafDomainLabel(leafDnsLabel);
        // Create NIC with creatable PIP
        Creatable<NetworkInterface> nicCreatable =
            this.nicDefinitionWithCreate.withNewPrimaryPublicIPAddress(this.implicitPipCreatable);
        this.creatablePrimaryNetworkInterfaceKey = this.addDependency(nicCreatable);
        return this;
    }

    @Override
    public VirtualMachineImpl withExistingPrimaryPublicIPAddress(PublicIpAddress publicIPAddress) {
        Creatable<NetworkInterface> nicCreatable =
            this.nicDefinitionWithCreate.withExistingPrimaryPublicIPAddress(publicIPAddress);
        this.creatablePrimaryNetworkInterfaceKey = this.addDependency(nicCreatable);
        return this;
    }

    @Override
    public VirtualMachineImpl withoutPrimaryPublicIPAddress() {
        Creatable<NetworkInterface> nicCreatable = this.nicDefinitionWithCreate;
        this.creatablePrimaryNetworkInterfaceKey = this.addDependency(nicCreatable);
        return this;
    }

    // Virtual machine primary network interface specific fluent methods
    //
    @Override
    public VirtualMachineImpl withNewPrimaryNetworkInterface(Creatable<NetworkInterface> creatable) {
        this.creatablePrimaryNetworkInterfaceKey = this.addDependency(creatable);
        return this;
    }

    public VirtualMachineImpl withNewPrimaryNetworkInterface(String name, String publicDnsNameLabel) {
        Creatable<NetworkInterface> definitionCreatable =
            prepareNetworkInterface(name).withNewPrimaryPublicIPAddress(publicDnsNameLabel);
        return withNewPrimaryNetworkInterface(definitionCreatable);
    }

    @Override
    public VirtualMachineImpl withExistingPrimaryNetworkInterface(NetworkInterface networkInterface) {
        this.existingPrimaryNetworkInterfaceToAssociate = networkInterface;
        return this;
    }

    // Virtual machine image specific fluent methods
    //
    @Override
    public VirtualMachineImpl withStoredWindowsImage(String imageUrl) {
        VirtualHardDisk userImageVhd = new VirtualHardDisk();
        userImageVhd.withUri(imageUrl);
        this.inner().storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().storageProfile().osDisk().withImage(userImageVhd);
        // For platform image osType will be null, azure will pick it from the image metadata.
        this.inner().storageProfile().osDisk().withOsType(OperatingSystemTypes.WINDOWS);
        this.inner().osProfile().withWindowsConfiguration(new WindowsConfiguration());
        // sets defaults for "Stored(User)Image" or "VM(Platform)Image"
        this.inner().osProfile().windowsConfiguration().withProvisionVMAgent(true);
        this.inner().osProfile().windowsConfiguration().withEnableAutomaticUpdates(true);
        return this;
    }

    @Override
    public VirtualMachineImpl withStoredLinuxImage(String imageUrl) {
        VirtualHardDisk userImageVhd = new VirtualHardDisk();
        userImageVhd.withUri(imageUrl);
        this.inner().storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().storageProfile().osDisk().withImage(userImageVhd);
        // For platform | custom image osType will be null, azure will pick it from the image metadata.
        // But for stored image, osType needs to be specified explicitly
        this.inner().storageProfile().osDisk().withOsType(OperatingSystemTypes.LINUX);
        this.inner().osProfile().withLinuxConfiguration(new LinuxConfiguration());
        return this;
    }

    @Override
    public VirtualMachineImpl withPopularWindowsImage(KnownWindowsVirtualMachineImage knownImage) {
        return withSpecificWindowsImageVersion(knownImage.imageReference());
    }

    @Override
    public VirtualMachineImpl withPopularLinuxImage(KnownLinuxVirtualMachineImage knownImage) {
        return withSpecificLinuxImageVersion(knownImage.imageReference());
    }

    @Override
    public VirtualMachineImpl withSpecificWindowsImageVersion(ImageReference imageReference) {
        this.inner().storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().storageProfile().withImageReference(imageReference);
        this.inner().osProfile().withWindowsConfiguration(new WindowsConfiguration());
        // sets defaults for "Stored(User)Image" or "VM(Platform)Image"
        this.inner().osProfile().windowsConfiguration().withProvisionVMAgent(true);
        this.inner().osProfile().windowsConfiguration().withEnableAutomaticUpdates(true);
        return this;
    }

    @Override
    public VirtualMachineImpl withSpecificLinuxImageVersion(ImageReference imageReference) {
        this.inner().storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().storageProfile().withImageReference(imageReference);
        this.inner().osProfile().withLinuxConfiguration(new LinuxConfiguration());
        this.isMarketplaceLinuxImage = true;
        return this;
    }

    @Override
    public VirtualMachineImpl withLatestWindowsImage(String publisher, String offer, String sku) {
        ImageReference imageReference = new ImageReference();
        imageReference.withPublisher(publisher);
        imageReference.withOffer(offer);
        imageReference.withSku(sku);
        imageReference.withVersion("latest");
        return withSpecificWindowsImageVersion(imageReference);
    }

    @Override
    public VirtualMachineImpl withLatestLinuxImage(String publisher, String offer, String sku) {
        ImageReference imageReference = new ImageReference();
        imageReference.withPublisher(publisher);
        imageReference.withOffer(offer);
        imageReference.withSku(sku);
        imageReference.withVersion("latest");
        return withSpecificLinuxImageVersion(imageReference);
    }

    @Override
    public VirtualMachineImpl withGeneralizedWindowsCustomImage(String customImageId) {
        ImageReference imageReferenceInner = new ImageReference();
        imageReferenceInner.withId(customImageId);
        this.inner().storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().storageProfile().withImageReference(imageReferenceInner);
        this.inner().osProfile().withWindowsConfiguration(new WindowsConfiguration());
        // sets defaults for "Stored(User)Image", "VM(Platform | Custom | Gallery)Image"
        this.inner().osProfile().windowsConfiguration().withProvisionVMAgent(true);
        this.inner().osProfile().windowsConfiguration().withEnableAutomaticUpdates(true);
        return this;
    }

    @Override
    public VirtualMachineImpl withSpecializedWindowsCustomImage(String customImageId) {
        this.withGeneralizedWindowsCustomImage(customImageId);
        this.removeOsProfile = true;
        return this;
    }

    @Override
    public VirtualMachineImpl withGeneralizedWindowsGalleryImageVersion(String galleryImageVersionId) {
        return this.withGeneralizedWindowsCustomImage(galleryImageVersionId);
    }

    @Override
    public VirtualMachineImpl withSpecializedWindowsGalleryImageVersion(String galleryImageVersionId) {
        return this.withSpecializedWindowsCustomImage(galleryImageVersionId);
    }

    @Override
    public VirtualMachineImpl withGeneralizedLinuxCustomImage(String customImageId) {
        ImageReference imageReferenceInner = new ImageReference();
        imageReferenceInner.withId(customImageId);
        this.inner().storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().storageProfile().withImageReference(imageReferenceInner);
        this.inner().osProfile().withLinuxConfiguration(new LinuxConfiguration());
        this.isMarketplaceLinuxImage = true;
        return this;
    }

    @Override
    public VirtualMachineImpl withSpecializedLinuxCustomImage(String customImageId) {
        this.withGeneralizedLinuxCustomImage(customImageId);
        this.removeOsProfile = true;
        return this;
    }

    @Override
    public VirtualMachineImpl withGeneralizedLinuxGalleryImageVersion(String galleryImageVersionId) {
        return this.withGeneralizedLinuxCustomImage(galleryImageVersionId);
    }

    @Override
    public VirtualMachineImpl withSpecializedLinuxGalleryImageVersion(String galleryImageVersionId) {
        return this.withSpecializedLinuxCustomImage(galleryImageVersionId);
    }

    @Override
    public VirtualMachineImpl withSpecializedOSUnmanagedDisk(String osDiskUrl, OperatingSystemTypes osType) {
        VirtualHardDisk osVhd = new VirtualHardDisk();
        osVhd.withUri(osDiskUrl);
        this.inner().storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.ATTACH);
        this.inner().storageProfile().osDisk().withVhd(osVhd);
        this.inner().storageProfile().osDisk().withOsType(osType);
        this.inner().storageProfile().osDisk().withManagedDisk(null);
        return this;
    }

    @Override
    public VirtualMachineImpl withSpecializedOSDisk(Disk disk, OperatingSystemTypes osType) {
        ManagedDiskParameters diskParametersInner = new ManagedDiskParameters();
        diskParametersInner.withId(disk.id());
        this.inner().storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.ATTACH);
        this.inner().storageProfile().osDisk().withManagedDisk(diskParametersInner);
        this.inner().storageProfile().osDisk().withOsType(osType);
        this.inner().storageProfile().osDisk().withVhd(null);
        return this;
    }

    // Virtual machine user name fluent methods
    @Override
    public VirtualMachineImpl withRootUsername(String rootUserName) {
        this.inner().osProfile().withAdminUsername(rootUserName);
        return this;
    }

    @Override
    public VirtualMachineImpl withAdminUsername(String adminUserName) {
        this.inner().osProfile().withAdminUsername(adminUserName);
        return this;
    }

    // Virtual machine optional fluent methods
    @Override
    public VirtualMachineImpl withSsh(String publicKeyData) {
        OSProfile osProfile = this.inner().osProfile();
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
    public VirtualMachineImpl withoutVMAgent() {
        this.inner().osProfile().windowsConfiguration().withProvisionVMAgent(false);
        return this;
    }

    @Override
    public VirtualMachineImpl withoutAutoUpdate() {
        this.inner().osProfile().windowsConfiguration().withEnableAutomaticUpdates(false);
        return this;
    }

    @Override
    public VirtualMachineImpl withTimeZone(String timeZone) {
        this.inner().osProfile().windowsConfiguration().withTimeZone(timeZone);
        return this;
    }

    @Override
    public VirtualMachineImpl withWinRM(WinRMListener listener) {
        if (this.inner().osProfile().windowsConfiguration().winRM() == null) {
            WinRMConfiguration winRMConfiguration = new WinRMConfiguration();
            this.inner().osProfile().windowsConfiguration().withWinRM(winRMConfiguration);
        }
        this.inner().osProfile().windowsConfiguration().winRM().listeners().add(listener);
        return this;
    }

    @Override
    public VirtualMachineImpl withRootPassword(String password) {
        this.inner().osProfile().withAdminPassword(password);
        return this;
    }

    @Override
    public VirtualMachineImpl withAdminPassword(String password) {
        this.inner().osProfile().withAdminPassword(password);
        return this;
    }

    @Override
    public VirtualMachineImpl withCustomData(String base64EncodedCustomData) {
        this.inner().osProfile().withCustomData(base64EncodedCustomData);
        return this;
    }

    @Override
    public VirtualMachineImpl withComputerName(String computerName) {
        this.inner().osProfile().withComputerName(computerName);
        return this;
    }

    @Override
    public VirtualMachineImpl withSize(String sizeName) {
        this.inner().hardwareProfile().withVmSize(VirtualMachineSizeTypes.fromString(sizeName));
        return this;
    }

    @Override
    public VirtualMachineImpl withSize(VirtualMachineSizeTypes size) {
        this.inner().hardwareProfile().withVmSize(size);
        return this;
    }

    @Override
    public VirtualMachineImpl withOSDiskCaching(CachingTypes cachingType) {
        this.inner().storageProfile().osDisk().withCaching(cachingType);
        return this;
    }

    @Override
    public VirtualMachineImpl withOSDiskVhdLocation(String containerName, String vhdName) {
        // Sets the native (un-managed) disk backing virtual machine OS disk
        if (isManagedDiskEnabled()) {
            return this;
        }
        StorageProfile storageProfile = this.inner().storageProfile();
        OSDisk osDisk = storageProfile.osDisk();
        // Setting native (un-managed) disk backing virtual machine OS disk is valid only when
        // the virtual machine is created from image.
        if (!this.isOSDiskFromImage(osDisk)) {
            return this;
        }
        // Exclude custom user image as they won't support using native (un-managed) disk to back
        // virtual machine OS disk.
        if (this.isOsDiskFromCustomImage(storageProfile)) {
            return this;
        }
        // OS Disk from 'Platform image' requires explicit storage account to be specified.
        if (this.isOSDiskFromPlatformImage(storageProfile)) {
            VirtualHardDisk osVhd = new VirtualHardDisk();
            osVhd.withUri(temporaryBlobUrl(containerName, vhdName));
            this.inner().storageProfile().osDisk().withVhd(osVhd);
            return this;
        }
        // 'Stored image' and 'Bring your own feature image' has a restriction that the native
        // disk backing OS disk based on these images should reside in the same storage account
        // as the image.
        if (this.isOSDiskFromStoredImage(storageProfile)) {
            VirtualHardDisk osVhd = new VirtualHardDisk();
            try {
                URL sourceCustomImageUrl = new URL(osDisk.image().uri());
                URL destinationVhdUrl =
                    new URL(
                        sourceCustomImageUrl.getProtocol(),
                        sourceCustomImageUrl.getHost(),
                        "/" + containerName + "/" + vhdName);
                osVhd.withUri(destinationVhdUrl.toString());
            } catch (MalformedURLException ex) {
                throw logger.logExceptionAsError(new RuntimeException(ex));
            }
            this.inner().storageProfile().osDisk().withVhd(osVhd);
        }
        return this;
    }

    @Override
    public VirtualMachineImpl withOSDiskStorageAccountType(StorageAccountTypes accountType) {
        if (this.inner().storageProfile().osDisk().managedDisk() == null) {
            this.inner().storageProfile().osDisk().withManagedDisk(new ManagedDiskParameters());
        }
        this.inner().storageProfile().osDisk().managedDisk().withStorageAccountType(accountType);
        return this;
    }

    @Override
    public VirtualMachineImpl withDataDiskDefaultCachingType(CachingTypes cachingType) {
        this.managedDataDisks.setDefaultCachingType(cachingType);
        return this;
    }

    @Override
    public VirtualMachineImpl withDataDiskDefaultStorageAccountType(StorageAccountTypes storageAccountType) {
        this.managedDataDisks.setDefaultStorageAccountType(storageAccountType);
        return this;
    }

    @Override
    public VirtualMachineImpl withOSDiskEncryptionSettings(DiskEncryptionSettings settings) {
        this.inner().storageProfile().osDisk().withEncryptionSettings(settings);
        return this;
    }

    @Override
    public VirtualMachineImpl withOSDiskSizeInGB(Integer size) {
        this.inner().storageProfile().osDisk().withDiskSizeGB(size);
        return this;
    }

    @Override
    public VirtualMachineImpl withOSDiskSizeInGB(int size) {
        this.inner().storageProfile().osDisk().withDiskSizeGB(size);
        return this;
    }

    @Override
    public VirtualMachineImpl withOSDiskName(String name) {
        this.inner().storageProfile().osDisk().withName(name);
        return this;
    }

    // Virtual machine optional native data disk fluent methods
    @Override
    public UnmanagedDataDiskImpl defineUnmanagedDataDisk(String name) {
        throwIfManagedDiskEnabled(ManagedUnmanagedDiskErrors.VM_BOTH_MANAGED_AND_UNMANAGED_DISK_NOT_ALLOWED);
        return UnmanagedDataDiskImpl.prepareDataDisk(name, this);
    }

    @Override
    public VirtualMachineImpl withNewUnmanagedDataDisk(Integer sizeInGB) {
        throwIfManagedDiskEnabled(ManagedUnmanagedDiskErrors.VM_BOTH_MANAGED_AND_UNMANAGED_DISK_NOT_ALLOWED);
        return defineUnmanagedDataDisk(null).withNewVhd(sizeInGB).attach();
    }

    @Override
    public VirtualMachineImpl withExistingUnmanagedDataDisk(
        String storageAccountName, String containerName, String vhdName) {
        throwIfManagedDiskEnabled(ManagedUnmanagedDiskErrors.VM_BOTH_MANAGED_AND_UNMANAGED_DISK_NOT_ALLOWED);
        return defineUnmanagedDataDisk(null).withExistingVhd(storageAccountName, containerName, vhdName).attach();
    }

    @Override
    public VirtualMachineImpl withoutUnmanagedDataDisk(String name) {
        // Its ok not to throw here, since in general 'withoutXX' can be NOP
        int idx = -1;
        for (VirtualMachineUnmanagedDataDisk dataDisk : this.unmanagedDataDisks) {
            idx++;
            if (dataDisk.name().equalsIgnoreCase(name)) {
                this.unmanagedDataDisks.remove(idx);
                this.inner().storageProfile().dataDisks().remove(idx);
                break;
            }
        }
        return this;
    }

    @Override
    public VirtualMachineImpl withoutUnmanagedDataDisk(int lun) {
        // Its ok not to throw here, since in general 'withoutXX' can be NOP
        int idx = -1;
        for (VirtualMachineUnmanagedDataDisk dataDisk : this.unmanagedDataDisks) {
            idx++;
            if (dataDisk.lun() == lun) {
                this.unmanagedDataDisks.remove(idx);
                this.inner().storageProfile().dataDisks().remove(idx);
                break;
            }
        }
        return this;
    }

    @Override
    public UnmanagedDataDiskImpl updateUnmanagedDataDisk(String name) {
        throwIfManagedDiskEnabled(ManagedUnmanagedDiskErrors.VM_NO_UNMANAGED_DISK_TO_UPDATE);
        for (VirtualMachineUnmanagedDataDisk dataDisk : this.unmanagedDataDisks) {
            if (dataDisk.name().equalsIgnoreCase(name)) {
                return (UnmanagedDataDiskImpl) dataDisk;
            }
        }
        throw logger.logExceptionAsError(new RuntimeException("A data disk with name  '" + name + "' not found"));
    }

    // Virtual machine optional managed data disk fluent methods
    @Override
    public VirtualMachineImpl withNewDataDisk(Creatable<Disk> creatable) {
        throwIfManagedDiskDisabled(ManagedUnmanagedDiskErrors.VM_BOTH_UNMANAGED_AND_MANAGED_DISK_NOT_ALLOWED);
        this.managedDataDisks.newDisksToAttach.put(this.addDependency(creatable), new DataDisk().withLun(-1));
        return this;
    }

    @Override
    public VirtualMachineImpl withNewDataDisk(Creatable<Disk> creatable, int lun, CachingTypes cachingType) {
        throwIfManagedDiskDisabled(ManagedUnmanagedDiskErrors.VM_BOTH_UNMANAGED_AND_MANAGED_DISK_NOT_ALLOWED);
        this
            .managedDataDisks
            .newDisksToAttach
            .put(this.addDependency(creatable), new DataDisk().withLun(lun).withCaching(cachingType));
        return this;
    }

    @Override
    public VirtualMachineImpl withNewDataDisk(int sizeInGB) {
        throwIfManagedDiskDisabled(ManagedUnmanagedDiskErrors.VM_BOTH_UNMANAGED_AND_MANAGED_DISK_NOT_ALLOWED);
        this.managedDataDisks.implicitDisksToAssociate.add(new DataDisk().withLun(-1).withDiskSizeGB(sizeInGB));
        return this;
    }

    @Override
    public VirtualMachineImpl withNewDataDisk(int sizeInGB, int lun, CachingTypes cachingType) {
        throwIfManagedDiskDisabled(ManagedUnmanagedDiskErrors.VM_BOTH_UNMANAGED_AND_MANAGED_DISK_NOT_ALLOWED);
        this
            .managedDataDisks
            .implicitDisksToAssociate
            .add(new DataDisk().withLun(lun).withDiskSizeGB(sizeInGB).withCaching(cachingType));
        return this;
    }

    @Override
    public VirtualMachineImpl withNewDataDisk(
        int sizeInGB, int lun, CachingTypes cachingType, StorageAccountTypes storageAccountType) {
        throwIfManagedDiskDisabled(ManagedUnmanagedDiskErrors.VM_BOTH_UNMANAGED_AND_MANAGED_DISK_NOT_ALLOWED);
        ManagedDiskParameters managedDiskParameters = new ManagedDiskParameters();
        managedDiskParameters.withStorageAccountType(storageAccountType);
        this
            .managedDataDisks
            .implicitDisksToAssociate
            .add(
                new DataDisk()
                    .withLun(lun)
                    .withDiskSizeGB(sizeInGB)
                    .withCaching(cachingType)
                    .withManagedDisk(managedDiskParameters));
        return this;
    }

    @Override
    public VirtualMachineImpl withExistingDataDisk(Disk disk) {
        throwIfManagedDiskDisabled(ManagedUnmanagedDiskErrors.VM_BOTH_UNMANAGED_AND_MANAGED_DISK_NOT_ALLOWED);
        ManagedDiskParameters managedDiskParameters = new ManagedDiskParameters();
        managedDiskParameters.withId(disk.id());
        this
            .managedDataDisks
            .existingDisksToAttach
            .add(new DataDisk().withLun(-1).withManagedDisk(managedDiskParameters));
        return this;
    }

    @Override
    public VirtualMachineImpl withExistingDataDisk(Disk disk, int lun, CachingTypes cachingType) {
        throwIfManagedDiskDisabled(ManagedUnmanagedDiskErrors.VM_BOTH_UNMANAGED_AND_MANAGED_DISK_NOT_ALLOWED);
        ManagedDiskParameters managedDiskParameters = new ManagedDiskParameters();
        managedDiskParameters.withId(disk.id());
        this
            .managedDataDisks
            .existingDisksToAttach
            .add(new DataDisk().withLun(lun).withManagedDisk(managedDiskParameters).withCaching(cachingType));
        return this;
    }

    @Override
    public VirtualMachineImpl withExistingDataDisk(Disk disk, int newSizeInGB, int lun, CachingTypes cachingType) {
        throwIfManagedDiskDisabled(ManagedUnmanagedDiskErrors.VM_BOTH_UNMANAGED_AND_MANAGED_DISK_NOT_ALLOWED);
        ManagedDiskParameters managedDiskParameters = new ManagedDiskParameters();
        managedDiskParameters.withId(disk.id());
        this
            .managedDataDisks
            .existingDisksToAttach
            .add(
                new DataDisk()
                    .withLun(lun)
                    .withDiskSizeGB(newSizeInGB)
                    .withManagedDisk(managedDiskParameters)
                    .withCaching(cachingType));
        return this;
    }

    @Override
    public VirtualMachineImpl withNewDataDiskFromImage(int imageLun) {
        this.managedDataDisks.newDisksFromImage.add(new DataDisk().withLun(imageLun));
        return this;
    }

    @Override
    public VirtualMachineImpl withNewDataDiskFromImage(int imageLun, int newSizeInGB, CachingTypes cachingType) {
        this
            .managedDataDisks
            .newDisksFromImage
            .add(new DataDisk().withLun(imageLun).withDiskSizeGB(newSizeInGB).withCaching(cachingType));
        return this;
    }

    @Override
    public VirtualMachineImpl withNewDataDiskFromImage(
        int imageLun, int newSizeInGB, CachingTypes cachingType, StorageAccountTypes storageAccountType) {
        ManagedDiskParameters managedDiskParameters = new ManagedDiskParameters();
        managedDiskParameters.withStorageAccountType(storageAccountType);
        this
            .managedDataDisks
            .newDisksFromImage
            .add(
                new DataDisk()
                    .withLun(imageLun)
                    .withDiskSizeGB(newSizeInGB)
                    .withManagedDisk(managedDiskParameters)
                    .withCaching(cachingType));
        return this;
    }

    @Override
    public VirtualMachineImpl withoutDataDisk(int lun) {
        if (!isManagedDiskEnabled()) {
            return this;
        }
        this.managedDataDisks.diskLunsToRemove.add(lun);
        return this;
    }

    // Virtual machine optional storage account fluent methods
    @Override
    public VirtualMachineImpl withNewStorageAccount(Creatable<StorageAccount> creatable) {
        // This method's effect is NOT additive.
        if (this.creatableStorageAccountKey == null) {
            this.creatableStorageAccountKey = this.addDependency(creatable);
        }
        return this;
    }

    @Override
    public VirtualMachineImpl withNewStorageAccount(String name) {
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
    public VirtualMachineImpl withExistingStorageAccount(StorageAccount storageAccount) {
        this.existingStorageAccountToAssociate = storageAccount;
        return this;
    }

    // Virtual machine optional availability set fluent methods
    @Override
    public VirtualMachineImpl withNewAvailabilitySet(Creatable<AvailabilitySet> creatable) {
        // This method's effect is NOT additive.
        if (this.creatableAvailabilitySetKey == null) {
            this.creatableAvailabilitySetKey = this.addDependency(creatable);
        }
        return this;
    }

    @Override
    public VirtualMachineImpl withProximityPlacementGroup(String proximityPlacementGroupId) {
        this.inner().withProximityPlacementGroup(new SubResource().withId(proximityPlacementGroupId));
        // clear the new setting
        newProximityPlacementGroupName = null;
        return this;
    }

    @Override
    public VirtualMachineImpl withNewProximityPlacementGroup(
        String proximityPlacementGroupName, ProximityPlacementGroupType type) {
        this.newProximityPlacementGroupName = proximityPlacementGroupName;
        this.newProximityPlacementGroupType = type;
        this.inner().withProximityPlacementGroup(null);
        return this;
    }

    @Override
    public VirtualMachineImpl withoutProximityPlacementGroup() {
        this.inner().withProximityPlacementGroup(null);

        return this;
    }

    @Override
    public VirtualMachineImpl withNewAvailabilitySet(String name) {
        AvailabilitySet.DefinitionStages.WithGroup definitionWithGroup =
            super.myManager.availabilitySets().define(name).withRegion(this.regionName());
        AvailabilitySet.DefinitionStages.WithSku definitionWithSku;
        if (this.creatableGroup != null) {
            definitionWithSku = definitionWithGroup.withNewResourceGroup(this.creatableGroup);
        } else {
            definitionWithSku = definitionWithGroup.withExistingResourceGroup(this.resourceGroupName());
        }
        Creatable<AvailabilitySet> creatable;
        if (isManagedDiskEnabled()) {
            creatable = definitionWithSku.withSku(AvailabilitySetSkuTypes.ALIGNED);
        } else {
            creatable = definitionWithSku.withSku(AvailabilitySetSkuTypes.CLASSIC);
        }
        return withNewAvailabilitySet(creatable);
    }

    @Override
    public VirtualMachineImpl withExistingAvailabilitySet(AvailabilitySet availabilitySet) {
        this.existingAvailabilitySetToAssociate = availabilitySet;
        return this;
    }

    @Override
    public VirtualMachineImpl withNewSecondaryNetworkInterface(Creatable<NetworkInterface> creatable) {
        this.creatableSecondaryNetworkInterfaceKeys.add(this.addDependency(creatable));
        return this;
    }

    @Override
    public VirtualMachineImpl withExistingSecondaryNetworkInterface(NetworkInterface networkInterface) {
        this.existingSecondaryNetworkInterfacesToAssociate.add(networkInterface);
        return this;
    }

    // Virtual machine optional extension settings
    @Override
    public VirtualMachineExtensionImpl defineNewExtension(String name) {
        return this.virtualMachineExtensions.define(name);
    }

    @Override
    public VirtualMachineImpl withoutSecondaryNetworkInterface(String name) {
        if (this.inner().networkProfile() != null && this.inner().networkProfile().networkInterfaces() != null) {
            int idx = -1;
            for (NetworkInterfaceReference nicReference : this.inner().networkProfile().networkInterfaces()) {
                idx++;
                if (!nicReference.primary()
                    && name.equalsIgnoreCase(ResourceUtils.nameFromResourceId(nicReference.id()))) {
                    this.inner().networkProfile().networkInterfaces().remove(idx);
                    break;
                }
            }
        }
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl updateExtension(String name) {
        return this.virtualMachineExtensions.update(name);
    }

    @Override
    public VirtualMachineImpl withoutExtension(String name) {
        this.virtualMachineExtensions.remove(name);
        return this;
    }

    @Override
    public VirtualMachineImpl withPlan(PurchasePlan plan) {
        this.inner().withPlan(new Plan());
        this.inner().plan().withPublisher(plan.publisher()).withProduct(plan.product()).withName(plan.name());
        return this;
    }

    @Override
    public VirtualMachineImpl withPromotionalPlan(PurchasePlan plan, String promotionCode) {
        this.withPlan(plan);
        this.inner().plan().withPromotionCode(promotionCode);
        return this;
    }

    @Override
    public VirtualMachineImpl withUnmanagedDisks() {
        this.isUnmanagedDiskSelected = true;
        return this;
    }

    @Override
    public VirtualMachineImpl withBootDiagnostics() {
        this.bootDiagnosticsHandler.withBootDiagnostics();
        return this;
    }

    @Override
    public VirtualMachineImpl withBootDiagnostics(Creatable<StorageAccount> creatable) {
        this.bootDiagnosticsHandler.withBootDiagnostics(creatable);
        return this;
    }

    @Override
    public VirtualMachineImpl withBootDiagnostics(String storageAccountBlobEndpointUri) {
        this.bootDiagnosticsHandler.withBootDiagnostics(storageAccountBlobEndpointUri);
        return this;
    }

    @Override
    public VirtualMachineImpl withBootDiagnostics(StorageAccount storageAccount) {
        this.bootDiagnosticsHandler.withBootDiagnostics(storageAccount);
        return this;
    }

    @Override
    public VirtualMachineImpl withoutBootDiagnostics() {
        this.bootDiagnosticsHandler.withoutBootDiagnostics();
        return this;
    }

    @Override
    public VirtualMachineImpl withPriority(VirtualMachinePriorityTypes priority) {
        this.inner().withPriority(priority);
        return this;
    }

    @Override
    public VirtualMachineImpl withLowPriority() {
        this.withPriority(VirtualMachinePriorityTypes.LOW);
        return this;
    }

    @Override
    public VirtualMachineImpl withLowPriority(VirtualMachineEvictionPolicyTypes policy) {
        this.withLowPriority();
        this.inner().withEvictionPolicy(policy);
        return this;
    }

    @Override
    public VirtualMachineImpl withSpotPriority() {
        this.withPriority(VirtualMachinePriorityTypes.SPOT);
        return this;
    }

    @Override
    public VirtualMachineImpl withSpotPriority(VirtualMachineEvictionPolicyTypes policy) {
        this.withSpotPriority();
        this.inner().withEvictionPolicy(policy);
        return this;
    }

    @Override
    public VirtualMachineImpl withMaxPrice(Double maxPrice) {
        this.inner().withBillingProfile(new BillingProfile().withMaxPrice(maxPrice));
        return this;
    }

    @Override
    public VirtualMachineImpl withSystemAssignedManagedServiceIdentity() {
        this.virtualMachineMsiHandler.withLocalManagedServiceIdentity();
        return this;
    }

    @Override
    public VirtualMachineImpl withoutSystemAssignedManagedServiceIdentity() {
        this.virtualMachineMsiHandler.withoutLocalManagedServiceIdentity();
        return this;
    }

    @Override
    public VirtualMachineImpl withSystemAssignedIdentityBasedAccessTo(String resourceId, BuiltInRole role) {
        this.virtualMachineMsiHandler.withAccessTo(resourceId, role);
        return this;
    }

    @Override
    public VirtualMachineImpl withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(BuiltInRole role) {
        this.virtualMachineMsiHandler.withAccessToCurrentResourceGroup(role);
        return this;
    }

    @Override
    public VirtualMachineImpl withSystemAssignedIdentityBasedAccessTo(String resourceId, String roleDefinitionId) {
        this.virtualMachineMsiHandler.withAccessTo(resourceId, roleDefinitionId);
        return this;
    }

    @Override
    public VirtualMachineImpl withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(String roleDefinitionId) {
        this.virtualMachineMsiHandler.withAccessToCurrentResourceGroup(roleDefinitionId);
        return this;
    }

    @Override
    public VirtualMachineImpl withNewUserAssignedManagedServiceIdentity(Creatable<Identity> creatableIdentity) {
        this.virtualMachineMsiHandler.withNewExternalManagedServiceIdentity(creatableIdentity);
        return this;
    }

    @Override
    public VirtualMachineImpl withExistingUserAssignedManagedServiceIdentity(Identity identity) {
        this.virtualMachineMsiHandler.withExistingExternalManagedServiceIdentity(identity);
        return this;
    }

    @Override
    public VirtualMachineImpl withoutUserAssignedManagedServiceIdentity(String identityId) {
        this.virtualMachineMsiHandler.withoutExternalManagedServiceIdentity(identityId);
        return this;
    }

    @Override
    public VirtualMachineImpl withLicenseType(String licenseType) {
        inner().withLicenseType(licenseType);
        return this;
    }

    // GETTERS
    @Override
    public boolean isManagedDiskEnabled() {
        if (isOsDiskFromCustomImage(this.inner().storageProfile())) {
            return true;
        }
        if (isOSDiskAttachedManaged(this.inner().storageProfile().osDisk())) {
            return true;
        }
        if (isOSDiskFromStoredImage(this.inner().storageProfile())) {
            return false;
        }
        if (isOSDiskAttachedUnmanaged(this.inner().storageProfile().osDisk())) {
            return false;
        }
        if (isOSDiskFromPlatformImage(this.inner().storageProfile())) {
            if (this.isUnmanagedDiskSelected) {
                return false;
            }
        }
        if (isInCreateMode()) {
            return true;
        } else {
            return this.inner().storageProfile().osDisk().vhd() == null;
        }
    }

    @Override
    public String computerName() {
        if (inner().osProfile() == null) {
            // VM created by attaching a specialized OS Disk VHD will not have the osProfile.
            return null;
        }
        return inner().osProfile().computerName();
    }

    @Override
    public VirtualMachineSizeTypes size() {
        return inner().hardwareProfile().vmSize();
    }

    @Override
    public OperatingSystemTypes osType() {
        if (inner().storageProfile().osDisk().osType() != null) {
            return inner().storageProfile().osDisk().osType();
        }
        if (inner().osProfile() != null) {
            if (inner().osProfile().linuxConfiguration() != null) {
                return OperatingSystemTypes.LINUX;
            }
            if (inner().osProfile().windowsConfiguration() != null) {
                return OperatingSystemTypes.WINDOWS;
            }
        }
        return null;
    }

    @Override
    public String osUnmanagedDiskVhdUri() {
        if (isManagedDiskEnabled()) {
            return null;
        }
        return inner().storageProfile().osDisk().vhd().uri();
    }

    @Override
    public CachingTypes osDiskCachingType() {
        return inner().storageProfile().osDisk().caching();
    }

    @Override
    public int osDiskSize() {
        return Utils.toPrimitiveInt(inner().storageProfile().osDisk().diskSizeGB());
    }

    @Override
    public StorageAccountTypes osDiskStorageAccountType() {
        if (!isManagedDiskEnabled() || this.storageProfile().osDisk().managedDisk() == null) {
            return null;
        }
        return this.storageProfile().osDisk().managedDisk().storageAccountType();
    }

    @Override
    public String osDiskId() {
        if (!isManagedDiskEnabled()) {
            return null;
        }
        return this.storageProfile().osDisk().managedDisk().id();
    }

    @Override
    public Map<Integer, VirtualMachineUnmanagedDataDisk> unmanagedDataDisks() {
        Map<Integer, VirtualMachineUnmanagedDataDisk> dataDisks = new HashMap<>();
        if (!isManagedDiskEnabled()) {
            for (VirtualMachineUnmanagedDataDisk dataDisk : this.unmanagedDataDisks) {
                dataDisks.put(dataDisk.lun(), dataDisk);
            }
        }
        return Collections.unmodifiableMap(dataDisks);
    }

    @Override
    public Map<Integer, VirtualMachineDataDisk> dataDisks() {
        Map<Integer, VirtualMachineDataDisk> dataDisks = new HashMap<>();
        if (isManagedDiskEnabled()) {
            List<DataDisk> innerDataDisks = this.inner().storageProfile().dataDisks();
            if (innerDataDisks != null) {
                for (DataDisk innerDataDisk : innerDataDisks) {
                    dataDisks.put(innerDataDisk.lun(), new VirtualMachineDataDiskImpl(innerDataDisk));
                }
            }
        }
        return Collections.unmodifiableMap(dataDisks);
    }

    @Override
    public NetworkInterface getPrimaryNetworkInterface() {
        return this.networkManager.networkInterfaces().getById(primaryNetworkInterfaceId());
    }

    @Override
    public PublicIpAddress getPrimaryPublicIPAddress() {
        return this.getPrimaryNetworkInterface().primaryIPConfiguration().getPublicIpAddress();
    }

    @Override
    public String getPrimaryPublicIPAddressId() {
        return this.getPrimaryNetworkInterface().primaryIPConfiguration().publicIpAddressId();
    }

    @Override
    public List<String> networkInterfaceIds() {
        List<String> nicIds = new ArrayList<>();
        for (NetworkInterfaceReference nicRef : inner().networkProfile().networkInterfaces()) {
            nicIds.add(nicRef.id());
        }
        return nicIds;
    }

    @Override
    public String primaryNetworkInterfaceId() {
        final List<NetworkInterfaceReference> nicRefs = this.inner().networkProfile().networkInterfaces();
        String primaryNicRefId = null;
        if (nicRefs.size() == 1) {
            // One NIC so assume it to be primary
            primaryNicRefId = nicRefs.get(0).id();
        } else if (nicRefs.size() == 0) {
            // No NICs so null
            primaryNicRefId = null;
        } else {
            // Find primary interface as flagged by Azure
            for (NetworkInterfaceReference nicRef : inner().networkProfile().networkInterfaces()) {
                if (nicRef.primary() != null && nicRef.primary()) {
                    primaryNicRefId = nicRef.id();
                    break;
                }
            }
            // If Azure didn't flag any NIC as primary then assume the first one
            if (primaryNicRefId == null) {
                primaryNicRefId = nicRefs.get(0).id();
            }
        }
        return primaryNicRefId;
    }

    @Override
    public String availabilitySetId() {
        if (inner().availabilitySet() != null) {
            return inner().availabilitySet().id();
        }
        return null;
    }

    @Override
    public String provisioningState() {
        return inner().provisioningState();
    }

    @Override
    public String licenseType() {
        return inner().licenseType();
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
    public Mono<List<VirtualMachineExtension>> listExtensionsAsync() {
        return this.virtualMachineExtensions.listAsync();
    }

    @Override
    public Map<String, VirtualMachineExtension> listExtensions() {
        return this.virtualMachineExtensions.asMap();
    }

    @Override
    public Plan plan() {
        return inner().plan();
    }

    @Override
    public StorageProfile storageProfile() {
        return inner().storageProfile();
    }

    @Override
    public OSProfile osProfile() {
        return inner().osProfile();
    }

    @Override
    public DiagnosticsProfile diagnosticsProfile() {
        return inner().diagnosticsProfile();
    }

    @Override
    public String vmId() {
        return inner().vmId();
    }

    @Override
    public VirtualMachineInstanceView instanceView() {
        if (this.virtualMachineInstanceView == null) {
            this.refreshInstanceView();
        }
        return this.virtualMachineInstanceView;
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
    public PowerState powerState() {
        return PowerState.fromInstanceView(this.instanceView());
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
    public BillingProfile billingProfile() {
        return this.inner().billingProfile();
    }

    @Override
    public VirtualMachinePriorityTypes priority() {
        return this.inner().priority();
    }

    @Override
    public VirtualMachineEvictionPolicyTypes evictionPolicy() {
        return this.inner().evictionPolicy();
    }

    // CreateUpdateTaskGroup.ResourceCreator.beforeGroupCreateOrUpdate implementation
    @Override
    public void beforeGroupCreateOrUpdate() {
        // [1]. StorageProfile: If implicit storage account creation is required then add Creatable<StorageAccount>.
        if (creatableStorageAccountKey == null && existingStorageAccountToAssociate == null) {
            if (osDiskRequiresImplicitStorageAccountCreation() || dataDisksRequiresImplicitStorageAccountCreation()) {
                Creatable<StorageAccount> storageAccountCreatable = null;
                if (this.creatableGroup != null) {
                    storageAccountCreatable =
                        this
                            .storageManager
                            .storageAccounts()
                            .define(this.namer.randomName("stg", 24).replace("-", ""))
                            .withRegion(this.regionName())
                            .withNewResourceGroup(this.creatableGroup);
                } else {
                    storageAccountCreatable =
                        this
                            .storageManager
                            .storageAccounts()
                            .define(this.namer.randomName("stg", 24).replace("-", ""))
                            .withRegion(this.regionName())
                            .withExistingResourceGroup(this.resourceGroupName());
                }
                this.creatableStorageAccountKey = this.addDependency(storageAccountCreatable);
            }
        }
        // [2]. BootDiagnosticsProfile: If any implicit resource creation is required then add Creatable<?>.
        this.bootDiagnosticsHandler.prepare();
    }

    // [2]. CreateUpdateTaskGroup.ResourceCreator.createResourceAsync implementation
    @Override
    public Mono<VirtualMachine> createResourceAsync() {
        // -- set creation-time only properties
        return prepareCreateResourceAsync()
            .flatMap(virtualMachine -> this.manager().inner().getVirtualMachines()
                .createOrUpdateAsync(resourceGroupName(), vmName, inner())
                .map(virtualMachineInner -> {
                    reset(virtualMachineInner);
                    return this;
                }));
    }

    private Mono<VirtualMachine> prepareCreateResourceAsync() {
        setOSDiskDefaults();
        setOSProfileDefaults();
        setHardwareProfileDefaults();
        if (isManagedDiskEnabled()) {
            managedDataDisks.setDataDisksDefaults();
        } else {
            UnmanagedDataDiskImpl.setDataDisksDefaults(this.unmanagedDataDisks, this.vmName);
        }
        this.handleUnManagedOSAndDataDisksStorageSettings();
        this.bootDiagnosticsHandler.handleDiagnosticsSettings();
        this.handleNetworkSettings();
        return this
            .createNewProximityPlacementGroupAsync()
            .map(
                virtualMachine -> {
                    this.handleAvailabilitySettings();
                    this.virtualMachineMsiHandler.processCreatedExternalIdentities();
                    this.virtualMachineMsiHandler.handleExternalIdentities();
                    return virtualMachine;
                });
    }

    public Accepted<VirtualMachine> beginCreate() {
        return AcceptedImpl.<VirtualMachine, VirtualMachineInner>newAccepted(logger,
            this.manager().inner(),
            () -> this.manager().inner().getVirtualMachines()
                .createOrUpdateWithResponseAsync(resourceGroupName(), vmName, inner()).block(),
            inner -> new VirtualMachineImpl(inner.name(), inner, this.manager(),
                this.storageManager, this.networkManager, this.authorizationManager),
            VirtualMachineInner.class,
            () -> {
                Flux<Indexable> dependencyTasksAsync =
                    taskGroup().invokeDependencyAsync(taskGroup().newInvocationContext());
                dependencyTasksAsync.blockLast();

                // same as createResourceAsync
                prepareCreateResourceAsync().block();
            },
            this::reset);
    }

    @Override
    public Mono<VirtualMachine> updateResourceAsync() {
        if (isManagedDiskEnabled()) {
            managedDataDisks.setDataDisksDefaults();
        } else {
            UnmanagedDataDiskImpl.setDataDisksDefaults(this.unmanagedDataDisks, this.vmName);
        }
        this.handleUnManagedOSAndDataDisksStorageSettings();
        this.bootDiagnosticsHandler.handleDiagnosticsSettings();
        this.handleNetworkSettings();
        this.handleAvailabilitySettings();
        this.virtualMachineMsiHandler.processCreatedExternalIdentities();

        VirtualMachineUpdateInner updateParameter = new VirtualMachineUpdateInner();
        updateParameter.withPlan(this.inner().plan());
        updateParameter.withHardwareProfile(this.inner().hardwareProfile());
        updateParameter.withStorageProfile(this.inner().storageProfile());
        updateParameter.withOsProfile(this.inner().osProfile());
        updateParameter.withNetworkProfile(this.inner().networkProfile());
        updateParameter.withDiagnosticsProfile(this.inner().diagnosticsProfile());
        updateParameter.withBillingProfile(this.inner().billingProfile());
        updateParameter.withAvailabilitySet(this.inner().availabilitySet());
        updateParameter.withLicenseType(this.inner().licenseType());
        updateParameter.withZones(this.inner().zones());
        updateParameter.withTags(this.inner().tags());
        updateParameter.withProximityPlacementGroup(this.inner().proximityPlacementGroup());
        updateParameter.withPriority(this.inner().priority());
        this.virtualMachineMsiHandler.handleExternalIdentities(updateParameter);

        final VirtualMachineImpl self = this;
        return this
            .manager()
            .inner()
            .getVirtualMachines()
            .updateAsync(resourceGroupName(), vmName, updateParameter)
            .map(
                virtualMachineInner -> {
                    reset(virtualMachineInner);
                    return self;
                });
    }

    // CreateUpdateTaskGroup.ResourceCreator.afterPostRunAsync implementation
    @Override
    public Mono<Void> afterPostRunAsync(boolean isGroupFaulted) {
        this.virtualMachineExtensions.clear();
        if (isGroupFaulted) {
            return Mono.empty();
        } else {
            return this.refreshAsync().then();
        }
    }

    // Helpers
    VirtualMachineImpl withExtension(VirtualMachineExtensionImpl extension) {
        this.virtualMachineExtensions.addExtension(extension);
        return this;
    }

    private void reset(VirtualMachineInner inner) {
        this.setInner(inner);
        clearCachedRelatedResources();
        initializeDataDisks();
        virtualMachineMsiHandler.clear();
    }

    VirtualMachineImpl withUnmanagedDataDisk(UnmanagedDataDiskImpl dataDisk) {
        this.inner().storageProfile().dataDisks().add(dataDisk.inner());
        this.unmanagedDataDisks.add(dataDisk);
        return this;
    }

    @Override
    public VirtualMachineImpl withAvailabilityZone(AvailabilityZoneId zoneId) {
        if (isInCreateMode()) {
            // Note: Zone is not updatable as of now, so this is available only during definition time.
            // Service return `ResourceAvailabilityZonesCannotBeModified` upon attempt to append a new
            // zone or remove one. Trying to remove the last one means attempt to change resource from
            // zonal to regional, which is not supported.
            // though not updatable, still adding above 'isInCreateMode' check just as a reminder to
            // take special handling of 'implicitPipCreatable' when avail zone update is supported.
            if (this.inner().zones() == null) {
                this.inner().withZones(new ArrayList<String>());
            }
            this.inner().zones().add(zoneId.toString());
            // zone aware VM can be attached to only zone aware public IP.
            if (this.implicitPipCreatable != null) {
                this.implicitPipCreatable.withAvailabilityZone(zoneId);
            }
        }
        return this;
    }

    AzureEnvironment environment() {
        return manager().environment();
    }

    private void setOSDiskDefaults() {
        if (isInUpdateMode()) {
            return;
        }
        StorageProfile storageProfile = this.inner().storageProfile();
        OSDisk osDisk = storageProfile.osDisk();
        if (isOSDiskFromImage(osDisk)) {
            // ODDisk CreateOption: FROM_IMAGE
            if (isManagedDiskEnabled()) {
                // Note:
                // Managed disk
                //     Supported: PlatformImage and CustomImage
                //     UnSupported: StoredImage
                if (osDisk.managedDisk() == null) {
                    osDisk.withManagedDisk(new ManagedDiskParameters());
                }
                if (osDisk.managedDisk().storageAccountType() == null) {
                    osDisk.managedDisk().withStorageAccountType(StorageAccountTypes.STANDARD_LRS);
                }
                osDisk.withVhd(null);
                // We won't set osDisk.name() explicitly for managed disk, if it is null CRP generates unique
                // name for the disk resource within the resource group.
            } else {
                // Note:
                // Native (un-managed) disk
                //     Supported: PlatformImage and StoredImage
                //     UnSupported: CustomImage
                if (isOSDiskFromPlatformImage(storageProfile) || isOSDiskFromStoredImage(storageProfile)) {
                    if (osDisk.vhd() == null) {
                        String osDiskVhdContainerName = "vhds";
                        String osDiskVhdName = this.vmName + "-os-disk-" + UUID.randomUUID().toString() + ".vhd";
                        withOSDiskVhdLocation(osDiskVhdContainerName, osDiskVhdName);
                    }
                    osDisk.withManagedDisk(null);
                }
                if (osDisk.name() == null) {
                    withOSDiskName(this.vmName + "-os-disk");
                }
            }
        } else {
            // ODDisk CreateOption: ATTACH
            if (isManagedDiskEnabled()) {
                // In case of attach, it is not allowed to change the storage account type of the
                // managed disk.
                if (osDisk.managedDisk() != null) {
                    osDisk.managedDisk().withStorageAccountType(null);
                }
                osDisk.withVhd(null);
            } else {
                osDisk.withManagedDisk(null);
                if (osDisk.name() == null) {
                    withOSDiskName(this.vmName + "-os-disk");
                }
            }
        }
        if (osDisk.caching() == null) {
            withOSDiskCaching(CachingTypes.READ_WRITE);
        }
    }

    private void setOSProfileDefaults() {
        if (isInUpdateMode()) {
            return;
        }
        StorageProfile storageProfile = this.inner().storageProfile();
        OSDisk osDisk = storageProfile.osDisk();
        if (!removeOsProfile && isOSDiskFromImage(osDisk)) {
            // ODDisk CreateOption: FROM_IMAGE
            if (osDisk.osType() == OperatingSystemTypes.LINUX || this.isMarketplaceLinuxImage) {
                // linux image: PlatformImage | CustomImage | StoredImage
                OSProfile osProfile = this.inner().osProfile();
                if (osProfile.linuxConfiguration() == null) {
                    osProfile.withLinuxConfiguration(new LinuxConfiguration());
                }
                this
                    .inner()
                    .osProfile()
                    .linuxConfiguration()
                    .withDisablePasswordAuthentication(osProfile.adminPassword() == null);
            }
            if (this.inner().osProfile().computerName() == null) {
                // VM name cannot contain only numeric values and cannot exceed 15 chars
                if (vmName.matches("[0-9]+")) {
                    this.inner().osProfile().withComputerName(namer.randomName("vm", 15));
                } else if (vmName.length() <= 15) {
                    this.inner().osProfile().withComputerName(vmName);
                } else {
                    this.inner().osProfile().withComputerName(namer.randomName("vm", 15));
                }
            }
        } else {
            // ODDisk CreateOption: ATTACH
            //
            // OS Profile must be set to null when an VM's OS disk is ATTACH-ed to a managed disk or
            // Specialized VHD
            this.inner().withOsProfile(null);
        }
    }

    private void setHardwareProfileDefaults() {
        if (!isInCreateMode()) {
            return;
        }
        HardwareProfile hardwareProfile = this.inner().hardwareProfile();
        if (hardwareProfile.vmSize() == null) {
            hardwareProfile.withVmSize(VirtualMachineSizeTypes.BASIC_A0);
        }
    }

    /** Prepare virtual machine disks profile (StorageProfile). */
    private void handleUnManagedOSAndDataDisksStorageSettings() {
        if (isManagedDiskEnabled()) {
            // NOP if the virtual machine is based on managed disk (managed and un-managed disk cannot be mixed)
            return;
        }
        StorageAccount storageAccount = null;
        if (this.creatableStorageAccountKey != null) {
            storageAccount = this.taskResult(this.creatableStorageAccountKey);
        } else if (this.existingStorageAccountToAssociate != null) {
            storageAccount = this.existingStorageAccountToAssociate;
        }
        if (isInCreateMode()) {
            if (storageAccount != null) {
                if (isOSDiskFromPlatformImage(inner().storageProfile())) {
                    String uri =
                        inner()
                            .storageProfile()
                            .osDisk()
                            .vhd()
                            .uri()
                            .replaceFirst("\\{storage-base-url}", storageAccount.endPoints().primary().blob());
                    inner().storageProfile().osDisk().vhd().withUri(uri);
                }
                UnmanagedDataDiskImpl.ensureDisksVhdUri(unmanagedDataDisks, storageAccount, vmName);
            }
        } else { // Update Mode
            if (storageAccount != null) {
                UnmanagedDataDiskImpl.ensureDisksVhdUri(unmanagedDataDisks, storageAccount, vmName);
            } else {
                UnmanagedDataDiskImpl.ensureDisksVhdUri(unmanagedDataDisks, vmName);
            }
        }
    }

    private Mono<VirtualMachineImpl> createNewProximityPlacementGroupAsync() {
        if (isInCreateMode()) {
            if (this.newProximityPlacementGroupName != null && !this.newProximityPlacementGroupName.isEmpty()) {
                ProximityPlacementGroupInner plgInner = new ProximityPlacementGroupInner();
                plgInner.withProximityPlacementGroupType(this.newProximityPlacementGroupType);
                plgInner.withLocation(this.inner().location());
                return this
                    .manager()
                    .inner()
                    .getProximityPlacementGroups()
                    .createOrUpdateAsync(this.resourceGroupName(), this.newProximityPlacementGroupName, plgInner)
                    .map(
                        createdPlgInner -> {
                            this.inner().withProximityPlacementGroup(new SubResource().withId(createdPlgInner.id()));
                            return this;
                        });
            }
        }
        return Mono.just(this);
    }

    private void handleNetworkSettings() {
        if (isInCreateMode()) {
            NetworkInterface primaryNetworkInterface = null;
            if (this.creatablePrimaryNetworkInterfaceKey != null) {
                primaryNetworkInterface = this.taskResult(this.creatablePrimaryNetworkInterfaceKey);
            } else if (this.existingPrimaryNetworkInterfaceToAssociate != null) {
                primaryNetworkInterface = this.existingPrimaryNetworkInterfaceToAssociate;
            }

            if (primaryNetworkInterface != null) {
                NetworkInterfaceReference nicReference = new NetworkInterfaceReference();
                nicReference.withPrimary(true);
                nicReference.withId(primaryNetworkInterface.id());
                this.inner().networkProfile().networkInterfaces().add(nicReference);
            }
        }

        // sets the virtual machine secondary network interfaces
        //
        for (String creatableSecondaryNetworkInterfaceKey : this.creatableSecondaryNetworkInterfaceKeys) {
            NetworkInterface secondaryNetworkInterface =
                this.taskResult(creatableSecondaryNetworkInterfaceKey);
            NetworkInterfaceReference nicReference = new NetworkInterfaceReference();
            nicReference.withPrimary(false);
            nicReference.withId(secondaryNetworkInterface.id());
            this.inner().networkProfile().networkInterfaces().add(nicReference);
        }

        for (NetworkInterface secondaryNetworkInterface : this.existingSecondaryNetworkInterfacesToAssociate) {
            NetworkInterfaceReference nicReference = new NetworkInterfaceReference();
            nicReference.withPrimary(false);
            nicReference.withId(secondaryNetworkInterface.id());
            this.inner().networkProfile().networkInterfaces().add(nicReference);
        }
    }

    private void handleAvailabilitySettings() {
        if (!isInCreateMode()) {
            return;
        }

        AvailabilitySet availabilitySet = null;
        if (this.creatableAvailabilitySetKey != null) {
            availabilitySet = this.taskResult(this.creatableAvailabilitySetKey);
        } else if (this.existingAvailabilitySetToAssociate != null) {
            availabilitySet = this.existingAvailabilitySetToAssociate;
        }

        if (availabilitySet != null) {
            if (this.inner().availabilitySet() == null) {
                this.inner().withAvailabilitySet(new SubResource());
            }

            this.inner().availabilitySet().withId(availabilitySet.id());
        }
    }

    private boolean osDiskRequiresImplicitStorageAccountCreation() {
        if (isManagedDiskEnabled()) {
            return false;
        }
        if (this.creatableStorageAccountKey != null
            || this.existingStorageAccountToAssociate != null
            || !isInCreateMode()) {
            return false;
        }
        return isOSDiskFromPlatformImage(this.inner().storageProfile());
    }

    private boolean dataDisksRequiresImplicitStorageAccountCreation() {
        if (isManagedDiskEnabled()) {
            return false;
        }
        if (this.creatableStorageAccountKey != null
            || this.existingStorageAccountToAssociate != null
            || this.unmanagedDataDisks.size() == 0) {
            return false;
        }
        boolean hasEmptyVhd = false;
        for (VirtualMachineUnmanagedDataDisk dataDisk : this.unmanagedDataDisks) {
            if (dataDisk.creationMethod() == DiskCreateOptionTypes.EMPTY
                || dataDisk.creationMethod() == DiskCreateOptionTypes.FROM_IMAGE) {
                if (dataDisk.inner().vhd() == null) {
                    hasEmptyVhd = true;
                    break;
                }
            }
        }
        if (isInCreateMode()) {
            return hasEmptyVhd;
        }
        if (hasEmptyVhd) {
            // In update mode, if any of the data disk has vhd uri set then use same container
            // to store this disk, no need to create a storage account implicitly.
            for (VirtualMachineUnmanagedDataDisk dataDisk : this.unmanagedDataDisks) {
                if (dataDisk.creationMethod() == DiskCreateOptionTypes.ATTACH && dataDisk.inner().vhd() != null) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Checks whether the OS disk is directly attached to a unmanaged VHD.
     *
     * @param osDisk the osDisk value in the storage profile
     * @return true if the OS disk is attached to a unmanaged VHD, false otherwise
     */
    private boolean isOSDiskAttachedUnmanaged(OSDisk osDisk) {
        return osDisk.createOption() == DiskCreateOptionTypes.ATTACH
            && osDisk.vhd() != null
            && osDisk.vhd().uri() != null;
    }

    /**
     * Checks whether the OS disk is directly attached to a managed disk.
     *
     * @param osDisk the osDisk value in the storage profile
     * @return true if the OS disk is attached to a managed disk, false otherwise
     */
    private boolean isOSDiskAttachedManaged(OSDisk osDisk) {
        return osDisk.createOption() == DiskCreateOptionTypes.ATTACH
            && osDisk.managedDisk() != null
            && osDisk.managedDisk().id() != null;
    }

    /**
     * Checks whether the OS disk is based on an image (image from PIR or custom image [captured, bringYourOwnFeature]).
     *
     * @param osDisk the osDisk value in the storage profile
     * @return true if the OS disk is configured to use image from PIR or custom image
     */
    private boolean isOSDiskFromImage(OSDisk osDisk) {
        return osDisk.createOption() == DiskCreateOptionTypes.FROM_IMAGE;
    }

    /**
     * Checks whether the OS disk is based on an platform image (image in PIR).
     *
     * @param storageProfile the storage profile
     * @return true if the OS disk is configured to be based on platform image.
     */
    private boolean isOSDiskFromPlatformImage(StorageProfile storageProfile) {
        ImageReference imageReference = storageProfile.imageReference();
        return isOSDiskFromImage(storageProfile.osDisk())
            && imageReference != null
            && imageReference.publisher() != null
            && imageReference.offer() != null
            && imageReference.sku() != null
            && imageReference.version() != null;
    }

    /**
     * Checks whether the OS disk is based on a CustomImage.
     *
     * <p>A custom image is represented by {@link VirtualMachineCustomImage}.
     *
     * @param storageProfile the storage profile
     * @return true if the OS disk is configured to be based on custom image.
     */
    private boolean isOsDiskFromCustomImage(StorageProfile storageProfile) {
        ImageReference imageReference = storageProfile.imageReference();
        return isOSDiskFromImage(storageProfile.osDisk()) && imageReference != null && imageReference.id() != null;
    }

    /**
     * Checks whether the OS disk is based on a stored image ('captured' or 'bring your own feature').
     *
     * <p>A stored image is created by calling {@link VirtualMachine#capture(String, String, boolean)}.
     *
     * @param storageProfile the storage profile
     * @return true if the OS disk is configured to use custom image ('captured' or 'bring your own feature')
     */
    private boolean isOSDiskFromStoredImage(StorageProfile storageProfile) {
        OSDisk osDisk = storageProfile.osDisk();
        return isOSDiskFromImage(osDisk) && osDisk.image() != null && osDisk.image().uri() != null;
    }

    private String temporaryBlobUrl(String containerName, String blobName) {
        return "{storage-base-url}" + containerName + "/" + blobName;
    }

    private NetworkInterface.DefinitionStages.WithPrimaryPublicIPAddress prepareNetworkInterface(String name) {
        NetworkInterface.DefinitionStages.WithGroup definitionWithGroup =
            this.networkManager.networkInterfaces().define(name).withRegion(this.regionName());
        NetworkInterface.DefinitionStages.WithPrimaryNetwork definitionWithNetwork;
        if (this.creatableGroup != null) {
            definitionWithNetwork = definitionWithGroup.withNewResourceGroup(this.creatableGroup);
        } else {
            definitionWithNetwork = definitionWithGroup.withExistingResourceGroup(this.resourceGroupName());
        }
        return definitionWithNetwork.withNewPrimaryNetwork("vnet" + name).withPrimaryPrivateIPAddressDynamic();
    }

    private void initializeDataDisks() {
        if (this.inner().storageProfile().dataDisks() == null) {
            this.inner().storageProfile().withDataDisks(new ArrayList<>());
        }

        this.isUnmanagedDiskSelected = false;
        this.managedDataDisks.clear();
        this.unmanagedDataDisks = new ArrayList<>();
        if (!isManagedDiskEnabled()) {
            for (DataDisk dataDiskInner : this.storageProfile().dataDisks()) {
                this.unmanagedDataDisks.add(new UnmanagedDataDiskImpl(dataDiskInner, this));
            }
        }
    }

    private NetworkInterface.DefinitionStages.WithPrimaryNetwork preparePrimaryNetworkInterface(String name) {
        NetworkInterface.DefinitionStages.WithGroup definitionWithGroup =
            this.networkManager.networkInterfaces().define(name).withRegion(this.regionName());
        NetworkInterface.DefinitionStages.WithPrimaryNetwork definitionAfterGroup;
        if (this.creatableGroup != null) {
            definitionAfterGroup = definitionWithGroup.withNewResourceGroup(this.creatableGroup);
        } else {
            definitionAfterGroup = definitionWithGroup.withExistingResourceGroup(this.resourceGroupName());
        }
        return definitionAfterGroup;
    }

    private void clearCachedRelatedResources() {
        this.virtualMachineInstanceView = null;
    }

    private void throwIfManagedDiskEnabled(String message) {
        if (this.isManagedDiskEnabled()) {
            throw logger.logExceptionAsError(new UnsupportedOperationException(message));
        }
    }

    private void throwIfManagedDiskDisabled(String message) {
        if (!this.isManagedDiskEnabled()) {
            throw logger.logExceptionAsError(new UnsupportedOperationException(message));
        }
    }

    private boolean isInUpdateMode() {
        return !this.isInCreateMode();
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

    /** Class to manage Data disk collection. */
    private class ManagedDataDiskCollection {
        private final Map<String, DataDisk> newDisksToAttach = new HashMap<>();
        private final List<DataDisk> existingDisksToAttach = new ArrayList<>();
        private final List<DataDisk> implicitDisksToAssociate = new ArrayList<>();
        private final List<Integer> diskLunsToRemove = new ArrayList<>();
        private final List<DataDisk> newDisksFromImage = new ArrayList<>();
        private final VirtualMachineImpl vm;
        private CachingTypes defaultCachingType;
        private StorageAccountTypes defaultStorageAccountType;

        ManagedDataDiskCollection(VirtualMachineImpl vm) {
            this.vm = vm;
        }

        void setDefaultCachingType(CachingTypes cachingType) {
            this.defaultCachingType = cachingType;
        }

        void setDefaultStorageAccountType(StorageAccountTypes defaultStorageAccountType) {
            this.defaultStorageAccountType = defaultStorageAccountType;
        }

        void setDataDisksDefaults() {
            VirtualMachineInner vmInner = this.vm.inner();
            if (isPending()) {
                if (vmInner.storageProfile().dataDisks() == null) {
                    vmInner.storageProfile().withDataDisks(new ArrayList<>());
                }
                List<DataDisk> dataDisks = vmInner.storageProfile().dataDisks();
                final List<Integer> usedLuns = new ArrayList<>();
                // Get all used luns
                for (DataDisk dataDisk : dataDisks) {
                    if (dataDisk.lun() != -1) {
                        usedLuns.add(dataDisk.lun());
                    }
                }
                for (DataDisk dataDisk : this.newDisksToAttach.values()) {
                    if (dataDisk.lun() != -1) {
                        usedLuns.add(dataDisk.lun());
                    }
                }
                for (DataDisk dataDisk : this.existingDisksToAttach) {
                    if (dataDisk.lun() != -1) {
                        usedLuns.add(dataDisk.lun());
                    }
                }
                for (DataDisk dataDisk : this.implicitDisksToAssociate) {
                    if (dataDisk.lun() != -1) {
                        usedLuns.add(dataDisk.lun());
                    }
                }
                for (DataDisk dataDisk : this.newDisksFromImage) {
                    if (dataDisk.lun() != -1) {
                        usedLuns.add(dataDisk.lun());
                    }
                }
                // Func to get the next available lun
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
                    setAttachableNewDataDisks(nextLun);
                    setAttachableExistingDataDisks(nextLun);
                    setImplicitDataDisks(nextLun);
                } catch (Exception ex) {
                    throw logger.logExceptionAsError(Exceptions.propagate(ex));
                }
                setImageBasedDataDisks();
                removeDataDisks();
            }
            if (vmInner.storageProfile().dataDisks() != null && vmInner.storageProfile().dataDisks().size() == 0) {
                if (vm.isInCreateMode()) {
                    // If there is no data disks at all, then setting it to null rather than [] is necessary.
                    // This is for take advantage of CRP's implicit creation of the data disks if the image has
                    // more than one data disk image(s).
                    vmInner.storageProfile().withDataDisks(null);
                }
            }
            this.clear();
        }

        private void clear() {
            newDisksToAttach.clear();
            existingDisksToAttach.clear();
            implicitDisksToAssociate.clear();
            diskLunsToRemove.clear();
            newDisksFromImage.clear();
        }

        private boolean isPending() {
            return newDisksToAttach.size() > 0
                || existingDisksToAttach.size() > 0
                || implicitDisksToAssociate.size() > 0
                || diskLunsToRemove.size() > 0
                || newDisksFromImage.size() > 0;
        }

        private void setAttachableNewDataDisks(Callable<Integer> nextLun) throws Exception {
            List<DataDisk> dataDisks = vm.inner().storageProfile().dataDisks();
            for (Map.Entry<String, DataDisk> entry : this.newDisksToAttach.entrySet()) {
                Disk managedDisk = vm.taskResult(entry.getKey());
                DataDisk dataDisk = entry.getValue();
                dataDisk.withCreateOption(DiskCreateOptionTypes.ATTACH);
                if (dataDisk.lun() == -1) {
                    dataDisk.withLun(nextLun.call());
                }
                dataDisk.withManagedDisk(new ManagedDiskParameters());
                dataDisk.managedDisk().withId(managedDisk.id());
                if (dataDisk.caching() == null) {
                    dataDisk.withCaching(getDefaultCachingType());
                }
                // Don't set default storage account type for the attachable managed disks, it is already
                // defined in the managed disk and not allowed to change.
                dataDisk.withName(null);
                dataDisks.add(dataDisk);
            }
        }

        private void setAttachableExistingDataDisks(Callable<Integer> nextLun) throws Exception {
            List<DataDisk> dataDisks = vm.inner().storageProfile().dataDisks();
            for (DataDisk dataDisk : this.existingDisksToAttach) {
                dataDisk.withCreateOption(DiskCreateOptionTypes.ATTACH);
                if (dataDisk.lun() == -1) {
                    dataDisk.withLun(nextLun.call());
                }
                if (dataDisk.caching() == null) {
                    dataDisk.withCaching(getDefaultCachingType());
                }
                // Don't set default storage account type for the attachable managed disks, it is already
                // defined in the managed disk and not allowed to change.
                dataDisk.withName(null);
                dataDisks.add(dataDisk);
            }
        }

        private void setImplicitDataDisks(Callable<Integer> nextLun) throws Exception {
            List<DataDisk> dataDisks = vm.inner().storageProfile().dataDisks();
            for (DataDisk dataDisk : this.implicitDisksToAssociate) {
                dataDisk.withCreateOption(DiskCreateOptionTypes.EMPTY);
                if (dataDisk.lun() == -1) {
                    dataDisk.withLun(nextLun.call());
                }
                if (dataDisk.caching() == null) {
                    dataDisk.withCaching(getDefaultCachingType());
                }
                if (dataDisk.managedDisk() == null) {
                    dataDisk.withManagedDisk(new ManagedDiskParameters());
                }
                if (dataDisk.managedDisk().storageAccountType() == null) {
                    dataDisk.managedDisk().withStorageAccountType(getDefaultStorageAccountType());
                }
                dataDisk.withName(null);
                dataDisks.add(dataDisk);
            }
        }

        private void setImageBasedDataDisks() {
            List<DataDisk> dataDisks = vm.inner().storageProfile().dataDisks();
            for (DataDisk dataDisk : this.newDisksFromImage) {
                dataDisk.withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
                // Don't set default storage account type for the disk, either user has to specify it explicitly or let
                // CRP pick it from the image
                dataDisk.withName(null);
                dataDisks.add(dataDisk);
            }
        }

        private void removeDataDisks() {
            List<DataDisk> dataDisks = vm.inner().storageProfile().dataDisks();
            for (Integer lun : this.diskLunsToRemove) {
                int indexToRemove = 0;
                for (DataDisk dataDisk : dataDisks) {
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

    /** Class to manage VM boot diagnostics settings. */
    private class BootDiagnosticsHandler {
        private final VirtualMachineImpl vmImpl;
        private String creatableDiagnosticsStorageAccountKey;

        BootDiagnosticsHandler(VirtualMachineImpl vmImpl) {
            this.vmImpl = vmImpl;
        }

        public boolean isBootDiagnosticsEnabled() {
            if (this.vmInner().diagnosticsProfile() != null
                && this.vmInner().diagnosticsProfile().bootDiagnostics() != null
                && this.vmInner().diagnosticsProfile().bootDiagnostics().enabled() != null) {
                return this.vmInner().diagnosticsProfile().bootDiagnostics().enabled();
            }
            return false;
        }

        public String bootDiagnosticsStorageUri() {
            // Even though diagnostics can disabled azure still keep the storage uri
            if (this.vmInner().diagnosticsProfile() != null
                && this.vmInner().diagnosticsProfile().bootDiagnostics() != null) {
                return this.vmInner().diagnosticsProfile().bootDiagnostics().storageUri();
            }
            return null;
        }

        BootDiagnosticsHandler withBootDiagnostics() {
            // Diagnostics storage uri will be set later by this.handleDiagnosticsSettings(..)
            this.enableDisable(true);
            return this;
        }

        BootDiagnosticsHandler withBootDiagnostics(Creatable<StorageAccount> creatable) {
            // Diagnostics storage uri will be set later by this.handleDiagnosticsSettings(..)
            this.enableDisable(true);
            this.creatableDiagnosticsStorageAccountKey = this.vmImpl.addDependency(creatable);
            return this;
        }

        BootDiagnosticsHandler withBootDiagnostics(String storageAccountBlobEndpointUri) {
            this.enableDisable(true);
            this.vmInner().diagnosticsProfile().bootDiagnostics().withStorageUri(storageAccountBlobEndpointUri);
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
            DiagnosticsProfile diagnosticsProfile = this.vmInner().diagnosticsProfile();
            if (diagnosticsProfile == null
                || diagnosticsProfile.bootDiagnostics() == null
                || diagnosticsProfile.bootDiagnostics().storageUri() != null) {
                return;
            }
            boolean enableBD = Utils.toPrimitiveBoolean(diagnosticsProfile.bootDiagnostics().enabled());
            if (!enableBD) {
                return;
            }
            if (this.creatableDiagnosticsStorageAccountKey != null
                || this.vmImpl.creatableStorageAccountKey != null
                || this.vmImpl.existingStorageAccountToAssociate != null) {
                return;
            }
            String accountName = this.vmImpl.namer.randomName("stg", 24).replace("-", "");
            Creatable<StorageAccount> storageAccountCreatable;
            if (this.vmImpl.creatableGroup != null) {
                storageAccountCreatable =
                    this
                        .vmImpl
                        .storageManager
                        .storageAccounts()
                        .define(accountName)
                        .withRegion(this.vmImpl.regionName())
                        .withNewResourceGroup(this.vmImpl.creatableGroup);
            } else {
                storageAccountCreatable =
                    this
                        .vmImpl
                        .storageManager
                        .storageAccounts()
                        .define(accountName)
                        .withRegion(this.vmImpl.regionName())
                        .withExistingResourceGroup(this.vmImpl.resourceGroupName());
            }
            this.creatableDiagnosticsStorageAccountKey = this.vmImpl.addDependency(storageAccountCreatable);
        }

        void handleDiagnosticsSettings() {
            DiagnosticsProfile diagnosticsProfile = this.vmInner().diagnosticsProfile();
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
                storageAccount = this.vmImpl.<StorageAccount>taskResult(creatableDiagnosticsStorageAccountKey);
            } else if (this.vmImpl.creatableStorageAccountKey != null) {
                storageAccount = this.vmImpl.<StorageAccount>taskResult(this.vmImpl.creatableStorageAccountKey);
            } else if (this.vmImpl.existingStorageAccountToAssociate != null) {
                storageAccount = this.vmImpl.existingStorageAccountToAssociate;
            }
            if (storageAccount == null) {
                throw logger.logExceptionAsError(new IllegalStateException(
                    "Unable to retrieve expected storageAccount instance for BootDiagnostics"));
            }
            vmInner()
                .diagnosticsProfile()
                .bootDiagnostics()
                .withStorageUri(storageAccount.endPoints().primary().blob());
        }

        private VirtualMachineInner vmInner() {
            // Inner cannot be cached as parent VirtualMachineImpl can refresh the inner in various cases
            return this.vmImpl.inner();
        }

        private void enableDisable(boolean enable) {
            if (this.vmInner().diagnosticsProfile() == null) {
                this.vmInner().withDiagnosticsProfile(new DiagnosticsProfile());
            }
            if (this.vmInner().diagnosticsProfile().bootDiagnostics() == null) {
                this.vmInner().diagnosticsProfile().withBootDiagnostics(new BootDiagnostics());
            }
            if (enable) {
                this.vmInner().diagnosticsProfile().bootDiagnostics().withEnabled(true);
            } else {
                this.vmInner().diagnosticsProfile().bootDiagnostics().withEnabled(false);
                this.vmInner().diagnosticsProfile().bootDiagnostics().withStorageUri(null);
            }
        }
    }
}
