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
import java.util.LinkedHashMap;

/**
 * Type to perform virtual machine disk (OS, Data) encryption.
 */
class VirtualMachineDiskEncrypt {
    private final String encryptionExtensionPublisher = "Microsoft.Azure.Security";
    private final OperatingSystemTypes osType;
    private final VirtualMachine virtualMachine;
    // Error messages
    private static final String ERROR_ENCRYPTION_EXTENSION_NOT_FOUND = "Expected encryption extension not found in the VM";
    private static final String ERROR_NON_SUCCESS_PROVISIONING_STATE = "ProvisioningState of Encryption extension is not 'Succeeded', found %s";
    private static final String ERROR_EXPECTED_KEY_VAULT_URL_NOT_FOUND = "Encryption extension status is empty, it should be valid keyVault URL";
    private static final String ERROR_EXPECTED_ENCRYPTION_EXTENSION_STATUS_NOT_FOUND = "Encryption extension with successful status not found in the VM";
    private static final String ERROR_ENCRYPTION_EXTENSION_STATUS_IS_EMPTY = "Encryption extension status is empty";

    public VirtualMachineDiskEncrypt(final VirtualMachine virtualMachine) {
        this.virtualMachine = virtualMachine;
        this.osType = this.virtualMachine.osType();
    }

    public Observable<DiskVolumeEncryptionStatus> getEncryptionStatusAsync() {
        return this.getEncryptionStatusAsync(false);
    }

    public <T extends VirtualMachineEncryptionSettings<T>> Observable<DiskVolumeEncryptionStatus> enableEncryptionAsync(final VirtualMachineEncryptionSettings<T> encryptionSettings) {
        final EnableDisableEncryptConfig encryptConfig = new EnableEncryptConfig(encryptionSettings);
                // Update the encryption extension if already installed
        return updateEncryptionExtensionAsync(encryptConfig)
                // If encryption extension is not installed then install it
                .switchIfEmpty(installingEncryptionExtensionAsync(encryptConfig))
                // Retrieve the encryption key URL after extension install or update
                .flatMap(new Func1<VirtualMachine, Observable<String>>() {
                    @Override
                    public Observable<String> call(VirtualMachine virtualMachine) {
                        return retrieveEncryptionExtensionStatusStringAsync(ERROR_EXPECTED_KEY_VAULT_URL_NOT_FOUND);
                    }
                })
                // Update the VM's OS profile with the encryption metadata
                .flatMap(new Func1<String, Observable<VirtualMachine>>() {
                    @Override
                    public Observable<VirtualMachine> call(String keyVaultSecretUrl) {
                        return updateVMOSProfileAsync(encryptConfig, keyVaultSecretUrl);
                    }
                })
                // Gets the encryption status
                .flatMap(new Func1<VirtualMachine, Observable<DiskVolumeEncryptionStatus>>() {
                    @Override
                    public Observable<DiskVolumeEncryptionStatus> call(VirtualMachine virtualMachine) {
                        return getEncryptionStatusAsync(true);
                    }
                });
    }

    public Observable<DiskVolumeEncryptionStatus> disableEncryptionAsync() {
        final EnableDisableEncryptConfig encryptConfig = new DisableEncryptConfig();
        // Update the encryption extension if already installed
        return updateEncryptionExtensionAsync(encryptConfig)
                // If encryption extension is not then install it
                .switchIfEmpty(installingEncryptionExtensionAsync(encryptConfig))
                // Validate and retrieve the encryption extension status
                .flatMap(new Func1<VirtualMachine, Observable<String>>() {
                    @Override
                    public Observable<String> call(VirtualMachine virtualMachine) {
                        return retrieveEncryptionExtensionStatusStringAsync(ERROR_ENCRYPTION_EXTENSION_STATUS_IS_EMPTY);
                    }
                })
                // Update the VM's OS profile by marking encryption disabled
                .flatMap(new Func1<String, Observable<VirtualMachine>>() {
                    @Override
                    public Observable<VirtualMachine> call(String status) {
                        return updateVMOSProfileAsync(encryptConfig);
                    }
                })
                // Gets the encryption status
                .flatMap(new Func1<VirtualMachine, Observable<DiskVolumeEncryptionStatus>>() {
                    @Override
                    public Observable<DiskVolumeEncryptionStatus> call(VirtualMachine virtualMachine) {
                        return getEncryptionStatusAsync(true);
                    }
                });
    }

    private String encryptionExtensionType() {
        if (this.osType == OperatingSystemTypes.LINUX) {
            return "AzureDiskEncryptionForLinux";
        } else {
            return "AzureDiskEncryption";
        }
    }

    private String encryptionExtensionVersion() {
        if (this.osType == OperatingSystemTypes.LINUX) {
            return "0.1";
        } else {
            return "1.1";
        }
    }

