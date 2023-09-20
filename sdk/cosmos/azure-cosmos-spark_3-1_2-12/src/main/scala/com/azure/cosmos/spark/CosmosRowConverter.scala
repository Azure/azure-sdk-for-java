// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper

import scala.collection.concurrent.TrieMap

// scalastyle:off
private[cosmos] object CosmosRowConverter {

  // TODO: Expose configuration to handle duplicate fields
  // See: https://github.com/Azure/azure-sdk-for-java/pull/18642#discussion_r558638474
  private val rowConverterMap = new TrieMap[CosmosSerializationConfig, CosmosRowConverter]

  def get(serializationConfig: CosmosSerializationConfig) : CosmosRowConverter = {
    rowConverterMap.get(serializationConfig) match {
      case Some(existingRowConverter) => existingRowConverter
      case None =>
        val newRowConverterCandidate = createRowConverter(serializationConfig)
        rowConverterMap.putIfAbsent(serializationConfig, newRowConverterCandidate) match {
          case Some(existingConcurrentlyCreatedRowConverter) => existingConcurrentlyCreatedRowConverter
          case None => newRowConverterCandidate
        }
    }
  }

  private def createRowConverter(serializationConfig: CosmosSerializationConfig): CosmosRowConverter = {
    val objectMapper = new ObjectMapper()
    serializationConfig.serializationInclusionMode match {
      case SerializationInclusionModes.NonNull => objectMapper.setSerializationInclusion(Include.NON_NULL)
      case SerializationInclusionModes.NonEmpty => objectMapper.setSerializationInclusion(Include.NON_EMPTY)
      case SerializationInclusionModes.NonDefault => objectMapper.setSerializationInclusion(Include.NON_DEFAULT)
      case _ => objectMapper.setSerializationInclusion(Include.ALWAYS)
    }

    new CosmosRowConverter(objectMapper, serializationConfig)
  }
}

private[cosmos] class CosmosRowConverter(private val objectMapper: ObjectMapper, private val serializationConfig: CosmosSerializationConfig)
    extends CosmosRowConverterBase(objectMapper, serializationConfig)
