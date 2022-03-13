// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.diagnostics

import com.azure.cosmos.models.PartitionKey

case class CosmosItemIdentifier(itemId: String, partitionKeyValue: PartitionKey)
