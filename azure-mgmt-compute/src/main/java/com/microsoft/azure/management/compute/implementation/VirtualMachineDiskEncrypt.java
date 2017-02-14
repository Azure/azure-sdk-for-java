/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.compute.DiskEncryptionSettings;
import com.microsoft.azure.management.compute.DiskVolumeEncryptionStatus;
import com.microsoft.azure.management.compute.KeyVaultSecretReference;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineEncryptionSettings;
import com.microsoft.azure.management.compute.VirtualMachineExtension;
import com.microsoft.azure.management.compute.VirtualMachineExtensionInstanceView;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

import java.util.HashMap;

/**
 * Type to perform virtual machine disk (OS, Data) encryption.
 */
class VirtualMachineDiskEncrypt<T extends VirtualMachineEncryptionSettings<T>> {
    final String extensionPublisher = "Microsoft.Azure.Security";
    final String extensionType;
    final String extensionVersion;
    final VirtualMachine virtualMachine;
    final VirtualMachineEncryptionSettings<T> encryptionSettings;

    public VirtualMachineDiskEncrypt(final VirtualMachine virtualMachine, final VirtualMachineEncryptionSettings<T> encryptionSettings) {
        this.virtualMachine = virtualMachine;
        this.encryptionSettings = encryptionSettings;
        if (encryptionSettings.osType() == OperatingSystemTypes.LINUX) {
            extensionType = "AzureDiskEncryptionForLinux";
            extensionVersion = "0.1";
        } else {
            extensionType = "AzureDiskEncryption";
            extensionVersion = "1.1";
        }
    }

    public Observable<DiskVolumeEncryptionStatus> encryptAsync() {
                // Update the encryption extension if already installed
        return updateEncryptionExtensionThroughVMUpdateAsync()
                // If encryption extension is not installed then install it
                .switchIfEmpty(installEncryptionExtensionThroughVMUpdateAsync())
                .map(new Func1<VirtualMachine, String>() {
                    @Override
                    public String call(VirtualMachine vm) {
                        return null;
                    }
                })
                // Retrieve the encryption key URL after extension install or update
                .concatWith(retrieveEncryptionSecretKeyVaultUrlAsync())
                .last()
                // Update the VM's OS profile with the encryption metadata
                .flatMap(new Func1<String, Observable<VirtualMachine>>() {
                    @Override
                    public Observable<VirtualMachine> call(String keyVaultSecretUrl) {
                        return updateEncryptionSettingsInVMAsync(keyVaultSecretUrl);
                    }
                })
                // Gets the encryption status
                .flatMap(new Func1<VirtualMachine, Observable<DiskVolumeEncryptionStatus>>() {
                    @Override
                    public Observable<DiskVolumeEncryptionStatus> call(VirtualMachine virtualMachine) {
                        return getEncryptionStatusAsync();
                    }
                });
    }

    private HashMap<String, Object> preparePublicSettings() {
        final HashMap<String, Object> publicSettings = new HashMap<>();
        publicSettings.put("AADClientID", encryptionSettings.aadClientId());
        publicSettings.put("EncryptionOperation", "EnableEncryption");
        publicSettings.put("KeyEncryptionAlgorithm", encryptionSettings.volumeEncryptionKeyEncryptAlgorithm());
        publicSettings.put("KeyVaultURL", encryptionSettings.keyVaultUrl());
        publicSettings.put("VolumeType", encryptionSettings.volumeType().toString());
        if (encryptionSettings.keyEncryptionKeyURL() != null) {
            publicSettings.put("KeyEncryptionKeyURL", encryptionSettings.keyEncryptionKeyURL());
        }
        return publicSettings;
    }

    private HashMap<String, Object> prepareProtectedSettings() {
        final HashMap<String, Object> protectedSettings = new HashMap<>();
        protectedSettings.put("AADClientSecret", encryptionSettings.aadSecret());
        if (encryptionSettings.osType() == OperatingSystemTypes.LINUX
                && encryptionSettings.linuxPassPhrase() != null) {
            protectedSettings.put("Passphrase", encryptionSettings.linuxPassPhrase());
        }
        return protectedSettings;
    }

    private <ResultT> Observable<ResultT> toErrorObservable(String message) {
        return Observable.error(new Exception(message));
    }

    private Observable<VirtualMachineExtension> getEncryptionExtensionInstalledInVMAsync() {
        return virtualMachine.getExtensionsAsync()
                .first(new Func1<VirtualMachineExtension, Boolean>() {
                    @Override
                    public Boolean call(final VirtualMachineExtension extension) {
                        return extension.publisherName().equalsIgnoreCase(extensionPublisher)
                                && extension.typeName().equalsIgnoreCase(extensionType);
                    }
                });
    }

