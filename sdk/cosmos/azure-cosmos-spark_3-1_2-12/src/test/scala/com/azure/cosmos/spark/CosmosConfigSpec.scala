// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.UUID

class CosmosConfigSpec extends UnitSpec {
  //scalastyle:off multiple.string.literals

  private val sampleProdEndpoint = "https://boson-test.documents.azure.com:443/"

  "Config Parser" should "parse account credentials" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://boson-test.documents.azure.com:443/",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.applicationName" -> "myapp",
      "spark.cosmos.useGatewayMode" -> "true",
      "spark.cosmos.preferredRegionsList" -> "[west us, eastus1]"
    )

    val endpointConfig = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)

    endpointConfig.endpoint shouldEqual sampleProdEndpoint
    endpointConfig.key shouldEqual "xyz"
    endpointConfig.applicationName.get shouldEqual "myapp"
    endpointConfig.useGatewayMode shouldEqual true
    endpointConfig.preferredRegionsList.isDefined shouldEqual true
    endpointConfig.preferredRegionsList.get should contain theSameElementsAs Array("westus", "eastus1")
  }

  it should "validate account endpoint" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "invalidUrl",
      "spark.cosmos.accountKey" -> "xyz"
    )

    try {
      CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
      fail("invalid URL")
    } catch {
      case e: Exception => e.getMessage shouldEqual
        "invalid configuration for spark.cosmos.accountEndpoint:invalidUrl." +
          " Config description: Cosmos DB Account Endpoint Uri"
    }
  }

  it should "validate preferred regions" in {
    var userConfig = Map(
      "spark.cosmos.accountEndpoint" -> sampleProdEndpoint,
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.preferredRegionsList" -> "a.b"
    )

    try {
      CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
      fail("invalid preferred region list")
    } catch {
      case e: Exception => {
        e.getMessage shouldEqual
          "invalid configuration for spark.cosmos.preferredRegionsList:a.b. Config description: Preferred Region List"
      }
    }

    userConfig = Map(
      "spark.cosmos.accountEndpoint" -> sampleProdEndpoint,
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.preferredRegionsList" -> "[westus, eastus"
    )

    try {
      CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
      fail("invalid preferred region list")
    } catch {
      case e: Exception => {
        e.getMessage shouldEqual
          "invalid configuration for spark.cosmos.preferredRegionsList:[westus, eastus. Config description: Preferred Region List"
      }
    }
  }

  it should "preferred regions parsing" in {
    var userConfig = Map(
      "spark.cosmos.accountEndpoint" -> sampleProdEndpoint,
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.preferredRegionsList" -> "[eastus, west us1]"
    )

    var config = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
    config.preferredRegionsList.get should contain theSameElementsAs Array("eastus", "westus1")

    userConfig = Map(
      "spark.cosmos.accountEndpoint" -> sampleProdEndpoint,
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.preferredRegionsList" -> "eastus, west us1"
    )

    config = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
    config.preferredRegionsList.get should contain theSameElementsAs Array("eastus", "westus1")

    userConfig = Map(
      "spark.cosmos.accountEndpoint" -> sampleProdEndpoint,
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.preferredRegionsList" -> " eastus , west us1 "
    )

    config = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
    config.preferredRegionsList.get should contain theSameElementsAs Array("eastus", "westus1")

    userConfig = Map(
      "spark.cosmos.accountEndpoint" -> sampleProdEndpoint,
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.preferredRegionsList" -> " [ eastus , west us1 ] "
    )

    config = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
    config.preferredRegionsList.get should contain theSameElementsAs Array("eastus", "westus1")

    userConfig = Map(
      "spark.cosmos.accountEndpoint" -> sampleProdEndpoint,
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.preferredRegionsList" -> "west us1"
    )

    config = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
    config.preferredRegionsList.get should contain theSameElementsAs Array("westus1")

    userConfig = Map(
      "spark.cosmos.accountEndpoint" -> sampleProdEndpoint,
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.preferredRegionsList" -> "[west us1]"
    )

    config = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
    config.preferredRegionsList.get should contain theSameElementsAs Array("westus1")

    userConfig = Map(
      "spark.cosmos.accountEndpoint" -> sampleProdEndpoint,
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.preferredRegionsList" -> "[]"
    )

    config = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
    config.preferredRegionsList.get should contain theSameElementsAs Array[String]()

  }


  it should "complain if mandatory config is missing" in {
    val userConfig = Map(
      "spark.cosmos.accountKey" -> "xyz"
    )

    try {
      CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
      fail("missing URL")
    } catch {
      case e: Exception => e.getMessage shouldEqual
        "mandatory option spark.cosmos.accountEndpoint is missing." +
          " Config description: Cosmos DB Account Endpoint Uri"
    }
  }

  it should "parse read configuration" in {
    val userConfig = Map(
      "spark.cosmos.read.forceEventualConsistency" -> "false",
      "spark.cosmos.read.schemaConversionMode" -> "Strict"
    )

    val config = CosmosReadConfig.parseCosmosReadConfig(userConfig)

    config.forceEventualConsistency shouldBe false
    config.schemaConversionMode shouldBe SchemaConversionModes.Strict
    config.customQuery shouldBe empty
  }

  it should "parse custom query option of read configuration" in {
    val queryText = s"SELECT * FROM c where c.id ='${UUID.randomUUID().toString}'"
    val userConfig = Map(
      "spark.cosmos.read.forceEventualConsistency" -> "false",
      "spark.cosmos.read.schemaConversionMode" -> "Strict",
      "spark.cosmos.read.customQuery" -> queryText
    )

    val config = CosmosReadConfig.parseCosmosReadConfig(userConfig)

    config.forceEventualConsistency shouldBe false
    config.schemaConversionMode shouldBe SchemaConversionModes.Strict
    config.customQuery.isDefined shouldBe true
    config.customQuery.get.queryText shouldBe queryText
  }

  it should "throw on invalid read configuration" in {
    val userConfig = Map(
      "spark.cosmos.read.schemaConversionMode" -> "not a valid value"
    )

    try {
      CosmosReadConfig.parseCosmosReadConfig(userConfig)
      fail("should have throw on invalid value")
    } catch {
      case e: Exception => succeed
    }
  }

  it should "parse read configuration default" in {

    val config = CosmosReadConfig.parseCosmosReadConfig(Map.empty[String, String])

    config.forceEventualConsistency shouldBe true
    config.schemaConversionMode shouldBe SchemaConversionModes.Relaxed
  }

  it should "parse inference configuration" in {
    val customQuery = "select * from c"
    val userConfig = Map(
      "spark.cosmos.read.inferSchema.samplingSize" -> "50",
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.read.inferSchema.includeSystemProperties" -> "true",
      "spark.cosmos.read.inferSchema.includeTimestamp" -> "true",
      "spark.cosmos.read.inferSchema.query" -> customQuery
    )

    val config = CosmosSchemaInferenceConfig.parseCosmosInferenceConfig(userConfig)
    config.inferSchemaSamplingSize shouldEqual 50
    config.inferSchemaEnabled shouldBe false
    config.includeSystemProperties shouldBe true
    config.includeTimestamp shouldBe true
    config.inferSchemaQuery shouldEqual Some(customQuery)
  }

  it should "provide default schema inference config" in {
    val userConfig = Map[String, String]()

    val config = CosmosSchemaInferenceConfig.parseCosmosInferenceConfig(userConfig)

    config.inferSchemaSamplingSize shouldEqual 1000
    config.inferSchemaEnabled shouldBe true
    config.includeSystemProperties shouldBe false
    config.includeTimestamp shouldBe false
  }

  it should "provide default write config" in {
    val userConfig = Map[String, String]()

    val config = CosmosWriteConfig.parseWriteConfig(userConfig)

    config.itemWriteStrategy shouldEqual ItemWriteStrategy.ItemOverwrite
    config.maxRetryCount shouldEqual 10
    config.bulkEnabled shouldEqual true
    config.pointMaxConcurrency.isDefined shouldEqual false
    config.bulkMaxPendingOperations.isDefined shouldEqual false

  }

  it should "parse point write config" in {
    val userConfig = Map(
      "spark.cosmos.write.strategy" -> "ItemAppend",
      "spark.cosmos.write.maxRetryCount" -> "8",
      "spark.cosmos.write.bulk.enabled" -> "false",
      "spark.cosmos.write.point.maxConcurrency" -> "12"
    )

    val config = CosmosWriteConfig.parseWriteConfig(userConfig)

    config.itemWriteStrategy shouldEqual ItemWriteStrategy.ItemAppend
    config.maxRetryCount shouldEqual 8
    config.bulkEnabled shouldEqual false
    config.pointMaxConcurrency.get shouldEqual 12
  }

  it should "parse bulk write config" in {
    val userConfig = Map(
      "spark.cosmos.write.strategy" -> "ItemAppend",
      "spark.cosmos.write.maxRetryCount" -> "8",
      "spark.cosmos.write.bulk.enabled" -> "true",
      "spark.cosmos.write.bulk.maxPendingOperations" -> "12"
    )

    val config = CosmosWriteConfig.parseWriteConfig(userConfig)

    config.itemWriteStrategy shouldEqual ItemWriteStrategy.ItemAppend
    config.maxRetryCount shouldEqual 8
    config.bulkEnabled shouldEqual true
    config.bulkMaxPendingOperations.get shouldEqual 12
  }

  it should "parse partitioning config with custom Strategy" in {
    val partitioningConfig = Map(
      "spark.cosmos.read.partitioning.strategy" -> "Custom",
      "spark.cosmos.partitioning.targetedCount" -> "8"
    )

    val config = CosmosPartitioningConfig.parseCosmosPartitioningConfig(partitioningConfig)

    config.partitioningStrategy shouldEqual PartitioningStrategies.Custom
    config.targetedPartitionCount.get shouldEqual 8
  }

  it should "parse partitioning config with custom Strategy even with incorrect casing" in {
    val partitioningConfig = Map(
      "spark.cosmos.read.partitioning.strategy" -> "CuSTom",
      "spark.cosmos.partitioning.tarGETedCount" -> "8"
    )

    val config = CosmosPartitioningConfig.parseCosmosPartitioningConfig(partitioningConfig)

    config.partitioningStrategy shouldEqual PartitioningStrategies.Custom
    config.targetedPartitionCount.get shouldEqual 8
  }

  it should "parse partitioning config without strategy" in {
    val partitioningConfig = Map(
      "spark.cosmos.partitioning.tarGETedCount" -> "8"
    )

    val config = CosmosPartitioningConfig.parseCosmosPartitioningConfig(partitioningConfig)

    config.partitioningStrategy shouldEqual PartitioningStrategies.Default
    config.targetedPartitionCount shouldEqual None
  }

  it should "complain when parsing custom partitioning strategy without  mandatory targetedCount" in {
    val partitioningConfig = Map(
      "spark.cosmos.read.partitioning.strategy" -> "Custom"
    )

    try {
      CosmosPartitioningConfig.parseCosmosPartitioningConfig(partitioningConfig)
      fail("missing targetedCount")
    } catch {
      case e: Exception => e.getMessage shouldEqual
        "mandatory option spark.cosmos.partitioning.targetedCount is missing." +
          " Config description: The targeted Partition Count. This parameter is optional and ignored unless " +
          "strategy==Custom is used. In this case the " +
          "Spark Connector won't dynamically calculate number of partitions but stick with this value."
    }
  }

  it should "complain when parsing invalid partitioning strategy" in {
    val partitioningConfig = Map(
      "spark.cosmos.read.partitioning.strategy" -> "Whatever"
    )

    try {
      CosmosPartitioningConfig.parseCosmosPartitioningConfig(partitioningConfig)
      fail("missing targetedCount")
    } catch {
      case e: Exception => e.getMessage shouldEqual
        "invalid configuration for spark.cosmos.read.partitioning.strategy:Whatever. " +
          "Config description: The partitioning strategy used (Default, Custom, Restrictive or Aggressive)"
    }
  }

  it should "parse partitioning config with restrictive Strategy ignores targetedCount" in {
    val partitioningConfig = Map(
      "spark.cosmos.read.partitioning.strategy" -> "restrictive",
      "spark.cosmos.partitioning.targetedCount" -> "8"
    )

    val config = CosmosPartitioningConfig.parseCosmosPartitioningConfig(partitioningConfig)

    config.partitioningStrategy shouldEqual PartitioningStrategies.Restrictive
    config.targetedPartitionCount shouldEqual None
  }

  it should "parse change feed config with defaults" in {
    val changeFeedConfig = Map(
      "" -> ""
    )

    val config = CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(changeFeedConfig)

    config.changeFeedMode shouldEqual ChangeFeedModes.Incremental
    config.startFrom shouldEqual ChangeFeedStartFromModes.Beginning
    config.startFromPointInTime shouldEqual None
    config.maxItemCountPerTrigger shouldEqual None
  }

  it should "parse change feed config for full fidelity with incorrect casing" in {
    val changeFeedConfig = Map(
      "spark.cosmos.changeFeed.mode" -> "FULLFidelity",
      "spark.cosmos.changeFeed.STARTfrom" -> "NOW",
      "spark.cosmos.changeFeed.itemCountPerTriggerHint" -> "54"
    )

    val config = CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(changeFeedConfig)

    config.changeFeedMode shouldEqual ChangeFeedModes.FullFidelity
    config.startFrom shouldEqual ChangeFeedStartFromModes.Now
    config.startFromPointInTime shouldEqual None
    config.maxItemCountPerTrigger.get shouldEqual 54
  }

  it should "parse change feed config with PIT start mode" in {
    val changeFeedConfig = Map(
      "spark.cosmos.changeFeed.mode" -> "incremental",
      "spark.cosmos.changeFeed.STARTfrom" -> "2019-12-31T10:45:10Z",
      "spark.cosmos.changeFeed.itemCountPerTriggerHint" -> "54"
    )

    val config = CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(changeFeedConfig)

    config.changeFeedMode shouldEqual ChangeFeedModes.Incremental
    config.startFrom shouldEqual ChangeFeedStartFromModes.PointInTime
    Instant.from(config.startFromPointInTime.get) shouldEqual
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        .parse("2019-12-31T10:45:10Z")
        .toInstant
    config.maxItemCountPerTrigger.get shouldEqual 54
  }

  it should "complain when parsing invalid change feed mode" in {
    val changeFeedConfig = Map(
      "spark.cosmos.changeFeed.mode" -> "Whatever",
      "spark.cosmos.changeFeed.STARTfrom" -> "2019-12-31T10:45:10Z",
      "spark.cosmos.changeFeed.maxItemCountPerTriggerHint" -> "54"
    )

    try {
      CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(changeFeedConfig)
      fail("incorrect mode")
    } catch {
      case e: Exception => e.getMessage shouldEqual
        "invalid configuration for spark.cosmos.changeFeed.mode:Whatever. Config description: " +
          "ChangeFeed mode (Incremental or FullFidelity)"
    }
  }

  it should "complain when parsing invalid Point in time (missing time zone)" in {
    val changeFeedConfig = Map(
      "spark.cosmos.changeFeed.mode" -> "Incremental",
      "spark.cosmos.changeFeed.STARTfrom" -> "2019-12-31T10:45:10",
      "spark.cosmos.changeFeed.maxItemCountPerTriggerHint" -> "54"
    )

    try {
      CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(changeFeedConfig)
      fail("incorrect mode")
    } catch {
      case e: Exception => e.getMessage shouldEqual
        "invalid configuration for spark.cosmos.changeFeed.startFrom:2019-12-31T10:45:10. " +
          "Config description: ChangeFeed Start from settings (Now, Beginning  or a certain " +
          "point in time (UTC) for example 2020-02-10T14:15:03Z) - the default value is 'Beginning'."
    }
  }

  it should "complain when parsing invalid number for maxItemCountPerTriggerHint" in {
    val changeFeedConfig = Map(
      "spark.cosmos.changeFeed.mode" -> "Incremental",
      "spark.cosmos.changeFeed.STARTfrom" -> "2019-12-31T10:45:10Z",
      "spark.cosmos.changeFeed.itemCountPerTriggerHint" -> "54OOrMore"
    )

    try {
      CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(changeFeedConfig)
      fail("incorrect mode")
    } catch {
      case e: Exception => e.getMessage shouldEqual
        "invalid configuration for spark.cosmos.changeFeed.itemCountPerTriggerHint:54OOrMore. " +
          "Config description: Approximate maximum number of items read from change feed for each trigger"
    }
  }

  //scalastyle:on multiple.string.literals
}
