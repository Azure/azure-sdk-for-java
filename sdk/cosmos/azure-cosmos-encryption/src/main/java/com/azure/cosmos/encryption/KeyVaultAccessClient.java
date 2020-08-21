// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Implements Core KeyVault access methods that uses the TODO: moderakh this doesn't need to be public. it is public due
 * to tests. FIXME
 * TODO: methods should be async moderakh
 */
public class KeyVaultAccessClient {
    private final AsyncCache<URI, KeyAsyncClient> akvClientCache;
    private final AsyncCache<URI, CryptographyAsyncClient> akvCryptoClientCache;
    private final KeyVaultTokenCredentialFactory keyVaultTokenCredentialFactory;
    private final KeyClientFactory keyClientFactory;
    private final CryptographyClientFactory cryptographyClientFactory;

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
     * TODO: this doesn't need to be public moderakh Initializes a new instance of the {@link KeyVaultAccessClient}
     * class Invokes internal factory Methods.
     *
     * @param keyVaultTokenCredentialFactory TokenCredential
     * @param keyClientFactory KeyClient Factory
     * @param cryptographyClientFactory KeyClient Factory
     * @return
     */
    public KeyVaultAccessClient(KeyVaultTokenCredentialFactory keyVaultTokenCredentialFactory,
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
     * @param wrappedKey encrypted bytes.
     * @param keyVaultUriProperties Parsed key Vault Uri Properties.Properties as in sample Format:
     * https://{keyvault-name}.vault.azure.net/keys/{key-name}/{key-version}
     * @return Mono of Result including KeyIdentifier and decrypted bytes in base64 string format, can be convert to
     * bytes using Convert.FromBase64String().
     */
    public Mono<byte[]> UnwrapKeyAsync(
        byte[] wrappedKey,
        KeyVaultKeyUriProperties keyVaultUriProperties) {
        UnwrapResult keyOpResult;

        // Get a Crypto Client for Wrap and UnWrap,this gets init per Key ID
        Mono<CryptographyAsyncClient> cryptoClientMono = this.GetCryptoClientAsync(keyVaultUriProperties);

        // TODO: moderakh change to async
        CryptographyAsyncClient cryptoClient = cryptoClientMono.block();

        try {
            keyOpResult = cryptoClient.unwrapKey(KeyVaultConstants.RsaOaep256, wrappedKey).block();
        } catch (AzureException ex) {

            throw new KeyVaultAccessException();
            // TODO: set proper exception moderakh
            //            throw new KeyVaultAccessException(
            //                ex.Status,
            //                ex.ErrorCode,
            //                "UnwrapKeyAsync:Failed to Unwrap the encrypted key.",
            //                ex);
        }

        // may return null
        return Mono.justOrEmpty(keyOpResult.getKey());
    }

    /**
     * Wrap the Key with latest Key version. Only supports bytes in base64 format.
     *
     * @param key plain text key.
     * @param keyVaultUriProperties Parsed key Vault Uri Properties.Properties as in sample Format:
     * https://{keyvault-name}.vault.azure.net/keys/{key-name}/{key-version}
     * @return Mono of Result including KeyIdentifier and encrypted bytes in base64 string format.
     */
    public Mono<byte[]> WrapKeyAsync(
        byte[] key,
        KeyVaultKeyUriProperties keyVaultUriProperties) {
        WrapResult keyOpResult;

        // Get a Crypto Client for Wrap and UnWrap,this gets init per Key ID
        CryptographyAsyncClient cryptoClient = this.GetCryptoClientAsync(keyVaultUriProperties).block();

        try {
            keyOpResult = cryptoClient.wrapKey(KeyVaultConstants.RsaOaep256, key).block();
        }
        // TODO: set proper exception moderakh moderakh
        //        catch (RequestFailedException ex)
        //        {
        //            throw new KeyVaultAccessException(
        //                ex.Status,
        //                ex.ErrorCode,
        //                "WrapKeyAsync: Failed to Wrap the data encryption key.",
        //                ex);
        //        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new KeyVaultAccessException();
        }

        // key vault may return null
        return Mono.justOrEmpty(keyOpResult.getEncryptedKey());
    }

