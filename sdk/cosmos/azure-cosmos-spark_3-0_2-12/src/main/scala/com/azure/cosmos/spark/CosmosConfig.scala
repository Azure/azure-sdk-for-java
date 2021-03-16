// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.models.{CosmosChangeFeedRequestOptions, FeedRange}
import com.azure.cosmos.spark.ItemWriteStrategy.ItemWriteStrategy
import com.azure.cosmos.spark.ChangeFeedModes.ChangeFeedMode
import com.azure.cosmos.spark.ChangeFeedStartFromModes.{ChangeFeedStartFromMode, PointInTime}
import com.azure.cosmos.spark.PartitioningStrategies.PartitioningStrategy
import java.net.URL
import java.util.Locale

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.read.streaming.ReadLimit

import java.time.{Duration, Instant}
import java.time.format.DateTimeFormatter
import collection.immutable.Map

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// scalastyle:off multiple.string.literals

// each config category will be a case class:
// TODO moderakh more configs
//case class ClientConfig()
//case class CosmosBatchWriteConfig()

private object CosmosConfig {

  def getEffectiveConfig(sparkConf: SparkConf, // spark application config
                         userProvidedOptions: Map[String, String] // user provided config
                        ) : Map[String, String] = {
    val conf = sparkConf.clone()
    conf.setAll(userProvidedOptions).getAll.toMap
  }

  @throws[IllegalStateException] // if there is no active spark session
  def getEffectiveConfig(userProvidedOptions: Map[String, String] = Map().empty) : Map[String, String] = {
    val session = SparkSession.active

    // TODO: moderakh we should investigate how spark sql config should be merged:
    // TODO: session.conf.getAll, // spark sql runtime config
    getEffectiveConfig(
      session.sparkContext.getConf, // spark application config
      userProvidedOptions) // user provided config
  }
}

private case class CosmosAccountConfig(
  endpoint: String,
  key: String,
  accountName: String,
  applicationName: Option[String],
  useGatewayMode: Boolean)

private object CosmosAccountConfig {
  private val CosmosAccountEndpointUri = CosmosConfigEntry[String](key = "spark.cosmos.accountEndpoint",
    mandatory = true,
    parseFromStringFunction = accountEndpointUri => {
      new URL(accountEndpointUri)
      accountEndpointUri
    },
    helpMessage = "Cosmos DB Account Endpoint Uri")

  private val CosmosKey = CosmosConfigEntry[String](key = "spark.cosmos.accountKey",
    mandatory = true,
    parseFromStringFunction = accountKey => accountKey,
    helpMessage = "Cosmos DB Account Key")

  private val CosmosAccountName = CosmosConfigEntry[String](key = "spark.cosmos.accountEndpoint",
    mandatory = true,
    parseFromStringFunction = accountEndpointUri => {
      val url = new URL(accountEndpointUri)
      val separatorIndex = url.getHost.indexOf('.')
      if (separatorIndex > 0) {
          url.getHost.substring(0, separatorIndex)
      } else {
        url.getHost
      }
    },
    helpMessage = "Cosmos DB Account Name")

  private val ApplicationName = CosmosConfigEntry[String](key = "spark.cosmos.applicationName",
    mandatory = false,
    parseFromStringFunction = applicationName => applicationName,
    helpMessage = "Application name")

  private val UseGatewayMode = CosmosConfigEntry[Boolean](key = "spark.cosmos.useGatewayMode",
    mandatory = false,
    defaultValue = Some(false),
    parseFromStringFunction = useGatewayMode => useGatewayMode.toBoolean,
    helpMessage = "Use gateway mode for the client operations")

  def parseCosmosAccountConfig(cfg: Map[String, String]): CosmosAccountConfig = {
    val endpointOpt = CosmosConfigEntry.parse(cfg, CosmosAccountEndpointUri)
    val key = CosmosConfigEntry.parse(cfg, CosmosKey)
    val accountName = CosmosConfigEntry.parse(cfg, CosmosAccountName)
    val applicationName = CosmosConfigEntry.parse(cfg, ApplicationName)
    val useGatewayMode = CosmosConfigEntry.parse(cfg, UseGatewayMode)

    // parsing above already validated these assertions
    assert(endpointOpt.isDefined)
    assert(key.isDefined)
    assert(accountName.isDefined)

    CosmosAccountConfig(
      endpointOpt.get,
      key.get,
      accountName.get,
      applicationName,
      useGatewayMode.get)
  }
}

