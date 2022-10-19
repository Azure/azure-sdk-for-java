// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import org.apache.spark.sql.streaming.StreamingQueryListener
import org.apache.spark.sql.streaming.StreamingQueryListener.{QueryProgressEvent, QueryStartedEvent, QueryTerminatedEvent}
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.util.UUID

/** sample test for query */
object SampleStructuredStreamingE2EMain {
  def main(args: Array[String]) {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY
    val cosmosDatabase = "SampleDatabase"
    val cosmosContainer = "GreenTaxiRecords"

    //    val client = new CosmosClientBuilder()
    //      .endpoint(cosmosEndpoint)
    //      .key(cosmosMasterKey)
    //      .consistencyLevel(ConsistencyLevel.EVENTUAL)
    //      .buildAsyncClient()
    //
    //    client.createDatabaseIfNotExists(cosmosDatabase).block()
    //    client.getDatabase(cosmosDatabase).createContainerIfNotExists(cosmosContainer, "/id").block()
    //    client.close()
    val spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .getOrCreate()

    spark.streams.addListener(new StreamingQueryListener() {
      override def onQueryStarted(queryStarted: QueryStartedEvent): Unit = {
        println("Query started: " + queryStarted.id)
      }
      override def onQueryTerminated(queryTerminated: QueryTerminatedEvent): Unit = {
        println("Query terminated: " + queryTerminated.id)
      }
      override def onQueryProgress(queryProgress: QueryProgressEvent): Unit = {
        println("Query made progress: " + queryProgress.progress)
      }
    })

    val changeFeedCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.changeFeed.startFrom" -> "Beginning",
      "spark.cosmos.changeFeed.mode" -> "Incremental",
      "spark.cosmos.changeFeed.itemCountPerTriggerHint" -> "10000",
      "spark.cosmos.preferredRegionsList" -> "[West Europe, North Europe]",
      "spark.cosmos.throughputControl.enabled" -> "true",
      "spark.cosmos.throughputControl.name" -> "CopyReadFromSource",
      "spark.cosmos.throughputControl.targetThroughputThreshold" -> "0.95",
      "spark.cosmos.throughputControl.globalControl.database" -> "SampleDatabase",
      "spark.cosmos.throughputControl.globalControl.container" -> "ThroughputControl",
      "spark.cosmos.clientTelemetry.enabled" -> "true",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive",
      "spark.cosmos.diagnostics" -> "feed"
    )
    val changeFeed_df = spark.readStream.format("cosmos.oltp.changeFeed").options(changeFeedCfg).load()

    changeFeed_df.writeStream
      .foreachBatch { (batchDF: DataFrame, batchId: Long) =>
        batchDF.persist()
        println(s"BatchId: $batchId, Document count: ${batchDF.count()}")
        batchDF.unpersist()
        ()
      }
      .option("checkpointLocation", s"/tmp/checkpoints/${UUID.randomUUID().toString}/")
      .start()
      .awaitTermination()

    spark.close()
  }
}
