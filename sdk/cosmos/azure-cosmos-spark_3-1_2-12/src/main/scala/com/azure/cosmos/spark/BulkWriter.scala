// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.guava25.base.Preconditions
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.spark.BulkWriter.MaxNumberOfThreadsPerCPUCore
import com.azure.cosmos.{BulkOperations, CosmosAsyncContainer, CosmosBulkOperationResponse, CosmosException, CosmosItemOperation}
import com.fasterxml.jackson.databind.node.ObjectNode
import reactor.core.Disposable
import reactor.core.publisher.Sinks
import reactor.core.scala.publisher.SMono.PimpJFlux
import reactor.core.scala.publisher.{SFlux, SMono}
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger, AtomicLong, AtomicReference}
import java.util.concurrent.locks.ReentrantLock

import com.azure.cosmos.spark.BulkWriter.emitFailureHandler
import reactor.core.publisher.Sinks.EmitFailureHandler
import reactor.core.publisher.Sinks.EmitResult

import scala.collection.concurrent.TrieMap

//scalastyle:off null
//scalastyle:off multiple.string.literals
class BulkWriter(container: CosmosAsyncContainer,
                 writeConfig: CosmosWriteConfig)
  extends AsyncItemWriter
    with CosmosLoggingTrait {

  // TODO: moderakh add a mocking unit test for Bulk where CosmosClient is mocked to simulator failure/retry scenario
  logInfo("BulkWriter instantiated ....")

  // TODO: moderakh this requires tuning.
  // TODO: moderakh should we do a max on the max memory to ensure we don't run out of memory?
  private val maxConcurrency = writeConfig.maxConcurrencyOpt
    .getOrElse(SparkUtils.getNumberOfHostCPUCores * MaxNumberOfThreadsPerCPUCore)

  private val closed = new AtomicBoolean(false)
  private val lock = new ReentrantLock
  private val pendingTasksCompleted = lock.newCondition
  private val activeTasks = new AtomicInteger(0)
  private val errorCaptureFirstException = new AtomicReference[Throwable]()
  private val bulkInputEmitter: Sinks.Many[CosmosItemOperation] = Sinks.many().unicast().onBackpressureBuffer()

  // TODO: moderakh discuss the context issue in the core SDK bulk api with the team.
  // public <TContext> Flux<CosmosBulkOperationResponse<TContext>> processBulkOperations(
  //    Flux<CosmosItemOperation> operations,
  //    BulkProcessingOptions<TContext> bulkOptions)
  // trying to play further with bulk api of the core SDK for the spark integration.
  // Currently we have the above API in the core SDK and context is tied to the options instead of the input operations flux.
  // The use case of the context is to pass a context info along each individual operation in the input operations flux.
  // I think this beta public API has issues.
  // This means it is not possible to pass context per operation.
  // I think we need to change this public API to allow passing context per operation
  // TODO: moderakh once that is added in the core SDK, drop activeOperations and rely on the core SDK
  // context passing for bulk
  private val activeOperations = new TrieMap[CosmosItemOperation, OperationContext]()
  private val semaphore = new Semaphore(maxConcurrency)

  private val totalScheduledMetrics = new AtomicLong(0)
  private val totalSuccessfulIngestionMetrics = new AtomicLong(0)

  private val subscriptionDisposable: Disposable = {
    val bulkOperationResponseFlux: SFlux[CosmosBulkOperationResponse[Object]] =
      container.processBulkOperations[Object](bulkInputEmitter.asFlux()).asScala

    bulkOperationResponseFlux.subscribe(
      resp => {
        var isGettingRetried = false
        try {
          val itemOperation = resp.getOperation
          val contextOpt = activeOperations.remove(itemOperation)
          assume(contextOpt.isDefined) // can't find the operation context!
          val context = contextOpt.get

          if (resp.getException != null) {
            Option(resp.getException) match {
              case Some(cosmosException: CosmosException) => {
                logDebug(s"encountered ${cosmosException.getStatusCode}")
                if (shouldIgnore(cosmosException)) {
                  logDebug(s"for itemId=[${context.itemId}], partitionKeyValue=[${context.partitionKeyValue}], " +
                    s"ignored encountered ${cosmosException.getStatusCode}")
                  totalSuccessfulIngestionMetrics.getAndIncrement()
                  // work done
                } else if (shouldRetry(cosmosException, contextOpt.get)) {
                  // requeue
                  logWarning(s"for itemId=[${context.itemId}], partitionKeyValue=[${context.partitionKeyValue}], " +
                    s"encountered ${cosmosException.getStatusCode}, will retry! " +
                    s"attemptNumber=${context.attemptNumber}, exceptionMessage=${cosmosException.getMessage}")

                  // this is to ensure the submission will happen on a different thread in background
                  // and doesn't block the active thread
                  SMono.defer(() => {
                    scheduleWriteInternal(itemOperation.getPartitionKeyValue,
                      itemOperation.getItem.asInstanceOf[ObjectNode],
                      OperationContext(context.itemId, context.partitionKeyValue, context.attemptNumber + 1))
                    SMono.empty
                  }).subscribeOn(Schedulers.boundedElastic())
                    .subscribe()

                  isGettingRetried = true
                } else {
                  logWarning(s"for itemId=[${context.itemId}], partitionKeyValue=[${context.partitionKeyValue}], " +
                    s"encountered ${cosmosException.getStatusCode}, all retries exhausted! " +
                    s"attemptNumber=${context.attemptNumber}, exceptionMessage=${cosmosException.getMessage}")
                  captureIfFirstFailure(cosmosException)
                  cancelWork()
                }
              }
              case _ =>
                logWarning(s"unexpected failure: itemId=[${context.itemId}], partitionKeyValue=[${context.partitionKeyValue}], " +
                  s"encountered , attemptNumber=${context.attemptNumber}, exceptionMessage=${resp.getException.getMessage}", resp.getException)
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
          logError("Unexpected failure code path in Bulk ingestion", ex)
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
    if (errorCaptureFirstException.get() != null) {
      logWarning("encountered failure earlier, rejecting new work")
      throw errorCaptureFirstException.get()
    }

    semaphore.acquire()
    val cnt = totalScheduledMetrics.getAndIncrement()
    logDebug(s"total scheduled ${cnt}")

    scheduleWriteInternal(partitionKeyValue, objectNode, OperationContext(getId(objectNode), partitionKeyValue, 1))
  }

  private def scheduleWriteInternal(partitionKeyValue: PartitionKey, objectNode: ObjectNode, operationContext: OperationContext): Unit = {
    activeTasks.incrementAndGet()
    if (operationContext.attemptNumber > 1) {
      logInfo(s"bulk scheduleWrite attemptCnt: ${operationContext.attemptNumber}")
    }

    val bulkItemOperation = writeConfig.itemWriteStrategy match {
      case ItemWriteStrategy.ItemOverwrite =>
        BulkOperations.getUpsertItemOperation(objectNode, partitionKeyValue)
      case ItemWriteStrategy.ItemAppend =>
        BulkOperations.getCreateItemOperation(objectNode, partitionKeyValue)
      case _ =>
        throw new RuntimeException(s"${writeConfig.itemWriteStrategy} not supported")
    }

    activeOperations.put(bulkItemOperation, operationContext)

    // For FAIL_NON_SERIALIZED, will keep retry, while for other errors, use the default behavior
    bulkInputEmitter.emitNext(bulkItemOperation, emitFailureHandler)
  }

  // the caller has to ensure that after invoking this method scheduleWrite doesn't get invoked
  override def flushAndClose(): Unit = {
    this.synchronized{
      try {
        if (closed.get()) {
          // scalastyle:off return
          return
          // scalastyle:on return
        }

        logInfo("flushAndClose invoked")

        logInfo(s"completed so far ${totalSuccessfulIngestionMetrics.get()}, pending tasks ${activeOperations.size}")

        // error handling, if there is any error and the subscription is cancelled
        // the remaining tasks will not be processed hence we never reach activeTasks = 0
        // once we do error handling we should think how to cover the scenario.
        lock.lock()
        try {
          while (activeTasks.get() > 0 || errorCaptureFirstException.get != null) {
            pendingTasksCompleted.await()
          }
        } finally {
          lock.unlock()
        }

        logInfo("invoking bulkInputEmitter.onComplete()")
        semaphore.release(activeTasks.get())
        bulkInputEmitter.tryEmitComplete()

        // which error to report?
        if (errorCaptureFirstException.get() != null) {
          logError(s"flushAndClose throw captured error ${errorCaptureFirstException.get().getMessage}")
          throw errorCaptureFirstException.get()
        }

        assume(activeTasks.get() == 0)
        assume(activeOperations.isEmpty)
        assume(semaphore.availablePermits() == maxConcurrency)

        logInfo(s"flushAndClose completed with no error. " +
          s"totalSuccessfulIngestionMetrics=${totalSuccessfulIngestionMetrics.get()}, totalScheduled=${totalScheduledMetrics}")
        assume(totalScheduledMetrics.get() == totalSuccessfulIngestionMetrics.get)
      } finally {
        closed.set(true)
      }
    }
  }

  private def markTaskCompletion(): Unit = {
    lock.lock()
    try {
      if (activeTasks.decrementAndGet() == 0 || errorCaptureFirstException.get() != null) {
        pendingTasksCompleted.signal()
      }
    } finally {
      lock.unlock()
    }
  }

  private def captureIfFirstFailure(throwable: Throwable) = {
    logError("capture failure", throwable)
    lock.lock()
    try {
      errorCaptureFirstException.compareAndSet(null, throwable)
      pendingTasksCompleted.signal()
    } finally {
      lock.unlock()
    }
  }

  private def cancelWork(): Unit = {
    logInfo(s"cancelling remaining un process tasks ${activeTasks.get}")
    subscriptionDisposable.dispose()
  }

  private def shouldIgnore(cosmosException: CosmosException): Boolean = {
    // ignore 409 on create-item
    writeConfig.itemWriteStrategy == ItemWriteStrategy.ItemAppend &&
      Exceptions.isResourceExistsException(cosmosException)
  }

  private def shouldRetry(cosmosException: CosmosException, operationContext: OperationContext): Boolean = {
    operationContext.attemptNumber < writeConfig.maxRetryCount &&
      Exceptions.canBeTransientFailure(cosmosException)
  }

  private case class OperationContext(itemId: String, partitionKeyValue: PartitionKey, attemptNumber: Int /** starts from 1 * */)

  private def getId(objectNode: ObjectNode) = {
    val idField = objectNode.get(CosmosConstants.Properties.Id)
    assume(idField != null && idField.isTextual)
    idField.textValue()
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
  val MaxNumberOfThreadsPerCPUCore = 2 * 1024 * 167 / 16

  val emitFailureHandler: EmitFailureHandler =
        (signalType, emitResult) => if (emitResult.equals(EmitResult.FAIL_NON_SERIALIZED)) true else false
}

//scalastyle:on multiple.string.literals
//scalastyle:on null
