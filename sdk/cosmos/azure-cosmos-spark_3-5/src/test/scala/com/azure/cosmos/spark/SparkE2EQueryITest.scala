// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.models.{CosmosContainerProperties, CosmosItemRequestOptions, PartitionKey, PartitionKeyBuilder, PartitionKeyDefinition, PartitionKeyDefinitionVersion, PartitionKind, ThroughputProperties}
import com.azure.cosmos.spark.udf.GetCosmosPartitionKeyValue
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.sql.functions.expr
import org.apache.spark.sql.types.StringType

import java.util.{ArrayList, UUID}

import scala.collection.JavaConverters._

class SparkE2EQueryITest
    extends SparkE2EQueryITestBase {

    // scalastyle:off multiple.string.literals
    "spark query" can "return proper Cosmos specific query plan on explain with nullable properties" in {
        val cosmosEndpoint = TestConfigurations.HOST
        val cosmosMasterKey = TestConfigurations.MASTER_KEY

        val id = UUID.randomUUID().toString

        val rawItem =
            s"""
               | {
               |   "id" : "$id",
               |   "nestedObject" : {
               |     "prop1" : 5,
               |     "prop2" : "6"
               |   }
               | }
               |""".stripMargin

        val objectNode = objectMapper.readValue(rawItem, classOf[ObjectNode])

        val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
        container.createItem(objectNode).block()

        val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
            "spark.cosmos.accountKey" -> cosmosMasterKey,
            "spark.cosmos.database" -> cosmosDatabase,
            "spark.cosmos.container" -> cosmosContainer,
            "spark.cosmos.read.inferSchema.forceNullableProperties" -> "true",
            "spark.cosmos.read.partitioning.strategy" -> "Restrictive"
        )

        val df = spark.read.format("cosmos.oltp").options(cfg).load()
        val rowsArray = df.where("nestedObject.prop2 = '6'").collect()
        rowsArray should have size 1

        var output = new java.io.ByteArrayOutputStream()
        Console.withOut(output) {
            df.explain()
        }
        var queryPlan = output.toString.replaceAll("#\\d+", "#x")
        logInfo(s"Query Plan: $queryPlan")
        queryPlan.contains("Cosmos Query: SELECT * FROM r") shouldEqual true

        output = new java.io.ByteArrayOutputStream()
        Console.withOut(output) {
            df.where("nestedObject.prop2 = '6'").explain()
        }
        queryPlan = output.toString.replaceAll("#\\d+", "#x")
        logInfo(s"Query Plan: $queryPlan")
        val expected = s"Cosmos Query: SELECT * FROM r WHERE (NOT(IS_NULL(r['nestedObject']['prop2'])) AND IS_DEFINED(r['nestedObject']['prop2'])) " +
            s"AND r['nestedObject']['prop2']=" +
            s"@param0${System.getProperty("line.separator")} > param: @param0 = 6"
        queryPlan.contains(expected) shouldEqual true

        val item = rowsArray(0)
        item.getAs[String]("id") shouldEqual id
    }

    "spark readManyByPartitionKeys" can "use a matching top-level partition key column without the UDF" in {
        val cosmosEndpoint = TestConfigurations.HOST
        val cosmosMasterKey = TestConfigurations.MASTER_KEY
        val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey)
        val requestOptions = new CosmosItemRequestOptions()

        Seq("pkA", "pkB").foreach { pkValue =>
            val item = objectMapper.createObjectNode()
            item.put("id", s"item-$pkValue")
            item.put("pk", pkValue)
            item.put("payload", s"value-$pkValue")

            container.createItem(item, new PartitionKey(pkValue), requestOptions).block()
        }

        val cfg = Map(
            "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
            "spark.cosmos.accountKey" -> cosmosMasterKey,
            "spark.cosmos.database" -> cosmosDatabase,
            "spark.cosmos.container" -> cosmosContainersWithPkAsPartitionKey,
            "spark.cosmos.read.inferSchema.enabled" -> "true"
        )

        val sparkSession = spark
        import sparkSession.implicits._

        val rows = CosmosItemsDataSource
            .readManyByPartitionKeys(Seq("pkA", "pkB").toDF("pk"), cfg.asJava)
            .selectExpr("id", "pk", "payload")
            .collect()

        rows should have size 2
        rows.map(_.getAs[String]("id")).toSet shouldEqual Set("item-pkA", "item-pkB")
        rows.map(_.getAs[String]("pk")).toSet shouldEqual Set("pkA", "pkB")
        rows.map(_.getAs[String]("payload")).toSet shouldEqual Set("value-pkA", "value-pkB")
    }

    "spark readManyByPartitionKeys" can "require the UDF for nested partition key paths and succeed with it" in {
        val cosmosEndpoint = TestConfigurations.HOST
        val cosmosMasterKey = TestConfigurations.MASTER_KEY
        val containerName = s"nested-pk-${UUID.randomUUID()}"

        val pkPaths = new ArrayList[String]()
        pkPaths.add("/tenant/id")

        val pkDefinition = new PartitionKeyDefinition()
        pkDefinition.setPaths(pkPaths)
        pkDefinition.setKind(PartitionKind.HASH)
        pkDefinition.setVersion(PartitionKeyDefinitionVersion.V2)

        val containerProperties = new CosmosContainerProperties(containerName, pkDefinition)
        cosmosClient
            .getDatabase(cosmosDatabase)
            .createContainerIfNotExists(containerProperties, ThroughputProperties.createManualThroughput(400))
            .block()

        try {
            val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(containerName)
            val requestOptions = new CosmosItemRequestOptions()

            Seq("tenantA", "tenantB").foreach { tenantId =>
                val item = objectMapper.createObjectNode()
                item.put("id", s"item-$tenantId")
                item.put("payload", s"value-$tenantId")
                item.putObject("tenant").put("id", tenantId)

                container.createItem(item, new PartitionKey(tenantId), requestOptions).block()
            }

            val cfg = Map(
                "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
                "spark.cosmos.accountKey" -> cosmosMasterKey,
                "spark.cosmos.database" -> cosmosDatabase,
                "spark.cosmos.container" -> containerName,
                "spark.cosmos.read.inferSchema.enabled" -> "true"
            )

            val sparkSession = spark
            import sparkSession.implicits._

            val missingUdfError = the[IllegalArgumentException] thrownBy {
                CosmosItemsDataSource.readManyByPartitionKeys(Seq("tenantA").toDF("tenantId"), cfg.asJava)
            }

            missingUdfError.getMessage should include("Nested paths cannot be resolved from DataFrame columns automatically")
            missingUdfError.getMessage should include("_partitionKeyIdentity")

            spark.udf.register("GetCosmosPartitionKeyValue", new GetCosmosPartitionKeyValue(), StringType)

            val inputDf = Seq("tenantA", "tenantB")
                .toDF("tenantId")
                .withColumn("_partitionKeyIdentity", expr("GetCosmosPartitionKeyValue(tenantId)"))

            val rows = CosmosItemsDataSource
                .readManyByPartitionKeys(inputDf, cfg.asJava)
                .selectExpr("id", "tenant.id as tenantId")
                .collect()

            rows should have size 2
            rows.map(_.getAs[String]("id")).toSet shouldEqual Set("item-tenantA", "item-tenantB")
            rows.map(_.getAs[String]("tenantId")).toSet shouldEqual Set("tenantA", "tenantB")
        } finally {
            cosmosClient
                .getDatabase(cosmosDatabase)
                .getContainer(containerName)
                .delete()
                .block()
        }
    }


    "spark readManyByPartitionKeys" can "support partial top-level hierarchical partition keys from DataFrame columns without the UDF" in {
        val cosmosEndpoint = TestConfigurations.HOST
        val cosmosMasterKey = TestConfigurations.MASTER_KEY
        val containerName = s"top-level-hpk-${UUID.randomUUID()}"

        val pkPaths = new ArrayList[String]()
        pkPaths.add("/tenant")
        pkPaths.add("/region")
        pkPaths.add("/team")

        val pkDefinition = new PartitionKeyDefinition()
        pkDefinition.setPaths(pkPaths)
        pkDefinition.setKind(PartitionKind.MULTI_HASH)
        pkDefinition.setVersion(PartitionKeyDefinitionVersion.V2)

        val containerProperties = new CosmosContainerProperties(containerName, pkDefinition)
        cosmosClient
            .getDatabase(cosmosDatabase)
            .createContainerIfNotExists(containerProperties, ThroughputProperties.createManualThroughput(400))
            .block()

        try {
            val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(containerName)
            val requestOptions = new CosmosItemRequestOptions()

            Seq(
                ("tenantA", "east", "sales", "item-a1"),
                ("tenantA", "west", "hr", "item-a2"),
                ("tenantB", "east", "sales", "item-b1")
            ).foreach { case (tenant, region, team, id) =>
                val item = objectMapper.createObjectNode()
                item.put("id", id)
                item.put("tenant", tenant)
                item.put("region", region)
                item.put("team", team)
                item.put("payload", s"$tenant-$region-$team")

                val pk = new PartitionKeyBuilder().add(tenant).add(region).add(team).build()
                container.createItem(item, pk, requestOptions).block()
            }

            val cfg = Map(
                "spark.cosmos.accountEndpoint" -> cosmosEndpoint,
                "spark.cosmos.accountKey" -> cosmosMasterKey,
                "spark.cosmos.database" -> cosmosDatabase,
                "spark.cosmos.container" -> containerName,
                "spark.cosmos.read.inferSchema.enabled" -> "true"
            )

            val sparkSession = spark
            import sparkSession.implicits._

            val tenantRows = CosmosItemsDataSource
                .readManyByPartitionKeys(Seq("tenantA").toDF("tenant"), cfg.asJava)
                .selectExpr("id", "tenant", "region", "team")
                .collect()

            tenantRows should have size 2
            tenantRows.map(_.getAs[String]("id")).toSet shouldEqual Set("item-a1", "item-a2")
            tenantRows.map(_.getAs[String]("tenant")).toSet shouldEqual Set("tenantA")

            val tenantRegionRows = CosmosItemsDataSource
                .readManyByPartitionKeys(Seq(("tenantA", "east")).toDF("tenant", "region"), cfg.asJava)
                .selectExpr("id", "tenant", "region", "team")
                .collect()

            tenantRegionRows should have size 1
            tenantRegionRows.head.getAs[String]("id") shouldEqual "item-a1"
        } finally {
            cosmosClient
                .getDatabase(cosmosDatabase)
                .getContainer(containerName)
                .delete()
                .block()
        }
    }

    // scalastyle:on multiple.string.literals
}
