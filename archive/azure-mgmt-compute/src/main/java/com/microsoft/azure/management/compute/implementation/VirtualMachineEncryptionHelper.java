/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.compute.DiskEncryptionSettings;
import com.microsoft.azure.management.compute.DiskVolumeEncryptionMonitor;
import com.microsoft.azure.management.compute.DiskVolumeType;
import com.microsoft.azure.management.compute.EncryptionStatus;
import com.microsoft.azure.management.compute.KeyVaultKeyReference;
import com.microsoft.azure.management.compute.KeyVaultSecretReference;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineEncryptionConfiguration;
import com.microsoft.azure.management.compute.VirtualMachineExtension;
import com.microsoft.azure.management.compute.VirtualMachineExtensionInstanceView;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * Helper type to enable or disable virtual machine disk (OS, Data) encryption.
 */
class VirtualMachineEncryptionHelper {
    private final String encryptionExtensionPublisher = "Microsoft.Azure.Security";
    private final OperatingSystemTypes osType;
    private final VirtualMachine virtualMachine;
    // Error messages
    private static final String ERROR_ENCRYPTION_EXTENSION_NOT_FOUND = "Expected encryption extension not found in the VM";
    private static final String ERROR_NON_SUCCESS_PROVISIONING_STATE = "Extension needed for disk encryption was not provisioned correctly, found ProvisioningState as '%s'";
    private static final String ERROR_EXPECTED_KEY_VAULT_URL_NOT_FOUND = "Could not found URL pointing to the secret for disk encryption";
    private static final String ERROR_EXPECTED_ENCRYPTION_EXTENSION_STATUS_NOT_FOUND = "Encryption extension with successful status not found in the VM";
    private static final String ERROR_ENCRYPTION_EXTENSION_STATUS_IS_EMPTY = "Encryption extension status is empty";
    private static final String ERROR_ON_LINUX_DECRYPTING_NON_DATA_DISK_IS_NOT_SUPPORTED = "Only data disk is supported to disable encryption on Linux VM";
    private static final String ERROR_ON_LINUX_DATA_DISK_DECRYPT_NOT_ALLOWED_IF_OS_DISK_IS_ENCRYPTED = "On Linux VM disabling data disk encryption is allowed only if OS disk is not encrypted";

    /**
     * Creates VirtualMachineEncryptionHelper.
     *
     * @param virtualMachine the virtual machine to enable or disable encryption
     */
    VirtualMachineEncryptionHelper(final VirtualMachine virtualMachine) {
        this.virtualMachine = virtualMachine;
        this.osType = this.virtualMachine.osType();
    }

    /**
     * Enables encryption.
     *
     * @param encryptionSettings the settings to be used for encryption extension
     * @param <T> the Windows or Linux encryption settings
     * @return an observable that emits the encryption status
     */
    <T extends VirtualMachineEncryptionConfiguration<T>> Observable<DiskVolumeEncryptionMonitor> enableEncryptionAsync(final VirtualMachineEncryptionConfiguration<T> encryptionSettings) {
        final EnableDisableEncryptConfig encryptConfig = new EnableEncryptConfig(encryptionSettings);
                // Update the encryption extension if already installed
        return updateEncryptionExtensionAsync(encryptConfig)
                // If encryption extension is not installed then install it
                .switchIfEmpty(installEncryptionExtensionAsync(encryptConfig))
                // Retrieve the encryption key URL after extension install or update
                .flatMap(new Func1<VirtualMachine, Observable<String>>() {
                    @Override
                    public Observable<String> call(VirtualMachine virtualMachine) {
                        return retrieveEncryptionExtensionStatusStringAsync(ERROR_EXPECTED_KEY_VAULT_URL_NOT_FOUND);
                    }
                })
                // Update the VM's OS Disk (in storage profile) with the encryption metadata
                .flatMap(new Func1<String, Observable<VirtualMachine>>() {
                    @Override
                    public Observable<VirtualMachine> call(String keyVaultSecretUrl) {
                        return updateVMStorageProfileAsync(encryptConfig, keyVaultSecretUrl);
                    }
                })
                // Gets the encryption status
                .flatMap(new Func1<VirtualMachine, Observable<DiskVolumeEncryptionMonitor>>() {
                    @Override
                    public Observable<DiskVolumeEncryptionMonitor> call(VirtualMachine virtualMachine) {
                        return getDiskVolumeEncryptDecryptStatusAsync(virtualMachine);
                    }
                });
    }

