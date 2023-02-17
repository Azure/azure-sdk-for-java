// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.core.management.AzureEnvironment
import com.azure.core.management.profile.AzureProfile
import com.azure.cosmos.implementation.clienttelemetry.TagName
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, CosmosDaemonThreadFactory, SparkBridgeImplementationInternal, Strings}
import com.azure.cosmos.models.{CosmosClientTelemetryConfig, CosmosMetricCategory, CosmosMetricTagName, CosmosMicrometerMetricsOptions}
import com.azure.cosmos.spark.CosmosPredicates.isOnSparkDriver
import com.azure.cosmos.spark.catalog.{CosmosCatalogClient, CosmosCatalogCosmosSDKClient, CosmosCatalogManagementSDKClient}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.{ConsistencyLevel, CosmosAsyncClient, CosmosClientBuilder, DirectConnectionConfig, ThrottlingRetryOptions}
import com.azure.identity.ClientSecretCredentialBuilder
import com.azure.resourcemanager.cosmos.CosmosManager
import org.apache.spark.scheduler.{SparkListener, SparkListenerApplicationEnd}
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkContext, TaskContext}

import java.time.{Duration, Instant}
import java.util.ConcurrentModificationException
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import scala.collection.concurrent.TrieMap

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// scalastyle:off multiple.string.literals
private[spark] object CosmosClientCache extends BasicLoggingTrait {

  SparkBridgeImplementationInternal.setUserAgentWithSnapshotInsteadOfBeta()
  System.setProperty("COSMOS.SWITCH_OFF_IO_THREAD_FOR_RESPONSE", "true")

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
            case aadAuthConfig: CosmosAadAuthConfig =>
                sparkCatalogClient =
                    CosmosCatalogManagementSDKClient(
                        cosmosClientConfiguration.resourceGroupName.get,
                        cosmosClientConfiguration.databaseAccountName,
                        createCosmosManagementClient(
                            cosmosClientConfiguration.subscriptionId.get,
                            cosmosClientConfiguration.azureEnvironment,
                            aadAuthConfig),
                        cosmosAsyncClient)
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

  // scalastyle:off method.length
  // scalastyle:off cyclomatic.complexity
  private[this] def createCosmosAsyncClient(cosmosClientConfiguration: CosmosClientConfiguration,
                                            cosmosClientStateHandle: Option[CosmosClientMetadataCachesSnapshot]): CosmosAsyncClient = {
      var builder = new CosmosClientBuilder()
          .endpoint(cosmosClientConfiguration.endpoint)
          .userAgentSuffix(cosmosClientConfiguration.applicationName)
          .throttlingRetryOptions(
              new ThrottlingRetryOptions()
                  .setMaxRetryAttemptsOnThrottledRequests(Int.MaxValue)
                  .setMaxRetryWaitTime(Duration.ofSeconds((Integer.MAX_VALUE / 1000) - 1)))

      val authConfig = cosmosClientConfiguration.authConfig
      authConfig match {
          case masterKeyAuthConfig: CosmosMasterKeyAuthConfig => builder.key(masterKeyAuthConfig.accountKey)
          case aadAuthConfig: CosmosAadAuthConfig =>
              val tokenCredential = new ClientSecretCredentialBuilder()
                  .authorityHost(cosmosClientConfiguration.azureEnvironment.getActiveDirectoryEndpoint())
                  .tenantId(aadAuthConfig.tenantId)
                  .clientId(aadAuthConfig.clientId)
                  .clientSecret(aadAuthConfig.clientSecret)
                  .build()
              builder.credential(tokenCredential)
          case _ => throw new IllegalArgumentException(s"Authorization type ${authConfig.getClass} is not supported")
      }

      if (CosmosClientMetrics.meterRegistry.isDefined) {
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

          val metricsOptions = new CosmosMicrometerMetricsOptions()
            .meterRegistry(CosmosClientMetrics.meterRegistry.get)
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

          val telemetryConfig = new CosmosClientTelemetryConfig()
              .metricsOptions(metricsOptions)
              .clientCorrelationId(clientCorrelationId)

          builder.clientTelemetryConfig(telemetryConfig)
      }

      if (cosmosClientConfiguration.disableTcpConnectionEndpointRediscovery) {
          builder.endpointDiscoveryEnabled(false)
      }

      if (cosmosClientConfiguration.useEventualConsistency) {
          builder = builder.consistencyLevel(ConsistencyLevel.EVENTUAL)
      }

      if (cosmosClientConfiguration.useGatewayMode) {
          builder = builder.gatewayMode()
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
                  .setIoThreadCountPerCoreFactor(directConfig, CosmosConstants.defaultIoThreadCountFactorPerCore)

          directConfig =
          // Spark workloads often result in very high CPU load
          // We have seen indicators that increasing Thread priority for I/O threads
          // can reduce transient I/O errors/timeouts in this case
              SparkBridgeImplementationInternal
                  .setIoThreadPriority(directConfig, Thread.MAX_PRIORITY)

          builder = builder.directMode(directConfig)
      }

      if (cosmosClientConfiguration.preferredRegionsList.isDefined) {
          builder.preferredRegions(cosmosClientConfiguration.preferredRegionsList.get.toList.asJava)
      }

      if (cosmosClientConfiguration.enableClientTelemetry) {
          System.setProperty(
              "COSMOS.CLIENT_TELEMETRY_ENDPOINT",
              cosmosClientConfiguration.clientTelemetryEndpoint.getOrElse(
                  "https://tools.cosmos.azure.com/api/clienttelemetry/trace"
              ))
          System.setProperty(
              "COSMOS.CLIENT_TELEMETRY_ENABLED",
              "true")

          builder.clientTelemetryEnabled(true)
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

      builder.buildAsyncClient()
  }
  // scalastyle:on method.length
  // scalastyle:on cyclomatic.complexity

  private[this] def createCosmosManagementClient(
                                                    subscriptionId: String,
                                                    azureEnvironment: AzureEnvironment,
                                                    authConfig: CosmosAadAuthConfig): CosmosManager = {
      val azureProfile = new AzureProfile(authConfig.tenantId, subscriptionId, azureEnvironment)
      val tokenCredential = new ClientSecretCredentialBuilder()
          .authorityHost(azureEnvironment.getActiveDirectoryEndpoint())
          .tenantId(authConfig.tenantId)
          .clientId(authConfig.clientId)
          .clientSecret(authConfig.clientSecret)
          .build()
      CosmosManager.authenticate(tokenCredential, azureProfile)
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
                s"${clientConfig.useEventualConsistency}")
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

  private[spark] case class ClientConfigurationWrapper (
                                                        endpoint: String,
                                                        authConfig: CosmosAuthConfig,
                                                        applicationName: String,
                                                        useGatewayMode: Boolean,
                                                        useEventualConsistency: Boolean,
                                                        preferredRegionsList: String)

  private[this] object ClientConfigurationWrapper {
    def apply(clientConfig: CosmosClientConfiguration): ClientConfigurationWrapper = {
      ClientConfigurationWrapper(
        clientConfig.endpoint,
        clientConfig.authConfig,
        clientConfig.applicationName,
        clientConfig.useGatewayMode,
        clientConfig.useEventualConsistency,
        clientConfig.preferredRegionsList match {
          case Some(regionListArray) => s"[${regionListArray.mkString(", ")}]"
          case None => ""
        }
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
  }

  private[this] class ApplicationEndListener(val ctx: SparkContext)
    extends SparkListener
      with BasicLoggingTrait {

    override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd) {
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
}
// scalastyle:on multiple.string.literals
