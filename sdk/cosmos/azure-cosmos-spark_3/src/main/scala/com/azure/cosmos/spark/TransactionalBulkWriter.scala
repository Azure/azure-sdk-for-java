// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

// scalastyle:off underscore.import
import com.azure.cosmos.implementation.batch.{BulkExecutorDiagnosticsTracker, CosmosBatchBulkOperation, CosmosBulkTransactionalBatchResponse, TransactionalBulkExecutor}
import com.azure.cosmos.implementation.{CosmosTransactionalBulkExecutionOptionsImpl, UUIDs}
import com.azure.cosmos.models.{CosmosBatch, CosmosBatchResponse}
import com.azure.cosmos.spark.BulkWriter.getThreadInfo
import com.azure.cosmos.spark.TransactionalBulkWriter.{BulkOperationFailedException, DefaultMaxPendingOperationPerCore, emitFailureHandler, transactionalBatchInputBoundedElastic, transactionalBulkWriterInputBoundedElastic, transactionalBulkWriterRequestsBoundedElastic}
import com.azure.cosmos.spark.diagnostics.DefaultDiagnostics
import com.azure.cosmos.{BridgeInternal, CosmosAsyncContainer, CosmosDiagnosticsContext, CosmosEndToEndOperationLatencyPolicyConfigBuilder, CosmosException}
import reactor.core.Scannable
import reactor.core.scala.publisher.SMono.PimpJFlux
import reactor.core.scheduler.Scheduler

