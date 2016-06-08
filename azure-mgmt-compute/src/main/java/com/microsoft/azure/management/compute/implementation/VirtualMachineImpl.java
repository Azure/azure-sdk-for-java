package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.*;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineInner;
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
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.azure.management.storage.implementation.api.AccountType;
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
        VirtualMachine.DefinitionBlank,
        VirtualMachine.DefinitionWithGroup,
        VirtualMachine.DefinitionWithPrimaryNetworkInterface,
        VirtualMachine.DefinitionWithOS,
        VirtualMachine.DefinitionWithMarketplaceImage,
        VirtualMachine.DefinitionWithOSType,
        VirtualMachine.DefinitionWithRootUserName,
        VirtualMachine.DefinitionWithAdminUserName,
        VirtualMachine.DefinitionLinuxCreatable,
        VirtualMachine.DefinitionWindowsCreatable,
        VirtualMachine.DefinitionCreatable {
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
    private String creatableNetworkInterfaceKey;
    // reference to an existing storage account to be used for virtual machine child resources that
    // requires storage [OS disk, data disk etc..]
    private StorageAccount existingStorageAccountToAssociate;
    // reference to an existing availability set that this virtual machine to put
    private AvailabilitySet existingAvailabilitySetToAssociate;
    // reference to an existing network interface that needs to be used as virtual machine's primary network interface
    private NetworkInterface existingNetworkInterfaceToAssociate;
    // Cached related resources
    private NetworkInterface primaryNetworkInterface;
    private PublicIpAddress primaryPublicIpAddress;
    private VMInstanceView vmInstanceView;
    // The data disks associated with the virtual machine
    private List<DataDisk> dataDisks;

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

    /**************************************************.
     * Setters
     **************************************************/

    // Virtual machine network interface specific fluent methods
    //

    @Override
    public DefinitionWithOS withNewPrimaryNetworkInterface(NetworkInterface.DefinitionCreatable creatable) {
        this.creatableNetworkInterfaceKey = creatable.key();
        this.addCreatableDependency(creatable);
        return this;
    }

    @Override
    public DefinitionWithOS withNewPrimaryNetworkInterface(String name) {
        return withNewPrimaryNetworkInterface(prepareNetworkInterface(name));
    }

    public DefinitionWithOS withNewPrimaryNetworkInterface(String name, String publicDnsNameLabel) {
        NetworkInterface.DefinitionCreatable definitionCreatable = prepareNetworkInterface(name)
                .withNewPrimaryPublicIpAddress(publicDnsNameLabel);
        return withNewPrimaryNetworkInterface(definitionCreatable);
    }

    @Override
    public DefinitionWithOS withExistingPrimaryNetworkInterface(NetworkInterface networkInterface) {
        this.existingNetworkInterfaceToAssociate = networkInterface;
        return this;
    }

    // Virtual machine image specific fluent methods
    //

    @Override
    public DefinitionWithMarketplaceImage withMarketplaceImage() {
        return this;
    }

    @Override
    public DefinitionWithOSType withStoredImage(String imageUrl) {
        VirtualHardDisk userImageVhd = new VirtualHardDisk();
        userImageVhd.withUri(imageUrl);
        this.inner().storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().storageProfile().osDisk().withImage(userImageVhd);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDisk(String osDiskUrl, OperatingSystemTypes osType) {
        VirtualHardDisk osDisk = new VirtualHardDisk();
        osDisk.withUri(osDiskUrl);
        this.inner().storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.ATTACH);
        this.inner().storageProfile().osDisk().withVhd(osDisk);
        this.inner().storageProfile().osDisk().withOsType(osType);
        return this;
    }

    @Override
    public DefinitionWithOSType version(ImageReference imageReference) {
        this.inner().storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().storageProfile().withImageReference(imageReference);
        return this;
    }

    @Override
    public DefinitionWithOSType latest(String publisher, String offer, String sku) {
        ImageReference imageReference = new ImageReference();
        imageReference.withPublisher(publisher);
        imageReference.withOffer(offer);
        imageReference.withSku(sku);
        imageReference.withVersion("latest");
        return version(imageReference);
    }

    @Override
    public DefinitionWithOSType popular(KnownVirtualMachineImage knownImage) {
        return version(knownImage.imageReference());
    }

    // Virtual machine operating system type fluent methods
    //

    @Override
    public DefinitionWithRootUserName withLinuxOS() {
        OSDisk osDisk = this.inner().storageProfile().osDisk();
        if (isStoredImage(osDisk)) {
            // For platform image osType should be null, azure will pick it from the image metadata.
            osDisk.withOsType(OperatingSystemTypes.LINUX);
        }
        this.inner().osProfile().withLinuxConfiguration(new LinuxConfiguration());
        return this;
    }

    @Override
    public DefinitionWithAdminUserName withWindowsOS() {
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
    public DefinitionLinuxCreatable withRootUserName(String rootUserName) {
        this.inner().osProfile().withAdminUsername(rootUserName);
        return this;
    }

    @Override
    public DefinitionWindowsCreatable withAdminUserName(String adminUserName) {
        this.inner().osProfile().withAdminUsername(adminUserName);
        return this;
    }

    // Virtual machine optional fluent methods
    //

    @Override
    public DefinitionLinuxCreatable withSsh(String publicKeyData) {
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
    public DefinitionWindowsCreatable disableVMAgent() {
        this.inner().osProfile().windowsConfiguration().withProvisionVMAgent(false);
        return this;
    }

    @Override
    public DefinitionWindowsCreatable disableAutoUpdate() {
        this.inner().osProfile().windowsConfiguration().withEnableAutomaticUpdates(false);
        return this;
    }

    @Override
    public DefinitionWindowsCreatable withTimeZone(String timeZone) {
        this.inner().osProfile().windowsConfiguration().withTimeZone(timeZone);
        return this;
    }

    @Override
    public DefinitionWindowsCreatable withWinRM(WinRMListener listener) {
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
    public DefinitionCreatable withPassword(String password) {
        this.inner().osProfile().withAdminPassword(password);
        return this;
    }

    @Override
    public DefinitionCreatable withSize(String sizeName) {
        this.inner().hardwareProfile().withVmSize(sizeName);
        return this;
    }

    @Override
    public DefinitionCreatable withSize(VirtualMachineSizeTypes size) {
        this.inner().hardwareProfile().withVmSize(size.toString());
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskCaching(CachingTypes cachingType) {
        this.inner().storageProfile().osDisk().withCaching(cachingType);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskVhdLocation(String containerName, String vhdName) {
        VirtualHardDisk osVhd = new VirtualHardDisk();
        osVhd.withUri(temporaryBlobUrl(containerName, vhdName));
        this.inner().storageProfile().osDisk().withVhd(osVhd);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskEncryptionSettings(DiskEncryptionSettings settings) {
        this.inner().storageProfile().osDisk().withEncryptionSettings(settings);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskSizeInGB(Integer size) {
        this.inner().storageProfile().osDisk().withDiskSizeGB(size);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskName(String name) {
        this.inner().storageProfile().osDisk().withName(name);
        return this;
    }

    // Virtual machine optional data disk fluent methods
    //

    @Override
    public DataDiskImpl defineNewDataDisk(String name) {
        return DataDiskImpl.prepareDataDisk(name, DiskCreateOptionTypes.EMPTY, this);
    }

    @Override
    public DataDiskImpl defineExistingDataDisk(String name) {
        return DataDiskImpl.prepareDataDisk(name, DiskCreateOptionTypes.ATTACH, this);
    }

    @Override
    public DefinitionCreatable withNewDataDisk(Integer sizeInGB) {
        DataDiskImpl dataDisk = DataDiskImpl.createNewDataDisk(sizeInGB, this);
        this.dataDisks().add(dataDisk);
        return this;
    }

    @Override
    public DefinitionCreatable withExistingDataDisk(String storageAccountName, String containerName, String vhdName) {
        DataDiskImpl dataDisk = DataDiskImpl.createFromExistingDisk(storageAccountName, containerName, vhdName, this);
        this.dataDisks().add(dataDisk);
        return this;
    }

    // Virtual machine optional storage account fluent methods
    //

    @Override
    public DefinitionCreatable withNewStorageAccount(StorageAccount.DefinitionCreatable creatable) {
        // This method's effect is NOT additive.
        if (this.creatableStorageAccountKey == null) {
            this.creatableStorageAccountKey = creatable.key();
            this.addCreatableDependency(creatable);
        }
        return this;
    }

    @Override
    public DefinitionCreatable withNewStorageAccount(String name) {
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
        return withNewStorageAccount(definitionAfterGroup.withAccountType(AccountType.STANDARD_GRS));
    }

    @Override
    public DefinitionCreatable withExistingStorageAccount(StorageAccount storageAccount) {
        this.existingStorageAccountToAssociate = storageAccount;
        return this;
    }

    // Virtual machine optional availability set fluent methods
    //

    @Override
    public DefinitionCreatable withNewAvailabilitySet(AvailabilitySet.DefinitionCreatable creatable) {
        // This method's effect is NOT additive.
        if (this.creatableAvailabilitySetKey == null) {
            this.creatableAvailabilitySetKey = creatable.key();
            this.addCreatableDependency(creatable);
        }
        return this;
    }

    @Override
    public DefinitionCreatable withNewAvailabilitySet(String name) {
        return withNewAvailabilitySet(availabilitySets.define(name)
                .withRegion(region())
                .withExistingGroup(this.resourceGroupName())
        );
    }

    @Override
    public DefinitionCreatable withExistingAvailabilitySet(AvailabilitySet availabilitySet) {
        this.existingAvailabilitySetToAssociate = availabilitySet;
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
                    .get(ResourceUtils.groupFromResourceId(primaryNicId), ResourceUtils.nameFromResourceId(primaryNicId));
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
    public VMInstanceView instanceView() {
        if (this.vmInstanceView == null) {
            this.vmInstanceView = new VMInstanceViewImpl(this.client, this);
        }
        return this.vmInstanceView;
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

    @Override
    public PowerState powerState() {
        return this.instanceView().powerState();
    }

    /**************************************************.
     * CreatableImpl::createResource
     **************************************************/

    @Override
    protected void createResource() throws Exception {
        // Sets defaults
        if (isInCreateMode()) {
            setOSDiskAndOSProfileDefaults();
            setHardwareProfileDefaults();
        }
        DataDiskImpl.setDataDisksDefaults(this.dataDisks, this.vmName);
        // Ensure various profiles
        handleStorageSettings();
        handleNetworkSettings();
        handleAvailabilitySettings();
        // PUT
        ServiceResponse<VirtualMachineInner> serviceResponse = this.client.createOrUpdate(this.resourceGroupName(), this.vmName, this.inner());
        this.setInner(serviceResponse.getBody());
        // refresh the data disks
        initializeDataDisks();
    }

    /**************************************************.
     * Helper methods
     **************************************************/

    /**
     * @param prefix the prefix
     * @return a random value (derived from the resource and resource group name) with the given prefix
     */
    private String nameWithPrefix(String prefix) {
        return prefix + "-" + this.randomId + "-" + this.resourceGroupName();
    }

    /**
     * This method sets default values for operating system disk and operating system profile properties, which are
     * required during creation time.
     */
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

    /**
     * Hardware profile (vm size) needs to be set when creating the virtual machine, vm size is optional in the
     * fluent model hence if its not selected by the user this method set it to default value.
     */
    private void setHardwareProfileDefaults() {
        if (!isInCreateMode()) {
            return;
        }

        HardwareProfile hardwareProfile = this.inner().hardwareProfile();
        if (hardwareProfile.vmSize() == null) {
            hardwareProfile.withVmSize(VirtualMachineSizeTypes.BASIC_A0);
        }
    }

    /**
     * This method is used to ensures the storage profile in the virtual machine payload for create or update
     * is valid.
     * <p>
     * The OS disk based on an image and new data disks requires storage uri where the backing vhd needs to be
     * stored, this method ensures the uris are correct.
     *
     * @throws Exception
     */
    private void handleStorageSettings() throws Exception {
        StorageAccount storageAccount = null;
        if (this.creatableStorageAccountKey != null) {
            storageAccount = (StorageAccount) this.createdResource(this.creatableStorageAccountKey);
        } else if (this.existingStorageAccountToAssociate != null) {
            storageAccount = this.existingStorageAccountToAssociate;
        } else if (requiresImplicitStorageAccountCreation()) {
            storageAccount = this.storageManager.storageAccounts()
                    .define(nameWithPrefix("stg"))
                    .withRegion(this.region())
                    .withExistingGroup(this.resourceGroupName())
                    .create();
        }

        if (storageAccount != null) {
            // Ensure the Vhd uris for OS Disk (create time) and data disks (create or update time)
            if (this.isInCreateMode()) {
                if (isOSDiskFromImage(this.inner().storageProfile().osDisk())) {
                    String uri = this.inner()
                            .storageProfile()
                            .osDisk().vhd().uri()
                            .replaceFirst("\\{storage-base-url}", storageAccount.endPoints().primary().blob());
                    this.inner().storageProfile().osDisk().vhd().withUri(uri);
                }
            }
            DataDiskImpl.ensureDisksVhdUri(this.dataDisks, storageAccount, this.vmName);
        }
    }

    /**
     * This method is used to ensure the network profile in the virtual machine payload for create or update is
     * valid.
     * <p>
     * a virtual machine requires primary network interface, this method sets the nic
     */
    private void handleNetworkSettings() {
        if (isInCreateMode()) {
            NetworkInterface networkInterface = null;
            if (this.creatableNetworkInterfaceKey != null) {
                networkInterface = (NetworkInterface) this.createdResource(this.creatableNetworkInterfaceKey);
            } else if (this.existingNetworkInterfaceToAssociate != null) {
                networkInterface = this.existingNetworkInterfaceToAssociate;
            }

            if (networkInterface != null) {
                NetworkInterfaceReference nicReference = new NetworkInterfaceReference();
                nicReference.withPrimary(true);
                nicReference.withId(networkInterface.id());
                this.inner().networkProfile().networkInterfaces().add(nicReference);
            }
        }
    }

    /**
     * a virtual machine can be associated with an availability set during creation time, this method is used to
     * ensure availability set reference is set if user asked for it.
     */
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

    /**
     * The storage account is an optional parameter in the virtual machine create or update fluent flow, but
     * sometime we need to create it implicitly even though user not opted for it, some scenarios are:
     * creating a virtual machine from image, attaching empty data disks
     *
     * @return @return <tt>true</tt> if storage account needs to be created implicitly
     */
    private boolean requiresImplicitStorageAccountCreation() {
        if (this.creatableStorageAccountKey == null && this.existingStorageAccountToAssociate == null) {
            if (this.isInCreateMode()) {
                if (isOSDiskFromImage(this.inner().storageProfile().osDisk())) {
                    return true;
                }
            }
            for (DataDisk dataDisk : this.dataDisks) {
                if (dataDisk.createOption() == DiskCreateOptionTypes.EMPTY) {
                    if (dataDisk.vhdUri() == null) {
                        return true;
                    }
                }
            }
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
        return "{storage-base-url}" + "/" + containerName + "/" + blobName;
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
}