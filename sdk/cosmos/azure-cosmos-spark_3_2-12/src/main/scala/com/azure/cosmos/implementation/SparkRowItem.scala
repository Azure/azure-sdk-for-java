// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation

import org.apache.spark.sql.Row

private[cosmos] case class SparkRowItem
(
  row: Row
)
