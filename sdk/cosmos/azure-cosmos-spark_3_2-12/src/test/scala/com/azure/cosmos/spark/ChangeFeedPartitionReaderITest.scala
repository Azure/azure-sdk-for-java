package com.azure.cosmos.spark

import com.azure.cosmos.{CosmosAsyncContainer, CosmosException}
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, TestConfigurations, Utils}
import com.azure.cosmos.models.{PartitionKey, ThroughputProperties}
import com.azure.cosmos.spark.diagnostics.{BasicLoggingTrait, DiagnosticsContext}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}

import java.util.{Base64, UUID}
import scala.util.Random

class ChangeFeedPartitionReaderITest
 extends IntegrationSpec
  with Spark
  with CosmosClient
  with CosmosContainer
  with BasicLoggingTrait {


 "change feed partition reader" should "honor endLSN during split with lower endLSN than changes" in {
  changeFeedPartitionReaderTest(4)
 }


 "change feed partition reader" should "honor endLSN during split with higher endLSN than changes" in {
  changeFeedPartitionReaderTest(20)
 }

 private[this] def changeFeedPartitionReaderTest(endLSN: Long): Unit = {
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
  val inputtedDocuments = 20
  for (_ <- 0 until inputtedDocuments) {
   ingestTestDocuments(sourceContainer, Random.nextInt())
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
  val diagnosticsConfig = new DiagnosticsConfig(None, false, None)
  val cosmosInputPartition = new CosmosInputPartition(NormalizedRange("", "FF"), Some(endLSN), Some(continuationStateEncoded))
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
   val row = changeFeedPartitionReader.get()
   count += 1
  }

  count shouldEqual Math.min((endLSN - 1) * 2, inputtedDocuments)
 }

 private[this] def initializeAndBroadcastCosmosClientStatesForContainer(config: Map[String, String])
 : Broadcast[CosmosClientMetadataCachesSnapshots] = {
  val userConfig = CosmosConfig.getEffectiveConfig(None, None, config)
  val effectiveUserConfig = CosmosConfig.getEffectiveConfig(None, None, userConfig)
  val cosmosContainerConfig = CosmosContainerConfig.parseCosmosContainerConfig(effectiveUserConfig)
  val readConfig = CosmosReadConfig.parseCosmosReadConfig(effectiveUserConfig)
  val cosmosClientConfig = CosmosClientConfiguration(
   effectiveUserConfig,
   useEventualConsistency = readConfig.forceEventualConsistency,
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
 ): String = {
  val id = UUID.randomUUID().toString
  val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
  objectNode.put("name", "Shrodigner's cat")
  objectNode.put("type", "cat")
  objectNode.put("age", 20)
  objectNode.put("sequenceNumber", sequenceNumber)
  objectNode.put("id", id)
  container.createItem(objectNode).block()

  id
 }

}
