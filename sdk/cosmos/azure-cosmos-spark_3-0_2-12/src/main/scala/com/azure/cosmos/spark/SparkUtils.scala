// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import java.util.concurrent.{Executors, ThreadFactory}

object SparkUtils {

  def daemonThreadFactory(): ThreadFactory = {
    new ThreadFactory() {
      override def newThread(r: Runnable): Thread = {
        val t = Executors.defaultThreadFactory.newThread(r)
        t.setDaemon(true)
        t
      }
    }
  }
}
