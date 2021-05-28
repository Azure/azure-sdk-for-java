// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package org.apache.spark.source

import com.codahale.metrics.{Counter, Histogram, MetricRegistry}
import org.apache.spark.metrics.source.Source

class MySource extends Source {
  override val sourceName: String = "MySource"

  override val metricRegistry: MetricRegistry = new MetricRegistry

  val FOO: Histogram = metricRegistry.histogram(MetricRegistry.name("fooHistory"))
  val FOO_COUNTER: Counter = metricRegistry.counter(MetricRegistry.name("fooCounter"))
}
