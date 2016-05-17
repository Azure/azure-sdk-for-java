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
            VirtualMachine.DefinitionWithStorageAccount,
            VirtualMachine.DefinitionWithOS,
            VirtualMachine.DefinitionWithOSType,
            VirtualMachine.DefinitionWithOptionalWindowsConfiguration,
            VirtualMachine.DefinitionWithWindowsConfiguration,
            VirtualMachine.DefinitionWithRootUserName,
            VirtualMachine.DefinitionWithAdminUserName,
            VirtualMachine.DefinitionWithOptionalSsh,
            VirtualMachine.DefinitionWithOptionalPassword,
            VirtualMachine.DefinitionWithNextTODO,
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
    public VirtualMachine.DefinitionWithOS withNewStorageAccount(String name) {
        return withNewStorageAccount(storageManager.storageAccounts().define(name)
                .withRegion(region())
                .withExistingGroup(this.resourceGroupName()));
    }

    @Override
    public VirtualMachine.DefinitionWithOS withNewStorageAccount(StorageAccount.DefinitionCreatable creatable) {
        this.storageAccountName = creatable.key();
        this.prerequisites().put(creatable.key(), creatable);
        return this;
    }

    @Override
    public VirtualMachine.DefinitionWithOS withExistingStorageAccount(String name) {
        this.storageAccountName = name;
        return this;
    }

    @Override
    public VirtualMachine refresh() throws Exception {
        return this;
    }

    @Override
    public DefinitionWithOSType withImage(ImageReference imageReference) {
        this.innerModel.storageProfile().osDisk().setCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.innerModel.storageProfile().setImageReference(imageReference);
        return this;
    }

    @Override
    public DefinitionWithOSType withLatestImage(String publisher, String offer, String sku) {
        ImageReference imageReference = new ImageReference();
        imageReference.setPublisher(publisher);
        imageReference.setOffer(offer);
        imageReference.setSku(sku);
        imageReference.setVersion("latest");
        return withImage(imageReference);
    }

    @Override
    public DefinitionWithOSType withKnownImage(KnownVirtualMachineImage knownImage) {
        return withImage(knownImage.imageReference());
    }

    @Override
    public DefinitionWithOSType withImage(String userImageUrl) {
        VirtualHardDisk userImageVhd = new VirtualHardDisk();
        userImageVhd.setUri(userImageUrl);
        this.innerModel.storageProfile().osDisk().setCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        this.innerModel.storageProfile().osDisk().setImage(userImageVhd);
        return this;
    }

    @Override
    public DefinitionWithNextTODO withWindowsOSDisk(String osDiskUrl) {
        VirtualHardDisk osDisk = new VirtualHardDisk();
        osDisk.setUri(osDiskUrl);
        this.innerModel.storageProfile().osDisk().setCreateOption(DiskCreateOptionTypes.ATTACH);
        this.innerModel.storageProfile().osDisk().setVhd(osDisk);
        this.innerModel.storageProfile().osDisk().setOsType(OperatingSystemTypes.WINDOWS);
        return this;
    }

    @Override
    public DefinitionWithNextTODO withLinuxOSDisk(String osDiskUrl) {
        VirtualHardDisk osDisk = new VirtualHardDisk();
        osDisk.setUri(osDiskUrl);
        this.innerModel.storageProfile().osDisk().setCreateOption(DiskCreateOptionTypes.ATTACH);
        this.innerModel.storageProfile().osDisk().setVhd(osDisk);
        this.innerModel.storageProfile().osDisk().setOsType(OperatingSystemTypes.LINUX);
        return this;
    }

    @Override
    public DefinitionWithRootUserName withLinuxOS() {
        OSDisk osDisk = this.innerModel.storageProfile().osDisk();
        if (isUserImage(osDisk)) {
            // For platform image osType should be null, azure will pick it from the image metadata.
            osDisk.setOsType(OperatingSystemTypes.LINUX);
        }
        return this;
    }

    @Override
    public DefinitionWithOptionalWindowsConfiguration withWindowsOS() {
        OSDisk osDisk = this.innerModel.storageProfile().osDisk();
        if (isUserImage(osDisk)) {
            // For platform image osType should be null, azure will pick it from the image metadata.
            osDisk.setOsType(OperatingSystemTypes.WINDOWS);
        }
        // VM from Windows "UserImage" or "VM(Platform)Image" must have default Windows configuration.
        defineConfiguration();
        this.innerModel.osProfile().windowsConfiguration().setProvisionVMAgent(true);
        this.innerModel.osProfile().windowsConfiguration().setEnableAutomaticUpdates(true);
        return this;
    }

    @Override
    public DefinitionWithWindowsConfiguration defineConfiguration() {
        OSProfile osProfile = new OSProfile();
        osProfile.setWindowsConfiguration(new WindowsConfiguration());
        this.innerModel.setOsProfile(osProfile);
        return this;
    }

    @Override
    public DefinitionWithWindowsConfiguration disableVMAgent() {
        this.innerModel.osProfile().windowsConfiguration().setProvisionVMAgent(false);
        return this;
    }

    @Override
    public DefinitionWithWindowsConfiguration disableAutoUpdate() {
        this.innerModel.osProfile().windowsConfiguration().setEnableAutomaticUpdates(false);
        return this;
    }

    @Override
    public DefinitionWithWindowsConfiguration withTimeZone(String timeZone) {
        this.innerModel.osProfile().windowsConfiguration().setTimeZone(timeZone);
        return this;
    }

    @Override
    public DefinitionWithWindowsConfiguration withWinRM(List<WinRMListener> listeners) {
        WinRMConfiguration winRMConfiguration = new WinRMConfiguration();
        winRMConfiguration.setListeners(listeners);
        this.innerModel.osProfile().windowsConfiguration().setWinRM(winRMConfiguration);
        return this;
    }

    @Override
    public DefinitionWithAdminUserName apply() {
        // Apply the Windows configuration.
        return this;
    }

    @Override
    public DefinitionWithOptionalSsh withRootUserName(String rootUserName) {
        this.innerModel.osProfile().setAdminUsername(rootUserName);
        return this;
    }

    @Override
    public DefinitionWithPassword withAdminUserName(String adminUserName) {
        this.innerModel.osProfile().setAdminUsername(adminUserName);
        return this;
    }

    @Override
    public DefinitionWithOptionalPassword withSsh(String publicKeyData) {
        OSProfile osProfile = this.innerModel.osProfile();
        if (osProfile.linuxConfiguration() == null) {
            LinuxConfiguration linuxConfiguration = new LinuxConfiguration();
            SshConfiguration sshConfiguration = new SshConfiguration();
            sshConfiguration.setPublicKeys(new ArrayList<SshPublicKey>());
            linuxConfiguration.setSsh(sshConfiguration);
            osProfile.setLinuxConfiguration(linuxConfiguration);
        }
        SshPublicKey sshPublicKey = new SshPublicKey();
        sshPublicKey.setKeyData(publicKeyData);
        sshPublicKey.setPath("/home/" + osProfile.adminUsername() + "/.ssh/authorized_keys");
        osProfile.linuxConfiguration().ssh().publicKeys().add(sshPublicKey);
        return this;
    }

    @Override
    public DefinitionWithOptionalPassword withSsh(List<String> publicKeyDataList) {
        for(String publicKeyData : publicKeyDataList) {
            withSsh(publicKeyData);
        }
        return this;
    }

    @Override
    public DefinitionWithPassword withoutSsh() {
        OSProfile osProfile = this.innerModel.osProfile();
        osProfile().setLinuxConfiguration(new LinuxConfiguration());
        osProfile.linuxConfiguration().setDisablePasswordAuthentication(false);
        return this;
    }

    @Override
    public DefinitionWithNextTODO withPassword(String password) {
        this.innerModel.osProfile().setAdminPassword(password);
        return this;
    }

    @Override
    public DefinitionWithNextTODO withoutPassword() {
        this.innerModel.osProfile().linuxConfiguration().setDisablePasswordAuthentication(true);
        return this;
    }

    @Override
    public DefinitionCreatable moreVMRequiredParameters() {
        // TODO This is not the final place setting the defaults but putting it here so that we won't forget it later.
        //
        OSDisk osDisk = this.innerModel.storageProfile().osDisk();
        if (!isSpecializedImage(osDisk)) {
            // Sets the OS disk VHD for "UserImage" and "VM(Platform)Image"
            withOSDiskVhdLocation("vhds", null /*TODO generate random vhd name */);
        }
        withOSDiskCaching(CachingTypes.READ_WRITE);
        withOSDiskName(null /*TODO generate random OSDisk name */);

        return this;
    }

    @Override
    public VirtualMachine create() throws Exception {
        return null;
    }

    // Optionals

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

    // Helper methods
    private boolean isUserImage(OSDisk osDisk) {
        return osDisk.image() != null;
    }

    private boolean isSpecializedImage(OSDisk osDisk) {
        return osDisk.createOption() == DiskCreateOptionTypes.ATTACH;
    }
}
