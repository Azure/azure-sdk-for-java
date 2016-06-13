package com.microsoft.azure.management.compute.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.DataDisk;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSize;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineSizeInner;
import com.microsoft.azure.management.compute.implementation.api.Plan;
import com.microsoft.azure.management.compute.implementation.api.HardwareProfile;
import com.microsoft.azure.management.compute.implementation.api.StorageProfile;
import com.microsoft.azure.management.compute.implementation.api.OSProfile;
import com.microsoft.azure.management.compute.implementation.api.DiagnosticsProfile;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineInstanceView;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineExtensionInner;
import com.microsoft.azure.management.compute.implementation.api.OperatingSystemTypes;
import com.microsoft.azure.management.compute.implementation.api.ImageReference;
import com.microsoft.azure.management.compute.implementation.api.WinRMListener;
import com.microsoft.azure.management.compute.implementation.api.CachingTypes;
import com.microsoft.azure.management.compute.implementation.api.DiskEncryptionSettings;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.implementation.api.VirtualHardDisk;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachinesInner;
import com.microsoft.azure.management.compute.implementation.api.OSDisk;
import com.microsoft.azure.management.compute.implementation.api.DiskCreateOptionTypes;
import com.microsoft.azure.management.compute.implementation.api.LinuxConfiguration;
import com.microsoft.azure.management.compute.implementation.api.WindowsConfiguration;
import com.microsoft.azure.management.compute.implementation.api.WinRMConfiguration;
import com.microsoft.azure.management.compute.implementation.api.SshConfiguration;
import com.microsoft.azure.management.compute.implementation.api.SshPublicKey;
import com.microsoft.azure.management.compute.implementation.api.NetworkInterfaceReference;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineCaptureParametersInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineCaptureResultInner;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.resources.implementation.api.PageImpl;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.rest.RestException;
import com.microsoft.rest.ServiceResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The type representing Azure virtual machine.
 */