private case class CosmosReadConfig(forceEventualConsistency: Boolean)

private object CosmosReadConfig {
  private val ForceEventualConsistency = CosmosConfigEntry[Boolean](key = "spark.cosmos.read.forceEventualConsistency",
    mandatory = false,
    defaultValue = Some(true),
    parseFromStringFunction = value => value.toBoolean,
    helpMessage = "Makes the client use Eventual consistency for read operations")

  def parseCosmosReadConfig(cfg: Map[String, String]): CosmosReadConfig = {
    val forceEventualConsistency = CosmosConfigEntry.parse(cfg, ForceEventualConsistency)

    CosmosReadConfig(forceEventualConsistency.get)
  }
}

private[cosmos] case class CosmosContainerConfig(database: String, container: String)

private object ItemWriteStrategy extends Enumeration {
  type ItemWriteStrategy = Value
  val ItemOverwrite, ItemAppend = Value

  def withNameOrThrow(name: String): Value =
    values.find(_.toString.toLowerCase == name.toLowerCase()).getOrElse(
      throw new IllegalArgumentException("name is not a valid ItemWriteStrategy"))
}

private case class CosmosWriteConfig(itemWriteStrategy: ItemWriteStrategy,
                                     maxRetryCount: Int,
                                     bulkEnabled: Boolean,
                                     maxConcurrencyOpt: Option[Int])

private object CosmosWriteConfig {
  private val bulkEnabled = CosmosConfigEntry[Boolean](key = "spark.cosmos.write.bulkEnabled",
    defaultValue = Option.apply(false),
    mandatory = false,
    parseFromStringFunction = bulkEnabledAsString => bulkEnabledAsString.toBoolean,
    helpMessage = "Cosmos DB Item Write bulk enabled")

  // TODO: moderakh this should be dynamic based on the number of CPU cores
  private val MaxPointWriteConcurrency = 100
  private val MaxBulkWriteConcurrency = 100000
  private val MaxRetryCount = 3

  private val writeConcurrency = CosmosConfigEntry[Int](key = "spark.cosmos.write.maxConcurrency",
    defaultValue = Option.apply(MaxPointWriteConcurrency),
    mandatory = false,
    parseFromStringFunction = bulkMaxConcurrencyAsString => bulkMaxConcurrencyAsString.toInt,
    helpMessage = s"Cosmos DB Item Write Max concurrency." +
      s" If not specified it will be determined based on the Spark executor VM Size")

  private val itemWriteStrategy = CosmosConfigEntry[ItemWriteStrategy](key = "spark.cosmos.write.strategy",
    defaultValue = Option.apply(ItemWriteStrategy.ItemOverwrite),
    mandatory = false,
    parseFromStringFunction = itemWriteStrategyAsString =>
      ItemWriteStrategy.withNameOrThrow(itemWriteStrategyAsString),
    helpMessage = "Cosmos DB Item write Strategy: ItemOverwrite (using upsert), ItemAppend (using create, ignore 409)")

  private val maxRetryCount = CosmosConfigEntry[Int](key = "spark.cosmos.write.maxRetryCount",
    mandatory = false,
    defaultValue = Option.apply(MaxRetryCount),
    parseFromStringFunction = maxRetryAttempt => {
      val cnt = maxRetryAttempt.toInt
      if (cnt < 0) {
        throw new IllegalArgumentException(s"expected a non-negative number")
      }
      cnt
    },
    helpMessage = "Cosmos DB Write Max Retry Attempts on failure")

