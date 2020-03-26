/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage.implementation;

import com.azure.management.storage.Encryption;
import com.azure.management.storage.EncryptionService;
import com.azure.management.storage.EncryptionServices;
import com.azure.management.storage.KeySource;
import com.azure.management.storage.KeyVaultProperties;
import com.azure.management.storage.StorageAccountCreateParameters;
import com.azure.management.storage.StorageAccountEncryptionKeySource;
import com.azure.management.storage.StorageAccountEncryptionStatus;
import com.azure.management.storage.StorageAccountUpdateParameters;
import com.azure.management.storage.StorageService;
import com.azure.management.storage.models.StorageAccountInner;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper to operate on storage account encryption {@link StorageAccountInner#encryption} property.
 */
final class StorageEncryptionHelper {
    private final boolean isInCreateMode;
    private final StorageAccountInner inner;
    private final StorageAccountCreateParameters createParameters;
    private final StorageAccountUpdateParameters updateParameters;

    /**
     * Creates StorageEncryptionHelper.
     *
     * @param createParameters the model representing payload for storage account create.
     */
    StorageEncryptionHelper(StorageAccountCreateParameters createParameters) {
        this.isInCreateMode = true;
        this.createParameters = createParameters;
        this.updateParameters = null;
        this.inner = null;
    }

    /**
     * Creates StorageEncryptionHelper.
     *
     * @param updateParameters the model representing payload for storage account update
     * @param inner            the current state of storage account
     */
    StorageEncryptionHelper(StorageAccountUpdateParameters updateParameters, final StorageAccountInner inner) {
        this.isInCreateMode = false;
        this.createParameters = null;
        this.updateParameters = updateParameters;
        this.inner = inner;
    }

    /**
     * Gets the encryption key source.
     *
     * @param inner the storage account
     * @return the encryption key source type
     */
    static StorageAccountEncryptionKeySource encryptionKeySource(StorageAccountInner inner) {
        if (inner.getEncryption() == null
                || inner.getEncryption().getKeySource() == null) {
            return null;
        }
        return StorageAccountEncryptionKeySource.fromString(inner.getEncryption().getKeySource().toString());
    }

    /**
     * Gets the encryption status of various storage services.
     *
     * @param inner the storage account
     * @return the map containing encryption statuses indexed by storage service name
     */
    static Map<StorageService, StorageAccountEncryptionStatus> encryptionStatuses(StorageAccountInner inner) {
        HashMap<StorageService, StorageAccountEncryptionStatus> statuses = new HashMap<>();
        EncryptionServices services = null;
        if (inner.getEncryption() != null) {
            services = inner.getEncryption().getServices();
        }
        statuses.put(StorageService.BLOB, new BlobServiceEncryptionStatusImpl(services));
        statuses.put(StorageService.FILE, new FileServiceEncryptionStatusImpl(services));
        statuses.put(StorageService.TABLE, new TableServiceEncryptionStatusImpl(services));
        statuses.put(StorageService.QUEUE, new QueueServiceEncryptionStatusImpl(services));
        return statuses;
    }

    /**
     * Specifies that storage blob encryption should be enabled.
     *
     * @return StorageEncryptionHelper
     */
    StorageEncryptionHelper withBlobEncryption() {
        Encryption encryption = getEncryptionConfig(true);
        if (encryption.getServices() == null) {
            encryption.setServices(new EncryptionServices());
        }
        // Enable encryption for blob service
        //
        if (encryption.getServices().getBlob() == null) {
            encryption.getServices().setBlob(new EncryptionService());
        }
        encryption.getServices().getBlob().setEnabled(true);
        if (encryption.getKeySource() == null) {
            encryption.setKeySource(KeySource.MICROSOFT_STORAGE);
        }
        return this;
    }

    /**
     * Specifies that storage file encryption should be enabled.
     *
     * @return StorageEncryptionHelper
     */
    StorageEncryptionHelper withFileEncryption() {
        Encryption encryption = getEncryptionConfig(true);
        if (encryption.getServices() == null) {
            encryption.setServices(new EncryptionServices());
        }
        // Enable encryption for file service
        //
        if (encryption.getServices().getFile() == null) {
            encryption.getServices().setFile(new EncryptionService());
        }
        encryption.getServices().getFile().setEnabled(true);
        if (encryption.getKeySource() == null) {
            encryption.setKeySource(KeySource.MICROSOFT_STORAGE);
        }
        return this;
    }

    /**
     * Specifies the key vault key to be used to encrypt the blobs and files.
     *
     * @return StorageEncryptionHelper
     */
    StorageEncryptionHelper withEncryptionKeyFromKeyVault(String keyVaultUri, String keyName, String keyVersion) {
        Encryption encryption = getEncryptionConfig(true);
        encryption.setKeySource(KeySource.MICROSOFT_KEYVAULT);
        encryption.setKeyVaultProperties(new KeyVaultProperties()
                .setKeyVaultUri(keyVaultUri)
                .setKeyName(keyName)
                .setKeyVersion(keyVersion));
        return this;
    }

    /**
     * Specifies that blob encryption should be disabled for storage blob.
     *
     * @return StorageEncryptionHelper
     */
    StorageEncryptionHelper withoutBlobEncryption() {
        Encryption encryption = getEncryptionConfig(true);
        if (encryption.getServices() == null) {
            encryption.setServices(new EncryptionServices());
        }
        // Disable encryption for blob service
        //
        if (encryption.getServices().getBlob() == null) {
            encryption.getServices().setBlob(new EncryptionService());
        }
        encryption.getServices().getBlob().setEnabled(false);
        if (encryption.getKeySource() == null) {
            encryption.setKeySource(KeySource.MICROSOFT_STORAGE);
        }
        return this;
    }

    /**
     * Specifies that encryption should be disabled for storage file.
     *
     * @return StorageEncryptionHelper
     */
    StorageEncryptionHelper withoutFileEncryption() {
        Encryption encryption = getEncryptionConfig(true);
        if (encryption.getServices() == null) {
            encryption.setServices(new EncryptionServices());
        }
        // Disable encryption for blob service
        //
        if (encryption.getServices().getFile() == null) {
            encryption.getServices().setFile(new EncryptionService());
        }
        encryption.getServices().getFile().setEnabled(false);
        if (encryption.getKeySource() == null) {
            encryption.setKeySource(KeySource.MICROSOFT_STORAGE);
        }
        return this;
    }

    /**
     * Gets the encryption configuration.
     *
     * @param createIfNotExists flag indicating whether to create a encryption config if it does not exists already
     * @return the encryption configuration
     */
    private Encryption getEncryptionConfig(boolean createIfNotExists) {
        if (isInCreateMode) {
            if (this.createParameters.getEncryption() == null) {
                if (createIfNotExists) {
                    this.createParameters.setEncryption(new Encryption());
                } else {
                    return null;
                }
            }
            return this.createParameters.getEncryption();
        } else {
            if (this.updateParameters.getEncryption() == null) {
                if (this.inner.getEncryption() == null) {
                    if (createIfNotExists) {
                        this.updateParameters.setEncryption(new Encryption());
                    } else {
                        return null;
                    }
                } else {
                    // Create clone of current encrption
                    //
                    Encryption clonedEncryption = new Encryption();
                    clonedEncryption.setKeySource(this.inner.getEncryption().getKeySource());
                    if (this.inner.getEncryption().getKeyVaultProperties() != null) {
                        clonedEncryption.setKeyVaultProperties(new KeyVaultProperties());
                        clonedEncryption.getKeyVaultProperties()
                                .setKeyName(this.inner
                                        .getEncryption()
                                        .getKeyVaultProperties()
                                        .getKeyName())
                                .setKeyVaultUri(this.inner
                                        .getEncryption()
                                        .getKeyVaultProperties()
                                        .getKeyVaultUri())
                                .setKeyVersion(this.inner
                                        .getEncryption()
                                        .getKeyVaultProperties()
                                        .getKeyVersion());
                    }
                    if (this.inner.getEncryption().getServices() != null) {
                        clonedEncryption.setServices(new EncryptionServices());
                        if (this.inner.getEncryption().getServices().getBlob() != null) {
                            clonedEncryption.getServices().setBlob(new EncryptionService());
                            clonedEncryption.getServices().getBlob()
                                    .setEnabled(this.inner.getEncryption().getServices().getBlob().isEnabled());
                        }
                        if (this.inner.getEncryption().getServices().getFile() != null) {
                            clonedEncryption.getServices().setFile(new EncryptionService());
                            clonedEncryption.getServices().getFile()
                                    .setEnabled(this.inner.getEncryption().getServices().getFile().isEnabled());
                        }
                    }
                    this.updateParameters.setEncryption(clonedEncryption);
                }
            }
            return this.updateParameters.getEncryption();
        }
    }
}