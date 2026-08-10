// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.catalog

import org.apache.spark.sql.connector.catalog.{NamespaceChange, TableChange}

private[spark] object CosmosThroughputProperties {
  val manualThroughputFieldName = "manualThroughput"
  val autoScaleMaxThroughputName = "autoScaleMaxThroughput"

  def getManualThroughput(properties: Map[String, String]): Option[Int] = {
    if (properties.contains(manualThroughputFieldName)) {
      Some(properties(manualThroughputFieldName).toInt)
    } else {
      None
    }
  }

  def getAutoScaleMaxThroughput(properties: Map[String, String]): Option[Int] = {
    if (properties.contains(autoScaleMaxThroughputName)) {
      Some(properties(autoScaleMaxThroughputName).toInt)
    } else {
      None
    }
  }

  def isThroughputProperty(tableChange: TableChange): Boolean = {
    if (!tableChange.isInstanceOf[TableChange.SetProperty]) {
      false
    } else {
      isThroughputProperty(tableChange.asInstanceOf[TableChange.SetProperty].property())
    }
  }

  def isThroughputProperty(tableChange: NamespaceChange): Boolean = {
    if (!tableChange.isInstanceOf[NamespaceChange.SetProperty]) {
      false
    } else {
      isThroughputProperty(tableChange.asInstanceOf[NamespaceChange.SetProperty].property())
    }
  }

  private def isThroughputProperty(setPropertyName: String): Boolean = {
      autoScaleMaxThroughputName.equalsIgnoreCase(setPropertyName) ||
        manualThroughputFieldName.equalsIgnoreCase(setPropertyName)
  }
}