import java.util.Objects
import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable
import scala.compat.java8.FunctionConverters.enrichAsJavaFunction
import scala.concurrent.duration.Duration
// scalastyle:on underscore.import
import com.azure.cosmos.implementation.guava25.base.Preconditions
import com.azure.cosmos.implementation.spark.{OperationContextAndListenerTuple, OperationListener}
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.spark.diagnostics.{DiagnosticsContext, DiagnosticsLoader, LoggerHelper, SparkTaskContext}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.TaskContext
import reactor.core.Disposable
import reactor.core.publisher.Sinks
import reactor.core.publisher.Sinks.{EmitFailureHandler, EmitResult}
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

  private val transactionalBulkExecutionConfigs = writeConfig.bulkExecutionConfigs.get.asInstanceOf[CosmosWriteTransactionalBulkExecutionConfigs]
  private val maxConcurrentPartitions = transactionalBulkExecutionConfigs.maxConcurrentCosmosPartitions match {
    // using the provided maximum of concurrent partitions per Spark partition on the input data
    // multiplied by 2 to leave space for partition splits during ingestion
    case Some(configuredMaxConcurrentPartitions) => 2 * configuredMaxConcurrentPartitions
    // using the total number of physical partitions
    // multiplied by 2 to leave space for partition splits during ingestion
    case None => 2 * ContainerFeedRangesCache.getFeedRanges(container, containerConfig.feedRangeRefreshIntervalInSecondsOpt).block().size
  }

  log.logInfo(
    s"TransactionalBulkWriter instantiated (Host CPU count: $cpuCount, maxPendingBatches: $maxPendingBatches, " +
      s"maxPendingOperations: $maxPendingOperations, maxConcurrentPartitions: $maxConcurrentPartitions ...")

  private val closed = new AtomicBoolean(false)
  private val lock = new ReentrantLock
  private val pendingTasksCompleted = lock.newCondition
  private val pendingRetries = new AtomicLong(0)
  private val pendingBatchRetries = new ConcurrentHashMap[PartitionKey, CosmosBatchOperation].asScala
  private val activeBatchTasks = new AtomicInteger(0)
  private val activeBatches = new ConcurrentHashMap[PartitionKey, CosmosBatchOperation].asScala
  private val errorCaptureFirstException = new AtomicReference[Throwable]()
  private val transactionalBulkInputEmitter: Sinks.Many[TransactionalBulkItem] = Sinks.many().unicast().onBackpressureBuffer()
  private val transactionalBatchInputEmitter: Sinks.Many[CosmosBatchBulkOperation] = Sinks.many().unicast().onBackpressureBuffer()

  // for transactional batch, all rows/items from the dataframe should be grouped as one cosmos batch
  private val transactionalBatchPartitionKeyScheduled = java.util.concurrent.ConcurrentHashMap.newKeySet[PartitionKey]().asScala

  private val semaphore = new Semaphore(maxPendingBatches)

  private val totalScheduledMetrics = new AtomicLong(0) // this will be for each operation
  private val totalSuccessfulIngestionMetrics = new AtomicLong(0)

  private val maxOperationTimeout = java.time.Duration.ofSeconds(CosmosConstants.batchOperationEndToEndTimeoutInSeconds)
  private val endToEndTimeoutPolicy = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(maxOperationTimeout)
    .enable(true)
    .build
  private val cosmosTransactionalBulkExecutionOptions = new CosmosTransactionalBulkExecutionOptionsImpl()
  private val monotonicOperationCounter = new AtomicLong(0)

  cosmosTransactionalBulkExecutionOptions.setSchedulerOverride(transactionalBulkWriterRequestsBoundedElastic)
  cosmosTransactionalBulkExecutionOptions.setMaxConcurrentCosmosPartitions(maxConcurrentPartitions)
  cosmosTransactionalBulkExecutionOptions.setCosmosEndToEndLatencyPolicyConfig(endToEndTimeoutPolicy)
  if (transactionalBulkExecutionConfigs.maxConcurrentOperations.isDefined) {
    cosmosTransactionalBulkExecutionOptions.setMaxOperationsConcurrency(transactionalBulkExecutionConfigs.maxConcurrentOperations.get)
  }
  if (transactionalBulkExecutionConfigs.maxConcurrentBatches.isDefined) {
    cosmosTransactionalBulkExecutionOptions.setMaxBatchesConcurrency(transactionalBulkExecutionConfigs.maxConcurrentBatches.get)
  }

  private class ForwardingMetricTracker(val verboseLoggingEnabled: AtomicBoolean) extends BulkExecutorDiagnosticsTracker {
    override def trackDiagnostics(ctx: CosmosDiagnosticsContext): Unit = {
      val ctxOption = Option.apply(ctx)
      outputMetricsPublisher.trackWriteOperation(0, ctxOption)
      if (ctxOption.isDefined && verboseLoggingEnabled.get) {
        TransactionalBulkWriter.log.logWarning(s"Track transactional bulk operation after re-enqueued retry: ${ctxOption.get.toJson}")
      }
    }

    override def verboseLoggingAfterReEnqueueingRetriesEnabled(): Boolean = {
      verboseLoggingEnabled.get()
    }
  }

  cosmosTransactionalBulkExecutionOptions.setDiagnosticsTracker(
    new ForwardingMetricTracker(verboseLoggingAfterReEnqueueingRetriesEnabled)
  )

  ThroughputControlHelper.populateThroughputControlGroupName(cosmosTransactionalBulkExecutionOptions, writeConfig.throughputControlConfig)

  private val operationContext = initializeOperationContext()

  private def initializeOperationContext(): SparkTaskContext = {
    val taskContext = TaskContext.get

    val diagnosticsContext: DiagnosticsContext = DiagnosticsContext(UUIDs.nonBlockingRandomUUID(), "TransactionalBulkWriter")

    if (taskContext != null) {
      val taskDiagnosticsContext = SparkTaskContext(diagnosticsContext.correlationActivityId,
        taskContext.stageId(),
        taskContext.partitionId(),
        taskContext.taskAttemptId(),
        "")

      val listener: OperationListener =
        DiagnosticsLoader.getDiagnosticsProvider(diagnosticsConfig).getLogger(this.getClass)

      val operationContextAndListenerTuple = new OperationContextAndListenerTuple(taskDiagnosticsContext, listener)
      cosmosTransactionalBulkExecutionOptions
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

  private def scheduleRetry(
                             trackPendingRetryAction: () => Boolean,
                             clearPendingRetryAction: () => Boolean,
                             cosmosBatchOperation: CosmosBatchOperation,
                             statusCode: Int): Unit = {
    if (trackPendingRetryAction()) {
      this.pendingRetries.incrementAndGet()
    }

    // this is to ensure the submission will happen on a different thread in background
    // and doesn't block the active thread
    val deferredRetryMono = SMono.defer(() => {
      scheduleBatchInternal(cosmosBatchOperation)

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

  private val bulkInputSubscriptionDisposable: Disposable = {
    log.logTrace(s"bulkInputSubscriptionDisposable, Context: ${operationContext.toString} $getThreadInfo")
    transactionalBulkInputEmitter
      .asFlux()
      .onBackpressureBuffer()
      .bufferUntilChanged[PartitionKey](
        new Function[TransactionalBulkItem, PartitionKey] {
          override def apply(x: TransactionalBulkItem): PartitionKey = x.partitionKey
        }.asJava
      )
      .flatMap(bulkItemsList => {
        if (bulkItemsList.size() == 0) {
          SMono.empty
        } else {
          val cosmosBatch = CosmosBatch.createCosmosBatch(bulkItemsList.get(0).partitionKey)
          bulkItemsList.forEach(bulkItem => {
            writeConfig.itemWriteStrategy match {
              case ItemWriteStrategy.ItemOverwrite => cosmosBatch.upsertItemOperation(bulkItem.objectNode)
              case _ => throw new IllegalStateException(s"Item write strategy ${writeConfig.itemWriteStrategy} is not supported for bulk with transactional")
            }
          })

          scheduleBatch(cosmosBatch)
          SMono.empty
        }
      })
      .publishOn(transactionalBulkWriterInputBoundedElastic)
      .doOnError(t => {
        log.logError(s"Bulk input publishing flux failed, Context: ${operationContext.toString} $getThreadInfo", t)
      })
      .subscribe()
  }

  private val batchSubscriptionDisposable: Disposable = {
    log.logTrace(s"batchSubscriptionDisposable, Context: ${operationContext.toString} $getThreadInfo")

    val batchInputFlux = transactionalBatchInputEmitter
      .asFlux()
      .onBackpressureBuffer()
      .publishOn(transactionalBatchInputBoundedElastic)
      .doOnError(t => {
        log.logError(s"Batch input publishing flux failed, Context: ${operationContext.toString} $getThreadInfo", t)
      })

    val transactionalExecutor = new TransactionalBulkExecutor(
      container,
      batchInputFlux,
      cosmosTransactionalBulkExecutionOptions)

    val batchResponseFlux: SFlux[CosmosBulkTransactionalBatchResponse] = transactionalExecutor.execute().asScala

    batchResponseFlux.subscribe(
      resp => {
        val isGettingRetried = new AtomicBoolean(false)
        val shouldSkipTaskCompletion = new AtomicBoolean(false)
        try {
          // all the operations in the batch will have the same partition key value
          // get the partition key value from the first result
          val partitionKeyValue = resp.getCosmosBatchBulkOperation.getCosmosBatch.getPartitionKeyValue
          val activeBatchOperationOpt = activeBatches.remove(partitionKeyValue)
          val pendingBatchOperationRetriesOpt = pendingBatchRetries.remove(partitionKeyValue)

          if (pendingBatchOperationRetriesOpt.isDefined) {
            pendingRetries.decrementAndGet()
          }

          if (activeBatchOperationOpt.isEmpty) {
            // can't find the batch operation in list of active operations!
            logInfoOrWarning(s"Cannot find active batch operation for '$partitionKeyValue'. This can happen when " +
              s"retries get re-enqueued.")
            shouldSkipTaskCompletion.set(true)
          }

          if (activeBatchOperationOpt.isDefined || pendingBatchOperationRetriesOpt.isDefined) {
            val batchOperation = activeBatchOperationOpt.orElse(pendingBatchOperationRetriesOpt).get

            if (resp.getException != null) {
              Option(resp.getException) match {
                case Some(cosmosException: CosmosException) =>
                  handleNonSuccessfulStatusCode(
                    batchOperation.operationContext,
                    batchOperation.cosmosBatchBulkOperation,
                    None,
                    isGettingRetried,
                    Some(cosmosException))
                case _ =>
                  log.logWarning(
                    s"unexpected failure: partitionKeyValue=[" +
                      s"${batchOperation.operationContext}], encountered , attemptNumber=${batchOperation.operationContext.attemptNumber}, " +
                      s"exceptionMessage=${resp.getException.getMessage}, " +
                      s"Context: ${operationContext.toString} $getThreadInfo", resp.getException)
                  captureIfFirstFailure(resp.getException)
                  cancelWork()
              }
            } else if (!resp.getResponse.isSuccessStatusCode) {
              handleNonSuccessfulStatusCode(
                batchOperation.operationContext,
                batchOperation.cosmosBatchBulkOperation,
                Some(resp.getResponse),
                isGettingRetried,
                None)
            } else {
              // no error case
              outputMetricsPublisher.trackWriteOperation(resp.getResponse.size(), None)
              totalSuccessfulIngestionMetrics.addAndGet(resp.getResponse.size())
            }
          }
        }
        finally {
          if (!isGettingRetried.get) {
            semaphore.release()
          }
        }

        if (!shouldSkipTaskCompletion.get) {
          markTaskCompletion()
        }
      },
      errorConsumer = Option.apply(
        ex => {
          log.logError(s"Unexpected failure code path in transactional batch ingestion, " +
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

    val transactionalBulkItem = TransactionalBulkItem(partitionKeyValue, objectNode)
    transactionalBulkInputEmitter.emitNext(transactionalBulkItem, emitFailureHandler)
  }

  private def scheduleBatch(cosmosBatch: CosmosBatch): Unit = {
    Preconditions.checkState(!closed.get())
    throwIfCapturedExceptionExists()

    val activeTasksSemaphoreTimeout = 10
    val cosmosBatchBulkOperation = new CosmosBatchBulkOperation(cosmosBatch)
    val operationContext =
      new OperationContext(
        cosmosBatch.getPartitionKeyValue,
        1,
        monotonicOperationCounter.incrementAndGet())
    if (!transactionalBatchPartitionKeyScheduled.add(cosmosBatch.getPartitionKeyValue)) {
      log.logError(s"There are already existing cosmos batch operation scheduled for partition key ${cosmosBatch.getPartitionKeyValue}," +
        s" transactional is not guaranteed, fail")
      SMono.error(new IllegalStateException(s"Transactional is not guaranteed for partition key ${cosmosBatch.getPartitionKeyValue}"))
    }

    val numberOfIntervalsWithIdenticalActiveOperationSnapshots = new AtomicLong(0)
    // Don't clone the activeOperations for the first iteration
    // to reduce perf impact before the Semaphore has been acquired
    // this means if the semaphore can't be acquired within 10 minutes
    // the first attempt will always assume it wasn't stale - so effectively we
    // allow staleness for ten additional minutes - which is perfectly fine
    var activeCosmosBatchSnapshot = mutable.Map.empty[PartitionKey, CosmosBatchOperation]
    var pendingCosmosBatchSnapshot = mutable.Map.empty[PartitionKey, CosmosBatchOperation]

    log.logTrace(
      s"Before TryAcquire ${totalScheduledMetrics.get}, Context: ${operationContext.toString} $getThreadInfo")
    while (!semaphore.tryAcquire(activeTasksSemaphoreTimeout, TimeUnit.MINUTES)) {
      log.logDebug(s"Not able to acquire semaphore, Context: ${operationContext.toString} $getThreadInfo")
      if (bulkInputSubscriptionDisposable.isDisposed || batchSubscriptionDisposable.isDisposed) {
        captureIfFirstFailure(
          new IllegalStateException("Can't accept any new work - TransactionalBulkWriter has been disposed already"))
      }

      throwIfProgressStaled(
        "Semaphore acquisition",
        activeCosmosBatchSnapshot,
        pendingCosmosBatchSnapshot,
        numberOfIntervalsWithIdenticalActiveOperationSnapshots,
        allowRetryOnNewBulkWriterInstance = false)

      activeCosmosBatchSnapshot = activeBatches.clone()
      pendingCosmosBatchSnapshot = pendingBatchRetries.clone()
    }

    val cnt = totalScheduledMetrics.getAndAdd(cosmosBatch.getOperations.size())
    log.logTrace(s"total scheduled $cnt, Context: ${operationContext.toString} $getThreadInfo")

    scheduleBatchInternal(CosmosBatchOperation(cosmosBatchBulkOperation, operationContext))
  }

  private def scheduleBatchInternal(cosmosBatchOperation: CosmosBatchOperation): Unit = {
    activeBatchTasks.incrementAndGet()

    if (cosmosBatchOperation.operationContext.attemptNumber > 1) {
      logInfoOrWarning(s"bulk scheduleWrite attemptCnt: ${cosmosBatchOperation.operationContext.attemptNumber}, " +
        s"Context: ${operationContext.toString} $getThreadInfo")
    }

    activeBatches.put(
      cosmosBatchOperation.cosmosBatchBulkOperation.getCosmosBatch.getPartitionKeyValue,
      cosmosBatchOperation)
    transactionalBatchInputEmitter.emitNext(cosmosBatchOperation.cosmosBatchBulkOperation, emitFailureHandler)
  }

  //scalastyle:off method.length
  //scalastyle:off cyclomatic.complexity
  private[this] def handleNonSuccessfulStatusCode
  (
    operationContext: OperationContext,
    cosmosBatchBulkOperation: CosmosBatchBulkOperation,
    cosmosBatchResponse: Option[CosmosBatchResponse],
    isGettingRetried: AtomicBoolean,
    responseException: Option[CosmosException]
  ) : Unit = {

    val exceptionMessage = cosmosBatchResponse match {
      case Some(r) => r.getErrorMessage
      case None => responseException match {
        case Some(e) => e.getMessage
        case None => ""
      }
    }

    val effectiveStatusCode = cosmosBatchResponse match {
      case Some(r) => r.getStatusCode
      case None => responseException match {
        case Some(e) => e.getStatusCode
        case None => CosmosConstants.StatusCodes.Timeout
      }
    }

    val effectiveSubStatusCode = cosmosBatchResponse match {
      case Some(r) => r.getSubStatusCode
      case None => responseException match {
        case Some(e) => e.getSubStatusCode
        case None => 0
      }
    }

    log.logDebug(s"encountered batch operation response with status code " +
      s"$effectiveStatusCode:$effectiveSubStatusCode, " +
      s"Context: ${operationContext.toString} $getThreadInfo")

    if (shouldRetry(effectiveStatusCode, effectiveSubStatusCode, operationContext)) {
      // requeue
      log.logWarning(s"for partitionKeyValue=[${operationContext.partitionKeyValueInput}], " +
        s"encountered status code '$effectiveStatusCode:$effectiveSubStatusCode', will retry! " +
        s"attemptNumber=${operationContext.attemptNumber}, exceptionMessage=${exceptionMessage},  " +
        s"Context: {${operationContext.toString}} $getThreadInfo")

      val batchOperationRetry = CosmosBatchOperation(
        cosmosBatchBulkOperation,
        new OperationContext(
          operationContext.partitionKeyValueInput,
          operationContext.attemptNumber + 1,
          operationContext.sequenceNumber)
      )

      this.scheduleRetry(
        trackPendingRetryAction = () => pendingBatchRetries.put(cosmosBatchBulkOperation.getCosmosBatch.getPartitionKeyValue, batchOperationRetry).isEmpty,
        clearPendingRetryAction = () => pendingBatchRetries.remove(cosmosBatchBulkOperation.getCosmosBatch.getPartitionKeyValue).isDefined,
        batchOperationRetry,
        effectiveStatusCode)
      isGettingRetried.set(true)

    } else {
      log.logError(s"for partitionKeyValue=[${operationContext.partitionKeyValueInput}], " +
        s"encountered status code '$effectiveStatusCode:$effectiveSubStatusCode', all retries exhausted! " +
        s"attemptNumber=${operationContext.attemptNumber}, exceptionMessage=${exceptionMessage}, " +
        s"Context: {${operationContext.toString} $getThreadInfo")

      val message = s"All retries exhausted for batch operation - " +
        s"statusCode=[$effectiveStatusCode:$effectiveSubStatusCode] " +
        s"partitionKeyValue=[${operationContext.partitionKeyValueInput}]"

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

  private[this] def getActiveOperationsLog(activeOperationsSnapshot: mutable.Map[PartitionKey, CosmosBatchOperation]): String = {
    val sb = new StringBuilder()

    // flatten the batches
    activeOperationsSnapshot
      .values
      .flatMap(batchOperation => {
        val statusTracker = batchOperation.cosmosBatchBulkOperation.getStatusTracker
        val statusHistory = if (statusTracker != null && statusTracker.getTotalCount > 0) {
          Some(statusTracker.toString)
        } else {
          None
        }
        batchOperation.cosmosBatchBulkOperation.getCosmosBatch.getOperations.asScala.map(itemOperation =>
          (itemOperation, batchOperation.operationContext.attemptNumber, statusHistory))
      })
      .toList
      .take(TransactionalBulkWriter.maxItemOperationsToShowInErrorMessage)
      .foreach(itemOperationTuple => {
        if (sb.nonEmpty) {
          sb.append(", ")
        }

        sb.append(itemOperationTuple._1.getOperationType)
        sb.append("->")
        sb.append(s"${itemOperationTuple._1.getId}/${itemOperationTuple._1.getPartitionKeyValue}/(${itemOperationTuple._2})")
        itemOperationTuple._3.foreach(history => sb.append(s", statusHistory=$history"))
      })

    sb.toString()
  }

  private[this] def sameBatchOperations
  (
    snapshot: mutable.Map[PartitionKey, CosmosBatchOperation],
    current:  mutable.Map[PartitionKey, CosmosBatchOperation]
  ): Boolean = {

    if (snapshot.size != current.size) {
      false
    } else {
      snapshot.keys.forall(partitionKey => {
        if (current.contains(partitionKey)) {

          snapshot(partitionKey).cosmosBatchBulkOperation.getCosmosBatch.getOperations.asScala.forall(itemOperationSnapshot => {
            current(partitionKey).cosmosBatchBulkOperation.getCosmosBatch.getOperations.asScala.exists(currentOperation =>
              itemOperationSnapshot.getOperationType == currentOperation.getOperationType
                && itemOperationSnapshot.getPartitionKeyValue == currentOperation.getPartitionKeyValue
                && Objects.equals(itemOperationSnapshot.getId, currentOperation.getId)
                && Objects.equals(itemOperationSnapshot.getItem[Object], currentOperation.getItem[Object])
            )
          })
        } else {
          false
        }
      })
    }
  }

  private[this] def throwIfProgressStaled
  (
    operationName: String,
    activeOperationsSnapshot: mutable.Map[PartitionKey, CosmosBatchOperation],
    pendingRetriesSnapshot: mutable.Map[PartitionKey, CosmosBatchOperation],
    numberOfIntervalsWithIdenticalActiveOperationSnapshots: AtomicLong,
    allowRetryOnNewBulkWriterInstance: Boolean
  ): Unit = {
    val operationsLog = getActiveOperationsLog(activeOperationsSnapshot)

    if (sameBatchOperations(pendingRetriesSnapshot ++ activeOperationsSnapshot, activeBatches ++ pendingBatchRetries)) {
      numberOfIntervalsWithIdenticalActiveOperationSnapshots.incrementAndGet()
      log.logWarning(
        s"$operationName has been waiting $numberOfIntervalsWithIdenticalActiveOperationSnapshots " +
          s"times for identical set of operations: $operationsLog " +
          s"Context: ${operationContext.toString} $getThreadInfo"
      )
    } else {
      numberOfIntervalsWithIdenticalActiveOperationSnapshots.set(0)
      logInfoOrWarning(
        s"$operationName is waiting for active batch operations: $operationsLog " +
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
      val exception = {
        // order by batch sequence number
        // then return all operations inside the batch
        val retriableRemainingOperations = if (allowRetryOnNewBulkWriterInstance) {
          Some(
            (pendingRetriesSnapshot ++ activeOperationsSnapshot)
              .toList
              .sortBy(op => op._2.operationContext.sequenceNumber)
              .map(batchOperationPartitionKeyPair => batchOperationPartitionKeyPair._2.cosmosBatchBulkOperation.getCosmosBatch)
              .flatMap(batch => batch.getOperations.asScala)
          )
        } else {
          None
        }

        new BulkWriterNoProgressException(
          s"Stale batch bulk ingestion identified in $operationName - the following active operations have not been " +
            s"completed (first ${TransactionalBulkWriter.maxItemOperationsToShowInErrorMessage} shown) or progressed after " +
            s"$maxNoProgressIntervalInSeconds seconds: $operationsLog",
          commitAttempt,
          retriableRemainingOperations)
      }

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
          log.logInfo(s"flushAndClose invoked, Context: ${operationContext.toString} $getThreadInfo")
          log.logInfo(s"completed so far ${totalSuccessfulIngestionMetrics.get()}, " +
            s"pending batch tasks ${activeBatches.size}" +
            s" Context: ${operationContext.toString} $getThreadInfo")

          logInfoOrWarning(s"invoking transactionalBulkInputEmitter.onComplete(), Context: ${operationContext.toString} $getThreadInfo")
          transactionalBulkInputEmitter.emitComplete(TransactionalBulkWriter.emitFailureHandlerForComplete)

          // error handling, if there is any error and the subscription is cancelled
          // the remaining tasks will not be processed hence we never reach activeTasks = 0
          // once we do error handling we should think how to cover the scenario.
          lock.lock()
          try {
            val numberOfIntervalsWithIdenticalActiveOperationSnapshots = new AtomicLong(0)
            var activeTasksSnapshot = activeBatchTasks.get()
            var pendingRetriesSnapshot = pendingRetries.get()
            while ((pendingRetriesSnapshot > 0 || activeTasksSnapshot > 0)
              && errorCaptureFirstException.get == null) {

              logInfoOrWarning(
                s"Waiting for pending activeBatchTasks $activeTasksSnapshot and/or pendingBatchRetries " +
                  s"$pendingRetriesSnapshot,  Context: ${operationContext.toString} $getThreadInfo")
              val activeOperationsSnapshot = activeBatches.clone()
              val pendingOperationsSnapshot = pendingBatchRetries.clone()
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
                  val bulkInputBuffered = Scannable.from(transactionalBulkInputEmitter).scan(Scannable.Attr.BUFFERED)
                  val batchBuffered = Scannable.from(transactionalBatchInputEmitter).scan(Scannable.Attr.BUFFERED)

                  if (verboseLoggingAfterReEnqueueingRetriesEnabled.compareAndSet(false, true)) {
                    log.logWarning(s"Starting to re-enqueue retries. Enabling verbose logs. "
                      + s"Number of intervals with identical pending operations: "
                      + s"$numberOfIntervalsWithIdenticalActiveOperationSnapshots Active Batch Operations: "
                      + s"$activeOperationsSnapshot, "
                      + s"PendingRetries: $pendingRetriesSnapshot, "
                      + s"Buffered bulk input tasks: $bulkInputBuffered, "
                      + s"Buffered batch input tasks: $batchBuffered, "
                      + s"Attempt: ${numberOfIntervalsWithIdenticalActiveOperationSnapshots.get} - "
                      + s"Context: ${operationContext.toString} $getThreadInfo")
                  } else if ((numberOfIntervalsWithIdenticalActiveOperationSnapshots.get % 3) == 0) {
                    log.logWarning(s"Reattempting to re-enqueue retries. Enabling verbose logs. "
                      + s"Number of intervals with identical pending operations: "
                      + s"$numberOfIntervalsWithIdenticalActiveOperationSnapshots Active Batch Operations: "
                      + s"$activeOperationsSnapshot,  "
                      + s"PendingRetries: $pendingRetriesSnapshot, "
                      + s"Buffered bulk input tasks: $bulkInputBuffered, "
                      + s"Buffered batch input tasks: $batchBuffered, "
                      + s"Attempt: ${numberOfIntervalsWithIdenticalActiveOperationSnapshots.get} - "
                      + s"Context: ${operationContext.toString} $getThreadInfo")
                  }

                  activeOperationsSnapshot.foreach(operationPartitionKeyPair => {
                    if (activeBatches.contains(operationPartitionKeyPair._1)) {
                      // re-validating whether the operation is still active - if so, just re-enqueue another retry
                      // this is harmless - because all bulkItemOperations from Spark connector are always idempotent
                      // For FAIL_NON_SERIALIZED, will keep retry, while for other errors, use the default behavior
                      transactionalBatchInputEmitter.emitNext(operationPartitionKeyPair._2.cosmosBatchBulkOperation, TransactionalBulkWriter.emitFailureHandler)
                      log.logWarning(s"Re-enqueued a retry for pending active batch task "
                        + s"(${operationPartitionKeyPair._1})' "
                        + s"- Attempt: ${numberOfIntervalsWithIdenticalActiveOperationSnapshots.get} - "
                        + s"Context: ${operationContext.toString} $getThreadInfo")
                    }
                  })
                }
              }

              activeTasksSnapshot = activeBatchTasks.get()
              pendingRetriesSnapshot = pendingRetries.get()
              val semaphoreAvailablePermitsSnapshot = semaphore.availablePermits()

              if (awaitCompleted) {
                logInfoOrWarning(s"Waiting completed for pending activeBatchTasks $activeTasksSnapshot, pendingBatchRetries " +
                  s"$pendingRetriesSnapshot Context: ${operationContext.toString} $getThreadInfo")
              } else {
                logInfoOrWarning(s"Waiting interrupted for pending activeBatchTasks $activeTasksSnapshot , pendingBatchRetries " +
                  s"$pendingRetriesSnapshot - available permits $semaphoreAvailablePermitsSnapshot, " +
                  s"Context: ${operationContext.toString} $getThreadInfo")
              }
            }

            logInfoOrWarning(s"Waiting completed for pending activeBatchTasks $activeTasksSnapshot, pendingBatchRetries " +
              s"$pendingRetriesSnapshot Context: ${operationContext.toString} $getThreadInfo")
          } finally {
            lock.unlock()
          }


          logInfoOrWarning(s"invoking transactionalBatchInputEmitter.onComplete(), Context: ${operationContext.toString} $getThreadInfo")
          semaphore.release(Math.max(0, activeBatchTasks.get()))
          transactionalBatchInputEmitter.emitComplete(TransactionalBulkWriter.emitFailureHandlerForComplete)

          throwIfCapturedExceptionExists()
          assume(activeBatchTasks.get() <= 0)
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
        bulkInputSubscriptionDisposable.dispose()
        batchSubscriptionDisposable.dispose()
      }

      closed.set(true)
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
      val activeTasksLeftSnapshot = activeBatchTasks.decrementAndGet()
      val exceptionSnapshot = errorCaptureFirstException.get()
      log.logTrace(s"markTaskCompletion, Active batch tasks left: $activeTasksLeftSnapshot, " +
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
    logInfoOrWarning(s"cancelling remaining unprocessed tasks ${activeBatchTasks.get} " +
      s"[batch tasks ${activeBatches.size}]" +
      s"Context: ${operationContext.toString}")
    bulkInputSubscriptionDisposable.dispose()
    batchSubscriptionDisposable.dispose()
  }

  private def shouldRetry(statusCode: Int, subStatusCode: Int, operationContext: OperationContext): Boolean = {
    var returnValue = false
    if (operationContext.attemptNumber < writeConfig.maxRetryCount) {
      returnValue = Exceptions.canBeTransientFailure(statusCode, subStatusCode) ||
        statusCode == 0 // Gateway mode reports inability to connect due to PoolAcquirePendingLimitException as status code 0
    }

    log.logDebug(s"Should retry statusCode '$statusCode:$subStatusCode' -> $returnValue, " +
      s"Context: ${operationContext.toString} $getThreadInfo")

    returnValue
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
      log.logWarning(s"TransactionalBulkWriter aborted and commit retried, Context: ${operationContext.toString} $getThreadInfo")
    }
    cancelWork()
  }

  private class OperationContext
  (
    val partitionKeyValueInput: PartitionKey,
    val attemptNumber: Int,
    val sequenceNumber: Long,
    /** starts from 1 * */)
  {
    override def equals(obj: Any): Boolean = partitionKeyValueInput.equals(obj)
    override def hashCode(): Int = partitionKeyValueInput.hashCode()
    override def toString: String = {
      partitionKeyValueInput.toString + s", attemptNumber = $attemptNumber"
    }
  }

  private case class CosmosBatchOperation(cosmosBatchBulkOperation: CosmosBatchBulkOperation, operationContext: OperationContext)
  private case class TransactionalBulkItem(partitionKey: PartitionKey, objectNode: ObjectNode)
}

private object TransactionalBulkWriter {
  private val log = new DefaultDiagnostics().getLogger(this.getClass)
  //scalastyle:off magic.number
  private val maxDelayOn408RequestTimeoutInMs = 3000
  private val minDelayOn408RequestTimeoutInMs = 500
  private val maxItemOperationsToShowInErrorMessage = 10
  private val TRANSACTIONAL_BULK_WRITER_REQUESTS_BOUNDED_ELASTIC_THREAD_NAME = "transactional-bulk-writer-requests-bounded-elastic"
  private val TRANSACTIONAL_BULK_WRITER_INPUT_BOUNDED_ELASTIC_THREAD_NAME = "transactional-bulk-writer-input-bounded-elastic"
  private val TRANSACTIONAL_BATCH_INPUT_BOUNDED_ELASTIC_THREAD_NAME = "transactional-batch-input-bounded-elastic"
  private val TRANSACTIONAL_BULK_WRITER_RESPONSES_BOUNDED_ELASTIC_THREAD_NAME = "transactional-bulk-writer-responses-bounded-elastic"
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

  val emitFailureHandlerForComplete: EmitFailureHandler =
    (signalType, emitResult) => {
      if (emitResult.equals(EmitResult.FAIL_NON_SERIALIZED)) {
        log.logDebug(s"emitFailureHandlerForComplete - Signal: ${signalType.toString}, Result: ${emitResult.toString}")
        true
      } else if (emitResult.equals(EmitResult.FAIL_CANCELLED) || emitResult.equals(EmitResult.FAIL_TERMINATED)) {
        log.logDebug(s"emitFailureHandlerForComplete - Already completed - Signal: ${signalType.toString}, Result: ${emitResult.toString}")
        false
      } else {
        log.logError(s"emitFailureHandlerForComplete - Signal: ${signalType.toString}, Result: ${emitResult.toString}")
        false
      }
    }

  private val maxPendingOperationsPerJVM: Int = DefaultMaxPendingOperationPerCore * SparkUtils.getNumberOfHostCPUCores

  // Custom bounded elastic scheduler to consume input flux
  val transactionalBulkWriterRequestsBoundedElastic: Scheduler = Schedulers.newBoundedElastic(
    Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
    Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE + 2 * maxPendingOperationsPerJVM,
    TRANSACTIONAL_BULK_WRITER_REQUESTS_BOUNDED_ELASTIC_THREAD_NAME,
    TTL_FOR_SCHEDULER_WORKER_IN_SECONDS, true)

  // Custom bounded elastic scheduler to consume input flux
  val transactionalBulkWriterInputBoundedElastic: Scheduler = Schedulers.newBoundedElastic(
    Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
    Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE + 2 * maxPendingOperationsPerJVM,
    TRANSACTIONAL_BULK_WRITER_INPUT_BOUNDED_ELASTIC_THREAD_NAME,
    TTL_FOR_SCHEDULER_WORKER_IN_SECONDS, true)

  val transactionalBatchInputBoundedElastic: Scheduler = Schedulers.newBoundedElastic(
    Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
    Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE + 2 * maxPendingOperationsPerJVM,
    TRANSACTIONAL_BATCH_INPUT_BOUNDED_ELASTIC_THREAD_NAME,
    TTL_FOR_SCHEDULER_WORKER_IN_SECONDS, true)

  // Custom bounded elastic scheduler to switch off IO thread to process response.
  val transactionalBulkWriterResponsesBoundedElastic: Scheduler = Schedulers.newBoundedElastic(
    Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
    Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE + maxPendingOperationsPerJVM,
    TRANSACTIONAL_BULK_WRITER_RESPONSES_BOUNDED_ELASTIC_THREAD_NAME,
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
