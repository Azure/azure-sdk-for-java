// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

private object CosmosPredicates {
  private[this] val ParameterName = "parameterName"

  private[this] def argumentMustNotBeNullOrEmptyMessage(parameterName: String): String =
    s"Argument '$parameterName' must not be null or empty."

  private[spark] def requireNotNull[T](candidate: T, parameterName: String): T = {
    requireNotNullOrEmpty(parameterName, ParameterName)
    require(candidate != null, s"Argument '$parameterName' must not be null.")
    candidate
  }

  private[spark] def requireNotNullOrEmpty(candidate: String, parameterName: String): String = {
    require(
      parameterName != null && !parameterName.isBlank,
      argumentMustNotBeNullOrEmptyMessage(ParameterName))
    require(candidate != null, argumentMustNotBeNullOrEmptyMessage(parameterName))
    candidate
  }

  private[spark] def assertNotNullOrEmpty(candidate: String, parameterName: String): String = {
    assert(
      parameterName != null && !parameterName.isBlank,
      argumentMustNotBeNullOrEmptyMessage(ParameterName))
    assert(candidate != null, argumentMustNotBeNullOrEmptyMessage(parameterName))
    candidate
  }

  private[spark] def assertNotNull[T](candidate: T, parameterName: String): T = {
    assertNotNullOrEmpty(parameterName, ParameterName)
    assert(candidate != null, s"Argument '$parameterName' must not be null.")
    candidate
  }

  private[spark] def requireNotNullOrEmpty[T](candidate: Array[T], parameterName: String): Array[T] = {
    require(parameterName != null && !parameterName.isBlank, argumentMustNotBeNullOrEmptyMessage(ParameterName))
    require(candidate != null && !candidate.isEmpty, argumentMustNotBeNullOrEmptyMessage(parameterName))
    candidate
  }

  private[spark] def assertNotNullOrEmpty[T](candidate: Array[T], parameterName: String): Array[T] = {
    assert(parameterName != null && !parameterName.isBlank, argumentMustNotBeNullOrEmptyMessage(ParameterName))
    assert(candidate != null && !candidate.isEmpty, argumentMustNotBeNullOrEmptyMessage(parameterName))
    candidate
  }
}
