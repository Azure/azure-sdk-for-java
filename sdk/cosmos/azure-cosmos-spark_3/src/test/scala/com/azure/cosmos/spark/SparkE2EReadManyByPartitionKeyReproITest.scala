// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType}

import java.util.UUID
import scala.collection.mutable.ListBuffer

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

/**
 * Repro-only suite for the customer-reported defects in
 * `CosmosItemsDataSource.readManyByPartitionKeys` (see the LSEG bi-temporal workflow report):
 *   1. the FIRST record of a partition is skipped, and
 *   2. the remaining records are DUPLICATED (the backend query is executed twice).
 *
 * Both stem from the `Iterator[Row]` returned by
 * [[CosmosReadManyByPartitionKeyReader.readManyByPartitionKeys]] violating the Scala/Java
 * iterator contract: its `hasNext` calls `reader.next()` (which ADVANCES the underlying reader)
 * instead of being idempotent.
 *
 * These tests intentionally assert the CORRECT behavior so they FAIL against the current
 * (buggy) code and clearly print each symptom. They are expected to pass once the reader is
 * fixed. No production code is changed by this suite.
 */
class SparkE2EReadManyByPartitionKeyReproITest
  extends IntegrationSpec
    with Spark
    with AutoCleanableCosmosContainersWithPkAsPartitionKey {

  private val pkProperty = "pk"

  // scalastyle:off multiple.string.literals
  // scalastyle:off magic.number

  private val recordCount = 5

  private def readSchema: StructType = StructType(Seq(
    StructField("id", StringType, nullable = false),
    StructField(pkProperty, StringType, nullable = false)
  ))

  private def readConfig: Map[String, String] = Map(
    "spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
    "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
    "spark.cosmos.database" -> cosmosDatabase,
    "spark.cosmos.container" -> cosmosContainersWithPkAsPartitionKey,
    "spark.cosmos.read.inferSchema.enabled" -> "false",
    "spark.cosmos.applicationName" -> "ReadManyByPKRepro"
  )

  /**
   * Inserts `recordCount` documents, each with a distinct partition-key value and exactly one
   * item per logical partition (mirroring the customer's "one current record per instrument"
   * bi-temporal scenario). Returns the expected (id -> pk) pairs.
   */
  private def seedRecords(): Seq[(String, String)] = {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
    val records = (1 to recordCount).map { i =>
      (s"id-$i-${UUID.randomUUID()}", s"pk-$i-${UUID.randomUUID()}")
    }
    records.foreach { case (id, pk) =>
      val node = Utils.getSimpleObjectMapper.createObjectNode()
      node.put("id", id)
      node.put(pkProperty, pk)
      container.createItem(node).block()
    }
    // give the emulator a moment so a subsequent read-many reliably sees all writes
    Thread.sleep(500)
    records
  }

  /**
   * Builds the single-partition "keys" DataFrame that drives readManyByPartitionKeys.
   * Coalescing to a single partition forces every record to flow through ONE reader so the
   * within-partition symptoms (missing first / duplicates) are crisp and deterministic.
   */
  private def buildKeysDataFrame(pkValues: Seq[String]) = {
    val rows = pkValues.map(pk => Row(pk)).asJava
    val pkSchema = StructType(Seq(StructField(pkProperty, StringType, nullable = false)))
    spark.createDataFrame(rows, pkSchema).coalesce(1)
  }

  it should "return each record exactly once (DUPLICATE-records repro)" in {
    val expected = seedRecords()
    val expectedIds = expected.map(_._1).toSet

    val keysDf = buildKeysDataFrame(expected.map(_._2))
    val resultDf = CosmosItemsDataSource.readManyByPartitionKeys(keysDf, readConfig.asJava, readSchema)

    // Drive the reader's iterator with two sequential drains. queryExecution.toRdd exposes the
    // bare RDDScanExec InternalRow iterator, which delegates hasNext 1:1 to the connector's
    // buggy iterator (no codegen buffering in between). The second drain reproduces exactly
    // what Spark's cache/columnar-batch builder does when it peeks hasNext again after the
    // first batch is complete: because hasNext calls reader.next() after the reader was already
    // exhausted+closed, the reader re-creates its inner iterator and RE-EXECUTES the whole
    // query -> every record is emitted a second time.
    val actualIds: Array[String] = resultDf.queryExecution.toRdd.mapPartitions(rowIter => {
      val buf = ListBuffer[String]()
      while (rowIter.hasNext) buf += rowIter.next().getUTF8String(0).toString // first drain
      while (rowIter.hasNext) buf += rowIter.next().getUTF8String(0).toString // peek-after-exhaustion -> re-executes query
      buf.iterator
    }).collect()

    val duplicatedIds = actualIds.groupBy(identity).filter(_._2.size > 1).keys.toList.sorted

    println(s"[DUPLICATE-repro] expected distinct records = ${expectedIds.size}")
    println(s"[DUPLICATE-repro] total rows returned        = ${actualIds.size}")
    println(s"[DUPLICATE-repro] duplicated ids             = $duplicatedIds")

    withClue(
      s"Expected exactly ${expectedIds.size} rows (each record once) but got ${actualIds.length}. " +
        s"Duplicated ids: $duplicatedIds. This confirms readManyByPartitionKeys returns duplicate " +
        s"records (the backend query is executed twice) - reproduces the customer report. ") {
      duplicatedIds shouldBe empty
      actualIds.length shouldEqual expectedIds.size
    }
  }

  it should "return the first record and not skip it (MISSING-FIRST-record repro)" in {
    val expected = seedRecords()
    val expectedIds = expected.map(_._1).toSet

    val keysDf = buildKeysDataFrame(expected.map(_._2))

    // A single hasNext "peek" before iterating (as Spark does when it checks a partition for
    // emptiness / primes a columnar batch) advances the underlying reader past the first record.
    // Because next() only returns the CURRENT row (it never re-reads the skipped one), the first
    // record is lost. Whole-stage codegen is disabled here so the peek hits the connector's buggy
    // iterator directly rather than a BufferedRowIterator that would buffer (and thus hide) the
    // skipped row - which is exactly what happens on engines/paths that don't buffer the scan.
    val priorWholeStageCodegen = spark.conf.get("spark.sql.codegen.wholeStage")
    spark.conf.set("spark.sql.codegen.wholeStage", "false")
    val actualIds: Set[String] = try {
      val resultDf = CosmosItemsDataSource.readManyByPartitionKeys(keysDf, readConfig.asJava, readSchema)
      resultDf.queryExecution.toRdd.mapPartitions(rowIter => {
        val buf = ListBuffer[String]()
        if (rowIter.hasNext) {
          // intentional peek: mirrors Spark peeking hasNext before the consume loop
        }
        while (rowIter.hasNext) buf += rowIter.next().getUTF8String(0).toString
        buf.iterator
      }).collect().toSet
    } finally {
      spark.conf.set("spark.sql.codegen.wholeStage", priorWholeStageCodegen)
    }

    val missingIds = (expectedIds -- actualIds).toList.sorted

    println(s"[MISSING-FIRST-repro] expected records = ${expectedIds.size}")
    println(s"[MISSING-FIRST-repro] returned records = ${actualIds.size}")
    println(s"[MISSING-FIRST-repro] missing ids      = $missingIds")

    withClue(
      s"Expected all ${expectedIds.size} records but ${missingIds.size} were missing: $missingIds. " +
        s"This confirms readManyByPartitionKeys skips the first record of a partition after a " +
        s"single hasNext peek - reproduces the customer report. ") {
      missingIds shouldBe empty
      actualIds shouldEqual expectedIds
    }
  }

  it should "return the correct multiset via .cache() (end-to-end customer scenario repro)" in {
    val expected = seedRecords()
    val expectedIds = expected.map(_._1).toList.sorted

    val keysDf = buildKeysDataFrame(expected.map(_._2))
    val resultDf = CosmosItemsDataSource.readManyByPartitionKeys(keysDf, readConfig.asJava, readSchema)

    // Faithful reproduction of the customer's notebook: read-many, cache, then materialize.
    // Caching builds columnar batches, whose iterator peeks hasNext before/after each batch and
    // therefore triggers BOTH symptoms at once (skipped first record + re-executed query).
    val cached = resultDf.cache()
    val collected = cached.collect()

    val actualIds = collected.map(_.getAs[String]("id")).toList.sorted
    val duplicatedIds = actualIds.groupBy(identity).filter(_._2.size > 1).keys.toList.sorted
    val missingIds = (expectedIds.toSet -- actualIds.toSet).toList.sorted

    println(s"[CACHE-repro] expected records  = ${expectedIds.size} -> $expectedIds")
    println(s"[CACHE-repro] returned rows      = ${actualIds.size} -> $actualIds")
    println(s"[CACHE-repro] duplicated ids     = $duplicatedIds")
    println(s"[CACHE-repro] missing ids        = $missingIds")

    withClue(
      s"Expected exactly the ${expectedIds.size} seeded records via .cache() but got " +
        s"${actualIds.size} rows (missing: $missingIds, duplicated: $duplicatedIds). " +
        s"Reproduces the customer's notebook result. ") {
      missingIds shouldBe empty
      duplicatedIds shouldBe empty
      actualIds shouldEqual expectedIds
    }
  }

  // scalastyle:on multiple.string.literals
  // scalastyle:on magic.number
}