    /**
     * Disables encryption on the given disk volume.
     *
     * @param volumeType the disk volume
     * @return an observable that emits the decryption status
     */
    Observable<DiskVolumeEncryptionMonitor> disableEncryptionAsync(final DiskVolumeType volumeType) {
        final EnableDisableEncryptConfig encryptConfig = new DisableEncryptConfig(volumeType);
        return validateBeforeDecryptAsync(volumeType)
                // Update the encryption extension if already installed
                .flatMap(new Func1<Boolean, Observable<VirtualMachine>>() {
                    @Override
                    public Observable<VirtualMachine> call(Boolean aBoolean) {
                        return updateEncryptionExtensionAsync(encryptConfig);
                    }
                })
                // If encryption extension is not then install it
                .switchIfEmpty(installEncryptionExtensionAsync(encryptConfig))
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
                        return updateVMStorageProfileAsync(encryptConfig);
                    }
                })
                // Gets the encryption status
                .flatMap(new Func1<VirtualMachine, Observable<DiskVolumeEncryptionMonitor>>() {
                    @Override
                    public Observable<DiskVolumeEncryptionMonitor> call(VirtualMachine virtualMachine) {
                        return getDiskVolumeEncryptDecryptStatusAsync(virtualMachine);
                    }
                });
    }

    /**
     * @return OS specific encryption extension type
     */
    private String encryptionExtensionType() {
        if (this.osType == OperatingSystemTypes.LINUX) {
            return "AzureDiskEncryptionForLinux";
        } else {
            return "AzureDiskEncryption";
        }
    }

    /**
     * @return OS specific encryption extension version
     */
    private String encryptionExtensionVersion() {
        if (this.osType == OperatingSystemTypes.LINUX) {
            return "0.1";
        } else {
            return "1.1";
        }
    }

    /**
     * Checks the given volume type in the virtual machine can be decrypted.
     *
     * @param volumeType the volume type to decrypt
     * @return observable that emit true if no validation error otherwise error observable
     */
    private Observable<Boolean> validateBeforeDecryptAsync(final DiskVolumeType volumeType) {
        if (osType == OperatingSystemTypes.LINUX) {
            if (volumeType != DiskVolumeType.DATA) {
                return toErrorObservable(ERROR_ON_LINUX_DECRYPTING_NON_DATA_DISK_IS_NOT_SUPPORTED);
            }
            return getDiskVolumeEncryptDecryptStatusAsync(virtualMachine)
                    .flatMap(new Func1<DiskVolumeEncryptionMonitor, Observable<Boolean>>() {
                        @Override
                        public Observable<Boolean> call(DiskVolumeEncryptionMonitor status) {
                            if (status.osDiskStatus().equals(EncryptionStatus.ENCRYPTED)) {
                                return toErrorObservable(ERROR_ON_LINUX_DATA_DISK_DECRYPT_NOT_ALLOWED_IF_OS_DISK_IS_ENCRYPTED);
                            }
                            return Observable.just(true);
                        }
                    });
        }
        return Observable.just(true);
    }

    /**
     * Retrieves encryption extension installed in the virtual machine, if the extension is
     * not installed then return an empty observable.
     *
     * @return an observable that emits the encryption extension installed in the virtual machine
     */
    private Observable<VirtualMachineExtension> getEncryptionExtensionInstalledInVMAsync() {
        return virtualMachine.listExtensionsAsync()
                // firstOrDefault() is used intentionally here instead of first() to ensure
                // this method return empty observable if matching extension is not found.
                //
                .firstOrDefault(null, new Func1<VirtualMachineExtension, Boolean>() {
                    @Override
                    public Boolean call(final VirtualMachineExtension extension) {
                        return extension.publisherName().equalsIgnoreCase(encryptionExtensionPublisher)
                                && extension.typeName().equalsIgnoreCase(encryptionExtensionType());
                    }
                }).flatMap(new Func1<VirtualMachineExtension, Observable<VirtualMachineExtension>>() {
                    @Override
                    public Observable<VirtualMachineExtension> call(VirtualMachineExtension extension) {
                        if (extension == null) {
                            return Observable.empty();
                        }
                        return Observable.just(extension);
                    }
                });
    }

    /**
     * Updates the encryption extension in the virtual machine using provided configuration.
     * If extension is not installed then this method return empty observable.
     *
     * @param encryptConfig the volume encryption configuration
     * @return an observable that emits updated virtual machine
     */
    private Observable<VirtualMachine> updateEncryptionExtensionAsync(final EnableDisableEncryptConfig encryptConfig) {
        return getEncryptionExtensionInstalledInVMAsync()
                .flatMap(new Func1<VirtualMachineExtension, Observable<VirtualMachine>>() {
                    @Override
                    public Observable<VirtualMachine> call(final VirtualMachineExtension encryptionExtension) {
                        final HashMap<String, Object> publicSettings = encryptConfig.extensionPublicSettings();
                        return virtualMachine.update()
                                .updateExtension(encryptionExtension.name())
                                    .withPublicSettings(publicSettings)
                                    .withProtectedSettings(encryptConfig.extensionProtectedSettings())
                                    .parent()
                                .applyAsync();
                    }
                });
    }

    /**
     * Prepare encryption extension using provided configuration and install it in the virtual machine.
     *
     * @param encryptConfig the volume encryption configuration
     * @return an observable that emits updated virtual machine
     */
    private Observable<VirtualMachine> installEncryptionExtensionAsync(final EnableDisableEncryptConfig encryptConfig) {
        return Observable.defer(new Func0<Observable<VirtualMachine>>() {
            @Override
            public Observable<VirtualMachine> call() {
                final String extensionName = encryptionExtensionType();
                return virtualMachine.update()
                        .defineNewExtension(extensionName)
                        .withPublisher(encryptionExtensionPublisher)
                        .withType(encryptionExtensionType())
                        .withVersion(encryptionExtensionVersion())
                        .withPublicSettings(encryptConfig.extensionPublicSettings())
                        .withProtectedSettings(encryptConfig.extensionProtectedSettings())
                        .withMinorVersionAutoUpgrade()
                        .attach()
                        .applyAsync();
            }
        });
    }

    /**
     * Retrieves the encryption extension status from the extension instance view.
     * An error observable will be returned if
     *   1. extension is not installed
     *   2. extension is not provisioned successfully
     *   2. extension status could be retrieved (either not found or empty)
     *
     * @param statusEmptyErrorMessage the error message to emit if unable to locate the status
     * @return an observable that emits status message
     */
    private Observable<String> retrieveEncryptionExtensionStatusStringAsync(final String statusEmptyErrorMessage) {
        final VirtualMachineEncryptionHelper self = this;
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

    /**
     * Updates the virtual machine's OS Disk model with the encryption specific details so that platform can
     * use it while booting the virtual machine.
     *
     * @param encryptConfig the configuration specific to enabling the encryption
     * @param encryptionSecretKeyVaultUrl the keyVault URL pointing to secret holding disk encryption key
     * @return an observable that emits updated virtual machine
     */
    private Observable<VirtualMachine> updateVMStorageProfileAsync(final EnableDisableEncryptConfig encryptConfig,
                                                                   final String encryptionSecretKeyVaultUrl) {
        DiskEncryptionSettings diskEncryptionSettings = encryptConfig.storageProfileEncryptionSettings();
        diskEncryptionSettings.diskEncryptionKey()
                .withSecretUrl(encryptionSecretKeyVaultUrl);
        return virtualMachine.update()
                .withOSDiskEncryptionSettings(diskEncryptionSettings)
                .applyAsync();
    }

    /**
     * Updates the virtual machine's OS Disk model with the encryption specific details.
     *
     * @param encryptConfig the configuration specific to disabling the encryption
     * @return an observable that emits updated virtual machine
     */
    private Observable<VirtualMachine> updateVMStorageProfileAsync(final EnableDisableEncryptConfig encryptConfig) {
        DiskEncryptionSettings diskEncryptionSettings = encryptConfig.storageProfileEncryptionSettings();
        return virtualMachine.update()
                .withOSDiskEncryptionSettings(diskEncryptionSettings)
                .applyAsync();
    }

    /**
     * Gets status object that describes the current status of the volume encryption or decryption process.
     *
     * @param virtualMachine the virtual machine on which encryption or decryption is running
     * @return an observable that emits current encrypt or decrypt status
     */
    private Observable<DiskVolumeEncryptionMonitor> getDiskVolumeEncryptDecryptStatusAsync(VirtualMachine virtualMachine) {
        if (osType == OperatingSystemTypes.LINUX) {
            return new LinuxDiskVolumeEncryptionMonitorImpl(virtualMachine.id(), virtualMachine.manager()).refreshAsync();
        } else {
            return new WindowsVolumeEncryptionMonitorImpl(virtualMachine.id(), virtualMachine.manager()).refreshAsync();
        }
    }

    /**
     * Wraps the given message in an error observable.
     *
     * @param message the error message
     * @param <ResultT> observable type
     * @return error observable with message wrapped
     */
    private <ResultT> Observable<ResultT> toErrorObservable(String message) {
        return Observable.error(new Exception(message));
    }

    /**
     * Base type representing configuration for enabling and disabling disk encryption.
     */
    private abstract class EnableDisableEncryptConfig {
        /**
         * @return encryption specific settings to be set on virtual machine storage profile
         */
        public abstract DiskEncryptionSettings storageProfileEncryptionSettings();
        /**
         * @return encryption extension public settings
         */
        public abstract HashMap<String, Object> extensionPublicSettings();
        /**
         * @return encryption extension protected settings
         */
        public abstract HashMap<String, Object> extensionProtectedSettings();
    }

    /**
     * Base type representing configuration for enabling disk encryption.
     *
     * @param <T>
     */
    private class EnableEncryptConfig<T extends VirtualMachineEncryptionConfiguration<T>> extends EnableDisableEncryptConfig {
        private final VirtualMachineEncryptionConfiguration<T> settings;

        EnableEncryptConfig(final VirtualMachineEncryptionConfiguration<T> settings) {
            this.settings = settings;
        }

        @Override
        public DiskEncryptionSettings storageProfileEncryptionSettings() {
            KeyVaultKeyReference keyEncryptionKey = null;
            if (settings.keyEncryptionKeyURL() != null) {
                keyEncryptionKey = new KeyVaultKeyReference();
                keyEncryptionKey.withKeyUrl(settings.keyEncryptionKeyURL());
                if (settings.keyEncryptionKeyVaultId() != null) {
                    keyEncryptionKey.withSourceVault(new SubResource().withId(settings.keyEncryptionKeyVaultId()));
                }
            }
            DiskEncryptionSettings diskEncryptionSettings = new DiskEncryptionSettings();
            diskEncryptionSettings
                    .withEnabled(true)
                    .withKeyEncryptionKey(keyEncryptionKey)
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
            publicSettings.put("SequenceVersion", UUID.randomUUID());
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
        private final DiskVolumeType volumeType;

        DisableEncryptConfig(final DiskVolumeType volumeType) {
            this.volumeType = volumeType;
        }

        @Override
        public DiskEncryptionSettings storageProfileEncryptionSettings() {
            DiskEncryptionSettings diskEncryptionSettings = new DiskEncryptionSettings();
            diskEncryptionSettings
                    .withEnabled(false);
            return diskEncryptionSettings;
        }

        @Override
        public HashMap<String, Object> extensionPublicSettings() {
            HashMap<String, Object> publicSettings = new LinkedHashMap<>();
            publicSettings.put("EncryptionOperation", "DisableEncryption");
            publicSettings.put("SequenceVersion", UUID.randomUUID());
            publicSettings.put("VolumeType", this.volumeType);
            return publicSettings;
        }

        @Override
        public HashMap<String, Object> extensionProtectedSettings() {
            return new LinkedHashMap<>();
        }
    }
}