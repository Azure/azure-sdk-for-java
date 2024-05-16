// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

private[cosmos] case class NormalizedRange
(
  min: String,
  max: String
) extends Ordered[NormalizedRange] {
  override def compare(that: NormalizedRange): Int = {
    val minComparison = this.min.compareTo(that.min)

    if (minComparison == 0) {
      this.max.compareTo(that.max)
    } else {
      minComparison
    }
  }
}
