// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.encryption.CosmosEncryptionType;
import com.azure.cosmos.encryption.EncryptionBridgeInternal;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.models.ClientEncryptionIncludedPath;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.microsoft.data.encryption.cryptography.AeadAes256CbcHmac256EncryptionAlgorithm;
import com.microsoft.data.encryption.cryptography.EncryptionKeyStoreProvider;
import com.microsoft.data.encryption.cryptography.EncryptionType;
import com.microsoft.data.encryption.cryptography.KeyEncryptionKey;
import com.microsoft.data.encryption.cryptography.MicrosoftDataEncryptionException;
import com.microsoft.data.encryption.cryptography.ProtectedDataEncryptionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.security.InvalidKeyException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

public final class EncryptionSettings {
    private final static Logger LOGGER = LoggerFactory.getLogger(EncryptionSettings.class);

    private AsyncCache<String, CachedEncryptionSettings> encryptionSettingCacheByPropertyName = new AsyncCache<>();
    private String clientEncryptionKeyId;
    private Instant encryptionSettingTimeToLive;
    private ProtectedDataEncryptionKey dataEncryptionKey;
    private AeadAes256CbcHmac256EncryptionAlgorithm aeadAes256CbcHmac256EncryptionAlgorithm;
    private EncryptionType encryptionType;

    public EncryptionSettings() {
    }

    public Mono<EncryptionSettings> getEncryptionSettingForPropertyAsync(
        String propertyName,
        EncryptionProcessor encryptionProcessor) {
        Mono<CachedEncryptionSettings> settingsMono = this.encryptionSettingCacheByPropertyName.getAsync(propertyName
            , null, () -> fetchCachedEncryptionSettingsAsync(propertyName, encryptionProcessor));
        return settingsMono.flatMap(cachedEncryptionSettings -> {
            if (cachedEncryptionSettings == null) {
                return Mono.empty();
            }

            // we just cache the algo for the property for a duration of  1 hour and when it expires we try to fetch
            // the cached Encrypted key
            // from the Cosmos Client and try to create a Protected Data Encryption Key which tries to unwrap the key.
            // 1) Try to check if the KEK has been revoked may be post rotation. If the request fails this could mean
            // the KEK was revoked,
            // the user might have rewraped the Key and that is when we try to force fetch it from the Backend.
            // So we only read back from the backend only when an operation like wrap/unwrap with the Master Key fails.
            if (cachedEncryptionSettings.getEncryptionSettingsExpiryUtc().isBefore(Instant.now())) {
                return this.encryptionSettingCacheByPropertyName.getAsync(propertyName, cachedEncryptionSettings,
                    () -> fetchCachedEncryptionSettingsAsync(propertyName, encryptionProcessor)).map(latestCachedEncryptionSettings -> cachedEncryptionSettings.getEncryptionSettings());
            }
            return Mono.just(cachedEncryptionSettings.getEncryptionSettings());
        });
    }

    private Mono<CachedEncryptionSettings> fetchCachedEncryptionSettingsAsync(String propertyName,
                                                                              EncryptionProcessor encryptionProcessor) {
        Mono<ClientEncryptionPolicy> encryptionPolicyMono =
            EncryptionBridgeInternal.getClientEncryptionPolicyAsync(encryptionProcessor.getEncryptionCosmosClient(),
                encryptionProcessor.getCosmosAsyncContainer(), false);
        AtomicBoolean forceRefreshClientEncryptionKey = new AtomicBoolean(false);
        return encryptionPolicyMono.flatMap(clientEncryptionPolicy -> {
            if (clientEncryptionPolicy != null) {
                for (ClientEncryptionIncludedPath propertyToEncrypt : clientEncryptionPolicy.getIncludedPaths()) {
                    if (propertyToEncrypt.path.substring(1).equals(propertyName)) {
                        Mono<CosmosClientEncryptionKeyProperties> keyPropertiesMono =
                            EncryptionBridgeInternal.getClientEncryptionPropertiesAsync(encryptionProcessor.getEncryptionCosmosClient(), propertyToEncrypt.clientEncryptionKeyId, encryptionProcessor.getCosmosAsyncContainer(), forceRefreshClientEncryptionKey.get());
                        keyPropertiesMono.flatMap(keyProperties -> {
                            ProtectedDataEncryptionKey protectedDataEncryptionKey;
                            try {
                                protectedDataEncryptionKey = buildProtectedDataEncryptionKey(keyProperties,
                                    encryptionProcessor.getEncryptionKeyStoreProvider(),
                                    propertyToEncrypt.clientEncryptionKeyId);
                            } catch (Exception ex) {
                                return Mono.error(ex);
                            }
                            EncryptionSettings encryptionSettings = new EncryptionSettings();
                            encryptionSettings.encryptionSettingTimeToLive =
                                Instant.now().plus(Duration.ofMinutes(Constants.CACHED_ENCRYPTION_SETTING_DEFAULT_DEFAULT_TTL_IN_MINUTES));
                            encryptionSettings.clientEncryptionKeyId = propertyToEncrypt.clientEncryptionKeyId;
                            encryptionSettings.dataEncryptionKey = protectedDataEncryptionKey;
                            EncryptionType encryptionType = EncryptionType.Plaintext;
                            switch (propertyToEncrypt.encryptionType) {
                                case CosmosEncryptionType.DETERMINISTIC:
                                    encryptionType = EncryptionType.Deterministic;
                                    break;
                                case CosmosEncryptionType.RANDOMIZED:
                                    encryptionType = EncryptionType.Randomized;
                                    break;
                                default:
                                    LOGGER.debug("Invalid encryption type {}", propertyToEncrypt.encryptionType);
                                    break;
                            }
                            try {
                                encryptionSettings = EncryptionSettings.create(encryptionSettings, encryptionType);
                            } catch (MicrosoftDataEncryptionException e) {
                                return Mono.error(e);
                            }
                            return Mono.just(encryptionSettings);
                        }).retryWhen(Retry.withThrowable((throwableFlux -> throwableFlux.flatMap(throwable -> {
                            //TODO DO we need to check for MicrosoftDataEncryptionException too ?
                            // ProtectedDataEncryptionKey.getOrCreate throws Exception object and not specific
                            // exceptions

                            // the key was revoked. Try to fetch the latest EncryptionKeyProperties from the backend.
                            // This should succeed provided the user has rewraped the key with right set of meta data.
                            InvalidKeyException invalidKeyException = Utils.as(throwable, InvalidKeyException.class);
                            if (invalidKeyException != null) {
                                forceRefreshClientEncryptionKey.set(true);
                                return Mono.delay(Duration.ZERO).flux();
                            }
                            return Flux.error(throwable);
                        }))));
                    }
                }
            }
            return Mono.empty();
        });
    }