    private Observable<VirtualMachineExtension> getEncryptionExtensionInstalledInVMAsync() {
        return virtualMachine.getExtensionsAsync()
                .first(new Func1<VirtualMachineExtension, Boolean>() {
                    @Override
                    public Boolean call(final VirtualMachineExtension extension) {
                        return extension.publisherName().equalsIgnoreCase(encryptionExtensionPublisher)
                                && extension.typeName().equalsIgnoreCase(encryptionExtensionType());
                    }
                });
    }

    private Observable<VirtualMachine> updateEncryptionExtensionAsync(final EnableDisableEncryptConfig encryptConfig) {
        final HashMap<String, Object> publicSettings = encryptConfig.extensionPublicSettings();
        return getEncryptionExtensionInstalledInVMAsync()
                .flatMap(new Func1<VirtualMachineExtension, Observable<VirtualMachine>>() {
                    @Override
                    public Observable<VirtualMachine> call(final VirtualMachineExtension encryptionExtension) {
                        publicSettings.put("SequenceVersion", nextSequenceVersion(encryptionExtension));
                        return virtualMachine.update()
                                .updateExtension(encryptionExtension.name())
                                    .withPublicSettings(publicSettings)
                                    .withProtectedSettings(encryptConfig.extensionProtectedSettings())
                                    .parent()
                                .applyAsync();
                    }
                });
    }

    private Observable<VirtualMachine> installingEncryptionExtensionAsync(final EnableDisableEncryptConfig encryptConfig) {
        return Observable.defer(new Func0<Observable<VirtualMachine>>() {
            final String extensionName = encryptionExtensionType();
            @Override
            public Observable<VirtualMachine> call() {
                return virtualMachine.update()
                        .defineNewExtension(extensionName)
                            .withPublisher(encryptionExtensionPublisher)
                            .withType(encryptionExtensionType())
                            .withVersion(encryptionExtensionVersion())
                            .withPublicSettings(encryptConfig.extensionPublicSettings())
                            .withProtectedSettings( encryptConfig.extensionProtectedSettings())
                            .withMinorVersionAutoUpgrade()
                            .attach()
                        .applyAsync();
            }
        });
    }

    private Observable<String> retrieveEncryptionExtensionStatusStringAsync(final String statusEmptyErrorMessage) {
        final VirtualMachineDiskEncrypt self = this;
        return getEncryptionExtensionInstalledInVMAsync()
                .switchIfEmpty(self.<VirtualMachineExtension>toErrorObservable(ERROR_ENCRYPTION_EXTENSION_NOT_FOUND))
                .flatMap(new Func1<VirtualMachineExtension, Observable<VirtualMachineExtensionInstanceView>>() {
                    @Override
                    public Observable<VirtualMachineExtensionInstanceView> call(VirtualMachineExtension extension) {
                        if (!extension.provisioningState().equalsIgnoreCase("Succeeded")) {
                            return self.toErrorObservable((String.format(ERROR_NON_SUCCESS_PROVISIONING_STATE, extension.provisioningState())));
                        }
                        return extension.getInstanceViewAsync();
                    }
                })
                .flatMap(new Func1<VirtualMachineExtensionInstanceView, Observable<String>>() {
                    @Override
                    public Observable<String> call(VirtualMachineExtensionInstanceView instanceView) {
                        if (instanceView == null
                                || instanceView.statuses() == null
                                || instanceView.statuses().size() == 0) {
                            return self.toErrorObservable(ERROR_EXPECTED_ENCRYPTION_EXTENSION_STATUS_NOT_FOUND);
                        }
                        String extensionStatus = instanceView.statuses().get(0).message();
                        if (extensionStatus == null) {
                            return self.toErrorObservable(statusEmptyErrorMessage);
                        }
                        return Observable.just(extensionStatus);
                    }
                });
    }

    private Observable<DiskVolumeEncryptionStatus> getEncryptionStatusAsync(boolean emitErrorIfExtensionNotFound) {
        final VirtualMachineDiskEncrypt self = this;
        if (emitErrorIfExtensionNotFound) {
            return getEncryptionExtensionInstalledInVMAsync()
                    .switchIfEmpty(self.<VirtualMachineExtension>toErrorObservable(ERROR_ENCRYPTION_EXTENSION_NOT_FOUND))
                    .map(new Func1<VirtualMachineExtension, DiskVolumeEncryptionStatus>() {
                        @Override
                        public DiskVolumeEncryptionStatus call(VirtualMachineExtension extension) {
                            return new DiskVolumeEncryptionStatusImpl(self.osType,
                                    extension);
                        }
                    });
        } else {
            return getEncryptionExtensionInstalledInVMAsync()
                    .map(new Func1<VirtualMachineExtension, DiskVolumeEncryptionStatus>() {
                        @Override
                        public DiskVolumeEncryptionStatus call(VirtualMachineExtension extension) {
                            return new DiskVolumeEncryptionStatusImpl(self.osType,
                                    extension);
                        }
                    })
                    .switchIfEmpty(Observable.<DiskVolumeEncryptionStatus>just(new DiskVolumeEncryptionStatusImpl(self.virtualMachine,
                            self.encryptionExtensionType())));
        }
    }

