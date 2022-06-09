// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.core.cryptography.KeyEncryptionKeyResolver;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncClientEncryptionKey;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosClientEncryptionKeyResponse;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

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
}
