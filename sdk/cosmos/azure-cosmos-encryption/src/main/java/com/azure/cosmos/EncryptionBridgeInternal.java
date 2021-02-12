// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.Warning;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.Warning.INTERNAL_USE_ONLY_WARNING;

public class EncryptionBridgeInternal {

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Mono<ClientEncryptionPolicy> getClientEncryptionPolicyAsync(EncryptionCosmosAsyncClient encryptionCosmosAsyncClient,
                                                                              CosmosAsyncContainer cosmosAsyncContainer,
                                                                              boolean shouldForceRefresh) {
        return encryptionCosmosAsyncClient.getClientEncryptionPolicyAsync(cosmosAsyncContainer, shouldForceRefresh);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Mono<CosmosClientEncryptionKeyProperties> getClientEncryptionPropertiesAsync(EncryptionCosmosAsyncClient encryptionCosmosAsyncClient,
                                                                                               String clientEncryptionKeyId,
                                                                                               CosmosAsyncContainer cosmosAsyncContainer,
                                                                                               boolean shouldForceRefresh) {
        return encryptionCosmosAsyncClient.getClientEncryptionPropertiesAsync(clientEncryptionKeyId,
            cosmosAsyncContainer, shouldForceRefresh);
    }
}
