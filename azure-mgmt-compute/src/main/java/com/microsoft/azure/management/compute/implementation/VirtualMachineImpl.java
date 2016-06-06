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
                       final ResourceManager resourceManager,
                       final StorageManager storageManager,
                       final NetworkManager networkManager) {
        super(name, innerModel, resourceManager.resourceGroups());
        this.client = client;
        this.networkInterfaces = networkInterfaces;
        this.availabilitySets = availabilitySets;
        this.resourceManager = resourceManager;
        this.storageManager = storageManager;
        this.networkManager = networkManager;
        initialize(name);
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
        userImageVhd.setUri(imageUrl);
        this.inner().storageProfile().osDisk().setCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().storageProfile().osDisk().setImage(userImageVhd);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDisk(String osDiskUrl, OperatingSystemTypes osType) {
        VirtualHardDisk osDisk = new VirtualHardDisk();
        osDisk.setUri(osDiskUrl);
        this.inner().storageProfile().osDisk().setCreateOption(DiskCreateOptionTypes.ATTACH);
        this.inner().storageProfile().osDisk().setVhd(osDisk);
        this.inner().storageProfile().osDisk().setOsType(osType);
        return this;
    }

    @Override
    public DefinitionWithOSType version(ImageReference imageReference) {
        this.inner().storageProfile().osDisk().setCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.inner().storageProfile().setImageReference(imageReference);
        return this;
    }

    @Override
    public DefinitionWithOSType latest(String publisher, String offer, String sku) {
        ImageReference imageReference = new ImageReference();
        imageReference.setPublisher(publisher);
        imageReference.setOffer(offer);
        imageReference.setSku(sku);
        imageReference.setVersion("latest");
        return version(imageReference);
    }

    @Override
    public DefinitionWithOSType popular(KnownVirtualMachineImage knownImage) {
        return version(knownImage.imageReference());
    }

    @Override
    public DefinitionWithRootUserName withLinuxOS() {
        OSDisk osDisk = this.inner().storageProfile().osDisk();
        if (isStoredImage(osDisk)) {
            // For platform image osType should be null, azure will pick it from the image metadata.
            osDisk.setOsType(OperatingSystemTypes.LINUX);
        }
        this.inner().osProfile().setLinuxConfiguration(new LinuxConfiguration());
        return this;
    }

    @Override
    public DefinitionWithAdminUserName withWindowsOS() {
        OSDisk osDisk = this.inner().storageProfile().osDisk();
        if (isStoredImage(osDisk)) {
            // For platform image osType should be null, azure will pick it from the image metadata.
            osDisk.setOsType(OperatingSystemTypes.WINDOWS);
        }
        this.inner().osProfile().setWindowsConfiguration(new WindowsConfiguration());
        // sets defaults for "Stored(User)Image" or "VM(Platform)Image"
        this.inner().osProfile().windowsConfiguration().setProvisionVMAgent(true);
        this.inner().osProfile().windowsConfiguration().setEnableAutomaticUpdates(true);
        return this;
    }

    @Override
    public DefinitionLinuxCreatable withRootUserName(String rootUserName) {
        this.inner().osProfile().setAdminUsername(rootUserName);
        return this;
    }

    @Override
    public DefinitionWindowsCreatable withAdminUserName(String adminUserName) {
        this.inner().osProfile().setAdminUsername(adminUserName);
        return this;
    }

    @Override
    public DefinitionLinuxCreatable withSsh(String publicKeyData) {
        OSProfile osProfile = this.inner().osProfile();
        if (osProfile.linuxConfiguration().ssh() == null) {
            SshConfiguration sshConfiguration = new SshConfiguration();
            sshConfiguration.setPublicKeys(new ArrayList<SshPublicKey>());
            osProfile.linuxConfiguration().setSsh(sshConfiguration);
        }
        SshPublicKey sshPublicKey = new SshPublicKey();
        sshPublicKey.setKeyData(publicKeyData);
        sshPublicKey.setPath("/home/" + osProfile.adminUsername() + "/.ssh/authorized_keys");
        osProfile.linuxConfiguration().ssh().publicKeys().add(sshPublicKey);
        return this;
    }

    @Override
    public DefinitionWindowsCreatable disableVMAgent() {
        this.inner().osProfile().windowsConfiguration().setProvisionVMAgent(false);
        return this;
    }

    @Override
    public DefinitionWindowsCreatable disableAutoUpdate() {
        this.inner().osProfile().windowsConfiguration().setEnableAutomaticUpdates(false);
        return this;
    }

    @Override
    public DefinitionWindowsCreatable withTimeZone(String timeZone) {
        this.inner().osProfile().windowsConfiguration().setTimeZone(timeZone);
        return this;
    }

    @Override
    public DefinitionWindowsCreatable withWinRM(WinRMListener listener) {
        if (this.inner().osProfile().windowsConfiguration().winRM() == null) {
            WinRMConfiguration winRMConfiguration = new WinRMConfiguration();
            this.inner().osProfile().windowsConfiguration().setWinRM(winRMConfiguration);
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
        this.inner().osProfile().setAdminPassword(password);
        return this;
    }

    @Override
    public DefinitionCreatable withSize(String sizeName) {
        this.inner().hardwareProfile().setVmSize(sizeName);
        return this;
    }

    @Override
    public DefinitionCreatable withSize(VirtualMachineSizeTypes size) {
        this.inner().hardwareProfile().setVmSize(size.toString());
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskCaching(CachingTypes cachingType) {
        this.inner().storageProfile().osDisk().setCaching(cachingType);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskVhdLocation(String containerName, String vhdName) {
        VirtualHardDisk osVhd = new VirtualHardDisk();
        osVhd.setUri(blobUrl(this.storageAccountName, containerName, vhdName));
        this.inner().storageProfile().osDisk().setVhd(osVhd);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskEncryptionSettings(DiskEncryptionSettings settings) {
        this.inner().storageProfile().osDisk().setEncryptionSettings(settings);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskSizeInGB(Integer size) {
        this.inner().storageProfile().osDisk().setDiskSizeGB(size);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskName(String name) {
        this.inner().storageProfile().osDisk().setName(name);
        return this;
    }

    // Virtual machine data disk fluent methods
    //

    @Override
    public ConfigureDataDisk<DefinitionCreatable> withLun(Integer lun) {
        DataDisk dataDisk = currentDataDisk();
        dataDisk.setLun(lun);
        return this;
    }

    @Override
    public ConfigureDataDisk<DefinitionCreatable> withCaching(CachingTypes cachingType) {
        DataDisk dataDisk = currentDataDisk();
        dataDisk.setCaching(cachingType);
        return this;
    }

    @Override
    public DefinitionCreatable attach() {
        return this;
    }

    @Override
    public ConfigureDataDisk<DefinitionCreatable> storeAt(String storageAccountName, String containerName, String vhdName) {
        DataDisk dataDisk = currentDataDisk();
        dataDisk.setVhd(new VirtualHardDisk());
        dataDisk.vhd().setUri(blobUrl(storageAccountName, containerName, vhdName)); // URL points to where the new data disk needs to be stored.
        return this;
    }

    @Override
    public ConfigureNewDataDiskWithStoreAt<DefinitionCreatable> withSizeInGB(Integer sizeInGB) {
        DataDisk dataDisk = currentDataDisk();
        dataDisk.setDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public ConfigureDataDisk<DefinitionCreatable> from(String storageAccountName, String containerName, String vhdName) {
        DataDisk dataDisk = currentDataDisk();
        dataDisk.setVhd(new VirtualHardDisk());
        dataDisk.vhd().setUri(blobUrl(storageAccountName, containerName, vhdName)); // URL points to an existing data disk to be attached.
        return this;
    }

    @Override
    public ConfigureNewDataDisk<DefinitionCreatable> defineNewDataDisk(String name) {
        DataDisk dataDisk = prepareNewDataDisk();
        dataDisk.setName(name);
        dataDisk.setCreateOption(DiskCreateOptionTypes.EMPTY);
        return this;
    }

    @Override
    public ConfigureExistingDataDisk<DefinitionCreatable> defineExistingDataDisk(String name) {
        DataDisk dataDisk = prepareNewDataDisk();
        dataDisk.setName(name);
        dataDisk.setCreateOption(DiskCreateOptionTypes.ATTACH);
        return this;
    }

    @Override
    public DefinitionCreatable withNewDataDisk(Integer sizeInGB) {
        DataDisk dataDisk = prepareNewDataDisk();
        dataDisk.setDiskSizeGB(sizeInGB);
        dataDisk.setCreateOption(DiskCreateOptionTypes.EMPTY);
        return this;
    }

    @Override
    public DefinitionCreatable withExistingDataDisk(String storageAccountName, String containerName, String vhdName) {
        DataDisk dataDisk = prepareNewDataDisk();
        VirtualHardDisk diskVhd = new VirtualHardDisk();
        diskVhd.setUri(blobUrl(storageAccountName, containerName, vhdName));
        dataDisk.setVhd(diskVhd);
        dataDisk.setCreateOption(DiskCreateOptionTypes.ATTACH);
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
        OSDisk osDisk = this.inner().storageProfile().osDisk();
        if (!isOSDiskAttached(osDisk)) {
            if (osDisk.vhd() == null) {
                // Sets the OS disk VHD for "UserImage" and "VM(Platform)Image"
                withOSDiskVhdLocation("vhds", this.key() + "-os-disk-" + UUID.randomUUID().toString() + ".vhd");
            }
            OSProfile osProfile = this.inner().osProfile();
            if (osDisk.osType() == OperatingSystemTypes.LINUX) {
                if (osProfile.linuxConfiguration() == null) {
                    osProfile.setLinuxConfiguration(new LinuxConfiguration());
                }
                this.inner().osProfile().linuxConfiguration().setDisablePasswordAuthentication(osProfile.adminPassword() == null);
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
        HardwareProfile hardwareProfile = this.inner().hardwareProfile();
        if (hardwareProfile.vmSize() == null) {
            hardwareProfile.setVmSize(VirtualMachineSizeTypes.BASIC_A0);
        }
    }

    private void setDataDisksDefaults() {
        List<DataDisk> dataDisks = this.inner().storageProfile().dataDisks();
        if (dataDisks.size() == 0) {
            this.inner().storageProfile().setDataDisks(null);
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
                dataDisk.setLun(i);
                usedLuns.add(i);
            }

            if (dataDisk.vhd() == null) {
                VirtualHardDisk diskVhd = new VirtualHardDisk();
                diskVhd.setUri(blobUrl(this.storageAccountName, "vhds",
                        this.key() + "-data-disk-" + dataDisk.lun() + "-" + UUID.randomUUID().toString() + ".vhd"));
                dataDisk.setVhd(diskVhd);
            }

            if (dataDisk.name() == null) {
                dataDisk.setName(this.key() + "-data-disk-" + dataDisk.lun());
            }

            if (dataDisk.caching() == null) {
                dataDisk.setCaching(CachingTypes.READ_WRITE);
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
        dataDisk.setLun(-1);
        this.inner().storageProfile().dataDisks().add(dataDisk);
        return dataDisk;
    }

    private DataDisk currentDataDisk() {
        List<DataDisk> dataDisks = this.inner().storageProfile().dataDisks();
        return dataDisks.get(dataDisks.size() - 1);
    }

    private String blobUrl(String storageAccountName, String containerName, String blobName) {
        return  "https://" + storageAccountName + ".blob.core.windows.net" + "/" + containerName + "/" + blobName;
    }

    private boolean requiresImplicitStorageAccountCreation() {
        if (this.storageAccountName == null) {
            if (!isOSDiskAttached(this.inner().storageProfile().osDisk())) {
                return true;
            }

            for (DataDisk dataDisk : this.inner().storageProfile().dataDisks()) {
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
        subnetInner.setId(virtualNetwork.inner().subnets().get(0).id());

        NetworkInterfaceInner networkInterfaceInner = new NetworkInterfaceInner();
        networkInterfaceInner.setLocation(this.region());
        networkInterfaceInner.setPrimary(true);
        NetworkInterfaceIPConfiguration nicIPConfig = new NetworkInterfaceIPConfiguration();
        nicIPConfig.setName("Nic-IP-config");
        nicIPConfig.setSubnet(subnetInner);
        ArrayList<NetworkInterfaceIPConfiguration> nicIPConfigs = new ArrayList<>();
        nicIPConfigs.add(nicIPConfig);
        networkInterfaceInner.setIpConfigurations(nicIPConfigs);

        try {
            ServiceResponse<NetworkInterfaceInner> newNic =
                    networkInterfaces.createOrUpdate(this.resourceGroupName(), this.primaryNetworkInterfaceName, networkInterfaceInner);
            NetworkInterfaceReference nicReference = new NetworkInterfaceReference();
            nicReference.setPrimary(true);
            nicReference.setId(newNic.getBody().id());
            return nicReference;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void createResource() throws Exception {
        // TODO This code to create NIC will be removed once we have the fluent model for NIC in place.
        NetworkInterfaceReference nicReference = createPrimaryNetworkInterface();
        this.inner().networkProfile().setNetworkInterfaces(new ArrayList<NetworkInterfaceReference>());
        this.inner().networkProfile().networkInterfaces().add(nicReference);

        setDefaults();
        ServiceResponse<VirtualMachineInner> serviceResponse = this.client.createOrUpdate(this.resourceGroupName(), this.key(), this.inner());
        this.setInner(serviceResponse.getBody());
    }

    private void initialize(String name) {
        if (this.inner().id() == null) {
            this.inner().setStorageProfile(new StorageProfile());
            this.inner().storageProfile().setOsDisk(new OSDisk());
            this.inner().storageProfile().setDataDisks(new ArrayList<DataDisk>());
            this.inner().setOsProfile(new OSProfile());
            this.inner().setHardwareProfile(new HardwareProfile());
            this.inner().setNetworkProfile(new NetworkProfile());
            this.inner().osProfile().setComputerName(name);
        }
    }
}
