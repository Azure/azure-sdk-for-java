// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosItemRequestOptionsHelper
import com.azure.cosmos.implementation.guava25.base.Preconditions.checkState
import com.azure.cosmos.implementation.spark.{OperationContextAndListenerTuple, OperationListener}
import com.azure.cosmos.models.{CosmosItemRequestOptions, PartitionKey}
import com.azure.cosmos.spark.PointWriter.MaxNumberOfThreadsPerCPUCore
import com.azure.cosmos.spark.diagnostics.{
  CosmosItemIdentifier,
  CreateOperation, DeleteOperation, DiagnosticsContext, DiagnosticsLoader, LoggerHelper, SparkTaskContext, UpsertOperation
}
import com.azure.cosmos.{CosmosAsyncContainer, CosmosException}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.TaskContext

import java.util.UUID
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import java.util.concurrent.{Callable, CompletableFuture, ExecutorService, SynchronousQueue, ThreadPoolExecutor, TimeUnit}
import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Success, Try}

// scalastyle:off underscore.import
import scala.compat.java8.FutureConverters._
// scalastyle:on underscore.import

class PointWriter(container: CosmosAsyncContainer, cosmosWriteConfig: CosmosWriteConfig, diagnosticsConfig: DiagnosticsConfig, taskContext: TaskContext)
  extends AsyncItemWriter {

  @transient private val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)

  private val maxConcurrency = cosmosWriteConfig.pointMaxConcurrency
    .getOrElse(SparkUtils.getNumberOfHostCPUCores * MaxNumberOfThreadsPerCPUCore)

  // TODO: moderakh do perf tuning on the maxConcurrency and also the thread pool config
  val executorService: ExecutorService = new ThreadPoolExecutor(
    maxConcurrency,
    maxConcurrency,
    0L,
    TimeUnit.MILLISECONDS,
    // A synchronous queue does not have any internal capacity, not even a capacity of one.
    new SynchronousQueue(),
    SparkUtils.daemonThreadFactory(),
    // if all worker threads are busy,
    // this policy makes the caller thread execute the task.
    // This provides a simple feedback control mechanism that will slow down the rate that new tasks are submitted.
    new ThreadPoolExecutor.CallerRunsPolicy()
  )

  private val capturedFailure = new AtomicReference[Throwable]()
  private val pendingPointWrites = new TrieMap[Future[Unit], Boolean]()
  private val closed = new AtomicBoolean(false)

  private val diagnosticsContext: DiagnosticsContext = DiagnosticsContext(UUID.randomUUID().toString, "PointWriter")

  private  val taskDiagnosticsContext = SparkTaskContext(diagnosticsContext.correlationActivityId,
    taskContext.stageId(),
    taskContext.partitionId(),
    taskContext.taskAttemptId(),
    "PointWriter")

  override def scheduleWrite(partitionKeyValue: PartitionKey, objectNode: ObjectNode): Unit = {
    checkState(!closed.get())

    cosmosWriteConfig.itemWriteStrategy match {
      case ItemWriteStrategy.ItemOverwrite => upsertWithRetryAsync(partitionKeyValue, objectNode)
      case ItemWriteStrategy.ItemAppend => createWithRetryAsync(partitionKeyValue, objectNode)
      case ItemWriteStrategy.ItemDelete =>
        deleteWithRetryAsync(partitionKeyValue, objectNode, false)
      case ItemWriteStrategy.ItemDeleteIfNotModified =>
        deleteWithRetryAsync(partitionKeyValue, objectNode, true)
    }
  }

  // scalastyle:off return
  override def flushAndClose() : Unit = {
    this.synchronized {
      try {
        if (!closed.compareAndSet(false, true)) {
          return
        }

        for ((future, _) <- pendingPointWrites.snapshot()) {
          Try(Await.result(future, Duration.Inf))
        }
      } finally {
        executorService.shutdown()
      }
    }
  }

  private def createWithRetryAsync(partitionKeyValue: PartitionKey,
                                   objectNode: ObjectNode): Unit = {

    val promise = Promise[Unit]()
    pendingPointWrites.put(promise.future, true)

    val createOperation = CreateOperation(taskDiagnosticsContext,
      CosmosItemIdentifier(objectNode.get(CosmosConstants.Properties.Id).asText(), partitionKeyValue))

    executeAsync(() => createWithRetry(partitionKeyValue, objectNode, createOperation))
      .onComplete {
        case Success(_) =>
          promise.success(Unit)
          pendingPointWrites.remove(promise.future)
          log.logItemWriteCompletion(createOperation)
        case Failure(e) =>
          promise.failure(e)
          captureIfFirstFailure(e)
          log.logItemWriteFailure(createOperation, e)
          pendingPointWrites.remove(promise.future)
      }
  }

  private def upsertWithRetryAsync(partitionKeyValue: PartitionKey,
                                   objectNode: ObjectNode): Unit = {
    val promise = Promise[Unit]()
    pendingPointWrites.put(promise.future, true)

    val upsertOperation = UpsertOperation(taskDiagnosticsContext,
      CosmosItemIdentifier(objectNode.get(CosmosConstants.Properties.Id).asText(), partitionKeyValue))

    executeAsync(() => upsertWithRetry(partitionKeyValue, objectNode, upsertOperation))
      .onComplete {
        case Success(_) =>
          promise.success(Unit)
          pendingPointWrites.remove(promise.future)
          log.logItemWriteCompletion(upsertOperation)
        case Failure(e) =>
          promise.failure(e)
          captureIfFirstFailure(e)
          pendingPointWrites.remove(promise.future)
          log.logItemWriteFailure(upsertOperation, e)
      }
  }

  private def deleteWithRetryAsync(partitionKeyValue: PartitionKey,
                                   objectNode: ObjectNode,
                                   onlyIfNotModified: Boolean): Unit = {

    val promise = Promise[Unit]()
    pendingPointWrites.put(promise.future, true)

    val deleteOperation = DeleteOperation(taskDiagnosticsContext,
      CosmosItemIdentifier(objectNode.get(CosmosConstants.Properties.Id).asText(), partitionKeyValue))

    executeAsync(() => deleteWithRetry(partitionKeyValue, objectNode, onlyIfNotModified, deleteOperation))
      .onComplete {
        case Success(_) =>
          promise.success(Unit)
          pendingPointWrites.remove(promise.future)
          log.logItemWriteCompletion(deleteOperation)
        case Failure(e) =>
          promise.failure(e)
          captureIfFirstFailure(e)
          pendingPointWrites.remove(promise.future)
          log.logItemWriteFailure(deleteOperation, e)
      }
  }

  // scalastyle:off multiple.string.literals
  private def createWithRetry(partitionKeyValue: PartitionKey,
                              objectNode: ObjectNode,
                              createOperation: CreateOperation): Unit = {

    var exceptionOpt = Option.empty[Exception]
    for (attempt <- 1 to cosmosWriteConfig.maxRetryCount + 1) {
      try {
        // TODO: moderakh, there is room for further improvement by making this code nonblocking
        // using reactive stream retry pattern
        container.createItem(objectNode, partitionKeyValue, getOptions()).block()
        return
      } catch {
        case e: CosmosException if Exceptions.isResourceExistsException(e) =>
          // TODO: what should we do on unique index violation? should we ignore or throw?
          // TODO moderakh we need to add log messages extract identifier (id, pk) and log
          log.logItemWriteDetails(createOperation, "item already exists")
          return
        case e: CosmosException if Exceptions.canBeTransientFailure(e) =>
          log.logWarning(
            s"create item $createOperation attempt #$attempt max remaining retries"
              + s"${cosmosWriteConfig.maxRetryCount + 1 - attempt}, encountered ${e.getMessage}")
          exceptionOpt = Option.apply(e)
      }
    }

    assert(exceptionOpt.isDefined)
    throw exceptionOpt.get
  }

  // scalastyle:on multiple.string.literals
  private def upsertWithRetry(partitionKeyValue: PartitionKey,
                              objectNode: ObjectNode,
                              upsertOperation: UpsertOperation): Unit = {

    var exceptionOpt = Option.empty[Exception]
    for (attempt <- 1 to cosmosWriteConfig.maxRetryCount + 1) {
      try {
        // TODO: moderakh, there is room for further improvement by making this code nonblocking
        // using reactive stream retry pattern
        container.upsertItem(objectNode,
          partitionKeyValue,
          getOptions())
          .block()
        return
      } catch {
        case e: CosmosException if Exceptions.canBeTransientFailure(e) =>
          log.logWarning(
            s"upsert item $upsertOperation attempt #$attempt max remaining retries "
              + s"${cosmosWriteConfig.maxRetryCount + 1 - attempt}, encountered ${e.getMessage}")
          exceptionOpt = Option.apply(e)
      }
    }

    log.logItemWriteFailure(upsertOperation, exceptionOpt.get)
    assert(exceptionOpt.isDefined)
    exceptionOpt.get.printStackTrace()
    throw exceptionOpt.get
  }
  // scalastyle:on return

  // scalastyle:off return
  private def deleteWithRetry(partitionKeyValue: PartitionKey,
                              objectNode: ObjectNode,
                              onlyIfNotModified: Boolean,
                              deleteOperation: DeleteOperation): Unit = {

    var exceptionOpt = Option.empty[Exception]
    for (attempt <- 1 to cosmosWriteConfig.maxRetryCount + 1) {
      try {
        // TODO: moderakh, there is room for further improvement by making this code nonblocking
        // using reactive stream retry pattern
        val itemId = objectNode.get(CosmosConstants.Properties.Id).asText()

        val options = if (onlyIfNotModified) {
          getOptions()
            .setIfMatchETag(objectNode.get(CosmosConstants.Properties.ETag).asText())
        } else {
          getOptions()
        }

        container.deleteItem(itemId,
          partitionKeyValue,
          options)
          .block()
        return
      } catch {
        case e: CosmosException if Exceptions.isNotFoundExceptionCore(e) =>
          log.logItemWriteSkipped(deleteOperation, "notFound")
          return
        case e: CosmosException if Exceptions.isPreconditionFailedException(e) && onlyIfNotModified =>
          log.logItemWriteSkipped(deleteOperation, "preConditionNotMet")
          return
        case e: CosmosException if Exceptions.canBeTransientFailure(e) =>
          log.logWarning(
            s"delete item attempt #$attempt max remaining retries"
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
          case e: Exception =>
            future.completeExceptionally(e)
        }
      }
    })
    future.toScala
  }

  private def getOptions(): CosmosItemRequestOptions = {
    val options =  new CosmosItemRequestOptions()
    if (diagnosticsConfig.mode.isDefined) {
      val taskDiagnosticsContext = SparkTaskContext(
        diagnosticsContext.correlationActivityId,
        taskContext.stageId(),
        taskContext.partitionId(),
        taskContext.taskAttemptId(),
        "")

      val listener: OperationListener =
        DiagnosticsLoader.getDiagnosticsProvider(diagnosticsConfig).getLogger(this.getClass)

      val operationContextAndListenerTuple = new OperationContextAndListenerTuple(taskDiagnosticsContext, listener)
      CosmosItemRequestOptionsHelper
        .getCosmosItemRequestOptionsAccessor()
        .setOperationContext(options, operationContextAndListenerTuple)
    }
    options
  }

  /**
   * Don't wait for any remaining work but signal to the writer the ungraceful close
   * Should not throw any exceptions
   */
  override def abort(): Unit = {
    // signal an exception that will be thrown for any pending work/flushAndClose if no other exception has
    // been registered
    captureIfFirstFailure(
      new IllegalStateException(s"The Spark task was aborted, Context: ${taskDiagnosticsContext.toString}"))

    closed.set(true);

    try {
      executorService.shutdownNow()
    } catch {
      case e: Throwable =>
        log.logWarning(s"Exception when trying to shut down executor service", e)
    }
  }

  private def captureIfFirstFailure(throwable: Throwable): Unit = {
    log.logError(s"capture failure, Context: {${taskDiagnosticsContext.toString}}", throwable)
    capturedFailure.compareAndSet(null, throwable)
  }
}

private object PointWriter {
  val MaxNumberOfThreadsPerCPUCore = 50
}