  def parseWriteConfig(cfg: Map[String, String]): CosmosWriteConfig = {
    val itemWriteStrategyOpt = CosmosConfigEntry.parse(cfg, itemWriteStrategy)
    val maxRetryCountOpt = CosmosConfigEntry.parse(cfg, maxRetryCount)
    val bulkEnabledOpt = CosmosConfigEntry.parse(cfg, bulkEnabled)
    assert(bulkEnabledOpt.isDefined)
    val maxConcurrencyOpt = CosmosConfigEntry.parse(cfg, writeConcurrency)

    // parsing above already validated this
    assert(itemWriteStrategyOpt.isDefined)
    assert(maxRetryCountOpt.isDefined)
    assert(bulkEnabledOpt.isDefined)

    CosmosWriteConfig(
      itemWriteStrategyOpt.get,
      maxRetryCountOpt.get,
      bulkEnabledOpt.get,
      maxConcurrencyOpt)
  }
}

private object CosmosContainerConfig {
  private val DATABASE_NAME_KEY = "spark.cosmos.database"
  private val CONTAINER_NAME_KEY = "spark.cosmos.container"

  private val databaseNameSupplier = CosmosConfigEntry[String](key = DATABASE_NAME_KEY,
    mandatory = true,
    parseFromStringFunction = database => database,
    helpMessage = "Cosmos DB database name")

  private val containerNameSupplier = CosmosConfigEntry[String](key = CONTAINER_NAME_KEY,
    mandatory = true,
    parseFromStringFunction = container => container,
    helpMessage = "Cosmos DB container name")

  def parseCosmosContainerConfig(cfg: Map[String, String]): CosmosContainerConfig = {
    this.parseCosmosContainerConfig(cfg, None, None)
  }

  def parseCosmosContainerConfig(
                                cfg: Map[String, String],
                                databaseName: Option[String],
                                containerName: Option[String]): CosmosContainerConfig = {

    val databaseOpt = databaseName.getOrElse(CosmosConfigEntry.parse(cfg, databaseNameSupplier).get)
    val containerOpt = containerName.getOrElse(CosmosConfigEntry.parse(cfg, containerNameSupplier).get)

    CosmosContainerConfig(databaseOpt, containerOpt)
  }
}

private case class CosmosSchemaInferenceConfig(inferSchemaSamplingSize: Int,
                                               inferSchemaEnabled: Boolean,
                                               includeSystemProperties: Boolean,
                                               includeTimestamp: Boolean,
                                               inferSchemaQuery: Option[String])

private object CosmosSchemaInferenceConfig {
  private val DefaultSampleSize: Int = 1000

  private val inferSchemaSamplingSize = CosmosConfigEntry[Int](key = "spark.cosmos.read.inferSchemaSamplingSize",
    mandatory = false,
    defaultValue = Some(DefaultSampleSize),
    parseFromStringFunction = size => size.toInt,
    helpMessage = "Sampling size to use when inferring schema")

  private val inferSchemaEnabled = CosmosConfigEntry[Boolean](key = "spark.cosmos.read.inferSchemaEnabled",
    mandatory = false,
    defaultValue = Some(true),
    parseFromStringFunction = enabled => enabled.toBoolean,
    helpMessage = "Whether schema inference is enabled or should return raw json")

  private val inferSchemaIncludeSystemProperties = CosmosConfigEntry[Boolean](key = "spark.cosmos.read.inferSchemaIncludeSystemProperties",
    mandatory = false,
    defaultValue = Some(false),
    parseFromStringFunction = include => include.toBoolean,
    helpMessage = "Whether schema inference should include the system properties in the schema")

  private val inferSchemaIncludeTimestamp = CosmosConfigEntry[Boolean](key = "spark.cosmos.read.inferSchemaIncludeTimestamp",
    mandatory = false,
    defaultValue = Some(false),
    parseFromStringFunction = include => include.toBoolean,
    helpMessage = "Whether schema inference should include the timestamp (_ts) property")

  private val inferSchemaQuery = CosmosConfigEntry[String](key = "spark.cosmos.read.inferSchemaQuery",
    mandatory = false,
    parseFromStringFunction = query => query,
    helpMessage = "When schema inference is enabled, used as custom query to infer it")

