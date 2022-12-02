// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.cosmosclient

import com.azure.cosmos.CosmosAsyncClient

private[spark] case class CosmosClientProvider (cosmosAsyncClient: CosmosAsyncClient)
