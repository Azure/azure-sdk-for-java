// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncContainer
import com.azure.cosmos.implementation.{SparkBridgeImplementationInternal, Utils}
import com.azure.cosmos.models.CosmosQueryRequestOptions
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.spark.udf.{GetBucketForPartitionKey, GetFeedRangesForBuckets}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.sql.types.{ArrayType, IntegerType, StringType}

import java.util.UUID
import scala.collection.mutable

class FeedRangesForBucketsITest
  extends IntegrationSpec
    with SparkWithDropwizardAndSlf4jMetrics
    with CosmosClient
    with CosmosContainer
    with BasicLoggingTrait
    with MetricAssertions {

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  override def afterEach(): Unit = {
    this.reinitializeContainer()
  }

  "feed ranges" can "be split into different buckets" in {
    spark.udf.register("GetFeedRangesForBuckets", new GetFeedRangesForBuckets(), ArrayType(StringType))
    var pkDefinition = "{\"paths\":[\"/id\"],\"kind\":\"Hash\"}"
    val dummyDf = spark.sql(s"SELECT GetFeedRangesForBuckets('$pkDefinition', 5)")
    val expectedFeedRanges = Array("-05C1C9CD673398", "05C1C9CD673398-05C1D9CD673398",
      "05C1D9CD673398-05C1E399CD6732", "05C1E399CD6732-05C1E9CD673398", "05C1E9CD673398-FF")
    val feedRange = dummyDf
      .collect()(0)
      .getList[String](0)
      .toArray

    assert(feedRange.sameElements(expectedFeedRanges), "Feed ranges do not match the expected values")
    val lastId = "45170a78-eac0-4d3a-be5e-9b00bb5f4649"

    var bucket = new GetBucketForPartitionKey().call(pkDefinition, lastId, expectedFeedRanges)
    assert(bucket == 0, "Bucket does not match the expected value")

    // test with hpk partition key definition
    pkDefinition = "{\"paths\":[\"/tenantId\",\"/userId\",\"/sessionId\"],\"kind\":\"MultiHash\"}"
    val pkValues = "[\"" + lastId + "\"]"

    bucket = new GetBucketForPartitionKey().call(pkDefinition, pkValues, expectedFeedRanges)
    assert(bucket == 4, "Bucket does not match the expected value")

  }

  "feed ranges" can "be converted into buckets for new partition key" in {
    feedRangesForBuckets(false)
  }

  "feed ranges" can "be converted into buckets for new hierarchical partition key" in {
    feedRangesForBuckets(true)
  }

  def feedRangesForBuckets(hpk: Boolean): Unit = {
    val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
    val docs = createItems(container, 50, hpk)

    spark.udf.register("GetFeedRangesForBuckets", new GetFeedRangesForBuckets(), ArrayType(StringType))
    val pkDefinition = if (hpk) {"{\"paths\":[\"/tenantId\",\"/userId\",\"/sessionId\"],\"kind\":\"MultiHash\"}"}
    else {"{\"paths\":[\"/id\"],\"kind\":\"Hash\"}"}

    val dummyDf = spark.sql(s"SELECT GetFeedRangesForBuckets('$pkDefinition', 5)")
    val feedRanges = dummyDf
      .collect()(0)
      .getList[String](0)
      .toArray(new Array[String](0))

    spark.udf.register("GetBucketForPartitionKey", new GetBucketForPartitionKey(), IntegerType)
    val bucketToDocsMap = mutable.Map[Int, List[ObjectNode]]().withDefaultValue(List())

    for (doc <- docs) {
      val lastId = if (!hpk) doc.get("id").asText() else "[\"" + doc.get("tenantId").asText() + "\"]"
      val bucket = new GetBucketForPartitionKey().call(pkDefinition, lastId, feedRanges)
      // Add the document to the corresponding bucket in the map
      bucketToDocsMap(bucket) = doc :: bucketToDocsMap(bucket)
    }

    for (i <- feedRanges.indices) {
      val range = SparkBridgeImplementationInternal.toCosmosRange(feedRanges(i))
      val feedRange = SparkBridgeImplementationInternal.toFeedRange(SparkBridgeImplementationInternal.rangeToNormalizedRange(range))
      val requestOptions = new CosmosQueryRequestOptions().setFeedRange(feedRange)
      container.queryItems("SELECT * FROM c", requestOptions, classOf[ObjectNode]).byPage().collectList().block().forEach { rsp =>
        val results = rsp.getResults
        var numDocs = 0
        val expectedResults = bucketToDocsMap(i)
        results.forEach(doc => {
          assert(expectedResults.collect({
            case expectedDoc if expectedDoc.get("id").asText() == doc.get("id").asText() => expectedDoc
          }).size >= 0, "Document not found in the expected bucket")
          numDocs += 1
        })
        assert(numDocs == results.size(), "Number of documents in the bucket does not match the number of docs for that feed range")
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

