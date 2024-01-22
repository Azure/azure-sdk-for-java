// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.udf

import com.azure.cosmos.spark.CosmosItemIdentityHelper
import com.azure.cosmos.spark.CosmosPredicates.{requireNotNull, requireNotNullOrEmpty}
import org.apache.spark.sql.api.java.UDF2

@SerialVersionUID(1L)
class GetCosmosItemIdentityValue extends UDF2[String, Object, String] {
  override def call
  (
    id: String,
    partitionKeyValue: Object
  ): String = {
    requireNotNullOrEmpty(id, "id")
    requireNotNull(partitionKeyValue, "partitionKeyValue")

    CosmosItemIdentityHelper.getCosmosItemIdentityValueString(id, partitionKeyValue)
  }
}
