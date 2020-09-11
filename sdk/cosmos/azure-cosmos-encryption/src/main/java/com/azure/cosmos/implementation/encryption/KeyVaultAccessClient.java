// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.encryption.KeyVaultTokenCredentialFactory;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Implements Core KeyVault access methods that uses the {@link KeyVaultAccessClient}
 */
public class KeyVaultAccessClient {
    private final AsyncCache<URI, KeyAsyncClient> akvClientCache;
    private final AsyncCache<URI, CryptographyAsyncClient> akvCryptoClientCache;
    private final KeyVaultTokenCredentialFactory keyVaultTokenCredentialFactory;
    private final KeyClientFactory keyClientFactory;
    private final CryptographyClientFactory cryptographyClientFactory;
    private final Logger logger = LoggerFactory.getLogger(KeyVaultAccessClient.class);

    /**
     * Initializes a new instance of the {@link KeyVaultAccessClient}
     *
     * @param keyVaultTokenCredentialFactory TokenCredentials factory
     */
    public KeyVaultAccessClient(KeyVaultTokenCredentialFactory keyVaultTokenCredentialFactory) {
        this.keyVaultTokenCredentialFactory = keyVaultTokenCredentialFactory;
        this.akvClientCache = new AsyncCache<>();
        this.akvCryptoClientCache = new AsyncCache<>();
        this.keyClientFactory = new KeyClientFactory();
        this.cryptographyClientFactory = new CryptographyClientFactory();
    }

    /**
     * Initializes a new instance of the {@link KeyVaultAccessClient}
     * class Invokes internal factory Methods.
     *
     * @param keyVaultTokenCredentialFactory TokenCredential
     * @param keyClientFactory KeyClient Factory
     * @param cryptographyClientFactory KeyClient Factory
     */
    KeyVaultAccessClient(KeyVaultTokenCredentialFactory keyVaultTokenCredentialFactory,
                                KeyClientFactory keyClientFactory,
                                CryptographyClientFactory cryptographyClientFactory) {
        this.keyVaultTokenCredentialFactory = keyVaultTokenCredentialFactory;
        this.akvClientCache = new AsyncCache<>();
        this.akvCryptoClientCache = new AsyncCache<>();
        this.keyClientFactory = keyClientFactory;
        this.cryptographyClientFactory = cryptographyClientFactory;
    }

    /**
     * Unwrap the encrypted Key. Only supports encrypted bytes in base64 format.
     *
     * Note: this may return an empty Mono if cryptoClient returns empty/null
     *
     * @param wrappedKey encrypted bytes.
     * @param keyVaultUriProperties Parsed key Vault Uri Properties.Properties as in sample Format:
     * https://{keyvault-name}.vault.azure.net/keys/{key-name}/{key-version}
     * @return Mono of Result including KeyIdentifier and decrypted bytes in base64 string format, can be convert to
     * bytes using Convert.FromBase64String().
     */
    public Mono<byte[]> unwrapKey(
        byte[] wrappedKey,
        KeyVaultKeyUriProperties keyVaultUriProperties) {

        // Get a Crypto Client for Wrap and UnWrap,this gets init per Key ID
        Mono<CryptographyAsyncClient> cryptoClientMono = this.getCryptoClient(keyVaultUriProperties);
        return cryptoClientMono.flatMap(
            cryptoClient ->
                cryptoClient.unwrapKey(KeyVaultConstants.RsaOaep256, wrappedKey)
                            .flatMap(keyOpResult ->
                                Mono.justOrEmpty(keyOpResult.getKey())
                            )
                            .onErrorResume(e -> {
                                logger.error("unwrapKeyAsync:Failed to Unwrap the encrypted key. {}", e.getMessage());
                                return Mono.error(
                                    cryptoClientExceptionToCosmosException("UnwrapKeyAsync"
                                        + ":Failed to Unwrap the encrypted key.", e));
                            })
        );
    }

    private CosmosException cryptoClientExceptionToCosmosException(String message, Throwable ex) {
        // TODO: discuss with dotnet folks,
        return new KeyVaultCosmosException(-1, message, ex);
    }

    /**
     * Wrap the Key with latest Key version. Only supports bytes in base64 format.
     *
     * @param key plain text key.
     * @param keyVaultUriProperties Parsed key Vault Uri Properties.Properties as in sample Format:
     * https://{keyvault-name}.vault.azure.net/keys/{key-name}/{key-version}
     * @return Mono of Result including KeyIdentifier and encrypted bytes in base64 string format.
     */
    public Mono<byte[]> wrapKey(
        byte[] key,
        KeyVaultKeyUriProperties keyVaultUriProperties) {

        // Get a Crypto Client for Wrap and UnWrap,this gets init per Key ID
        Mono<CryptographyAsyncClient> cryptoClientMono = this.getCryptoClient(keyVaultUriProperties);
        return cryptoClientMono.flatMap(
            cryptoClient ->
                cryptoClient.wrapKey(KeyVaultConstants.RsaOaep256, key)
                            .flatMap(keyOpResult ->
                                Mono.justOrEmpty(keyOpResult.getEncryptedKey())
                            )
                            .onErrorResume(e -> {
                                logger.error("unwrapKeyAsync:Failed to Unwrap the encrypted key. {}", e.getMessage());
                                return Mono.error(cryptoClientExceptionToCosmosException("UnwrapKeyAsync"
                                    + ":Failed to Unwrap the encrypted key.", e));
                            })
        );
    }

