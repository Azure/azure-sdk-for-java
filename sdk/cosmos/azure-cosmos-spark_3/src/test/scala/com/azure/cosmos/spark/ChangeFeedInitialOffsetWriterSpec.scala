// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

class ChangeFeedInitialOffsetWriterSpec extends UnitSpec {

  "validateVersion" should "return version for valid version string within supported range" in {
    ChangeFeedInitialOffsetWriter.validateVersion("v1", 1) shouldBe 1
  }

  it should "return version when version is less than max supported" in {
    ChangeFeedInitialOffsetWriter.validateVersion("v1", 5) shouldBe 1
  }

  it should "return version when version equals max supported" in {
    ChangeFeedInitialOffsetWriter.validateVersion("v3", 3) shouldBe 3
  }

  it should "throw IllegalStateException for version exceeding max supported" in {
    val exception = intercept[IllegalStateException] {
      ChangeFeedInitialOffsetWriter.validateVersion("v2", 1)
    }
    exception.getMessage should include("UnsupportedLogVersion")
    exception.getMessage should include("v1")
    exception.getMessage should include("v2")
  }

  it should "throw IllegalStateException for non-numeric version" in {
    val exception = intercept[IllegalStateException] {
      ChangeFeedInitialOffsetWriter.validateVersion("vabc", 1)
    }
    exception.getMessage should include("malformed")
  }

  it should "throw IllegalStateException for empty string" in {
    val exception = intercept[IllegalStateException] {
      ChangeFeedInitialOffsetWriter.validateVersion("", 1)
    }
    exception.getMessage should include("malformed")
  }

  it should "throw IllegalStateException for string without v prefix" in {
    val exception = intercept[IllegalStateException] {
      ChangeFeedInitialOffsetWriter.validateVersion("1", 1)
    }
    exception.getMessage should include("malformed")
  }

  it should "throw IllegalStateException for v0 (zero version)" in {
    val exception = intercept[IllegalStateException] {
      ChangeFeedInitialOffsetWriter.validateVersion("v0", 1)
    }
    exception.getMessage should include("malformed")
  }

  it should "throw IllegalStateException for negative version" in {
    val exception = intercept[IllegalStateException] {
      ChangeFeedInitialOffsetWriter.validateVersion("v-1", 1)
    }
    exception.getMessage should include("malformed")
  }

  it should "throw IllegalStateException for version string with only v" in {
    val exception = intercept[IllegalStateException] {
      ChangeFeedInitialOffsetWriter.validateVersion("v", 1)
    }
    exception.getMessage should include("malformed")
  }
}
