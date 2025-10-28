// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.core.util.Context
import com.azure.cosmos.{CosmosDiagnosticsContext, CosmosDiagnosticsHandler, CosmosDiagnosticsThresholds}
import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.models.{CosmosClientTelemetryConfig, FeedRange, PartitionKey, ShowQueryMode}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.test.faultinjection.{CosmosFaultInjectionHelper, FaultInjectionConditionBuilder, FaultInjectionConnectionType, FaultInjectionEndpointBuilder, FaultInjectionOperationType, FaultInjectionResultBuilders, FaultInjectionRule, FaultInjectionRuleBuilder, FaultInjectionServerErrorType}
import org.apache.spark.scheduler.{AccumulableInfo, SparkListener, SparkListenerTaskEnd}
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.concurrent.Waiters.{interval, timeout}
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

import java.time.Duration

class SparkE2EBulkWriteITest
  extends IntegrationSpec
    with SparkWithJustDropwizardAndNoSlf4jMetrics
    with CosmosClient
    with AutoCleanableCosmosContainer
    with BasicLoggingTrait
    with MetricAssertions {

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number
  //scalastyle:off null

  it should s"support bulk ingestion when BulkWriter needs to get restarted" in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val configMapBuilder = scala.collection.mutable.Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
      "spark.cosmos.serialization.inclusionMode" -> "NonDefault"
    )

    var faultInjectionRuleOption : Option[FaultInjectionRule] = None

    try {
      // set-up logging
      val logs = scala.collection.mutable.ListBuffer[CosmosDiagnosticsContext]()

      configMapBuilder += "spark.cosmos.account.clientBuilderInterceptors" -> "com.azure.cosmos.spark.TestCosmosClientBuilderInterceptor"
      TestCosmosClientBuilderInterceptor.setCallback(builder => {
        val thresholds = new CosmosDiagnosticsThresholds()
          .setPointOperationLatencyThreshold(Duration.ZERO)
          .setNonPointOperationLatencyThreshold(Duration.ZERO)
        val telemetryCfg = new CosmosClientTelemetryConfig()
          .showQueryMode(ShowQueryMode.ALL)
          .diagnosticsHandler(new CompositeLoggingHandler(logs))
          .diagnosticsThresholds(thresholds)
        builder.clientTelemetryConfig(telemetryCfg)
      })

      // set-up fault injection
      configMapBuilder += "spark.cosmos.account.clientInterceptors" -> "com.azure.cosmos.spark.TestFaultInjectionClientInterceptor"
      configMapBuilder += "spark.cosmos.write.flush.intervalInSeconds" -> "10"
      configMapBuilder += "spark.cosmos.write.flush.noProgress.maxIntervalInSeconds" -> "30"
      configMapBuilder += "spark.cosmos.write.flush.noProgress.maxRetryIntervalInSeconds" -> "300"
      configMapBuilder += "spark.cosmos.write.onRetryCommitInterceptor" -> "com.azure.cosmos.spark.TestWriteOnRetryCommitInterceptor"
      TestFaultInjectionClientInterceptor.setCallback(client => {
        val faultInjectionResultBuilder = FaultInjectionResultBuilders
          .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
          .delay(Duration.ofHours(10000))
          .times(1)

        val endpoints = new FaultInjectionEndpointBuilder(
          FeedRange.forLogicalPartition(new PartitionKey("range_1")))
          .build()

        val result = faultInjectionResultBuilder.build
        val condition = new FaultInjectionConditionBuilder()
          .operationType(FaultInjectionOperationType.BATCH_ITEM)
          .connectionType(FaultInjectionConnectionType.DIRECT)
          .endpoints(endpoints)
          .build

        faultInjectionRuleOption = Some(new FaultInjectionRuleBuilder("InjectedEndlessResponseDelay")
          .condition(condition)
          .result(result)
          .build)

        TestWriteOnRetryCommitInterceptor.setCallback(() => faultInjectionRuleOption.get.disable())

        CosmosFaultInjectionHelper.configureFaultInjectionRules(
          client.getDatabase(cosmosDatabase).getContainer(cosmosContainer),
          List(faultInjectionRuleOption.get).asJava).block

        client
      })

      val cfg = configMapBuilder.toMap

      val newSpark = getSpark

      // scalastyle:off underscore.import
      // scalastyle:off import.grouping
      import spark.implicits._
      val spark = newSpark
      // scalastyle:on underscore.import
      // scalastyle:on import.grouping

      val toBeIngested = scala.collection.mutable.ListBuffer[String]()
      for (i <- 1 to 100) {
        toBeIngested += s"record_$i"
      }

      val df = toBeIngested.toDF("id")

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

      recordsWrittenSnapshot shouldEqual 100
      bytesWrittenSnapshot > 0 shouldEqual true

      // that the write by spark is visible by the client query
      // wait for a second to allow replication is completed.
      Thread.sleep(1000)

      // the new item will be always persisted
      val ids = queryItems("SELECT c.id FROM c ORDER by c.id").toArray
      ids should have size 100
      val firstDoc = ids(0)
      firstDoc.get("id").asText() shouldEqual "record_1"

      // validate logs
      logs.nonEmpty shouldEqual true
    } finally {
      TestCosmosClientBuilderInterceptor.resetCallback()
      TestFaultInjectionClientInterceptor.resetCallback()
      faultInjectionRuleOption match {
        case Some(rule) => rule.disable()
        case None =>
      }
    }
  }

  class CompositeLoggingHandler(logs: scala.collection.mutable.ListBuffer[CosmosDiagnosticsContext]) extends CosmosDiagnosticsHandler {
    /**
     * This method will be invoked when an operation completed (successfully or failed) to allow diagnostic handlers to
     * emit the diagnostics NOTE: Any code in handleDiagnostics should not execute any I/O operations, do thread
     * switches or execute CPU intense work - if needed a diagnostics handler should queue this work asynchronously. The
     * method handleDiagnostics will be invoked on the hot path - so, any inefficient diagnostics handler will increase
     * end-to-end latency perceived by the application
     *
     * @param diagnosticsContext the Cosmos DB diagnostic context with metadata for the operation
     * @param traceContext       the Azure trace context
     */
    override def handleDiagnostics(diagnosticsContext: CosmosDiagnosticsContext, traceContext: Context): Unit = {
      logs += diagnosticsContext

      CosmosDiagnosticsHandler.DEFAULT_LOGGING_HANDLER.handleDiagnostics(diagnosticsContext, traceContext)
    }
  }
}

