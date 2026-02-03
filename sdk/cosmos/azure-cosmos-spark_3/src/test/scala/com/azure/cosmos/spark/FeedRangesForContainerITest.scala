// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncContainer
import com.azure.cosmos.implementation.{SparkBridgeImplementationInternal, TestConfigurations, Utils}
import com.azure.cosmos.models.CosmosQueryRequestOptions
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.spark.udf.{GetFeedRangesForContainer, GetOverlappingFeedRange}
import com.fasterxml.jackson.databind.node.ObjectNode

import java.util.UUID
import scala.collection.mutable

class FeedRangesForContainerITest
  extends IntegrationSpec
    with SparkWithMetrics
    with CosmosClient
    with CosmosContainer
    with BasicLoggingTrait
    with MetricAssertions {

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  override def afterEach(): Unit = {
    this.reinitializeContainer()
  }

  "feed ranges" can "be split into different sub feed ranges" in {

    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val cfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
    )
    var pkDefinition = "{\"paths\":[\"/id\"],\"kind\":\"Hash\"}"
    val feedRanges = new GetFeedRangesForContainer().call(cfg, Option(5))
    val expectedFeedRanges = Array("-05C1C9CD673398", "05C1C9CD673398-05C1D9CD673398",
      "05C1D9CD673398-05C1E399CD6732", "05C1E399CD6732-05C1E9CD673398", "05C1E9CD673398-FF")



    assert(feedRanges.sameElements(expectedFeedRanges), "Feed ranges do not match the expected values")
    val lastId = "45170a78-eac0-4d3a-be5e-9b00bb5f4649"

    var feedRangeResult = new GetOverlappingFeedRange().call(pkDefinition, lastId, expectedFeedRanges)
    assert(feedRangeResult == "-05C1C9CD673398", "feed range does not match the expected value")

    // test with hpk partition key definition
    pkDefinition = "{\"paths\":[\"/tenantId\",\"/userId\",\"/sessionId\"],\"kind\":\"MultiHash\"}"
    val pkValues = "[\"" + lastId + "\"]"

    feedRangeResult = new GetOverlappingFeedRange().call(pkDefinition, pkValues, expectedFeedRanges)
    assert(feedRangeResult == "05C1E9CD673398-FF", "feed range does not match the expected value")

  }

  "feed ranges" can "be mapped to new partition key" in {
    feedRangesForPK(false)
  }

  "feed ranges" can "be mapped to new hierarchical partition key" in {
    feedRangesForPK(true)
  }

  def feedRangesForPK(hpk: Boolean): Unit = {

    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val docs = createItems(container, 50, hpk)
    val cfg = Map(
      "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
      "spark.cosmos.accountKey" -> cosmosMasterKey,
      "spark.cosmos.database" -> cosmosDatabase,
      "spark.cosmos.container" -> cosmosContainer,
    )

    val pkDefinition = if (hpk) {"{\"paths\":[\"/tenantId\",\"/userId\",\"/sessionId\"],\"kind\":\"MultiHash\"}"}
    else {"{\"paths\":[\"/id\"],\"kind\":\"Hash\"}"}

    val feedRanges = new GetFeedRangesForContainer().call(cfg, Option(5))

    val feedRangeToDocsMap = mutable.Map[String, List[ObjectNode]]().withDefaultValue(List())

    for (doc <- docs) {
      val lastId = if (!hpk) doc.get("id").asText() else "[\"" + doc.get("tenantId").asText() + "\"]"
      val feedRange = new GetOverlappingFeedRange().call(pkDefinition, lastId, feedRanges)
      // Add the document to the corresponding feed range in the map
      feedRangeToDocsMap(feedRange) = doc :: feedRangeToDocsMap(feedRange)
    }

    for (i <- feedRanges.indices) {
      val range = SparkBridgeImplementationInternal.toCosmosRange(feedRanges(i))
      val feedRange = SparkBridgeImplementationInternal.toFeedRange(SparkBridgeImplementationInternal.rangeToNormalizedRange(range))
      val requestOptions = new CosmosQueryRequestOptions().setFeedRange(feedRange)
      container.queryItems("SELECT * FROM c", requestOptions, classOf[ObjectNode]).byPage().collectList().block().forEach { rsp =>
        val results = rsp.getResults
        var numDocs = 0
        val expectedResults = feedRangeToDocsMap(feedRanges(i))
        results.forEach(doc => {
          assert(expectedResults.collect({
            case expectedDoc if expectedDoc.get("id").asText() == doc.get("id").asText() => expectedDoc
          }).size >= 0, "Document not found in the expected feed range")
          numDocs += 1
        })
        assert(numDocs == results.size(), "Number of documents in the target feed range does not match the number of docs for that feed range")
      }
    }
  }

  def createItems(container: CosmosAsyncContainer, numOfItems: Int, hpk: Boolean): Array[ObjectNode] = {
    val docs = new Array[ObjectNode](numOfItems)
    for (sequenceNumber <- 1 to numOfItems) {
      val lastId = UUID.randomUUID().toString
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's cat")
      objectNode.put("type", "cat")
      objectNode.put("age", 20)
      objectNode.put("sequenceNumber", sequenceNumber)
      objectNode.put("id", lastId)
      if (hpk) {
        objectNode.put("tenantId", lastId)
        objectNode.put("userId", "userId1")
        objectNode.put("sessionId", "sessionId1")
      }
      docs(sequenceNumber - 1) = objectNode
      container.createItem(objectNode).block()
    }
    docs
  }

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}

