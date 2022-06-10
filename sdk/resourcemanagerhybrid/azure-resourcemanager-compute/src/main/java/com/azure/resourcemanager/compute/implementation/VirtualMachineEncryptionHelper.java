// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.DiskEncryptionSettings;
import com.azure.resourcemanager.compute.models.DiskVolumeEncryptionMonitor;
import com.azure.resourcemanager.compute.models.DiskVolumeType;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineEncryptionConfiguration;
import com.azure.resourcemanager.compute.models.VirtualMachineExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Helper type to enable or disable virtual machine disk (OS, Data) encryption. */
class VirtualMachineEncryptionHelper {
    private final OperatingSystemTypes osType;
    private final VirtualMachine virtualMachine;
    // Error messages
    private static final String ERROR_ENCRYPTION_EXTENSION_NOT_FOUND =
        "Expected encryption extension not found in the VM";
    private static final String ERROR_NON_SUCCESS_PROVISIONING_STATE =
        "Extension needed for disk encryption was not provisioned correctly, found ProvisioningState as '%s'";
    private static final String ERROR_EXPECTED_KEY_VAULT_URL_NOT_FOUND =
        "Could not found URL pointing to the secret for disk encryption";
    private static final String ERROR_EXPECTED_ENCRYPTION_EXTENSION_STATUS_NOT_FOUND =
        "Encryption extension with successful status not found in the VM";
    private static final String ERROR_ENCRYPTION_EXTENSION_STATUS_IS_EMPTY = "Encryption extension status is empty";
    private static final String ERROR_ON_LINUX_ONLY_DATA_DISK_CAN_BE_DECRYPTED =
        "Only data disk is supported to disable encryption on Linux VM";
    private static final String ERROR_LEGACY_ENCRYPTION_EXTENSION_FOUND_AAD_PARAMS_REQUIRED =
        "VM has Legacy Encryption Extension installed, updating it requires aadClientId and aadSecret parameters";
    private static final String ERROR_NOAAD_ENCRYPTION_EXTENSION_FOUND_AAD_PARAMS_NOT_REQUIRED =
        "VM has NoAAD Encryption Extension installed, aadClientId and aadSecret parameters are not allowed for this"
            + " extension.";
    private static final String ERROR_NO_DECRYPT_ENCRYPTION_EXTENSION_NOT_FOUND =
        ERROR_ENCRYPTION_EXTENSION_NOT_FOUND + ", no decryption to perform";
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
     * @param encryptionConfig the settings to be used for encryption extension
     * @param <T> the Windows or Linux encryption settings
     * @return an observable that emits the encryption status
     */
    <T extends VirtualMachineEncryptionConfiguration<T>> Mono<DiskVolumeEncryptionMonitor> enableEncryptionAsync(
        final VirtualMachineEncryptionConfiguration<T> encryptionConfig) {
        final EncryptionSettings.Enable<T> encryptSettings = EncryptionSettings.<T>createEnable(encryptionConfig);
        // If encryption extension is already installed then ensure user input aligns with state of the extension
        return validateBeforeEncryptAsync(encryptSettings)
            // If encryption extension is already installed then update it
            .flatMap(
                virtualMachineExtension -> updateEncryptionExtensionAsync(encryptSettings, virtualMachineExtension))
            // If encryption extension is not installed then install it
            .switchIfEmpty(installEncryptionExtensionAsync(encryptSettings))
            .flatMap(
                virtualMachine -> {
                    if (encryptSettings.requestedForNoAADEncryptExtension()) {
                        return noAADExtensionEncryptPostProcessingAsync(virtualMachine);
                    } else {
                        return legacyExtensionEncryptPostProcessingAsync(encryptSettings);
                    }
                });
    }

    /**
     * Disables encryption on the given disk volume.
     *
     * @param volumeType the disk volume
     * @return an observable that emits the decryption status
     */
    Mono<DiskVolumeEncryptionMonitor> disableEncryptionAsync(final DiskVolumeType volumeType) {
        final EncryptionSettings.Disable encryptSettings = EncryptionSettings.createDisable(volumeType);
        //
        return validateBeforeDecryptAsync(volumeType)
            // Update the encryption extension
            .flatMap(
                virtualMachineExtension ->
                    updateEncryptionExtensionAsync(encryptSettings, virtualMachineExtension)
                        .map(virtualMachine -> new VMExtTuple(virtualMachine, virtualMachineExtension)))
            .flatMap(
                vmExt -> {
                    if (EncryptionExtensionIdentifier.isNoAADVersion(osType, vmExt.encryptExtension.versionName())) {
                        return noAADExtensionDecryptPostProcessingAsync(vmExt.virtualMachine);
                    } else {
                        return legacyExtensionDecryptPostProcessingAsync(encryptSettings);
                    }
                });
    }

