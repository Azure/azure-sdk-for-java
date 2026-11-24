// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models

import com.azure.cosmos.implementation.Utils

private[cosmos] object SparkModelBridgeInternal {
  private val objectMapper = Utils.getSimpleObjectMapper

  def createIndexingPolicyFromJson(json: String): IndexingPolicy = {
    new IndexingPolicy(json)
  }

  def createPartitionKeyDefinitionFromJson(json: String): PartitionKeyDefinition = {
    new PartitionKeyDefinition(json)
  }

  def createVectorEmbeddingPolicyFromJson(json: String): CosmosVectorEmbeddingPolicy = {
    try {
      objectMapper.readValue(json, classOf[CosmosVectorEmbeddingPolicy])
    } catch {
      case e: Exception =>
        throw new IllegalArgumentException(
          s"Failed to parse vectorEmbeddingPolicy JSON. Ensure the JSON is well-formed: ${e.getMessage}", e)
    }
  }

  def vectorEmbeddingPolicyToJson(policy: CosmosVectorEmbeddingPolicy): String = {
    objectMapper.writeValueAsString(policy)
  }
}
