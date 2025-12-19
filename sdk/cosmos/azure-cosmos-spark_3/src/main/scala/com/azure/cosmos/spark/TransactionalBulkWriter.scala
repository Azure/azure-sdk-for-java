// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

// scalastyle:off underscore.import
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils
import com.azure.cosmos.implementation.batch.{BatchRequestResponseConstants, BulkExecutorDiagnosticsTracker, ItemBulkOperation, TransactionalBulkExecutor}
import com.azure.cosmos.implementation.{CosmosDaemonThreadFactory, CosmosBulkExecutionOptionsImpl, ImplementationBridgeHelpers, UUIDs}
import com.azure.cosmos.models._
import com.azure.cosmos.spark.TransactionalBulkWriter.{BulkOperationFailedException, bulkWriterInputBoundedElastic, bulkWriterRequestsBoundedElastic, bulkWriterResponsesBoundedElastic, getThreadInfo}
import com.azure.cosmos.spark.diagnostics.DefaultDiagnostics
import com.azure.cosmos.{BridgeInternal, CosmosAsyncContainer, CosmosDiagnosticsContext, CosmosEndToEndOperationLatencyPolicyConfigBuilder, CosmosException}
import reactor.core.Scannable
import reactor.core.publisher.{Flux, Mono}
import reactor.core.scheduler.Scheduler

import java.util
import java.util.Objects
import java.util.concurrent.{ScheduledFuture, ScheduledThreadPoolExecutor}
import scala.collection.concurrent.TrieMap
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.collection.mutable
import scala.concurrent.duration.Duration
// scalastyle:on underscore.import
import com.azure.cosmos.implementation.ImplementationBridgeHelpers
import com.azure.cosmos.implementation.guava25.base.Preconditions
import com.azure.cosmos.implementation.spark.{OperationContextAndListenerTuple, OperationListener}
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.spark.TransactionalBulkWriter.{DefaultMaxPendingOperationPerCore, emitFailureHandler}
import com.azure.cosmos.spark.diagnostics.{DiagnosticsContext, DiagnosticsLoader, LoggerHelper, SparkTaskContext}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.TaskContext
import reactor.core.Disposable
import reactor.core.publisher.Sinks
import reactor.core.publisher.Sinks.{EmitFailureHandler, EmitResult}
import reactor.core.scala.publisher.SMono.PimpJFlux
import reactor.core.scala.publisher.{SFlux, SMono}
import reactor.core.scheduler.Schedulers

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger, AtomicLong, AtomicReference}
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.{Semaphore, TimeUnit}
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