    /**
     * Validate the Purge Protection AndSoft Delete Settings.
     *
     * @param keyVaultUriProperties Parsed key Vault Uri Properties.
     * @return Whether The Customer has the correct Deletion Level.
     */
    public Mono<Boolean> ValidatePurgeProtectionAndSoftDeleteSettingsAsync(
        KeyVaultKeyUriProperties keyVaultUriProperties) {
        KeyAsyncClient akvClient = this.GetAkvClientAsync(keyVaultUriProperties).block();
        try {
            KeyVaultKey getKeyResponse = akvClient.getKey(keyVaultUriProperties.getKeyName()).block();

            String keyDeletionRecoveryLevel = null;
            if (getKeyResponse != null && getKeyResponse.getProperties() != null) {
                keyDeletionRecoveryLevel = getKeyResponse.getProperties().getRecoveryLevel();
            }

            return Mono.just(keyDeletionRecoveryLevel != null && (
                keyDeletionRecoveryLevel.contains(KeyVaultConstants.DeletionRecoveryLevel.Recoverable)
                    || keyDeletionRecoveryLevel.contains(KeyVaultConstants.DeletionRecoveryLevel.RecoverableProtectedSubscription)
                    || keyDeletionRecoveryLevel.contains(KeyVaultConstants.DeletionRecoveryLevel.CustomizedRecoverable)
                    || keyDeletionRecoveryLevel.contains(KeyVaultConstants.DeletionRecoveryLevel.CustomizedRecoverableProtectedSubscription)));
        }
        //        catch (RequestFailedException ex)
        //        {
        //            throw new KeyVaultAccessException(
        //                ex.Status,
        //                ex.ErrorCode,
        //                "ValidatePurgeProtectionAndSoftDeleteSettingsAsync: Failed to fetch Key from Key Vault.",
        //                ex);
        //        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new KeyVaultAccessException();
        }
    }

    /**
     * Obtains the KeyClient to retrieve keys from Keyvault.
     *
     * @param keyVaultUriProperties Parsed key Vault Uri Properties.
     * @return Key Client
     */
    private Mono<KeyAsyncClient> GetAkvClientAsync(
        KeyVaultKeyUriProperties keyVaultUriProperties) {

        // Called once per KEYVALTNAME
        // Eg:https://KEYVALTNAME.vault.azure.net/
        Mono<KeyAsyncClient> akvClientMono = this.akvClientCache.getAsync(
            /** key: */keyVaultUriProperties.getKeyUri(),
            /** obsoleteValue: */null,
            /** singleValueInitFunc: */() ->
            {
                TokenCredential tokenCred =
                    this.keyVaultTokenCredentialFactory.getTokenCredentialAsync(keyVaultUriProperties.getKeyUri()).block();
                return Mono.just(this.keyClientFactory.getKeyClient(keyVaultUriProperties, tokenCred));
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
    private Mono<CryptographyAsyncClient> GetCryptoClientAsync(
        KeyVaultKeyUriProperties keyVaultUriProperties) {

        // Get a Crypto Client for Wrap and UnWrap,this gets init per Key Version
        // Cache it against the KeyVersion/KeyId
        // Eg: :https://KEYVAULTNAME.vault.azure.net/keys/keyname/KEYID
        Mono<CryptographyAsyncClient> cryptoClientMono = this.akvCryptoClientCache.getAsync(
            keyVaultUriProperties.getKeyUri(),
            /** obsoleteValue: */null,
            /** singleValueInitFunc: */() ->
            {
                // we need to acquire the Client Cert Creds for cases where we directly access Crypto Services.
                Mono<TokenCredential> tokenCredMono =
                    this.keyVaultTokenCredentialFactory.getTokenCredentialAsync(keyVaultUriProperties.getKeyUri());
                return tokenCredMono.map(
                    tokenCred -> {
                        return this.cryptographyClientFactory.GetCryptographyClient(keyVaultUriProperties, tokenCred);
                    }

                );
            });
        return cryptoClientMono;
    }
}
