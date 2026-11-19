// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.core.cryptography.KeyEncryptionKeyResolver;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncClientEncryptionKey;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.encryption.implementation.Constants;
import com.azure.cosmos.encryption.implementation.EncryptionProcessor;
import com.azure.cosmos.encryption.implementation.EncryptionSettings;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosClientEncryptionKeyResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosEncryptionAsyncClientUnitTest {
    private static final String MIXED_ARRAYS_ENABLED_PROPERTY = "COSMOS.ENCRYPTION_MIXED_ARRAYS_ENABLED";

    private final static ImplementationBridgeHelpers.CosmosAsyncClientEncryptionKeyHelper.CosmosAsyncClientEncryptionKeyAccessor cosmosAsyncClientEncryptionKeyAccessor = ImplementationBridgeHelpers.CosmosAsyncClientEncryptionKeyHelper.getCosmosAsyncClientEncryptionKeyAccessor();

    @Test(groups = {"unit"}, timeOut = TestSuiteBase.TIMEOUT)
    public void clientEncryptionPropertiesAsync() {
        CosmosAsyncClient mockAsyncClient = Mockito.mock(CosmosAsyncClient.class);
        KeyEncryptionKeyResolver mockKeyEncryptionKeyResolver = Mockito.mock(KeyEncryptionKeyResolver.class);
        CosmosAsyncContainer mockCosmosAsyncContainer = Mockito.mock(CosmosAsyncContainer.class);
        CosmosAsyncDatabase mockCosmosAsyncDatabase = Mockito.mock(CosmosAsyncDatabase.class);
        CosmosAsyncClientEncryptionKey mockCosmosAsyncClientEncryptionKey = Mockito.mock(CosmosAsyncClientEncryptionKey.class);
        CosmosClientEncryptionKeyResponse mockKeyResponse = Mockito.mock(CosmosClientEncryptionKeyResponse.class);
        CosmosClientEncryptionKeyProperties mockKeyProperties = Mockito.mock(CosmosClientEncryptionKeyProperties.class);
        Mockito.when(mockCosmosAsyncContainer.getDatabase()).thenReturn(mockCosmosAsyncDatabase);
        Mockito.when(mockCosmosAsyncDatabase.getClientEncryptionKey(Mockito.anyString())).thenReturn(mockCosmosAsyncClientEncryptionKey);
        Mockito.when(cosmosAsyncClientEncryptionKeyAccessor.readClientEncryptionKey(mockCosmosAsyncClientEncryptionKey, Mockito.any(RequestOptions.class))).thenReturn(Mono.just(mockKeyResponse));
        Mockito.when(mockKeyResponse.getProperties()).thenReturn(mockKeyProperties);

        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = new CosmosEncryptionAsyncClient(mockAsyncClient, mockKeyEncryptionKeyResolver, "test");
        CosmosEncryptionAsyncClient spyEncryptionAsyncClient = Mockito.spy(cosmosEncryptionAsyncClient);
        spyEncryptionAsyncClient.getClientEncryptionPropertiesAsync("testKey", "testDB", mockCosmosAsyncContainer, false, null, false).block();
        Mockito.verify(spyEncryptionAsyncClient, Mockito.times(1)).fetchClientEncryptionKeyPropertiesAsync(Mockito.any(CosmosAsyncContainer.class), Mockito.anyString(), Mockito.any(RequestOptions.class));

        cosmosEncryptionAsyncClient = new CosmosEncryptionAsyncClient(mockAsyncClient, mockKeyEncryptionKeyResolver, "test");
        spyEncryptionAsyncClient = Mockito.spy(cosmosEncryptionAsyncClient);
        spyEncryptionAsyncClient.getClientEncryptionPropertiesAsync("testKey", "testDB", mockCosmosAsyncContainer, true, null, false).block();
        Mockito.verify(spyEncryptionAsyncClient, Mockito.times(2)).fetchClientEncryptionKeyPropertiesAsync(Mockito.any(CosmosAsyncContainer.class), Mockito.anyString(), Mockito.any(RequestOptions.class));

        cosmosEncryptionAsyncClient = new CosmosEncryptionAsyncClient(mockAsyncClient, mockKeyEncryptionKeyResolver, "test");
        spyEncryptionAsyncClient = Mockito.spy(cosmosEncryptionAsyncClient);
        spyEncryptionAsyncClient.getClientEncryptionPropertiesAsync("testKey", "testDB", mockCosmosAsyncContainer, false, null, true).block();
        Mockito.verify(spyEncryptionAsyncClient, Mockito.times(2)).fetchClientEncryptionKeyPropertiesAsync(Mockito.any(CosmosAsyncContainer.class), Mockito.anyString(), Mockito.any(RequestOptions.class));

        cosmosEncryptionAsyncClient = new CosmosEncryptionAsyncClient(mockAsyncClient, mockKeyEncryptionKeyResolver, "test");
        spyEncryptionAsyncClient = Mockito.spy(cosmosEncryptionAsyncClient);
        spyEncryptionAsyncClient.getClientEncryptionPropertiesAsync("testKey", "testDB", mockCosmosAsyncContainer, true, null, true).block();
        Mockito.verify(spyEncryptionAsyncClient, Mockito.times(2)).fetchClientEncryptionKeyPropertiesAsync(Mockito.any(CosmosAsyncContainer.class), Mockito.anyString(), Mockito.any(RequestOptions.class));
    }

    @DataProvider(name = "mixedArrayUserAgentSettings")
    public Object[][] mixedArrayUserAgentSettings() {
        return new Object[][] {{null, false}, {"true", false}, {"false", true}};
    }

    @Test(groups = {"unit"}, dataProvider = "mixedArrayUserAgentSettings", timeOut = TestSuiteBase.TIMEOUT)
    public void encryptionClientAppendsUserAgentSuffix(String setting, boolean optOutExpected) throws Exception {
        CosmosAsyncClient mockAsyncClient = Mockito.mock(CosmosAsyncClient.class);
        RxDocumentClientImpl mockDocClient = Mockito.mock(RxDocumentClientImpl.class);
        KeyEncryptionKeyResolver mockKeyResolver = Mockito.mock(KeyEncryptionKeyResolver.class);
        UserAgentContainer userAgent = new UserAgentContainer();
        userAgent.setSuffix("customer-app");
        FieldUtils.writeField(mockDocClient, "userAgentContainer", userAgent, true);
        Mockito.doCallRealMethod().when(mockDocClient).appendUserAgentSuffix(Mockito.anyString());
        String previousValue = System.getProperty(MIXED_ARRAYS_ENABLED_PROPERTY);

        org.mockito.MockedStatic<CosmosBridgeInternal> bridgeMock = Mockito.mockStatic(CosmosBridgeInternal.class);
        try {
            if (setting == null) {
                System.clearProperty(MIXED_ARRAYS_ENABLED_PROPERTY);
            } else {
                System.setProperty(MIXED_ARRAYS_ENABLED_PROPERTY, setting);
            }
            bridgeMock.when(() -> CosmosBridgeInternal.getAsyncDocumentClient(mockAsyncClient))
                .thenReturn(mockDocClient);

            new CosmosEncryptionAsyncClient(mockAsyncClient, mockKeyResolver, "TEST_KEY_RESOLVER");
            new CosmosEncryptionAsyncClient(mockAsyncClient, mockKeyResolver, "TEST_KEY_RESOLVER");

            assertThat(userAgent.getUserAgent())
                .contains("customer-app", Constants.USER_AGENT_SUFFIX);
            assertThat(userAgent.getSuffix().split("\\s+"))
                .containsOnlyOnce("customer-app", Constants.USER_AGENT_SUFFIX);
            if (optOutExpected) {
                assertThat(userAgent.getSuffix().split("\\s+")).containsOnlyOnce("encryption-mixed-arrays-disabled");
            } else {
                assertThat(userAgent.getUserAgent()).doesNotContain("encryption-mixed-arrays-disabled");
            }
        } finally {
            bridgeMock.close();
            restoreMixedArraySetting(previousValue);
        }
    }

    @Test(groups = {"unit"}, timeOut = TestSuiteBase.TIMEOUT)
    public void encryptionOptOutSnapshotMatchesUserAgent() throws Exception {
        CosmosAsyncClient mockAsyncClient = Mockito.mock(CosmosAsyncClient.class);
        AsyncDocumentClient mockDocClient = Mockito.mock(AsyncDocumentClient.class);
        KeyEncryptionKeyResolver mockKeyResolver = Mockito.mock(KeyEncryptionKeyResolver.class);
        String previousValue = System.getProperty(MIXED_ARRAYS_ENABLED_PROPERTY);
        try (org.mockito.MockedStatic<CosmosBridgeInternal> bridgeMock = Mockito.mockStatic(CosmosBridgeInternal.class)) {
            bridgeMock.when(() -> CosmosBridgeInternal.getAsyncDocumentClient(mockAsyncClient))
                .thenReturn(mockDocClient);
            System.setProperty(MIXED_ARRAYS_ENABLED_PROPERTY, "false");
            CosmosEncryptionAsyncClient client =
                new CosmosEncryptionAsyncClient(mockAsyncClient, mockKeyResolver, "TEST_KEY_RESOLVER");
            System.setProperty(MIXED_ARRAYS_ENABLED_PROPERTY, "true");

            EncryptionProcessor processor = new EncryptionProcessor(Mockito.mock(CosmosAsyncContainer.class), client);
            JsonNode array = new ObjectMapper().readTree("[{},42]");
            JsonNode encryptedArray = processor.encryptAndSerializePatchProperty(
                Mockito.mock(EncryptionSettings.class), array, "sensitiveString");

            assertThat(client.isMixedArrayEncryptionEnabled()).isFalse();
            assertThat(encryptedArray.get(1).asInt()).isEqualTo(42);
            Mockito.verify(mockDocClient).appendUserAgentSuffix(Constants.MIXED_ARRAYS_DISABLED_USER_AGENT_SUFFIX);
        } finally {
            restoreMixedArraySetting(previousValue);
        }
    }

    private void restoreMixedArraySetting(String previousValue) {
        if (previousValue == null) {
            System.clearProperty(MIXED_ARRAYS_ENABLED_PROPERTY);
        } else {
            System.setProperty(MIXED_ARRAYS_ENABLED_PROPERTY, previousValue);
        }
    }

    @Test(groups = {"unit"}, timeOut = TestSuiteBase.TIMEOUT)
    public void encryptionUserAgentSuffixContainsVersionInfo() {
        // Verify the suffix constants are properly loaded from properties
        assertThat(Constants.CURRENT_NAME).isNotEmpty();
        assertThat(Constants.CURRENT_VERSION).isNotEmpty();
        assertThat(Constants.USER_AGENT_SUFFIX).isEqualTo(Constants.CURRENT_NAME + "/" + Constants.CURRENT_VERSION);
        assertThat(Constants.USER_AGENT_SUFFIX).startsWith("azure-cosmos-encryption/");
    }

    @Test(groups = {"unit"}, timeOut = TestSuiteBase.TIMEOUT)
    public void encryptionClientHandlesAppendFailureGracefully() {
        // If getAsyncDocumentClient throws, encryption client should still be created
        CosmosAsyncClient mockAsyncClient = Mockito.mock(CosmosAsyncClient.class);
        KeyEncryptionKeyResolver mockKeyResolver = Mockito.mock(KeyEncryptionKeyResolver.class);

        org.mockito.MockedStatic<CosmosBridgeInternal> bridgeMock = Mockito.mockStatic(CosmosBridgeInternal.class);
        try {
            bridgeMock.when(() -> CosmosBridgeInternal.getAsyncDocumentClient(mockAsyncClient))
                .thenThrow(new RuntimeException("simulated failure"));

            // Should not throw — the failure is caught and logged
            CosmosEncryptionAsyncClient client =
                new CosmosEncryptionAsyncClient(mockAsyncClient, mockKeyResolver, "TEST_KEY_RESOLVER");
            assertThat(client).isNotNull();
            assertThat(client.getCosmosAsyncClient()).isSameAs(mockAsyncClient);
        } finally {
            bridgeMock.close();
        }
    }
}