//scalastyle:off null
//scalastyle:off multiple.string.literals
//scalastyle:off file.size.limit
private class TransactionalBulkWriter
(
  container: CosmosAsyncContainer,
  containerConfig: CosmosContainerConfig,
  partitionKeyDefinition: PartitionKeyDefinition,
  writeConfig: CosmosWriteConfig,
  diagnosticsConfig: DiagnosticsConfig,
  outputMetricsPublisher: OutputMetricsPublisherTrait,
  commitAttempt: Long = 1
) extends AsyncItemWriter {

  private val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)

  private val verboseLoggingAfterReEnqueueingRetriesEnabled = new AtomicBoolean(false)

  private val cpuCount = SparkUtils.getNumberOfHostCPUCores
  
  // NOTE: The public API config property is "maxPendingOperations" for backward compatibility,
  // but internally TransactionalBulkWriter limits concurrent *batches* not individual operations.
  // This is semantically correct because transactional batches are atomic units.
  // We convert maxPendingOperations to maxPendingBatches by assuming ~50 operations per batch.
  private val maxPendingBatches = {
    val maxOps = writeConfig.bulkMaxPendingOperations.getOrElse(DefaultMaxPendingOperationPerCore)
    // Assume average batch size of 50 operations - limit concurrent batches accordingly
    Math.max(1, maxOps / 50)
  }
  private val maxPendingOperations = writeConfig.bulkMaxPendingOperations
    .getOrElse(DefaultMaxPendingOperationPerCore)
  private val maxConcurrentPartitions = writeConfig.maxConcurrentCosmosPartitions match {
    // using the provided maximum of concurrent partitions per Spark partition on the input data
    // multiplied by 2 to leave space for partition splits during ingestion
    case Some(configuredMaxConcurrentPartitions) => 2 * configuredMaxConcurrentPartitions
    // using the total number of physical partitions
    // multiplied by 2 to leave space for partition splits during ingestion
    case None => 2 * ContainerFeedRangesCache.getFeedRanges(container, containerConfig.feedRangeRefreshIntervalInSecondsOpt).block().size
  }
  // Validate write strategy for transactional batches
  require(writeConfig.itemWriteStrategy == ItemWriteStrategy.ItemOverwrite,
    s"Transactional batches only support ItemOverwrite (upsert) write strategy. Requested: ${writeConfig.itemWriteStrategy}")

  log.logInfo(
    s"TransactionalBulkWriter instantiated (Host CPU count: $cpuCount, maxPendingBatches: $maxPendingBatches, " +
  s"maxPendingOperations: $maxPendingOperations, maxConcurrentPartitions: $maxConcurrentPartitions ...")


  private val closed = new AtomicBoolean(false)
  private val lock = new ReentrantLock
  private val pendingTasksCompleted = lock.newCondition
  private val pendingRetries = new AtomicLong(0)
  private val pendingBulkWriteRetries = java.util.concurrent.ConcurrentHashMap.newKeySet[CosmosItemOperation]().asScala
  private val activeTasks = new AtomicInteger(0)
  private val errorCaptureFirstException = new AtomicReference[Throwable]()
  private val bulkInputEmitter: Sinks.Many[CosmosBatch] = Sinks.many().unicast().onBackpressureBuffer()

  // Partition key buffering for batch construction
  // Buffer holds tuples of (ObjectNode, OperationContext, effectiveOperationType)
  private var currentPartitionKey: PartitionKey = null
  private val currentBatchOperations = new mutable.ListBuffer[(ObjectNode, OperationContext, String)]()
  private val batchConstructionLock = new Object()

  // Batch-level tracking - BatchTracker holds all information about a batch
  private case class BatchTracker(
    batchId: String,
    operations: List[CosmosItemOperation],
    contexts: List[OperationContext],
    sourceData: List[(ObjectNode, OperationContext, String)],
    attemptNumber: Int
  )
  
  // Map batch ID to BatchTracker
  private val activeBatches = new java.util.concurrent.ConcurrentHashMap[String, BatchTracker]().asScala
  // Map batch ID to collected responses for that batch
  private val batchResponseCollector = new java.util.concurrent.ConcurrentHashMap[String, java.util.concurrent.ConcurrentLinkedQueue[CosmosBulkOperationResponse[Object]]]().asScala
  // Map individual operation to its batch ID for response grouping
  private val operationToBatchId = new java.util.concurrent.ConcurrentHashMap[CosmosItemOperation, String]().asScala
  // Semaphore limits number of outstanding batches
  private val semaphore = new Semaphore(maxPendingBatches)
  private val activeBatchCount = new AtomicInteger(0)
  // Track batches being retried
  private val pendingBatchRetries = java.util.concurrent.ConcurrentHashMap.newKeySet[String]().asScala

  private val totalScheduledMetrics = new AtomicLong(0)
  private val totalSuccessfulIngestionMetrics = new AtomicLong(0)

  private val maxOperationTimeout = java.time.Duration.ofSeconds(CosmosConstants.batchOperationEndToEndTimeoutInSeconds)
  private val endToEndTimeoutPolicy = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(maxOperationTimeout)
    .enable(true)
    .build
  private val cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions(TransactionalBulkWriter.bulkProcessingThresholds)

  private val cosmosBulkExecutionOptionsImpl = ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper
    .getCosmosBulkExecutionOptionsAccessor
    .getImpl(cosmosBulkExecutionOptions)
  private val monotonicOperationCounter = new AtomicLong(0)

  cosmosBulkExecutionOptionsImpl.setSchedulerOverride(bulkWriterRequestsBoundedElastic)
  cosmosBulkExecutionOptionsImpl.setMaxConcurrentCosmosPartitions(maxConcurrentPartitions)
  cosmosBulkExecutionOptionsImpl.setCosmosEndToEndLatencyPolicyConfig(endToEndTimeoutPolicy)

  private class ForwardingMetricTracker(val verboseLoggingEnabled: AtomicBoolean) extends BulkExecutorDiagnosticsTracker {
    override def trackDiagnostics(ctx: CosmosDiagnosticsContext): Unit = {
      val ctxOption = Option.apply(ctx)
      outputMetricsPublisher.trackWriteOperation(0, ctxOption)
      if (ctxOption.isDefined && verboseLoggingEnabled.get) {
        TransactionalBulkWriter.log.logWarning(s"Track bulk operation after re-enqueued retry: ${ctxOption.get.toJson}")
      }
    }

    override def verboseLoggingAfterReEnqueueingRetriesEnabled(): Boolean = {
      verboseLoggingEnabled.get()
    }
  }

  cosmosBulkExecutionOptionsImpl.setDiagnosticsTracker(
      new ForwardingMetricTracker(verboseLoggingAfterReEnqueueingRetriesEnabled)
    )

  ThroughputControlHelper.populateThroughputControlGroupName(cosmosBulkExecutionOptions, writeConfig.throughputControlConfig)

  writeConfig.maxMicroBatchPayloadSizeInBytes match {
    case Some(customMaxMicroBatchPayloadSizeInBytes) =>
      cosmosBulkExecutionOptionsImpl
        .setMaxMicroBatchPayloadSizeInBytes(customMaxMicroBatchPayloadSizeInBytes)
    case None =>
  }

  writeConfig.initialMicroBatchSize match {
    case Some(customInitialMicroBatchSize) =>
      cosmosBulkExecutionOptions.setInitialMicroBatchSize(Math.max(1, customInitialMicroBatchSize))
    case None =>
  }

  writeConfig.maxMicroBatchSize match {
    case Some(customMaxMicroBatchSize) =>
     cosmosBulkExecutionOptions.setMaxMicroBatchSize(
       Math.max(
        1,
        Math.min(customMaxMicroBatchSize, BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST)
       )
     )
    case None =>
  }

  private val operationContext = initializeOperationContext()


  private def initializeOperationContext(): SparkTaskContext = {
    val taskContext = TaskContext.get

    val diagnosticsContext: DiagnosticsContext = DiagnosticsContext(UUIDs.nonBlockingRandomUUID(), "transactional-BulkWriter")

    if (taskContext != null) {
      val taskDiagnosticsContext = SparkTaskContext(diagnosticsContext.correlationActivityId,
        taskContext.stageId(),
        taskContext.partitionId(),
        taskContext.taskAttemptId(),
        "")

      val listener: OperationListener =
        DiagnosticsLoader.getDiagnosticsProvider(diagnosticsConfig).getLogger(this.getClass)

      val operationContextAndListenerTuple = new OperationContextAndListenerTuple(taskDiagnosticsContext, listener)
      cosmosBulkExecutionOptionsImpl
        .setOperationContextAndListenerTuple(operationContextAndListenerTuple)

      taskDiagnosticsContext
    } else{
      SparkTaskContext(diagnosticsContext.correlationActivityId,
        -1,
        -1,
        -1,
        "")
    }
  }




  private def processBatchResponses(
                                      batchId: String,
                                      batchTracker: BatchTracker,
                                      responseQueue: java.util.concurrent.ConcurrentLinkedQueue[CosmosBulkOperationResponse[Object]]): Unit = {
    
    var hasRetryableError = false
    var hasFatalError = false
    var fatalException: Throwable = null
    val successCount = new AtomicInteger(0)
    
    // Analyze all responses to make batch-level decision
    val responses = responseQueue.asScala.toList
    responses.zipWithIndex.foreach { case (resp, idx) =>
      val context = batchTracker.contexts(idx)
      val itemResponse = resp.getResponse
      
      if (resp.getException != null) {
        Option(resp.getException) match {
          case Some(cosmosException: CosmosException) =>
            val statusCode = cosmosException.getStatusCode
            val subStatusCode = cosmosException.getSubStatusCode
            
            if (shouldIgnore(statusCode, subStatusCode)) {
              successCount.incrementAndGet()
            } else if (shouldRetry(statusCode, subStatusCode, context)) {
              hasRetryableError = true
              log.logDebug(s"Batch $batchId has retryable error: $statusCode:$subStatusCode for item ${context.itemId}")
            } else {
              hasFatalError = true
              fatalException = cosmosException
              log.logError(s"Batch $batchId has fatal error: $statusCode:$subStatusCode for item ${context.itemId}")
            }
          case _ =>
            hasFatalError = true
            fatalException = resp.getException
            log.logError(s"Batch $batchId has unexpected exception for item ${context.itemId}", resp.getException)
        }
      } else if (Option(itemResponse).isEmpty || !itemResponse.isSuccessStatusCode) {
        val statusCode = Option(itemResponse).map(_.getStatusCode).getOrElse(CosmosConstants.StatusCodes.Timeout)
        val subStatusCode = Option(itemResponse).map(_.getSubStatusCode).getOrElse(0)
        
        if (shouldIgnore(statusCode, subStatusCode)) {
          successCount.incrementAndGet()
        } else if (shouldRetry(statusCode, subStatusCode, context)) {
          hasRetryableError = true
          log.logDebug(s"Batch $batchId has retryable error: $statusCode:$subStatusCode for item ${context.itemId}")
        } else {
          hasFatalError = true
          fatalException = new BulkOperationFailedException(statusCode, subStatusCode,
            s"Fatal error in batch $batchId for item ${context.itemId}", null)
          log.logError(s"Batch $batchId has fatal error: $statusCode:$subStatusCode for item ${context.itemId}")
        }
      } else {
        // Success
        successCount.incrementAndGet()
      }
    }
    
    // Make batch-level decision
    if (hasFatalError) {
      // Fatal error - fail entire batch
      log.logError(s"Batch $batchId failed with fatal error, cannot retry")
      captureIfFirstFailure(fatalException)
      cancelWork()
      cleanupBatch(batchId, batchTracker.operations.size)
    } else if (hasRetryableError) {
      // Retry entire batch atomically
      log.logWarning(s"Batch $batchId has retryable errors, will retry entire batch atomically")
      scheduleBatchRetry(batchId, batchTracker)
      cleanupBatch(batchId, batchTracker.operations.size)
    } else {
      // All operations succeeded
      log.logDebug(s"Batch $batchId completed successfully with ${successCount.get} operations")
      outputMetricsPublisher.trackWriteOperation(successCount.get, None)
      totalSuccessfulIngestionMetrics.addAndGet(successCount.get)
      cleanupBatch(batchId, batchTracker.operations.size)
    }
  }
  
  private def cleanupBatch(batchId: String, operationCount: Int): Unit = {
    // Clean up batch tracking
    activeBatches.remove(batchId)
    batchResponseCollector.remove(batchId)
    pendingBatchRetries.remove(batchId)
    
    // Release semaphore
    activeBatchCount.decrementAndGet()
    semaphore.release()
    log.logTrace(s"Released semaphore for batch $batchId, activeBatchCount: ${activeBatchCount.get} $getThreadInfo")
    
    // Mark task completion for all operations in batch
    (1 to operationCount).foreach(_ => markTaskCompletion())
  }
  
  private def scheduleBatchRetry(batchId: String, batchTracker: BatchTracker): Unit = {
    this.pendingRetries.incrementAndGet()
    pendingBatchRetries.add(batchId)
    
    // Determine if this is a timeout to apply delay
    val hasTimeout = batchResponseCollector.get(batchId).exists { queue =>
      queue.asScala.exists { resp =>
        val statusCode = Option(resp.getResponse).map(_.getStatusCode)
          .orElse(Option(resp.getException).collect { case ce: CosmosException => ce.getStatusCode })
          .getOrElse(CosmosConstants.StatusCodes.Timeout)
        Exceptions.isTimeout(statusCode)
      }
    }
    
    val deferredRetryMono = SMono.defer(() => {
      log.logWarning(s"Retrying batch $batchId with ${batchTracker.operations.size} operations, " +
        s"attempt ${batchTracker.attemptNumber + 1}")
      
      retryBatch(batchTracker)
      this.pendingRetries.decrementAndGet()
      SMono.empty
    })
    
    if (hasTimeout) {
      deferredRetryMono
        .delaySubscription(
          Duration(
            TransactionalBulkWriter.minDelayOn408RequestTimeoutInMs +
              scala.util.Random.nextInt(
                TransactionalBulkWriter.maxDelayOn408RequestTimeoutInMs - TransactionalBulkWriter.minDelayOn408RequestTimeoutInMs),
            TimeUnit.MILLISECONDS),
          Schedulers.boundedElastic())
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe()
    } else {
      deferredRetryMono
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe()
    }
  }
  
  private def retryBatch(batchTracker: BatchTracker): Unit = {
    // Acquire semaphore for retry
    val activeTasksSemaphoreTimeout = 10
    val numberOfIntervalsWithIdenticalActiveOperationSnapshots = new AtomicLong(0)
    var activeBatchesSnapshot = mutable.Set.empty[String]
    var pendingBatchRetriesSnapshot = mutable.Set.empty[String]
    
    while (!semaphore.tryAcquire(activeTasksSemaphoreTimeout, TimeUnit.MINUTES)) {
      if (subscriptionDisposable.isDisposed) {
        captureIfFirstFailure(
          new IllegalStateException("Can't accept any new work - BulkWriter has been disposed already"))
      }
      
      throwIfProgressStaled(
        "Batch retry semaphore acquisition",
        activeBatchesSnapshot.flatMap(id => activeBatches.get(id).map(_.operations).getOrElse(List.empty)).to[mutable.Set],
        pendingBatchRetriesSnapshot.flatMap(id => activeBatches.get(id).map(_.operations).getOrElse(List.empty)).to[mutable.Set],
        numberOfIntervalsWithIdenticalActiveOperationSnapshots,
        allowRetryOnNewBulkWriterInstance = false)

      activeBatchesSnapshot = activeBatches.keySet.to[mutable.Set]
      pendingBatchRetriesSnapshot = pendingBatchRetries.to[mutable.Set]
    }
    
    activeBatchCount.incrementAndGet()
    
    // Generate new batch ID for retry
    val newBatchId = java.util.UUID.randomUUID().toString
    
    // Recreate batch with incremented attempt numbers
    val partitionKey = batchTracker.operations.head.getPartitionKeyValue
    val batch = CosmosBatch.createCosmosBatch(partitionKey)
    val updatedContexts = new scala.collection.mutable.ListBuffer[OperationContext]()
    
    batchTracker.sourceData.foreach { case (objectNode, context, _) =>
      batch.upsertItemOperation(objectNode)
      val updatedContext = new OperationContext(
        context.itemId,
        context.partitionKeyValue,
        context.eTag,
        batchTracker.attemptNumber + 1,
        context.sequenceNumber,
        context.sourceItem
      )
      updatedContexts += updatedContext
    }
    
    // Create new BatchTracker for retry
    val batchOperations = batch.getOperations()
    val operationsList = batchOperations.asScala.toList
    val newBatchTracker = BatchTracker(
      batchId = newBatchId,
      operations = operationsList,
      contexts = updatedContexts.toList,
      sourceData = batchTracker.sourceData,
      attemptNumber = batchTracker.attemptNumber + 1
    )
    
    // Register new batch
    activeBatches.put(newBatchId, newBatchTracker)
    batchResponseCollector.put(newBatchId, new java.util.concurrent.ConcurrentLinkedQueue[CosmosBulkOperationResponse[Object]]())
    operationsList.foreach(op => operationToBatchId.put(op, newBatchId))
    
    log.logDebug(s"Created retry batch $newBatchId (original: ${batchTracker.batchId}) with ${operationsList.size} operations")
    
    // Emit retry batch
    bulkInputEmitter.emitNext(batch, emitFailureHandler)
  }

  private def scheduleRetry(
                               trackPendingRetryAction: () => Boolean,
                               clearPendingRetryAction: () => Boolean,
                               partitionKey: PartitionKey,
                               objectNode: ObjectNode,
                               operationContext: OperationContext,
                               statusCode: Int): Unit = {
      if (trackPendingRetryAction()) {
        this.pendingRetries.incrementAndGet()
      }
      // this is to ensure the submission will happen on a different thread in background
      // and doesn't block the active thread
      val deferredRetryMono = SMono.defer(() => {
          scheduleWriteInternal(
              partitionKey,
              objectNode,
              new OperationContext(
                  operationContext.itemId,
                  operationContext.partitionKeyValue,
                  operationContext.eTag,
                  operationContext.attemptNumber + 1,
                  operationContext.sequenceNumber))
          if (clearPendingRetryAction()) {
            this.pendingRetries.decrementAndGet()
          }
          SMono.empty
      })

      if (Exceptions.isTimeout(statusCode)) {
          deferredRetryMono
              .delaySubscription(
                  Duration(
                      TransactionalBulkWriter.minDelayOn408RequestTimeoutInMs +
                          scala.util.Random.nextInt(
                              TransactionalBulkWriter.maxDelayOn408RequestTimeoutInMs - TransactionalBulkWriter.minDelayOn408RequestTimeoutInMs),
                      TimeUnit.MILLISECONDS),
                  Schedulers.boundedElastic())
              .subscribeOn(Schedulers.boundedElastic())
              .subscribe()

      } else {
          deferredRetryMono
              .subscribeOn(Schedulers.boundedElastic())
              .subscribe()
      }
  }

  private val subscriptionDisposable: Disposable = {
    log.logTrace(s"subscriptionDisposable, Context: ${operationContext.toString} $getThreadInfo")

    val inputFlux = bulkInputEmitter
      .asFlux()
      .onBackpressureBuffer()
      .publishOn(bulkWriterInputBoundedElastic)
      .doOnError(t => {
        log.logError(s"Input publishing flux failed, Context: ${operationContext.toString} $getThreadInfo", t)
      })

    // Use TransactionalBulkExecutor which internally uses SinglePartitionKeyServerBatchRequest
    // with setAtomicBatch(true) and setShouldContinueOnError(false) for true transactional semantics
    val cosmosBulkExecutionOptionsImpl = ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper
      .getCosmosBulkExecutionOptionsAccessor
      .getImpl(cosmosBulkExecutionOptions)
    
    val transactionalExecutor = new TransactionalBulkExecutor[Object](
      container,
      inputFlux,
      cosmosBulkExecutionOptionsImpl)

    val bulkOperationResponseFlux: SFlux[CosmosBulkOperationResponse[Object]] =
      transactionalExecutor
          .execute()
          .onBackpressureBuffer()
          .publishOn(bulkWriterResponsesBoundedElastic)
          .doOnError(t => {
            log.logError(s"Transactional bulk execution flux failed, Context: ${operationContext.toString} $getThreadInfo", t)
          })
          .asScala

    bulkOperationResponseFlux.subscribe(
      resp => {
        try {
          val itemOperation = resp.getOperation
          
          // Find which batch this operation belongs to
          val batchIdOption = operationToBatchId.get(itemOperation)
          
          if (batchIdOption.isEmpty) {
            logInfoOrWarning(s"Cannot find batch ID for operation '${itemOperation.getOperationType} " +
              s"${itemOperation.getPartitionKeyValue}/${itemOperation.getId}'. This can happen when " +
              s"retries get re-enqueued.")
          } else {
            val batchId = batchIdOption.get
            
            // Add response to batch collector
            batchResponseCollector.get(batchId) match {
              case Some(responseQueue) =>
                responseQueue.add(resp)
                
                // Check if we've received all responses for this batch
                val batchTrackerOption = activeBatches.get(batchId)
                if (batchTrackerOption.isDefined) {
                  val batchTracker = batchTrackerOption.get
                  val expectedCount = batchTracker.operations.size
                  val receivedCount = responseQueue.size()
                  
                  if (receivedCount == expectedCount) {
                    // All responses received - process batch
                    processBatchResponses(batchId, batchTracker, responseQueue)
                  }
                }
              case None =>
                log.logWarning(s"Response collector not found for batch $batchId")
            }
          }
        }
        catch {
          case ex: Exception =>
            log.logError(s"Error processing bulk operation response, Context: ${operationContext.toString} $getThreadInfo", ex)
            captureIfFirstFailure(ex)
            cancelWork()
        }
      },
      errorConsumer = Option.apply(
        ex => {
          log.logError(s"Unexpected failure code path in Bulk ingestion, " +
            s"Context: ${operationContext.toString} $getThreadInfo", ex)
          // if there is any failure this closes the bulk.
          // at this point bulk api doesn't allow any retrying
          // we don't know the list of failed item-operations
          // they only way to retry to keep a dictionary of pending operations outside
          // so we know which operations failed and which ones can be retried.
          // this is currently a kill scenario.
          captureIfFirstFailure(ex)
          cancelWork()
          markTaskCompletion()
        }
      )
    )
  }

  override def scheduleWrite(partitionKeyValue: PartitionKey, objectNode: ObjectNode): Unit = {
    Preconditions.checkState(!closed.get())
    throwIfCapturedExceptionExists()

    val operationContext = new OperationContext(
      getId(objectNode),
      partitionKeyValue,
      getETag(objectNode),
      1,
      monotonicOperationCounter.incrementAndGet(),
      None)

    val cnt = totalScheduledMetrics.getAndIncrement()
    log.logTrace(s"total scheduled $cnt, Context: ${operationContext.toString} $getThreadInfo")

    scheduleWriteInternal(partitionKeyValue, objectNode, operationContext)
  }

  private def scheduleWriteInternal(partitionKeyValue: PartitionKey,
                                    objectNode: ObjectNode,
                                    operationContext: OperationContext): Unit = {
      activeTasks.incrementAndGet()
      if (operationContext.attemptNumber > 1) {
        logInfoOrWarning(s"TransactionalBulkWriter scheduleWrite attemptCnt: ${operationContext.attemptNumber}, " +
              s"Context: ${operationContext.toString} $getThreadInfo")
      }

      scheduleBulkWriteInternal(partitionKeyValue, objectNode, operationContext)
  }

  private def scheduleBulkWriteInternal(partitionKeyValue: PartitionKey,
                                        objectNode: ObjectNode,
                                        operationContext: OperationContext): Unit = {

    // For transactional batches, only upsert is supported
    // This is validated in the constructor
    val effectiveOperationType = "upsert"

    // Buffer operation for batch construction
    this.addOperationToBatch(partitionKeyValue, objectNode, operationContext, effectiveOperationType)
  }

  private[this] def addOperationToBatch(partitionKey: PartitionKey,
                                        objectNode: ObjectNode,
                                        operationContext: OperationContext,
                                        effectiveOperationType: String): Unit = {

    batchConstructionLock.synchronized {
      // Check if partition key changed - flush existing batch if needed
      if (currentPartitionKey != null && !currentPartitionKey.equals(partitionKey)) {
        flushCurrentBatch()
      }

      // Initialize partition key if this is the first operation
      if (currentPartitionKey == null) {
        currentPartitionKey = partitionKey
      }

      // Add operation data to buffer
      currentBatchOperations += ((objectNode, operationContext, effectiveOperationType))

      // Note: We don't auto-flush at 100 operations in transactional mode because:
      // 1. Auto-splitting would break transactional atomicity
      // 2. The service will reject batches with >100 operations with a clear error
      // 3. Users should ensure their transactional batches respect the 100 operation limit
    }
  }

  private[this] def flushCurrentBatch(): Unit = {
    // Must be called within batchConstructionLock.synchronized
    if (currentBatchOperations.nonEmpty && currentPartitionKey != null) {
      // Acquire semaphore before emitting batch - this limits concurrent batches
      val activeTasksSemaphoreTimeout = 10
      val numberOfIntervalsWithIdenticalActiveOperationSnapshots = new AtomicLong(0)
      var activeBatchesSnapshot = mutable.Set.empty[String]
      var pendingBatchRetriesSnapshot = mutable.Set.empty[String]
      
      log.logTrace(s"Before acquiring semaphore for batch emission, activeBatchCount: ${activeBatchCount.get} $getThreadInfo")
      while (!semaphore.tryAcquire(activeTasksSemaphoreTimeout, TimeUnit.MINUTES)) {
        log.logDebug(s"Not able to acquire semaphore for batch, activeBatchCount: ${activeBatchCount.get} $getThreadInfo")
        if (subscriptionDisposable.isDisposed) {
          captureIfFirstFailure(
            new IllegalStateException("Can't accept any new work - BulkWriter has been disposed already"))
        }

        throwIfProgressStaled(
          "Batch semaphore acquisition",
          activeBatchesSnapshot.flatMap(batchId => activeBatches.get(batchId).map(_.operations).getOrElse(List.empty)).to[mutable.Set],
          pendingBatchRetriesSnapshot.flatMap(batchId => activeBatches.get(batchId).map(_.operations).getOrElse(List.empty)).to[mutable.Set],
          numberOfIntervalsWithIdenticalActiveOperationSnapshots,
          allowRetryOnNewBulkWriterInstance = false)

        activeBatchesSnapshot = activeBatches.keySet.to[mutable.Set]
        pendingBatchRetriesSnapshot = pendingBatchRetries.to[mutable.Set]
      }
      
      activeBatchCount.incrementAndGet()
      log.logTrace(s"Acquired semaphore for batch emission, activeBatchCount: ${activeBatchCount.get} $getThreadInfo")
      
      // Generate unique batch ID
      val batchId = java.util.UUID.randomUUID().toString
      
      // Build the batch using the builder API
      val batch = CosmosBatch.createCosmosBatch(currentPartitionKey)

      // Build the batch and keep track of context for each operation
      // For transactional batches, all operations are upserts
      val contextList = new scala.collection.mutable.ListBuffer[OperationContext]()

      currentBatchOperations.foreach { case (objectNode, operationContext, _) =>
        batch.upsertItemOperation(objectNode)
        contextList += operationContext
      }

      // After building the batch, get the operations and create BatchTracker
      val batchOperations = batch.getOperations()
      val operationsList = batchOperations.asScala.toList
      
      // Create BatchTracker with all batch information
      val batchTracker = BatchTracker(
        batchId = batchId,
        operations = operationsList,
        contexts = contextList.toList,
        sourceData = currentBatchOperations.toList,
        attemptNumber = 1
      )
      
      // Register batch and map operations to batch ID
      activeBatches.put(batchId, batchTracker)
      batchResponseCollector.put(batchId, new java.util.concurrent.ConcurrentLinkedQueue[CosmosBulkOperationResponse[Object]]())
      operationsList.foreach(op => operationToBatchId.put(op, batchId))
      
      log.logDebug(s"Created batch $batchId with ${operationsList.size} operations")

      // Emit the batch
      bulkInputEmitter.emitNext(batch, emitFailureHandler)

      // Clear the buffer
      currentBatchOperations.clear()
      currentPartitionKey = null
    }
  }

  private[this] def finalFlushBatch(): Unit = {
    batchConstructionLock.synchronized {
      flushCurrentBatch()
    }
  }

  //scalastyle:off method.length
  //scalastyle:off cyclomatic.complexity
  private[this] def handleNonSuccessfulStatusCode
  (
    context: OperationContext,
    itemOperation: CosmosItemOperation,
    itemResponse: CosmosBulkItemResponse,
    isGettingRetried: AtomicBoolean,
    responseException: Option[CosmosException]
  ) : Unit = {

    val exceptionMessage = responseException match {
      case Some(e) => e.getMessage
      case None => ""
    }

    val effectiveStatusCode = Option(itemResponse) match {
      case Some(r) => r.getStatusCode
      case None => responseException match {
        case Some(e) => e.getStatusCode
        case None => CosmosConstants.StatusCodes.Timeout
      }
    }

    val effectiveSubStatusCode = Option(itemResponse) match {
      case Some(r) => r.getSubStatusCode
      case None => responseException match {
        case Some(e) => e.getSubStatusCode
        case None => 0
      }
    }

    log.logDebug(s"encountered item operation response with status code " +
      s"$effectiveStatusCode:$effectiveSubStatusCode, " +
      s"Context: ${operationContext.toString} $getThreadInfo")
    if (shouldIgnore(effectiveStatusCode, effectiveSubStatusCode)) {
      log.logDebug(s"for itemId=[${context.itemId}], partitionKeyValue=[${context.partitionKeyValue}], " +
        s"ignored encountered status code '$effectiveStatusCode:$effectiveSubStatusCode', " +
        s"Context: ${operationContext.toString}")
      totalSuccessfulIngestionMetrics.getAndIncrement()
      // work done
    } else if (shouldRetry(effectiveStatusCode, effectiveSubStatusCode, context)) {
      // requeue
      log.logWarning(s"for itemId=[${context.itemId}], partitionKeyValue=[${context.partitionKeyValue}], " +
        s"encountered status code '$effectiveStatusCode:$effectiveSubStatusCode', will retry! " +
        s"attemptNumber=${context.attemptNumber}, exceptionMessage=$exceptionMessage,  " +
        s"Context: {${operationContext.toString}} $getThreadInfo")

      // During retry, use the original objectNode to ensure consistency
      val sourceItem = itemOperation match {
          case _: ItemBulkOperation[ObjectNode, OperationContext] =>
              context.sourceItem match {
                  case Some(bulkOperationSourceItem) => bulkOperationSourceItem
                  case None => itemOperation.getItem.asInstanceOf[ObjectNode]
              }
          case _ => itemOperation.getItem.asInstanceOf[ObjectNode]
      }

      this.scheduleRetry(
        trackPendingRetryAction = () => pendingBulkWriteRetries.add(itemOperation),
        clearPendingRetryAction = () => pendingBulkWriteRetries.remove(itemOperation),
        itemOperation.getPartitionKeyValue,
        sourceItem,
        context,
        effectiveStatusCode)
      isGettingRetried.set(true)
    } else {
      log.logError(s"for itemId=[${context.itemId}], partitionKeyValue=[${context.partitionKeyValue}], " +
        s"encountered status code '$effectiveStatusCode:$effectiveSubStatusCode', all retries exhausted! " +
        s"attemptNumber=${context.attemptNumber}, exceptionMessage=$exceptionMessage, " +
        s"Context: {${operationContext.toString} $getThreadInfo")

      val message = s"All retries exhausted for '${itemOperation.getOperationType}' bulk operation - " +
        s"statusCode=[$effectiveStatusCode:$effectiveSubStatusCode] " +
        s"itemId=[${context.itemId}], partitionKeyValue=[${context.partitionKeyValue}]"

      val exceptionToBeThrown = responseException match {
        case Some(e) =>
          new BulkOperationFailedException(effectiveStatusCode, effectiveSubStatusCode, message, e)
        case None =>
          new BulkOperationFailedException(effectiveStatusCode, effectiveSubStatusCode, message, null)
      }

      captureIfFirstFailure(exceptionToBeThrown)
      cancelWork()
    }
  }
  //scalastyle:on method.length
  //scalastyle:on cyclomatic.complexity

  private[this] def throwIfCapturedExceptionExists(): Unit = {
    val errorSnapshot = errorCaptureFirstException.get()
    if (errorSnapshot != null) {
      log.logError(s"throw captured error ${errorSnapshot.getMessage}, " +
        s"Context: ${operationContext.toString} $getThreadInfo")
      throw errorSnapshot
    }
  }

  private[this] def getActiveOperationsLog(
                                              activeOperationsSnapshot: mutable.Set[CosmosItemOperation]): String = {
    val sb = new StringBuilder()

    activeOperationsSnapshot
      .take(TransactionalBulkWriter.maxItemOperationsToShowInErrorMessage)
      .foreach(itemOperation => {
        if (sb.nonEmpty) {
          sb.append(", ")
        }

        sb.append(itemOperation.getOperationType)
        sb.append("->")
        val ctx = itemOperation.getContext[OperationContext]
        sb.append(s"${ctx.partitionKeyValue}/${ctx.itemId}/${ctx.eTag}(${ctx.attemptNumber})")
      })

    sb.toString()
  }

  private[this] def sameBulkWriteOperations
  (
    snapshot: mutable.Set[CosmosItemOperation],
    current: mutable.Set[CosmosItemOperation]
  ): Boolean = {

    if (snapshot.size != current.size) {
      false
    } else {
      snapshot.forall(snapshotOperation => {
        current.exists(
          currentOperation => snapshotOperation.getOperationType == currentOperation.getOperationType
          && snapshotOperation.getPartitionKeyValue == currentOperation.getPartitionKeyValue
          && Objects.equals(snapshotOperation.getId, currentOperation.getId)
          && Objects.equals(snapshotOperation.getItem[ObjectNode], currentOperation.getItem[ObjectNode])
        )
      })
    }
  }

  private[this] def throwIfProgressStaled
  (
    operationName: String,
    activeOperationsSnapshot: mutable.Set[CosmosItemOperation],
    pendingRetriesSnapshot: mutable.Set[CosmosItemOperation],
    numberOfIntervalsWithIdenticalActiveOperationSnapshots: AtomicLong,
    allowRetryOnNewBulkWriterInstance: Boolean
  ): Unit = {

    val operationsLog = getActiveOperationsLog(activeOperationsSnapshot)

    val currentActiveOperations = activeBatches.values.flatMap(_.operations).to[mutable.Set]
    val currentPendingRetries = pendingBatchRetries.flatMap(batchId => activeBatches.get(batchId).map(_.operations).getOrElse(List.empty)).to[mutable.Set]
    if (sameBulkWriteOperations(pendingRetriesSnapshot ++ activeOperationsSnapshot, currentActiveOperations ++ currentPendingRetries)) {

      numberOfIntervalsWithIdenticalActiveOperationSnapshots.incrementAndGet()
      log.logWarning(
        s"$operationName has been waiting $numberOfIntervalsWithIdenticalActiveOperationSnapshots " +
          s"times for identical set of operations: $operationsLog " +
          s"Context: ${operationContext.toString} $getThreadInfo"
      )
    } else {
      numberOfIntervalsWithIdenticalActiveOperationSnapshots.set(0)
      logInfoOrWarning(
        s"$operationName is waiting for active bulkWrite operations: $operationsLog " +
          s"Context: ${operationContext.toString} $getThreadInfo"
      )
    }

    val secondsWithoutProgress = numberOfIntervalsWithIdenticalActiveOperationSnapshots.get *
      writeConfig.flushCloseIntervalInSeconds
    val maxNoProgressIntervalInSeconds = if (commitAttempt == 1 && allowRetryOnNewBulkWriterInstance) {
      writeConfig.maxInitialNoProgressIntervalInSeconds
    } else {
      writeConfig.maxRetryNoProgressIntervalInSeconds
    }
    val maxAllowedIntervalWithoutAnyProgressExceeded = secondsWithoutProgress >= maxNoProgressIntervalInSeconds

    if (maxAllowedIntervalWithoutAnyProgressExceeded) {

      val retriableRemainingOperations = if (allowRetryOnNewBulkWriterInstance) {
        Some(
          (pendingRetriesSnapshot ++ activeOperationsSnapshot)
            .toList
            .sortBy(op => op.getContext[OperationContext].sequenceNumber)
        )
      } else {
        None
      }

      val exception = new BulkWriterNoProgressException(
        s"Stale bulk ingestion identified in $operationName - the following active operations have not been " +
          s"completed (first ${TransactionalBulkWriter.maxItemOperationsToShowInErrorMessage} shown) or progressed after " +
          s"$maxNoProgressIntervalInSeconds seconds: $operationsLog",
        commitAttempt,
        retriableRemainingOperations)

      captureIfFirstFailure(exception)

      cancelWork()
    }

    throwIfCapturedExceptionExists()
  }

  // the caller has to ensure that after invoking this method scheduleWrite doesn't get invoked
  // scalastyle:off method.length
  // scalastyle:off cyclomatic.complexity
  override def flushAndClose(): Unit = {
    this.synchronized {
      try {
        if (!closed.get()) {
          // Flush any remaining batched operations before closing
          finalFlushBatch()
          
          log.logInfo(s"flushAndClose invoked $getThreadInfo")
          val totalActiveOperations = activeBatches.values.map(_.operations.size).sum
          log.logInfo(s"completed so far ${totalSuccessfulIngestionMetrics.get()}, " +
            s"pending bulkWrite tasks (operations across ${activeBatches.size} batches): $totalActiveOperations $getThreadInfo")

          // error handling, if there is any error and the subscription is cancelled
          // the remaining tasks will not be processed hence we never reach activeTasks = 0
          // once we do error handling we should think how to cover the scenario.
          lock.lock()
          try {
            val numberOfIntervalsWithIdenticalActiveOperationSnapshots = new AtomicLong(0)
            var activeTasksSnapshot = activeTasks.get()
            var pendingRetriesSnapshot = pendingRetries.get()
            while ((pendingRetriesSnapshot > 0 || activeTasksSnapshot > 0)
              && errorCaptureFirstException.get == null) {

              logInfoOrWarning(
                s"Waiting for pending activeTasks $activeTasksSnapshot and/or pendingRetries " +
                  s"$pendingRetriesSnapshot,  Context: ${operationContext.toString} $getThreadInfo")
              val activeOperationsSnapshot = activeBatches.values.flatMap(_.operations).to[mutable.Set]
              val pendingOperationsSnapshot = pendingBatchRetries.flatMap(batchId => activeBatches.get(batchId).map(_.operations).getOrElse(List.empty)).to[mutable.Set]
              val awaitCompleted = pendingTasksCompleted.await(writeConfig.flushCloseIntervalInSeconds, TimeUnit.SECONDS)
              if (!awaitCompleted) {
                throwIfProgressStaled(
                  "FlushAndClose",
                  activeOperationsSnapshot,
                  pendingOperationsSnapshot,
                  numberOfIntervalsWithIdenticalActiveOperationSnapshots,
                  allowRetryOnNewBulkWriterInstance = true
                )

                if (numberOfIntervalsWithIdenticalActiveOperationSnapshots.get > 0L) {


                  val buffered = Scannable.from(bulkInputEmitter).scan(Scannable.Attr.BUFFERED)

                  if (verboseLoggingAfterReEnqueueingRetriesEnabled.compareAndSet(false, true)) {
                    log.logWarning(s"Starting to re-enqueue retries. Enabling verbose logs. "
                      + s"Number of intervals with identical pending operations: "
                      + s"$numberOfIntervalsWithIdenticalActiveOperationSnapshots Active Bulk Operations: "
                      + s"$activeOperationsSnapshot,  "
                      + s"PendingRetries: $pendingRetriesSnapshot, Buffered tasks: $buffered "
                      + s"Attempt: ${numberOfIntervalsWithIdenticalActiveOperationSnapshots.get} - "
                      + s"Context: ${operationContext.toString} $getThreadInfo")
                  } else if ((numberOfIntervalsWithIdenticalActiveOperationSnapshots.get % 3) == 0) {
                    log.logWarning(s"Reattempting to re-enqueue retries. Enabling verbose logs. "
                      + s"Number of intervals with identical pending operations: "
                      + s"$numberOfIntervalsWithIdenticalActiveOperationSnapshots Active Bulk Operations: "
                      + s"$activeOperationsSnapshot,  "
                      + s"PendingRetries: $pendingRetriesSnapshot, Buffered tasks: $buffered "
                      + s"Attempt: ${numberOfIntervalsWithIdenticalActiveOperationSnapshots.get} - "
                      + s"Context: ${operationContext.toString} $getThreadInfo")
                  }

                  activeOperationsSnapshot.foreach(operation => {
                    // Note: In transactional batch mode, we don't retry individual operations
                    // because batches are constructed and executed atomically. The executor
                    // handles retries at the batch level. Individual operation retry would require
                    // reconstructing batches, which could lead to incorrect retry behavior.
                    // Failed batches will be retried by Spark's task retry mechanism.
                    log.logTrace(s"Skipping individual operation retry in transactional batch mode - "
                      + s"Context: ${operationContext.toString} $getThreadInfo")
                  })
                }
              }

              activeTasksSnapshot = activeTasks.get()
              pendingRetriesSnapshot = pendingRetries.get()
              val semaphoreAvailablePermitsSnapshot = semaphore.availablePermits()

              if (awaitCompleted) {
                logInfoOrWarning(s"Waiting completed for pending activeTasks $activeTasksSnapshot, pendingRetries " +
                  s"$pendingRetriesSnapshot Context: ${operationContext.toString} $getThreadInfo")
              } else {
                logInfoOrWarning(s"Waiting interrupted for pending activeTasks $activeTasksSnapshot , pendingRetries " +
                  s"$pendingRetriesSnapshot - available permits $semaphoreAvailablePermitsSnapshot, " +
                  s"Context: ${operationContext.toString} $getThreadInfo")
              }
            }

            logInfoOrWarning(s"Waiting completed for pending activeTasks $activeTasksSnapshot, pendingRetries " +
              s"$pendingRetriesSnapshot Context: ${operationContext.toString} $getThreadInfo")
          } finally {
            lock.unlock()
          }

          logInfoOrWarning(s"invoking bulkInputEmitter.onComplete(), Context: ${operationContext.toString} $getThreadInfo")
          // Release any remaining batch permits
          val remainingBatches = activeBatchCount.get()
          if (remainingBatches > 0) {
            semaphore.release(remainingBatches)
            log.logDebug(s"Released $remainingBatches batch permits during cleanup")
          }
          bulkInputEmitter.emitComplete(TransactionalBulkWriter.emitFailureHandlerForComplete)

          throwIfCapturedExceptionExists()

          assume(activeTasks.get() <= 0)

          assume(activeBatches.isEmpty)
          assume(semaphore.availablePermits() >= maxPendingBatches)

          if (totalScheduledMetrics.get() != totalSuccessfulIngestionMetrics.get) {
            log.logWarning(s"flushAndClose completed with no error but inconsistent total success and " +
              s"scheduled metrics. This indicates that successful completion was only possible after re-enqueueing " +
              s"retries. totalSuccessfulIngestionMetrics=${totalSuccessfulIngestionMetrics.get()}, " +
              s"totalScheduled=$totalScheduledMetrics, Context: ${operationContext.toString} $getThreadInfo")
          } else {
            logInfoOrWarning(s"flushAndClose completed with no error. " +
              s"totalSuccessfulIngestionMetrics=${totalSuccessfulIngestionMetrics.get()}, " +
              s"totalScheduled=$totalScheduledMetrics, Context: ${operationContext.toString} $getThreadInfo")
          }
        }
      } finally {
        subscriptionDisposable.dispose()
        closed.set(true)
      }
    }
  }
  // scalastyle:on method.length
  // scalastyle:on cyclomatic.complexity

  private def logInfoOrWarning(msg: => String): Unit = {
    if (this.verboseLoggingAfterReEnqueueingRetriesEnabled.get()) {
      log.logWarning(msg)
    } else {
      log.logInfo(msg)
    }
  }

  private def markTaskCompletion(): Unit = {
    lock.lock()
    try {
      val activeTasksLeftSnapshot = activeTasks.decrementAndGet()
      val exceptionSnapshot = errorCaptureFirstException.get()
      log.logTrace(s"markTaskCompletion, Active tasks left: $activeTasksLeftSnapshot, " +
        s"error: $exceptionSnapshot, Context: ${operationContext.toString} $getThreadInfo")
      if (activeTasksLeftSnapshot == 0 || exceptionSnapshot != null) {
        pendingTasksCompleted.signal()
      }
    } finally {
      lock.unlock()
    }
  }

  private def captureIfFirstFailure(throwable: Throwable): Unit = {
    log.logError(s"capture failure, Context: {${operationContext.toString}} $getThreadInfo", throwable)
    lock.lock()
    try {
      errorCaptureFirstException.compareAndSet(null, throwable)
      pendingTasksCompleted.signal()
    } finally {
      lock.unlock()
    }
  }

  private def cancelWork(): Unit = {
    val totalActiveOperations = activeBatches.values.map(_.operations.size).sum
    logInfoOrWarning(s"cancelling remaining unprocessed tasks ${activeTasks.get} " +
        s"[bulkWrite tasks (operations across ${activeBatches.size} batches): $totalActiveOperations] " +
        s"Context: ${operationContext.toString}")
    subscriptionDisposable.dispose()
  }

  private def shouldIgnore(statusCode: Int, subStatusCode: Int): Boolean = {
    // Transactional batches only support ItemOverwrite - no errors to ignore
    false
  }

  private def shouldRetry(statusCode: Int, subStatusCode: Int, operationContext: OperationContext): Boolean = {
    var returnValue = false
    if (operationContext.attemptNumber < writeConfig.maxRetryCount) {
      // Transactional batches only support ItemOverwrite (upsert)
      // Upsert can return 404/0 in rare cases (when due to TTL expiration there is a race condition)
      returnValue = Exceptions.canBeTransientFailure(statusCode, subStatusCode) ||
        statusCode == 0 || // Gateway mode reports inability to connect due to PoolAcquirePendingLimitException as status code 0
        Exceptions.isNotFoundExceptionCore(statusCode, subStatusCode)
    }

    log.logDebug(s"Should retry statusCode '$statusCode:$subStatusCode' -> $returnValue, " +
      s"Context: ${operationContext.toString} $getThreadInfo")

    returnValue
  }

  private def getId(objectNode: ObjectNode) = {
    val idField = objectNode.get(CosmosConstants.Properties.Id)
    assume(idField != null && idField.isTextual)
    idField.textValue()
  }

  /**
   * Don't wait for any remaining work but signal to the writer the ungraceful close
   * Should not throw any exceptions
   */
  override def abort(shouldThrow: Boolean): Unit = {
    if (shouldThrow) {
      log.logError(s"Abort, Context: ${operationContext.toString} $getThreadInfo")
      // signal an exception that will be thrown for any pending work/flushAndClose if no other exception has
      // been registered
      captureIfFirstFailure(
        new IllegalStateException(s"The Spark task was aborted, Context: ${operationContext.toString}"))
    } else {
      log.logWarning(s"BulkWriter aborted and commit retried, Context: ${operationContext.toString} $getThreadInfo")
    }
    cancelWork()
  }

  private class OperationContext
  (
    itemIdInput: String,
    partitionKeyValueInput: PartitionKey,
    eTagInput: Option[String],
    val attemptNumber: Int,
    val sequenceNumber: Long,
    /** starts from 1 * */
    sourceItemInput: Option[ObjectNode] = None) // for patchBulkUpdate: source item refers to the original objectNode from which SDK constructs the final bulk item operation
  {
    private val ctxCore: OperationContextCore = OperationContextCore(itemIdInput, partitionKeyValueInput, eTagInput, sourceItemInput)

    override def equals(obj: Any): Boolean = ctxCore.equals(obj)

    override def hashCode(): Int = ctxCore.hashCode()

    override def toString: String = {
      ctxCore.toString + s", attemptNumber = $attemptNumber"
    }

    def itemId: String = ctxCore.itemId

    def partitionKeyValue: PartitionKey = ctxCore.partitionKeyValue

    def eTag: Option[String] = ctxCore.eTag

    def sourceItem: Option[ObjectNode] = ctxCore.sourceItem
  }

  private case class OperationContextCore
  (
    itemId: String,
    partitionKeyValue: PartitionKey,
    eTag: Option[String],
    sourceItem: Option[ObjectNode] = None) // for patchBulkUpdate: source item refers to the original objectNode from which SDK constructs the final bulk item operation
  {
    override def productPrefix: String = "OperationContext"
  }

}

