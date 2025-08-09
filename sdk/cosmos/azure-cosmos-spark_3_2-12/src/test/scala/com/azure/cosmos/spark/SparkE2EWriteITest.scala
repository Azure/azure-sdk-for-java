// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.models.ThroughputProperties
import com.azure.cosmos.spark.CosmosPatchOperationTypes.CosmosPatchOperationTypes
import com.azure.cosmos.spark.ItemWriteStrategy.ItemWriteStrategy
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.spark.scheduler.{AccumulableInfo, SparkListener, SparkListenerTaskEnd}
import org.apache.spark.sql.functions.{col, from_json}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.concurrent.Waiters.{interval, timeout}
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import java.util.UUID

class SparkE2EWriteITest
  extends IntegrationSpec
    with SparkWithMetrics
    with CosmosClient
    with AutoCleanableCosmosContainer
    with BasicLoggingTrait
    with MetricAssertions
{

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number
  //scalastyle:off null

  private case class UpsertParameterTest(
                                          bulkEnabled: Boolean,
                                          itemWriteStrategy: ItemWriteStrategy,
                                          hasId: Boolean = true,
                                          initialBatchSize: Option[Int] = None,
                                          maxBatchSize: Option[Int] = None,
                                          minTargetBatchSize: Option[Int] = None)

  private val upsertParameterTest = Seq(
    UpsertParameterTest(bulkEnabled = true, itemWriteStrategy = ItemWriteStrategy.ItemOverwrite, initialBatchSize = None, maxBatchSize = None, minTargetBatchSize = None),
    UpsertParameterTest(bulkEnabled = true, itemWriteStrategy = ItemWriteStrategy.ItemOverwrite, initialBatchSize = Some(1), maxBatchSize = None, minTargetBatchSize = None),
    UpsertParameterTest(bulkEnabled = true, itemWriteStrategy = ItemWriteStrategy.ItemOverwrite, initialBatchSize = Some(1), maxBatchSize = Some(5), minTargetBatchSize = None),
    UpsertParameterTest(bulkEnabled = true, itemWriteStrategy = ItemWriteStrategy.ItemOverwrite, initialBatchSize = Some(1), maxBatchSize = Some(5), minTargetBatchSize = Some(2)),
    UpsertParameterTest(bulkEnabled = false, itemWriteStrategy = ItemWriteStrategy.ItemOverwrite, initialBatchSize = None, maxBatchSize = None, minTargetBatchSize = None),
    UpsertParameterTest(bulkEnabled = false, itemWriteStrategy = ItemWriteStrategy.ItemAppend, initialBatchSize = None, maxBatchSize = None, minTargetBatchSize = None)
  )

  for (UpsertParameterTest(bulkEnabled, itemWriteStrategy, hasId, initialBatchSize, maxBatchSize, minTargetBatchSize) <- upsertParameterTest) {
    it should s"support upserts with bulkEnabled = $bulkEnabled itemWriteStrategy = $itemWriteStrategy hasId = $hasId initialBatchSize = $initialBatchSize, maxBatchSize = $maxBatchSize, minTargetBatchSize = $minTargetBatchSize" in {
      val cosmosEndpoint = TestConfigurations.HOST
      val cosmosMasterKey = TestConfigurations.MASTER_KEY

      val configMapBuilder = scala.collection.mutable.Map(
        "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.serialization.inclusionMode" -> "NonDefault"
      )

      val configOverrideMapBuilder = scala.collection.mutable.Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.write.strategy" -> itemWriteStrategy.toString,
        "spark.cosmos.write.bulk.enabled" -> bulkEnabled.toString,
        "spark.cosmos.serialization.inclusionMode" -> "NonDefault"
      )

      initialBatchSize match {
        case Some(customInitialBatchSize) =>
          configMapBuilder += (
            "spark.cosmos.write.bulk.initialBatchSize" -> customInitialBatchSize.toString,
          )

          configOverrideMapBuilder += (
            "spark.cosmos.write.bulk.initialBatchSize" -> customInitialBatchSize.toString,
            )
        case None =>
      }

      maxBatchSize match {
        case Some(customMaxBatchSize) =>
          configMapBuilder += (
            "spark.cosmos.write.bulk.maxBatchSize" -> customMaxBatchSize.toString,
            )

          configOverrideMapBuilder += (
            "spark.cosmos.write.bulk.maxBatchSize" -> customMaxBatchSize.toString,
            )
        case None =>
      }

      minTargetBatchSize match {
        case Some(customMinTargetBatchSize) =>
          configMapBuilder += (
            "spark.cosmos.write.bulk.minTargetBatchSize" -> customMinTargetBatchSize.toString,
            )

          configOverrideMapBuilder += (
            "spark.cosmos.write.bulk.minTargetBatchSize" -> customMinTargetBatchSize.toString,
            )
        case None =>
      }

      val cfg = configMapBuilder.toMap

      val cfgOverwrite = configOverrideMapBuilder.toMap

      val newSpark = getSpark

      // scalastyle:off underscore.import
      // scalastyle:off import.grouping
      import spark.implicits._
      val spark = newSpark
      // scalastyle:on underscore.import
      // scalastyle:on import.grouping

      val df = Seq(
        ("Quark", "Quark", "Red", 1.0 / 2, ""),
      ).toDF("particle name", "id", "color", "spin", "empty")

      var bytesWrittenSnapshot = 0L
      var recordsWrittenSnapshot = 0L
      var totalRequestChargeSnapshot: Option[AccumulableInfo] = None

      val statusStore = spark.sharedState.statusStore
      val oldCount = statusStore.executionsCount()

      spark.sparkContext
        .addSparkListener(
          new SparkListener {
            override def onTaskEnd(taskEnd: SparkListenerTaskEnd): Unit = {
              val outputMetrics = taskEnd.taskMetrics.outputMetrics
              logInfo(s"ON_TASK_END - Records written: ${outputMetrics.recordsWritten}, " +
                s"Bytes written: ${outputMetrics.bytesWritten}, " +
                s"${taskEnd.taskInfo.accumulables.mkString(", ")}")
              bytesWrittenSnapshot = outputMetrics.bytesWritten

              recordsWrittenSnapshot = outputMetrics.recordsWritten

              taskEnd
                .taskInfo
                .accumulables
                .filter(accumulableInfo => accumulableInfo.name.isDefined &&
                  accumulableInfo.name.get.equals(CosmosConstants.MetricNames.TotalRequestCharge))
                .foreach(
                  accumulableInfo => {
                    totalRequestChargeSnapshot = Some(accumulableInfo)
                  }
                )
            }
          })

      df.write.format("cosmos.oltp").mode("Append").options(cfg).save()

      // Wait until the new execution is started and being tracked.
      eventually(timeout(10.seconds), interval(10.milliseconds)) {
        assert(statusStore.executionsCount() > oldCount)
      }

      // Wait for listener to finish computing the metrics for the execution.
      eventually(timeout(10.seconds), interval(10.milliseconds)) {
        assert(statusStore.executionsList().nonEmpty &&
          statusStore.executionsList().last.metricValues != null)
      }

      recordsWrittenSnapshot shouldEqual 1
      bytesWrittenSnapshot > 0 shouldEqual  true
      if (!spark.sparkContext.version.startsWith("3.1.")) {
        totalRequestChargeSnapshot.isDefined shouldEqual true
      }

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

      // TODO (kuthapar) to investigate this
      // assertMetrics(meterRegistry, "cosmos.client.op.latency", expectedToFind = true)
      // assertMetrics(meterRegistry, "cosmos.client.system.avgCpuLoad", expectedToFind = true)
      // Gateway requests are not happening always - but they can happen
      // assertMetrics(meterRegistry, "cosmos.client.req.gw", expectedToFind = true)
      // assertMetrics(meterRegistry, "cosmos.client.req.rntbd", expectedToFind = true)
      // assertMetrics(meterRegistry, "cosmos.client.rntbd", expectedToFind = true)

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
        .withColumn("car", from_json(col("childNodeJson"), StructType(Array(StructField("manufacturer", StringType, nullable = true), StructField("carType", StringType, nullable = true)))))
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
          .withColumn("car", from_json(col("childNodeJson"), StructType(Array(StructField("manufacturer", StringType, nullable = true), StructField("carType", StringType, nullable = true)))))
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

  private case class PatchBulkUpdateParameterTest(bulkEnabled: Boolean, patchColumnConfigString: String)

  private var patchBulkUpdateParameterTest = Seq(
      PatchBulkUpdateParameterTest(bulkEnabled = true, "[col(color), col(spin).path(/spin)]"),
      PatchBulkUpdateParameterTest(bulkEnabled = false, "[col(color), col(spin).path(/spin)]"),
  )

  for (PatchBulkUpdateParameterTest(bulkEnabled, patchColumnConfigString) <- patchBulkUpdateParameterTest) {
      it should s"support patch bulk update with bulkEnabled = $bulkEnabled columnConfigString = $patchColumnConfigString" in {
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
              "spark.cosmos.write.strategy" -> ItemWriteStrategy.ItemBulkUpdate.toString,
              "spark.cosmos.write.bulk.enabled" -> bulkEnabled.toString,
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

          val patchBulkUpdateDf = Seq(
              ("Quark", "green", 0.03)
          ).toDF("id", "color", "spin")

          patchBulkUpdateDf.write.format("cosmos.oltp").mode("Append").options(cfgPatch).save()

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
          quark.get("spin").asDouble() shouldEqual 0.03
      }
  }

  patchBulkUpdateParameterTest = Seq(
      PatchBulkUpdateParameterTest(bulkEnabled = true, "[col(car)]"),
      PatchBulkUpdateParameterTest(bulkEnabled = false, "[col(car)]"),
      PatchBulkUpdateParameterTest(bulkEnabled = true, "[col(car).rawJson]"),
      PatchBulkUpdateParameterTest(bulkEnabled = false, "[col(car).rawJson]")
  )

  for (PatchBulkUpdateParameterTest(bulkEnabled, patchColumnConfigString) <- patchBulkUpdateParameterTest) {
      it should s"support patch with bulkEnabled = $bulkEnabled columnConfigString = $patchColumnConfigString" in {
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
              "spark.cosmos.write.strategy" -> ItemWriteStrategy.ItemBulkUpdate.toString,
              "spark.cosmos.write.bulk.enabled" -> bulkEnabled.toString,
              "spark.cosmos.write.bulkUpdate.columnConfigs" -> patchColumnConfigString
          )

          val newSpark = getSpark

          // scalastyle:off underscore.import
          // scalastyle:off import.grouping
          import spark.implicits._
          val spark = newSpark
          // scalastyle:on underscore.import
          // scalastyle:on import.grouping

          val dfWithJson = Seq(
              ("Quark", "Quark", "Red", 1.0 / 2, "", "{ \"manufacturer\": \"BMW\", \"carType\": \"X3\" }")
          ).toDF("particle name", "id", "color", "spin", "empty", "childNodeJson")

          val df = dfWithJson
              .withColumn("car", from_json(col("childNodeJson"), StructType(Array(StructField("manufacturer", StringType, nullable = true), StructField("carType", StringType, nullable = true)))))
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

          val patchDf = if (patchColumnConfigString.endsWith(".rawJson]")) {
              Seq(("Quark", "{ \"manufacturer\": \"BMW\", \"carType\": \"X5\" }"))
                  .toDF("id", "car")
          } else {
              Seq(("Quark", "{ \"manufacturer\": \"BMW\", \"carType\": \"X5\" }"))
                  .toDF("id", "childNodeJson")
                  .withColumn("car", from_json(col("childNodeJson"), StructType(Array(StructField("manufacturer", StringType, nullable = true), StructField("carType", StringType, nullable = true)))))
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

  patchBulkUpdateParameterTest = Seq(
    PatchBulkUpdateParameterTest(bulkEnabled = true, ""),
    PatchBulkUpdateParameterTest(bulkEnabled = false, "")
  )

  for (PatchBulkUpdateParameterTest(bulkEnabled, _) <- patchBulkUpdateParameterTest) {
    it should s"support patch with pk != id with bulkEnabled = $bulkEnabled" in {
        val cosmosEndpoint = TestConfigurations.HOST
        val cosmosMasterKey = TestConfigurations.MASTER_KEY

        // Create a container with pk different than id
        val targetContainerName = s"containerPk-${UUID.randomUUID().toString}"
        cosmosClient
            .getDatabase(cosmosDatabase)
            .createContainerIfNotExists(
              targetContainerName,
              "/pk",
              ThroughputProperties.createManualThroughput(400))
            .block()
        val targetContainer = cosmosClient.getDatabase(cosmosDatabase).getContainer(targetContainerName)

        val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
            "spark.cosmos.accountKey" -> cosmosMasterKey,
            "spark.cosmos.database" -> cosmosDatabase,
            "spark.cosmos.container" -> targetContainerName,
            "spark.cosmos.serialization.inclusionMode" -> "NonDefault"
        )

        val cfgPatch = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
            "spark.cosmos.accountKey" -> cosmosMasterKey,
            "spark.cosmos.database" -> cosmosDatabase,
            "spark.cosmos.container" -> targetContainerName,
            "spark.cosmos.write.strategy" -> ItemWriteStrategy.ItemBulkUpdate.toString,
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
            ("QuarkPk", "Quark", "Red", 1.0 / 2, ""),
            ("QuarkPk", "Quark2", "Red", 1.0 / 2, "")
        ).toDF("pk", "id", "color", "spin", "empty")

        df.show(false)
        df.write.format("cosmos.oltp").mode("Append").options(cfg).save()

        // verify data is written
        // wait for a second to allow replication is completed.
        Thread.sleep(1000)

        // the item with the same id/pk will be persisted based on the upsert config
        var quarks = queryItems("SELECT * FROM r where r.id = 'Quark'", cosmosDatabase, targetContainerName).toArray
        quarks should have size 1

        var quark = quarks(0)
        quark.get("pk").asText() shouldEqual "QuarkPk"
        quark.get("id").asText() shouldEqual "Quark"
        quark.get("color").asText() shouldEqual "Red"

        var quarks2 = queryItems("SELECT * FROM r where r.id = 'Quark2'", cosmosDatabase, targetContainerName).toArray
        quarks2 should have size 1

        var quark2 = quarks2(0)
        quark2.get("pk").asText() shouldEqual "QuarkPk"
        quark2.get("id").asText() shouldEqual "Quark2"
        quark2.get("color").asText() shouldEqual "Red"

        // It will test few scenario:
        // 1. Since item with id 'Quark', item with id 'Quark2' already exists, so they will be updated
        // 2. Since item with id 'Quark3' does not exist, so it will be created
        // 3. There are two rows targeting the same item with id 'Quark', SDK internally will handle properly to retry, so eventually
        // the color of the item should be updated to be 'Purple'
        val patchDf = Seq(
            ("QuarkPk", "Quark", "Blue"),
            ("QuarkPk", "Quark", "Purple"),
            ("QuarkPk", "Quark2", "Blue"),
            ("QuarkPk", "Quark3", "Green")
        ).toDF("pk", "id", "color")

        patchDf.write.format("cosmos.oltp").mode("Append").options(cfgPatch).save()

        // verify data is written
        // wait for a second to allow replication is completed.
        Thread.sleep(1000)

        // validate item 'Quark' is updated properly
        quarks = queryItems("SELECT * FROM r where r.id = 'Quark'", cosmosDatabase, targetContainerName).toArray
        quarks should have size 1

        quark = quarks(0)
        quark.get("pk").asText() shouldEqual "QuarkPk"
        quark.get("id").asText() shouldEqual "Quark"
        quark.get("color").asText() shouldEqual "Purple"

        // Validate item 'Quark2' is updated properly
        quarks2 = queryItems("SELECT * FROM r where r.id = 'Quark2'", cosmosDatabase, targetContainerName).toArray
        quarks2 should have size 1

        quark2 = quarks2(0)
        quark2.get("pk").asText() shouldEqual "QuarkPk"
        quark2.get("id").asText() shouldEqual "Quark2"
        quark2.get("color").asText() shouldEqual "Blue"

        // Validate item 'Quark3' is created properly
        val quarks3 = queryItems("SELECT * FROM r where r.id = 'Quark3'", cosmosDatabase, targetContainerName).toArray
        quarks3 should have size 1

        val quark3 = quarks3(0)
        quark3.get("pk").asText() shouldEqual "QuarkPk"
        quark3.get("id").asText() shouldEqual "Quark3"
        quark3.get("color").asText() shouldEqual "Green"

        targetContainer.delete().block()
    }
  }
  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
  //scalastyle:on null
}
