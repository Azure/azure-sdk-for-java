// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.compute.models.DiskEncryptionSettings;
import com.azure.resourcemanager.compute.models.DiskVolumeType;
import com.azure.resourcemanager.compute.models.KeyVaultKeyReference;
import com.azure.resourcemanager.compute.models.KeyVaultSecretReference;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineEncryptionConfiguration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

/** Internal base type exposing settings to enable and disable disk encryption extension. */
abstract class EncryptionSettings {
    /** @return encryption specific settings to be set on virtual machine storage profile */
    abstract DiskEncryptionSettings storageProfileEncryptionSettings();
    /** @return encryption extension public settings */
    abstract HashMap<String, Object> extensionPublicSettings();
    /** @return encryption extension protected settings */
    abstract HashMap<String, Object> extensionProtectedSettings();

    /**
     * Creates an instance of type representing settings to enable encryption.
     *
     * @param config the user provided encryption configuration
     * @param <T> the config type
     * @return enable settings
     */
    static <T extends VirtualMachineEncryptionConfiguration<T>> Enable<T> createEnable(
        final VirtualMachineEncryptionConfiguration<T> config) {
        return new Enable<T>(config);
    }

    /**
     * Creates an instance of type representing settings to disable encryption.
     *
     * @param volumeType the disk volume that user required to disable encryption for
     * @return disable settings
     */
    static Disable createDisable(final DiskVolumeType volumeType) {
        return new EncryptionSettings.Disable(volumeType);
    }

    /**
     * Internal type exposing settings for enabling disk encryption.
     *
     * @param <T>
     */
    static class Enable<T extends VirtualMachineEncryptionConfiguration<T>> extends EncryptionSettings {
        private final VirtualMachineEncryptionConfiguration<T> config;

        Enable(final VirtualMachineEncryptionConfiguration<T> config) {
            this.config = config;
        }

        @Override
        DiskEncryptionSettings storageProfileEncryptionSettings() {
            KeyVaultKeyReference keyEncryptionKey = null;
            if (config.keyEncryptionKeyUrl() != null) {
                keyEncryptionKey = new KeyVaultKeyReference();
                keyEncryptionKey.withKeyUrl(config.keyEncryptionKeyUrl());
                if (config.keyEncryptionKeyVaultId() != null) {
                    keyEncryptionKey.withSourceVault(new SubResource().withId(config.keyEncryptionKeyVaultId()));
                }
            }
            DiskEncryptionSettings diskEncryptionSettings = new DiskEncryptionSettings();
            diskEncryptionSettings
                .withEnabled(true)
                .withKeyEncryptionKey(keyEncryptionKey)
                .withDiskEncryptionKey(new KeyVaultSecretReference())
                .diskEncryptionKey()
                .withSourceVault(new SubResource().withId(config.keyVaultId()));
            return diskEncryptionSettings;
        }

        @Override
        HashMap<String, Object> extensionPublicSettings() {
            HashMap<String, Object> publicSettings = new LinkedHashMap<>();
            publicSettings.put("EncryptionOperation", "EnableEncryption");
            publicSettings.put("KeyEncryptionAlgorithm", config.volumeEncryptionKeyEncryptAlgorithm());
            publicSettings.put("KeyVaultURL", config.keyVaultUrl()); // KeyVault to hold "Disk Encryption Key".
            publicSettings.put("VolumeType", config.volumeType().toString());
            publicSettings.put("SequenceVersion", UUID.randomUUID());
            if (config.keyEncryptionKeyUrl() != null) {
                publicSettings
                    .put(
                        "KeyEncryptionKeyURL",
                        config
                            .keyEncryptionKeyUrl()); // KeyVault to hold Key for encrypting "Disk Encryption Key" (aka
                                                     // kek).
            }
            if (this.requestedForLegacyEncryptExtension()) {
                // Legacy-Encrypt-Extension requires AAD credentials (AADClientID in PublicSettings & AADClientSecret in
                // ProtectedSettings) to access KeyVault.
                publicSettings.put("AADClientID", config.aadClientId());
            } else {
                // Without AAD credentials (AADClientID in PublicSettings & AADClientSecret in ProtectedSettings) to
                // access KeyVault,
                // ARM resource id of KeyVaults are required.
                //
                publicSettings.put("KeyVaultResourceId", config.keyVaultId());
                if (config.keyEncryptionKeyUrl() != null && config.keyEncryptionKeyVaultId() != null) {
                    publicSettings.put("KekVaultResourceId", config.keyEncryptionKeyVaultId());
                }
            }
            return publicSettings;
        }

        @Override
        HashMap<String, Object> extensionProtectedSettings() {
            if (this.requestedForLegacyEncryptExtension()) {
                HashMap<String, Object> protectedSettings = new LinkedHashMap<>();
                // NoAAD-Encrypt-Extension requires AAD credentials (AADClientID in PublicSettings & AADClientSecret in
                // ProtectedSettings) to access KeyVault.
                protectedSettings.put("AADClientSecret", config.aadSecret());
                if (config.osType() == OperatingSystemTypes.LINUX && config.linuxPassPhrase() != null) {
                    protectedSettings.put("Passphrase", config.linuxPassPhrase());
                }
                return protectedSettings;
            } else {
                // No protected settings for NoAAD-Encrypt-Extension.
                //
                return new LinkedHashMap<>();
            }
        }

        /** @return the encryption version based on user selected OS and encryption extension. */
        String encryptionExtensionVersion() {
            return EncryptionExtensionIdentifier.version(this.config.osType(), requestedForNoAADEncryptExtension());
        }

        /** @return true if user requested for NoAAD-Encrypt-Extension. */
        boolean requestedForNoAADEncryptExtension() {
            return this.config.aadClientId() == null && this.config.aadSecret() == null;
        }

        /** @return true if user requested for Legacy-Encrypt-Extension. */
        boolean requestedForLegacyEncryptExtension() {
            return !requestedForNoAADEncryptExtension();
        }
    }

    /** Internal type exposing settings for disabling disk encryption. */
    static class Disable extends EncryptionSettings {
        private final DiskVolumeType volumeType;

        Disable(final DiskVolumeType volumeType) {
            this.volumeType = volumeType;
        }

        @Override
        DiskEncryptionSettings storageProfileEncryptionSettings() {
            DiskEncryptionSettings diskEncryptionSettings = new DiskEncryptionSettings();
            diskEncryptionSettings.withEnabled(false);
            return diskEncryptionSettings;
        }

        @Override
        HashMap<String, Object> extensionPublicSettings() {
            HashMap<String, Object> publicSettings = new LinkedHashMap<>();
            publicSettings.put("EncryptionOperation", "DisableEncryption");
            publicSettings.put("SequenceVersion", UUID.randomUUID());
            publicSettings.put("VolumeType", this.volumeType);
            return publicSettings;
        }

        @Override
        HashMap<String, Object> extensionProtectedSettings() {
            return new LinkedHashMap<>();
        }
    }
}