    /**
     * Perform any post processing after initiating VM encryption through NoAAD extension.
     *
     * @param virtualMachine the encrypted virtual machine
     * @return the encryption progress monitor
     */
    private Mono<DiskVolumeEncryptionMonitor> noAADExtensionEncryptPostProcessingAsync(
        final VirtualMachine virtualMachine) {
        // Gets the encryption status
        return osType == OperatingSystemTypes.LINUX
            ? new LinuxDiskVolumeNoAADEncryptionMonitorImpl(virtualMachine.id(), virtualMachine.manager())
                .refreshAsync()
            : new WindowsVolumeNoAADEncryptionMonitorImpl(virtualMachine.id(), virtualMachine.manager()).refreshAsync();
    }

    /**
     * Perform any post processing after initiating VM encryption through Legacy extension.
     *
     * @param encryptConfig the user provided encryption config
     * @return the encryption progress monitor
     */
    private <T extends VirtualMachineEncryptionConfiguration<T>>
        Mono<DiskVolumeEncryptionMonitor> legacyExtensionEncryptPostProcessingAsync(
            final EncryptionSettings.Enable<T> encryptConfig) {
        // Retrieve the encryption key URL after extension install or update
        return retrieveEncryptionExtensionStatusStringAsync(ERROR_EXPECTED_KEY_VAULT_URL_NOT_FOUND)
            // Update the VM's OS Disk (in storage profile) with the encryption metadata
            .flatMap(keyVaultSecretUrl -> updateVMStorageProfileAsync(encryptConfig, keyVaultSecretUrl))
            // Gets the encryption status
            .flatMap(
                virtualMachine ->
                    osType == OperatingSystemTypes.LINUX
                        ? new LinuxDiskVolumeLegacyEncryptionMonitorImpl(virtualMachine.id(), virtualMachine.manager())
                            .refreshAsync()
                        : new WindowsVolumeLegacyEncryptionMonitorImpl(virtualMachine.id(), virtualMachine.manager())
                            .refreshAsync());
    }

    /**
     * Perform any post processing after initiating VM decryption through NoAAD extension.
     *
     * @param virtualMachine the decrypted virtual machine
     * @return the decryption progress monitor
     */
    private Mono<DiskVolumeEncryptionMonitor> noAADExtensionDecryptPostProcessingAsync(
        final VirtualMachine virtualMachine) {
        // Gets the encryption status
        return osType == OperatingSystemTypes.LINUX
            ? new LinuxDiskVolumeNoAADEncryptionMonitorImpl(virtualMachine.id(), virtualMachine.manager())
                .refreshAsync()
            : new WindowsVolumeNoAADEncryptionMonitorImpl(virtualMachine.id(), virtualMachine.manager()).refreshAsync();
    }

    /**
     * Perform any post processing after initiating VM encryption through Legacy extension.
     *
     * @param encryptConfig the user provided encryption config
     * @return the encryption progress monitor
     */
    private Mono<DiskVolumeEncryptionMonitor> legacyExtensionDecryptPostProcessingAsync(
        final EncryptionSettings.Disable encryptConfig) {
        return retrieveEncryptionExtensionStatusStringAsync(ERROR_ENCRYPTION_EXTENSION_STATUS_IS_EMPTY)
            // Update the VM's OS profile by marking encryption disabled
            .flatMap(s -> updateVMStorageProfileAsync(encryptConfig))
            // Gets the encryption status
            .flatMap(
                virtualMachine ->
                    osType == OperatingSystemTypes.LINUX
                        ? new LinuxDiskVolumeLegacyEncryptionMonitorImpl(virtualMachine.id(), virtualMachine.manager())
                            .refreshAsync()
                        : new WindowsVolumeLegacyEncryptionMonitorImpl(virtualMachine.id(), virtualMachine.manager())
                            .refreshAsync());
    }

