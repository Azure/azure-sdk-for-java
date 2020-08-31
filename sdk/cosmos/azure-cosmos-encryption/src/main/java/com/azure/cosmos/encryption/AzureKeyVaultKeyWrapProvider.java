// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.encryption.CryptographyClientFactory;
import com.azure.cosmos.implementation.encryption.EncryptionUtils;
import com.azure.cosmos.implementation.encryption.KeyClientFactory;
import com.azure.cosmos.implementation.encryption.KeyVaultAccessClient;
import com.azure.cosmos.implementation.encryption.KeyVaultConstants;
import com.azure.cosmos.implementation.encryption.KeyVaultKeyUriProperties;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TODO: moderakh now this should be internal
 * Provides functionality to wrap (encrypt) and unwrap (decrypt) data encryption keys using master keys stored in Azure Key Vault.
 * Unwrapped data encryption keys will be cached within the client SDK for a period of 1 hour.
 */
public class AzureKeyVaultKeyWrapProvider implements EncryptionKeyWrapProvider {
    private final KeyVaultAccessClient keyVaultAccessClient;
    private final Duration rawDekCacheTimeToLive;

    /**
     * Creates a new instance of a provider to wrap (encrypt) and unwrap (decrypt) data encryption keys using master
     * keys stored in Azure Key Vault.
     *
     * @param keyVaultTokenCredentialFactory KeyVaultTokenCredentialFactory instance
     * <p>
     * Amount of time the unencrypted form of the data encryption key can be cached on the client before UnwrapKeyAsync
     * needs to be called again.
     */
    public AzureKeyVaultKeyWrapProvider(KeyVaultTokenCredentialFactory keyVaultTokenCredentialFactory) {
        this.keyVaultAccessClient = new KeyVaultAccessClient(keyVaultTokenCredentialFactory);
        this.rawDekCacheTimeToLive = Duration.ofHours(1);
    }

    /**
     * Creates a new instance of a provider to wrap (encrypt) and unwrap (decrypt) data encryption keys using master
     * keys stored in Azure Key Vault.
     *
     * @param keyVaultTokenCredentialFactory KeyVaultTokenCredentialFactory instance
     * @param keyClientFactory KeyClient Factory Methods
     * @param cryptographyClientFactory CryptographyClient Factory Methods.
     */
    public AzureKeyVaultKeyWrapProvider(KeyVaultTokenCredentialFactory keyVaultTokenCredentialFactory,
                                        KeyClientFactory keyClientFactory,
                                        CryptographyClientFactory cryptographyClientFactory) {
        this.keyVaultAccessClient = new KeyVaultAccessClient(keyVaultTokenCredentialFactory, keyClientFactory,
            cryptographyClientFactory);
        this.rawDekCacheTimeToLive = Duration.ofHours(1);
    }

    @Override
    public Mono<EncryptionKeyUnwrapResult> unwrapKey(byte[] wrappedKey,
                                                     EncryptionKeyWrapMetadata metadata) {
        if (!StringUtils.equals(metadata.type, AzureKeyVaultKeyWrapMetadata.TYPE_CONSTANT)) {
            throw new IllegalArgumentException("Invalid metadata metadata");
        }

        if (!StringUtils.equals(metadata.algorithm, KeyVaultConstants.RsaOaep256.toString())) {
            throw new IllegalArgumentException(
                String.format("Unknown encryption key wrap algorithm %s metadata", metadata.algorithm));
        }

        AtomicReference<KeyVaultKeyUriProperties> keyVaultUriPropertiesRef =
            new AtomicReference<KeyVaultKeyUriProperties>();

        if (!KeyVaultKeyUriProperties.tryParse(EncryptionUtils.toURI(metadata.value), keyVaultUriPropertiesRef)) {
            throw new IllegalArgumentException(String.format("KeyVault Key Uri %s is invalid.", metadata.value));
        }

        return this.keyVaultAccessClient
            .unwrapKey(wrappedKey, keyVaultUriPropertiesRef.get())
            .map(
                dataEncryptionKey -> new EncryptionKeyUnwrapResult(dataEncryptionKey, this.rawDekCacheTimeToLive)
            ).switchIfEmpty(
                Mono.error(
                    new IllegalArgumentException("keyVaultAccessClient.unwrapKey returned no bytes: dataEncryptionKey is null")
                )
            );
    }

    @Override
    public Mono<EncryptionKeyWrapResult> wrapKey(byte[] key,
                                                 EncryptionKeyWrapMetadata metadata) {
        if (!StringUtils.equals(metadata.type, AzureKeyVaultKeyWrapMetadata.TYPE_CONSTANT)) {
            throw new IllegalArgumentException("Invalid metadata metadata");
        }

        AtomicReference<KeyVaultKeyUriProperties> keyVaultUriPropertiesRef =
            new AtomicReference<>();
        if (!KeyVaultKeyUriProperties.tryParse(EncryptionUtils.toURI(metadata.value), keyVaultUriPropertiesRef)) {
            throw new IllegalArgumentException(String.format("KeyVault Key Uri %s is invalid.", metadata.value));
        }

        return this.keyVaultAccessClient
            .validatePurgeProtectionAndSoftDeleteSettings(keyVaultUriPropertiesRef.get())
            .flatMap(
                isValid -> {
                    if (!isValid) {
                        throw new IllegalArgumentException(
                            String.format("Key Vault %s provided must have soft delete and purge protection enabled."
                                , keyVaultUriPropertiesRef.get().getKeyUri()));
                    }

                    return this.keyVaultAccessClient
                        .wrapKey(key, keyVaultUriPropertiesRef.get())
                        .map(
                            wrappedDataEncryptionKey -> {  // TODO: what happens if wrappedDataEncryptionKey is null?
                                EncryptionKeyWrapMetadata responseMetadata =
                                    new EncryptionKeyWrapMetadata(metadata.type, metadata.value,
                                    KeyVaultConstants.RsaOaep256.toString());
                                return new EncryptionKeyWrapResult(wrappedDataEncryptionKey, responseMetadata);
                            }
                        ).switchIfEmpty(
                            Mono.error(
                                new IllegalArgumentException("keyVaultAccessClient.wrapKey returned no bytes: wrappedDataEncryptionKey is null")
                            ));
                }
            );
        }
}
