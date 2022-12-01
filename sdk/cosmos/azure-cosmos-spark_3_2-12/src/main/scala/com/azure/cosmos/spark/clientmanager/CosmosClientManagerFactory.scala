// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.clientmanager

import com.azure.cosmos.spark.{CosmosAadAuthConfig, CosmosAuthConfig, CosmosMasterKeyAuthConfig}

object CosmosClientManagerFactory {
    private val masterKeyAuthClientManager = new CosmosMasterKeyAuthClientManager()
    private val aadAuthClientManager = new CosmosAadAuthClientManager()

    def getCosmosClientManager(config: Map[String, String]): CosmosClientManager = {
        val authConfig = CosmosAuthConfig.parseCosmosAuthConfig(config)
        authConfig match {
            case _: CosmosMasterKeyAuthConfig => masterKeyAuthClientManager
            case _: CosmosAadAuthConfig => aadAuthClientManager
            case _ => throw new IllegalArgumentException(s"Auth type ${authConfig.getClass} is not supported")
        }
    }
}
