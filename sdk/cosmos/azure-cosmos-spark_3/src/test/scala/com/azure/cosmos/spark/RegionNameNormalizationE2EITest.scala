// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.core.util.Context
import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.models.CosmosClientTelemetryConfig
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.{BridgeInternal, CosmosDiagnosticsContext, CosmosDiagnosticsHandler}

import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import scala.collection.JavaConverters._

/**
 * End-to-end proof that the region-name-mapper feature inside `azure-cosmos` reaches the
 * Spark connector through the shaded jar.
 *
 * Customer scenario: pass non-canonical region names (lowercase, no spaces) via
 * `spark.cosmos.preferredRegionsList`. The connector's `CosmosConfig.PreferredRegionRegex`
 * accepts that form, so the value reaches `CosmosClientBuilder.preferredRegions(...)` in
 * the shaded `azure-cosmos`. Without the region-name-mapper, the SDK's `LocationCache`
 * cannot match `westus3` to the canonical `West US 3` account region and routing falls
 * back to whatever the default endpoint chooses.
 *
 * The test discovers the canonical region of the test account dynamically — no hard
 * coding. It runs a probe ingest with NO `preferredRegionsList`, parses the diagnostics
 * to capture whichever region the SDK contacted by default, then runs the real workload
 * with that same region in **non-canonical** lowercase-no-spaces form. The assertion: the
 * contacted region after the rename equals the canonical name the probe discovered. If the
 * mapper is not wired through the shaded SDK, the second call will either contact a
 * different region or none at all and the test fails.
 */
