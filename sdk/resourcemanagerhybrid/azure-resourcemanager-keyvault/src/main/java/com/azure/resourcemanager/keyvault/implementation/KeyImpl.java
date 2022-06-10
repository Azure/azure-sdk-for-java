// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
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
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** Implementation for Vault and its parent interfaces. */
class KeyImpl extends CreatableUpdatableImpl<Key, KeyProperties, KeyImpl>
    implements Key, Key.Definition, Key.UpdateWithCreate, Key.UpdateWithImport {

    private final ClientLogger logger = new ClientLogger(this.getClass());

    private final Vault vault;

    private CreateKeyOptions createKeyRequest;
    private UpdateKeyOptions updateKeyRequest;
    private ImportKeyOptions importKeyRequest = null;

    private CryptographyAsyncClient cryptographyClient;

    private JsonWebKey jsonWebKey;

    private CryptographyAsyncClient cryptographyClient() {
        return cryptographyClient;
    }

    private static class UpdateKeyOptions {
        private KeyProperties keyProperties = new KeyProperties();
        private List<KeyOperation> keyOperations = new ArrayList<>();
    }

    KeyImpl(String name, KeyProperties innerObject, Vault vault) {
        super(name, innerObject);
        this.vault = vault;
    }

    KeyImpl(String name, KeyVaultKey keyVaultKey, Vault vault) {
        super(name, keyVaultKey.getProperties());
        this.jsonWebKey = keyVaultKey.getKey();
        this.vault = vault;
    }

    private void init(boolean createNewCryptographyClient) {
        this.createKeyRequest = null;
        this.updateKeyRequest = new UpdateKeyOptions();
        if (innerModel() != null) {
            updateKeyRequest.keyProperties = innerModel();
            if (createNewCryptographyClient) {
                cryptographyClient =
                    new CryptographyClientBuilder()
                        .keyIdentifier(innerModel().getId())
                        .pipeline(vault.vaultHttpPipeline())
                        .buildAsyncClient();
            }
        }
    }

    private KeyImpl wrapModel(KeyProperties keyProperties) {
        return new KeyImpl(keyProperties.getName(), keyProperties, vault);
    }

    @Override
    public String id() {
        return this.innerModel().getId();
    }

    @Override
    public JsonWebKey getJsonWebKey() {
        return this.getJsonWebKeyAsync().block();
    }

    @Override
    public Mono<JsonWebKey> getJsonWebKeyAsync() {
        if (jsonWebKey != null) {
            return Mono.just(jsonWebKey);
        } else {
            return this.getInnerAsync().map(ignored -> jsonWebKey);
        }
    }

    @Override
    public KeyProperties attributes() {
        return innerModel();
    }

    @Override
    public Map<String, String> tags() {
        return innerModel().getTags();
    }

    @Override
    public boolean managed() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().isManaged());
    }

    @Override
    public PagedIterable<Key> listVersions() {
        return new PagedIterable<>(listVersionsAsync());
    }

    @Override
    public PagedFlux<Key> listVersionsAsync() {
        return PagedConverter.mapPage(vault
            .keyClient()
            .listPropertiesOfKeyVersions(this.name()),
            this::wrapModel);
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
        return ResourceManagerUtils.toPrimitiveBoolean(verifyAsync(algorithm, digest, signature).block());
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
    protected Mono<KeyProperties> getInnerAsync() {
        return vault.keyClient().getKey(this.name()).map(keyVaultKey -> {
            this.jsonWebKey = keyVaultKey.getKey();
            return keyVaultKey.getProperties();
        });
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
                keyVaultKey -> {
                    this.setInner(keyVaultKey.getProperties());
                    this.jsonWebKey = keyVaultKey.getKey();
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
                        keyVaultKey -> {
                            this.setInner(keyVaultKey.getProperties());
                            this.jsonWebKey = keyVaultKey.getKey();
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
        if (importKeyRequest == null) {
            importKeyRequest = new ImportKeyOptions(name(), key);
        } else {
            throw logger.logExceptionAsError(new IllegalStateException("Not in import flow"));
        }
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
        if (importKeyRequest != null) {
            importKeyRequest.setHardwareProtected(isHsm);
        } else {
            throw logger.logExceptionAsError(new IllegalStateException("Not in import flow"));
        }
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
