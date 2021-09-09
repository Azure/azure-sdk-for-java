// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.SparkBridgeImplementationInternal

class SparkBridgeImplementationInternalSpec extends UnitSpec {
  private[this] val rnd = scala.util.Random
  //scalastyle:off multiple.string.literals

  it should "parse multiple LSN formats" in {
    val rawLsn = rnd.nextLong()
    val asText = rawLsn.toString
    val withEscapeCharacters = "\"" + rawLsn + "\""

    SparkBridgeImplementationInternal.toLsn(asText) shouldBe rawLsn
    SparkBridgeImplementationInternal.toLsn(withEscapeCharacters) shouldBe rawLsn
  }
  //scalastyle:on multiple.string.literals
}

