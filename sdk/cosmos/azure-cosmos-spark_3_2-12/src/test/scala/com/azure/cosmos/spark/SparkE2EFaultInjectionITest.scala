// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.core.util.Context
import com.azure.cosmos.{CosmosAsyncClient, CosmosDiagnosticsContext, CosmosDiagnosticsHandler}
import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.models.CosmosClientTelemetryConfig
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.test.faultinjection.{CosmosFaultInjectionHelper, FaultInjectionCondition, FaultInjectionConditionBuilder, FaultInjectionOperationType, FaultInjectionResultBuilders, FaultInjectionRule, FaultInjectionRuleBuilder, FaultInjectionServerErrorResult, FaultInjectionServerErrorResultBuilder, FaultInjectionServerErrorType}

import java.time.Duration
import java.util
import java.util.concurrent.atomic.AtomicLong

class SparkE2EFaultInjectionITest extends IntegrationSpec
  with SparkWithJustDropwizardAndNoSlf4jMetrics
  with CosmosClient
  with AutoCleanableCosmosContainersWithPkAsPartitionKey
  with BasicLoggingTrait
  with MetricAssertions {

  "Spark write job with fault injection" can "be completed due to retries" in {
    val newSpark = getSpark

    // scalastyle:off underscore.import
    // scalastyle:off import.grouping
    import spark.implicits._
    val spark = newSpark
    // scalastyle:on underscore.import
    // scalastyle:on import.grouping

    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val successful = new AtomicLong(0)
    val failures = new AtomicLong(0)

    TestCosmosClientBuilderInterceptor.setCallback(
      builder => {
        logInfo(
          s"Injecting FAULTS here as well as a diagnostics handler to be able to build useful assertions...")

        val clientTelemetryConfig = new CosmosClientTelemetryConfig()
          .diagnosticsHandler(new TestCountDiagnosticsHandler(successful, failures))

        builder
          .clientTelemetryConfig(clientTelemetryConfig)
      }
    )

    try {
      sys.props.put("COSMOS.FLUSH_CLOSE_INTERVAL_SEC","10")
      val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabase,
        "spark.cosmos.container" -> cosmosContainer,
        "spark.cosmos.account.clientBuilderInterceptors" -> "com.azure.cosmos.spark.TestCosmosClientBuilderInterceptor"
      )

      val df = Seq(
        ("HelloWorld", "HelloWorld", "yellow", 1.0 / 4),
      ).toDF("particle name", "id", "color", "spin")

      df.write.format("cosmos.oltp").mode("Append").options(cfg).save()

      failures.get() shouldEqual 0
      successful.get() shouldEqual 1
      successful.set(0)

      val clientFromCache = udf.CosmosAsyncClientCache
        .getCosmosClientFromCache(cfg)
        .getClient
        .asInstanceOf[CosmosAsyncClient]

      val ruleBuilder: FaultInjectionRuleBuilder = new FaultInjectionRuleBuilder("Spark-408-Bulk")
      val faultInjectionRules: util.List[FaultInjectionRule] = new util.ArrayList[FaultInjectionRule]
      var conditionBuilder = new FaultInjectionConditionBuilder()
        .operationType(FaultInjectionOperationType.BATCH_ITEM)

      val faultInjectionCondition = conditionBuilder.build
      val serverErrorResult = FaultInjectionResultBuilders
        .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
        .delay(Duration.ofMinutes(5))
        .suppressServiceRequests(true)
        .build()

      // sustained fault injection// sustained fault injection
      val batchTimeoutRule = ruleBuilder
        .condition(faultInjectionCondition)
        .result(serverErrorResult)
        .hitLimit(3)
        .build

      faultInjectionRules.add(batchTimeoutRule)

      val container = clientFromCache.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
      CosmosFaultInjectionHelper
        .configureFaultInjectionRules(container, faultInjectionRules).block

      df.write.format("cosmos.oltp").mode("Append").options(cfg).save()

      logInfo(s"SUCCESS ${successful.get()}")
      logInfo(s"FAILURES ${failures.get()}")

      failures.get() shouldEqual 3
      successful.get() >= 1 shouldEqual true
    } finally {
      TestCosmosClientBuilderInterceptor.resetCallback()
      sys.props.remove("COSMOS.FLUSH_CLOSE_INTERVAL_SEC")
    }
  }

  private class TestCountDiagnosticsHandler(successful: AtomicLong, failures: AtomicLong) extends CosmosDiagnosticsHandler {
    override def handleDiagnostics(diagnosticsContext: CosmosDiagnosticsContext, traceContext: Context): Unit = {
      if (diagnosticsContext.getResourceType != "Document" || diagnosticsContext.getOperationType != "Batch") {
        return;
      }

      if (diagnosticsContext.getStatusCode == 200) {
        successful.incrementAndGet()
        logInfo(s"Diagnostics Ctx: ${diagnosticsContext.toJson}")
      } else {
        failures.incrementAndGet()
        logWarning(s"Diagnostics Ctx: ${diagnosticsContext.toJson}")
      }
    }
  }
}
