// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.encryption.implementation.EncryptionProcessor;
import com.azure.cosmos.implementation.Warning;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosContainerProperties;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.Warning.INTERNAL_USE_ONLY_WARNING;

public class EncryptionBridgeInternal {
    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Mono<CosmosContainerProperties> getContainerPropertiesMono(CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient,
                                                                                 CosmosAsyncContainer cosmosAsyncContainer,
                                                                                 boolean shouldForceRefresh) {
        return cosmosEncryptionAsyncClient.getContainerPropertiesAsync(cosmosAsyncContainer, shouldForceRefresh);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Mono<CosmosClientEncryptionKeyProperties> getClientEncryptionPropertiesAsync(CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient,
                                                                                               String clientEncryptionKeyId,
                                                                                               String databaseRid,
                                                                                               CosmosAsyncContainer cosmosAsyncContainer,
                                                                                               boolean shouldForceRefresh) {
        return cosmosEncryptionAsyncClient.getClientEncryptionPropertiesAsync(clientEncryptionKeyId, databaseRid,
            cosmosAsyncContainer, shouldForceRefresh);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static EncryptionProcessor getEncryptionProcessor(CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer) {
        return cosmosEncryptionAsyncContainer.getEncryptionProcessor();
    }
}
