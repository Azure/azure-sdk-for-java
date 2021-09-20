// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

// scalastyle:off underscore.import
import com.azure.cosmos.{models, _}
import com.azure.cosmos.models.{CosmosBulkExecutionOptions, CosmosBulkExecutionThresholdsState, CosmosBulkItemRequestOptions, CosmosBulkOperations}

import scala.collection.mutable
import scala.concurrent.duration.Duration
// scalastyle:on underscore.import
import com.azure.cosmos.implementation.ImplementationBridgeHelpers
import com.azure.cosmos.implementation.guava25.base.Preconditions
import com.azure.cosmos.implementation.spark.{OperationContextAndListenerTuple, OperationListener}
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.spark.BulkWriter.{DefaultMaxPendingOperationPerCore, emitFailureHandler}
import com.azure.cosmos.spark.diagnostics.{DiagnosticsContext, DiagnosticsLoader, LoggerHelper, SparkTaskContext}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.TaskContext
import reactor.core.Disposable
import reactor.core.publisher.Sinks
import reactor.core.publisher.Sinks.{EmitFailureHandler, EmitResult}
import reactor.core.scala.publisher.SMono.PimpJFlux
import reactor.core.scala.publisher.{SFlux, SMono}
import reactor.core.scheduler.Schedulers

import java.util.UUID
import java.util.concurrent.{Semaphore, TimeUnit}
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger, AtomicLong, AtomicReference}
import java.util.concurrent.locks.ReentrantLock
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

