// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.fasterxml.jackson.core.JsonParseException

import java.util.UUID

class ChangeFeedOffsetSpec extends UnitSpec {
  private[this] def getOffsetJson(changeFeedState: String) = {
    String.format(
      "{" +
        "\"id\":\"com.azure.cosmos.spark.changeFeed.offset.v1\"," +
        "\"state\":\"%s\"" +
        "}",
      changeFeedState)
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
    offset.isEmpty shouldEqual false
    val serializedString = offset.get.json()
    //scalastyle:off null
    serializedString should not be null
    //scalastyle:on null
    serializedString shouldEqual offsetJson
  }

  it should " pass serialization/deserialization test for json with unnecessary whitespaces" in {
    val changeFeedState = UUID.randomUUID().toString
    val offsetJson = getOffsetJson(changeFeedState)
    val offsetJsonWithWhitespaces = getOffsetJsonWithUnnecessaryWhitespaces(changeFeedState)
    val offset = ChangeFeedOffset.fromJson(offsetJsonWithWhitespaces)
    offset.isEmpty shouldEqual false
    val serializedString = offset.get.json()
    //scalastyle:off null
    serializedString should not be null
    //scalastyle:on null
    serializedString shouldEqual offsetJson
  }

  it should " should ignore valid json for different offset" in {
    val changeFeedState = UUID.randomUUID().toString
    val offsetJson = getOffsetJson(changeFeedState).replace(
      "com.azure.cosmos.spark.changeFeed.offset.v1",
      "com.azure.cosmos.spark.changeFeed.offset.v356"
    )
    val offset = ChangeFeedOffset.fromJson(offsetJson)
    offset.isEmpty shouldEqual true
  }

  it should "complain when parsing invalid json" in {
    val invalidJson = UUID.randomUUID().toString

    try {
      ChangeFeedOffset.fromJson(invalidJson)
      fail("invalid json")
    } catch {
      case e: JsonParseException =>
    }
  }
  //scalastyle:on multiple.string.literals
}