  def parseCosmosReadConfig(cfg: Map[String, String]): CosmosSchemaInferenceConfig = {
    val samplingSize = CosmosConfigEntry.parse(cfg, inferSchemaSamplingSize)
    val enabled = CosmosConfigEntry.parse(cfg, inferSchemaEnabled)
    val query = CosmosConfigEntry.parse(cfg, inferSchemaQuery)
    val includeSystemProperties = CosmosConfigEntry.parse(cfg, inferSchemaIncludeSystemProperties)
    val includeTimestamp = CosmosConfigEntry.parse(cfg, inferSchemaIncludeTimestamp)

    assert(samplingSize.isDefined)
    assert(enabled.isDefined)
    CosmosSchemaInferenceConfig(
      samplingSize.get,
      enabled.get,
      includeSystemProperties.get,
      includeTimestamp.get,
      query)
  }
}

private object PartitioningStrategies extends Enumeration {
  type PartitioningStrategy = Value

  val Restrictive: PartitioningStrategies.Value = Value("Restrictive")
  val Default: PartitioningStrategies.Value  = Value("Default")
  val Aggressive: PartitioningStrategies.Value  = Value("Aggressive")
  val Custom: PartitioningStrategies.Value  = Value("Custom")
}

private case class CosmosPartitioningConfig
(
  partitioningStrategy: PartitioningStrategy,
  targetedPartitionCount: Option[Int]
)

private object CosmosPartitioningConfig {
  private val DefaultPartitioningStrategy: PartitioningStrategy = PartitioningStrategies.Default

  private val targetedPartitionCount = CosmosConfigEntry[Int](
    key = "spark.cosmos.partitioning.targetedCount",
    keySuffix = Option.apply("(if strategy is custom)"),
    mandatory = true,
    parseFromStringFunction = targetedCountText => targetedCountText.toInt,
    helpMessage = "The targeted Partition Count. This parameter is optional and ignored unless " +
      "strategy==Custom is used. In this case the Spark Connector won't dynamically calculate " +
      "number of partitions but stick with this value.")

  private val partitioningStrategy = CosmosConfigEntry[PartitioningStrategy](
    key = "spark.cosmos.partitioning.strategy",
    mandatory = false,
    parseFromStringFunction = strategyNotYetParsed => this.validatePartitioningStrategy(strategyNotYetParsed),
    helpMessage = "The partitioning strategy used (Default, Custom, Restrictive or Aggressive)")

  private def validatePartitioningStrategy(partitioningStrategyName: String): PartitioningStrategy = {
    Option(partitioningStrategyName).fold(DefaultPartitioningStrategy)(p => {
      val strategyName = p.trim

      if (strategyName.isEmpty) {
        DefaultPartitioningStrategy
      } else {
        PartitioningStrategies
          .values
          .find(_.toString.equalsIgnoreCase(strategyName))
          .getOrElse(throw new IllegalArgumentException(s"Invalid partitioning strategy '$partitioningStrategyName'"))
      }
    })
  }

  def parseCosmosPartitioningConfig(cfg: Map[String, String]): CosmosPartitioningConfig = {
    val partitioningStrategyParsed = CosmosConfigEntry
      .parse(cfg, partitioningStrategy)
      .getOrElse(DefaultPartitioningStrategy)

    val targetedPartitionCountParsed = if (partitioningStrategyParsed == PartitioningStrategies.Custom) {
      CosmosConfigEntry.parse(cfg, targetedPartitionCount)
    } else {
      None
    }

    CosmosPartitioningConfig(
      partitioningStrategyParsed,
      targetedPartitionCountParsed
    )
  }
}

private object ChangeFeedModes extends Enumeration {
  type ChangeFeedMode = Value

  val Incremental: ChangeFeedModes.Value = Value("Incremental")
  val FullFidelity: ChangeFeedModes.Value = Value("FullFidelity")
}

private object ChangeFeedStartFromModes extends Enumeration {
  type ChangeFeedStartFromMode = Value