    private Observable<VirtualMachine> updateEncryptionExtensionThroughVMUpdateAsync() {
        final HashMap<String, Object> publicSettings = this.preparePublicSettings();
        final HashMap<String, Object> protectedSettings = this.prepareProtectedSettings();
        return getEncryptionExtensionInstalledInVMAsync()
                .flatMap(new Func1<VirtualMachineExtension, Observable<VirtualMachine>>() {
                    @Override
                    public Observable<VirtualMachine> call(final VirtualMachineExtension encryptionExtension) {
                        String nextSequenceVersion = "1";
                        if (encryptionExtension.publicSettings().containsKey("SequenceVersion")) {
                            String currentSequenceVersion = (String) encryptionExtension
                                    .publicSettings()
                                    .get("SequenceVersion");
                            nextSequenceVersion = Integer.toString(Integer.parseInt(currentSequenceVersion) + 1);
                        }
                        publicSettings.put("SequenceVersion", nextSequenceVersion);
                        return virtualMachine.update()
                                .updateExtension(encryptionExtension.name())
                                    .withPublicSettings(publicSettings)
                                    .withProtectedSettings(protectedSettings)
                                    .parent()
                                .applyAsync();
                    }
                });
    }

    private Observable<VirtualMachine> installEncryptionExtensionThroughVMUpdateAsync() {
        final HashMap<String, Object> publicSettings = this.preparePublicSettings();
        final HashMap<String, Object> protectedSettings = this.prepareProtectedSettings();
        return Observable.defer(new Func0<Observable<VirtualMachine>>() {
            final String extensionName = extensionType;
            @Override
            public Observable<VirtualMachine> call() {
                return virtualMachine.update()
                        .defineNewExtension(extensionName)
                            .withPublisher(extensionPublisher)
                            .withType(extensionType)
                            .withVersion(extensionVersion)
                            .withPublicSettings(publicSettings)
                            .withProtectedSettings(protectedSettings)
                            .withMinorVersionAutoUpgrade()
                            .attach()
                        .applyAsync();
            }
        });
    }

    private Observable<String> retrieveEncryptionSecretKeyVaultUrlAsync() {
        final VirtualMachineDiskEncrypt<T> self = this;
        return getEncryptionExtensionInstalledInVMAsync()
                .switchIfEmpty(self.<VirtualMachineExtension>toErrorObservable("Expected encryption extension not found in the VM"))
                // Check the provisioning state
                .flatMap(new Func1<VirtualMachineExtension, Observable<VirtualMachineExtensionInstanceView>>() {
                    @Override
                    public Observable<VirtualMachineExtensionInstanceView> call(VirtualMachineExtension extension) {
                        if (!extension.provisioningState().equalsIgnoreCase("Succeeded")) {
                            return self.toErrorObservable((String.format("ProvisioningState of Encryption extension is not 'Succeeded', found %s", extension.provisioningState())));
                        }
                        return extension.getInstanceViewAsync();
                    }
                })
                // Retrieve the encryption secret key vault URL
                .flatMap(new Func1<VirtualMachineExtensionInstanceView, Observable<String>>() {
                    @Override
                    public Observable<String> call(VirtualMachineExtensionInstanceView instanceView) {
                        if (instanceView == null
                                || instanceView.statuses() == null
                                || instanceView.statuses().size() == 0) {
                            return self.toErrorObservable("Encryption extension with successful status not found in the VM");
                        }
                        String encryptionSecretKeyVaultUrl = instanceView.statuses().get(0).message();
                        if (encryptionSecretKeyVaultUrl == null) {
                            return self.toErrorObservable("Encryption extension status is empty, it should be vaild keyvault URL");
                        }
                        return Observable.just(encryptionSecretKeyVaultUrl);
                    }
                });
    }

    private Observable<VirtualMachine> updateEncryptionSettingsInVMAsync(String encryptionSecretKeyVaultUrl) {
        final String keyVaultId = this.encryptionSettings.keyVaultId();
        DiskEncryptionSettings diskEncryptionSettings = new DiskEncryptionSettings();
        diskEncryptionSettings
                .withEnabled(true)
                .withDiskEncryptionKey(new KeyVaultSecretReference())
                .diskEncryptionKey()
                .withSecretUrl(encryptionSecretKeyVaultUrl)
                .withSourceVault(new SubResource().withId(keyVaultId));
        return virtualMachine.update()
                .withOsDiskEncryptionSettings(diskEncryptionSettings)
                .applyAsync();
    }

    private Observable<DiskVolumeEncryptionStatus> getEncryptionStatusAsync() {
        final VirtualMachineDiskEncrypt<T> self = this;
        return getEncryptionExtensionInstalledInVMAsync()
                .switchIfEmpty(self.<VirtualMachineExtension>toErrorObservable("Expected encryption extension not found in the VM"))
                .map(new Func1<VirtualMachineExtension, DiskVolumeEncryptionStatus>() {
                    @Override
                    public DiskVolumeEncryptionStatus call(VirtualMachineExtension extension) {
                        return new DiskVolumeEncryptionStatusImpl(self.encryptionSettings.osType(),
                                extension,
                                extension.inner().instanceView());
                    }
                });
    }
}