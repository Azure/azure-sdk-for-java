// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils
import com.azure.cosmos.{CosmosAsyncContainer, CosmosException}
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, TestConfigurations, Utils}
import com.azure.cosmos.models.{PartitionKey, ThroughputProperties}
import com.azure.cosmos.spark.diagnostics.DiagnosticsContext
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}

import java.lang.Thread.sleep
import java.util.{Base64, UUID}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class ChangeFeedPartitionReaderITest
 extends IntegrationSpec
  with Spark
  with CosmosContainer
  with CosmosClient {


 "change feed partition reader" should "honor endLSN during split" in {
  val cosmosEndpoint = TestConfigurations.HOST
  val cosmosMasterKey = TestConfigurations.MASTER_KEY
  val testId = UUID.randomUUID().toString
  val sourceContainerResponse = cosmosClient.getDatabase(cosmosDatabase).createContainer(
   "source_" + testId,
   "/sequenceNumber",
   ThroughputProperties.createManualThroughput(11000)).block()
  val sourceContainer = cosmosClient.getDatabase(cosmosDatabase).getContainer(sourceContainerResponse.getProperties.getId)
  val rid = sourceContainerResponse.getProperties.getResourceId
  val continuationState = s"""{
  "V": 1,
  "Rid": "$rid",
  "Mode": "INCREMENTAL",
  "StartFrom": {
    "Type": "BEGINNING"
  },
  "Continuation": {
    "V": 1,
    "Rid": "$rid",
    "Continuation": [
      {
        "token": "1",
        "range": {
          "min": "",
          "max": "FF"
        }
      }
    ],
    "Range": {
      "min": "",
      "max": "FF"
    }
  }
}"""
  val encoder = Base64.getEncoder
  val encodedBytes = encoder.encode(continuationState.getBytes("UTF-8"))
  val continuationStateEncoded = new String(encodedBytes, "UTF-8")

  val changeFeedCfg = Map(
   "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
   "spark.cosmos.accountKey" -> cosmosMasterKey,
   "spark.cosmos.database" -> cosmosDatabase,
   "spark.cosmos.container" -> sourceContainer.getId(),
   "spark.cosmos.read.inferSchema.enabled" -> "false",
  )
  var inputtedDocuments = 10
  var lsn1 = 0L
  var lsn2 = 0L
  for (_ <- 0 until inputtedDocuments) {
   lsn1 = ingestTestDocuments(sourceContainer, 1)
   lsn2 = ingestTestDocuments(sourceContainer, 2)
  }
  inputtedDocuments *= 2

  while (lsn1 != lsn2) {
   if (lsn1 < lsn2) {
    lsn1 = ingestTestDocuments(sourceContainer, 1)
   } else {
    lsn2 = ingestTestDocuments(sourceContainer, 2)
   }
   inputtedDocuments += 1
  }

  val structs = Array(
   StructField("_rawBody", StringType, false),
   StructField("_etag", StringType, false),
   StructField("_ts", StringType, false),
   StructField("id", StringType, false),
   StructField("_lsn", StringType, false)
  )
  val schema = new StructType(structs)

  val diagnosticContext = DiagnosticsContext(UUID.randomUUID(), "")
  val cosmosClientStateHandles = initializeAndBroadcastCosmosClientStatesForContainer(changeFeedCfg)
  val diagnosticsConfig = DiagnosticsConfig()
  val cosmosInputPartition = new CosmosInputPartition(NormalizedRange("", "FF"), Some(lsn1),
   Some(continuationStateEncoded))
  val changeFeedPartitionReader = new ChangeFeedPartitionReader(
   cosmosInputPartition,
   changeFeedCfg,
   schema,
   diagnosticContext,
   cosmosClientStateHandles,
   diagnosticsConfig,
   ""
  )
  var count = 0

  while (changeFeedPartitionReader.next()) {
   changeFeedPartitionReader.get()
   count += 1
  }

  count shouldEqual inputtedDocuments
 }


 "change feed partition reader" should "honor endLSN during split and should hang" in {
  val cosmosEndpoint = TestConfigurations.HOST
  val cosmosMasterKey = TestConfigurations.MASTER_KEY
  val testId = UUID.randomUUID().toString
  val sourceContainerResponse = cosmosClient.getDatabase(cosmosDatabase).createContainer(
   "source_" + testId,
   "/sequenceNumber",
   ThroughputProperties.createManualThroughput(11000)).block()
  val sourceContainer = cosmosClient.getDatabase(cosmosDatabase).getContainer(sourceContainerResponse.getProperties.getId)
  val rid = sourceContainerResponse.getProperties.getResourceId
  val continuationState = s"""{
  "V": 1,
  "Rid": "$rid",
  "Mode": "INCREMENTAL",
  "StartFrom": {
    "Type": "BEGINNING"
  },
  "Continuation": {
    "V": 1,
    "Rid": "$rid",
    "Continuation": [
      {
        "token": "1",
        "range": {
          "min": "",
          "max": "FF"
        }
      }
    ],
    "Range": {
      "min": "",
      "max": "FF"
    }
  }
}"""
  val encoder = Base64.getEncoder
  val encodedBytes = encoder.encode(continuationState.getBytes("UTF-8"))
  val continuationStateEncoded = new String(encodedBytes, "UTF-8")

  val changeFeedCfg = Map(
   "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
   "spark.cosmos.accountKey" -> cosmosMasterKey,
   "spark.cosmos.database" -> cosmosDatabase,
   "spark.cosmos.container" -> sourceContainer.getId(),
   "spark.cosmos.read.inferSchema.enabled" -> "false",
  )
  var inputtedDocuments = 10
  var lsn1 = 0L
  var lsn2 = 0L
  for (_ <- 0 until inputtedDocuments) {
   lsn1 = ingestTestDocuments(sourceContainer, 1)
   lsn2 = ingestTestDocuments(sourceContainer, 2)
  }
  inputtedDocuments *= 2

  val structs = Array(
   StructField("_rawBody", StringType, false),
   StructField("_etag", StringType, false),
   StructField("_ts", StringType, false),
   StructField("id", StringType, false),
   StructField("_lsn", StringType, false)
  )
  val schema = new StructType(structs)

  val diagnosticContext = DiagnosticsContext(UUID.randomUUID(), "")
  val cosmosClientStateHandles = initializeAndBroadcastCosmosClientStatesForContainer(changeFeedCfg)
  val diagnosticsConfig = DiagnosticsConfig()
  val cosmosInputPartition = new CosmosInputPartition(NormalizedRange("", "FF"), Some(Math.max(lsn1, lsn2) + 1)
   , Some(continuationStateEncoded))
  val changeFeedPartitionReader = new ChangeFeedPartitionReader(
   cosmosInputPartition,
   changeFeedCfg,
   schema,
   diagnosticContext,
   cosmosClientStateHandles,
   diagnosticsConfig,
   ""
  )
  var count = 0
  implicit val ec: ExecutionContext = ExecutionContext.global
  val future = Future {
   while (changeFeedPartitionReader.next()) {
    changeFeedPartitionReader.get()
    count += 1
   }
  }
  sleep(2000)
  future.isCompleted shouldEqual false
  while (lsn1 != lsn2) {
   if (lsn1 < lsn2) {
    lsn1 = ingestTestDocuments(sourceContainer, 1)
   } else {
    lsn2 = ingestTestDocuments(sourceContainer, 2)
   }
   inputtedDocuments += 1
  }
  for (_ <- 0 until 15) {
   ingestTestDocuments(sourceContainer, Random.nextInt())
  }
  future.isCompleted shouldEqual true
  count shouldEqual inputtedDocuments + 2

 }

 "change feed partition reader" should "report custom metrics" in {
  val cosmosEndpoint = TestConfigurations.HOST
  val cosmosMasterKey = TestConfigurations.MASTER_KEY
  val testId = UUID.randomUUID().toString
  val sourceContainerResponse = cosmosClient.getDatabase(cosmosDatabase).createContainer(
   "source_" + testId,
   "/sequenceNumber",
   ThroughputProperties.createManualThroughput(400)).block()
  val sourceContainer = cosmosClient.getDatabase(cosmosDatabase).getContainer(sourceContainerResponse.getProperties.getId)
  val rid = sourceContainerResponse.getProperties.getResourceId
  val continuationState = s"""{
  "V": 1,
  "Rid": "$rid",
  "Mode": "INCREMENTAL",
  "StartFrom": {
    "Type": "BEGINNING"
  },
  "Continuation": {
    "V": 1,
    "Rid": "$rid",
    "Continuation": [
      {
        "token": "1",
        "range": {
          "min": "",
          "max": "FF"
        }
      }
    ],
    "Range": {
      "min": "",
      "max": "FF"
    }
  }
}"""
  val encoder = Base64.getEncoder
  val encodedBytes = encoder.encode(continuationState.getBytes("UTF-8"))
  val continuationStateEncoded = new String(encodedBytes, "UTF-8")

  val changeFeedCfg = Map(
   "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
   "spark.cosmos.accountKey" -> cosmosMasterKey,
   "spark.cosmos.database" -> cosmosDatabase,
   "spark.cosmos.container" -> sourceContainer.getId(),
   "spark.cosmos.read.inferSchema.enabled" -> "false",
  )
  var inputtedDocuments = 10
  var lsn1 = 0L
  for (_ <- 0 until inputtedDocuments) {
   lsn1 = ingestTestDocuments(sourceContainer, 1)
  }

  val structs = Array(
   StructField("_rawBody", StringType, false),
   StructField("_etag", StringType, false),
   StructField("_ts", StringType, false),
   StructField("id", StringType, false),
   StructField("_lsn", StringType, false)
  )
  val schema = new StructType(structs)

  val diagnosticContext = DiagnosticsContext(UUID.randomUUID(), "")
  val cosmosClientStateHandles = initializeAndBroadcastCosmosClientStatesForContainer(changeFeedCfg)
  val diagnosticsConfig = DiagnosticsConfig()
  val cosmosInputPartition =
   new CosmosInputPartition(
    NormalizedRange("", "FF"),
    Some(lsn1 + 1),
    Some(continuationStateEncoded),
    index = Some(2))
  val changeFeedPartitionReader = new ChangeFeedPartitionReader(
   cosmosInputPartition,
   changeFeedCfg,
   schema,
   diagnosticContext,
   cosmosClientStateHandles,
   diagnosticsConfig,
   ""
  )
  var count = 0
  implicit val ec: ExecutionContext = ExecutionContext.global
  Future {
   while (changeFeedPartitionReader.next()) {
    changeFeedPartitionReader.get()
    count += 1
   }
  }
  sleep(2000)

  val currentMetrics = changeFeedPartitionReader.currentMetricsValues()
  currentMetrics.length shouldBe 3
  currentMetrics(0).name() shouldBe CosmosConstants.MetricNames.ChangeFeedLsnRange
  currentMetrics(0).value() shouldBe(lsn1 - 1)
  currentMetrics(1).name() shouldBe CosmosConstants.MetricNames.ChangeFeedItemsCnt
  currentMetrics(1).value() shouldBe 10
  currentMetrics(2).name() shouldBe CosmosConstants.MetricNames.ChangeFeedPartitionIndex
  currentMetrics(2).value() shouldBe 2
 }

 private[this] def initializeAndBroadcastCosmosClientStatesForContainer(config: Map[String, String])
 : Broadcast[CosmosClientMetadataCachesSnapshots] = {
  val userConfig = CosmosConfig.getEffectiveConfig(None, None, config)
  val effectiveUserConfig = CosmosConfig.getEffectiveConfig(None, None, userConfig)
  val cosmosContainerConfig = CosmosContainerConfig.parseCosmosContainerConfig(effectiveUserConfig)
  val readConfig = CosmosReadConfig.parseCosmosReadConfig(effectiveUserConfig)
  val cosmosClientConfig = CosmosClientConfiguration(
   effectiveUserConfig,
   readConsistencyStrategy = readConfig.readConsistencyStrategy,
   "")
  val calledFrom = s"ChangeFeedPartitionReaderTest.initializeAndBroadcastCosmosClientStateForContainer"
  Loan(
   List[Option[CosmosClientCacheItem]](
    Some(CosmosClientCache(
     cosmosClientConfig,
     None,
     calledFrom)),
    ThroughputControlHelper.getThroughputControlClientCacheItem(
     effectiveUserConfig,
     calledFrom,
     None,
     ""
    )
   ))
   .to(clientCacheItems => {
    val container =
     ThroughputControlHelper.getContainer(
      effectiveUserConfig,
      cosmosContainerConfig,
      clientCacheItems(0).get,
      clientCacheItems(1))

    try {
     container.readItem(
       UUID.randomUUID().toString, new PartitionKey(UUID.randomUUID().toString), classOf[ObjectNode])
      .block()
    } catch {
     case _: CosmosException =>
    }

    val state = new CosmosClientMetadataCachesSnapshot()
    state.serialize(clientCacheItems(0).get.cosmosClient)

    var throughputControlState: Option[CosmosClientMetadataCachesSnapshot] = None
    if (clientCacheItems(1).isDefined) {
     throughputControlState = Some(new CosmosClientMetadataCachesSnapshot())
     throughputControlState.get.serialize(clientCacheItems(1).get.cosmosClient)
    }

    val metadataSnapshots = CosmosClientMetadataCachesSnapshots(state, throughputControlState)
    val sparkSession = SparkSession.active
    sparkSession.sparkContext.broadcast(metadataSnapshots)
   })
 }


 private[this] def ingestTestDocuments
 (
  container: CosmosAsyncContainer,
  sequenceNumber: Int
 ): Long = {
  val id = UUID.randomUUID().toString
  val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
  objectNode.put("name", "Shrodigner's cat")
  objectNode.put("type", "cat")
  objectNode.put("age", 20)
  objectNode.put("sequenceNumber", sequenceNumber)
  objectNode.put("id", id)
  val response = container.createItem(objectNode).block()

  getLSN(response.getSessionToken)
 }

 private[this] def getLSN(sessionToken: String): Long = {
  val parsedSessionToken = sessionToken.substring(sessionToken.indexOf(":"))
  val segments = StringUtils.split(parsedSessionToken, "#")
  var latestLsn = segments(0)
  if (segments.length >= 2) {
   // default to Global LSN
   latestLsn = segments(1)
  }

  latestLsn.toLong
 }

}
