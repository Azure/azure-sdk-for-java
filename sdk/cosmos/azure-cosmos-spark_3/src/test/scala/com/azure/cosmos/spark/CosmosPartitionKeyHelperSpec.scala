// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.models.{PartitionKey, PartitionKeyBuilder}

class CosmosPartitionKeyHelperSpec extends UnitSpec {
  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  it should "return the correct partition key value string for single PK" in {
    val pkString = CosmosPartitionKeyHelper.getCosmosPartitionKeyValueString(List("pk1"))
    pkString shouldEqual "pk([\"pk1\"])"
  }

  it should "return the correct partition key value string for HPK" in {
    val pkString = CosmosPartitionKeyHelper.getCosmosPartitionKeyValueString(List("city1", "zip1"))
    pkString shouldEqual "pk([\"city1\",\"zip1\"])"
  }

  it should "return the correct partition key value string for 3-level HPK" in {
    val pkString = CosmosPartitionKeyHelper.getCosmosPartitionKeyValueString(List("a", "b", "c"))
    pkString shouldEqual "pk([\"a\",\"b\",\"c\"])"
  }

  it should "parse valid single PK string" in {
    val pk = CosmosPartitionKeyHelper.tryParsePartitionKey("pk([\"myPkValue\"])")
    pk.isDefined shouldBe true
    pk.get shouldEqual new PartitionKey("myPkValue")
  }

  it should "parse valid HPK string" in {
    val pk = CosmosPartitionKeyHelper.tryParsePartitionKey("pk([\"city1\",\"zip1\"])")
    pk.isDefined shouldBe true
    val expected = new PartitionKeyBuilder().add("city1").add("zip1").build()
    pk.get shouldEqual expected
  }

  it should "parse valid 3-level HPK string" in {
    val pk = CosmosPartitionKeyHelper.tryParsePartitionKey("pk([\"a\",\"b\",\"c\"])")
    pk.isDefined shouldBe true
    val expected = new PartitionKeyBuilder().add("a").add("b").add("c").build()
    pk.get shouldEqual expected
  }

  it should "roundtrip single PK" in {
    val original = "pk([\"roundtrip\"])"
    val parsed = CosmosPartitionKeyHelper.tryParsePartitionKey(original)
    parsed.isDefined shouldBe true
    val serialized = CosmosPartitionKeyHelper.getCosmosPartitionKeyValueString(List("roundtrip"))
    serialized shouldEqual original
  }

  it should "roundtrip HPK" in {
    val original = "pk([\"city\",\"zip\"])"
    val parsed = CosmosPartitionKeyHelper.tryParsePartitionKey(original)
    parsed.isDefined shouldBe true
    val serialized = CosmosPartitionKeyHelper.getCosmosPartitionKeyValueString(List("city", "zip"))
    serialized shouldEqual original
  }

  it should "return None for malformed string" in {
    val pk = CosmosPartitionKeyHelper.tryParsePartitionKey("invalid_format")
    pk.isDefined shouldBe false
  }

  it should "return None for missing pk prefix" in {
    val pk = CosmosPartitionKeyHelper.tryParsePartitionKey("[\"value\"]")
    pk.isDefined shouldBe false
  }

  it should "be case-insensitive for parsing" in {
    val pk = CosmosPartitionKeyHelper.tryParsePartitionKey("PK([\"value\"])")
    pk.isDefined shouldBe true
    pk.get shouldEqual new PartitionKey("value")
  }


  it should "return None for malformed JSON inside pk() wrapper" in {
    // Invalid JSON that would cause JsonProcessingException
    val pk = CosmosPartitionKeyHelper.tryParsePartitionKey("pk({invalid json})")
    pk.isDefined shouldBe false
  }

  it should "return None for truncated JSON inside pk() wrapper" in {
    val pk = CosmosPartitionKeyHelper.tryParsePartitionKey("pk(["unterminated)")
    pk.isDefined shouldBe false
  }

  //scalastyle:on multiple.string.literals
  //scalastyle:on magic.number
}