class RegionNameNormalizationE2EITest extends IntegrationSpec
  with SparkWithJustDropwizardAndNoSlf4jMetrics
  with CosmosClient
  with AutoCleanableCosmosContainersWithPkAsPartitionKey
  with BasicLoggingTrait {

  //scalastyle:off multiple.string.literals

  "Spark connector with non-canonical preferredRegionsList" should
    "route ingest and read traffic to the canonical region discovered via probe" in {

    val newSpark = getSpark
    // scalastyle:off underscore.import
    import newSpark.implicits._
    // scalastyle:on underscore.import

    val captured = new ConcurrentLinkedQueue[CosmosDiagnosticsContext]()

    TestCosmosClientBuilderInterceptor.setCallback(
      builder => {
        logInfo("Region-name-mapper E2E: registering capture diagnostics handler.")
        builder.clientTelemetryConfig(
          new CosmosClientTelemetryConfig()
            .diagnosticsHandler(new CapturingDiagnosticsHandler(captured)))
      })

    try {
      val baseCfg = Map(
        "spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
        "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.account.clientBuilderInterceptors" ->
          "com.azure.cosmos.spark.TestCosmosClientBuilderInterceptor"
      )

      // ----- 1. Probe: ingest a row with NO preferredRegionsList to discover the
      //                 account's default-routed region in canonical form.
      val probeDocs = (1 to 3).map(i => (UUID.randomUUID().toString, s"probe-$i"))
      probeDocs.toDF("id", "label")
        .write.format("cosmos.oltp").mode("Append").options(baseCfg).save()

      val probeContacted =
        captured.asScala.toList.flatMap(_.getContactedRegionNames.asScala).toSet
      logInfo(s"Region-name-mapper E2E: probe contacted regions = $probeContacted")
      probeContacted should not be empty
      // The probe must pin to a single canonical region; otherwise we can't form a stable
      // expectation for the rename test.
      probeContacted.size shouldEqual 1
      val canonicalRegion = probeContacted.head
      val nonCanonicalRegion = canonicalRegion.toLowerCase.replace(" ", "")
      logInfo(
        s"Region-name-mapper E2E: discovered canonical region = '$canonicalRegion', " +
          s"feeding non-canonical form '$nonCanonicalRegion' into preferredRegionsList.")
      captured.clear()

      val cfg = baseCfg + ("spark.cosmos.preferredRegionsList" -> nonCanonicalRegion)

      // ----- 2. Ingest path with non-canonical preferredRegionsList.
      val docs = (1 to 5).map(i => (UUID.randomUUID().toString, s"row-$i"))
      docs.toDF("id", "label")
        .write.format("cosmos.oltp").mode("Append").options(cfg).save()

      val ingestContacted =
        captured.asScala.toList.flatMap(_.getContactedRegionNames.asScala).toSet
      logInfo(s"Region-name-mapper E2E: ingest contacted regions = $ingestContacted")
      withClue(
        s"Ingest must route to canonical '$canonicalRegion' when given non-canonical " +
          s"'$nonCanonicalRegion'; got $ingestContacted. Failure implies the " +
          s"region-name-mapper is not wired through the shaded SDK.") {
        ingestContacted shouldEqual Set(canonicalRegion)
      }
      captured.clear()

      // ----- 3. Read path with non-canonical preferredRegionsList.
      val readDf = newSpark.read.format("cosmos.oltp").options(cfg).load()
      val rowCount = readDf.count()
      rowCount should be >= docs.size.toLong

      val readContacted =
        captured.asScala.toList.flatMap(_.getContactedRegionNames.asScala).toSet
      logInfo(s"Region-name-mapper E2E: read contacted regions = $readContacted")
      withClue(
        s"Read must route to canonical '$canonicalRegion' when given non-canonical " +
          s"'$nonCanonicalRegion'; got $readContacted. Failure implies the " +
          s"region-name-mapper is not wired through the shaded SDK.") {
        readContacted shouldEqual Set(canonicalRegion)
      }
    } finally {
      TestCosmosClientBuilderInterceptor.resetCallback()
    }
  }

  "Spark connector with non-canonical preferredRegionsList pointing at a NON-primary region" should
    "force routing to that secondary region (only meaningful for multi-region accounts)" in {

    // Discover all readable + writeable regions for the test account from the bootstrap
    // client's GlobalEndpointManager cache (no network call — the bootstrap client has
    // already resolved the account on creation). This is the differential test: the
    // previous case fed back the SDK's default region (a round-trip). Here we force traffic
    // to MOVE to a secondary region. If the region-name-mapper is not wired through the
    // shaded SDK, the lowercase-no-spaces form won't match any account region and routing
    // falls back to the primary — the assertion then fails loudly.
    val account = BridgeInternal.getContextClient(cosmosClient)
      .getGlobalEndpointManager
      .getLatestDatabaseAccount
    val writeableRegions = account.getWritableLocations.asScala.map(_.getName).toList
    val readableRegions = account.getReadableLocations.asScala.map(_.getName).toList
    logInfo(
      s"Region-name-mapper E2E (secondary): account writeable = $writeableRegions, " +
        s"readable = $readableRegions")

    if (readableRegions.size < 2) {
      logWarning(
        s"Region-name-mapper E2E (secondary): account only has ${readableRegions.size} " +
          s"readable region(s); cannot run the secondary-region differential test.")
      cancel("Account does not have a secondary readable region.")
    }

    val newSpark = getSpark
    // scalastyle:off underscore.import
    import newSpark.implicits._
    // scalastyle:on underscore.import

    val captured = new ConcurrentLinkedQueue[CosmosDiagnosticsContext]()

    TestCosmosClientBuilderInterceptor.setCallback(
      builder => {
        logInfo("Region-name-mapper E2E (secondary): registering capture diagnostics handler.")
        builder.clientTelemetryConfig(
          new CosmosClientTelemetryConfig()
            .diagnosticsHandler(new CapturingDiagnosticsHandler(captured)))
      })

    try {
      val baseCfg = Map(
        "spark.cosmos.accountEndpoint" -> TestConfigurations.HOST,
        "spark.cosmos.accountKey" -> TestConfigurations.MASTER_KEY,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.account.clientBuilderInterceptors" ->
          "com.azure.cosmos.spark.TestCosmosClientBuilderInterceptor"
      )

      // ----- 1. Probe with NO preferredRegionsList to confirm the SDK's default region.
      val probeDocs = (1 to 3).map(i => (UUID.randomUUID().toString, s"probe-$i"))
      probeDocs.toDF("id", "label")
        .write.format("cosmos.oltp").mode("Append").options(baseCfg).save()
      val probeContacted =
        captured.asScala.toList.flatMap(_.getContactedRegionNames.asScala).toSet
      logInfo(s"Region-name-mapper E2E (secondary): probe contacted regions = $probeContacted")
      probeContacted.size shouldEqual 1
      val defaultRegionFromDiagnostics = probeContacted.head
      captured.clear()

      // ----- 2. Pick a READABLE region distinct from the probe's contacted region (in
      //          normalized form so the comparison is robust to canonical-vs-lowercase
      //          differences in the SDK's reported region name).
      val canonicalSecondary = readableRegions
        .find(r => normalize(r) != normalize(defaultRegionFromDiagnostics))
        .getOrElse(
          fail(
            s"Could not find a readable region distinct from probe's contacted " +
              s"'$defaultRegionFromDiagnostics'; readable = $readableRegions"))
      val nonCanonicalSecondary = normalize(canonicalSecondary)
      val secondaryIsWriteable =
        writeableRegions.exists(r => normalize(r) == nonCanonicalSecondary)
      logInfo(
        s"Region-name-mapper E2E (secondary): probe default = '$defaultRegionFromDiagnostics', " +
          s"selected canonical secondary = '$canonicalSecondary', " +
          s"feeding non-canonical form '$nonCanonicalSecondary' into preferredRegionsList " +
          s"(secondary writeable = $secondaryIsWriteable).")

      val cfg = baseCfg + ("spark.cosmos.preferredRegionsList" -> nonCanonicalSecondary)

      // ----- 3. Ingest path — only assert region routing if the secondary region is
      //          writeable; otherwise the SDK MUST fall back to a writeable region (not a
      //          mapper failure).
      val docs = (1 to 5).map(i => (UUID.randomUUID().toString, s"row-$i"))
      docs.toDF("id", "label")
        .write.format("cosmos.oltp").mode("Append").options(cfg).save()
      val ingestContacted =
        captured.asScala.toList.flatMap(_.getContactedRegionNames.asScala).toSet
      logInfo(
        s"Region-name-mapper E2E (secondary): ingest contacted regions = $ingestContacted")
      if (secondaryIsWriteable) {
        withClue(
          s"Ingest must route to canonical secondary '$canonicalSecondary' when given " +
            s"non-canonical '$nonCanonicalSecondary'; got $ingestContacted. Routing back " +
            s"to primary '$defaultRegionFromDiagnostics' implies the region-name-mapper " +
            s"is not wired through the shaded SDK.") {
          normalizeSet(ingestContacted) shouldEqual Set(nonCanonicalSecondary)
        }
      } else {
        logInfo(
          s"Region-name-mapper E2E (secondary): secondary '$canonicalSecondary' is " +
            s"read-only; ingest routed to writeable region(s) $ingestContacted as expected.")
      }
      captured.clear()

      // ----- 4. Read path — always assert routing to canonical secondary, since reads
      //          can be served from any readable region.
      val readDf = newSpark.read.format("cosmos.oltp").options(cfg).load()
      val rowCount = readDf.count()
      rowCount should be >= docs.size.toLong
      val readContacted =
        captured.asScala.toList.flatMap(_.getContactedRegionNames.asScala).toSet
      logInfo(
        s"Region-name-mapper E2E (secondary): read contacted regions = $readContacted")
      withClue(
        s"Read must route to canonical secondary '$canonicalSecondary' when given " +
          s"non-canonical '$nonCanonicalSecondary'; got $readContacted. Routing back to " +
          s"primary '$defaultRegionFromDiagnostics' or any other region implies the " +
          s"region-name-mapper is not wired through the shaded SDK.") {
        normalizeSet(readContacted) shouldEqual Set(nonCanonicalSecondary)
      }
    } finally {
      TestCosmosClientBuilderInterceptor.resetCallback()
    }
  }

  private def normalizeSet(regions: Set[String]): Set[String] =
    regions.map(normalize)

  private def normalize(region: String): String =
    region.toLowerCase.replace(" ", "")

  private class CapturingDiagnosticsHandler(
    captured: ConcurrentLinkedQueue[CosmosDiagnosticsContext]
  ) extends CosmosDiagnosticsHandler {
    override def handleDiagnostics(
      diagnosticsContext: CosmosDiagnosticsContext,
      traceContext: Context
    ): Unit = {
      captured.add(diagnosticsContext)
    }
  }
}
