// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.spark.udf.{GetBucketForPartitionKey, GetFeedRangeForHierarchicalPartitionKeyValues, GetFeedRangesForBuckets}
import org.apache.spark.sql.types._

import java.util.UUID

class FeedRangesForBucketsITest
  extends IntegrationSpec
    with SparkWithDropwizardAndSlf4jMetrics
    with CosmosClient
    with AutoCleanableCosmosContainerWithSubpartitions
    with BasicLoggingTrait
    with MetricAssertions {

  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  override def afterEach(): Unit = {
    this.reinitializeContainer()
  }

  "feed ranges" can "can be split into different buckets" in {
    spark.udf.register("GetFeedRangesForBuckets", new GetFeedRangesForBuckets(), ArrayType(StringType))
    val pkDefinition = "{\"paths\":[\"/id\"],\"kind\":\"Hash\"}"
    var dummyDf = spark.sql(s"SELECT GetFeedRangesForBuckets('$pkDefinition', 5)")
    val expectedFeedRanges = Array("-05C1C9CD673398", "05C1C9CD673398-05C1D9CD673398",
      "05C1D9CD673398-05C1E399CD6732", "05C1E399CD6732-05C1E9CD673398", "05C1E9CD673398-FF")
    val feedRange = dummyDf
      .collect()(0)
      .getList[String](0)
      .toArray

    logInfo(s"FeedRange from UDF: $feedRange")
    assert(feedRange.sameElements(expectedFeedRanges), "Feed ranges do not match the expected values")

//    spark.udf.register("GetBucketForPartitionKey", new GetBucketForPartitionKey(), IntegerType)
//    dummyDf = spark.sql(s"SELECT GetBucketForPartitionKey('$pkDefinition', 4979ea4a-6ba6-42ee-b9e6-1f5bf996a01f, '$feedRange')")
//    val bucket = dummyDf.collect()(0).getInt(0)
//    assert(bucket == 0, "Bucket does not match the expected value")


  }

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}