  val Beginning: ChangeFeedStartFromModes.Value = Value("Beginning")
  val Now: ChangeFeedStartFromModes.Value = Value("Now")
  val PointInTime: ChangeFeedStartFromModes.Value = Value("PointInTime")
}

private case class CosmosChangeFeedConfig
(
  changeFeedMode: ChangeFeedMode,
  startFrom: ChangeFeedStartFromMode,
  startFromPointInTime: Option[Instant],
  maxItemCountPerTrigger: Option[Long]
) {

  def toRequestOptions(feedRange: FeedRange): CosmosChangeFeedRequestOptions = {
    val options = this.startFrom match {
      case ChangeFeedStartFromModes.Now =>
        CosmosChangeFeedRequestOptions.createForProcessingFromNow(feedRange)
      case ChangeFeedStartFromModes.Beginning =>
        CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(feedRange)
      case ChangeFeedStartFromModes.PointInTime =>
        CosmosChangeFeedRequestOptions.createForProcessingFromPointInTime(this.startFromPointInTime.get, feedRange)
    }

    this.changeFeedMode match {
      case ChangeFeedModes.Incremental => options
      case ChangeFeedModes.FullFidelity => options.fullFidelity()
    }
  }

  def toReadLimit: ReadLimit = {
    this.maxItemCountPerTrigger match {
      case Some(maxItemCount) => ReadLimit.maxRows(maxItemCount)
      case None => ReadLimit.allAvailable()
    }
  }
}

private object CosmosChangeFeedConfig {
  private val DefaultChangeFeedMode: ChangeFeedMode = ChangeFeedModes.Incremental
  private val DefaultStartFromMode: ChangeFeedStartFromMode = ChangeFeedStartFromModes.Beginning

  private val startFrom = CosmosConfigEntry[ChangeFeedStartFromMode](
    key = "spark.cosmos.changeFeed.startFrom",
    mandatory = false,
    parseFromStringFunction = startFromNotYetValidated => this.validateStartFromMode(startFromNotYetValidated),
    helpMessage = "ChangeFeed Start from settings (Now, Beginning  or a certain point in " +
      "time (UTC) for example 2020-02-10T14:15:03) - the default value is 'Beginning'.")

  private val startFromPointInTime = CosmosConfigEntry[Instant](
    key = "spark.cosmos.changeFeed.startFrom",
    keySuffix = Option.apply("(for point in time)"),
    mandatory = true,
    parseFromStringFunction = startFrom => Instant.from(DateTimeFormatter
      .ISO_INSTANT
      .parse(startFrom.trim)),
    helpMessage = "ChangeFeed Start from settings (Now, Beginning  or a certain point in " +
      "time (UTC) for example 2020-02-10T14:15:03Z) - the default value is 'Beginning'.")

  private val changeFeedMode = CosmosConfigEntry[ChangeFeedMode](
    key = "spark.cosmos.changeFeed.mode",
    mandatory = false,
    parseFromStringFunction = changeFeedModeString => validateChangeFeedMode(changeFeedModeString),
    helpMessage = "ChangeFeed mode (Incremental or FullFidelity)")

  private val maxItemCountPerTriggerHint = CosmosConfigEntry[Long](
    key = "spark.cosmos.changeFeed.maxItemCountPerTriggerHint",
    mandatory = false,
    parseFromStringFunction = maxItemCount => maxItemCount.toInt,
    helpMessage = "Approximate maximum number of items read from change feed for each trigger")

  private def validateChangeFeedMode(mode: String): ChangeFeedMode = {
    Option(mode).fold(DefaultChangeFeedMode)(p => {
      val modeName = p.trim

      if (modeName.isEmpty) {
        DefaultChangeFeedMode
      } else {
        ChangeFeedModes
          .values
          .find(_.toString.equalsIgnoreCase(modeName))
          .getOrElse(throw new IllegalArgumentException(s"Invalid change feed mode '$modeName'"))
      }
    })
  }

