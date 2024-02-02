// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.models.{PartitionKey, PartitionKeyBuilder}

class CosmosItemIdentityHelperSpec extends UnitSpec {
  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  it should "return the correct cosmos item identity string" in {
    val itemIdentityString = CosmosItemIdentityHelper.getCosmosItemIdentityValueString("1", List("1"))
    itemIdentityString shouldEqual "id(1).pk([\"1\"])"

    // subpartitionKey
    val subpartitionKeyBuilder = new PartitionKeyBuilder()
    subpartitionKeyBuilder.add("1")
    subpartitionKeyBuilder.add("2")
    val itemIdentityStringForSubPartitionKey = CosmosItemIdentityHelper.getCosmosItemIdentityValueString("1", List("1", "2"))
    itemIdentityStringForSubPartitionKey shouldEqual "id(1).pk([\"1\",\"2\"])"
  }

  it should "parse valid cosmos item identity string value" in {
    val cosmosItemIdentity = CosmosItemIdentityHelper.tryParseCosmosItemIdentity("id(1).pk([\"1\"])")
    cosmosItemIdentity.isDefined shouldBe true
    cosmosItemIdentity.get.getId shouldEqual "1"
    val expectedPartitionKey = new PartitionKey("1")
    cosmosItemIdentity.get.getPartitionKey shouldEqual expectedPartitionKey

    val cosmosItemIdentityWithSubPartitionKey = CosmosItemIdentityHelper.tryParseCosmosItemIdentity("id(1).pk([\"1\",\"2\"])")
    cosmosItemIdentityWithSubPartitionKey.isDefined shouldBe true
    cosmosItemIdentityWithSubPartitionKey.get.getId shouldEqual "1"
    val subPartitionKeyBuilder = new PartitionKeyBuilder()
    subPartitionKeyBuilder.add("1")
    subPartitionKeyBuilder.add("2")
    cosmosItemIdentityWithSubPartitionKey.get.getPartitionKey shouldEqual subPartitionKeyBuilder.build()
  }

  it should "not throw exceptions if the cosmos item identity string value is in wrong format" in {
    val cosmosItemIdentity = CosmosItemIdentityHelper.tryParseCosmosItemIdentity("id(1)pk([\"1\"])")
    cosmosItemIdentity.isDefined shouldBe false
  }

  //scalastyle:on multiple.string.literals
  //scalastyle:on magic.number
}
