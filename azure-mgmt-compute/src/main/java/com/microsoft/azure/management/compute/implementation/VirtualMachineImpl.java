package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.implementation.api.*;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.implementation.StorageManager;

import java.util.ArrayList;
import java.util.List;

class VirtualMachineImpl
        extends GroupableResourceImpl<VirtualMachine, VirtualMachineInner, VirtualMachineImpl>
        implements
            VirtualMachine,
            VirtualMachine.DefinitionBlank,
            VirtualMachine.DefinitionWithGroup,
            VirtualMachine.DefinitionWithOS,
        VirtualMachine.DefinitionWithMarketplaceImage,
            VirtualMachine.DefinitionWithOSType,
            VirtualMachine.DefinitionWithRootUserName,
            VirtualMachine.DefinitionWithAdminUserName,
            VirtualMachine.DefinitionLinuxCreatable,
            VirtualMachine.DefinitionWindowsCreatable,
            VirtualMachine.DefinitionCreatable {
    private final VirtualMachinesInner client;
    private final VirtualMachineInner innerModel;
    private final ResourceManager resourceManager;
    private final StorageManager storageManager;

    private String storageAccountName;

    VirtualMachineImpl(String name, VirtualMachineInner innerModel, VirtualMachinesInner client,
                       ResourceManager resourceManager, StorageManager storageManager) {
        super(name, innerModel, resourceManager.resourceGroups());
        this.client = client;
        this.innerModel = innerModel;
        this.resourceManager = resourceManager;
        this.storageManager = storageManager;

        this.innerModel.setStorageProfile(new StorageProfile());
        this.innerModel.storageProfile().setOsDisk(new OSDisk());
        this.innerModel.setOsProfile(new OSProfile());
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
    public DefinitionWithMarketplaceImage withMarketplaceImage() {
        return this;
    }

    @Override
    public DefinitionWithOSType withStoredImage(String imageUrl) {
        VirtualHardDisk userImageVhd = new VirtualHardDisk();
        userImageVhd.setUri(imageUrl);
        this.innerModel.storageProfile().osDisk().setCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.innerModel.storageProfile().osDisk().setImage(userImageVhd);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDisk(String osDiskUrl, OperatingSystemTypes osType) {
        VirtualHardDisk osDisk = new VirtualHardDisk();
        osDisk.setUri(osDiskUrl);
        this.innerModel.storageProfile().osDisk().setCreateOption(DiskCreateOptionTypes.ATTACH);
        this.innerModel.storageProfile().osDisk().setVhd(osDisk);
        this.innerModel.storageProfile().osDisk().setOsType(osType);
        return this;
    }

    @Override
    public DefinitionWithOSType version(ImageReference imageReference) {
        this.innerModel.storageProfile().osDisk().setCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.innerModel.storageProfile().setImageReference(imageReference);
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
        OSDisk osDisk = this.innerModel.storageProfile().osDisk();
        if (isStoredImage(osDisk)) {
            // For platform image osType should be null, azure will pick it from the image metadata.
            osDisk.setOsType(OperatingSystemTypes.LINUX);
        }
        this.innerModel.osProfile().setLinuxConfiguration(new LinuxConfiguration());
        return this;
    }

    @Override
    public DefinitionWithAdminUserName withWindowsOS() {
        OSDisk osDisk = this.innerModel.storageProfile().osDisk();
        if (isStoredImage(osDisk)) {
            // For platform image osType should be null, azure will pick it from the image metadata.
            osDisk.setOsType(OperatingSystemTypes.WINDOWS);
        }
        this.innerModel.osProfile().setWindowsConfiguration(new WindowsConfiguration());
        // sets defaults for "Stored(User)Image" or "VM(Platform)Image"
        this.innerModel.osProfile().windowsConfiguration().setProvisionVMAgent(true);
        this.innerModel.osProfile().windowsConfiguration().setEnableAutomaticUpdates(true);
        return this;
    }


    @Override
    public DefinitionLinuxCreatable withRootUserName(String rootUserName) {
        this.innerModel.osProfile().setAdminUsername(rootUserName);
        return this;
    }

    @Override
    public DefinitionWindowsCreatable withAdminUserName(String adminUserName) {
        this.innerModel.osProfile().setAdminUsername(adminUserName);
        return this;
    }

    @Override
    public DefinitionLinuxCreatable withSsh(String publicKeyData) {
        OSProfile osProfile = this.innerModel.osProfile();
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
        this.innerModel.osProfile().windowsConfiguration().setProvisionVMAgent(false);
        return this;
    }

    @Override
    public DefinitionWindowsCreatable disableAutoUpdate() {
        this.innerModel.osProfile().windowsConfiguration().setEnableAutomaticUpdates(false);
        return this;
    }

    @Override
    public DefinitionWindowsCreatable withTimeZone(String timeZone) {
        this.innerModel.osProfile().windowsConfiguration().setTimeZone(timeZone);
        return this;
    }

    @Override
    public DefinitionWindowsCreatable withWinRM(WinRMListener listener) {
        if (this.innerModel.osProfile().windowsConfiguration().winRM() == null) {
            WinRMConfiguration winRMConfiguration = new WinRMConfiguration();
            this.innerModel.osProfile().windowsConfiguration().setWinRM(winRMConfiguration);
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
        this.innerModel.osProfile().setAdminPassword(password);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskCaching(CachingTypes cachingType) {
        this.innerModel.storageProfile().osDisk().setCaching(cachingType);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskVhdLocation(String containerName, String vhdName) {
        VirtualHardDisk osVhd = new VirtualHardDisk();
        osVhd.setUri(null); // TODO generate and sets VHD URI.
        this.innerModel.storageProfile().osDisk().setVhd(osVhd);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskEncryptionSettings(DiskEncryptionSettings settings) {
        this.innerModel.storageProfile().osDisk().setEncryptionSettings(settings);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskSizeInGB(Integer size) {
        this.innerModel.storageProfile().osDisk().setDiskSizeGB(size);
        return this;
    }

    @Override
    public DefinitionCreatable withOSDiskName(String name) {
        this.innerModel.storageProfile().osDisk().setName(name);
        return this;
    }

    @Override
    public DefinitionCreatable withNewStorageAccount(String name) {
        return withNewStorageAccount(storageManager.storageAccounts().define(name)
                .withRegion(location())
                .withExistingGroup(this.resourceGroupName()));
    }

    @Override
    public DefinitionCreatable withNewStorageAccount(StorageAccount.DefinitionCreatable creatable) {
        this.storageAccountName = creatable.key();
        this.prerequisites().put(creatable.key(), creatable);
        return this;
    }

    @Override
    public DefinitionCreatable withExistingStorageAccount(String name) {
        this.storageAccountName = name;
        return this;
    }


    @Override
    public VirtualMachine create() throws Exception {
        setDefaults();
        return null;
    }

    // Helper methods
    //
    private boolean isStoredImage(OSDisk osDisk) {
        return osDisk.image() != null;
    }

    private boolean isOSDiskAttached(OSDisk osDisk) {
        return osDisk.createOption() == DiskCreateOptionTypes.ATTACH;
    }

    private void setDefaults() {
        OSDisk osDisk = this.innerModel.storageProfile().osDisk();
        if (!isOSDiskAttached(osDisk)) {
            if (osDisk.vhd() == null) {
                // Sets the OS disk VHD for "UserImage" and "VM(Platform)Image"
                withOSDiskVhdLocation("vhds", null /*TODO generate random vhd name */);
            }
            OSProfile osProfile = this.innerModel.osProfile();
            if (osDisk.osType() == OperatingSystemTypes.LINUX) {
                if (osProfile.linuxConfiguration() == null) {
                    osProfile.setLinuxConfiguration(new LinuxConfiguration());
                }
                this.innerModel.osProfile().linuxConfiguration().setDisablePasswordAuthentication(osProfile.adminPassword() == null);
            }
        }

        if (osDisk.caching() == null) {
            withOSDiskCaching(CachingTypes.READ_WRITE);
        }

        if (osDisk.name() == null) {
            withOSDiskName(null /*TODO generate random OSDisk name */);
        }
    }
}
