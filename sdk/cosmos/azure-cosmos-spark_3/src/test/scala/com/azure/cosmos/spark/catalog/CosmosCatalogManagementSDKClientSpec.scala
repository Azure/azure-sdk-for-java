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

import java.lang.reflect.InvocationTargetException
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

  private def invokeGetPartitionKeyDefinition(containerProperties: Map[String, String]): ContainerPartitionKey = {
    val method = client.getClass.getDeclaredMethod("getPartitionKeyDefinition", classOf[Map[_, _]])
    method.setAccessible(true)
    try {
      method.invoke(client, containerProperties).asInstanceOf[ContainerPartitionKey]
    } catch {
      case e: InvocationTargetException => throw e.getCause
    }
  }

  // --- Multi-path partition key tests ---

  "getPartitionKeyDefinition" should "default to MULTI_HASH with version 2 for multi-path without explicit kind or version" in {
    val props = Map("partitionKeyPath" -> "/tenantId,/userId,/sessionId")
    val result = invokeGetPartitionKeyDefinition(props)

    result.kind() shouldEqual PartitionKind.MULTI_HASH
    result.version() shouldEqual 2
    result.paths().asScala should contain theSameElementsInOrderAs List("/tenantId", "/userId", "/sessionId")
  }

  it should "succeed for multi-path with explicit MultiHash kind" in {
    val props = Map(
      "partitionKeyPath" -> "/a,/b",
      "partitionKeyKind" -> "MultiHash"
    )
    val result = invokeGetPartitionKeyDefinition(props)

    result.kind() shouldEqual PartitionKind.MULTI_HASH
    result.version() shouldEqual 2
    result.paths().asScala should contain theSameElementsInOrderAs List("/a", "/b")
  }

  it should "throw IllegalArgumentException for multi-path with Hash kind" in {
    val props = Map(
      "partitionKeyPath" -> "/a,/b",
      "partitionKeyKind" -> "Hash"
    )

    an[IllegalArgumentException] should be thrownBy invokeGetPartitionKeyDefinition(props)
  }

  it should "throw IllegalArgumentException for multi-path with V1 version" in {
    val props = Map(
      "partitionKeyPath" -> "/a,/b",
      "partitionKeyVersion" -> "V1"
    )

    an[IllegalArgumentException] should be thrownBy invokeGetPartitionKeyDefinition(props)
  }

  it should "succeed for multi-path with explicit V2 version" in {
    val props = Map(
      "partitionKeyPath" -> "/a,/b",
      "partitionKeyVersion" -> "V2"
    )
    val result = invokeGetPartitionKeyDefinition(props)

    result.kind() shouldEqual PartitionKind.MULTI_HASH
    result.version() shouldEqual 2
    result.paths().asScala should contain theSameElementsInOrderAs List("/a", "/b")
  }

  // --- Single-path partition key tests ---

  it should "default to HASH for single-path without explicit kind" in {
    val props = Map("partitionKeyPath" -> "/id")
    val result = invokeGetPartitionKeyDefinition(props)

    result.kind() shouldEqual PartitionKind.HASH
    result.paths().asScala should contain theSameElementsInOrderAs List("/id")
  }

  it should "throw IllegalArgumentException for single-path with MULTI_HASH kind" in {
    val props = Map(
      "partitionKeyPath" -> "/id",
      "partitionKeyKind" -> "MultiHash"
    )

    an[IllegalArgumentException] should be thrownBy invokeGetPartitionKeyDefinition(props)
  }

  it should "set version 1 for single-path with explicit V1 version" in {
    val props = Map(
      "partitionKeyPath" -> "/id",
      "partitionKeyVersion" -> "V1"
    )
    val result = invokeGetPartitionKeyDefinition(props)

    result.kind() shouldEqual PartitionKind.HASH
    result.version() shouldEqual 1
    result.paths().asScala should contain theSameElementsInOrderAs List("/id")
  }

  it should "set version 2 for single-path with explicit V2 version" in {
    val props = Map(
      "partitionKeyPath" -> "/id",
      "partitionKeyVersion" -> "V2"
    )
    val result = invokeGetPartitionKeyDefinition(props)

    result.kind() shouldEqual PartitionKind.HASH
    result.version() shouldEqual 2
    result.paths().asScala should contain theSameElementsInOrderAs List("/id")
  }

  // --- Default path test ---

  it should "use default partition key path /id when partitionKeyPath is not specified" in {
    val props = Map.empty[String, String]
    val result = invokeGetPartitionKeyDefinition(props)

    result.kind() shouldEqual PartitionKind.HASH
    result.paths().asScala should contain theSameElementsInOrderAs List("/id")
  }

  //scalastyle:on multiple.string.literals
}
