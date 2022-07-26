// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot

@SerialVersionUID(100L)
private[cosmos] case class CosmosClientMetadataCachesSnapshots(
                                          cosmosClientMetadataCaches: CosmosClientMetadataCachesSnapshot,
                                          throughputControlClientMetadataCaches: Option[CosmosClientMetadataCachesSnapshot]) extends Serializable {
 override def toString =
  s"cosmosClientMetadata is ${cosmosClientMetadataCaches.toString}, throughputControlClientMetadata is ${throughputControlClientMetadataCaches.toString}"

}
