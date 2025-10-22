// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.core.credential.{AccessToken, TokenCredential, TokenRequestContext}
import com.azure.core.management.AzureEnvironment
import com.azure.core.management.profile.AzureProfile
import com.azure.core.tracing.opentelemetry.OpenTelemetryTracingOptions
import com.azure.core.util.TracingOptions
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry
import com.azure.cosmos.implementation.{Configs, CosmosClientMetadataCachesSnapshot, CosmosDaemonThreadFactory, ImplementationBridgeHelpers, SparkBridgeImplementationInternal, Strings}
import com.azure.cosmos.models.{CosmosClientTelemetryConfig, CosmosMetricCategory, CosmosMetricTagName, CosmosMicrometerMetricsOptions}
import com.azure.cosmos.spark.CosmosPredicates.isOnSparkDriver
import com.azure.cosmos.spark.catalog.{CosmosCatalogClient, CosmosCatalogCosmosSDKClient, CosmosCatalogManagementSDKClient}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.{ConsistencyLevel, CosmosAsyncClient, CosmosClientBuilder, CosmosContainerProactiveInitConfigBuilder, CosmosDiagnosticsThresholds, DirectConnectionConfig, GatewayConnectionConfig, ReadConsistencyStrategy, ThrottlingRetryOptions}
import com.azure.identity.{ClientCertificateCredentialBuilder, ClientSecretCredentialBuilder, ManagedIdentityCredentialBuilder}
import com.azure.monitor.opentelemetry.autoconfigure.{AzureMonitorAutoConfigure, AzureMonitorAutoConfigureOptions}
import com.azure.resourcemanager.cosmos.CosmosManager
import com.microsoft.applicationinsights.TelemetryConfiguration
import io.micrometer.azuremonitor.AzureMonitorMeterRegistry
import io.micrometer.core.instrument.{Clock, MeterRegistry}
import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import io.netty.util.ResourceLeakDetector
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.logs.Severity
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.trace.samplers.Sampler
import org.apache.spark.scheduler.{SparkListener, SparkListenerApplicationEnd}
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkContext, TaskContext}
import reactor.core.publisher.Mono
import reactor.core.scheduler.{Scheduler, Schedulers}

