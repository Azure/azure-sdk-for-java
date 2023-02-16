// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.spark.CosmosPatchOperationTypes.CosmosPatchOperationTypes
import com.azure.cosmos.spark.ItemWriteStrategy.ItemWriteStrategy
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.spark.sql.functions.{col, from_json}
import org.apache.spark.sql.types.{MapType, StringType, StructField, StructType}

class SparkE2EWriteITest
  extends IntegrationSpec
    with SparkWithJustDropwizardAndNoSlf4jMetrics
    with CosmosClient
    with AutoCleanableCosmosContainer
    with BasicLoggingTrait
    with MetricAssertions
{

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number
  //scalastyle:off null

  private case class UpsertParameterTest(bulkEnabled: Boolean, itemWriteStrategy: ItemWriteStrategy, hasId: Boolean = true)

  private val upsertParameterTest = Seq(
    UpsertParameterTest(bulkEnabled = true, itemWriteStrategy = ItemWriteStrategy.ItemOverwrite),

    UpsertParameterTest(bulkEnabled = false, itemWriteStrategy = ItemWriteStrategy.ItemOverwrite),
    UpsertParameterTest(bulkEnabled = false, itemWriteStrategy = ItemWriteStrategy.ItemAppend)
  )

  for (UpsertParameterTest(bulkEnabled, itemWriteStrategy, hasId) <- upsertParameterTest) {
    it should s"support upserts with bulkEnabled = $bulkEnabled itemWriteStrategy = $itemWriteStrategy hasId = $hasId" in {
      val cosmosEndpoint = TestConfigurations.HOST
      val cosmosMasterKey = TestConfigurations.MASTER_KEY

      val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.serialization.inclusionMode" -> "NonDefault"
      )

      val cfgOverwrite = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.write.strategy" -> itemWriteStrategy.toString,
        "spark.cosmos.write.bulk.enabled" -> bulkEnabled.toString,
        "spark.cosmos.serialization.inclusionMode" -> "NonDefault"
      )

      val newSpark = getSpark

      // scalastyle:off underscore.import
      // scalastyle:off import.grouping
      import spark.implicits._
      val spark = newSpark
      // scalastyle:on underscore.import
      // scalastyle:on import.grouping

      val df = Seq(
        ("Quark", "Quark", "Red", 1.0 / 2, "")
      ).toDF("particle name", "id", "color", "spin", "empty")

      df.write.format("cosmos.oltp").mode("Append").options(cfg).save()

      val overwriteDf = Seq(
        ("Quark", "Quark", "green", "Yes", ""),
        ("Boson", "Boson", "", "", "")

      ).toDF("particle name", if (hasId) "id" else "no-id", "color", "color charge", "empty")


      try {
        overwriteDf.write.format("cosmos.oltp").mode("Append").options(cfgOverwrite).save()
        hasId shouldBe true
      } catch {
        case _: Exception =>
          hasId shouldBe false
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
      boson.get("color") shouldEqual null
      boson.get("empty") shouldEqual null

      // the item with the same id/pk will be persisted based on the upsert config
      val quarks = queryItems("SELECT * FROM r where r.id = 'Quark'").toArray
      quarks should have size 1

      val quark = quarks(0)
      quark.get("particle name").asText() shouldEqual "Quark"
      quark.get("id").asText() shouldEqual "Quark"
      quark.get("color").asText() shouldEqual (if (itemWriteStrategy == ItemWriteStrategy.ItemOverwrite) "green" else "Red")
      quark.get("empty") shouldEqual null

      quark.has("spin") shouldEqual !(itemWriteStrategy == ItemWriteStrategy.ItemOverwrite)
      if (!(itemWriteStrategy == ItemWriteStrategy.ItemOverwrite)) {
        quark.get("spin").asDouble() shouldEqual 0.5
      }

      if (itemWriteStrategy == ItemWriteStrategy.ItemOverwrite) {
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
      // Gateway requests are not happening always - but they can happen
      // assertMetrics(meterRegistry, "cosmos.client.req.gw", expectedToFind = true)
      assertMetrics(meterRegistry, "cosmos.client.req.rntbd", expectedToFind = true)
      assertMetrics(meterRegistry, "cosmos.client.rntbd", expectedToFind = true)

      // Address resolutions are rather unlikely - but possible - so, no assertions on it
      // assertMetrics(meterRegistry, "cosmos.client.rntbd.addressResolution", expectedToFind = true)
    }
  }

  case class SaveModeTestParameter(saveMode: String, success: Boolean, tableExists: Boolean)

  private val saveModeTestParameters = Seq(
    SaveModeTestParameter("Append", success = true, tableExists = true),

    // non supported scenarios success = false
    SaveModeTestParameter("Append", success = false, tableExists = false), // non-existent container and can't create it
    SaveModeTestParameter("Overwrite", success = false, tableExists = true),
    SaveModeTestParameter("Overwrite", success = false, tableExists = false), // non-existent container and can't create it
    SaveModeTestParameter("Ignore", success = false, tableExists = false),
    SaveModeTestParameter("Ignore", success = false, tableExists = true), // non-existent container and can't create it
    SaveModeTestParameter("ErrorIfExists", success = false, tableExists = true),
    SaveModeTestParameter("ErrorIfExists", success = false, tableExists = false) // non-existent container and can't create it
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

      val newSpark = getSpark

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
        case _: Exception =>
          if (success) {
            fail("expected success")
          }
      }
    }
  }

  private case class PatchParameterTest(bulkEnabled: Boolean, defaultOptionType: CosmosPatchOperationTypes, patchColumnConfigString: String, patchConditionFilter: String)

  private var patchParameterTest = Seq(
    PatchParameterTest(bulkEnabled = true, CosmosPatchOperationTypes.Set, "[col(color).op(replace), col(spin).path(/spin).op(increment)]", "from c where exists(c.color)"),
    PatchParameterTest(bulkEnabled = false, CosmosPatchOperationTypes.Set, "[col(color).op(replace), col(spin).path(/spin).op(increment)]", "from c where exists(c.color)"),
  )

  for (PatchParameterTest(bulkEnabled, patchDefaultOperationType, patchColumnConfigString, patchConditionFilter) <- patchParameterTest) {
    it should s"support patch with bulkEnabled = $bulkEnabled defaultOperationType = $patchDefaultOperationType columnConfigString = $patchColumnConfigString patchConditionFilter = $patchConditionFilter " in {
      val cosmosEndpoint = TestConfigurations.HOST
      val cosmosMasterKey = TestConfigurations.MASTER_KEY

      val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.serialization.inclusionMode" -> "NonDefault"
      )

      val cfgPatch = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.write.strategy" -> ItemWriteStrategy.ItemPatch.toString,
        "spark.cosmos.write.bulk.enabled" -> bulkEnabled.toString,
        "spark.cosmos.write.patch.defaultOperationType" -> patchDefaultOperationType.toString,
        "spark.cosmos.write.patch.columnConfigs" -> patchColumnConfigString
      )

      val newSpark = getSpark

      // scalastyle:off underscore.import
      // scalastyle:off import.grouping
      import spark.implicits._
      val spark = newSpark
      // scalastyle:on underscore.import
      // scalastyle:on import.grouping

      val df = Seq(
        ("Quark", "Quark", "Red", 1.0 / 2, "")
      ).toDF("particle name", "id", "color", "spin", "empty")

      df.write.format("cosmos.oltp").mode("Append").options(cfg).save()

      val patchDf = Seq(
        ("Quark", "green", 0.03)
      ).toDF("id", "color", "spin")

      patchDf.write.format("cosmos.oltp").mode("Append").options(cfgPatch).save()

      // verify data is written
      // wait for a second to allow replication is completed.
      Thread.sleep(1000)

      // the item with the same id/pk will be persisted based on the upsert config
      val quarks = queryItems("SELECT * FROM r where r.id = 'Quark'").toArray
      quarks should have size 1

      val quark = quarks(0)
      quark.get("particle name").asText() shouldEqual "Quark"
      quark.get("id").asText() shouldEqual "Quark"
      quark.get("color").asText() shouldEqual "green"
      quark.get("spin").asDouble() shouldEqual 0.53
    }
  }

  patchParameterTest = Seq(
    PatchParameterTest(bulkEnabled = true, CosmosPatchOperationTypes.Replace, "[col(car).op(replace)]", "from c where exists(c.id)"),
    PatchParameterTest(bulkEnabled = false, CosmosPatchOperationTypes.Replace, "[col(car).op(replace)]", "from c where exists(c.id)"),
    PatchParameterTest(bulkEnabled = true, CosmosPatchOperationTypes.Replace, "[col(car).op(replace).rawJson]", "from c where exists(c.id)"),
    PatchParameterTest(bulkEnabled = false, CosmosPatchOperationTypes.Replace, "[col(car).op(replace).rawJson]", "from c where exists(c.id)"),
  )

  for (PatchParameterTest(bulkEnabled, patchDefaultOperationType, patchColumnConfigString, patchConditionFilter) <- patchParameterTest) {
    it should s"support patch with bulkEnabled = $bulkEnabled defaultOperationType = $patchDefaultOperationType columnConfigString = $patchColumnConfigString patchConditionFilter = $patchConditionFilter " in {
      val cosmosEndpoint = TestConfigurations.HOST
      val cosmosMasterKey = TestConfigurations.MASTER_KEY

      val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.serialization.inclusionMode" -> "NonDefault"
      )

      val cfgPatch = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.write.strategy" -> ItemWriteStrategy.ItemPatch.toString,
        "spark.cosmos.write.bulk.enabled" -> bulkEnabled.toString,
        "spark.cosmos.write.patch.defaultOperationType" -> patchDefaultOperationType.toString,
        "spark.cosmos.write.patch.columnConfigs" -> patchColumnConfigString
      )

      val newSpark = getSpark

      // scalastyle:off underscore.import
      // scalastyle:off import.grouping
      import spark.implicits._
      val spark = newSpark
      // scalastyle:on underscore.import
      // scalastyle:on import.grouping

      val dfWithJson= Seq(
        ("Quark", "Quark", "Red", 1.0 / 2, "", "{ \"manufacturer\": \"BMW\", \"carType\": \"X3\" }")
      ).toDF("particle name", "id", "color", "spin", "empty", "childNodeJson")

      val df = dfWithJson
        .withColumn("car", from_json(col("childNodeJson"), StructType(Array(StructField("manufacturer", StringType, true), StructField("carType", StringType, true)))))
        .drop("childNodeJson")
      df.show(false)
      df.write.format("cosmos.oltp").mode("Append").options(cfg).save()

      // verify data is written
      // wait for a second to allow replication is completed.
      Thread.sleep(1000)

      // the item with the same id/pk will be persisted based on the upsert config
      var quarks = queryItems("SELECT * FROM r where r.id = 'Quark'").toArray
      quarks should have size 1

      var quark = quarks(0)
      quark.get("particle name").asText() shouldEqual "Quark"
      quark.get("id").asText() shouldEqual "Quark"
      quark.get("car").get("carType").asText() shouldEqual "X3"

      val patchDf = if(patchColumnConfigString.endsWith(".rawJson]")) {
        Seq(("Quark", "{ \"manufacturer\": \"BMW\", \"carType\": \"X5\" }"))
          .toDF("id", "car")
      } else {
        Seq(("Quark", "{ \"manufacturer\": \"BMW\", \"carType\": \"X5\" }"))
          .toDF("id", "childNodeJson")
          .withColumn("car", from_json(col("childNodeJson"), StructType(Array(StructField("manufacturer", StringType, true), StructField("carType", StringType, true)))))
          .drop("childNodeJson")
      }

      logInfo(s"Schema of patchDf: ${patchDf.schema}")

      patchDf.write.format("cosmos.oltp").mode("Append").options(cfgPatch).save()

      // verify data is written
      // wait for a second to allow replication is completed.
      Thread.sleep(1000)

      // the item with the same id/pk will be persisted based on the upsert config
      quarks = queryItems("SELECT * FROM r where r.id = 'Quark'").toArray
      quarks should have size 1

      logInfo(s"JSON returned from query: ${quarks(0)}")

      quark = quarks(0)
      quark.get("particle name").asText() shouldEqual "Quark"
      quark.get("id").asText() shouldEqual "Quark"
      quark.get("car").get("carType").asText() shouldEqual "X5"
    }
  }
  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
  //scalastyle:on null
}
