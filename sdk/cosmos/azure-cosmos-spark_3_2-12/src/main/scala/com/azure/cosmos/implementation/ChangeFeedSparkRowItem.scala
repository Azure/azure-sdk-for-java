// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation

import com.azure.cosmos.models.PartitionKey
import org.apache.spark.sql.Row

private[cosmos] case class ChangeFeedSparkRowItem
(
  row: Row,
  pkValue: Option[PartitionKey],
  lsn: String
)
