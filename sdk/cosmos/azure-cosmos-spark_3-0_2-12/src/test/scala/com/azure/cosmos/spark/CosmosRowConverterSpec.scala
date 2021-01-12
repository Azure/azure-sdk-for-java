// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.fasterxml.jackson.databind.node.ArrayNode
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{ArrayType, IntegerType, NullType, StringType, StructField, StructType}

class CosmosRowConverterSpec extends UnitSpec {
  //scalastyle:off null
  //scalastyle:off multiple.string.literals

  "basic spark row" should "translate to ObjectNode" in {

    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colVal1 = 8
    val colVal2 = "strVal"

    val row = new GenericRowWithSchema(
      Array(colVal1, colVal2),
      StructType(Seq(StructField(colName1, IntegerType), StructField(colName2, StringType))))

    val objectNode = CosmosRowConverter.rowToObjectNode(row)
    objectNode.get(colName1).asInt() shouldEqual colVal1
    objectNode.get(colName2).asText() shouldEqual colVal2
  }

  "null type in spark row" should "translate to null in ObjectNode" in {

    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colVal1 = null
    val colVal2 = "strVal"

    val row = new GenericRowWithSchema(
      Array(colVal1, colVal2),
      StructType(Seq(StructField(colName1, NullType), StructField(colName2, StringType))))

    val objectNode = CosmosRowConverter.rowToObjectNode(row)
    objectNode.get(colName1).isNull shouldBe true
    objectNode.get(colName2).asText() shouldEqual colVal2
  }

  "array in spark row" should "translate to null in ArrayNode" in {
    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colVal1 = "strVal"

    val row = new GenericRowWithSchema(
      Array(Seq("arrayElement1", "arrayElement2"), colVal1),
      StructType(Seq(StructField(colName1, ArrayType(StringType, true)), StructField(colName2, StringType))))

    val objectNode = CosmosRowConverter.rowToObjectNode(row)
    objectNode.get(colName1).isArray shouldBe true
    objectNode.get(colName1).asInstanceOf[ArrayNode] should have size 2
    objectNode.get(colName1).asInstanceOf[ArrayNode].get(0).asText() shouldEqual "arrayElement1"
    objectNode.get(colName1).asInstanceOf[ArrayNode].get(1).asText() shouldEqual "arrayElement2"

    objectNode.get(colName2).asText() shouldEqual colVal1
  }
  //scalastyle:on null
  //scalastyle:on multiple.string.literals
  // TODO moderakh add more tests for all primitive types, Map, List, Nested Type, ...
}
