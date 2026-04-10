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
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosClientEncryptionKeyResponse;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosEncryptionAsyncClientUnitTest {

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

    @Test(groups = {"unit"}, timeOut = TestSuiteBase.TIMEOUT)
    public void encryptionClientAppendsUserAgentSuffix() {
        // Setup: mock CosmosAsyncClient with a real AsyncDocumentClient to verify UA suffix
        CosmosAsyncClient mockAsyncClient = Mockito.mock(CosmosAsyncClient.class);
        AsyncDocumentClient mockDocClient = Mockito.mock(AsyncDocumentClient.class);
        KeyEncryptionKeyResolver mockKeyResolver = Mockito.mock(KeyEncryptionKeyResolver.class);

        org.mockito.MockedStatic<CosmosBridgeInternal> bridgeMock = Mockito.mockStatic(CosmosBridgeInternal.class);
        try {
            bridgeMock.when(() -> CosmosBridgeInternal.getAsyncDocumentClient(mockAsyncClient))
                .thenReturn(mockDocClient);

            new CosmosEncryptionAsyncClient(mockAsyncClient, mockKeyResolver, "TEST_KEY_RESOLVER");

            // Verify appendUserAgentSuffix was called with the encryption SDK suffix
            Mockito.verify(mockDocClient, Mockito.times(1))
                .appendUserAgentSuffix(Constants.USER_AGENT_SUFFIX);
        } finally {
            bridgeMock.close();
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
