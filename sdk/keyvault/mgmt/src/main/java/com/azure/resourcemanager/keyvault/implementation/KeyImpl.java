// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.implementation;

import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.ImportKeyOptions;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Implementation for Vault and its parent interfaces. */
class KeyImpl extends CreatableUpdatableImpl<Key, KeyVaultKey, KeyImpl>
    implements Key, Key.Definition, Key.UpdateWithCreate, Key.UpdateWithImport {

    private final Vault vault;

    private CreateKeyOptions createKeyRequest;
    private UpdateKeyOptions updateKeyRequest;
    private ImportKeyOptions importKeyRequest = null;

    private CryptographyAsyncClient cryptographyClient;

    private CryptographyAsyncClient cryptographyClient() {
        return cryptographyClient;
    }

    private static class UpdateKeyOptions {
        private KeyProperties keyProperties = new KeyProperties();
        private List<KeyOperation> keyOperations = new ArrayList<>();
    }

    KeyImpl(String name, KeyVaultKey innerObject, Vault vault) {
        super(name, innerObject);
        this.vault = vault;
    }

    private void init(boolean createNewCryptographyClient) {
        this.createKeyRequest = null;
        this.updateKeyRequest = new UpdateKeyOptions();
        if (inner() != null) {
            updateKeyRequest.keyProperties = inner().getProperties();
            if (createNewCryptographyClient) {
                cryptographyClient =
                    new CryptographyClientBuilder()
                        .keyIdentifier(inner().getKey().getId())
                        .pipeline(vault.vaultHttpPipeline())
                        .buildAsyncClient();
            }
        }
    }

    private KeyImpl wrapModel(KeyVaultKey key) {
        return new KeyImpl(key.getName(), key, vault);
    }

    @Override
    public String id() {
        return this.inner() == null ? null : this.inner().getId();
    }

    @Override
    public JsonWebKey getJsonWebKey() {
        return inner().getKey();
    }

    @Override
    public KeyProperties getAttributes() {
        return inner().getProperties();
    }

    @Override
    public Map<String, String> getTags() {
        return inner().getProperties().getTags();
    }

    @Override
    public boolean isManaged() {
        return Utils.toPrimitiveBoolean(inner().getProperties().isManaged());
    }

    @Override
    public Iterable<Key> listVersions() {
        return listVersionsAsync().toIterable();
    }

    @Override
    public Flux<Key> listVersionsAsync() {
        return vault
            .keyClient()
            .listPropertiesOfKeyVersions(this.name())
            .flatMap(p -> vault.keyClient().getKey(p.getName(), p.getVersion()))
            .map(this::wrapModel);
    }

    @Override
    public byte[] backup() {
        return backupAsync().block();
    }

    @Override
    public Mono<byte[]> backupAsync() {
        return vault.keyClient().backupKey(this.name());
    }

    @Override
    public byte[] encrypt(EncryptionAlgorithm algorithm, byte[] content) {
        return encryptAsync(algorithm, content).block();
    }

    @Override
    public Mono<byte[]> encryptAsync(final EncryptionAlgorithm algorithm, final byte[] content) {
        return cryptographyClient().encrypt(algorithm, content).map(EncryptResult::getCipherText);
    }

    @Override
    public byte[] decrypt(EncryptionAlgorithm algorithm, byte[] content) {
        return decryptAsync(algorithm, content).block();
    }

    @Override
    public Mono<byte[]> decryptAsync(final EncryptionAlgorithm algorithm, final byte[] content) {
        return cryptographyClient().decrypt(algorithm, content).map(DecryptResult::getPlainText);
    }

    @Override
    public byte[] sign(SignatureAlgorithm algorithm, byte[] digest) {
        return signAsync(algorithm, digest).block();
    }

    @Override
    public Mono<byte[]> signAsync(final SignatureAlgorithm algorithm, final byte[] digest) {
        return cryptographyClient().sign(algorithm, digest).map(SignResult::getSignature);
    }

    @Override
    public boolean verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature) {
        return Utils.toPrimitiveBoolean(verifyAsync(algorithm, digest, signature).block());
    }

    @Override
    public Mono<Boolean> verifyAsync(final SignatureAlgorithm algorithm, final byte[] digest, final byte[] signature) {
        return cryptographyClient().verify(algorithm, digest, signature).map(VerifyResult::isValid);
    }

    @Override
    public byte[] wrapKey(KeyWrapAlgorithm algorithm, byte[] key) {
        return wrapKeyAsync(algorithm, key).block();
    }

    @Override
    public Mono<byte[]> wrapKeyAsync(final KeyWrapAlgorithm algorithm, final byte[] key) {
        return cryptographyClient().wrapKey(algorithm, key).map(WrapResult::getEncryptedKey);
    }

    @Override
    public byte[] unwrapKey(KeyWrapAlgorithm algorithm, byte[] key) {
        return unwrapKeyAsync(algorithm, key).block();
    }

    @Override
    public Mono<byte[]> unwrapKeyAsync(final KeyWrapAlgorithm algorithm, final byte[] key) {
        return cryptographyClient().unwrapKey(algorithm, key).map(UnwrapResult::getKey);
    }

    @Override
    protected Mono<KeyVaultKey> getInnerAsync() {
        return vault.keyClient().getKey(this.name());
    }

    @Override
    public KeyImpl withTags(Map<String, String> tags) {
        if (isInCreateMode()) {
            if (createKeyRequest != null) {
                createKeyRequest.setTags(tags);
            } else if (importKeyRequest != null) {
                importKeyRequest.setTags(tags);
            }
        } else {
            updateKeyRequest.keyProperties.setTags(tags);
        }
        return this;
    }

    @Override
    public boolean isInCreateMode() {
        return id() == null;
    }

    @Override
    public Mono<Key> createResourceAsync() {
        Mono<KeyVaultKey> mono;
        if (createKeyRequest != null) {
            if (createKeyRequest instanceof CreateEcKeyOptions) {
                mono = vault.keyClient().createEcKey((CreateEcKeyOptions) createKeyRequest);
            } else if (createKeyRequest instanceof CreateRsaKeyOptions) {
                mono = vault.keyClient().createRsaKey((CreateRsaKeyOptions) createKeyRequest);
            } else {
                mono = vault.keyClient().createKey(createKeyRequest);
            }
        } else {
            mono = vault.keyClient().importKey(importKeyRequest);
        }
        return mono
            .map(
                inner -> {
                    this.setInner(inner);
                    init(true);
                    return this;
                });
    }

    @Override
    public Mono<Key> updateResourceAsync() {
        UpdateKeyOptions optionsToUpdate = updateKeyRequest;
        Mono<Key> mono = Mono.just(this);
        if (createKeyRequest != null || importKeyRequest != null) {
            mono =
                createResourceAsync()
                    .then(
                        Mono
                            .fromCallable(
                                () -> {
                                    // merge optionsToUpdate into refreshed updateKeyRequest
                                    updateKeyRequest
                                        .keyProperties
                                        .setEnabled(optionsToUpdate.keyProperties.isEnabled());
                                    updateKeyRequest
                                        .keyProperties
                                        .setExpiresOn(optionsToUpdate.keyProperties.getExpiresOn());
                                    updateKeyRequest
                                        .keyProperties
                                        .setNotBefore(optionsToUpdate.keyProperties.getNotBefore());
                                    updateKeyRequest.keyProperties.setTags(optionsToUpdate.keyProperties.getTags());
                                    updateKeyRequest.keyOperations = optionsToUpdate.keyOperations;
                                    return this;
                                }));
        }
        return mono
            .then(
                vault
                    .keyClient()
                    .updateKeyProperties(
                        updateKeyRequest.keyProperties, updateKeyRequest.keyOperations.toArray(new KeyOperation[0]))
                    .map(
                        inner -> {
                            this.setInner(inner);
                            init(false);
                            return this;
                        }));
    }

    @Override
    public KeyImpl withAttributes(KeyProperties attributes) {
        if (isInCreateMode()) {
            if (createKeyRequest != null) {
                createKeyRequest.setEnabled(attributes.isEnabled());
                createKeyRequest.setExpiresOn(attributes.getExpiresOn());
                createKeyRequest.setNotBefore(attributes.getNotBefore());
                createKeyRequest.setTags(attributes.getTags());
            } else if (importKeyRequest != null) {
                importKeyRequest.setEnabled(attributes.isEnabled());
                importKeyRequest.setExpiresOn(attributes.getExpiresOn());
                importKeyRequest.setNotBefore(attributes.getNotBefore());
                importKeyRequest.setTags(attributes.getTags());
            }
        } else {
            updateKeyRequest.keyProperties.setEnabled(attributes.isEnabled());
            updateKeyRequest.keyProperties.setExpiresOn(attributes.getExpiresOn());
            updateKeyRequest.keyProperties.setNotBefore(attributes.getNotBefore());
            updateKeyRequest.keyProperties.setTags(attributes.getTags());
        }
        return this;
    }

    @Override
    public KeyImpl withKeyTypeToCreate(KeyType keyType) {
        if (keyType == KeyType.EC || keyType == KeyType.EC_HSM) {
            CreateEcKeyOptions request = new CreateEcKeyOptions(name());
            request.setHardwareProtected(keyType == KeyType.EC_HSM);

            createKeyRequest = request;
        } else if (keyType == KeyType.RSA || keyType == KeyType.RSA_HSM) {
            CreateRsaKeyOptions request = new CreateRsaKeyOptions(name());
            request.setHardwareProtected(keyType == KeyType.RSA_HSM);

            createKeyRequest = request;
        } else {
            createKeyRequest = new CreateKeyOptions(name(), keyType);
        }
        return this;
    }

    @Override
    public KeyImpl withLocalKeyToImport(JsonWebKey key) {
        importKeyRequest = new ImportKeyOptions(name(), key);
        return this;
    }

    @Override
    public KeyImpl withKeyOperations(List<KeyOperation> keyOperations) {
        if (isInCreateMode()) {
            createKeyRequest.setKeyOperations(keyOperations.toArray(new KeyOperation[0]));
        } else {
            updateKeyRequest.keyOperations = keyOperations;
        }
        return this;
    }

    @Override
    public KeyImpl withKeyOperations(KeyOperation... keyOperations) {
        return withKeyOperations(Arrays.asList(keyOperations));
    }

    @Override
    public KeyImpl withHsm(boolean isHsm) {
        importKeyRequest.setHardwareProtected(isHsm);
        return this;
    }

    @Override
    public KeyImpl withKeySize(int size) {
        if (createKeyRequest instanceof CreateRsaKeyOptions) {
            ((CreateRsaKeyOptions) createKeyRequest).setKeySize(size);
        }
        return this;
    }

    @Override
    public DefinitionStages.WithCreate withKeyCurveName(KeyCurveName keyCurveName) {
        if (createKeyRequest instanceof CreateEcKeyOptions) {
            ((CreateEcKeyOptions) createKeyRequest).setCurveName(keyCurveName);
        }
        return this;
    }
}
