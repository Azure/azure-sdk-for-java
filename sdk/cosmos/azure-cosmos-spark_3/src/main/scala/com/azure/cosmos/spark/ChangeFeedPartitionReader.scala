// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple
import com.azure.cosmos.implementation.{ChangeFeedSparkRowItem, ImplementationBridgeHelpers, ObjectNodeMap, SparkBridgeImplementationInternal, Strings, Utils}
import com.azure.cosmos.models.{CosmosChangeFeedRequestOptions, ModelBridgeInternal, PartitionKeyDefinition}
import com.azure.cosmos.spark.ChangeFeedPartitionReader.LsnPropertyName
import com.azure.cosmos.spark.CosmosConstants.MetricNames
import com.azure.cosmos.spark.CosmosPredicates.requireNotNull
import com.azure.cosmos.spark.CosmosTableSchemaInferrer.LsnAttributeName
import com.azure.cosmos.spark.diagnostics.{DetailedFeedDiagnosticsProvider, DiagnosticsContext, DiagnosticsLoader, LoggerHelper, SparkTaskContext}
import com.azure.cosmos.{CosmosItemSerializer, CosmosItemSerializerNoExceptionWrapping, SparkBridgeInternal}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.TaskContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.connector.metric.CustomTaskMetric
import org.apache.spark.sql.connector.read.PartitionReader
import org.apache.spark.sql.types.StructType

import java.util

private object ChangeFeedPartitionReader {
  val LsnPropertyName: String = LsnAttributeName
}