    /**
     * Validate the Purge Protection AndSoft Delete Settings.
     *
     * @param keyVaultUriProperties Parsed key Vault Uri Properties.
     * @return Whether The Customer has the correct Deletion Level.
     */
    public Mono<Boolean> validatePurgeProtectionAndSoftDeleteSettings(
        KeyVaultKeyUriProperties keyVaultUriProperties) {
        Mono<KeyAsyncClient> akvClientMono = this.getAkvClient(keyVaultUriProperties);
        return akvClientMono.flatMap(
            akvClient ->
                akvClient.getKey(keyVaultUriProperties.getKeyName())
                         .flatMap(
                             getKeyResponse -> {
                                 String keyDeletionRecoveryLevel = null;
                                 if (getKeyResponse != null && getKeyResponse.getProperties() != null) {
                                     keyDeletionRecoveryLevel = getKeyResponse.getProperties().getRecoveryLevel();
                                 }

                                 return Mono.just(keyDeletionRecoveryLevel != null && (
                                     keyDeletionRecoveryLevel.contains(KeyVaultConstants.DeletionRecoveryLevel.RECOVERABLE)
                                         || keyDeletionRecoveryLevel.contains(KeyVaultConstants.DeletionRecoveryLevel.RECOVERABLE_PROTECTED_SUBSCRIPTION)
                                         || keyDeletionRecoveryLevel.contains(KeyVaultConstants.DeletionRecoveryLevel.CUSTOMIZED_RECOVERABLE)
                                         || keyDeletionRecoveryLevel.contains(KeyVaultConstants.DeletionRecoveryLevel.CUSTOMIZED_RECOVERABLE_PROTECTED_SUBSCRIPTION)));
                             }
                         )
        ).onErrorResume(
            e -> Mono.error(cryptoClientExceptionToCosmosException("ValidatePurgeProtectionAndSoftDeleteSettingsAsync: Failed to fetch Key from Key Vault.", e))
        );
    }

    /**
     * Obtains the KeyClient to retrieve keys from Keyvault.
     * returned Mono will never be an empty Mono.
     * @param keyVaultUriProperties Parsed key Vault Uri Properties.
     * @return Mono of KeyClient
     */
    private Mono<KeyAsyncClient> getAkvClient(
        KeyVaultKeyUriProperties keyVaultUriProperties) {

        // Called once per KEYVALTNAME
        // Eg:https://KEYVALTNAME.vault.azure.net/
        Mono<KeyAsyncClient> akvClientMono = this.akvClientCache.getAsync(
            /** key: */keyVaultUriProperties.getKeyUri(),
            /** obsoleteValue: */null,
            /** singleValueInitFunc: */
            () -> {
                Mono<TokenCredential> tokenCredMono =
                    this.keyVaultTokenCredentialFactory.getTokenCredential(keyVaultUriProperties.getKeyUri());
                return tokenCredMono.map(
                    tokenCred ->
                        this.keyClientFactory.getKeyClient(keyVaultUriProperties, tokenCred)
                );
            });

        return akvClientMono;
    }

    //
    //    /// <summary>
    //    /// Obtains the Crypto Client for Wrap/UnWrap.
    //    /// </summary>
    //    /// <param name="keyVaultUriProperties"> Parsed key Vault Uri Properties. </param>
    //    /// <param name="cancellationToken"> cancellation token </param>
    //    /// <returns> CryptographyClient </returns>
    private Mono<CryptographyAsyncClient> getCryptoClient(
        KeyVaultKeyUriProperties keyVaultUriProperties) {

        // Get a Crypto Client for Wrap and UnWrap,this gets init per Key Version
        // Cache it against the KeyVersion/KeyId
        // Eg: :https://KEYVAULTNAME.vault.azure.net/keys/keyname/KEYID
        Mono<CryptographyAsyncClient> cryptoClientMono = this.akvCryptoClientCache.getAsync(
            keyVaultUriProperties.getKeyUri(),
            /** obsoleteValue: */null,
            /** singleValueInitFunc: */
            () -> {
                // we need to acquire the Client Cert Creds for cases where we directly access Crypto Services.
                Mono<TokenCredential> tokenCredMono =
                    this.keyVaultTokenCredentialFactory.getTokenCredential(keyVaultUriProperties.getKeyUri());
                return tokenCredMono.map(
                    tokenCred ->
                         this.cryptographyClientFactory.getCryptographyClient(keyVaultUriProperties, tokenCred)
                );
            });
        return cryptoClientMono;
    }
}
