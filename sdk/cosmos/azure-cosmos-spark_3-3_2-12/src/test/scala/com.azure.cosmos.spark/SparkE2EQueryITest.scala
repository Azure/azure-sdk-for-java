// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import com.fasterxml.jackson.databind.node.ObjectNode

import java.util.UUID

class SparkE2EQueryITest
    extends SparkE2EQueryITestBase {

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
}