// per spark task there will be one CosmosPartitionReader.
// This provides iterator to read from the assigned spark partition
// For now we are creating only one spark partition per physical partition
private case class ChangeFeedPartitionReader
(
  partition: CosmosInputPartition,
  config: Map[String, String],
  readSchema: StructType,
  diagnosticsContext: DiagnosticsContext,
  cosmosClientStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots],
  diagnosticsConfig: DiagnosticsConfig,
  sparkEnvironmentInfo: String
) extends PartitionReader[InternalRow] {

  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)

  requireNotNull(partition, "partition")
  assert(partition.continuationState.isDefined, "Argument 'partition.continuationState' must be defined here.")
  log.logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  private val startLsn = getPartitionStartLsn

  private val changeFeedLSNRangeMetric = new CustomTaskMetric {
    override def name(): String = MetricNames.ChangeFeedLsnRange
    override def value(): Long = getChangeFeedLSNRange
  }
  private val changeFeedItemsCntMetric = new CustomTaskMetric {
    override def name(): String = MetricNames.ChangeFeedItemsCnt
    override def value(): Long = getChangeFeedItemsCnt
  }
  private val changeFeedPartitionIndexMetric = new CustomTaskMetric {
    override def name(): String = MetricNames.ChangeFeedPartitionIndex
    override def value(): Long = if (partition.index.isDefined) partition.index.get else -1
  }

  private val containerTargetConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)
  log.logInfo(s"Reading from feed range ${partition.feedRange}, startLsn $getPartitionStartLsn, " +
    s"endLsn ${partition.endLsn} of " +
    s"container ${containerTargetConfig.database}.${containerTargetConfig.container}")
  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(config)
  private val clientCacheItem = CosmosClientCache(
    CosmosClientConfiguration(
      config,
      readConfig.readConsistencyStrategy,
      sparkEnvironmentInfo),
    Some(cosmosClientStateHandles.value.cosmosClientMetadataCaches),
    s"ChangeFeedPartitionReader(partition $partition)")

  private val throughputControlClientCacheItemOpt =
    ThroughputControlHelper.getThroughputControlClientCacheItem(
      config,
      clientCacheItem.context,
      Some(cosmosClientStateHandles),
      sparkEnvironmentInfo)

  private val cosmosAsyncContainer =
    ThroughputControlHelper.getContainer(
      config,
      containerTargetConfig,
      clientCacheItem,
      throughputControlClientCacheItemOpt)

  private val partitionKeyDefinition: Option[PartitionKeyDefinition] =
    if (diagnosticsConfig.mode.isDefined &&
      diagnosticsConfig.mode.get.equalsIgnoreCase(classOf[DetailedFeedDiagnosticsProvider].getName)) {

      Option.apply(SparkBridgeInternal
        .getContainerPropertiesFromCollectionCache(cosmosAsyncContainer)
        .getPartitionKeyDefinition)
    } else {
      None
    }

  private val cosmosSerializationConfig = CosmosSerializationConfig.parseSerializationConfig(config)
  private val cosmosRowConverter = CosmosRowConverter.get(cosmosSerializationConfig)
  private val cosmosChangeFeedConfig = CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(config)


  override def currentMetricsValues(): Array[CustomTaskMetric] = {
    Array(
      changeFeedLSNRangeMetric,
      changeFeedItemsCntMetric,
      changeFeedPartitionIndexMetric
    )
  }

  private def changeFeedItemFactoryMethod(objectNode: ObjectNode): ChangeFeedSparkRowItem = {
    val pkValue = partitionKeyDefinition match {
      case Some(pkDef) => Some(PartitionKeyHelper.getPartitionKeyPath(objectNode, pkDef))
      case None => None
    }

    val row = cosmosRowConverter.fromObjectNodeToRow(readSchema,
      objectNode,
      readConfig.schemaConversionMode)

    ChangeFeedSparkRowItem(row, pkValue, objectNode.get(LsnPropertyName).asText())
  }

  private def changeFeedItemFactoryMethodV1(objectNode: ObjectNode): ChangeFeedSparkRowItem = {
    val pkValue = partitionKeyDefinition match {
      case Some(pkDef) => Some(PartitionKeyHelper.getPartitionKeyPath(objectNode, pkDef))
      case None => None
    }
    val row = cosmosRowConverter.fromObjectNodeToChangeFeedRowV1(readSchema,
      objectNode,
      readConfig.schemaConversionMode)
    ChangeFeedSparkRowItem(row, pkValue, cosmosRowConverter.getChangeFeedLsn(objectNode))
  }

  private val changeFeedItemDeserializer: CosmosItemSerializer = new CosmosItemSerializerNoExceptionWrapping {
    override def serialize[T](item: T): util.Map[String, AnyRef] = ???

    override def deserialize[T](jsonNodeMap: util.Map[String, AnyRef], classType: Class[T]): T = {
      if (jsonNodeMap == null) {
        throw new IllegalStateException("The 'jsonNodeMap' should never be null here.")
      }

      if (classType != classOf[ChangeFeedSparkRowItem]) {
        throw new IllegalStateException("The 'classType' must be 'classOf[ChangeFeedSparkRowItem])' here.")
      }

      val objectNode: ObjectNode = jsonNodeMap match {
        case map: ObjectNodeMap =>
          map.getObjectNode
        case _ =>
          Utils.getSimpleObjectMapper.convertValue(jsonNodeMap, classOf[ObjectNode])
      }

      changeFeedItemFactoryMethod(objectNode).asInstanceOf[T]
    }
  }

  private val changeFeedItemDeserializerV1: CosmosItemSerializer = new CosmosItemSerializerNoExceptionWrapping {
    override def serialize[T](item: T): util.Map[String, AnyRef] = ???

    override def deserialize[T](jsonNodeMap: util.Map[String, AnyRef], classType: Class[T]): T = {
      if (jsonNodeMap == null) {
        throw new IllegalStateException("The 'jsonNodeMap' should never be null here.")
      }

      if (classType != classOf[ChangeFeedSparkRowItem]) {
        throw new IllegalStateException("The 'classType' must be 'classOf[ChangeFeedSparkRowItem])' here.")
      }

      val objectNode: ObjectNode = jsonNodeMap match {
        case map: ObjectNodeMap =>
          map.getObjectNode
        case _ =>
          Utils.getSimpleObjectMapper.convertValue(jsonNodeMap, classOf[ObjectNode])
      }

      changeFeedItemFactoryMethodV1(objectNode).asInstanceOf[T]
    }
  }

  private def getPartitionStartLsn: Long = {
    if (partition.continuationState.isDefined) {
      SparkBridgeImplementationInternal.extractLsnFromChangeFeedContinuation(this.partition.continuationState.get)
    } else {
      0
    }
  }

  private val changeFeedRequestOptions = {

    val startLsn = getPartitionStartLsn
    log.logDebug(
      s"Request options for Range '${partition.feedRange.min}-${partition.feedRange.max}' LSN '$startLsn'")

    val options = CosmosChangeFeedRequestOptions
      .createForProcessingFromContinuation(this.partition.continuationState.get)
      .setMaxItemCount(readConfig.maxItemCount)
    ThroughputControlHelper.populateThroughputControlGroupName(options, readConfig.throughputControlConfig)

    val itemDeserializer: CosmosItemSerializer = cosmosChangeFeedConfig.changeFeedMode match {
      case ChangeFeedModes.Incremental | ChangeFeedModes.LatestVersion =>
        changeFeedItemDeserializer
      case ChangeFeedModes.FullFidelity | ChangeFeedModes.AllVersionsAndDeletes =>
        changeFeedItemDeserializerV1
    }
    if (this.partition.endLsn.isDefined) {
      ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.getCosmosChangeFeedRequestOptionsAccessor
        .setEndLSN(options, this.partition.endLsn.get)
    }

    options.setCustomItemSerializer(itemDeserializer)
  }

  private val rowSerializer: ExpressionEncoder.Serializer[Row] = RowSerializerPool.getOrCreateSerializer(readSchema)

  private var operationContextAndListenerTuple: Option[OperationContextAndListenerTuple] = None

  initializeDiagnosticsIfConfigured()

  private def initializeDiagnosticsIfConfigured(): Unit = {
    if (diagnosticsConfig.mode.isDefined) {
      val taskContext = TaskContext.get
      assert(taskContext != null)

      val taskDiagnosticsContext = SparkTaskContext(
        diagnosticsContext.correlationActivityId,
        taskContext.stageId(),
        taskContext.partitionId(),
        taskContext.taskAttemptId(),
        s"${partition.feedRange} ${diagnosticsContext.details}")

      val listener =
        DiagnosticsLoader.getDiagnosticsProvider(diagnosticsConfig).getLogger(this.getClass)

      operationContextAndListenerTuple =
        Some(new OperationContextAndListenerTuple(taskDiagnosticsContext, listener))

      ImplementationBridgeHelpers
        .CosmosChangeFeedRequestOptionsHelper
        .getCosmosChangeFeedRequestOptionsAccessor
        .setOperationContext(changeFeedRequestOptions, operationContextAndListenerTuple.get)
    }
  }

  private lazy val iterator: TransientIOErrorsRetryingIterator[ChangeFeedSparkRowItem] =
    new TransientIOErrorsRetryingIterator[ChangeFeedSparkRowItem](
      continuationToken => {
        if (!Strings.isNullOrWhiteSpace(continuationToken)) {
          ModelBridgeInternal.setChangeFeedRequestOptionsContinuation(continuationToken, changeFeedRequestOptions)
        } else {
          // scalastyle:off null
          ModelBridgeInternal.setChangeFeedRequestOptionsContinuation(null, changeFeedRequestOptions)
          // scalastyle:on null
        }
        cosmosAsyncContainer.queryChangeFeed(changeFeedRequestOptions, classOf[ChangeFeedSparkRowItem])
      },
      readConfig.maxItemCount,
      readConfig.prefetchBufferSize,
      operationContextAndListenerTuple,
      this.partition.endLsn
    )

  override def next(): Boolean = {
    this.iterator.hasNext
  }

  override def get(): InternalRow = {
    cosmosRowConverter.fromRowToInternalRow(this.iterator.next().row, rowSerializer)
  }

  override def close(): Unit = {
    this.iterator.close()
    RowSerializerPool.returnSerializerToPool(readSchema, rowSerializer)
    clientCacheItem.close()
    if (throughputControlClientCacheItemOpt.isDefined) {
      throughputControlClientCacheItemOpt.get.close()
    }
  }

  private def getChangeFeedItemsCnt: Long = {
    this.iterator.getTotalChangeFeedItemsCnt
  }

  private def getChangeFeedLSNRange: Long = {
    // calculate the changes per lsn
    val latestLsnOpt = this.iterator.getLatestContinuationToken match {
      case Some(continuationToken) =>
        // for cases where the feed range spans multiple physical partitions
        // pick the smallest lsn
        Some(SparkBridgeImplementationInternal
         .extractContinuationTokensFromChangeFeedStateJson(continuationToken)
         .minBy(_._2)._2)
      case None =>
        // for change feed, we would only reach here before the first page got fetched
        // fallback to use the continuation token from the partition instead
        Some(SparkBridgeImplementationInternal
         .extractContinuationTokensFromChangeFeedStateJson(partition.continuationState.get)
         .minBy(_._2)._2)
    }

    if (latestLsnOpt.isDefined) latestLsnOpt.get - startLsn else 0
  }
}
