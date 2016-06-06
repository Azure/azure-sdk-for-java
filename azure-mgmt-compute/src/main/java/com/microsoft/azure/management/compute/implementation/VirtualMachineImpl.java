package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineInner;
import com.microsoft.azure.management.compute.implementation.api.Plan;
import com.microsoft.azure.management.compute.implementation.api.HardwareProfile;
import com.microsoft.azure.management.compute.implementation.api.StorageProfile;
import com.microsoft.azure.management.compute.implementation.api.OSProfile;
import com.microsoft.azure.management.compute.implementation.api.NetworkProfile;
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
import com.microsoft.azure.management.compute.implementation.api.DataDisk;
import com.microsoft.azure.management.compute.implementation.api.LinuxConfiguration;
import com.microsoft.azure.management.compute.implementation.api.WindowsConfiguration;
import com.microsoft.azure.management.compute.implementation.api.WinRMConfiguration;
import com.microsoft.azure.management.compute.implementation.api.SshConfiguration;
import com.microsoft.azure.management.compute.implementation.api.SshPublicKey;
import com.microsoft.azure.management.compute.implementation.api.NetworkInterfaceReference;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.azure.management.network.implementation.api.SubnetInner;
import com.microsoft.azure.management.network.implementation.api.NetworkInterfaceInner;
import com.microsoft.azure.management.network.implementation.api.NetworkInterfacesInner;
import com.microsoft.azure.management.network.implementation.api.NetworkInterfaceIPConfiguration;
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
            VirtualMachine.DefinitionCreatable,
            VirtualMachine.ConfigureDataDisk,
            VirtualMachine.ConfigureNewDataDiskWithStoreAt,
            VirtualMachine.ConfigureNewDataDisk,
            VirtualMachine.ConfigureExistingDataDisk {
    private final VirtualMachinesInner client;
    private final VirtualMachineInner innerModel;
    private final AvailabilitySets availabilitySets;
    private final NetworkInterfacesInner networkInterfaces;
    private final ResourceManager resourceManager;
    private final StorageManager storageManager;
    private final NetworkManager networkManager;

    private String storageAccountName;
    private String availabilitySetName;
    private String primaryNetworkInterfaceName;

    VirtualMachineImpl(String name,
                       VirtualMachineInner innerModel,
                       VirtualMachinesInner client,
                       NetworkInterfacesInner networkInterfaces, // TODO this will be removed once we have NetworkInterfaces entry point available in NetworkManager
                       AvailabilitySets availabilitySets,
                       ResourceManager resourceManager,
                       StorageManager storageManager,
                       NetworkManager networkManager) {
        super(name, innerModel, resourceManager.resourceGroups());
        this.client = client;
        this.innerModel = innerModel;
        this.networkInterfaces = networkInterfaces;
        this.availabilitySets = availabilitySets;
        this.resourceManager = resourceManager;
        this.storageManager = storageManager;
        this.networkManager = networkManager;

        this.innerModel.withStorageProfile(new StorageProfile());
        this.innerModel.storageProfile().withOsDisk(new OSDisk());
        this.innerModel.storageProfile().withDataDisks(new ArrayList<DataDisk>());
        this.innerModel.withOsProfile(new OSProfile());
        this.innerModel.withHardwareProfile(new HardwareProfile());
        this.innerModel.withNetworkProfile(new NetworkProfile());
        this.innerModel.osProfile().withComputerName(name);
    }

    @Override
    public Plan plan() {
        return inner().plan();
    }

    @Override
    public HardwareProfile hardwareProfile() {
        return inner().hardwareProfile();
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
    public NetworkProfile networkProfile() {
        return inner().networkProfile();
    }

    @Override
    public DiagnosticsProfile diagnosticsProfile() {
        return inner().diagnosticsProfile();
    }

    @Override
    public SubResource availabilitySet() {
        return inner().availabilitySet();
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
    public VirtualMachine refresh() throws Exception {
        return this;
    }

    @Override
    public DefinitionWithOS withNewPrimaryNetworkInterface(String name) {
        this.primaryNetworkInterfaceName = name;
        return this;
    }

    @Override
    public DefinitionWithMarketplaceImage withMarketplaceImage() {
        return this;
    }

    @Override
    public DefinitionWithOSType withStoredImage(String imageUrl) {
        VirtualHardDisk userImageVhd = new VirtualHardDisk();
        userImageVhd.withUri(imageUrl);
        this.innerModel.storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.innerModel.storageProfile().osDisk().withImage(userImageVhd);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDisk(String osDiskUrl, OperatingSystemTypes osType) {
        VirtualHardDisk osDisk = new VirtualHardDisk();
        osDisk.withUri(osDiskUrl);
        this.innerModel.storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.ATTACH);
        this.innerModel.storageProfile().osDisk().withVhd(osDisk);
        this.innerModel.storageProfile().osDisk().withOsType(osType);
        return this;
    }

    @Override
    public DefinitionWithOSType version(ImageReference imageReference) {
        this.innerModel.storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.innerModel.storageProfile().withImageReference(imageReference);
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

    @Override
    public DefinitionWithRootUserName withLinuxOS() {
        OSDisk osDisk = this.innerModel.storageProfile().osDisk();
        if (isStoredImage(osDisk)) {
            // For platform image osType should be null, azure will pick it from the image metadata.
            osDisk.withOsType(OperatingSystemTypes.LINUX);
        }
        this.innerModel.osProfile().withLinuxConfiguration(new LinuxConfiguration());
        return this;
    }

    @Override
    public DefinitionWithAdminUserName withWindowsOS() {
        OSDisk osDisk = this.innerModel.storageProfile().osDisk();
        if (isStoredImage(osDisk)) {
            // For platform image osType should be null, azure will pick it from the image metadata.
            osDisk.withOsType(OperatingSystemTypes.WINDOWS);
        }
        this.innerModel.osProfile().withWindowsConfiguration(new WindowsConfiguration());
        // sets defaults for "Stored(User)Image" or "VM(Platform)Image"
        this.innerModel.osProfile().windowsConfiguration().withProvisionVMAgent(true);
        this.innerModel.osProfile().windowsConfiguration().withEnableAutomaticUpdates(true);
        return this;
    }

    @Override
    public DefinitionLinuxCreatable withRootUserName(String rootUserName) {
        this.innerModel.osProfile().withAdminUsername(rootUserName);
        return this;
    }

    @Override
    public DefinitionWindowsCreatable withAdminUserName(String adminUserName) {
        this.innerModel.osProfile().withAdminUsername(adminUserName);
        return this;
    }

    @Override
    public DefinitionLinuxCreatable withSsh(String publicKeyData) {
        OSProfile osProfile = this.innerModel.osProfile();
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
        this.innerModel.osProfile().windowsConfiguration().withProvisionVMAgent(false);
        return this;
    }

    @Override
    public DefinitionWindowsCreatable disableAutoUpdate() {
        this.innerModel.osProfile().windowsConfiguration().withEnableAutomaticUpdates(false);
        return this;
    }

    @Override
    public DefinitionWindowsCreatable withTimeZone(String timeZone) {
        this.innerModel.osProfile().windowsConfiguration().withTimeZone(timeZone);
        return this;
    }

    @Override
    public DefinitionWindowsCreatable withWinRM(WinRMListener listener) {
        if (this.innerModel.osProfile().windowsConfiguration().winRM() == null) {
            WinRMConfiguration winRMConfiguration = new WinRMConfiguration();
            this.innerModel.osProfile().windowsConfiguration().withWinRM(winRMConfiguration);
        }

        this.innerModel.osProfile()
                .windowsConfiguration()
                .winRM()
                .listeners()
                .add(listener);
        return this;
    }

    @Override
    public DefinitionCreatable withPassword(String password) {
        this.innerModel.osProfile().withAdminPassword(password);
        return this;
    }

    @Override
    public DefinitionCreatable withSize(String sizeName) {
        this.innerModel.hardwareProfile().withVmSize(sizeName);
        return this;
    }

    @Override
    public DefinitionCreatable withSize(VirtualMachineSizeTypes size) {
        this.innerModel.hardwareProfile().withVmSize(size.toString());
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskCaching(CachingTypes cachingType) {
        this.innerModel.storageProfile().osDisk().withCaching(cachingType);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskVhdLocation(String containerName, String vhdName) {
        VirtualHardDisk osVhd = new VirtualHardDisk();
        osVhd.withUri(blobUrl(this.storageAccountName, containerName, vhdName));
        this.innerModel.storageProfile().osDisk().withVhd(osVhd);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskEncryptionSettings(DiskEncryptionSettings settings) {
        this.innerModel.storageProfile().osDisk().withEncryptionSettings(settings);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskSizeInGB(Integer size) {
        this.innerModel.storageProfile().osDisk().withDiskSizeGB(size);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskName(String name) {
        this.innerModel.storageProfile().osDisk().withName(name);
        return this;
    }

    // Virtual machine data disk fluent methods
    //

    @Override
    public ConfigureDataDisk<DefinitionCreatable> withLun(Integer lun) {
        DataDisk dataDisk = currentDataDisk();
        dataDisk.withLun(lun);
        return this;
    }

    @Override
    public ConfigureDataDisk<DefinitionCreatable> withCaching(CachingTypes cachingType) {
        DataDisk dataDisk = currentDataDisk();
        dataDisk.withCaching(cachingType);
        return this;
    }

    @Override
    public DefinitionCreatable attach() {
        return this;
    }

    @Override
    public ConfigureDataDisk<DefinitionCreatable> storeAt(String storageAccountName, String containerName, String vhdName) {
        DataDisk dataDisk = currentDataDisk();
        dataDisk.withVhd(new VirtualHardDisk());
        dataDisk.vhd().withUri(blobUrl(storageAccountName, containerName, vhdName)); // URL points to where the new data disk needs to be stored.
        return this;
    }

    @Override
    public ConfigureNewDataDiskWithStoreAt<DefinitionCreatable> withSizeInGB(Integer sizeInGB) {
        DataDisk dataDisk = currentDataDisk();
        dataDisk.withDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public ConfigureDataDisk<DefinitionCreatable> from(String storageAccountName, String containerName, String vhdName) {
        DataDisk dataDisk = currentDataDisk();
        dataDisk.withVhd(new VirtualHardDisk());
        dataDisk.vhd().withUri(blobUrl(storageAccountName, containerName, vhdName)); // URL points to an existing data disk to be attached.
        return this;
    }

    @Override
    public ConfigureNewDataDisk<DefinitionCreatable> defineNewDataDisk(String name) {
        DataDisk dataDisk = prepareNewDataDisk();
        dataDisk.withName(name);
        dataDisk.withCreateOption(DiskCreateOptionTypes.EMPTY);
        return this;
    }

    @Override
    public ConfigureExistingDataDisk<DefinitionCreatable> defineExistingDataDisk(String name) {
        DataDisk dataDisk = prepareNewDataDisk();
        dataDisk.withName(name);
        dataDisk.withCreateOption(DiskCreateOptionTypes.ATTACH);
        return this;
    }

    @Override
    public DefinitionCreatable withNewDataDisk(Integer sizeInGB) {
        DataDisk dataDisk = prepareNewDataDisk();
        dataDisk.withDiskSizeGB(sizeInGB);
        dataDisk.withCreateOption(DiskCreateOptionTypes.EMPTY);
        return this;
    }

    @Override
    public DefinitionCreatable withExistingDataDisk(String storageAccountName, String containerName, String vhdName) {
        DataDisk dataDisk = prepareNewDataDisk();
        VirtualHardDisk diskVhd = new VirtualHardDisk();
        diskVhd.withUri(blobUrl(storageAccountName, containerName, vhdName));
        dataDisk.withVhd(diskVhd);
        dataDisk.withCreateOption(DiskCreateOptionTypes.ATTACH);
        return this;
    }

    // Virtual machine storage account fluent methods
    //

    @Override
    public DefinitionCreatable withNewStorageAccount(String name) {
        return withNewStorageAccount(storageManager.storageAccounts().define(name)
                .withRegion(region())
                .withExistingGroup(this.resourceGroupName()));
    }

    @Override
    public DefinitionCreatable withNewStorageAccount(StorageAccount.DefinitionCreatable creatable) {
        this.storageAccountName = creatable.key();
        this.addCreatableDependency(creatable);
        return this;
    }

    @Override
    public DefinitionCreatable withExistingStorageAccount(String name) {
        this.storageAccountName = name;
        return this;
    }

    // Virtual machine availability set fluent methods
    //

    @Override
    public DefinitionCreatable withNewAvailabilitySet(String name) {
        return withNewAvailabilitySet(availabilitySets.define(name)
                .withRegion(region())
                .withExistingGroup(this.resourceGroupName())
        );
    }

    @Override
    public DefinitionCreatable withNewAvailabilitySet(AvailabilitySet.DefinitionCreatable creatable) {
        this.availabilitySetName = creatable.key();
        this.addCreatableDependency(creatable);
        return this;
    }

    @Override
    public DefinitionCreatable withExistingAvailabilitySet(String name) {
        this.availabilitySetName = name;
        return this;
    }

    @Override
    public VirtualMachine create() throws Exception {
        if (requiresImplicitStorageAccountCreation()) {
            withNewStorageAccount(this.key() + UUID.randomUUID().toString());
        }

        super.creatablesCreate();
        return this;
    }

    // helper methods to set various virtual machine's default properties
    //

    private void setDefaults() {
            setOSDiskAndOSProfileDefaults();
            setHardwareProfileDefaults();
            setDataDisksDefaults();
    }

    private void setOSDiskAndOSProfileDefaults() {
        OSDisk osDisk = this.innerModel.storageProfile().osDisk();
        if (!isOSDiskAttached(osDisk)) {
            if (osDisk.vhd() == null) {
                // Sets the OS disk VHD for "UserImage" and "VM(Platform)Image"
                withOSDiskVhdLocation("vhds", this.key() + "-os-disk-" + UUID.randomUUID().toString() + ".vhd");
            }
            OSProfile osProfile = this.innerModel.osProfile();
            if (osDisk.osType() == OperatingSystemTypes.LINUX) {
                if (osProfile.linuxConfiguration() == null) {
                    osProfile.withLinuxConfiguration(new LinuxConfiguration());
                }
                this.innerModel.osProfile().linuxConfiguration().withDisablePasswordAuthentication(osProfile.adminPassword() == null);
            }
        }

        if (osDisk.caching() == null) {
            withOSDiskCaching(CachingTypes.READ_WRITE);
        }

        if (osDisk.name() == null) {
            withOSDiskName(this.key() + "-os-disk");
        }
    }

    private void setHardwareProfileDefaults() {
        HardwareProfile hardwareProfile = this.innerModel.hardwareProfile();
        if (hardwareProfile.vmSize() == null) {
            hardwareProfile.withVmSize(VirtualMachineSizeTypes.BASIC_A0);
        }
    }

    private void setDataDisksDefaults() {
        List<DataDisk> dataDisks = this.innerModel.storageProfile().dataDisks();
        if (dataDisks.size() == 0) {
            this.innerModel.storageProfile().withDataDisks(null);
            return;
        }

        List<Integer> usedLuns = new ArrayList<>();
        for (DataDisk dataDisk : dataDisks) {
            if (dataDisk.lun() != -1) {
                usedLuns.add(dataDisk.lun());
            }
        }

        for (DataDisk dataDisk : dataDisks) {
            if (dataDisk.lun() == -1) {
                Integer i = 0;
                while (usedLuns.contains(i)) {
                    i++;
                }
                dataDisk.withLun(i);
                usedLuns.add(i);
            }

            if (dataDisk.vhd() == null) {
                VirtualHardDisk diskVhd = new VirtualHardDisk();
                diskVhd.withUri(blobUrl(this.storageAccountName, "vhds",
                        this.key() + "-data-disk-" + dataDisk.lun() + "-" + UUID.randomUUID().toString() + ".vhd"));
                dataDisk.withVhd(diskVhd);
            }

            if (dataDisk.name() == null) {
                dataDisk.withName(this.key() + "-data-disk-" + dataDisk.lun());
            }

            if (dataDisk.caching() == null) {
                dataDisk.withCaching(CachingTypes.READ_WRITE);
            }
        }
    }

    // Helper methods
    //

    private boolean isStoredImage(OSDisk osDisk) {
        return osDisk.image() != null;
    }

    private boolean isOSDiskAttached(OSDisk osDisk) {
        return osDisk.createOption() == DiskCreateOptionTypes.ATTACH;
    }

    private DataDisk prepareNewDataDisk() {
        DataDisk dataDisk = new DataDisk();
        dataDisk.withLun(-1);
        this.innerModel.storageProfile().dataDisks().add(dataDisk);
        return dataDisk;
    }

    private DataDisk currentDataDisk() {
        List<DataDisk> dataDisks = this.innerModel.storageProfile().dataDisks();
        return dataDisks.get(dataDisks.size() - 1);
    }

    private String blobUrl(String storageAccountName, String containerName, String blobName) {
        return  "https://" + storageAccountName + ".blob.core.windows.net" + "/" + containerName + "/" + blobName;
    }

    private boolean requiresImplicitStorageAccountCreation() {
        if (this.storageAccountName == null) {
            if (!isOSDiskAttached(this.innerModel.storageProfile().osDisk())) {
                return true;
            }

            for (DataDisk dataDisk : this.innerModel.storageProfile().dataDisks()) {
                if (dataDisk.createOption() != DiskCreateOptionTypes.ATTACH) {
                    return true;
                }
            }
        }
        return false;
    }

    private NetworkInterfaceReference createPrimaryNetworkInterface() {
        /**
        // Note: This is a temporary code Once we have the fluent model for NIC, we will be simply doing
        //
        Network.DefinitionCreatable networkCreatable = networkManager.networks().define("vnet1")
                .withRegion(this.region())
                .withExistingGroup(this.resourceGroupName());

         NetworkInterface networkInterface = networkManager.networkInterfaces().define(this.primaryNetworkInterfaceName)
           .withNewNetwork(networkCreatable)
           .create();

         return networkInterface;
         **/

        Network virtualNetwork;
        try {
            virtualNetwork = networkManager.networks().define("vnet1-" + this.key())
                    .withRegion(this.region())
                    .withExistingGroup(this.resourceGroupName())
                    .create();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        SubnetInner subnetInner = new SubnetInner();
        subnetInner.withId(virtualNetwork.inner().subnets().get(0).id());

        NetworkInterfaceInner networkInterfaceInner = new NetworkInterfaceInner();
        networkInterfaceInner.withLocation(this.region());
        networkInterfaceInner.withPrimary(true);
        NetworkInterfaceIPConfiguration nicIPConfig = new NetworkInterfaceIPConfiguration();
        nicIPConfig.withName("Nic-IP-config");
        nicIPConfig.withSubnet(subnetInner);
        ArrayList<NetworkInterfaceIPConfiguration> nicIPConfigs = new ArrayList<>();
        nicIPConfigs.add(nicIPConfig);
        networkInterfaceInner.withIpConfigurations(nicIPConfigs);

        try {
            ServiceResponse<NetworkInterfaceInner> newNic =
                    networkInterfaces.createOrUpdate(this.resourceGroupName(), this.primaryNetworkInterfaceName, networkInterfaceInner);
            NetworkInterfaceReference nicReference = new NetworkInterfaceReference();
            nicReference.withPrimary(true);
            nicReference.withId(newNic.getBody().id());
            return nicReference;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void createResource() throws Exception {
        // TODO This code to create NIC will be removed once we have the fluent model for NIC in place.
        NetworkInterfaceReference nicReference = createPrimaryNetworkInterface();
        this.innerModel.networkProfile().withNetworkInterfaces(new ArrayList<NetworkInterfaceReference>());
        this.innerModel.networkProfile().networkInterfaces().add(nicReference);

        setDefaults();
        ServiceResponse<VirtualMachineInner> serviceResponse = this.client.createOrUpdate(this.resourceGroupName(), this.name(), this.innerModel);
        this.withInner(serviceResponse.getBody());
    }
}
