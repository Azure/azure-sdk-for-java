// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.models;

import com.azure.cosmos.encryption.CosmosEncryptionAsyncContainer;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.Warning;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import reactor.core.publisher.Mono;
import java.util.HashMap;

import static com.azure.cosmos.implementation.Warning.INTERNAL_USE_ONLY_WARNING;

/**
 * This is an internal class in the encryption project.
 * This is meant to be used only internally as a bridge access to classes in implementation.
 */
@Warning(value = INTERNAL_USE_ONLY_WARNING)
public class EncryptionModelBridgeInternal {

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static HashMap<String, SqlParameter> getEncryptionParamMap(SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption) {
        return sqlQuerySpecWithEncryption.getEncryptionParamMap();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Mono<Void> addEncryptionParameterAsync(SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption, String path, SqlParameter sqlParameter, CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer){
        return sqlQuerySpecWithEncryption.addEncryptionParameterAsync(path, sqlParameter, cosmosEncryptionAsyncContainer);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static SqlQuerySpec getSqlQuerySpec(SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption){
        return sqlQuerySpecWithEncryption.getSqlQuerySpec();
    }
}
