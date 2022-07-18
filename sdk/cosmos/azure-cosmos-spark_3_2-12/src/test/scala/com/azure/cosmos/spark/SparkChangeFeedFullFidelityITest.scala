package com.azure.cosmos.spark

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
import com.azure.cosmos.models.{CosmosChangeFeedRequestOptions, FeedRange}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.fasterxml.jackson.databind.node.ObjectNode

class SparkChangeFeedFullFidelityITest
  extends IntegrationSpec
    with Spark
    with CosmosClient
//    with CosmosContainerWithRetention
    with BasicLoggingTrait {

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  override def afterEach(): Unit = {
//    this.reinitializeContainer()
  }

//  "spark change feed query (full fidelity)" can "use default schema" in {
//
//    val container = cosmosClient.getDatabase("SampleDatabase").getContainer("GreenTaxiRecords")
//
//    var cosmosChangeFeedRequestOptions = CosmosChangeFeedRequestOptions
//      .createForProcessingFromNow(FeedRange.forFullRange()).fullFidelity()
//
//    var feedResponseIterator = container.queryChangeFeed(cosmosChangeFeedRequestOptions, classOf[ObjectNode])
//      .byPage().toIterable.iterator()
//    var continuationToken = ""
//
//    while (continuationToken != null) {
//      var feedResponse = feedResponseIterator.next()
//      println(s"FeedResponse results are ", feedResponse.getResults)
//      continuationToken = feedResponse.getContinuationToken
//      cosmosChangeFeedRequestOptions = CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(continuationToken)
//      feedResponseIterator = container.queryChangeFeed(cosmosChangeFeedRequestOptions, classOf[ObjectNode])
//        .byPage().toIterable.iterator()
//    }
//  }
  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals

  "spark change feed query (full fidelity)" can "use default schema" in {

    val container = cosmosClient.getDatabase("SampleDatabase").getContainer("GreenTaxiRecords")

    var cosmosChangeFeedRequestOptions = CosmosChangeFeedRequestOptions
      .createForProcessingFromBeginning(FeedRange.forFullRange())

    var feedResponseIterator = container.queryChangeFeed(cosmosChangeFeedRequestOptions, classOf[ObjectNode])
      .byPage().toIterable.iterator()
    var continuationToken = ""

    while (continuationToken != null) {
      var feedResponse = feedResponseIterator.next()
      println(s"FeedResponse results are ", feedResponse.getResults)
      continuationToken = feedResponse.getContinuationToken
      cosmosChangeFeedRequestOptions = CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(continuationToken)
      feedResponseIterator = container.queryChangeFeed(cosmosChangeFeedRequestOptions, classOf[ObjectNode])
        .byPage().toIterable.iterator()
    }
  }
}