private object TransactionalBulkWriter {
  private val log = new DefaultDiagnostics().getLogger(this.getClass)
  //scalastyle:off magic.number
  private val maxDelayOn408RequestTimeoutInMs = 3000
  private val minDelayOn408RequestTimeoutInMs = 500
  private val maxItemOperationsToShowInErrorMessage = 10
  private val BULK_WRITER_REQUESTS_BOUNDED_ELASTIC_THREAD_NAME = "bulk-writer-requests-bounded-elastic"
  private val BULK_WRITER_INPUT_BOUNDED_ELASTIC_THREAD_NAME = "bulk-writer-input-bounded-elastic"
  private val BULK_WRITER_RESPONSES_BOUNDED_ELASTIC_THREAD_NAME = "bulk-writer-responses-bounded-elastic"
  private val TTL_FOR_SCHEDULER_WORKER_IN_SECONDS = 60 // same as BoundedElasticScheduler.DEFAULT_TTL_SECONDS
  //scalastyle:on magic.number

  // let's say the spark executor VM has 16 CPU cores.
  // let's say we have a cosmos container with 1M RU which is 167 partitions
  // let's say we are ingesting items of size 1KB
  // let's say max request size is 1MB
  // hence we want 1MB/ 1KB items per partition to be buffered
  // 1024 * 167 items should get buffered on a 16 CPU core VM
  // so per CPU core we want (1024 * 167 / 16) max items to be buffered
  // Reduced the targeted buffer from 2MB per partition and core to 1 MB because
  // we had a few customers seeing to high CPU usage with the previous setting
  // Reason is that several customers use larger than 1 KB documents so we need
  // to be less aggressive with the buffering
  val DefaultMaxPendingOperationPerCore: Int = 1024 * 167 / 16