  // TODO @fabianm consider normalizing on single enum parsing implementation
  private def validateStartFromMode(startFrom: String): ChangeFeedStartFromMode = {
    Option(startFrom).fold(DefaultStartFromMode)(sf => {
      val trimmed = sf.trim

      if (trimmed.isEmpty) {
        DefaultStartFromMode
      } else if (trimmed.equalsIgnoreCase(ChangeFeedStartFromModes.Beginning.toString)) {
        ChangeFeedStartFromModes.Beginning
      } else if (trimmed.equalsIgnoreCase(ChangeFeedStartFromModes.Now.toString)) {
        ChangeFeedStartFromModes.Now
      } else {
        ChangeFeedStartFromModes.PointInTime
      }
    })
  }

  def parseCosmosChangeFeedConfig(cfg: Map[String, String]): CosmosChangeFeedConfig = {
    val changeFeedModeParsed = CosmosConfigEntry.parse(cfg, changeFeedMode)
    val startFromModeParsed = CosmosConfigEntry.parse(cfg, startFrom)
    val maxItemCountPerTriggerHintParsed = CosmosConfigEntry.parse(cfg, maxItemCountPerTriggerHint)
    val startFromPointInTimeParsed = startFromModeParsed match {
      case Some(PointInTime) => CosmosConfigEntry.parse(cfg, startFromPointInTime)
      case _ => None
    }

    CosmosChangeFeedConfig(
      changeFeedModeParsed.getOrElse(DefaultChangeFeedMode),
      startFromModeParsed.getOrElse(DefaultStartFromMode),
      startFromPointInTimeParsed,
      maxItemCountPerTriggerHintParsed)
  }
}

private case class CosmosThroughputControlConfig(groupName: String,
                                                 targetThroughput: Option[Int],
                                                 targetThroughputThreshold: Option[Double],
                                                 globalControlDatabase: String,
                                                 globalControlContainer: String,
                                                 globalControlRenewInterval: Option[Duration],
                                                 globalControlExpireInterval: Option[Duration])

private object CosmosThroughputControlConfig {
    private val throughputControlEnabledSupplier = CosmosConfigEntry[Boolean](
        key = "spark.cosmos.throughputControlEnabled",
        mandatory = false,
        defaultValue = Some(false),
        parseFromStringFunction = enableThroughputControl => enableThroughputControl.toBoolean,
        helpMessage = "A flag to indicate whether throughput control is enabled.")

    private val groupNameSupplier = CosmosConfigEntry[String](
        key = "spark.cosmos.throughputControl.name",
        mandatory = false,
        parseFromStringFunction = groupName => groupName,
        helpMessage = "Throughput control group name. " +
            "Since customer is allowed to create many groups for a container, the name should be unique.")

    private val targetThroughputSupplier = CosmosConfigEntry[Int](
        key = "spark.cosmos.throughputControl.targetThroughput",
        mandatory = false,
        parseFromStringFunction = targetThroughput => targetThroughput.toInt,
        helpMessage = "Throughput control group target throughput. The value should be larger than 0.")

    private val targetThroughputThresholdSupplier = CosmosConfigEntry[Double](
        key = "spark.cosmos.throughputControl.targetThroughputThreshold",
        mandatory = false,
        parseFromStringFunction = targetThroughput => targetThroughput.toDouble,
        helpMessage = "Throughput control group target throughput threshold. The value should be between (0,1]. ")

    private val globalControlDatabaseSupplier = CosmosConfigEntry[String](
        key = "spark.cosmos.throughputControl.globalControl.database",
        mandatory = false,
        parseFromStringFunction = globalControlDatabase => globalControlDatabase,
        helpMessage = "Database which will be used for throughput global control.")

    private val globalControlContainerSupplier = CosmosConfigEntry[String](
        key = "spark.cosmos.throughputControl.globalControl.container",
        mandatory = false,
        parseFromStringFunction = globalControlContainer => globalControlContainer,
        helpMessage = "Container which will be used for throughput global control.")

    private val globalControlItemRenewIntervalSupplier = CosmosConfigEntry[Duration](
        key = "spark.cosmos.throughputControl.globalControl.renewIntervalInMS",
        mandatory = false,
        parseFromStringFunction = renewIntervalInMilliseconds => Duration.ofMillis(renewIntervalInMilliseconds.toInt),
        helpMessage = "This controls how often the client is going to update the throughput usage of itself " +
            "and adjust its own throughput share based on the throughput usage of other clients. " +
            "Default is 5s, the allowed min value is 5s.")

