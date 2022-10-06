// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.spark.ItemWriteStrategy.ItemWriteStrategy
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait

class SparkE2EGatewayWriteITest
  extends IntegrationSpec
    with SparkWithJustDropwizardAndNoSlf4jMetrics
    with CosmosGatewayClient
    with AutoCleanableCosmosContainer
    with BasicLoggingTrait
    with MetricAssertions
{

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number
  //scalastyle:off null
  private case class DeleteParameterTest
  (
    bulkEnabled: Boolean,
    itemWriteStrategy: ItemWriteStrategy,
    hasId: Boolean = true,
    hasETag: Boolean = true
  )

  private val deleteParameterTest = Seq(
    DeleteParameterTest(bulkEnabled = true, itemWriteStrategy = ItemWriteStrategy.ItemDelete),
    DeleteParameterTest(bulkEnabled = true, itemWriteStrategy = ItemWriteStrategy.ItemDelete, hasETag = false),
    DeleteParameterTest(bulkEnabled = true, itemWriteStrategy = ItemWriteStrategy.ItemDeleteIfNotModified),
    DeleteParameterTest(bulkEnabled = true, itemWriteStrategy = ItemWriteStrategy.ItemDeleteIfNotModified, hasId = false),
    DeleteParameterTest(bulkEnabled = true, itemWriteStrategy = ItemWriteStrategy.ItemDeleteIfNotModified, hasETag = false),

    DeleteParameterTest(bulkEnabled = false, itemWriteStrategy = ItemWriteStrategy.ItemDelete),
    DeleteParameterTest(bulkEnabled = false, itemWriteStrategy = ItemWriteStrategy.ItemDeleteIfNotModified)
  )

  for (DeleteParameterTest(bulkEnabled, itemWriteStrategy, hasId, hasETag) <- deleteParameterTest) {
    it should s"support deletes with bulkEnabled = $bulkEnabled " +
      s"itemWriteStrategy = $itemWriteStrategy hasId = $hasId hasETag = $hasETag" in {

      val cosmosEndpoint = TestConfigurations.HOST
      val cosmosMasterKey = TestConfigurations.MASTER_KEY

      CosmosClientMetrics.meterRegistry.isDefined shouldEqual true
      val meterRegistry = CosmosClientMetrics.meterRegistry.get

      val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.useGatewayMode" -> "true",
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.read.inferSchema.includeSystemProperties" -> "true",
        "spark.cosmos.read.inferSchema.forceNullableProperties" -> "true"
      )

      val cfgDelete = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.useGatewayMode" -> "true",
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.write.strategy" -> itemWriteStrategy.toString,
        "spark.cosmos.write.bulk.enabled" -> bulkEnabled.toString
      )

      val cfgOverwrite = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.useGatewayMode" -> "true",
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.write.strategy" -> "ItemOverWrite",
        "spark.cosmos.write.bulk.enabled" -> bulkEnabled.toString
      )

      val newSpark = getSpark

      // scalastyle:off underscore.import
      // scalastyle:off import.grouping
      import spark.implicits._
      val spark = newSpark
      // scalastyle:on underscore.import
      // scalastyle:on import.grouping

      val df = Seq(
        ("HelloWorld", "HelloWorld", "yellow", 1.0 / 4),
        ("Quark", "Quark", "Red", 1.0 / 2)
      ).toDF("particle name", "id", "color", "spin")

      df.write.format("cosmos.oltp").mode("Append").options(cfg).save()

      // Unless if we use an account with strong consistency there is no guarantee
      // that the write by spark is visible by the client query
      // wait for a second to allow replication is completed.
      Thread.sleep(1000)

      val originalRecords = queryItems("SELECT * FROM r").toArray
      originalRecords should have size 2

      var delete_df = spark.read.format("cosmos.oltp").options(cfg).load().toDF()
      delete_df.persist()
      delete_df.count() shouldEqual 2

      val overwriteDf = Seq(
        ("Quark", "Quark", "green", "Yes"),
        ("Boson", "Boson", "", "")
      ).toDF("particle name", "id", "color", "color charge")

      overwriteDf.write.format("cosmos.oltp").mode("Append").options(cfgOverwrite).save()
      // Unless if we use an account with strong consistency there is no guarantee
      // that the write by spark is visible by the client query
      // wait for a second to allow replication is completed.
      Thread.sleep(1000)

      val afterUpsertRecords = queryItems("SELECT * FROM r").toArray
      afterUpsertRecords should have size 3

      if (!hasId) {
        delete_df = delete_df.withColumnRenamed("id", "_id")
      }

      if (!hasETag) {
        delete_df = delete_df.withColumnRenamed("_etag", "__etag")
      }

      try {
        delete_df.write.format("cosmos.oltp").mode("Append").options(cfgDelete).save()
        hasId shouldBe true
        if (itemWriteStrategy == ItemWriteStrategy.ItemDeleteIfNotModified) {
          hasETag shouldBe true
        }
      } catch {
        case e: Exception =>
          logInfo("EXCEPTION: " + e.getMessage, e)
          !hasId || (itemWriteStrategy == ItemWriteStrategy.ItemDeleteIfNotModified && !hasETag) shouldBe true
      }

      delete_df.unpersist()

      if (hasETag && hasId) {
        // verify data is deleted

        // Unless if we use an account with strong consistency there is no guarantee
        // that the write by spark is visible by the client query
        // wait for a second to allow replication is completed.
        Thread.sleep(1000)

        val afterDelete_df = spark.read.format("cosmos.oltp").options(cfg).load().toDF()

        if (itemWriteStrategy == ItemWriteStrategy.ItemDeleteIfNotModified) {
          // Only the unmodified Record - HelloWorld should be deleted
          afterDelete_df.count() shouldEqual 2
          val bosons = queryItems("SELECT * FROM r where r.id = 'Boson'").toArray
          bosons should have size 1
          val quarks = queryItems("SELECT * FROM r where r.id = 'Quark'").toArray
          quarks should have size 1
        } else {
          // Both original records should be deleted - only Boson should still exist
          afterDelete_df.count() shouldEqual 1
          val bosons = queryItems("SELECT * FROM r where r.id = 'Boson'").toArray
          bosons should have size 1
        }
      }

      assertMetrics(meterRegistry, "cosmos.client.op.latency", expectedToFind = true)
      assertMetrics(meterRegistry, "cosmos.client.system.avgCpuLoad", expectedToFind = true)
      assertMetrics(meterRegistry, "cosmos.client.req.gw", expectedToFind = true)
      assertMetrics(meterRegistry, "cosmos.client.req.rntbd", expectedToFind = false)
      assertMetrics(meterRegistry, "cosmos.client.rntbd", expectedToFind = false)
      assertMetrics(meterRegistry, "cosmos.client.rntbd.addressResolution", expectedToFind = false)
    }
  }
  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
  //scalastyle:on null
}