import java.io.ByteArrayInputStream
import java.time.{Duration, Instant}
import java.util
import java.util.{Base64, ConcurrentModificationException}
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import java.util.function.BiPredicate
import scala.collection.concurrent.TrieMap

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// scalastyle:off multiple.string.literals
private[spark] object CosmosClientCache extends BasicLoggingTrait {

  SparkBridgeImplementationInternal.setUserAgentWithSnapshotInsteadOfBeta()
  System.setProperty("COSMOS.SWITCH_OFF_IO_THREAD_FOR_RESPONSE", "true")
  SparkBridgeImplementationInternal.overrideDefaultTcpOptionsForSparkUsage()

  // removing clients from the cache after 15 minutes
  // The clients won't be disposed - so any still running task can still keep using it
  // but it helps to allow the GC to clean-up the resources if no running task is using the client anymore
  private[this] val unusedClientTtlInMs = 15 * 60 * 1000
  private[this] val cleanupIntervalInSeconds = 1 * 60
  private[this] val cache = new TrieMap[ClientConfigurationWrapper, CosmosClientCacheMetadata]
  private[this] val monitoredSparkApplications = new TrieMap[SparkContext, Int]
  private[this] val toBeClosedWhenNotActiveAnymore =  new TrieMap[ClientConfigurationWrapper, CosmosClientCacheMetadata]
  private[this] val executorService:ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
    new CosmosDaemonThreadFactory("CosmosClientCache"))

  this.executorService.scheduleWithFixedDelay(
    () => this.onCleanup(),
    this.cleanupIntervalInSeconds,
    this.cleanupIntervalInSeconds,
    TimeUnit.SECONDS)

  private val AAD_AUTH_BOUNDED_ELASTIC_THREAD_NAME = "cosmos-client-cache-auth-bounded-elastic"
  private val TTL_FOR_SCHEDULER_WORKER_IN_SECONDS = 60 // same as BoundedElasticScheduler.DEFAULT_TTL_SECONDS

  private val aadAuthBoundedElastic: Scheduler = Schedulers.newBoundedElastic(
    Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
    Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE,
    AAD_AUTH_BOUNDED_ELASTIC_THREAD_NAME,
    TTL_FOR_SCHEDULER_WORKER_IN_SECONDS, true)

  def apply(cosmosClientConfiguration: CosmosClientConfiguration,
            cosmosClientStateHandle: Option[CosmosClientMetadataCachesSnapshot],
            calledFrom: String): CosmosClientCacheItem = {

    if (isOnSparkDriver()) {
      SparkSession.getActiveSession match {
        case Some(session) =>
          val ctx = session.sparkContext
          val newListener = new ApplicationEndListener(ctx)
          monitoredSparkApplications.putIfAbsent(ctx, 0) match {
            case Some(_) =>
            case None =>
              logInfo(s"Registering ApplicationEndListener for Spark Context '${ctx.hashCode}'")
              ctx.addSparkListener(newListener)
          }
        case None =>
      }
    }

    val ownerInfo = OwnerInfo(calledFrom)

    cache.get(ClientConfigurationWrapper(cosmosClientConfiguration)) match {
      case Some(clientCacheMetadata) => clientCacheMetadata.createCacheItemForReuse(ownerInfo)
      case None => syncCreate(cosmosClientConfiguration, cosmosClientStateHandle, ownerInfo)
    }
  }

  def isStillReferenced(cosmosClientConfiguration: CosmosClientConfiguration): Boolean = {
    cache.get(ClientConfigurationWrapper(cosmosClientConfiguration)) match {
      case Some(_) => true
      case None => toBeClosedWhenNotActiveAnymore
        .readOnlySnapshot()
        .contains(ClientConfigurationWrapper(cosmosClientConfiguration))
    }
  }

  def ownerInformation(cosmosClientConfiguration: CosmosClientConfiguration): String = {
    cache.get(ClientConfigurationWrapper(cosmosClientConfiguration)) match {
      case None => ""
      case Some(existingClientCacheMetadata) => existingClientCacheMetadata
        .owners
        .keys
        .mkString(", ")
    }
  }

  def purge(cosmosClientConfiguration: CosmosClientConfiguration): Unit = {
    purgeImpl(ClientConfigurationWrapper(cosmosClientConfiguration), forceClosure = false)
  }

  private[this]def purgeImpl(clientConfigWrapper: ClientConfigurationWrapper, forceClosure: Boolean): Unit = {
    cache.get(clientConfigWrapper) match {
      case None =>
      case Some(existingClientCacheMetadata) =>
        cache.remove(clientConfigWrapper) match {
          case None =>
          case Some(_) =>
            // there is a race condition here - technically between the check in onCleanup
            // when the client wasn't retrieved for certain period of time
            // and it wasn't actively used anymore someone could have
            // retrieved it form the cache before we remove it here
            // so if it is actively used now we need to keep a reference and close it
            // when it isn't used anymore
            if (forceClosure || existingClientCacheMetadata.refCount.get() == 0) {
              existingClientCacheMetadata.closeClients()
            } else {
              toBeClosedWhenNotActiveAnymore.put(clientConfigWrapper, existingClientCacheMetadata)
            }
        }
    }
  }

  private def validateAadConfigs(cosmosClientConfiguration: CosmosClientConfiguration): Boolean = {
    val shouldLog = !(cosmosClientConfiguration.resourceGroupName.isDefined &&
      cosmosClientConfiguration.subscriptionId.isDefined &&
      cosmosClientConfiguration.tenantId.isDefined)
    if (shouldLog) {
      logWarning(
        "To create Databases, Containers and other resources in Cosmos DB using Microsoft Entra ID, " +
          "you need to provide resourceGroupName, subscriptionId and tenantId in the configuration. " +
          "Otherwise, the Cosmos Catalog will not be able to create resources."
      )
    }
    shouldLog
  }

  private[this] def syncCreate(cosmosClientConfiguration: CosmosClientConfiguration,
                               cosmosClientStateHandle: Option[CosmosClientMetadataCachesSnapshot],
                               ownerInfo: OwnerInfo)
  : CosmosClientCacheItem = synchronized {

    val clientConfigWrapper = ClientConfigurationWrapper(cosmosClientConfiguration)
    cache.get(clientConfigWrapper) match {
      case Some(clientCacheMetadata) => clientCacheMetadata.createCacheItemForReuse(ownerInfo)
      case None =>
        val cosmosAsyncClient = createCosmosAsyncClient(cosmosClientConfiguration, cosmosClientStateHandle)
        var sparkCatalogClient: CosmosCatalogClient = CosmosCatalogCosmosSDKClient(cosmosAsyncClient)
        // When using AAD auth, cosmos catalog will change to use management sdk instead of cosmos sdk
        cosmosClientConfiguration.authConfig match {
          case aadAuthConfig: CosmosServicePrincipalAuthConfig =>
            if (!validateAadConfigs(cosmosClientConfiguration)) {
              sparkCatalogClient =
                CosmosCatalogManagementSDKClient(
                  cosmosClientConfiguration.resourceGroupName.get,
                  cosmosClientConfiguration.databaseAccountName,
                  createCosmosManagementClient(
                    cosmosClientConfiguration.subscriptionId.get,
                    new AzureEnvironment(cosmosClientConfiguration.azureEnvironmentEndpoints),
                    aadAuthConfig),
                  cosmosAsyncClient)
            }
          case managedIdentityAuth: CosmosManagedIdentityAuthConfig =>
            if (!validateAadConfigs(cosmosClientConfiguration)) {
              sparkCatalogClient =
                CosmosCatalogManagementSDKClient(
                  cosmosClientConfiguration.resourceGroupName.get,
                  cosmosClientConfiguration.databaseAccountName,
                  createCosmosManagementClient(
                    cosmosClientConfiguration.subscriptionId.get,
                    new AzureEnvironment(cosmosClientConfiguration.azureEnvironmentEndpoints),
                    managedIdentityAuth),
                  cosmosAsyncClient)
            }
          case accessTokenProviderAuth: CosmosAccessTokenAuthConfig =>
            if (!validateAadConfigs(cosmosClientConfiguration)) {
              sparkCatalogClient =
                CosmosCatalogManagementSDKClient(
                  cosmosClientConfiguration.resourceGroupName.get,
                  cosmosClientConfiguration.databaseAccountName,
                  createCosmosManagementClient(
                    cosmosClientConfiguration.subscriptionId.get,
                    new AzureEnvironment(cosmosClientConfiguration.azureEnvironmentEndpoints),
                    accessTokenProviderAuth),
                  cosmosAsyncClient)
            }
          case _ =>
        }

        val epochNowInMs = Instant.now.toEpochMilli
        val owners = new TrieMap[OwnerInfo, Option[Boolean]]
        owners.put(ownerInfo, None)

        val newClientCacheEntry = CosmosClientCacheMetadata(
          cosmosAsyncClient,
          sparkCatalogClient,
          cosmosClientConfiguration,
          new AtomicLong(epochNowInMs),
          new AtomicLong(epochNowInMs),
          new AtomicLong(epochNowInMs),
          new AtomicLong(1),
          owners)

        cache.putIfAbsent(clientConfigWrapper, newClientCacheEntry) match {
          case None => new CacheItemImpl(cosmosAsyncClient, sparkCatalogClient, newClientCacheEntry, ownerInfo)
          case Some(_) =>
            throw new ConcurrentModificationException("Should not reach here because its synchronized")
        }
    }
  }

  private[this] def toTokenCredential(
                                       cosmosClientConfiguration: CosmosClientConfiguration,
                                       authConfig: CosmosAuthConfig
                                     ): TokenCredential = {

    authConfig match {
      case masterKeyAuthConfig: CosmosMasterKeyAuthConfig =>
        throw new IllegalStateException("Cannot create TokenCredential from CosmosMasterKeyAuthConfig")
      case servicePrincipalAuthConfig: CosmosServicePrincipalAuthConfig =>
        if (servicePrincipalAuthConfig.clientCertPemBase64.isDefined) {
          val certInputStream = new ByteArrayInputStream(Base64.getDecoder.decode(servicePrincipalAuthConfig.clientCertPemBase64.get))
          new ClientCertificateCredentialBuilder()
            .authorityHost(new AzureEnvironment(cosmosClientConfiguration.azureEnvironmentEndpoints).getActiveDirectoryEndpoint())
            .tenantId(servicePrincipalAuthConfig.tenantId)
            .clientId(servicePrincipalAuthConfig.clientId)
            .pemCertificate(certInputStream)
            .sendCertificateChain(servicePrincipalAuthConfig.sendChain)
            .build()
        } else {
          new ClientSecretCredentialBuilder()
            .authorityHost(new AzureEnvironment(cosmosClientConfiguration.azureEnvironmentEndpoints).getActiveDirectoryEndpoint())
            .tenantId(servicePrincipalAuthConfig.tenantId)
            .clientId(servicePrincipalAuthConfig.clientId)
            .clientSecret(servicePrincipalAuthConfig.clientSecret.get)
            .build()
        }
      case managedIdentityAuthConfig: CosmosManagedIdentityAuthConfig =>
        createTokenCredential(managedIdentityAuthConfig)
      case accessTokenAuthConfig: CosmosAccessTokenAuthConfig =>
        createTokenCredential(accessTokenAuthConfig)
      case _ => throw new IllegalArgumentException(s"Authorization type ${authConfig.getClass} is not supported")
    }
  }

  private[this] def hasAnyMeterRegistry(registry: MeterRegistry): Boolean = {
    if (!registry.isInstanceOf[CompositeMeterRegistry]) {
      logInfo(s"Found real MeterRegistry - $registry, ${registry.getClass.getCanonicalName}")
      true
    } else {
      registry
        .asInstanceOf[CompositeMeterRegistry]
        .getRegistries
        .stream()
        .filter(r => hasAnyMeterRegistry(r))
        .count() > 0
    }
  }
  private[this] def configureDiagnostics
  (
    cosmosClientConfiguration: CosmosClientConfiguration,
    builder: CosmosClientBuilder
  ): Unit = {
    val isAzureMonitorOpenTelemetryEnabled = cosmosClientConfiguration.azureMonitorConfig.isDefined &&
      cosmosClientConfiguration.azureMonitorConfig.get.enabled

    val isSampledDiagnosticsLoggerEnabled = cosmosClientConfiguration.sampledDiagnosticsLoggerConfig.isDefined

    if (isSampledDiagnosticsLoggerEnabled || isAzureMonitorOpenTelemetryEnabled) {
      configureDiagnosticsCore (
        cosmosClientConfiguration,
        builder,
        isAzureMonitorOpenTelemetryEnabled,
        isSampledDiagnosticsLoggerEnabled
      )
    } else {
      logInfo("No diagnostics enabled")
    }
  }

  private[this] def configureOpenTelemetrySdk
  (
    cosmosClientConfiguration: CosmosClientConfiguration,
    azMonConfig: AzureMonitorConfig
  ) : OpenTelemetrySdk = {

    System.setProperty("otel.java.global-autoconfigure.enabled", "true")

    val sdkBuilder = AutoConfiguredOpenTelemetrySdk
      .builder
      .addPropertiesCustomizer(_ => {
        val additionalSystemPropertyOverrides = new util.HashMap[String, String](2)
        additionalSystemPropertyOverrides.put(
          Configs.APPLICATIONINSIGHTS_CONNECTION_STRING,
          azMonConfig.connectionString)
        additionalSystemPropertyOverrides.put(
          "otel.java.global-autoconfigure.enabled",
          "true")
        additionalSystemPropertyOverrides.put(
          "otel.metrics.exporter",
          "none")
        additionalSystemPropertyOverrides.put(
          "otel.traces.exporter", "azure_monitor")
        additionalSystemPropertyOverrides.put(
          "applicationinsights.live.metrics.enabled",
          azMonConfig.liveMetricsEnabled.toString)

        additionalSystemPropertyOverrides
      })

    val azMonitorOptions: AzureMonitorAutoConfigureOptions =
      if (azMonConfig.authEnabled) {
        new AzureMonitorAutoConfigureOptions()
          .connectionString(azMonConfig.connectionString)
          //.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
          .credential(
            toTokenCredential(
              cosmosClientConfiguration,
              azMonConfig.authConfig.get))
      } else {
        new AzureMonitorAutoConfigureOptions()
          .connectionString(azMonConfig.connectionString)
      }

    AzureMonitorAutoConfigure.customize(sdkBuilder, azMonitorOptions)

    val openTelemetryWithMeterProvider: OpenTelemetrySdk = sdkBuilder
      .addResourceCustomizer((resource, configProperties) => {
        val resourceBuilder = resource
          .toBuilder

        val machineId = Option(ClientTelemetry.blockingGetOrLoadMachineId(null))
        if (resource.getAttributes.get(AttributeKey.stringKey("service.name")).startsWith("unknown_service")) {
          val svcName = s"${CosmosConstants.currentName}:${CosmosConstants.currentVersion}"
          val instanceName = cosmosClientConfiguration.getRoleInstanceName(machineId)

          logInfo("Initializing Resource Customizer with Service attributes - "
            + s"service.name: $svcName, service.instance.id: $instanceName")

          resourceBuilder
            .put("service.namespace", "azure-cosmos-spark")
            .put("service.name", svcName)
            .put("service.version", CosmosConstants.currentVersion)
            .put("service.instance.id", instanceName)

        } else {
          logInfo("Initializing Resource Customizer without Service attributes")
        }

        resourceBuilder
          .build()
      })
      .addSamplerCustomizer((oldSampler, _) => {
        // OpenTelemetry only allows head-sampling - meaning you have to make a choice
        // whether to sample-in the span when the Span is started
        // this allows controlling predictable amount of spans being processed
        // It has a few drawbacks as well - you can't change the sampling decision based
        // on the outcome of the span (when the span ends) - so, you can't evert the decision
        // to sample-in a previously sampled-out span when an error happens
        // This is why i would strongly encourage any customer to also enable sampled
        // diagnostics - those will at least make sure diagnostic logs are maintained for
        // errors - including the Cosmos diagnostics.

        Sampler
          .parentBased(
            new CosmosMaxCountPerIntervalSpanHeadSampler(
              azMonConfig.samplingRateMaxCount,
              azMonConfig.samplingRateIntervalInSeconds,
              azMonConfig.samplingRate
            )
          )
      })
      .build
      .getOpenTelemetrySdk

    // Only way to disable metrics is the hack to clone OpenTelemetrySdk and override MeterProvider
    // with effective noop implementation
    // SdkMeterProvider is effectively a no-op as long as no reader registrations exist
    val noopSdkMeterProvider = SdkMeterProvider.builder()
      .build()

    OpenTelemetrySdk
      .builder()
      .setPropagators(openTelemetryWithMeterProvider.getPropagators)
      .setLoggerProvider(openTelemetryWithMeterProvider.getSdkLoggerProvider)
      .setTracerProvider(openTelemetryWithMeterProvider.getSdkTracerProvider)
      .setMeterProvider(noopSdkMeterProvider)
      .build()
  }

  private[this] def configureAzureMonitorDiagnostics
  (
    cosmosClientConfiguration: CosmosClientConfiguration,
    isSampledDiagnosticsLoggerEnabled: Boolean,
    telemetryConfig: CosmosClientTelemetryConfig
  ): TracingOptions = {
    val azMonConfig = cosmosClientConfiguration.azureMonitorConfig.get
    val openTelemetry = configureOpenTelemetrySdk(cosmosClientConfiguration, azMonConfig)

    // Pass OpenTelemetry container to TracingOptions.
    val tracingOptions = new OpenTelemetryTracingOptions()
      .setOpenTelemetry(openTelemetry)
      .setEnabled(true)
    val tracerProviderName = Option(tracingOptions.getTracerProvider)
      .map(p => p.getCanonicalName)
      .getOrElse("NO_TRACER_PROVIDER")
    logInfo(
      s"TracingOptions - enabled: ${tracingOptions.setEnabled(true)}, "
        + s"tracerProvider: $tracerProviderName")

    if (azMonConfig.metricCollectionIntervalInSeconds > 0) {
      val legacyTelemetryConfig = TelemetryConfiguration
        .createDefault()
      legacyTelemetryConfig.setConnectionString(azMonConfig.connectionString)

      val azureMonitorLegacyMeterConfig = new CosmosAzureMonitorLegacyConfig(
        60,
        azMonConfig.connectionString
      )

      val azureMonitorRegistry = AzureMonitorMeterRegistry
        .builder(azureMonitorLegacyMeterConfig)
        .telemetryConfiguration(legacyTelemetryConfig)
        .clock(Clock.SYSTEM)
        .build()

      CosmosClientMetrics.addMeterRegistry(azureMonitorRegistry)
      val metricsOptions = new CosmosMicrometerMetricsOptions()
        .meterRegistry(azureMonitorRegistry)
        .configureDefaultTagNames(
          CosmosMetricTagName.CONTAINER,
          CosmosMetricTagName.CLIENT_CORRELATION_ID,
          CosmosMetricTagName.OPERATION,
          CosmosMetricTagName.OPERATION_STATUS_CODE,
          CosmosMetricTagName.PARTITION_KEY_RANGE_ID,
          CosmosMetricTagName.SERVICE_ADDRESS,
          CosmosMetricTagName.ADDRESS_RESOLUTION_COLLECTION_MAP_REFRESH,
          CosmosMetricTagName.ADDRESS_RESOLUTION_FORCED_REFRESH,
          CosmosMetricTagName.REQUEST_STATUS_CODE,
          CosmosMetricTagName.REQUEST_OPERATION_TYPE
        )
        .setMetricCategories(
          CosmosMetricCategory.SYSTEM,
          CosmosMetricCategory.OPERATION_SUMMARY,
          CosmosMetricCategory.REQUEST_SUMMARY,
          CosmosMetricCategory.DIRECT_ADDRESS_RESOLUTIONS,
          CosmosMetricCategory.DIRECT_REQUESTS,
          CosmosMetricCategory.DIRECT_CHANNELS
        )
        .applyDiagnosticThresholdsForTransportLevelMeters(isSampledDiagnosticsLoggerEnabled)

      logInfo("Azure Monitor metrics configured.")
      telemetryConfig.metricsOptions(metricsOptions)
    } else {
      logInfo("Azure Monitor metrics disabled.")
    }

    logInfo(s"Azure Monitor OpenTelemetry configured")

    tracingOptions
  }

  private[this] def configureSampledDiagnosticsLogger
  (
    cosmosClientConfiguration: CosmosClientConfiguration,
    telemetryConfig: CosmosClientTelemetryConfig
  ): CosmosClientTelemetryConfig = {

    val sampledDiagnosticsLoggerConfig = cosmosClientConfiguration.sampledDiagnosticsLoggerConfig.get
    val diagnosticsLogger = new CosmosSamplingDiagnosticsLogger(
      sampledDiagnosticsLoggerConfig.samplingRateMaxCount,
      sampledDiagnosticsLoggerConfig.samplingRateIntervalInSeconds
    )

    val thresholds = new CosmosDiagnosticsThresholds()
      .setRequestChargeThreshold(sampledDiagnosticsLoggerConfig.thresholdsRequestCharge.toFloat)
      .setPointOperationLatencyThreshold(
        Duration.ofMillis(sampledDiagnosticsLoggerConfig.thresholdsPointOperationLatencyInMs)
      )
      .setNonPointOperationLatencyThreshold(
        Duration.ofMillis(sampledDiagnosticsLoggerConfig.thresholdsNonPointOperationLatencyInMs)
      )

    val failureHandler: BiPredicate[Integer, Integer] = (statusCode, subStatusCode) => {
      if (statusCode < 400) {
        false
      } else if (
        (statusCode == 404 && subStatusCode == 0)
          || statusCode == 409
          || statusCode == 412
      ) {
        false
      } else {
        statusCode != 429 || (subStatusCode > 0 && subStatusCode != 3200)
      }
    }

    thresholds.setFailureHandler(failureHandler)
    telemetryConfig
      .diagnosticsThresholds(thresholds)
      .diagnosticsHandler(diagnosticsLogger)
  }

  private[this] def configureDiagnosticsCore
  (
    cosmosClientConfiguration: CosmosClientConfiguration,
    builder: CosmosClientBuilder,
    isAzureMonitorOpenTelemetryEnabled: Boolean,
    isSampledDiagnosticsLoggerEnabled: Boolean
  ) : Unit =  {

    val customApplicationNameSuffix = cosmosClientConfiguration.customApplicationNameSuffix
      .getOrElse("")

    val clientCorrelationId = SparkSession.getActiveSession match {
      case Some(session) =>
        val ctx = session.sparkContext

        if (Strings.isNullOrWhiteSpace(customApplicationNameSuffix)) {
          s"${CosmosClientMetrics.executorId}-${ctx.appName}"
        } else {
          s"$customApplicationNameSuffix-${CosmosClientMetrics.executorId}-${ctx.appName}"
        }
      case None => customApplicationNameSuffix
    }

    var telemetryConfig = ImplementationBridgeHelpers
      .CosmosClientTelemetryConfigHelper
      .getCosmosClientTelemetryConfigAccessor
      .setOtelSpanAttributeNamingSchema(
        new CosmosClientTelemetryConfig().clientCorrelationId(clientCorrelationId),
        "V1"
      )

    if (isAzureMonitorOpenTelemetryEnabled) {
      val tracingOptions = configureAzureMonitorDiagnostics(
        cosmosClientConfiguration,
        isSampledDiagnosticsLoggerEnabled,
        telemetryConfig
      )

      telemetryConfig = telemetryConfig
        .tracingOptions(tracingOptions)
    } else {
      logInfo("Azure Monitor traces/logs disabled.")
    }


    if (isSampledDiagnosticsLoggerEnabled) {
      telemetryConfig = configureSampledDiagnosticsLogger(cosmosClientConfiguration, telemetryConfig)
    }

    builder.clientTelemetryConfig(telemetryConfig)
  }

  // scalastyle:off method.length
  // scalastyle:off cyclomatic.complexity
  private[this] def createCosmosAsyncClient(cosmosClientConfiguration: CosmosClientConfiguration,
                                            cosmosClientStateHandle: Option[CosmosClientMetadataCachesSnapshot]): CosmosAsyncClient = {
      if (cosmosClientConfiguration.enforceNativeTransport && !io.netty.channel.epoll.Epoll.isAvailable) {
        throw new IllegalStateException(
          "The enforcement of native transport is enabled in your configuration and native transport is not " +
            "available. Either ensure `spark.cosmos.enforceNativeTransport` is set to false or make " +
            "sure you use a Spark environment supporting native transport.",
          io.netty.channel.epoll.Epoll.unavailabilityCause()
        )
      }

      logInfo(s"Creating a CosmosClient for endpoint ${cosmosClientConfiguration.endpoint} "
      + s"(${cosmosClientConfiguration.applicationName}), Netty Leak detection enabled: "
      + s"${ResourceLeakDetector.isEnabled} at level: ${ResourceLeakDetector.getLevel}.")
      var builder = new CosmosClientBuilder()
          .endpoint(cosmosClientConfiguration.endpoint)
          .userAgentSuffix(cosmosClientConfiguration.applicationName)
          .throttlingRetryOptions(
              new ThrottlingRetryOptions()
                  .setMaxRetryAttemptsOnThrottledRequests(Int.MaxValue)
                  .setMaxRetryWaitTime(Duration.ofSeconds((Integer.MAX_VALUE / 1000) - 1)))

      val globalMetricsExporter = System.getProperty("otel.metrics.exporter")
      if (Option.apply(globalMetricsExporter).getOrElse("").isEmpty) {
        System.setProperty("otel.metrics.exporter", "none")
      } else {
        logInfo(s"Global Metrics Exporter $globalMetricsExporter")
      }
      val authConfig = cosmosClientConfiguration.authConfig
      authConfig match {
          case masterKeyAuthConfig: CosmosMasterKeyAuthConfig => builder.key(masterKeyAuthConfig.accountKey)
          case _ => builder.credential(toTokenCredential(cosmosClientConfiguration, authConfig))
      }

      configureDiagnostics(cosmosClientConfiguration, builder)

      if (cosmosClientConfiguration.disableTcpConnectionEndpointRediscovery) {
          builder.endpointDiscoveryEnabled(false)
      }

      if (cosmosClientConfiguration.readConsistencyStrategy != ReadConsistencyStrategy.DEFAULT) {
        if (cosmosClientConfiguration.readConsistencyStrategy == ReadConsistencyStrategy.EVENTUAL) {
          builder = builder.consistencyLevel(ConsistencyLevel.EVENTUAL)
        } else {
          builder = builder.readConsistencyStrategy(cosmosClientConfiguration.readConsistencyStrategy)
        }
      }

      if (cosmosClientConfiguration.useGatewayMode) {
          val gatewayCfg = new GatewayConnectionConfig()
              .setMaxConnectionPoolSize(cosmosClientConfiguration.httpConnectionPoolSize)
          builder = builder.gatewayMode(gatewayCfg)
      } else {
          var directConfig = new DirectConnectionConfig()
              .setConnectTimeout(Duration.ofSeconds(CosmosConstants.defaultDirectRequestTimeoutInSeconds))
              .setNetworkRequestTimeout(Duration.ofSeconds(CosmosConstants.defaultDirectRequestTimeoutInSeconds))

          directConfig =
          // Duplicate the default number of I/O threads per core
          // We know that Spark often works with large payloads and we have seen
          // indicators that the default number of I/O threads can be too low
          // for workloads with large payloads
              SparkBridgeImplementationInternal
                  .setIoThreadCountPerCoreFactor(directConfig, SparkBridgeImplementationInternal.getIoThreadCountPerCoreOverride)

          directConfig =
          // Spark workloads often result in very high CPU load
          // We have seen indicators that increasing Thread priority for I/O threads
          // can reduce transient I/O errors/timeouts in this case
              SparkBridgeImplementationInternal
                  .setIoThreadPriority(directConfig, Thread.MAX_PRIORITY)

          builder = builder.directMode(directConfig)

          if (cosmosClientConfiguration.proactiveConnectionInitialization.isDefined &&
            cosmosClientConfiguration.proactiveConnectionInitialization.get.nonEmpty) {
            val containerIdentities = CosmosAccountConfig.parseProactiveConnectionInitConfigs(
              cosmosClientConfiguration.proactiveConnectionInitialization.get)

            val initConfig = new CosmosContainerProactiveInitConfigBuilder(containerIdentities)
              .setAggressiveWarmupDuration(
                Duration.ofSeconds(cosmosClientConfiguration.proactiveConnectionInitializationDurationInSeconds))
              .setProactiveConnectionRegionsCount(1)
              .build
            builder.openConnectionsAndInitCaches(initConfig)
          }
      }

      if (cosmosClientConfiguration.preferredRegionsList.isDefined) {
          builder.preferredRegions(cosmosClientConfiguration.preferredRegionsList.get.toList.asJava)
      }

      // We saw incidents where even when Spark restarted Executors we haven't been able
      // to recover - most likely due to stale cache state being broadcast
      // Ideally the SDK would always be able to recover from stale cache state
      // but the main purpose of broadcasting the cache state is to avoid peeks in metadata
      // RU usage when multiple workers/executors are all started at the same time
      // Skipping the broadcast cache state for retries should be safe - because not all executors
      // will be restarted at the same time - and it adds an additional layer of safety.
      val isTaskRetryAttempt: Boolean = TaskContext.get() != null && TaskContext.get().attemptNumber() > 0

      val effectiveClientStateHandle = if (cosmosClientStateHandle.isDefined && !isTaskRetryAttempt) {
          Some(cosmosClientStateHandle.get)
      } else {

          if (cosmosClientStateHandle.isDefined && isTaskRetryAttempt) {
              logInfo(s"Ignoring broadcast client state handle because Task is getting retried. " +
                  s"Attempt Count: ${TaskContext.get().attemptNumber()}")
          }

          None
      }

      effectiveClientStateHandle match {
          case Some(handle) =>
              val metadataCache = handle
              SparkBridgeImplementationInternal.setMetadataCacheSnapshot(builder, metadataCache)
          case None =>
      }

      if (cosmosClientConfiguration.clientBuilderInterceptors.isDefined) {
        logInfo(s"Applying CosmosClientBuilder interceptors")
        for (interceptorFunction <- cosmosClientConfiguration.clientBuilderInterceptors.get) {
          builder = interceptorFunction.apply(builder)
        }
      }

      var client = builder.buildAsyncClient()

    if (cosmosClientConfiguration.clientInterceptors.isDefined) {
      logInfo(s"Applying CosmosClient interceptors")
      for (interceptorFunction <- cosmosClientConfiguration.clientInterceptors.get) {
        client = interceptorFunction.apply(client)
      }
    }

    client
  }
  // scalastyle:on method.length
  // scalastyle:on cyclomatic.complexity

  private[this] def createCosmosManagementClient
  (
    subscriptionId: String,
    azureEnvironment: AzureEnvironment,
    authConfig: CosmosServicePrincipalAuthConfig
  ): CosmosManager = {

      val azureProfile = new AzureProfile(authConfig.tenantId, subscriptionId, azureEnvironment)
      val tokenCredential = if (authConfig.clientCertPemBase64.isDefined) {
        val certInputStream = new ByteArrayInputStream(Base64.getDecoder.decode(authConfig.clientCertPemBase64.get))
        new ClientCertificateCredentialBuilder()
          .authorityHost(azureEnvironment.getActiveDirectoryEndpoint())
          .tenantId(authConfig.tenantId)
          .clientId(authConfig.clientId)
          .pemCertificate(certInputStream)
          .sendCertificateChain(authConfig.sendChain)
          .build()
      } else {
        new ClientSecretCredentialBuilder()
          .authorityHost(azureEnvironment.getActiveDirectoryEndpoint())
          .tenantId(authConfig.tenantId)
          .clientId(authConfig.clientId)
          .clientSecret(authConfig.clientSecret.get)
          .build()
      }
      CosmosManager.authenticate(tokenCredential, azureProfile)
  }

  private[this] def createCosmosManagementClient( subscriptionId: String,
                                                  azureEnvironment: AzureEnvironment,
                                                  authConfig: CosmosManagedIdentityAuthConfig): CosmosManager = {
    val azureProfile = new AzureProfile(authConfig.tenantId, subscriptionId, azureEnvironment)

    CosmosManager.authenticate(createTokenCredential(authConfig), azureProfile)
  }

  private[this] def createCosmosManagementClient(subscriptionId: String,
                                                 azureEnvironment: AzureEnvironment,
                                                 authConfig: CosmosAccessTokenAuthConfig): CosmosManager = {
    val tenantId = authConfig.tenantId.getOrElse(
      throw new IllegalArgumentException("Tenant ID must be provided for CosmosAccessTokenAuthConfig")
    )
    val azureProfile = new AzureProfile(tenantId, subscriptionId, azureEnvironment)

    CosmosManager.authenticate(createTokenCredential(authConfig), azureProfile)
  }

  private[this] def createTokenCredential(authConfig: CosmosManagedIdentityAuthConfig): CosmosAccessTokenCredential = {
    val tokenProvider: List[String] => CosmosAccessToken = {
        val tokenCredentialBuilder = new ManagedIdentityCredentialBuilder()
        if (authConfig.clientId.isDefined) {
          tokenCredentialBuilder.clientId(authConfig.clientId.get)
        }

        if (authConfig.resourceId.isDefined) {
          tokenCredentialBuilder.resourceId(authConfig.resourceId.get)
        }

        val tokenCredential = tokenCredentialBuilder.build()

        (tokenRequestContextStrings: List[String]) => {
          val tokenRequestContext = new TokenRequestContext
          tokenRequestContext.setScopes(tokenRequestContextStrings.asJava)
          val accessToken = tokenCredential
            .getToken(tokenRequestContext)
            .block()

          CosmosAccessToken(accessToken.getToken, accessToken.getExpiresAt)
        }
    }

    new CosmosAccessTokenCredential(tokenProvider)
  }

  private[this] def createTokenCredential(authConfig: CosmosAccessTokenAuthConfig): CosmosAccessTokenCredential = {
    new CosmosAccessTokenCredential(authConfig.tokenProvider)
  }

  private[this] def onCleanup(): Unit = {
    try {
      logDebug(s"-->onCleanup (${cache.size} clients)")
      val snapshot = cache.readOnlySnapshot()
      snapshot.foreach(pair => {
        val clientConfig = pair._1
        val clientMetadata = pair._2

        if (clientMetadata.lastRetrieved.get() < Instant.now.toEpochMilli - unusedClientTtlInMs) {
          if (clientMetadata.refCount.get() == 0) {
            if (clientMetadata.lastModified.get() < Instant.now.toEpochMilli - (cleanupIntervalInSeconds * 1000)) {
              logDebug(s"Removing client due to inactivity from the cache - ${clientConfig.endpoint}, " +
                s"${clientConfig.applicationName}, ${clientConfig.preferredRegionsList}, ${clientConfig.useGatewayMode}, " +
                s"${clientConfig.readConsistencyStrategy}, ${clientConfig.httpConnectionPoolSize}")
              purgeImpl(clientConfig, forceClosure = false)
            } else {
              logDebug("Client has not been retrieved from the cache recently and no spark task has been using " +
                s"it for < $cleanupIntervalInSeconds seconds. Waiting one more clean-up cycle before closing it, in " +
                s"case newly scheduled spark tasks need it - Created: ${clientMetadata.created}, " +
                s"LastModified ${clientMetadata.lastModified}, RefCount: ${clientMetadata.refCount}, " +
                s"Owning Spark tasks: [${clientMetadata.owners.keys.mkString(", ")}]")
            }
          } else {
            logDebug(s"Client has not been retrieved from the cache recently - Created: ${clientMetadata.created}, " +
              s"LastModified ${clientMetadata.lastModified}, RefCount: ${clientMetadata.refCount}, " +
              s"Owning Spark tasks: [${clientMetadata.owners.keys.mkString(", ")}]")
          }
        }
      })

      cleanUpToBeClosedWhenNotActiveAnymore(forceClosure = false)
    }
    catch {
      case t: Throwable =>
        logError("Callback invocation 'onCleanup' failed.", t)
    }
  }

  private[this] def cleanUpToBeClosedWhenNotActiveAnymore(forceClosure: Boolean): Unit = {
    toBeClosedWhenNotActiveAnymore
      .readOnlySnapshot()
      .foreach(kvp => if (forceClosure || kvp._2.refCount.get == 0) {
        if (toBeClosedWhenNotActiveAnymore.remove(kvp._1).isDefined) {
          // refCount is never going to increase once in this list
          // so it is save to close the client
          kvp._2.closeClients()
        }
      })
  }

  private[this] case class CosmosClientCacheMetadata
  (
    cosmosClient: CosmosAsyncClient,
    sparkCatalogClient: CosmosCatalogClient,
    clientConfig: CosmosClientConfiguration,
    lastRetrieved: AtomicLong,
    lastModified: AtomicLong,
    created: AtomicLong,
    refCount: AtomicLong,
    owners: TrieMap[OwnerInfo, Option[Boolean]]
  ) {
    def createCacheItemForReuse(ownerInfo: OwnerInfo) : CacheItemImpl = {
      val nowInEpochMilli = Instant.now.toEpochMilli
      lastRetrieved.set(nowInEpochMilli)
      lastModified.set(nowInEpochMilli)
      refCount.incrementAndGet()
      owners.putIfAbsent(ownerInfo, None)

      new CacheItemImpl(cosmosClient, sparkCatalogClient, this, ownerInfo)
    }

    def closeClients(): Unit = {
        cosmosClient.close()
        sparkCatalogClient.close()
    }
  }

  private[this] case class OwnerInfo(
                                      calledFrom: String,
                                      partitionId: Int,
                                      stageId: Int,
                                      taskAttemptId: Long,
                                      attemptNumber: Int,
                                      stageAttemptNumber: Int
                                    )

  private[this] object OwnerInfo {
    def apply(calledFrom: String): OwnerInfo = {
      Option[TaskContext](TaskContext.get) match {
        case Some(ctx) => OwnerInfo(calledFrom, ctx.partitionId(), ctx.stageId(), ctx.taskAttemptId(), ctx.attemptNumber(), ctx.stageAttemptNumber())
        case None => OwnerInfo(calledFrom, -1, -1, -1, -1, -1)
      }
    }
  }

  private[spark] case class ClientConfigurationWrapper
  (
    endpoint: String,
    authConfig: CosmosAuthConfig,
    applicationName: String,
    useGatewayMode: Boolean,
    // Intentionally not looking at proactive connection
    // initialization to distinguish cache key
    // You would never want separate clients just for this
    // difference
    httpConnectionPoolSize: Int,
    readConsistencyStrategy: ReadConsistencyStrategy,
    preferredRegionsList: String,
    clientBuilderInterceptors: Option[List[CosmosClientBuilder => CosmosClientBuilder]],
    clientInterceptors: Option[List[CosmosAsyncClient => CosmosAsyncClient]],
    sampledDiagnosticsLoggerConfig: Option[SampledDiagnosticsLoggerConfig],
    azureMonitorConfig: Option[AzureMonitorConfig]
  )

  private[this] object ClientConfigurationWrapper {
    def apply(clientConfig: CosmosClientConfiguration): ClientConfigurationWrapper = {
      ClientConfigurationWrapper(
        clientConfig.endpoint,
        clientConfig.authConfig,
        clientConfig.applicationName,
        clientConfig.useGatewayMode,
        clientConfig.httpConnectionPoolSize,
        clientConfig.readConsistencyStrategy,
        clientConfig.preferredRegionsList match {
          case Some(regionListArray) => s"[${regionListArray.mkString(", ")}]"
          case None => ""
        },
        clientConfig.clientBuilderInterceptors,
        clientConfig.clientInterceptors,
        clientConfig.sampledDiagnosticsLoggerConfig,
        clientConfig.azureMonitorConfig
      )
    }
  }

  def clearCache(): Unit = {
    cache.readOnlySnapshot().keys.foreach(clientCfgWrapper => purgeImpl(clientCfgWrapper, forceClosure = true))
    cache.clear()
    cleanUpToBeClosedWhenNotActiveAnymore(forceClosure = true)
  }

  private[this] class CacheItemImpl
  (
    val cosmosAsyncClient: CosmosAsyncClient,
    val catalogClient: CosmosCatalogClient,
    val ref: CosmosClientCacheMetadata,
    val ownerInfo: OwnerInfo
  ) extends CosmosClientCacheItem with BasicLoggingTrait {

    override def cosmosClient: CosmosAsyncClient = this.cosmosAsyncClient
    override def sparkCatalogClient: CosmosCatalogClient = this.catalogClient

    override def context: String = this.ownerInfo.toString

    override def close(): Unit = {
      val remainingActiveClients = ref.refCount.decrementAndGet()
      if (remainingActiveClients < 0) {
        logError(s"Cached cosmos client has been released to the Cache more often than acquired.")
      }

      ref.owners.remove(ownerInfo)
      ref.lastModified.set(Instant.now.toEpochMilli)

      logDebug("Returned client to the pool = remaining active clients - Count: " +
        s"$remainingActiveClients, Spark contexts: ${ref.owners.keys.mkString(", ")}")
    }

    override def getRefCount: Long = ref.refCount.get()
  }

  private[this] class ApplicationEndListener(val ctx: SparkContext)
    extends SparkListener
      with BasicLoggingTrait {

    override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd): Unit = {
        monitoredSparkApplications.remove(ctx) match {
          case Some(_) =>
            logInfo(
              s"ApplicationEndListener:onApplicationEnd(${ctx.hashCode}) closed - purging all cosmos clients")
            clearCache()
          case None =>
            logWarning(s"ApplicationEndListener:onApplicationEnd (${ctx.hashCode}) - not monitored anymore")
        }
    }
  }

  private[this] class CosmosAccessTokenCredential(val tokenProvider: List[String] =>CosmosAccessToken) extends TokenCredential {
    override def getToken(tokenRequestContext: TokenRequestContext): Mono[AccessToken] = {
      val returnValue: Mono[AccessToken] = Mono.fromCallable(() => {
        val token = tokenProvider
          .apply(tokenRequestContext.getScopes.asScala.toList)

        new AccessToken(token.token, token.Offset)
      })

      returnValue.publishOn(aadAuthBoundedElastic)
    }
  }

  private[this] class CosmosAzureMonitorLegacyConfig
  (
    val intervalInSeconds: Int,
    val effectiveConnectionString: String
  ) extends io.micrometer.azuremonitor.AzureMonitorConfig {

    override def get(s: String): String = {
      null
    }

    override def step(): Duration = {
      Duration.ofSeconds(intervalInSeconds)
    }

    override def connectionString(): String = effectiveConnectionString

    override def instrumentationKey(): String = ???

    override def enabled(): Boolean = {
      true
    }
  }
}
// scalastyle:on multiple.string.literals
