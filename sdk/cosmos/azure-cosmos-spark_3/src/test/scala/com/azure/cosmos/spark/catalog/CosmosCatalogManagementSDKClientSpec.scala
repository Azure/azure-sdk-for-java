// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.catalog

import com.azure.cosmos.spark.UnitSpec
import com.azure.resourcemanager.cosmos.CosmosManager
import com.azure.resourcemanager.cosmos.fluent.CosmosDBManagementClient
import com.azure.resourcemanager.cosmos.fluent.SqlResourcesClient
import com.azure.resourcemanager.cosmos.models.{ContainerPartitionKey, PartitionKind}
import org.mockito.{MockMakers, Mockito}
import org.mockito.Mockito.{mock, when}

import scala.collection.JavaConverters._

class CosmosCatalogManagementSDKClientSpec extends UnitSpec {
  //scalastyle:off multiple.string.literals

  private val mockCosmosManager = mock(classOf[CosmosManager], Mockito.withSettings().mockMaker(MockMakers.INLINE))
  private val mockServiceClient = mock(classOf[CosmosDBManagementClient], Mockito.withSettings().mockMaker(MockMakers.INLINE))
  private val mockSqlResources = mock(classOf[SqlResourcesClient])
  when(mockCosmosManager.serviceClient()).thenReturn(mockServiceClient)
  when(mockServiceClient.getSqlResources()).thenReturn(mockSqlResources)

  private val client = CosmosCatalogManagementSDKClient(
    resourceGroupName = "rg",
    databaseAccountName = "account",
    cosmosManager = mockCosmosManager,
    cosmosAsyncClient = null
  )

  // --- Multi-path partition key tests ---

  "getPartitionKeyDefinition" should "default to MULTI_HASH with version 2 for multi-path without explicit kind or version" in {
    val props = Map("partitionKeyPath" -> "/tenantId,/userId,/sessionId")
    val result = client.getPartitionKeyDefinition(props)

    result.kind() shouldEqual PartitionKind.MULTI_HASH
    result.version() shouldEqual 2
    result.paths().asScala should contain theSameElementsInOrderAs List("/tenantId", "/userId", "/sessionId")
  }

  it should "succeed for multi-path with explicit MultiHash kind" in {
    val props = Map(
      "partitionKeyPath" -> "/a,/b",
      "partitionKeyKind" -> "MultiHash"
    )
    val result = client.getPartitionKeyDefinition(props)

    result.kind() shouldEqual PartitionKind.MULTI_HASH
    result.version() shouldEqual 2
    result.paths().asScala should contain theSameElementsInOrderAs List("/a", "/b")
  }

  it should "throw IllegalArgumentException for multi-path with Hash kind" in {
    val props = Map(
      "partitionKeyPath" -> "/a,/b",
      "partitionKeyKind" -> "Hash"
    )

    an[IllegalArgumentException] should be thrownBy client.getPartitionKeyDefinition(props)
  }

  it should "throw IllegalArgumentException for multi-path with V1 version" in {
    val props = Map(
      "partitionKeyPath" -> "/a,/b",
      "partitionKeyVersion" -> "V1"
    )

    an[IllegalArgumentException] should be thrownBy client.getPartitionKeyDefinition(props)
  }

  it should "succeed for multi-path with explicit V2 version" in {
    val props = Map(
      "partitionKeyPath" -> "/a,/b",
      "partitionKeyVersion" -> "V2"
    )
    val result = client.getPartitionKeyDefinition(props)

    result.kind() shouldEqual PartitionKind.MULTI_HASH
    result.version() shouldEqual 2
    result.paths().asScala should contain theSameElementsInOrderAs List("/a", "/b")
  }

  // --- Single-path partition key tests ---

  it should "default to HASH for single-path without explicit kind" in {
    val props = Map("partitionKeyPath" -> "/id")
    val result = client.getPartitionKeyDefinition(props)

    result.kind() shouldEqual PartitionKind.HASH
    result.paths().asScala should contain theSameElementsInOrderAs List("/id")
  }

  it should "throw IllegalArgumentException for single-path with MULTI_HASH kind" in {
    val props = Map(
      "partitionKeyPath" -> "/id",
      "partitionKeyKind" -> "MultiHash"
    )

    an[IllegalArgumentException] should be thrownBy client.getPartitionKeyDefinition(props)
  }

  it should "set version 1 for single-path with explicit V1 version" in {
    val props = Map(
      "partitionKeyPath" -> "/id",
      "partitionKeyVersion" -> "V1"
    )
    val result = client.getPartitionKeyDefinition(props)

    result.kind() shouldEqual PartitionKind.HASH
    result.version() shouldEqual 1
    result.paths().asScala should contain theSameElementsInOrderAs List("/id")
  }

  it should "set version 2 for single-path with explicit V2 version" in {
    val props = Map(
      "partitionKeyPath" -> "/id",
      "partitionKeyVersion" -> "V2"
    )
    val result = client.getPartitionKeyDefinition(props)

    result.kind() shouldEqual PartitionKind.HASH
    result.version() shouldEqual 2
    result.paths().asScala should contain theSameElementsInOrderAs List("/id")
  }

  // --- Default path test ---

  it should "use default partition key path /id when partitionKeyPath is not specified" in {
    val props = Map.empty[String, String]
    val result = client.getPartitionKeyDefinition(props)

    result.kind() shouldEqual PartitionKind.HASH
    result.paths().asScala should contain theSameElementsInOrderAs List("/id")
  }

  // --- Edge case: whitespace in paths ---

  it should "trim whitespace from multi-path partition key paths" in {
    val props = Map("partitionKeyPath" -> "/a , /b , /c")
    val result = client.getPartitionKeyDefinition(props)

    result.kind() shouldEqual PartitionKind.MULTI_HASH
    result.version() shouldEqual 2
    result.paths().asScala should contain theSameElementsInOrderAs List("/a", "/b", "/c")
  }

  // --- Edge case: trailing comma produces empty segment, filtered out ---

  it should "treat trailing comma as single-path after filtering empty segments" in {
    val props = Map("partitionKeyPath" -> "/a,")
    val result = client.getPartitionKeyDefinition(props)

    result.kind() shouldEqual PartitionKind.HASH
    result.paths().asScala should contain theSameElementsInOrderAs List("/a")
  }

  // --- Edge case: invalid partition key kind ---

  it should "throw IllegalArgumentException for invalid partition key kind on multi-path" in {
    val props = Map(
      "partitionKeyPath" -> "/a,/b",
      "partitionKeyKind" -> "Foo"
    )

    an[IllegalArgumentException] should be thrownBy client.getPartitionKeyDefinition(props)
  }

  it should "throw IllegalArgumentException for invalid partition key kind on single-path" in {
    val props = Map(
      "partitionKeyPath" -> "/a",
      "partitionKeyKind" -> "Foo"
    )

    an[IllegalArgumentException] should be thrownBy client.getPartitionKeyDefinition(props)
  }

  // --- Edge case: invalid partition key version ---

  it should "throw IllegalArgumentException for invalid partition key version on multi-path" in {
    val props = Map(
      "partitionKeyPath" -> "/a,/b",
      "partitionKeyVersion" -> "V3"
    )

    an[IllegalArgumentException] should be thrownBy client.getPartitionKeyDefinition(props)
  }

  //scalastyle:on multiple.string.literals
}
