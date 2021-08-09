// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.spark.ItemWriteStrategy.{ItemAppend, ItemDelete, ItemDeleteIfNotModified, ItemOverwrite, ItemWriteStrategy}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.scalatest.Succeeded

class SparkE2EWriteITest
  extends IntegrationSpec
    with Spark
    with CosmosClient
    with AutoCleanableCosmosContainer
    with BasicLoggingTrait {

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  private case class UpsertParameterTest(bulkEnabled: Boolean, itemWriteStrategy: ItemWriteStrategy, hasId: Boolean = true)

  private val upsertParameterTest = Seq(
    UpsertParameterTest(bulkEnabled = true, itemWriteStrategy = ItemOverwrite),

    UpsertParameterTest(bulkEnabled = false, itemWriteStrategy = ItemOverwrite),
    UpsertParameterTest(bulkEnabled = false, itemWriteStrategy = ItemAppend)
  )

  for (UpsertParameterTest(bulkEnabled, itemWriteStrategy, hasId) <- upsertParameterTest) {
    it should s"support upserts with bulkEnabled = ${bulkEnabled} itemWriteStrategy = ${itemWriteStrategy} hasId = ${hasId}" in {
      val cosmosEndpoint = TestConfigurations.HOST
      val cosmosMasterKey = TestConfigurations.MASTER_KEY

      val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer
      )

      val cfgOverwrite = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.write.strategy" -> itemWriteStrategy.toString,
        "spark.cosmos.write.bulk.enabled" -> bulkEnabled.toString
      )

      val newSpark = getSpark()

      // scalastyle:off underscore.import
      // scalastyle:off import.grouping
      import spark.implicits._
      val spark = newSpark
      // scalastyle:on underscore.import
      // scalastyle:on import.grouping

      val df = Seq(
        ("Quark", "Quark", "Red", 1.0 / 2)
      ).toDF("particle name", "id", "color", "spin")

      df.write.format("cosmos.oltp").mode("Append").options(cfg).save()

      val overwriteDf = Seq(
        ("Quark", "Quark", "green", "Yes"),
        ("Boson", "Boson", "", "")

      ).toDF("particle name", if (hasId) "id" else "no-id", "color", "color charge")


      try {
        overwriteDf.write.format("cosmos.oltp").mode("Append").options(cfgOverwrite).save()
        hasId shouldBe true
      } catch {
        case e: Exception => {
          hasId shouldBe false
          Succeeded
        }
      }

      // verify data is written

      // TODO: moderakh note unless if we use an account with strong consistency there is no guarantee
      // that the write by spark is visible by the client query
      // wait for a second to allow replication is completed.
      Thread.sleep(1000)

      // the new item will be always persisted
      val bosons = queryItems("SELECT * FROM r where r.id = 'Boson'").toArray
      bosons should have size 1
      val boson = bosons(0)
      boson.get("id").asText() shouldEqual "Boson"
      boson.get("color").asText() shouldEqual ""

      // the item with the same id/pk will be persisted based on the upsert config
      val quarks = queryItems("SELECT * FROM r where r.id = 'Quark'").toArray
      quarks should have size 1

      val quark = quarks(0)
      quark.get("particle name").asText() shouldEqual "Quark"
      quark.get("id").asText() shouldEqual "Quark"
      quark.get("color").asText() shouldEqual (if (itemWriteStrategy == ItemOverwrite) "green" else "Red")

      quark.has("spin") shouldEqual !(itemWriteStrategy == ItemOverwrite)
      if (!(itemWriteStrategy == ItemOverwrite)) {
        quark.get("spin").asDouble() shouldEqual 0.5
      }

      if ((itemWriteStrategy == ItemOverwrite)) {
        quark.get("color charge").asText() shouldEqual "Yes"
      } else {
        quark.has("color charge") shouldEqual false
      }
    }
  }

  private case class DeleteParameterTest
  (
    bulkEnabled: Boolean,
    itemWriteStrategy: ItemWriteStrategy,
    hasId: Boolean = true,
    hasETag: Boolean = true
  )

  private val deleteParameterTest = Seq(
    DeleteParameterTest(bulkEnabled = true, itemWriteStrategy = ItemDelete),
    DeleteParameterTest(bulkEnabled = true, itemWriteStrategy = ItemDelete, true, false),
    DeleteParameterTest(bulkEnabled = true, itemWriteStrategy = ItemDeleteIfNotModified),
    DeleteParameterTest(bulkEnabled = true, itemWriteStrategy = ItemDeleteIfNotModified, false, true),
    DeleteParameterTest(bulkEnabled = true, itemWriteStrategy = ItemDeleteIfNotModified, true, false),

    DeleteParameterTest(bulkEnabled = false, itemWriteStrategy = ItemDelete),
    DeleteParameterTest(bulkEnabled = false, itemWriteStrategy = ItemDeleteIfNotModified)
  )

  for (DeleteParameterTest(bulkEnabled, itemWriteStrategy, hasId, hasETag) <- deleteParameterTest) {
    it should s"support deletes with bulkEnabled = ${bulkEnabled} " +
      s"itemWriteStrategy = ${itemWriteStrategy} hasId = ${hasId} hasETag = $hasETag" in {

      val cosmosEndpoint = TestConfigurations.HOST
      val cosmosMasterKey = TestConfigurations.MASTER_KEY

      val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.read.inferSchema.includeSystemProperties" -> "true",
        "spark.cosmos.read.inferSchema.forceNullableProperties" -> "true"
      )

      val cfgDelete = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.write.strategy" -> itemWriteStrategy.toString,
        "spark.cosmos.write.bulk.enabled" -> bulkEnabled.toString
      )

      val cfgOverwrite = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.write.strategy" -> "ItemOverWrite",
        "spark.cosmos.write.bulk.enabled" -> bulkEnabled.toString
      )

      val newSpark = getSpark()

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
        if (itemWriteStrategy == ItemDeleteIfNotModified) {
          hasETag shouldBe true
        }
      } catch {
        case e: Exception => {
          logInfo("EXCEPTION: " + e.getMessage, e)
          !hasId || (itemWriteStrategy == ItemDeleteIfNotModified && !hasETag) shouldBe true
          Succeeded
        }
      }

      delete_df.unpersist()

      if (hasETag && hasId) {
        // verify data is deleted

        // Unless if we use an account with strong consistency there is no guarantee
        // that the write by spark is visible by the client query
        // wait for a second to allow replication is completed.
        Thread.sleep(1000)

        val afterDelete_df = spark.read.format("cosmos.oltp").options(cfg).load().toDF()

        if (itemWriteStrategy == ItemDeleteIfNotModified) {
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
    }
  }

  case class SaveModeTestParameter(saveMode: String, success: Boolean, tableExists: Boolean)

  private val saveModeTestParameters = Seq(
    SaveModeTestParameter("Append", success = true, true),

    // non supported scenarios success = false
    SaveModeTestParameter("Append", success = false, false), // non-existent container and can't create it
    SaveModeTestParameter("Overwrite", success = false, true),
    SaveModeTestParameter("Overwrite", success = false, false), // non-existent container and can't create it
    SaveModeTestParameter("Ignore", success = false, false),
    SaveModeTestParameter("Ignore", success = false, true), // non-existent container and can't create it
    SaveModeTestParameter("ErrorIfExists", success = false, true),
    SaveModeTestParameter("ErrorIfExists", success = false, false) // non-existent container and can't create it
  )

  for (SaveModeTestParameter(saveMode, success, tableExists) <- saveModeTestParameters) {
    s"write with $saveMode" should
      s"${if (success) "success" else "fail"} when table ${if (tableExists) "exists" else "doesn't exist"}" in {
      val cosmosEndpoint = TestConfigurations.HOST
      val cosmosMasterKey = TestConfigurations.MASTER_KEY

      val cosmosContainerName = if (tableExists) cosmosContainer else "unknown"

      val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainerName
      )

      val newSpark = getSpark()

      // scalastyle:off underscore.import
      // scalastyle:off import.grouping
      import spark.implicits._
      val spark = newSpark
      // scalastyle:on underscore.import
      // scalastyle:on import.grouping

      val df = Seq(
        (299792458, "speed of light")
      ).toDF("number", "id")
      df.printSchema()

      try {
        df.write.format("cosmos.oltp").mode(saveMode).options(cfg).save()
        if (!success) {
          fail("expected failure")
        }

      } catch {
        case e: Exception =>
          if (success) {
            fail("expected success")
          }
      }
    }
  }
  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}
