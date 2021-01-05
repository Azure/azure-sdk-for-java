// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation

import com.azure.cosmos.CosmosClientBuilder
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosClientBuilderHelper

object SparkBridgeInternal {
  def setMetadataCacheSnapshot(cosmosClientBuilder: CosmosClientBuilder, metadataCache: CosmosClientMetadataCachesSnapshot): Unit = {
    val clientBuilderAccessor = CosmosClientBuilderHelper.getCosmosClientBuilderAccessor()
    clientBuilderAccessor.setCosmosClientMetadataCachesSnapshot(cosmosClientBuilder, metadataCache)
  }
}
