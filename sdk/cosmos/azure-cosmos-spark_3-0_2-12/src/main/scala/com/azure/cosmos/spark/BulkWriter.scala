// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.{BulkOperations, CosmosAsyncContainer, CosmosBulkOperationResponse, CosmosItemOperation}
import com.fasterxml.jackson.databind.node.ObjectNode
import reactor.core.Disposable
import reactor.core.publisher.EmitterProcessor
import reactor.core.scala.publisher.SFlux
import reactor.core.scala.publisher.SMono.PimpJFlux

import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}
import java.util.concurrent.locks.ReentrantLock

//scalastyle:off null
case class BulkWriter(container: CosmosAsyncContainer,
                      writeConfig: CosmosWriteConfig) {

  private val activeTasks = new AtomicInteger(0)
  private val lock = new ReentrantLock
  private val pendingTasksCompleted = lock.newCondition
  private val errorCaptureFirstException = new AtomicReference[Throwable]()
  private val bulkInputEmitter: EmitterProcessor[CosmosItemOperation] = EmitterProcessor.create[CosmosItemOperation]()
  
  // TODO: moderakh implement retry
  // TODO: moderakh handle 409

  private val subscriptionDisposable: Disposable = {
    val bulkOperationResponseFlux: SFlux[CosmosBulkOperationResponse[Object]] =
      container.processBulkOperations[Object](bulkInputEmitter).asScala

    bulkOperationResponseFlux.subscribe(
      resp => {
        resp.getBatchContext
        if (resp.getException != null) {
          // TODO: moderakh we should retry
          captureIfFirstFailure(resp.getException)
          cancelWork()
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
          captureIfFirstFailure(ex)
          cancelWork()
          markTaskCompletion()
        }
      )
    )
  }

  def scheduleWrite(partitionKeyValue: PartitionKey, objectNode: ObjectNode): Unit = {
    val bulkItemOperation = writeConfig.itemWriteStrategy match {
      case ItemWriteStrategy.ItemOverwrite =>
        BulkOperations.getUpsertItemOperation(objectNode, partitionKeyValue)

      // TODO moderakh add support for non upsert mode in bulk
      case _ =>
        throw new RuntimeException(s"${writeConfig.itemWriteStrategy} not supported")
    }

    activeTasks.incrementAndGet()
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
}
//scalastyle:on null
