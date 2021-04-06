// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models

private[cosmos] object SparkModelBridgeInternal {
  def createIndexingPolicyFromJson(json: String): IndexingPolicy = {
    new IndexingPolicy(json)
  }
}
