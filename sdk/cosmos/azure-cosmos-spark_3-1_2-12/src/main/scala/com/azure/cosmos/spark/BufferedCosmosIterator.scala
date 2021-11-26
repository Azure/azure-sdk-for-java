// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosDaemonThreadFactory
import com.azure.cosmos.spark.diagnostics.ILogger
import com.fasterxml.jackson.databind.node.ObjectNode

import java.util.concurrent.{ArrayBlockingQueue, Executors, TimeUnit}
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import scala.util.control.Breaks.{break, breakable}

//scalastyle:off null
private class BufferedCosmosIterator
(
  log: ILogger,
  threadName: String,
  sourceIterator: java.util.Iterator[ObjectNode],
  maxBufferedItemCount: Int
) extends java.util.Iterator[ObjectNode] {


  private val buffer = new ArrayBlockingQueue[ObjectNode](maxBufferedItemCount)
  private val queryIteratorFullyDrained = new AtomicBoolean(false)
  private val innerError = new AtomicReference[Option[Throwable]](None)
  private val executor = Executors.newSingleThreadExecutor(new CosmosDaemonThreadFactory(threadName))
  executor.submit(new QueryIteratorDrainer)

  private class QueryIteratorDrainer() extends Runnable {
    override def run(): Unit = {
      try {
        while (sourceIterator.hasNext) {
          buffer.put(sourceIterator.next())
        }

        queryIteratorFullyDrained.set(true)
      }
      catch {
        case t: Throwable =>
          log.logError("Callback QueryIteratorDrainer.run failed.", t)
          innerError.set(Some(t))
      }
    }
  }

  private def throwIfInnerErrorExists() = {
    innerError.get match {
      case Some(t) => throw t
      case None =>
    }
  }

  override def hasNext: Boolean = {
    var returnValue: Boolean = false

    breakable {
      while (true) {
        if (buffer.peek() != null) {
          returnValue = true
          break
        }

        if (queryIteratorFullyDrained.get()) {
          throwIfInnerErrorExists()
          returnValue = buffer.peek() != null
          break
        }

        Thread.sleep(CosmosConstants.bufferedIteratorPollingIntervalInMs)
        throwIfInnerErrorExists()
      }
    }

    returnValue
  }

  override def next(): ObjectNode = {
    var returnValue: ObjectNode = null

    breakable {
      while (true) {
        returnValue = buffer.poll(
          CosmosConstants.bufferedIteratorPollingIntervalInMs,
          TimeUnit.MILLISECONDS)
        if (returnValue != null) {
          break
        }

        if (queryIteratorFullyDrained.get()) {
          throwIfInnerErrorExists()
          returnValue = null
          break
        }

        throwIfInnerErrorExists()
      }
    }

    returnValue
  }
}
//scalastyle:on null