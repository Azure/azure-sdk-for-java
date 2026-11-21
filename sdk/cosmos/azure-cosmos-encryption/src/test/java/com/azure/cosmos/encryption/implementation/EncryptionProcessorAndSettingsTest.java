// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.implementation;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncClient;
import com.azure.cosmos.encryption.EncryptionAsyncApiCrudTest;
import com.azure.cosmos.encryption.implementation.keyprovider.EncryptionKeyStoreProviderImpl;
import com.azure.cosmos.encryption.implementation.mdesrc.cryptography.AeadAes256CbcHmac256EncryptionAlgorithm;
import com.azure.cosmos.encryption.implementation.mdesrc.cryptography.EncryptionKeyStoreProvider;
import com.azure.cosmos.encryption.implementation.mdesrc.cryptography.MicrosoftDataEncryptionException;
import com.azure.cosmos.encryption.models.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.models.CosmosEncryptionType;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.ClientEncryptionIncludedPath;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

public class EncryptionProcessorAndSettingsTest {
    private static final int TIMEOUT = 6000_000;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final EncryptionAsyncApiCrudTest.TestKeyEncryptionKeyResolver keyEncryptionKeyResolver =
        new EncryptionAsyncApiCrudTest.TestKeyEncryptionKeyResolver();
    private final static ImplementationBridgeHelpers.CosmosContainerPropertiesHelper.CosmosContainerPropertiesAccessor cosmosContainerPropertiesAccessor =
        ImplementationBridgeHelpers.CosmosContainerPropertiesHelper.getCosmosContainerPropertiesAccessor();
    private final static EncryptionImplementationBridgeHelpers.CosmosEncryptionAsyncClientHelper.CosmosEncryptionAsyncClientAccessor cosmosEncryptionAsyncClientAccessor =
        EncryptionImplementationBridgeHelpers.CosmosEncryptionAsyncClientHelper.getCosmosEncryptionAsyncClientAccessor();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(
        JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(),
        true
    );

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void initializeEncryptionSettingsAsync() throws Exception {
        CosmosAsyncContainer cosmosAsyncContainer = Mockito.mock(CosmosAsyncContainer.class);
        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = Mockito.mock(CosmosEncryptionAsyncClient.class);
        Mockito.when(cosmosEncryptionAsyncClient.getKeyEncryptionKeyResolver()).thenReturn(keyEncryptionKeyResolver);
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getContainerPropertiesAsync(cosmosEncryptionAsyncClient,
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateContainerWithCosmosEncryptionPolicy()));
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getClientEncryptionPropertiesAsync(cosmosEncryptionAsyncClient,
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean(), Mockito.nullable(String.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateClientEncryptionKeyProperties()));
        EncryptionKeyStoreProviderImpl encryptionKeyStoreProvider = new EncryptionKeyStoreProviderImpl(keyEncryptionKeyResolver, "TEST_KEY_RESOLVER");
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getEncryptionKeyStoreProviderImpl(cosmosEncryptionAsyncClient)).thenReturn(encryptionKeyStoreProvider);
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
    public void withoutInitializeEncryptionSettingsAsync() throws Exception {
        CosmosAsyncContainer cosmosAsyncContainer = Mockito.mock(CosmosAsyncContainer.class);
        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = Mockito.mock(CosmosEncryptionAsyncClient.class);
        Mockito.when(cosmosEncryptionAsyncClient.getKeyEncryptionKeyResolver()).thenReturn(keyEncryptionKeyResolver);
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getContainerPropertiesAsync(cosmosEncryptionAsyncClient,
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateContainerWithCosmosEncryptionPolicy()));
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getClientEncryptionPropertiesAsync(cosmosEncryptionAsyncClient,
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean(), Mockito.nullable(String.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateClientEncryptionKeyProperties()));
        EncryptionKeyStoreProviderImpl encryptionKeyStoreProvider = new EncryptionKeyStoreProviderImpl(keyEncryptionKeyResolver, "TEST_KEY_RESOLVER");
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getEncryptionKeyStoreProviderImpl(cosmosEncryptionAsyncClient)).thenReturn(encryptionKeyStoreProvider);
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
    public void encryptionSettingCachedTimeToLive()  throws Exception {
        CosmosAsyncContainer cosmosAsyncContainer = Mockito.mock(CosmosAsyncContainer.class);
        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = Mockito.mock(CosmosEncryptionAsyncClient.class);
        Mockito.when(cosmosEncryptionAsyncClient.getKeyEncryptionKeyResolver()).thenReturn(keyEncryptionKeyResolver);
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getContainerPropertiesAsync(cosmosEncryptionAsyncClient,
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateContainerWithCosmosEncryptionPolicy()));
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getClientEncryptionPropertiesAsync(cosmosEncryptionAsyncClient,
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean(), Mockito.nullable(String.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateClientEncryptionKeyProperties()));
        EncryptionKeyStoreProviderImpl encryptionKeyStoreProvider = new EncryptionKeyStoreProviderImpl(keyEncryptionKeyResolver, "TEST_KEY_RESOLVER");
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getEncryptionKeyStoreProviderImpl(cosmosEncryptionAsyncClient)).thenReturn(encryptionKeyStoreProvider);

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
            Instant.now().minus(Duration.ofSeconds(5)));
        // fetchCachedEncryptionSettingsAsync should be called for second time as cached encryption setting is expired
        spyEncryptionSettings.getEncryptionSettingForPropertyAsync("sensitiveString", encryptionProcessor).block();
        Mockito.verify(spyEncryptionSettings, Mockito.times(2)).fetchCachedEncryptionSettingsAsync(Mockito.anyString(), Mockito.any(EncryptionProcessor.class));
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void invalidClientEncryptionKeyException() throws Exception {
        CosmosAsyncContainer cosmosAsyncContainer = Mockito.mock(CosmosAsyncContainer.class);
        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = Mockito.mock(CosmosEncryptionAsyncClient.class);
        Mockito.when(cosmosEncryptionAsyncClient.getKeyEncryptionKeyResolver()).thenReturn(keyEncryptionKeyResolver);
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getContainerPropertiesAsync(cosmosEncryptionAsyncClient,
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateContainerWithCosmosEncryptionPolicy()));
        CosmosClientEncryptionKeyProperties keyProperties = generateClientEncryptionKeyProperties();
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getClientEncryptionPropertiesAsync(cosmosEncryptionAsyncClient,
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean(), Mockito.nullable(String.class), Mockito.anyBoolean())).thenReturn(Mono.just(keyProperties));
        EncryptionKeyStoreProviderImpl encryptionKeyStoreProvider = new EncryptionKeyStoreProviderImpl(keyEncryptionKeyResolver, "TEST_KEY_RESOLVER");
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getEncryptionKeyStoreProviderImpl(cosmosEncryptionAsyncClient)).thenReturn(encryptionKeyStoreProvider);
        EncryptionProcessor encryptionProcessor = new EncryptionProcessor(cosmosAsyncContainer,
            cosmosEncryptionAsyncClient);
        EncryptionSettings encryptionSettings = encryptionProcessor.getEncryptionSettings();

        EncryptionSettings mockEncryptionSettings = Mockito.mock(EncryptionSettings.class);
        ReflectionUtils.setEncryptionSettings(encryptionProcessor, mockEncryptionSettings);
        Mockito.when(mockEncryptionSettings.buildProtectedDataEncryptionKey(Mockito.any(CosmosClientEncryptionKeyProperties.class), Mockito.any(EncryptionKeyStoreProvider.class), Mockito.anyString())).
            thenThrow(new InvalidKeyException()).thenReturn(encryptionSettings.buildProtectedDataEncryptionKey(keyProperties, cosmosEncryptionAsyncClientAccessor.getEncryptionKeyStoreProviderImpl(cosmosEncryptionAsyncClient), keyProperties.getId()));
        Mockito.doNothing().when(mockEncryptionSettings).setEncryptionSettingForProperty(Mockito.anyString(),
            Mockito.any(EncryptionSettings.class), Mockito.any(Instant.class));
        Assertions.assertThat(ReflectionUtils.isEncryptionSettingsInitDone(encryptionProcessor).get()).isFalse();
        encryptionProcessor.initializeEncryptionSettingsAsync(false).block();

        //Throw InvalidKeyException thrice , we will retry refreshing key from database only once
        encryptionProcessor = new EncryptionProcessor(cosmosAsyncContainer,
            cosmosEncryptionAsyncClient);
        encryptionSettings = encryptionProcessor.getEncryptionSettings();
        mockEncryptionSettings = Mockito.mock(EncryptionSettings.class);
        ReflectionUtils.setEncryptionSettings(encryptionProcessor, mockEncryptionSettings);
        Mockito.when(mockEncryptionSettings.buildProtectedDataEncryptionKey(Mockito.any(CosmosClientEncryptionKeyProperties.class), Mockito.any(EncryptionKeyStoreProvider.class), Mockito.anyString())).
            thenThrow(new InvalidKeyException(), new InvalidKeyException(), new InvalidKeyException()).thenReturn(encryptionSettings.buildProtectedDataEncryptionKey(keyProperties, cosmosEncryptionAsyncClientAccessor.getEncryptionKeyStoreProviderImpl(cosmosEncryptionAsyncClient), keyProperties.getId()));
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
        includedPath1.setEncryptionType(CosmosEncryptionType.DETERMINISTIC.getName());
        includedPath1.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName());
        List<ClientEncryptionIncludedPath> paths = new ArrayList<>();
        paths.add(includedPath1);
        return new ClientEncryptionPolicy(paths);
    }

    private CosmosContainerProperties generateContainerWithCosmosEncryptionPolicy() {
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(UUID.randomUUID().toString(), "/mypk");
        cosmosContainerPropertiesAccessor.setSelfLink(containerProperties, "dbs/testDb/colls/testCol");
        ClientEncryptionIncludedPath includedPath1 = new ClientEncryptionIncludedPath();
        includedPath1.setClientEncryptionKeyId("key1");
        includedPath1.setPath("/sensitiveString");
        includedPath1.setEncryptionType(CosmosEncryptionType.DETERMINISTIC.getName());
        includedPath1.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName());
        List<ClientEncryptionIncludedPath> paths = new ArrayList<>();
        paths.add(includedPath1);
        return containerProperties.setClientEncryptionPolicy(new ClientEncryptionPolicy(paths));
    }

    private CosmosClientEncryptionKeyProperties generateClientEncryptionKeyProperties() throws JsonProcessingException {
        TextNode treeNode = new TextNode("S84PieiyZNyHxeuUuX5IXSV2KOktpt02tQM4QLhm8dI=");
        byte[] key = MAPPER.treeToValue(treeNode, byte[].class);
        EncryptionKeyWrapMetadata metadata = new EncryptionKeyWrapMetadata("TEST_KEY_RESOLVER",
            "key1", "tempmetadata1", "RSA-OAEP");
        return new CosmosClientEncryptionKeyProperties("key1",
            CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName(), key, metadata);
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void encryptEmptyArray()  throws Exception {
        CosmosAsyncContainer cosmosAsyncContainer = Mockito.mock(CosmosAsyncContainer.class);
        String json = "{\"id\":\"idValue\",\"sensitiveString\":{\"arr\":[]}}";

        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = Mockito.mock(CosmosEncryptionAsyncClient.class);
        Mockito.when(cosmosEncryptionAsyncClient.getKeyEncryptionKeyResolver()).thenReturn(keyEncryptionKeyResolver);
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getContainerPropertiesAsync(cosmosEncryptionAsyncClient,
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateContainerWithCosmosEncryptionPolicy()));
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getClientEncryptionPropertiesAsync(cosmosEncryptionAsyncClient,
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean(), Mockito.nullable(String.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateClientEncryptionKeyProperties()));
        EncryptionKeyStoreProviderImpl encryptionKeyStoreProvider = new EncryptionKeyStoreProviderImpl(keyEncryptionKeyResolver, "TEST_KEY_RESOLVER");
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getEncryptionKeyStoreProviderImpl(cosmosEncryptionAsyncClient)).thenReturn(encryptionKeyStoreProvider);

        EncryptionProcessor encryptionProcessor = new EncryptionProcessor(cosmosAsyncContainer,
            cosmosEncryptionAsyncClient);

        ClientEncryptionIncludedPath includedPath = new ClientEncryptionIncludedPath();
        includedPath.setClientEncryptionKeyId("a");
        includedPath.setPath("/sensitiveString");
        includedPath.setEncryptionType(CosmosEncryptionType.DETERMINISTIC.getName());
        includedPath.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName());
        List<ClientEncryptionIncludedPath> paths = new ArrayList<>();
        paths.add(includedPath);
        encryptionProcessor.setClientEncryptionPolicy(new ClientEncryptionPolicy(paths));

        JsonNode document = OBJECT_MAPPER.readValue(json, ObjectNode.class);
        String output = new String(encryptionProcessor.decrypt(
            encryptionProcessor.encryptObjectNode(document).block(),
            CosmosItemSerializer.DEFAULT_SERIALIZER).block().getLeft());
        assertThat(output).isEqualTo(json);
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void encryptPatchMixedArray() throws Exception {
        EncryptionProcessor encryptionProcessor = createEncryptionProcessor();
        JsonNode patchValue = OBJECT_MAPPER.readTree(
            "[{\"label\":\"patched\"},\"PATCH_SECRET\",[\"NESTED_SECRET\"],null,42,{\"tail\":\"TAIL_SECRET\"}]");
        JsonNode expectedValue = patchValue.deepCopy();

        JsonNode encryptedPatchValue = encryptionProcessor.encryptPatchNode(patchValue, "/sensitiveString").block();

        assertThat(encryptedPatchValue.get(0).get("label").isBinary()).isTrue();
        assertThat(encryptedPatchValue.get(1).isBinary()).isTrue();
        assertThat(encryptedPatchValue.get(2).get(0).isBinary()).isTrue();
        assertThat(encryptedPatchValue.get(3).isNull()).isTrue();
        assertThat(encryptedPatchValue.get(4).isBinary()).isTrue();
        assertThat(encryptedPatchValue.get(5).get("tail").isBinary()).isTrue();

        ObjectNode patchedDocument = OBJECT_MAPPER.createObjectNode();
        patchedDocument.put("id", "patchItem");
        patchedDocument.set("sensitiveString", encryptedPatchValue);
        JsonNode decryptedPatchedDocument = encryptionProcessor.decrypt(patchedDocument,
            CosmosItemSerializer.DEFAULT_SERIALIZER).block().getRight();
        assertThat(decryptedPatchedDocument.get("sensitiveString").toString()).isEqualTo(expectedValue.toString());

        ObjectNode document = OBJECT_MAPPER.createObjectNode();
        document.put("id", "createItem");
        document.set("sensitiveString", expectedValue.deepCopy());
        byte[] encryptedDocumentBytes = encryptionProcessor.encrypt(document).block();
        JsonNode encryptedDocument = OBJECT_MAPPER.readTree(encryptedDocumentBytes);
        assertThat(encryptedDocument.toString()).doesNotContain("patched", "PATCH_SECRET", "NESTED_SECRET",
            "TAIL_SECRET");
        JsonNode decryptedDocument = encryptionProcessor.decrypt(encryptedDocumentBytes,
            CosmosItemSerializer.DEFAULT_SERIALIZER).block().getRight();
        assertThat(decryptedDocument.get("sensitiveString").toString()).isEqualTo(expectedValue.toString());
    }

    @DataProvider(name = "legacyMixedArrays")
    public Object[][] legacyMixedArrays() {
        return new Object[][] {
            {"[{\"label\":\"secret\"},42]", IllegalArgumentException.class},
            {"[{\"label\":\"secret\"},true]", IllegalArgumentException.class},
            {"[{\"label\":\"secret\"},\"LEGACY_SECRET!\"]", IOException.class},
            {"[{\"label\":\"secret\"},\"\"]", IllegalArgumentException.class},
            {"[{\"label\":\"secret\"},\"YWJjZA==\"]", MicrosoftDataEncryptionException.class},
            {"[[\"secret\"],42]", IllegalArgumentException.class}
        };
    }

    @Test(groups = {"unit"}, dataProvider = "legacyMixedArrays", timeOut = TIMEOUT)
    public void decryptLegacyMixedArrayReportsMigration(String arrayJson,
                                                       Class<? extends Throwable> expectedCause) throws Exception {
        EncryptionProcessor encryptionProcessor = createEncryptionProcessor();
        ArrayNode legacyArray = (ArrayNode) OBJECT_MAPPER.readTree(arrayJson);
        // Old writers encrypted the leading container but skipped subsequent scalar elements.
        legacyArray.set(0, encryptionProcessor.encryptPatchNode(legacyArray.get(0), "/sensitiveString/0").block());
        ObjectNode document = OBJECT_MAPPER.createObjectNode();
        document.put("id", "legacyItem");
        document.set("sensitiveString", legacyArray);
        byte[] storedDocument = OBJECT_MAPPER.writeValueAsBytes(document);

        assertThatThrownBy(() -> encryptionProcessor.decrypt(storedDocument,
            CosmosItemSerializer.DEFAULT_SERIALIZER).block())
            .isInstanceOf(IllegalStateException.class)
            .hasCauseInstanceOf(expectedCause)
            .hasMessageContaining("index 1")
            .hasMessageContaining("encrypted property '/sensitiveString'")
            .hasMessageContaining("SDK version that wrote it")
            .hasMessageContaining("rewrite the decrypted item")
            .hasMessageContaining("-DCOSMOS.ENCRYPTION_MIXED_ARRAYS_ENABLED=false")
            .hasMessageContaining("COSMOS_ENCRYPTION_MIXED_ARRAYS_ENABLED=false")
            .hasMessageNotContaining("secret")
            .hasMessageNotContaining("LEGACY_SECRET!")
            .hasMessageNotContaining("YWJjZA==");

        EncryptionProcessor legacyReader = createEncryptionProcessor(false);
        JsonNode decryptedDocument = legacyReader.decrypt(storedDocument,
            CosmosItemSerializer.DEFAULT_SERIALIZER).block().getRight();
        assertThat(decryptedDocument.get("sensitiveString").toString())
            .isEqualTo(OBJECT_MAPPER.readTree(arrayJson).toString());
    }

    @DataProvider(name = "mixedArrayModes")
    public Object[][] mixedArrayModes() {
        return new Object[][] {{true}, {false}};
    }

    @Test(groups = {"unit"}, dataProvider = "mixedArrayModes", timeOut = TIMEOUT)
    public void decryptArrayPreservesAuthenticationFailure(boolean mixedArraysEnabled) throws Exception {
        EncryptionProcessor encryptionProcessor = createEncryptionProcessor(mixedArraysEnabled);
        EncryptionSettings settings = Mockito.mock(EncryptionSettings.class);
        AeadAes256CbcHmac256EncryptionAlgorithm algorithm = Mockito.mock(AeadAes256CbcHmac256EncryptionAlgorithm.class);
        Mockito.when(settings.getAeadAes256CbcHmac256EncryptionAlgorithm()).thenReturn(algorithm);
        MicrosoftDataEncryptionException authenticationFailure =
            new MicrosoftDataEncryptionException("Authentication failed");
        Mockito.when(algorithm.decrypt(Mockito.any(byte[].class))).thenThrow(authenticationFailure);
        JsonNode array = OBJECT_MAPPER.readTree("[\"AQID\"]");

        assertThatThrownBy(() -> encryptionProcessor.decryptAndSerializeProperty(settings, null, array,
            "sensitiveString"))
            .isInstanceOf(IllegalStateException.class)
            .hasCause(authenticationFailure)
            .hasMessageContaining("check the encryption key and ciphertext integrity");
    }

    @DataProvider(name = "legacyArrayWrites")
    public Object[][] legacyArrayWrites() {
        String objectFirst = "[{\"label\":\"secret\"},42,\"LEGACY_SECRET\",true,null,[\"nested\"],"
            + "{\"tail\":\"secret\"}]";
        String nestedArray = "{\"nested\":[[\"secret\"],42,\"LEGACY_SECRET\",null]}";
        return new Object[][] {
            {objectFirst, false}, {objectFirst, true},
            {nestedArray, false}, {nestedArray, true},
            {"[\"secret\",42,true]", false}, {"[\"secret\",42,true]", true}
        };
    }

    @Test(groups = {"unit"}, dataProvider = "legacyArrayWrites", timeOut = TIMEOUT)
    public void legacyOptOutAppliesToItemAndPatchWrites(String valueJson, boolean patch) throws Exception {
        EncryptionProcessor legacyProcessor = createEncryptionProcessor(false);
        JsonNode originalValue = OBJECT_MAPPER.readTree(valueJson);
        ObjectNode document = OBJECT_MAPPER.createObjectNode();
        document.put("id", "legacyItem");
        document.set("sensitiveString", originalValue.deepCopy());

        if (patch) {
            document.set("sensitiveString",
                legacyProcessor.encryptPatchNode(document.get("sensitiveString"), "/sensitiveString").block());
        } else {
            document = (ObjectNode) OBJECT_MAPPER.readTree(legacyProcessor.encrypt(document).block());
        }

        JsonNode encryptedValue = document.get("sensitiveString");
        JsonNode encryptedArray = encryptedValue.isObject() ? encryptedValue.get("nested") : encryptedValue;
        if (originalValue.isObject() || originalValue.get(0).isContainerNode()) {
            assertThat(encryptedArray.get(1).asInt()).isEqualTo(42);
            assertThat(encryptedArray.get(2).asText()).isEqualTo("LEGACY_SECRET");
        } else {
            assertThat(encryptedArray.get(0).asText()).isNotEqualTo("secret");
        }

        JsonNode decryptedDocument = legacyProcessor.decrypt(OBJECT_MAPPER.writeValueAsBytes(document),
            CosmosItemSerializer.DEFAULT_SERIALIZER).block().getRight();
        assertThat(decryptedDocument.get("sensitiveString").toString()).isEqualTo(originalValue.toString());
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void migrateLegacyMixedArrayWithOptOut() throws Exception {
        EncryptionProcessor legacyProcessor = createEncryptionProcessor(false);
        EncryptionProcessor fixedProcessor = createEncryptionProcessor(true);
        ObjectNode document = (ObjectNode) OBJECT_MAPPER.readTree(
            "{\"id\":\"migrateItem\",\"sensitiveString\":[{\"label\":\"secret\"},42,\"LEGACY_SECRET\"]}");
        String expectedDocument = document.toString();
        byte[] legacyBytes = legacyProcessor.encrypt(document).block();
        JsonNode decryptedLegacyDocument = legacyProcessor.decrypt(legacyBytes,
            CosmosItemSerializer.DEFAULT_SERIALIZER).block().getRight();

        byte[] migratedBytes = fixedProcessor.encrypt(decryptedLegacyDocument).block();
        assertThat(OBJECT_MAPPER.readTree(migratedBytes).toString()).doesNotContain("secret", "LEGACY_SECRET");
        assertThat(OBJECT_MAPPER.readTree(migratedBytes).get("sensitiveString").get(1).isTextual()).isTrue();
        JsonNode decryptedMigratedDocument = fixedProcessor.decrypt(migratedBytes,
            CosmosItemSerializer.DEFAULT_SERIALIZER).block().getRight();
        assertThat(decryptedMigratedDocument.toString()).isEqualTo(expectedDocument);
    }

    @DataProvider(name = "legacyUnsupportedArrays")
    public Object[][] legacyUnsupportedArrays() {
        return new Object[][] {
            {"[42,{\"label\":\"secret\"}]", false}, {"[42,{\"label\":\"secret\"}]", true},
            {"[\"secret\",[42]]", false}, {"[\"secret\",[42]]", true},
            {"[null]", false}, {"[null]", true}
        };
    }

    @Test(groups = {"unit"}, dataProvider = "legacyUnsupportedArrays", timeOut = TIMEOUT)
    public void legacyOptOutPreservesUnsupportedArrayFailures(String valueJson, boolean patch) throws Exception {
        EncryptionProcessor legacyProcessor = createEncryptionProcessor(false);
        JsonNode value = OBJECT_MAPPER.readTree(valueJson);
        if (patch) {
            assertThatThrownBy(() -> legacyProcessor.encryptPatchNode(value, "/sensitiveString").block())
                .hasMessageContaining("Invalid or Unsupported Data Type");
        } else {
            ObjectNode document = OBJECT_MAPPER.createObjectNode();
            document.put("id", "legacyItem");
            document.set("sensitiveString", value);
            assertThatThrownBy(() -> legacyProcessor.encrypt(document).block())
                .hasMessageContaining("Invalid or Unsupported Data Type");
        }
    }

    @DataProvider(name = "mixedArrayConfigurationValues")
    public Object[][] mixedArrayConfigurationValues() {
        return new Object[][] {
            {null, null, true}, {"", "", true},
            {"false", null, false}, {null, "false", false}, {"", "false", false},
            {"true", "false", true}, {"false", "true", false},
            {"FALSE", null, false}, {null, "TRUE", true}
        };
    }

    @Test(groups = {"unit"}, dataProvider = "mixedArrayConfigurationValues", timeOut = TIMEOUT)
    public void mixedArrayConfigurationPrecedence(String systemProperty, String environmentVariable, boolean expected) {
        assertThat(EncryptionConfigs.isMixedArrayEncryptionEnabled(systemProperty, environmentVariable))
            .isEqualTo(expected);
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void invalidMixedArrayConfigurationFailsExplicitly() {
        assertThatThrownBy(() -> EncryptionConfigs.isMixedArrayEncryptionEnabled("tru", "false"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Only 'true' or 'false'");
        assertThatThrownBy(() -> EncryptionConfigs.isMixedArrayEncryptionEnabled(null, "0"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(EncryptionConfigs.MIXED_ARRAYS_ENABLED_VARIABLE);
    }

    private EncryptionProcessor createEncryptionProcessor(boolean mixedArraysEnabled) throws JsonProcessingException {
        String previousValue = System.getProperty(EncryptionConfigs.MIXED_ARRAYS_ENABLED);
        try {
            System.setProperty(EncryptionConfigs.MIXED_ARRAYS_ENABLED, Boolean.toString(mixedArraysEnabled));
            return createEncryptionProcessor();
        } finally {
            if (previousValue == null) {
                System.clearProperty(EncryptionConfigs.MIXED_ARRAYS_ENABLED);
            } else {
                System.setProperty(EncryptionConfigs.MIXED_ARRAYS_ENABLED, previousValue);
            }
        }
    }

    private EncryptionProcessor createEncryptionProcessor() throws JsonProcessingException {
        CosmosAsyncContainer cosmosAsyncContainer = Mockito.mock(CosmosAsyncContainer.class);
        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = Mockito.mock(CosmosEncryptionAsyncClient.class);
        Mockito.when(cosmosEncryptionAsyncClientAccessor.isMixedArrayEncryptionEnabled(cosmosEncryptionAsyncClient))
            .thenReturn(EncryptionConfigs.isMixedArrayEncryptionEnabled());
        Mockito.when(cosmosEncryptionAsyncClient.getKeyEncryptionKeyResolver()).thenReturn(keyEncryptionKeyResolver);
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getContainerPropertiesAsync(cosmosEncryptionAsyncClient,
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateContainerWithCosmosEncryptionPolicy()));
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getClientEncryptionPropertiesAsync(cosmosEncryptionAsyncClient,
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(CosmosAsyncContainer.class), Mockito.anyBoolean(), Mockito.nullable(String.class), Mockito.anyBoolean())).thenReturn(Mono.just(generateClientEncryptionKeyProperties()));
        EncryptionKeyStoreProviderImpl encryptionKeyStoreProvider = new EncryptionKeyStoreProviderImpl(keyEncryptionKeyResolver, "TEST_KEY_RESOLVER");
        Mockito.when(cosmosEncryptionAsyncClientAccessor.getEncryptionKeyStoreProviderImpl(cosmosEncryptionAsyncClient)).thenReturn(encryptionKeyStoreProvider);
        return new EncryptionProcessor(cosmosAsyncContainer, cosmosEncryptionAsyncClient);
    }
}