    private val globalControlItemExpireIntervalSupplier = CosmosConfigEntry[Duration](
        key = "spark.cosmos.throughputControl.globalControl.expireIntervalInMS",
        mandatory = false,
        parseFromStringFunction = expireIntervalInMilliseconds => Duration.ofMillis(expireIntervalInMilliseconds.toInt),
        helpMessage = "This controls how quickly we will detect the client has been offline " +
            "and hence allow its throughput share to be taken by other clients. " +
            "Default is 11s, the allowed min value is 2 * renewIntervalInMS + 1")

    def parseThroughputControlConfig(cfg: Map[String, String]): Option[CosmosThroughputControlConfig] = {
        val throughputControlEnabled = CosmosConfigEntry.parse(cfg, throughputControlEnabledSupplier).get

        if (throughputControlEnabled) {
            val groupName = CosmosConfigEntry.parse(cfg, groupNameSupplier)
            val targetThroughput = CosmosConfigEntry.parse(cfg, targetThroughputSupplier)
            val targetThroughputThreshold = CosmosConfigEntry.parse(cfg, targetThroughputThresholdSupplier)
            val globalControlDatabase = CosmosConfigEntry.parse(cfg, globalControlDatabaseSupplier)
            val globalControlContainer = CosmosConfigEntry.parse(cfg, globalControlContainerSupplier)
            val globalControlItemRenewInterval = CosmosConfigEntry.parse(cfg, globalControlItemRenewIntervalSupplier)
            val globalControlItemExpireInterval = CosmosConfigEntry.parse(cfg, globalControlItemExpireIntervalSupplier)

            assert(groupName.isDefined)
            assert(globalControlDatabase.isDefined)
            assert(globalControlContainer.isDefined)

            Some(CosmosThroughputControlConfig(
                groupName.get,
                targetThroughput,
                targetThroughputThreshold,
                globalControlDatabase.get,
                globalControlContainer.get,
                globalControlItemRenewInterval,
                globalControlItemExpireInterval))
        } else {
            None
        }
    }
}

private case class CosmosConfigEntry[T](key: String,
                                        mandatory: Boolean,
                                        defaultValue: Option[T] = Option.empty,
                                        parseFromStringFunction: String => T,
                                        helpMessage: String,
                                        keySuffix: Option[String] = None) {
  CosmosConfigEntry.configEntriesDefinitions.put(key + keySuffix.getOrElse(""), this)

  def parse(paramAsString: String) : T = {
    try {
      parseFromStringFunction(paramAsString)
    } catch {
      case e: Exception => throw new RuntimeException(
        s"invalid configuration for $key:$paramAsString. Config description: $helpMessage",  e)
    }
  }
}

// TODO: moderakh how to merge user config with SparkConf application config?
private object CosmosConfigEntry {
  private val configEntriesDefinitions = new java.util.HashMap[String, CosmosConfigEntry[_]]()

  def allConfigNames(): Seq[String] = {
    configEntriesDefinitions.keySet().asScala.toSeq
  }

  def parse[T](configuration: Map[String, String], configEntry: CosmosConfigEntry[T]): Option[T] = {
    // TODO moderakh: where should we handle case sensitivity?
    // we are doing this here per config parsing for now
    val opt = configuration
      .map { case (key, value) => (key.toLowerCase(Locale.ROOT), value) }
      .get(configEntry.key.toLowerCase(Locale.ROOT))
    if (opt.isDefined) {
      Option.apply(configEntry.parse(opt.get))
    }
    else {
      if (configEntry.mandatory) {
        throw new RuntimeException(
          s"mandatory option ${configEntry.key} is missing. Config description: ${configEntry.helpMessage}")
      } else {
        configEntry.defaultValue.orElse(Option.empty)
      }
    }
  }
}
// scalastyle:on multiple.string.literals
