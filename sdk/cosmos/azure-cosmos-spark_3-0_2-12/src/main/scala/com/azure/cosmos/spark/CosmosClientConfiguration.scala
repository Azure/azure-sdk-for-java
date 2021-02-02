// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.ConsistencyLevel
import com.azure.cosmos.BridgeInternal

private case class CosmosClientConfiguration (
    endpoint: String,
    key: String,
    consistencyLevel: ConsistencyLevel)

private object CosmosClientConfiguration {
    def apply(config: Map[String, String]): CosmosClientConfiguration = {
        val cosmosAccountConfig = CosmosAccountConfig.parseCosmosAccountConfig(config)
        val consistency = cosmosAccountConfig.consistency match {
            case Some(consistencyAsString) =>
                parseStringAsConsistencyLevel(consistencyAsString) match {
                    case Some(parsedConsistency) => parsedConsistency
                    case None => ConsistencyLevel.EVENTUAL
                }
            case None => ConsistencyLevel.EVENTUAL
        }
        CosmosClientConfiguration(
            cosmosAccountConfig.endpoint,
            cosmosAccountConfig.key,
            consistency)
    }

    private def parseStringAsConsistencyLevel(stringValue: String): Option[ConsistencyLevel] = {
        try {
            val result = BridgeInternal.fromServiceSerializedFormat(stringValue)
            Some(result)
        }
        catch {
            case e: IllegalArgumentException =>
                // ignore the exception and return the default
                None
        }

    }
}
