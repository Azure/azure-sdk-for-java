// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.models.{CosmosItemRequestOptions, PartitionKey}
import com.azure.cosmos.{CosmosAsyncContainer, CosmosException}
import com.fasterxml.jackson.databind.node.ObjectNode

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{Callable, CompletableFuture, Executors}
import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Success, Try}

// scalastyle:off underscore.import
import scala.compat.java8.FutureConverters._
// scalastyle:on underscore.import

class PointWriter(container: CosmosAsyncContainer, cosmosWriteConfig: CosmosWriteConfig)
  extends AsyncItemWriter
    with CosmosLoggingTrait {


  // TODO: moderakh executorService needs to get closed, otherwise we will have resource leak
  private val executorService = Executors.newFixedThreadPool(cosmosWriteConfig.maxConcurrency)
  private val capturedFailure = new AtomicReference[Throwable]()
  private val pendingPointWrites = new TrieMap[Future[Unit], Boolean]()

  def scheduleWrite(partitionKeyValue: PartitionKey, objectNode: ObjectNode): Unit = {
    cosmosWriteConfig.itemWriteStrategy match {
      case ItemWriteStrategy.ItemOverwrite => upsertWithRetryAsync(partitionKeyValue, objectNode)
      case ItemWriteStrategy.ItemAppend => createWithRetryAsync(partitionKeyValue, objectNode)
    }
  }

  def flushAndClose() {
    for ((future, _) <- pendingPointWrites.snapshot()) {
      Try(Await.result(future, Duration.Inf))
    }
  }

  // scalastyle:off return
  private def createWithRetryAsync(partitionKeyValue: PartitionKey,
                                   objectNode: ObjectNode): Unit = {

    val promise = Promise[Unit]()
    pendingPointWrites.put(promise.future, true)

    executeAsync(() => createWithRetry(partitionKeyValue, objectNode))
      .onComplete {
        case Success(value) => {
          promise.success(Unit)
          pendingPointWrites.remove(promise.future)
        }
        case Failure(e) => {
          promise.failure(e)
          capturedFailure.set(e)
          pendingPointWrites.remove(promise.future)
        }
      }
  }

  private def upsertWithRetryAsync(partitionKeyValue: PartitionKey,
                                   objectNode: ObjectNode): Unit = {
    val promise = Promise[Unit]()
    pendingPointWrites.put(promise.future, true)

    executeAsync(() => upsertWithRetry(partitionKeyValue, objectNode))
      .onComplete {
        case Success(value) => {
          promise.success(Unit)
          pendingPointWrites.remove(promise.future)
        }
        case Failure(e) => {
          promise.failure(e)
          capturedFailure.set(e)
          pendingPointWrites.remove(promise.future)
        }
      }
  }

  private def createWithRetry(partitionKeyValue: PartitionKey,
                              objectNode: ObjectNode): Unit = {

    var exceptionOpt = Option.empty[Exception]
    for (attempt <- 1 to cosmosWriteConfig.maxRetryCount + 1) {
      try {
        // TODO: moderakh, there is room for further improvement by making this code nonblocking
        // using reactive stream retry pattern
        container.createItem(objectNode, partitionKeyValue, new CosmosItemRequestOptions()).block()
        return
      } catch {
        case e: CosmosException if Exceptions.isResourceExistsException(e) =>
          // TODO: what should we do on unique index violation? should we ignore or throw?
          // TODO moderakh we need to add log messages extract identifier (id, pk) and log
          return
        case e: CosmosException if Exceptions.canBeTransientFailure(e) =>
          logWarning(
            s"create item attempt #$attempt max remaining retries"
              + s"${cosmosWriteConfig.maxRetryCount + 1 - attempt}, encountered ${e.getMessage}")
          exceptionOpt = Option.apply(e)
      }
    }

    assert(exceptionOpt.isDefined)
    throw exceptionOpt.get
  }

  private def upsertWithRetry(partitionKeyValue: PartitionKey,
                              objectNode: ObjectNode): Unit = {
    var exceptionOpt = Option.empty[Exception]
    for (attempt <- 1 to cosmosWriteConfig.maxRetryCount + 1) {

      try {
        // TODO: moderakh, there is room for further improvement by making this code nonblocking
        // using reactive stream retry pattern
        container.upsertItem(objectNode, partitionKeyValue, new CosmosItemRequestOptions()).block()
        return
      } catch {
        case e: CosmosException if Exceptions.canBeTransientFailure(e) =>
          logWarning(
            s"upsert item attempt #$attempt max remaining retries "
              + s"${cosmosWriteConfig.maxRetryCount + 1 - attempt}, encountered ${e.getMessage}")
          exceptionOpt = Option.apply(e)
      }
    }

    assert(exceptionOpt.isDefined)
    throw exceptionOpt.get
  }
  // scalastyle:on return

  private def executeAsync(work: () => Any) : Future[Unit] = {
    val future = new CompletableFuture[Unit]()
    executorService.submit(new Callable[Unit] {
      override def call(): Unit = {
        try {
          work()
          future.complete(Unit)
        } catch {
          case e: Exception => {
            future.completeExceptionally(e)
          }
        }
      }
    })
    future.toScala
  }
}