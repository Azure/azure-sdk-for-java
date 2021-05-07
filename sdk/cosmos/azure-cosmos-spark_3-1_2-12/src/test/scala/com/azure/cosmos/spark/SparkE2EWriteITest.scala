// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.spark.ItemWriteStrategy.{ItemAppend, ItemOverwrite, ItemWriteStrategy}
import org.scalatest.Succeeded

class SparkE2EWriteITest extends IntegrationSpec with Spark with CosmosClient with AutoCleanableCosmosContainer {

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  private case class UpsertParameterTest(bulkEnabled: Boolean, itemWriteStrategy: ItemWriteStrategy, hasId: Boolean = true)

  private val upsertParameterTest = Seq(
    UpsertParameterTest(bulkEnabled = true, itemWriteStrategy = ItemOverwrite),

    UpsertParameterTest(bulkEnabled = false, itemWriteStrategy = ItemOverwrite),
    UpsertParameterTest(bulkEnabled = false, itemWriteStrategy = ItemAppend)
  )

  for (UpsertParameterTest(bulkEnabled, itemWriteStrategy, hasId) <- upsertParameterTest) {
    it should s"support bulkEnabled = ${bulkEnabled} itemWriteStrategy = ${itemWriteStrategy} hasId = ${hasId}" in {
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
