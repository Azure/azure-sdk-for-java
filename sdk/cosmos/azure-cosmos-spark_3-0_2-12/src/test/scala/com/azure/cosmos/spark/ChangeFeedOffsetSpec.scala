// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.fasterxml.jackson.core.JsonParseException

import java.util.UUID

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class ChangeFeedOffsetSpec extends UnitSpec {
  private[this] def getOffsetJson(changeFeedState: String) = {
    // scala style rule flaky - even complaining on partial log messages
    // scalastyle:off multiple.string.literals
    String.format(
      "{" +
        "\"id\":\"com.azure.cosmos.spark.changeFeed.offset.v1\"," +
        "\"state\":\"%s\"" +
        "}",
      changeFeedState)
    // scalastyle:on multiple.string.literals
  }

  private[this] def getOffsetJsonWithInputPartitions
  (
    changeFeedState: String,
    partitions: Array[CosmosInputPartition]
  ) = {
    // scala style rule flaky - even complaining on partial log messages
    // scalastyle:off multiple.string.literals
    String.format(
      "{" +
        "\"id\":\"com.azure.cosmos.spark.changeFeed.offset.v1\"," +
        "\"state\":\"%s\"," +
        "\"partitions\":[%s]" +
        "}",
      changeFeedState,
      String.join(",", partitions.map(p => raw"""${p.json()}""" ).toList.asJava)
    )
    // scalastyle:on multiple.string.literals
  }

  private[this] def getOffsetJsonWithUnnecessaryWhitespaces(changeFeedState: String) = {
    String.format(
      "{" +
        "\"id\": \"com.azure.cosmos.spark.changeFeed.offset.v1\", " +
        "\"state\": \"%s\"" +
        "}",
      changeFeedState)
  }

  //scalastyle:off multiple.string.literals
  it should " pass serialization/deserialization test" in {
    val changeFeedState = UUID.randomUUID().toString
    val offsetJson = getOffsetJson(changeFeedState)
    val offset = ChangeFeedOffset.fromJson(offsetJson)
    //scalastyle:off null
    offset should not be null
    val serializedString = offset.json()
    serializedString should not be null
    //scalastyle:on null
    serializedString shouldEqual offsetJson
  }

  //scalastyle:off multiple.string.literals
  it should " pass serialization/deserialization test with Input partitions" in {
    val changeFeedState = UUID.randomUUID().toString
    val partitions = new Array[CosmosInputPartition](3)
    // scalastyle:off magic.number
    partitions(0) = CosmosInputPartition(NormalizedRange("", "AA"), Some(99L), Some(UUID.randomUUID().toString))
    partitions(1) = CosmosInputPartition(NormalizedRange("AA", "BB"), None, None)
    partitions(2) = CosmosInputPartition(NormalizedRange("BB", "FF"), None, Some(UUID.randomUUID().toString))
    // scalastyle:on magic.number
    val offsetJson = getOffsetJsonWithInputPartitions(changeFeedState, partitions)
    val offset = ChangeFeedOffset.fromJson(offsetJson)
    //scalastyle:off null
    offset should not be null
    val serializedString = offset.json()
    serializedString should not be null
    //scalastyle:on null
    serializedString shouldEqual offsetJson
  }

  it should " pass serialization/deserialization test for json with unnecessary whitespaces" in {
    val changeFeedState = UUID.randomUUID().toString
    val offsetJson = getOffsetJson(changeFeedState)
    val offsetJsonWithWhitespaces = getOffsetJsonWithUnnecessaryWhitespaces(changeFeedState)
    val offset = ChangeFeedOffset.fromJson(offsetJsonWithWhitespaces)
    //scalastyle:off null
    offset should not be null
    val serializedString = offset.json()

    serializedString should not be null
    //scalastyle:on null
    serializedString shouldEqual offsetJson
  }

  it should " complain on valid but incompatible json for different offset" in {
    val changeFeedState = UUID.randomUUID().toString
    val offsetJson = getOffsetJson(changeFeedState).replace(
      "com.azure.cosmos.spark.changeFeed.offset.v1",
      "com.azure.cosmos.spark.changeFeed.offset.v356"
    )

    try {
      ChangeFeedOffset.fromJson(offsetJson)
      fail("Invalid version never get here.")
    } catch {
      case _:IllegalArgumentException =>
    }
  }

  it should "complain when parsing invalid json" in {
    val invalidJson = UUID.randomUUID().toString

    try {
      ChangeFeedOffset.fromJson(invalidJson)
      fail("invalid json")
    } catch {
      case _:JsonParseException =>
    }
  }
  //scalastyle:on multiple.string.literals
}