  val emitFailureHandler: EmitFailureHandler =
    (signalType, emitResult) => {
      if (emitResult.equals(EmitResult.FAIL_NON_SERIALIZED)) {
        log.logDebug(s"emitFailureHandler - Signal: ${signalType.toString}, Result: ${emitResult.toString}")
        true
      } else {
        log.logError(s"emitFailureHandler - Signal: ${signalType.toString}, Result: ${emitResult.toString}")
        false
      }
    }

  private val emitFailureHandlerForComplete: EmitFailureHandler = (signalType, emitResult) => {
    if (emitResult.equals(EmitResult.FAIL_NON_SERIALIZED)) {
      log.logWarning(s"emitFailureHandlerForComplete - Signal:$signalType, Result:$emitResult")
      true
    } else if (emitResult.equals(EmitResult.FAIL_CANCELLED) ||
      emitResult.equals(EmitResult.FAIL_TERMINATED) ||
      emitResult.equals(EmitResult.FAIL_OVERFLOW)) {
      log.logWarning(s"emitFailureHandlerForComplete - Signal:$signalType, Result:$emitResult")
      false
    } else {
      log.logError(s"emitFailureHandlerForComplete - Signal:$signalType, Result:$emitResult")
      false
    }
  }

  private val bulkProcessingThresholds = new CosmosBulkExecutionThresholdsState()

