// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.models.PartitionKey

private[spark] case class ReadManyFilter(partitionKey: PartitionKey, value: String)
