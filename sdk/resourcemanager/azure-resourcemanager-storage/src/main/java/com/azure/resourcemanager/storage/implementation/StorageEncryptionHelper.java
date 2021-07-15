// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.models.Encryption;
import com.azure.resourcemanager.storage.models.EncryptionService;
import com.azure.resourcemanager.storage.models.EncryptionServices;
import com.azure.resourcemanager.storage.models.KeySource;
import com.azure.resourcemanager.storage.models.KeyType;
import com.azure.resourcemanager.storage.models.KeyVaultProperties;
import com.azure.resourcemanager.storage.models.StorageAccountCreateParameters;
import com.azure.resourcemanager.storage.models.StorageAccountEncryptionKeySource;
import com.azure.resourcemanager.storage.models.StorageAccountEncryptionStatus;
import com.azure.resourcemanager.storage.models.StorageAccountUpdateParameters;
import com.azure.resourcemanager.storage.models.StorageService;
import com.azure.resourcemanager.storage.fluent.models.StorageAccountInner;
import java.util.HashMap;
import java.util.Map;

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
     * @param inner the current state of storage account
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
        if (inner.encryption() == null || inner.encryption().keySource() == null) {
            return null;
        }
        return StorageAccountEncryptionKeySource.fromString(inner.encryption().keySource().toString());
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
        if (inner.encryption() != null) {
            services = inner.encryption().services();
        }
        statuses.put(StorageService.BLOB, new BlobServiceEncryptionStatusImpl(services));
        statuses.put(StorageService.FILE, new FileServiceEncryptionStatusImpl(services));
        statuses.put(StorageService.TABLE, new TableServiceEncryptionStatusImpl(services));
        statuses.put(StorageService.QUEUE, new QueueServiceEncryptionStatusImpl(services));
        return statuses;
    }

    boolean infrastructureEncryptionEnabled() {
        return inner != null && inner.encryption() != null
            && ResourceManagerUtils.toPrimitiveBoolean(inner.encryption().requireInfrastructureEncryption());
    }

    /**
     * Specifies that storage blob encryption should be enabled.
     *
     * @return StorageEncryptionHelper
     */
    StorageEncryptionHelper withBlobEncryption() {
        Encryption encryption = getEncryptionConfig(true);
        if (encryption.services() == null) {
            encryption.withServices(new EncryptionServices());
        }
        // Enable encryption for blob service
        //
        if (encryption.services().blob() == null) {
            encryption.services().withBlob(new EncryptionService());
        }
        encryption.services().blob().withEnabled(true);
        if (encryption.keySource() == null) {
            encryption.withKeySource(KeySource.MICROSOFT_STORAGE);
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
        if (encryption.services() == null) {
            encryption.withServices(new EncryptionServices());
        }
        // Enable encryption for file service
        //
        if (encryption.services().file() == null) {
            encryption.services().withFile(new EncryptionService());
        }
        encryption.services().file().withEnabled(true);
        if (encryption.keySource() == null) {
            encryption.withKeySource(KeySource.MICROSOFT_STORAGE);
        }
        return this;
    }

    StorageEncryptionHelper withTableEncryption() {
        Encryption encryption = getEncryptionConfig(true);
        if (encryption.services() == null) {
            encryption.withServices(new EncryptionServices());
        }
        if (encryption.services().table() == null) {
            encryption.services().withTable(new EncryptionService());
        }
        encryption.services().table().withEnabled(true);
        encryption.services().table().withKeyType(KeyType.ACCOUNT);
        if (encryption.keySource() == null) {
            encryption.withKeySource(KeySource.MICROSOFT_STORAGE);
        }
        return this;
    }

    StorageEncryptionHelper withQueueEncryption() {
        Encryption encryption = getEncryptionConfig(true);
        if (encryption.services() == null) {
            encryption.withServices(new EncryptionServices());
        }
        if (encryption.services().queue() == null) {
            encryption.services().withQueue(new EncryptionService());
        }
        encryption.services().queue().withEnabled(true);
        encryption.services().queue().withKeyType(KeyType.ACCOUNT);
        if (encryption.keySource() == null) {
            encryption.withKeySource(KeySource.MICROSOFT_STORAGE);
        }
        return this;
    }

    StorageEncryptionHelper withInfrastructureEncryption() {
        Encryption encryption = getEncryptionConfig(true);
        encryption.withRequireInfrastructureEncryption(true);
        return this;
    }

    /**
     * Specifies the key vault key to be used to encrypt the blobs and files.
     *
     * @return StorageEncryptionHelper
     */
    StorageEncryptionHelper withEncryptionKeyFromKeyVault(String keyVaultUri, String keyName, String keyVersion) {
        Encryption encryption = getEncryptionConfig(true);
        encryption.withKeySource(KeySource.MICROSOFT_KEYVAULT);
        encryption
            .withKeyVaultProperties(
                new KeyVaultProperties().withKeyVaultUri(keyVaultUri).withKeyName(keyName).withKeyVersion(keyVersion));
        return this;
    }

    /**
     * Specifies that blob encryption should be disabled for storage blob.
     *
     * @return StorageEncryptionHelper
     */
    StorageEncryptionHelper withoutBlobEncryption() {
        Encryption encryption = getEncryptionConfig(true);
        if (encryption.services() == null) {
            encryption.withServices(new EncryptionServices());
        }
        // Disable encryption for blob service
        //
        if (encryption.services().blob() == null) {
            encryption.services().withBlob(new EncryptionService());
        }
        encryption.services().blob().withEnabled(false);
        if (encryption.keySource() == null) {
            encryption.withKeySource(KeySource.MICROSOFT_STORAGE);
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
        if (encryption.services() == null) {
            encryption.withServices(new EncryptionServices());
        }
        // Disable encryption for blob service
        //
        if (encryption.services().file() == null) {
            encryption.services().withFile(new EncryptionService());
        }
        encryption.services().file().withEnabled(false);
        if (encryption.keySource() == null) {
            encryption.withKeySource(KeySource.MICROSOFT_STORAGE);
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
            if (this.createParameters.encryption() == null) {
                if (createIfNotExists) {
                    this.createParameters.withEncryption(new Encryption());
                } else {
                    return null;
                }
            }
            return this.createParameters.encryption();
        } else {
            if (this.updateParameters.encryption() == null) {
                if (this.inner.encryption() == null) {
                    if (createIfNotExists) {
                        this.updateParameters.withEncryption(new Encryption());
                    } else {
                        return null;
                    }
                } else {
                    // Create clone of current encrption
                    //
                    Encryption clonedEncryption = new Encryption();
                    clonedEncryption.withKeySource(this.inner.encryption().keySource());
                    if (this.inner.encryption().requireInfrastructureEncryption() != null) {
                        clonedEncryption.withRequireInfrastructureEncryption(
                            this.inner.encryption().requireInfrastructureEncryption());
                    }
                    if (this.inner.encryption().keyVaultProperties() != null) {
                        clonedEncryption.withKeyVaultProperties(new KeyVaultProperties());
                        clonedEncryption
                            .keyVaultProperties()
                            .withKeyName(this.inner.encryption().keyVaultProperties().keyName())
                            .withKeyVaultUri(this.inner.encryption().keyVaultProperties().keyVaultUri())
                            .withKeyVersion(this.inner.encryption().keyVaultProperties().keyVersion());
                    }
                    if (this.inner.encryption().services() != null) {
                        clonedEncryption.withServices(new EncryptionServices());
                        if (this.inner.encryption().services().blob() != null) {
                            clonedEncryption.services().withBlob(new EncryptionService());
                            clonedEncryption
                                .services()
                                .blob()
                                .withEnabled(this.inner.encryption().services().blob().enabled())
                                .withKeyType(this.inner.encryption().services().blob().keyType());
                        }
                        if (this.inner.encryption().services().file() != null) {
                            clonedEncryption.services().withFile(new EncryptionService());
                            clonedEncryption
                                .services()
                                .file()
                                .withEnabled(this.inner.encryption().services().file().enabled())
                                .withKeyType(this.inner.encryption().services().file().keyType());
                        }
                        if (this.inner.encryption().services().table() != null) {
                            clonedEncryption.services().withTable(new EncryptionService());
                            clonedEncryption
                                .services()
                                .table()
                                .withEnabled(this.inner.encryption().services().table().enabled())
                                .withKeyType(this.inner.encryption().services().table().keyType());
                        }
                        if (this.inner.encryption().services().queue() != null) {
                            clonedEncryption.services().withQueue(new EncryptionService());
                            clonedEncryption
                                .services()
                                .queue()
                                .withEnabled(this.inner.encryption().services().queue().enabled())
                                .withKeyType(this.inner.encryption().services().queue().keyType());
                        }
                    }
                    this.updateParameters.withEncryption(clonedEncryption);
                }
            }
            return this.updateParameters.encryption();
        }
    }
}