  // For batch-level backpressure: use a conservative estimate of concurrent batches across the JVM
  // Each batch can have up to 100 operations, so this sizing provides headroom for the scheduler queues
  private val maxPendingBatchesPerJVM: Int = DefaultMaxPendingOperationPerCore * SparkUtils.getNumberOfHostCPUCores / 50

  // Custom bounded elastic scheduler to consume input flux
  val bulkWriterRequestsBoundedElastic: Scheduler = Schedulers.newBoundedElastic(
    Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
    Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE + 2 * maxPendingBatchesPerJVM,
    BULK_WRITER_REQUESTS_BOUNDED_ELASTIC_THREAD_NAME,
    TTL_FOR_SCHEDULER_WORKER_IN_SECONDS, true)

  // Custom bounded elastic scheduler to consume input flux
  val bulkWriterInputBoundedElastic: Scheduler = Schedulers.newBoundedElastic(
    Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
    Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE + 2 * maxPendingBatchesPerJVM,
    BULK_WRITER_INPUT_BOUNDED_ELASTIC_THREAD_NAME,
    TTL_FOR_SCHEDULER_WORKER_IN_SECONDS, true)

  // Custom bounded elastic scheduler to switch off IO thread to process response.
  val bulkWriterResponsesBoundedElastic: Scheduler = Schedulers.newBoundedElastic(
    Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
    Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE + maxPendingBatchesPerJVM,
    BULK_WRITER_RESPONSES_BOUNDED_ELASTIC_THREAD_NAME,
    TTL_FOR_SCHEDULER_WORKER_IN_SECONDS, true)


  def getThreadInfo: String = {
    val t = Thread.currentThread()
    val group = Option.apply(t.getThreadGroup) match {
      case Some(group) => group.getName
      case None => "n/a"
    }
    s"Thread[Name: ${t.getName}, Group: $group, IsDaemon: ${t.isDaemon} Id: ${t.getId}]"
  }

  private class BulkOperationFailedException(statusCode: Int, subStatusCode: Int, message:String, cause: Throwable)
    extends CosmosException(statusCode, message, null, cause) {
      BridgeInternal.setSubStatusCode(this, subStatusCode)
  }
}

//scalastyle:on multiple.string.literals
//scalastyle:on null
//scalastyle:on file.size.limit