    ProtectedDataEncryptionKey buildProtectedDataEncryptionKey(CosmosClientEncryptionKeyProperties keyProperties,
                                                               EncryptionKeyStoreProvider encryptionKeyStoreProvider,
                                                               String keyId) throws Exception {

        KeyEncryptionKey keyEncryptionKey =
            KeyEncryptionKey.getOrCreate(keyProperties.getEncryptionKeyWrapMetadata().name,
                keyProperties.getEncryptionKeyWrapMetadata().value, encryptionKeyStoreProvider, false);
        return ProtectedDataEncryptionKey.getOrCreate(keyId, keyEncryptionKey,
            keyProperties.getWrappedDataEncryptionKey());
    }

    public String getClientEncryptionKeyId() {
        return clientEncryptionKeyId;
    }

    public void setClientEncryptionKeyId(String clientEncryptionKeyId) {
        this.clientEncryptionKeyId = clientEncryptionKeyId;
    }

    public AsyncCache<String, CachedEncryptionSettings> getEncryptionSettingCacheByPropertyName() {
        return encryptionSettingCacheByPropertyName;
    }

    public Instant getEncryptionSettingTimeToLive() {
        return encryptionSettingTimeToLive;
    }

    public void setEncryptionSettingTimeToLive(Instant encryptionSettingTimeToLive) {
        this.encryptionSettingTimeToLive = encryptionSettingTimeToLive;
    }

    public ProtectedDataEncryptionKey getDataEncryptionKey() {
        return dataEncryptionKey;
    }

    public void setDataEncryptionKey(ProtectedDataEncryptionKey dataEncryptionKey) {
        this.dataEncryptionKey = dataEncryptionKey;
    }

    public void setEncryptionSettingCacheByPropertyName(AsyncCache<String, CachedEncryptionSettings> encryptionSettingCacheByPropertyName) {
        this.encryptionSettingCacheByPropertyName = encryptionSettingCacheByPropertyName;
    }

    public AeadAes256CbcHmac256EncryptionAlgorithm getAeadAes256CbcHmac256EncryptionAlgorithm() {
        return aeadAes256CbcHmac256EncryptionAlgorithm;
    }

    public void setAeadAes256CbcHmac256EncryptionAlgorithm(AeadAes256CbcHmac256EncryptionAlgorithm aeadAes256CbcHmac256EncryptionAlgorithm) {
        this.aeadAes256CbcHmac256EncryptionAlgorithm = aeadAes256CbcHmac256EncryptionAlgorithm;
    }

    public EncryptionType getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(EncryptionType encryptionType) {
        this.encryptionType = encryptionType;
    }

    void setEncryptionSettingForProperty(String propertyName, EncryptionSettings encryptionSettings,
                                         Instant expiryUtc) {
        CachedEncryptionSettings cachedEncryptionSettings = new CachedEncryptionSettings(encryptionSettings, expiryUtc);
        this.encryptionSettingCacheByPropertyName.set(propertyName, cachedEncryptionSettings);
    }

    static EncryptionSettings create(
        EncryptionSettings settingsForKey,
        EncryptionType encryptionType) throws MicrosoftDataEncryptionException {
        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setClientEncryptionKeyId(encryptionSettings.clientEncryptionKeyId);
        encryptionSettings.setDataEncryptionKey(encryptionSettings.getDataEncryptionKey());
        encryptionSettings.setEncryptionSettingTimeToLive(encryptionSettings.getEncryptionSettingTimeToLive());
        AeadAes256CbcHmac256EncryptionAlgorithm aeadAes256CbcHmac256Algorithm =
            AeadAes256CbcHmac256EncryptionAlgorithm.getOrCreate(settingsForKey.getDataEncryptionKey(), encryptionType);
        encryptionSettings.setAeadAes256CbcHmac256EncryptionAlgorithm(aeadAes256CbcHmac256Algorithm);
        return encryptionSettings;
    }
}
