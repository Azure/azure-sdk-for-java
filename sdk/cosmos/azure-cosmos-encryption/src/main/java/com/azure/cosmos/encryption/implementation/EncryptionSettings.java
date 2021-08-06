// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.implementation;

import com.azure.cosmos.encryption.EncryptionBridgeInternal;
import com.azure.cosmos.encryption.models.CosmosEncryptionType;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.models.ClientEncryptionIncludedPath;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosContainerProperties;
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
import reactor.core.scheduler.Schedulers;
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
    private String databaseRid;

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

    Mono<CachedEncryptionSettings> fetchCachedEncryptionSettingsAsync(String propertyName,
                                                                      EncryptionProcessor encryptionProcessor) {
        Mono<CosmosContainerProperties> containerPropertiesMono =
            EncryptionBridgeInternal.getContainerPropertiesMono(encryptionProcessor.getEncryptionCosmosClient(),
                encryptionProcessor.getCosmosAsyncContainer(), false);
        AtomicBoolean forceRefreshClientEncryptionKey = new AtomicBoolean(false);
        return containerPropertiesMono.flatMap(cosmosContainerProperties -> {
            if (cosmosContainerProperties.getClientEncryptionPolicy() != null) {
                for (ClientEncryptionIncludedPath propertyToEncrypt : cosmosContainerProperties.getClientEncryptionPolicy().getIncludedPaths()) {
                    if (propertyToEncrypt.getPath().substring(1).equals(propertyName)) {
                        return EncryptionBridgeInternal.getClientEncryptionPropertiesAsync(encryptionProcessor.getEncryptionCosmosClient(),
                            propertyToEncrypt.getClientEncryptionKeyId(),
                            this.databaseRid,
                            encryptionProcessor.getCosmosAsyncContainer(),
                            forceRefreshClientEncryptionKey.get())
                            .publishOn(Schedulers.boundedElastic())
                            .flatMap(keyProperties -> {
                                ProtectedDataEncryptionKey protectedDataEncryptionKey;
                                try {
                                    protectedDataEncryptionKey = buildProtectedDataEncryptionKey(keyProperties,
                                        encryptionProcessor.getEncryptionKeyStoreProvider(),
                                        propertyToEncrypt.getClientEncryptionKeyId());
                                } catch (Exception ex) {
                                    return Mono.error(ex);
                                }
                                EncryptionSettings encryptionSettings = new EncryptionSettings();
                                encryptionSettings.setDatabaseRid(databaseRid);
                                encryptionSettings.encryptionSettingTimeToLive =
                                    Instant.now().plus(Duration.ofMinutes(Constants.CACHED_ENCRYPTION_SETTING_DEFAULT_DEFAULT_TTL_IN_MINUTES));
                                encryptionSettings.clientEncryptionKeyId = propertyToEncrypt.getClientEncryptionKeyId();
                                encryptionSettings.dataEncryptionKey = protectedDataEncryptionKey;
                                EncryptionType encryptionType = EncryptionType.Plaintext;
                                switch (propertyToEncrypt.getEncryptionType()) {
                                    case CosmosEncryptionType.DETERMINISTIC:
                                        encryptionType = EncryptionType.Deterministic;
                                        break;
                                    case CosmosEncryptionType.RANDOMIZED:
                                        encryptionType = EncryptionType.Randomized;
                                        break;
                                    default:
                                        LOGGER.debug("Invalid encryption type {}", propertyToEncrypt.getEncryptionType());
                                        break;
                                }
                                try {
                                    encryptionSettings = EncryptionSettings.create(encryptionSettings, encryptionType);
                                } catch (MicrosoftDataEncryptionException e) {
                                    return Mono.error(e);
                                }
                                return Mono.just(new CachedEncryptionSettings(encryptionSettings,
                                    encryptionSettings.encryptionSettingTimeToLive));
                            }).retryWhen(Retry.withThrowable((throwableFlux -> throwableFlux.flatMap(throwable -> {
                                //TODO DO we need to check for MicrosoftDataEncryptionException too ?
                                // ProtectedDataEncryptionKey.getOrCreate throws Exception object and not specific
                                // exceptions

                                // the key was revoked. Try to fetch the latest EncryptionKeyProperties from the
                                // backend.
                                // This should succeed provided the user has rewraped the key with right set of meta
                                // data.
                                InvalidKeyException invalidKeyException = Utils.as(throwable,
                                    InvalidKeyException.class);
                                if (invalidKeyException != null && !forceRefreshClientEncryptionKey.get()) {
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
            KeyEncryptionKey.getOrCreate(keyProperties.getEncryptionKeyWrapMetadata().getName(),
                keyProperties.getEncryptionKeyWrapMetadata().getValue(), encryptionKeyStoreProvider, false);
        return ProtectedDataEncryptionKey.getOrCreate(keyId, keyEncryptionKey,
            keyProperties.getWrappedDataEncryptionKey());
    }

    String getClientEncryptionKeyId() {
        return clientEncryptionKeyId;
    }

    void setClientEncryptionKeyId(String clientEncryptionKeyId) {
        this.clientEncryptionKeyId = clientEncryptionKeyId;
    }

    AsyncCache<String, CachedEncryptionSettings> getEncryptionSettingCacheByPropertyName() {
        return encryptionSettingCacheByPropertyName;
    }

    Instant getEncryptionSettingTimeToLive() {
        return encryptionSettingTimeToLive;
    }

    void setEncryptionSettingTimeToLive(Instant encryptionSettingTimeToLive) {
        this.encryptionSettingTimeToLive = encryptionSettingTimeToLive;
    }

    ProtectedDataEncryptionKey getDataEncryptionKey() {
        return dataEncryptionKey;
    }

    void setDataEncryptionKey(ProtectedDataEncryptionKey dataEncryptionKey) {
        this.dataEncryptionKey = dataEncryptionKey;
    }

    public void setEncryptionSettingCacheByPropertyName(AsyncCache<String, CachedEncryptionSettings> encryptionSettingCacheByPropertyName) {
        this.encryptionSettingCacheByPropertyName = encryptionSettingCacheByPropertyName;
    }

    public AeadAes256CbcHmac256EncryptionAlgorithm getAeadAes256CbcHmac256EncryptionAlgorithm() {
        return aeadAes256CbcHmac256EncryptionAlgorithm;
    }

    void setAeadAes256CbcHmac256EncryptionAlgorithm(AeadAes256CbcHmac256EncryptionAlgorithm aeadAes256CbcHmac256EncryptionAlgorithm) {
        this.aeadAes256CbcHmac256EncryptionAlgorithm = aeadAes256CbcHmac256EncryptionAlgorithm;
    }

    public EncryptionType getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(EncryptionType encryptionType) {
        this.encryptionType = encryptionType;
    }

    public String getDatabaseRid() {
        return databaseRid;
    }

    public void setDatabaseRid(String databaseRid) {
        this.databaseRid = databaseRid;
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
        encryptionSettings.setDatabaseRid(settingsForKey.getDatabaseRid());
        encryptionSettings.setClientEncryptionKeyId(settingsForKey.clientEncryptionKeyId);
        encryptionSettings.setDataEncryptionKey(settingsForKey.getDataEncryptionKey());
        encryptionSettings.setEncryptionSettingTimeToLive(settingsForKey.getEncryptionSettingTimeToLive());
        encryptionSettings.setEncryptionType(encryptionType);
        AeadAes256CbcHmac256EncryptionAlgorithm aeadAes256CbcHmac256Algorithm =
            AeadAes256CbcHmac256EncryptionAlgorithm.getOrCreate(settingsForKey.getDataEncryptionKey(), encryptionType);
        encryptionSettings.setAeadAes256CbcHmac256EncryptionAlgorithm(aeadAes256CbcHmac256Algorithm);
        return encryptionSettings;
    }
}
