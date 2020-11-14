// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Inside, Inspectors, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

abstract class UnitSpec extends AnyFlatSpec
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with Matchers
  with OptionValues
  with Inside
  with Inspectors
