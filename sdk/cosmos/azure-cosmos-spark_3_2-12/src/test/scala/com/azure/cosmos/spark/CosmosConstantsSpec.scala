// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

class CosmosConstantsSpec extends UnitSpec {
  "CurrentVersion" should "not be null" in {
    CosmosConstants.currentVersion == null shouldBe false
    CosmosConstants.currentVersion.startsWith("4.") shouldBe true
  }

  "CurrentName" should "not be null" in {
    CosmosConstants.currentName  == null shouldBe false
    CosmosConstants.currentName.startsWith("azure-cosmos-spark") shouldBe true
  }

  "UserAgentSuffix" should "combine name and version" in {
    CosmosConstants.userAgentSuffix shouldBe
      s"SparkConnector|${CosmosConstants.currentName}|${CosmosConstants.currentVersion}"
  }

  "ContainerFeedRangeConfig" should "be able to get feed range refresh interval" in {
    CosmosConstants.ContainerFeedRangeConfigs.FeedRangeRefreshIntervalInMinutes shouldEqual 2L

    System.setProperty("spark.cosmos.feedRange.refreshInterval.minutes", "10")
    CosmosConstants.ContainerFeedRangeConfigs.FeedRangeRefreshIntervalInMinutes shouldEqual 10L
    System.clearProperty("spark.cosmos.feedRange.refreshInterval.minutes")
  }
}
