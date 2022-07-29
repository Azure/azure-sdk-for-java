// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util.UUID
import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.composite.CompositeMeterRegistry

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class SparkE2EGatewayChangeFeedITest
  extends IntegrationSpec
    with SparkWithDropwizardAndSlf4jMetrics
    with CosmosGatewayClient
    with CosmosContainerWithRetention
    with BasicLoggingTrait {

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  override def afterEach(): Unit = {
    this.reinitializeContainer()
  }

  "spark change feed micro batch (incremental)" can "use default schema" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    CosmosClientMetrics.meterRegistry.isDefined shouldEqual true
    val meterRegistry = CosmosClientMetrics.meterRegistry.get

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val sinkContainerName = cosmosClient
      .getDatabase(cosmosDatabase)
      .createContainer(s"sink-${UUID.randomUUID().toString}", "/id")
      .block
      .getProperties
      .getId

    val readCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.useGatewayMode" -> "true",
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.read.maxItemCount" -> "200000",
      "spark.cosmos.changeFeed.startFrom" -> "Beginning",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val writeCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.useGatewayMode" -> "true",
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> sinkContainerName,
      "spark.cosmos.write.strategy" -> "ItemOverwrite",
      "spark.cosmos.write.bulk.enabled" -> "true"
    )

    val testId = UUID.randomUUID().toString.replace("-", "")

    for (i <- 1 to 5) {
      for (state <- Array(true, false)) {
        val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
        objectNode.put("name", "Shrodigner's fish")
        objectNode.put("type", "fish")
        objectNode.put("age", 20 + i)
        objectNode.put("isAlive", state)
        objectNode.put("id", UUID.randomUUID().toString)
        container.createItem(objectNode).block()
      }

      val changeFeedDF = spark
        .readStream
        .format("cosmos.oltp.changeFeed")
        .options(readCfg)
        .load()
      val microBatchQuery = changeFeedDF
        .writeStream
        .format("cosmos.oltp")
        .queryName(testId)
        .options(writeCfg)
        .option("checkpointLocation", s"/tmp/$testId/")
        .outputMode("append")
        .start()

      microBatchQuery.processAllAvailable()
      microBatchQuery.stop()
    }

    val validationCfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.useGatewayMode" -> "true",
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> sinkContainerName,
      "spark.cosmos.read.inferSchema.enabled" -> "false",
      "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
    )

    val validationDF = spark
      .read
      .format("cosmos.oltp")
      .options(validationCfg)
      .load()

    val rowCount = validationDF.count()

    rowCount shouldEqual 10

    validationDF
      .show(truncate = false)

    assertMetrics(meterRegistry, "cosmos.client.op.latency", expectedToFind = true)
    assertMetrics(meterRegistry, "cosmos.client.system.avgCpuLoad", expectedToFind = true)
    assertMetrics(meterRegistry, "cosmos.client.req.gw", expectedToFind = true)
    assertMetrics(meterRegistry, "cosmos.client.req.rntbd", expectedToFind = false)
    assertMetrics(meterRegistry, "cosmos.client.rntbd", expectedToFind = false)
  }

  private[this] def assertMetrics(meterRegistry: CompositeMeterRegistry, prefix: String, expectedToFind: Boolean): Unit = {
    meterRegistry.getRegistries.size() > 0 shouldEqual true
    val firstRegistry: MeterRegistry = meterRegistry.getRegistries().toArray()(0).asInstanceOf[MeterRegistry]
    val meters = firstRegistry.getMeters.asScala

    if (expectedToFind) {
      if (meters.size <= 0) {
        logError("No meters found")
      }

      meters.size > 0 shouldEqual true
    }

    val firstMatchedMetersIndex = meters
      .indexWhere(meter => meter.getId.getName.startsWith(prefix))

    if (firstMatchedMetersIndex >= 0 != expectedToFind) {
      logError(s"Matched meter with index $firstMatchedMetersIndex does not reflect expectation $expectedToFind")
    }

    firstMatchedMetersIndex >= 0 shouldEqual expectedToFind
  }

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}
