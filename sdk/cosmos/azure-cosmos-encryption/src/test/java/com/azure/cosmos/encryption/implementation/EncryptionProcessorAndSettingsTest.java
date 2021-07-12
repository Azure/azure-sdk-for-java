// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.implementation;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncClient;
import com.azure.cosmos.encryption.EncryptionAsyncApiCrudTest;
import com.azure.cosmos.encryption.EncryptionBridgeInternal;
import com.azure.cosmos.encryption.models.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.models.CosmosEncryptionType;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.ClientEncryptionIncludedPath;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.microsoft.data.encryption.cryptography.EncryptionKeyStoreProvider;
import org.assertj.core.api.Assertions;
import org.bouncycastle.util.encoders.Hex;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.security.InvalidKeyException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class EncryptionProcessorAndSettingsTest {
    private static final int TIMEOUT = 6000_000;
    private static final EncryptionAsyncApiCrudTest.TestEncryptionKeyStoreProvider keyStoreProvider =
        new EncryptionAsyncApiCrudTest.TestEncryptionKeyStoreProvider();

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void initializeEncryptionSettingsAsync() {
        CosmosAsyncContainer cosmosAsyncContainer = Mockito.mock(CosmosAsyncContainer.class);
        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = Mockito.mock(CosmosEncryptionAsyncClient.class);
        Mockito.when(cosmosEncryptionAsyncClient.getEncryptionKeyStoreProvider()).thenReturn(keyStoreProvider);
        Mockito.when(EncryptionBridgeInternal.getContainerPropertiesMono(cosmosEncryptionAsyncClient,
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateContainerWithCosmosEncryptionPolicy()));
        Mockito.when(EncryptionBridgeInternal.getClientEncryptionPropertiesAsync(cosmosEncryptionAsyncClient,
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateClientEncryptionKeyProperties()));
        EncryptionProcessor encryptionProcessor = new EncryptionProcessor(cosmosAsyncContainer,
            cosmosEncryptionAsyncClient);

        EncryptionSettings encryptionSettings = encryptionProcessor.getEncryptionSettings();
        try {
            encryptionSettings.getEncryptionSettingCacheByPropertyName().getAsync("sensitiveString", null, null).block();
            fail("encryptionSettings should be empty");
        } catch (NullPointerException ex) {
            // expected as we don't have any value in cache yet
        }
        Assertions.assertThat(ReflectionUtils.isEncryptionSettingsInitDone(encryptionProcessor).get()).isFalse();

        encryptionProcessor.initializeEncryptionSettingsAsync(false).block();
        encryptionSettings = encryptionProcessor.getEncryptionSettings();
        //We should have the value now in encryptionSettingCacheByPropertyName
        CachedEncryptionSettings cachedEncryptionSettings =
            encryptionSettings.getEncryptionSettingCacheByPropertyName().getAsync("sensitiveString", null, null).block();
        assertThat(ReflectionUtils.isEncryptionSettingsInitDone(encryptionProcessor)).isTrue();
        assertThat(cachedEncryptionSettings).isNotNull();
        assertThat(cachedEncryptionSettings.getEncryptionSettings().getClientEncryptionKeyId()).isEqualTo("key1");
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void withoutInitializeEncryptionSettingsAsync() {
        CosmosAsyncContainer cosmosAsyncContainer = Mockito.mock(CosmosAsyncContainer.class);
        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = Mockito.mock(CosmosEncryptionAsyncClient.class);
        Mockito.when(cosmosEncryptionAsyncClient.getEncryptionKeyStoreProvider()).thenReturn(keyStoreProvider);
        Mockito.when(EncryptionBridgeInternal.getContainerPropertiesMono(cosmosEncryptionAsyncClient,
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateContainerWithCosmosEncryptionPolicy()));
        Mockito.when(EncryptionBridgeInternal.getClientEncryptionPropertiesAsync(cosmosEncryptionAsyncClient,
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateClientEncryptionKeyProperties()));
        EncryptionProcessor encryptionProcessor = new EncryptionProcessor(cosmosAsyncContainer,
            cosmosEncryptionAsyncClient);

        EncryptionSettings encryptionSettings = encryptionProcessor.getEncryptionSettings();
        encryptionSettings.setDatabaseRid("TestDb");
        try {
            encryptionSettings.getEncryptionSettingCacheByPropertyName().getAsync("sensitiveString", null, null).block();
            fail("encryptionSettings should be empty");
        } catch (NullPointerException ex) {
            // expected as we dont have any value in cache yet
        }

        EncryptionSettings spyEncryptionSettings = Mockito.spy(encryptionSettings);
        EncryptionSettings cachedEncryptionSettings =
            spyEncryptionSettings.getEncryptionSettingForPropertyAsync("sensitiveString", encryptionProcessor).block();
        assertThat(ReflectionUtils.isEncryptionSettingsInitDone(encryptionProcessor).get()).isFalse();
        assertThat(cachedEncryptionSettings).isNotNull();
        assertThat(cachedEncryptionSettings.getClientEncryptionKeyId()).isEqualTo("key1");
        Mockito.verify(spyEncryptionSettings, Mockito.times(1)).fetchCachedEncryptionSettingsAsync(Mockito.anyString(), Mockito.any(EncryptionProcessor.class));

        // fetchCachedEncryptionSettingsAsync should not called second time, encrytion setting should served from cache
        spyEncryptionSettings.getEncryptionSettingForPropertyAsync("sensitiveString", encryptionProcessor).block();
        Mockito.verify(spyEncryptionSettings, Mockito.times(1)).fetchCachedEncryptionSettingsAsync(Mockito.anyString(), Mockito.any(EncryptionProcessor.class));
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void encryptionSettingCachedTimeToLive() {
        CosmosAsyncContainer cosmosAsyncContainer = Mockito.mock(CosmosAsyncContainer.class);
        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = Mockito.mock(CosmosEncryptionAsyncClient.class);
        Mockito.when(cosmosEncryptionAsyncClient.getEncryptionKeyStoreProvider()).thenReturn(keyStoreProvider);
        Mockito.when(EncryptionBridgeInternal.getContainerPropertiesMono(cosmosEncryptionAsyncClient,
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateContainerWithCosmosEncryptionPolicy()));
        Mockito.when(EncryptionBridgeInternal.getClientEncryptionPropertiesAsync(cosmosEncryptionAsyncClient,
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateClientEncryptionKeyProperties()));
        EncryptionProcessor encryptionProcessor = new EncryptionProcessor(cosmosAsyncContainer,
            cosmosEncryptionAsyncClient);

        EncryptionSettings encryptionSettings = encryptionProcessor.getEncryptionSettings();
        encryptionSettings.setDatabaseRid("TestDb");
        EncryptionSettings spyEncryptionSettings = Mockito.spy(encryptionSettings);
        EncryptionSettings cachedEncryptionSettings =
            spyEncryptionSettings.getEncryptionSettingForPropertyAsync("sensitiveString", encryptionProcessor).block();
        Mockito.verify(spyEncryptionSettings, Mockito.times(1)).fetchCachedEncryptionSettingsAsync(Mockito.anyString(), Mockito.any(EncryptionProcessor.class));
        assertThat(cachedEncryptionSettings).isNotNull();
        assertThat(cachedEncryptionSettings.getClientEncryptionKeyId()).isEqualTo("key1");
        assertThat(cachedEncryptionSettings.getEncryptionSettingTimeToLive()).isAfter(Instant.now().plus(Duration.ofMinutes(59)));
        assertThat(cachedEncryptionSettings.getEncryptionSettingTimeToLive()).isBefore(Instant.now().plus(Duration.ofMinutes(61)));

        spyEncryptionSettings.setEncryptionSettingForProperty("sensitiveString", cachedEncryptionSettings,
            Instant.now());
        // fetchCachedEncryptionSettingsAsync should be called for second time as cached encryption setting is expired
        spyEncryptionSettings.getEncryptionSettingForPropertyAsync("sensitiveString", encryptionProcessor).block();
        Mockito.verify(spyEncryptionSettings, Mockito.times(2)).fetchCachedEncryptionSettingsAsync(Mockito.anyString(), Mockito.any(EncryptionProcessor.class));
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void invalidClientEncryptionKeyException() throws Exception {
        CosmosAsyncContainer cosmosAsyncContainer = Mockito.mock(CosmosAsyncContainer.class);
        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = Mockito.mock(CosmosEncryptionAsyncClient.class);
        Mockito.when(cosmosEncryptionAsyncClient.getEncryptionKeyStoreProvider()).thenReturn(keyStoreProvider);
        Mockito.when(EncryptionBridgeInternal.getContainerPropertiesMono(cosmosEncryptionAsyncClient,
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateContainerWithCosmosEncryptionPolicy()));
        CosmosClientEncryptionKeyProperties keyProperties = generateClientEncryptionKeyProperties();
        Mockito.when(EncryptionBridgeInternal.getClientEncryptionPropertiesAsync(cosmosEncryptionAsyncClient,
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean())).thenReturn(Mono.just(keyProperties));
        EncryptionProcessor encryptionProcessor = new EncryptionProcessor(cosmosAsyncContainer,
            cosmosEncryptionAsyncClient);
        EncryptionSettings encryptionSettings = encryptionProcessor.getEncryptionSettings();

        EncryptionSettings mockEncryptionSettings = Mockito.mock(EncryptionSettings.class);
        ReflectionUtils.setEncryptionSettings(encryptionProcessor, mockEncryptionSettings);
        Mockito.when(mockEncryptionSettings.buildProtectedDataEncryptionKey(Mockito.any(CosmosClientEncryptionKeyProperties.class), Mockito.any(EncryptionKeyStoreProvider.class), Mockito.anyString())).
            thenThrow(new InvalidKeyException()).thenReturn(encryptionSettings.buildProtectedDataEncryptionKey(keyProperties, keyStoreProvider, keyProperties.getId()));
        Mockito.doNothing().when(mockEncryptionSettings).setEncryptionSettingForProperty(Mockito.anyString(),
            Mockito.any(EncryptionSettings.class), Mockito.any(Instant.class));
        Assertions.assertThat(ReflectionUtils.isEncryptionSettingsInitDone(encryptionProcessor).get()).isFalse();
        encryptionProcessor.initializeEncryptionSettingsAsync(false).block();

        //Throw InvalidKeyException twice , we will retry refreshing key from database only once
        encryptionProcessor = new EncryptionProcessor(cosmosAsyncContainer,
            cosmosEncryptionAsyncClient);
        encryptionSettings = encryptionProcessor.getEncryptionSettings();
        mockEncryptionSettings = Mockito.mock(EncryptionSettings.class);
        ReflectionUtils.setEncryptionSettings(encryptionProcessor, mockEncryptionSettings);
        Mockito.when(mockEncryptionSettings.buildProtectedDataEncryptionKey(Mockito.any(CosmosClientEncryptionKeyProperties.class), Mockito.any(EncryptionKeyStoreProvider.class), Mockito.anyString())).
            thenThrow(new InvalidKeyException(), new InvalidKeyException()).thenReturn(encryptionSettings.buildProtectedDataEncryptionKey(keyProperties, keyStoreProvider, keyProperties.getId()));
        try {
            encryptionProcessor.initializeEncryptionSettingsAsync(false).block();
            fail("Expecting initializeEncryptionSettingsAsync to throw InvalidKeyException");
        } catch (Exception ex) {
            //expecting InvalidKeyException
            InvalidKeyException invalidKeyException = Utils.as(ex.getCause(), InvalidKeyException.class);
            assertThat(invalidKeyException).isNotNull();
        }
    }

    private ClientEncryptionPolicy generateClientEncryptionPolicy() {
        ClientEncryptionIncludedPath includedPath1 = new ClientEncryptionIncludedPath();
        includedPath1.setClientEncryptionKeyId("key1");
        includedPath1.setPath("/sensitiveString");
        includedPath1.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath1.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);
        List<ClientEncryptionIncludedPath> paths = new ArrayList<>();
        paths.add(includedPath1);
        return new ClientEncryptionPolicy(paths);
    }

    private CosmosContainerProperties generateContainerWithCosmosEncryptionPolicy() {
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(UUID.randomUUID().toString(), "/mypk");
        Resource resource = ReflectionUtils.geResource(containerProperties);
        resource.setSelfLink("dbs/testDb/colls/testCol");
        ClientEncryptionIncludedPath includedPath1 = new ClientEncryptionIncludedPath();
        includedPath1.setClientEncryptionKeyId("key1");
        includedPath1.setPath("/sensitiveString");
        includedPath1.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath1.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);
        List<ClientEncryptionIncludedPath> paths = new ArrayList<>();
        paths.add(includedPath1);
        return containerProperties.setClientEncryptionPolicy(new ClientEncryptionPolicy(paths));
    }

    private CosmosClientEncryptionKeyProperties generateClientEncryptionKeyProperties() {
        byte[] key = Hex.decode(("34 62 52 77 f9 ee 11 9f 04 8c 6f 50 9c e4 c2 5b b3 39 f4 d0 4d c1 6a 32 fa 2b 3b aa" +
            " " +
            "ae 1e d9 1c").replace(" ", ""));
        EncryptionKeyWrapMetadata metadata = new EncryptionKeyWrapMetadata(keyStoreProvider.getProviderName(),
            "key1", "tempmetadata1");
        return new CosmosClientEncryptionKeyProperties("key1",
            CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, key, metadata);
    }
}