    /**
     * If VM has encryption extension installed then validate that it can be updated based on user provided params, if
     * invalid then return observable emitting error otherwise an observable emitting the extension. If extension is not
     * installed then return empty observable.
     *
     * @param encryptSettings the user provided configuration
     * @return observable emitting error, extension or empty.
     */
    private <T extends VirtualMachineEncryptionConfiguration<T>>
        Mono<VirtualMachineExtension> validateBeforeEncryptAsync(final EncryptionSettings.Enable<T> encryptSettings) {
        if (this.virtualMachine.storageProfile().osDisk().encryptionSettings() != null
            && encryptSettings.requestedForNoAADEncryptExtension()) {
            return Mono.error(new RuntimeException(ERROR_LEGACY_ENCRYPTION_EXTENSION_FOUND_AAD_PARAMS_REQUIRED));
        }
        return getEncryptionExtensionInstalledInVMAsync()
            .flatMap(
                extension -> {
                    if (EncryptionExtensionIdentifier.isNoAADVersion(osType, extension.versionName())) {
                        // NoAAD-Encrypt-Extension exists so Legacy-Encrypt-Extension cannot be installed hence AAD
                        // params are not required.
                        return encryptSettings.requestedForNoAADEncryptExtension()
                            ? Mono.just(extension)
                            : Mono
                                .error(
                                    new RuntimeException(
                                        ERROR_NOAAD_ENCRYPTION_EXTENSION_FOUND_AAD_PARAMS_NOT_REQUIRED));
                    } else {
                        // Legacy-Encrypt-Extension exists so NoAAD-Encrypt-Extension cannot be installed hence AAD
                        // params are required.
                        return encryptSettings.requestedForNoAADEncryptExtension()
                            ? Mono
                                .error(
                                    new RuntimeException(ERROR_LEGACY_ENCRYPTION_EXTENSION_FOUND_AAD_PARAMS_REQUIRED))
                            : Mono.just(extension);
                    }
                });
    }

    /**
     * Checks the given volume type in the virtual machine can be decrypted.
     *
     * @param volumeType the volume type to decrypt
     * @return observable that emit existing encryption extension if installed else empty observable
     */
    private Mono<VirtualMachineExtension> validateBeforeDecryptAsync(final DiskVolumeType volumeType) {
        if (osType == OperatingSystemTypes.LINUX && volumeType != DiskVolumeType.DATA) {
            return toErrorMono(ERROR_ON_LINUX_ONLY_DATA_DISK_CAN_BE_DECRYPTED);
        }
        return getEncryptionExtensionInstalledInVMAsync()
            .switchIfEmpty(this.toErrorMono(ERROR_NO_DECRYPT_ENCRYPTION_EXTENSION_NOT_FOUND));
    }

    /**
     * Retrieves encryption extension installed in the virtual machine, if the extension is not installed then return an
     * empty observable.
     *
     * @return an observable that emits the encryption extension installed in the virtual machine
     */
    private Mono<VirtualMachineExtension> getEncryptionExtensionInstalledInVMAsync() {
        return virtualMachine
            .listExtensionsAsync()
            .flatMapMany(Flux::fromIterable)
            // firstOrDefault() is used intentionally here instead of first() to ensure
            // this method return empty observable if matching extension is not found.
            .filter(
                extension ->
                    EncryptionExtensionIdentifier.isEncryptionPublisherName(extension.publisherName())
                        && EncryptionExtensionIdentifier.isEncryptionTypeName(extension.typeName(), osType))
            .singleOrEmpty();
    }

    /**
     * Updates the encryption extension in the virtual machine using provided configuration. If extension is not
     * installed then this method return empty observable.
     *
     * @param encryptSettings the volume encryption extension settings
     * @param encryptionExtension existing encryption extension
     * @return an observable that emits updated virtual machine if extension was already installed otherwise an empty
     *     observable.
     */
    private Mono<VirtualMachine> updateEncryptionExtensionAsync(
        final EncryptionSettings encryptSettings, VirtualMachineExtension encryptionExtension) {
        return virtualMachine
            .update()
            .updateExtension(encryptionExtension.name())
            .withPublicSettings(encryptSettings.extensionPublicSettings())
            .withProtectedSettings(encryptSettings.extensionProtectedSettings())
            .parent()
            .applyAsync();
    }

