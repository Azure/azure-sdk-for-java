// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import org.apache.spark.sql.types.StructType

import scala.collection.immutable.Map

private[spark] case class ViewDefinition
(
  databaseName: String,
  viewName: String,
  userProvidedSchema: Option[StructType],
  options: Map[String, String]
)