    private Observable<VirtualMachine> updateVMOSProfileAsync(final EnableDisableEncryptConfig encryptConfig,
                                                              final String encryptionSecretKeyVaultUrl) {
        DiskEncryptionSettings diskEncryptionSettings = encryptConfig.osProfileEncryptionSettings();
        diskEncryptionSettings.diskEncryptionKey()
                .withSecretUrl(encryptionSecretKeyVaultUrl);
        return virtualMachine.update()
                .withOsDiskEncryptionSettings(diskEncryptionSettings)
                .applyAsync();
    }

    private Observable<VirtualMachine> updateVMOSProfileAsync(final EnableDisableEncryptConfig encryptConfig) {
        DiskEncryptionSettings diskEncryptionSettings = encryptConfig.osProfileEncryptionSettings();
        return virtualMachine.update()
                .withOsDiskEncryptionSettings(diskEncryptionSettings)
                .applyAsync();
    }

    private <ResultT> Observable<ResultT> toErrorObservable(String message) {
        return Observable.error(new Exception(message));
    }

    private String nextSequenceVersion(final VirtualMachineExtension encryptionExtension) {
        String nextSequenceVersion = "1";
        if (encryptionExtension.publicSettings().containsKey("SequenceVersion")) {
            String currentSequenceVersion = (String) encryptionExtension
                    .publicSettings()
                    .get("SequenceVersion");
            nextSequenceVersion = Integer.toString(Integer.parseInt(currentSequenceVersion) + 1);
        }
        return nextSequenceVersion;
    }

    /**
     * Base type representing configuration for enabling and disabling disk encryption.
     */
    private abstract class EnableDisableEncryptConfig {
        abstract public DiskEncryptionSettings osProfileEncryptionSettings();
        abstract public HashMap<String, Object> extensionPublicSettings();
        abstract public HashMap<String, Object> extensionProtectedSettings();
    }

    /**
     * Base type representing configuration for enabling disk encryption.
     *
     * @param <T>
     */
    private class EnableEncryptConfig<T extends VirtualMachineEncryptionSettings<T>> extends EnableDisableEncryptConfig {
        private final VirtualMachineEncryptionSettings<T> settings;

        EnableEncryptConfig(final VirtualMachineEncryptionSettings<T> settings) {
            this.settings = settings;
        }

        @Override
        public DiskEncryptionSettings osProfileEncryptionSettings() {
            DiskEncryptionSettings diskEncryptionSettings = new DiskEncryptionSettings();
            diskEncryptionSettings
                    .withEnabled(true)
                    .withDiskEncryptionKey(new KeyVaultSecretReference())
                    .diskEncryptionKey()
                    .withSourceVault(new SubResource().withId(settings.keyVaultId()));
            return diskEncryptionSettings;
        }

        @Override
        public HashMap<String, Object> extensionPublicSettings() {
            HashMap<String, Object> publicSettings = new LinkedHashMap<>();
            publicSettings.put("EncryptionOperation", "EnableEncryption");
            publicSettings.put("AADClientID", settings.aadClientId());
            publicSettings.put("KeyEncryptionAlgorithm", settings.volumeEncryptionKeyEncryptAlgorithm());
            publicSettings.put("KeyVaultURL", settings.keyVaultUrl());
            publicSettings.put("VolumeType", settings.volumeType().toString());
            if (settings.keyEncryptionKeyURL() != null) {
                publicSettings.put("KeyEncryptionKeyURL", settings.keyEncryptionKeyURL());
            }
            return publicSettings;
        }

        @Override
        public HashMap<String, Object> extensionProtectedSettings() {
            HashMap<String, Object> protectedSettings = new LinkedHashMap<>();
            protectedSettings.put("AADClientSecret", settings.aadSecret());
            if (settings.osType() == OperatingSystemTypes.LINUX
                    && settings.linuxPassPhrase() != null) {
                protectedSettings.put("Passphrase", settings.linuxPassPhrase());
            }
            return protectedSettings;
        }
    }

    /**
     * Base type representing configuration for disabling disk encryption.
     */
    private class DisableEncryptConfig extends EnableDisableEncryptConfig {
        @Override
        public DiskEncryptionSettings osProfileEncryptionSettings() {
            DiskEncryptionSettings diskEncryptionSettings = new DiskEncryptionSettings();
            diskEncryptionSettings
                    .withEnabled(false);
            return diskEncryptionSettings;
        }

        @Override
        public HashMap<String, Object> extensionPublicSettings() {
            HashMap<String, Object> publicSettings = new LinkedHashMap<>();
            publicSettings.put("EncryptionOperation", "DisableEncryption");
            return publicSettings;
        }

        @Override
        public HashMap<String, Object> extensionProtectedSettings() {
            return new LinkedHashMap<>();
        }
    }
}