    /**
     * Prepare encryption extension using provided configuration and install it in the virtual machine.
     *
     * @param encryptSettings the volume encryption configuration
     * @return an observable that emits updated virtual machine
     */
    private <T extends VirtualMachineEncryptionConfiguration<T>> Mono<VirtualMachine> installEncryptionExtensionAsync(
        final EncryptionSettings.Enable<T> encryptSettings) {
        return Mono
            .defer(
                () -> {
                    final String typeName = EncryptionExtensionIdentifier.typeName(osType);
                    return virtualMachine
                        .update()
                        .defineNewExtension(typeName)
                        .withPublisher(EncryptionExtensionIdentifier.publisherName())
                        .withType(typeName)
                        .withVersion(encryptSettings.encryptionExtensionVersion())
                        .withPublicSettings(encryptSettings.extensionPublicSettings())
                        .withProtectedSettings(encryptSettings.extensionProtectedSettings())
                        .withMinorVersionAutoUpgrade()
                        .attach()
                        .applyAsync();
                });
    }

    /**
     * Retrieves the encryption extension status from the extension instance view. An error observable will be returned
     * if 1. extension is not installed 2. extension is not provisioned successfully 2. extension status could be
     * retrieved (either not found or empty)
     *
     * @param statusEmptyErrorMessage the error message to emit if unable to locate the status
     * @return an observable that emits status message
     */
    private Mono<String> retrieveEncryptionExtensionStatusStringAsync(final String statusEmptyErrorMessage) {
        final VirtualMachineEncryptionHelper self = this;
        return getEncryptionExtensionInstalledInVMAsync()
            .switchIfEmpty(self.toErrorMono(ERROR_ENCRYPTION_EXTENSION_NOT_FOUND))
            .flatMap(
                extension -> {
                    if (!extension.provisioningState().equalsIgnoreCase("Succeeded")) {
                        return self
                            .toErrorMono(
                                (String.format(ERROR_NON_SUCCESS_PROVISIONING_STATE, extension.provisioningState())));
                    }
                    return extension.getInstanceViewAsync();
                })
            .flatMap(
                instanceView -> {
                    String extensionStatus = instanceView.statuses().get(0).message();
                    if (extensionStatus == null) {
                        return self.toErrorMono(statusEmptyErrorMessage);
                    }
                    return Mono.just(extensionStatus);
                })
            .switchIfEmpty(self.toErrorMono(ERROR_EXPECTED_ENCRYPTION_EXTENSION_STATUS_NOT_FOUND));
    }

    /**
     * Updates the virtual machine's OS Disk model with the encryption specific details so that platform can use it
     * while booting the virtual machine.
     *
     * @param encryptSettings the configuration specific to enabling the encryption
     * @param encryptionSecretKeyVaultUrl the keyVault URL pointing to secret holding disk encryption key
     * @return an observable that emits updated virtual machine
     */
    private Mono<VirtualMachine> updateVMStorageProfileAsync(
        final EncryptionSettings encryptSettings, final String encryptionSecretKeyVaultUrl) {
        DiskEncryptionSettings diskEncryptionSettings = encryptSettings.storageProfileEncryptionSettings();
        diskEncryptionSettings.diskEncryptionKey().withSecretUrl(encryptionSecretKeyVaultUrl);
        return virtualMachine.update().withOSDiskEncryptionSettings(diskEncryptionSettings).applyAsync();
    }

    /**
     * Updates the virtual machine's OS Disk model with the encryption specific details.
     *
     * @param encryptSettings the configuration specific to disabling the encryption
     * @return an observable that emits updated virtual machine
     */
    private Mono<VirtualMachine> updateVMStorageProfileAsync(final EncryptionSettings encryptSettings) {
        DiskEncryptionSettings diskEncryptionSettings = encryptSettings.storageProfileEncryptionSettings();
        return virtualMachine.update().withOSDiskEncryptionSettings(diskEncryptionSettings).applyAsync();
    }

    /**
     * Wraps the given message in an error Mono.
     *
     * @param message the error message
     * @param <ResultT> Mono type
     * @return error Mono with message wrapped
     */
    private <ResultT> Mono<ResultT> toErrorMono(String message) {
        return Mono.error(new Exception(message));
    }

    private static class VMExtTuple {
        private final VirtualMachine virtualMachine;
        private final VirtualMachineExtension encryptExtension;
        //
        VMExtTuple(VirtualMachine vm, VirtualMachineExtension ext) {
            this.virtualMachine = vm;
            this.encryptExtension = ext;
        }
    }
}
