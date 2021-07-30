// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

// scalastyle:off underscore.import
import com.azure.cosmos._

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
  log.logInfo("BulkWriter instantiated ....")

  // TODO: moderakh this requires tuning.
  // TODO: moderakh should we do a max on the max memory to ensure we don't run out of memory?
  private val maxPendingOperations = writeConfig.bulkMaxPendingOperations
    .getOrElse(SparkUtils.getNumberOfHostCPUCores * DefaultMaxPendingOperationPerCore)

  private val closed = new AtomicBoolean(false)
  private val lock = new ReentrantLock
  private val pendingTasksCompleted = lock.newCondition
  private val activeTasks = new AtomicInteger(0)
  private val errorCaptureFirstException = new AtomicReference[Throwable]()
  private val bulkInputEmitter: Sinks.Many[CosmosItemOperation] = Sinks.many().unicast().onBackpressureBuffer()

  // TODO: fabianm - remove this later
  // Leaving activeOperations here primarily for debugging purposes (for example in case of hangs)
  private val activeOperations = java.util.concurrent.ConcurrentHashMap.newKeySet[CosmosItemOperation]().asScala
  private val semaphore = new Semaphore(maxPendingOperations)

  private val totalScheduledMetrics = new AtomicLong(0)
  private val totalSuccessfulIngestionMetrics = new AtomicLong(0)

  private val bulkOptions = new BulkExecutionOptions(BulkWriter.bulkProcessingThresholds)
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
        .setOperationContext(bulkOptions, operationContextAndListenerTuple)

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
    val bulkOperationResponseFlux: SFlux[CosmosBulkOperationResponse[Object]] =
      container
          .processBulkOperations[Object](
            bulkInputEmitter.asFlux(),
            bulkOptions)
          .asScala

    //scalastyle:off magic.number
    val maxDelayOn408RequestTimeoutInMs = 1000
    //scalastyle:on magic.number

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
                        Duration(scala.util.Random.nextInt(maxDelayOn408RequestTimeoutInMs), TimeUnit.MILLISECONDS),
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
    while (!semaphore.tryAcquire(1, TimeUnit.MINUTES)) {
      acquisitionAttempt += 1
      if (subscriptionDisposable.isDisposed) {
        throw new IllegalStateException("Can't accept any new work - BulkWriter has been disposed already");
      }

      log.logWarning(
        s"Semaphore acquisition attempt#${acquisitionAttempt} timed out after 1 minute. Retrying...")
    }

    val cnt = totalScheduledMetrics.getAndIncrement()
    log.logDebug(s"total scheduled $cnt, Context: ${operationContext.toString}")

    scheduleWriteInternal(partitionKeyValue, objectNode, OperationContext(getId(objectNode), partitionKeyValue, getETag(objectNode), 1))
  }

  private def scheduleWriteInternal(partitionKeyValue: PartitionKey, objectNode: ObjectNode, operationContext: OperationContext): Unit = {
    activeTasks.incrementAndGet()
    if (operationContext.attemptNumber > 1) {
      log.logInfo(s"bulk scheduleWrite attemptCnt: ${operationContext.attemptNumber}, Context: ${operationContext.toString}")
    }

    val bulkItemOperation = writeConfig.itemWriteStrategy match {
      case ItemWriteStrategy.ItemOverwrite =>
        BulkOperations.getUpsertItemOperation(objectNode, partitionKeyValue, operationContext)
      case ItemWriteStrategy.ItemAppend =>
        BulkOperations.getCreateItemOperation(objectNode, partitionKeyValue, operationContext)
      case ItemWriteStrategy.ItemDelete =>
        BulkOperations.getDeleteItemOperation(operationContext.itemId, partitionKeyValue, operationContext)
      case ItemWriteStrategy.ItemDeleteIfNotModified =>
        BulkOperations.getDeleteItemOperation(
          operationContext.itemId,
          partitionKeyValue,
          operationContext.eTag match {
            case Some(eTag) => new BulkItemRequestOptions().setIfMatchETag(eTag)
            case _ =>  new BulkItemRequestOptions()
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
            var numberOfIntervalsWithIdenticalActiveOperationSnapshots = 0
            var activeTasksSnapshot = activeTasks.get()
            while (activeTasksSnapshot > 0 && errorCaptureFirstException.get == null) {
              log.logInfo(
                s"Waiting for pending activeTasks $activeTasksSnapshot, Context: ${operationContext.toString}")
              val activeOperationsSnapshot = activeOperations.clone()
              val awaitCompleted = pendingTasksCompleted.await(1, TimeUnit.MINUTES)
              if (!awaitCompleted) {
                val sb = new StringBuilder()
                //scalastyle:off magic.number
                val maxItemOperationsToShowInErrorMessage = 10
                //scalastyle:on magic.number
                activeOperationsSnapshot
                  .take(maxItemOperationsToShowInErrorMessage)
                  .foreach(itemOperation => {
                    if (sb.nonEmpty) {
                      sb.append(", ")
                    }

                    sb.append(itemOperation.getOperationType)
                    sb.append("->")
                    val ctx = itemOperation.getContext[OperationContext]
                    sb.append(s"${ctx.partitionKeyValue}/${ctx.itemId}/${ctx.eTag}(${ctx.attemptNumber})")
                  })

                if (activeOperationsSnapshot.equals(activeOperations)) {
                  numberOfIntervalsWithIdenticalActiveOperationSnapshots += 1
                  log.logWarning(
                    s"FlushAndClose has been waiting ${numberOfIntervalsWithIdenticalActiveOperationSnapshots} " +
                      s"times for identical set of operations: ${sb} Context: ${operationContext.toString}"
                  )
                } else {
                  numberOfIntervalsWithIdenticalActiveOperationSnapshots = 0
                  log.logInfo(
                    s"FlushAndClose is waiting for active operations: ${sb} Context: ${operationContext.toString}"
                  )
                }

                if (numberOfIntervalsWithIdenticalActiveOperationSnapshots >= 15) {
                  captureIfFirstFailure(
                    new IllegalStateException(
                      s"Stale bulk ingestion identified - the following active operations have not been completed " +
                        s"(first ${maxItemOperationsToShowInErrorMessage} shown) or progressed after 15 minutes: $sb"
                  ))
                }

                throwIfCapturedExceptionExists()
              }
              activeTasksSnapshot = activeTasks.get()
              val semaphoreAvailablePermitsSnapshot = semaphore.availablePermits()
              val toBeReleased = maxPendingOperations - activeTasksSnapshot - semaphoreAvailablePermitsSnapshot
              if (toBeReleased > 0) {
                log.logWarning(s"Semaphore available Permits is ${semaphoreAvailablePermitsSnapshot} " +
                s"instead of expected ${maxPendingOperations - activeTasksSnapshot}. Releasing additional " +
                s"${toBeReleased} permits. Context: ${operationContext.toString}");
                semaphore.release(toBeReleased)
              }

              if (awaitCompleted) {
                log.logInfo(s"Waiting completed for pending activeTasks $activeTasksSnapshot, " +
                  s"Context: ${operationContext.toString}")
              } else {
                log.logInfo(s"Waiting interrupted for pending activeTasks $activeTasksSnapshot, " +
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
}

private object BulkWriter {
  // let's say the spark executor VM has 16 CPU cores.
  // let's say we have a cosmos container with 1M RU which is 167 partitions
  // let's say we are ingesting items of size 1KB
  // let's say max request size is 1MB
  // hence we want 2MB/ 1KB items per partition to be buffered
  // 2 * 1024 * 167 items should get buffered on a 16 CPU core VM
  // so per CPU core we want (2 * 1024 * 167 / 16) max items to be buffered
  val DefaultMaxPendingOperationPerCore: Int = 2 * 1024 * 167 / 16

  val emitFailureHandler: EmitFailureHandler =
        (_, emitResult) => if (emitResult.equals(EmitResult.FAIL_NON_SERIALIZED)) true else false

  val bulkProcessingThresholds = new BulkExecutionThresholds()
}

//scalastyle:on multiple.string.literals
//scalastyle:on null