//scalastyle:off null
//scalastyle:off multiple.string.literals
class BulkWriter(container: CosmosAsyncContainer,
                 writeConfig: CosmosWriteConfig,
                 diagnosticsConfig: DiagnosticsConfig)
  extends AsyncItemWriter {

  private val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)

  // TODO: moderakh add a mocking unit test for Bulk where CosmosClient is mocked to simulator failure/retry scenario

  // TODO: moderakh this requires tuning.
  // TODO: moderakh should we do a max on the max memory to ensure we don't run out of memory?
  private val cpuCount = SparkUtils.getNumberOfHostCPUCores
  private val maxPendingOperations = writeConfig.bulkMaxPendingOperations
    .getOrElse(math.max(cpuCount, 1) * DefaultMaxPendingOperationPerCore)
  log.logInfo(
    s"BulkWriter instantiated (CPU count: ${cpuCount}, maxPendingOperations: ${maxPendingOperations} ...")

  private val closed = new AtomicBoolean(false)
  private val lock = new ReentrantLock
  private val pendingTasksCompleted = lock.newCondition
  private val activeTasks = new AtomicInteger(0)
  private val errorCaptureFirstException = new AtomicReference[Throwable]()
  private val bulkInputEmitter: Sinks.Many[models.CosmosItemOperation] = Sinks.many().unicast().onBackpressureBuffer()

  // TODO: fabianm - remove this later
  // Leaving activeOperations here primarily for debugging purposes (for example in case of hangs)
  private val activeOperations = java.util.concurrent.ConcurrentHashMap.newKeySet[models.CosmosItemOperation]().asScala
  private val semaphore = new Semaphore(maxPendingOperations)

  private val totalScheduledMetrics = new AtomicLong(0)
  private val totalSuccessfulIngestionMetrics = new AtomicLong(0)

  private val cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions(BulkWriter.bulkProcessingThresholds)
  private val operationContext = initializeOperationContext()

  private def initializeOperationContext(): SparkTaskContext = {
    val taskContext = TaskContext.get

    val diagnosticsContext: DiagnosticsContext = DiagnosticsContext(UUID.randomUUID().toString, "BulkWriter")

    if (taskContext != null) {
      val taskDiagnosticsContext = SparkTaskContext(diagnosticsContext.correlationActivityId,
        taskContext.stageId(),
        taskContext.partitionId(),
        taskContext.taskAttemptId(),
        "")

      val listener: OperationListener =
        DiagnosticsLoader.getDiagnosticsProvider(diagnosticsConfig).getLogger(this.getClass)

      val operationContextAndListenerTuple = new OperationContextAndListenerTuple(taskDiagnosticsContext, listener)
      ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper
        .getCosmosBulkExecutionOptionsAccessor
        .setOperationContext(cosmosBulkExecutionOptions, operationContextAndListenerTuple)

      taskDiagnosticsContext
    } else{
      SparkTaskContext(diagnosticsContext.correlationActivityId,
        -1,
        -1,
        -1,
        "")
    }
  }

  private val subscriptionDisposable: Disposable = {
    val bulkOperationResponseFlux: SFlux[models.CosmosBulkOperationResponse[Object]] =
      container
          .executeBulkOperations[Object](
            bulkInputEmitter.asFlux(),
            cosmosBulkExecutionOptions)
          .asScala

    bulkOperationResponseFlux.subscribe(
      resp => {
        var isGettingRetried = false
        try {
          val itemOperation = resp.getOperation
          val itemOperationFound = activeOperations.remove(itemOperation)
          assume(itemOperationFound) // can't find the item operation in list of active operations!
          val context = itemOperation.getContext[OperationContext]

          if (resp.getException != null) {
            Option(resp.getException) match {
              case Some(cosmosException: CosmosException) =>
                log.logDebug(s"encountered ${cosmosException.getStatusCode}, Context: ${operationContext.toString}")
                if (shouldIgnore(cosmosException)) {
                  log.logDebug(s"for itemId=[${context.itemId}], partitionKeyValue=[${context.partitionKeyValue}], " +
                    s"ignored encountered ${cosmosException.getStatusCode}, Context: ${operationContext.toString}")
                  totalSuccessfulIngestionMetrics.getAndIncrement()
                  // work done
                } else if (shouldRetry(cosmosException, context)) {
                  // requeue
                  log.logWarning(s"for itemId=[${context.itemId}], partitionKeyValue=[${context.partitionKeyValue}], " +
                    s"encountered ${cosmosException.getStatusCode}, will retry! " +
                    s"attemptNumber=${context.attemptNumber}, exceptionMessage=${cosmosException.getMessage}, Context: {${operationContext.toString}}")

                  // this is to ensure the submission will happen on a different thread in background
                  // and doesn't block the active thread
                  val deferredRetryMono = SMono.defer(() => {
                      scheduleWriteInternal(itemOperation.getPartitionKeyValue,
                        itemOperation.getItem.asInstanceOf[ObjectNode],
                        OperationContext(context.itemId, context.partitionKeyValue, context.eTag, context.attemptNumber + 1))
                      SMono.empty
                    })

                  if (Exceptions.isTimeout(cosmosException)) {
                    deferredRetryMono
                      .delaySubscription(
                        Duration(
                          scala.util.Random.nextInt(BulkWriter.maxDelayOn408RequestTimeoutInMs),
                          TimeUnit.MILLISECONDS),
                        Schedulers.boundedElastic())
                      .subscribeOn(Schedulers.boundedElastic())
                      .subscribe()

                  } else {
                    deferredRetryMono
                      .subscribeOn(Schedulers.boundedElastic())
                      .subscribe()
                  }

                  isGettingRetried = true
                } else {
                  log.logWarning(s"for itemId=[${context.itemId}], partitionKeyValue=[${context.partitionKeyValue}], " +
                    s"encountered ${cosmosException.getStatusCode}, all retries exhausted! " +
                    s"attemptNumber=${context.attemptNumber}, exceptionMessage=${cosmosException.getMessage}, Context: {${operationContext.toString}")
                  captureIfFirstFailure(cosmosException)
                  cancelWork()
                }
              case _ =>
                log.logWarning(s"unexpected failure: itemId=[${context.itemId}], partitionKeyValue=[${context.partitionKeyValue}], " +
                  s"encountered , attemptNumber=${context.attemptNumber}, exceptionMessage=${resp.getException.getMessage}, " +
                  s"Context: ${operationContext.toString}", resp.getException)
                captureIfFirstFailure(resp.getException)
                cancelWork()
            }
          } else {
            // no error case
            totalSuccessfulIngestionMetrics.getAndIncrement()
          }

        }
        finally {
          if (!isGettingRetried) {
            semaphore.release()
          }
        }

        markTaskCompletion()
      },
      errorConsumer = Option.apply(
        ex => {
          log.logError(s"Unexpected failure code path in Bulk ingestion, Context: ${operationContext.toString}", ex)
          // if there is any failure this closes the bulk.
          // at this point bulk api doesn't allow any retrying
          // we don't know the list of failed item-operations
          // they only way to retry to keep a dictionary of pending operations outside
          // so we know which operations failed and which ones can be retried.
          // TODO: moderakh discuss the bulk API in the core SDK.
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

    var acquisitionAttempt = 0
    val operationContext = OperationContext(getId(objectNode), partitionKeyValue, getETag(objectNode), 1)
    var numberOfIntervalsWithIdenticalActiveOperationSnapshots = new AtomicLong(0)
    // Don't clone the activeOperations for the first iteration
    // to reduce perf impact before the Semaphore has been acquired
    // this means if the semaphore can't be acquired within 1 minute
    // the first attempt will always assume it wasn't stale - so effectively we
    // allow staleness for one additional minute - which is perfectly fine
    var activeOperationsSnapshot = mutable.Set.empty[models.CosmosItemOperation]
    while (!semaphore.tryAcquire(1, TimeUnit.MINUTES)) {
      if (subscriptionDisposable.isDisposed) {
        captureIfFirstFailure(
          new IllegalStateException("Can't accept any new work - BulkWriter has been disposed already"));
      }

      throwIfProgressStaled(
        "Semaphore acquisition",
        activeOperationsSnapshot,
        numberOfIntervalsWithIdenticalActiveOperationSnapshots)

      activeOperationsSnapshot = activeOperations.clone()
    }

    val cnt = totalScheduledMetrics.getAndIncrement()
    log.logDebug(s"total scheduled $cnt, Context: ${operationContext.toString}")

    scheduleWriteInternal(partitionKeyValue, objectNode, operationContext)
  }

  private def scheduleWriteInternal(partitionKeyValue: PartitionKey, objectNode: ObjectNode, operationContext: OperationContext): Unit = {
    activeTasks.incrementAndGet()
    if (operationContext.attemptNumber > 1) {
      log.logInfo(s"bulk scheduleWrite attemptCnt: ${operationContext.attemptNumber}, Context: ${operationContext.toString}")
    }

    val bulkItemOperation = writeConfig.itemWriteStrategy match {
      case ItemWriteStrategy.ItemOverwrite =>
        CosmosBulkOperations.getUpsertItemOperation(objectNode, partitionKeyValue, operationContext)
      case ItemWriteStrategy.ItemAppend =>
        CosmosBulkOperations.getCreateItemOperation(objectNode, partitionKeyValue, operationContext)
      case ItemWriteStrategy.ItemDelete =>
        CosmosBulkOperations.getDeleteItemOperation(operationContext.itemId, partitionKeyValue, operationContext)
      case ItemWriteStrategy.ItemDeleteIfNotModified =>
        CosmosBulkOperations.getDeleteItemOperation(
          operationContext.itemId,
          partitionKeyValue,
          operationContext.eTag match {
            case Some(eTag) => new CosmosBulkItemRequestOptions().setIfMatchETag(eTag)
            case _ =>  new CosmosBulkItemRequestOptions()
          },
          operationContext)
      case _ =>
        throw new RuntimeException(s"${writeConfig.itemWriteStrategy} not supported")
    }

    activeOperations.add(bulkItemOperation)

    // For FAIL_NON_SERIALIZED, will keep retry, while for other errors, use the default behavior
    bulkInputEmitter.emitNext(bulkItemOperation, emitFailureHandler)
  }

  private[this] def throwIfCapturedExceptionExists(): Unit = {
    val errorSnapshot = errorCaptureFirstException.get()
    if (errorSnapshot != null) {
      log.logError(s"throw captured error ${errorSnapshot.getMessage}, " +
        s"Context: ${operationContext.toString}")
      throw errorSnapshot
    }
  }

  private[this] def getActiveOperationsLog(activeOperationsSnapshot: mutable.Set[models.CosmosItemOperation]): String = {
    val sb = new StringBuilder()

    activeOperationsSnapshot
      .take(BulkWriter.maxItemOperationsToShowInErrorMessage)
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

  private[this] def throwIfProgressStaled
  (
    operationName: String,
    activeOperationsSnapshot: mutable.Set[models.CosmosItemOperation],
    numberOfIntervalsWithIdenticalActiveOperationSnapshots: AtomicLong
  ) = {

    val operationsLog = getActiveOperationsLog(activeOperationsSnapshot)

    if (activeOperationsSnapshot.equals(activeOperations)) {
      numberOfIntervalsWithIdenticalActiveOperationSnapshots.incrementAndGet()
      log.logWarning(
        s"${operationName} has been waiting ${numberOfIntervalsWithIdenticalActiveOperationSnapshots} " +
          s"times for identical set of operations: ${operationsLog} Context: ${operationContext.toString}"
      )
    } else {
      numberOfIntervalsWithIdenticalActiveOperationSnapshots.set(0)
      log.logInfo(
        s"${operationName} is waiting for active operations: ${operationsLog} Context: ${operationContext.toString}"
      )
    }

    if (numberOfIntervalsWithIdenticalActiveOperationSnapshots.get >= BulkWriter.maxAllowedMinutesWithoutAnyProgress) {

      captureIfFirstFailure(
        new IllegalStateException(
          s"Stale bulk ingestion identified in ${operationName} - the following active operations have not been " +
            s"completed (first ${BulkWriter.maxItemOperationsToShowInErrorMessage} shown) or progressed after " +
            s"${BulkWriter.maxAllowedMinutesWithoutAnyProgress} minutes: $operationsLog"
        ))
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
          log.logInfo(s"flushAndClose invoked, Context: ${operationContext.toString}")
          log.logInfo(s"completed so far ${totalSuccessfulIngestionMetrics.get()}, " +
            s"pending tasks ${activeOperations.size}, Context: ${operationContext.toString}")

          // error handling, if there is any error and the subscription is cancelled
          // the remaining tasks will not be processed hence we never reach activeTasks = 0
          // once we do error handling we should think how to cover the scenario.
          lock.lock()
          try {
            var numberOfIntervalsWithIdenticalActiveOperationSnapshots = new AtomicLong(0)
            var activeTasksSnapshot = activeTasks.get()
            while (activeTasksSnapshot > 0 && errorCaptureFirstException.get == null) {
              log.logInfo(
                s"Waiting for pending activeTasks $activeTasksSnapshot, Context: ${operationContext.toString}")
              val activeOperationsSnapshot = activeOperations.clone()
              val awaitCompleted = pendingTasksCompleted.await(1, TimeUnit.MINUTES)
              if (!awaitCompleted) {
                throwIfProgressStaled(
                  "FlushAndClose",
                  activeOperationsSnapshot,
                  numberOfIntervalsWithIdenticalActiveOperationSnapshots
                )
              }
              activeTasksSnapshot = activeTasks.get()
              val semaphoreAvailablePermitsSnapshot = semaphore.availablePermits()


              if (awaitCompleted) {
                log.logInfo(s"Waiting completed for pending activeTasks $activeTasksSnapshot, " +
                  s"Context: ${operationContext.toString}")
              } else {
                log.logInfo(s"Waiting interrupted for pending activeTasks $activeTasksSnapshot - " +
                  s"available permits ${semaphoreAvailablePermitsSnapshot}, " +
                  s"Context: ${operationContext.toString}")
              }
            }

            log.logInfo(s"Waiting completed for pending activeTasks $activeTasksSnapshot, " +
              s"Context: ${operationContext.toString}")
          } finally {
            lock.unlock()
          }

          log.logInfo(s"invoking bulkInputEmitter.onComplete(), Context: ${operationContext.toString}")
          semaphore.release(activeTasks.get())
          bulkInputEmitter.tryEmitComplete()

          throwIfCapturedExceptionExists()

          assume(activeTasks.get() == 0)
          assume(activeOperations.isEmpty)
          assume(semaphore.availablePermits() == maxPendingOperations)
          log.logInfo(s"flushAndClose completed with no error. " +
            s"totalSuccessfulIngestionMetrics=${totalSuccessfulIngestionMetrics.get()}, " +
            s"totalScheduled=$totalScheduledMetrics, Context: ${operationContext.toString}")
          assume(totalScheduledMetrics.get() == totalSuccessfulIngestionMetrics.get)
        }
      } finally {
        subscriptionDisposable.dispose();
        closed.set(true)
      }
    }
  }
  // scalastyle:on method.length
  // scalastyle:on cyclomatic.complexity

  private def markTaskCompletion(): Unit = {
    lock.lock()
    try {
      val activeTasksLeftSnapshot = activeTasks.decrementAndGet()
      val exceptionSnapshot = errorCaptureFirstException.get()
      if (activeTasksLeftSnapshot == 0 || exceptionSnapshot != null) {
        log.logDebug(s"markTaskCompletion, Active tasks left: $activeTasksLeftSnapshot, " +
          s"error: $exceptionSnapshot, Context: ${operationContext.toString}")
        pendingTasksCompleted.signal()
      }
    } finally {
      lock.unlock()
    }
  }

  private def captureIfFirstFailure(throwable: Throwable): Unit = {
    log.logError(s"capture failure, Context: {${operationContext.toString}}", throwable)
    lock.lock()
    try {
      errorCaptureFirstException.compareAndSet(null, throwable)
      pendingTasksCompleted.signal()
    } finally {
      lock.unlock()
    }
  }

  private def cancelWork(): Unit = {
    log.logInfo(s"cancelling remaining unprocessed tasks ${activeTasks.get}, " +
      s"Context: ${operationContext.toString}")
    subscriptionDisposable.dispose()
  }

  private def shouldIgnore(cosmosException: CosmosException): Boolean = {
    writeConfig.itemWriteStrategy match {
      case ItemWriteStrategy.ItemAppend => Exceptions.isResourceExistsException(cosmosException)
      case ItemWriteStrategy.ItemDelete => Exceptions.isNotFoundExceptionCore(cosmosException)
      case ItemWriteStrategy.ItemDeleteIfNotModified => Exceptions.isNotFoundExceptionCore(cosmosException) ||
        Exceptions.isPreconditionFailedException(cosmosException)
      case _ => false
    }
  }

  private def shouldRetry(cosmosException: CosmosException, operationContext: OperationContext): Boolean = {
    operationContext.attemptNumber < writeConfig.maxRetryCount &&
      Exceptions.canBeTransientFailure(cosmosException)
  }

  private case class OperationContext
  (
    itemId: String,
    partitionKeyValue: PartitionKey,
    eTag: Option[String],
    attemptNumber: Int /** starts from 1 * */)

  private def getId(objectNode: ObjectNode) = {
    val idField = objectNode.get(CosmosConstants.Properties.Id)
    assume(idField != null && idField.isTextual)
    idField.textValue()
  }

  private def getETag(objectNode: ObjectNode) = {
    val eTagField = objectNode.get(CosmosConstants.Properties.ETag)
    if (eTagField != null && eTagField.isTextual) {
      Some(eTagField.textValue())
    } else {
      None
    }
  }

  /**
   * Don't wait for any remaining work but signal to the writer the ungraceful close
   * Should not throw any exceptions
   */
  override def abort(): Unit = {
    // signal an exception that will be thrown for any pending work/flushAndClose if no other exception has
    // been registered
    captureIfFirstFailure(
      new IllegalStateException(s"The Spark task was aborted, Context: ${operationContext.toString}"))
    cancelWork()
  }
}

private object BulkWriter {
  //scalastyle:off magic.number
  val maxDelayOn408RequestTimeoutInMs = 5000
  val maxItemOperationsToShowInErrorMessage = 10

  // we used to use 15 minutes here - extending it because of several incidents where
  // backend returned 420/3088 (ThrottleDueToSplit) for >= 30 minutes
  // UPDATE - reverting back to 15 minutes - causing an unreasonably large delay/hang
  // due to a backend issue doesn't sound right for most customers (helpful during my own
  // long stress runs - but for customers 15 minutes is more reasonable)
  // UPDATE - TODO @fabianm - with 15 minutes the end-to-end sample fails too often - because the extensive 429/3088
  // intervals are around 2 hours. So I need to increase this threshold for now again - will move it
  // to 45 minutes - and when I am back from vacation will drive an investigation to improve the
  // end-to-end behavior on 429/3088 with the backend and monitoring teams.
  val maxAllowedMinutesWithoutAnyProgress = 45
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
        (_, emitResult) => if (emitResult.equals(EmitResult.FAIL_NON_SERIALIZED)) true else false

  val bulkProcessingThresholds = new CosmosBulkExecutionThresholdsState()
}

//scalastyle:on multiple.string.literals
//scalastyle:on null
