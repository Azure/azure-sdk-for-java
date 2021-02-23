// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.{BulkOperations, CosmosAsyncContainer, CosmosBulkOperationResponse, CosmosException, CosmosItemOperation}
import com.fasterxml.jackson.databind.node.ObjectNode
import reactor.core.Disposable
import reactor.core.publisher.EmitterProcessor
import reactor.core.scala.publisher.SFlux
import reactor.core.scala.publisher.SMono.PimpJFlux

import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}
import java.util.concurrent.locks.ReentrantLock
import scala.collection.concurrent.TrieMap

//scalastyle:off null
class BulkWriter(container: CosmosAsyncContainer,
                 writeConfig: CosmosWriteConfig) {

  private val activeTasks = new AtomicInteger(0)
  private val lock = new ReentrantLock
  private val pendingTasksCompleted = lock.newCondition
  private val errorCaptureFirstException = new AtomicReference[Throwable]()
  private val bulkInputEmitter: EmitterProcessor[CosmosItemOperation] = EmitterProcessor.create[CosmosItemOperation]()
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

  private val subscriptionDisposable: Disposable = {
    val bulkOperationResponseFlux: SFlux[CosmosBulkOperationResponse[Object]] =
      container.processBulkOperations[Object](bulkInputEmitter).asScala

    bulkOperationResponseFlux.subscribe(
      resp => {
        val itemOperation = resp.getOperation
        val contextOpt = activeOperations.remove(itemOperation)
        assume(contextOpt.isDefined) // can't find the operation context!

        if (resp.getException != null && !shouldIgnore(resp.getException)) {
          if (shouldRetry(resp.getException, contextOpt.get)) {
            scheduleWriteInternal(itemOperation.getPartitionKeyValue,
              itemOperation.getItem.asInstanceOf[ObjectNode],
              OperationContext(contextOpt.get.attemptNumber + 1))
          } else {
            captureIfFirstFailure(resp.getException)
            cancelWork()
          }
        }
        markTaskCompletion()
      },
      errorConsumer = Option.apply(
        ex => {
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


  def scheduleWrite(partitionKeyValue: PartitionKey, objectNode: ObjectNode): Unit = {
    scheduleWriteInternal(partitionKeyValue, objectNode, OperationContext(1))
  }

  private def scheduleWriteInternal(partitionKeyValue: PartitionKey, objectNode: ObjectNode, operationContext: OperationContext): Unit = {
    val bulkItemOperation = writeConfig.itemWriteStrategy match {
      case ItemWriteStrategy.ItemOverwrite =>
        BulkOperations.getUpsertItemOperation(objectNode, partitionKeyValue)
      case ItemWriteStrategy.ItemAppend =>
        BulkOperations.getCreateItemOperation(objectNode, partitionKeyValue)
      case _ =>
        throw new RuntimeException(s"${writeConfig.itemWriteStrategy} not supported")
    }

    activeTasks.incrementAndGet()

    activeOperations.put(bulkItemOperation, operationContext)
    bulkInputEmitter.onNext(bulkItemOperation)
  }

  // the caller has to ensure that after invoking this method scheduleWrite doesn't get invoked
  def flushAndClose(): Unit = {
    bulkInputEmitter.onComplete()

    // error handling, if there is any error and the subscription is cancelled
    // the remaining tasks will not be processed hence we never reach activeTasks = 0
    // once we do error handling we should think how to cover the scenario.

    lock.lock()
    try {
      while (activeTasks.get() > 0 || errorCaptureFirstException.get() != null) {
        pendingTasksCompleted.await()
      }
    } finally {
      lock.unlock()
    }

    // which error to report?
    if (errorCaptureFirstException.get() != null) {
      throw errorCaptureFirstException.get()
    }
  }

  private def markTaskCompletion() : Unit = {
    lock.lock()
    try {
      if (activeTasks.decrementAndGet() == 0) {
        pendingTasksCompleted.signal()
      }
    } finally {
      lock.unlock()
    }
  }

  private def captureIfFirstFailure(throwable: Throwable) = {
    errorCaptureFirstException.compareAndSet(null, throwable)
  }

  private def cancelWork(): Unit = {
    subscriptionDisposable.dispose()
  }

  private def shouldIgnore(exception: Exception) : Boolean = {
    // ignore 409 on create-item
    writeConfig.itemWriteStrategy == ItemWriteStrategy.ItemAppend &&
      exception.isInstanceOf[CosmosException] &&
      Exceptions.isResourceExistsException(exception.asInstanceOf[CosmosException])
  }

  private def shouldRetry(exception: Exception, operationContext: OperationContext) : Boolean = {
    operationContext.attemptNumber < writeConfig.maxRetryCount &&
      exception.isInstanceOf[CosmosException] &&
      Exceptions.canBeTransientFailure(exception.asInstanceOf[CosmosException])
  }

  private case class OperationContext(val attemptNumber: Int /** starts from 1 **/)
}
//scalastyle:on null
