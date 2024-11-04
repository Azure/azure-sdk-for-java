// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.core.management.AzureEnvironment
import com.azure.cosmos.implementation.ImplementationBridgeHelpers
import com.azure.cosmos.implementation.batch.BatchRequestResponseConstants
import com.azure.cosmos.models.CosmosContainerIdentity
import com.azure.cosmos.spark.CosmosPatchOperationTypes.Increment
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.spark.utils.CosmosPatchTestHelper
import org.apache.spark.sql.types.{NumericType, StructType}

import java.text.SimpleDateFormat
import java.time.{Duration, Instant}
import java.util.UUID
import scala.collection.mutable.ListBuffer
import scala.util.Random

class CosmosConfigSpec extends UnitSpec with BasicLoggingTrait {
//scalastyle:off multiple.string.literals

  private val sampleProdEndpoint = "https://boson-test.documents.azure.com:443/"
  private val defaultPatchOperationType = CosmosPatchOperationTypes.Replace
  private val testAccountTenantId = UUID.randomUUID().toString
  private val testAccountSubscriptionId = UUID.randomUUID().toString
  private val testAccountResourceGroupName = "test-resourceGroup"
  private val testServicePrincipalClientId = UUID.randomUUID().toString
  private val testServicePrincipalClientSecret = "test-secret"
  private val testServicePrincipalClientCert = "PEMBase64"

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
    endpointConfig.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual "xyz"
    endpointConfig.applicationName.get shouldEqual "myapp"
    endpointConfig.useGatewayMode shouldEqual true
    endpointConfig.preferredRegionsList.isDefined shouldEqual true
    endpointConfig.preferredRegionsList.get should contain theSameElementsAs Array("west us", "eastus1")
  }

  "Config Parser" should "parse default account AAD authentication credentials with cert" in {

    for (authType <- Array("ServicePrinciple", "ServicePrincipal")) {
      val userConfig = Map(
        "spark.cosmos.accountEndpoint" -> "https://boson-test.documents.azure.com:443/",
        "spark.cosmos.auth.type" -> authType,
        "spark.cosmos.account.subscriptionId" -> testAccountSubscriptionId,
        "spark.cosmos.account.tenantId" -> testAccountTenantId,
        "spark.cosmos.account.resourceGroupName" -> testAccountResourceGroupName,
        "spark.cosmos.auth.aad.clientId" -> testServicePrincipalClientId,
        "spark.cosmos.auth.aad.clientCertPemBase64" -> testServicePrincipalClientCert,
        "spark.cosmos.auth.aad.clientCertSendChain" -> "true"
      )

      val endpointConfig = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)

      endpointConfig.endpoint shouldEqual sampleProdEndpoint

      val servicePrincipalAuthConfig = endpointConfig.authConfig.asInstanceOf[CosmosServicePrincipalAuthConfig]
      endpointConfig.subscriptionId.get shouldEqual testAccountSubscriptionId
      servicePrincipalAuthConfig.tenantId shouldEqual testAccountTenantId
      endpointConfig.resourceGroupName.get shouldEqual testAccountResourceGroupName
      servicePrincipalAuthConfig.clientId shouldEqual testServicePrincipalClientId
      servicePrincipalAuthConfig.clientSecret.isDefined shouldEqual false
      servicePrincipalAuthConfig.clientCertPemBase64.isDefined shouldEqual true
      servicePrincipalAuthConfig.clientCertPemBase64.get shouldEqual testServicePrincipalClientCert
      servicePrincipalAuthConfig.sendChain shouldEqual true
      new AzureEnvironment(endpointConfig.azureEnvironmentEndpoints).getActiveDirectoryEndpoint shouldEqual AzureEnvironment.AZURE.getActiveDirectoryEndpoint
      endpointConfig.accountName shouldEqual "boson-test"
    }
  }

  "Config Parser" should "parse default account AAD authentication credentials" in {

      for (authType <- Array("ServicePrinciple", "ServicePrincipal")) {
          val userConfig = Map(
              "spark.cosmos.accountEndpoint" -> "https://boson-test.documents.azure.com:443/",
              "spark.cosmos.auth.type" -> authType,
              "spark.cosmos.account.subscriptionId" -> testAccountSubscriptionId,
              "spark.cosmos.account.tenantId" -> testAccountTenantId,
              "spark.cosmos.account.resourceGroupName" -> testAccountResourceGroupName,
              "spark.cosmos.auth.aad.clientId" -> testServicePrincipalClientId,
              "spark.cosmos.auth.aad.clientSecret" -> testServicePrincipalClientSecret
          )

          val endpointConfig = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)

          endpointConfig.endpoint shouldEqual sampleProdEndpoint

          val servicePrincipalAuthConfig = endpointConfig.authConfig.asInstanceOf[CosmosServicePrincipalAuthConfig]
          endpointConfig.subscriptionId.get shouldEqual testAccountSubscriptionId
          servicePrincipalAuthConfig.tenantId shouldEqual testAccountTenantId
          endpointConfig.resourceGroupName.get shouldEqual testAccountResourceGroupName
          servicePrincipalAuthConfig.clientId shouldEqual testServicePrincipalClientId
          servicePrincipalAuthConfig.clientSecret.isDefined shouldEqual true
          servicePrincipalAuthConfig.clientSecret.get shouldEqual testServicePrincipalClientSecret
          servicePrincipalAuthConfig.clientCertPemBase64.isDefined shouldEqual false
          servicePrincipalAuthConfig.sendChain shouldEqual false
          new AzureEnvironment(endpointConfig.azureEnvironmentEndpoints).getActiveDirectoryEndpoint shouldEqual AzureEnvironment.AZURE.getActiveDirectoryEndpoint
          endpointConfig.accountName shouldEqual "boson-test"
      }
  }

  "Config Parser" should "parse managed identity AAD authentication credentials" in {

    for (managedIdentitySelector <- Array("", "clientId", "resourceId", "clientIdAndResourceId")) {
      val userConfigMutable = collection.mutable.Map(
        "spark.cosmos.accountEndpoint" -> "https://boson-test.documents.azure.com:443/",
        "spark.cosmos.auth.type" -> "ManagedIdentity",
        "spark.cosmos.account.subscriptionId" -> testAccountSubscriptionId,
        "spark.cosmos.account.tenantId" -> testAccountTenantId,
        "spark.cosmos.account.resourceGroupName" -> testAccountResourceGroupName,
        "spark.cosmos.auth.aad.clientSecret" -> testServicePrincipalClientSecret
      )

      val randomId = UUID.randomUUID().toString

      managedIdentitySelector match {
        case "" =>
        case "clientId" => userConfigMutable.put("spark.cosmos.auth.aad.clientId", randomId)
        case "resourceId" => userConfigMutable.put("spark.cosmos.auth.aad.resourceId", randomId)
        case "clientIdAndResourceId"  => userConfigMutable.put("spark.cosmos.auth.aad.clientId", randomId)
          userConfigMutable.put("spark.cosmos.auth.aad.resourceId", randomId)
      }

      val userConfig = userConfigMutable.toMap
      val endpointConfig = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)

      endpointConfig.endpoint shouldEqual sampleProdEndpoint

      val managedIdentityAuthConfig = endpointConfig.authConfig.asInstanceOf[CosmosManagedIdentityAuthConfig]
      endpointConfig.subscriptionId.get shouldEqual testAccountSubscriptionId
      managedIdentityAuthConfig.tenantId shouldEqual testAccountTenantId
      endpointConfig.resourceGroupName.get shouldEqual testAccountResourceGroupName
      if (managedIdentitySelector == "clientId" || managedIdentitySelector == "clientIdAndResourceId") {
        managedIdentityAuthConfig.clientId shouldEqual Some(randomId)
      } else {
        managedIdentityAuthConfig.clientId shouldEqual None
      }

      if (managedIdentitySelector == "resourceId" || managedIdentitySelector == "clientIdAndResourceId") {
        managedIdentityAuthConfig.resourceId shouldEqual Some(randomId)
      } else {
        managedIdentityAuthConfig.resourceId shouldEqual None
      }

      new AzureEnvironment(endpointConfig.azureEnvironmentEndpoints).getActiveDirectoryEndpoint shouldEqual AzureEnvironment.AZURE.getActiveDirectoryEndpoint
      endpointConfig.accountName shouldEqual "boson-test"
    }
  }

  "Config Parser" should "parse account AAD authentication credentials" in {
      for (authType <- Array("ServicePrinciple", "ServicePrincipal")) {
          val userConfig = Map(
              "spark.cosmos.accountEndpoint" -> "https://boson-test.documents.azure.com:443/",
              "spark.cosmos.auth.type" -> authType,
              "spark.cosmos.account.subscriptionId" -> testAccountSubscriptionId,
              "spark.cosmos.account.tenantId" -> testAccountTenantId,
              "spark.cosmos.account.resourceGroupName" -> testAccountResourceGroupName,
              "spark.cosmos.account.azureEnvironment" -> "AzureUsGovernment",
              "spark.cosmos.auth.aad.clientId" -> testServicePrincipalClientId,
              "spark.cosmos.auth.aad.clientSecret" -> testServicePrincipalClientSecret,
          )

          val endpointConfig = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)

          endpointConfig.endpoint shouldEqual sampleProdEndpoint

          val servicePrincipalAuthConfig = endpointConfig.authConfig.asInstanceOf[CosmosServicePrincipalAuthConfig]
          endpointConfig.subscriptionId.get shouldEqual testAccountSubscriptionId
          servicePrincipalAuthConfig.tenantId shouldEqual testAccountTenantId
          endpointConfig.resourceGroupName.get shouldEqual testAccountResourceGroupName
          servicePrincipalAuthConfig.clientId shouldEqual testServicePrincipalClientId
          servicePrincipalAuthConfig.clientSecret.isDefined shouldEqual true
          servicePrincipalAuthConfig.clientSecret.get shouldEqual testServicePrincipalClientSecret
          new AzureEnvironment(endpointConfig.azureEnvironmentEndpoints).getActiveDirectoryEndpoint shouldEqual AzureEnvironment.AZURE_US_GOVERNMENT.getActiveDirectoryEndpoint
          endpointConfig.accountName shouldEqual "boson-test"
      }
  }

    "Config Parser" should "parse account credentials with spark.cosmos.preferredRegions" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://boson-test.documents.azure.com:443/",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.applicationName" -> "myapp",
      "spark.cosmos.useGatewayMode" -> "true",
      "spark.cosmos.preferredRegions" -> "[west us, eastus1]"
    )

    val endpointConfig = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)

    endpointConfig.endpoint shouldEqual sampleProdEndpoint
    endpointConfig.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual "xyz"
    endpointConfig.applicationName.get shouldEqual "myapp"
    endpointConfig.useGatewayMode shouldEqual true
    endpointConfig.preferredRegionsList.isDefined shouldEqual true
    endpointConfig.preferredRegionsList.get should contain theSameElementsAs Array("west us", "eastus1")
  }

  "Config Parser" should "parse account credentials with spark.cosmos.preferredRegions and spark.cosmos.preferredRegionsList" in {
    val userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://boson-test.documents.azure.com:443/",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.applicationName" -> "myapp",
      "spark.cosmos.useGatewayMode" -> "true",
      "spark.cosmos.preferredRegions" -> "[west us, eastus1]",
      "spark.cosmos.preferredRegionsList" -> "[west us, eastus1]"
    )

    try {
      CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
      fail("multiple conflicting options")
    } catch {
      case e: Exception => e.getMessage shouldEqual(
        "specified multiple conflicting options [spark.cosmos.preferredRegionsList] and [spark.cosmos.preferredRegions]. Only one should be specified")
    }
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

      userConfig = Map(
          "spark.cosmos.accountEndpoint" -> sampleProdEndpoint,
          "spark.cosmos.accountKey" -> "xyz",
          "spark.cosmos.preferredRegionsList" -> "[west  us, eastus]"
      )

      try {
          CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
          fail("invalid preferred region list")
      } catch {
          case e: Exception => {
              e.getMessage shouldEqual
                  "invalid configuration for spark.cosmos.preferredRegionsList:[west  us, eastus]. Config description: Preferred Region List"
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
    config.preferredRegionsList.get should contain theSameElementsAs Array("eastus", "west us1")

    userConfig = Map(
      "spark.cosmos.accountEndpoint" -> sampleProdEndpoint,
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.preferredRegionsList" -> "eastus, west us1"
    )

    config = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
    config.preferredRegionsList.get should contain theSameElementsAs Array("eastus", "west us1")

    userConfig = Map(
      "spark.cosmos.accountEndpoint" -> sampleProdEndpoint,
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.preferredRegionsList" -> " eastus , west us1 "
    )

    config = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
    config.preferredRegionsList.get should contain theSameElementsAs Array("eastus", "west us1")

    userConfig = Map(
      "spark.cosmos.accountEndpoint" -> sampleProdEndpoint,
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.preferredRegionsList" -> " [ eastus , west us1 ] "
    )

    config = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
    config.preferredRegionsList.get should contain theSameElementsAs Array("eastus", "west us1")

    userConfig = Map(
      "spark.cosmos.accountEndpoint" -> sampleProdEndpoint,
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.preferredRegionsList" -> "west us1"
    )

    config = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
    config.preferredRegionsList.get should contain theSameElementsAs Array("west us1")

    userConfig = Map(
      "spark.cosmos.accountEndpoint" -> sampleProdEndpoint,
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.preferredRegionsList" -> "[west us1]"
    )

    config = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
    config.preferredRegionsList.get should contain theSameElementsAs Array("west us1")

    userConfig = Map(
      "spark.cosmos.accountEndpoint" -> sampleProdEndpoint,
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.preferredRegionsList" -> "[]"
    )

    config = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
    config.preferredRegionsList.get should contain theSameElementsAs Array[String]()

    userConfig = Map(
      "spark.cosmos.accountEndpoint" -> sampleProdEndpoint,
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.preferredRegionsList" -> "[west us 1, east us 2]"
    )

    config = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
    config.preferredRegionsList.get should contain theSameElementsAs Array("west us 1","east us 2")

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
    var userConfig = Map(
      "spark.cosmos.read.forceEventualConsistency" -> "false",
      "spark.cosmos.read.schemaConversionMode" -> "Strict"
    )

    var config = CosmosReadConfig.parseCosmosReadConfig(userConfig)

    config.forceEventualConsistency shouldBe false
    config.schemaConversionMode shouldBe SchemaConversionModes.Strict
    config.customQuery shouldBe empty
    config.maxItemCount shouldBe 1000
    config.prefetchBufferSize shouldBe 8
    config.dedicatedGatewayRequestOptions.getMaxIntegratedCacheStaleness shouldBe null
    config.runtimeFilteringEnabled shouldBe true
    config.readManyFilteringConfig.readManyFilteringEnabled shouldBe false
    config.readManyFilteringConfig.readManyFilterProperty shouldEqual "_itemIdentity"

    userConfig = Map(
      "spark.cosmos.read.forceEventualConsistency" -> "false",
      "spark.cosmos.read.schemaConversionMode" -> "Strict",
      "spark.cosmos.read.maxItemCount" -> "1000",
      "spark.cosmos.read.maxIntegratedCacheStalenessInMS" -> "1000",
      "spark.cosmos.read.runtimeFiltering.enabled" -> "false",
      "spark.cosmos.read.readManyFiltering.enabled" -> "true"
    )

    config = CosmosReadConfig.parseCosmosReadConfig(userConfig)

    config.forceEventualConsistency shouldBe false
    config.schemaConversionMode shouldBe SchemaConversionModes.Strict
    config.customQuery shouldBe empty
    config.maxItemCount shouldBe 1000
    config.prefetchBufferSize shouldBe 8
    config.dedicatedGatewayRequestOptions.getMaxIntegratedCacheStaleness shouldBe Duration.ofMillis(1000)
    config.runtimeFilteringEnabled shouldBe false
    config.readManyFilteringConfig.readManyFilteringEnabled shouldBe true
    config.readManyFilteringConfig.readManyFilterProperty shouldEqual "_itemIdentity"

    userConfig = Map(
      "spark.cosmos.read.forceEventualConsistency" -> "false",
      "spark.cosmos.read.schemaConversionMode" -> "Strict",
      "spark.cosmos.read.maxItemCount" -> "1001",
      "spark.cosmos.read.prefetchBufferSize" -> "16"
    )

    config = CosmosReadConfig.parseCosmosReadConfig(userConfig)

    config.forceEventualConsistency shouldBe false
    config.schemaConversionMode shouldBe SchemaConversionModes.Strict
    config.customQuery shouldBe empty
    config.maxItemCount shouldBe 1001
    config.prefetchBufferSize shouldBe 16

    userConfig = Map(
      "spark.cosmos.read.forceEventualConsistency" -> "false",
      "spark.cosmos.read.schemaConversionMode" -> "Strict",
      "spark.cosmos.read.maxItemCount" -> "1001",
      "spark.cosmos.read.prefetchBufferSize" -> "2"
    )

    config = CosmosReadConfig.parseCosmosReadConfig(userConfig)

    config.forceEventualConsistency shouldBe false
    config.schemaConversionMode shouldBe SchemaConversionModes.Strict
    config.customQuery shouldBe empty
    config.maxItemCount shouldBe 1001
    config.prefetchBufferSize shouldBe 2 // will be converted/rounded to effectively 8 later at runtime not in config

    userConfig = Map(
      "spark.cosmos.read.forceEventualConsistency" -> "false",
      "spark.cosmos.read.schemaConversionMode" -> "Strict",
      "spark.cosmos.read.maxItemCount" -> "1001",
      "spark.cosmos.read.prefetchBufferSize" -> "1"
    )

    config = CosmosReadConfig.parseCosmosReadConfig(userConfig)

    config.forceEventualConsistency shouldBe false
    config.schemaConversionMode shouldBe SchemaConversionModes.Strict
    config.customQuery shouldBe empty
    config.maxItemCount shouldBe 1001
    config.prefetchBufferSize shouldBe 1

    userConfig = Map(
      "spark.cosmos.read.forceEventualConsistency" -> "false",
      "spark.cosmos.read.schemaConversionMode" -> "Strict",
      "spark.cosmos.read.maxItemCount" -> "1001"
    )

    config = CosmosReadConfig.parseCosmosReadConfig(userConfig)

    config.forceEventualConsistency shouldBe false
    config.schemaConversionMode shouldBe SchemaConversionModes.Strict
    config.customQuery shouldBe empty
    config.maxItemCount shouldBe 1001
    config.prefetchBufferSize shouldBe 1
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

    val config = CosmosWriteConfig.parseWriteConfig(userConfig, StructType(Nil))

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

    val config = CosmosWriteConfig.parseWriteConfig(userConfig, StructType(Nil))

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

    val config = CosmosWriteConfig.parseWriteConfig(userConfig, StructType(Nil))

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

  it should "parse change feed config for all versions and deletes with incorrect casing" in {
    val changeFeedConfig = Map(
      "spark.cosmos.changeFeed.mode" -> "AllVersionsANDDELETES",
      "spark.cosmos.changeFeed.STARTfrom" -> "NOW",
      "spark.cosmos.changeFeed.itemCountPerTriggerHint" -> "54"
    )

    val config = CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(changeFeedConfig)

    config.changeFeedMode shouldEqual ChangeFeedModes.AllVersionsAndDeletes
    config.startFrom shouldEqual ChangeFeedStartFromModes.Now
    config.startFromPointInTime shouldEqual None
    config.maxItemCountPerTrigger.get shouldEqual 54
  }

  it should "parse change feed config (incremental) with PIT start mode" in {
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

  it should "parse change feed config (latestversion) with PIT start mode" in {
    val changeFeedConfig = Map(
      "spark.cosmos.changeFeed.mode" -> "latestversion",
      "spark.cosmos.changeFeed.STARTfrom" -> "2019-12-31T10:45:10Z",
      "spark.cosmos.changeFeed.itemCountPerTriggerHint" -> "54"
    )

    val config = CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(changeFeedConfig)

    config.changeFeedMode shouldEqual ChangeFeedModes.LatestVersion
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
          "ChangeFeed mode (Incremental/LatestVersion or FullFidelity/AllVersionsAndDeletes)"
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

  "Default patch config" should "be valid" in {
    val schema = CosmosPatchTestHelper.getPatchConfigTestSchema()
    val userConfig = Map(
      "spark.cosmos.write.strategy" -> "ItemPatch",
    )
    val writeConfig: CosmosWriteConfig = CosmosWriteConfig.parseWriteConfig(userConfig, schema)
    writeConfig should not be null
    writeConfig.patchConfigs.isDefined shouldEqual true
    val patchConfigs = writeConfig.patchConfigs.get

    patchConfigs.filter.isDefined shouldEqual false

    patchConfigs.columnConfigsMap.size shouldEqual schema.fields.size
    patchConfigs.columnConfigsMap.values.foreach(
      config => {
        config.mappingPath shouldEqual s"/${config.columnName}"
        config.operationType shouldEqual defaultPatchOperationType
      }
    )
  }

  "Customizing MaxBulKPayloadSizeInBytes" should "be possible" in {
    val schema = CosmosPatchTestHelper.getPatchConfigTestSchema()
    var userConfig = Map(
      "spark.cosmos.write.strategy" -> "ItemOverwrite",
      "spark.cosmos.write.bulk.enabled" -> "True",
    )
    var writeConfig: CosmosWriteConfig = CosmosWriteConfig.parseWriteConfig(userConfig, schema)
    writeConfig should not be null
    writeConfig.maxMicroBatchPayloadSizeInBytes should not be null
    writeConfig.maxMicroBatchPayloadSizeInBytes.isDefined shouldEqual true
    writeConfig.maxMicroBatchPayloadSizeInBytes.get shouldEqual BatchRequestResponseConstants.DEFAULT_MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES

    userConfig = Map(
      "spark.cosmos.write.strategy" -> "ItemOverwrite",
      "spark.cosmos.write.bulk.enabled" -> "True",
      "spark.cosmos.write.bulk.targetedPayloadSizeInBytes" -> "1000000",
    )

    writeConfig = CosmosWriteConfig.parseWriteConfig(userConfig, schema)
    writeConfig should not be null
    writeConfig.maxMicroBatchPayloadSizeInBytes should not be null
    writeConfig.maxMicroBatchPayloadSizeInBytes.isDefined shouldEqual true
    writeConfig.maxMicroBatchPayloadSizeInBytes.get shouldEqual 1000000
  }

  "Config Parser" should "validate default operation types for patch configs" in {
    val schema = CosmosPatchTestHelper.getPatchConfigTestSchema()

    val invalidOperationType = "dummy"
    val incrementOperationType = Increment.toString
    CosmosPatchOperationTypes.values.map(operationType => operationType.toString)
     .toList
     .+:(invalidOperationType)
     .foreach(operationTypeString => {
       val userConfig = Map(
         "spark.cosmos.write.strategy" -> "ItemPatch",
         "spark.cosmos.write.patch.defaultOperationType" -> s"$operationTypeString"
       )

       operationTypeString match {
         case _ if operationTypeString == incrementOperationType =>
           try {
             CosmosWriteConfig.parseWriteConfig(userConfig, schema)
             fail("Using increment as default operation type should fail as there are non-numeric type schema field")
           } catch {
             case e: Exception => e.getMessage should startWith(
               "Increment patch operation does not support for type")
           }
         case _ if operationTypeString == invalidOperationType =>
           try {
             CosmosWriteConfig.parseWriteConfig(userConfig, schema)
             fail("Using invalid operation type should fail")
           } catch {
             case e: Exception => e.getMessage should startWith(
               "invalid configuration for spark.cosmos.write.patch.defaultOperationType:dummy")
           }
         case _ =>
           val writeConfig: CosmosWriteConfig = CosmosWriteConfig.parseWriteConfig(userConfig, schema)
           writeConfig.patchConfigs.isDefined shouldEqual true

           val patchConfigs = writeConfig.patchConfigs.get
           patchConfigs.filter.isDefined shouldEqual false

           patchConfigs.columnConfigsMap.size shouldEqual schema.fields.size
           patchConfigs.columnConfigsMap.values.foreach(
             config => {
               config.mappingPath shouldEqual s"/${config.columnName}"
               config.operationType.toString shouldEqual operationTypeString
             }
           )
       }
     })
  }

  it should "validate column configs for patch configs" in {
    val schema = CosmosPatchTestHelper.getPatchConfigTestSchema()
    val testParameters = new ListBuffer[PatchColumnConfigParameterTest]

    testParameters +=
     PatchColumnConfigParameterTest(isValid = true, columnName = "", overrideConfigsString = "")
    testParameters +=
     PatchColumnConfigParameterTest(isValid = true, columnName = "", overrideConfigsString = "[]")
    testParameters +=
     PatchColumnConfigParameterTest(isValid = true, columnName = "", overrideConfigsString = " [  ] ")
    testParameters +=
     PatchColumnConfigParameterTest(
       isValid = false,
       columnName = "" ,
       overrideConfigsString = "[",
       errorMessage = Some("invalid configuration for spark.cosmos.write.patch.columnConfigs:["))
    testParameters +=
     PatchColumnConfigParameterTest(
       isValid = false,
       columnName = "",
       overrideConfigsString = "[col(column.path.random]",
       errorMessage = Some("invalid configuration for spark.cosmos.write.patch.columnConfigs:[col(column.path.random]"))

    // Add other test cases which will covered different columns combined with different match pattern (different cases of all the key words)
    val columnKeyWords = new ListBuffer[String]
    CosmosPatchTestHelper.getAllPermutationsOfKeyWord("col", "", columnKeyWords)
    val columnKeyWordRandom = new Random()

    val pathKeyWords= new ListBuffer[String]
    CosmosPatchTestHelper.getAllPermutationsOfKeyWord("path", "", pathKeyWords)
    val pathKeyWordRandom = new Random()

    val operationTypeKeyWords = new ListBuffer[String]
    CosmosPatchTestHelper.getAllPermutationsOfKeyWord("op", "", operationTypeKeyWords)
    val operationTypeKeyWordRandom = new Random()

    val usePathKeyword = new Random()

    schema.fields.foreach(field => {
      CosmosPatchOperationTypes.values.foreach(operationType => {
        var isValid = true
        var errorMessage = ""
        var mappingPath = s"/${field.name}"
        var configString = "["
        val columnKeyWord = columnKeyWords(columnKeyWordRandom.nextInt(columnKeyWords.size))
        configString += s"$columnKeyWord(${field.name})."

        if (usePathKeyword.nextBoolean()) {
          val pathKeyWord = pathKeyWords(pathKeyWordRandom.nextInt(pathKeyWords.size))
          mappingPath = s"$mappingPath-1"
          configString += s"$pathKeyWord(${mappingPath})."
        }

        val operationTypeKeyWord = operationTypeKeyWords(operationTypeKeyWordRandom.nextInt(operationTypeKeyWords.size))
        configString += s"$operationTypeKeyWord(${operationType})"
        if (operationType == Increment) {
          field.dataType match {
            case _: NumericType => isValid = true
            case _ =>
              isValid = false
              errorMessage = "Increment patch operation does not support for type"
          }
        }
        configString += "]"

        if (isValid) {
          testParameters +=
           PatchColumnConfigParameterTest(
             isValid,
             field.name,
             configString,
             Some(CosmosPatchColumnConfig(field.name, operationType, mappingPath, false)))
        } else {
          testParameters +=
           PatchColumnConfigParameterTest(
             isValid,
             field.name,
             configString,
             Some(CosmosPatchColumnConfig(field.name, operationType, mappingPath, false)),
             Some(errorMessage))
        }

      })
    })

    testParameters.foreach(testParameter => {

      val userConfig = Map(
        "spark.cosmos.write.strategy" -> "ItemPatch",
        "spark.cosmos.write.patch.columnConfigs" -> s"${testParameter.overrideConfigsString}"
      )

      testParameter.isValid match {
        case true =>
          val writeConfig: CosmosWriteConfig = CosmosWriteConfig.parseWriteConfig(userConfig, schema)
          writeConfig.patchConfigs.isDefined shouldEqual true

          val patchConfigs = writeConfig.patchConfigs.get
          patchConfigs.filter.isDefined shouldEqual false

          patchConfigs.columnConfigsMap.size shouldEqual schema.fields.size
          patchConfigs.columnConfigsMap.values.foreach(
            config => {
              if (testParameter.columnName == config.columnName && testParameter.overrideColumnConfig.isDefined) {
                config.mappingPath shouldEqual testParameter.overrideColumnConfig.get.mappingPath
                config.operationType shouldEqual testParameter.overrideColumnConfig.get.operationType
              } else {
                config.mappingPath shouldEqual s"/${config.columnName}"
                config.operationType shouldEqual defaultPatchOperationType
              }
            }
          )
        case _ =>
          try {
            CosmosWriteConfig.parseWriteConfig(userConfig, schema)
            fail(s"The test should have failed due to ${testParameter.errorMessage.get}")
          } catch {
            case e: Exception => e.getMessage should startWith(testParameter.errorMessage.get)
          }
      }
    })
  }

  "Patch column configs contain multiple definitions" should "be valid" in {
    val schema = CosmosPatchTestHelper.getPatchConfigTestSchema()

    val overrideConfig = Map(
      "longTypeColumn" -> CosmosPatchColumnConfig(
        "longTypeColumn", CosmosPatchOperationTypes.Increment, "/longTypeColumn", false),
      "stringTypeColumn" -> CosmosPatchColumnConfig(
        "stringTypeColumn", CosmosPatchOperationTypes.Add, "/newPath", false)
    )

    var aggregratedConfigString = "["
    overrideConfig.foreach(entry => {
      aggregratedConfigString += CosmosPatchTestHelper.getColumnConfigString(entry._2)
      aggregratedConfigString += ","
    })
    aggregratedConfigString += "]"

    val patchFilterPredicate = "where c.booleanTypeColumn = true"
    val defaultOperationType = CosmosPatchOperationTypes.Add

    val userConfig = Map(
      "spark.cosmos.write.strategy" -> "ItemPatch",
      "spark.cosmos.write.patch.defaultOperationType" -> defaultOperationType.toString,
      "spark.cosmos.write.patch.columnConfigs" -> aggregratedConfigString,
      "spark.cosmos.write.patch.filter" -> patchFilterPredicate
    )

    val writeConfig: CosmosWriteConfig = CosmosWriteConfig.parseWriteConfig(userConfig, schema)
    writeConfig should not be null

    writeConfig.patchConfigs.isDefined shouldEqual true
    val patchConfig = writeConfig.patchConfigs.get

    patchConfig.filter.isDefined shouldEqual true
    patchConfig.filter.get shouldEqual patchFilterPredicate

    patchConfig.columnConfigsMap.size shouldEqual schema.fields.size
    patchConfig.columnConfigsMap.values.foreach(columnConfig => {
      overrideConfig.get(columnConfig.columnName) match {
        case Some(overrideConfig) =>
          columnConfig.operationType shouldEqual overrideConfig.operationType
          columnConfig.mappingPath shouldEqual overrideConfig.mappingPath
        case _ =>
          columnConfig.operationType shouldEqual defaultOperationType
          columnConfig.mappingPath shouldEqual s"/${columnConfig.columnName}"
      }
    })

  }

  "Config Parser" should "validate column configs for column does not exists in schema for patch configs" in {
    val schema = CosmosPatchTestHelper.getPatchConfigTestSchema()

    val testParameters = new ListBuffer[PatchColumnConfigParameterTest]
    CosmosPatchOperationTypes.values.foreach(operationType => {

      val columnConfig = CosmosPatchColumnConfig("dummyColumn", operationType, "/dummyColumn", false)
      operationType match {
        case CosmosPatchOperationTypes.Remove | CosmosPatchOperationTypes.None =>
          testParameters +=
           PatchColumnConfigParameterTest(
             true,
             "dummyColumn",
             s"[${CosmosPatchTestHelper.getColumnConfigString(columnConfig)}]",
             Some(columnConfig))
        case _ =>
          testParameters +=
           PatchColumnConfigParameterTest(
             false,
             "dummyColumn",
             s"[${CosmosPatchTestHelper.getColumnConfigString(columnConfig)}]",
             Some(columnConfig),
             Some("Invalid column config. Column dummyColumn does not exist in schema"))
      }
    })

    testParameters.foreach(testParameter => {
      val userConfig = Map(
        "spark.cosmos.write.strategy" -> "ItemPatch",
        "spark.cosmos.write.patch.columnConfigs" -> testParameter.overrideConfigsString
      )

      testParameter.isValid match {
        case true =>
          val writeConfig: CosmosWriteConfig = CosmosWriteConfig.parseWriteConfig(userConfig, schema)
          writeConfig.patchConfigs.isDefined shouldEqual true
          writeConfig.patchConfigs.get.columnConfigsMap.size shouldEqual schema.fields.size + 1
        case false =>
          try {
            CosmosWriteConfig.parseWriteConfig(userConfig, schema)
            fail(s"The test should have failed for ${testParameter.overrideConfigsString} due to ${testParameter.errorMessage.get}")
          } catch {
            case e: Exception => e.getMessage should startWith(testParameter.errorMessage.get)
          }
      }
    })
  }

  "Default patchBulkUpdate config" should "be valid" in {
      val schema = CosmosPatchTestHelper.getPatchConfigTestSchema()
      val userConfig = Map(
          "spark.cosmos.write.strategy" -> "ItemBulkUpdate",
      )
      val writeConfig: CosmosWriteConfig = CosmosWriteConfig.parseWriteConfig(userConfig, schema)
      writeConfig should not be null
      writeConfig.patchConfigs.isDefined shouldEqual true
      val patchConfigs = writeConfig.patchConfigs.get

      patchConfigs.filter.isDefined shouldEqual false

      patchConfigs.columnConfigsMap.size shouldEqual 0
  }

  it should "validate column configs for patchBulkUpdate configs" in {
      val schema = CosmosPatchTestHelper.getPatchConfigTestSchema()
      val testParameters = new ListBuffer[PatchColumnConfigParameterTest]

      testParameters +=
          PatchColumnConfigParameterTest(isValid = true, columnName = "", overrideConfigsString = "")
      testParameters +=
          PatchColumnConfigParameterTest(isValid = true, columnName = "", overrideConfigsString = "[]")
      testParameters +=
          PatchColumnConfigParameterTest(isValid = true, columnName = "", overrideConfigsString = " [  ] ")
      testParameters +=
          PatchColumnConfigParameterTest(
              isValid = false,
              columnName = "",
              overrideConfigsString = "[",
              errorMessage = Some("invalid configuration for spark.cosmos.write.bulkUpdate.columnConfigs:["))
      testParameters +=
          PatchColumnConfigParameterTest(
              isValid = false,
              columnName = "",
              overrideConfigsString = "[col(column.path.random]",
              errorMessage = Some("invalid configuration for spark.cosmos.write.bulkUpdate.columnConfigs:[col(column.path.random]"))

      // Add other test cases which will covered different columns combined with different match pattern (different cases of all the key words)
      val columnKeyWords = new ListBuffer[String]
      CosmosPatchTestHelper.getAllPermutationsOfKeyWord("col", "", columnKeyWords)
      val columnKeyWordRandom = new Random()

      val pathKeyWords = new ListBuffer[String]
      CosmosPatchTestHelper.getAllPermutationsOfKeyWord("path", "", pathKeyWords)
      val pathKeyWordRandom = new Random()

      val usePathKeyword = new Random()
      val useRawJson = new Random()

      schema.fields.foreach(field => {
          var isValid = true
          var errorMessage = ""
          var mappingPath = s"/${field.name}"
          var configString = "["
          val columnKeyWord = columnKeyWords(columnKeyWordRandom.nextInt(columnKeyWords.size))
          configString += s"$columnKeyWord(${field.name})"

          if (usePathKeyword.nextBoolean()) {
              val pathKeyWord = pathKeyWords(pathKeyWordRandom.nextInt(pathKeyWords.size))
              mappingPath = s"$mappingPath-1"
              configString += s".$pathKeyWord(${mappingPath})"
          }

          if (useRawJson.nextBoolean()) {
              configString += s".rawJson"
          }

          configString += "]"

          if (isValid) {
              testParameters +=
                  PatchColumnConfigParameterTest(
                      isValid,
                      field.name,
                      configString,
                      Some(CosmosPatchColumnConfig(field.name, CosmosPatchOperationTypes.Set, mappingPath, false)))
          } else {
              testParameters +=
                  PatchColumnConfigParameterTest(
                      isValid,
                      field.name,
                      configString,
                      Some(CosmosPatchColumnConfig(field.name, CosmosPatchOperationTypes.Set, mappingPath, false)),
                      Some(errorMessage))
          }
      })

      testParameters.foreach(testParameter => {

          val userConfig = Map(
              "spark.cosmos.write.strategy" -> "ItemBulkUpdate",
              "spark.cosmos.write.bulkUpdate.columnConfigs" -> s"${testParameter.overrideConfigsString}"
          )

          testParameter.isValid match {
              case true =>
                  val writeConfig: CosmosWriteConfig = CosmosWriteConfig.parseWriteConfig(userConfig, schema)
                  writeConfig.patchConfigs.isDefined shouldEqual true

                  val patchConfigs = writeConfig.patchConfigs.get
                  patchConfigs.filter.isDefined shouldEqual false

                  patchConfigs.columnConfigsMap.values.foreach(
                      config => {
                          if (testParameter.columnName == config.columnName && testParameter.overrideColumnConfig.isDefined) {
                              config.mappingPath shouldEqual testParameter.overrideColumnConfig.get.mappingPath
                              config.operationType shouldEqual testParameter.overrideColumnConfig.get.operationType
                          } else {
                              config.mappingPath shouldEqual s"/${config.columnName}"
                              config.operationType shouldEqual defaultPatchOperationType
                          }
                      }
                  )
              case _ =>
                  try {
                      CosmosWriteConfig.parseWriteConfig(userConfig, schema)
                      fail(s"The test should have failed due to ${testParameter.errorMessage.get}")
                  } catch {
                      case e: Exception => e.getMessage should startWith(testParameter.errorMessage.get)
                  }
          }
      })
  }

  "CosmosAccountConfig" should "parse proactiveInitializationConfig config correctly" in {
    val identityAccessor = ImplementationBridgeHelpers
      .CosmosContainerIdentityHelper
      .getCosmosContainerIdentityAccessor
    var userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://boson-test.documents.azure.com:443/",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.proactiveConnectionInitialization" -> "Database/Container",
      "spark.cosmos.proactiveConnectionInitializationDurationInSeconds" -> "120"
    )

    var endpointConfig = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)

    endpointConfig.endpoint shouldEqual sampleProdEndpoint
    endpointConfig.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual "xyz"
    endpointConfig.useGatewayMode shouldEqual false
    endpointConfig.preferredRegionsList.isDefined shouldEqual false
    endpointConfig.proactiveConnectionInitialization.isDefined shouldEqual true
    var containerList = CosmosAccountConfig.parseProactiveConnectionInitConfigs(
      endpointConfig.proactiveConnectionInitialization.get)
    containerList.size() shouldEqual 1
    identityAccessor.getDatabaseName(containerList.get(0)) shouldEqual "Database"
    identityAccessor.getContainerName(containerList.get(0)) shouldEqual "Container"
    containerList.get(0) shouldEqual new CosmosContainerIdentity("Database", "Container")
    endpointConfig.proactiveConnectionInitializationDurationInSeconds shouldEqual 120

    userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://boson-test.documents.azure.com:443/",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.proactiveConnectionInitialization" -> "Database  / Container"
    )

    endpointConfig = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)

    endpointConfig.endpoint shouldEqual sampleProdEndpoint
    endpointConfig.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual "xyz"
    endpointConfig.useGatewayMode shouldEqual false
    endpointConfig.preferredRegionsList.isDefined shouldEqual false
    endpointConfig.proactiveConnectionInitialization.isDefined shouldEqual true
    containerList = CosmosAccountConfig.parseProactiveConnectionInitConfigs(
      endpointConfig.proactiveConnectionInitialization.get)
    containerList.size() shouldEqual 1
    identityAccessor.getDatabaseName(containerList.get(0)) shouldEqual "Database"
    identityAccessor.getContainerName(containerList.get(0)) shouldEqual "Container"
    containerList.get(0) shouldEqual new CosmosContainerIdentity("Database", "Container")
    endpointConfig.proactiveConnectionInitializationDurationInSeconds shouldEqual 120

    userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://boson-test.documents.azure.com:443/",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.proactiveConnectionInitialization" -> "Database  / Container;Db2/C2",
      "spark.cosmos.proactiveConnectionInitializationDurationInSeconds" -> "23"
    )

    endpointConfig = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)

    endpointConfig.endpoint shouldEqual sampleProdEndpoint
    endpointConfig.authConfig.asInstanceOf[CosmosMasterKeyAuthConfig].accountKey shouldEqual "xyz"
    endpointConfig.useGatewayMode shouldEqual false
    endpointConfig.preferredRegionsList.isDefined shouldEqual false
    endpointConfig.proactiveConnectionInitialization.isDefined shouldEqual true
    containerList = CosmosAccountConfig.parseProactiveConnectionInitConfigs(
      endpointConfig.proactiveConnectionInitialization.get)
    containerList.size() shouldEqual 2
    identityAccessor.getDatabaseName(containerList.get(0)) shouldEqual "Database"
    identityAccessor.getContainerName(containerList.get(0)) shouldEqual "Container"
    identityAccessor.getDatabaseName(containerList.get(1)) shouldEqual "Db2"
    identityAccessor.getContainerName(containerList.get(1)) shouldEqual "C2"
    containerList.get(0) shouldEqual new CosmosContainerIdentity("Database", "Container")
    containerList.get(1) shouldEqual new CosmosContainerIdentity("Db2", "C2")
    endpointConfig.proactiveConnectionInitializationDurationInSeconds shouldEqual 23

    userConfig = Map(
      "spark.cosmos.accountEndpoint" -> "https://boson-test.documents.azure.com:443/",
      "spark.cosmos.accountKey" -> "xyz",
      "spark.cosmos.proactiveConnectionInitialization" -> "Invalid\\ConfigText;"
    )

    try {
      CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
      fail("Should have thrown error")
    }
    catch {
      case e: RuntimeException =>
        if (e.getCause.isInstanceOf[IllegalArgumentException]) {
          logInfo("Expected exception", e.getCause)
        } else {
          logError("Unexpected exception", e)

          throw e
        }
    }
  }

  private case class PatchColumnConfigParameterTest
  (
   isValid: Boolean,
   columnName: String,
   overrideConfigsString: String,
   overrideColumnConfig: Option[CosmosPatchColumnConfig] = None,
   errorMessage: Option[String] = None
  )
  //scalastyle:on multiple.string.literals
}
