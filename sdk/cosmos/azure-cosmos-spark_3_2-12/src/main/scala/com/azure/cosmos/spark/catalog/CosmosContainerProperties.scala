// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.catalog

private[spark] object CosmosContainerProperties {
  val OnlySystemPropertiesIndexingPolicyName: String = "OnlySystemProperties"
  val AllPropertiesIndexingPolicyName: String = "AllProperties"

  private val partitionKeyPath = "partitionKeyPath"
  private val partitionKeyVersion = "partitionKeyVersion"
  private val indexingPolicy = "indexingPolicy"
  private val defaultTtlPropertyName = "defaultTtlInSeconds"
  private val analyticalStoreTtlPropertyName = "analyticalStoreTtlInSeconds"
  private val defaultPartitionKeyPath = "/id"
  private val defaultIndexingPolicy = AllPropertiesIndexingPolicyName

  def getPartitionKeyPath(properties: Map[String, String]): String = {
    properties.getOrElse(partitionKeyPath, defaultPartitionKeyPath)
  }

  def getPartitionKeyVersion(properties: Map[String, String]): Option[String] = {
    properties.get(partitionKeyVersion)
  }

  def getIndexingPolicy(properties: Map[String, String]): String = {
    properties.getOrElse(indexingPolicy, defaultIndexingPolicy)
  }

  def getDefaultTtlInSeconds(properties: Map[String, String]): Option[Int] = {
    if (properties.contains(defaultTtlPropertyName)) {
      Some(properties(defaultTtlPropertyName).toInt)
    } else {
      None
    }
  }

  def getAnalyticalStoreTtlInSeconds(properties: Map[String, String]): Option[Int] = {
    if (properties.contains(analyticalStoreTtlPropertyName)) {
      Some(properties(analyticalStoreTtlPropertyName).toInt)
    } else {
      None
    }
  }
}
