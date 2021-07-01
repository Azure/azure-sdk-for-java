// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Inside, Inspectors, OptionValues}

abstract class IntegrationSpec extends AnyFlatSpec
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with Matchers
  with OptionValues
  with Inside
  with Inspectors
