// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{ImplementationBridgeHelpers, Utils}
import com.azure.cosmos.implementation.routing.PartitionKeyInternal
import com.azure.cosmos.models.{CosmosItemIdentity, ModelBridgeInternal, PartitionKey}

private object CosmosItemIdentityHelper {
    // pattern will be recognized
    // 1. id(idValue).pk(partitionKeyValue)
    //
    // (?i) : The whole matching is case-insensitive
    // id[(](.*?)[)]: id value
    // [.]pk[(](.*)[)]: partitionKey Value
    private val cosmosItemIdentityStringRegx = """(?i)id[(](.*?)[)][.]pk[(](.*)[)]""".r
    def getCosmosItemIdentityValueString(id: String, partitionKey: PartitionKey): String = {
        val internalPartitionKey = ModelBridgeInternal.getPartitionKeyInternal(partitionKey)
        s"id($id).pk(${Utils.getSimpleObjectMapper.writeValueAsString(internalPartitionKey)})"
    }

    def tryParseCosmosItemIdentity(cosmosItemIdentityString: String): Option[CosmosItemIdentity] = {
        cosmosItemIdentityString match {
            case cosmosItemIdentityStringRegx(idValue, pkValue) =>
                val internalPartitionKey = Utils.parse(pkValue, classOf[PartitionKeyInternal])
                Some(
                    new CosmosItemIdentity(
                        ImplementationBridgeHelpers
                            .PartitionKeyHelper
                            .getPartitionKeyAccessor
                            .toPartitionKey(internalPartitionKey),
                        idValue)
                )
            case _ => None
        }
    }
}
