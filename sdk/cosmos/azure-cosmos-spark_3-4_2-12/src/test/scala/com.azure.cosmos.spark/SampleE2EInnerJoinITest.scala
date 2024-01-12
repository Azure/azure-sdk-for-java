// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.spark.udf.GetCosmosItemIdentityValue
import com.azure.cosmos.{ConsistencyLevel, CosmosClientBuilder}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.expr
import org.apache.spark.sql.types.StringType

object SampleE2EInnerJoinITest {
    //scalastyle:off
    def main(args: Array[String]) {
        val cosmosEndpoint = TestConfigurations.HOST
        val cosmosMasterKey = TestConfigurations.MASTER_KEY
        val cosmosDatabase = "SampleDatabase"
        val cosmosContainer = "GreenTaxiRecords"

        val client = new CosmosClientBuilder()
            .endpoint(cosmosEndpoint)
            .key(cosmosMasterKey)
            .consistencyLevel(ConsistencyLevel.EVENTUAL)
            .buildAsyncClient()

        client.createDatabaseIfNotExists(cosmosDatabase).block()
        client.getDatabase(cosmosDatabase).createContainerIfNotExists(cosmosContainer, "/id").block()
        client.close()

        val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
            "spark.cosmos.accountKey" -> cosmosMasterKey,
            "spark.cosmos.database" -> cosmosDatabase,
            "spark.cosmos.container" -> cosmosContainer,
            "spark.cosmos.read.runtimeFiltering.enabled" -> "true"
        )

        val spark = SparkSession.builder()
            .appName("spark connector sample")
            .master("local")
            .config("spark.sql.optimizer.dynamicPartitionPruning.reuseBroadcastOnly", false)
            .getOrCreate()

        LocalJavaFileSystem.applyToSparkSession(spark)

        // scalastyle:off underscore.import
        // scalastyle:off import.grouping
        // scalastyle:on underscore.import
        // scalastyle:on import.grouping

        val joinCfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
            "spark.cosmos.accountKey" -> cosmosMasterKey,
            "spark.cosmos.database" -> cosmosDatabase,
            "spark.cosmos.container" -> "GreenTaxiRecordsCFSink"
        )

        val dfWithLimit10 =
            spark
                .read
                .format("cosmos.oltp")
                .options(joinCfg)
                .load()
                .select("id", "vendorID")
                .limit(100)

        spark.udf.register("GetCosmosItemIdentityValue", new GetCosmosItemIdentityValue(), StringType)

        val dfWithCosmosIdentity =
            dfWithLimit10
                .withColumn("_itemIdentity", expr("GetCosmosItemIdentityValue(id, id)"))
                .withColumn("vendorIDInc", dfWithLimit10("vendorID") + 1)

        dfWithCosmosIdentity.createOrReplaceTempView("source")
        println(dfWithCosmosIdentity.queryExecution.logical.stats.sizeInBytes)

        val df = spark.read.format("cosmos.oltp").options(cfg).load()

        //        df.createOrReplaceTempView("cosmosView")
        println(df.queryExecution.logical.stats.sizeInBytes)

        val result = df.join(dfWithCosmosIdentity).where(df("_itemIdentity") === dfWithCosmosIdentity("_itemIdentity") && dfWithCosmosIdentity("vendorIDInc") >= 2).collect()
        //        val resultDf = spark.sql("select * from cosmosView as cosmosView join source as sourceView on cosmosView.id = sourceView.id where sourceView.vendorID = 2")
        //        resultDf.explain(true)
        //        resultDf.collect()

        println(result.size)
        spark.close()
    }
}
