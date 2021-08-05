// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.Strings
import org.apache.spark.SparkEnv

private object CosmosPredicates {
  private[this] val ParameterName = "parameterName"

  /**
   * Executor id for the driver.  In earlier versions of Spark, this was `<driver>`, but this was
   * changed to `driver` because the angle brackets caused escaping issues in URLs and XML (see
   * SPARK-6716 for more details).
   */
  private[this] val DRIVER_IDENTIFIER = "driver"

  private[this] def argumentMustNotBeNullOrEmptyMessage(parameterName: String): String =
    s"Argument '$parameterName' must not be null or empty."

  private[spark] def requireNotNull[T](candidate: T, parameterName: String): T = {
    requireNotNullOrEmpty(parameterName, ParameterName)
    require(candidate != null, s"Argument '$parameterName' must not be null.")
    candidate
  }

  private[spark] def requireNotNullOrEmpty(candidate: String, parameterName: String): String = {
    require(
      !Strings.isNullOrWhiteSpace(parameterName),
      argumentMustNotBeNullOrEmptyMessage(ParameterName))
    require(!Strings.isNullOrWhiteSpace(candidate), argumentMustNotBeNullOrEmptyMessage(parameterName))
    candidate
  }

  private[spark] def assertNotNullOrEmpty(candidate: String, parameterName: String): String = {
    assert(
      !Strings.isNullOrWhiteSpace(parameterName),
      argumentMustNotBeNullOrEmptyMessage(ParameterName))
    assert(!Strings.isNullOrWhiteSpace(candidate), argumentMustNotBeNullOrEmptyMessage(parameterName))
    candidate
  }

  private[spark] def assertNotNull[T](candidate: T, parameterName: String): T = {
    assertNotNullOrEmpty(parameterName, ParameterName)
    assert(candidate != null, s"Argument '$parameterName' must not be null.")
    candidate
  }

  private[spark] def requireNotNullOrEmpty[T](candidate: Array[T], parameterName: String): Array[T] = {
    require(!Strings.isNullOrWhiteSpace(parameterName), argumentMustNotBeNullOrEmptyMessage(ParameterName))
    require(candidate != null && !candidate.isEmpty, argumentMustNotBeNullOrEmptyMessage(parameterName))
    candidate
  }

  private[spark] def assertNotNullOrEmpty[T](candidate: Array[T], parameterName: String): Array[T] = {
    assert(!Strings.isNullOrWhiteSpace(parameterName), argumentMustNotBeNullOrEmptyMessage(ParameterName))
    assert(candidate != null && !candidate.isEmpty, argumentMustNotBeNullOrEmptyMessage(parameterName))
    candidate
  }

  private[spark] def assertOnSparkDriver(): Unit = {
    // assert that we're only accessing it on the driver.
    assert(SparkEnv.get.executorId == DRIVER_IDENTIFIER, "This code should only be executed on the Spark driver.")
  }
}
