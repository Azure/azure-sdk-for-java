package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.implementation.api.*;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.storage.StorageAccount;

import java.util.ArrayList;
import java.util.List;

class VirtualMachineImpl
        extends GroupableResourceImpl<VirtualMachine, VirtualMachineInner, VirtualMachineImpl>
        implements
            VirtualMachine,
            VirtualMachine.DefinitionBlank,
            VirtualMachine.DefinitionWithGroup,
            VirtualMachine.DefinitionWithDataDisk,
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
            VirtualMachine.DefinitionProvisionable {
    private final VirtualMachinesInner client;
    private VirtualMachineInner innerModel;

    VirtualMachineImpl(String name, VirtualMachineInner innerModel, VirtualMachinesInner client, ResourceGroups resourceGroups) {
        super(name, innerModel, resourceGroups);
        this.client = client;
        this.innerModel = innerModel;

        this.innerModel.setStorageProfile(new StorageProfile());
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
        // TODO Setup storage account details
        DefinitionWithNewOSDiskImpl definitionWithNewOSDiskImpl = new DefinitionWithNewOSDiskImpl(this, this.name());
        return this;
    }

    @Override
    public VirtualMachine.DefinitionWithOS withExistingStorageAccount(String name) {
        // TODO Setup storage account details
        return this;
    }

    @Override
    public VirtualMachine.DefinitionWithOS withExistingStorageAccount(StorageAccount.DefinitionProvisionable provisionable) {
        // TODO Setup storage account details
        return this;
    }

    @Override
    public VirtualMachine refresh() throws Exception {
        return this;
    }

    @Override
    public DefinitionWithOSType withImage(ImageReference imageReference) {
        OSDisk osDisk = new OSDisk();
        osDisk.setCreateOption(DiskCreateOptionTypes.FROMIMAGE);
        this.innerModel.storageProfile().setOsDisk(osDisk);
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
        OSDisk osDisk = new OSDisk();
        osDisk.setCreateOption(DiskCreateOptionTypes.FROMIMAGE);
        osDisk.setImage(userImageVhd);
        this.innerModel.storageProfile().setOsDisk(osDisk);
        return this;
    }

    @Override
    public DefinitionWithDataDisk withWindowsOSDisk(String osDiskUrl) {
        VirtualHardDisk specializedImage = new VirtualHardDisk();
        specializedImage.setUri(osDiskUrl);
        OSDisk osDisk = new OSDisk();
        osDisk.setCreateOption(DiskCreateOptionTypes.ATTACH);
        osDisk.setOsType(OperatingSystemTypes.WINDOWS);
        osDisk.setVhd(specializedImage);
        this.innerModel.storageProfile().setOsDisk(osDisk);
        return this;
    }

    @Override
    public DefinitionWithDataDisk withLinxOSDisk(String osDiskUrl) {
        VirtualHardDisk specializedImage = new VirtualHardDisk();
        specializedImage.setUri(osDiskUrl);
        OSDisk osDisk = new OSDisk();
        osDisk.setCreateOption(DiskCreateOptionTypes.ATTACH);
        osDisk.setOsType(OperatingSystemTypes.LINUX);
        osDisk.setVhd(specializedImage);
        this.innerModel.storageProfile().setOsDisk(osDisk);
        return this;
    }

    @Override
    public DefinitionWithRootUserName withLinuxOS() {
        return this;
    }

    @Override
    public DefinitionWithOptionalWindowsConfiguration withWindowsOS() {
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
    public DefinitionWithAdminUserName withDefaultConfiguration() {
        WindowsConfiguration windowsConfiguration = new WindowsConfiguration();
        windowsConfiguration.setProvisionVMAgent(true);
        windowsConfiguration.setEnableAutomaticUpdates(true);
        OSProfile osProfile = new OSProfile();
        osProfile.setWindowsConfiguration(windowsConfiguration);
        this.innerModel.setOsProfile(osProfile);
        return this;
    }

    @Override
    public DefinitionWithWindowsConfiguration enableVMAgent() {
        this.innerModel.osProfile().windowsConfiguration().setProvisionVMAgent(true);
        return this;
    }

    @Override
    public DefinitionWithWindowsConfiguration disableVMAgent() {
        this.innerModel.osProfile().windowsConfiguration().setProvisionVMAgent(false);
        return this;
    }

    @Override
    public DefinitionWithWindowsConfiguration enableAutoUpdate() {
        this.innerModel.osProfile().windowsConfiguration().setEnableAutomaticUpdates(true);
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
        return this;
    }

    @Override
    public DefinitionWithOptionalSsh withRootUserName(String userName) {
        this.innerModel.osProfile().setAdminUsername(userName);
        return this;
    }

    @Override
    public DefinitionWithPassword withAdminUserName(String userName) {
        this.innerModel.osProfile().setAdminUsername(userName);
        return this;
    }

    @Override
    public DefinitionWithOptionalPassword withSsh(String publicKeyData) {
        SshPublicKey sshPublicKey = new SshPublicKey();
        sshPublicKey.setKeyData(publicKeyData);
        // TODO set path
        List<SshPublicKey> publicKeys = new ArrayList<>();
        publicKeys.add(sshPublicKey);
        SshConfiguration sshConfiguration = new SshConfiguration();
        sshConfiguration.setPublicKeys(publicKeys);
        LinuxConfiguration linuxConfiguration = new LinuxConfiguration();
        linuxConfiguration.setSsh(sshConfiguration);
        this.innerModel.osProfile().setLinuxConfiguration(linuxConfiguration);
        return this;
    }

    @Override
    public DefinitionWithOptionalPassword withSsh(List<String> publicKeyDatas) {
        List<SshPublicKey> publicKeys = new ArrayList<>();
        for(String publicKeyData : publicKeyDatas) {
            SshPublicKey sshPublicKey = new SshPublicKey();
            sshPublicKey.setKeyData(publicKeyData);
            // TODO set path
            publicKeys.add(sshPublicKey);
        }

        SshConfiguration sshConfiguration = new SshConfiguration();
        sshConfiguration.setPublicKeys(publicKeys);
        LinuxConfiguration linuxConfiguration = new LinuxConfiguration();
        linuxConfiguration.setSsh(sshConfiguration);
        this.innerModel.osProfile().setLinuxConfiguration(linuxConfiguration);
        return this;
    }

    @Override
    public DefinitionWithPassword withoutSsh() {
        SshConfiguration sshConfiguration = new SshConfiguration();
        LinuxConfiguration linuxConfiguration = new LinuxConfiguration();
        linuxConfiguration.setSsh(sshConfiguration);
        this.innerModel.osProfile().setLinuxConfiguration(linuxConfiguration);
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
    public DefinitionProvisionable notImplemented() {
        return null;
    }

    @Override
    public VirtualMachine provision() throws Exception {
        return null;
    }
}