class VirtualMachineImpl
        extends GroupableResourceImpl<VirtualMachine, VirtualMachineInner, VirtualMachineImpl>
        implements
        VirtualMachine,
        VirtualMachine.Definitions,
        VirtualMachine.Update {
    // Clients
    private final VirtualMachinesInner client;
    private final AvailabilitySets availabilitySets;
    private final StorageManager storageManager;
    private final NetworkManager networkManager;
    // the name of the virtual machine
    private final String vmName;
    // used to generate unique name for any dependency resources
    private final String randomId;
    // unique key of a creatable storage account to be used for virtual machine child resources that
    // requires storage [OS disk, data disk etc..]
    private String creatableStorageAccountKey;
    // unique key of a creatable availability set that this virtual machine to put
    private String creatableAvailabilitySetKey;
    // unique key of a creatable network interface that needs to be used as virtual machine's primary network interface
    private String creatablePrimaryNetworkInterfaceKey;
    // unique key of a creatable network interfaces that needs to be used as virtual machine's secondary network interface
    private List<String> creatableSecondaryNetworkInterfaceKeys;
    // reference to an existing storage account to be used for virtual machine child resources that
    // requires storage [OS disk, data disk etc..]
    private StorageAccount existingStorageAccountToAssociate;
    // reference to an existing availability set that this virtual machine to put
    private AvailabilitySet existingAvailabilitySetToAssociate;
    // reference to an existing network interface that needs to be used as virtual machine's primary network interface
    private NetworkInterface existingPrimaryNetworkInterfaceToAssociate;
    // reference to a list of existing network interfaces that needs to be used as virtual machine's secondary network interface
    private List<NetworkInterface> existingSecondaryNetworkInterfacesToAssociate;
    // Cached related resources
    private NetworkInterface primaryNetworkInterface;
    private PublicIpAddress primaryPublicIpAddress;
    // The data disks associated with the virtual machine
    private List<DataDisk> dataDisks;
    // Intermediate state of network interface definition to which private ip can be associated
    private NetworkInterface
            .DefinitionWithPrivateIp nicDefinitionWithPrivateIp;
    // Intermediate state of network interface definition to which subnet can be associated
    private NetworkInterface
            .DefinitionWithSubnet nicDefinitionWithSubnet;
    // Intermediate state of network interface definition to which public Ip can be associated
    private NetworkInterface
            .DefinitionWithPublicIpAddress nicDefinitionWithPublicIp;
    // Virtual machine size converter
    private final PagedListConverter<VirtualMachineSizeInner, VirtualMachineSize> virtualMachineSizeConverter;

    VirtualMachineImpl(String name,
                       VirtualMachineInner innerModel,
                       VirtualMachinesInner client,
                       AvailabilitySets availabilitySets,
                       final ResourceManager resourceManager,
                       final StorageManager storageManager,
                       final NetworkManager networkManager) {
        super(name, innerModel, resourceManager.resourceGroups());
        this.client = client;
        this.availabilitySets = availabilitySets;
        this.storageManager = storageManager;
        this.networkManager = networkManager;
        this.vmName = name;
        this.randomId = Utils.randomId(this.vmName);
        this.creatableSecondaryNetworkInterfaceKeys = new ArrayList<>();
        this.existingSecondaryNetworkInterfacesToAssociate = new ArrayList<>();
        this.virtualMachineSizeConverter = new PagedListConverter<VirtualMachineSizeInner, VirtualMachineSize>() {
            @Override
            public VirtualMachineSize typeConvert(VirtualMachineSizeInner inner) {
                return new VirtualMachineSizeImpl(inner);
            }
        };
        initializeDataDisks();
    }

    /**************************************************.
     * Verbs
     **************************************************/

    @Override
    public VirtualMachine refresh() throws Exception {
        ServiceResponse<VirtualMachineInner> response =
                this.client.get(this.resourceGroupName(), this.name());
        this.setInner(response.getBody());
        initializeDataDisks();
        return this;
    }

    @Override
    public VirtualMachine create() throws Exception {
        super.creatablesCreate();
        return this;
    }

    @Override
    public VirtualMachineImpl update() throws Exception {
        return this;
    }

    @Override
    public VirtualMachine apply() throws Exception {
        return this.create();
    }

    @Override
    public void deallocate() throws CloudException, IOException, InterruptedException {
        this.client.deallocate(this.resourceGroupName(), this.name());
    }

    @Override
    public void generalize() throws CloudException, IOException {
        this.client.generalize(this.resourceGroupName(), this.name());
    }

    @Override
    public void powerOff() throws CloudException, IOException, InterruptedException {
        this.client.powerOff(this.resourceGroupName(), this.name());
    }

    @Override
    public void restart() throws CloudException, IOException, InterruptedException {
        this.client.restart(this.resourceGroupName(), this.name());
    }

    @Override
    public void start() throws CloudException, IOException, InterruptedException {
        this.client.start(this.resourceGroupName(), this.name());
    }

    @Override
    public void redeploy() throws CloudException, IOException, InterruptedException {
        this.client.redeploy(this.resourceGroupName(), this.name());
    }

    @Override
    public PagedList<VirtualMachineSize> availableSizes() throws CloudException, IOException {
        PageImpl<VirtualMachineSizeInner> page = new PageImpl<>();
        page.setItems(this.client.listAvailableSizes(this.resourceGroupName(), this.name()).getBody());
        page.setNextPageLink(null);
        return this.virtualMachineSizeConverter.convert(new PagedList<VirtualMachineSizeInner>(page) {
            @Override
            public Page<VirtualMachineSizeInner> nextPage(String nextPageLink) throws RestException, IOException {
                return null;
            }
        });
    }

    @Override
    public String capture(String containerName, boolean overwriteVhd) throws CloudException, IOException, InterruptedException {
        VirtualMachineCaptureParametersInner parameters = new VirtualMachineCaptureParametersInner();
        parameters.withDestinationContainerName(containerName);
        parameters.withOverwriteVhds(overwriteVhd);
        ServiceResponse<VirtualMachineCaptureResultInner> captureResult = this.client.capture(this.resourceGroupName(), this.name(), parameters);
        ObjectMapper mapper = new ObjectMapper();
        //Object to JSON string
        return mapper.writeValueAsString(captureResult.getBody().output());
    }

    /**************************************************.
     * Setters
     **************************************************/

    // Fluent methods for defining virtual network association for the new primary network interface

    @Override
    public VirtualMachineImpl withNewPrimaryNetwork(Network.DefinitionCreatable creatable) {
        this.nicDefinitionWithPrivateIp = this.preparePrimaryNetworkInterface(nameWithPrefix("nic", "-"))
                .withNewPrimaryNetwork(creatable);
        return this;
    }

    @Override
    public VirtualMachineImpl withNewPrimaryNetwork(String addressSpace) {
        this.nicDefinitionWithPrivateIp = this.preparePrimaryNetworkInterface(nameWithPrefix("nic", "-"))
                .withNewPrimaryNetwork(addressSpace);
        return this;
    }

    @Override
    public VirtualMachineImpl withExistingPrimaryNetwork(Network network) {
        this.nicDefinitionWithSubnet = this.preparePrimaryNetworkInterface(nameWithPrefix("nic", "-"))
                .withExistingPrimaryNetwork(network);
        return this;
    }

    @Override
    public VirtualMachineImpl withSubnet(String name) {
        this.nicDefinitionWithPrivateIp = this.nicDefinitionWithSubnet
                .withSubnet(name);
        return this;
    }

    // Fluent methods for defining private Ip association for the new primary network interface

    @Override
    public VirtualMachineImpl withPrimaryPrivateIpAddressDynamic() {
        this.nicDefinitionWithPublicIp = this.nicDefinitionWithPrivateIp
                .withPrimaryPrivateIpAddressDynamic();
        return this;
    }

    @Override
    public VirtualMachineImpl withPrimaryPrivateIpAddressStatic(String staticPrivateIpAddress) {
        this.nicDefinitionWithPublicIp = this.nicDefinitionWithPrivateIp
                .withPrimaryPrivateIpAddressStatic(staticPrivateIpAddress);
        return this;
    }

    // Fluent methods for defining public Ip association for the new primary network interface

    @Override
    public VirtualMachineImpl withNewPrimaryPublicIpAddress(PublicIpAddress.DefinitionCreatable creatable) {
        NetworkInterface.DefinitionCreatable nicCreatable = this.nicDefinitionWithPublicIp
                .withNewPrimaryPublicIpAddress(creatable);
        this.addCreatableDependency(nicCreatable);
        return this;
    }

    @Override
    public VirtualMachineImpl withNewPrimaryPublicIpAddress(String leafDnsLabel) {
        NetworkInterface.DefinitionCreatable nicCreatable = this.nicDefinitionWithPublicIp
                .withNewPrimaryPublicIpAddress(leafDnsLabel);
        this.creatablePrimaryNetworkInterfaceKey = nicCreatable.key();
        this.addCreatableDependency(nicCreatable);
        return this;
    }

    @Override
    public VirtualMachineImpl withExistingPrimaryPublicIpAddress(PublicIpAddress publicIpAddress) {
        NetworkInterface.DefinitionCreatable nicCreatable = this.nicDefinitionWithPublicIp
                .withExistingPrimaryPublicIpAddress(publicIpAddress);
        this.creatablePrimaryNetworkInterfaceKey = nicCreatable.key();
        this.addCreatableDependency(nicCreatable);
        return this;
    }

    @Override
    public VirtualMachineImpl withoutPrimaryPublicIpAddress() {
        this.creatablePrimaryNetworkInterfaceKey = this.nicDefinitionWithPublicIp.key();
        this.addCreatableDependency(this.nicDefinitionWithPublicIp);
        return this;
    }

    // Virtual machine primary network interface specific fluent methods
    //
    @Override
    public VirtualMachineImpl withNewPrimaryNetworkInterface(NetworkInterface.DefinitionCreatable creatable) {
        this.creatablePrimaryNetworkInterfaceKey = creatable.key();
        this.addCreatableDependency(creatable);
        return this;
    }

    public VirtualMachineImpl withNewPrimaryNetworkInterface(String name, String publicDnsNameLabel) {
        NetworkInterface.DefinitionCreatable definitionCreatable = prepareNetworkInterface(name)
                .withNewPrimaryPublicIpAddress(publicDnsNameLabel);
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
    public VirtualMachineImpl withMarketplaceImage() {
        return this;
    }

    @Override
    public VirtualMachineImpl withStoredImage(String imageUrl) {
        VirtualHardDisk userImageVhd = new VirtualHardDisk();
        userImageVhd.withUri(imageUrl);
        this.inner().storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().storageProfile().osDisk().withImage(userImageVhd);
        return this;
    }

    @Override
    public VirtualMachineImpl withOSDisk(String osDiskUrl, OperatingSystemTypes osType) {
        VirtualHardDisk osDisk = new VirtualHardDisk();
        osDisk.withUri(osDiskUrl);
        this.inner().storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.ATTACH);
        this.inner().storageProfile().osDisk().withVhd(osDisk);
        this.inner().storageProfile().osDisk().withOsType(osType);
        return this;
    }

    @Override
    public VirtualMachineImpl version(ImageReference imageReference) {
        this.inner().storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().storageProfile().withImageReference(imageReference);
        return this;
    }

    @Override
    public VirtualMachineImpl latest(String publisher, String offer, String sku) {
        ImageReference imageReference = new ImageReference();
        imageReference.withPublisher(publisher);
        imageReference.withOffer(offer);
        imageReference.withSku(sku);
        imageReference.withVersion("latest");
        return version(imageReference);
    }

    @Override
    public VirtualMachineImpl popular(KnownVirtualMachineImage knownImage) {
        return version(knownImage.imageReference());
    }

    // Virtual machine operating system type fluent methods
    //

    @Override
    public VirtualMachineImpl withLinuxOS() {
        OSDisk osDisk = this.inner().storageProfile().osDisk();
        if (isStoredImage(osDisk)) {
            // For platform image osType should be null, azure will pick it from the image metadata.
            osDisk.withOsType(OperatingSystemTypes.LINUX);
        }
        this.inner().osProfile().withLinuxConfiguration(new LinuxConfiguration());
        return this;
    }

    @Override
    public VirtualMachineImpl withWindowsOS() {
        OSDisk osDisk = this.inner().storageProfile().osDisk();
        if (isStoredImage(osDisk)) {
            // For platform image osType should be null, azure will pick it from the image metadata.
            osDisk.withOsType(OperatingSystemTypes.WINDOWS);
        }
        this.inner().osProfile().withWindowsConfiguration(new WindowsConfiguration());
        // sets defaults for "Stored(User)Image" or "VM(Platform)Image"
        this.inner().osProfile().windowsConfiguration().withProvisionVMAgent(true);
        this.inner().osProfile().windowsConfiguration().withEnableAutomaticUpdates(true);
        return this;
    }

    // Virtual machine user name fluent methods
    //

    @Override
    public VirtualMachineImpl withRootUserName(String rootUserName) {
        this.inner().osProfile().withAdminUsername(rootUserName);
        return this;
    }

    @Override
    public VirtualMachineImpl withAdminUserName(String adminUserName) {
        this.inner().osProfile().withAdminUsername(adminUserName);
        return this;
    }

    // Virtual machine optional fluent methods
    //

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
    public VirtualMachineImpl disableVMAgent() {
        this.inner().osProfile().windowsConfiguration().withProvisionVMAgent(false);
        return this;
    }

    @Override
    public VirtualMachineImpl disableAutoUpdate() {
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

        this.inner().osProfile()
                .windowsConfiguration()
                .winRM()
                .listeners()
                .add(listener);
        return this;
    }

    @Override
    public VirtualMachineImpl withPassword(String password) {
        this.inner().osProfile().withAdminPassword(password);
        return this;
    }

    @Override
    public VirtualMachineImpl withSize(String sizeName) {
        this.inner().hardwareProfile().withVmSize(sizeName);
        return this;
    }

    @Override
    public VirtualMachineImpl withSize(VirtualMachineSizeTypes size) {
        this.inner().hardwareProfile().withVmSize(size.toString());
        return this;
    }

    @Override
    public VirtualMachineImpl withOSDiskCaching(CachingTypes cachingType) {
        this.inner().storageProfile().osDisk().withCaching(cachingType);
        return this;
    }

    @Override
    public VirtualMachineImpl withOSDiskVhdLocation(String containerName, String vhdName) {
        VirtualHardDisk osVhd = new VirtualHardDisk();
        osVhd.withUri(temporaryBlobUrl(containerName, vhdName));
        this.inner().storageProfile().osDisk().withVhd(osVhd);
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
    public VirtualMachineImpl withOSDiskName(String name) {
        this.inner().storageProfile().osDisk().withName(name);
        return this;
    }

    // Virtual machine optional data disk fluent methods
    //

    @Override
    public DataDiskImpl defineNewDataDisk(String name) {
        DataDiskImpl dataDisk =  DataDiskImpl.prepareDataDisk(name, DiskCreateOptionTypes.EMPTY, this);
        this.dataDisks().add(dataDisk);
        return dataDisk;
    }

    @Override
    public DataDiskImpl defineExistingDataDisk(String name) {
        DataDiskImpl dataDisk =  DataDiskImpl.prepareDataDisk(name, DiskCreateOptionTypes.ATTACH, this);
        this.dataDisks().add(dataDisk);
        return dataDisk;
    }

    @Override
    public VirtualMachineImpl withNewDataDisk(Integer sizeInGB) {
        DataDiskImpl dataDisk = DataDiskImpl.createNewDataDisk(sizeInGB, this);
        this.dataDisks().add(dataDisk);
        return this;
    }

    @Override
    public VirtualMachineImpl withExistingDataDisk(String storageAccountName, String containerName, String vhdName) {
        DataDiskImpl dataDisk = DataDiskImpl.createFromExistingDisk(storageAccountName, containerName, vhdName, this);
        this.dataDisks().add(dataDisk);
        return this;
    }

    // Virtual machine optional storage account fluent methods
    //

    @Override
    public VirtualMachineImpl withNewStorageAccount(StorageAccount.DefinitionCreatable creatable) {
        // This method's effect is NOT additive.
        if (this.creatableStorageAccountKey == null) {
            this.creatableStorageAccountKey = creatable.key();
            this.addCreatableDependency(creatable);
        }
        return this;
    }

    @Override
    public VirtualMachineImpl withNewStorageAccount(String name) {
        StorageAccount.DefinitionWithGroup definitionWithGroup = this.storageManager
                .storageAccounts()
                .define(name)
                .withRegion(this.region());
        StorageAccount.DefinitionCreatable definitionAfterGroup;
        if (this.newGroup != null) {
            definitionAfterGroup = definitionWithGroup.withNewGroup(this.newGroup);
        } else {
            definitionAfterGroup = definitionWithGroup.withExistingGroup(this.resourceGroupName());
        }
        return withNewStorageAccount(definitionAfterGroup);
    }

    @Override
    public VirtualMachineImpl withExistingStorageAccount(StorageAccount storageAccount) {
        this.existingStorageAccountToAssociate = storageAccount;
        return this;
    }

    // Virtual machine optional availability set fluent methods
    //

    @Override
    public VirtualMachineImpl withNewAvailabilitySet(AvailabilitySet.DefinitionCreatable creatable) {
        // This method's effect is NOT additive.
        if (this.creatableAvailabilitySetKey == null) {
            this.creatableAvailabilitySetKey = creatable.key();
            this.addCreatableDependency(creatable);
        }
        return this;
    }

    @Override
    public VirtualMachineImpl withNewAvailabilitySet(String name) {
        return withNewAvailabilitySet(availabilitySets.define(name)
                .withRegion(region())
                .withExistingGroup(this.resourceGroupName())
        );
    }

    @Override
    public VirtualMachineImpl withExistingAvailabilitySet(AvailabilitySet availabilitySet) {
        this.existingAvailabilitySetToAssociate = availabilitySet;
        return this;
    }

    @Override
    public VirtualMachineImpl withNewSecondaryNetworkInterface(NetworkInterface.DefinitionCreatable creatable) {
        this.creatableSecondaryNetworkInterfaceKeys.add(creatable.key());
        this.addCreatableDependency(creatable);
        return this;
    }

    @Override
    public VirtualMachineImpl withExistingSecondaryNetworkInterface(NetworkInterface networkInterface) {
        this.existingSecondaryNetworkInterfacesToAssociate.add(networkInterface);
        return this;
    }

    // Virtual machine update only settings

    @Override
    public VirtualMachineImpl withoutDataDisk(String name) {
        int idx = -1;
        for (DataDisk dataDisk : this.dataDisks) {
            idx++;
            if (dataDisk.name().equalsIgnoreCase(name)) {
                this.dataDisks.remove(idx);
                this.inner().storageProfile().dataDisks().remove(idx);
                break;
            }
        }
        return this;
    }

    @Override
    public VirtualMachineImpl withoutDataDisk(int lun) {
        int idx = -1;
        for (DataDisk dataDisk : this.dataDisks) {
            idx++;
            if (dataDisk.lun() == lun) {
                this.dataDisks.remove(idx);
                this.inner().storageProfile().dataDisks().remove(idx);
                break;
            }
        }
        return this;
    }

    @Override
    public DataDiskImpl updateDataDisk(String name) {
        for (DataDisk dataDisk : this.dataDisks) {
            if (dataDisk.name().equalsIgnoreCase(name)) {
                return (DataDiskImpl) dataDisk;
            }
        }
        throw new RuntimeException("A data disk with name  '" + name + "' not found");
    }

    @Override
    public VirtualMachineImpl withoutSecondaryNetworkInterface(String name) {
        if (this.inner().networkProfile() != null
                && this.inner().networkProfile().networkInterfaces() != null) {
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

    /**************************************************.
     * Getters
     **************************************************/

    @Override
    public String computerName() {
        return inner().osProfile().computerName();
    }

    @Override
    public String size() {
        return inner().hardwareProfile().vmSize();
    }

    @Override
    public OperatingSystemTypes osType() {
        return inner().storageProfile().osDisk().osType();
    }

    @Override
    public String osDiskVhdUri() {
        return inner().storageProfile().osDisk().vhd().uri();
    }

    @Override
    public CachingTypes osDiskCachingType() {
        return inner().storageProfile().osDisk().caching();
    }

    @Override
    public Integer osDiskSize() {
        return inner().storageProfile().osDisk().diskSizeGB();
    }

    @Override
    public List<DataDisk> dataDisks() {
        return this.dataDisks;
    }

    @Override
    public NetworkInterface primaryNetworkInterface() throws CloudException, IOException {
        if (this.primaryNetworkInterface == null) {
            String primaryNicId = primaryNetworkInterfaceId();
            this.primaryNetworkInterface = this.networkManager.networkInterfaces()
                    .getByGroup(ResourceUtils.groupFromResourceId(primaryNicId), ResourceUtils.nameFromResourceId(primaryNicId));
        }
        return this.primaryNetworkInterface;
    }

    @Override
    public PublicIpAddress primaryPublicIpAddress()  throws CloudException, IOException {
        if (this.primaryPublicIpAddress == null) {
            this.primaryPublicIpAddress = this.primaryNetworkInterface().primaryPublicIpAddress();
        }
        return this.primaryPublicIpAddress;
    }

    @Override
    public List<String> networkInterfaceIds() {
        List nicIds = new ArrayList();
        for (NetworkInterfaceReference nicRef : inner().networkProfile().networkInterfaces()) {
            nicIds.add(nicRef.id());
        }
        return nicIds;
    }

    @Override
    public String primaryNetworkInterfaceId() {
        for (NetworkInterfaceReference nicRef : inner().networkProfile().networkInterfaces()) {
            if (nicRef.primary() != null && nicRef.primary()) {
                return nicRef.id();
            }
        }
        return null;
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
    public VirtualMachineInstanceView instanceView() {
        return inner().instanceView();
    }

    @Override
    public String licenseType() {
        return inner().licenseType();
    }

    @Override
    public List<VirtualMachineExtensionInner> resources() {
        return inner().resources();
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


    /**************************************************.
     * CreatableImpl::createResource
     **************************************************/

    @Override
    protected void createResource() throws Exception {
        if (isInCreateMode()) {
            setOSDiskAndOSProfileDefaults();
            setHardwareProfileDefaults();
        }
        DataDiskImpl.setDataDisksDefaults(this.dataDisks, this.vmName);

        handleStorageSettings();
        handleNetworkSettings();
        handleAvailabilitySettings();

        ServiceResponse<VirtualMachineInner> serviceResponse = this.client.createOrUpdate(this.resourceGroupName(), this.vmName, this.inner());
        this.setInner(serviceResponse.getBody());
        initializeDataDisks();
    }

    /**************************************************.
     * Helper methods
     **************************************************/

    /**
     * @param prefix the prefix
     * @param separator the separator between prefix and random string
     * @return a random value (derived from the resource name) with the given prefix
     */
    private String nameWithPrefix(String prefix, String separator) {
        return prefix + separator + this.randomId;
    }

    private void setOSDiskAndOSProfileDefaults() {
        if (!isInCreateMode()) {
            return;
        }

        OSDisk osDisk = this.inner().storageProfile().osDisk();
        if (isOSDiskFromImage(osDisk)) {
            if (osDisk.vhd() == null) {
                // Sets the OS disk VHD for "UserImage" and "VM(Platform)Image"
                withOSDiskVhdLocation("vhds", this.vmName + "-os-disk-" + UUID.randomUUID().toString() + ".vhd");
            }
            OSProfile osProfile = this.inner().osProfile();
            if (osDisk.osType() == OperatingSystemTypes.LINUX) {
                if (osProfile.linuxConfiguration() == null) {
                    osProfile.withLinuxConfiguration(new LinuxConfiguration());
                }
                this.inner().osProfile().linuxConfiguration().withDisablePasswordAuthentication(osProfile.adminPassword() == null);
            }
        }

        if (osDisk.caching() == null) {
            withOSDiskCaching(CachingTypes.READ_WRITE);
        }

        if (osDisk.name() == null) {
            withOSDiskName(this.vmName + "-os-disk");
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

    private void handleStorageSettings() throws Exception {
        StorageAccount storageAccount = null;
        if (this.creatableStorageAccountKey != null) {
            storageAccount = (StorageAccount) this.createdResource(this.creatableStorageAccountKey);
        } else if (this.existingStorageAccountToAssociate != null) {
            storageAccount = this.existingStorageAccountToAssociate;
        } else if (osDiskRequiresImplicitStorageAccountCreation()
                   || dataDisksRequiresImplicitStorageAccountCreation()) {
            storageAccount = this.storageManager.storageAccounts()
                    .define(nameWithPrefix("stg", null))
                    .withRegion(this.region())
                    .withExistingGroup(this.resourceGroupName())
                    .withAccountType(AccountType.STANDARD_GRS)
                    .create();
        }

        if (isInCreateMode()) {
            if (isOSDiskFromImage(this.inner().storageProfile().osDisk())) {
                String uri = this.inner()
                        .storageProfile()
                        .osDisk().vhd().uri()
                        .replaceFirst("\\{storage-base-url}", storageAccount.endPoints().primary().blob());
                this.inner().storageProfile().osDisk().vhd().withUri(uri);
            }
            DataDiskImpl.ensureDisksVhdUri(this.dataDisks, storageAccount, this.vmName);
        } else {
            if (storageAccount != null) {
                DataDiskImpl.ensureDisksVhdUri(this.dataDisks, storageAccount, this.vmName);
            } else {
                DataDiskImpl.ensureDisksVhdUri(this.dataDisks, this.vmName);
            }
        }
    }

    private void handleNetworkSettings() {
        if (isInCreateMode()) {
            NetworkInterface primaryNetworkInterface = null;
            if (this.creatablePrimaryNetworkInterfaceKey != null) {
                primaryNetworkInterface = (NetworkInterface) this.createdResource(this.creatablePrimaryNetworkInterfaceKey);
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
            NetworkInterface secondaryNetworkInterface = (NetworkInterface) this.createdResource(creatableSecondaryNetworkInterfaceKey);
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
            availabilitySet = (AvailabilitySet) this.createdResource(this.creatableAvailabilitySetKey);
        } else if (this.existingAvailabilitySetToAssociate != null) {
            availabilitySet = this.existingAvailabilitySetToAssociate;
        }

        if (availabilitySet != null) {
            this.inner().availabilitySet().withId(availabilitySet.id());
        }
    }

    private boolean osDiskRequiresImplicitStorageAccountCreation() {
        if (this.creatableStorageAccountKey != null
                || this.existingStorageAccountToAssociate != null
                || !isInCreateMode()) {
            return false;
        }

        return isOSDiskFromImage(this.inner().storageProfile().osDisk());
    }

    private boolean dataDisksRequiresImplicitStorageAccountCreation() {
        if (this.creatableStorageAccountKey != null
                || this.existingStorageAccountToAssociate != null
                || this.dataDisks.size() == 0) {
            return false;
        }

        boolean hasEmptyVhd = false;
        for (DataDisk dataDisk : this.dataDisks) {
            if (dataDisk.createOption() == DiskCreateOptionTypes.EMPTY) {
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
            for (DataDisk dataDisk : this.dataDisks) {
                if (dataDisk.createOption() == DiskCreateOptionTypes.ATTACH && dataDisk.inner().vhd() != null) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    private boolean isStoredImage(OSDisk osDisk) {
        return osDisk.image() != null;
    }

    private boolean isOSDiskAttached(OSDisk osDisk) {
        return osDisk.createOption() == DiskCreateOptionTypes.ATTACH;
    }

    private boolean isOSDiskFromImage(OSDisk osDisk) {
        return !isOSDiskAttached(osDisk);
    }

    private String temporaryBlobUrl(String containerName, String blobName) {
        return "{storage-base-url}" + containerName + "/" + blobName;
    }

    private NetworkInterface.DefinitionWithPublicIpAddress prepareNetworkInterface(String name) {
        NetworkInterface.DefinitionWithGroup definitionWithGroup = this.networkManager
                .networkInterfaces()
                .define(name)
                .withRegion(this.region());
        NetworkInterface.DefinitionWithNetwork definitionWithNetwork;
        if (this.newGroup != null) {
            definitionWithNetwork = definitionWithGroup.withNewGroup(this.newGroup);
        } else {
            definitionWithNetwork = definitionWithGroup.withExistingGroup(this.resourceGroupName());
        }
        return definitionWithNetwork
                .withNewPrimaryNetwork("vnet" + name)
                .withPrimaryPrivateIpAddressDynamic();
    }

    private void initializeDataDisks() {
        if (this.inner().storageProfile().dataDisks() == null) {
            this.inner()
                    .storageProfile()
                    .withDataDisks(new ArrayList<com.microsoft.azure.management.compute.implementation.api.DataDisk>());
        }
        this.dataDisks = new ArrayList<>();
        for (com.microsoft.azure.management.compute.implementation.api.DataDisk dataDiskInner : this.storageProfile().dataDisks()) {
            this.dataDisks().add(new DataDiskImpl(dataDiskInner.name(), dataDiskInner, this, false));
        }
    }

    private NetworkInterface.DefinitionWithNetwork preparePrimaryNetworkInterface(String name) {
        NetworkInterface.DefinitionWithGroup definitionWithGroup = this.networkManager.networkInterfaces()
                .define(name)
                .withRegion(this.region());
        NetworkInterface.DefinitionWithNetwork definitionAfterGroup;
        if (this.newGroup != null) {
            definitionAfterGroup = definitionWithGroup.withNewGroup(this.newGroup);
        } else {
            definitionAfterGroup = definitionWithGroup.withExistingGroup(this.resourceGroupName());
        }
        return definitionAfterGroup;
    }
}